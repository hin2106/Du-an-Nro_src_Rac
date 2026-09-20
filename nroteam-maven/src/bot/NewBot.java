package bot;

import java.util.Random;
import server.Manager;
import system.Template;

public class NewBot {

    private static NewBot instance;

    public boolean LOAD_PART = true;
    public int MAXPART = 0;
    public static int[][] PARTBOT;

    private final Random random = new Random();

    private final String[] FULL_NAMES = {
            "hung123", "mamama", "ductaidz", "bap11", "vip888", "admin2k5",
            "Sharen", "hungdzok", "tam123", "tuanblue2", "cuongpvp", "namkame99",
            "thanhz123", "datz777", "linhbluez", "phongdz11", "duongxpro", "HIEUDAUBUOI"
    };

    public static NewBot gI() {
        if (instance == null) {
            instance = new NewBot();
        }
        return instance;
    }

    private NewBot() {
        PARTBOT = new int[Manager.ITEM_TEMPLATES.size()][4];
    }

    public void loadPart() {
        if (!LOAD_PART)
            return;
        int i = 0;
        for (Template.ItemTemplate it : Manager.ITEM_TEMPLATES) {
            if (it.type == 5 && it.head != -1 && it.body != -1 && it.leg != -1 && it.leg != 194) {
                if (it.gender == 0 || it.gender == 2) {
                    PARTBOT[i][0] = 383; // head
                    PARTBOT[i][1] = 384; // body
                    PARTBOT[i][2] = 385; // leg
                } else {
                    PARTBOT[i][0] = 391;
                    PARTBOT[i][1] = 392;
                    PARTBOT[i][2] = 393;
                }
                PARTBOT[i][3] = it.gender;
                i++;
                MAXPART++;
            }
        }
        LOAD_PART = false;
    }

    public String getRandomName() {
        return FULL_NAMES[random.nextInt(FULL_NAMES.length)] + random.nextInt(999);
    }

    public int getIndex(int gender) {
        if (MAXPART == 0) {
            System.err.println("[NewBot] MAXPART = 0, không thể tạo bot!");
            return 0;
        }
        int idx = random.nextInt(MAXPART);
        int gend = PARTBOT[idx][3];
        return (gend == gender) ? idx : getIndex(gender);
    }

    public void runBot(int type, int slot) {
        try {
            loadPart();

            if (MAXPART == 0) {
                System.err.println("[NewBot] Không có PART nào để tạo bot!");
                return;
            }

            for (int i = 0; i < slot; i++) {
                try {
                    int gender = random.nextInt(3);
                    int randomPart = getIndex(gender);
                    int head = PARTBOT[randomPart][0];
                    int body = PARTBOT[randomPart][1];
                    int leg = PARTBOT[randomPart][2];
                    Bot bot = new Bot((short) head, (short) body, (short) leg, type, getRandomName(), null);
                    bot.gender = (byte) gender;
                    int bonus = random.nextInt(50_000_000);
                    bot.nPoint.limitPower = 6;
                    bot.nPoint.power = 1_000 + bonus;
                    bot.nPoint.tiemNang = 20_000_000 + bonus;
                    bot.nPoint.dameg = 10;
                    bot.nPoint.defg = 10;
                    bot.nPoint.hpg = 10_000;
                    bot.nPoint.mpg = 200_000;
                    bot.nPoint.hpMax = 10_000;
                    bot.nPoint.mpMax = 200_000;
                    bot.nPoint.hp = bot.nPoint.hpMax;
                    bot.nPoint.mp = bot.nPoint.mpMax;
                    bot.nPoint.stamina = bot.nPoint.maxStamina = 20_000;
                    bot.nPoint.critg = 10;

                    bot.leakSkill();

                    switch (type) {
                        case 0 -> bot.mobb = new Mobb(bot);
                        case 1 -> bot.sanb = new Sanb(bot);
                        case 2 -> {
                            ChatBot cb = new ChatBot();
                            cb.bot = bot;
                            bot.chatBot = cb;
                        }
                    }

                    bot.joinMap();

                    if (type == 0 && bot.mobb != null && bot.zone != null) {
                        bot.mobb.scaleStatsToZone(bot.zone);
                    }

                    BotManager.gI().addBot(bot);
                } catch (Exception e) {
                    System.err.println("[NewBot] Lỗi tạo bot thứ " + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("[NewBot] Lỗi trong runBot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
