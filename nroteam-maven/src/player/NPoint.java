package player;

import radar.Card;
import radar.OptionCard;
import consts.ConstPlayer;
import consts.ConstRatio;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import skill.Skill;
import server.Manager;
import services.EffectSkillService;
import services.ItemService;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;
import player.badges.BagesTemplate;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import mob.Mob;

public class NPoint {

    public static final byte MAX_LIMIT = 6;

    @Setter
    private Player player;

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
    }

    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;

    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    public int dameAfter;

    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public short stamina, maxStamina;

    public byte limitPower;
    public long power;
    public long tiemNang;

    public long hp, hpMax, hpg;
    public long mp, mpMax, mpg;
    public long dame, dameg;
    public int def, defg;
    public int crit, critg, critdragon;
    public byte speed = 3;

    public boolean teleport;

    public boolean khangTDHS;

    /**
     * Chỉ số cộng thêm
     */
    public long hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd;

    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;

    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;

    /**
     * Tỉ lệ giáp cộng thêm
     */
    public byte tlDef;

    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;

    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public long hpHoi, mpHoi, mpHoiCute;

    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public short tlHpHoi, tlMpHoi;

    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public short tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;

    /**
     * Hồi HP +#%/10s cho bản thân và đồng đội
     */
    public short tlHpHoiAura10s;

    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public short tlHutHp, tlHutMp, tlHutHpMob;

    public int tlDameToBoss = 0; // Tấn công +#% lên Boss

    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public short tlHutHpMpXQ;

    /**
     * Tỉ lệ phản sát thương
     */
    public short tlPST;

    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;

    /**
     * Tỉ lệ vàng cộng thêm
     */
    public short tlGold;

    /**
     * Tỉ lệ né đòn
     */
    public short tlNeDon;

    public short tlBom;

    public short tlGiap;

    public short tlxgcc;

    public short tlxgc;

    public int tlSatThuongChuan;

    public short tlchinhxac;

    public short tlTNSMPet;
    public short xChuong;

    /** Tỉ lệ tăng rơi vật phẩm từ quái của option 236. */
    public int tlItemDrop;

    /** Giới hạn và số % sức đánh đã cộng dồn khi chỉ dùng chiêu đấm (option 156). */
    public int maxPunchDameStack;
    public int punchDameStack;

    /** Giảm đồng thời HP/KI tối đa của option 155. */
    public short tlSubHpMp;

    public short setltdb;
    public short setTinhAn;
    public short setNhatAn;
    public short setNguyetAn;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlCoolDame;

    /**
     * Tỉ lệ giảm sức đánh
     */
    public short tlSubSD;

    public int voHieuChuong;

    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;

    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean islinhthuydanhbac;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isLamCham;
    public boolean isDoSPL;
    public boolean isThoBulma;

    public short tlHpGiamODo;

    public boolean isGogeta;

    public int tlSpeed;

    public int levelBT;

    /*-------------------------------------------------------------------------*/
    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */
    public void calPoint() {
        if (this.player.pet != null) {
            this.player.pet.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
    }

    private void setPointWhenWearClothes() {
        resetPoint();
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
            tlHutHp += RewardBlackBall.R3S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) {
            tlPST += RewardBlackBall.R4S_2;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
            tlDameCrit.add(RewardBlackBall.R5S_1);
            tlSDCM += RewardBlackBall.R5S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) {
            tlNeDon += RewardBlackBall.R7S_1;
        }
        if (player.setClothes.checkSetblack()) {
            tlSDCM += 5;
        }

        // Xử lý set champa
        if (this.player.setClothes.setchampa == 5) {
            tlSDCM += 20;
        }
        if (this.player.setClothes.setchampa == 2 || this.player.setClothes.setchampa == 3) {
            tlDameToBoss += 5;

        }
        if (this.player.setClothes.setchampa == 4) {
            tlDameToBoss += 10;
        }
        if (this.player.setClothes.setchampa == 5) {
            tlDameToBoss += 15;
        }

        Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
        if (card != null) {
            for (OptionCard io : card.Options) {
                if ((card.Level >= 1 && io.active <= card.Level) || (card.Level == -1 && io.active == 0)) {
                    switch (io.id) {
                        case 0 -> // Tấn công +#
                            this.dameAdd += io.param;
                        case 2 -> {
                            // HP, KI+#000
                            this.hpAdd += io.param * 1000;
                            this.mpAdd += io.param * 1000;
                        }
                        case 3 -> // vô hiệu chưởng
                            this.voHieuChuong += io.param;
                        case 5 -> {
                            // +#% sức đánh chí mạng
                            this.tlDameCrit.add(io.param);
                            this.tlSDCM += io.param;
                        }
                        case 6 -> // HP+#
                            this.hpAdd += io.param;
                        case 7 -> // KI+#
                            this.mpAdd += io.param;
                        case 8 -> // Hút #% HP, KI xung quanh mỗi 5 giây
                            this.tlHutHpMpXQ += io.param;
                        case 10 -> {
                            // +#% Sát thương chuẩn
                            this.tlSatThuongChuan += io.param;
                        }

                        case 14 -> // Chí mạng+#%
                            this.critAdd += io.param;
                        case 16, 114, 148 -> // Speed
                            this.tlSpeed += io.param;
                        case 18 -> // Chinh xac
                            this.tlchinhxac += io.param;
                        case 19 -> // Tấn công+#% khi đánh quái
                            this.tlDameAttMob.add(io.param);
                        case 22 -> // HP+#K
                            this.hpAdd += io.param * 1000;
                        case 23 -> // MP+#K
                            this.mpAdd += io.param * 1000;
                        case 27 -> // +# HP/30s
                            this.hpHoiAdd += io.param;
                        case 28 -> // +# KI/30s
                            this.mpHoiAdd += io.param;
                        case 33 -> // dịch chuyển tức thời
                            this.teleport = true;
                        case 34 ->
                            this.setTinhAn += 1;
                        case 35 ->
                            this.setNguyetAn += 1;
                        case 36 ->
                            this.setNhatAn += 1;
                        case 47 -> // Giáp+#
                            this.defAdd += io.param;
                        case 48 -> {
                            // HP/KI+#
                            this.hpAdd += io.param;
                            this.mpAdd += io.param;
                        }
                        case 49, 50 -> // Sức đánh+#%
                            this.tlDame.add(io.param);
                        case 77 -> // HP+#%
                            this.tlHp.add(io.param);
                        case 80 -> // Tấn công+#%
                            // HP+#%/30s
                            this.tlHpHoi += io.param;
                        case 81 -> // MP+#%/30s
                            this.tlMpHoi += io.param;
                        case 88 -> // Cộng #% exp khi đánh quái
                            this.tlTNSM.add(io.param);
                        case 94 -> // Giảm #% sát thương
                            this.tlDef += io.param;
                        case 95 -> // Biến #% tấn công thành HP
                            this.tlHutHp += io.param;
                        case 96 -> // Biến #% tấn công thành MP
                            this.tlHutMp += io.param;
                        case 97 -> // Phản #% sát thương
                            this.tlPST += io.param;
                        case 98 -> // Xuyen giap chuong
                            this.tlxgc += io.param;
                        case 99 -> // Xuyen giap can chien
                            this.tlxgcc += io.param;
                        case 100 -> // +#% vàng từ quái
                            this.tlGold += io.param;
                        case 101 -> // +#% TN,SM
                            this.tlTNSM.add(io.param);
                        case 103 -> // KI +#%
                            this.tlMp.add(io.param);
                        case 104 -> // Biến #% tấn công quái thành HP
                            this.tlHutHpMob += io.param;
                        case 105 -> // Vô hình khi không đánh quái và boss
                            this.wearingVoHinh = true;
                        case 106 -> // Không ảnh hưởng bởi cái lạnh
                            this.isKhongLanh = true;
                        case 108 -> // #% Né đòn
                            this.tlNeDon += io.param;
                        case 109 -> // Hôi, giảm #% HP
                            this.tlHpGiamODo += io.param;
                        case 116 -> // Kháng thái dương hạ san
                            this.khangTDHS = true;
                        case 226, 117 -> {
                            // Đẹp +#% SĐ cho mình và người xung quanh
                            if (io.param > this.tlSexyDame) {
                                this.tlSexyDame = io.param;
                            }
                        }
                        case 204 -> // Tấn công +#% lên Boss
                            this.tlDameToBoss += io.param;

                        case 206 -> {
                            // Vật phẩm hiếm rơi từ quái (+#% HP, KI sức đánh)
                            this.tlHp.add(io.param);
                            this.tlMp.add(io.param);
                            this.tlDame.add(io.param);
                        }
                        case 207 -> {
                            // Vật phẩm hiếm rơi từ quái (+#% HP, KI sức đánh)
                            this.tlHp.add(io.param);
                            this.tlMp.add(io.param);
                            this.tlDame.add(io.param);
                        }

                        case 250 -> {
                            // Cool +#% SĐ cho mình và người xung quanh
                            if (io.param > this.tlCoolDame) {
                                this.tlCoolDame = io.param;
                            }
                        }
                        case 147 -> // +#% sức đánh
                            this.tlDame.add(io.param);
                        case 155 -> {
                            // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                            this.tlSubSD = (short) Math.max(this.tlSubSD, 50);
                            this.tlSubHpMp = (short) Math.max(this.tlSubHpMp, 50);
                            this.tlTNSM.add(io.param);
                            this.tlGold += io.param;
                        }
                        case 156 -> this.maxPunchDameStack = Math.max(this.maxPunchDameStack, io.param);
                        case 162 -> // Cute hồi #% KI/s bản thân và xung quanh
                            this.mpHoiCute += io.param;
                        case 173 -> {
                            // Phục hồi #% HP và KI cho đồng đội
                            this.tlHpHoiBanThanVaDongDoi += io.param;
                            this.tlMpHoiBanThanVaDongDoi += io.param;
                        }
                        case 211 ->
                            this.setltdb += 1;
                        case 153 -> // % phát nổ sau khi chết
                            this.tlBom += io.param;

                    }
                    // Speed
                    // Tấn công+#%
                }
            }
        }
        // Bông tai cấp 2
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            this.player.inventory.itemsBag.stream()
                    .filter(it -> it.isNotNullItem() && it.template.id == 921)
                    .findFirst()
                    .ifPresent(btc2 -> {
                        for (ItemOption io : btc2.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }
        // Bông tai cấp 3
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            this.player.inventory.itemsBag.stream()
                    .filter(it -> it.isNotNullItem() && it.template.id == 1884)
                    .findFirst()
                    .ifPresent(btc3 -> {
                        for (ItemOption io : btc3.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }
        this.player.setClothes.worldcup = 0;
        for (Item item : this.player.inventory.itemsBody) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 966, 982, 983, 883, 904 ->
                        player.setClothes.worldcup++;
                }
                if (item.template.id >= 592 && item.template.id <= 594) {
                    teleport = true;
                }
                for (ItemOption io : item.itemOptions) {
                    addOption(io);
                }
            }
        }
        if (!this.player.isPet && this.player.itemEvent != null && this.player.itemEvent.isShenronPetX3) {
            this.tlTNSMPet += 100;
        }

        if (this.player.setClothes.gohan >= 5) {
            this.tlGold += 30;
        }

        if (this.player.dataBadges != null && !this.player.dataBadges.isEmpty()) {
            List<ItemOption> badgeOptions = BagesTemplate.sendListItemOption(this.player);
            if (badgeOptions != null && !badgeOptions.isEmpty()) {
                for (ItemOption io : badgeOptions) {
                    addOption(io);
                }
            }
        }
        setDameTrainArmor();
        setBasePoint();
        setOutfitFusion();
        setSpeed();
    }

    private void addOption(ItemOption io) {
        switch (io.optionTemplate.id) {
            case 0 -> // Tấn công +#
                this.dameAdd += io.param;
            case 2 -> {
                // HP, KI+#000
                this.hpAdd += io.param * 1000;
                this.mpAdd += io.param * 1000;
            }
            case 3 -> // vô hiệu chưởng
                this.voHieuChuong += io.param;
            case 5 -> {
                // +#% sức đánh chí mạng
                this.tlDameCrit.add(io.param);
                this.tlSDCM += io.param;
            }
            case 6 -> // HP+#
                this.hpAdd += io.param;
            case 7 -> // KI+#
                this.mpAdd += io.param;
            case 8 -> // Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += io.param;
            case 10 -> {
                // +#% Sát thương chuẩn
                this.tlSatThuongChuan += io.param;
            }

            case 14 -> // Chí mạng+#%
                this.critAdd += io.param;
            case 16, 114, 148 -> // Speed
                this.tlSpeed += io.param;
            case 18 -> // Chinh xac
                this.tlchinhxac += io.param;
            case 19 -> // Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(io.param);
            case 22 -> // HP+#K
                this.hpAdd += io.param * 1000;
            case 23 -> // MP+#K
                this.mpAdd += io.param * 1000;
            case 24 -> // Làm chậm
                this.isLamCham = true;
            case 25 -> // Tàn hình
                this.isTanHinh = true;
            case 26 -> // Hóa đá
                this.isHoaDa = true;
            case 27 -> // +# HP/30s
                this.hpHoiAdd += io.param;
            case 28 -> // +# KI/30s
                this.mpHoiAdd += io.param;
            case 33 -> // dịch chuyển tức thời
                this.teleport = true;
            case 34 ->
                this.setTinhAn += 1;
            case 35 ->
                this.setNguyetAn += 1;
            case 36 ->
                this.setNhatAn += 1;
            case 47 -> // Giáp+#
                this.defAdd += io.param;
            case 48 -> {
                // HP/KI+#
                this.hpAdd += io.param;
                this.mpAdd += io.param;
            }
            case 49, 50 -> // Sức đánh+#%
                this.tlDame.add(io.param);
            case 77 -> // HP+#%
                this.tlHp.add(io.param);
            case 80 -> // Tấn công+#%
                // HP+#%/30s
                this.tlHpHoi += io.param;
            case 81 -> // MP+#%/30s
                this.tlMpHoi += io.param;
            case 88 -> // Cộng #% exp khi đánh quái
                this.tlTNSM.add(io.param);
            case 94 -> // Giáp #%
                this.tlDef += io.param;
            case 95 -> // Biến #% tấn công thành HP
                this.tlHutHp += io.param;
            case 96 -> // Biến #% tấn công thành MP
                this.tlHutMp += io.param;
            case 97 -> // Phản #% sát thương
                this.tlPST += io.param;
            case 98 -> // Xuyen giap chuong
                this.tlxgc += io.param;
            case 99 -> // Xuyen giap can chien
                this.tlxgcc += io.param;
            case 100 -> // +#% vàng từ quái
                this.tlGold += io.param;
            case 101 -> // +#% TN,SM
                this.tlTNSM.add(io.param);
            case 103 -> // KI +#%
                this.tlMp.add(io.param);
            case 104 -> // Biến #% tấn công quái thành HP
                this.tlHutHpMob += io.param;
            case 105 -> // Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
            case 106 -> // Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
            case 108 -> // #% Né đòn
                this.tlNeDon += io.param;
            case 109 -> // Hôi, giảm #% HP
                this.tlHpGiamODo += io.param;
            case 110 -> // Do spl
                this.isDoSPL = true;
            case 116 -> // Kháng thái dương hạ san
                this.khangTDHS = true;
            case 226, 117 -> {
                // Đẹp +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlSexyDame) {
                    this.tlSexyDame = io.param;
                }
            }
            case 147 -> // +#% sức đánh
                this.tlDame.add(io.param);
            case 155 -> {
                // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD = (short) Math.max(this.tlSubSD, 50);
                this.tlSubHpMp = (short) Math.max(this.tlSubHpMp, 50);
                this.tlTNSM.add(io.param);
                this.tlGold += io.param;
            }
            case 156 -> this.maxPunchDameStack = Math.max(this.maxPunchDameStack, io.param);
            case 162 -> // Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += io.param;
            case 159 -> // x chưởng
                this.xChuong = (short) io.param;
            case 160 -> // TNSM PET;
                this.tlTNSMPet += io.param;
            case 173 -> {
                // Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += io.param;
                this.tlMpHoiBanThanVaDongDoi += io.param;
            }
            case 236 -> this.tlItemDrop += io.param;
            case 251 -> // Hồi HP +#%/10s cho bản thân và đồng đội
                this.tlHpHoiAura10s += io.param;
            case 211 ->
                this.setltdb += 1;
            case 153 -> // % phát nổ sau khi chết
                this.tlBom += io.param;
            case 204 -> // Tấn công +#% lên Boss
                this.tlDameToBoss += io.param;

            case 206 -> {
                // Vật phẩm hiếm rơi từ quái (+#% HP, KI sức đánh)
                this.tlHp.add(io.param);
                this.tlMp.add(io.param);
                this.tlDame.add(io.param);
            }

            case 207 -> {
                // Vật phẩm hiếm rơi từ quái (+#% HP, KI sức đánh)
                this.tlHp.add(io.param);
                this.tlMp.add(io.param);
                this.tlDame.add(io.param);
            }
            case 250 -> {
                // Cool +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlCoolDame) {
                    this.tlCoolDame = io.param;
                }
            }
        }
    }

    private void setSpeed() {
        if (player.isPl()) {
            speed = (byte) (8 + (8 * tlSpeed / 100));
        }
    }

    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null
                || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta = skin.template.id == 2133 && pskin.template.id == 2134
                    || skin.template.id == 2134 && pskin.template.id == 2133;
        } else {
            this.isGogeta = false;
        }
    }

    private void setDameTrainArmor() {
        if (!this.player.isPet && !this.player.isBoss) {
            if (this.player.inventory == null || this.player.inventory.itemsBody == null
                    || this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl != null && gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
                } else {
                    if (this.player.inventory.trainArmor == null) {
                        if (this.player.inventory.itemsBag != null) {
                            gtl = this.player.inventory.itemsBag.stream()
                                    .filter(item -> item != null && item.isNotNullItem() && item.template != null
                                            && item.template.type == 32
                                            && item.itemOptions != null
                                            && item.itemOptions.stream()
                                                    .filter(io -> io != null && io.optionTemplate != null
                                                            && io.optionTemplate.id == 9 && io.param > 0)
                                                    .findFirst()
                                                    .orElse(null) != null)
                                    .findFirst().orElse(null);
                        }
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    if (this.player.inventory.trainArmor != null
                            && this.player.inventory.trainArmor.itemOptions != null) {
                        for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                            if (io != null && io.optionTemplate != null
                                    && io.optionTemplate.id == 9 && io.param > 0) {
                                this.tlDame
                                        .add(ItemService.gI().getPercentTrainArmor(this.player.inventory.trainArmor));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi get giáp tập luyện " + (this.player != null ? this.player.name : "unknown") + "\n" + e
                        + "\n");
            }
        }
    }

    public void setBasePoint() {
        setHpMax();
        setHp();
        setMpMax();
        setMp();
        setDame();
        setDef();
        setCrit();
        setHpHoi();
        setMpHoi();
        setLtdb();
        setThoBulma();
        setTinhNhatNguyetAn();
    }

    private void setLtdb() {
        this.islinhthuydanhbac = this.setltdb >= 5;
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoi / 100);

        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoi / 100);

        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

    }

    private void setHpMax() {
        this.hpMax = this.hpg;
        this.hpMax += this.hpAdd;

        for (Integer tl : this.tlHp) {
            hpMax += (hpMax * tl / 100L);
        }

        hpMax -= hpMax * tlSubHpMp / 100L;

        double[] box1Bonus = calculateBox1Bonus();
        if (box1Bonus[1] > 0) {
            hpMax += (hpMax * (long) box1Bonus[1] / 100L);
        }
        // Xử lý set nappa
        if (this.player.setClothes.nappa == 5) {
            hpMax += (hpMax * 80L / 100L);
        }

        if (this.player.setClothes.cadicM >= 2) {
            hpMax += (hpMax * 10L / 100L);

        }
        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            hpMax += (hpMax * 10 / 100L);
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            hpMax += (hpMax * 10L / 100L);
        }

        // Xử lý set nhật ấn
        if (this.isNhatAn) {
            hpMax += (hpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100L);
        }

        if (player.setClothes.checkSetblack()) {
            hpMax += (hpMax * 4 / 100L);
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100L);
            }
        }

        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 5 / 100L);
        }

        // Xử lý pet berus
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 30 / 100L);
        }

        // Xử lý pet pic
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 20 / 100L);
        }

        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 40 / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8062) {
            hpMax += (hpMax * 5 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            hpMax += (hpMax * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }

        // Xử lý +hp đệ
        if (this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            hpMax += this.player.pet.nPoint.hpMax;
        }

        // Xử lý bổ huyết
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet && !this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2;
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
        }

        // Xử lý huýt sáo
        if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }
        if (!this.player.isPet && this.player.itemEvent != null && this.player.itemEvent.isShenronPorataHp
                && isPorataFusion()) {
            hpMax += (hpMax * 15L / 100L);
        }

        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.typeChibi != -1 && this.player.effectSkill.isChibi
                && this.player.typeChibi == 3) {
            hpMax *= 2;
        }

        // Xử lý map lạnh
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }

        // if (hpMax > 2_000_000_000) {
        // hpMax = 2_000_000_000;
        // }
        this.hpMax = hpMax;
    }

    private void setHp() {
        if (this.hp > this.hpMax) {
            this.hp = this.hpMax;
        }
    }

    private void setMpMax() {
        this.mpMax = this.mpg;
        this.mpMax += this.mpAdd;

        for (Integer tl : this.tlMp) {
            mpMax += (mpMax * tl / 100L);
        }

        mpMax -= mpMax * tlSubHpMp / 100L;

        double[] box1Bonus = calculateBox1Bonus();
        if (box1Bonus[2] > 0) {
            mpMax += (mpMax * (long) box1Bonus[2] / 100L);
        }

        if (this.isNguyetAn) {
            mpMax += (mpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        if (player.setClothes.checkSetblack()) {
            mpMax += (mpMax * 4 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            mpMax += (this.mpMax * 10 / 100L);
        }

        // Xử lý pet pic
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (this.mpMax * 20 / 100L);
        }

        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (this.mpMax * 5 / 100L);
        }

        // Xử lý pet br
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (this.mpMax * 30 / 100L);// MP berus
        }

        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (this.mpMax * 40 / 100L);// MP black
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            mpMax += (mpMax * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Xử lý hợp thể
        if (this.player.fusion.typeFusion != 0) {
            mpMax += this.player.pet.nPoint.mpMax;
        }

        // Xử lý bổ khí
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi && !this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2;
        }
        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.typeChibi != -1 && this.player.effectSkill.isChibi
                && this.player.typeChibi == 1) {
            mpMax *= 2;
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
        }
        if (!this.player.isPet && this.player.itemEvent != null && this.player.itemEvent.isShenronPorataKi
                && isPorataFusion()) {
            mpMax += (mpMax * 15L / 100L);
        }

        this.mpMax = mpMax;
    }

    private void setMp() {
        if (this.mp > this.mpMax) {
            this.mp = this.mpMax;
        }

    }

    public long getHP() {
        return Math.max(0L, this.hp);
    }

    public long getMP() {
        long value = Math.min(this.mp, this.mpMax);
        return Math.max(value, 0);
    }

    public void setMP(long mp) {
        if (mp < 0) {
            this.mp = 0;
        } else if (mp > this.mpMax) {
            this.mp = this.mpMax;
        } else {
            this.mp = mp;
        }
    }

    private void setDame() {
        long baseDame = this.dameg + this.dameAdd;

        // Buff dame đều cộng trên sức đánh gốc (dameg), không lấy phần cộng từ đồ (dameAdd)
        if (this.player.itemTime != null) {
            long bonusFromBaseDame = 0;

            if (this.player.itemTime.isUseCuongNo2) {
                bonusFromBaseDame += (this.dameg * 120 / 100L);
            } else if (this.player.itemTime.isUseCuongNo) {
                bonusFromBaseDame += this.dameg;
            }

            if (this.player.itemTime.isUseBanhChung) {
                bonusFromBaseDame += (this.dameg * 10 / 100L);
            }
            if (this.player.itemTime.isUseBanhTet) {
                bonusFromBaseDame += (this.dameg * 5 / 100L);
            }

            baseDame = this.dameg + bonusFromBaseDame + this.dameAdd;
        }

        long dame = baseDame;
        for (Integer tl : this.tlDame) {
            dame += (dame * tl / 100L);
        }

        double[] box1Bonus = calculateBox1Bonus();
        if (box1Bonus[0] > 0) {
            dame += (dame * (long) box1Bonus[0] / 100L);
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 20 / 100L);
        }

        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 5 / 100L);
        }

        // Xử lý pet br
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 30 / 100L);
        }

        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 40 / 100L);
        }

        // Xử lý set tinh ấn
        if (this.isTinhAn) {
            dame += (dame * 15L / 100L);
        }

        // Xử lý thức ăn
        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal
                || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8060) {
            dame += (dame * 5 / 100L);
        }

        if (this.player.setClothes.nail >= 2) {
            this.tlDameCrit.add(10);
        }
        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            this.tlDameCrit.add(5);
            this.tlSDCM += 5;
        }

        // // Xử lý cuồng nộ
        // if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo &&
        // !this.player.itemTime.isUseCuongNo2) {
        // dame =dame* 2-dameAdd;
        // }
        // if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
        // dame *= 2.2-dameAdd;
        // }
        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100L);
        }

        if (player.setClothes.checkSetblack()) {
            dame += (dame * 4 / 100L);

        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            dame += (dame * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            dame += (dame * 10L / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        if (this.player.fusion.typeFusion != 0) {
            dame += this.player.pet.nPoint.dame;
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100L);
            }
        }
        if (!this.player.isPet && this.player.itemEvent != null && this.player.itemEvent.isShenronPorataDame
                && isPorataFusion()) {
            dame += (dame * 15L / 100L);
        }

        // Xử lý giảm dame
        dame -= (dame * tlSubSD / 100L);

        if (this.player != null && this.player.effectSkill != null && this.player.effectSkill.isCarrot) {
            dame -= (dame * 15 / 100L);
        }

        // Xử lý map cold
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }

        this.dame = dame;
    }

    private void setDef() {
        this.def = this.defg * 4;
        this.def += this.defAdd;
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        this.crit += this.critdragon;
        if (this.player.setClothes.thanVuTruKaio >= 2) {
            this.crit += 10;
        }
        if (this.player.effectSkill.isMonkey) {
            this.crit = 110;
        }
    }

    private void resetPoint() {
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef = 0;
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.tlHpHoiAura10s = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlBom = 0;
        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlchinhxac = 0;
        this.tlTNSMPet = 0;
        this.tlItemDrop = 0;
        this.maxPunchDameStack = 0;
        this.tlSubHpMp = 0;
        this.xChuong = 0;
        this.setltdb = 0;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlCoolDame = 0;
        this.tlSubSD = 0;
        this.tlHpGiamODo = 0;
        this.tlSpeed = 0;
        this.tlSatThuongChuan = 0;
        this.tlDameToBoss = 0;
        this.teleport = false;

        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.isLamCham = false;
        this.isDoSPL = false;
        this.isThoBulma = false;
    }

    public void addHp(long hp) {
        if (hp > 0) {
            long potentialHp = this.hp + hp;
            if (potentialHp > this.hpMax) {
                this.hp = this.hpMax;
            } else {
                this.hp = Math.max(0, potentialHp);
            }
        }
    }

    public void addMp(long mp) {
        long potentialMp = this.mp + mp;
        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = potentialMp;
        }
    }

    public void setMp(long mp) {
        this.mp = Math.max(0, mp);
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25
                && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }

    public long getDameAttack(boolean isAttackMob) {
        setIsCrit();
        long dameAttack = this.dame;
        intrinsic = this.player.playerIntrinsic.intrinsic;
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        byte percentXDame = 0;
        long satThuongChuan = 0;
        Skill skillSelect = player.playerSkill.skillSelect;
        int skillId = skillSelect.template.id;
        if (maxPunchDameStack <= 0) {
            punchDameStack = 0;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK) {
            if (maxPunchDameStack > 0) {
                punchDameStack = Math.min(maxPunchDameStack, punchDameStack + 1);
            }
        } else {
            punchDameStack = 0;
        }
        if (punchDameStack > 0) {
            dameAttack += dameAttack * punchDameStack / 100L;
        }
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        switch (skillSelect.template.id) {
            case Skill.DRAGON -> {
                if (intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
            }
            case Skill.KAMEJOKO -> {
                if (intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.songoku == 5) {
                    percentXDame = 100;
                }
            }
            case Skill.GALICK -> {
                if (intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.kakarot == 5) {
                    percentXDame = 100;
                }
            }
            case Skill.ANTOMIC -> {
                if (intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
            }
            case Skill.TU_SAT -> {
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.cadicM == 4) {
                    percentXDame = 20;
                } else if (this.player.setClothes.cadicM == 5) {
                    percentXDame = 50;
                }
            }
            case Skill.DEMON -> {
                if (intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
            }
            case Skill.MASENKO -> {
                if (intrinsic != null && intrinsic.id == 9) {
                    percentXDame += intrinsic.param1;
                }
                if (this.player.setClothes.nail == 5) {
                    percentXDame += 100;
                }
                percentDameSkill = skillSelect.damage;
            }

            case Skill.LIEN_HOAN -> {
                if (intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                // Áp dụng % sát thương theo cấp độ chiêu cho Liên Hoàn
                percentDameSkill = skillSelect.damage;
            }
            case Skill.KAIOKEN -> {
                percentDameSkill = skillSelect.damage;
                if (player.setClothes.thanVuTruKaio == 5) {
                    percentXDame = 100;
                }
            }
            case Skill.DICH_CHUYEN_TUC_THOI -> {
                isCrit = true;
                isCritTele = true;
                dameAttack = Util.nextInt((int) (int) Math.min(2_000_000_000L, (dameAttack - (dameAttack / 100 * 5))),
                        (int) (int) Math.min(2_000_000_000L, (dameAttack + (dameAttack / 100 * 5))));
            }
            case Skill.MAKANKOSAPPO -> {
                percentDameSkill = skillSelect.damage;
                double multiplier = 1.0;

                if (this.player.setClothes.picolo == 5) {
                    multiplier += 0.5; // +50%
                }
                long dameSkill = (long) Math.min(2_000_000_000L, this.mpMax * (percentDameSkill / 100.0) * multiplier);

                return dameSkill;
            }

            case Skill.QUA_CAU_KENH_KHI -> {
                int point = this.player.playerSkill.skillSelect.point;
                int baseDamage = this.player.playerSkill.skillSelect.template.skills[point - 1].damage;
                int range = SkillUtil.getRangeQCKK(point);

                long totalHp = 0;
                int targets = 0;
                // Quái
                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie() && Util.getDistance(this.player, mob) <= range) {
                        totalHp += mob.point.hp;
                        targets++;
                    }
                }

                // Người chơi
                for (Player pl : this.player.zone.getHumanoids()) {
                    if (pl.isDie() || pl.id == this.player.id) {
                        continue;
                    }
                    if (Util.getDistance(this.player, pl) <= range) {
                        totalHp += pl.nPoint.hp;
                        targets++;
                    }
                }
                long avgHp = targets > 0 ? totalHp / targets : 0;
                long damage = (long) (avgHp * 0.025 + this.player.nPoint.dame * (baseDamage / 100));

                if (this.player.setClothes.kirin == 5) {
                    damage *= 2;
                }

                return Math.max(damage, 1);
            }

            case Skill.DE_TRUNG -> {
                if (player.setClothes.pikkoroDaimao == 5) {
                    dameAttack *= 2;
                }
                return dameAttack;
            }
        }

        if (intrinsic.id == 18 && this.player.effectSkill.isMonkey) {
            percentDameIntrinsic = intrinsic.param1;
        }

        if (percentDameSkill != 0) {
            dameAttack = dameAttack * percentDameSkill / 100;
        }

        dameAttack += (dameAttack * percentDameIntrinsic / 100);
        dameAttack += (dameAttack * dameAfter / 100);
        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }
        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }

            // Xử lý khẩu trang - tăng 10% sức đánh khi đánh quái
            if (this.player.itemTime != null && this.player.itemTime.isUseKhauTrang) {
                dameAttack += (dameAttack * 10 / 100);
            }

            if (this.player.isPet && ((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2;
            int sumCritBonus = 0;
            for (Integer tl : this.tlDameCrit) {
                sumCritBonus += tl;
            }
            dameAttack += (dameAttack * sumCritBonus / 100);
        }

        dameAttack += (dameAttack * percentXDame / 100);

        long tempDameAttack = (dameAttack / 100L * 5L);
        if (tempDameAttack <= 0) {
            tempDameAttack = 1;
        }
        dameAttack += (Util.getOne(-1, 1) * Util.nextInt((int) tempDameAttack) + 1);

        if (player.effectSkin != null && player.effectSkin.isXChuong
                && (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO
                        || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC
                        || player.playerSkill.skillSelect.template.id == Skill.MASENKO)) {
            dameAttack *= xChuong;
            player.effectSkin.isXDame = true;
            player.effectSkin.isXChuong = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }
        return dameAttack;
    }

    public long getDameAttackToBoss(boolean isAttackMob) {
        long dameAttack = getDameAttack(isAttackMob);
        System.out.println("[DEBUG] getDameAttackToBoss called, tlDameToBoss: " + this.tlDameToBoss);
        if (this.tlDameToBoss > 0) {
            long bonus = (dameAttack * this.tlDameToBoss / 100L);
            dameAttack += bonus;
            System.out.println("[DEBUG] Bonus dame: " + bonus + ", Final dame: " + dameAttack);
        }
        return dameAttack;
    }

    public long getSatThuongChuan(long dameBase) {
        if (this.tlSatThuongChuan > 0) {
            long satThuongChuan = (long) (dameBase * this.tlSatThuongChuan / 100);
            return satThuongChuan;
        }
        return 0;
    }

    public long getCurrPercentHP() {
        if (this.hpMax == 0) {
            return 100;
        }
        return this.hp * 100 / this.hpMax;
    }

    public long getCurrPercentMP() {
        return this.mp * 100 / this.mpMax;
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.mp = this.mpMax;

    }

    public void subHP(double sub) {
        this.hp -= sub;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public void subMP(float sub) {
        this.mp -= sub;
        if (this.mp < 0) {
            this.mp = 0;
        }
    }

    public void setHp(long hp) {
        if (hp > this.hpMax) {
            this.hp = this.hpMax;
        } else {
            this.hp = hp;
        }
    }

    public long calSucManhTiemNang(long tiemNang) {
        if (power < getPowerLimit()) {
            for (Integer tl : this.tlTNSM) {
                tiemNang += tiemNang * tl / 100;
            }
            long base = tiemNang;
            if (this.player.effectSkill != null && this.player.typeChibi != -1 && this.player.effectSkill.isChibi
                    && this.player.typeChibi == 2) {
                tiemNang *= 2;
            }
            if (player.charms.tdTriTue > System.currentTimeMillis()) {
                tiemNang += base;
            }
            if (player.charms.tdTriTue3 > System.currentTimeMillis()) {
                tiemNang += base * 2;
            }
            if (player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += base * 3;
            }

            if (this.player.itemEvent != null && this.player.itemEvent.isShenronPlayerX3) {
                tiemNang += base * 2;
            }
            if ((player.getSession() != null && player.getSession().vip > 0)
                    || (player.isPet && ((Pet) player).master.getSession() != null
                            && ((Pet) player).master.getSession().vip > 0)) {
                tiemNang += base * 3;
            }
            if (player.itemTime != null && player.itemTime.isUseDK) {
                tiemNang += base * 2;
            }
            if (player.itemTime != null && player.itemTime.isUseKhauTrang) {
                tiemNang += base * 5 / 100;
            }
            if (player.zone.map.mapId >= 135 && player.zone.map.mapId <= 138
                    && player.itemTime != null && player.itemTime.isUseKhoBauX2) {
                tiemNang += base * 2;
            }
            if (player.satellite != null && player.satellite.isIntelligent) {
                tiemNang += base / 5;
            }
            if (intrinsic != null && intrinsic.id == 24) {
                tiemNang += ((long) tiemNang * intrinsic.param1 / 100);
            }

            if (power >= 60_000_000_000L) {
                tiemNang -= ((long) tiemNang * 80 / 100);
            }

            if (player.isPet) {
                Pet pet = (Pet) player;
                if (pet.master.itemTime.isUseBuaSanta) {
                    tiemNang += base * 2;
                }
                if (pet.master.nPoint != null && pet.master.nPoint.tlTNSMPet > 0) {
                    tiemNang += base * pet.master.nPoint.tlTNSMPet / 100;
                }
            }

            if (MapService.gI().isMapNguHanhSon(player.zone.map.mapId)) {
                tiemNang *= 4;
            } else if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
                tiemNang *= 2;
            }
            if (player.cFlag != 0) {
                tiemNang += ((long) tiemNang * (player.cFlag == 8 ? 10 : 5) / 100);
            }
            tiemNang *= Manager.RATE_EXP_SERVER;
            tiemNang = calSubTNSM(tiemNang);
            if (tiemNang <= 0) {
                tiemNang = 1;
            }

        } else {
            tiemNang = 10;
        }
        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {
        if (power >= 80_000_000_000L) {
            tiemNang /= 80; // Giảm 40 lần
        } else if (power >= 60_000_000_000L) {
            tiemNang /= 50; // Giảm 30 lần
        } else if (power >= 50_000_000_000L) {
            tiemNang /= 30; // Giảm 20 lần
        } else if (power >= 40_000_000_000L) {
            tiemNang /= 10; // Giảm 10 lần
        }
        return tiemNang;
    }

    public short getTileHutHp(boolean isMob) {
        if (isMob) {
            return (short) (this.tlHutHp + this.tlHutHpMob);
        } else {
            return this.tlHutHp;
        }
    }

    public short getTiLeHutMp() {
        return this.tlHutMp;
    }

    public double subDameInjureWithDeff(double dame) {
        return subDameInjureWithDeff(dame, null);
    }

    public double subDameInjureWithDeff(double dame, Player attacker) {

        int def = this.def;

        // Vì tlDef là short => dùng trực tiếp
        double defPercentage = this.tlDef;

        // Đệ tử bị quái đánh: cap % giảm sát thương tối đa 50%, flat def giảm 50%
        boolean isPetVsMob = this.player.isPet && attacker == null;
        if (isPetVsMob) {
            def = def / 2;
            if (defPercentage > 50) {
                defPercentage = 50;
            }
        }

        // Giới hạn giống gốc: tối đa 85%
        if (defPercentage > 85) {
            defPercentage = 85;
        }

        // Xuyên giáp
        double piercingPercent = 0;
        if (attacker != null
                && attacker.playerSkill != null
                && attacker.playerSkill.skillSelect != null
                && attacker.nPoint != null) {

            int skillId = attacker.playerSkill.skillSelect.template.id;

            // Chưởng
            if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
                piercingPercent = attacker.nPoint.tlxgc;
            } // Cận chiến
            else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK
                    || skillId == Skill.KAIOKEN || skillId == Skill.LIEN_HOAN) {
                piercingPercent = attacker.nPoint.tlxgcc;
            }
        }

        if (piercingPercent > 0) {
            def -= (int) (def * piercingPercent / 100.0);
            if (def < 0) {
                def = 0;
            }
        }

        dame -= def;
        if (dame < 0) {
            return 0;
        }

        if (piercingPercent > 0 && defPercentage > 0) {
            defPercentage -= defPercentage * piercingPercent / 100.0;
            if (defPercentage < 0) {
                defPercentage = 0;
            }
        }

        // Trừ % giảm sát thương từ option 94
        dame -= dame * defPercentage / 100.0;
        if (dame < 0) {
            dame = 0;
        }

        return dame;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        return switch (limitPower) {
            case 0 ->
                17999999999L;
            case 1 ->
                19999999999L;
            case 2 ->
                24999999999L;
            case 3 ->
                29999999999L;
            case 4 ->
                39999999999L;
            case 5 ->
                50010000000L;
            case 6 ->
                60010000000L;
            default ->
                0;
        };
    }

    public long getPowerNextLimit() {
        return switch (limitPower + 1) {
            case 0 ->
                17999999999L;
            case 1 ->
                19999999999L;
            case 2 ->
                24999999999L;
            case 3 ->
                29999999999L;
            case 4 ->
                39999999999L;
            case 5 ->
                50010000000L;
            case 6 ->
                60010000000L;
            default ->
                0;
        };
    }

    public int getHpMpLimit() {
        return switch (limitPower) {
            case 0 ->
                220000;
            case 1 ->
                240000;
            case 2 ->
                300000;
            case 3 ->
                350000;
            case 4 ->
                400000;
            case 5 ->
                450000;
            case 6 ->
                500000;
            default ->
                0;
        };
    }

    public int getDameLimit() {
        return switch (limitPower) {
            case 0 ->
                11000;
            case 1 ->
                12000;
            case 2 ->
                15000;
            case 3 ->
                18000;
            case 4 ->
                20000;
            case 5 ->
                22000;
            case 6 ->
                24000;
            default ->
                0;
        };
    }

    public short getDefLimit() {
        return switch (limitPower) {
            case 0 ->
                550;
            case 1 ->
                600;
            case 2 ->
                700;
            case 3 ->
                800;
            case 4 ->
                1000;
            case 5 ->
                1200;
            case 6 ->
                1400;
            default ->
                0;
        };
    }

    public byte getCritLimit() {
        return switch (limitPower) {
            case 0 ->
                5;
            case 1 ->
                6;
            case 2 ->
                7;
            case 3 ->
                8;
            case 4 ->
                9;
            case 5 ->
                10;
            case 6 ->
                10;
            default ->
                0;
        };
    }

    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    public void tiemNangDown(long tiemNang) {
        this.tiemNang -= tiemNang;
    }

    public void increasePoint(byte type, short point) {
        if (point <= 0 || point > 100) {
            return;
        }
        long tiemNangUse;
        if (type == 0) {
            int pointHp = point * 20;
            tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
            if ((this.hpg + pointHp) <= getHpMpLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 1) {
            int pointMp = point * 20;
            tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
            if ((this.mpg + pointMp) <= getHpMpLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 2) {
            TaskService.gI().checkDoneTaskNangCS(player);
            tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
            if ((this.dameg + point) <= getDameLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                }
                TaskService.gI().checkDoneTaskNangCS(player);
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 3) {
            tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
            if ((this.defg + point) <= getDefLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 4) {
            tiemNangUse = 50000000L;
            for (int i = 0; i < this.critg; i++) {
                tiemNangUse *= 5L;
            }
            if ((this.critg + point) <= getCritLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    critg += point;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        Service.gI().point(player);
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ tiềm năng");
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = ((50L * (((long) Math.pow(5L, critg) - 1L)) / (5L - 1L) * 1000000L));
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    // --------------------------------------------------------------------------
    private long lastTimeHoiPhuc;
    private long lastTimeHoiStamina;

    public void update() {
        if (player == null || player.effectSkill == null || player.playerSkill.skillSelect == null) {
            return;
        }
        if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
            int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
            if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill()
                    && (hp < hpMax || mp < mpMax)) {
                long hpRecovered = hpMax / 100 * tiLeHoiPhuc;
                long mpRecovered = mpMax / 100 * tiLeHoiPhuc;

                PlayerService.gI().hoiPhuc(player, hpRecovered, mpRecovered);

                if (player.effectSkill.countCharging % 3 == 0) {
                    Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                }
            } else {
                EffectSkillService.gI().stopCharge(player);
            }
            if (++player.effectSkill.countCharging >= 10) {
                EffectSkillService.gI().stopCharge(player);
            }
        }
        if (this.player.isBoss) {
            return;
        }
        if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
            PlayerService.gI().hoiPhuc(this.player, hpHoi, mpHoi);
            this.lastTimeHoiPhuc = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
            this.stamina++;
            this.lastTimeHoiStamina = System.currentTimeMillis();

            if (!this.player.isBoss && !this.player.isPet) {
                PlayerService.gI().sendCurrentStamina(this.player);
            }
        }
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
    }

    public long calPercent(long param, int percent) {
        return param * percent / 100;
    }

    private boolean isPorataFusion() {
        return this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                || this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                || this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3;
    }

   
    public double[] calculateBox1Bonus() {
        double[] bonus = { 0, 0, 0 }; 

        if (this.player == null || this.player.inventory == null || this.player.inventory.itemsBox1 == null) {
            return bonus;
        }

        // Đếm số item thực tế
        int filledSlots = countFilledSlots();

        // ✅ Nếu < 10 item: không cộng gì
        if (filledSlots < 10) {
            return bonus;
        }

        // 10+ item: +2% MP
        if (filledSlots >= 10) {
            bonus[2] += 2; // MP
        }

        // 20+ item: +4% MP (tích lũy = 2 + 2)
        if (filledSlots >= 20) {
            bonus[2] += 2; // MP tích lũy = 4%
        }

        // 30+ item: +1% ATK, +1% HP
        if (filledSlots >= 30) {
            bonus[0] += 1; // ATK
            bonus[1] += 1; // HP
        }

        // 40+ item: +2% ATK, +2% HP (tích lũy = 1 + 2 = 3%)
        if (filledSlots >= 40) {
            bonus[0] += 2; // ATK tích lũy = 3%
            bonus[1] += 2; // HP tích lũy = 3%
        }

        return bonus;
    }

    /**
     * Đếm số item thực tế trong rương box1
     */
    private int countFilledSlots() {
        int count = 0;
        if (this.player.inventory.itemsBox1 != null) {
            for (Item item : this.player.inventory.itemsBox1) {
                if (item != null && item.isNotNullItem()) {
                    count++;
                }
            }
        }
        return count;
    }

}
