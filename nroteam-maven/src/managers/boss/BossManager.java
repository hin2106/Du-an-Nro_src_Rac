package managers.boss;
import boss.Boss;
import boss.BossID;
import boss.tau_77.TauBayBay;
import boss.nappa.Rambo;
import boss.nappa.MapDauDinh;
import boss.nappa.Kuku;
import boss.android.Android19;
import boss.android.Pic;
import boss.android.Android14;
import boss.android.Poc;
import boss.android.Android13;
import boss.android.KingKong;
import boss.android.DrKore;
import boss.android.Android15;
import boss.black.BlackGoku;
import utils.Util;
import boss.golden_fide.DeathBeam1;
import boss.golden_fide.DeathBeam2;
import boss.golden_fide.DeathBeam3;
import boss.golden_fide.DeathBeam4;
import boss.golden_fide.DeathBeam5;
import boss.golden_fide.GoldenFrieza;
import boss.cold.Cooler;
import boss.cell.SieuBoHung;
import boss.broly.Broly;
import boss.event.noel.OngGiaNoel;
import boss.fide.Fide;
import boss.event.hungvuong.SonTinh;
import boss.event.hungvuong.ThuyTinh;
import boss.event.halloween.BiMa;
import boss.event.halloween.Doi;
import boss.event.halloween.MaTroi;
import boss.event.trungthu.KhiDot;
import boss.mabu_12H.Mabu;
import boss.mabu_12H.BuiBui;
import boss.mabu_12H.BuiBui2;
import boss.mabu_12H.Cadic;
import boss.mabu_12H.Drabura;
import boss.mabu_12H.Drabura2;
import boss.mabu_12H.Drabura3;
import boss.mabu_12H.Goku;
import boss.mabu_12H.Yacon;
import boss.mabu_14H.Mabu2H;
import boss.mabu_14H.SuperBu;
import boss.ginyu_nappa.SO1;
import boss.ginyu_nappa.SO2;
import boss.ginyu_nappa.SO3;
import boss.ginyu_nappa.SO4;
import boss.ginyu_nappa.TDT;
import boss.ginyu_namek.SO1_NM;
import boss.ginyu_namek.SO2_NM;
import boss.ginyu_namek.SO3_NM;
import boss.ginyu_namek.SO4_NM;
import boss.ginyu_namek.TDT_NM;
import boss.earth.BIDO;
import boss.earth.BOJACK;
import boss.earth.BUJIN;
import boss.earth.KOGU;
import boss.earth.SUPER_BOJACK;
import boss.earth.ZANGYA;
import boss.yardat.CHIENBINH0;
import boss.yardat.CHIENBINH1;
import boss.yardat.CHIENBINH2;
import boss.yardat.CHIENBINH3;
import boss.yardat.CHIENBINH4;
import boss.yardat.CHIENBINH5;
import boss.yardat.DOITRUONG5;
import boss.yardat.TANBINH0;
import boss.yardat.TANBINH1;
import boss.yardat.TANBINH2;
import boss.yardat.TANBINH3;
import boss.yardat.TANBINH4;
import boss.yardat.TANBINH5;
import boss.yardat.TAPSU0;
import boss.yardat.TAPSU1;
import boss.yardat.TAPSU2;
import boss.yardat.TAPSU3;
import boss.yardat.TAPSU4;
import boss.cell.XENCON1;
import boss.cell.XENCON2;
import boss.cell.XENCON3;
import boss.cell.XENCON4;
import boss.cell.XENCON5;
import boss.cell.XENCON6;
import boss.cell.XENCON7;
import boss.event.newyear.LanCon;
import boss.miniboss.AnTrom;
import boss.miniboss.Odo;
import boss.miniboss.SoiHecQuyn;
import boss.miniboss.Virut;
import boss.miniboss.XinBaTo;
import boss.baotri.Su;
import boss.baotri.Mai;
import boss.baotri.Pilap;
import boss.cell.XenBoHung;
import boss.challenge.FideChallenge;
import boss.challenge.MabuChallenge;
import boss.challenge.XenBoHungChallenge;
import boss.chill.Chill;
import boss.event.noel.TuanLoc;
import boss.event.trungthu.Gogeta;
import boss.event.trungthu.Omega;
import boss.nguctu.Cumber;
import boss.pokemon.Pikachu;
import boss.pokemon.Squirtle;
import boss.pokemon.Bullbasaur;
import boss.pokemon.Charmender;
import boss.pokemon.Korochi;
import boss.pokemon.Meowth;
import boss.pokemon.Mushashi;
import consts.BossStatus;

