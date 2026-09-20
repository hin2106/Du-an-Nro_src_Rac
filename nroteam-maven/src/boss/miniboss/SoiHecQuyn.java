package boss.miniboss;

import boss.BossID;
import consts.BossStatus;
import managers.boss.BossManager;
import boss.*;
import consts.BossType;
import consts.ConstTaskBadges;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import player.Player;
import services.map.ChangeMapService;
import task.BadgesTaskService;
import utils.Logger;
import utils.Util;

public class SoiHecQuyn extends Boss {

    private long timeSpawn;
    private int timeLeave;
    private boolean hasPickedBone = false;
    private long timePickedBone = 0;
    private long timeLeaveDelay = 0;

    public SoiHecQuyn() throws Exception {
        super(BossType.ANTROM,BossID.SOI_HEC_QUYN1, BossesData.SOI_HEC_QUYN);
    }
    
    @Override
    public void reward(Player plKill) {
         BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.KE_THAO_TUNG_SOI, 1);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.changeStatus(BossStatus.CHAT_S);
            this.chat("Còn lâu ngươi mới khuất phục được ta");
            this.hasPickedBone = false;
            this.timePickedBone = 0;
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
                    if (zone.getNumOfPlayers() <= 10 && !BossManager.gI().checkBosses(zone, BossID.SOI_HEC_QUYN)) {
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
                    this.chat("Còn lâu ngươi mới khuất phục được ta");
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
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        try {
            String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            this.chat(prefix, textChat);
            this.lastTimeChatM = System.currentTimeMillis();
            this.timeChatM = Util.nextInt(3000, 20000);
        } catch (Exception e) {
            Logger.error("Lỗi trong chatM SoiHecQuyn: " + e.getMessage());
        }
    }

    @Override
    public void active() {
        this.attack();
        this.chatM();
        this.autoLeaveMap();
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

    public void pickBone() {
        hasPickedBone = true;
        timePickedBone = System.currentTimeMillis();
        this.chat("Ế! miếng xương ngon quá");
        timeLeaveDelay = System.currentTimeMillis();
    }

    public boolean hasPickedBone() {
        return hasPickedBone;
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.location == null) {
                    return;
                }
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

                if (timePickedBone > 0) {
                    if (Util.canDoWithTime(timePickedBone, 5000)) {
                        timePickedBone = 0;
                        hasPickedBone = false;
                    }
                }
            } catch (Exception ex) {
                Logger.error("Lỗi attack SoiHecQuyn: " + ex.getMessage());
            }
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }
}
