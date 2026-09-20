package managers;

import dungeon.MajinBuu14H;
import services.dungeon.MajinBuu14HService;
import utils.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class MajinBuu14HMonitor {

    private static MajinBuu14HMonitor instance;

    public static MajinBuu14HMonitor gI() {
        if (instance == null) {
            instance = new MajinBuu14HMonitor();
        }
        return instance;
    }

    private final AtomicLong scaleUpCount = new AtomicLong(0);
    private final AtomicLong scaleDownCount = new AtomicLong(0);
    private final AtomicLong totalPlayersServed = new AtomicLong(0);
    private final AtomicLong peakInstanceCount = new AtomicLong(0);
    private final AtomicLong peakPlayerCount = new AtomicLong(0);

    private long monitorStartTime;

    private MajinBuu14HMonitor() {
        this.monitorStartTime = System.currentTimeMillis();
    }

    public void reset() {
        scaleUpCount.set(0);
        scaleDownCount.set(0);
        totalPlayersServed.set(0);
        peakInstanceCount.set(0);
        peakPlayerCount.set(0);
        monitorStartTime = System.currentTimeMillis();
        Logger.logln("[MajinBuu14HMonitor] Metrics reset");
    }

    public void recordScaleUp(int newInstanceCount) {
        scaleUpCount.incrementAndGet();
        updatePeakInstanceCount(newInstanceCount);
        Logger.logln("[MajinBuu14HMonitor] ⬆ SCALE UP #" + scaleUpCount.get() + " | Instances: " + newInstanceCount);
    }

    public void recordScaleDown(int newInstanceCount) {
        scaleDownCount.incrementAndGet();
        Logger.logln(
                "[MajinBuu14HMonitor] ⬇ SCALE DOWN #" + scaleDownCount.get() + " | Instances: " + newInstanceCount);
    }

    private void updatePeakInstanceCount(int count) {
        long current = peakInstanceCount.get();
        if (count > current) {
            peakInstanceCount.set(count);
        }
    }

    private void updatePeakPlayerCount(int count) {
        long current = peakPlayerCount.get();
        if (count > current) {
            peakPlayerCount.set(count);
        }
    }

    public void recordPlayerJoin() {
        totalPlayersServed.incrementAndGet();
    }

    public void updateStats(int currentInstances, int currentPlayers) {
        updatePeakInstanceCount(currentInstances);
        updatePeakPlayerCount(currentPlayers);
    }

    public String getDetailedReport() {
        long uptime = System.currentTimeMillis() - monitorStartTime;
        int currentInstances = MajinBuu14HService.gI().maBu2Hs.size();
        int currentPlayers = 0;
        int runningInstances = 0;

        for (MajinBuu14H inst : MajinBuu14HService.gI().maBu2Hs) {
            currentPlayers += inst.getTotalPlayers();
            if (inst.isRunning()) {
                runningInstances++;
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("\n╔═══════════════════════════════════════════════════════════════╗\n");
        report.append("║          MAJIN BUU 14H AUTO-SCALING MONITOR REPORT          ║\n");
        report.append("╠═══════════════════════════════════════════════════════════════╣\n");

        // System Info
        report.append(String.format("║ Uptime: %s                                   ║\n", formatUptime(uptime)));
        report.append("╠═══════════════════════════════════════════════════════════════╣\n");

        // Current Status
        report.append("║ CURRENT STATUS                                                ║\n");
        report.append(String.format("║   Instances: %d/%d running (Min: %d, Max: %d)              ║\n",
                runningInstances, currentInstances,
                MajinBuu14H.MIN_INSTANCES, MajinBuu14H.MAX_INSTANCES));
        report.append(
                String.format("║   Active Players: %d                                        ║\n", currentPlayers));
        report.append(String.format("║   Avg Players/Instance: %.1f                               ║\n",
                currentInstances > 0 ? (double) currentPlayers / currentInstances : 0.0));
        report.append("╠═══════════════════════════════════════════════════════════════╣\n");

        // Scaling Events
        report.append("║ SCALING EVENTS                                                ║\n");
        report.append(
                String.format("║   Scale UP events: %d                                      ║\n", scaleUpCount.get()));
        report.append(String.format("║   Scale DOWN events: %d                                    ║\n",
                scaleDownCount.get()));
        report.append(String.format("║   Total scaling operations: %d                             ║\n",
                scaleUpCount.get() + scaleDownCount.get()));
        report.append("╠═══════════════════════════════════════════════════════════════╣\n");

        // Peak Statistics
        report.append("║ PEAK STATISTICS                                               ║\n");
        report.append(String.format("║   Peak instances: %d                                       ║\n",
                peakInstanceCount.get()));
        report.append(String.format("║   Peak players: %d                                         ║\n",
                peakPlayerCount.get()));
        report.append(String.format("║   Total players served: %d                                 ║\n",
                totalPlayersServed.get()));
        report.append("╠═══════════════════════════════════════════════════════════════╣\n");

        // Resource Efficiency
        double avgScaleFrequency = uptime > 0
                ? (double) (scaleUpCount.get() + scaleDownCount.get()) / (uptime / 3600000.0)
                : 0;
        report.append("║ RESOURCE EFFICIENCY                                           ║\n");
        report.append(
                String.format("║   Avg scaling frequency: %.2f events/hour                  ║\n", avgScaleFrequency));
        report.append(String.format("║   Instance utilization: %.1f%%                             ║\n",
                currentInstances > 0
                        ? (double) currentPlayers / (currentInstances * MajinBuu14H.PLAYERS_PER_INSTANCE) * 100
                        : 0));
        report.append("╚═══════════════════════════════════════════════════════════════╝\n");

        return report.toString();
    }

    /**
     * Get compact stats (for regular logging)
     */
    public String getCompactStats() {
        int currentInstances = MajinBuu14HService.gI().maBu2Hs.size();
        int currentPlayers = 0;

        for (MajinBuu14H inst : MajinBuu14HService.gI().maBu2Hs) {
            currentPlayers += inst.getTotalPlayers();
        }

        return String.format("[Monitor] Instances: %d | Players: %d | Scale: ↑%d ↓%d | Peak: I=%d P=%d",
                currentInstances, currentPlayers,
                scaleUpCount.get(), scaleDownCount.get(),
                peakInstanceCount.get(), peakPlayerCount.get());
    }

    /**
     * Format uptime thành human-readable string
     */
    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Get instance details
     */
    public String getInstanceDetails() {
        StringBuilder details = new StringBuilder();
        details.append("\n[Instance Details]\n");

        for (MajinBuu14H inst : MajinBuu14HService.gI().maBu2Hs) {
            long idleTime = System.currentTimeMillis() - inst.getLastActivityTime();
            details.append(String.format("  #%d: %s | Players: %d/%d | Zones: %d | Idle: %s\n",
                    inst.getId(),
                    inst.isRunning() ? "RUNNING" : "STOPPED",
                    inst.getTotalPlayers(),
                    MajinBuu14H.PLAYERS_PER_INSTANCE,
                    inst.getZones().size(),
                    formatUptime(idleTime)));
        }

        return details.toString();
    }

    // Getters cho metrics
    public long getScaleUpCount() {
        return scaleUpCount.get();
    }

    public long getScaleDownCount() {
        return scaleDownCount.get();
    }

    public long getTotalPlayersServed() {
        return totalPlayersServed.get();
    }

    public long getPeakInstanceCount() {
        return peakInstanceCount.get();
    }

    public long getPeakPlayerCount() {
        return peakPlayerCount.get();
    }
}
