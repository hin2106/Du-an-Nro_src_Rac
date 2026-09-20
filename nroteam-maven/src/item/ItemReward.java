package item;

import java.util.Arrays;

public class ItemReward {

    public int[] mapId;
    public int tempId;
    public int ratio;
    public int typeRatio;
    public boolean forAllGender;

    @Override
    public String toString() {
        return "ItemReward{" + "mapId=" + Arrays.toString(mapId) + ", tempId=" + tempId + ", ratio=" + ratio + ", typeRatio=" + typeRatio + ", forAllGender=" + forAllGender + '}';
    }
}
