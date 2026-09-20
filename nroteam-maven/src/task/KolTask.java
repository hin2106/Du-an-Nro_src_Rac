package task;

public class KolTask {

    public int missionId;

    public String name;

    public String description;

    public int count;

    public int maxCount;

    public boolean claimed;

    public boolean claimedVip;

    public KolTask() {
        missionId = -1;
        name = null;
        description = null;
        count = 0;
        maxCount = 1;
        claimed = false;
        claimedVip = false;
    }

    public boolean isDone() {
        if (maxCount <= 0) {
            return false;
        }
        return count >= maxCount;
    }

    public int getPercent() {
        if (maxCount <= 0) {
            return 0;
        }
        if (count >= maxCount) {
            return 100;
        }
        if (count < 0) {
            return 0;
        }
        return (int) ((long) count * 100 / maxCount);
    }
}
