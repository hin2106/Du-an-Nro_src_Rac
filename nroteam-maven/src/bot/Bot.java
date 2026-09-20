package bot;

import consts.ConstPlayer;
import java.util.Random;
import map.Map;
import map.Zone;
import player.Player;
import server.Manager;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.PlayerService;
import skill.NClass;
import skill.Skill;
import system.Template.SkillTemplate;
import utils.Util;

public class Bot extends Player {

    private static final int PLAYER_BODY_SLOT_COUNT = 11;
    private static final long NO_ZONE_LOG_INTERVAL = 15_000L;
    private static final long REJOIN_RETRY_INTERVAL = 5_000L;
    private static final int MAX_NO_ZONE_RETRIES = 24;

    private final short head_;
    private short body_;
    private short leg_;
    private int type;
    private int index_ = 0;
    private long lastNoZoneLogTime;
    private long lastRejoinAttemptTime;
    private int noZoneRetryCount;

    public ChatBot chatBot;
    public Sanb sanb;
    public Mobb mobb;
    // ===========================

    private Player plAttack;

    private int[] TraiDat = { 1, 2, 3, 4, 6, 29, 30, 28, 27, 42 };
    private int[] Namec = { 8, 9, 10, 11, 12, 13, 33, 34, 32, 31 };
    private int[] XayDa = { 15, 16, 17, 18, 19, 20, 37, 36, 35, 44, 52 };

    public Bot(short head, short body, short leg, int type, String name, ChatBot chat) {
        this.head_ = head;
        this.body_ = body;
        this.leg_ = leg;
        this.name = name;
        this.id = new Random().nextInt(2_000_000_000);
        this.type = type;
        this.isBot = true;

        // Bot dùng cùng cấu trúc trang bị 11 ô của người chơi hiện tại.
        // Tất cả ô phụ (cải trang, giáp luyện tập, thú cưỡi, đeo lưng, pet theo sau)
        // đều để trống; ngoại hình cơ bản được cung cấp bởi head_/body_/leg_.
        for (int i = 0; i < PLAYER_BODY_SLOT_COUNT; i++) {
            this.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }

        this.chatBot = chat;
        if (this.chatBot != null) {
            this.chatBot.bot = this;
        }
    }

    public int MapToPow() {
        Random random = new Random();
        long power = this.nPoint.power;
        int mapId = 21;
        if (power < 20_000_000) {
            mapId = switch (this.gender) {
                case 0 ->
                    TraiDat[random.nextInt(TraiDat.length)];
                case 1 ->
                    Namec[random.nextInt(Namec.length)];
                default ->
                    XayDa[random.nextInt(XayDa.length)];
            };
        } else if (power < 100_000_000) {
            mapId = 62 + random.nextInt(15);
        } else if (power < 1_000_000_000) {
            if (Util.isTrue(30, 100)) {
                mapId = 91 + random.nextInt(3);
            } else if (Util.isTrue(30, 100)) {
                mapId = 95 + random.nextInt(5);
            } else {
                mapId = 102 + random.nextInt(2);
            }
        } else {
            if (Util.isTrue(30, 100)) {
                mapId = 104 + random.nextInt(6);
            } else if (Util.isTrue(30, 100)) {
                mapId = 173 + random.nextInt(3);
            } else {
                mapId = 157 + random.nextInt(2);
            }
        }
        return mapId;
    }

    public void joinMap() {
        Zone zone = getRandomZone(MapToPow());
        if (zone != null) {
            ChangeMapService.gI().goToMap(this, zone);

            if (this.zone == null || this.location == null) {
                return;
            }

            if (zone.map != null && this.location != null) {
                int mapWidth = zone.map.mapWidth;
                int mapHeight = zone.map.mapHeight;
                Random rand = new Random();

                if (mapWidth > 0 && mapHeight > 0) {
                    this.location.x = (short) (rand.nextInt(Math.max(100, mapWidth - 100)) + 50);
                    this.location.y = (short) (rand.nextInt(Math.max(50, mapHeight / 2)));
                }
            }

            this.zone.load_Me_To_Another(this);
            this.noZoneRetryCount = 0;

            if (this.mobb != null) {
                this.mobb.lastTimeChanM = System.currentTimeMillis();
            }
        }
    }

