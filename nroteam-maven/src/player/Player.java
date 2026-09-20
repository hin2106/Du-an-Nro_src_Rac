package player;

import audit.AssetAuditService;

import npc.NonInteractiveNPC;
import radar.Card;
import radar.RadarCard;
import services.RadarService;
import services.dungeon.MajinBuuService;
import skill.PlayerSkill;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import clan.Clan;
import intrinsic.IntrinsicPlayer;
import item.Item;
import item.ItemTime;
import npc.MagicTree;
import consts.ConstPlayer;
import consts.ConstTask;
import npc.MabuEgg;
import mob.MobMe;
import data.DataGame;
import clan.ClanMember;
import consts.ConstAchievement;
import map.Zone;
import interfaces.IPVP;
import matches.TYPE_LOSE_PVP;
import skill.Skill;
import services.Service;
import services.SkillService;
import task.TaskPlayer;
import network.Message;
import server.Client;
import services.EffectSkillService;
import services.player.FriendAndEnemyService;
import services.map.MapService;
import services.player.PlayerService;
import services.TaskService;
import services.map.ChangeMapService;
import combine.Combine;
import daos.DailyGiftDAO;
import daos.HistoryTransactionDAO;
import utils.Logger;
import utils.Util;
import utils.SkillUtil;
import java.util.ArrayList;
import services.dungeon.BlackBallWarService;
import managers.tournament.The23rdMartialArtCongressManager;
import map.ItemMap;
import map.MaBuHold;
import dungeon.MajinBuu14H;
import services.dungeon.SuperDivineWaterService;
import services.player.InventoryService;
import services.dungeon.NgocRongNamecService;
import static item.ItemTime.TEXT_NHIEM_VU_HANG_NGAY;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import network.MySession;
import player.badges.Badges;
import player.badges.BadgesData;
import services.ItemTimeService;
import services.PetService;
import services.player.DailyGiftService;
import services.shenron.SummonDragonBonney;
import task.BadgesTask;
import task.BadgesTaskService;
import task.KolTask;
import tournament.The23rdMartialArtCongress;

public class Player {
    public long lastTimeTuanLocTradePrompt;
    public long lastTimeEatPea;
    public long lastTimeSummonMobFromItem = 0;
    public Map<Integer, Long> activeEffects = new HashMap<>();
    public MySession session;
    public long id;
    public String name;
    public byte gender;
    public boolean isNewMember;
    public short head;
    public int deltaTime;
    public byte typePk;
    public byte cFlag;
    public boolean haveTennisSpaceShip;
    public IDMark iDMark;

    public Player playertarget;
    public Badges badges;

    public PlayerEvent event;
    public boolean isCopy;

    public boolean beforeDispose;

    public int mbv = 0;
    public boolean baovetaikhoan;
    public long mbvtime;

    public int timeGohome;

    public long lastUpdateGohomeTime;

    public boolean goHome;

    public long lastPkCommesonTime;

    public boolean callBossPocolo;
    public Zone zoneSieuThanhThuy;
    public boolean winSTT;
    public long lastTimeWinSTT;
    public long lastTimeUpdateSTT;

    public MajinBuu14H maBu2H;
    public boolean isMabuHold;
    public MaBuHold maBuHold;
    public int precentMabuHold;
    public boolean isPhuHoMapMabu;

    public boolean danhanthoivang;
    public long lastRewardGoldBarTime;

    public String nameClan;
    public int levelBDKBDone;
    public long timeBDKBDone;
    public long lastTimeUpdateTopBDKB;

    public int timesPerDayBDKB = 0;
    public long lastTimeJoinBDKB;

    public boolean joinCDRD;
    public long lastTimeJoinCDRD;
    public boolean talkToThuongDe;
    public boolean talkToThanMeo;
    public long timeChangeMap144;

    public long lastTimeJoinDT;

    public int typeChibi;
    public long lastTimeChibi;
    public long lastTimeUpdateChibi;

    public String captcha = "";
    public boolean doesNotAttack;
    public long lastTimePlayerNotAttack;
    public int timeNotAttack = 1800000;

    public boolean isPet;
    public boolean isNewPet;
    public boolean isNewPet1;
    public boolean isBoss;
    public boolean isPlayer;
    public IPVP pvp;
    public byte maxTime = 30;
    public byte type = 0;
    public boolean isOffline = false;

    public String notify = null;

    public int mapIdBeforeLogout;
    public List<Zone> mapBlackBall;
    public List<Zone> mapMaBu;

    public List<Player> temporaryEnemies = new ArrayList<>();
    public List<String> textRuongGo = new ArrayList<>();
    public Zone zone;
    public Zone mapBeforeCapsule;
    public List<Zone> mapCapsule;
    public Pet pet;
    public NewPet newPet;
    public MobMe mobMe;
    public Location location;
    public SetClothes setClothes;
    public EffectSkill effectSkill;
    public MabuEgg mabuEgg;
    public TaskPlayer playerTask;
    public ItemTime itemTime;
    public Fusion fusion;
    public MagicTree magicTree;
    public IntrinsicPlayer playerIntrinsic;
    public Inventory inventory;
    public PlayerSkill playerSkill;
    public Combine combineNew;
    public IDMark idMark;
    public Charms charms;
    public EffectSkin effectSkin;
    public NPoint nPoint;
    public RewardBlackBall rewardBlackBall;
    public FightMabu fightMabu;
    public NewSkill newSkill;
    public Satellite satellite;
    public Achievement achievement;
    public GiftCode giftCode;
    public Traning traning;

    public Clan clan;
    public ClanMember clanMember;

    public List<Friend> friends;
    public List<Enemy> enemies;

    public int masterId = -1;
    public int discipleId = -1;

    public boolean justRevived;
    public long lastTimeRevived;

    public long timeChangeZone;
    public long lastUseOptionTime;

    public short idNRNM = -1;
    public short idGo = -1;
    public long lastTimePickNRNM;

    public List<Card> Cards = new ArrayList<>();

    public int levelWoodChest;
    public long goldChallenge;
    public long rubyChallenge;
    public long lastTimeRewardWoodChest;
    public List<Item> itemsWoodChest = new ArrayList<>();
    public int indexWoodChest;
    public long lastTimePKDHVT23;

    public boolean lostByDeath;

    public boolean isPKDHVT;

    public int xSend;
    public int ySend;
    public boolean isFly;

    // shenron event
    public long lastTimeShenronAppeared;
    public boolean isShenronAppear;
    public SummonDragonBonney shenronEvent;

    // vo dai sinh tu
    public long lastTimePKVoDaiSinhTu;
    public boolean haveRewardVDST;
    public int gemVoDaiSinhTu;
    public long lastTimeFreeHalloweenWish;

    public long timePKVDST;

