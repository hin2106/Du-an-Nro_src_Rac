package player;

import consts.ConstPlayer;
import data.DataGame;
import item.Item;
import lombok.Getter;
import services.map.MapService;
import mob.Mob;
import skill.Skill;
import utils.SkillUtil;
import services.Service;
import utils.Util;
import network.Message;
import services.ItemTimeService;
import services.player.PlayerService;
import services.SkillService;
import services.EffectSkillService;
import services.map.ChangeMapService;
import utils.TimeUtil;

import java.util.List;

import services.func.UseItem;
import utils.Logger;

public class Pet extends Player {

    private static final short ARANGE_CAN_ATTACK = 300;
    private static final short ARANGE_ATT_SKILL1 = 50;

    private static final short[][] PET_ID = {{285, 286, 287}, {288, 289, 290}, {282, 283, 284},
    {304, 305, 303}};

    public static final byte FOLLOW = 0;
    public static final byte PROTECT = 1;
    public static final byte ATTACK = 2;
    public static final byte GOHOME = 3;
    public static final byte FUSION = 4;
    public static final byte HTVV = 5;

    public Player master;
    @Getter
    public byte status = 0;

    public byte typePet;
    public boolean isTransform;

    public long lastTimeDie;

    private boolean goingHome;

    private Mob mobAttack;
    private Player playerAttack;

    private static final int TIME_WAIT_AFTER_UNFUSION = 5000;
    private long lastTimeUnfusion;

    private int indexChat = 0;
    private long lastTimeChat;

    public Pet(Player master) {
        this.master = master;
        this.isPet = true;
    }

    public void changeStatus(byte status) {
        if (goingHome || master.fusion.typeFusion != 0 || (this.isDie() && status == FUSION)) {
            Service.gI().sendThongBao(master, "Không thể thực hiện");
            return;
        }
        Service.gI().chatJustForMe(master, this, getTextStatus(status));
        if (status == GOHOME) {
            goHome();
        } else if (status == FUSION) {
            fusion(false);
        }
        this.status = status;
    }

    public void joinMapMaster() {
        if (status != GOHOME && status != FUSION && !isDie()) {
            this.location.x = master.location.x + Util.nextInt(-10, 10);
            this.location.y = master.location.y;
            if (MapService.gI().isMapOffline(this.master.zone.map.mapId) || this.master.zone.map.mapId == 113) {
                ChangeMapService.gI().goToMap(this, MapService.gI().getMapCanJoin(this, master.gender + 21, -1));
                return;
            }
            ChangeMapService.gI().goToMap(this, master.zone);
            this.zone.load_Me_To_Another(this);
        }
    }

    public void goHome() {
        if (this.status == GOHOME) {
            return;
        }
        if (this.effectSkill.tiLeHPHuytSao != 0) {
            EffectSkillService.gI().removeHuytSao(this);
        }
        goingHome = true;
        new Thread(() -> {
            try {
                Pet.this.status = Pet.ATTACK;
                Thread.sleep(2000);
            } catch (Exception e) {
            }
            if (master != null) {
                try {
                    ChangeMapService.gI().goToMap(this, MapService.gI().getMapCanJoin(this, master.gender + 21, -1));
                } catch (Exception e) {
                }
                this.zone.load_Me_To_Another(this);
                Pet.this.status = Pet.GOHOME;
                goingHome = false;
            }
        }).start();
    }

    private String getTextStatus(byte status) {
        if (this.typePet == 4) {
            switch (status) {
                case FOLLOW:
                    return "Lũ con người không đủ tư cách để nói chuyện với ta";
                case PROTECT:
                    return "Ta sẽ cho người biết sức mạnh của một vị thần là như thế nào !";
                case ATTACK:
                    return "Ta sẽ thống trị vũ trụ";
                case GOHOME:
                    return "Không lí nào ta lại run sợ bọn con người sao";
                case HTVV:
                    return "Lũ các ngươi làm ta thấy đau rồi ấy haha";
                default:
                    return "Sức mạnh của ta là không có giới hạn";
            }
        }
        switch (status) {
            case FOLLOW:
                return "Ok con theo sư phụ";
            case PROTECT:
                return "Ok con sẽ bảo vệ sư phụ";
            case ATTACK:
                return "Ok sư phụ để con lo cho";
            case GOHOME:
                return "OK con về, bibi sư phụ";
            case HTVV:
                return "Dm sư phụ";
            default:
                return "Sư phụ ơi con lên cấp rồi";
        }
    }

    public void fusion3(boolean porata) {
        if (this.isDie()) {
            Service.gI().sendThongBao(master, "Yêu cầu phải có đệ tử và đệ tử còn sống");
            return;
        }
        if (Util.canDoWithTime(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION)) {
            if (this.effectSkill.tiLeHPHuytSao != 0) {
                EffectSkillService.gI().removeHuytSao(this);
            }
            if (porata) {
                master.fusion.typeFusion = ConstPlayer.HOP_THE_PORATA3;
            }
            this.status = FUSION;
            ChangeMapService.gI().exitMap(this);
            fusionEffect(master.fusion.typeFusion);
            master.nPoint.calPoint();
            master.nPoint.setFullHpMp();
            Service.gI().point(master);
            Service.gI().Send_Caitrang(master);
        } else {
            Service.gI().sendThongBao(this.master, "Vui lòng đợi "
                    + TimeUtil.getTimeLeft(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION / 1000) + " nữa");
        }
    }

    public void fusion2(boolean porata) {
        if (this.isDie()) {
            Service.gI().sendThongBao(master, "Yêu cầu phải có đệ tử và đệ tử còn sống");
            return;
        }
        if (Util.canDoWithTime(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION)) {
            if (this.effectSkill.tiLeHPHuytSao != 0) {
                EffectSkillService.gI().removeHuytSao(this);
            }
            if (porata) {
                master.fusion.typeFusion = ConstPlayer.HOP_THE_PORATA2;
            }
            this.status = FUSION;
            ChangeMapService.gI().exitMap(this);
            fusionEffect(master.fusion.typeFusion);
            master.nPoint.calPoint();
            master.nPoint.setFullHpMp();
            Service.gI().point(master);
            Service.gI().Send_Caitrang(master);
        } else {
            Service.gI().sendThongBao(this.master, "Vui lòng đợi "
                    + TimeUtil.getTimeLeft(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION / 1000) + " nữa");
        }
    }

