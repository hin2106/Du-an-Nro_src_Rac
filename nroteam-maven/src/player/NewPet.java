package player;

import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.map.ChangeMapService;
import services.func.EffectMapService;
import utils.Util;

public class NewPet extends Player {

    public Player master;
    public short body;
    public short leg;
    public static int idb = -310720020;

    public int itemId = -1;

    private long lastEffectTime = 0;
    private static final long EFFECT_COOLDOWN = 500; // 0.5 giây

    public NewPet(Player master, short h, short b, short l, int itemId ) {
        this.master = master;
        this.isNewPet = true;
        this.isNewPet1 = true;
        this.id = idb;
        idb--;
        this.head = h;
        this.body = b;
        this.leg = l;
        this.itemId = itemId;
    }

    @Override
    public short getHead() {
        return head;
    }

    @Override
    public short getBody() {
        return body;
    }

    @Override
    public short getLeg() {
        return leg;
    }

    public void joinMapMaster() {
        if (master == null || master.zone == null) {
            return;
        }
        this.location.x = master.location.x + Util.nextInt(-10, 10);
        this.location.y = master.location.y;
        if (isPl()) {
            this.dispose();
            return;
        }
        if (MapService.gI().isMapOffline(master.zone.map.mapId) || master.zone.map.mapId == 113) {
            ChangeMapService.gI().goToMap(this, MapService.gI().getMapCanJoin(this, master.gender + 21, -1));
            if (this.zone != null) {
                this.zone.load_Me_To_Another(this);
            }
            return;
        }
        ChangeMapService.gI().goToMap(this, master.zone);
        this.zone.load_Me_To_Another(this);
    }

    private long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;

    private void moveIdle() {
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
        if (master != null && master.zone != null && (this.zone == null || this.zone != master.zone)
                && !MapService.gI().isMapOffline(master.zone.map.mapId)) {
            joinMapMaster();
        }
        if (master != null && master.isDie()) {
            return;
        }
        moveIdle();
    }

    public void followMaster() {
        followMaster(90);
    }

    private void followMaster(int dis) {
        if (master == null || master.location == null || this.location == null) {
            return;
        }

        int mX = master.location.x;
        int mY = master.location.y;
        long deltaX = this.location.x - mX;
        long deltaY = this.location.y - mY;
        long currentDistanceSq = (deltaX * deltaX) + (deltaY * deltaY);
        long followDistanceSq = (long) dis * dis;

        if (currentDistanceSq >= followDistanceSq) {
            int randomOffset = Util.nextInt(0, dis);
            if (deltaX < 0) {
                this.location.x = mX - randomOffset;
            } else {
                this.location.x = mX + randomOffset;
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
        sendPetEffectIfNeeded();
    }

    private void sendPetEffectIfNeeded() {
        if (this.itemId != 1929) {
            return;
        }
        if (this.master.isFly) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (!Util.canDoWithTime(lastEffectTime, EFFECT_COOLDOWN)) {
            return;
        }
        if (this.zone == null || this.location == null) {
            return;
        }
        EffectMapService.gI().sendEffectMapToAllInMap(
                zone,
                190, // effect ID
                3, // layer
                5, // loop
                this.location.x, // x position (dưới chân pet)
                this.location.y, // y position
                1 // delay
        );
        lastEffectTime = currentTime;
    }

    @Override
    public void dispose() {
        this.master = null;
        ChangeMapService.gI().exitMap(this);
        super.dispose();
    }
}
