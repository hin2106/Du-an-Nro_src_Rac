package boss.learn;


import boss.BossesData;
import consts.ConstTask;
import player.Player;
import services.TaskService;
import utils.Util;


import boss.BossID;
import consts.BossStatus;
import static consts.BossType.PHOBAN;
import services.Service;
import services.map.ChangeMapService;

public class TauPayPay extends TrainingBoss {

    public TauPayPay(Player player) throws Exception {
        super(PHOBAN, BossID.TAUPAYPAY, BossesData.TAUPAYPAY);
        this.playerAtt = player;
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, 775);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.doneChatS) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[playerAtt.isThachDau ? 1 : 0];
            int prefix = 0;

            int startIndex = 1;
            int endIndex = textChat.lastIndexOf("|");

            if (endIndex == -1 || endIndex <= startIndex) {
                prefix = 0;
            } else {
                try {
                    prefix = Integer.parseInt(textChat.substring(startIndex, endIndex));
                } catch (NumberFormatException e) {
                    prefix = 0;
                }
                textChat = textChat.substring(endIndex + 1);
            }

            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.moveToPlayer(playerAtt);
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = 2000;
            doneChatS = true;
        }
        return false;
    }

//    @Override
//    public void leaveMap() {
//        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
//        Message msg;
//        try {
//            msg = new Message(-6);
//            msg.writer().writeInt((int) this.id);
//            playerAtt.sendMessage(msg);
//            msg.cleanup();
//            this.zone = null;
//        } catch (IOException e) {
//            Logger.logException(MapService.class, e);
//        }
//    }

    @Override
    public void buffPea() {
    }
    
    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.LEAVE_MAP);
        this.chatE(); 
        this.lastTimeAFK = 0;
        Service.gI().sendPlayerVS(playerAtt, null, (byte) 0);
        Player pDone = plKill != null ? plKill : playerAtt;

        if (pDone != null) {
            TaskService.gI().doneTask(pDone, ConstTask.TASK_10_1);
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(400, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (TaskService.gI().getIdTask(plAtt) != ConstTask.TASK_10_1) {
                return 100;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            this.nPoint.subHP(damage);

            if (this.nPoint.hp > 0 && this.nPoint.hp < this.nPoint.hpMax / 5) {
                if (Util.canDoWithTime(lastTimeChat, 2000)) {
                    String[] text = {"AAAAAAAAA", "ai da"};
                    this.chat(text[Util.nextInt(text.length)]);
                }
            }

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
    public void afk() {
        if (Util.canDoWithTime(lastTimeAFK, 5000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
}
