package npc.list;

import boss.challenge.BossChallenge;
import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.Service;
import services.dungeon.BossChallengeService;
import services.player.ClanService;
import services.map.ChangeMapService;

public class GiuMaDauBo extends Npc {

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            String[] menus;
            boolean attended = false;
            if (player != null && player.clan != null) {
                clan.ClanMember cm = player.clan.getClanMember((int) player.id);
                if (cm != null) {
                    attended = utils.TimeUtil.isSameDay(cm.attendanceTime, System.currentTimeMillis());
                }
            }
            boolean opened = BossChallengeService.gI().getBossChallengeByPlayer(player) != null;
            String first = opened ? "Vào\nKhiêu chiến\nBoss" : "Khiêu chiến\nBoss";
            
            if (attended) {
                menus = new String[] { first, "Đóng" }; 
            } else {
                menus = new String[] { first, "Điểm danh\n+1 Capsule\nBang", "Đóng" };
            }
            
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ngươi muốn gì nào?",
                    menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (player != null && player.idMark != null && player.idMark.getIndexMenu() == ConstNpc.BOSS_CHALLENGE_COST) {
            if (select == 0) {
                long today = System.currentTimeMillis() / 86_400_000L;
                if (player.clan.lastBossChallenge != 0 && player.clan.lastBossChallenge != today) {
                    player.clan.bossChallengeToday = 0;
                    player.clan.lastBossChallenge = today;
                }
                if (player.clan.lastBossChallenge == 0) {
                    player.clan.lastBossChallenge = today;
                }
                int attempts = player.clan.bossChallengeToday;
                int cost = attempts == 0 ? 0 : (attempts == 1 ? 300 : 500);
                if (cost > 0) {
                    if (player.inventory.gem < cost) {
                        Service.gI().sendThongBao(player,
                                "Bạn không đủ ngọc, còn thiếu " + (cost - player.inventory.gem) + " ngọc nữa");
                        return;
                    }
                    player.inventory.gem -= cost;
                    Service.gI().sendMoney(player);
                }
                player.clan.bossChallengeToday++;
                player.clan.lastBossChallenge = System.currentTimeMillis() / 86_400_000L;
                player.clan.update();
                BossChallengeService.gI().openBossChallenge(player);
            }
            return;
        }
        if (canOpenNpc(player)) {
            boolean attended = false;
            if (player != null && player.clan != null) {
                clan.ClanMember cm = player.clan.getClanMember((int) player.id);
                if (cm != null) {
                    attended = utils.TimeUtil.isSameDay(cm.attendanceTime, System.currentTimeMillis());
                }
            }
            
            int mappedSelect = select;
            if (attended && select >= 1) {
                mappedSelect = select + 1; 
            }
            
            switch (mappedSelect) {
                case 0 -> {
                    BossChallenge bch = BossChallengeService.gI().getBossChallengeByPlayer(player);
                    if (bch != null && bch.isOpened) {
                        if (bch.roundCompleted && bch.currentRound < bch.maxRounds) {
                            bch.startNextRound(player);
                            return;
                        }
                        ChangeMapService.gI().changeMapNonSpaceship(player, 164, 420, 408);
                        return;
                    }
                    long today = System.currentTimeMillis() / 86_400_000L;
                    if (player.clan.lastBossChallenge != 0 && player.clan.lastBossChallenge != today) {
                        player.clan.bossChallengeToday = 0;
                        player.clan.lastBossChallenge = today;
                    }
                    if (player.clan.lastBossChallenge == 0) {
                        player.clan.lastBossChallenge = today;
                    }
                    int attempts = player.clan.bossChallengeToday;
                    int cost = attempts == 0 ? 0 : (attempts == 1 ? 300 : 500);
                    this.createOtherMenu(player, ConstNpc.BOSS_CHALLENGE_COST,
                            "Thời gian khiêu chiến Boss là 30 phút.\n"
                                    + "Top 5 đánh boss +15 Capsule Bang\n"
                                    + "Top 10 đánh boss +10 Capsule Bang\n"
                                    + "Top 11 trở lên +5 Capsule Bang\n"
                                    + "Người đánh Boss cuối cùng thưởng thêm 10 Capsule Bang\n"
                                    + "Mở cửa vào ngày thứ 7 và Chủ Nhật hàng tuần.",
                            new String[] {
                                    cost == 0 ? "Khiêu chiến\nMiễn phí" : (cost == 300 ? "300 ngọc" : "500 ngọc"),
                                    "Đóng" });
                }
                case 1 -> {
                    if (player.clan != null) {
                        ClanService.gI().rollCallCapsuleClan(player);
                    }
                }
            }
        }
    }
}