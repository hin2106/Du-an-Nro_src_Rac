package top;

import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import mail.entity.MailReward;
import mail.enums.RewardType;
import mail.service.MailService;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import player.Player;
import services.top.TopAppearanceCache;
import top.TopBoardModels.Entry;
import top.TopBoardModels.Reward;
import utils.Logger;

public final class TopBoardService {
    private static final TopBoardService INSTANCE = new TopBoardService();
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private TopBoardService() {}
    public static TopBoardService gI() { return INSTANCE; }

    public void initialize() { TopBoardRepository.ensureSchema(); }

    public void sendBoard(Player viewer, byte clientBoard) {
        String board = clientBoard == TopBoardModels.CLIENT_WHIS ? TopBoardModels.WHIS : TopBoardModels.SUPER_RANK;
        List<Entry> all = board.equals(TopBoardModels.WHIS) ? queryWhis() : querySuperRank();
        Entry self = null;
        for (int i = 0; i < all.size(); i++) {
            Entry e = all.get(i); e.rank = board.equals(TopBoardModels.WHIS) ? i + 1 : e.rank;
            if (e.playerId == viewer.id) self = e;
        }
        List<Entry> top = all.size() > 100 ? new ArrayList<>(all.subList(0, 100)) : all;
        for (Entry e : top) e.rewards.addAll(TopBoardRepository.rewardsFor(board, e.rank));
        if (self != null && self.rewards.isEmpty()) self.rewards.addAll(TopBoardRepository.rewardsFor(board, self.rank));
        Message msg = null;
        try {
            msg = new Message(-58);
            msg.writer().writeByte(20);
            msg.writer().writeByte(clientBoard);
            msg.writer().writeUTF(board.equals(TopBoardModels.WHIS) ? "Top Whis" : "Top Siêu Hạng");
            msg.writer().writeByte(top.size());
            for (Entry e : top) writeEntry(msg, e, true);
            msg.writer().writeBoolean(self != null);
            if (self != null) writeEntry(msg, self, self.rank <= 100);
            viewer.sendMessage(msg);
        } catch (Exception e) { Logger.logException(TopBoardService.class, e, "Gửi bảng Top thất bại"); }
        finally { if (msg != null) msg.cleanup(); }
    }

    private void writeEntry(Message msg, Entry e, boolean showRank) throws Exception {
        msg.writer().writeInt(showRank ? e.rank : -1);
        msg.writer().writeInt(e.playerId);
        msg.writer().writeShort(e.head); msg.writer().writeShort(e.body); msg.writer().writeShort(e.leg);
        msg.writer().writeUTF(e.name == null ? "" : e.name);
        msg.writer().writeUTF(e.guild == null ? "" : e.guild);
        msg.writer().writeInt(e.primaryValue); msg.writer().writeLong(e.timeMs); msg.writer().writeLong(e.achievedAt);
        msg.writer().writeByte(Math.min(20, e.rewards.size()));
        for (int i = 0; i < e.rewards.size() && i < 20; i++) {
            Reward r = e.rewards.get(i);
            msg.writer().writeByte(r.type); msg.writer().writeShort(r.itemId); msg.writer().writeInt(r.amount);
        }
    }

    public List<Entry> querySuperRank() {
        List<Entry> result = new ArrayList<>();
        String sql = "SELECT sr.player_id,sr.rank,sr.name,sr.win,p.head,p.gender,COALESCE(c.name,'') guild "
                + "FROM super_rank sr JOIN player p ON p.id=sr.player_id LEFT JOIN clan c ON c.id=p.clan_id "
                + "WHERE sr.rank>0 ORDER BY sr.rank ASC";
        try (Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {
            while (rs.next()) {
                Entry e=new Entry(); e.playerId=rs.getInt(1);e.rank=rs.getInt(2);e.name=rs.getString(3);e.primaryValue=rs.getInt(4);
                short head=rs.getShort(5);int gender=rs.getInt(6);short body=(short)(gender==1?59:57),leg=(short)(gender==1?60:58);
                short[] parts=TopAppearanceCache.getAppearance(e.playerId,head,body,leg);e.head=parts[0];e.body=parts[1];e.leg=parts[2];e.guild=rs.getString(7);
                result.add(e);
            }
        } catch(Exception e){Logger.logException(TopBoardService.class,e,"Đọc Top Siêu Hạng thất bại");}
        return result;
    }

    public List<Entry> queryWhis() {
        List<Entry> result=new ArrayList<>();
        String sql="SELECT p.id,p.name,p.head,p.gender,p.data_luyentap,COALESCE(c.name,'') guild FROM player p LEFT JOIN clan c ON c.id=p.clan_id WHERE p.data_luyentap IS NOT NULL";
        try(Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                JSONArray a=(JSONArray)JSONValue.parse(rs.getString(5)); if(a==null)continue;
                int level=Math.max(number(a,0).intValue(),number(a,5).intValue());
                long time=number(a,6).longValue(), achieved=number(a,7).longValue();
                if(level<=0 && time<=0 && achieved<=0)continue;
                Entry e=new Entry();e.playerId=rs.getInt(1);e.name=rs.getString(2);e.primaryValue=level;
                e.timeMs=time;e.achievedAt=achieved;e.guild=rs.getString(6);
                short head=rs.getShort(3);int gender=rs.getInt(4);short body=(short)(gender==1?59:57),leg=(short)(gender==1?60:58);
                short[]parts=TopAppearanceCache.getAppearance(e.playerId,head,body,leg);e.head=parts[0];e.body=parts[1];e.leg=parts[2];result.add(e);
            }
        }catch(Exception e){Logger.logException(TopBoardService.class,e,"Đọc Top Whis thất bại");}
        result.sort(Comparator.comparingInt((Entry e)->e.primaryValue).reversed().thenComparingLong(e->e.timeMs).thenComparingLong(e->e.achievedAt));
        for(int i=0;i<result.size();i++)result.get(i).rank=i+1;
        return result;
    }

