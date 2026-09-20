package network;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ResourceSendLimiter {

    private static ResourceSendLimiter instance;
    private static final int MAX_CONCURRENT_RESOURCE_SENDS = 20;

    private final Semaphore resourceSendSemaphore;

    private ResourceSendLimiter() {
        this.resourceSendSemaphore = new Semaphore(MAX_CONCURRENT_RESOURCE_SENDS, true);
    }

    public static ResourceSendLimiter gI() {
        if (instance == null) {
            synchronized (ResourceSendLimiter.class) {
                if (instance == null) {
                    instance = new ResourceSendLimiter();
                }
            }
        }
        return instance;
    }

    /**
     * Thử acquire permit để gửi resource
     * 
     * @return true nếu có thể gửi, false nếu đang quá tải
     */
    public boolean tryAcquire() {
        return resourceSendSemaphore.tryAcquire();
    }

    public boolean tryAcquire(long timeoutMs) {
        try {
            return resourceSendSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void release() {
        resourceSendSemaphore.release();
    }

    public int getAvailablePermits() {
        return resourceSendSemaphore.availablePermits();
    }

    /**
     * Lấy số lượng resource đang được gửi
     */
    public int getActiveSends() {
        return MAX_CONCURRENT_RESOURCE_SENDS - resourceSendSemaphore.availablePermits();
    }
}