    public int binhChonHatMit;
    public int binhChonPlayer;
    public Zone zoneBinhChon;

    public ItemEvent itemEvent;

    public int levelLuyenTap;
    public boolean isThachDau;
    public int tnsmLuyenTap;
    public boolean dangKyTapTuDong;
    public long lastTimeOffline;
    public int mapIdDangTapTuDong;
    public int lastMapOffline;
    public int lastZoneOffline;
    public int lastXOffline;
    public String thongBaoTapTuDong;
    public boolean teleTapTuDong;
    public byte vip;
    public long timevip;
    public int timesPerDayCuuSat;
    public long lastTimeCuuSat;
    public boolean nhanVangNangVIP;
    public boolean nhanDeTuNangVIP;
    public boolean nhanSKHVIP;

    public long totalDamageTaken;
    public boolean thongBaoChangeMap;
    public String textThongBaoChangeMap;
    public boolean thongBaoThua;
    public String textThongBaoThua;

    public SuperRank superRank;

    public boolean canReward;
    public boolean changeMapVIP;
    public boolean haveReward;

    public int tayThong;
    public int ghiDanhPoint;
    public long ghiDanhPointTime;

    public int mayDoHitCount;
    public int mayDoPendingPoints;
    public long mayDoLastHitTime;

    public List<Item> itemsTradeWVP = new ArrayList<>();
    public long goldTradeWVP;
    public boolean tradeWVP;
    public long plIdWVP;

    private DropItem dropItem;
    public int eventPointType1;
    public int eventPointType2;
    public int eventPointType3;
    public int eventPointType4;
    public int eventPointType5;
    public int eventPointType6;
    public int eventPointType9;
    public int eventPointType10;
    public int eventPointType11;
    public long eventTimeType1;
    public long eventTimeType2;
    public long eventTimeType9;
    public long eventTimeType10;
    public long eventTimeType11;

    // List of NPCs where player has declined/received candy. Persisted in DB.
    public java.util.HashSet<Integer> candyDeclinedNpcIds = new java.util.HashSet<>();
    // Last date this list was reset (format YYYY-MM-DD)
    public String lastCandyResetDate;
    public boolean checkDailyReward;
    public boolean checkTopReward1;
    public boolean checkTopReward2;
    public boolean checkTopReward3;
    private String lastChatMessage;
    public List<BadgesData> dataBadges = new ArrayList<>();
    public List<BadgesTask> dataTaskBadges = new ArrayList<>();
    public long lastTimeChangeBadges;
    public int autoTrainState = 0;

    public KolTask kolTask;
    public KolTask kolTaskVip;

    public int rubyNormar;
    public int rubyVIP;
    public int id_CSMM_Ruby;

    public int gemNormar;
    public int gemVIP;
    public int id_CSMM_Gem;

    public int goldNormar;
    public int goldVIP;
    public int id_CSMM_Gold;

    public Date firstTimeLogin;

    public Long lastTimeOnline;
    public int goldXiu;
    public int goldTai;

    public List<Integer> BoughtSkill = new ArrayList<>();
    public LearnSkill LearnSkill;

    public int profanityWarnings = 0;
    public long accountLockUntil = 0;

    public byte CheckDayOnl;
    public long diemdanh;
    public int soDuVND;
    public int soThoiVang;

    public long tongNapTrongNgay;
    public long thoiGianNapCuoi;

    public int isBienBroly;

    public transient List<HistoryTransactionDAO.TransactionLog> lichSuGiaoDichDangXem;
    public transient int trangThaiLichSuGd;
    public List<DailyGiftDAO> dailyGiftDao = new ArrayList<>();
    public long lastDailyGiftReset = 0L;
    public transient boolean dailyGiftChecked = false;

    public boolean isBot;

    public boolean autoUnstuckEnabled = true;
    public boolean autoUnstuckLogoutAfter = false;
    public long lastTimeAutoUnstuckAttempt;
    public int autoUnstuckAttempts;

    private short lastAuraId = -1;
    public long lastTimeUseRadarCard = 0;

    public boolean loadmap = true;

    public Map<String, List<Item>> topRewards;
    public String Hppl = "\n";
    public int lastTrungThuDropDay;
    public String lastTrungThuDropBoot = null;
    public int lastHalloweenDropDay = -1;
    public String lastHalloweenDropBoot = null;
    public int lastGemDropDay = -1;
    public String lastGemDropBoot = null;
    public long createTime;
    public long timeUpSKH;
    public boolean daMuaGiaHanSKH;
    public boolean isUpdating;
    public final AtomicBoolean updateInProgress = new AtomicBoolean(false);

    public Player() {
        topRewards = new HashMap<>();
        LearnSkill = new LearnSkill();
        lastUseOptionTime = System.currentTimeMillis();
        location = new Location();
        nPoint = new NPoint(this);
        inventory = new Inventory();
        playerSkill = new PlayerSkill(this);
        setClothes = new SetClothes(this);
        effectSkill = new EffectSkill(this);
        fusion = new Fusion(this);
        playerIntrinsic = new IntrinsicPlayer();
        rewardBlackBall = new RewardBlackBall(this);
        fightMabu = new FightMabu(this);
        idMark = new IDMark();
        combineNew = new Combine();
        playerTask = new TaskPlayer();
        friends = new ArrayList<>();
        enemies = new ArrayList<>();
        itemTime = new ItemTime(this);
        charms = new Charms(this);
        effectSkin = new EffectSkin(this);
        newSkill = new NewSkill(this);
        satellite = new Satellite();
        achievement = new Achievement(this);
        giftCode = new GiftCode();
        traning = new Traning();
        itemEvent = new ItemEvent(this);
        superRank = new SuperRank(this);
        dropItem = new DropItem(this);
        event = new PlayerEvent(this);
        badges = new Badges();
        iDMark = new IDMark();
        masterId = -1;
        discipleId = -1;
        ghiDanhPoint = 0;
        ghiDanhPointTime = 0;
        mayDoHitCount = 0;
        mayDoPendingPoints = 0;
        mayDoLastHitTime = 0;
        eventTimeType1 = 0L;
        eventTimeType2 = 0L;
    }

    // --------------------------------------------------------------------------
    public boolean isAccountLocked() {
        return System.currentTimeMillis() < this.accountLockUntil;
    }

    /**
     * Kiểm tra player có die không Fix: Validate null checks
     */
    public boolean isDie() {
        if (this.nPoint != null) {
            return this.nPoint.hp <= 0;
        }
        // Nếu nPoint null, coi như die để an toàn
        return true;
    }

    public void sendMessage(Message msg) {
        if (this.session != null) {
            session.sendMessage(msg);
        }
    }

    public boolean isPl() {
        return isPlayer && !isPet && !isBoss && !isNewPet && !isNewPet1 && !isBot
                && !(this instanceof NonInteractiveNPC);
    }

