package daos;
import data.AlyraManager;
import data.AlyraResultSet;
import item.Item;
import java.util.*;
import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TopRewardDAO {

    public static class RewardCfg {

        public byte board;
        public int rankFrom, rankTo;
        public Map<Integer, Integer> qty = new HashMap<>();
        public Map<Integer, List<Item.ItemOption>> opts = new HashMap<>();
    }

    public static List<RewardCfg> loadAll() {
        List<RewardCfg> list = new ArrayList<>();
        try {
            AlyraResultSet rs = AlyraManager.executeQuery(
                    "SELECT board,rank_from,rank_to,detail FROM gift_top");
            while (rs.next()) {
                RewardCfg c = new RewardCfg();
                c.board = (byte) rs.getInt("board");
                c.rankFrom = rs.getInt("rank_from");
                c.rankTo = rs.getInt("rank_to");

                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (Object o : jar) {
                        JSONObject obj = (JSONObject) o;
                        int id = Integer.parseInt(obj.get("id").toString());
                        int quantity = Integer.parseInt(obj.get("quantity").toString());
                        c.qty.put(id, quantity);

                        JSONArray option = (JSONArray) obj.get("options");
                        List<Item.ItemOption> optionList = new ArrayList<>();
                        if (option != null) {
                            for (Object u : option) {
                                JSONObject jo = (JSONObject) u;
                                optionList.add(new Item.ItemOption(
                                        Integer.parseInt(jo.get("id").toString()),
                                        Integer.parseInt(jo.get("param").toString())));
                            }
                        }
                        c.opts.put(id, optionList);
                    }
                }
                list.add(c);
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
