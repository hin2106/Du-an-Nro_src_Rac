package combine;


import item.Item;
import java.util.ArrayList;
import java.util.List;

public class Combine {

    public long lastTimeCombine;

    public List<Item> itemsCombine;
    public int typeCombine;

    public long goldCombine;
    public int gemCombine;
    public float ratioCombine;
    public int countDaNangCap;
    public short countDaBaoVe;
    public int countDap;
    public List<Integer> quantities;
    public List<Integer> itemIds;
    public int upgradeMultiplier;
    public int levelCombine;
    public int DaNangcap;
    public float TileNangcap;
    public long timeDelay;
    public long startTimeDelay;
    
    public static final byte COMBINE_TYPE_INIT = 1;

    public Combine() {
        this.itemsCombine = new ArrayList<>();
    }

    public void clearItemCombine() {
        this.itemsCombine.clear();
    }

    public void clearParamCombine() {
        this.goldCombine = 0;
        this.gemCombine = 0;
        this.ratioCombine = 0;
        this.countDaNangCap = 0;
        this.countDaBaoVe = 0;
        this.upgradeMultiplier = 0;
        this.countDaNangCap = 0;

    }
    public void clearCombine() {
        if (this.quantities != null) {
            this.quantities.clear();
        } else {
             this.quantities = new ArrayList<>();
        }
        if (this.itemIds != null) {
            this.itemIds.clear();
        } else {
             this.itemIds = new ArrayList<>();
        }
        this.ratioCombine = 0;
        this.goldCombine = 0;
    }
    
    public int getTypeCombine() {
        return this.typeCombine;
    }
    
    public void setTypeCombine(int type) {
        this.typeCombine = type;
    }
    
    public void dispose() {
        this.itemsCombine = null;
    }
}
