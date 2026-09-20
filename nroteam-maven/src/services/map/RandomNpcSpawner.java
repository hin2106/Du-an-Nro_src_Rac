package services.map;

import npc.Npc;
import npc.NpcFactory;
import map.Map;
import map.Zone;
import server.Manager;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import utils.Logger;
import utils.Util;

public final class RandomNpcSpawner {

    private final int npcTempId;
    private final List<Integer> allowedMapIds;
    private final Duration ttl;
    private final Duration respawnDelay;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<Npc> current = new AtomicReference<>();
    private volatile boolean running = false;

    public RandomNpcSpawner(int npcTempId, Collection<Integer> allowedMapIds,
            Duration ttl, Duration respawnDelay) {
        this.npcTempId = npcTempId;
        this.allowedMapIds = List.copyOf(allowedMapIds);
        this.ttl = ttl;
        this.respawnDelay = respawnDelay;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NpcSpawner-" + npcTempId);
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        scheduler.execute(this::spawnOnceAndScheduleNext);
    }

    public synchronized void stop() {
        running = false;
        scheduler.shutdownNow();
        despawnCurrentIfAny();
    }

    private void spawnOnceAndScheduleNext() {
        if (!running) {
            return;
        }
        try {
            despawnCurrentIfAny();
            spawnNewAtRandomMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (running) {
            scheduler.schedule(this::onTtlExpired, ttl.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void onTtlExpired() {
        if (!running) {
            return;
        }
        try {
            despawnCurrentIfAny();
        } finally {
            if (running) {
                scheduler.schedule(this::spawnOnceAndScheduleNext,
                        respawnDelay.toMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private void spawnNewAtRandomMap() {
        if (allowedMapIds.isEmpty()) {
            return;
        }

        int mapId = allowedMapIds.get(Util.nextInt(0, allowedMapIds.size() - 1));
        Map map = getMapById(mapId);
        if (map == null) {
            return;
        }

        Zone zone = pickZone(map);
        if (zone == null) {
            return;
        }
        int x = Util.nextInt(50, Math.max(100, map.mapWidth - 50));
        int y = map.yPhysicInTop(x, 0);
        Npc npc = NpcFactory.createNPC(mapId, 0, x, y, npcTempId);
        if (npc == null) {
            return;
        }

        synchronized (zone.map.npcs) {
            zone.map.npcs.add(npc);
        }

        if (npc.mapId != mapId) {
            npc.mapId = mapId;
            npc.map = map;
        }

        NpcService.gI().createNpcAppear(npc, zone);
        current.set(npc);

        // Logger.logln("Spawned NPC tempId=" + npcTempId + " tại map " + mapId + " (" + x + "," + y + ")");
    }

    private void despawnCurrentIfAny() {
        Npc npc = current.getAndSet(null);
        if (npc == null) {
            return;
        }
        // Tìm zone chứa NPC
        Zone zone = null;
        if (npc.map != null && npc.map.zones != null) {
            for (Zone z : npc.map.zones) {
                if (z != null && z.map.npcs.contains(npc)) {
                    zone = z;
                    break;
                }
            }
        }

        if (zone == null) {
            synchronized (Manager.NPCS) {
                Manager.NPCS.remove(npc);
            }
            // Logger.logln("Despawn NPC tempId=" + npc.tempId + " (not found in zone)");
            return;
        }

        synchronized (zone.map.npcs) {
            zone.map.npcs.remove(npc);
        }
        synchronized (Manager.NPCS) {
            Manager.NPCS.remove(npc);
        }
        NpcService.gI().removeNpc(npc, zone);
        // Logger.logln("Despawn NPC tempId=" + npc.tempId);
    }

    private Map getMapById(int mapId) {
        for (Map m : Manager.MAPS) {
            if (m.mapId == mapId) {
                return m;
            }
        }
        return null;
    }

    private Zone pickZone(Map map) {
        Zone best = null;
        int minPlayer = Integer.MAX_VALUE;
        for (Zone z : map.zones) {
            int count = z.getNumOfPlayers();
            if (!z.isFullPlayer() && count < minPlayer) {
                minPlayer = count;
                best = z;
            }
        }
        return best != null ? best : map.zones.get(Util.nextInt(0, map.zones.size() - 1));
    }
}
