package combine;
import item.Item;
import item.Item.ItemOption;

public class CombineSystem {

    public static boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem() && item.template != null) {
            return item.template.type < 5 || item.template.type == 32;
        } else {
            return false;
        }
    }

    public static boolean isDaPhaLe(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        return item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20);
    }

    public static boolean isDotl(Item item) {
        if (item.template == null) {
            return false;
        }
        return item.template.id >= 555 && item.template.id <= 567;
    }

    public static boolean isDaKichHoat(Item item) {
        if (item.template == null) {
            return false;
        }
        return item.template.id >= 1772;
    }

    public static boolean isDestroyClothes(int id) {
        return id >= 650 && id <= 662;
    }

    public static int getGemEpSao(int star) {
        switch (star) {
            case 0 -> {
                return 10;
            }
            case 1 -> {
                return 10;
            }
            case 2 -> {
                return 10;
            }
            case 3 -> {
                return 10;
            }
            case 4 -> {
                return 10;
            }
            case 5 -> {
                return 10;
            }
            case 6 -> {
                return 10;
            }
            case 7 -> {
                return 10;
            }
            case 8 -> {
                return 10;
            }
        }
        return 0;
    }

    public static int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe == null || daPhaLe.template == null) {
            return -1;
        }
        if (daPhaLe.template.type == 30) {
            if (daPhaLe.itemOptions != null && !daPhaLe.itemOptions.isEmpty()) {
                return daPhaLe.itemOptions.get(0).optionTemplate.id;
            }
            return -1;
        }
        return switch (daPhaLe.template.id) {
            case 20 -> 77;
            case 19 -> 103;
            case 18 -> 80;
            case 17 -> 81;
            case 16 -> 50;
            case 15 -> 94;
            case 14 -> 108;
            default -> -1;
        };
    }

    public static int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe == null || daPhaLe.template == null) {
            return -1;
        }
        if (daPhaLe.template.type == 30) {
            if (daPhaLe.itemOptions != null && !daPhaLe.itemOptions.isEmpty()) {
                return daPhaLe.itemOptions.get(0).param;
            }
            return -1;
        }
        return switch (daPhaLe.template.id) {
            case 20 -> 5;
            case 19 -> 5;
            case 18 -> 5;
            case 17 -> 5;
            case 16 -> 3;
            case 15 -> 2;
            case 14 -> 2;
            default -> -1;
        };
    }

    public static int getGoldPhaLeHoa(int star) {
        switch (star) {
            case 0 -> {
                return 5000000;
            }
            case 1 -> {
                return 10000000;
            }
            case 2 -> {
                return 20000000;
            }
            case 3 -> {
                return 40000000;
            }
            case 4 -> {
                return 60000000;
            }
            case 5 -> {
                return 90000000;
            }
            case 6 -> {
                return 120000000;
            }
            case 7 -> {
                return 200000000;
            }
            case 8 -> {
                return 300000000;
            }
        }
        return 0;
    }

    public static float getRatioPhaLeHoa(int star) {
        switch (star) {
            case 0 -> {
                return 50f;
            }
            case 1 -> {
                return 20f;
            }
            case 2 -> {
                return 10f;
            }
            case 3 -> {
                return 5f;
            }
            case 4 -> {
                return 2f;
            }
            case 5 -> {
                return 1f;
            }
            case 6 -> {
                return 1f;
            }
            case 7 -> {
                return 0.5f;
            }
            case 8 -> {
                return 100f;
            }
            case 9 -> {
                return 0.5f;
            }
            case 10 -> {
                return 0.2f;
            }
            case 11 -> {
                return 0.1f;
            }
            case 12 -> {
                return 0.1f;
            }
        }
        return 0;
    }

    public static int getGemPhaLeHoa(int star) {
        switch (star) {
            case 0 -> {
                return 1;
            }
            case 1 -> {
                return 2;
            }
            case 2 -> {
                return 3;
            }
            case 3 -> {
                return 4;
            }
            case 4 -> {
                return 5;
            }
            case 5 -> {
                return 6;
            }
            case 6 -> {
                return 7;
            }
            case 7 -> {
                return 8;
            }
            case 8 -> {
                return 9;
            }
            case 9 -> {
                return 280;
            }
            case 10 -> {
                return 560;
            }
            case 11 -> {
                return 1120;
            }
            case 12 -> {
                return 2240;
            }
        }
        return 0;
    }

    public static boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null
                && trangBi.template != null && daNangCap.template != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else {
                return trangBi.template.type == 4 && daNangCap.template.id == 220;
            }
        } else {
            return false;
        }
    }

    public static int getCountDaNangCapDo(int level) {
        switch (level) {
            case 0 -> {
                return 3;
            }
            case 1 -> {
                return 7;
            }
            case 2 -> {
                return 11;
            }
            case 3 -> {
                return 17;
            }
            case 4 -> {
                return 23;
            }
            case 5 -> {
                return 35;
            }
            case 6 -> {
                return 50;
            }
            case 7 -> {
                return 70;
            }
        }
        return 0;
    }

    public static int getCountDaBaoVe(int level) {
        return 1;
    }

    public static int getGoldNangCapDo(int level) {
        switch (level) {
            case 0 -> {
                return 10000;
            }
            case 1 -> {
                return 70000;
            }
            case 2 -> {
                return 300000;
            }
            case 3 -> {
                return 1500000;
            }
            case 4 -> {
                return 7000000;
            }
            case 5 -> {
                return 23000000;
            }
            case 6 -> {
                return 100000000;
            }
            case 7 -> {
                return 250000000;
            }
        }
        return 0;
    }

    public static double getTileNangCapDo(int level) {
        switch (level) {
            case 0 -> {
                return 80;
            }
            case 1 -> {
                return 50;
            }
            case 2 -> {
                return 20;
            }
            case 3 -> {
                return 10;
            }
            case 4 -> {
                return 7;
            }
            case 5 -> {
                return 5;
            }
            case 6 -> {
                return 1;
            }
            case 7 -> {
                return 0.3;
            }
            case 8 -> {
                return 5;
            }
            case 9 -> {
                return 1;
            }
            case 10 -> {
                return 0.3;
            }
            case 11 -> {
                return 0.3;
            }
            case 12 -> {
                return 0.3;
            }
        }
        return 0;
    }

    public static int getTempIdItemC3(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0 -> {
                switch (type) {
                    case 0 -> {
                        return 0;
                    }
                    case 1 -> {
                        return 6;
                    }
                    case 2 -> {
                        return 21;
                    }
                    case 3 -> {
                        return 27;
                    }
                }
            }
            case 1 -> {
                switch (type) {
                    case 0 -> {
                        return 1;
                    }
                    case 1 -> {
                        return 7;
                    }
                    case 2 -> {
                        return 22;
                    }
                    case 3 -> {
                        return 28;
                    }
                }
            }
            case 2 -> {
                switch (type) {
                    case 0 -> {
                        return 2;
                    }
                    case 1 -> {
                        return 8;
                    }
                    case 2 -> {
                        return 23;
                    }
                    case 3 -> {
                        return 29;
                    }
                }
            }
        }
        return -1;
    }

    public static String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada";
        }
        switch (gender) {
            case 0 -> {
                switch (type) {
                    case 0 -> {
                        return "Áo";
                    }
                    case 1 -> {
                        return "Quần";
                    }
                    case 2 -> {
                        return "Găng";
                    }
                    case 3 -> {
                        return "Giầy";
                    }
                }
            }
            case 1 -> {
                switch (type) {
                    case 0 -> {
                        return "Áo";
                    }
                    case 1 -> {
                        return "Quần";
                    }
                    case 2 -> {
                        return "Găng";
                    }
                    case 3 -> {
                        return "Giầy";
                    }
                }
            }
            case 2 -> {
                switch (type) {
                    case 0 -> {
                        return "Áo";
                    }
                    case 1 -> {
                        return "Quần";
                    }
                    case 2 -> {
                        return "Găng";
                    }
                    case 3 -> {
                        return "Giầy";
                    }
                }
            }
        }
        return "";
    }

    public static boolean CheckSlot(Item trangBi, int starEmpty) {
        if (starEmpty < 8) {
            return true;
        }
        if (trangBi == null || trangBi.itemOptions == null) {
            return false;
        }
        for (ItemOption io : trangBi.itemOptions) {
            if (io != null && io.optionTemplate != null) {
                if (starEmpty == 8 && io.optionTemplate.id == 228) {
                    return io.param >= 8;
                } else if (starEmpty == 9 && io.optionTemplate.id == 228) {
                    return io.param >= 9;
                }
            }
        }
        return false;
    }

    public static String getTypeTrangBi(int type) {
        switch (type) {
            case 0 -> {
                return "Áo";
            }
            case 1 -> {
                return "Quần";
            }
            case 2 -> {
                return "Găng";
            }
            case 3 -> {
                return "Giày";
            }
            case 4 -> {
                return "Nhẫn";
            }
        }
        return "";
    }

}
