package shop;

import java.nio.file.*;

public class ConfigWatcher implements Runnable {

    private final String folder = "data";

    @Override
    public void run() {

        try {

            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(folder);

            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {

                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {

                    Path changed = (Path) event.context();

                    if (changed.toString().equals("santa_shop.cfg")) {

                        System.out.println("Detected santa_shop.cfg change → reload");

                        SantaShopConfig.load();
                    }
                }

                key.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}