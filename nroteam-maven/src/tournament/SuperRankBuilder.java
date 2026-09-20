package tournament;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SuperRankBuilder {

    private int id;
    private int rank;
    private long lastPKTime;
    private long lastTimeReward;
    private int ticket;
    private int win;
    private int lose;
    private String info;

    private int head;
    private int body;
    private int leg;
    private String name;
    private String customString;

    private long power;
    private Long lastTimeOnline;

    private int level; 
    private long time;
    private long lasttime; 
    private int point;
    private int gender;     
    private String itemsBody; 

    public Long getLastTimeOnline() {
        return lastTimeOnline;
    }

    public void setLastTimeOnline(Long lastTimeOnline) {
        this.lastTimeOnline = lastTimeOnline;
    }

    public void dispose() {
        name = null;
        info = null;
    }
}


