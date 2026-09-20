package player.badges;

import player.Player;
import java.util.Iterator;

public class BadgesService {

    public static void removeExpiredBadges(Player player) {
        if (player.dataBadges == null || player.dataBadges.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Iterator<BadgesData> iterator = player.dataBadges.iterator();

        while (iterator.hasNext()) {
            BadgesData data = iterator.next();
            if (data.timeofUseBadges != Long.MAX_VALUE && data.timeofUseBadges < currentTime) {
                iterator.remove();
            }
        }
    }

    public static boolean toggleBadges(Player player, int id) {
        if (player.dataBadges == null) {
            return false;
        }

        BadgesData targetBadge = null;
        for (BadgesData data : player.dataBadges) {
            if (data.idBadGes == id) {
                targetBadge = data;
                break;
            }
        }

        if (targetBadge == null) {
            return false;
        }
        if (targetBadge.isUse) {
            targetBadge.isUse = false;
            return false;
        }
        for (BadgesData data : player.dataBadges) {
            data.isUse = (data.idBadGes == id);
        }
        return true;
    }

    public static void turnOnBadges(Player player, int id) {
        if (player.dataBadges != null) {
            for (BadgesData data : player.dataBadges) {
                if (data.idBadGes == id) {
                    data.isUse = true;
                } else {
                    data.isUse = false;
                }
            }
        }
    }

    /**
     * Tắt tất cả danh hiệu
     */
    public static void turnOffAllBadges(Player player) {
        if (player.dataBadges != null) {
            for (BadgesData data : player.dataBadges) {
                data.isUse = false;
            }
        }
    }

    public static void addBadges(Player player, int effectId, int days) {
        if (player.dataBadges == null) {
            return;
        }

        boolean exists = false;
        for (BadgesData data : player.dataBadges) {
            if (data.idBadGes == effectId) {
                long additionalTime = (long) days * 24 * 60 * 60 * 1000;
                if (data.timeofUseBadges < System.currentTimeMillis()) {
                    data.timeofUseBadges = System.currentTimeMillis() + additionalTime;
                } else {
                    data.timeofUseBadges += additionalTime;
                }
                exists = true;
                break;
            }
        }

        if (!exists) {
            long expireTime = System.currentTimeMillis() + ((long) days * 24 * 60 * 60 * 1000);
            BadgesData newBadge = new BadgesData(effectId, expireTime, false);
            player.dataBadges.add(newBadge);
        }
    }

}
