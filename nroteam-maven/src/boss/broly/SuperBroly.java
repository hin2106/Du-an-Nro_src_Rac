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
import managers.boss.BrolyManager;
import map.Zone;
import player.Pet;
import player.Player;
import services.ItemService;
import services.PetService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.player.PlayerService;
import skill.Skill;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public class SuperBroly extends Boss {
    private boolean isAggressive = false;
    private long minHpMaxFromBroly = 0;
    private int petGender = -1;
    
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
    private long lastHealCast = 0;
    private long nextAttackDelay = 280;

    public SuperBroly(Zone zone, int x, int y) throws Exception {
        this(zone, x, y, 0);
    }

    public SuperBroly(Zone zone, int x, int y, long minHpMax) throws Exception {

        super(BROLY, BossID.SUPER_BROLY, false, false, new BossData(
                "Super Broly",
                ConstPlayer.XAYDA,
                new short[] { 294, 295, 296, -1, -1, -1, -1, -1 },
                100,
                new long[] { 1_500_000L },
                new int[] { 5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38 },
                new int[][] {
                        { Skill.TAI_TAO_NANG_LUONG, 1, 1000 }, { Skill.TAI_TAO_NANG_LUONG, 2, 1000 },
                        { Skill.TAI_TAO_NANG_LUONG, 3, 1000 }, { Skill.TAI_TAO_NANG_LUONG, 4, 1000 },
                        { Skill.TAI_TAO_NANG_LUONG, 5, 1000 }, { Skill.TAI_TAO_NANG_LUONG, 6, 1000 },
                        { Skill.TAI_TAO_NANG_LUONG, 7, 1000 },
                        { Skill.DRAGON, 1, 1000 }, { Skill.DRAGON, 2, 1000 }, { Skill.DRAGON, 3, 1000 },
                        { Skill.DRAGON, 4, 1000 }, { Skill.DRAGON, 5, 1000 }, { Skill.DRAGON, 6, 1000 },
                        { Skill.DRAGON, 7, 1000 },
                        { Skill.DEMON, 1, 1000 }, { Skill.DEMON, 2, 1000 }, { Skill.DEMON, 3, 1000 },
                        { Skill.DEMON, 4, 1000 }, { Skill.DEMON, 5, 1000 }, { Skill.DEMON, 6, 1000 },
                        { Skill.DEMON, 7, 1000 },
                        { Skill.GALICK, 1, 1000 }, { Skill.GALICK, 2, 1000 }, { Skill.GALICK, 3, 1000 },
                        { Skill.GALICK, 4, 1000 }, { Skill.GALICK, 5, 1000 }, { Skill.GALICK, 6, 1000 },
                        { Skill.GALICK, 7, 1000 },
                        { Skill.KAMEJOKO, 1, 1000 }, { Skill.KAMEJOKO, 2, 1000 }, { Skill.KAMEJOKO, 3, 1000 },
                        { Skill.KAMEJOKO, 4, 1000 }, { Skill.KAMEJOKO, 5, 1000 }, { Skill.KAMEJOKO, 6, 1000 },
                        { Skill.KAMEJOKO, 7, 1000 },
                        { Skill.MASENKO, 1, 1000 }, { Skill.MASENKO, 2, 1000 }, { Skill.MASENKO, 3, 1000 },
                        { Skill.MASENKO, 4, 1000 }, { Skill.MASENKO, 5, 1000 }, { Skill.MASENKO, 6, 1000 },
                        { Skill.MASENKO, 7, 1000 },
                        { Skill.ANTOMIC, 1, 1000 }, { Skill.ANTOMIC, 2, 1000 }, { Skill.ANTOMIC, 3, 1000 },
                        { Skill.ANTOMIC, 4, 1000 }, { Skill.ANTOMIC, 5, 1000 }, { Skill.ANTOMIC, 6, 1000 },
                        { Skill.ANTOMIC, 7, 1000 }, }, // skill
                new String[] {}, // text chat 1
                new String[] { "|-1|Haha! ta sẽ giết hết các ngươi",
                        "|-1|Sức mạnh của ta là tuyệt đối",
                        "|-1|Vào hết đây!!!", },
                new String[] { "|-1|Các ngươi giỏi lắm. Ta sẽ quay lại." },
                600));
        this.currentLevel = 0;
        this.initBase();
        this.zone = zone;
        this.minHpMaxFromBroly = minHpMax;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill == null) {
            return;
        }

        if (plKill.pet != null) {
            return;
        }

        if (plKill.zone == null) {
            return;
        }

        if (plKill.isDie()) {
            return;
        }

        int rewardPetGender;
        if (this.pet != null && this.pet.gender >= 0 && this.pet.gender <= 2) {
            rewardPetGender = this.pet.gender;
        } else if (this.petGender >= 0 && this.petGender <= 2) {
            rewardPetGender = this.petGender;
        } else {
            rewardPetGender = Util.nextInt(0, 2);
        }

        try {
            PetService.gI().createNormalPet(plKill, rewardPetGender);

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    if (plKill != null && !plKill.isDie() && plKill.pet != null && !plKill.pet.isDie()) {
                        if (plKill.zone != null && (plKill.pet.zone == null || plKill.pet.zone != plKill.zone)) {
                            plKill.pet.joinMapMaster();
                        }
                        if (plKill.pet.zone != null && plKill.pet.zone == plKill.zone) {
                            Service.gI().point(plKill.pet);
                            plKill.pet.zone.load_Me_To_Another(plKill.pet);
                        }
                    }
                } catch (Exception e) {
                    Logger.error("SuperBroly reward pet joinMap error: " + e.getMessage() + "\n");
                }
            }).start();
        } catch (Exception e) {
            Logger.error("SuperBroly reward error: " + e.getMessage() + "\n");
        }
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
                lastHealCast = currentTime;
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
            nextWalkDelay = Util.nextInt(500, 1400);
        }
    }

    @Override
    public void joinMap() {
        this.name = "Super Broly " + Util.nextInt(10, 100);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isPrimeTime = (hour >= 18 || hour < 3);

        long randomHp;
        if (isPrimeTime) {
            randomHp = Util.nextInt(2_000_000, 16_070_778);
        } else {
            randomHp = Util.nextInt(1_000_000, 16_070_777);
        }

        if (this.minHpMaxFromBroly > 0) {
            this.nPoint.hpMax = Math.max(randomHp, this.minHpMaxFromBroly);
        } else {
            this.nPoint.hpMax = randomHp;
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

        if (this.zone != null) {
            map.Map currentMap = this.zone.map;
            if (currentMap == null || currentMap.zones == null || currentMap.zones.isEmpty()) {
                super.joinMap();
                return;
            }

            synchronized (BrolyManager.gI()) {
                List<Zone> preferredZones = new ArrayList<>();
                List<Zone> otherZones = new ArrayList<>();

                for (int i = 0; i < currentMap.zones.size(); i++) {
                    Zone zone = currentMap.zones.get(i);
                    if (zone == null) {
                        continue;
                    }

                    if (zone.zoneId < 2) {
                        continue;
                    }

                    if (zone.getBosses() != null) {
                        boolean hasSuperBroly = false;
                        boolean hasBroly = false;
                        for (Player boss : zone.getBosses()) {
                            if (boss != null && !boss.isDie() && !boss.equals(this)) {
                                if (boss.id == BossID.SUPER_BROLY) {
                                    hasSuperBroly = true;
                                }
                                if (boss.id == BossID.BROLY) {
                                    hasBroly = true;
                                }
                            }
                        }
                        if (hasSuperBroly || hasBroly) {
                            continue;
                        }
                    }

                    boolean hasSuperBrolyInManager = BrolyManager.gI().checkBosses(zone, BossID.SUPER_BROLY);
                    if (hasSuperBrolyInManager) {
                        continue;
                    }

                    boolean hasBrolyInManager = BrolyManager.gI().checkBosses(zone, BossID.BROLY);
                    if (hasBrolyInManager) {
                        continue;
                    }

                    if (i >= 0 && i <= 2) {
                        preferredZones.add(zone);
                    } else {
                        otherZones.add(zone);
                    }
                }

                Zone selectedZone = null;
                if (!preferredZones.isEmpty() && Util.isTrue(80, 100)) {
                    selectedZone = preferredZones.get(Util.nextInt(0, preferredZones.size() - 1));
                } else if (!otherZones.isEmpty()) {
                    selectedZone = otherZones.get(Util.nextInt(0, otherZones.size() - 1));
                } else if (!preferredZones.isEmpty()) {
                    selectedZone = preferredZones.get(Util.nextInt(0, preferredZones.size() - 1));
                }

                if (selectedZone != null) {
                    boolean hasSuperBrolyInSelectedZone = BrolyManager.gI().checkBosses(selectedZone,
                            BossID.SUPER_BROLY);
                    if (hasSuperBrolyInSelectedZone) {
                        super.joinMap();
                        return;
                    }

                    if (selectedZone.getBosses() != null) {
                        boolean hasSuperBroly = false;
                        boolean hasBroly = false;
                        for (Player boss : selectedZone.getBosses()) {
                            if (boss != null && !boss.isDie() && !boss.equals(this)) {
                                if (boss.id == BossID.SUPER_BROLY) {
                                    hasSuperBroly = true;
                                }
                                if (boss.id == BossID.BROLY) {
                                    hasBroly = true;
                                }
                            }
                        }
                        if (hasSuperBroly || hasBroly) {
                            super.joinMap();
                            return;
                        }
                    }

                    // Tìm vị trí spawn trên mặt đất - ngẫu nhiên trong map
                    int x = Util.nextInt(100, 1000);
                    int y = selectedZone.map.yPhysicInTop(x, 0);
                    ChangeMapService.gI().changeMap(this, selectedZone, x, y);
                    this.changeStatus(BossStatus.CHAT_S);
                    this.notifyJoinMap();
                } else {
                    super.joinMap();
                }
            }
        } else {
            super.joinMap();
        }

        if (this.zone != null && this.zone.map != null) {
            try {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        if (this.zone != null && this.zone.map != null && this.pet == null) {
                            createDisciplePetForBoss();
                        }
                    } catch (Exception e) {
                        Logger.error("SuperBroly joinMap create Pet thread error: " + e.getMessage() + "\n");
                    }
                }).start();
            } catch (Exception e) {
                Logger.error("SuperBroly joinMap create Pet error: " + e.getMessage() + "\n");
            }
        }
    }

    private long lastTimeHeal = 0;
    private static final long HEAL_COOLDOWN = 3000;
    private boolean lastChuongAttack = false;
    private long lastChuongChatTime = 0;

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

            int skillId = -1;
            if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                    && plAtt.playerSkill.skillSelect.template != null) {
                skillId = plAtt.playerSkill.skillSelect.template.id;
            }

            boolean isChuong = (skillId == Skill.KAMEJOKO || skillId == Skill.ANTOMIC
                    || skillId == Skill.MASENKO || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN);

            if (isChuong) {
                damage = 1;
                if (plAtt != null && !plAtt.isBoss && !plAtt.isPet
                        && (!lastChuongAttack || Util.canDoWithTime(lastChuongChatTime, 5000))) {
                    Service.gI().chat(plAtt, "Trời chơi chưởng lực hoàn toàn vô dụng với hắn");
                    lastChuongChatTime = System.currentTimeMillis();
                }
                lastChuongAttack = true;
            } else {
                lastChuongAttack = false;
                damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);

                if (skillId == Skill.QUA_CAU_KENH_KHI) {
                    damage = damage / 4;
                } else if (skillId == Skill.MAKANKOSAPPO) {
                    damage = damage / 3;
                } else {
                    if (!piercing && damage > this.nPoint.hpMax / 100) {
                        damage = this.nPoint.hpMax / 100;
                    }
                }
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
                int skillsSize = this.playerSkill.skills.size();
                if (skillsSize > 0) {
                    int maxIndex = Math.min(6, skillsSize - 1);
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, maxIndex));
                    this.tangChiSo();
                    SkillService.gI().useSkill(this, null, null, -1, null);
                }
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

    @Override
    public void attack() {
        // Chỉ tấn công nếu đang aggressive và có player trong aggro list
        if (!this.isAggressive || aggroList.isEmpty()) {
            return;
        }
        
        if (Util.canDoWithTime(this.lastTimeAttack, nextAttackDelay) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            nextAttackDelay = Util.nextInt(240, 500);
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
                int skillsSize = this.playerSkill.skills.size();
                if (skillsSize == 0) {
                    return;
                }
                if (skillsSize <= 7) {
                    int maxIndex = Math.max(0, skillsSize - 1);
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, maxIndex));
                } else {
                    int maxIndex = Math.max(7, skillsSize - 1);
                    if (maxIndex >= 7) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(7, maxIndex));
                    } else {
                        maxIndex = Math.max(0, skillsSize - 1);
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, maxIndex));
                    }
                }
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    if (Util.isTrue(1, 100)) {
                        if (skillsSize > 0) {
                            int maxIndex = Math.min(6, skillsSize - 1);
                            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, maxIndex));
                            this.tangChiSo();
                        }
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                Logger.error("SuperBroly attack error: " + ex.getMessage() + "\n");
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
                this.nextWalkDelay = Util.nextInt(500, 1400);
                this.chat("Hmm... không còn ai dám chọc ta nữa sao?");
            }
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
            Logger.error("SuperBroly useHealSkill error: " + ex.getMessage() + "\n");
        }
    }

    private void tangChiSo() {
        long hpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(80, 100);
        hpMax = hpMax + hpMax / rand < 16_070_777 ? hpMax + hpMax / rand : 16_070_777;
        this.nPoint.hpMax = hpMax;
        this.nPoint.dame = hpMax / 10;
    }

    private long stAutoLeave;

    @Override
    public void autoLeaveMap() {
        if (stAutoLeave == 0) {
            stAutoLeave = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(stAutoLeave, 900000)) {
            super.leaveMap();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            stAutoLeave = System.currentTimeMillis();
        }
    }

    private void createDisciplePetForBoss() {
        if (this.pet != null) {
            return;
        }

        if (this.zone == null || this.zone.map == null) {
            return;
        }

        if (this.location == null) {
            return;
        }

        try {
            int[] data = getDataPetNormal();
            Pet pet = new Pet(this);

            this.petGender = Util.nextInt(0, 2);
            pet.name = "$Đệ tử";
            pet.typePet = 0;
            pet.gender = (byte) this.petGender;
            pet.status = Pet.FOLLOW;

            pet.id = -Math.abs((int) this.id) - 200000;
            pet.nPoint.power = 2000;
            pet.nPoint.stamina = 1000;
            pet.nPoint.maxStamina = 1000;
            pet.nPoint.hpg = data[0];
            pet.nPoint.mpg = data[1];
            pet.nPoint.dameg = data[2];
            pet.nPoint.defg = data[3];
            pet.nPoint.critg = data[4];

            // Pet chỉ có 7 ô trang bị (index 0-6), không có index 7
            if (pet.inventory.itemsBody == null) {
                pet.inventory.itemsBody = new ArrayList<>();
            }
            pet.inventory.itemsBody.clear();
            for (int i = 0; i < 7; i++) {
                pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
            }
            // Đảm bảo chính xác 7 ô (index 0-6)
            if (pet.inventory.itemsBody.size() != 7) {
                Logger.warning("SuperBroly createDisciplePetForBoss: Pet itemsBody size = "
                        + pet.inventory.itemsBody.size() + ", expected 7. Fixing...");
                while (pet.inventory.itemsBody.size() < 7) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                while (pet.inventory.itemsBody.size() > 7) {
                    pet.inventory.itemsBody.remove(pet.inventory.itemsBody.size() - 1);
                }
            }
            // Đảm bảo pet luôn có đủ 8 skills (index 0-7) để tránh lỗi IndexOutOfBounds
            pet.playerSkill.skills.clear();

            // Thêm skill đầu tiên
            Skill firstSkill = SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1);
            if (firstSkill != null) {
                pet.playerSkill.skills.add(firstSkill);
            } else {
                pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
            }

            // Thêm 7 skills nữa để tổng cộng có 8 skills
            for (int i = 0; i < 7; i++) {
                pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
            }

            // Double check: Đảm bảo chắc chắn có đủ 8 skills
            while (pet.playerSkill.skills.size() < 8) {
                pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
            }

            // Safety check: Nếu vẫn không đủ 8 skills (trường hợp bất thường), thêm cho đủ
            if (pet.playerSkill.skills.size() != 8) {
                Logger.warning("SuperBroly createDisciplePetForBoss: Pet skills size = " + pet.playerSkill.skills.size()
                        + ", expected 8. Fixing...");
                while (pet.playerSkill.skills.size() < 8) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                // Nếu nhiều hơn 8, xóa bớt (giữ lại 8 skills đầu)
                while (pet.playerSkill.skills.size() > 8) {
                    pet.playerSkill.skills.remove(pet.playerSkill.skills.size() - 1);
                }
            }

            if (pet.inventory.itemsBody.size() != 7) {
                Logger.warning("SuperBroly createDisciplePetForBoss: itemsBody size mismatch before calPoint: "
                        + pet.inventory.itemsBody.size() + ", expected 7. Fixing...");
                while (pet.inventory.itemsBody.size() < 7) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                while (pet.inventory.itemsBody.size() > 7) {
                    pet.inventory.itemsBody.remove(pet.inventory.itemsBody.size() - 1);
                }
            }
            if (pet.playerSkill.skills.size() != 8) {
                Logger.warning("SuperBroly createDisciplePetForBoss: skills size mismatch before calPoint: "
                        + pet.playerSkill.skills.size() + ", expected 8. Fixing...");
                while (pet.playerSkill.skills.size() < 8) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                while (pet.playerSkill.skills.size() > 8) {
                    pet.playerSkill.skills.remove(pet.playerSkill.skills.size() - 1);
                }
            }

            pet.nPoint.setFullHpMp();
            pet.nPoint.calPoint();

            if (this.location != null) {
                pet.location.x = this.location.x + Util.nextInt(-30, 30);
                pet.location.y = this.location.y;
            }

            this.pet = pet;

            if (this.zone != null && this.zone.map != null && this.location != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        if (this.pet != null && !this.pet.isDie() && this.zone != null && this.zone.map != null
                                && this.location != null && this.pet.location != null && this.pet.master != null
                                && this.pet.master.zone != null && this.pet.master.zone.map != null) {
                            if (this.pet.zone == null || this.pet.zone != this.zone) {
                                try {
                                    ChangeMapService.gI().goToMap(this.pet, this.zone);
                                    if (this.pet.zone != null) {
                                        this.pet.zone.load_Me_To_Another(this.pet);
                                    }
                                } catch (Exception ex) {
                                    Logger.error("SuperBroly createDisciplePetForBoss goToMap error: " + ex.getMessage()
                                            + "\n");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.error(
                                "SuperBroly createDisciplePetForBoss goToMap thread error: " + e.getMessage() + "\n");
                    }
                }).start();
            }
        } catch (IndexOutOfBoundsException e) {
            Logger.error("SuperBroly createDisciplePetForBoss IndexOutOfBounds error: " + e.getMessage()
                    + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace()) + "\n");
            if (this.pet != null) {
                try {
                    int skillsSize = this.pet.playerSkill != null && this.pet.playerSkill.skills != null
                            ? this.pet.playerSkill.skills.size()
                            : -1;
                    int itemsBodySize = this.pet.inventory != null && this.pet.inventory.itemsBody != null
                            ? this.pet.inventory.itemsBody.size()
                            : -1;
                    Logger.error("Pet state when error: skillsSize=" + skillsSize + ", itemsBodySize=" + itemsBodySize
                            + "\n");
                    this.pet.dispose();
                    this.pet = null;
                } catch (Exception ex) {
                    Logger.error("Error disposing pet: " + ex.getMessage() + "\n");
                }
            }
        } catch (Exception e) {
            Logger.error("SuperBroly createDisciplePetForBoss error: " + e.getMessage()
                    + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace()) + "\n");
            if (this.pet != null) {
                try {
                    this.pet.dispose();
                    this.pet = null;
                } catch (Exception ex) {
                    // Ignore dispose error
                }
            }
        }
    }

    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20;
        petData[1] = Util.nextInt(40, 105) * 20;
        petData[2] = Util.nextInt(20, 45);
        petData[3] = Util.nextInt(9, 50);
        petData[4] = Util.nextInt(0, 2);
        return petData;
    }

    @Override
    public void leaveMap() {
        if (this.pet != null) {
            try {
                ChangeMapService.gI().exitMap(this.pet);
                this.pet.dispose();
                this.pet = null;
            } catch (Exception e) {
                Logger.error("SuperBroly leaveMap dispose pet error: " + e.getMessage() + "\n");
            }
        }
        super.leaveMap();
        BrolyManager.gI().removeBoss(this);
        this.dispose();
    }
}
