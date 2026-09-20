package network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AntiDDoSEngine {
    private static final int MAX_TRACKED_IPS = 50_000;

    // ─────────────────────────────── Defense Modes ────────────────────────────
    public enum DefenseMode {
        NORMAL      ("Bình Thường",             240,   400,  32768,      20,     10,    2500,     150,    3000,    300),
        UNDER_ATTACK("Đang Bị Tấn Công",        120,   240,  32768,      12,      5,    1500,      80,    2500,    180),
        EMERGENCY   ("Khẩn Cấp",                 60,   120,  32768,       6,      2,     800,      40,    1500,     80);

        public final String label;
        public final int packetRateLimit;
        public final int burstLimit;
        public final int maxPacketSize;
        public final int maxConnectionsPerIP;
        public final int maxNewConnPerSecPerIP;
        public final int globalPpsThreshold;
        public final int maxNewConnPerSec;
        public final int maxTotalSessions;
        public final int maxPreAuthSessions;

        DefenseMode(String label, int pps, int burst, int pktSize, int maxConn,
                    int newConnPerSecIP, int globalPps, int maxNewConnPerSec,
                    int maxTotalSessions, int maxPreAuthSessions) {
            this.label               = label;
            this.packetRateLimit     = pps;
            this.burstLimit          = burst;
            this.maxPacketSize       = pktSize;
            this.maxConnectionsPerIP = maxConn;
            this.maxNewConnPerSecPerIP = newConnPerSecIP;
            this.globalPpsThreshold  = globalPps;
            this.maxNewConnPerSec    = maxNewConnPerSec;
            this.maxTotalSessions    = maxTotalSessions;
            this.maxPreAuthSessions  = maxPreAuthSessions;
        }
    }

    // ─────────────────────────────── Per-IP Entry ─────────────────────────────
    public static final class IpEntry {
        public final AtomicLong  tokens           = new AtomicLong(300);
        public volatile long     lastRefillNanos  = System.nanoTime();
        public final AtomicInteger reputation     = new AtomicInteger(0);
        public final AtomicInteger activeConnections = new AtomicInteger(0);
        public final AtomicLong    totalPackets      = new AtomicLong(0);
        public volatile long       lastActivityMs    = System.currentTimeMillis();
        public volatile long       firstSeenMs       = System.currentTimeMillis();
        public final long[]        windowBuckets  = new long[10];
        public volatile int        currentBucket  = 0;
        public volatile long       bucketStartMs  = System.currentTimeMillis();
        public final AtomicInteger newConnThisSecond = new AtomicInteger(0);
        public volatile long       connRateWindowStartMs = System.currentTimeMillis();
        public final AtomicInteger violationCount = new AtomicInteger(0);
        public final AtomicLong lastPenaltyMs = new AtomicLong(0);
        public volatile long       blockedUntilMs = 0;
        public volatile String     blockReason    = null;
        public volatile long       blockTimeMs    = 0;

        public boolean isTempBlocked() {
            return blockedUntilMs > System.currentTimeMillis();
        }
        public synchronized int getRecentPps() {
            if (System.currentTimeMillis() - bucketStartMs >= 1_000L) return 0;
            long sum = 0;
            for (long b : windowBuckets) sum += b;
            return (int) sum;
        }
    }

    // ──────────────────────────── Blocked IP Record ───────────────────────────
    public static final class BlockedInfo {
        public final String ip;
        public final String reason;
        public final long   blockedAtMs;
        public final long   durationMs;
        public BlockedInfo(String ip, String reason, long blockedAtMs, long durationMs) {
            this.ip = ip; this.reason = reason; this.blockedAtMs = blockedAtMs; this.durationMs = durationMs;
        }
    }

    // ──────────────────────────── Global Statistics ───────────────────────────
    public static final class GlobalStats {
        public final long totalPacketsDropped, totalIPsEverBlocked, totalConnectionsAccepted;
        public final int currentConnections, trackedIPs, blockedIPs, whitelistedIPs;
        public final DefenseMode mode;
        public GlobalStats(long dropped, long blocked, long totalConn, int curConn, DefenseMode mode,
                           int tracked, int blked, int whited) {
            this.totalPacketsDropped = dropped; this.totalIPsEverBlocked = blocked;
            this.totalConnectionsAccepted = totalConn; this.currentConnections = curConn; this.mode = mode;
            this.trackedIPs = tracked; this.blockedIPs = blked; this.whitelistedIPs = whited;
        }
    }

    // ────────────────────────────── UI Listener ───────────────────────────────
    public interface AntiDDoSListener {
        void onIPBlocked(String ip, String reason);
        void onIPUnblocked(String ip);
        void onModeChanged(DefenseMode newMode);
        void onLogEvent(String message);
    }

    // ──────────────────────────────── Singleton ───────────────────────────────
    private static volatile AntiDDoSEngine instance;
    public static AntiDDoSEngine gI() {
        if (instance == null) {
            synchronized (AntiDDoSEngine.class) {
                if (instance == null) instance = new AntiDDoSEngine();
            }
        }
        return instance;
    }

    // ─────────────────────────────────── State ────────────────────────────────
    private volatile DefenseMode mode = DefenseMode.NORMAL;
    private final ConcurrentHashMap<String, IpEntry> ipMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockedInfo> permanentBlocks = new ConcurrentHashMap<>();
    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();

    private final AtomicLong totalPacketsDropped = new AtomicLong(0);
    private final AtomicLong totalIPsEverBlocked = new AtomicLong(0);
    private final AtomicLong totalConnectionsAccepted = new AtomicLong(0);
    private final AtomicLong totalConnectionAttempts = new AtomicLong(0);
    private final AtomicLong totalPacketObservations = new AtomicLong(0);
    private final AtomicInteger globalActiveSessions = new AtomicInteger(0);
    private final AtomicInteger preAuthSessions = new AtomicInteger(0);
    private final AtomicInteger authenticatedSessions = new AtomicInteger(0);

    private final AtomicInteger globalNewConnThisSecond = new AtomicInteger(0);
    private volatile long globalConnRateWindowStartMs = System.currentTimeMillis();

    private final AtomicLong globalPacketCountThisSecond = new AtomicLong(0);
    private volatile long globalPacketWindowStartMs = System.currentTimeMillis();

    private final LinkedBlockingQueue<String> eventLog = new LinkedBlockingQueue<>(4000);
    private final ScheduledExecutorService scheduler;
    private final Gson gson = new Gson();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private volatile AntiDDoSListener listener;
    private final ConcurrentHashMap<String, Long> lastEventByKey = new ConcurrentHashMap<>();
    private int attackSamples;
    private int emergencySamples;
    private int recoverySamples;
    private long lastAutoPackets;
    private long lastAutoConnections;

    // ─────────────────────────── Constructor / Init ───────────────────────────
    private AntiDDoSEngine() {
        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "AntiDDoS-Engine");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::cleanup, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::autoAdjustMode, 5, 2, TimeUnit.SECONDS);
    }

    // ════════════════════╡ STAGE 1 — Connection Guard ╞════════════════════════
    public boolean checkConnectionAllowed(String ip) {
        if (ip == null) return false;
        totalConnectionAttempts.incrementAndGet();
        boolean trusted = whitelist.contains(ip);
        if (!trusted && permanentBlocks.containsKey(ip)) {
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        IpEntry entry = ipMap.get(ip);
        if (entry == null) {
            if (ipMap.size() >= MAX_TRACKED_IPS) {
                totalPacketsDropped.incrementAndGet();
                return false;
            }
            entry = ipMap.computeIfAbsent(ip, k -> new IpEntry());
        }
        entry.lastActivityMs = System.currentTimeMillis();
        if (!trusted && entry.isTempBlocked()) {
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        // Global new‑connection rate
        if (!trusted && !checkGlobalNewConnRate()) {
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        // Per‑IP new‑connection rate
        if (!trusted && !checkPerIPNewConnRate(entry, ip)) {
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        // Global session cap
        if (!trusted && globalActiveSessions.get() >= mode.maxTotalSessions) {
            totalPacketsDropped.incrementAndGet();
            logEvent("[GLOBAL CAP] Total sessions: " + globalActiveSessions.get() + "/" + mode.maxTotalSessions);
            return false;
        }

        // Pre‑auth cap
        if (!trusted && preAuthSessions.get() >= mode.maxPreAuthSessions) {
            totalPacketsDropped.incrementAndGet();
            logEvent("[PRE-AUTH CAP] " + preAuthSessions.get() + "/" + mode.maxPreAuthSessions);
            return false;
        }

        // Per‑IP connection limit
        if (!trusted && entry.activeConnections.get() >= mode.maxConnectionsPerIP) {
            totalPacketsDropped.incrementAndGet();
            logEvent("[CONN LIMIT] " + ip + " : " + entry.activeConnections.get() + "/" + mode.maxConnectionsPerIP);
            return false;
        }

        // Accept
        entry.activeConnections.incrementAndGet();
        preAuthSessions.incrementAndGet();
        globalActiveSessions.incrementAndGet();
        totalConnectionsAccepted.incrementAndGet();
        return true;
    }

    private synchronized boolean checkGlobalNewConnRate() {
        long now = System.currentTimeMillis();
        if (now - globalConnRateWindowStartMs >= 1000) {
            globalConnRateWindowStartMs = now;
            globalNewConnThisSecond.set(1);
            return true;
        }
        return globalNewConnThisSecond.incrementAndGet() <= mode.maxNewConnPerSec;
    }

    private boolean checkPerIPNewConnRate(IpEntry entry, String ip) {
        synchronized (entry) {
            long now = System.currentTimeMillis();
            if (now - entry.connRateWindowStartMs >= 1000) {
                entry.connRateWindowStartMs = now;
                entry.newConnThisSecond.set(1);
                return true;
            }
            int val = entry.newConnThisSecond.incrementAndGet();
            if (val > mode.maxNewConnPerSecPerIP) {
                logEvent("[IP NEW CONN RATE] " + ip + " : " + val + "/" + mode.maxNewConnPerSecPerIP);
                addReputation(entry, ip, 30, "Too many new connections per second");
                return false;
            }
            return true;
        }
    }

    public void onConnectionClosed(String ip, boolean wasAuthenticated) {
        if (ip == null) return;
        IpEntry entry = ipMap.get(ip);
        if (entry != null) decrementIfPositive(entry.activeConnections);
        decrementIfPositive(globalActiveSessions);
        if (wasAuthenticated) decrementIfPositive(authenticatedSessions);
        else decrementIfPositive(preAuthSessions);
    }

    public void onPlayerAuthenticated(String ip) {
        decrementIfPositive(preAuthSessions);
        authenticatedSessions.incrementAndGet();
    }

    private static void decrementIfPositive(AtomicInteger counter) {
        counter.getAndUpdate(value -> value > 0 ? value - 1 : 0);
    }

    // ════════════════════╡ STAGE 2 — Packet Guard ╞════════════════════════════
    public boolean checkPacketAllowed(String ip, int cmdId, int packetSize) {
        if (ip == null) return true;
        totalPacketObservations.incrementAndGet();
        if (whitelist.contains(ip)) return true;
        if (permanentBlocks.containsKey(ip)) {
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        IpEntry entry = ipMap.computeIfAbsent(ip, k -> new IpEntry());
        long nowMs = System.currentTimeMillis();
        entry.lastActivityMs = nowMs;
        entry.totalPackets.incrementAndGet();

        // Observe global traffic for automatic mode changes. Do not globally
        // discard all players' packets when one aggregate threshold is crossed.
        checkGlobalPacketRate();

        // Packet size
        if (packetSize > mode.maxPacketSize) {
            addReputation(entry, ip, 100, "Oversized packet: " + packetSize + "B (cmd=" + cmdId + ")");
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        // Token bucket
        if (!consumeToken(entry)) {
            addReputation(entry, ip, 20, "Rate limit (cmd=" + cmdId + ")");
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        // Sliding window burst
        updateSlidingWindow(entry, nowMs);
        int recentPps = entry.getRecentPps();
        if (recentPps > mode.burstLimit) {
            addReputation(entry, ip, 50, "Burst: " + recentPps + " pps");
            if (entry.reputation.get() > 200) {
                blockIP(ip, "Burst attack: " + recentPps + " pps", 5 * 60_000L);
            }
            totalPacketsDropped.incrementAndGet();
            return false;
        }

        return true;
    }

    private boolean checkGlobalPacketRate() {
        long now = System.currentTimeMillis();
        if (now - globalPacketWindowStartMs >= 1000) {
            globalPacketWindowStartMs = now;
            globalPacketCountThisSecond.set(1);
            return true;
        }
        long limit = switch (mode) {
            case NORMAL -> 50000;
            case UNDER_ATTACK -> 20000;
            case EMERGENCY -> 5000;
        };
        long count = globalPacketCountThisSecond.incrementAndGet();
        if (count > limit) {
            logEvent("[GLOBAL PACKET RATE] " + count + " pps > " + limit);
            return false;
        }
        return true;
    }

    // Lock‑free token bucket
    private boolean consumeToken(IpEntry entry) {
        synchronized (entry) {
            long nowNanos = System.nanoTime();
            long nsPerToken = 1_000_000_000L / Math.max(1, mode.packetRateLimit);
            int burstCap = mode.burstLimit;
            long lastRefill = entry.lastRefillNanos;
            long elapsed = nowNanos - lastRefill;
            long toAdd = elapsed / nsPerToken;
            long current = entry.tokens.get();
            long newTokens = Math.min(burstCap, current + toAdd);
            if (newTokens <= 0) return false;
            if (toAdd > 0) {
                entry.lastRefillNanos = lastRefill + toAdd * nsPerToken;
            }
            entry.tokens.set(newTokens - 1);
            return true;
        }
    }

    private void updateSlidingWindow(IpEntry entry, long nowMs) {
        synchronized (entry) {
            int idx = (int) ((nowMs / 100) % 10);
            long expectedStart = (nowMs / 100) * 100;
            if (entry.bucketStartMs != expectedStart) {
                long prevStart = entry.bucketStartMs;
                long skipped = (expectedStart - prevStart) / 100;
                if (skipped >= 10) {
                    Arrays.fill(entry.windowBuckets, 0);
                } else {
                    for (int i = 1; i <= skipped; i++) {
                        int bucketIdx = (int) (((prevStart / 100) + i) % 10);
                        entry.windowBuckets[bucketIdx] = 0;
                    }
                }
                entry.bucketStartMs = expectedStart;
                entry.currentBucket = idx;
            }
            entry.windowBuckets[idx]++;
        }
    }

    // Reputation & Block
    public void addReputationPublic(String ip, int points, String reason) {
        if (ip == null) return;
        IpEntry entry = ipMap.computeIfAbsent(ip, k -> new IpEntry());
        addReputation(entry, ip, points, reason);
    }

    private void addReputation(IpEntry entry, String ip, int points, String reason) {
        long now = System.currentTimeMillis();
        long previous = entry.lastPenaltyMs.get();
        if (points < 100 && now - previous < 1_000L) return;
        if (points < 100 && !entry.lastPenaltyMs.compareAndSet(previous, now)) return;
        if (points >= 100) entry.lastPenaltyMs.set(now);
        int newRep = entry.reputation.addAndGet(points);
        entry.violationCount.incrementAndGet();
        logEvent("[⚠] " + ip + " rep+" + points + " (" + reason + ") => " + newRep);
        if (newRep >= 800 && !permanentBlocks.containsKey(ip)) {
            blockIP(ip, "Severe violations (rep=" + newRep + "): " + reason, 60 * 60_000L);
        } else if (newRep >= 400 && !permanentBlocks.containsKey(ip)) {
            blockIP(ip, "High reputation (rep=" + newRep + "): " + reason, 10 * 60_000L);
        } else if (newRep >= 150 && !permanentBlocks.containsKey(ip)) {
            blockIP(ip, "Violations detected (rep=" + newRep + "): " + reason, 5 * 60_000L);
        }
    }

    public void blockIP(String ip, String reason, long durationMs) {
        if (ip == null || ip.isEmpty()) return;
        if (whitelist.contains(ip)) return;
        long now = System.currentTimeMillis();
        permanentBlocks.put(ip, new BlockedInfo(ip, reason, now, durationMs));
        IpEntry entry = ipMap.computeIfAbsent(ip, k -> new IpEntry());
        entry.blockedUntilMs = durationMs < 0 ? Long.MAX_VALUE : now + durationMs;
        entry.blockReason = reason;
        entry.blockTimeMs = now;
        totalIPsEverBlocked.incrementAndGet();
        logEvent("[CHẶN] " + ip + " - " + reason + (durationMs < 0 ? " (vĩnh viễn)" : " (" + (durationMs / 1000) + "s)"));
        // Avoid spawning thousands of netsh processes during a distributed
        // attack. Temporary bans stay in the fast in-process deny map.
        if (durationMs < 0) applyFirewallBlock(ip);
        if (listener != null) listener.onIPBlocked(ip, reason);
        persistAsync();
    }

    public void unblockIP(String ip) {
        if (ip == null) return;
        BlockedInfo removed = permanentBlocks.remove(ip);
        IpEntry entry = ipMap.get(ip);
        if (entry != null) {
            entry.blockedUntilMs = 0;
            entry.reputation.set(0);
            entry.violationCount.set(0);
            entry.blockReason = null;
        }
        logEvent("[BỎ CHẶN] " + ip);
        if (removed != null && removed.durationMs < 0) removeFirewallBlock(ip);
        if (listener != null) listener.onIPUnblocked(ip);
        persistAsync();
    }

    public void blockIPPermanent(String ip, String reason) {
        blockIP(ip, reason, -1L);
    }

    public void unblockAll() {
        Set<String> ips = new HashSet<>(permanentBlocks.keySet());
        ips.forEach(this::unblockIP);
        logEvent("[BỎ CHẶN TẤT CẢ] Gỡ chặn " + ips.size() + " IP.");
    }

    public void addToWhitelist(String ip) {
        if (ip == null) return;
        whitelist.add(ip);
        BlockedInfo removed = permanentBlocks.remove(ip);
        if (removed != null && removed.durationMs < 0) removeFirewallBlock(ip);
        logEvent("[WHITELIST +] " + ip);
        persistAsync();
    }

    public void removeFromWhitelist(String ip) {
        if (ip == null) return;
        whitelist.remove(ip);
        logEvent("[WHITELIST -] " + ip);
        persistAsync();
    }

    // Auto mode
    public void setMode(DefenseMode newMode) {
        DefenseMode old = this.mode;
        this.mode = newMode;
        if (old != newMode) {
            logEvent("[CHẾ ĐỘ] → " + newMode.label);
            if (newMode == DefenseMode.EMERGENCY) applyEmergencyFirewallRules();
            if (old == DefenseMode.EMERGENCY) removeEmergencyFirewallRules();
            if (listener != null) listener.onModeChanged(newMode);
        }
    }

    public DefenseMode getMode() { return mode; }
    public int getMaxInboundPacketSize() { return mode.maxPacketSize; }

    private void autoAdjustMode() {
        try {
            long packetTotal = totalPacketObservations.get();
            long connectionTotal = totalConnectionAttempts.get();
            long totalPps = Math.max(0L, (packetTotal - lastAutoPackets) / 2L);
            long newConnPs = Math.max(0L, (connectionTotal - lastAutoConnections) / 2L);
            lastAutoPackets = packetTotal;
            lastAutoConnections = connectionTotal;
            int preAuth = preAuthSessions.get();
            int authenticated = authenticatedSessions.get();
            long emergencyPps = Math.max(8_000L, authenticated * 80L);
            long attackPps = Math.max(2_000L, authenticated * 40L);
            long recoveryPps = Math.max(600L, authenticated * 20L);

            boolean emergency = totalPps > emergencyPps || newConnPs > 500 || preAuth > 800;
            boolean attack = totalPps > attackPps || newConnPs > 200 || preAuth > 400;
            boolean recovered = totalPps < recoveryPps && newConnPs < 60 && preAuth < 200;

            emergencySamples = emergency ? emergencySamples + 1 : 0;
            attackSamples = attack ? attackSamples + 1 : 0;
            recoverySamples = recovered ? recoverySamples + 1 : 0;

            if (emergencySamples >= 2 && mode != DefenseMode.EMERGENCY) {
                setMode(DefenseMode.EMERGENCY);
            } else if (attackSamples >= 3 && mode == DefenseMode.NORMAL) {
                setMode(DefenseMode.UNDER_ATTACK);
            } else if (recoverySamples >= 15 && mode != DefenseMode.NORMAL) {
                setMode(DefenseMode.NORMAL);
            }
        } catch (Exception ignored) {}
    }

    // Cleanup & idle detection
    private void cleanup() {
        long now = System.currentTimeMillis();
        long staleMs = now - 5 * 60_000L;
        permanentBlocks.entrySet().removeIf(e -> {
            IpEntry entry = ipMap.get(e.getKey());
            if (entry == null) return false;
            boolean expired = entry.blockedUntilMs > 0 && entry.blockedUntilMs < now && e.getValue().durationMs >= 0;
            if (expired) {
                logEvent("[HẾT HẠN] Unblock " + e.getKey());
                if (e.getValue().durationMs < 0) removeFirewallBlock(e.getKey());
                if (listener != null) listener.onIPUnblocked(e.getKey());
            }
            return expired;
        });
        ipMap.entrySet().removeIf(e -> {
            IpEntry entry = e.getValue();
            if (entry.lastActivityMs < staleMs && !permanentBlocks.containsKey(e.getKey())) {
                return true;
            }
            int rep = entry.reputation.get();
            if (rep > 0) entry.reputation.set(Math.max(0, rep - 10));
            return false;
        });
    }

    // Firewall
    private void applyFirewallBlock(String ip) {
        if (!isValidIp(ip)) return;
        scheduler.execute(() -> {
            try {
                ProcessBuilder pb = isWindows()
                        ? new ProcessBuilder("netsh", "advfirewall", "firewall", "add", "rule",
                                "name=AntiDDoS_" + Integer.toHexString(ip.hashCode()),
                                "dir=in", "action=block", "remoteip=" + ip)
                        : new ProcessBuilder("iptables", "-I", "INPUT", "1", "-s", ip, "-j", "DROP");
                pb.redirectErrorStream(true);
                pb.start().waitFor(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logEvent("[FIREWALL ERR] Block " + ip + ": " + e.getMessage());
            }
        });
    }

    private void removeFirewallBlock(String ip) {
        if (!isValidIp(ip)) return;
        scheduler.execute(() -> {
            try {
                ProcessBuilder pb = isWindows()
                        ? new ProcessBuilder("netsh", "advfirewall", "firewall", "delete", "rule",
                                "name=AntiDDoS_" + Integer.toHexString(ip.hashCode()))
                        : new ProcessBuilder("iptables", "-D", "INPUT", "-s", ip, "-j", "DROP");
                pb.redirectErrorStream(true);
                pb.start().waitFor(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logEvent("[FIREWALL ERR] Unblock " + ip + ": " + e.getMessage());
            }
        });
    }

    private void applyEmergencyFirewallRules() {
        scheduler.execute(() -> {
            try {
                if (!isWindows()) {
                    new ProcessBuilder("iptables", "-A", "INPUT", "-p", "tcp", "--syn",
                            "-m", "limit", "--limit", "10/s", "--limit-burst", "20",
                            "-j", "ACCEPT").start().waitFor(3, TimeUnit.SECONDS);
                    new ProcessBuilder("iptables", "-A", "INPUT", "-p", "tcp", "--syn",
                            "-j", "DROP").start().waitFor(3, TimeUnit.SECONDS);
                }
                logEvent("[KHẨN CẤP] Applied SYN‑rate firewall rules.");
            } catch (Exception e) {
                logEvent("[FIREWALL ERR] Emergency rules: " + e.getMessage());
            }
        });
    }

    private void removeEmergencyFirewallRules() {
        scheduler.execute(() -> {
            try {
                if (!isWindows()) {
                    new ProcessBuilder("iptables", "-D", "INPUT", "-p", "tcp", "--syn",
                            "-m", "limit", "--limit", "10/s", "--limit-burst", "20",
                            "-j", "ACCEPT").start().waitFor(3, TimeUnit.SECONDS);
                    new ProcessBuilder("iptables", "-D", "INPUT", "-p", "tcp", "--syn",
                            "-j", "DROP").start().waitFor(3, TimeUnit.SECONDS);
                }
            } catch (Exception ignored) {}
        });
    }

    // Persistence
    private void persistAsync() { scheduler.execute(this::persist); }
    public void persist() {
        try {
            Map<String, Object> state = new HashMap<>();
            List<String> permanent = permanentBlocks.entrySet().stream()
                    .filter(entry -> entry.getValue().durationMs < 0)
                    .map(Map.Entry::getKey)
                    .toList();
            state.put("blocked", permanent);
            state.put("whitelist", new ArrayList<>(whitelist));
            Files.writeString(Paths.get("antiddos_state.json"), gson.toJson(state));
        } catch (IOException e) {
            logEvent("[PERSIST ERR] " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!Files.exists(Paths.get("antiddos_state.json"))) return;
            String json = Files.readString(Paths.get("antiddos_state.json"));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> state = gson.fromJson(json, type);
            if (state == null) return;
            List<String> blocked = (List<String>) state.get("blocked");
            if (blocked != null) blocked.forEach(ip -> blockIP(ip, "Loaded from file", -1L));
            List<String> wl = (List<String>) state.get("whitelist");
            if (wl != null) whitelist.addAll(wl);
            logEvent("[LOAD] " + (blocked != null ? blocked.size() : 0) + " blocked, " + whitelist.size() + " whitelist.");
        } catch (Exception e) {
            logEvent("[LOAD ERR] " + e.getMessage());
        }
    }

    // Getters & helpers
    public GlobalStats getGlobalStats() {
        return new GlobalStats(
                totalPacketsDropped.get(),
                totalIPsEverBlocked.get(),
                totalConnectionsAccepted.get(),
                globalActiveSessions.get(), mode,
                ipMap.size(), permanentBlocks.size(), whitelist.size()
        );
    }
    public Map<String, IpEntry> getIpMap() { return Collections.unmodifiableMap(ipMap); }
    public Map<String, BlockedInfo> getBlockedIPs() { return Collections.unmodifiableMap(permanentBlocks); }
    public Set<String> getWhitelist() { return Collections.unmodifiableSet(whitelist); }
    public boolean isBlocked(String ip) { return ip != null && permanentBlocks.containsKey(ip); }
    public boolean isWhitelisted(String ip) { return ip != null && whitelist.contains(ip); }
    public List<String> drainLog(int max) { List<String> result = new ArrayList<>(max); eventLog.drainTo(result, max); return result; }
    public void setListener(AntiDDoSListener listener) { this.listener = listener; }
    public void shutdown() { persist(); scheduler.shutdownNow(); }

    private void logEvent(String msg) {
        if (msg.startsWith("[IP NEW CONN RATE]") || msg.startsWith("[GLOBAL ")
                || msg.startsWith("[CONN LIMIT]") || msg.startsWith("[⚠]")) {
            int end = msg.indexOf(']');
            String key = end > 0 ? msg.substring(0, end + 1) : msg;
            long now = System.currentTimeMillis();
            Long previous = lastEventByKey.put(key, now);
            if (previous != null && now - previous < 1_000L) return;
        }
        String formatted = "[" + LocalTime.now().format(dateFmt) + "] " + msg;
        if (!eventLog.offer(formatted)) { eventLog.poll(); eventLog.offer(formatted); }
        Logger.logln(formatted);
    }
    private boolean isWindows() { return System.getProperty("os.name", "").toLowerCase().contains("win"); }
    private boolean isValidIp(String ip) {
        try {
            return ip != null && !ip.isBlank() && InetAddress.getByName(ip).getHostAddress() != null;
        } catch (Exception ignored) {
            return false;
        }
    }
}
