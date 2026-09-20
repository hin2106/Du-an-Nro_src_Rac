package player;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import mob.Mob;
import services.SkillService;
import skill.Skill;

public class NewSkill {

    public static final int TIME_GONG = 2000;
    public static final int TIME_END_24_25 = 3000;
    public static final int TIME_END_26 = 11000;

    private Player player;

    public NewSkill(Player player) {
        this.player = player;
        this.playersTaget = new ArrayList<>();
        this.mobsTaget = new ArrayList<>();
    }

    public Skill skillSelect;

    public byte dir;

    public short _xPlayer;

    public short _yPlayer;

    public short _xObjTaget;

    public short _yObjTaget;

    public List<Player> playersTaget;

    public List<Mob> mobsTaget;

    public boolean isStartSkillSpecial;

    public byte stepSkillSpecial;

    public long lastTimeSkillSpecial;

    public byte typePaint = 0;

    public byte typeItem = 0;

    private void update() {
        if (this.isStartSkillSpecial) {
            SkillService.gI().updateSkillSpecial(player);
        }
    }

    public byte getTypePaint() {
        if (player.inventory != null && player.inventory.itemsBody != null) {
            for (item.Item it : player.inventory.itemsBody) {
                if (it == null || !it.isNotNullItem()) continue;
                int itemId = it.template.id;
                // Sách tuyệt kỹ 1 theo hành tinh: 0:1044, 1:1211, 2:1212 => typePaint = 2
                if ((player.gender == 0 && itemId == 1044)
                        || (player.gender == 1 && itemId == 1211)
                        || (player.gender == 2 && itemId == 1212)) {
                    return 2;
                }
                // Sách tuyệt kỹ 2 theo hành tinh: 0:1278, 1:1279, 2:1280 => typePaint = 3
                if ((player.gender == 0 && itemId == 1278)
                        || (player.gender == 1 && itemId == 1279)
                        || (player.gender == 2 && itemId == 1280)) {
                    return 3;
                }
            }
        }
        return 0;
    }

    public byte getTypeItem() {
        if (player.inventory != null && player.inventory.itemsBody != null) {
            for (item.Item it : player.inventory.itemsBody) {
                if (it == null || !it.isNotNullItem()) continue;
                int itemId = it.template.id;
                // Trái đất: 0
                // Namec (1): sách 1 (1211) => 2, sách 2 (1279) => 2 (yêu cầu)
                if (player.gender == 1) {
                    if (itemId == 1211 || itemId == 1279) {
                        return 2;
                    }
                }
                // Xayda (2): sách 1 (1212) => 2, sách 2 (1280) => 3 (giữ nguyên)
                if (player.gender == 2) {
                    if (itemId == 1212) {
                        return 2;
                    }
                    if (itemId == 1280) {
                        return 3;
                    }
                }
            }
        }
        return 0;
    }

    public void setSkillSpecial(byte dir, short _xPlayer, short _yPlayer, short _xObjTaget, short _yObjTaget) {
        this.skillSelect = this.player.playerSkill.skillSelect;
        if (this.player.isPl() && skillSelect.currLevel < 1000) {
            skillSelect.currLevel++;
            SkillService.gI().sendCurrLevelSpecial(player, skillSelect);
        }
        this.dir = dir;
        this._xPlayer = _xPlayer;
        this._yPlayer = _yPlayer;

        int length = _xObjTaget - _xPlayer;
        int dx = dir * (skillSelect.point * 100 + 100);
        if (skillSelect.template.id != Skill.MA_PHONG_BA) {
            if (Math.abs(dx) < Math.abs(length) || Math.abs(length) < 100) {
                this._xObjTaget = (short) dx;
            } else {
                this._xObjTaget = (short) length;
            }
        } else {
            this._xObjTaget = 75;
        }
        this._xObjTaget = (short) Math.abs(this._xObjTaget);
        this._yObjTaget = (short) Math.abs(_yObjTaget);
        this.isStartSkillSpecial = true;
        this.stepSkillSpecial = 0;
        this.lastTimeSkillSpecial = System.currentTimeMillis();
        this.start(250);
    }

    public void sonPhiPhai() {
        if (player.isAdmin()) {
            typePaint = -1;
        }
    }

    public void closeSkillSpecial() {
        this.isStartSkillSpecial = false;
        this.stepSkillSpecial = 0;
        this.playersTaget.clear();
        this.mobsTaget.clear();
        this.close();
    }

    private Timer timer;
    private TimerTask timerTask;
    private boolean isActive = false;

    private void close() {
        try {
            this.isActive = false;
            this.timer.cancel();
            this.timerTask.cancel();
            this.timer = null;
            this.timerTask = null;
        } catch (Exception e) {
            this.timer = null;
            this.timerTask = null;
        }
    }

    public void start(int leep) {
        if (this.isActive == false) {
            this.isActive = true;
            this.timer = new Timer();
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (player == null || player.newSkill == null) {
                        close();
                        return;
                    }
                    NewSkill.this.update();
                }
            };
            this.timer.schedule(timerTask, leep, leep);
        }
    }

    public void dispose() {
        this.player = null;
        this.skillSelect = null;
    }

}