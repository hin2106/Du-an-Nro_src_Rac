package player;
import item.Item;
import mob.Mob;
import services.ItemService;
import services.player.PlayerService;
import services.Service;
import services.player.InventoryService;
import services.map.MapService;
import utils.Util;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import services.EffectSkillService;
import services.ItemTimeService;
import skill.Skill;

public class EffectSkin {

    public static final String[] textOdo = new String[] {
            "Hôi quá, tránh xa ta ra", "Biến đi", "Trời ơi đồ ở dơ",
            "Thúi quá", "Mùi gì hôi quá"
    };

    private static final String[] textThoBulma = new String[] {
            "Wow, sexy quá"
    };

    private static final String[] textBuffSD = new String[] {
            "Wow! Sexy quá", };

    @Setter
    private Player player;

    public EffectSkin(Player player) {
        this.player = player;
        this.xHPKI = 1;
        this.xDame = 1;
    }

    public long lastTimeAttack;
    public long lastTimeVoHinh;
    private long lastTimeOdo;
    private long lastTimeltdb;
    private long lastTimeThoBulma;
    private long lastTimeMaPhongBa;
    private long lastTimeXenHutHpKi;
    private long lastTimeCoolDame;
    private long lastTimeCuteMp;
    private long lastTimeClanHeal;

    public long lastTimeAddTimeTrainArmor;
    public long lastTimeSubTimeTrainArmor;

    public boolean isVoHinh;

    public long lastTimeXHPKI;
    public int xHPKI;

    public long lastTimeXDame;
    public int xDame;

    public long lastTimeUpdateCTHT;

    private long lastTimeTanHinh;
    private long lastTimeLamCham;
    private long lastTimeHoaDa;
    public long lastTimeXChuong;
    public boolean isXChuong;
    public boolean isXDame;
    private long lastTimeHealAura10s;

    public void update() {
        updateVoHinh();
        if (!this.player.isDie() && this.player.zone != null
                && !MapService.gI().isMapOffline(this.player.zone.map.mapId)) {
            updateOdo();
            updateThoBulma();
            updateHealAura10s();
            updateCuteMp();
            updateClanHeal();
            updateMaPhongBa();
            updateXenHutXungQuanh();
            updateTanHinh();
            updateHoaDa();
            updateLamCham();
            updateXChuong();
            updateCoolDame();
            // updateHalloween();
        }
        if (!this.player.isBoss && !this.player.isPet && !player.isNewPet) {
            updateTrainArmor();
        }
        if (xHPKI != 1 && Util.canDoWithTime(lastTimeXHPKI, 1800000)) {
            xHPKI = 1;
            Service.gI().point(player);
        }
        if (xDame != 1 && Util.canDoWithTime(lastTimeXDame, 1800000)) {
            xDame = 1;
            Service.gI().point(player);
        }
        updateCTHaiTac();
    }

