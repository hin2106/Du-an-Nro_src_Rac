package boss.challenge;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import player.Player;
import services.Service;
import utils.Util;

public class XenBoHungChallenge extends Boss {

    private long st;

    public XenBoHungChallenge() throws Exception {
        super(BossID.XEN_BO_HUNG_CHALLENGE, BossesData.SIEU_BO_HUNG_2);
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
                    
                    java.util.List<Integer> sortedPlayerIds = new java.util.ArrayList<>(this.statisticsDamage.keySet());
                    sortedPlayerIds.sort((id1, id2) -> Long.compare(this.statisticsDamage.get(id2), this.statisticsDamage.get(id1)));
                    
                    int rank = 1;
                    for (int playerId : sortedPlayerIds) {
                        Player pl = this.zone.getPlayerInMap(playerId); 
                        
                        if (pl != null && pl.clan != null && pl.clan.id == plKill.clan.id) {
                            int capsulePlus = 5; // Mặc định top 11+
                            
                            if (rank <= 5) {
                                capsulePlus = 15; // Top 5 dame thực tế
                            } else if (rank <= 10) {
                                capsulePlus = 10; // Top 10 dame thực tế
                            }
                            
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
                utils.Logger.logException(XenBoHungChallenge.class, e);
            } finally {
                this.statisticsDamage.clear();
            }
        }
        this.changeStatus(BossStatus.DIE);
    }
}
