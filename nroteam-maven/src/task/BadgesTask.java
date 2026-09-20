package task;

public class BadgesTask {

    public ClanTaskTemplate template;

    public int id;
    public int count;
    public int countMax;
    public int idBadgesReward;
    public boolean badgeClaimed; // Flag để đánh dấu đã nhận badge hay chưa

    public BadgesTask() {
        id = -1;
        count = -1;
        countMax = -1;
        idBadgesReward = -1;
        badgeClaimed = false;
    }

    public boolean isDone() {
        return this.count >= this.countMax && this.countMax > 0;
    }

    public boolean isExactlyDone() {
        return this.count == this.countMax && this.countMax > 0;
    }

    public int getPercentProcess() {
        if (this.count >= this.countMax) {
            return 100;
        }
        int percent = (int) Math.round((double) count * 100.0 / countMax);
        return Math.min(100, percent);
    }

    @Override
    public String toString() {
        final String n = "\"";
        return "{" + n + "id" + n + ":" + n + id + n + ","
                + n + "count" + n + ":" + n + count + n + ","
                + n + "countMax" + n + ":" + n + countMax + n + ","
                + n + "idBadgesReward" + n + ":" + n + idBadgesReward + n + ","
                + n + "badgeClaimed" + n + ":" + n + badgeClaimed + n + "}";
    }
}