    private void updateCTHaiTac() {
        if (this.player.setClothes != null && this.player.setClothes.ctHaiTac != -1
                && this.player.zone != null
                && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
            int count = 0;
            int[] cts = new int[9];
            cts[this.player.setClothes.ctHaiTac - 618] = this.player.setClothes.ctHaiTac;
            List<Player> players = new ArrayList<>();
            players.add(player);
            try {
                for (Player pl : player.zone.getNotBosses()) {
                    if (!player.equals(pl) && pl.setClothes.ctHaiTac != -1 && Util.getDistance(player, pl) <= 300) {
                        cts[pl.setClothes.ctHaiTac - 618] = pl.setClothes.ctHaiTac;
                        players.add(pl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < cts.length; i++) {
                if (cts[i] != 0) {
                    count++;
                }
            }
            for (Player pl : players) {
                Item ct = pl.inventory.itemsBody.get(5);
                if (ct.isNotNullItem() && ct.template.id >= 618 && ct.template.id <= 626) {
                    for (Item.ItemOption io : ct.itemOptions) {
                        if (io.optionTemplate.id == 147
                                || io.optionTemplate.id == 77
                                || io.optionTemplate.id == 103) {
                            io.param = count * 3;
                        }
                    }
                }
                if (!pl.isPet && !pl.isNewPet && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
                    InventoryService.gI().sendItemBody(pl);
                }
                pl.effectSkin.lastTimeUpdateCTHT = System.currentTimeMillis();
            }
        }
    }

    private void updateHealAura10s() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.tlHpHoiAura10s > 0) {
                if (Util.canDoWithTime(lastTimeHealAura10s, 10000)) {
                    int percent = this.player.nPoint.tlHpHoiAura10s;
                    long selfHp = (this.player.nPoint.hpMax * percent / 100);
                    short icon = getIconForOptions(251);
                    PlayerService.gI().hoiPhuc(this.player, selfHp, 0);
                    if (icon > 0) {
                        ItemTimeService.gI().sendItemTime(this.player, icon, 11);
                    }
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 120) {
                            long hp = (pl.nPoint.hpMax * percent / 100);
                            PlayerService.gI().hoiPhuc(pl, hp, 0);
                            if (icon > 0) {
                                ItemTimeService.gI().sendItemTime(pl, icon, 11);
                            }
                        }
                    }
                    this.lastTimeHealAura10s = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Option 162: hồi KI mỗi giây cho người đeo và thành viên cùng bang ở gần. */
    private void updateCuteMp() {
        if (player.nPoint == null || player.nPoint.mpHoiCute <= 0
                || !Util.canDoWithTime(lastTimeCuteMp, 1000)) {
            return;
        }
        int percent = (int) Math.min(100, player.nPoint.mpHoiCute);
        PlayerService.gI().hoiPhuc(player, 0, player.nPoint.mpMax * percent / 100L);
        for (Player ally : player.zone.getNotBosses()) {
            if (isNearbyClanAlly(ally, 120)) {
                PlayerService.gI().hoiPhuc(ally, 0, ally.nPoint.mpMax * percent / 100L);
            }
        }
        lastTimeCuteMp = System.currentTimeMillis();
    }

    /** Option 173: mỗi 30 giây hồi HP/KI cho thành viên cùng bang, không hồi người đeo. */
    private void updateClanHeal() {
        if (player.nPoint == null
                || (player.nPoint.tlHpHoiBanThanVaDongDoi <= 0
                        && player.nPoint.tlMpHoiBanThanVaDongDoi <= 0)
                || !Util.canDoWithTime(lastTimeClanHeal, 30000)) {
            return;
        }
        int hpPercent = Math.min(100, player.nPoint.tlHpHoiBanThanVaDongDoi);
        int mpPercent = Math.min(100, player.nPoint.tlMpHoiBanThanVaDongDoi);
        for (Player ally : player.zone.getNotBosses()) {
            if (isNearbyClanAlly(ally, 120)) {
                PlayerService.gI().hoiPhuc(ally,
                        ally.nPoint.hpMax * hpPercent / 100L,
                        ally.nPoint.mpMax * mpPercent / 100L);
            }
        }
        lastTimeClanHeal = System.currentTimeMillis();
    }

    private boolean isNearbyClanAlly(Player ally, int range) {
        return ally != null && ally != player && !ally.isBoss && !ally.isDie() && ally.nPoint != null
                && player.clan != null && ally.clan != null && player.clan.equals(ally.clan)
                && Util.getDistance(player, ally) <= range;
    }

    private void updateXenHutXungQuanh() {
        try {
            if (this.player.nPoint != null
                    && (player.nPoint.hp < player.nPoint.hpMax || player.nPoint.mp < player.nPoint.mpMax)) {
                int param = this.player.nPoint.tlHutHpMpXQ;
                if (param > 0) {
                    if (!this.player.isDie() && Util.canDoWithTime(lastTimeXenHutHpKi, 5000)) {
                        int hpHut = 0;
                        int mpHut = 0;
                        List<Player> players = new ArrayList<>();
                        List<Player> playersMap = this.player.zone.getNotBosses();
                        for (Player pl : playersMap) {
                            if (!this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                    && Util.getDistance(this.player, pl) <= 200) {
                                players.add(pl);
                            }

                        }
                        for (Mob mob : this.player.zone.mobs) {
                            if (mob.point.gethp() > 1) {
                                if (Util.getDistance(this.player, mob) <= 200) {
                                    long subHp = mob.point.getHpFull() * param / 100;
                                    if (subHp >= mob.point.gethp()) {
                                        subHp = mob.point.gethp() - 1;
                                    }
                                    hpHut += subHp;
                                    mob.injured(null, subHp, false);
                                }
                            }
                        }
                        for (Player pl : players) {
                            long subHp = (pl.nPoint.hpMax * param / 100);
                            long subMp = (pl.nPoint.mpMax * param / 100);
                            if (subHp >= pl.nPoint.hp) {
                                subHp = pl.nPoint.hp - 1;
                            }
                            if (subMp >= pl.nPoint.mp) {
                                subMp = pl.nPoint.mp - 1;
                            }
                            hpHut += subHp;
                            mpHut += subMp;
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.gI().Send_Info_NV(pl);
                            pl.injured(null, subHp, true, false);
                        }
                        this.player.nPoint.addHp(hpHut);
                        this.player.nPoint.addMp(mpHut);
                        PlayerService.gI().sendInfoHpMpMoney(this.player);
                        Service.gI().Send_Info_NV(this.player);
                        this.lastTimeXenHutHpKi = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOdo() {
        try {
            if (this.player.nPoint != null) {
                int param = this.player.nPoint.tlHpGiamODo;
                if (param > 0) {
                    if (Util.canDoWithTime(lastTimeOdo, 10000)) {
                        List<Player> playersMap = this.player.zone.getNotBosses();
                        for (int i = playersMap.size() - 1; i >= 0; i--) {
                            Player pl = playersMap.get(i);
                            if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                    && Util.getDistance(this.player, pl) <= 200) {
                                long subHp = (pl.nPoint.hpMax * param / 100);
                                if (subHp >= pl.nPoint.hp) {
                                    subHp = pl.nPoint.hp - 1;
                                }
                                Service.gI().chat(pl, textOdo[Util.nextInt(0, textOdo.length - 1)]);
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                Service.gI().Send_Info_NV(pl);
                                pl.injured(null, subHp, true, false);
                            }

                        }
                        this.lastTimeOdo = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateThoBulma() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.tlSexyDame > 0) {
                if (Util.canDoWithTime(lastTimeThoBulma, 10000)) {
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (isNearbyClanAlly(pl, 120)) {
                            if (player.nPoint.isThoBulma) {
                                Service.gI().chat(pl, textThoBulma[Util.nextInt(0, textThoBulma.length - 1)]);
                            } else {
                                Service.gI().chat(pl, textBuffSD[Util.nextInt(0, textBuffSD.length - 1)]);
                            }
                            EffectSkillService.gI().setDameBuff(pl, 11000, player.nPoint.tlSexyDame);
                        }
                    }
                    this.lastTimeThoBulma = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCoolDame() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.tlCoolDame > 0) {
                if (Util.canDoWithTime(lastTimeCoolDame, 10000)) {
                    short icon = getIconForOptions(251);
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (isNearbyClanAlly(pl, 120)) {
                            EffectSkillService.gI().setDameBuff(pl, 11000, player.nPoint.tlCoolDame);
                            if (icon > 0) {
                                ItemTimeService.gI().sendItemTime(pl, icon, 11);
                            }
                        }
                    }
                    this.lastTimeCoolDame = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTanHinh() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isTanHinh) {
                if (Util.canDoWithTime(lastTimeTanHinh, 5000)) {
                    EffectSkillService.gI().setIsTanHinh(player, 1500);
                    this.lastTimeTanHinh = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHoaDa() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isHoaDa) {
                if (Util.canDoWithTime(lastTimeHoaDa, 30000)) {
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            EffectSkillService.gI().setIsStone(pl, 6000);
                        }

                    }
                    this.lastTimeHoaDa = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLamCham() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.isLamCham) {
                if (Util.canDoWithTime(lastTimeLamCham, 10000)) {

                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (pl != null && pl.nPoint != null && !this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            Service.gI().chat(pl, "Nặng quá!");
                            EffectSkillService.gI().setIsLamCham(pl, 5000);
                        }

                    }
                    this.lastTimeLamCham = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateXChuong() {
        try {
            if (this.player.nPoint != null && this.player.nPoint.xChuong != 0) {
                if (Util.canDoWithTime(lastTimeXChuong, 60000)) {
                    this.isXChuong = true;
                    this.lastTimeXChuong = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMaPhongBa() {
        try {
            if (this.player.effectSkill != null && this.player.effectSkill.isBinh
                    && this.player.effectSkill.playerUseMafuba != null) {
                if (Util.canDoWithTime(lastTimeMaPhongBa, 500)
                        && this.player.effectSkill.playerUseMafuba.playerSkill != null) {
                    long param = this.player.effectSkill.playerUseMafuba.playerSkill
                            .getSkillbyId(Skill.MA_PHONG_BA).point * (this.player.effectSkill.typeBinh == 0 ? 1 : 2);
                    long subHp = (this.player.effectSkill.playerUseMafuba.nPoint.hpMax * param / 100);
                    if (subHp >= this.player.nPoint.hp) {
                        subHp = Math.abs(this.player.nPoint.hp - 100);
                    }
                    PlayerService.gI().sendInfoHpMpMoney(this.player);
                    Service.gI().Send_Info_NV(this.player);
                    this.player.injured(this.player.effectSkill.playerUseMafuba, subHp, true, false);
                    this.lastTimeMaPhongBa = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private void updateHalloween() {
    // try {
    // if (this.player.effectSkill != null && this.player.effectSkill.isHalloween) {
    // if (Util.canDoWithTime(lastTimeHalloween, 10000)) {
    // List<Player> playersMap = this.player.zone.getNotBosses();
    // for (int i = playersMap.size() - 1; i >= 0; i--) {
    // Player pl = playersMap.get(i);
    // if (pl != null && pl.nPoint != null && !this.player.equals(pl) &&
    // pl.effectSkill != null && !pl.effectSkill.isHalloween && !pl.isPet &&
    // !pl.isDie()
    // && Util.getDistance(this.player, pl) <= 200) {
    // EffectSkillService.gI().setIsHalloween(pl, -1, 1800000);
    // }
    //
    // }
    // this.lastTimeHalloween = System.currentTimeMillis();
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // giáp tập luyện
    private void updateTrainArmor() {
        if (this.player == null || this.player.inventory == null) {
            return;
        }
        if (Util.canDoWithTime(lastTimeAddTimeTrainArmor, 60000) && !Util.canDoWithTime(lastTimeAttack, 30000)) {
            if (this.player.nPoint != null && this.player.nPoint.wearingTrainArmor
                    && this.player.inventory.trainArmor != null
                    && this.player.inventory.trainArmor.itemOptions != null) {
                for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                    if (io != null && io.optionTemplate != null && io.optionTemplate.id == 9) {
                        if (io.param < 1000) {
                            io.param++;
                            InventoryService.gI().sendItemBody(player);
                        }
                        break;
                    }
                }
            }
            this.lastTimeAddTimeTrainArmor = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeSubTimeTrainArmor, 60000)) {
            if (this.player.inventory.itemsBag != null) {
                for (Item item : this.player.inventory.itemsBag) {
                    if (item != null && item.isNotNullItem()) {
                        if (ItemService.gI().isTrainArmor(item) && item.itemOptions != null) {
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io != null && io.optionTemplate != null && io.optionTemplate.id == 9) {
                                    if (io.param > 0) {
                                        io.param--;
                                    }
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            if (this.player.inventory.itemsBox != null) {
                for (Item item : this.player.inventory.itemsBox) {
                    if (item != null && item.isNotNullItem()) {
                        if (ItemService.gI().isTrainArmor(item) && item.itemOptions != null) {
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io != null && io.optionTemplate != null && io.optionTemplate.id == 9) {
                                    if (io.param > 0) {
                                        io.param--;
                                    }
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            this.lastTimeSubTimeTrainArmor = System.currentTimeMillis();
            InventoryService.gI().sendItemBags(player);
            Service.gI().point(this.player);
        }
    }

    private void updateVoHinh() {
        if (this.player.nPoint != null && this.player.nPoint.wearingVoHinh) {
            if (Util.canDoWithTime(lastTimeAttack, 5000)) {
                isVoHinh = Util.canDoWithTime(lastTimeVoHinh, 5000);
            } else {
                isVoHinh = false;
            }
        } else {
            isVoHinh = false;
        }
    }

    public void dispose() {
        this.player = null;
    }

    private short getIconForOptions(int... optionIds) {
        try {
            if (this.player != null && this.player.inventory != null && this.player.inventory.itemsBody != null) {
                for (Item it : this.player.inventory.itemsBody) {
                    if (it != null && it.isNotNullItem() && it.itemOptions != null && it.template != null) {
                        for (Item.ItemOption io : it.itemOptions) {
                            if (io != null && io.optionTemplate != null) {
                                for (int id : optionIds) {
                                    if (io.optionTemplate.id == id) {
                                        return it.template.iconID;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
