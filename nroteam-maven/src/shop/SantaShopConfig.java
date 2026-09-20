package shop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import utils.Util;

public class SantaShopConfig {

    private static final String PATH = "data/config/santa_shop.cfg";

    public static void load() {

        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(PATH))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] data = smartSplit(line);

                if (data.length < 5) {
                    System.out.println("Invalid config line: " + line);
                    continue;
                }

                int itemId = Integer.parseInt(data[0]);
                byte typeSell = Byte.parseByte(data[1]);
                int cost = Integer.parseInt(data[2]);

                int[][] options = parseOptions(data[3]);

                boolean isNew = Boolean.parseBoolean(data[4]);

                items.add(new TabShopSanta.SantaItemConfig(
                        itemId,
                        typeSell,
                        cost,
                        options,
                        isNew
                ));
            }

            TabShopSanta.replaceItems(items);

            System.out.println("SantaShop loaded: " + items.size());

        } catch (Exception e) {
            System.out.println("Error loading Santa Shop config");
            e.printStackTrace();
        }
    }

    /**
     * Split line nhưng bỏ qua dấu phẩy trong ()
     */
    private static String[] smartSplit(String line) {

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int bracket = 0;

        for (char c : line.toCharArray()) {

            if (c == '(') bracket++;
            if (c == ')') bracket--;

            if (c == ',' && bracket == 0) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        parts.add(current.toString());

        return parts.toArray(new String[0]);
    }

    private static int[][] parseOptions(String text) {

        String[] opts = text.split("\\|");

        List<int[]> list = new ArrayList<>();

        for (String opt : opts) {

            String[] pair = opt.split(":");

            if (pair.length < 2) continue;

            int id = Integer.parseInt(pair[0]);
            int param;

            if (pair[1].startsWith("rand")) {

                String s = pair[1]
                        .replace("rand(", "")
                        .replace(")", "");

                String[] r = s.split(",");

                param = Util.nextInt(
                        Integer.parseInt(r[0]),
                        Integer.parseInt(r[1])
                );

            } else {
                param = Integer.parseInt(pair[1]);
            }

            list.add(new int[]{id, param});
        }

        return list.toArray(new int[list.size()][]);
    }
}