    public void fusion(boolean porata) {
        if (this.isDie()) {
            Service.gI().sendThongBao(master, "Yêu cầu phải có đệ tử và đệ tử còn sống");
            return;
        }
        if (Util.canDoWithTime(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION)) {
            // Remove huyet_sao effect before fusion
            if (this.effectSkill.tiLeHPHuytSao != 0) {
                EffectSkillService.gI().removeHuytSao(this);
            }
            if (porata) {
                master.fusion.typeFusion = ConstPlayer.HOP_THE_PORATA;
            } else {
                master.fusion.lastTimeFusion = System.currentTimeMillis();
                master.fusion.typeFusion = ConstPlayer.LUONG_LONG_NHAT_THE;
                ItemTimeService.gI().sendItemTime(master, master.gender == ConstPlayer.NAMEC ? 3901 : 3790,
                        Fusion.TIME_FUSION / 1000);
            }
            this.status = FUSION;
            ChangeMapService.gI().exitMap(this);
            fusionEffect(master.fusion.typeFusion);
            master.nPoint.calPoint(); // must recalculate isGogeta BEFORE Send_Caitrang
            master.nPoint.setFullHpMp();
            Service.gI().point(master);
            Service.gI().Send_Caitrang(master);
        } else {
            Service.gI().sendThongBao(this.master, "Vui lòng đợi "
                    + TimeUtil.getTimeLeft(lastTimeUnfusion, TIME_WAIT_AFTER_UNFUSION / 1000) + " nữa");
        }
    }

    public void unFusion() {
        master.fusion.typeFusion = 0;
        this.status = PROTECT;
        Service.gI().point(master);
        joinMapMaster();
        fusionEffect(master.fusion.typeFusion);
        Service.gI().Send_Caitrang(master);
        Service.gI().point(master);
        this.lastTimeUnfusion = System.currentTimeMillis();
    }

