package network;

import java.util.concurrent.*;
import utils.Logger;

/**
 * Centralized Thread Pool for Network Operations
 * Replaces per-session threads with shared thread pool for better resource
 * management
 */
public class NetworkThreadPool {

    private static volatile NetworkThreadPool instance;

    // Thread pool for message processing (QueueHandler)
    private ExecutorService messageProcessorPool;

    // Thread pool for I/O operations if needed in future
    private ExecutorService ioOperationPool;

    // Default pool sizes - can be tuned based on server load
    private static final int MESSAGE_PROCESSOR_THREADS = Math.max(4,
            Math.min(16, Runtime.getRuntime().availableProcessors() * 2));
    private static final int IO_THREADS = Math.max(2,
            Math.min(8, Runtime.getRuntime().availableProcessors()));

    private NetworkThreadPool() {
        initializePools();
    }

    public static NetworkThreadPool gI() {
        // Double-checked locking pattern - safe with volatile or synchronized
        NetworkThreadPool localInstance = instance;
        if (localInstance == null) {
            synchronized (NetworkThreadPool.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NetworkThreadPool();
                }
            }
        }
        return localInstance;
    }

    private void initializePools() {
        // Message processor pool with bounded queue to prevent memory issues
        this.messageProcessorPool = new ThreadPoolExecutor(
                MESSAGE_PROCESSOR_THREADS,
                MESSAGE_PROCESSOR_THREADS * 2, // Allow expansion under load
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000), // Bounded queue
                new ThreadFactory() {
                    private int threadNum = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "MessageProcessor-" + (threadNum++));
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Fallback if queue full
        );

        // I/O operation pool (for future use)
        this.ioOperationPool = Executors.newFixedThreadPool(
                IO_THREADS,
                new ThreadFactory() {
                    private int threadNum = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "IOOperation-" + (threadNum++));
                        t.setDaemon(true);
                        return t;
                    }
                });

        Logger.success("NetworkThreadPool initialized: " + MESSAGE_PROCESSOR_THREADS + " message processor threads\n");
    }

    /**
     * Submit message processing task to shared pool
     * Replaces per-session QueueHandler threads
     */
    public void submitMessageTask(Runnable task) {
        try {
            this.messageProcessorPool.submit(task);
        } catch (RejectedExecutionException e) {
            Logger.error("Message processor pool rejected task - queue may be full\n");
            // Fallback: execute in current thread if pool is overwhelmed
            task.run();
        }
    }

    /**
     * Submit I/O operation to pool (for future use)
     */
    public void submitIOTask(Runnable task) {
        this.ioOperationPool.submit(task);
    }

    /**
     * Get current pool statistics for monitoring
     */
    public String getPoolStats() {
        if (messageProcessorPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) messageProcessorPool;
            return String.format(
                    "MessagePool: Active=%d, Pool=%d, Queue=%d, Completed=%d",
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getQueue().size(),
                    tpe.getCompletedTaskCount());
        }
        return "Pool stats not available";
    }

    /**
     * Shutdown all pools gracefully
     */
    public void shutdown() {
        Logger.warning("Shutting down NetworkThreadPool...\n");
        shutdownPool(messageProcessorPool, "MessageProcessor");
        shutdownPool(ioOperationPool, "IOOperation");
    }

    private void shutdownPool(ExecutorService pool, String name) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    Logger.warning(name + " pool did not terminate gracefully, forcing shutdown\n");
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public ExecutorService getMessageProcessorPool() {
        return messageProcessorPool;
    }
}
