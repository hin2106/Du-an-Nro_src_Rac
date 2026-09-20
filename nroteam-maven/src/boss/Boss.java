package boss;

import consts.AppearType;
import consts.BossStatus;
import static consts.BossStatus.ACTIVE;
import static consts.BossStatus.AFK;
import static consts.BossStatus.CHAT_E;
import static consts.BossStatus.CHAT_S;
import static consts.BossStatus.DIE;
import static consts.BossStatus.JOIN_MAP;
import static consts.BossStatus.LEAVE_MAP;
import static consts.BossStatus.RESPAWN;
import static consts.BossStatus.REST;
import consts.BossType;
import static consts.BossType.ANTROM;
import static consts.BossType.BOSS12H;
import static consts.BossType.BROLY;
import static consts.BossType.CHRISTMAS_EVENT;
import static consts.BossType.FINAL;
import static consts.BossType.HALLOWEEN_EVENT;
import static consts.BossType.HUNGVUONG_EVENT;
import static consts.BossType.PHOBAN;
import static consts.BossType.PHOBANBDKB;
import static consts.BossType.PHOBANCDRD;
import static consts.BossType.PHOBANDT;
import static consts.BossType.PHOBANKGHD;
import static consts.BossType.SKILLSUMMONED;
import static consts.BossType.TET_EVENT;
import static consts.BossType.TRUNGTHU_EVENT;
import static consts.BossType.YARDART;
import consts.ConstPlayer;
import data.DataGame;
import managers.boss.*;
import network.Message;
import java.util.List;
import map.Zone;
import mob.Mob;
import player.Pet;
import player.Player;
import skill.Skill;
import server.ServerNotify;

import services.EffectSkillService;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;
import interfaces.IBoss;
import item.Item;

import java.io.IOException;
import java.util.ArrayList;
import map.Map;
import static player.Player.idOutfitMafuba;

public class Boss extends Player implements IBoss {

    public int currentLevel = -1;
    public final BossData[] data;

    public BossStatus bossStatus;

    public Zone lastZone;

    protected long lastTimeRest;
    protected int secondsRest;
    protected boolean restByTimeout;

    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;

    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;

    protected long lastTimeChatM;
    protected int timeChatM;

    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    protected Player playerTarger;

    public Boss parentBoss;
    public Boss[][] bossAppearTogether;

    public Zone zoneFinal = null;

    public Player playerReward;

    public int lv;

    public int error;

    public boolean prepareBom;

    public boolean isNotifyDisabled;
    public boolean isZone01SpawnDisabled;
    //logic tinh sat thuong gay ra cho bos
    public java.util.Map<Integer, Long> statisticsDamage = new java.util.HashMap<>();

    public Boss(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data)
            throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        BossManager.gI().addBoss(this);
        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        boss.lv = j;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        switch (bossType) {
            case YARDART ->
                YardartManager.gI().addBoss(this);
            case FINAL ->
                FinalBossManager.gI().addBoss(this);
            case BOSS12H ->
                Boss12hManager.gI().addBoss(this);
            case SKILLSUMMONED ->
                SkillSummonedManager.gI().addBoss(this);
            case BROLY ->
                BrolyManager.gI().addBoss(this);
            case PHOBAN ->
                OtherBossManager.gI().addBoss(this);
            case PHOBANDT ->
                RedRibbonHQManager.gI().addBoss(this);
            case PHOBANBDKB ->
                TreasureUnderSeaManager.gI().addBoss(this);
            case PHOBANCDRD ->
                SnakeWayManager.gI().addBoss(this);
            case PHOBANKGHD ->
                GasDestroyManager.gI().addBoss(this);
            case TRUNGTHU_EVENT ->
                TrungThuEventManager.gI().addBoss(this);
            case HALLOWEEN_EVENT ->
                HalloweenEventManager.gI().addBoss(this);
            case CHRISTMAS_EVENT ->
                ChristmasEventManager.gI().addBoss(this);
            case HUNGVUONG_EVENT ->
                HungVuongEventManager.gI().addBoss(this);
            case TET_EVENT ->
                LunarNewYearEventManager.gI().addBoss(this);
            case ANTROM ->
                AnTromManager.gI().addBoss(this);
        }

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    @Override
    public void initBase() {
        BossData data = this.data[this.currentLevel];
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002;
        this.nPoint.dameg = data.getDame();
        this.nPoint.hpg = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        this.nPoint.hp = nPoint.hpg;
        this.nPoint.calPoint();
        this.initSkill();
        this.resetBase();
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }

        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;

