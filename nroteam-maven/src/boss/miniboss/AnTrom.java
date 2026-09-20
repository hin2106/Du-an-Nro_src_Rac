package boss.miniboss;

import boss.Boss;
import boss.BossData;
import boss.BossID;
import consts.BossStatus;
import consts.BossType;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import map.ItemMap;
import map.Zone;
import player.Player;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.PlayerService;
import skill.Skill;
import task.BadgesTaskService;
import utils.Util;

public class AnTrom extends Boss {

    private long goldAnTrom;
    private long lastTimeAnTrom;
    private long st;
    private boolean isEscaping;
    private long lastTimeEscape;
    private long lastTimeAttacked; // Thời gian bị tấn công cuối cùng
    private int moveDirection; // Hướng di chuyển: 1 = phải, -1 = trái

    public AnTrom() throws Exception {
        super(BossType.ANTROM, BossID.AN_TROM, new BossData(
                "Ăn Trộm",
                ConstPlayer.TRAI_DAT,
                new short[] { 201, 202, 203, -1, -1, -1 },
                1,
                new long[] { 100 },
                new int[] { 5, 7, 0, 14 },
                new int[][] {
                        { Skill.THAI_DUONG_HA_SAN, 7, 50000 } },
                new String[] { "|-1|Tới giờ làm việc, lụm lụm", "|-1|Cảm giác mình vào phải khu người nghèo :))" },
                new String[] { "|-1|Ái chà vàng vàng", "|-1|Không làm vẫn có ăn :))",
                        "|-2|Giám ăn trộm giữa ban ngày thế à", "|-2|Cút ngay không là ăn đòn" },
                new String[] { "|-1|Híc lần sau ta sẽ cho ngươi phá sản", "|-2|Chừa thói ăn trộm nghe chưa" },
                600));
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].mapJoin[Util.nextInt(0,
                this.data[this.currentLevel].mapJoin.length - 1)];
        return MapService.gI().getMapById(mapId).zones.get(0);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        damage = 1;
        this.nPoint.subHP(damage);
        if (!this.isEscaping) {
            this.isEscaping = true;
            this.lastTimeEscape = System.currentTimeMillis();
        }
        this.lastTimeAttacked = System.currentTimeMillis();
        if (goldAnTrom >= 2_000_000 && Util.isTrue(20, 100)) {
            int dropAmount = (int) Math.min(50000, goldAnTrom);
            goldAnTrom -= dropAmount;
            ItemMap goldDrop = new ItemMap(this.zone, 190, dropAmount,
                    this.location.x + Util.nextInt(-20, 20),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plAtt.id);
            Service.gI().dropItemMap(this.zone, goldDrop);
        }

