package managers;
import daos.TopRewardDAO;
import daos.TopRewardDAO.RewardCfg;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import services.ItemService;

public class TopRewardManager {

    private static TopRewardManager instance;

    public static TopRewardManager gI() {
        if (instance == null) {
            instance = new TopRewardManager();
        }
        return instance;
    }

    private final List<RewardCfg> cfgs = new ArrayList<>();

    public void reload() {
        cfgs.clear();
        cfgs.addAll(TopRewardDAO.loadAll());
    }

    public int countCfg() {
        return cfgs.size();
    }

    public List<Item> buildItems(byte board, int rank) {
        List<Item> out = new ArrayList<>();
        for (RewardCfg c : cfgs) {
            if (c.board != board) {
                continue;
            }
            if (rank < c.rankFrom || rank > c.rankTo) {
                continue;
            }
            for (var e : c.qty.entrySet()) {
                short id = (short) e.getKey().intValue();
                int q = Math.max(1, e.getValue());
                Item it = ItemService.gI().createNewItem(id, q);
                if (it == null || it.template == null) {
                    continue; 
                }
                List<Item.ItemOption> ops = c.opts.getOrDefault((int) id, List.of());
                if (ops != null && !ops.isEmpty()) {
                    it.itemOptions.addAll(ops);
                }
                out.add(it);
            }
        }
        return out;
    }
}