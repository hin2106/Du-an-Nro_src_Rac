package item;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import services.ItemService;
import system.Template;
import system.Template.ItemTemplate;
import utils.Util;

public class Item {

    public ItemTemplate template;

    public String info;

    public String content;

    public int quantity;

    public int quantityGD = 0;

    public List<ItemOption> itemOptions;

    public long createTime;
    public int id;
    /** Ma lo dung de truy vet vat pham qua vut/nhat/giao dich. */
    public String auditTraceId;

    public String ensureAuditTraceId() {
        if (auditTraceId == null || auditTraceId.isBlank()) {
            auditTraceId = UUID.randomUUID().toString();
        }
        return auditTraceId;
    }

    public boolean isNotNullItem() {
        return this.template != null;
    }

    public Item() {
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public Item(short itemId) {
        this.template = ItemService.gI().getTemplate(itemId);
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public String getInfo() {
        String strInfo = "";
        for (ItemOption itemOption : itemOptions) {
            strInfo += itemOption.getOptionString();
        }
        return strInfo;
    }

    public String getContent() {
        return "Yêu cầu sức mạnh " + this.template.strRequire + " trở lên";
    }

    public void dispose() {
        this.template = null;
        this.info = null;
        this.content = null;
        if (this.itemOptions != null) {
            for (ItemOption io : this.itemOptions) {
                io.dispose();
            }
            this.itemOptions.clear();
        }
        this.itemOptions = null;
    }

    public static class ItemOption {

        public int param;

        public Template.ItemOptionTemplate optionTemplate;

        public ItemOption() {
        }

        public ItemOption(ItemOption io) {
            this.param = io.param;
            this.optionTemplate = io.optionTemplate;
        }

        public ItemOption(int tempId, int param) {
            this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
            this.param = param;
        }

        

        public ItemOption(Template.ItemOptionTemplate temp, int param) {
            this.optionTemplate = temp;
            this.param = param;
        }

        public String getOptionString() {
            return Util.replace(this.optionTemplate.name, "#", String.valueOf(this.param));
        }

        public void dispose() {
            this.optionTemplate = null;
        }

        @Override
        public String toString() {
            final String n = "\"";
            return "{"
                    + n + "id" + n + ":" + n + optionTemplate.id + n + ","
                    + n + "param" + n + ":" + n + param + n
                    + "}";
        }
    }

    public short getId() {
        return template.id;
    }

    public String getName() {
        return template.name;
    }

    public byte getType() {
        return template.type;
    }

    public boolean isSKH() {
        for (ItemOption itemOption : itemOptions) {
            if (itemOption.optionTemplate.id >= 127 && itemOption.optionTemplate.id <= 135) {
                return true;
            }
        }
        return false;
    }

    public boolean isVaiTho() {
        return this.template.id >= 0 && this.template.id <= 65;
    }

    public boolean isDTS() {
        return this.template.id >= 1048 && this.template.id <= 1062;
    }

    public boolean isDTL() {
        return this.template.id >= 555 && this.template.id <= 567;
    }

    public boolean isDHD() {
        return this.template.id >= 650 && this.template.id <= 662;
    }

    public boolean isManhTS() {
        return this.template.id >= 1066 && this.template.id <= 1070;
    }

    public boolean isGiayMau() {
        return this.template.id == 1505;
    }

    public boolean isDaNangCap() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public int getOptionParam(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                return itemOption.param;
            }
        }
        return 0;
    }