    private void fusionEffect(int type) {
        Message msg;
        try {
            msg = new Message(125);
            msg.writer().writeByte(type);
            msg.writer().writeInt((int) master.id);
            msg.writer().writeShort(master.getHead());
            msg.writer().writeShort(master.getBody());
            msg.writer().writeShort(master.getLeg());
            Service.gI().sendMessAllPlayerInMap(master, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;

    private void moveIdle() {
        if (status == GOHOME || status == FUSION || status == HTVV) {
            return;
        }
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + (dir == -1 ? 50 : -50), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
            idle = false;
        }
        // Util.nextInt(dir == -1 ? 50 : -50, dir == -1 ? 50 : 50)
    }

    private void masterDoesNotAttack() {
        if (Util.canDoWithTime(master.lastTimePlayerNotAttack, master.timeNotAttack)) {
            if (!MapService.gI().isMapOffline(master.zone.map.mapId)) {
                master.doesNotAttack = true;
            }
            master.lastTimePlayerNotAttack = System.currentTimeMillis();
            master.timeNotAttack = Util.nextInt(1800000, 3600000); // random 30p - 1h
        }
    }

    private long lastTimeMoveAtHome;
    private byte directAtHome = -1;

    /**
     * TODO: Code nhìn như cc rảnh sẽ fix lại
     */
    @Override
    public void update() {
        try {
            if (this.master != null && this.master.zone != null) {
                super.update();
                increasePoint(); // cộng chỉ số
                updatePower(); // check mở skill...
                if (isDie()) {
                    if (System.currentTimeMillis() - lastTimeDie > 50000) {
                        Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
                    } else {
                        return;
                    }
                }

                if (this.newSkill != null && this.newSkill.isStartSkillSpecial) {
                    return;
                }

                if (justRevived && this.zone == master.zone) {
                    Service.gI().chatJustForMe(master, this, "Sư phụ ơi con đây nè");
                    justRevived = false;
                }

                if (this.zone == null || this.zone != master.zone) {
                    joinMapMaster();
                }
                if (master.isDie() || this.isDie() || effectSkill.isHaveEffectSkill()) {
                    return;
                }
                masterDoesNotAttack();
                moveIdle();
                switch (status) {
                    case FOLLOW ->
                        followMaster(60);
                    case PROTECT -> {
                        if (useSkill3() || useSkill4() || useSkill5()) {
                            break;
                        }
                        playerAttack = findPlayerAttack();
                        if (playerAttack != null) {
                            if ((this.typePet == 9) && Util.isTrue(1, 5) && playerAttack.nPoint.hp < 20_000_000
                                    && !playerAttack.nPoint.islinhthuydanhbac && !playerAttack.isBoss) {
                                playerAttack.nPoint.subHP(20_000_000);
                                Service.gI().chat(this, "HAKAI " + playerAttack.name + "!");
                                Service.gI().sendThongBao(playerAttack, "Bạn đã bị Hakai!");
                            } else {
                                petSay(playerAttack);
                            }
                            int disToPlayer = Util.getDistance(this, playerAttack);
                            if (disToPlayer <= ARANGE_ATT_SKILL1) {
                                // đấm
                                this.playerSkill.skillSelect = getSkill(1);
                                if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                    if (SkillService.gI().canUseSkillWithMana(this)) {
                                        PlayerService.gI().playerMove(this,
                                                playerAttack.location.x + Util.nextInt(-60, 60),
                                                playerAttack.location.y);
                                        SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                    } else {
                                        askPea();
                                    }
                                }
                            } else {
                                // chưởng
                                this.playerSkill.skillSelect = getSkill(2);
                                if (this.playerSkill.skillSelect.skillId != -1) {
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                } else {
                                    this.playerSkill.skillSelect = getSkill(1);
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            PlayerService.gI().playerMove(this,
                                                    playerAttack.location.x + Util.nextInt(-60, 60),
                                                    playerAttack.location.y);
                                            SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                }
                            }
                            return;
                        }

                        mobAttack = findMobAttack();
                        if (mobAttack != null) {
                            int disToMob = Util.getDistance(this, mobAttack);
                            if (disToMob <= ARANGE_ATT_SKILL1) {
                                // đấm
                                this.playerSkill.skillSelect = getSkill(1);
                                if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                    if (SkillService.gI().canUseSkillWithMana(this)) {
                                        PlayerService.gI().playerMove(this,
                                                mobAttack.location.x + Util.nextInt(-60, 60), mobAttack.location.y);
                                        SkillService.gI().useSkill(this, null, mobAttack, -1, null);
                                    } else {
                                        askPea();
                                    }
                                }
                            } else {
                                // chưởng
                                this.playerSkill.skillSelect = getSkill(2);
                                if (this.playerSkill.skillSelect.skillId != -1) {
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            SkillService.gI().useSkill(this, null, mobAttack, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                } else {
                                    this.playerSkill.skillSelect = getSkill(1);
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            PlayerService.gI().playerMove(this,
                                                    mobAttack.location.x + Util.nextInt(-60, 60), mobAttack.location.y);
                                            SkillService.gI().useSkill(this, null, mobAttack, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                }
                            }

                        } else {
                            idle = true;
                        }
                    }
                    case ATTACK -> {
                        if (useSkill3() || useSkill4() || useSkill5()) {
                            break;
                        }
                        playerAttack = findPlayerAttack();
                        if (playerAttack != null) {
                            if ((this.typePet == 9) && Util.isTrue(1, 5) && playerAttack.nPoint.hp < 20_000_000
                                    && !playerAttack.nPoint.islinhthuydanhbac && !playerAttack.isBoss) {
                                // playerAttack.setDie(this);
                                playerAttack.nPoint.subHP(20_000_000);
                                Service.gI().chat(this, "HAKAI " + playerAttack.name + "!");
                                Service.gI().sendThongBao(playerAttack, "Bạn đã bị Hakai!");
                            } else {
                                petSay(playerAttack);
                            }
                            int disToPlayer = Util.getDistance(this, playerAttack);
                            if (disToPlayer <= ARANGE_ATT_SKILL1) {
                                // đấm
                                this.playerSkill.skillSelect = getSkill(1);
                                if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                    if (SkillService.gI().canUseSkillWithMana(this)) {
                                        PlayerService.gI().playerMove(this,
                                                playerAttack.location.x + Util.nextInt(-60, 60),
                                                playerAttack.location.y);
                                        SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                    } else {
                                        askPea();
                                    }
                                }
                            } else {
                                // chưởng
                                this.playerSkill.skillSelect = getSkill(2);
                                if (this.playerSkill.skillSelect.skillId != -1) {
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                } else {
                                    this.playerSkill.skillSelect = getSkill(1);
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            PlayerService.gI().playerMove(this,
                                                    playerAttack.location.x + Util.nextInt(-60, 60),
                                                    playerAttack.location.y);
                                            SkillService.gI().useSkill(this, playerAttack, null, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                }
                            }
                            return;
                        }
                        mobAttack = findMobAttack();
                        if (mobAttack != null) {
                            int disToMob = Util.getDistance(this, mobAttack);
                            if (disToMob <= ARANGE_ATT_SKILL1) {
                                this.playerSkill.skillSelect = getSkill(1);
                                if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                    if (SkillService.gI().canUseSkillWithMana(this)) {
                                        PlayerService.gI().playerMove(this,
                                                mobAttack.location.x + Util.nextInt(-20, 20), mobAttack.location.y);
                                        SkillService.gI().useSkill(this, playerAttack, mobAttack, -1, null);
                                    } else {
                                        askPea();
                                    }
                                }
                            } else {
                                this.playerSkill.skillSelect = getSkill(2);
                                if (this.playerSkill.skillSelect.skillId != -1) {
                                    if (SkillService.gI().canUseSkillWithMana(this)) {
                                        PlayerService.gI().playerMove(this,
                                                mobAttack.location.x + Util.nextInt(-20, 20), mobAttack.location.y);
                                        SkillService.gI().useSkill(this, playerAttack, mobAttack, -1, null);
                                    }
                                } else {
                                    this.playerSkill.skillSelect = getSkill(1);
                                    if (SkillService.gI().canUseSkillWithCooldown(this) && canAttack()) {
                                        if (SkillService.gI().canUseSkillWithMana(this)) {
                                            PlayerService.gI().playerMove(this,
                                                    mobAttack.location.x + Util.nextInt(-20, 20), mobAttack.location.y);
                                            SkillService.gI().useSkill(this, playerAttack, mobAttack, -1, null);
                                        } else {
                                            askPea();
                                        }
                                    }
                                }
                            }

                        } else {
                            idle = true;
                        }
                    }

                    case GOHOME -> {
                        if (this.zone != null && (this.zone.map.mapId == 21 || this.zone.map.mapId == 22
                                || this.zone.map.mapId == 23)) {
                            if (System.currentTimeMillis() - lastTimeMoveAtHome <= 5000) {
                                return;
                            } else {
                                if (this.zone.map.mapId == 21) {
                                    if (directAtHome == -1) {

                                        PlayerService.gI().playerMove(this, 250, 336);
                                        directAtHome = 1;
                                    } else {
                                        PlayerService.gI().playerMove(this, 200, 336);
                                        directAtHome = -1;
                                    }
                                } else if (this.zone.map.mapId == 22) {
                                    if (directAtHome == -1) {
                                        PlayerService.gI().playerMove(this, 500, 336);
                                        directAtHome = 1;
                                    } else {
                                        PlayerService.gI().playerMove(this, 452, 336);
                                        directAtHome = -1;
                                    }
                                } else if (this.zone.map.mapId == 22) {
                                    if (directAtHome == -1) {
                                        PlayerService.gI().playerMove(this, 250, 336);
                                        directAtHome = 1;
                                    } else {
                                        PlayerService.gI().playerMove(this, 200, 336);
                                        directAtHome = -1;
                                    }
                                }
                                Service.gI().chatJustForMe(master, this, "Là do bạn không chơi đồ đấy bạn ạ!");
                                lastTimeMoveAtHome = System.currentTimeMillis();
                            }
                        }
                    }
                    case HTVV -> {
                        if (master.gender == 1) {
                            fusionEffect(ConstPlayer.LUONG_LONG_NHAT_THE);
                            ChangeMapService.gI().exitMap(this);
                            Service.gI().addSMTN(master, (byte) 1, this.nPoint.power, true);
                            master.pet = null;
                            Service.gI().sendHavePet(master);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long lastTimeAskPea;

    public void askPea() {
        if (Util.canDoWithTime(lastTimeAskPea, 10000)) {
            if (this.master.isPet) {
                if (!this.isDie()) {
                    int statima = 100 * 10;
                    int hpKiHoiPhuc = 100000;
                    this.nPoint.stamina += statima;
                    if (this.nPoint.stamina > this.nPoint.maxStamina) {
                        this.nPoint.stamina = this.nPoint.maxStamina;
                    }
                    this.nPoint.setHp(this.nPoint.hp + hpKiHoiPhuc);
                    this.nPoint.setMp(this.nPoint.mp + hpKiHoiPhuc);
                    Service.gI().sendInfoPlayerEatPea(this);
                }
                lastTimeAskPea = System.currentTimeMillis();
                return;
            }
            Service.gI().chatJustForMe(master, this,
                    this.typePet == 4 ? "Đưa ta đậu, nếu không ta sẽ hủy diệt thế giới này!"
                            : "Sư phụ ơi cho con đậu thần");
            UseItem.gI().eatPea(master);
            lastTimeAskPea = System.currentTimeMillis();
        }
    }

    private int countTTNL;

    private boolean useSkill3() {
        try {
            playerSkill.skillSelect = getSkill(3);
            if (playerSkill.skillSelect.skillId == -1) {
                return false;
            }
            switch (this.playerSkill.skillSelect.template.id) {
                case Skill.THAI_DUONG_HA_SAN:
                    if (SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        Service.gI().chatJustForMe(master, this, "Bất ngờ chưa ông già");
                        return true;
                    }
                    return false;
                case Skill.TAI_TAO_NANG_LUONG:
                    if (this.effectSkill.isCharging && this.countTTNL < Util.nextInt(3, 5)) {
                        this.countTTNL++;
                        return true;
                    }
                    if (SkillService.gI().canUseSkillWithCooldown(this) && SkillService.gI().canUseSkillWithMana(this)
                            && (this.nPoint.getCurrPercentHP() <= 20 || this.nPoint.getCurrPercentMP() <= 20)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        this.countTTNL = 0;
                        return true;
                    }
                    return false;
                case Skill.KAIOKEN:
                    if (SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {

                        mobAttack = this.findMobAttack();
                        playerAttack = this.findPlayerAttack();
                        if (playerAttack != null) {
                            mobAttack = null;
                            int dis = Util.getDistance(this, playerAttack);
                            if (dis > ARANGE_ATT_SKILL1) {
                                PlayerService.gI().playerMove(this, playerAttack.location.x, playerAttack.location.y);
                            } else {
                                if (SkillService.gI().canUseSkillWithCooldown(this)
                                        && SkillService.gI().canUseSkillWithMana(this)) {
                                    PlayerService.gI().playerMove(this, playerAttack.location.x + Util.nextInt(-20, 20),
                                            playerAttack.location.y);
                                }
                            }
                        } else if (mobAttack == null) {
                            return false;
                        }
                        if (mobAttack != null) {
                            int dis = Util.getDistance(this, mobAttack);
                            if (dis > ARANGE_ATT_SKILL1) {
                                PlayerService.gI().playerMove(this, mobAttack.location.x, mobAttack.location.y);
                            } else {
                                if (SkillService.gI().canUseSkillWithCooldown(this)
                                        && SkillService.gI().canUseSkillWithMana(this)) {
                                    PlayerService.gI().playerMove(this, mobAttack.location.x + Util.nextInt(-20, 20),
                                            mobAttack.location.y);
                                }
                            }
                        }

                        SkillService.gI().useSkill(this, playerAttack, mobAttack, -1, null);
                        getSkill(1).lastTimeUseThisSkill = System.currentTimeMillis();
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean useSkill4() {
        try {
            this.playerSkill.skillSelect = getSkill(4);
            if (this.playerSkill.skillSelect.skillId == -1) {
                return false;
            }
            switch (this.playerSkill.skillSelect.template.id) {
                case Skill.BIEN_KHI:
                    if (!this.effectSkill.isMonkey && SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        return true;
                    }
                    return false;
                case Skill.KHIEN_NANG_LUONG:
                    if (!this.effectSkill.isShielding && SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        return true;
                    }
                    return false;
                case Skill.DE_TRUNG:
                    if (this.mobMe == null && SkillService.gI().canUseSkillWithCooldown(this)
                            && SkillService.gI().canUseSkillWithMana(this)) {
                        SkillService.gI().useSkill(this, null, null, -1, null);
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean useSkill5() {
        try {
            Skill skill = this.playerSkill.skillSelect = getSkill(5);
            if (skill == null || skill.skillId == -1) {
                return false;
            }
            this.playerSkill.skillSelect = skill;
            boolean canUse = SkillService.gI().canUseSkillWithCooldown(this)
                    && SkillService.gI().canUseSkillWithMana(this);
            if (!canUse || this.newSkill == null) {
                return false;
            }
            int skillId = skill.template.id;
            switch (skillId) {
                case Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA -> {
                    short dx = (short) this.location.x;
                    short dy = (short) this.location.y;
                    short x = dx;
                    short y = dy;
                    byte dir = 1;
                    Player target = findPlayerAttack();
                    Mob mobTarget = findMobAttack();

                    if (target != null) {
                        x = (short) target.location.x;
                        y = (short) target.location.y;
                    } else if (mobTarget != null) {
                        x = (short) mobTarget.location.x;
                        y = (short) mobTarget.location.y;
                    } else {
                        return false;
                    }

                    dir = (byte) (dx > x ? -1 : 1);

                    this.newSkill.setSkillSpecial(dir, dx, dy, x, y);

                    SkillService.gI().newSkillNotFocus(this, 20);
                    SkillService.gI().affterUseSkill(this, skillId);

                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private long lastTimeIncreasePoint;

    private void increasePoint() {
        if (this.nPoint == null || this.master == null) {
            return;
        }
        if (this.nPoint.tiemNang <= 0) {
            return;
        }
        if (!Util.canDoWithTime(lastTimeIncreasePoint, 0)) {
            return;
        }

        int maxAttempts = 20;
        int attempts = 0;
        int defAddedInCycle = 0;

        while (attempts < maxAttempts && this.nPoint != null && this.nPoint.tiemNang > 0) {
            if (this.nPoint == null) {
                break;
            }

            int dameLimit;
            int hpMpLimit;
            short defLimit;
            try {
                dameLimit = this.nPoint.getDameLimit();
                hpMpLimit = this.nPoint.getHpMpLimit();
                defLimit = this.nPoint.getDefLimit();
            } catch (Exception e) {
                break; // Nếu lỗi khi lấy giới hạn, dừng
            }
            boolean dameFull = (this.nPoint.dameg >= dameLimit);
            boolean hpFull = (this.nPoint.hpg >= hpMpLimit);
            boolean mpFull = (this.nPoint.mpg >= hpMpLimit);
            boolean defFull = (this.nPoint.defg >= defLimit);
            boolean allMainFull = dameFull && hpFull && mpFull;

            // Nếu tất cả đều full, thoát
            if (allMainFull && defFull) {
                break;
            }

            byte pointType = -1;
            short pointAmount = 1;

            // Xác định chỉ số cần cộng dựa trên ưu tiên
            if (allMainFull && !defFull && defAddedInCycle == 0) {
                // Chỉ cộng Def 1 lần mỗi chu kỳ khi các chỉ số khác đã full
                pointType = 3; // Def
                defAddedInCycle = 1;
            } else if (!allMainFull) {
                // Ưu tiên: Dame > HP > MP
                if (!dameFull) {
                    pointType = 2; // Dame - ưu tiên cao nhất
                } else if (!hpFull) {
                    pointType = 0; // HP - ưu tiên thứ hai
                } else if (!mpFull) {
                    pointType = 1; // MP/Ki - ưu tiên thứ ba
                }
            } else {
                // Nếu đã full 3 chỉ số chính, random vào 3 chỉ số chính
                int[] mainStats = {2, 0, 1}; // Dame, HP, MP
                pointType = (byte) mainStats[Util.nextInt(0, mainStats.length)];
            }

            // Nếu không xác định được pointType, thoát
            if (pointType == -1) {
                break;
            }

            // Lưu lại giá trị trước khi thử
            long tiemNangBefore = this.nPoint.tiemNang;
            long damegBefore = this.nPoint.dameg;
            long hpgBefore = this.nPoint.hpg;
            long mpgBefore = this.nPoint.mpg;
            int defgBefore = this.nPoint.defg;

            // Tính toán tiềm năng cần thiết và kiểm tra điều kiện
            long tiemNangNeeded = 0;
            boolean canIncrease = false;

            switch (pointType) {
                case 0: // HP
                    if (!hpFull) {
                        int pointHp = pointAmount * 20;
                        if ((this.nPoint.hpg + pointHp) <= hpMpLimit) {
                            tiemNangNeeded = pointAmount * (2 * (this.nPoint.hpg + 1000) + pointHp - 20) / 2;
                            canIncrease = (this.nPoint.tiemNang >= tiemNangNeeded);
                        }
                    }
                    break;
                case 1: // MP
                    if (!mpFull) {
                        int pointMp = pointAmount * 20;
                        if ((this.nPoint.mpg + pointMp) <= hpMpLimit) {
                            tiemNangNeeded = pointAmount * (2 * (this.nPoint.mpg + 1000) + pointMp - 20) / 2;
                            canIncrease = (this.nPoint.tiemNang >= tiemNangNeeded);
                        }
                    }
                    break;
                case 2: // Dame
                    if (!dameFull) {
                        if ((this.nPoint.dameg + pointAmount) <= dameLimit) {
                            tiemNangNeeded = pointAmount * (2 * this.nPoint.dameg + pointAmount - 1) / 2 * 100;
                            canIncrease = (this.nPoint.tiemNang >= tiemNangNeeded);
                        }
                    }
                    break;
                case 3: // Def
                    if (!defFull) {
                        if ((this.nPoint.defg + pointAmount) <= defLimit) {
                            tiemNangNeeded = 2 * (this.nPoint.defg + 5) / 2 * 100000;
                            canIncrease = (this.nPoint.tiemNang >= tiemNangNeeded);
                        }
                    }
                    break;
            }

            // Thử cộng điểm nếu đủ điều kiện
            if (canIncrease) {
                try {
                    this.nPoint.increasePoint(pointType, pointAmount);

                    // Kiểm tra xem có thực sự cộng được không
                    boolean pointIncreased = false;
                    switch (pointType) {
                        case 0:
                            pointIncreased = (this.nPoint.hpg > hpgBefore);
                            break;
                        case 1:
                            pointIncreased = (this.nPoint.mpg > mpgBefore);
                            break;
                        case 2:
                            pointIncreased = (this.nPoint.dameg > damegBefore);
                            break;
                        case 3:
                            pointIncreased = (this.nPoint.defg > defgBefore);
                            break;
                    }

                    if (pointIncreased && this.nPoint.tiemNang < tiemNangBefore) {
                        // Đã cộng thành công
                        attempts++;
                        continue;
                    }
                } catch (Exception e) {
                    // Nếu có lỗi khi cộng điểm, dừng
                    break;
                }
            }

            // Nếu không cộng được, thử chỉ số tiếp theo theo thứ tự ưu tiên (chỉ thử 1 lần)
            if (!canIncrease || this.nPoint.tiemNang >= tiemNangBefore) {
                boolean triedNext = false;

                if (!dameFull && pointType == 2 && this.nPoint.tiemNang > 0) {
                    // Thử HP nếu Dame không được
                    if (!hpFull) {
                        int pointHp = pointAmount * 20;
                        if ((this.nPoint.hpg + pointHp) <= hpMpLimit) {
                            long tiemNangNeeded2 = pointAmount * (2 * (this.nPoint.hpg + 1000) + pointHp - 20) / 2;
                            if (this.nPoint.tiemNang >= tiemNangNeeded2) {
                                long tiemNangBefore2 = this.nPoint.tiemNang;
                                long hpgBefore2 = this.nPoint.hpg;
                                try {
                                    this.nPoint.increasePoint((byte) 0, pointAmount);
                                    triedNext = (this.nPoint.hpg > hpgBefore2
                                            && this.nPoint.tiemNang < tiemNangBefore2);
                                    if (triedNext) {
                                        attempts++;
                                        continue;
                                    }
                                } catch (Exception e) {
                                    Logger.logException(Pet.class, e);
                                }
                            }
                        }
                    }
                } else if (!hpFull && pointType == 0 && this.nPoint.tiemNang > 0) {
                    if (!mpFull) {
                        int pointMp = pointAmount * 20;
                        if ((this.nPoint.mpg + pointMp) <= hpMpLimit) {
                            long tiemNangNeeded2 = pointAmount * (2 * (this.nPoint.mpg + 1000) + pointMp - 20) / 2;
                            if (this.nPoint.tiemNang >= tiemNangNeeded2) {
                                long tiemNangBefore2 = this.nPoint.tiemNang;
                                long mpgBefore2 = this.nPoint.mpg;
                                try {
                                    this.nPoint.increasePoint((byte) 1, pointAmount);
                                    triedNext = (this.nPoint.mpg > mpgBefore2
                                            && this.nPoint.tiemNang < tiemNangBefore2);
                                    if (triedNext) {
                                        attempts++;
                                        continue;
                                    }
                                } catch (Exception e) {
                                    // Lỗi, tiếp tục
                                }
                            }
                        }
                    }
                }
                if (!triedNext) {
                    break;
                }
            }

            attempts++;
        }

        lastTimeIncreasePoint = System.currentTimeMillis();
    }

    public void followMaster() {
        if (this.isDie() || effectSkill.isHaveEffectSkill()) {
            return;
        }
        switch (this.status) {
            case ATTACK:
                if ((mobAttack != null && Util.getDistance(this, master) <= 1000)) {
                    break;
                }
            case FOLLOW:
            case PROTECT:
                followMaster(500);
                break;
        }
    }

    private void followMaster(int dis) {
        int mX = master.location.x;
        int mY = master.location.y;
        int disX = this.location.x - mX;
        if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= dis || disX < 50) {
            if (disX < 0) {
                this.location.x = mX - 50;
            } else {
                this.location.x = mX + 50;
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
    }

    public short getAvatar() {
        return switch (this.typePet) {
            case 1 ->
                297;
            case 2 ->
                508;
            case 3 ->
                237;
            case 4 ->
                946;
            case 5 ->
                876;
            case 6 ->
                1422;
            default ->
                PET_ID[3][this.gender];
        };
    }

    @Override
    public short getHead() {
        // Kiểm tra effect skill trước
        if (effectSkill != null) {
            if (effectSkill.isBinh) {
                return idOutfitMafuba[effectSkill.typeBinh][0];
            }
            if (effectSkill.isStone) {
                return 454;
            }
            if (effectSkill.isHalloween) {
                return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][0];
            }
            if (effectSkill.isMonkey) {
                int idx = effectSkill.levelMonkey - 1;
                if (idx >= 0 && idx < ConstPlayer.HEADMONKEY.length) {
                    return (short) ConstPlayer.HEADMONKEY[idx];
                }
            }
            if (effectSkill.isSocola) {
                return 412;
            }
        }

        switch (this.typePet) {
            case 1:
                if (!this.isTransform) {
                    return 297;
                }
                break;
            case 2:
                if (!this.isTransform) {
                    return 508;
                }
                break;
            case 3:
                if (!this.isTransform) {
                    return 237;
                }
                break;
            case 4:
                if (!this.isTransform) {
                    return 946;
                }
                break;
            case 5:
                if (!this.isTransform) {
                    return 876;
                }
                break;
            case 6:
                if (!this.isTransform) {
                    return 1422;
                }
                break;
        }

        // Kiểm tra item body tại vị trí 5
        if (inventory != null && inventory.itemsBody != null
                && inventory.itemsBody.size() > 5
                && inventory.itemsBody.get(5).isNotNullItem()) {
            int part = inventory.itemsBody.get(5).template.head;
            if (part != -1) {
                return (short) part;
            }
        }

        // Kiểm tra power level
        if (this.nPoint.power < 1500000) {
            return PET_ID[this.gender][0];
        }

        // Power >= 1.5tr - trả về theo typePet hoặc default
        return switch (this.typePet) {
            case 1 ->
                (short) 297;
            case 2 ->
                (short) 508;
            case 3 ->
                (short) 237;
            case 4 ->
                (short) 946;
            case 5 ->
                (short) 876;
            case 6 ->
                (short) 1422;
            default ->
                PET_ID[3][this.gender];
        };
    }

    @Override
    public short getBody() {
        // Kiểm tra effect skill trước
        if (effectSkill != null) {
            if (effectSkill.isBinh) {
                return idOutfitMafuba[effectSkill.typeBinh][1];
            }
            if (effectSkill.isStone) {
                return 455;
            }
            if (effectSkill.isHalloween) {
                return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][1];
            }
            if (effectSkill.isMonkey) {
                return 193;
            }
            if (effectSkill.isSocola) {
                return 413;
            }
        }

        // Kiểm tra typePet và transform
        switch (this.typePet) {
            case 1:
                if (!this.isTransform) {
                    return 298;
                }
                break;
            case 2:
                if (!this.isTransform) {
                    return 509;
                }
                break;
            case 3:
                if (!this.isTransform) {
                    return 238;
                }
                break;
            case 4:
                if (!this.isTransform) {
                    return 947;
                }
                break;
            case 5:
                if (!this.isTransform) {
                    return 877;
                }
                break;
            case 6:
                if (!this.isTransform) {
                    return 1423;
                }
                break;
        }

        // Kiểm tra item body tại vị trí 5 (outfit)
        if (inventory != null && inventory.itemsBody != null
                && inventory.itemsBody.size() > 5
                && inventory.itemsBody.get(5).isNotNullItem()) {
            int part = inventory.itemsBody.get(5).template.body;
            if (part != -1) {
                return (short) part;
            }
        }

        // Kiểm tra item body tại vị trí 0 (áo)
        if (inventory != null && inventory.itemsBody != null
                && inventory.itemsBody.size() > 0
                && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }

        // Kiểm tra power level
        if (this.nPoint.power < 1500000) {
            return PET_ID[this.gender][1];
        }

        // Power >= 1.5tr - trả về theo typePet hoặc default
        return switch (this.typePet) {
            case 1 ->
                (short) 298;
            case 2 ->
                (short) 509;
            case 3 ->
                (short) 238;
            case 4 ->
                (short) 947;
            case 5 ->
                (short) 877;
            case 6 ->
                (short) 1423;
            default ->
                (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
        };
    }

    

    @Override
    public short getLeg() {
        // Kiểm tra effect skill trước
        if (effectSkill != null) {
            if (effectSkill.isBinh) {
                return idOutfitMafuba[effectSkill.typeBinh][2];
            }
            if (effectSkill.isStone) {
                return 456;
            }
            if (effectSkill.isHalloween) {
                return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][2];
            }
            if (effectSkill.isMonkey) {
                return 194;
            }
            if (effectSkill.isSocola) {
                return 414;
            }
        }

        // Kiểm tra typePet và transform
        switch (this.typePet) {
            case 1:
                if (!this.isTransform) {
                    return 299;
                }
                break;
            case 2:
                if (!this.isTransform) {
                    return 510;
                }
                break;
            case 3:
                if (!this.isTransform) {
                    return 239;
                }
                break;
            case 4:
                if (!this.isTransform) {
                    return 948;
                }
                break;
            case 5:
                if (!this.isTransform) {
                    return 878;
                }
                break;
            case 6:
                if (!this.isTransform) {
                    return 1424;
                }
                break;
        }

        // Kiểm tra item body tại vị trí 5 (outfit)
        if (inventory != null && inventory.itemsBody != null
                && inventory.itemsBody.size() >= 5
                && inventory.itemsBody.get(5).isNotNullItem()) {
            int part = inventory.itemsBody.get(5).template.leg;
            if (part != -1) {
                return (short) part;
            }
        }

        // Kiểm tra item body tại vị trí 1 (quần)
        if (inventory != null && inventory.itemsBody != null
                && inventory.itemsBody.size() > 1
                && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }

        // Kiểm tra power level
        if (this.nPoint.power < 1500000) {
            return PET_ID[this.gender][2];
        }

        // Power >= 1.5tr - trả về theo typePet hoặc default
        return switch (this.typePet) {
            case 1 ->
                (short) 299;
            case 2 ->
                (short) 510;
            case 3 ->
                (short) 239;
            case 4 ->
                (short) 948;
            case 5 ->
                (short) 878;
            case 6 ->
                (short) 1424;
            default ->
                (short) (gender == ConstPlayer.NAMEC ? 60 : 58);
        };
    }

    private Player findPlayerAttack() {
        List<Player> playersMap = zone.getHumanoids();
        int dis = ARANGE_CAN_ATTACK;
        Player plAtt = null;

        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            if (!cantAttack(pl)) {
                int d = Util.getDistance(this, pl);
                if (d <= dis) {
                    dis = d;
                    plAtt = pl;
                }
            }
        }

        return plAtt;
    }

    private boolean cantAttack(Player player) {
        return player == null || player.location == null || player.isDie() || Util.getDistance(this, player) > 500
                || this.equals(player) || (player.equals(master) && this.typePet != 2 && this.typePet != 4)
                || (!temporaryEnemies.contains(player) && !master.temporaryEnemies.contains(player))
                || (!SkillService.gI().canAttackPlayer(this, player));
    }

    private Mob findMobAttack() {
        int dis = ARANGE_CAN_ATTACK;
        Mob mobAtt = null;
        for (Mob mob : zone.mobs) {
            if (mob.isDie()) {
                continue;
            }
            int d = Util.getDistance(this, mob);
            if (d <= dis) {
                dis = d;
                mobAtt = mob;
            }
        }
        return mobAtt;
    }

    // Sức mạnh mở skill đệ
    private void updatePower() {
        if (this.playerSkill != null) {
            switch (this.playerSkill.getSizeSkill()) {
                case 1:
                    if (this.nPoint.power >= 150000000) {
                        openSkill2();
                    }
                    break;
                case 2:
                    if (this.nPoint.power >= 1500000000) {
                        openSkill3();
                    }
                    break;
                case 3:
                    if (this.nPoint.power >= 20000000000L) {
                        openSkill4();
                    }
                    break;
                case 4:
                    if (this.nPoint.power >= 60000000000L) {
                        openSkill5();
                    }
                    break;
            }
        }
    }

    public void openSkill2() {
        Skill currentSkill = this.playerSkill.skills.get(1);
        Skill skill = null;

        int[] tiLe = {33, 33, 34};
        byte[] skills = {Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC};

        int totalTiLe = 0;
        for (int t : tiLe) {
            totalTiLe += t;
        }

        int rd = Util.nextInt(1, totalTiLe);

        int accumulatedTiLe = 0;
        for (int i = 0; i < tiLe.length; i++) {
            accumulatedTiLe += tiLe[i];
            if (rd <= accumulatedTiLe) {
                skill = SkillUtil.createSkill(skills[i], 1);
                break;
            }
        }

        while (currentSkill != null && skill != null && skill.skillId == currentSkill.skillId) {
            rd = Util.nextInt(1, totalTiLe);
            accumulatedTiLe = 0;
            for (int i = 0; i < tiLe.length; i++) {
                accumulatedTiLe += tiLe[i];
                if (rd <= accumulatedTiLe) {
                    skill = SkillUtil.createSkill(skills[i], 1);
                    break;
                }
            }
        }

        skill.coolDown = 1000;
        this.playerSkill.skills.set(1, skill); // Thêm kỹ năng mới vào vị trí 1
    }

    public void openSkill3() {
        Skill skill = null;

        // Danh sách kỹ năng và tỉ lệ tương ứng
        int[] tiLe = {30, 40, 30};
        byte[] skills = {Skill.THAI_DUONG_HA_SAN, Skill.TAI_TAO_NANG_LUONG, Skill.KAIOKEN};

        int totalTiLe = 0;
        for (int t : tiLe) {
            totalTiLe += t; // Tính tổng tỉ lệ
        }
        int rd = Util.nextInt(1, totalTiLe); // Sinh số ngẫu nhiên trong tổng tỉ lệ

        // Xác định kỹ năng mới dựa trên xác suất
        int accumulatedTiLe = 0;
        for (int i = 0; i < tiLe.length; i++) {
            accumulatedTiLe += tiLe[i];
            if (rd <= accumulatedTiLe) {
                skill = SkillUtil.createSkill(skills[i], 1);
                break;
            }
        }

        this.playerSkill.skills.set(2, skill); // Thêm kỹ năng mới vào vị trí 2
    }

    public void openSkill4() {
        Skill skill = null;
        int tiLeBienKhi = 10;
        int tiLeDeTrung = 70;
        int tiLeKNL = 20;

        int rd = Util.nextInt(1, 100);
        if (rd <= tiLeBienKhi) {
            skill = SkillUtil.createSkill(Skill.BIEN_KHI, 1);
        } else if (rd <= tiLeBienKhi + tiLeDeTrung) {
            skill = SkillUtil.createSkill(Skill.DE_TRUNG, 1);
        } else if (rd <= tiLeBienKhi + tiLeDeTrung + tiLeKNL) {
            skill = SkillUtil.createSkill(Skill.KHIEN_NANG_LUONG, 1);
        }
        this.playerSkill.skills.set(3, skill);
    }

    public void openSkill5() {
        // Chỉ cho phép type 4, 5, 6 mở skill
        if (typePet >= 4 && typePet <= 6) {
            int tiLeSuperKame = 35;
            int tiLeLienHoanChuong = 35;
            int tiLeMaPhongBa = 30;

            int rd = Util.nextInt(1, 100);
            Skill skill = null;

            if (rd <= tiLeSuperKame) {
                skill = SkillUtil.createSkill(Skill.SUPER_KAME, 1);
            } else if (rd <= tiLeSuperKame + tiLeLienHoanChuong) {
                skill = SkillUtil.createSkill(Skill.LIEN_HOAN_CHUONG, 1);
            } else if (rd <= tiLeSuperKame + tiLeLienHoanChuong + tiLeMaPhongBa) {
                skill = SkillUtil.createSkill(Skill.MA_PHONG_BA, 1);
            }

            if (skill != null && this.playerSkill.skills.size() > 4) {
                this.playerSkill.skills.set(4, skill);
            }
        }
    }

    private Skill getSkill(int indexSkill) {
        return this.playerSkill.skills.get(indexSkill - 1);
    }

    public void transform() {
        if (this.typePet == 1) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Bố Mày Là Bư Nè !! Bư..Bư..Bư..Ma..Nhân..Bư....");
        }
        if (this.typePet == 2) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Tao là thần");
        }
        if (this.typePet == 2) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Chúng mày quỳ xuống");
        }
        if (this.typePet == 4) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Hỡi nhân loại thấp kém! Hãy chiêm ngưỡng vẻ đẹp của ta!");
        }
        if (this.typePet == 5) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Hỡi nhân loại thấp kém! Hãy chiêm ngưỡng vẻ đẹp của ta!");
        }
        if (this.typePet == 6) {
            this.isTransform = !this.isTransform;
            Service.gI().Send_Caitrang(this);
            Service.gI().chat(this, "Hỡi nhân loại thấp kém! Hãy chiêm ngưỡng vẻ đẹp của ta!");
        }
    }

    public long lastTimeAskAttack;

    public boolean canAttack() {
        if (this.master.isPl() && this.master.doesNotAttack && this.master.charms.tdDeTu < System.currentTimeMillis()) {
            if (Util.canDoWithTime(lastTimeAskAttack, 10000)) {
                Service.gI().chatJustForMe(master, this,
                        this.typePet == 4 ? "Sao ngươi không đánh đi?" : "Sao sư phụ không đánh đi?");
                lastTimeAskAttack = System.currentTimeMillis();
            }
            return false;
        }
        return true;
    }

    public void petSay(Player player) {
        if (this.typePet == 4) {
            if (Util.canDoWithTime(lastTimeChat, indexChat == 0 ? 15000 : 1500)) {
                String[] chat = {
                    "Ta chính là thế giới",
                    "Ta chính là công lí",
                    "Hãy chiêm ngưỡng vẻ đẹp của ta! Hỡi con người",
                    "Sức mạnh to lớn nằm trong cơ thể bất tử",
                    "Ta sẽ đem công lí tới toàn bộ vũ trụ này"
                };
                Service.gI().chat(this, chat[indexChat]);
                indexChat = (indexChat + 1) % chat.length;
                lastTimeChat = System.currentTimeMillis();
            }
        } else {
            if (Util.canDoWithTime(lastTimeChat, indexChat == 0 ? 15000 : 1500)) {
                String[] chat = {
                    "Mày chán sống rồi à " + player.name + "?",
                    "Mày muốn chết đúng không?",
                    "Ngày này năm sau",
                    "Tao sẽ nhớ uống thật nhiều nước",
                    "Để đái vào mộ mày"
                };
                Service.gI().chat(this, chat[indexChat]);
                indexChat = (indexChat + 1) % chat.length;
                lastTimeChat = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void dispose() {
        this.mobAttack = null;
        this.playerAttack = null;
        this.master = null;
        ChangeMapService.gI().exitMap(this);
        super.dispose();
    }
}