    public boolean traning() {
        return itemTime != null && itemTime.isUseTDLT;
    }

    public boolean isDisconnected() {
        return this.session == null;
    }

    public MySession getSession() {
        return session;
    }

    public void setSession(MySession session) {
        this.session = session;
    }

    public void update() {
        fastUpdate();
        slowUpdate();
        AssetAuditService.gI().reconcileBackgroundBalance(this);
    }

    public void fastUpdate() {
        if (this.beforeDispose) {
            return;
        }
        try {
            if (this.zone != null || (!this.isPl() && this.zone == null)) {
                if (itemTime != null) {
                    itemTime.update();
                }
                if (magicTree != null) {
                    magicTree.update();
                }

                if (this.zone != null && hasEffect(this, 7143)) {
                    activeEffects.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());
                    this.spreadEffectToNearbyPlayers();
                }
            }
            if (effectSkill != null) {
                effectSkill.update();
            }

            if ((this.zone != null && !MapService.gI().isHome(this.zone.map.mapId))
                    || (!this.isPl() && this.zone == null)) {
                if (isPl() && idMark != null && idMark.isBan() && Util.canDoWithTime(idMark.getLastTimeBan(), 5000)) {
                    Client.gI().kickSession(session);
                    return;
                }

                if (nPoint != null) {
                    nPoint.update();
                }

                if (fusion != null) {
                    fusion.update();
                }

                if (mobMe != null) {
                    mobMe.update();
                }

                if (effectSkin != null) {
                    effectSkin.update();
                }
                if (pet != null) {
                    pet.update();
                }
                if (newPet != null) {
                    newPet.update();
                }
                if (satellite != null) {
                    satellite.update();
                }
                if (event != null) {
                    event.update();
                }
                if (!this.isPl() || this.isDie() || this.effectSkill == null) {
                    return;
                }
                if (this.effectSkill.isChibi && Util.canDoWithTime(lastTimeUpdateChibi, 1000)) {
                    boolean updated = false;
                    switch (this.typeChibi) {
                        case 1 -> { // Hồi MP
                            long mpMissing = this.nPoint.mpMax - this.nPoint.mp;
                            if (mpMissing > 0) {
                                long regen = this.nPoint.mpMax / 10;
                                this.nPoint.mp += Math.min(regen, mpMissing);
                                updated = true;
                            }
                        }
                        case 3 -> { // Hồi HP
                            long hpMissing = this.nPoint.hpMax - this.nPoint.hp;
                            if (hpMissing > 0) {
                                long regen = this.nPoint.hpMax / 10;
                                this.nPoint.hp += Math.min(regen, hpMissing);
                                updated = true;
                            }
                        }
                    }
                    if (updated) {
                        PlayerService.gI().sendInfoHpMp(this);
                        Service.gI().point(this);
                        lastTimeUpdateChibi = System.currentTimeMillis();
                    }
                }

                if (this.isPl() && this.effectSkill != null && this.effectSkill.isMabuHold) {
                    this.nPoint.subHP(this.nPoint.hpMax / 100);
                    if (Util.isTrue(1, 10)) {
                        Service.gI().chat(this, "Cứu tôi với");
                    }
                    PlayerService.gI().sendInfoHp(this);
                    if (this.precentMabuHold > 15) {
                        EffectSkillService.gI().removeMabuHold(this);
                    }
                    if (this.nPoint.hp <= 0) {
                        EffectSkillService.gI().removeMabuHold(this);
                        setDie();
                    }
                }
                if (dropItem != null) {
                    dropItem.update();
                }
                MajinBuuService.gI().update(this);
                SuperDivineWaterService.gI().update(this);
            }
        } catch (Exception e) {
            Logger.logException(Player.class, e, "Lỗi fastUpdate tại player: " + this.name);
        }
    }

    public void slowUpdate() {
        if (this.beforeDispose || !this.isPl() || this.zone == null) {
            return;
        }
        try {
            if (this.zone.map.mapId == this.gender + 21
                    && (TaskService.gI().getIdTask(this) == ConstTask.TASK_0_0
                    || TaskService.gI().getIdTask(this) == ConstTask.TASK_0_1)) {
                this.playerTask.taskMain.index = 2;
                TaskService.gI().sendTaskMain(this);
            }
            autoSendBadges();
            BadgesTaskService.updateDoneTask(this);
            sendTextTimeDaiLyGift();
            send_text_time_nhiem_vu();
            if (this.achievement != null) {
                this.achievement.done(ConstAchievement.HOAT_DONG_CHAM_CHI, 1000);
            }
            if (Util.canDoWithTime(lastTimeChibi, 300000)
                    && this.effectSkill != null && !this.effectSkill.isChibi
                    && !MapService.gI().isMapBlackBallWar(this.zone.map.mapId)) {
                lastTimeChibi = System.currentTimeMillis();
                if (Util.isTrue(15, 100)) {
                    EffectSkillService.gI().setChibi(this, 600_000); //Chibi activated for player
                }
            }
            if (this.effectSkin != null && this.effectSkin.xHPKI > 1
                    && !MapService.gI().isMapBlackBallWar(this.zone.map.mapId)) {
                this.effectSkin.xHPKI = 1;
                this.nPoint.calPoint();
                Service.gI().point(this);
            }

            if (this.effectSkin != null && this.effectSkin.xDame > 1
                    && !MapService.gI().isMapBlackBallWar(this.zone.map.mapId)) {
                this.effectSkin.xDame = 1;
                this.nPoint.calPoint();
                Service.gI().point(this);
            }

            fixBlackBallWar();

            if (this.zone.map.mapId == (21 + this.gender) && this.mabuEgg != null) {
                this.mabuEgg.sendMabuEgg();
            }
            if (this.isPl() && combineNew != null && combineNew.timeDelay > 0) {
                if ((System.currentTimeMillis() - combineNew.startTimeDelay) >= combineNew.timeDelay) {
                    Service.gI().sendThongBao(this, "Túi trà khô đã sấy xong");
                    combineNew.timeDelay = 0;
                }
            }


            // if (this.inventory != null && this.inventory.itemsBody != null 
            //         && this.inventory.itemsBody.size() >= 8) {
            //     Item mount = this.inventory.itemsBody.get(7);
            //     boolean hasItem1922 = mount != null && mount.isNotNullItem() && mount.template.id == 1922;
                
            //     if (hasItem1922) {
            //         if (this.isFly && (this.mobMe == null || this.mobMe.isDie())) {
            //             if (lastTimeSummonMobFromItem == 0 || Util.canDoWithTime(lastTimeSummonMobFromItem, 300000)) {
            //                 if (this.mobMe != null) {
            //                     this.mobMe.mobMeDie();
            //                     this.mobMe.dispose();
            //                     this.mobMe = null;
            //                 }
            //                 this.mobMe = new MobMe(this, 96, 120);
            //                 lastTimeSummonMobFromItem = System.currentTimeMillis();
            //             }
            //         }
            //     } else if (this.mobMe != null && this.mobMe.tempId == 96) {
            //         this.mobMe.mobMeDie();
            //         this.mobMe.dispose();
            //         this.mobMe = null;
            //         lastTimeSummonMobFromItem = 0;
            //     }
            // }

            if (this.isPhuHoMapMabu && !MapService.gI().isMapMabu2H(this.zone.map.mapId)) {
                this.isPhuHoMapMabu = false;
                this.nPoint.calPoint();
                Service.gI().point(this);
                Service.gI().Send_Info_NV(this);
                Service.gI().Send_Caitrang(this);
            }

            if (this.clan != null && this.clan.ConDuongRanDoc != null
                    && this.joinCDRD && this.clan.ConDuongRanDoc.allMobsDead
                    && this.talkToThanMeo && this.zone.map.mapId == 47
                    && Util.canDoWithTime(timeChangeMap144, 5000)) {
                ChangeMapService.gI().changeMapYardrat(this, this.clan.ConDuongRanDoc.getMapById(144),
                        300 + Util.nextInt(-100, 100), 312);
                this.timeChangeMap144 = System.currentTimeMillis();
            }

            if (!MapService.gI().isMapMaBu(this.zone.map.mapId) && (this.cFlag == 9 || this.cFlag == 10)) {
                Service.gI().changeFlag(this, 0);
            }

            if (MapService.gI().isMapMaBu(this.zone.map.mapId) && this.cFlag != 9 && this.cFlag != 10) {
                Service.gI().changeFlag(this, Util.nextInt(9, 10));
            }

            if (this.superRank != null) {
                if (Util.isAfterMidnight(this.superRank.lastRewardTime)) {
                    this.superRank.reward();
                }
                if (this.superRank.rank < 10 && TaskService.gI().getIdTask(this) == ConstTask.TASK_34_0) {
                    // TaskService.gI().checkDoneTaskSuperRank(this);
                }
            }
            if (!isBoss && this.idMark != null && this.idMark.isGotoFuture()
                    && Util.canDoWithTime(this.idMark.getLastTimeGoToFuture(), 60000)) {
                ChangeMapService.gI().changeMapBySpaceShip(this, 102, -1, Util.nextInt(60, 200));
                this.idMark.setGotoFuture(false);

            }
        } catch (Exception e) {
            Logger.logException(Player.class,
                    e, "Lỗi slowUpdate tại player: " + this.name);
        }
    }

    public void autoSendBadges() {
        boolean hasRemovedActiveBadge = false;
        Iterator<BadgesData> iterator = dataBadges.iterator();
        
        while (iterator.hasNext()) {
            BadgesData data = iterator.next();
            
            // Kiểm tra danh hiệu hết hạn
            if (System.currentTimeMillis() >= data.timeofUseBadges) {
                // Nếu danh hiệu đang dùng bị hết hạn → cần recalc stats
                if (data.isUse) {
                    hasRemovedActiveBadge = true;
                }
                iterator.remove();
            } else if (data.isUse) {
                badges.idBadges = data.idBadGes;
            }
        }
        
        // Nếu đã xóa danh hiệu đang dùng → reset và recalc stats
        if (hasRemovedActiveBadge) {
            badges.idBadges = -1;
            this.nPoint.calPoint();  // Cập nhật lại stats ngay lập tức
        }
        
        // Gửi badge info cho client mỗi 10s
        if (badges.idBadges != -1 && Util.canDoWithTime(badges.lastTimeSendBadges, 10000)) {
            Service.gI().sendBadgesPlayer(this, 5, badges.idBadges);
            badges.lastTimeSendBadges = System.currentTimeMillis();
            this.nPoint.update();
            Service.gI().point(this);
        }
    }

    private static final short[][] idOutfitFusion = {
        {380, 381, 382}, {383, 384, 385}, {391, 392, 393},
        {1204, 1205, 1206}, {2049, 2050, 2051}, {1210, 1211, 1212},
        {870, 871, 872}, {873, 874, 875}, {867, 868, 869},
        {2168, 2169, 2170}, {2174, 2175, 2176}, {2171, 2172, 2173},
        {1853, 1856, 1857}, {1812, 1815, 1816}, {1807, 1810, 1811}
    };

    public static final short[][] idOutfitGod = {
        {-1, 472, 473}, {-1, 476, 477}, {-1, 474, 475}
    };

    public static final short[][][] idOutfitHalloween = {
        {
            {545, 548, 549}, {547, 548, 549}, {546, 548, 549}
        },
        {
            {2082, 2085, 2086}, {2084, 2085, 2086}, {2083, 2085, 2086}
        },
        {
            {760, 761, 762}, {760, 761, 762}, {760, 761, 762}
        },
        {
            {654, 655, 656}, {654, 655, 656}, {654, 655, 656}
        },
        {
            {651, 652, 653}, {651, 652, 653}, {651, 652, 653}
        }};

    public static final short[][] idOutfitMafuba = {
        {1221, 1222, 1223}, {-1, -1, -1}, {1218, 1219, 1220}
    };

    public int getHat() {
        return -1;
    }

    public void updateAura() {
        short currentAura = getAura();
        if (currentAura != lastAuraId) {
            lastAuraId = currentAura;
            Service.gI().sendAura(this);
        }
    }

    public byte getAura() {
        if (!isPl() || this.Cards.isEmpty()) {
            return -1;
        }
        for (Card card : this.Cards) {
            if (card != null && card.Level >= 1 && card.Used == 1) {
                RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                        .filter(r -> r.Id == card.Id).findFirst().orElse(null);
                if (radarTemplate != null) {
                    if (!radarTemplate.AuraByLevel.isEmpty()) {
                        int lvl = Math.max(1, card.Level);
                        int idx = Math.min(lvl - 1, radarTemplate.AuraByLevel.size() - 1);
                        return radarTemplate.AuraByLevel.get(idx).byteValue();
                    }
                    return (byte) radarTemplate.AuraId;
                }
            }
        }
        return -1;
    }

    public byte getEffFront() {
        if (this.inventory == null) {
            return -1;
        }
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        int levelAo = 0;
        Item.ItemOption optionLevelAo = null;
        int levelQuan = 0;
        Item.ItemOption optionLevelQuan = null;
        int levelGang = 0;
        Item.ItemOption optionLevelGang = null;
        int levelGiay = 0;
        Item.ItemOption optionLevelGiay = null;
        int levelNhan = 0;
        Item.ItemOption optionLevelNhan = null;
        Item itemAo = this.inventory.itemsBody.get(0);
        Item itemQuan = this.inventory.itemsBody.get(1);
        Item itemGang = this.inventory.itemsBody.get(2);
        Item itemGiay = this.inventory.itemsBody.get(3);
        Item itemNhan = this.inventory.itemsBody.get(4);
        for (Item.ItemOption io : itemAo.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelAo = io.param;
                optionLevelAo = io;
                break;
            }
        }
        for (Item.ItemOption io : itemQuan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelQuan = io.param;
                optionLevelQuan = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGang.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGang = io.param;
                optionLevelGang = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGiay.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGiay = io.param;
                optionLevelGiay = io;
                break;
            }
        }
        for (Item.ItemOption io : itemNhan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelNhan = io.param;
                optionLevelNhan = io;
                break;
            }
        }
        if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null
                && optionLevelNhan != null
                && levelAo >= 8 && levelQuan >= 8 && levelGang >= 8 && levelGiay >= 8 && levelNhan >= 8) {
            return 8;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null
                && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 7 && levelQuan >= 7 && levelGang >= 7 && levelGiay >= 7 && levelNhan >= 7) {
            return 7;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null
                && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 6 && levelQuan >= 6 && levelGang >= 6 && levelGiay >= 6 && levelNhan >= 6) {
            return 6;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null
                && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 5 && levelQuan >= 5 && levelGang >= 5 && levelGiay >= 5 && levelNhan >= 5) {
            return 5;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null
                && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 4 && levelQuan >= 4 && levelGang >= 4 && levelGiay >= 4 && levelNhan >= 4) {
            return 4;
        } else {
            return -1;
        }
    }

    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isStone) {
            return 454;
        }
        if (effectSkill != null && effectSkill.isCarrot) {
            return 406;
        }
        if (effectSkill != null && effectSkill.isHalloween) {
            int outfitIdx = effectSkill.idOutfitHalloween;
            int gIdx = (this.gender >= 0 && this.gender < idOutfitHalloween[0].length) ? this.gender : 0;
            if (outfitIdx >= 0 && outfitIdx < idOutfitHalloween.length) {
                return idOutfitHalloween[outfitIdx][gIdx][0];
            }
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            int idx = effectSkill.levelMonkey - 1;
            if (idx >= 0 && idx < ConstPlayer.HEADMONKEY.length) {
                return (short) ConstPlayer.HEADMONKEY[idx];
            }
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 412;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (nPoint != null && nPoint.isGogeta) {
                return 2100;
            } else if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (nPoint != null && nPoint.levelBT == 3) {
                    return idOutfitFusion[9 + this.gender][0];
                }
                return idOutfitFusion[6 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[12 + this.gender][0];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int headId = inventory.itemsBody.get(5).template.head;
            if (headId > 0) { // must be a valid positive icon ID; -1 = no override, 0 = missing/unset (treat as no override)
                return (short) headId;
            }
        }
        // FIX: Ensure we never return invalid head ID
        if (this.head <= 0) {
            Logger.error("WARNING: Player " + this.id + " has invalid base head: " + this.head + ", using default 516");
            return 516;  // Default head for male characters
        }
        return this.head;
    }

    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isStone) {
            return 455;
        }
        if (effectSkill != null && effectSkill.isCarrot) {
            return 407;
        }
        if (effectSkill != null && effectSkill.isHalloween) {
            int outfitIdx = effectSkill.idOutfitHalloween;
            int gIdx = (this.gender >= 0 && this.gender < idOutfitHalloween[0].length) ? this.gender : 0;
            if (outfitIdx >= 0 && outfitIdx < idOutfitHalloween.length) {
                return idOutfitHalloween[outfitIdx][gIdx][1];
            }
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 413;
        } else if (isPhuHoMapMabu && fusion != null && fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return idOutfitGod[this.gender][1];
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (nPoint != null && nPoint.isGogeta) {
                return 2101;
            } else if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                // if (this.pet.typePet == 1) {
                // return idOutfitFusion[3 + this.gender][1];
                // }
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (nPoint != null && nPoint.levelBT == 3) {
                    return idOutfitFusion[9 + this.gender][1];
                }
                return idOutfitFusion[6 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[12 + this.gender][1];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int body = inventory.itemsBody.get(5).template.body;
            if (body > 0) { // must be a valid positive icon ID; -1 = no override, 0 = missing/unset
                return (short) body;
            }
        }
        if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }
        return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
    }

    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isStone) {
            return 456;
        }
        if (effectSkill != null && effectSkill.isCarrot) {
            return 408;
        }
        if (effectSkill != null && effectSkill.isHalloween) {
            int outfitIdx = effectSkill.idOutfitHalloween;
            int gIdx = (this.gender >= 0 && this.gender < idOutfitHalloween[0].length) ? this.gender : 0;
            if (outfitIdx >= 0 && outfitIdx < idOutfitHalloween.length) {
                return idOutfitHalloween[outfitIdx][gIdx][2];
            }
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 414;
        } else if (isPhuHoMapMabu && fusion != null && fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return idOutfitGod[this.gender][2];
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (nPoint != null && nPoint.isGogeta) {
                return 2102;
            } else if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {

                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (nPoint != null && nPoint.levelBT == 3) {
                    return idOutfitFusion[9 + this.gender][2];
                }
                return idOutfitFusion[6 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[12 + this.gender][2];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int leg = inventory.itemsBody.get(5).template.leg;
            if (leg > 0) { // must be a valid positive icon ID; -1 = no override, 0 = missing/unset
                return (short) leg;
            }
        }
        if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }
        return (short) (gender == 1 ? 60 : 58);
    }

    public short getFlagBag() {
        if (this.idMark != null && this.idMark.isHoldBlackBall()) {
            return 31;
        } else if (this.idNRNM >= 353 && this.idNRNM <= 359) {
            return 30;
        }
        if (TaskService.gI().getIdTask(this) == ConstTask.TASK_3_2) {
            return 28;
        }
        if (this.inventory != null && this.inventory.itemsBody != null) {
            if (this.inventory.itemsBody.size() >= 8) {
                if (this.inventory.itemsBody.get(8).isNotNullItem()) {
                    return this.inventory.itemsBody.get(8).template.part;
                }
            }
            if (this.isPet && this.inventory.itemsBody.size() >= 7) {
                if (this.inventory.itemsBody.get(6).isNotNullItem()) {
                    return this.inventory.itemsBody.get(6).template.part;
                }
            }
        }
        if (this.clan != null) {
            return (short) this.clan.imgId;
        }
        return -1;
    }

    public short getMount() {
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 8) {
            return -1;
        }
        Item item = this.inventory.itemsBody.get(7);
        if (!item.isNotNullItem()) {
            return -1;
        }
        if (item.template.type == 24 || item.template.type == 23) {
            if (item.template.gender == 3 || item.template.gender == this.gender) {
                return item.template.id;
            } else {
                return -1;
            }
        } else {
            if (item.template.id < 500) {
                return item.template.id;
            } else {
                Short value = (Short) DataGame.MAP_MOUNT_NUM.get(item.template.id);
                return value != null ? value : -1;
            }
        }
    }

    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        // Một tick tấn công có thể đang chạy đúng lúc player rời/chuyển khu.
        // Giữ snapshot để các kiểm tra map phía dưới không dereference zone null.
        Zone injuredZone = this.zone;
        if (injuredZone == null || injuredZone.map == null) {
            return 0;
        }
        if (!this.isDie()) {
            if (plAtt != null && !plAtt.equals(this)) {
                setTemporaryEnemies(plAtt);
            }

            if (plAtt != null && plAtt.playerSkill.skillSelect != null && !plAtt.isBoss
                    && MapService.gI().isMapMaBu(injuredZone.map.mapId)) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN ->
                        damage = damage > this.nPoint.hpMax / 20 ? this.nPoint.hpMax / 20 : damage;
                }
            }
            if (plAtt != null && plAtt.isBoss) {
                this.effectSkin.isVoHinh = false;
                this.effectSkin.lastTimeVoHinh = System.currentTimeMillis();
            }
            if (plAtt != null && plAtt.effectSkill != null && plAtt.effectSkill.isBinh
                    && !Util.canDoWithTime(plAtt.effectSkill.lastTimeUpBinh, 3000)) {
                return 0;
            }
            if (plAtt != null && plAtt.isPl() && this.maBuHold != null && this.zone != null
                    && this.zone.map.mapId == 128) {
                this.precentMabuHold++;
                damage = 1;
            }
            if (plAtt != null && plAtt.idNRNM != -1 && (this.isBoss || this.isNewPet)) {
                return 1;
            }
            if (plAtt != null && (plAtt.idNRNM != -1 || this.idNRNM != -1) && plAtt.clan != null && this.clan != null
                    && plAtt.clan == this.clan) {
                Service.gI().chatJustForMe(plAtt, this, "Ê cùng bang mà");
                return 0;
            }
            if (!Util.canDoWithTime(this.lastTimeRevived, 1500)) {
                return 0;
            }

            if (plAtt != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC -> {
                        if (this.nPoint.voHieuChuong > 0) {
                            PlayerService.gI().hoiPhuc(this, 0, (int) (damage * this.nPoint.voHieuChuong / 100));
                            return 0;
                        }
                    }
                }
            }
            int tlNeDon = this.nPoint.tlNeDon;
            // Đệ tử bị quái đánh: cap né đòn tối đa 20%
            if (isMobAttack && this.isPet && tlNeDon > 20) {
                tlNeDon = 20;
            }
            if (plAtt != null && !isMobAttack && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN, Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DICH_CHUYEN_TUC_THOI ->
                        tlNeDon -= plAtt.nPoint.tlchinhxac;
                    default ->
                        tlNeDon = 0;
                }
            }

            if (tlNeDon > 90) {
                tlNeDon = 90;
            }

            if (Util.isTrue(tlNeDon, 100)) {
                return 0;
            }

            // Chỉ trừ giáp một lần (truyền thông tin kẻ tấn công để tính xuyên giáp)
            if (!piercing) {
                damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            }

            boolean isUseGX = false;
            if (!piercing && plAtt != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN, Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DICH_CHUYEN_TUC_THOI ->
                        isUseGX = true;
                }
            }
            if ((isUseGX || isMobAttack) && this.itemTime != null) {
                if (this.itemTime.isUseGiapXen && !this.itemTime.isUseGiapXen2) {
                    damage /= 2;
                }
                if (this.itemTime.isUseGiapXen2) {
                    damage = damage / 100 * 40;
                }
            }

            if (effectSkill.isShielding) {
                if (this.idMark != null) {
                    this.idMark.setDamePST((int) Math.min(damage, 2_000_000_000));
                }
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                // Khiên năng lượng: mọi đòn nhận vào được giảm còn 1 sát thương.
                damage = 1;
            }
            if (isMobAttack && this.charms.tdBatTu > System.currentTimeMillis() && damage >= this.nPoint.hp) {
                damage = this.nPoint.hp - 1;
            }
            if (isMobAttack && damage >= this.nPoint.hp && hasClanOcTieuProtection()) {
                damage = this.nPoint.hp - 1;
            }
            if (isMobAttack && itemTime != null && nPoint != null) {
                boolean isHalloweenMode = itemTime.isHallowen && itemTime.timeHallowen > 0;
                if (isHalloweenMode && damage >= nPoint.hp) {
                    damage = nPoint.hp - 1;
                }
            }
            if (injuredZone.map.mapId == 129) {
                if (damage >= this.nPoint.hp) {
                    this.lostByDeath = true;
                    The23rdMartialArtCongress mc = The23rdMartialArtCongressManager.gI().getMC(injuredZone);
                    if (mc != null) {
                        mc.die();
                    }
                    return 0;
                }
            }

            if (damage > 0 && injuredZone.map.mapId == 51) {
                this.totalDamageTaken += damage;
            }

            if (damage > 0) {
                this.nPoint.subHP(damage);
            }

            if ((plAtt != null || isMobAttack) && isDie() && !isBoss && !isNewPet && !isNewPet1) {
                if (plAtt != null && this.isPl()) {
                    TaskService.gI().checkDoneTaskPK(plAtt);
                    if (this.idMark != null && this.idMark.isHoldBlackBall()) {
                        // TaskService.gI().checkDoneTaskNRSD(plAtt);
                    }
                }
                if (Util.isTrue(this.nPoint.tlBom, 100)) {
                    setBom(plAtt);
                } else {
                    setDie(plAtt);
                }
            }
            return damage;
        } else {
            return 0;
        }
    }

    public void setTemporaryEnemies(Player pl) {
        if (!temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    protected void setBom(Player plAtt) {
        if (zone != null && nPoint != null) {
            Skill bomLevel4 = SkillUtil.createSkill(Skill.TU_SAT, 4);
            long damage = nPoint.hpMax;
            if (bomLevel4 != null) {
                damage = damage * bomLevel4.damage / 100L;
                SkillService.gI().sendPlayerBomEffect(this, bomLevel4.skillId);
            }
            for (mob.Mob mob : new ArrayList<>(zone.mobs)) {
                if (mob != null && !mob.isDie() && Util.getDistance(this, mob) <= 200) {
                    mob.injured(this, damage, true);
                }
            }
            for (Player target : new ArrayList<>(zone.getHumanoids())) {
                if (target != null && target != this && !target.isDie()
                        && Util.getDistance(this, target) <= 200
                        && SkillService.gI().canAttackPlayer2(this, target)) {
                    target.injured(this, damage, true, false);
                }
            }
        }
        setDie(plAtt);
    }

    /** Set Ốc Tiêu đủ 5 món bảo vệ thành viên cùng bang trong bán kính 120 khỏi quái kết liễu. */
    private boolean hasClanOcTieuProtection() {
        if (zone == null || clan == null) {
            return false;
        }
        for (Player ally : zone.getNotBosses()) {
            if (ally != null && ally != this && !ally.isDie() && ally.clan != null
                    && clan.equals(ally.clan) && ally.setClothes != null && ally.setClothes.ocTieu == 5
                    && Util.getDistance(this, ally) <= 120) {
                return true;
            }
        }
        return false;
    }

    public void setDie() {
        this.setDie(null);
    }

    protected void setDie(Player plAtt) {
        int groundY = -1;
        if (this.location != null && this.zone != null && this.isPl()) {
            groundY = this.zone.map.yPhysicInTop(this.location.x, 100);
        }

        if (this.isPl()) {
            double vangtru = this.nPoint.power / 1_000_000L;
            if (vangtru > 32000) {
                vangtru = 32000;
            }
            int vang = (int) (vangtru - Util.nextInt(10, 100));
            if (this.inventory.gold >= vang && vang >= 1) {
                this.inventory.gold -= vang;
                Service.gI().sendMoney(this);

                int vangDrop = vang * 95 / 100;
                int itemId;
                if (vangDrop < 10000) {
                    itemId = 189;
                } else if (vangDrop < 20000) {
                    itemId = 188;
                } else {
                    itemId = 190;
                }
                int itemY = (groundY > 0) ? groundY : this.location.y;
                Service.gI().dropItemMap(this.zone,
                        new ItemMap(zone, itemId, vangDrop, this.location.x, itemY, this.id));
            }
        }

        int mapid = this.zone.map.mapId;
        double phanTramSucManhBiTru = 0.0;
        if (MapService.gI().isMapUpPorata(mapid)) {
            phanTramSucManhBiTru = 0.004;
        } else if (MapService.gI().isMap3Planets(mapid)) {
            phanTramSucManhBiTru = 0.002;
        }

        if (phanTramSucManhBiTru > 0) {
            int dieuKien = (int) (this.nPoint.power * phanTramSucManhBiTru);
            if (dieuKien < 1) {
                dieuKien = 1;
            }
            if (this.nPoint.power >= dieuKien) {
                this.nPoint.power -= dieuKien;
                Service.gI().point(this);
            }
        }

        boolean needUpdatePoint = false;

        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            needUpdatePoint = true;
        }
        if (this.effectSkin.xDame > 1) {
            this.effectSkin.xDame = 1;
            needUpdatePoint = true;
        }
        if (needUpdatePoint) {
            Service.gI().point(this);
        }

        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        this.effectSkill.removeSkillEffectWhenDie();
        this.nPoint.setHp(0);
        this.nPoint.setMp(0);
        if (this.location != null && this.zone != null && this.isPl() && groundY > 0) {
            if (this.location.y < groundY || this.location.y < 0) {
                this.location.y = groundY;
                try {
                    Message msg = new Message(46);
                    msg.writer().writeShort(this.location.x);
                    msg.writer().writeShort(this.location.y);
                    this.sendMessage(msg);
                    msg.cleanup();

                } catch (Exception e) {
                    utils.Logger.logException(Player.class,
                            e, "Error sending position update when die");
                }
            }
        }

        if (this.mobMe != null) {
            this.mobMe.mobMeDie();
            this.mobMe.dispose();
            this.mobMe = null;
        }

        // Remove khỏi spatial grid khi player chết
        if (this.zone != null && this.zone.spatialGridEnabled && this.zone.spatialGrid != null
                && this.isPl() && this.location != null) {
            try {
                this.zone.spatialGrid.removePlayer(this);
            } catch (Exception e) {
                utils.Logger.logException(Player.class, e, "Lỗi remove player khỏi spatial grid khi die");
            }
        }

        Service.gI().charDie(this);
        if (!this.isPet && !this.isNewPet && !this.isNewPet1 && !this.isBoss
                && plAtt != null && !plAtt.isPet && !plAtt.isNewPet && !plAtt.isNewPet1 && !plAtt.isBoss && !isBot) {

            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);

                if (TaskService.gI().getIdTask(plAtt) == ConstTask.TASK_14_0) {
                    TaskService.gI().checkDoneTaskKillPlayer(plAtt);
                }
            }
        }

        this.typePk = 0;
        if (this.pvp != null) {
            this.pvp.lose(this, TYPE_LOSE_PVP.DEAD);
        }
        BlackBallWarService.gI().dropBlackBall(this);
        NgocRongNamecService.gI().dropNamekBall(this);
    }

    public void setClanMember() {
        if (this.clanMember != null) {
            this.clanMember.powerPoint = this.nPoint.power;
            this.clanMember.head = this.getHead();
            this.clanMember.body = this.getBody();
            this.clanMember.leg = this.getLeg();
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isAdmin() {
        return this.session != null && this.session.isAdmin;
    }

    public void setJustRevivaled() {
        this.justRevived = true;
        this.lastTimeRevived = System.currentTimeMillis();
    }

    public boolean isActive() {
        return (this.isPl() && this.session != null && this.session.actived)
                || (this.isPet && ((Pet) this).master.session != null && ((Pet) this).master.session.actived);
    }

    // public void sendNewPet() {
    //     if (isPl() && inventory != null && inventory.itemsBody.get(9) != null) {
    //         Item it = inventory.itemsBody.get(9);//bo_pet
    //         if (it != null && it.isNotNullItem()) {
    //             PetService.Pet2(this, it.template.head, it.template.body, it.template.leg);
    //             Service.gI().point(this);
    //         }
    //     }
    // }

    private void fixBlackBallWar() {
        int x = this.location.x;
        int y = this.location.y;
        switch (this.zone.map.mapId) {
            case 85, 86, 87, 88, 89, 90, 91 -> {
                if (this.isPl()) {
                    if (x < 24 || x > this.zone.map.mapWidth - 24 || y < 0 || y > this.zone.map.mapHeight - 24) {
                        if (MapService.gI().getWaypointPlayerIn(this) == null) {
                            Service.gI().resetPoint(this, x, this.zone.map.yPhysicInTop(this.location.x, 100));
                            this.nPoint.hp -= this.nPoint.hpMax / 10;
                            PlayerService.gI().sendInfoHp(this);
                            return;
                        }
                    }
                    int yTop = this.zone.map.yPhysicInTop(this.location.x, this.location.y);
                    if (yTop >= this.zone.map.mapHeight - 24) {
                        Service.gI().resetPoint(this, x, this.zone.map.yPhysicInTop(this.location.x, 100));
                        this.nPoint.hp -= this.nPoint.hpMax / 10;
                        PlayerService.gI().sendInfoHp(this);
                    }
                }
            }
        }
    }

    public void move(int _toX, int _toY) {
        if (_toX != this.location.x) {
            this.location.x = _toX;
        }
        if (_toY != this.location.y) {
            this.location.y = _toY;
        }
        MapService.gI().sendPlayerMove(this);
    }

    public void dispose() {
        if (itemsTradeWVP != null) {
            if (!itemsTradeWVP.isEmpty()) {
                for (Item item : itemsTradeWVP) {
                    InventoryService.gI().addItemBag(this, item);
                }
            }
            itemsTradeWVP.clear();
            itemsTradeWVP = null;
        }
        if (pet != null) {
            pet.dispose();
            pet = null;
        }
        if (newPet != null) {
            newPet.dispose();
            newPet = null;
        }
        if (mapBlackBall != null) {
            mapBlackBall.clear();
            mapBlackBall = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapMaBu != null) {
            mapMaBu.clear();
            mapMaBu = null;
        }
        mapBeforeCapsule = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (mobMe != null) {
            mobMe.dispose();
            mobMe = null;
        }
        location = null;
        if (setClothes != null) {
            setClothes.dispose();
            setClothes = null;
        }
        if (effectSkill != null) {
            effectSkill.dispose();
            effectSkill = null;
        }
        if (mabuEgg != null) {
            mabuEgg.dispose();
            mabuEgg = null;
        }
        if (playerTask != null) {
            playerTask.dispose();
            playerTask = null;
        }
        if (itemTime != null) {
            itemTime.dispose();
            itemTime = null;
        }
        if (fusion != null) {
            fusion.dispose();
            fusion = null;
        }
        if (magicTree != null) {
            magicTree.dispose();
            magicTree = null;
        }
        if (playerIntrinsic != null) {
            playerIntrinsic.dispose();
            playerIntrinsic = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (playerSkill != null) {
            playerSkill.dispose();
            playerSkill = null;
        }
        if (combineNew != null) {
            combineNew.dispose();
            combineNew = null;
        }
        if (idMark != null) {
            idMark.dispose();
            idMark = null;
        }
        iDMark = null;
        if (charms != null) {
            charms.dispose();
            charms = null;
        }
        if (effectSkin != null) {
            effectSkin.dispose();
            effectSkin = null;
        }
        if (nPoint != null) {
            nPoint.dispose();
            nPoint = null;
        }
        if (rewardBlackBall != null) {
            rewardBlackBall.dispose();
            rewardBlackBall = null;
        }
        if (pvp != null) {
            pvp.dispose();
            pvp = null;
        }
        if (superRank != null) {
            superRank.dispose();
            superRank = null;
        }
        if (dropItem != null) {
            dropItem.dispose();
            dropItem = null;
        }
        if (satellite != null) {
            satellite = null;
        }
        if (achievement != null) {
            achievement.dispose();
            achievement = null;
        }
        if (giftCode != null) {
            giftCode.dispose();
            giftCode = null;
        }
        if (traning != null) {
            traning = null;
        }
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (Cards != null) {
            Cards.clear();
            Cards = null;
        }
        if (itemsWoodChest != null) {
            itemsWoodChest.clear();
            itemsWoodChest = null;
        }
        if (friends != null) {
            friends.clear();
            friends = null;
        }
        if (enemies != null) {
            enemies.clear();
            enemies = null;
        }
        if (temporaryEnemies != null) {
            temporaryEnemies.clear();
            temporaryEnemies = null;
        }
        itemsWoodChest = null;
        Cards = null;
        itemEvent = null;
        maBu2H = null;
        maBuHold = null;
        zoneSieuThanhThuy = null;
        thongBaoTapTuDong = null;
        notify = null;
        clan = null;
        clanMember = null;
        friends = null;
        enemies = null;
        session = null;
        newSkill = null;
        name = null;
        textThongBaoChangeMap = null;
        textThongBaoThua = null;
    }

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(String message) {
        this.lastChatMessage = message;
    }

    public boolean hasEffect(Player player, int effectId) {
        Long effectEndTime = player.activeEffects.get(effectId);
        return effectEndTime != null && System.currentTimeMillis() < effectEndTime;
    }

    public void spreadEffectToNearbyPlayers() {
        if (hasEffect(this, 7143)) {
            try {
                List<Player> playersMap = this.zone.getNotBosses();
                for (Player targetPlayer : playersMap) {
                    if (targetPlayer != null && !targetPlayer.isDie() && targetPlayer != this
                            && !hasEffect(targetPlayer, 7143) && Util.getDistance(this, targetPlayer) <= 200) {

                        long effectDuration = 10000;
                        long effectEndTime = System.currentTimeMillis() + effectDuration;
                        targetPlayer.activeEffects.put(7143, effectEndTime);
                        ItemTimeService.gI().sendItemTime(targetPlayer, 7143, (int) (effectDuration / 1000));
                        Service.gI().chat(targetPlayer, "Bạn đã bị lây hiệu ứng từ " + this.name + "!");

                        System.out.println("[DEBUG] Hiệu ứng lan sang: " + targetPlayer.name);
                    }
                }
            } catch (Exception e) {
            }
        } else {
            System.out.println("[DEBUG] Không có hiệu ứng 7143, không lan ra cho người chơi khác.");
        }
    }

    public long lastimelogin3;

    public void send_text_time_nhiem_vu() {
        if (this.playerTask != null && this.playerTask.sideTask != null && this.playerTask.sideTask.template != null) {
            if (Util.canDoWithTime(lastimelogin3, 60_000)) {
                try {
                    String name = this.playerTask.sideTask.getName();
                    int percent = this.playerTask.sideTask.getPercentProcess();
                    ItemTimeService.gI().sendTextTime(this, TEXT_NHIEM_VU_HANG_NGAY,
                            "Nhiệm vụ hằng ngày: " + name + " (" + percent + "%)", 20);
                    lastimelogin3 = System.currentTimeMillis();
                } catch (Exception e) {
                    Logger.error("Lỗi khi gửi nhiệm vụ hằng ngày: " + e.getMessage());
                }
            }
        }
    }

    public long lastTimeSendTextTime;

    public void sendTextTimeDaiLyGift() {
        if (Util.canDoWithTime(lastTimeSendTextTime, 300000)) {
            if (DailyGiftService.checkDailyGift(this, DailyGiftService.NHAN_BUA_MIEN_PHI)) {
                ItemTimeService.gI().sendTextTime(this, ItemTime.TEXT_NHAN_BUA_MIEN_PHI,
                        "Nhận ngẫu nhiên bùa 1h mỗi ngày tại Bà Hạt Mít ở vách núi", 15);
            }
            lastTimeSendTextTime = System.currentTimeMillis();
        }
    }
}
