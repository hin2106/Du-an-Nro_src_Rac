package boss.broly;

import boss.Boss;
import boss.BossData;
import boss.BossID;
import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import map.Zone;
import player.Player;
import services.SkillService;
import services.map.ChangeMapService;
import services.player.PlayerService;
import skill.Skill;
import utils.SkillUtil;
import utils.Util;

public class Broly extends Boss {

    private boolean isAggressive = false;
    
    // Aggro system - danh sách player bị boss tấn công
    private final Set<Long> aggroList = ConcurrentHashMap.newKeySet();
    private final Map<Long, Long> playerNearbyStartTime = new ConcurrentHashMap<>();
    private static final int AGGRO_RADIUS = 140; // pixel
    private static final long AGGRO_TIME_THRESHOLD = 3000; // 3 giây
    
    // Passive walking system - đi lại tự nhiên
    private long lastWalkTime = 0;
    private long nextWalkDelay = 0; // thời gian nghỉ trước khi đi tiếp
    private long lastWarningTime = 0;
    private static final long WARNING_INTERVAL = 15000; // cảnh báo mỗi 15 giây
    private int walkDirection = 1; // hướng đi: 1 = phải, -1 = trái
    private int walkStepsRemaining = 0; // số bước còn lại theo hướng hiện tại
    private boolean isResting = false; // đang nghỉ ngơi
    private long restStartTime = 0;
    private long restDuration = 0;
    
    // Idle healing system - dùng skill trong khoảng 3-5 giây
    private long idleHealStartTime = 0;
    private long idleHealDuration = 0; // thời gian sẽ dùng skill (3-5 giây)
    private boolean isIdleHealing = false;
    private long nextAttackDelay = 350;

    public Broly() throws Exception {
        super(BROLY, BossID.BROLY, new BossData(
                "Broly",
                ConstPlayer.XAYDA,
                new short[] { 291, 292, 293, -1, -1, -1 },
                100,
                new long[] { 1000 },
                new int[] { 5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38 },
                new int[][] {
                        { Skill.TAI_TAO_NANG_LUONG, 1, 3000 }, { Skill.TAI_TAO_NANG_LUONG, 2, 3000 },
                        { Skill.TAI_TAO_NANG_LUONG, 3, 3000 }, { Skill.TAI_TAO_NANG_LUONG, 4, 3000 },
                        { Skill.TAI_TAO_NANG_LUONG, 5, 3000 }, { Skill.TAI_TAO_NANG_LUONG, 6, 5000 },
                        { Skill.TAI_TAO_NANG_LUONG, 7, 10000 },
                        { Skill.DRAGON, 1, 3000 }, { Skill.DRAGON, 2, 3000 }, { Skill.DRAGON, 3, 3000 },
                        { Skill.DRAGON, 4, 3000 }, { Skill.DRAGON, 5, 3000 }, { Skill.DRAGON, 6, 3000 },
                        { Skill.DRAGON, 7, 3000 },
                        { Skill.DEMON, 1, 3000 }, { Skill.DEMON, 2, 3000 }, { Skill.DEMON, 3, 3000 },
                        { Skill.DEMON, 4, 3000 }, { Skill.DEMON, 5, 3000 }, { Skill.DEMON, 6, 3000 },
                        { Skill.DEMON, 7, 3000 },
                        { Skill.GALICK, 1, 3000 }, { Skill.GALICK, 2, 3000 }, { Skill.GALICK, 3, 3000 },
                        { Skill.GALICK, 4, 3000 }, { Skill.GALICK, 5, 3000 }, { Skill.GALICK, 6, 3000 },
                        { Skill.GALICK, 7, 3000 },
                        { Skill.KAMEJOKO, 1, 30000 }, { Skill.KAMEJOKO, 2, 15000 }, { Skill.KAMEJOKO, 3, 20000 },
                        { Skill.KAMEJOKO, 4, 30000 }, { Skill.KAMEJOKO, 5, 35000 }, { Skill.KAMEJOKO, 6, 40000 },
                        { Skill.MASENKO, 1, 3000 }, { Skill.MASENKO, 2, 3000 }, { Skill.MASENKO, 3, 3000 },
                        { Skill.MASENKO, 4, 3000 }, { Skill.MASENKO, 5, 3000 }, { Skill.MASENKO, 6, 3000 },
                        { Skill.MASENKO, 7, 3000 },
                        { Skill.ANTOMIC, 1, 2000 }, { Skill.ANTOMIC, 2, 3000 }, { Skill.ANTOMIC, 3, 3000 },
                        { Skill.ANTOMIC, 4, 3000 }, { Skill.ANTOMIC, 5, 3000 }, { Skill.ANTOMIC, 6, 3000 },
                        { Skill.ANTOMIC, 7, 30000 }, }, // skill
                new String[] {},
                new String[] { "|-1|Haha! ta sẽ giết hết các ngươi",
                        "|-1|Sức mạnh của ta là tuyệt đối",
                        "|-1|Vào hết đây!!!" },
                new String[] { "|-1|Các ngươi giỏi lắm. Ta sẽ quay lại." },
                600));
    }

