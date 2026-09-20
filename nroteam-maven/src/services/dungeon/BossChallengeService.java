package services.dungeon;

import boss.challenge.BossChallenge;

import java.util.ArrayList;
import java.util.List;
import map.Map;
import map.Zone;
import player.Player;
import services.map.MapService;

public class BossChallengeService {

    private static BossChallengeService instance;
    private final List<BossChallenge> bossChallenges;

    private BossChallengeService() {
        this.bossChallenges = new ArrayList<>();
        initBossChallenges();
    }

    public static BossChallengeService gI() {
        if (instance == null) {
            instance = new BossChallengeService();
        }
        return instance;
    }

    private void initBossChallenges() {
        Map map164 = MapService.gI().getMapById(164);
        if (map164 == null || map164.zones.isEmpty()) {
            System.err.println("[BossChallengeService] Map 164 not found or has no zones!");
            return;
        }
        int count = Math.min(10, map164.zones.size());
        for (int i = 0; i < count; i++) {
            Zone zone = map164.zones.get(i);
            if (zone != null) {
                BossChallenge challenge = new BossChallenge(i);
                challenge.getZones().add(zone);
                bossChallenges.add(challenge);
            }
        }
    }

    public void openBossChallenge(Player player) {
        if (player == null || player.clan == null) {
            return;
        }
        BossChallenge existing = getBossChallengeByPlayer(player);
        if (existing != null) {
            return;
        }
        BossChallenge available = null;
        for (BossChallenge bc : bossChallenges) {
            if (!bc.isOpened) {
                available = bc;
                break;
            }
        }
        if (available == null) {
            return;
        }
        available.openBossChallenge(player, player.clan);
    }

    public BossChallenge getBossChallengeByPlayer(Player player) {
        if (player == null || player.clan == null) {
            return null;
        }
        for (BossChallenge bc : bossChallenges) {
            if (bc.isOpened && bc.clan != null && bc.clan.equals(player.clan)) {
                return bc;
            }
        }
        return null;
    }

    public void closeBossChallenge(BossChallenge bc) {
        if (bc != null) {
            bc.dispose();
        }
    }

    public void addMapBossChallenge(int id, Zone zone) {
        if (id >= 0 && id < bossChallenges.size() && zone != null) {
            bossChallenges.get(id).addZone(zone);
        }
    }

    public List<BossChallenge> getAllChallenges() {
        return bossChallenges;
    }
}
