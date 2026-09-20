
package boss.miniboss;



import boss.BossID;
import consts.BossStatus;
import managers.boss.BossManager;
import boss.*;
import consts.BossType;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import player.Player;
import services.map.ChangeMapService;
import services.Service;
import skill.Skill;
import task.BadgesTaskService;
import utils.Logger;
import utils.Util;
import item.Item;

public class XinBaTo extends Boss {

    private long timeSpawn;
    private int timeLeave;
    private boolean hasDrunkWater = false;
    private long timeDrunkWater = 0;
    private long timeLeaveDelay = 0;

    public XinBaTo() throws Exception {
        super(BossType.ANTROM,BossID.XIN_BA_TO, new BossData(
                "Xinbatô " + Util.nextInt(1, 49),
                ConstPlayer.TRAI_DAT,
                new short[]{359, 360, 361, -1, -1, -1},
                1000,
                new long[]{500000},
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 79, 80, 81, 82, 83, 84, 92, 93, 94, 96, 97, 98, 99, 100, 102, 103, 104, 105, 106, 107, 108, 109, 110},
                new int[][]{
                    {Skill.DRAGON, 7, 10000}},
                                 new String[]{},
                 new String[]{"|-1|Đường ống nước quê tôi bị vỡ, dân làng sắp chết khát mất!", "|-1|Cho tôi xin ít nước! Làm on!"},
                 new String[]{"|-1|Đường ống nước quê tôi bị vỡ, dân làng sắp chết khát mất!", "|-1|Dân làng tôi sắp chết khát mất!"},
                600000));
    }


    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.NUOC_ANH_BAO, 1);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.changeStatus(BossStatus.CHAT_S);
            this.hasDrunkWater = false;
            this.timeDrunkWater = 0;
            return;
        }

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
                List<Zone> availableZones = new ArrayList<>();
                for (Zone zone : this.zone.map.zones) {
                    if (zone.getNumOfPlayers() <= 10 && !BossManager.gI().checkBosses(zone, BossID.XIN_BA_TO)) {
                        availableZones.add(zone);
                    }
                }

                if (!availableZones.isEmpty()) {
                    int randomIndex = Util.nextInt(availableZones.size());
                    this.zone = availableZones.get(randomIndex);
                    ChangeMapService.gI().changeMap(this, this.zone,
                            Util.nextInt(100, 500),
                            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24));
                    this.changeStatus(BossStatus.CHAT_S);
                    timeSpawn = System.currentTimeMillis();
                    timeLeave = Util.nextInt(100000, 300000);
                } else {
                    this.leaveMapNew();
                }
            } catch (Exception e) {
                Logger.error(this.data[0].getName() + ": Lỗi đang tiến hành REST\n" + e.getMessage());
                this.changeStatus(BossStatus.REST);
            }
        } else {
            Logger.error(this.data[0].getName() + ": Lỗi map đang tiến hành RESPAWN\n");
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void chatM() {
        if (this.data[this.currentLevel].getTextM().length == 0) return;
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) return;

        try {
            String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            this.chat(prefix, textChat);
            this.lastTimeChatM = System.currentTimeMillis();
            this.timeChatM = 5000; // Chat mỗi 5 giây
            
            // Kiểm tra người chơi gần nhất và gửi thông báo
            Player nearestPlayer = getNearestPlayer();
            if (nearestPlayer != null) {
                checkPlayerWater(nearestPlayer);
            }
        } catch (Exception e) {
            Logger.error("Lỗi trong chatM XinBaTo: " + e.getMessage());
        }
    }
    
    private Player getNearestPlayer() {
        Player nearestPlayer = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : this.zone.getPlayers()) {
            if (player != null && player.location != null) {
                double distance = Util.getDistance(this, player);
                if (distance < minDistance && distance <= 200) {
                    minDistance = distance;
                    nearestPlayer = player;
                }
            }
        }
        return nearestPlayer;
    }
    
    private void checkPlayerWater(Player player) {
        if (player == null) return;
        
        int waterCount = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == 456) {
                waterCount += item.quantity;
            }
        }
        
        if (waterCount < 99) {
            Service.gI().sendThongBao(player, "Mau tìm cho xinbato x99 bình nước!");
        } else {
            Service.gI().sendThongBao(player, "Mau đem x99 bình nước cho xinbato!");
        }
    }

    @Override
    public void active() {
        this.attack();
        this.chatM();
        this.autoLeaveMap();
    }
    
    @Override
    public void update() {
        super.update();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(timeSpawn, timeLeave)) {
            this.leaveMapNew();
        }
        if (timeLeaveDelay > 0 && Util.canDoWithTime(timeLeaveDelay, 20000)) {
            timeLeaveDelay = 0;
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

    public void drinkWater() {
        if (!hasDrunkWater) {
            hasDrunkWater = true;
            timeDrunkWater = System.currentTimeMillis();
            timeLeaveDelay = System.currentTimeMillis();
            try {
                this.chat(-1, "Cảm ơn bạn đã cho tôi uống nước!");
            } catch (Exception e) {
                Logger.error("Lỗi khi boss cảm ơn: " + e.getMessage());
            }
            this.lastTimeChatM = System.currentTimeMillis() + 999999999;
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                this.leaveMapNew();
            }).start();
        }
    }

    public boolean hasDrunkWater() {
        return hasDrunkWater;
    }

    @Override
    public void die(Player plKill) {
        this.executeReward(plKill);
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.location == null) return;

                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20) && Util.getDistance(this, pl) > 50) {
                        if (Util.isTrue(5, 20)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)), pl.location.y);
                        }
                    }
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }

                if (timeDrunkWater > 0 && Util.canDoWithTime(timeDrunkWater, 5000)) {
                    timeDrunkWater = 0;
                    hasDrunkWater = false;
                }
            } catch (Exception ex) {
                Logger.error("Lỗi attack XinBaTo: " + ex.getMessage());
            }
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }
}
