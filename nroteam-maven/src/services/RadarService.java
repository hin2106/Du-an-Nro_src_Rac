package services;
import radar.Card;
import radar.OptionCard;
import radar.RadarCard;
import player.Player;
import network.Message;
import java.util.ArrayList;
import java.util.List;

public class RadarService {

    public List<RadarCard> RADAR_TEMPLATE = new ArrayList<>();

    private static RadarService instance;

    public static RadarService gI() {
        if (instance == null) {
            instance = new RadarService();
        }
        return instance;
    }

    public void sendRadar(Player pl, List<Card> cards) {
        try {
            Message m = new Message(127);
            m.writer().writeByte(0);
            m.writer().writeShort(RadarService.gI().RADAR_TEMPLATE.size());
            for (RadarCard radar : RadarService.gI().RADAR_TEMPLATE) {
                Card card = cards.stream().filter(c -> c.Id == radar.Id).findFirst().orElse(null);
                if (card == null) {
                    card = new Card(radar.Max, radar.Options);
                }
                m.writer().writeShort(radar.Id);
                short iconToSend = radar.IconId;
                if (!radar.IconByLevel.isEmpty()) {
                    int lvl = Math.max(1, card.Level);
                    int idx = Math.min(lvl - 1, radar.IconByLevel.size() - 1);
                    iconToSend = radar.IconByLevel.get(idx);
                }
                m.writer().writeShort(iconToSend);
                m.writer().writeByte(radar.Rank);
                m.writer().writeByte(card.Amount);
                m.writer().writeByte(card.MaxAmount);
                m.writer().writeByte(radar.Type);
                switch (radar.Type) {
                    case 0 ->
                        m.writer().writeShort(radar.Template);
                    case 1 -> {
                        if (!radar.PartsByLevel.isEmpty()) {
                            int lvl = Math.max(1, card.Level);
                            int idx = Math.min(lvl - 1, radar.PartsByLevel.size() - 1);
                            short[] parts = radar.PartsByLevel.get(idx);
                            m.writer().writeShort(parts[0]);
                            m.writer().writeShort(parts[1]);
                            m.writer().writeShort(parts[2]);
                            m.writer().writeShort(parts[3]);
                        } else {
                            m.writer().writeShort(radar.Head);
                            m.writer().writeShort(radar.Body);
                            m.writer().writeShort(radar.Leg);
                            m.writer().writeShort(radar.Bag);
                        }
                    }
                }
                m.writer().writeUTF(radar.Name);
                m.writer().writeUTF(radar.Info);
                m.writer().writeByte(card.Level);
                m.writer().writeByte(card.Used);
                m.writer().writeByte(radar.Options.size());
                for (OptionCard option : radar.Options) {
                    m.writer().writeShort(option.id);
                    m.writer().writeInt(option.param);
                    m.writer().writeByte(option.active);
                }
            }
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void Radar1(Player pl, short id, int use) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(1);
            message.writer().writeShort(id);
            message.writer().writeByte(use);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void RadarSetLevel(Player pl, int id, int level) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(2);
            message.writer().writeShort(id);
            message.writer().writeByte(level);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void RadarSetAmount(Player pl, int id, int amount, int max_amount) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(3);
            message.writer().writeShort(id);
            message.writer().writeByte(amount);
            message.writer().writeByte(max_amount);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendAura(Player pl, int id_Aura, int id_Eff_Set_Item) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(id_Aura);
            message.writer().writeByte(id_Eff_Set_Item);
            Service.gI().sendMessAllPlayerInMap(pl, message);
            message.cleanup();
        } catch (Exception e) {
        }
    }
}