    public void addOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                itemOption.param += param;
                return;
            }
        }
        this.itemOptions.add(new ItemOption(id, param));
    }

    public ItemOption getOptionDaPhaLe() {
        return switch (template.id) {
            case 20 ->
                new ItemOption(77, 5);
            case 19 ->
                new ItemOption(103, 5);
            case 18 ->
                new ItemOption(80, 5);
            case 17 ->
                new ItemOption(81, 5);
            case 16 ->
                new ItemOption(50, 3);
            case 15 ->
                new ItemOption(94, 2);
            case 14 ->
                new ItemOption(108, 2);

            case 441 ->
                new ItemOption(95, 5);
            case 442 ->
                new ItemOption(96, 5);
            case 443 ->
                new ItemOption(97, 5);
            case 444 ->
                new ItemOption(98, 5);
            case 445 ->
                new ItemOption(99, 5);
            case 446 ->
                new ItemOption(100, 5);
            case 447 ->
                new ItemOption(101, 5);

            case 1416 ->
                new ItemOption(95, 5);
            case 1417 ->
                new ItemOption(96, 5);
            case 1418 ->
                new ItemOption(97, 5);
            case 1419 ->
                new ItemOption(98, 5);
            case 1420 ->
                new ItemOption(99, 5);
            case 1421 ->
                new ItemOption(100, 5);
            case 1422 ->
                new ItemOption(101, 5);

            case 1426 ->
                new ItemOption(95, 5);
            case 1427 ->
                new ItemOption(96, 5);
            case 1428 ->
                new ItemOption(97, 5);
            case 1429 ->
                new ItemOption(98, 5);
            case 1430 ->
                new ItemOption(99, 5);
            case 1431 ->
                new ItemOption(100, 5);
            case 1432 ->
                new ItemOption(101, 5);
            case 1433 ->
                new ItemOption(153, 5);
            case 1434 ->
                new ItemOption(160, 5);
            default ->
                itemOptions.get(0);
        };
    }

    public Item cloneItem() {
        Item item = new Item();
        item.itemOptions = new ArrayList<>();
        item.template = this.template;
        item.info = this.info;
        item.content = this.content;
        item.quantity = this.quantity;
        item.createTime = this.createTime;
        for (Item.ItemOption io : this.itemOptions) {
            item.itemOptions.add(new Item.ItemOption(io));
        }
        return item;
    }

    public String getOptionInfo() {
        StringJoiner optionInfo = new StringJoiner("\n");
        for (ItemOption io : this.itemOptions) {
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107 && io.optionTemplate.id != 218) {
                optionInfo.add(io.getOptionString());
            }
        }
        return optionInfo.toString();
    }

    public boolean isThanLinh() {
        return this.template.id >= 555 && this.template.id <= 567;
    }

    public boolean isThucAn() {
        return this.template.id >= 663 && this.template.id <= 667;
    }

    public String getOptionInfo(Item item) {
        boolean haveOption = false;
        StringJoiner optionInfo = new StringJoiner("\n");
        Item itC = this.cloneItem();
        ItemOption iodpl = item.getOptionDaPhaLe();
        for (ItemOption io : itC.itemOptions) {
            if (!haveOption && io.optionTemplate.id == iodpl.optionTemplate.id) {
                io.param += iodpl.param;
                haveOption = true;
            }
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107) {
                optionInfo.add(io.getOptionString());
            }
        }
        if (!haveOption) {
            optionInfo.add(iodpl.getOptionString());
        }
        itC.dispose();
        return optionInfo.toString();
    }

    public String getOptionInfoCuongHoa(Item item) {
        StringJoiner optionInfo = new StringJoiner("\n");
        Item itC = this.cloneItem();
        ItemOption iodpl = item.getOptionDaPhaLe();
        for (ItemOption io : itC.itemOptions) {
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107 && io.optionTemplate.id != 218) {
                optionInfo.add(io.getOptionString());
            }
        }
        optionInfo.add(iodpl.getOptionString());
        itC.dispose();
        return optionInfo.toString();
    }

    public void subOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                itemOption.param -= param;
                return;
            }
        }
    }

    public boolean isDaNangCap1() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public String typeName() {
        return switch (this.template.type) {
            case 0 -> "Áo";
            case 1 -> "Quần";
            case 2 -> "Găng";
            case 3 -> "Giày";
            case 4 -> "Rada";
            default -> "";
        };
    }

    public String typeHanhTinh() {
        return switch (this.template.id) {
            case 1071 -> "Trái đất";
            case 1084 -> "Trái đất";
            case 1072 -> "Namếc";
            case 1085 -> "Namếc";
            case 1073 -> "Xayda";
            case 1086 -> "Xayda";
            default -> "";
        };
    }

    public byte typeIdManh() {
        if (!isManhTS())
            return -1;
        return switch (this.template.id) {
            case 1066 -> 0;
            case 1067 -> 1;
            case 1070 -> 2;
            case 1068 -> 3;
            case 1069 -> 4;
            default -> -1;
        };
    }

    public String typeNameManh() {
        return switch (this.template.id) {
            case 1066 -> "Áo";
            case 1067 -> "Quần";
            case 1070 -> "Găng";
            case 1068 -> "Giày";
            case 1069 -> "Nhẫn";
            default -> "";
        };
    }

    public String typeDanangcap() {
        return switch (this.template.id) {
            case 1074 -> "cấp 1";
            case 1075 -> "cấp 2";
            case 1076 -> "cấp 3";
            case 1077 -> "cấp 4";
            case 1078 -> "cấp 5";
            default -> "";
        };
    }

    public String typeDaMayman() {
        return switch (this.template.id) {
            case 1079 -> "cấp 1";
            case 1080 -> "cấp 2";
            case 1081 -> "cấp 3";
            case 1082 -> "cấp 4";
            case 1083 -> "cấp 5";
            default -> "";
        };

    }

    public boolean isDaMayMan() {
        return this.template.id >= 1079 && this.template.id <= 1083;
    }

    public boolean isCongThucVip() {
        return (this.template.id >= 1071 && this.template.id <= 1073)
                || (this.template.id >= 1084 && this.template.id <= 1086);
    }

    public boolean isDoKyGui() {
        return this.template != null && (this.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 86)
                || this.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 87) || this.template.type == 14
                || this.template.type == 15 || this.template.type == 6
                || this.template.id >= 14 && this.template.id <= 20);
    }

    public boolean isCaiTrang() {
        return this.template.type == 5;
    }

    public boolean isPet() {
        return this.template.type == 33;
    }

    public boolean isPhuKienBang() {
        return this.template.type == 25;
    }

    public int getOptionValue(int optionId) {
        for (ItemOption io : this.itemOptions) {
            if (io.optionTemplate.id == optionId) {
                return io.param;
            }
        }
        return 0;
    }

    public String getInfoItem() {
        String strInfo = "|1|" + template.name + "\n|0|";
        for (ItemOption itemOption : itemOptions) {
            strInfo += itemOption.getOptionString() + "\n";
        }
        strInfo += "|2|" + template.description;
        return strInfo;
    }

    public boolean isHaveOption(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isSachTuyetKy() {
        return template.id == 1044 || template.id == 1211 || template.id == 1212;
    }

    public boolean isSachTuyetKy2() {
        return template.id >= 1278 && template.id <= 1280;
    }

}
