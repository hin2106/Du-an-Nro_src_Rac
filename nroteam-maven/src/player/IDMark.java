package player;

import consts.ConstNpc;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import npc.Npc;
import shop.Shop;
import shop.ItemShop;
import lombok.Data;
import map.Zone;

@Data
public class IDMark {

    private List<Player> relevantPlayers;

    private int idItemUpTop;
    private int typeChangeMap;
    private int indexMenu;
    private int typeInput;
    private byte typeLuckyRound;

    private int eventMenuId;

    private long idPlayThachDau;
    private int goldThachDau;
    private long killCharId = -9999;
    private long idEnemy;

    private Shop shopOpen;
    private String tagNameShop;
    private byte idSpaceShip;
    private ItemShop itemShopBuy;

    private int mbv;

    private String captcha;
    private long recaptcha;

    private long lastTimeBan;
    private boolean isBan;

    private int ott;

    // giao dịch
    private int playerTradeId = -1;
    private Player playerTrade;
    private long lastTimeTrade;

    private long lastTimeNotifyTimeHoldBlackBall;
    private long lastTimeHoldBlackBall;
    private int tempIdBlackBallHold = -1;
    private boolean holdBlackBall;

    private int tempIdNamecBallHold = -1;
    private boolean holdNamecBall;

    private boolean loadedAllDataPlayer;

    private long lastTimeChangeFlag;

    // xoc dia
    private int typeDatXD;
    private int slDatXD;
    private Npc npcXD;

    // Tai Xiu
    private int typeDatTX;
    private Npc npcTX;

    // Bau cua
    private int typeDatBC;
    private Npc npcBC;

    // tới tương lai
    private boolean gotoFuture;
    private long lastTimeGoToFuture;

    // ChangeMap Khi gas
    private Zone zoneKhiGasHuyDiet;
    private int xMapKhiGasHuyDiet;
    private int yMapKhiGasHuyDiet;
    private boolean goToKGHD;
    private long lastTimeGoToKGHD;

    private long lastTimeChangeZone;
    private long lastTimeChatGlobal;
    private long lastTimeChatPrivate;

    private long lastTimePickItem;

    private boolean goToBDKB;
    private long lastTimeGoToBDKB;
    private long lastTimeAnXienTrapBDKB;

    public int shenronType = 2;

    private Npc npcChose; // npc mở

    private byte loaiThe; // loại thẻ nạp

    private boolean acpTrade;

    private int damePST;

    private long lastTimeRevenge;

    private int menuType;

    private int tangHoaType;

    private boolean transactionWP;

    private boolean transactionWVP;

    private long lastTimeCombine;
    private String tempCardSerial;
    private String lastInputText;

    private boolean isGemCSMM;
    private int moneyKeoBuaBao;
    private long timePlayKeoBuaBao;
    private byte keoBuaBaoPlayer;
    private byte keoBuaBaoServer;
    private static final int MAX_KBB_HISTORY_SIZE = 5;
    private LinkedList<Byte> recentKBBPlayerChoices = new LinkedList<>();

    public boolean isBaseMenu() {
        return this.indexMenu == ConstNpc.BASE_MENU;
    }

    public void dispose() {
        if (this.shopOpen != null) {
            this.shopOpen.dispose();
            this.shopOpen = null;
        }
        this.npcChose = null;
        this.tagNameShop = null;
        this.playerTrade = null;
        this.npcXD = null;
        this.npcTX = null;
        this.npcBC = null;
        this.zoneKhiGasHuyDiet = null;
        this.tempCardSerial = null;
        this.lastInputText = null;
    }

    public List<Player> getPlayers() {
        return this.relevantPlayers;
    }

    public List<Byte> getRecentPlayerChoices(int historySizeAI) {
        return new ArrayList<>(this.recentKBBPlayerChoices);
    }

    public byte getIdSpaceShip() {
        return this.idSpaceShip;
    }

    public void setIdSpaceShip(byte idSpaceShip) {
        this.idSpaceShip = idSpaceShip;
    }

    public int getTypeChangeMap() {
        return this.typeChangeMap;
    }

    public void setTypeChangeMap(int typeChangeMap) {
        this.typeChangeMap = typeChangeMap;
    }

    public void addPlayerChoiceHistory(byte choice) {
        if (this.recentKBBPlayerChoices == null) {
            this.recentKBBPlayerChoices = new LinkedList<>();
        }
        this.recentKBBPlayerChoices.addLast(choice);
        if (this.recentKBBPlayerChoices.size() > MAX_KBB_HISTORY_SIZE) {
            this.recentKBBPlayerChoices.removeFirst();
        }
    }

}