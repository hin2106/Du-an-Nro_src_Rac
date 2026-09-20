package task;

import player.Player;
import player.badges.BadgesData;
import player.badges.BagesTemplate;
import server.Manager;

public class BadgesTaskService {

    public static void createAndResetTask(Player player) {
        if (player == null) {
            return;
        }

        player.dataTaskBadges.clear();
        for (BadgesTaskTemplate BTT : Manager.TASKS_BADGES_TEMPLATE) {
            BadgesTask data = new BadgesTask();
            data.id = BTT.id;
            data.count = 0;
            data.countMax = BTT.count;
            data.idBadgesReward = BTT.idbadgesReward;
            data.badgeClaimed = false; // Reset flag khi tạo mới
            player.dataTaskBadges.add(data);
        }
    }

    public static void updateDoneTask(Player player) {
        if (player == null || player.dataTaskBadges == null || player.dataBadges == null) {
            return;
        }

        for (BadgesTask data : player.dataTaskBadges) {
            if (data.isExactlyDone() && !data.badgeClaimed) {
                boolean alreadyHave = false;
                for (BadgesData bg : player.dataBadges) {
                    if (bg.idBadGes == data.idBadgesReward) {
                        alreadyHave = true;
                        break;
                    }
                }

                if (!alreadyHave) {
                    int days = BagesTemplate.getDaysFromBadgeOptions(data.idBadgesReward);
                    new BadgesData(player, data.idBadgesReward, days);
                    data.badgeClaimed = true;
                    data.count = 0;
                } else {
                    data.badgeClaimed = true;
                    data.count = 0;
                }
            }
        }
    }

    public static void updateCountBagesTask(Player player, int id, int amount) {
        if (player == null || player.dataTaskBadges == null || amount <= 0) {
            return;
        }

        for (BadgesTask data : player.dataTaskBadges) {
            if (data.id == id) {
                if (!data.badgeClaimed) {
                    int oldCount = data.count;
                    data.count += amount;
                    if (data.count > data.countMax) {
                        data.count = data.countMax;
                    }
                    if (oldCount < data.countMax && data.count >= data.countMax) {
                        data.count = data.countMax;
                    }
                }
                break;
            }
        }
    }

    public static int sendPercenBadgesTask(Player player, int idBadgesReward) {
        for (BadgesTask data : player.dataTaskBadges) {
            if (data.idBadgesReward == idBadgesReward) {
                if (data.getPercentProcess() > 0) {
                    return data.getPercentProcess();
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static void setCoutBagesTask(Player player, int id, int count) {
        if (player == null || player.dataTaskBadges == null || count < 0) {
            return;
        }

        for (BadgesTask data : player.dataTaskBadges) {
            if (data.id == id) {
                // Chỉ cập nhật nếu task chưa được claim
                if (!data.badgeClaimed) {
                    data.count = count;
                    if (data.count > data.countMax) {
                        data.count = data.countMax;
                    }
                    // Đảm bảo count không vượt quá countMax
                    if (data.countMax > 0 && data.count > data.countMax) {
                        data.count = data.countMax;
                    }
                }
                break;
            }
        }
    }

    public static int sendDay(Player player, int id) {
        for (BadgesData data : player.dataBadges) {
            if (data.idBadGes == id) {
                if (data.timeofUseBadges == Long.MAX_VALUE) {
                    return -1;
                }

                long currentTime = System.currentTimeMillis();
                long timeDifference = data.timeofUseBadges - currentTime;
                if (timeDifference <= 0) {
                    return 0;
                }
                int days = (int) Math.ceil(timeDifference / (24.0 * 60.0 * 60.0 * 1000.0));
                return days;
            }
        }
        return 0;
    }

}