import player.Player;
import network.Message;
import services.map.MapService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import map.Zone;
import server.Maintenance;
import server.ServerNotify;
import services.map.ChangeMapService;
import utils.Functions;
import utils.Logger;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public int getBossCount() {
        return this.bosses.size();
    }

    /** Unique boss types shown by the in-game admin command "boss". */
    public List<Boss> getDashboardBosses() {
        List<Boss> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        synchronized (this.bosses) {
            for (Boss boss : this.bosses) {
                if (boss == null || boss.data == null || boss.data.length == 0
                        || boss.data[0].getMapJoin() == null || boss.data[0].getMapJoin().length == 0) {
                    continue;
                }
                int mapId = boss.data[0].getMapJoin()[0];
                if (MapService.gI().isMapBossFinal(mapId) || MapService.gI().isMapHuyDiet(mapId)
                        || MapService.gI().isMapYardart(mapId) || MapService.gI().isMapMaBu(mapId)
                        || MapService.gI().isMapBlackBallWar(mapId) || !seen.add((int) boss.id)) {
                    continue;
                }
                result.add(boss);
            }
        }
        result.sort((a, b) -> Integer.compare((int) a.id, (int) b.id));
        return result;
    }

    public Boss findBossByBossID(int bossID) {
        for (Boss boss : this.bosses) {
            if (boss == null) {
                continue;
            }
            if (boss.id == bossID) {
                if (boss.zone != null && boss.zone.map != null) {
                    int mapId = boss.zone.map.mapId;
                    int zoneId = boss.zone.zoneId;

                    if (mapId >= 0 && zoneId >= 0) {
                        return boss;
                    }
                }
            }
        }
        return null;
    }

    public Boss findBossByUniqueId(int uniqueId) {
        int bossId = (uniqueId >> 16);
        int mapId = (uniqueId >> 8) & 0xFF;
        int zoneId = uniqueId & 0xFF;
        if (mapId == 0 && zoneId == 0) {
            return findBossByBossID(bossId);
        }
        for (Boss boss : this.bosses) {
            if (boss != null && boss.id == bossId && boss.zone != null && boss.zone.map != null
                    && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId) {
                return boss;
            }
        }
        return null;
    }

    public Set<Integer> getExistingBossIDs() {
        HashSet<Integer> ids = new HashSet<>();
        for (Boss boss : this.bosses) {
            if (boss != null) {
                ids.add((int) boss.id);
            }
        }
        return ids;
    }

    public void loadBoss(Player pl) {
        this.createBoss(BossID.BROLY, 50);
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.KING_KONG);
        this.createBoss(BossID.XEN_BO_HUNG);
        this.createBoss(BossID.SIEU_BO_HUNG);
        this.createBoss(BossID.KUKU, 3);
        this.createBoss(BossID.MAP_DAU_DINH, 3);
        this.createBoss(BossID.RAMBO, 3);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.COOLER);
        this.createBoss(BossID.BLACK_GOKU, 1);
        this.createBoss(BossID.GOLDEN_FRIEZA, 5);
        this.createBoss(BossID.SOI_HEC_QUYN1, 2);
        this.createBoss(BossID.AN_TROM);
        this.createBoss(BossID.O_DO1);
        this.createBoss(BossID.XIN_BA_TO);
        this.createBoss(BossID.CHILL);
        this.createBoss(BossID.CUMBER);
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {
                case BossID.BROLY ->
                    new Broly();
                case BossID.SUPER_BROLY ->
                    null;
                case BossID.TAP_SU_0 ->
                    new TAPSU0();
                case BossID.TAP_SU_1 ->
                    new TAPSU1();
                case BossID.TAP_SU_2 ->
                    new TAPSU2();
                case BossID.TAP_SU_3 ->
                    new TAPSU3();
                case BossID.TAP_SU_4 ->
                    new TAPSU4();
                case BossID.TAN_BINH_5 ->
                    new TANBINH5();
                case BossID.TAN_BINH_0 ->
                    new TANBINH0();
                case BossID.TAN_BINH_1 ->
                    new TANBINH1();
                case BossID.TAN_BINH_2 ->
                    new TANBINH2();
                case BossID.TAN_BINH_3 ->
                    new TANBINH3();
                case BossID.TAN_BINH_4 ->
                    new TANBINH4();
                case BossID.CHIEN_BINH_5 ->
                    new CHIENBINH5();
                case BossID.CHIEN_BINH_0 ->
                    new CHIENBINH0();
                case BossID.CHIEN_BINH_1 ->
                    new CHIENBINH1();
                case BossID.CHIEN_BINH_2 ->
                    new CHIENBINH2();
                case BossID.CHIEN_BINH_3 ->
                    new CHIENBINH3();
                case BossID.CHIEN_BINH_4 ->
                    new CHIENBINH4();
                case BossID.DOI_TRUONG_5 ->
                    new DOITRUONG5();
                case BossID.SO_4 ->
                    new SO4();
                case BossID.SO_3 ->
                    new SO3();
                case BossID.SO_2 ->
                    new SO2();
                case BossID.SO_1 ->
                    new SO1();
                case BossID.TIEU_DOI_TRUONG ->
                    new TDT();
                case BossID.SO_4_NM ->
                    new SO4_NM();
                case BossID.SO_3_NM ->
                    new SO3_NM();
                case BossID.SO_2_NM ->
                    new SO2_NM();
                case BossID.SO_1_NM ->
                    new SO1_NM();
                case BossID.TIEU_DOI_TRUONG_NM ->
                    new TDT_NM();
                case BossID.BUJIN ->
                    new BUJIN();
                case BossID.KOGU ->
                    new KOGU();
                case BossID.ZANGYA ->
                    new ZANGYA();
                case BossID.BIDO ->
                    new BIDO();
                case BossID.BOJACK ->
                    new BOJACK();
                case BossID.SUPER_BOJACK ->
                    new SUPER_BOJACK();
                case BossID.KUKU ->
                    new Kuku();
                case BossID.MAP_DAU_DINH ->
                    new MapDauDinh();
                case BossID.RAMBO ->
                    new Rambo();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN ->
                    new TauBayBay();
                case BossID.DRABURA ->
                    new Drabura();
                case BossID.BUI_BUI ->
                    new BuiBui();
                case BossID.BUI_BUI_2 ->
                    new BuiBui2();
                case BossID.YA_CON ->
                    new Yacon();
                case BossID.DRABURA_2 ->
                    new Drabura2();
                case BossID.GOKU ->
                    new Goku();
                case BossID.CADIC_12H ->
                    new Cadic();
                case BossID.MABU_12H ->
                    new Mabu();
                case BossID.DRABURA_3 ->
                    new Drabura3();
                case BossID.MABU ->
                    new Mabu2H();
                case BossID.SUPERBU ->
                    new SuperBu();
                case BossID.FIDE ->
                    new Fide();
                case BossID.DR_KORE ->
                    new DrKore();
                case BossID.ANDROID_19 ->
                    new Android19();
                case BossID.ANDROID_13 ->
                    new Android13();
                case BossID.ANDROID_14 ->
                    new Android14();
                case BossID.ANDROID_15 ->
                    new Android15();
                case BossID.PIC ->
                    new Pic();
                case BossID.POC ->
                    new Poc();
                case BossID.KING_KONG ->
                    new KingKong();
                case BossID.XEN_BO_HUNG ->
                    new XenBoHung();
                case BossID.SIEU_BO_HUNG ->
                    new SieuBoHung();
                case BossID.XEN_CON_1 ->
                    new XENCON1();
                case BossID.XEN_CON_2 ->
                    new XENCON2();
                case BossID.XEN_CON_3 ->
                    new XENCON3();
                case BossID.XEN_CON_4 ->
                    new XENCON4();
                case BossID.XEN_CON_5 ->
                    new XENCON5();
                case BossID.XEN_CON_6 ->
                    new XENCON6();
                case BossID.XEN_CON_7 ->
                    new XENCON7();
                case BossID.COOLER ->
                    new Cooler();
                case BossID.CHILL ->
                    new Chill();
                case BossID.KHIDOT ->
                    new KhiDot();
                case BossID.GOGETA ->
                    new Gogeta();
                case BossID.OMEGA ->
                    new Omega();
                case BossID.THO_DAI_CA ->
                    new boss.event.trungthu.ThoDaiCa();
                case BossID.GOLDEN_FRIEZA ->
                    new GoldenFrieza();
                case BossID.DEATH_BEAM_1 ->
                    new DeathBeam1();
                case BossID.DEATH_BEAM_2 ->
                    new DeathBeam2();
                case BossID.DEATH_BEAM_3 ->
                    new DeathBeam3();
                case BossID.DEATH_BEAM_4 ->
                    new DeathBeam4();
                case BossID.DEATH_BEAM_5 ->
                    new DeathBeam5();
                case BossID.BIMA ->
                    new BiMa();
                case BossID.MATROI ->
                    new MaTroi();
                case BossID.DOI ->
                    new Doi();
                case BossID.ONG_GIA_NOEL ->
                    new OngGiaNoel();
                case BossID.TUAN_LOC ->
                    new TuanLoc();
                case BossID.SON_TINH ->
                    new SonTinh();
                case BossID.THUY_TINH ->
                    new ThuyTinh();
                case BossID.LAN_CON ->
                    new LanCon();
                case BossID.SOI_HEC_QUYN1 ->
                    new SoiHecQuyn();
                case BossID.O_DO1 ->
                    new Odo();
                case BossID.VIRUT ->
                    new Virut();
                case BossID.AN_TROM ->
                    new AnTrom();
                case BossID.XIN_BA_TO ->
                    new XinBaTo();
                case BossID.BLACK_GOKU ->
                    new BlackGoku();
                case BossID.SU ->
                    new Su();
                case BossID.MAI ->
                    new Mai();
                case BossID.FILAP ->
                    new Pilap();
                case BossID.PIKACHU ->
                    new Pikachu();
                case BossID.CHARMENDER ->
                    new Charmender();
                case BossID.SQUIRTLE ->
                    new Squirtle();
                case BossID.BULLBASAUR ->
                    new Bullbasaur();
                case BossID.MUSHASHI ->
                    new Mushashi();
                case BossID.KOROCHI ->
                    new Korochi();
                case BossID.MEOWTH ->
                    new Meowth();
                case BossID.FIDE_CHALLENGE ->
                    new FideChallenge();
                case BossID.XEN_BO_HUNG_CHALLENGE ->
                    new XenBoHungChallenge();
                case BossID.MABU_CHALLENGE ->
                    new MabuChallenge();
                case BossID.CUMBER ->
                    new Cumber();
                default ->
                    null;
            };
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            msg.writer()
                    .writeByte((int) bosses.stream()
                            .filter(boss -> !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0]))
                            .count());
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                if ( MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(
                            boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("=))");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showBossDashboard(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(4);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh Sách Boss");
            long total = bosses.stream()
                    .filter(boss -> !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0]))
                    .count();
            msg.writer().writeByte((int) total);
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);

                if (MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])) {
                    continue;
                }

                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF("#" + boss.id + " - " + boss.data[0].getName());

                if (boss.zone != null) {
                    msg.writer().writeUTF("Trạng thái: " + boss.bossStatus);
                    msg.writer().writeUTF("Vị trí: " + boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu "
                            + boss.zone.zoneId);
                } else {
                    msg.writer().writeUTF("Trạng thái: " + boss.bossStatus);
                    msg.writer().writeUTF("Vị trí: chưa xuất hiện");
                }
            }

            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossByIdIncludingDead(int bossId) {
        return this.bosses.stream().filter(boss -> boss != null && boss.id == bossId).findFirst().orElse(null);
    }

    public Boss getBossByType(int bossType) {
        return this.bosses.stream().filter(boss -> boss instanceof Boss && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream()
                .filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie())
                .findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        if (player == null || player.zone == null) {
            return null;
        }
        int bossCloneId = Util.createIdBossClone((int) player.id);
        return player.zone.getBosses().stream().filter(boss -> boss.id == bossCloneId && !boss.isDie()).findFirst()
                .orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null
                && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst()
                .orElse(null);
    }

    public int[] getBossStatusCounts() {
        int aliveCount = 0;
        int respawningCount = 0;
        int waitingCount = 0;
        synchronized (this.bosses) {
            for (Boss boss : this.bosses) {
                if (boss == null || boss.bossStatus == null) {
                    continue;
                }
                switch (boss.bossStatus) {
                    case RESPAWN ->
                        respawningCount++;
                    case DIE, REST ->
                        waitingCount++;
                    default ->
                        aliveCount++;
                }
            }
        }
        return new int[]{aliveCount, respawningCount, waitingCount};
    }

    public void resetAllBosses() {
        synchronized (this.bosses) {
            for (Boss boss : this.bosses) {
                if (boss != null) {
                    try {
                        boss.changeStatus(BossStatus.REST);
                        if (boss.zone != null) {
                            ChangeMapService.gI().exitMap(boss);
                        }
                        boss.lastZone = null;
                        // boss.lastTimeRest.set(System.currentTimeMillis());
                    } catch (Exception e) {
                        Logger.error("Lỗi khi reset boss " + boss.name + ": " + e.getMessage());
                    }
                }
            }
        }
        ServerNotify.gI().notify("Tất cả boss đã được reset!");
        Logger.success("All bosses have been reset successfully.\n");
    }

    public int respawnAllRestingBosses() {
        int count = 0;
        synchronized (this.bosses) {
            for (Boss boss : this.bosses) {
                if (boss != null && boss.bossStatus == BossStatus.REST && boss.parentBoss == null) {
                    boss.changeStatus(BossStatus.RESPAWN);
                    count++;
                }
            }
        }
        if (count > 0) {
            ServerNotify.gI().notify("Toàn bộ Boss đã được hồi sinh theo lệnh Admin!");
            Logger.success(count + " All bosses revived.\n");
        }

        return count;
    }

//    @Override
//    public void run() {
//        while (!Maintenance.isRunning()) {
//            try {
//                long st = System.currentTimeMillis();
//                for (int i = this.bosses.size() - 1; i >= 0; i--) {
//                    try {
//                        this.bosses.get(i).update();
//                    } catch (Exception e) {
//                    }
//                }
//                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - st), 10));
//            } catch (Exception e) {
//            }
//        }
//    }
    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                long st = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    if (i < this.bosses.size()) {
                        Boss boss = this.bosses.get(i);
                        try {
                            boss.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                removeBoss(boss);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int removeAllByBossId(int bossId) {
        int removed = 0;
        synchronized (this.bosses) {
            for (int i = this.bosses.size() - 1; i >= 0; i--) {
                Boss b = this.bosses.get(i);
                if (b != null && b.id == bossId) {
                    try {
                        if (b.zone != null) {
                            try {
                                b.changeStatus(BossStatus.REST);
                            } catch (Exception ignored) {
                            }
                            services.map.ChangeMapService.gI().exitMap(b);
                        }
                    } catch (Exception ignored) {
                    }
                    this.bosses.remove(i);
                    removed++;
                }
            }

        }
        return removed;
    }

}
