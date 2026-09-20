package bot;

import java.util.Random;
import map.Zone;
import services.ChatGlobalService;
import services.Service;
import services.map.MapService;
import services.player.PlayerService;

public class ChatBot {

    private static final long WORLD_CHAT_INTERVAL_MIN = 30_000;
    private static final long WORLD_CHAT_INTERVAL_MAX = 90_000;
    private static final long LOCAL_CHAT_INTERVAL_MIN = 10_000;
    private static final long LOCAL_CHAT_INTERVAL_MAX = 25_000;
    private static final long MOVE_INTERVAL_MIN = 3_000;
    private static final long MOVE_INTERVAL_MAX = 8_000;
    private static final long CHANGE_MAP_INTERVAL_MIN = 120_000;
    private static final long CHANGE_MAP_INTERVAL_MAX = 300_000;

    private static final String[] WORLD_MESSAGES = {
            "Ai đi săn boss cho mình theo với?",
            "Có team nào còn slot không?",
            "Mọi người đang farm map nào vậy?",
            "Nay server đông vui ghê :)",
            "Ai giao lưu vài trận không?",
            "Chúc mọi người đập đồ may mắn nha",
            "Có ai vừa thấy boss xuất hiện không?",
            "Mình mới quay lại chơi, chào mọi người!"
    };

    private static final String[] LOCAL_MESSAGES = {
            "Map này farm ổn không bạn?",
            "Chào bạn nha",
            "Cho mình farm ké một lúc nhé",
            "Bạn cũng đang làm nhiệm vụ à?",
            "Đông vui quá ha",
            "Mình nghỉ tay chút rồi farm tiếp"
    };

    private final Random random = new Random();
    public Bot bot;
    private long nextWorldChatAt;
    private long nextLocalChatAt;
    private long nextMoveAt;
    private long nextChangeMapAt;

    public ChatBot() {
        long now = System.currentTimeMillis();
        this.nextWorldChatAt = now + getRandomBetween(WORLD_CHAT_INTERVAL_MIN, WORLD_CHAT_INTERVAL_MAX);
        this.nextLocalChatAt = now + getRandomBetween(LOCAL_CHAT_INTERVAL_MIN, LOCAL_CHAT_INTERVAL_MAX);
        this.nextMoveAt = now + getRandomBetween(MOVE_INTERVAL_MIN, MOVE_INTERVAL_MAX);
        this.nextChangeMapAt = now + getRandomBetween(CHANGE_MAP_INTERVAL_MIN, CHANGE_MAP_INTERVAL_MAX);
    }

    public ChatBot(ChatBot copy) {
    }

    public void update() {
        if (bot == null || bot.zone == null)
            return;
        long now = System.currentTimeMillis();

        handleMovement(now);
        handleChat(now);
    }

    private void handleMovement(long now) {
        if (bot.zone == null || bot.zone.map == null)
            return;

        if (now >= nextMoveAt) {
            try {
                int mapWidth = bot.zone.map.mapWidth;
                int mapHeight = bot.zone.map.mapHeight;

                if (mapWidth > 0 && mapHeight > 0) {
                    int targetX = random.nextInt(Math.max(100, mapWidth - 100)) + 50;
                    int targetY = random.nextInt(Math.max(50, mapHeight / 2));

                    // Có lúc chỉ đứng quan sát; tránh bot cứ hết giờ là chạy ngay.
                    if (random.nextInt(5) != 0) {
                        PlayerService.gI().playerMove(bot, (short) targetX, (short) targetY);
                    }
                    nextMoveAt = now + getRandomBetween(MOVE_INTERVAL_MIN, MOVE_INTERVAL_MAX);
                }
            } catch (Exception e) {
                System.err.println("[ChatBot] Lỗi di chuyển bot: " + bot.name);
            }
        }
    }

    private void handleChat(long now) {
        if (now >= nextWorldChatAt) {
            try {
                String msg = getRandomMessage(WORLD_MESSAGES);
                ChatGlobalService.gI().chatBot(bot, msg);
                nextWorldChatAt = now + getRandomBetween(WORLD_CHAT_INTERVAL_MIN, WORLD_CHAT_INTERVAL_MAX);
            } catch (Exception e) {
                System.err.println("[ChatBot] Lỗi chat thế giới: " + bot.name);
            }
        }

        if (now >= nextLocalChatAt) {
            if (isValidMap(bot.zone)) {
                try {
                    String msg = getRandomMessage(LOCAL_MESSAGES);
                    Service.gI().chat(bot, msg);
                } catch (Exception e) {
                    System.err.println("[ChatBot] Lỗi chat local: " + bot.name);
                }
            }
            nextLocalChatAt = now + getRandomBetween(LOCAL_CHAT_INTERVAL_MIN, LOCAL_CHAT_INTERVAL_MAX);
        }
    }

    private void handleMapChange(long now) {
        if (now >= nextChangeMapAt) {
            try {
                bot.joinMap();
                nextChangeMapAt = now + getRandomBetween(CHANGE_MAP_INTERVAL_MIN, CHANGE_MAP_INTERVAL_MAX);
                nextMoveAt = now + getRandomBetween(1_500, 4_500);
            } catch (Exception e) {
                System.err.println("[ChatBot] Lỗi đổi map: " + bot.name);
            }
        }
    }

    private boolean isValidMap(Zone zone) {
        if (zone == null || zone.map == null)
            return false;
        int mapId = zone.map.mapId;
        return !MapService.gI().isMapBanDoKhoBau(mapId)
                && !MapService.gI().isHome(mapId)
                && !MapService.gI().isMapPhoBan(mapId);
    }

    private String getRandomMessage(String[] list) {
        return list[random.nextInt(list.length)];
    }

    private long getRandomBetween(long min, long max) {
        return min + random.nextInt((int) (max - min + 1));
    }
}
