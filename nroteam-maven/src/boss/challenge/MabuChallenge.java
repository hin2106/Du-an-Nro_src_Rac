package boss.challenge;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import player.Player;
import services.Service;
import utils.Util;

public class MabuChallenge extends Boss {

    private long st;

    public MabuChallenge() throws Exception {
        super(BossID.MABU_CHALLENGE, BossesData.MABU_CHALLENGE);
    }


    @Override
    public void joinMap() {
        super.joinMap();
        if (this.zone != null) {
            this.changeStatus(BossStatus.ACTIVE);
        }
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    protected void notifyJoinMap() {
        // Không gửi thông báo spam cho challenge boss
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            executeReward(plKill);
            
            // --- XỬ LÝ TRAO THƯỞNG CAPSULE BANG THEO DAME CHUẨN ---
            try {
                if (this.zone != null && plKill.clan != null) {
                    
                    // Sắp xếp danh sách ID người chơi dựa trên lượng Dame từ cao xuống thấp
                    java.util.List<Integer> sortedPlayerIds = new java.util.ArrayList<>(this.statisticsDamage.keySet());
                    sortedPlayerIds.sort((id1, id2) -> Long.compare(this.statisticsDamage.get(id2), this.statisticsDamage.get(id1)));
                    
                    int rank = 1;
                    for (int playerId : sortedPlayerIds) {
                        // Tìm đối tượng Player đang có mặt trong khu vực dựa vào ID
                        Player pl = this.zone.getPlayerInMap(playerId); 
                        
                        // Nếu không tìm thấy hàm getPlayerInMap, bạn có thể duyệt qua list của zone như sau:
                        // Player pl = this.zone.getPlayers().stream().filter(p -> p != null && p.id == playerId).findFirst().orElse(null);

                        if (pl != null && pl.clan != null && pl.clan.id == plKill.clan.id) {
                            int capsulePlus = 5; // Mặc định top 11+
                            
                            if (rank <= 5) {
                                capsulePlus = 15; // Top 5 dame thực tế
                            } else if (rank <= 10) {
                                capsulePlus = 10; // Top 10 dame thực tế
                            }
                            
                            // Thưởng kết liễu (Last Hit)
                            if (pl.id == plKill.id) {
                                capsulePlus += 10;
                            }
                            
                            clan.ClanMember cm = pl.clan.getClanMember((int) pl.id);
                            if (cm != null) {
                                cm.clanPoint += capsulePlus; 
                                long totalDame = this.statisticsDamage.getOrDefault(playerId, 0L);
                                services.Service.gI().sendThongBao(pl, "Top " + rank + " sát thương Boss (Dame thực: " + totalDame + ").\nNhận được " + capsulePlus + " Capsule Bang!");
                            }
                            rank++;
                        }
                    }
                }
            } catch (Exception e) {
                utils.Logger.logException(MabuChallenge.class, e); // Đổi tên class tương ứng từng file
            } finally {
                
                this.statisticsDamage.clear();
            }
            // --- KẾT THÚC LOGIC TRAO THƯỞNG ---
        }
        this.changeStatus(BossStatus.DIE);
    }
}
