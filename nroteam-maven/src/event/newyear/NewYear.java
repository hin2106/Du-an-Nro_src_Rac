package event.newyear;

import boss.BossID;
import event.Event;
import java.util.ArrayList;
import java.util.List;
import managers.boss.BossManager;
import managers.boss.ChristmasEventManager;
import map.ItemMap;
import mob.Mob;
import server.Manager;
import shop.TabShopSanta;
// =====================
// NẤU BÁNH NGÀY TẾT
// A. Nguyên Liệu
// - Thịt Heo: Đánh Quái Heo để nhận 
// - Nếp: Tham Gia Võ Đài (Thắng nhận 5, thua nhận 1)
// - Đậu Xanh: Làm nhiệm vụ Bò Mộng, đánh quái trong phó bản( Doanh Trại, Bản Đồ Kho Báu,...)
// - Lá Dong: Mộc Nhân
// B. Nấu Bánh
// Đến NPC Nồi Bánh để nấu bánh theo công thức sau:
// - Bánh Chưng: Thịt heo x15 + Nếp x10 + Đậu xanh x10 + Lá dong x10 + 10 triệu vàng + 10 Ngọc Xanh
// - Bánh Tét: Thịt heo x10 + Nếp x10 + Đậu xanh x10 + Lá dong x5 + 5 triệu vàng
// =====================
// 
// =====================
// GIẢI CỨU NGỘ KHÔNG
// A. Nguyên Liệu
// - Bùa giải khai phong ấn: Đánh quái rơi, đánh siêu hạng, hoặc đưa quả hồng đào cho NPC Ngộ Không để nhận.
// - Quả hồng đào: mua ở shop santa.
// =====================
public class NewYear extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TET;
    }

    @Override
    public void init() {
        initNpc();
        spawnBosses();
    }

    private void spawnBosses() {
        if (Manager.EVENT_SEVER != eventId()) {
            return;
        }
        if (ChristmasEventManager.gI().getBossCount() == 0) {
            int count = 10; 
            for (int i = 0; i < count; i++) {
                BossManager.gI().createBoss(BossID.LAN_CON);
            }
        }
    }

    @Override
    public void initNpc() {
        try {
            new event.newyear.Npc_DuongTang().initNpc();
            new event.newyear.Npc_NgoKhong().initNpc();
            new event.newyear.Npc_NoiBanh().initNpc();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(player.Player p, Mob m, List<ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(player.Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event Newyear - thêm/sửa/xóa item tại đây
        // ví dụ 5 item sau.
        items.add(new TabShopSanta.SantaItemConfig(541, (byte) 1, 1, new int[][]{{30, 0}}, true));
        // items.add(new TabShopSanta.SantaItemConfig(649, (byte) 1, 50, new int[][]{{30, 0}, {0, 15}}, true));
        // items.add(new TabShopSanta.SantaItemConfig(386, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        // items.add(new TabShopSanta.SantaItemConfig(389, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        // items.add(new TabShopSanta.SantaItemConfig(392, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        return items;
    }

    
}
