package player;

import item.Item;
import services.Service;

public class SetClothes {

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }

    public byte songoku;
    public byte thienXinHang;
    public byte kirin;
    public byte kaioken;
    public byte thanVuTruKaio;

    public boolean huydietClothers;

    public boolean setblack;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;
    public byte lienHoan;
    public byte nail;

    public byte kakarot;
    public byte cadic;
    public byte nappa;
    public byte giamSatThuong;
    public byte cadicM;
    public byte setchampa;

    public byte worldcup;
    public byte setDHD;

    public byte gohan;

    public boolean godClothes;
    public int ctHaiTac = -1;

    private boolean hasSetEffect = false;
    private int currentSetEffectId = -1;

    public void setup() {
        setDefault();
        setupSKT();
        if (this.player.inventory == null || this.player.inventory.itemsBody == null) {
            updateSetEffect();
            return;
        }

        this.godClothes = true;
        int maxIndex = Math.min(5, this.player.inventory.itemsBody.size());
        for (int i = 0; i < maxIndex; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id > 567 || item.template.id < 555) {
                    this.godClothes = false;
                    break;
                }
            } else {
                this.godClothes = false;
                break;
            }
        }

        if (this.player.inventory.itemsBody.size() > 5) {
            Item ct = this.player.inventory.itemsBody.get(5);
            if (ct.isNotNullItem()) {
                switch (ct.template.id) {
                    case 618:
                    case 619:
                    case 620:
                    case 621:
                    case 622:
                    case 623:
                    case 624:
                    case 626:
                    case 627:
                        this.ctHaiTac = ct.template.id;
                        break;
                }
            }
        }
        updateSetEffect();
    }

    private void setupSKT() {
        if (this.player.inventory == null || this.player.inventory.itemsBody == null) {
            return;
        }
        int maxIndex = Math.min(5, this.player.inventory.itemsBody.size());
        for (int i = 0; i < maxIndex; i++) {
            Item item = this.player.inventory.itemsBody.get(i);

            if (item == null || !item.isNotNullItem()) {
                continue;
            }

            boolean isActSetForItem = false;
            for (Item.ItemOption io : item.itemOptions) {
                if (io == null || io.optionTemplate == null) {
                    continue;
                }
                switch (io.optionTemplate.id) {
                    case 129:
                    case 141:
                        isActSetForItem = true;
                        songoku++;
                        break;
                    case 127:
                    case 139:
                        isActSetForItem = true;
                        thienXinHang++;
                        break;
                    case 128:
                    case 140:
                        isActSetForItem = true;
                        kirin++;
                        break;
                    case 131:
                    case 143:
                        isActSetForItem = true;
                        ocTieu++;
                        break;
                    case 132:
                    case 144:
                        isActSetForItem = true;
                        pikkoroDaimao++;
                        break;
                    case 130:
                    case 142:
                        isActSetForItem = true;
                        picolo++;
                        break;
                    case 135:
                    case 138:
                        isActSetForItem = true;
                        nappa++;
                        break;
                    case 133:
                    case 136:
                        isActSetForItem = true;
                        kakarot++;
                        break;
                    case 134:
                    case 137:
                        isActSetForItem = true;
                        cadic++;
                        break;
                    case 250:
                    case 253:
                        isActSetForItem = true;
                        kaioken++;
                        break;
                    case 245:
                    case 246:
                    case 247:
                    case 248:
                        isActSetForItem = true;
                        thanVuTruKaio++;
                        break;
                    case 237:
                    case 238:
                    case 239:
                    case 240:
                        isActSetForItem = true;
                        nail++;
                        break;
                    case 241:
                    case 242:
                    case 243:
                    case 244:
                        isActSetForItem = true;
                        cadicM++;
                        break;
                    case 233:
                    case 236:
                        isActSetForItem = true;
                        gohan++;
                        break;
                    case 254:
                    case 255:
                    case 256:
                    case 257:
                        isActSetForItem = true;
                        setchampa++;
                        break;
                    case 21:
                        if (io.param == 80) {
                            setDHD++;
                        }
                        break;
                }

                if (isActSetForItem) {
                    break;
                }
            }
        }
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.kaioken = 0;
        this.thanVuTruKaio = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.setchampa = 0;
        this.nail = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.giamSatThuong = 0;
        this.cadicM = 0;
        this.worldcup = 0;
        this.setDHD = 0;
        this.gohan = 0;
        this.godClothes = false;
        this.ctHaiTac = -1;
    }

    private void updateSetEffect() {
        if (this.player == null || this.player.isBot || this.player.isBoss) {
            return;
        }
        boolean shouldHaveEffect = false;
        if (this.songoku == 5 || this.thienXinHang == 5 || this.kirin == 5 ||
                this.kaioken == 5 || this.thanVuTruKaio == 5 || this.ocTieu == 5 ||
                this.pikkoroDaimao == 5 || this.picolo == 5 || this.lienHoan == 5 ||
                this.nail == 5 || this.kakarot == 5 || this.cadic == 5 ||
                this.nappa == 5 || this.giamSatThuong == 5 || this.cadicM == 5 || this.setchampa == 5 ||
                this.gohan == 5 || this.setDHD == 5) {
            shouldHaveEffect = true;
        }

        if (this.godClothes) {
            shouldHaveEffect = true;
        }

        if (this.huydietClothers) {
            shouldHaveEffect = true;
        }

        if (this.setblack) {
            shouldHaveEffect = true;
        }

        if (shouldHaveEffect && !this.hasSetEffect) {
            enableSetEffect(504);
        } else if (!shouldHaveEffect && this.hasSetEffect) {
            disableSetEffect();
        }
    }

    private void enableSetEffect(int effectId) {
        try {
            if (this.player == null) {
                return;
            }
            if (this.hasSetEffect && this.currentSetEffectId != -1 && this.currentSetEffectId != effectId) {
                if (this.player.zone != null) {
                    Service.gI().removeEffPlayer(this.player, this.currentSetEffectId);
                }
            }

            this.hasSetEffect = true;
            this.currentSetEffectId = effectId;

            sendSetEffectToSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tắt hiệu ứng set hiện tại
     */
    private void disableSetEffect() {
        try {
            if (this.player == null || !this.hasSetEffect || this.currentSetEffectId == -1) {
                return;
            }
            if (this.player.zone != null) {
                Service.gI().removeEffPlayer(this.player, this.currentSetEffectId);
            }

            this.hasSetEffect = false;
            this.currentSetEffectId = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSetEffectToSelf() {
        try {
            if (this.player == null || !this.hasSetEffect || this.currentSetEffectId == -1) {
                return;
            }

            if (this.player.getSession() == null || this.player.zone == null) {
                return;
            }

            Service.gI().sendEffPlayer(this.player, this.player, this.currentSetEffectId, 1, -1, 1);
        } catch (Exception e) {
        }
    }

    public void sendSetEffectToPlayer(Player viewer) {
        try {
            if (this.player == null || viewer == null || !this.hasSetEffect || this.currentSetEffectId == -1) {
                return;
            }

            if (viewer.getSession() == null || viewer.zone == null) {
                return;
            }
            if (this.player != viewer && this.player.zone != viewer.zone) {
                return;
            }
            Service.gI().sendEffPlayer(this.player, viewer, this.currentSetEffectId, 1, -1, 1);
        } catch (Exception e) {
        }
    }

    public boolean hasSetEffect() {
        return this.hasSetEffect && this.currentSetEffectId != -1;
    }

    public boolean checkSetGod() {
        if (this.player.isBot) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 555 || item.template.id > 567) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetblack() {
        if (this.player.isBot || this.player.isBoss) {
            return false;
        }
        if (this.player.isPet) {
            return false;
        }
        int[] setblack = { 1731, 1702 };
        int checksetblack = 0;
        int maxIndex = Math.min(11, this.player.inventory.itemsBody.size());
        for (int i = 0; i < maxIndex; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                int id = item.template.id;
                if (id == setblack[0] || id == setblack[1]) {
                    checksetblack++;
                }
            }

        }
        this.setblack = checksetblack == 2;
        return this.setblack;
    }

    public boolean checkSetDes() {
        if (this.player.isBot) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 650 || item.template.id > 662) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean setGod14() {
        int setGod14Count = 0;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id >= 650 && item.template.id <= 663) {
                    setGod14Count++;
                } else {
                    this.huydietClothers = false;
                    return false;
                }
            } else {
                this.huydietClothers = false;
                return false;
            }
        }
        this.huydietClothers = setGod14Count == 5;
        return this.huydietClothers;
    }

    public boolean checkSetCadic() {
        if (this.player.isBot || this.player.isBoss) {
            return false;
        }
        int countCadic = 0;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item == null || !item.isNotNullItem()) {
                return false;
            }
            boolean hasCadicOption = false;
            if (item.itemOptions != null) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io != null && io.optionTemplate != null) {
                        if (io.optionTemplate.id == 134 || io.optionTemplate.id == 137) {
                            hasCadicOption = true;
                            countCadic++;
                            break;
                        }
                    }
                }
            }
            if (!hasCadicOption) {
                return false;
            }
        }
        return countCadic == 5;
    }

    public void dispose() {
        disableSetEffect();
        this.player = null;
    }
}