    @Override
    public void active() {
        refreshAggroState();
        checkNearbyPlayers();
        refreshAggroState();
        if (this.isAggressive) {
            if (this.typePk != ConstPlayer.PK_ALL) {
                PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
            }
            attack();
        } else {
            if (this.typePk != ConstPlayer.NON_PK) {
                PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
            }
            passiveWalk();
            sayWarning();
            idleHeal();
        }
    }
    
    // Dùng skill tái tạo năng lượng trong khoảng 3-5 giây khi không có player để đánh
    private void idleHeal() {
        long currentTime = System.currentTimeMillis();
        
        if (!isIdleHealing) {
            // Bắt đầu chu kỳ heal mới - random thời gian 3-5 giây
            isIdleHealing = true;
            idleHealStartTime = currentTime;
            idleHealDuration = Util.nextInt(3000, 5000);
        }
        
        // Kiểm tra còn trong thời gian heal không
        if (currentTime - idleHealStartTime < idleHealDuration) {
                this.useHealSkill();
        } else {
            isIdleHealing = false;
        }
    }
    
    // Đi lại tự nhiên trong map
    private void passiveWalk() {
        if (this.zone == null || this.zone.map == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Đang nghỉ ngơi - đứng yên một lúc
        if (isResting) {
            if (currentTime - restStartTime >= restDuration) {
                isResting = false;
                // Sau khi nghỉ, random delay trước khi đi tiếp (1-3 giây)
                nextWalkDelay = Util.nextInt(1000, 3000);
                lastWalkTime = currentTime;
            }
            return;
        }
        
        // Chờ delay giữa các bước
        if (currentTime - lastWalkTime < nextWalkDelay) {
            return;
        }
        
        lastWalkTime = currentTime;
        
        // Nếu hết bước thì nghỉ một lúc rồi đổi hướng
        if (walkStepsRemaining <= 0) {
            // 40% cơ hội nghỉ ngơi (3-8 giây)
            if (Util.isTrue(40, 100)) {
                isResting = true;
                restStartTime = currentTime;
                restDuration = Util.nextInt(3000, 8000);
                return;
            }
            
            // Random hướng mới và số bước (3-8 bước)
            walkDirection = Util.getOne(-1, 1);
            walkStepsRemaining = Util.nextInt(3, 8);
        }
        
        // Đi từng bước (20-60 pixel)
        int stepSize = Util.nextInt(20, 60);
        int newX = this.location.x + (walkDirection * stepSize);
        
        // Kiểm tra biên map
        int mapWidth = this.zone.map.mapWidth;
        if (newX < 100 || newX > mapWidth - 100) {
            // Đổi hướng khi chạm biên, nghỉ một chút
            walkDirection = -walkDirection;
            isResting = true;
            restStartTime = currentTime;
            restDuration = Util.nextInt(1000, 2000);
            return;
        }
        
        // Tìm mặt đất tại vị trí mới
        int newY = this.zone.map.yPhysicInTop(newX, 0);
        
        // Di chuyển
        this.move(newX, newY);
        walkStepsRemaining--;
        
        // Delay giữa các bước (300-800ms - tốc độ đi bộ tự nhiên)
        nextWalkDelay = Util.nextInt(300, 800);
    }
    
    // Kiểm tra player và pet đứng gần trong phạm vi 140px
    private void checkNearbyPlayers() {
        if (this.zone == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        List<Player> players = this.zone.getPlayers();
        
        if (players == null) {
            return;
        }
        
        Set<Long> currentNearby = new HashSet<>();
        
        for (Player pl : players) {
            if (pl == null || pl.isDie() || pl.id == this.id) {
                continue;
            }
            
            // Kiểm tra khoảng cách player
            double distance = Util.getDistance(this, pl);
            if (distance <= AGGRO_RADIUS) {
                currentNearby.add(pl.id);
                
                if (!playerNearbyStartTime.containsKey(pl.id)) {
                    playerNearbyStartTime.put(pl.id, currentTime);
                } else {
                    long timeNearby = currentTime - playerNearbyStartTime.get(pl.id);
                    if (timeNearby >= AGGRO_TIME_THRESHOLD && !aggroList.contains(pl.id)) {
                        addToAggroList(pl);
                    }
                }
            }
            
            // Kiểm tra khoảng cách pet của player - nếu pet đứng gần thì đánh pet
            if (pl.pet != null && !pl.pet.isDie() && pl.pet.zone == this.zone) {
                double petDistance = Util.getDistance(this, pl.pet);
                if (petDistance <= AGGRO_RADIUS) {
                    currentNearby.add(pl.pet.id);
                    
                    if (!playerNearbyStartTime.containsKey(pl.pet.id)) {
                        playerNearbyStartTime.put(pl.pet.id, currentTime);
                    } else {
                        long timeNearby = currentTime - playerNearbyStartTime.get(pl.pet.id);
                        if (timeNearby >= AGGRO_TIME_THRESHOLD && !aggroList.contains(pl.pet.id)) {
                            // Add pet vào aggro list, không phải chủ
                            addToAggroList(pl.pet);
                        }
                    }
                }
            }
        }
        
        // Xóa player/pet đã ra khỏi phạm vi
        playerNearbyStartTime.keySet().removeIf(id -> !currentNearby.contains(id));
    }
    
    // Thêm player vào danh sách aggro
    private void addToAggroList(Player pl) {
        if (pl == null || aggroList.contains(pl.id)) {
            return;
        }
        aggroList.add(pl.id);
        this.isAggressive = true;
        this.chat("Mi làm ta nổi giận rồi " + pl.name);
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }
    
    // Nói cảnh báo định kỳ
    private void sayWarning() {
        if (Util.canDoWithTime(lastWarningTime, WARNING_INTERVAL) && !this.isAggressive) {
            lastWarningTime = System.currentTimeMillis();
            this.chat("Tránh xa ta ra. Đừng để ta nổi giận.");
        }
    }
    
    // Lấy player hoặc pet trong aggro list để tấn công
    private Player getAggroTarget() {
        if (this.zone == null || aggroList.isEmpty()) {
            return null;
        }
        
        List<Player> players = this.zone.getPlayers();
        if (players == null) {
            return null;
        }
        
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player pl : players) {
            if (pl != null && !pl.isDie() && aggroList.contains(pl.id)) {
                double distance = Util.getDistance(this, pl);
                if (distance < nearestDistance) {
                    nearest = pl;
                    nearestDistance = distance;
                }
            }
            if (pl != null && pl.pet != null && !pl.pet.isDie() && pl.pet.zone == this.zone && aggroList.contains(pl.pet.id)) {
                double distance = Util.getDistance(this, pl.pet);
                if (distance < nearestDistance) {
                    nearest = pl.pet;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    private void refreshAggroState() {
        if (this.zone == null) {
            aggroList.clear();
            playerNearbyStartTime.clear();
            isAggressive = false;
            return;
        }
        Set<Long> validTargets = new HashSet<>();
        List<Player> players = this.zone.getPlayers();
        if (players != null) {
            for (Player pl : players) {
                if (pl == null || pl.isDie() || pl.zone != this.zone) continue;
                validTargets.add(pl.id);
                if (pl.pet != null && !pl.pet.isDie() && pl.pet.zone == this.zone) {
                    validTargets.add(pl.pet.id);
                }
            }
        }
        aggroList.retainAll(validTargets);
        playerNearbyStartTime.keySet().retainAll(validTargets);
        if (aggroList.isEmpty() && isAggressive) {
            isAggressive = false;
            this.playerTarger = null;
            isResting = false;
            walkStepsRemaining = 0;
            lastWalkTime = System.currentTimeMillis();
            nextWalkDelay = Util.nextInt(600, 1600);
        }
    }

    @Override
    public void joinMap() {
        this.name = "Broly " + Util.nextInt(10, 100);
        // Khung giờ 18h-3h: HP từ 10k - 1tr
        // Ngoài khung giờ: HP từ 100 - 10k (như cũ)
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isPrimeTime = (hour >= 22 || hour < 3);

        if (isPrimeTime) {
            this.nPoint.hpMax = Util.nextInt(10_000, 1_000_001);
        } else {
            this.nPoint.hpMax = Util.nextInt(100, 10000);
        }

        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        this.nPoint.crit = Util.nextInt(50);
        this.isAggressive = false;
        
        // Reset aggro system khi join map
        this.aggroList.clear();
        this.playerNearbyStartTime.clear();
        this.lastWalkTime = System.currentTimeMillis();
        this.lastWarningTime = System.currentTimeMillis();
        this.isResting = false;
        this.walkStepsRemaining = 0;
        this.nextWalkDelay = Util.nextInt(2000, 5000); // delay ban đầu trước khi đi
        
        this.joinMap2();
    }

    public void joinMap2() {
        if (this.zone == null) {
            this.zone = (this.parentBoss != null) ? this.parentBoss.zone
                    : (this.lastZone != null) ? this.lastZone : getMapJoin();
        }

        if (this.zone != null) {
            try {
                int zoneid = Util.nextInt(2, this.zone.map.zones.size());
                while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
                    zoneid++;
                }

                if (zoneid < this.zone.map.zones.size()) {
                    this.zone = this.zone.map.zones.get(zoneid);
                } else if (this.id == BossID.BROLY) {
                    this.changeStatus(BossStatus.DIE);
                    return;
                } else {
                    this.zone = this.zone.map.zones.get(Util.nextInt(2, this.zone.map.zones.size()));
                }

                if (this.zone.zoneId < 2) {
                    this.leaveMap();
                }

                // Tìm vị trí spawn trên mặt đất
                int spawnX = Util.nextInt(100, 1000);
                int spawnY = this.zone.map.yPhysicInTop(spawnX, 0);
                
                ChangeMapService.gI().changeMap(this, this.zone, spawnX, spawnY);
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    private long lastTimeHeal = 0;
    private static final long HEAL_COOLDOWN = 3000;

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            // Thêm player tấn công vào aggro list
            if (plAtt != null && plAtt != this && !plAtt.isBoss && !aggroList.contains(plAtt.id)) {
                addToAggroList(plAtt);
            }
            
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            long currPercentHP = this.nPoint.getCurrPercentHP();
            boolean shouldHeal = false;
            int healChance = 30;

            if (currPercentHP <= 20) {
                healChance = 5;
                shouldHeal = Util.canDoWithTime(lastTimeHeal, HEAL_COOLDOWN);
            } else if (currPercentHP <= 30) {
                healChance = 8;
                shouldHeal = Util.canDoWithTime(lastTimeHeal, HEAL_COOLDOWN * 2);
            } else if (currPercentHP <= 50) {
                healChance = 15;
                shouldHeal = Util.canDoWithTime(lastTimeHeal, HEAL_COOLDOWN * 3);
            }

            if (shouldHeal && Util.isTrue(1, healChance)) {
                this.useHealSkill();
                lastTimeHeal = System.currentTimeMillis();
            } else if (Util.isTrue(1, 30)) {
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
                this.updateStats();
                SkillService.gI().useSkill(this, null, null, -1, null);
            }

            damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            if (!piercing && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                    && plAtt.playerSkill.skillSelect.template != null
                    && plAtt.playerSkill.skillSelect.template.id != Skill.TU_SAT
                    && damage > this.nPoint.hpMax / 100) {
                damage = this.nPoint.hpMax / 100;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(resolveKiller(plAtt));
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    private void useHealSkill() {
        try {
            if (this.playerSkill == null || this.playerSkill.skills == null || this.playerSkill.skills.isEmpty()) {
                return;
            }
            for (skill.Skill skill : this.playerSkill.skills) {
                if (skill != null && skill.template != null && skill.template.id == Skill.TAI_TAO_NANG_LUONG) {
                    // Set skillSelect TRƯỚC rồi mới check cooldown
                    this.playerSkill.skillSelect = skill;
                    if (SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            utils.Logger.error("Broly useHealSkill error: " + ex.getMessage() + "\n");
        }
    }

    @Override
    public void attack() {
        // Chỉ tấn công nếu đang aggressive và có player trong aggro list
        if (!this.isAggressive || aggroList.isEmpty()) {
            return;
        }
        
        if (Util.canDoWithTime(this.lastTimeAttack, nextAttackDelay) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            nextAttackDelay = Util.nextInt(320, 620);
            try {
                long currPercentHP = this.nPoint.getCurrPercentHP();
                if (currPercentHP <= 30 && Util.canDoWithTime(lastTimeHeal, HEAL_COOLDOWN) && Util.isTrue(1, 10)) {
                    this.useHealSkill();
                    lastTimeHeal = System.currentTimeMillis();
                    return;
                }

                // Chỉ tấn công player trong aggro list
                Player pl = getAggroTarget();
                if (pl == null || pl.isDie()) {
                    refreshAggroState();
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(7, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + Util.nextInt(-200, 200),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + Util.nextInt(-40, 40),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    if (Util.isTrue(1, 100)) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
                        this.updateStats();
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                utils.Logger.error("Broly attack error: " + ex.getMessage() + "\n");
            }
        }
    }
    
    @Override
    public void checkPlayerDie(Player player) {
        if (player != null && player.isDie()) {
            this.chat("Lần sau chừa nhé " + player.name);
            aggroList.remove(player.id);
            playerNearbyStartTime.remove(player.id);
            if (!player.isPet && player.pet != null) {
                aggroList.remove(player.pet.id);
                playerNearbyStartTime.remove(player.pet.id);
            }
            
            // Nếu hết player trong aggro list thì quay lại passive mode
            if (aggroList.isEmpty()) {
                this.isAggressive = false;
                this.playerTarger = null;
                this.isResting = false;
                this.walkStepsRemaining = 0;
                this.lastWalkTime = System.currentTimeMillis();
                this.nextWalkDelay = Util.nextInt(600, 1600);
                this.chat("Hmm... không còn ai dám chọc ta nữa sao?");
            }
        }
    }

    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.DIE);
    }

    private void updateStats() {
        long hpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(4, 10);
        this.nPoint.hpMax = Math.min(hpMax + hpMax / rand, 16_070_777);
        this.nPoint.dame = this.nPoint.hpMax / 10;
    }

    @Override
    public void leaveMap() {
        if (this.isDie()) {
            Zone currentZone = this.zone;
            int x = this.location.x;
            int y = this.location.y;

            try {
                if (currentZone == null) {
                    super.leaveMap();
                    return;
                }

                long brolyHpMax = this.nPoint.hpMax;
                if (brolyHpMax >= 1_500) {
                    synchronized (managers.boss.BrolyManager.gI()) {
                        if (currentZone.getBosses() != null) {
                            boolean hasSuperBroly = false;
                            for (Player boss : currentZone.getBosses()) {
                                if (boss != null && boss.id == BossID.SUPER_BROLY && !boss.isDie()) {
                                    hasSuperBroly = true;
                                    break;
                                }
                            }
                            if (hasSuperBroly) {
                                super.leaveMap();
                                return;
                            }
                        }

                        boolean hasSuperBrolyInManager = managers.boss.BrolyManager.gI().checkBosses(currentZone,
                                BossID.SUPER_BROLY);
                        if (hasSuperBrolyInManager) {
                            super.leaveMap();
                            return;
                        }

                        SuperBroly superBroly = null;
                        try {
                            superBroly = new SuperBroly(currentZone, x, y, brolyHpMax);

                            int superBrolyCountInZone = managers.boss.BrolyManager.gI()
                                    .countSuperBrolyInZone(currentZone);
                            if (superBrolyCountInZone > 1) {
                                Boss firstSuperBroly = managers.boss.BrolyManager.gI()
                                        .getFirstSuperBrolyInZone(currentZone);
                                if (firstSuperBroly != null && !firstSuperBroly.equals(superBroly)) {
                                    managers.boss.BrolyManager.gI().removeBoss(superBroly);
                                    superBroly.dispose();
                                    super.leaveMap();
                                    return;
                                }
                            }

                            superBroly.joinMap();
                            superBroly.changeStatus(BossStatus.CHAT_S);
                        } catch (Exception e) {
                            if (superBroly != null) {
                                try {
                                    managers.boss.BrolyManager.gI().removeBoss(superBroly);
                                    superBroly.dispose();
                                } catch (Exception ex) {
                                }
                            }
                            utils.Logger.error("Broly leaveMap create SuperBroly error: " + e.getMessage() + "\n");
                        }
                    }
                } else {
                    if (Util.isTrue(10, 100)) {
                        spawnSuperBrolyRandomMap(brolyHpMax);
                    }
                }
            } catch (Exception ex) {
                utils.Logger.error("Broly leaveMap spawn SuperBroly error: " + ex.getMessage() + "\n");
            }
        }
        super.leaveMap();
    }

    private void spawnSuperBrolyRandomMap(long brolyHpMax) {
        try {
            int[] brolyMaps = new int[] { 5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38 };
            int randomMapId = brolyMaps[Util.nextInt(brolyMaps.length)];
            map.Map targetMap = services.map.MapService.gI().getMapById(randomMapId);
            if (targetMap == null || targetMap.zones == null || targetMap.zones.isEmpty()) {
                return;
            }

            synchronized (managers.boss.BrolyManager.gI()) {
                List<Zone> availableZones = new ArrayList<>();
                for (Zone zone : targetMap.zones) {
                    if (zone == null) {
                        continue;
                    }

                    if (zone.zoneId < 2) {
                        continue;
                    }

                    if (zone.getBosses() != null) {
                        boolean hasSuperBroly = false;
                        for (Player boss : zone.getBosses()) {
                            if (boss != null && boss.id == BossID.SUPER_BROLY && !boss.isDie()) {
                                hasSuperBroly = true;
                                break;
                            }
                        }
                        if (hasSuperBroly) {
                            continue;
                        }
                    }

                    boolean hasSuperBroly = managers.boss.BrolyManager.gI().checkBosses(zone, BossID.SUPER_BROLY);
                    if (hasSuperBroly) {
                        continue;
                    }

                    availableZones.add(zone);
                }

                if (availableZones.isEmpty()) {
                    return;
                }

                Zone randomZone = availableZones.get(Util.nextInt(0, availableZones.size() - 1));

                boolean hasSuperBrolyInSelectedZone = managers.boss.BrolyManager.gI().checkBosses(randomZone,
                        BossID.SUPER_BROLY);
                if (hasSuperBrolyInSelectedZone) {
                    return;
                }

                if (randomZone.getBosses() != null) {
                    boolean hasSuperBroly = false;
                    for (Player boss : randomZone.getBosses()) {
                        if (boss != null && boss.id == BossID.SUPER_BROLY && !boss.isDie()) {
                            hasSuperBroly = true;
                            break;
                        }
                    }
                    if (hasSuperBroly) {
                        return;
                    }
                }

                int x = randomZone.map.mapWidth > 100 ? Util.nextInt(100, randomZone.map.mapWidth - 100)
                        : Util.nextInt(100);
                int y = randomZone.map.yPhysicInTop(x, 100);
                SuperBroly superBroly = new SuperBroly(randomZone, x, y, brolyHpMax);
                superBroly.joinMap();
                superBroly.changeStatus(BossStatus.CHAT_S);
            }
        } catch (Exception ex) {
            utils.Logger.error("Broly spawnSuperBrolyRandomMap error: " + ex.getMessage() + "\n");
        }
    }
}