        int[][] skillTemps = data[this.currentLevel].getSkillTemp();

        for (int[] skillTemp : skillTemps) {

            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);

            if (skill == null) {
                // Tạo fallback cho skill đặc biệt (tuyệt kỹ) nếu không có trong DB
                if (skillTemp[0] == Skill.SUPER_KAME || skillTemp[0] == Skill.LIEN_HOAN_CHUONG
                        || skillTemp[0] == Skill.MA_PHONG_BA) {
                    skill = new Skill();
                    skill.template = new system.Template.SkillTemplate();
                    skill.template.id = (byte) skillTemp[0];
                    skill.template.type = 4;
                    skill.point = skillTemp[1];
                    skill.damage = 150;
                } else {
                    continue;
                }
            }

            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }

            this.playerSkill.skills.add(skill);
        }
    }

    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
    }

    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        return this.data[this.currentLevel].getOutfit()[2];

    }

    @Override
    public short getFlagBag() {
        return this.data[this.currentLevel].getOutfit()[3];
    }

    @Override
    public byte getAura() {
        return (byte) this.data[this.currentLevel].getOutfit()[4];
    }

    @Override
    public byte getEffFront() {
        return (byte) this.data[this.currentLevel].getOutfit()[5];
    }


    public Zone getMapJoin() {
        if (this.currentLevel < 0 || this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        int[] mapJoinArray = this.data[this.currentLevel].getMapJoin();
        if (mapJoinArray == null || mapJoinArray.length == 0) {
            return null;
        }
        int mapId = mapJoinArray[Util.nextInt(0, mapJoinArray.length - 1)];
        if (this.isZone01SpawnDisabled) {
            Map mapObj = MapService.gI().getMapById(mapId);
            if (mapObj != null && mapObj.zones != null && !mapObj.zones.isEmpty()) {
                List<Zone> validZones = new ArrayList<>();
                for (Zone z : mapObj.zones) {
                    if (z != null && z.zoneId != 0 && z.zoneId != 1) {
                        validZones.add(z);
                    }
                }
                if (!validZones.isEmpty()) {
                    return validZones.get(Util.nextInt(0, validZones.size() - 1));
                }
            }
            return null;
        }
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        return map;
    }

    @Override
    public void changeStatus(BossStatus status) {
        this.bossStatus = status;
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }
        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone))) {
            this.playerTarger = null;
        }
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }
        if (this.playerTarger != null && this.playerTarger.isPet && ((Pet) this.playerTarger).master != null
                && ((Pet) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        return this.playerTarger;
    }

    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        if (prepareBom) {
            return;
        }
        super.update();
        this.nPoint.mp = this.nPoint.mpg;
        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill()
                || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
            return;
        }
        switch (this.bossStatus) {
            case CHAT_S, AFK, ACTIVE ->
                this.autoLeaveMap();
        }
        switch (this.bossStatus) {
            case REST ->
                this.rest();
            case RESPAWN -> {
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
            }
            case JOIN_MAP ->
                this.joinMap();
            case CHAT_S -> {
                if (chatS()) {
                    this.doneChatS();
                    this.lastTimeChatM = System.currentTimeMillis();
                    this.timeChatM = 5000;
                    if (this.bossStatus != BossStatus.AFK) {
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                }
            }
            case AFK ->
                this.afk();
            case ACTIVE -> {
                this.chatM();
                if (this.effectSkill.isCharging && !Util.isTrue(1, 20) || this.effectSkill.useTroi) {
                    return;
                }
                this.active();
            }
            case DIE ->
                this.changeStatus(BossStatus.CHAT_E);
            case CHAT_E -> {
                if (chatE()) {
                    this.doneChatE();
                    this.changeStatus(BossStatus.LEAVE_MAP);
                }
            }
            case LEAVE_MAP ->
                this.leaveMap();
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {

    }

    @Override
    public void respawn() {
        this.currentLevel++;
        if (this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        this.initBase();
        this.changeToTypeNonPK();
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.wakeupAnotherBossWhenAppear();
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            try {
                if (this.currentLevel == 0) {
                    if (this.parentBoss == null) {
                        int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100)
                                : Util.nextInt(100);
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    } else {
                        int x = this.parentBoss.location.x - (this.lv + 1) * 30;
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    }
                    this.wakeupAnotherBossWhenAppear();
                } else {
                    ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
                }
                this.notifyJoinMap();

                Service.gI().sendFlagBag(this);
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
                if (error < 5) {
                    Logger.error("Lỗi : " + e + "\n");
                    error++;
                }
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    public void joinMapByZone(Zone zone) {
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

    private boolean canSendNotify() {
        return !(this.isNotifyDisabled || this.zone.map.mapId == 140
                || this.zone.map.mapId == 111
                || MapService.gI().isMapPhoBan(this.zone.map.mapId)
                || MapService.gI().isMapMaBu(this.zone.map.mapId)
                || MapService.gI().isMapBlackBallWar(this.zone.map.mapId));
    }

    @Override
    public boolean chatS() {
        if (!Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            return false;
        }

        try {
            String[] texts = this.data[this.currentLevel].getTextS();
            if (texts == null || texts.length == 0) {
                return true;
            }

            if (this.indexChatS >= texts.length) {
                return true;
            }

            String textChat = texts[this.indexChatS];
            if (textChat == null || textChat.isEmpty()) {
                this.indexChatS++;
                return false;
            }

            int first = textChat.indexOf('|');
            int last = textChat.lastIndexOf('|');

            int prefix = 0;
            String msg = textChat;

            // kiểm tra hợp lệ
            if (first == 0 && last > first) {
                // Ví dụ: |2|Hello
                String numStr = textChat.substring(1, last);
                try {
                    prefix = Integer.parseInt(numStr.trim());
                } catch (NumberFormatException e) {
                    prefix = 0; // fallback
                }
                msg = textChat.substring(last + 1).trim();
            }

            // gửi chat
            if (!this.chat(prefix, msg)) {
                return false;
            }

            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = Math.min(msg.length() * 100, 2000);
            this.indexChatS++;

        } catch (Exception e) {
            System.err.println("Error in Boss.chatS(" + this.name + "): " + e.getMessage());
            e.printStackTrace();
            this.indexChatS++; // bỏ qua dòng lỗi để không loop
        }

        return false;
    }

    @Override
    public void doneChatS() {

    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK) {
            return;
        }
        String[] textArray = this.data[this.currentLevel].getTextM();
        if (textArray == null || textArray.length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }

        try {
            String textChat = textArray[Util.nextInt(0, textArray.length - 1)];
            if (textChat == null || textChat.isEmpty()) {
                return;
            }

            int first = textChat.indexOf('|');
            int last = textChat.lastIndexOf('|');

            int prefix = 0;
            String msg = textChat;

            if (first == 0 && last > first) {
                String numStr = textChat.substring(1, last).trim();
                try {
                    prefix = Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    prefix = 0; // fallback nếu không phải số
                }
                msg = textChat.substring(last + 1).trim();
            }

            this.chat(prefix, msg);

            this.lastTimeChatM = System.currentTimeMillis();
            this.timeChatM = Util.nextInt(3000, 20000);

        } catch (Exception e) {
            System.err.println("[BossChatM-Error] " + this.name + " text=" + e.getMessage());
            e.printStackTrace();
            this.lastTimeChatM = System.currentTimeMillis();
            this.timeChatM = 3000;
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    protected long lastTimeAttack;

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 1000) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }
            if (this.playerSkill == null || this.playerSkill.skills == null
                    || this.playerSkill.skills.isEmpty()) {
                return;
            }
            this.playerSkill.skillSelect = this.playerSkill.skills
                    .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.isTrue(5, 20)) {
                    if (SkillUtil.isUseSkillChuong(this)) {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                    } else {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                    }
                }
                SkillService.gI().useSkill(this, pl, null, -1, null);
                checkPlayerDie(pl);
            } else {
                if (Util.isTrue(1, 2)) {
                    this.moveToPlayer(pl);
                }
            }
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player.isDie()) {

        }
    }

    protected int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK
                || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 500;
    }

    @Override
    public void die(Player plKill) {
        plKill = resolveKiller(plKill);
        if (plKill != null) {
            executeReward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    /** Runs legacy reward side effects inside the configurable boss-drop context. */
    protected final void executeReward(Player plKill) {
        plKill = resolveKiller(plKill);
        Player rewardOwner = plKill;
        boss.drop.BossDropService.gI().executeReward(this, rewardOwner, () -> reward(rewardOwner));
    }

    /** Credits a disciple's boss kill and rewards to its master. */
    protected final Player resolveKiller(Player killer) {
        if (killer instanceof Pet) {
            Player master = ((Pet) killer).master;
            if (master != null) {
                return master;
            }
        }
        return killer;
    }

    @Override
    public boolean chatE() {
        if (!Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            return false;
        }
        try {
            String[] texts = this.data[this.currentLevel].getTextE();
            if (texts == null || texts.length == 0) {
                return true;
            }

            if (this.indexChatE >= texts.length) {
                return true;
            }

            String textChat = texts[this.indexChatE];
            if (textChat == null || textChat.isEmpty()) {
                this.indexChatE++;
                return false;
            }

            int first = textChat.indexOf('|');
            int last = textChat.lastIndexOf('|');

            int prefix = 0;
            String msg = textChat;

            if (first == 0 && last > first) {
                String numStr = textChat.substring(1, last).trim();
                try {
                    prefix = Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    prefix = 0;
                }
                msg = textChat.substring(last + 1).trim();
            }

            if (!this.chat(prefix, msg)) {
                return false;
            }

            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = Math.min(msg.length() * 100, 2000);
            this.indexChatE++;

        } catch (Exception e) {
            System.err.println("[BossChatE-Error] " + this.name + ": " + e.getMessage());
            e.printStackTrace();
            this.indexChatE++;
            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = 1000;
        }

        return false;
    }

    @Override
    public void doneChatE() {
        this.leaveMap();
    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
        if (restByTimeout) {
            this.lastTimeRest = System.currentTimeMillis() - (this.secondsRest - 3) * 1000;
            restByTimeout = false;
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            // 1. Tính toán các hiệu ứng tăng/giảm dame trước
            if (plAtt != null && plAtt.nPoint.tlDameToBoss > 0) {
                double damboss = (long) (damage * plAtt.nPoint.tlDameToBoss / 100L);
                damage += damboss;
            }

            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
        
            this.nPoint.subHP(damage);
            Player creditedAttacker = resolveKiller(plAtt);
            if (creditedAttacker != null && !creditedAttacker.isBoss && damage > 0) {
                int playerId = (int) creditedAttacker.id;
                long actualDamage = (long) damage; 
                
                this.statisticsDamage.put(playerId, this.statisticsDamage.getOrDefault(playerId, 0L) + actualDamage);
            }

            if (isDie()) {
                dragonpass.DragonPassService.gI().onBossKilled(creditedAttacker);
                this.setDie(creditedAttacker);
                die(creditedAttacker);
            }
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        if (prefix == -1) {
            this.chat(textChat);
        } else if (prefix == -2) {
            if (this.zone != null) {
                Player plMap = this.zone.getRandomPlayerInMap();
                if (plMap != null && !plMap.isDie() && Util.getDistance(this, plMap) <= 600) {
                    Service.gI().chat(plMap, textChat);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (prefix == -3) {
            if (this.parentBoss != null && !this.parentBoss.isDie()) {
                this.parentBoss.chat(textChat);
            }
        } else if (prefix >= 0) {
            if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                Boss boss = this.bossAppearTogether[this.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            } else if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                Boss boss = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            }
        }
        return true;
    }

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss != null) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                boss.changeStatus(BossStatus.RESPAWN);
                boss.respawn();
                boss.joinMap();
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {
    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        try {
            if (!prepareBom) {
                prepareBom = true;
                this.nPoint.hp = 1;
                long lastTime = System.currentTimeMillis();
                Message msg;
                try {
                    msg = new Message(-45);
                    msg.writer().writeByte(7);
                    msg.writer().writeInt((int) Boss.this.id);
                    msg.writer().writeShort(104);
                    msg.writer().writeShort(2000);
                    Service.gI().sendMessAllPlayerInMap(Boss.this, msg);
                    msg.cleanup();
                } catch (IOException e) {
                }
                while (prepareBom) {
                    if (Util.canDoWithTime(lastTime, 2500)) {
                        setDie(this);
                        die(resolveKiller(plAtt));
                        long dame = Boss.this.nPoint.hpMax;
                        for (Mob mob : Boss.this.zone.mobs) {
                            mob.injured(Boss.this, dame, true);
                        }
                        List<Player> playersMap = Boss.this.zone.getNotBosses();
                        if (!MapService.gI().isMapOffline(Boss.this.zone.map.mapId)) {
                            for (int i = playersMap.size() - 1; i >= 0; i--) {
                                Player pl = playersMap.get(i);
                                if (!Boss.this.equals(pl)) {
                                    pl.injured(Boss.this, dame, false, false);
                                    PlayerService.gI().sendInfoHpMpMoney(pl);
                                    Service.gI().Send_Info_NV(pl);
                                }
                            }
                        }
                        prepareBom = false;
                    }
                }
            }
        } catch (Exception e) {
            if (prepareBom) {
                prepareBom = false;
            }
            setDie(this);
            die(resolveKiller(plAtt));
        }
    }

}
