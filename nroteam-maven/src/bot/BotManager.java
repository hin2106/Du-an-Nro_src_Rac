package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import server.Maintenance;

public class BotManager implements Runnable {

    private static volatile BotManager instance;

    private final List<Bot> bots = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = true;
    private Thread thread;

    private BotManager() {
        start();
    }

    public static BotManager gI() {
        if (instance == null) {
            synchronized (BotManager.class) {
                if (instance == null) {
                    instance = new BotManager();
                }
            }
        }
        return instance;
    }

    public void addBot(Bot bot) {
        if (bot == null) {
            return;
        }
        bots.add(bot);
    }

    public void removeBot(Bot bot) {
        if (bot == null) {
            return;
        }
        bots.remove(bot);
    }

    public int getBotCount() {
        return bots.size();
    }

    public List<Bot> getBotCopy() {
        synchronized (bots) {
            return new ArrayList<>(bots);
        }
    }

    public void removeAllBots() {
        synchronized (bots) {
            bots.clear();
        }
    }

    public int getBotCountByType(int type) {
        int count = 0;
        synchronized (bots) {
            for (Bot bot : bots) {
                if (bot != null && bot.getType() == type) {
                    count++;
                }
            }
        }
        return count;
    }

    private void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, "BotManager-Thread");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (running && !Maintenance.isRunning()) {
            try {
                long st = System.currentTimeMillis();
                List<Bot> copy = getBotCopy();

                for (Bot b : copy) {
                    try {
                        if (b == null) {
                            continue;
                        }
                        b.update();
                    } catch (Exception ex) {
                        System.err.println("[BotManager] Lỗi update bot: " + (b != null ? b.name : "null"));
                        ex.printStackTrace();
                        if (b != null) {
                            removeBot(b);
                        }
                    }
                }

                long elapsed = System.currentTimeMillis() - st;
                long delay = Math.max(15, 150 - elapsed);
                Thread.sleep(delay);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.err.println("[BotManager] Lỗi trong vòng lặp chính");
                ex.printStackTrace();
            }
        }
    }

}
