package boss.event.newyear;

import consts.BossStatus;
import managers.boss.LunarNewYearEventManager;
import boss.*;
import static consts.BossType.TET_EVENT;
import player.Player;
import server.Client;
import services.EffectSkillService;
import services.Service;
import services.map.ChangeMapService;
import utils.Logger;
import utils.Util;

public class LanCon extends Boss {

    private long st;
    private int timeLeave;
    private long playerId;
    private boolean afk;

    // --- Hệ thống đi dạo tự nhiên (Kế thừa logic từ Broly) ---
    private long lastWalkTime = 0;
    private long nextWalkDelay = 0;      // Thời gian chờ trước khi bước tiếp
    private int walkDirection = 1;       // Hướng đi: 1 = phải, -1 = trái
    private int walkStepsRemaining = 0;  // Số bước còn lại trong lượt đi hiện tại
    private boolean isResting = false;   // Đang đứng im nghỉ ngơi
    private long restStartTime = 0;
    private long restDuration = 0;       // Thời gian đứng nghỉ ngơi (ms)

    public LanCon() throws Exception {
        super(TET_EVENT, BossID.LAN_CON, BossesData.LAN_CON);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
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
                int zoneid = 0;
                boolean foundZone = false;

                // Quét tìm khu vực hợp lệ (Dưới 10 người và mỗi khu chỉ tối đa 1 Lân Con)
                while (zoneid < this.zone.map.zones.size()) {
                    var currentZone = this.zone.map.zones.get(zoneid);
                    if (currentZone.getNumOfPlayers() <= 10
                            && !LunarNewYearEventManager.gI().checkBosses(currentZone, BossID.LAN_CON)) {
                        this.zone = currentZone;
                        foundZone = true;
                        break;
                    }
                    zoneid++;
                }

                if (!foundZone) {
                    this.leaveMapNew();
                    this.changeStatus(BossStatus.REST); 
                    return;
                }

                ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(100, 500), this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24));
                this.changeStatus(BossStatus.CHAT_S);
                
                // Khởi tạo các thông số đi dạo khi vừa vào map
                long currentTime = System.currentTimeMillis();
                this.lastWalkTime = currentTime;
                this.isResting = false;
                this.walkStepsRemaining = 0;
                this.nextWalkDelay = Util.nextInt(2000, 4000); // Đứng đợi một lúc rồi mới đi dạo

                st = System.currentTimeMillis();
                timeLeave = Util.nextInt(100000, 300000);
            } catch (Exception e) {
                Logger.error(this.data[0].getName() + ": Lỗi đang tiến hành REST\n");
                this.changeStatus(BossStatus.REST);
            }
        } else {
            Logger.error(this.data[0].getName() + ": Lỗi map đang tiến hành RESPAWN\n");
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    // Cơ chế xử lý Lân con tự đi lại tự do khi chưa bị thu phục
    private void passiveWalk() {
        if (this.zone == null || this.zone.map == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Trạng thái đang đứng nghỉ ngơi
        if (isResting) {
            if (currentTime - restStartTime >= restDuration) {
                isResting = false;
                nextWalkDelay = Util.nextInt(1000, 3000); // Delay ngẫu nhiên trước bước đi tiếp theo
                lastWalkTime = currentTime;
            }
            return;
        }
        
        // Kiểm tra giãn cách giữa các bước đi dạo
        if (currentTime - lastWalkTime < nextWalkDelay) {
            return;
        }
        
        lastWalkTime = currentTime;
        
        // Nếu đã đi hết chu kỳ số bước quy định thì dừng lại nghỉ
        if (walkStepsRemaining <= 0) {
            // 40% tỷ lệ Lân con sẽ dừng chân đứng im (Nghỉ từ 3 đến 7 giây)
            if (Util.isTrue(40, 100)) {
                isResting = true;
                restStartTime = currentTime;
                restDuration = Util.nextInt(3000, 7000);
                return;
            }
            
            // Random ngẫu nhiên hướng di chuyển mới và số bước đi dạo liên tục (từ 3 đến 8 bước)
            walkDirection = Util.getOne(-1, 1);
            walkStepsRemaining = Util.nextInt(3, 8);
        }
        
        // Khoảng cách mỗi bước di chuyển (25-50 pixel)
        int stepSize = Util.nextInt(25, 50);
        int newX = this.location.x + (walkDirection * stepSize);
        
        // Giới hạn biên bản đồ tránh Lân con đi ra khỏi góc khuất map
        int mapWidth = this.zone.map.mapWidth;
        if (newX < 100 || newX > mapWidth - 100) {
            walkDirection = -walkDirection; // Đổi hướng ngược lại
            isResting = true;
            restStartTime = currentTime;
            restDuration = Util.nextInt(1000, 2000);
            return;
        }
        
        // Định vị tọa độ mặt đất tại vị trí X mới
        int newY = this.zone.map.yPhysicInTop(newX, 0);
        
        // Thực hiện di chuyển dạo chơi
        this.move(newX, newY);
        walkStepsRemaining--;
        
        // Khoảng thời gian trì hoãn giữa các bước đi (Tạo cảm giác di chuyển mượt mà, tự nhiên)
        nextWalkDelay = Util.nextInt(400, 900);
    }

    @Override
    public void chatM() {
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeave)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.playerId = -1;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

    @Override
    public void attack() {
        // Nếu chưa có playerId chủ nhân (chưa bị đánh trắng máu), thực hiện đi dạo tự do giống Broly
        if (this.playerId <= 0) {
            passiveWalk();
        }
    }

    @Override
    public void afk() {
        // CHỈ CHẠY LOGIC NÀY KHI ĐÃ BỊ THU PHỤC (ĐÁNH TRẮNG MÁU) -> ĐI THEO NGƯỜI CHƠI
        if (Util.canDoWithTime(this.lastTimeAttack, 500)) {
            this.lastTimeAttack = System.currentTimeMillis();
            Player pl = Client.gI().getPlayer(playerId);
            if (pl == null || pl.zone == null) {
                return;
            }
            if (pl.haveReward) {
                pl.haveReward = false;
                this.leaveMap();
                return;
            }
            if (this.zone.equals(pl.zone)) {
                int dis = Util.getDistance(this, pl);
                if (dis <= 300) {
                    if (dis > 50) {
                        int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                        int move = Util.nextInt(50, 100);
                        move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                        st = System.currentTimeMillis();
                    }
                    afk = false;
                    pl.canReward = true;
                } else {
                    afk = true;
                    pl.canReward = false;
                }
            } else if (!afk) {
                if (pl.changeMapVIP) {
                    pl.changeMapVIP = false;
                    pl.canReward = false;
                    afk = true;
                    return;
                }
                ChangeMapService.gI().changeMap(this, pl.zone, pl.location.x + Util.nextInt(-10, 10), pl.location.y);
            }
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(100, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage > 500_000) {
                damage = 500_000;
            }

            // Kiểm tra điều kiện thu phục: Đánh trắng máu hoặc chat "kiki"
            if (plAtt != null && (damage >= this.nPoint.hp || isPlayerChatKiKi(plAtt))) {
                this.changeToTypeNonPK();
                this.playerId = Math.abs(plAtt.id);
                Service.gI().chat(plAtt, "Đi thôi lân con!");
                this.nPoint.hp = this.nPoint.hpMax;
                
                // Đổi trạng thái boss sang AFK để vòng lặp của game tự kích hoạt hàm afk() bám đuôi người chơi
                this.changeStatus(BossStatus.AFK); 
                return 0;
            }
            this.nPoint.subHP(damage);
            return (int) damage;
        } else {
            return 0;
        }
    }

    private boolean isPlayerChatKiKi(Player plAtt) {
        if (plAtt == null) {
            return false;
        }
        String lastChat = plAtt.getLastChatMessage();  
        return lastChat != null && lastChat.toLowerCase().contains("kiki");
    }

    @Override
    public void reward(Player plKill) {
    }
}
