package network;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BehavioralDetector — per-session game-logic anomaly detection.
 *
 * Tracked per session key (IP:sessionId or playerName):
 *   - Actions per second via sliding window (10 × 100 ms buckets)
 *   - Per-command spam detection (same cmd > MAX_SAME_CMD_PER_SEC)
 *   - Total violation count
 *
 * Called from game logic layer (Stage 4) via:
 *   {@code BehavioralDetector.gI().recordAction(sessionKey, cmdId)}
 *
 * Results feed back into AntiDDoSEngine reputation when violations exceed
 * the configured thresholds.
 */
public class BehavioralDetector {

    // ─────────────────────────────── Singleton ────────────────────────────────

    private static volatile BehavioralDetector instance;

    public static BehavioralDetector gI() {
        if (instance == null) {
            synchronized (BehavioralDetector.class) {
                if (instance == null) instance = new BehavioralDetector();
            }
        }
        return instance;
    }

    // ──────────────────────────── Per-Session Profile ─────────────────────────

    public static final class SessionProfile {
        /** 10 × 100 ms buckets; index = (nowMs / 100) % 10 */
        public final long[]        actionBuckets  = new long[10];
        public volatile int        currentIdx     = 0;
        public volatile long       bucketStartMs  = System.currentTimeMillis();

        public final AtomicLong    totalActions   = new AtomicLong(0);
        public final AtomicInteger violations     = new AtomicInteger(0);
        public volatile long       lastActionMs   = System.currentTimeMillis();

        /** Per-command frequency within the current 1-second window. */
        public final ConcurrentHashMap<Byte, AtomicInteger> cmdFrequency = new ConcurrentHashMap<>();
        public volatile long cmdWindowStartMs = System.currentTimeMillis();

        /** Approximate actions-per-second over the last 1-second window. */
        public int getAps() {
            long sum = 0;
            for (long b : actionBuckets) sum += b;
            return (int) sum; // sum over 10 × 100 ms = 1 s
        }
    }

    // ─────────────────────────────── Thresholds ────────────────────────────────

    /** Max actions per second before anomaly is logged. */
    public static final int MAX_APS_NORMAL    = 30;
    /** Hard burst limit — reject if APS exceeds this. */
    public static final int MAX_APS_SPIKE     = 60;
    /** Same command repeated more than this per second = spam. */
    public static final int MAX_SAME_CMD_PER_SEC = 20;

    // ─────────────────────────────── State ────────────────────────────────────

    private final ConcurrentHashMap<String, SessionProfile> profiles = new ConcurrentHashMap<>();

    // ─────────────────────────── Public API ───────────────────────────────────

    /**
     * Record one player action and check for anomalies.
     *
     * @param sessionKey unique key: IP_port or player name
     * @param cmdId      game command byte
     * @return true  → action looks normal
     *         false → anomaly detected (violation incremented)
     */
    public boolean recordAction(String sessionKey, byte cmdId) {
        if (sessionKey == null) return true;

        SessionProfile profile = profiles.computeIfAbsent(sessionKey, k -> new SessionProfile());
        long now = System.currentTimeMillis();
        profile.lastActionMs = now;
        profile.totalActions.incrementAndGet();

        // ── Update sliding window ──
        int  idx           = (int) ((now / 100) % 10);
        long expectedStart = (now / 100) * 100;

        if (profile.bucketStartMs != expectedStart) {
            long prevStart = profile.bucketStartMs;
            long skipped   = (expectedStart - prevStart) / 100;
            if (skipped >= 10) {
                java.util.Arrays.fill(profile.actionBuckets, 0);
            } else {
                for (int i = 1; i <= skipped; i++) {
                    profile.actionBuckets[(int) (((prevStart / 100) + i) % 10)] = 0;
                }
            }
            profile.bucketStartMs = expectedStart;
            profile.currentIdx    = idx;
        }
        profile.actionBuckets[idx]++;

        int aps = profile.getAps();

        // ── APS check ──
        if (aps > MAX_APS_SPIKE) {
            profile.violations.incrementAndGet();
            return false;
        }

        // ── Per-command spam check (reset window every second) ──
        if (now - profile.cmdWindowStartMs > 1_000) {
            profile.cmdFrequency.clear();
            profile.cmdWindowStartMs = now;
        }
        AtomicInteger cmdCount =
                profile.cmdFrequency.computeIfAbsent(cmdId, k -> new AtomicInteger());
        if (cmdCount.incrementAndGet() > MAX_SAME_CMD_PER_SEC) {
            profile.violations.incrementAndGet();
            return false;
        }

        return true;
    }

    /** Remove the profile when a session/player disconnects. */
    public void removeSession(String sessionKey) {
        if (sessionKey != null) profiles.remove(sessionKey);
    }

    public SessionProfile getProfile(String sessionKey) {
        return profiles.get(sessionKey);
    }

    public Map<String, SessionProfile> getAllProfiles() {
        return Collections.unmodifiableMap(profiles);
    }

    /**
     * Evict profiles that have been idle for more than {@code idleMs} ms.
     * Should be called periodically (e.g., every 60 s).
     */
    public void cleanup(long idleMs) {
        long cutoff = System.currentTimeMillis() - idleMs;
        profiles.entrySet().removeIf(e -> e.getValue().lastActionMs < cutoff);
    }
}