        if (isDie()) {
            this.setDie(plAtt);
            die(resolveKiller(plAtt));
        } else {
            if (this.playerSkill != null && this.playerSkill.skills != null && !this.playerSkill.skills.isEmpty()) {
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                SkillService.gI().useSkill(this, plAtt, null, 0, null);
            }
        }
        return (int) damage;
    }

    // Di chuyển an toàn tránh bug xuyên tường với tự động đổi hướng
    private void moveToSafe(int targetY) {
        // Kiểm tra nếu chạm giới hạn thì đổi hướng
        if (this.location.x <= 100) {
            moveDirection = 1; // Đổi sang di chuyển sang phải
        } else if (this.location.x >= this.zone.map.mapWidth - 100) {
            moveDirection = -1; // Đổi sang di chuyển sang trái
        }

        // Nếu chưa có hướng, chọn ngẫu nhiên
        if (moveDirection == 0) {
            moveDirection = Util.isTrue(1, 2) ? 1 : -1;
        }

        // Di chuyển theo hướng đã chọn
        int moveDistance = Util.nextInt(40, 60);
        int newX = this.location.x + (moveDirection * moveDistance);

        // Giới hạn vị trí trong map
        if (newX < 50) {
            newX = 50;
            moveDirection = 1; // Đổi hướng sang phải
        }
        if (newX > this.zone.map.mapWidth - 50) {
            newX = this.zone.map.mapWidth - 50;
            moveDirection = -1; // Đổi hướng sang trái
        }

        PlayerService.gI().playerMove(this, newX, targetY);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                if (this.isEscaping) {
                    // Kiểm tra nếu đã lâu không bị tấn công (15 giây) thì thoát chế độ chạy trốn
                    if (Util.canDoWithTime(this.lastTimeAttacked, 15000)) {
                        this.isEscaping = false;
                        Service.gI().chat(this, "Hehe an toàn rồi, tiếp tục kiếm tiền thôi!");
                        return;
                    }
                    Player pl = this.getPlayerAttack();
                    if (pl != null && !pl.isDie()) {
                        // Nếu gần người chơi, cố gắng chạy ngược hướng
                        if (Util.getDistance(this, pl) < 150) {
                            int dirX = this.location.x - pl.location.x > 0 ? 1 : -1;
                            // Cập nhật hướng di chuyển để chạy xa người chơi
                            moveDirection = dirX;
                        }
                        // Di chuyển tự động, sẽ đổi hướng khi chạm giới hạn
                        this.moveToSafe(this.location.y);

                        // Tiếp tục sử dụng Thái Dương Hạ San sau khi hồi skill
                        if (Util.canDoWithTime(this.lastTimeEscape, 8000)) { // 8 giây cooldown
                            this.lastTimeEscape = System.currentTimeMillis();
                            if (this.playerSkill != null && this.playerSkill.skills != null
                                    && !this.playerSkill.skills.isEmpty()) {
                                this.playerSkill.skillSelect = this.playerSkill.skills
                                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                                SkillService.gI().useSkill(this, pl, null, 0, null);
                            }
                        }
                    }
                    return;
                }
                // Cơ chế ăn trộm bình thường
                Player pl = this.getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                if (Util.getDistance(this, pl) <= 40) {
                    if (!Util.canDoWithTime(this.lastTimeAnTrom, Util.nextInt(1000, 3500))) { // 5 giây mỗi lần trộm
                        return;
                    }
                    if (pl.isPl() && pl.inventory.gold > 0) {
                        // Ăn trộm 0.3% tổng vàng người chơi hiện có (giảm từ 1% xuống 0.3%)
                        long gold = (long) (pl.inventory.gold * 0.002);

                        if (gold > 0) {
                            if (gold > Integer.MAX_VALUE) {
                                gold = Integer.MAX_VALUE;
                            }

                            pl.inventory.gold -= gold;
                            goldAnTrom += gold;
                            Service.gI().stealMoney(pl, -(int) gold);
                            PlayerService.gI().sendInfoHpMpMoney(pl);

                            // Hiệu ứng ăn trộm
                            ItemMap itemMap = new ItemMap(this.zone, 190, (int) gold,
                                    (this.location.x + pl.location.x) / 2,
                                    this.location.y, this.id);
                            Service.gI().sendToAntherMePickItem(this, itemMap.itemMapId);
                            this.zone.removeItemMap(itemMap);

                            String goldText = Util.numberToMoney(goldAnTrom);
                            Service.gI().chat(this, "Chôm được " + goldText + " vàng rồi ha ha ha");

                            this.lastTimeAnTrom = System.currentTimeMillis();
                        }
                    }
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(30, 40);
        PlayerService.gI().playerMove(this,
                this.location.x + (dir == 1 ? move : -move), y);
    }

    @Override
    public void die(Player plKill) {
        this.executeReward(plKill);
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.BI_MOC_SACH_TUI, 1);

        if (goldAnTrom > 0) {
            int dropPercent = Util.nextInt(30, 50);
            long goldDrop = goldAnTrom * dropPercent / 100;

            // Giới hạn 5-8 cục vàng để tránh lag server
            int numPiles = Util.nextInt(8, 13);
            long goldPerPile = goldDrop / numPiles;

            if (goldPerPile > 0) {
                for (int i = 0; i < numPiles; i++) {
                    long currentGold = goldPerPile;
                    if (currentGold > Integer.MAX_VALUE) {
                        currentGold = Integer.MAX_VALUE;
                    }

                    ItemMap it = new ItemMap(this.zone, 190, (int) currentGold,
                            this.location.x + Util.nextInt(-40, 40),
                            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                            plKill.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
            }
            String goldText = Util.numberToMoney(goldDrop);
            Service.gI().chat(this, "Chết rồi... mất " + goldText + " vàng (" + dropPercent + "%)...");
        } else {
            int totalGold = Util.nextInt(50000, 100000);
            ItemMap it = new ItemMap(this.zone, 190, totalGold,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);
            Service.gI().dropItemMap(this.zone, it);

            Service.gI().chat(this, "Thua rồi... chưa kịp trộm được gì...");
        }
        Service.gI().dropItemMap(this.zone,
                new ItemMap(zone, 1194, 1, this.location.x, this.location.y, plKill.id));
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
        if (Util.canDoWithTime(st, 900_000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void joinMap() {
        this.name = "Ăn Trộm " + Util.nextInt(1, 49);
        this.nPoint.hpMax = 100;
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dameg = this.nPoint.hpMax / 10;
        goldAnTrom = 0;
        isEscaping = false;
        lastTimeEscape = 0;
        lastTimeAttacked = 0;
        this.joinMap2();
        st = System.currentTimeMillis();
    }

    public void joinMap2() {
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone != null) {
            try {
                int zoneid = 0;
                this.zone = this.zone.map.zones.get(zoneid);
                ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
