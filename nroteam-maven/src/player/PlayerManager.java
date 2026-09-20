package player;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import managers.GiftCodeManager;

public class PlayerManager {

    private static final ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();

    /**
     * Session đăng nhập mới là nguồn hiện hành. Client sẽ đóng session cũ sau
     * khi đăng ký session mới, vì vậy registry phải thay object cũ thay vì bỏ
     * qua object mới.
     */
    public static void addPlayer(Player p) {
        if (p == null || p.id <= 0) {
            return;
        }
        players.put(p.id, p);
    }


    public static void removePlayer(Player p) {
        if (p == null || p.id <= 0) {
            return;
        }
        // Session cũ đóng muộn không được xóa object của session mới cùng ID.
        players.remove(p.id, p);
    }

    public static Player getPlayer(long id) {
        return players.get(id);
    }

    public static Collection<Player> getPlayers() {
        return players.values();
    }

    public static int size() {
        return players.size();
    }
}
