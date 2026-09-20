package services.top;
import daos.NDVSqlFetcher;
import player.Player;
import utils.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class TopAppearanceCache {

    private static final ConcurrentHashMap<Integer, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final long TTL_MS = 300_000L;

    public static short[] getAppearance(int playerId, short fallbackHead, short fallbackBody, short fallbackLeg) {
        try {
            CacheEntry entry = CACHE.get(playerId);
            long now = System.currentTimeMillis();
            if (entry != null && now - entry.time <= TTL_MS) {
                return new short[]{entry.head, entry.body, entry.leg};
            }
            Player p = NDVSqlFetcher.loadById(playerId);
            if (p != null) {
                try {
                    p.setClothes.setup();
                    if (p.pet != null) {
                        p.pet.setClothes.setup();
                    }
                    short head = p.getHead();
                    short body = p.getBody();
                    short leg = p.getLeg();
                    CACHE.put(playerId, new CacheEntry(head, body, leg, now));
                    return new short[]{head, body, leg};
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Logger.error("TopAppearanceCache error: " + e);
        }
        return new short[]{fallbackHead, fallbackBody, fallbackLeg};
    }

    private static class CacheEntry {

        short head;
        short body;
        short leg;
        long time;

        CacheEntry(short head, short body, short leg, long time) {
            this.head = head;
            this.body = body;
            this.leg = leg;
            this.time = time;
        }
    }
}