    public Zone getRandomZone(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        Zone zone = null;
        try {
            if (map != null) {
                zone = map.zones.stream()
                        .filter(z -> z.getNumOfPlayers() == 0)
                        .findFirst()
                        .orElseGet(() -> {
                            Zone randomZone = map.zones.get(Util.nextInt(0, map.zones.size() - 1));
                            return randomZone.isFullPlayer() ? null : randomZone;
                        });
            }
        } catch (Exception e) {
        }
        if (zone != null) {
            this.index_ = 0;
            return zone;
        } else {
            this.index_++;
            if (this.index_ >= 20) {
                BotManager.gI().removeBot(this);
                ChangeMapService.gI().exitMap(this);
                return null;
            } else {
                return getRandomZone(MapToPow());
            }
        }
    }

    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isMonkey) {
            int idx = effectSkill.levelMonkey - 1;
            if (idx >= 0 && idx < ConstPlayer.HEADMONKEY.length) {
                return (short) ConstPlayer.HEADMONKEY[idx];
            }
        }
        return this.head_;
    }

    @Override
    public short getBody() {
        return effectSkill.isMonkey ? 193 : this.body_;
    }

    @Override
    public short getLeg() {
        return effectSkill.isMonkey ? 194 : this.leg_;
    }

    @Override
    public short getFlagBag() {
        return -1;
    }

    public int getType() {
        return this.type;
    }

    @Override
    public void update() {
        super.update();
        this.increasePoint();

        if (this.zone == null) {
            long now = System.currentTimeMillis();
            if (now - this.lastNoZoneLogTime >= NO_ZONE_LOG_INTERVAL) {
                this.lastNoZoneLogTime = now;
                System.err.println("[Bot] " + this.name + " không có zone, tham gia lại map");
            }
            if (now - this.lastRejoinAttemptTime >= REJOIN_RETRY_INTERVAL) {
                this.lastRejoinAttemptTime = now;
                this.noZoneRetryCount++;
                this.joinMap();
            }
            if (this.zone == null && this.noZoneRetryCount >= MAX_NO_ZONE_RETRIES) {
                System.err.println("[Bot] " + this.name + " retry join map quá nhiều, remove bot");
                BotManager.gI().removeBot(this);
                ChangeMapService.gI().exitMap(this);
            }
            return;
        }

        this.noZoneRetryCount = 0;

        try {
            switch (this.type) {
                case 0 -> {
                    if (this.mobb != null) {
                        this.mobb.update();
                    } else {
                        System.err.println("[Bot] " + this.name + " type=0 nhưng mobb=null");
                    }
                }
                case 1 -> {
                    if (this.sanb != null) {
                        this.sanb.update();
                    } else {
                        System.err.println("[Bot] " + this.name + " type=1 nhưng sanb=null");
                    }
                }
                case 2 -> {
                    if (this.chatBot != null) {
                        this.chatBot.update();
                    } else {
                        System.err.println("[Bot] " + this.name + " type=2 nhưng chatBot=null");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Bot] Lỗi update bot " + this.name + " type=" + this.type);
            e.printStackTrace();
        }

        if (this.isDie()) {
            Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
    }

    public void leakSkill() {
        for (NClass n : Manager.NCLASS) {
            if (n.classId == this.gender) {
                for (SkillTemplate template : n.skillTemplatess) {
                    for (Skill s : template.skillss) {
                        Skill clone = new Skill(s);
                        this.playerSkill.skills.add(clone);
                    }
                }
                break;
            }
        }
    }

    public boolean UseLastTimeSkill() {
        Skill skill = this.playerSkill.skillSelect;
        if (skill != null && skill.lastTimeUseThisSkill < System.currentTimeMillis() - skill.coolDown) {
            skill.lastTimeUseThisSkill = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void increasePoint() {
        long tiemNangUse;
        int point;
        if (this.nPoint != null) {
            if (Util.isTrue(50, 100)) {
                point = 100;
                int pointHp = point * 20;
                tiemNangUse = point * (2 * (this.nPoint.hpg + 1000) + pointHp - 20) / 2;
                if (doUseTiemNang(tiemNangUse)) {
                    this.nPoint.hpMax += point;
                    this.nPoint.hpg += point;
                    Service.gI().point(this);
                }
            } else {
                point = 10;
                tiemNangUse = point * (2 * this.nPoint.dameg + point - 1) / 2 * 100L;
                if (doUseTiemNang(tiemNangUse)) {
                    this.nPoint.dameg += point;
                    Service.gI().point(this);
                }
            }
        }
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.nPoint.tiemNang < tiemNang) {
            return false;
        } else {
            this.nPoint.tiemNang -= tiemNang;
            return true;
        }
    }

    public void useSkill(int skillId) {
        new Thread(() -> {
            switch (skillId) {
                case Skill.BIEN_KHI -> {
                    EffectSkillService.gI().sendEffectMonkey(this);
                    EffectSkillService.gI().setIsMonkey(this);
                    EffectSkillService.gI().sendEffectMonkey(this);
                    Service.gI().sendSpeedPlayer(this, 0);
                    Service.gI().Send_Caitrang(this);
                    Service.gI().sendSpeedPlayer(this, -1);
                    PlayerService.gI().sendInfoHpMp(this);
                    Service.gI().point(this);
                    Service.gI().Send_Info_NV(this);
                    Service.gI().sendInfoPlayerEatPea(this);
                }
                case Skill.QUA_CAU_KENH_KHI -> {
                    this.playerSkill.prepareQCKK = !this.playerSkill.prepareQCKK;
                    this.playerSkill.lastTimePrepareQCKK = System.currentTimeMillis();
                    SkillService.gI().sendPlayerPrepareSkill(this, 1000);
                }
                case Skill.MAKANKOSAPPO -> {
                    this.playerSkill.prepareLaze = !this.playerSkill.prepareLaze;
                    this.playerSkill.lastTimePrepareLaze = System.currentTimeMillis();
                    SkillService.gI().sendPlayerPrepareSkill(this, 3000);
                }
            }
        }).start();
    }
}