    private Number number(JSONArray a,int index){if(index>=a.size()||a.get(index)==null)return 0;Object v=a.get(index);if(v instanceof Number)return(Number)v;try{return Long.parseLong(String.valueOf(v));}catch(Exception e){return 0;}}

    public void rewardSuperRankDaily(Player player) {
        if(player==null||player.superRank==null||player.superRank.rank<1||player.superRank.rank>100)return;
        String period=LocalDate.now(VIETNAM).toString();
        deliver(TopBoardModels.SUPER_RANK,period,player.id,player.superRank.rank,"Phần thưởng Top Siêu Hạng ngày",7);
        player.superRank.lastRewardTime=System.currentTimeMillis();
    }

    public void rewardWhisWeekly() {
        LocalDate today=LocalDate.now(VIETNAM);WeekFields wf=WeekFields.ISO;
        try { if (TopBoardRepository.loadBoard(TopBoardModels.WHIS).isEmpty()) {
            Logger.warning("Chưa cấu hình quà Top Whis, bỏ qua trao quà và không reset dữ liệu.\n"); return;
        }} catch (Exception e) { Logger.logException(TopBoardService.class,e,"Không kiểm tra được cấu hình Top Whis"); return; }
        String period=today.get(wf.weekBasedYear())+"-W"+String.format(Locale.US,"%02d",today.get(wf.weekOfWeekBasedYear()));
        List<Entry> rows=queryWhis();
        for(Entry e:rows){if(e.rank>100)break;deliver(TopBoardModels.WHIS,period,e.playerId,e.rank,"Phần thưởng Top Whis tuần",7);}
        try { AlyraManager.executeUpdate("UPDATE player SET data_luyentap='[0,0,0,0,0,0,0,0]' WHERE data_luyentap IS NOT NULL"); }
        catch(Exception e){Logger.logException(TopBoardService.class,e,"Reset Top Whis thất bại");}
        try { for (Player p : server.Client.gI().getPlayers()) if (p != null && p.traning != null) {
            p.traning.setTop(0); p.traning.setTopWhis(0); p.traning.setTime(0); p.traning.setLastTime(0); p.traning.setLastTop(0);
        }} catch (Exception e) { Logger.logException(TopBoardService.class,e,"Reset Top Whis online thất bại"); }
    }

    private void deliver(String board,String period,long playerId,int rank,String title,int expiryDays){
        List<Reward> cfg=TopBoardRepository.rewardsFor(board,rank);if(cfg.isEmpty())return;
        boolean reserved=false;
        try(Connection con=AlyraManager.getConnection()){
            reserved=TopBoardRepository.reserveDelivery(con,board,period,playerId,rank);if(!reserved)return;
            MailService mail=new MailService();long mailId=mail.sendMail(playerId,title+" - Top "+rank,
                    "Chúc mừng bạn đạt Top "+rank+". Phần thưởng đã được đính kèm trong thư.",expiryDays);
            List<MailReward> rewards=new ArrayList<>();
            for(Reward r:cfg){if(r.type==RewardType.ITEM.getCode())rewards.add(new MailReward(mailId,r.type,r.itemId,r.amount,r.options));else rewards.add(new MailReward(mailId,r.type,r.amount));}
            mail.getRewardDAO().saveRewards(rewards);mail.getMailDAO().updateHasReward(mailId,true);
            TopBoardRepository.completeDelivery(con,board,period,playerId,mailId);
        }catch(Exception e){if(reserved)TopBoardRepository.cancelDelivery(board,period,playerId);Logger.logException(TopBoardService.class,e,"Trao quà Top thất bại");}
    }
}
