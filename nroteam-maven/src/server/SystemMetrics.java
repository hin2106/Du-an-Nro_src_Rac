package server;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class SystemMetrics {

    public static String getMetrics() {
        try {
            Runtime runtime = Runtime.getRuntime();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            double systemCpuLoad = -1;
            double processCpuLoad = -1;
            long systemTotalRam = -1;
            long systemFreeRam = -1;

            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                try {
                    // Các phương thức mới không deprecated (Java 17–21)
                    systemCpuLoad = sunOsBean.getCpuLoad() * 100;
                    processCpuLoad = sunOsBean.getProcessCpuLoad() * 100;
                    systemTotalRam = sunOsBean.getTotalMemorySize() / (1024 * 1024);
                    systemFreeRam = sunOsBean.getFreeMemorySize() / (1024 * 1024);
                } catch (Exception ignored) {
                    // fallback nếu JVM không hỗ trợ
                }
            }

            // ===== Java Memory =====
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;

            int threadCount = Thread.activeCount();
            int cpuCore = osBean.getAvailableProcessors();

            double javaMemUsagePercent = maxMemory > 0 ? (usedMemory * 100.0 / maxMemory) : 0;
            double systemMemUsagePercent = (systemTotalRam > 0)
                    ? ((systemTotalRam - systemFreeRam) * 100.0 / systemTotalRam)
                    : 0;

            // ===== Format output =====
            StringBuilder sb = new StringBuilder();
            sb.append("=== HỆ THỐNG ===\n");
            sb.append("CPU Core: ").append(cpuCore).append("\n");

            if (systemCpuLoad >= 0)
                sb.append(String.format("CPU Hệ thống: %.1f%% | ", systemCpuLoad));
            if (processCpuLoad >= 0)
                sb.append(String.format("CPU Java: %.1f%%\n", processCpuLoad));

            if (systemTotalRam > 0) {
                sb.append(String.format("RAM Máy: %.1f%% (%d/%d MB)\n",
                        systemMemUsagePercent, (systemTotalRam - systemFreeRam), systemTotalRam));
            }

            sb.append(String.format("RAM Java: %.1f%% (%d/%d MB)\n",
                    javaMemUsagePercent, usedMemory, maxMemory));

            sb.append("Threads: ").append(threadCount);

            return sb.toString();

        } catch (Exception e) {
            return "Lỗi lấy thông tin hệ thống: " + e.getMessage();
        }
    }

    public static String ToString() {
        return getMetrics();
    }

    @Override
    public String toString() {
        return getMetrics();
    }
}
