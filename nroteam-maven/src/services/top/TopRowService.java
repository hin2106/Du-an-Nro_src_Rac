package services.top;
public class TopRowService {

    public final int id;
    public final String name;
    public final int head;
    public final int body;
    public final int leg;
    public final long lastTime;
    public final Long power;
    public final Integer level;
    public final Long time;
    public final String leaderName;
    public final Integer point;
    public final Long lastTimeOnline;
    public final Long lastPointTime;

    public TopRowService(int id, String name, int head, int body, int leg, long lastTime) {
        this(id, name, head, body, leg, lastTime, null, null, null, null, null, null, null);
    }

    public TopRowService(int id, String name, int head, int body, int leg, long lastTime, Long power, Integer level, Long time, String leaderName) {
        this(id, name, head, body, leg, lastTime, power, level, time, leaderName, null, null, null);
    }

    public TopRowService(int id, String name, int head, int body, int leg, long lastTime, Long power, Integer level, Long time, String leaderName,
            Integer point, Long lastTimeOnline, Long lastPointTime) {
        this.id = id;
        this.name = name;
        this.head = head;
        this.body = body;
        this.leg = leg;
        this.lastTime = lastTime;
        this.power = power;
        this.level = level;
        this.time = time;
        this.leaderName = leaderName;
        this.point = point;
        this.lastTimeOnline = lastTimeOnline;
        this.lastPointTime = lastPointTime;
    }
}
