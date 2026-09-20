
using Assets.Scripts.Mod;
using Assets.src.g;
using Mod.XMAP;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using UnityEngine;

public class Panel : IActionListener, IChatable
{
    public class PlayerChat
    {
        public string name;

        public int charID;

        public bool isNewMessage;

        public List<InfoItem> chats;
    }

    public bool isShow;

    public int X;

    public int Y;

    public int W;

    public int H;

    public int ITEM_HEIGHT;

    public int TAB_W;

    public int TAB_W_NEW;

    public int cmtoY;

    public int cmy;

    public int cmdy;

    public int cmvy;

    public int cmyLim;

    public int xc;

    public int[] cmyLast;

    public int cmtoX;

    public int cmx;

    public int cmxLim;

    public int cmxMap;

    public int cmyMap;

    public int cmxMapLim;

    public int cmyMapLim;

    public int cmyQuest;

    public static Image imgBantay;

    public static Image imgX;

    public static Image imgMap;

    public TabClanIcon tabIcon;

    public MyVector vItemCombine = new MyVector();

    public int moneyGD;

    public int friendMoneyGD;

    public bool isLock;

    public bool isFriendLock;

    public bool isAccept;

    public bool isFriendAccep;

    public string topName;

    public ChatTextField chatTField;

    public static string specialInfo;

    public static short spearcialImage;

    public static Image imgStar;

    public static Image imgMaxStar;

    public static Image imgStar8;

    public static Image imgStar10;

    public static Image imgNew;

    public static Image imgXu;

    public static Image imgThoivang;

    public static Image imgTicket;

    public static Image imgLuong;

    public static Image imgLuongKhoa; 

    private static Image imgUp;

    private static Image imgDown;

    public static Image imgInventoryBackground;

    private static int FrameInventoryGif = 15;
    private static Image[] inventoryBgFrames = new Image[15];

    private static Image[] inventoryBackgroundsByGender = new Image[3];

    private static bool inventoryGifFramesLoaded;

    public static bool isChangeBackground = false; // true = GIF, false = PNG

    private int pa1;

    private int pa2;

    private bool trans;

    private int pX;

    private int pY;

    private Command left = new Command(mResources.SELECT, 0);

    public int type;

    public int currentTabIndex;

    public int startTabPos;

    public int[] lastTabIndex;

    public string[][] currentTabName;

    private int[] currClanOption;

    public int mainTabPos = 4;

    public int shopTabPos = 50;

    public int boxTabPos = 50;

    public string[][] mainTabName;

    public string[] mapNames;

    public string[] planetNames;

    public static string[] strTool = new string[7]
    {
        mResources.gameInfo,
        mResources.change_flag,
        mResources.change_zone,
        mResources.chat_world,
        mResources.account,
        mResources.option,
        mResources.change_account
    };

    public static string[] strCauhinh = new string[4]
    {
        (!GameCanvas.isPlaySound) ? mResources.turnOnSound : mResources.turnOffSound,
        mResources.increase_vga,
        mResources.analog,
        (mGraphics.zoomLevel <= 1) ? mResources.x2Screen : mResources.x1Screen
    };

    public static string[] strModFunc = new string[1] { "" };

    public static string[] strAccount = new string[5]
    {
        mResources.inventory_Pass,
        mResources.friend,
        mResources.enemy,
        mResources.msg,
        mResources.charger
    };

    public static string[] strAuto = new string[1] { mResources.useGem };

    private int hotkeyListeningIndex = -1;

    private KeyCode hotkeyListeningOriginal = KeyCode.None;

    public static int graphics = 0;

    public string[][] shopTabName;

    public int[] maxPageShop;

    public int[] currPageShop;

    private static string[][] boxTabName = new string[2][]
    {
        mResources.chestt,
        mResources.inventory
    };

    private static string[][] boxCombine = new string[2][]
    {
        mResources.combine,
        mResources.inventory
    };

    private static string[][] boxZone = new string[1][] { mResources.zonee };

    private static string[][] boxMap = new string[1][] { mResources.mapp };

    private static string[][] boxGD = new string[3][]
    {
        mResources.inventory,
        mResources.item_give,
        mResources.item_receive
    };

    private static string[][] boxPet = mResources.petMainTab;

    public string[][][] tabName = new string[30][][]
    {
        null,
        null,
        boxTabName,
        boxZone,
        boxMap,
        null,
        null,
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        boxCombine,
        boxGD,
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        boxPet,
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        new string[1][] { new string[1] { string.Empty } },
        boxMod,
        new string[1][] { new string[1] { string.Empty } },
        boxPet,
        new string[1][] { new string[1] { string.Empty } }
    };

    private static readonly string[][] boxMod = new string[4][]
    {
        new string[2] { "Chức", "năng" },
        new string[2] { "Tự", "động" },
        new string[2] { "Hiển", "thị" },
        new string[2] { "Cài", "đặt" }
    };

    private static readonly sbyte BOX_BAG = 0;

    private static readonly sbyte BAG_BOX = 1;

    private static readonly sbyte BODY_BOX = 3;

    private static readonly sbyte BAG_BODY = 4;

    private static readonly sbyte BODY_BAG = 5;

    private static readonly sbyte BAG_PET = 6;

    private static readonly sbyte PET_BAG = 7;

    private static readonly sbyte BAG_PET2 = 8;

    private static readonly sbyte PET2_BAG = 9;

    public int hasUse;

    public int hasUseBag;

    public int currentListLength;

    private int[] lastSelect;

    public static int[] mapIdTraidat = new int[16]
    {
        21, 0, 1, 2, 24, 3, 4, 5, 6, 27,
        28, 29, 30, 42, 47, 46
    };

    public static int[] mapXTraidat = new int[16]
    {
        39, 42, 105, 93, 61, 93, 142, 165, 210, 100,
        165, 220, 233, 10, 125, 125
    };

    public static int[] mapYTraidat = new int[16]
    {
        28, 60, 48, 96, 88, 131, 136, 95, 32, 200,
        189, 167, 120, 110, 20, 20
    };

    public static int[] mapIdNamek = new int[14]
    {
        22, 7, 8, 9, 25, 11, 12, 13, 10, 31,
        32, 33, 34, 43
    };

    public static int[] mapXNamek = new int[14]
    {
        55, 30, 93, 80, 24, 149, 219, 220, 233, 170,
        148, 195, 148, 10
    };

    public static int[] mapYNamek = new int[14]
    {
        136, 84, 69, 34, 25, 42, 32, 110, 192, 70,
        106, 156, 210, 57
    };

    public static int[] mapIdSaya = new int[14]
    {
        23, 14, 15, 16, 26, 17, 18, 20, 19, 35,
        36, 37, 38, 44
    };

    public static int[] mapXSaya = new int[14]
    {
        90, 95, 144, 234, 231, 122, 176, 158, 205, 54,
        105, 159, 231, 27
    };

    public static int[] mapYSaya = new int[14]
    {
        10, 43, 20, 36, 69, 87, 112, 167, 160, 151,
        173, 207, 194, 29
    };

    public static int[][] mapId = new int[3][] { mapIdTraidat, mapIdNamek, mapIdSaya };

    public static int[][] mapX = new int[3][] { mapXTraidat, mapXNamek, mapXSaya };

    public static int[][] mapY = new int[3][] { mapYTraidat, mapYNamek, mapYSaya };

    public Item currItem;

    public Clan currClan;

    public ClanMessage currMess;

    public Member currMem;

    public Clan[] clans;

    public MyVector member;

    public MyVector myMember;

    public MyVector logChat = new MyVector();

    public MyVector vPlayerMenu = new MyVector();

    public MyVector vFriend = new MyVector();

    public MyVector vMyGD = new MyVector();

    public MyVector vFriendGD = new MyVector();

    public MyVector vTop = new MyVector();

    public MyVector vEnemy = new MyVector();

    public MyVector vFlag = new MyVector();

    public MyVector vPlayerMenu_id = new MyVector();

    public Command cmdClose;

    public static bool CanNapTien = false;

    public static int WIDTH_PANEL = 240;

    private int position;

    public string playerChat;

    public Dictionary<string, PlayerChat> chats = new Dictionary<string, PlayerChat>();

    public Char charMenu;

    private bool isThachDau;

    public int typeShop = -1;

    public int xScroll;

    public int yScroll;

    public int wScroll;

    public int hScroll;

    public ChatPopup cp;

    public int idIcon;

    public int[] partID;

    private int timeShow;

    public bool isBoxClan;

    public int w;

    private int pa;

    public int selected;

    private int cSelected;

    private int newSelected;

    private bool isClanOption;

    public bool isSearchClan;

    public bool isMessage;

    public bool isViewMember;

    public const int TYPE_MAIN = 0;

    public const int TYPE_SHOP = 1;

    public const int TYPE_BOX = 2;

    public const int TYPE_ZONE = 3;

    public const int TYPE_MAP = 4;

    public const int TYPE_CLANS = 5;

    public const int TYPE_INFOMATION = 6;

    public const int TYPE_BODY = 7;

    public const int TYPE_MESS = 8;

    public const int TYPE_ARCHIVEMENT = 9;

    public const int PLAYER_MENU = 10;

    public const int TYPE_FRIEND = 11;

    public const int TYPE_COMBINE = 12;

    public const int TYPE_GIAODICH = 13;

    public const int TYPE_MAPTRANS = 14;

    public const int TYPE_TOP = 15;

    public const int TYPE_ENEMY = 16;

    public const int TYPE_KIGUI = 17;

    public const int TYPE_FLAG = 18;

    public const int TYPE_OPTION = 19;

    public const int TYPE_ACCOUNT = 20;

    public const int TYPE_PET_MAIN = 21;

    public const int TYPE_AUTO = 22;

    public const int TYPE_GAMEINFO = 23;

    public const int TYPE_GAMEINFOSUB = 24;

    public const int TYPE_SPEACIALSKILL = 25;

    private int pointerDownTime;

    private int pointerDownFirstX;

    private int[] pointerDownLastX = new int[3];

    private bool tabClickedInPointerDown = false; // Flag để đánh dấu đã click vào tab trong pointerDown

    private bool pointerIsDowning;

    private bool isDownWhenRunning;

    private bool wantUpdateList;

    private int waitToPerform;

    private int cmRun;

    private int keyTouchLock = -1;

    private int keyToundGD = -1;

    private int keyTouchCombine = -1;

    private int keyTouchMapButton = -1;

    public int indexMouse = -1;

    private bool justRelease;

    private int keyTouchTab = -1;

    private int nTableItem;

    public string[][] clansOption = new string[2][]
    {
        mResources.findClan,
        mResources.createClan
    };

    public string clanInfo = string.Empty;

    public string clanReport = string.Empty;

    private bool isHaveClan;

    private Scroll scroll;

    private int cmvx;

    private int cmdx;

    private bool isSelectPlayerMenu;

    private string[] strStatus = new string[6]
    {
        mResources.follow,
        mResources.defend,
        mResources.attack,
        mResources.gohome,
        mResources.fusion,
        mResources.fusionForever
    };

    private static string log;

    private int tt;

    private int currentButtonPress;

    public static long[] t_tiemnang = new long[14]
    {
        50000000L, 250000000L, 1250000000L, 5000000000L, 15000000000L, 30000000000L, 45000000000L, 60000000000L, 75000000000L, 90000000000L,
        110000000000L, 130000000000L, 150000000000L, 170000000000L
    };

    private int[] zoneColor = new int[3] { 43520, 14743570, 14155776 };

    public string[] combineInfo;

    public string[] combineTopInfo;

    public static int[] color1 = new int[3] { 2327248, 8982199, 16713222 };

    public static int[] color2 = new int[3] { 4583423, 16719103, 16714764 };

    private int sellectInventory;

    private Item itemInvenNew;

    private Effect eBanner;

    private static FrameImage screenTab6;

    private bool isUp;

    private int compare;

    public static string strWantToBuy = string.Empty;

    public int xstart;

    public int ystart;

    public int popupW = 140;

    public int popupH = 160;

    public int cmySK;

    public int cmtoYSK;

    public int cmdySK;

    public int cmvySK;

    public int cmyLimSK;

    public int popupY;

    public int popupX;

    public int isborderIndex;

    public int isselectedRow;

    public int indexSize = 28;

    public int indexTitle;

    public int indexSelect;

    public int indexRow = -1;

    public int indexRowMax;

    public int indexMenu;

    public int columns = 6;

    public int rows;

    public int inforX;

    public int inforY;

    public int inforW;

    public int inforH;

    private int yPaint;

    private int xMap;

    private int yMap;

    private int xMapTask;

    private int yMapTask;

    private int xMove;

    private int yMove;

    public static bool isPaintMap = true;

    public bool isClose;

    private int infoSelect;

    public static MyVector vGameInfo = new MyVector(string.Empty);

    public static string[] contenInfo;

    public bool isViewChatServer;

    private int currInfoItem;

    public Char charInfo;

    private bool isChangeZone;

    private bool isKiguiXu;

    private bool isKiguiLuong;

    private int delayKigui;

    public sbyte combineSuccess = -1;

    public int idNPC;

    public int xS;

    public int yS;

    private int rS;

    private int angleS;

    private int angleO;

    private int iAngleS;

    private int iDotS;

    private int speed;

    private int[] xArgS;

    private int[] yArgS;

    private int[] xDotS;

    private int[] yDotS;

    private int time;

    private int typeCombine;

    private int countUpdate;

    private int countR;

    private int countWait;

    private bool isSpeedCombine;

    private bool isCompleteEffCombine = true;

    private bool isPaintCombine;

    public bool isDoneCombine = true;

    public short iconID1;

    public short iconID2;

    public short iconID3;

    public short[] iconID;

    public string[][] speacialTabName;

    public static int[] sizeUpgradeEff = new int[3] { 2, 1, 1 };

    public static int nsize = 1;

    public const sbyte COLOR_WHITE = 0;

    public const sbyte COLOR_GREEN = 1;

    public const sbyte COLOR_PURPLE = 2;

    public const sbyte COLOR_ORANGE = 3;

    public const sbyte COLOR_BLUE = 4;

    public const sbyte COLOR_YELLOW = 5;

    public const sbyte COLOR_RED = 6;

    public const sbyte COLOR_BLACK = 7;

    public static int[][] colorUpgradeEffect = new int[7][]
    {
        new int[6] { 16777215, 15000805, 13487823, 11711155, 9671828, 7895160 },
        new int[6] { 61952, 58624, 52224, 45824, 39168, 32768 },
        new int[6] { 13500671, 12058853, 10682572, 9371827, 7995545, 6684800 },
        new int[6] { 16744192, 15037184, 13395456, 11753728, 10046464, 8404992 },
        new int[6] { 37119, 33509, 28108, 24499, 21145, 17536 },
        new int[6] { 16776192, 15063040, 12635136, 11776256, 10063872, 8290304 },
        new int[6] { 16711680, 15007744, 13369344, 11730944, 10027008, 8388608 }
    };

    public const int color_item_white = 15987701;

    public const int color_item_green = 2786816;

    public const int color_item_purple = 7078041;

    public const int color_item_orange = 12537346;

    public const int color_item_blue = 1269146;

    public const int color_item_yellow = 13279744;

    public const int color_item_red = 11599872;

    public const int color_item_black = 2039326;

    private Image imgo_0;

    private Image imgo_00;

    private Image imgo_1;

    private Image imgo_2;

    private Image imgo_3;

    private Image imgo_4;

    private Image imgo_5;

    private Image imgo_6;

    private Image imgo_7;

    private Image imgo_8;

    private Image imgo_10;

    private Image imgo_11;

    private Image imgo_12;

    private Image imgo_13;

    private Image imgo_14;

    private Image imgo_15;

    private Image imgo_16;

    private Image imgo_17;

    private Image imgo_18;

    private Image imgo_19;

    private Image imgo_20;

    private Image imgo_21;

    private Image imgo_22;

    private Image imgo_23;

    public const int numItem = 20;

    public const sbyte INVENTORY_TAB = 1;

    public sbyte size_tab;

    private bool isnewInventory;

    private static Image[] bgcam = new Image[8];

    private static Image[] bgdo = new Image[8];

    private static Image[] bgxanhla = new Image[8];

    private static Image[] bgtim = new Image[8];

    private static Image[] bgxanhnhat = new Image[8];

    private static Image[] bgxanhdam = new Image[8];

    private static Image[] effcam = new Image[8];

    private static Image[] effdo = new Image[8];

    private static Image[] effxanhla = new Image[8];

    private static Image[] efftim = new Image[8];

    private static Image[] effxanhnhat = new Image[8];

    private static Image[] effxanhdam = new Image[8];

    private int WidthBoxNew = 28;

    private int CountBoxInRow = 6;

    public static string[][] MenuOption = new string[3][]
    {
        new string[2] { "D.sách", "Item" },
        new string[2] { "Chức", "Năng" },
        new string[2] { "H.Dẫn", "Sử Dụng" }
    };

    public Panel()
    {
        init();
        cmdClose = new Command(string.Empty, this, 1003, null);
        cmdClose.img = GameCanvas.loadImage("/mainImage/myTexture2dbtX.png");
        cmdClose.cmdClosePanel = true;
        currItem = null;
    }

    public static void loadBg()
    {
        imgMap = GameCanvas.loadImage("/img/map" + TileMap.planetID + ".png");
        imgBantay = GameCanvas.loadImage("/mainImage/myTexture2dbantay.png");
        imgX = GameCanvas.loadImage("/mainImage/myTexture2dbtX.png");
        imgXu = GameCanvas.loadImage("/mainImage/myTexture2dimgMoney.png");
        imgThoivang = GameCanvas.loadImage("/mainImage/thoivang.png");
        imgLuong = GameCanvas.loadImage("/mainImage/myTexture2dimgDiamond.png");
        imgLuongKhoa = GameCanvas.loadImage("/mainImage/luongkhoa.png"); // commented since luongkhoa not used for now
        imgUp = GameCanvas.loadImage("/mainImage/myTexture2dup.png");
        imgDown = GameCanvas.loadImage("/mainImage/myTexture2ddown.png");
        imgStar = GameCanvas.loadImage("/mainImage/star.png");
        imgMaxStar = GameCanvas.loadImage("/mainImage/starE.png");
        imgStar8 = GameCanvas.loadImage("/mainImage/star8.png");
        imgStar10 = GameCanvas.loadImage("/mainImage/star10.png");
        imgNew = GameCanvas.loadImage("/mainImage/new.png");
        imgTicket = GameCanvas.loadImage("/mainImage/ticket12.png");
        LoadInventoryBackground();
        loadEff();
    }

    public static void setInventoryBackground(string imagePath)
    {
        imgInventoryBackground = GameCanvas.loadImage(imagePath);
    }

    public static void setInventoryBackground(Image image)
    {
        imgInventoryBackground = image;
    }

    public static void LoadInventoryBackground()
    {
        int playerGender = Char.myCharz().cgender;
        string backgroundPath = "/mainImage/inventd.png"; 
        if (playerGender == 0)
        {
            backgroundPath = "/mainImage/inventd.png";  // Gender 0: Trái Đất
        }
        else if (playerGender == 1)
        {
            backgroundPath = "/mainImage/invennm.png";  // Gender 1: Namek
        }
        else if (playerGender == 2)
        {
            backgroundPath = "/mainImage/invenxd.png";  // Gender 3: Xayda
        }

        if (isChangeBackground)
        {
            // Chỉ tạo Texture2D một lần. Hàm này được gọi từ paint inventory.
            if (!inventoryGifFramesLoaded)
            {
                for (int i = 0; i < FrameInventoryGif; i++)
                {
                    inventoryBgFrames[i] = GameCanvas.loadImage("/mainImage/bgfr/" + (i + 1) + ".jpeg");
                }
                inventoryGifFramesLoaded = true;
            }
            if (inventoryBgFrames[0] != null)
            {
                imgInventoryBackground = inventoryBgFrames[0];
            }
        }
        else
        {
            int genderIndex = playerGender >= 0 && playerGender < inventoryBackgroundsByGender.Length ? playerGender : 0;
            if (inventoryBackgroundsByGender[genderIndex] == null)
            {
                inventoryBackgroundsByGender[genderIndex] = GameCanvas.loadImage(backgroundPath);
            }
            imgInventoryBackground = inventoryBackgroundsByGender[genderIndex];
        }
    }

    // Method để xóa background
    public static void clearInventoryBackground()
    {
        imgInventoryBackground = null;
        inventoryBackgroundsByGender = new Image[3];
        inventoryGifFramesLoaded = false;
        for (int i = 0; i < FrameInventoryGif; i++)
        {
            inventoryBgFrames[i] = null;
        }
        for (int i = 0; i < 8; i++)
        {
            if (bgcam[i] == null)
            {
                bgcam[i] = GameCanvas.loadEffect("/cam/bg/" + i);
            }
            if (bgdo[i] == null)
            {
                bgdo[i] = GameCanvas.loadEffect("/do/bg/" + i);
            }
            if (bgtim[i] == null)
            {
                bgtim[i] = GameCanvas.loadEffect("/tim/bg/" + i);
            }
            if (bgxanhdam[i] == null)
            {
                bgxanhdam[i] = GameCanvas.loadEffect("/xanhdam/bg/" + i);
            }
            if (bgxanhla[i] == null)
            {
                bgxanhla[i] = GameCanvas.loadEffect("/xanhla/bg/" + i);
            }
            if (bgxanhnhat[i] == null)
            {
                bgxanhnhat[i] = GameCanvas.loadEffect("/xanhnhat/bg/" + i);
            }
            if (effcam[i] == null)
            {
                effcam[i] = GameCanvas.loadEffect("/cam/eff/" + i);
            }
            if (effdo[i] == null)
            {
                effdo[i] = GameCanvas.loadEffect("/do/eff/" + i);
            }
            if (efftim[i] == null)
            {
                efftim[i] = GameCanvas.loadEffect("/tim/eff/" + i);
            }
            if (effxanhdam[i] == null)
            {
                effxanhdam[i] = GameCanvas.loadEffect("/xanhdam/eff/" + i);
            }
            if (effxanhla[i] == null)
            {
                effxanhla[i] = GameCanvas.loadEffect("/xanhla/eff/" + i);
            }
            if (effxanhnhat[i] == null)
            {
                effxanhnhat[i] = GameCanvas.loadEffect("/xanhnhat/eff/" + i);
            }
        }
    }

    private void paintEffectItem(mGraphics g, Item item, int x, int y)
    {
        if (ModFunc.isEffectInven)
        {
            return;
        }
        try
        {
            Image[] bg = null;
            Image[] eff = null;
            if (item == null || item.itemOption == null)
            {
                return;
            }
            ItemOption[] itemOption = item.itemOption;
            foreach (ItemOption option in itemOption)
            {
                if (option != null && (option.optionTemplate.id == 72 || (option.optionTemplate.id >= 42 && option.optionTemplate.id <= 46)))
                {
                    switch (option.param)
                    {
                        case 1:
                            eff = effxanhnhat;
                            break;
                        case 2:
                            bg = bgxanhnhat;
                            eff = effxanhnhat;
                            break;
                        case 3:
                            eff = effxanhla;
                            break;
                        case 4:
                            bg = bgxanhla;
                            eff = effxanhla;
                            break;
                        case 5:
                            eff = efftim;
                            break;
                        case 6:
                            bg = bgtim;
                            eff = efftim;
                            break;
                        case 7:
                            eff = effcam;
                            break;
                        case 8:
                            bg = bgcam;
                            eff = effcam;
                            break;
                        case 9:
                            eff = effdo;
                            break;
                        case 10:
                            bg = bgdo;
                            eff = effdo;
                            break;
                        default:
                            bg = bgdo;
                            eff = effdo;
                            break;
                    }
                    if (eff != null)
                    {
                        g.drawImage(eff[GameCanvas.gameTick / 4 % 7], x + 2, y + 2);
                    }
                    if (bg != null)
                    {
                        g.drawImage(bg[GameCanvas.gameTick / 4 % 7], x - 1, y - 1);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            Debug.LogException(exception);
        }
    }

    public void init()
    {
        pX = GameCanvas.pxLast + cmxMap;
        pY = GameCanvas.pyLast + cmyMap;
        lastTabIndex = new int[tabName.Length];
        for (int i = 0; i < lastTabIndex.Length; i++)
        {
            lastTabIndex[i] = -1;
        }
    }

    public int getXMap()
    {
        for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
        {
            if (TileMap.mapID == mapId[TileMap.planetID][i])
            {
                return mapX[TileMap.planetID][i];
            }
        }
        return -1;
    }

    public int getYMap()
    {
        for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
        {
            if (TileMap.mapID == mapId[TileMap.planetID][i])
            {
                return mapY[TileMap.planetID][i];
            }
        }
        return -1;
    }

    public int getXMapTask()
    {
        if (Char.myCharz().taskMaint == null)
        {
            return -1;
        }
        for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
        {
            if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
            {
                return mapX[TileMap.planetID][i];
            }
        }
        return -1;
    }

    public int getYMapTask()
    {
        if (Char.myCharz().taskMaint == null)
        {
            return -1;
        }
        for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
        {
            if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
            {
                return mapY[TileMap.planetID][i];
            }
        }
        return -1;
    }

    private void setType(int position)
    {
        typeShop = -1;
        W = WIDTH_PANEL;
        H = GameCanvas.h;
        X = 0;
        Y = 0;
        ITEM_HEIGHT = 24;
        this.position = position;
        switch (position)
        {
            case 0:
                xScroll = 2;
                yScroll = 80;
                wScroll = W - 4;
                hScroll = H - 96;
                cmx = wScroll;
                cmtoX = 0;
                X = 0;
                break;
            case 1:
                wScroll = W - 4;
                xScroll = GameCanvas.w - wScroll;
                yScroll = 80;
                hScroll = H - 96;
                X = xScroll - 2;
                cmx = -(GameCanvas.w + W);
                cmtoX = GameCanvas.w - W;
                break;
            case 23:
            case 24:
                currentTabIndex = 0;
                loadTabModFunc();
                break;
        }
        TAB_W = W / 5 - 1;
        currentTabIndex = 0;
        currentTabName = tabName[type];
        if (currentTabName.Length < 5)
        {
            TAB_W += 5;
        }
        startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
        lastSelect = new int[currentTabName.Length];
        cmyLast = new int[currentTabName.Length];
        for (int i = 0; i < currentTabName.Length; i++)
        {
            lastSelect[i] = (GameCanvas.isTouch ? (-1) : 0);
        }
        if (lastTabIndex[type] != -1)
        {
            currentTabIndex = lastTabIndex[type];
        }
        if (currentTabIndex < 0)
        {
            currentTabIndex = 0;
        }
        if (currentTabIndex > currentTabName.Length - 1)
        {
            currentTabIndex = currentTabName.Length - 1;
        }
        scroll = null;
    }

    public void setTypeMapTrans()
    {
        type = 14;
        setType(0);
        setTabMapTrans();
        cmx = (cmtoX = 0);
    }

    public void setTypeMap()
    {
        if (!GameScr.gI().isMapFize() && isPaintMap)
        {
            if (Hint.isOnTask(2, 0))
            {
                Hint.isViewMap = true;
                GameScr.info1.addInfo(mResources.go_to_quest, 0);
            }
            if (Hint.isOnTask(3, 0))
            {
                Hint.isViewPotential = true;
            }
            type = 4;
            currentTabName = tabName[type];
            startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
            cmx = (cmtoX = 0);
            setTabMap();
        }
    }

    public void setTypeArchivement()
    {
        currentListLength = Char.myCharz().arrArchive.Length;
        setType(0);
        type = 9;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = 0);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public void setTypeKiGuiOnly()
    {
        type = 17;
        setType(1);
        setTabKiGui();
        typeShop = 2;
        currentTabIndex = 0;
    }

    public void setTabKiGui()
    {
        ITEM_HEIGHT = 29;
        currentListLength = Char.myCharz().arrItemShop[4].Length;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public void setTypeBodyOnly()
    {
        type = 7;
        setType(1);
        setTabInventory(resetSelect: true);
        currentTabIndex = 0;
    }

    public void addChatMessage(InfoItem info)
    {
        logChat.insertElementAt(info, 0);
        if (logChat.size() > 20)
        {
            logChat.removeElementAt(logChat.size() - 1);
        }
    }

    public void setTabPlayerMenu()
    {
        ITEM_HEIGHT = 29;
        currentListLength = vPlayerMenu.size();
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public void setTypeFlag()
    {
        type = 18;
        setType(0);
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        setTabFlag();
    }

    public void setTabFlag()
    {
        currentListLength = vFlag.size();
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        if (selected > currentListLength - 1)
        {
            selected = currentListLength - 1;
        }
        cmx = (cmtoX = 0);
    }

    public void setTypePlayerMenu(Char c)
    {
        type = 10;
        setType(0);
        setTabPlayerMenu();
        charMenu = c;
    }

    public void setTypeFriend()
    {
        type = 11;
        setType(0);
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        setTabFriend();
    }

    public void setTypeEnemy()
    {
        type = 16;
        setType(0);
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        setTabEnemy();
    }

    public void setTypeTop(sbyte t)
    {
        type = 15;
        setType(0);
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        setTabTop();
        isThachDau = t != 0;
    }

    public void setTabTop()
    {
        currentListLength = vTop.size();
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        if (selected > currentListLength - 1)
        {
            selected = currentListLength - 1;
        }
        cmx = (cmtoX = 0);
    }

    public void setTabFriend()
    {
        currentListLength = vFriend.size();
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        if (selected > currentListLength - 1)
        {
            selected = currentListLength - 1;
        }
        cmx = (cmtoX = 0);
    }

    public void setTabEnemy()
    {
        currentListLength = vEnemy.size();
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        if (selected > currentListLength - 1)
        {
            selected = currentListLength - 1;
        }
        cmx = (cmtoX = 0);
    }

    public void setTypeMessage()
    {
        type = 8;
        setType(0);
        setTabMessage();
        currentTabIndex = 0;
    }

    public void setTypeShop(int typeShop)
    {
        type = 1;
        setType(0);
        setTabShop();
        currentTabIndex = 0;
        this.typeShop = typeShop;
    }

    public void setTypeBox()
    {
        type = 2;
        if (GameCanvas.w > 2 * WIDTH_PANEL)
        {
            boxTabName = new string[1][] { mResources.chestt };
        }
        else
        {
            boxTabName = new string[2][]
            {
                mResources.chestt,
                mResources.inventory
            };
        }
        tabName[2] = boxTabName;
        setType(0);
        if (currentTabIndex == 0)
        {
            setTabBox();
        }
        if (currentTabIndex == 1)
        {
            setTabInventory(resetSelect: true);
        }
        if (GameCanvas.w > 2 * WIDTH_PANEL)
        {
            GameCanvas.panel2 = new Panel();
            GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
            GameCanvas.panel2.setTypeBodyOnly();
            GameCanvas.panel2.show();
        }
    }

    public void setTypeCombine()
    {
        type = 12;
        if (GameCanvas.w > 2 * WIDTH_PANEL)
        {
            boxCombine = new string[1][] { mResources.combine };
        }
        else
        {
            boxCombine = new string[2][]
            {
                mResources.combine,
                mResources.inventory
            };
        }
        tabName[type] = boxCombine;
        setType(0);
        if (currentTabIndex == 0)
        {
            setTabCombine();
        }
        if (currentTabIndex == 1)
        {
            setTabInventory(resetSelect: true);
        }
        if (GameCanvas.w > 2 * WIDTH_PANEL)
        {
            GameCanvas.panel2 = new Panel();
            GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
            GameCanvas.panel2.setTypeBodyOnly();
            GameCanvas.panel2.show();
        }
        combineSuccess = -1;
        isDoneCombine = true;
    }

    public void setTabCombine()
    {
        currentListLength = vItemCombine.size() + 1;
        ITEM_HEIGHT = 29;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 9;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public void setTypeAuto()
    {
        type = 22;
        setType(0);
        setTabAuto();
        cmx = (cmtoX = 0);
    }

    private void setTabAuto()
    {
        currentListLength = strAuto.Length;
        ITEM_HEIGHT = 29;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    public void setTypePetMain()
    {
        type = 21;
        if (GameCanvas.panel2 != null)
        {
            boxPet = mResources.petMainTab2;
        }
        else
        {
            boxPet = mResources.petMainTab;
        }
        tabName[21] = boxPet;
        if (Char.myCharz().cgender == 1)
        {
            strStatus = new string[6]
            {
                mResources.follow,
                mResources.defend,
                mResources.attack,
                mResources.gohome,
                mResources.fusion,
                mResources.fusionForever
            };
        }
        else
        {
            strStatus = new string[5]
            {
                mResources.follow,
                mResources.defend,
                mResources.attack,
                mResources.gohome,
                mResources.fusion
            };
        }
        setType(2);

        // --- ĐOẠN SỬA ĐỔI TẠI setTypePetMain ---
        if (currentTabIndex == 0)
        {
            // Gọi Skill trước để kiểm tra dữ liệu/vị trí cuộn, sau đó gọi Inventory để chốt tổng độ dài danh sách chính xác nhất
            setTabPetSkill(isPet2: false); 
            setTabPetInventory(isPet2: false); 
        }
        else if (currentTabIndex == 1)
        {
            setTabPetStatus();
        }
        else if (currentTabIndex == 2)
        {
            setTabInventory(resetSelect: true);
        }
    }

    public void setTypePet2Main()
    {
        type = 28;
        if (GameCanvas.panel2 != null)
        {
            boxPet = mResources.petMainTab2;
        }
        else
        {
            boxPet = mResources.petMainTab;
        }
        tabName[28] = boxPet;
        if (Char.myCharz().cgender == 1)
        {
            strStatus = new string[6]
            {
                mResources.follow,
                mResources.defend,
                mResources.attack,
                mResources.gohome,
                mResources.fusion,
                mResources.fusionForever
            };
        }
        else
        {
            strStatus = new string[5]
            {
                mResources.follow,
                mResources.defend,
                mResources.attack,
                mResources.gohome,
                mResources.fusion
            };
        }
        setType(2);
        if (currentTabIndex == 0)
        {
            setTabPetInventory(isPet2: true);
        }
        else if (currentTabIndex == 1)
        {
            setTabPetSkill(isPet2: true);
        }
        else if (currentTabIndex == 2)
        {
            setTabPetStatus();
        }
        else if (currentTabIndex == 3)
        {
            setTabInventory(resetSelect: true);
        }
    }

    public void setTypeMain()
    {
        type = 0;
        setType(0);
        if (currentTabIndex == 1)
        {
            setTabInventory(resetSelect: true);
        }
        if (currentTabIndex == 2)
        {
            setTabSkill();
        }
        if (currentTabIndex == 3)
        {
            if (mainTabName.Length == 4)
            {
                setTabTool();
            }
            else
            {
                setTabClans();
            }
        }
        if (currentTabIndex == 4)
        {
            setTabTool();
        }
    }

    public void setTypeZone()
    {
        type = 3;
        setType(0);
        setTabZone();
        cmx = (cmtoX = 0);
    }

    public void addItemDetail(Item item)
    {
        try
        {
            cp = new ChatPopup();
            string empty = string.Empty;
            string text = string.Empty;
            if (item.template.gender != Char.myCharz().cgender)
            {
                if (item.template.gender == 0)
                {
                    text = text + "\n|7|1|" + mResources.from_earth;
                }
                else if (item.template.gender == 1)
                {
                    text = text + "\n|7|1|" + mResources.from_namec;
                }
                else if (item.template.gender == 2)
                {
                    text = text + "\n|7|1|" + mResources.from_sayda;
                }
            }
            _ = mFont.tahoma_7b_dark;
            string text2 = string.Empty;
            if (item.itemOption != null)
            {
                for (int i = 0; i < item.itemOption.Length; i++)
                {
                    if (item.itemOption[i].optionTemplate.id == 72)
                    {
                        text2 = " [+" + item.itemOption[i].param + "]";
                    }
                    if (item.itemOption[i].optionTemplate.id == 225)
                    {
                        text2 = " [+" + item.itemOption[i].param + "]";
                    }
                }
            }
            string itemDisplayName = item.template.name;
            if (ModFunc.isInventory && ModFunc.isShowID)
            {
                itemDisplayName = "[" + item.template.id + "] " + item.template.name;
            }
            bool flag = false;
            if (item.itemOption != null)
            {
                for (int j = 0; j < item.itemOption.Length; j++)
                {
                    if (item.itemOption[j].optionTemplate.id == 225)
                    {
                        flag = true;
                        if (item.itemOption[j].param >= 1 && item.itemOption[j].param <= 2)
                        {
                            text = text + "|0|1|" + itemDisplayName + text2;
                        }
                        if (item.itemOption[j].param >= 3 && item.itemOption[j].param <= 4)
                        {
                            text = text + "|2|1|" + itemDisplayName + text2;
                        }
                        if (item.itemOption[j].param >= 5 && item.itemOption[j].param <= 6)
                        {
                            text = text + "|8|1|" + itemDisplayName + text2;
                        }
                        if (item.itemOption[j].param >= 7 && item.itemOption[j].param <= 10)
                        {
                            text = text + "|7|1|" + itemDisplayName + text2;
                        }
                    }
                    if (item.itemOption[j].optionTemplate.id == 72)
                    {
                        flag = true;
                        if (item.itemOption[j].param >= 1 && item.itemOption[j].param <= 5)
                        {
                            text = text + "|2|1|" + itemDisplayName + text2;
                        }
                        if (item.itemOption[j].param >= 6 && item.itemOption[j].param <= 7)
                        {
                            text = text + "|8|1|" + itemDisplayName + text2;
                        }
                        if (item.itemOption[j].param >= 8 && item.itemOption[j].param <= 10)
                        {
                            text = text + "|7|1|" + itemDisplayName + text2;
                        }
                    }
                }
            }
            if (!flag)
            {
                text = text + "|0|1|" + itemDisplayName + text2;
            }
            if (item.itemOption != null)
            {
                for (int k = 0; k < item.itemOption.Length; k++)
                {
                    if (item.itemOption[k].optionTemplate.name.StartsWith("$") ? true : false)
                    {
                        empty = item.itemOption[k].getOptiongColor();
                        if (item.itemOption[k].param == 1)
                        {
                            text = text + "\n|1|1|" + empty;
                        }
                        if (item.itemOption[k].param == 0)
                        {
                            text = text + "\n|0|1|" + empty;
                        }
                        continue;
                    }
                    empty = item.itemOption[k].getOptionString();
                    if (!empty.Equals(string.Empty) && item.itemOption[k].optionTemplate.id != 72)
                    {
                        if (item.itemOption[k].optionTemplate.id == 102)
                        {
                            cp.starSlot = (sbyte)item.itemOption[k].param;
                            Res.outz("STAR SLOT= " + cp.starSlot);
                        }
                        else if (item.itemOption[k].optionTemplate.id == 107)
                        {
                            cp.maxStarSlot = (sbyte)item.itemOption[k].param;
                            Res.outz("STAR SLOT= " + cp.maxStarSlot);
                        }
                        else
                        {
                            text = text + "\n|1|1|" + empty;
                        }
                    }
                }
            }
            if (currItem.template.strRequire > 1)
            {
                string text3 = mResources.pow_request + ": " + currItem.template.strRequire;
                if (currItem.template.strRequire > Char.myCharz().cPower)
                {
                    text = text + "\n|3|1|" + text3;
                    string text4 = text;
                    text = text4 + "\n|3|1|" + mResources.your_pow + ": " + Char.myCharz().cPower;
                }
                else
                {
                    text = text + "\n|6|1|" + text3;
                }
            }
            else
            {
                text += "\n|6|1|";
            }
            currItem.compare = getCompare(currItem);
            text += "\n--";
            text = text + "\n|6|" + item.template.description;
            if (!item.reason.Equals(string.Empty))
            {
                if (!item.template.description.Equals(string.Empty))
                {
                    text += "\n--";
                }
                text = text + "\n|2|" + item.reason;
            }
            if (cp.maxStarSlot > 0)
            {
                text += "\n\n";
            }
            popUpDetailInit(cp, text);
            idIcon = item.template.iconID;
            partID = null;
            charInfo = null;
        }
        catch (Exception ex)
        {
            Res.outz("ex " + ex.StackTrace);
        }
    }

    public void popUpDetailInit(ChatPopup cp, string chat)
    {
        cp.isClip = false;
        cp.sayWidth = 180;
        // Align popup inside panel: left panel center, right panel right-aligned
        int paddingRight = 8;
        if (X != 0)
        {
            cp.cx = X + W - cp.sayWidth - paddingRight;
        }
        else
        {
            cp.cx = X + (W - cp.sayWidth) / 2;
        }
        cp.isFromPanel = true;
        cp.says = mFont.tahoma_7_red.splitFontArray(chat, cp.sayWidth - 10);
        cp.delay = 10000000;
        cp.c = null;
        cp.sayRun = 7;
        cp.ch = 15 - cp.sayRun + cp.says.Length * 12 + 10;
        if (cp.ch > GameCanvas.h - 80)
        {
            cp.ch = GameCanvas.h - 80;
            cp.lim = cp.says.Length * 12 - cp.ch + 17;
            if (cp.lim < 0)
            {
                cp.lim = 0;
            }
            ChatPopup.cmyText = 0;
            cp.isClip = true;
        }
        // Y position: place above menu buttons
        cp.cy = GameCanvas.menu.menuY - cp.ch;
        while (cp.cy < 10)
        {
            cp.cy++;
            GameCanvas.menu.menuY++;
        }
        cp.mH = 0;
        cp.strY = 10;
    }

    public void addMessageDetail(ClanMessage cm)
    {
        cp = new ChatPopup();
        string text = "|0|" + cm.playerName;
        text = text + "\n|1|" + Member.getRole(cm.role);
        for (int i = 0; i < myMember.size(); i++)
        {
            Member member = (Member)myMember.elementAt(i);
            if (cm.playerId == member.ID)
            {
                string text2 = text;
                text = text2 + "\n|5|" + mResources.clan_capsuledonate + ": " + member.clanPoint;
                text2 = text;
                text = text2 + "\n|5|" + mResources.clan_capsuleself + ": " + member.curClanPoint;
                text2 = text;
                text = text2 + "\n|4|" + mResources.give_pea + ": " + member.donate + mResources.time;
                text2 = text;
                text = text2 + "\n|4|" + mResources.receive_pea + ": " + member.receive_donate + mResources.time;
                partID = new int[3] { member.head, member.leg, member.body };
                break;
            }
        }
        text += "\n--";
        for (int j = 0; j < cm.chat.Length; j++)
        {
            text = text + "\n" + cm.chat[j];
        }
        if (cm.type == 1)
        {
            string text3 = text;
            text = text3 + "\n|6|" + mResources.received + " " + cm.recieve + "/" + cm.maxCap;
        }
        popUpDetailInit(cp, text);
        charInfo = null;
    }

    public void addThachDauDetail(TopInfo t)
    {
        string text = "|0|1|" + t.name;
        text = text + "\n|1|Top " + t.rank;
        text = text + "\n|1|" + t.info;
        text = text + "\n|2|" + t.info2;
        cp = new ChatPopup();
        popUpDetailInit(cp, text);
        partID = new int[3] { t.headID, t.leg, t.body };
        currItem = null;
        charInfo = null;
    }

    public void addClanMemberDetail(Member m)
    {
        string text = "|0|1|" + m.name;
        string text2 = "\n|2|1|";
        if (m.role == 0)
        {
            text2 = "\n|7|1|";
        }
        if (m.role == 1)
        {
            text2 = "\n|1|1|";
        }
        if (m.role == 2)
        {
            text2 = "\n|0|1|";
        }
        text = text + text2 + Member.getRole(m.role);
        string text3 = text;
        text = text3 + "\n|2|1|" + mResources.power + ": " + m.powerPoint;
        text += "\n--";
        text3 = text;
        text = text3 + "\n|5|" + mResources.clan_capsuledonate + ": " + m.clanPoint;
        text3 = text;
        text = text3 + "\n|5|" + mResources.clan_capsuleself + ": " + m.curClanPoint;
        text3 = text;
        text = text3 + "\n|4|" + mResources.give_pea + ": " + m.donate + mResources.time;
        text3 = text;
        text = text3 + "\n|4|" + mResources.receive_pea + ": " + m.receive_donate + mResources.time;
        text3 = text;
        text = text3 + "\n|6|" + mResources.join_date + ": " + m.joinTime;
        cp = new ChatPopup();
        popUpDetailInit(cp, text);
        partID = new int[3] { m.head, m.leg, m.body };
        currItem = null;
        charInfo = null;
    }

    public void addClanDetail(Clan cl)
    {
        try
        {
            string text = "|0|" + cl.name;
            string[] array = mFont.tahoma_7_green.splitFontArray(cl.slogan, wScroll - 60);
            for (int i = 0; i < array.Length; i++)
            {
                text = text + "\n|2|" + array[i];
            }
            text += "\n--";
            string text2 = text;
            text = text2 + "\n|7|" + mResources.clan_leader + ": " + cl.leaderName;
            text2 = text;
            text = text2 + "\n|1|" + mResources.power_point + ": " + cl.powerPoint;
            text2 = text;
            text = text2 + "\n|4|" + mResources.member + ": " + cl.currMember + "/" + cl.maxMember;
            text2 = text;
            text = text2 + "\n|4|" + mResources.level + ": " + cl.level;
            text2 = text;
            text = text2 + "\n|4|" + mResources.clan_birthday + ": " + NinjaUtil.getDate(cl.date);
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            idIcon = ClanImage.getClanImage((short)cl.imgID).idImage[0];
            currItem = null;
        }
        catch (Exception ex)
        {
            Res.outz("Throw  exception " + ex.StackTrace);
        }
    }

    public void addSkillDetail(SkillTemplate tp, Skill skill, Skill nextSkill)
    {
        string text = "|0|" + tp.name;
        for (int i = 0; i < tp.description.Length; i++)
        {
            text = text + "\n|4|" + tp.description[i];
        }
        text += "\n--";
        if (skill != null)
        {
            string text2 = text;
            text = text2 + "\n|2|" + mResources.cap_do + ": " + skill.point;
            text = text + "\n|5|" + NinjaUtil.Replace(tp.damInfo, "#", skill.damage + string.Empty);
            text2 = text;
            text = text2 + "\n|5|" + mResources.KI_consume + skill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
            text2 = text;
            text = text2 + "\n|5|" + mResources.cooldown + ": " + skill.strTimeReplay() + "s";
            text += "\n--";
            if (skill.point == tp.maxPoint)
            {
                text = text + "\n|0|" + mResources.max_level_reach;
            }
            else
            {
                if (!skill.template.isSkillSpec())
                {
                    text2 = text;
                    text = text2 + "\n|1|" + mResources.next_level_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
                }
                text = text + "\n|4|" + NinjaUtil.Replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
            }
        }
        else
        {
            text = text + "\n|2|" + mResources.not_learn;
            string text3 = text;
            text = text3 + "\n|1|" + mResources.learn_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
            text = text + "\n|4|" + NinjaUtil.Replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
            text3 = text;
            text = text3 + "\n|4|" + mResources.KI_consume + nextSkill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
            text3 = text;
            text = text3 + "\n|4|" + mResources.cooldown + ": " + nextSkill.strTimeReplay() + "s";
        }
        currItem = null;
        partID = null;
        charInfo = null;
        cp = new ChatPopup();
        popUpDetailInit(cp, text);
        idIcon = 0;
    }

    public void show()
    {
        if (GameCanvas.isTouch)
        {
            cmdClose.x = 182;
            cmdClose.y = 0;
        }
        else
        {
            cmdClose.x = GameCanvas.w - 19;
            cmdClose.y = GameCanvas.h - 19;
        }
        cmdClose.isPlaySoundButton = false;
        ChatPopup.currChatPopup = null;
        InfoDlg.hide();
        timeShow = 20;
        isShow = true;
        isClose = false;
        SoundMn.gI().panelOpen();
        if (isTypeShop())
        {
            Char.myCharz().setPartOld();
        }
    }

    public void chatTFUpdateKey()
    {
        if (chatTField != null && chatTField.isShow)
        {
            if (chatTField.left != null && (GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(chatTField.left)) && chatTField.left != null)
            {
                chatTField.left.performAction();
            }
            if (chatTField.right != null && (GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(chatTField.right)) && chatTField.right != null)
            {
                chatTField.right.performAction();
            }
            if (chatTField.center != null && (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(chatTField.center)) && chatTField.center != null)
            {
                chatTField.center.performAction();
            }
            if (chatTField.isShow && GameCanvas.keyAsciiPress != 0)
            {
                chatTField.keyPressed(GameCanvas.keyAsciiPress);
                GameCanvas.keyAsciiPress = 0;
            }
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
        }
    }

    public void updateKey()
    {
        if ((chatTField != null && chatTField.isShow) || !GameCanvas.panel.isDoneCombine || InfoDlg.isShow)
        {
            return;
        }
        if (tabIcon != null && tabIcon.isShow)
        {
            tabIcon.updateKey();
        }
        else
        {
            if (isClose || !isShow)
            {
                return;
            }
            if (cmdClose.isPointerPressInside())
            {
                cmdClose.performAction();
                return;
            }
            if (GameCanvas.keyPressed[13])
            {
                if (type != 4)
                {
                    hide();
                    return;
                }
                setTypeMain();
                cmx = (cmtoX = 0);
            }
            if (GameCanvas.keyPressed[12] || GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
            {
                if (left.idAction > 0)
                {
                    perform(left.idAction, left.p);
                }
                else
                {
                    waitToPerform = 2;
                }
            }
            if (Equals(GameCanvas.panel) && GameCanvas.panel2 == null && GameCanvas.isPointerJustRelease && !GameCanvas.isPointer(X, Y, W, H) && !pointerIsDowning)
            {
                hide();
                return;
            }
            if (!isClanOption)
            {
                updateKeyInTabBar();
            }
            switch (type)
            {
                case 3:
                case 8:
                case 9:
                case 10:
                case 11:
                case 14:
                case 15:
                case 16:
                case 18:
                case 23:
                case 24:
                case 26:
                case 27:
                case 32:
                case 33:
                    updateKeyScrollView();
                    break;
                case 21:
                case 28:
                    if (currentTabIndex == 0 || currentTabIndex == 2)
                    {
                        updateKeyScrollView();
                    }
                    else if (currentTabIndex == 1)
                    {
                        updateKeyPetStatus();
                    }
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        updateKeyQuest();
                        GameCanvas.clearKeyPressed();
                        return;
                    }
                    if (currentTabIndex == 1)
                    {
                        updateKeyInventory();
                    }
                    if (currentTabIndex == 2)
                    {
                        updateKeySkill();
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length == 4)
                        {
                            updateKeyTool();
                        }
                        else
                        {
                            updateKeyClans();
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        updateKeyTool();
                    }
                    break;
                case 2:
                    updateKeyInventory();
                    break;
                case 1:
                case 17:
                case 25:
                    if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                    {
                        updateKeyScrollView();
                    }
                    else if (typeShop == 0)
                    {
                        updateKeyInventory();
                    }
                    else
                    {
                        updateKeyScrollView();
                    }
                    break;
                case 4:
                    updateKeyMap();
                    GameCanvas.clearKeyPressed();
                    return;
                case 7:
                    updateKeyInventory();
                    break;
                case 12:
                    updateKeyCombine();
                    break;
                case 13:
                    updateKeyGiaoDich();
                    break;
                case 19:
                    updateKeyOption();
                    break;
                case 20:
                    updateKeyOption();
                    break;
                case 22:
                    updateKeyAuto();
                    break;
                case 29:
                    updateKeyHotkeySettings();
                    break;
            }
            GameCanvas.clearKeyHold();
            for (int i = 0; i < GameCanvas.keyPressed.Length; i++)
            {
                GameCanvas.keyPressed[i] = false;
            }
        }
    }

    private void updateKeyHotkeySettings()
    {
        if (hotkeyListeningIndex >= 0)
        {
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
            return;
        }
        updateKeyScrollView();
    }

    private void updateKeyPetStatus()
    {
        updateKeyScrollView();
    }

    private void keyGiaodich()
    {
        if (ModFunc.isInventory)
        {
            updateKeyGiaoDichGrid(currentTabIndex == 1);
            return;
        }
        updateKeyScrollView();
    }

    private void updateKeyAuto()
    {
    }

    public void setTypeHotkeySettings()
    {
        type = 29;
        setType(0);
        ModFunc.GI().BeginHotkeyDraft();
        hotkeyListeningIndex = -1;
        setTabHotkeySettings();
        cmx = (cmtoX = 0);
    }

    private void setTabHotkeySettings()
    {
        currentListLength = ModFunc.hotkeyDefinitions.Length + 3;
        ITEM_HEIGHT = 24;
        selected = GameCanvas.isTouch ? -1 : 0;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = 0);
    }

    public bool HandleHotkeyKeyDown(KeyCode key)
    {
        if (!isShow || type != 29 || hotkeyListeningIndex < 0)
        {
            return false;
        }
        if (key == KeyCode.Escape)
        {
            ModFunc.GI().SetDraftHotkey(hotkeyListeningIndex, hotkeyListeningOriginal);
            hotkeyListeningIndex = -1;
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
            return true;
        }
        if (!ModFunc.IsAssignableHotkey(key))
        {
            return true;
        }
        ModFunc.GI().SetDraftHotkey(hotkeyListeningIndex, key);
        hotkeyListeningIndex = -1;
        GameCanvas.clearKeyHold();
        GameCanvas.clearKeyPressed();
        return true;
    }

    private int getGiaoDichGridSelection(bool isMe, int pointerX, int contentY)
    {
        MyVector tradeItems = isMe ? vMyGD : vFriendGD;
        int itemCount = tradeItems.size();
        int itemRows = (itemCount + 5) / 6;
        int relativeY = contentY - yScroll;
        if (pointerX < xScroll || pointerX >= xScroll + wScroll || relativeY < 0)
        {
            return -1;
        }
        if (relativeY < itemRows * ITEM_HEIGHT)
        {
            int relativeX = pointerX - xScroll;
            int column = relativeX / 33;
            if (column < 0 || column >= 6 || relativeX % 33 >= 32)
            {
                return -1;
            }
            int itemIndex = relativeY / ITEM_HEIGHT * 6 + column;
            return itemIndex < itemCount ? itemIndex : -1;
        }

        int controlRow = (relativeY - itemRows * ITEM_HEIGHT) / ITEM_HEIGHT;
        if (controlRow < 0 || controlRow > 2 || (!isMe && controlRow > 1))
        {
            return -1;
        }
        return itemCount + controlRow;
    }

    private int getGiaoDichGridSelectedY(int itemCount)
    {
        int itemRows = (itemCount + 5) / 6;
        int row = (selected < itemCount) ? selected / 6 : itemRows + selected - itemCount;
        return row * ITEM_HEIGHT;
    }

    private void updateKeyGiaoDichGrid(bool isMe)
    {
        MyVector tradeItems = isMe ? vMyGD : vFriendGD;
        int itemCount = tradeItems.size();
        currentListLength = itemCount + 3;
        int itemRows = (itemCount + 5) / 6;
        cmyLim = (itemRows + 3) * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }

        int maxSelect = itemCount + (isMe ? 2 : 1);
        bool moved = false;
        if (selected < 0 && !GameCanvas.isTouch)
        {
            selected = 0;
        }
        if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
        {
            moved = true;
            if (selected > itemCount)
            {
                selected--;
            }
            else if (selected == itemCount)
            {
                selected = (itemCount > 0) ? itemCount - 1 : maxSelect;
            }
            else if (selected >= 6)
            {
                selected -= 6;
            }
            else
            {
                selected = maxSelect;
            }
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22])
        {
            moved = true;
            if (selected < itemCount && selected + 6 < itemCount)
            {
                selected += 6;
            }
            else if (selected < itemCount)
            {
                selected = itemCount;
            }
            else if (selected < maxSelect)
            {
                selected++;
            }
            else
            {
                selected = 0;
            }
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23] && selected < itemCount)
        {
            moved = true;
            if (selected % 6 > 0)
            {
                selected--;
            }
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24] && selected < itemCount)
        {
            moved = true;
            if (selected % 6 < 5 && selected + 1 < itemCount)
            {
                selected++;
            }
        }

        if (selected > maxSelect)
        {
            selected = maxSelect;
        }
        if (moved)
        {
            lastSelect[currentTabIndex] = selected;
            cmtoY = getGiaoDichGridSelectedY(itemCount) - hScroll / 2;
            if (cmtoY < 0) cmtoY = 0;
            if (cmtoY > cmyLim) cmtoY = cmyLim;
            cmy = cmtoY;
        }

        if (GameCanvas.isPointerDown)
        {
            if (!pointerIsDowning && GameCanvas.isPointer(xScroll, yScroll, wScroll, hScroll))
            {
                pointerIsDowning = true;
                pointerDownFirstX = GameCanvas.py;
                for (int i = 0; i < pointerDownLastX.Length; i++)
                {
                    pointerDownLastX[i] = GameCanvas.py;
                }
                pointerDownTime = 0;
                isDownWhenRunning = cmRun != 0;
                cmRun = 0;
            }
            else if (pointerIsDowning)
            {
                pointerDownTime++;
                int deltaY = GameCanvas.py - pointerDownLastX[0];
                if (deltaY != 0)
                {
                    selected = -1;
                    for (int i = pointerDownLastX.Length - 1; i > 0; i--)
                    {
                        pointerDownLastX[i] = pointerDownLastX[i - 1];
                    }
                    pointerDownLastX[0] = GameCanvas.py;
                    cmtoY -= deltaY;
                    if (cmtoY < 0) cmtoY = 0;
                    if (cmtoY > cmyLim) cmtoY = cmyLim;
                    if (cmy < 0 || cmy > cmyLim)
                    {
                        deltaY /= 2;
                    }
                    cmy -= deltaY;
                }
            }
        }

        if (GameCanvas.isPointerJustRelease && pointerIsDowning)
        {
            bool isTap = Res.abs(GameCanvas.py - pointerDownFirstX) < 20 && !isDownWhenRunning;
            if (isTap)
            {
                selected = getGiaoDichGridSelection(isMe, GameCanvas.px, GameCanvas.py + cmy);
                if (selected >= 0)
                {
                    lastSelect[currentTabIndex] = selected;
                    waitToPerform = 1;
                    SoundMn.gI().panelClick();
                }
            }
            if (cmy < 0)
            {
                cmtoY = 0;
            }
            else if (cmy > cmyLim)
            {
                cmtoY = cmyLim;
            }
            else if (!isTap)
            {
                int momentum = GameCanvas.py - pointerDownLastX[0]
                    + (pointerDownLastX[0] - pointerDownLastX[1])
                    + (pointerDownLastX[1] - pointerDownLastX[2]);
                momentum = (momentum > 10) ? 10 : ((momentum < -10) ? -10 : 0);
                cmRun = -momentum * 100;
            }
            else
            {
                cmtoY = cmy;
            }
            pointerIsDowning = false;
            pointerDownTime = 0;
            GameCanvas.isPointerJustRelease = false;
        }
    }

    private void updateKeyGiaoDich()
    {
        if (currentTabIndex == 0)
        {
            if (Equals(GameCanvas.panel))
            {
                updateKeyInventory();
            }
            if (Equals(GameCanvas.panel2))
            {
                keyGiaodich();
            }
        }
        if (currentTabIndex == 1 || currentTabIndex == 2)
        {
            keyGiaodich();
        }
    }

    private void updateKeyTool()
    {
        updateKeyScrollView();
    }

    private void updateKeySkill()
    {
        updateKeyScrollView();
    }

    public void setTabGiaoDich(bool isMe)
    {
        currentListLength = ((!isMe) ? (vFriendGD.size() + 3) : (vMyGD.size() + 3));
        ITEM_HEIGHT = 29;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        int contentRows = currentListLength;
        if (ModFunc.isInventory)
        {
            int itemCount = (!isMe) ? vFriendGD.size() : vMyGD.size();
            contentRows = (itemCount + 5) / 6 + 3;
        }
        cmyLim = contentRows * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    public void setTypeGiaoDich(Char cGD)
    {
        type = 13;
        tabName[type] = boxGD;
        isAccept = false;
        isLock = false;
        isFriendLock = false;
        vMyGD.removeAllElements();
        vFriendGD.removeAllElements();
        moneyGD = 0;
        friendMoneyGD = 0;
        if (GameCanvas.w > 2 * WIDTH_PANEL)
        {
            GameCanvas.panel2 = new Panel();
            GameCanvas.panel2.type = 13;
            GameCanvas.panel2.tabName[type] = new string[1][] { mResources.item_receive };
            GameCanvas.panel2.setType(1);
            GameCanvas.panel2.setTabGiaoDich(isMe: false);
            GameCanvas.panel.tabName[type] = new string[2][]
            {
                mResources.inventory,
                mResources.item_give
            };
            GameCanvas.panel2.show();
            GameCanvas.panel2.charMenu = cGD;
        }
        if (Equals(GameCanvas.panel))
        {
            setType(0);
        }
        if (currentTabIndex == 0)
        {
            setTabInventory(resetSelect: true);
        }
        if (currentTabIndex == 1)
        {
            setTabGiaoDich(isMe: true);
        }
        if (currentTabIndex == 2)
        {
            setTabGiaoDich(isMe: false);
        }
        charMenu = cGD;
    }

    private void paintGiaoDich(mGraphics g, bool isMe)
    {
        if (ModFunc.isInventory)
        {
            paintGiaoDichGrid(g, isMe);
            return;
        }

        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        MyVector myVector = ((!isMe) ? vFriendGD : vMyGD);
        for (int i = 0; i < currentListLength; i++)
        {
            int num = xScroll + 29;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = wScroll - 29;
            int num4 = ITEM_HEIGHT - 1;
            int num5 = xScroll;
            int num6 = yScroll + i * ITEM_HEIGHT;
            int num7 = ITEM_HEIGHT - 1;
            int num8 = ITEM_HEIGHT - 1;
            if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            if (i == currentListLength - 1)
            {
                if (!isMe)
                {
                    continue;
                }
                g.setColor(15196114);
                g.fillRect(num5, num2, wScroll, num4);
                if (!isLock)
                {
                    if (!isFriendLock)
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                }
                else if (isFriendLock)
                {
                    g.setColor(15196114);
                    g.fillRect(num5, num2, wScroll, num4);
                    g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                    ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.done, xScroll + wScroll - 22, num2 + 7, 2);
                    mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                }
                continue;
            }
            if (i == currentListLength - 2)
            {
                if (isMe)
                {
                    g.setColor(15196114);
                    g.fillRect(num5, num2, wScroll, num4);
                    if (!isAccept)
                    {
                        if (!isLock)
                        {
                            g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                            ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.mlock, xScroll + wScroll - 22, num2 + 7, 2);
                            mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.not_lock_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                        }
                        else
                        {
                            g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                            ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.CANCEL, xScroll + wScroll - 22, num2 + 7, 2);
                            mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                        }
                    }
                }
                else if (!isFriendLock)
                {
                    mFont.tahoma_7b_dark.drawString(g, mResources.not_lock_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7b_dark.drawString(g, mResources.locked_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                }
                continue;
            }
            if (i == currentListLength - 3)
            {
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                }
                g.fillRect(num, num2, num3, num4);
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 9993045 : 7300181);
                }
                g.fillRect(num5, num6, num7, num8);
                g.drawImage(imgXu, num5 + num7 / 2, num6 + num8 / 2, 3);
                mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((!isMe) ? friendMoneyGD : moneyGD) + " " + mResources.XU, num + 5, num2 + 11, 0);
                mFont.tahoma_7_green.drawString(g, mResources.money_trade, num + 5, num2, 0);
                continue;
            }
            if (myVector.size() == 0)
            {
                return;
            }
            if (isLock)
            {
                g.setColor(13748667);
            }
            else
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
            }
            g.fillRect(num, num2, num3, num4);
            if (isLock)
            {
                g.setColor(13748667);
            }
            else
            {
                g.setColor((i != selected) ? 9993045 : 9541120);
            }
            Item item = (Item)myVector.elementAt(i);
            if (item != null)
            {
                for (int j = 0; j < item.itemOption.Length; j++)
                {
                    if (item.itemOption[j].optionTemplate.id != 72 || item.itemOption[j].param <= 0)
                    {
                        continue;
                    }
                    sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                    if (GetColor_ItemBg(color_Item_Upgrade) != -1)
                    {
                        if (isLock)
                        {
                            g.setColor(13748667);
                        }
                        else
                        {
                            g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                        }
                    }
                }
            }
            g.fillRect(num5, num6, num7, num8);
            if (item == null)
            {
                continue;
            }
            string text = string.Empty;
            mFont mFont2 = mFont.tahoma_7_green2;
            if (item.itemOption != null)
            {
                for (int k = 0; k < item.itemOption.Length; k++)
                {
                    if (item.itemOption[k].optionTemplate.id == 72)
                    {
                        text = " [+" + item.itemOption[k].param + "]";
                    }
                    if (item.itemOption[k].optionTemplate.id == 225)
                    {
                        text = " [+" + item.itemOption[k].param + "]";
                    }
                    if (item.itemOption[k].optionTemplate.id == 225)
                    {
                        if (item.itemOption[k].param >= 1 && item.itemOption[k].param <= 2)
                        {
                            mFont2 = GetFont(0);
                        }
                        else if (item.itemOption[k].param >= 3 && item.itemOption[k].param <= 4)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[k].param >= 5 && item.itemOption[k].param <= 6)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[k].param >= 7 && item.itemOption[k].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                    if (item.itemOption[k].optionTemplate.id == 72)
                    {
                        if (item.itemOption[k].param >= 1 && item.itemOption[k].param <= 5)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[k].param >= 6 && item.itemOption[k].param <= 7)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[k].param >= 8 && item.itemOption[k].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                }
            }
            if (ModFunc.isShowID)
            {
                mFont2.drawString(g, "[" + item.template.id + "] " + item.template.name + text, num + 5, num2 + 1, 0);
            }
            else
            {
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
            }
            string text2 = string.Empty;
            if (item.itemOption != null)
            {
                if (item.itemOption.Length != 0 && item.itemOption[0] != null)
                {
                    text2 += item.itemOption[0].getOptionString();
                }
                mFont mFont3 = mFont.tahoma_7_blue;
                if (item.compare < 0 && item.template.type != 5)
                {
                    mFont3 = mFont.tahoma_7_red;
                }
                if (item.itemOption.Length > 1)
                {
                    for (int l = 1; l < Math.min(item.itemOption.Length, 3); l++)
                    {
                        if (item.itemOption[l] != null && item.itemOption[l].IsValidOption())
                        {
                            text2 = text2 + ", " + item.itemOption[l].getOptionString();
                        }
                    }
                }
                mFont3.drawString(g, text2, num + 5, num2 + 10, mFont.LEFT);
            }
            SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
            if (item.itemOption != null)
            {
                for (int m = 0; m < item.itemOption.Length; m++)
                {
                    paintOptItemInventory(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num5, num6, num7, num8, item);
                }
                for (int n = 0; n < item.itemOption.Length; n++)
                {
                    paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                }
            }
            if (item.quantity > 1)
            {
                mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
            }
        }
        paintScrollArrow(g);
    }

    private void paintGiaoDichGrid(mGraphics g, bool isMe)
    {
        MyVector tradeItems = isMe ? vMyGD : vFriendGD;
        int itemCount = tradeItems.size();
        int itemRows = (itemCount + 5) / 6;
        int contentRows = itemRows + 3;
        currentListLength = itemCount + 3;
        cmyLim = contentRows * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (!pointerIsDowning && cmtoY > cmyLim)
        {
            cmtoY = cmyLim;
        }

        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);

        const int columns = 6;
        const int slotWidth = 32;
        int slotHeight = ITEM_HEIGHT - 1;
        for (int i = 0; i < itemCount; i++)
        {
            int slotX = xScroll + i % columns * (slotWidth + 1);
            int slotY = yScroll + i / columns * ITEM_HEIGHT;
            if (slotY - cmy > yScroll + hScroll || slotY - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }

            Item item = (Item)tradeItems.elementAt(i);
            bool isSelected = i == selected;
            g.setColor(isSelected ? 16383818 : 15723751);
            g.fillRect(slotX, slotY, slotWidth, slotHeight, 4);
            g.setColor(isLock ? 13748667 : (isSelected ? 9541120 : 11837316));
            g.fillRect(slotX + 1, slotY + 1, slotWidth - 2, slotHeight - 2, 3);
            paintEffectItem(g, item, slotX + 1, slotY + 1);

            if (item == null)
            {
                continue;
            }
            SmallImage.drawSmallImage(g, item.template.iconID, slotX + slotWidth / 2, slotY + slotHeight / 2, 0, 3);
            if (item.itemOption != null)
            {
                for (int j = 0; j < item.itemOption.Length; j++)
                {
                    paintOptItemInventory(g, item.itemOption[j].optionTemplate.id, item.itemOption[j].param, slotX, slotY, slotWidth, slotHeight, item);
                }
                for (int j = 0; j < item.itemOption.Length; j++)
                {
                    paintOptSlotItem(g, item.itemOption[j].optionTemplate.id, item.itemOption[j].param, slotX, slotY, slotWidth, slotHeight);
                }
            }
            if (item.quantity > 1)
            {
                string quantity = string.Format("{0:N0}", item.quantity).Replace(",", ".");
                mFont.tahoma_7_yellow.drawString(g, quantity, slotX + slotWidth, slotY + slotHeight - mFont.tahoma_7_yellow.getHeight(), mFont.RIGHT);
            }
        }

        int controlY = yScroll + itemRows * ITEM_HEIGHT;
        paintGiaoDichGridControls(g, isMe, itemCount, controlY);
        paintScrollArrow(g);
    }

    private void paintGiaoDichGridControls(mGraphics g, bool isMe, int itemCount, int controlY)
    {
        int rowHeight = ITEM_HEIGHT - 1;
        int moneyIndex = itemCount;
        int lockIndex = itemCount + 1;
        int doneIndex = itemCount + 2;

        int moneyTextX = xScroll + 29;
        g.setColor(isLock ? 13748667 : ((selected == moneyIndex) ? 16383818 : 15196114));
        g.fillRect(moneyTextX, controlY, wScroll - 29, rowHeight);
        g.setColor(isLock ? 13748667 : ((selected == moneyIndex) ? 7300181 : 9993045));
        g.fillRect(xScroll, controlY, 28, rowHeight);
        g.drawImage(imgXu, xScroll + 14, controlY + rowHeight / 2, 3);
        mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(isMe ? moneyGD : friendMoneyGD) + " " + mResources.XU, moneyTextX + 5, controlY + 11, mFont.LEFT);
        mFont.tahoma_7_green.drawString(g, mResources.money_trade, moneyTextX + 5, controlY, mFont.LEFT);

        int lockY = controlY + ITEM_HEIGHT;
        g.setColor(15196114);
        g.fillRect(xScroll, lockY, wScroll, rowHeight);
        if (isMe)
        {
            if (!isAccept)
            {
                g.drawImage((selected == lockIndex) ? GameScr.imgLbtnFocus2 : GameScr.imgLbtn2, xScroll + wScroll - 5, lockY + 2, StaticObj.TOP_RIGHT);
                string buttonText = isLock ? mResources.CANCEL : mResources.mlock;
                ((selected == lockIndex) ? mFont.tahoma_7b_green2 : mFont.tahoma_7b_dark).drawString(g, buttonText, xScroll + wScroll - 22, lockY + 7, mFont.CENTER);
                mFont.tahoma_7_grey.drawString(g, mResources.you + (isLock ? mResources.locked_trade : mResources.not_lock_trade), xScroll + 5, lockY + rowHeight / 2 - 4, mFont.LEFT);
            }
        }
        else
        {
            mFont.tahoma_7b_dark.drawString(g, isFriendLock ? mResources.locked_trade_upper : mResources.not_lock_trade_upper, xScroll + wScroll / 2, lockY + rowHeight / 2 - 4, mFont.CENTER);
        }

        if (!isMe)
        {
            return;
        }
        int doneY = lockY + ITEM_HEIGHT;
        g.setColor(15196114);
        g.fillRect(xScroll, doneY, wScroll, rowHeight);
        if (!isLock)
        {
            mFont.tahoma_7_grey.drawString(g, mResources.opponent + (isFriendLock ? mResources.locked_trade : mResources.not_lock_trade), xScroll + wScroll / 2, doneY + rowHeight / 2 - 4, mFont.CENTER);
        }
        else if (isFriendLock)
        {
            g.drawImage((selected == doneIndex) ? GameScr.imgLbtnFocus2 : GameScr.imgLbtn2, xScroll + wScroll - 5, doneY + 2, StaticObj.TOP_RIGHT);
            ((selected == doneIndex) ? mFont.tahoma_7b_green2 : mFont.tahoma_7b_dark).drawString(g, mResources.done, xScroll + wScroll - 22, doneY + 7, mFont.CENTER);
            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + 5, doneY + rowHeight / 2 - 4, mFont.LEFT);
        }
        else
        {
            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, doneY + rowHeight / 2 - 4, mFont.CENTER);
        }
    }

    private void updateKeyMap()
    {
        if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
        {
            yMove -= 5;
            cmyMap = yMove - (yScroll + hScroll / 2);
            if (yMove < yScroll)
            {
                yMove = yScroll;
            }
        }
        if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
        {
            yMove += 5;
            cmyMap = yMove - (yScroll + hScroll / 2);
            if (yMove > yScroll + 200)
            {
                yMove = yScroll + 200;
            }
        }
        if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
        {
            xMove -= 5;
            cmxMap = xMove - wScroll / 2;
            if (xMove < 16)
            {
                xMove = 16;
            }
        }
        if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
        {
            xMove += 5;
            cmxMap = xMove - wScroll / 2;
            if (xMove > 250)
            {
                xMove = 250;
            }
        }
        if (GameCanvas.isPointerDown)
        {
            pointerIsDowning = true;
            if (!trans)
            {
                pa1 = cmxMap;
                pa2 = cmyMap;
                trans = true;
            }
            cmxMap = pa1 + (GameCanvas.pxLast - GameCanvas.px);
            cmyMap = pa2 + (GameCanvas.pyLast - GameCanvas.py);
        }
        if (GameCanvas.isPointerJustRelease)
        {
            trans = false;
            GameCanvas.pxLast = GameCanvas.px;
            GameCanvas.pyLast = GameCanvas.py;
            pX = GameCanvas.pxLast + cmxMap;
            pY = GameCanvas.pyLast + cmyMap;
        }
        if (GameCanvas.isPointerClick)
        {
            pointerIsDowning = false;
        }
        if (cmxMap < 0)
        {
            cmxMap = 0;
        }
        if (cmxMap > cmxMapLim)
        {
            cmxMap = cmxMapLim;
        }
        if (cmyMap < 0)
        {
            cmyMap = 0;
        }
        if (cmyMap > cmyMapLim)
        {
            cmyMap = cmyMapLim;
        }
    }

    private void updateKeyCombine()
    {
        if (currentTabIndex == 0)
        {
            updateKeyScrollView();
            keyTouchCombine = -1;
            if (selected == vItemCombine.size() && GameCanvas.isPointerClick)
            {
                GameCanvas.isPointerClick = false;
                keyTouchCombine = 1;
            }
        }
        if (currentTabIndex == 1)
        {
            updateKeyScrollView();
        }
    }

    private void updateKeyQuest()
    {
        if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
        {
            cmyQuest -= 5;
        }
        if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
        {
            cmyQuest += 5;
        }
        if (cmyQuest < 0)
        {
            cmyQuest = 0;
        }
        int num = indexRowMax * 12 - (hScroll - 60);
        if (num < 0)
        {
            num = 0;
        }
        if (cmyQuest > num)
        {
            cmyQuest = num;
        }
        if (scroll != null)
        {
            if (!GameCanvas.isTouch)
            {
                scroll.cmy = cmyQuest;
            }
            scroll.updateKey();
        }
        int num2 = xScroll + wScroll / 2 - 35;
        int num3 = ((GameCanvas.h <= 300) ? 15 : 20);
        int num4 = yScroll + hScroll - num3 - 15;
        int px = GameCanvas.px;
        int py = GameCanvas.py;
        keyTouchMapButton = -1;
        if (isPaintMap && !GameScr.gI().isMapDocNhan() && px >= num2 && px <= num2 + 70 && py >= num4 && py <= num4 + 30 && (scroll == null || !scroll.pointerIsDowning))
        {
            keyTouchMapButton = 1;
            if (GameCanvas.isPointerJustRelease)
            {
                SoundMn.gI().buttonClick();
                waitToPerform = 2;
                GameCanvas.clearAllPointerEvent();
            }
        }
    }

    private void getCurrClanOtion()
    {
        isClanOption = false;
        if (type != 0 || mainTabName.Length != 5 || currentTabIndex != 3)
        {
            return;
        }
        isClanOption = false;
        if (selected == 0)
        {
            currClanOption = new int[clansOption.Length];
            for (int i = 0; i < currClanOption.Length; i++)
            {
                currClanOption[i] = i;
            }
            if (!isViewMember)
            {
                isClanOption = true;
            }
        }
        else if (selected != 1 && !isSearchClan && selected > 0)
        {
            currClanOption = new int[1];
            for (int j = 0; j < currClanOption.Length; j++)
            {
                currClanOption[j] = j;
            }
            isClanOption = true;
        }
    }

    private void updateKeyClansOption()
    {
        if (currClanOption == null)
        {
            return;
        }
        if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
        {
            currMess = getCurrMessage();
            cSelected--;
            if (selected == 0 && cSelected < 0)
            {
                cSelected = currClanOption.Length - 1;
            }
            if (selected > 1 && isMessage && currMess.option != null && cSelected < 0)
            {
                cSelected = currMess.option.Length - 1;
            }
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
        {
            currMess = getCurrMessage();
            cSelected++;
            if (selected == 0 && cSelected > currClanOption.Length - 1)
            {
                cSelected = 0;
            }
            if (selected > 1 && isMessage && currMess.option != null && cSelected > currMess.option.Length - 1)
            {
                cSelected = 0;
            }
        }
    }

    private void updateKeyClans()
    {
        updateKeyScrollView();
        updateKeyClansOption();
    }

    private void checkOptionSelect()
    {
        try
        {
            if (type != 0 || currentTabIndex != 3 || mainTabName.Length != 5 || selected == -1)
            {
                return;
            }
            int num = 0;
            if (selected == 0)
            {
                num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                int tabEndX = num + clansOption.Length * TAB_W;
                // Kiểm tra click có nằm trong vùng tab hợp lệ không
                if (GameCanvas.px < num || GameCanvas.px >= tabEndX)
                {
                    cSelected = -1;
                    return;
                }
                cSelected = (GameCanvas.px - num) / TAB_W;
                // Đảm bảo cSelected nằm trong phạm vi hợp lệ
                if (cSelected < 0 || cSelected >= clansOption.Length)
                {
                    cSelected = -1;
                }
            }
            else
            {
                currMess = getCurrMessage();
                if (currMess != null && currMess.option != null)
                {
                    num = xScroll + wScroll - 2 - currMess.option.Length * 40;
                    int optionEndX = num + currMess.option.Length * 40;
                    // Kiểm tra click có nằm trong vùng option hợp lệ không
                    if (GameCanvas.px < num || GameCanvas.px >= optionEndX)
                    {
                        cSelected = -1;
                        return;
                    }
                    cSelected = (GameCanvas.px - num) / 40;
                    // Đảm bảo cSelected nằm trong phạm vi hợp lệ
                    if (cSelected < 0 || cSelected >= currMess.option.Length)
                    {
                        cSelected = -1;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Res.outz("Throw err " + ex.StackTrace);
        }
    }

    public void updateScroolMouse(int a)
    {
        bool flag = false;
        if (GameCanvas.pxMouse > wScroll)
        {
            return;
        }
        if (indexMouse == -1)
        {
            indexMouse = selected;
        }
        if (a > 0)
        {
            indexMouse -= a;
            flag = true;
        }
        else if (a < 0)
        {
            indexMouse += -a;
            flag = true;
        }
        if (indexMouse < 0)
        {
            indexMouse = 0;
        }
        if (flag)
        {
            cmtoY = indexMouse * 12;
            if (cmtoY > cmyLim)
            {
                cmtoY = cmyLim;
            }
            if (cmtoY < 0)
            {
                cmtoY = 0;
            }
        }
    }

    private void updateKeyScrollView222222()
    {
        if (currentListLength <= 0)
        {
            return;
        }
        bool flag = false;
        if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
        {
            flag = true;
            if (isTabInven() && isnewInventory)
            {
                if (selected > 0 && sellectInventory == 0)
                {
                    selected--;
                }
            }
            else
            {
                selected--;
                if (type == 24 || type == 27)
                {
                    selected -= 2;
                    if (selected < 0)
                    {
                        selected = 0;
                    }
                }
                if (selected < 0)
                {
                    if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                    {
                        InfoDlg.showWait();
                        if (currPageShop[currentTabIndex] <= 0)
                        {
                            Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                        }
                        else
                        {
                            Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                        }
                        return;
                    }
                    selected = currentListLength - 1;
                    if (isClanOption)
                    {
                        selected = -1;
                    }
                    if (size_tab > 0)
                    {
                        selected = -1;
                    }
                }
                lastSelect[currentTabIndex] = selected;
                cSelected = 0;
                getCurrClanOtion();
            }
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22])
        {
            flag = true;
            if (isTabInven() && isnewInventory)
            {
                if (selected < 1 && sellectInventory == 0)
                {
                    selected++;
                }
            }
            else
            {
                selected++;
                if (type == 24 || type == 27)
                {
                    selected += 2;
                    if (selected > currentListLength - 1)
                    {
                        selected = currentListLength - 1;
                    }
                }
                else if (selected > currentListLength - 1)
                {
                    // For shop tabs, check against actual array length instead of currentListLength
                    int maxSelect = currentListLength - 1;
                    if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                    {
                        if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                        {
                            maxSelect = Char.myCharz().arrItemShop[currentTabIndex].Length - 1;
                        }
                    }
                    if (selected > maxSelect)
                    {
                        if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                        {
                            InfoDlg.showWait();
                            if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                            }
                            else
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                            }
                            return;
                        }
                        selected = 0;
                    }
                }
                lastSelect[currentTabIndex] = selected;
                cSelected = 0;
                getCurrClanOtion();
            }
        }
        if (isnewInventory && GameCanvas.keyPressed[5] && itemInvenNew != null)
        {
            pointerDownTime = 0;
            waitToPerform = 2;
        }
        if (flag)
        {
            cmtoY = selected * ITEM_HEIGHT - hScroll / 2;
            if (cmtoY > cmyLim)
            {
                cmtoY = cmyLim;
            }
            if (cmtoY < 0)
            {
                cmtoY = 0;
            }
            cmy = cmtoY;
        }
        if (GameCanvas.isPointerDown)
        {
            justRelease = false;
            if (!pointerIsDowning && GameCanvas.isPointer(xScroll, yScroll, wScroll, hScroll))
            {
                if (isTabInven() && size_tab > 0)
                {
                    int tabYStart = 79; 
                    int tabYEnd = 100; 

                    if (GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd &&
                        GameCanvas.px >= xScroll && GameCanvas.px < xScroll + wScroll)
                    {
                        Item[] arrItemBodyCheck = Char.myCharz().arrItemBody;
                        Item[] arrItemBagCheck = Char.myCharz().arrItemBag;
                        int numTabs = (arrItemBodyCheck.Length + arrItemBagCheck.Length) / 20 + (((arrItemBodyCheck.Length + arrItemBagCheck.Length) % 20 > 0) ? 1 : 0);
                        int tabWidth = (numTabs > 0) ? (wScroll / numTabs) : wScroll;

                        int tabClickX = GameCanvas.px - xScroll;
                        int clickedTab = tabClickX / tabWidth;

                        if (clickedTab >= 0 && clickedTab < size_tab)
                        {
                            int oldNewSelectedEarly = newSelected;
                            newSelected = clickedTab;
                            selected = -1;
                            checkOptionSelect();
                            pointerDownTime = 0;
                            waitToPerform = 0; 
                            SoundMn.gI().panelClick();
                            tabClickedInPointerDown = true;
                        }
                    }
                }

                for (int i = 0; i < pointerDownLastX.Length; i++)
                {
                    pointerDownLastX[0] = GameCanvas.py;
                }
                pointerDownFirstX = GameCanvas.py;
                tabClickedInPointerDown = false;
                pointerIsDowning = true;
                isDownWhenRunning = cmRun != 0;
                cmRun = 0;
            }
            else if (pointerIsDowning)
            {
                pointerDownTime++;
                if (pointerDownTime > 5 && pointerDownFirstX == GameCanvas.py && !isDownWhenRunning)
                {
                    if (tabClickedInPointerDown)
                    {
                        return; 
                    }
                    pointerDownFirstX = -1000;
                    selected = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                    if (selected >= currentListLength)
                    {
                        selected = -1;
                    }
                    checkOptionSelect();
                }
                else
                {
                    indexMouse = -1;
                }
                int num = GameCanvas.py - pointerDownLastX[0];
                if (num != 0 && selected != -1)
                {
                    selected = -1;
                    cSelected = -1;
                }
                for (int num2 = pointerDownLastX.Length - 1; num2 > 0; num2--)
                {
                    pointerDownLastX[num2] = pointerDownLastX[num2 - 1];
                }
                pointerDownLastX[0] = GameCanvas.py;
                cmtoY -= num;
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmy < 0 || cmy > cmyLim)
                {
                    num /= 2;
                }
                cmy -= num;
                if (cmy < -(GameCanvas.h / 3))
                {
                    wantUpdateList = true;
                }
                else
                {
                    wantUpdateList = false;
                }
                if (isnewInventory)
                {
                    int num3 = GameCanvas.px - xScroll;
                    int num4 = GameCanvas.py - yScroll;
                    sellectInventory = num4 / 34 * 5 + num3 / 34;
                }
            }
        }
        if (!GameCanvas.isPointerJustRelease || !pointerIsDowning)
        {
            return;
        }
        justRelease = true;
        int i2 = GameCanvas.py - pointerDownLastX[0];
        GameCanvas.isPointerJustRelease = false;
        if (Res.abs(i2) < 20 && Res.abs(GameCanvas.py - pointerDownFirstX) < 20 && !isDownWhenRunning)
        {
            cmRun = 0;
            cmtoY = cmy;
            pointerDownFirstX = -1000;

            if (isTabInven())
            {

                if (tabClickedInPointerDown)
                {
                    return; 
                }
                int tabYStart = 79; 
                int tabYEnd = 100;  // Vị trí kết thúc của tab (89 + 21 - 10)
                if (size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd)
                {
                    // Kiểm tra click có nằm trong vùng X của tab không
                    if (GameCanvas.px >= xScroll && GameCanvas.px < xScroll + wScroll)
                    {
                        Item[] arrItemBodyCheck = Char.myCharz().arrItemBody;
                        Item[] arrItemBagCheck = Char.myCharz().arrItemBag;
                        int numTabs = (arrItemBodyCheck.Length + arrItemBagCheck.Length) / 20 + (((arrItemBodyCheck.Length + arrItemBagCheck.Length) % 20 > 0) ? 1 : 0);
                        int tabWidth = (numTabs > 0) ? (wScroll / numTabs) : wScroll;
                        int tabClickX = GameCanvas.px - xScroll;
                        int clickedTab = tabClickX / tabWidth;
                        if (clickedTab >= 0 && clickedTab < size_tab)
                        {
                            int oldNewSelected = newSelected;
                            newSelected = clickedTab;
                            selected = -1; // Reset selected về -1 để không mở hành trang
                            checkOptionSelect();
                            pointerDownTime = 0;
                            waitToPerform = 0; // Set về 0 để không trigger logic mở hành trang
                            SoundMn.gI().panelClick();
                            tabClickedInPointerDown = true; // Đánh dấu đã click vào tab
                            return; // Return ngay để không xử lý click vào bag items
                        }
                        else
                        {
                            UnityEngine.Debug.Log($"[TAB CLICK pointerDown] FAILED: clickedTab={clickedTab} is out of range [0, {size_tab})");
                        }
                    }
                    else
                    {
                        UnityEngine.Debug.Log($"[TAB CLICK pointerDown] FAILED X check: px={GameCanvas.px} is NOT in range [{xScroll}, {xScroll + wScroll})");
                    }
                }
                else
                {
                    UnityEngine.Debug.Log($"[TAB CLICK pointerDown] FAILED Y check: py={GameCanvas.py} is NOT in range [{tabYStart}, {tabYEnd}) or size_tab={size_tab} <= 0");
                }

                int pX = GameCanvas.px - xScroll;
                int pY = GameCanvas.py - yScroll + cmy;
                int itemBoxWidth = 28;
                int itemBoxHeight = 28;
                int gap = 2;
                int bodyH = itemBoxHeight + gap;
                int leftColX = 2;
                int rightColX = wScroll - itemBoxWidth - 2;
                Item[] arrItemBody = Char.myCharz().arrItemBody;
                int bodyItemsPerColumn = (arrItemBody.Length + 1) / 2;

                selected = -1; // Reset selection
                for (int i = 0; i < 9; i++)
                {
                    int colX = (i < 5) ? leftColX : rightColX;
                    int rowIndex = (i < 5) ? i : (i - 5);
                    int rowY = rowIndex * bodyH; // Tọa độ relative với yScroll

                    if (pX >= colX && pX <= colX + itemBoxWidth &&
                        pY >= rowY && pY <= rowY + itemBoxHeight)
                    {
                        selected = i;
                        break;
                    }
                }

                bool isInTabArea = (size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd);
                if (isInTabArea)
                {
                    return; // Return ngay để không xử lý click vào bag items
                }

                if (selected == -1 && !isInTabArea)
                {
                    int bagStartY = yScroll + bodyItemsPerColumn * (itemBoxHeight + gap) + 10;

                    int clickY = GameCanvas.py;
                    int bagVisibleStartY = bagStartY - cmy;
                    int bagVisibleEndY = bagStartY - cmy + (Char.myCharz().arrItemBag.Length + 5) / 6 * ITEM_HEIGHT;
                    if (clickY >= bagVisibleStartY && clickY < bagVisibleEndY)
                    {
                        int bagClickY = clickY - (bagStartY - cmy);
                        int bagSize = 32; // Kích thước ô hành trang
                        int col = pX / (bagSize + 1);
                        int row = bagClickY / ITEM_HEIGHT;
                        if (col >= 0 && col < 6 && row >= 0) // Chỉ lấy 6 cột
                        {
                            int indexBag = row * 6 + col;
                            int finalIndex = 9 + indexBag; // 9 là số lượng item Body
                            if (indexBag >= 0 && indexBag < Char.myCharz().arrItemBag.Length)
                            {
                                selected = finalIndex;
                            }
                            else
                            {
                                UnityEngine.Debug.Log($"[BAG CLICK pointerDown] FAILED: indexBag={indexBag} is out of range [0, {Char.myCharz().arrItemBag.Length})");
                            }
                        }
                        else
                        {
                            UnityEngine.Debug.Log($"[BAG CLICK pointerDown] FAILED: col={col} or row={row} is out of range");
                        }
                    }
                    else
                    {
                        UnityEngine.Debug.Log($"[BAG CLICK pointerDown] FAILED bag Y check: clickY={clickY} is NOT in range [{bagVisibleStartY}, {bagVisibleEndY})");
                    }
                }
                else
                {
                    if (selected != -1)
                        UnityEngine.Debug.Log($"[BAG CLICK pointerDown] SKIPPED: selected={selected} (body item clicked)");
                    if (isInTabArea)
                        UnityEngine.Debug.Log($"[BAG CLICK pointerDown] SKIPPED: isInTabArea=true (tab area clicked)");
                }
            }
            else if (isTabBox()) // Giữ nguyên logic cũ của Box (Rương đồ)
            {
                int actualRow = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                int sizeItem = 32;
                int column4 = (GameCanvas.px - xScroll) / (sizeItem + 1);
                if (column4 < 0) column4 = 0;
                if (column4 >= CountBoxInRow) column4 = CountBoxInRow - 1;

                selected = actualRow * CountBoxInRow + column4;

                if (selected < 0 || selected >= Char.myCharz().arrItemBox.Length)
                {
                    selected = -1;
                }
            }
            else // Các tab list thường khác (Nhiệm vụ, Chức năng...)
            {
                if (isClanOption && clansOption != null && clansOption.Length > 0)
                {
                    int tabStartX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                    int tabY = yScroll;
                    int tabHeight = 23;
                    if (GameCanvas.py >= tabY && GameCanvas.py < tabY + tabHeight &&
                        GameCanvas.px >= tabStartX && GameCanvas.px < tabStartX + clansOption.Length * TAB_W)
                    {
                        int tabClickX = GameCanvas.px - tabStartX;
                        int clickedTab = tabClickX / TAB_W;
                        if (clickedTab < 0) clickedTab = 0;
                        if (clickedTab >= clansOption.Length) clickedTab = clansOption.Length - 1;
                        cSelected = clickedTab;
                        selected = 0; 
                        if (cSelected == 0)
                        {
                            // Chat bang
                            isMessage = true;
                            isViewMember = false;
                            isSearchClan = false;
                        }
                        else if (cSelected == 1)
                        {
                            // Xin đậu
                            isMessage = false;
                            isViewMember = false;
                            isSearchClan = false;
                        }
                        else if (cSelected == 2)
                        {
                            // Thành viên
                            isMessage = false;
                            isViewMember = true;
                            isSearchClan = false;
                        }
                        initTabClans();
                    }
                    else
                    {
                        // Kiểm tra click có nằm trong vùng hiển thị hợp lệ không
                        if (GameCanvas.px < xScroll || GameCanvas.px >= xScroll + wScroll)
                        {
                            selected = -1;
                        }
                        else
                        {
                            int row4 = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                            selected = row4;
                            if (selected >= currentListLength || selected < 0)
                            {
                                selected = -1;
                            }
                            else if (selected == 0)
                            {
                                // Nếu click vào row 0 (tab row) nhưng ngoài vùng tab, không cho chọn
                                int clanTabX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                                int clanTabEndX = clanTabX + clansOption.Length * TAB_W;
                                if (GameCanvas.px < clanTabX || GameCanvas.px >= clanTabEndX)
                                {
                                    selected = -1;
                                }
                            }
                        }
                    }
                }
                else if (size_tab > 0 && GameCanvas.py >= yScroll && GameCanvas.py < yScroll + 21)
                {
                    int tabClickX = GameCanvas.px - xScroll;
                    int clickedTab = tabClickX / TAB_W_NEW;
                    if (clickedTab >= 0 && clickedTab < size_tab)
                    {
                        newSelected = clickedTab;
                        selected = 0; 
                    }
                }
                else
                {
                    int row4 = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                    selected = row4;
                    // For shop tabs, validate against actual array length instead of currentListLength
                    if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                    {
                        // Shop tab - validate against actual shop array length
                        if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                        {
                            if (selected >= Char.myCharz().arrItemShop[currentTabIndex].Length)
                            {
                                selected = -1;
                            }
                        }
                        else
                        {
                            if (selected >= currentListLength) selected = -1;
                        }
                    }
                    // For other tabs (Top, Friend, Enemy, Archive, etc.), validate against actual size
                    else if (type == 15) // Top tab
                    {
                        if (selected >= vTop.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 11) // Friend tab
                    {
                        if (selected >= vFriend.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 16) // Enemy tab
                    {
                        if (selected >= vEnemy.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 9) // Archive tab
                    {
                        if (Char.myCharz().arrArchive != null && selected >= Char.myCharz().arrArchive.Length)
                        {
                            selected = -1;
                        }
                        else if (selected >= currentListLength)
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 10) // PlayerMenu tab
                    {
                        if (selected >= vPlayerMenu.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 12) // Combine tab
                    {
                        if (selected >= vItemCombine.size() + 1) // +1 for combine button
                        {
                            selected = -1;
                        }
                    }
                    else
                    {
                        if (selected >= currentListLength) selected = -1;
                    }
                }
            }

            checkOptionSelect();
            pointerDownTime = 0;
            waitToPerform = 10;
            SoundMn.gI().panelClick();
        }
        else if (selected != -1 && pointerDownTime > 5)
        {
            pointerDownTime = 0;
            waitToPerform = 1;
        }
        else if (selected == -1 && !isDownWhenRunning)
        {
            if (cmy < 0)
            {
                cmtoY = 0;
            }
            else if (cmy > cmyLim)
            {
                cmtoY = cmyLim;
            }
            else
            {
                int num5 = GameCanvas.py - pointerDownLastX[0] + (pointerDownLastX[0] - pointerDownLastX[1]) + (pointerDownLastX[1] - pointerDownLastX[2]);
                num5 = ((num5 > 10) ? 10 : ((num5 < -10) ? (-10) : 0));
                cmRun = -num5 * 100;
            }
        }
        if (isTabInven() && !ModFunc.isInventory && GameCanvas.py >= yScroll && GameCanvas.py < yScroll + 21
            && Res.abs(GameCanvas.py - pointerDownFirstX) < 10 && pointerDownTime <= 5)
        {
            setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, resetSelect: false);
            selected = -1;
            return;
        }
        pointerIsDowning = false;
        pointerDownTime = 0;
        GameCanvas.isPointerJustRelease = false;
    }

    private void updateKeyScrollView()
    {
        // For shop tabs, ensure currentListLength is always up-to-date with actual array length
        if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
        {
            if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
            {
                int actualLength = Char.myCharz().arrItemShop[currentTabIndex].Length;
                currentListLength = actualLength;
                cmyLim = actualLength * ITEM_HEIGHT - hScroll;
                if (cmyLim < 0)
                {
                    cmyLim = 0;
                }
            }
        }
        // For other tabs, ensure currentListLength is always up-to-date with actual size
        else if (type == 15) // Top tab
        {
            int actualSize = vTop.size();
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        else if (type == 11) // Friend tab
        {
            int actualSize = vFriend.size();
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        else if (type == 16) // Enemy tab
        {
            int actualSize = vEnemy.size();
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        else if (type == 9) // Archive tab
        {
            if (Char.myCharz().arrArchive != null)
            {
                int actualSize = Char.myCharz().arrArchive.Length;
                currentListLength = actualSize;
                cmyLim = actualSize * ITEM_HEIGHT - hScroll;
                if (cmyLim < 0)
                {
                    cmyLim = 0;
                }
            }
        }
        else if (type == 10) // PlayerMenu tab
        {
            int actualSize = vPlayerMenu.size();
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        else if (type == 12) // Combine tab
        {
            int actualSize = vItemCombine.size() + 1; // +1 for combine button
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        if (currentListLength <= 0)
        {
            return;
        }
        if (!ModFunc.isInventory)
        {
            updateKeyScrollView222222();
            return;
        }
        bool flag = false;
        if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
        {
            flag = true;
            if (isTabInven())
            {
                if (selected < 9) // Body
                {
                    if (selected % 5 > 0) // Các hàng trừ hàng đầu
                        selected--;
                }
                else // Bag
                {
                    int bagIndex = selected - 9;
                    if (bagIndex < 6) // Hàng đầu tiên của túi đồ
                    {
                        selected = 5; // Ô cuối cột trái
                    }
                    else
                    {
                        selected -= 6;
                    }
                }
            }
            else
            {
                selected--;
            }

            if (selected < 0)
            {
                if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                {
                    InfoDlg.showWait();
                    if (currPageShop[currentTabIndex] <= 0)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                    }
                    return;
                }
                int maxSelect = currentListLength - 1;
                if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                {
                    if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                    {
                        maxSelect = Char.myCharz().arrItemShop[currentTabIndex].Length - 1;
                    }
                }
                selected = maxSelect;
                if (isTabInven())
                {
                    int maxBagIndex = Char.myCharz().arrItemBag.Length > 0 ? (9 + Char.myCharz().arrItemBag.Length - 1) : 8;
                    selected = (maxBagIndex > 8) ? maxBagIndex : 8;
                }
                else if (isTabBox())
                {
                    selected = Char.myCharz().arrItemBox.Length - 1;
                }
                if (isClanOption)
                {
                    selected = -1;
                }
                if (size_tab > 0)
                {
                    selected = -1;
                }
            }
            lastSelect[currentTabIndex] = selected;
            cSelected = 0;
            getCurrClanOtion();
        }
        else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22]) // DOWN
        {
            flag = true;
            if (isTabInven())
            {
                if (selected < 9) // Body
                {
                    if (selected < 4) // Cột trái, chưa phải hàng cuối
                        selected++;
                    else if (selected == 4) // Cột trái, hàng cuối -> xuống túi đồ
                        selected = 9;
                    else if (selected >= 5 && selected < 8)
                        selected++;
                    else if (selected == 8)
                        selected = 12; 
                }
                else // Bag
                {
                    selected += 6;
                    int maxBagSelect = Char.myCharz().arrItemBag.Length > 0 ? (9 + Char.myCharz().arrItemBag.Length - 1) : 8;
                    if (selected > maxBagSelect)
                    {
                        selected = 0; 
                    }
                }
            }
            else
            {
                selected++;
                if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                {
                    if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                    {
                        if (selected >= Char.myCharz().arrItemShop[currentTabIndex].Length)
                        {
                            selected = 0; 
                        }
                    }
                }
            }
        }
        if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24]) // RIGHT
        {
            if (isTabInven())
            {
                if (selected >= 0 && selected <= 4) // Cột trái
                {
                    selected += 5; // Chuyển sang cột phải
                }
                else if (selected >= 9) // Túi đồ
                {
                    int bagIndex = selected - 9;
                    if (bagIndex % 6 < 5)
                    {
                        selected++;
                    }
                }
            }
            else
            {
                selected++;
                // For shop tabs, ensure selected doesn't exceed actual array length
                if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                {
                    if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                    {
                        if (selected >= Char.myCharz().arrItemShop[currentTabIndex].Length)
                        {
                            selected = 0; // Wrap around to first item
                        }
                    }
                }
                // For other tabs, check against actual size
                else if (type == 15) // Top tab
                {
                    if (selected >= vTop.size())
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
                else if (type == 11) // Friend tab
                {
                    if (selected >= vFriend.size())
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
                else if (type == 16) // Enemy tab
                {
                    if (selected >= vEnemy.size())
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
                else if (type == 9) // Archive tab
                {
                    if (Char.myCharz().arrArchive != null && selected >= Char.myCharz().arrArchive.Length)
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
                else if (type == 10) // PlayerMenu tab
                {
                    if (selected >= vPlayerMenu.size())
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
                else if (type == 12) // Combine tab
                {
                    if (selected >= vItemCombine.size() + 1) // +1 for combine button
                    {
                        selected = 0; // Wrap around to first item
                    }
                }
            }
        }
        if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23]) // LEFT
        {
            if (isTabInven())
            {
                if (selected >= 5 && selected <= 8) // Cột phải
                {
                    selected -= 5; // Chuyển sang cột trái
                }
                else if (selected >= 9) // Túi đồ
                {
                    int bagIndex = selected - 9;
                    if (bagIndex % 6 > 0)
                    {
                        selected--;
                    }
                }
            }
            else
            {
                selected--;
            }
        }
        // Validate selected against actual size for all tabs
        if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
        {
            // Shop tabs
            if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
            {
                int maxSelect = Char.myCharz().arrItemShop[currentTabIndex].Length - 1;
                if (selected > maxSelect)
                {
                    if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                    {
                        InfoDlg.showWait();
                        if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                        {
                            Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                        }
                        else
                        {
                            Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                        }
                        return;
                    }
                    selected = 0;
                }
            }
        }
        else if (type == 15) // Top tab
        {
            if (selected >= vTop.size())
            {
                selected = vTop.size() - 1;
            }
        }
        else if (type == 11) // Friend tab
        {
            if (selected >= vFriend.size())
            {
                selected = vFriend.size() - 1;
            }
        }
        else if (type == 16) // Enemy tab
        {
            if (selected >= vEnemy.size())
            {
                selected = vEnemy.size() - 1;
            }
        }
        else if (type == 9) // Archive tab
        {
            if (Char.myCharz().arrArchive != null && selected >= Char.myCharz().arrArchive.Length)
            {
                selected = Char.myCharz().arrArchive.Length - 1;
            }
            else if (selected >= currentListLength)
            {
                selected = currentListLength - 1;
            }
        }
        else if (type == 10) // PlayerMenu tab
        {
            if (selected >= vPlayerMenu.size())
            {
                selected = vPlayerMenu.size() - 1;
            }
        }
        else if (type == 12) // Combine tab
        {
            if (selected >= vItemCombine.size() + 1) // +1 for combine button
            {
                selected = vItemCombine.size(); // Clamp to combine button
            }
        }
        else if (selected >= 0)
        {
            int maxSelect = Char.myCharz().arrItemBag.Length > 0 ? (12 + Char.myCharz().arrItemBag.Length - 1) : 11;
            if (selected > maxSelect)
            {
                selected = maxSelect;
            }
        }
        else if (selected > currentListLength - 1)
        {
            // For other tabs, check against actual size instead of currentListLength
            int maxSelect = currentListLength - 1;
            if (type == 15) // Top tab
            {
                maxSelect = vTop.size() - 1;
            }
            else if (type == 11) // Friend tab
            {
                maxSelect = vFriend.size() - 1;
            }
            else if (type == 16) // Enemy tab
            {
                maxSelect = vEnemy.size() - 1;
            }
            else if (type == 9) // Archive tab
            {
                if (Char.myCharz().arrArchive != null)
                {
                    maxSelect = Char.myCharz().arrArchive.Length - 1;
                }
            }
            else if (type == 10) // PlayerMenu tab
            {
                maxSelect = vPlayerMenu.size() - 1;
            }
            else if (type == 12) // Combine tab
            {
                maxSelect = vItemCombine.size(); // +1 for combine button, but index is 0-based
            }
            
            if (selected > maxSelect)
            {
                if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                {
                    InfoDlg.showWait();
                    if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                    }
                    return;
                }
                selected = 0;
            }
        }
        lastSelect[currentTabIndex] = selected;
        getCurrClanOtion();
        if (flag)
        {
            int s = selected;
            if (isTabInven())
            {
                if (selected < 9)
                {
                    int itemBoxHeight = 28;
                    int gap = 2;
                    int bodyH = itemBoxHeight + gap;
                    int rowIndex = (selected < 5) ? selected : (selected - 5);
                    s = rowIndex * bodyH / ITEM_HEIGHT;
                }
                else
                {
                    int bagIndex = selected - 9;
                    int bagRow = bagIndex / 6;
                    int itemBoxHeight = 28;
                    int gap = 2;
                    int bodyH = itemBoxHeight + gap;
                    int bodyItemsPerColumn = (Char.myCharz().arrItemBody.Length + 1) / 2;
                    int bodyRows = (bodyItemsPerColumn * bodyH + 10) / ITEM_HEIGHT;
                    s = bodyRows + bagRow;
                }
            }
            else if (isTabBox())
            {
                s = selected / CountBoxInRow;
            }
            cmtoY = s * ITEM_HEIGHT - hScroll / 2;
            if (cmtoY > cmyLim)
            {
                cmtoY = cmyLim;
            }
            if (cmtoY < 0)
            {
                cmtoY = 0;
            }
            cmy = cmtoY;
        }
        if (GameCanvas.isPointerDown)
        {
            justRelease = false;
            if (!pointerIsDowning && GameCanvas.isPointer(xScroll, yScroll, wScroll, hScroll))
            {
                if (isTabInven() && size_tab > 0)
                {
                    int tabYStart = 79; // Vị trí bắt đầu của tab (89 - 10)
                    int tabYEnd = 100;  // Vị trí kết thúc của tab (89 + 21 - 10)

                    if (GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd &&
                        GameCanvas.px >= xScroll && GameCanvas.px < xScroll + wScroll)
                    {
                        // Tính TAB_W_NEW giống như trong paintInventory để đảm bảo đúng
                        Item[] arrItemBodyCheck = Char.myCharz().arrItemBody;
                        Item[] arrItemBagCheck = Char.myCharz().arrItemBag;
                        int numTabs = (arrItemBodyCheck.Length + arrItemBagCheck.Length) / 20 + (((arrItemBodyCheck.Length + arrItemBagCheck.Length) % 20 > 0) ? 1 : 0);
                        int tabWidth = (numTabs > 0) ? (wScroll / numTabs) : wScroll;

                        // Click vào vùng sub-tabs - tính newSelected dựa trên vị trí click
                        int tabClickX = GameCanvas.px - xScroll;
                        int clickedTab = tabClickX / tabWidth;

                        // Validate clickedTab
                        if (clickedTab >= 0 && clickedTab < size_tab)
                        {
                            int oldNewSelectedEarly = newSelected;
                            newSelected = clickedTab;
                            selected = -1; // Reset selected về -1 để không mở hành trang
                            checkOptionSelect();
                            pointerDownTime = 0;
                            waitToPerform = 0;
                            SoundMn.gI().panelClick();
                            tabClickedInPointerDown = true; // Đánh dấu đã click vào tab
                        }
                    }
                }

                for (int i = 0; i < pointerDownLastX.Length; i++)
                {
                    pointerDownLastX[0] = GameCanvas.py;
                }
                pointerDownFirstX = GameCanvas.py;
                tabClickedInPointerDown = false; // Reset flag khi bắt đầu pointer down mới
                pointerIsDowning = true;
                isDownWhenRunning = cmRun != 0;
                cmRun = 0;
            }
            else if (pointerIsDowning)
            {
                pointerDownTime++;
                if (pointerDownTime > CountBoxInRow && pointerDownFirstX == GameCanvas.py && !isDownWhenRunning)
                {
                    if (tabClickedInPointerDown)
                    {
                        return; // Return ngay để không xử lý click vào item
                    }
                    pointerDownFirstX = -1000;

                    if (isTabInven())
                    {
                        int tabYStart = 79;
                        int tabYEnd = 100;  
                        if (size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd)
                        {
                            if (GameCanvas.px >= xScroll && GameCanvas.px < xScroll + wScroll)
                            {
                                Item[] arrItemBodyCheck = Char.myCharz().arrItemBody;
                                Item[] arrItemBagCheck = Char.myCharz().arrItemBag;
                                int numTabs = (arrItemBodyCheck.Length + arrItemBagCheck.Length) / 20 + (((arrItemBodyCheck.Length + arrItemBagCheck.Length) % 20 > 0) ? 1 : 0);
                                int tabWidth = (numTabs > 0) ? (wScroll / numTabs) : wScroll;
                                // Click vào vùng sub-tabs - tính newSelected dựa trên vị trí click
                                int tabClickX = GameCanvas.px - xScroll;
                                int clickedTab = tabClickX / tabWidth;
                                if (clickedTab >= 0 && clickedTab < size_tab)
                                {
                                    newSelected = clickedTab;
                                    selected = -1; // Reset selected về -1 để không mở hành trang
                                    checkOptionSelect();
                                    pointerDownTime = 0;
                                    waitToPerform = 0; // Set về 0 để không trigger logic mở hành trang
                                    SoundMn.gI().panelClick();
                                    return; // Return ngay để không xử lý click vào bag items
                                }
                                else
                                {
                                    UnityEngine.Debug.Log($"[TAB CLICK updateKeyScrollView] FAILED: clickedTab={clickedTab} is out of range [0, {size_tab})");
                                }
                            }
                            else
                            {
                                UnityEngine.Debug.Log($"[TAB CLICK updateKeyScrollView] FAILED X check: px={GameCanvas.px} is NOT in range [{xScroll}, {xScroll + wScroll})");
                            }
                        }
                        else
                        {
                            UnityEngine.Debug.Log($"[TAB CLICK updateKeyScrollView] FAILED Y check: py={GameCanvas.py} is NOT in range [{tabYStart}, {tabYEnd}) or size_tab={size_tab} <= 0");
                        }

                        int pX = GameCanvas.px - xScroll;
                        int pY = GameCanvas.py - yScroll + cmy;

                        // Các thông số giống hệt bên paintInventory
                        int itemBoxWidth = 28;
                        int itemBoxHeight = 28;
                        int gap = 2;
                        int bodyH = itemBoxHeight + gap;

                        int leftColX = 2;
                        int rightColX = wScroll - itemBoxWidth - 2;

                        Item[] arrItemBody = Char.myCharz().arrItemBody;
                        int bodyItemsPerColumn = (arrItemBody.Length + 1) / 2;

                        selected = -1; 

                        for (int i = 0; i < 9; i++)
                        {
                            int colX = (i < 5) ? leftColX : rightColX;
                            int bodyRowIndex = (i < 5) ? i : (i - 5);
                            int rowY = bodyRowIndex * bodyH; // Tọa độ relative với yScroll

                            if (pX >= colX && pX <= colX + itemBoxWidth &&
                                pY >= rowY && pY <= rowY + itemBoxHeight)
                            {
                                selected = i;
                                break;
                            }
                        }
                        if (tabClickedInPointerDown)
                        {
                            return;
                        }
                        if (selected == -1 && !(size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd))
                        {
                            int bagStartY = yScroll + bodyItemsPerColumn * (itemBoxHeight + gap) + 10;
                            int clickY = GameCanvas.py;
                            if (clickY >= bagStartY - cmy && clickY < bagStartY - cmy + (Char.myCharz().arrItemBag.Length + 5) / 6 * ITEM_HEIGHT)
                            {
                                int bagClickY = clickY - (bagStartY - cmy);
                                int bagSize = 32; // Kích thước ô hành trang

                                // Tính dòng và cột
                                int col = pX / (bagSize + 1);
                                int row = bagClickY / ITEM_HEIGHT;

                                if (col >= 0 && col < 6) // Chỉ lấy 6 cột
                                {
                                    int indexBag = row * 6 + col;
                                    int finalIndex = 9 + indexBag; // 9 là số lượng item Body

                                    if (indexBag >= 0 && indexBag < Char.myCharz().arrItemBag.Length)
                                    {
                                        selected = finalIndex;
                                    }
                                }
                            }
                        }
                    }
                    else if (isTabBox())
                    {
                        int actualRow = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                        int sizeItem = 32;
                        int column2 = (GameCanvas.px - xScroll) / (sizeItem + 1);
                        if (column2 < 0) column2 = 0;
                        if (column2 >= CountBoxInRow) column2 = CountBoxInRow - 1;
                        selected = actualRow * CountBoxInRow + column2;
                        if (selected < 0 || selected >= Char.myCharz().arrItemBox.Length)
                        {
                            selected = -1;
                        }
                    }
                    else
                    {
                        if (isClanOption && clansOption != null && clansOption.Length > 0)
                        {
                            int tabStartX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                            int tabY = yScroll;
                            int tabHeight = 23;

                            if (GameCanvas.py >= tabY && GameCanvas.py < tabY + tabHeight &&
                                GameCanvas.px >= tabStartX && GameCanvas.px < tabStartX + clansOption.Length * TAB_W)
                            {
                                int tabClickX = GameCanvas.px - tabStartX;
                                int clickedTab = tabClickX / TAB_W;

                                if (clickedTab < 0) clickedTab = 0;
                                if (clickedTab >= clansOption.Length) clickedTab = clansOption.Length - 1;

                                cSelected = clickedTab;
                                selected = 0;

                                if (cSelected == 0)
                                {
                                    isMessage = true;
                                    isViewMember = false;
                                    isSearchClan = false;
                                }
                                else if (cSelected == 1)
                                {
                                    isMessage = false;
                                    isViewMember = false;
                                    isSearchClan = false;
                                }
                                else if (cSelected == 2)
                                {
                                    isMessage = false;
                                    isViewMember = true;
                                    isSearchClan = false;
                                }
                                initTabClans();
                                return; 
                            }
                        }
                        if (GameCanvas.px < xScroll || GameCanvas.px >= xScroll + wScroll)
                        {
                            selected = -1;
                        }
                        else
                        {
                            int rowIndex = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                            selected = rowIndex;
                            if (selected >= currentListLength || selected < 0)
                            {
                                selected = -1;
                            }
                            if (isClanOption && clansOption != null && selected == 0)
                            {
                                int tabStartX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                                int tabEndX = tabStartX + clansOption.Length * TAB_W;
                                if (GameCanvas.px < tabStartX || GameCanvas.px >= tabEndX)
                                {
                                    selected = -1;
                                }
                            }
                        }
                    }
                    if (isClanOption && selected == -1)
                    {
                        return;
                    }
                    checkOptionSelect();
                }
                else
                {
                    indexMouse = -1;
                }
                int num = GameCanvas.py - pointerDownLastX[0];
                if (num != 0 && selected != -1)
                {
                    selected = -1;
                    cSelected = -1;
                }
                for (int num2 = pointerDownLastX.Length - 1; num2 > 0; num2--)
                {
                    pointerDownLastX[num2] = pointerDownLastX[num2 - 1];
                }
                pointerDownLastX[0] = GameCanvas.py;
                cmtoY -= num;
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmy < 0 || cmy > cmyLim)
                {
                    num /= 2;
                }
                cmy -= num;
                if (cmy < -(GameCanvas.h / 3))
                {
                    wantUpdateList = true;
                }
                else
                {
                    wantUpdateList = false;
                }
            }
        }
        if (!GameCanvas.isPointerJustRelease || !pointerIsDowning)
        {
            return;
        }
        justRelease = true;
        int i2 = GameCanvas.py - pointerDownLastX[0];
        GameCanvas.isPointerJustRelease = false;
        if (Res.abs(i2) < 20 && Res.abs(GameCanvas.py - pointerDownFirstX) < 20 && !isDownWhenRunning)
        {
            cmRun = 0;
            cmtoY = cmy;
            if (tabClickedInPointerDown)
            {
                pointerDownFirstX = -1000;
                return; 
            }

            pointerDownFirstX = -1000;

            if (isTabInven())
            {
                int tabYStart = 79; 
                int tabYEnd = 100;

                if (size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd)
                {
                    if (GameCanvas.px >= xScroll && GameCanvas.px < xScroll + wScroll)
                    {
                        Item[] arrItemBodyCheck = Char.myCharz().arrItemBody;
                        Item[] arrItemBagCheck = Char.myCharz().arrItemBag;
                        int numTabs = (arrItemBodyCheck.Length + arrItemBagCheck.Length) / 20 + (((arrItemBodyCheck.Length + arrItemBagCheck.Length) % 20 > 0) ? 1 : 0);
                        int tabWidth = (numTabs > 0) ? (wScroll / numTabs) : wScroll;
                        int tabClickX = GameCanvas.px - xScroll;
                        int clickedTab = tabClickX / tabWidth;
                        if (clickedTab >= 0 && clickedTab < size_tab)
                        {
                            newSelected = clickedTab;
                            selected = -1; // Reset selected về -1 để không mở hành trang
                            checkOptionSelect();
                            pointerDownTime = 0;
                            waitToPerform = 0; // Set về 0 để không trigger logic mở hành trang
                            SoundMn.gI().panelClick();
                            return; // Return ngay để không xử lý click vào bag items
                        }
                        else
                        {
                            UnityEngine.Debug.Log($"[TAB CLICK pointerUp] FAILED: clickedTab={clickedTab} is out of range [0, {size_tab})");
                        }
                    }
                    else
                    {
                        UnityEngine.Debug.Log($"[TAB CLICK pointerUp] FAILED X check: px={GameCanvas.px} is NOT in range [{xScroll}, {xScroll + wScroll})");
                    }
                }
                else
                {
                    UnityEngine.Debug.Log($"[TAB CLICK pointerUp] FAILED Y check: py={GameCanvas.py} is NOT in range [{tabYStart}, {tabYEnd}) or size_tab={size_tab} <= 0");
                }

                int pX = GameCanvas.px - xScroll;
                int pY = GameCanvas.py - yScroll + cmy;

                int itemBoxWidth = 28;
                int itemBoxHeight = 28;
                int gap = 2;
                int bodyH = itemBoxHeight + gap;
                int leftColX = 2;
                int rightColX = wScroll - itemBoxWidth - 2;
                // Tính bodyItemsPerColumn giống như trong paintInventory
                Item[] arrItemBody = Char.myCharz().arrItemBody;
                int bodyItemsPerColumn = (arrItemBody.Length + 1) / 2;
                selected = -1;
                for (int i = 0; i < 9; i++)
                {
                    int colX = (i < 5) ? leftColX : rightColX;
                    int rowIndex = (i < 5) ? i : (i - 5);
                    int rowY = rowIndex * bodyH; // Tọa độ relative với yScroll

                    if (pX >= colX && pX <= colX + itemBoxWidth &&
                        pY >= rowY && pY <= rowY + itemBoxHeight)
                    {
                        selected = i;
                        break;
                    }
                }

                bool isInTabArea = (size_tab > 0 && GameCanvas.py >= tabYStart && GameCanvas.py < tabYEnd);
                if (selected == -1 && !isInTabArea)
                {
                    int bagStartY = yScroll + bodyItemsPerColumn * (itemBoxHeight + gap) + 10;

                    int clickY = GameCanvas.py;
                    int bagVisibleStartY = bagStartY - cmy;
                    int bagVisibleEndY = bagStartY - cmy + (Char.myCharz().arrItemBag.Length + 5) / 6 * ITEM_HEIGHT;
                    if (clickY >= bagVisibleStartY && clickY < bagVisibleEndY)
                    {

                        int bagClickY = clickY - (bagStartY - cmy);
                        int bagSize = 32; // Kích thước ô hành trang

                        int col = pX / (bagSize + 1);
                        int row = bagClickY / ITEM_HEIGHT;


                        // Kiểm tra xem click có nằm trong vùng bag hợp lệ không
                        if (col >= 0 && col < 6 && row >= 0) // Chỉ lấy 6 cột
                        {
                            int indexBag = row * 6 + col;
                            int finalIndex = 9 + indexBag; // 9 là số lượng item Body

                            if (indexBag >= 0 && indexBag < Char.myCharz().arrItemBag.Length)
                            {
                                selected = finalIndex;
                                // Validation: đảm bảo selected không vượt quá maxSelect
                                if (selected >= 0)
                                {
                                    int maxSelect = Char.myCharz().arrItemBag.Length > 0 ? (12 + Char.myCharz().arrItemBag.Length - 1) : 11;
                                    if (selected > maxSelect)
                                    {
                                        selected = maxSelect;
                                    }
                                }
                            }
                            else
                            {
                                UnityEngine.Debug.Log($"[BAG CLICK pointerUp] FAILED: indexBag={indexBag} is out of range [0, {Char.myCharz().arrItemBag.Length})");
                            }
                        }
                        else
                        {
                            UnityEngine.Debug.Log($"[BAG CLICK pointerUp] FAILED: col={col} or row={row} is out of range");
                        }
                    }
                    else
                    {
                        UnityEngine.Debug.Log($"[BAG CLICK pointerUp] FAILED bag Y check: clickY={clickY} is NOT in range [{bagVisibleStartY}, {bagVisibleEndY})");
                    }
                }
                else
                {
                    if (selected != -1)
                        UnityEngine.Debug.Log($"[BAG CLICK pointerUp] SKIPPED: selected={selected} (body item clicked)");
                    if (isInTabArea)
                        UnityEngine.Debug.Log($"[BAG CLICK pointerUp] SKIPPED: isInTabArea=true (tab area clicked)");
                }
            }
            else if (isTabBox())
            {
                int actualRow = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                int sizeItem = 32; // Match paintBox itemWidth
                int column4 = (GameCanvas.px - xScroll) / (sizeItem + 1);
                if (column4 < 0) column4 = 0;
                if (column4 >= CountBoxInRow) column4 = CountBoxInRow - 1;
                selected = actualRow * CountBoxInRow + column4;
                if (selected < 0 || selected >= Char.myCharz().arrItemBox.Length)
                {
                    selected = -1;
                }
            }
            else
            {
                if (isClanOption && clansOption != null && clansOption.Length > 0)
                {
                    int tabStartX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                    int tabY = yScroll;
                    int tabHeight = 23;

                    if (GameCanvas.py >= tabY && GameCanvas.py < tabY + tabHeight &&
                        GameCanvas.px >= tabStartX && GameCanvas.px < tabStartX + clansOption.Length * TAB_W)
                    {
                        int tabClickX = GameCanvas.px - tabStartX;
                        int clickedTab = tabClickX / TAB_W;
                        if (clickedTab < 0) clickedTab = 0;
                        if (clickedTab >= clansOption.Length) clickedTab = clansOption.Length - 1;
                        cSelected = clickedTab;
                        selected = 0; 
                        if (cSelected == 0)
                        {
                            // Chat bang
                            isMessage = true;
                            isViewMember = false;
                            isSearchClan = false;
                        }
                        else if (cSelected == 1)
                        {
                            // Xin đậu
                            isMessage = false;
                            isViewMember = false;
                            isSearchClan = false;
                        }
                        else if (cSelected == 2)
                        {
                            // Thành viên
                            isMessage = false;
                            isViewMember = true;
                            isSearchClan = false;
                        }
                        initTabClans();
                        return; // Đã xử lý click vào sub-tab CLAN, không xử lý tiếp click content
                    }
                    else
                    {
                        // Kiểm tra click có nằm trong vùng hiển thị hợp lệ không
                        if (GameCanvas.px < xScroll || GameCanvas.px >= xScroll + wScroll)
                        {
                            selected = -1;
                            return;
                        }
                        
                        int row4 = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                        selected = row4;
                        if (selected >= currentListLength || selected < 0)
                        {
                            selected = -1;
                            return;
                        }
                        if (selected == 0)
                        {
                            int clanTabX = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                            int clanTabEndX = clanTabX + clansOption.Length * TAB_W;
                            if (GameCanvas.px < clanTabX || GameCanvas.px >= clanTabEndX)
                            {
                                selected = -1;
                                return;
                            }
                        }
                    }
                }
                else if (size_tab > 0 && GameCanvas.py >= yScroll && GameCanvas.py < yScroll + 21)
                {
                    int tabClickX = GameCanvas.px - xScroll;
                    int clickedTab = tabClickX / TAB_W_NEW;
                    if (clickedTab >= 0 && clickedTab < size_tab)
                    {
                        newSelected = clickedTab;
                        selected = 0; 
                    }
                }
                else
                {
                    // Click vào content area
                    int row4 = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                    selected = row4;
                    // For shop tabs, validate against actual array length instead of currentListLength
                    if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                    {
                        // Shop tab - validate against actual shop array length
                        if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                        {
                            if (selected >= Char.myCharz().arrItemShop[currentTabIndex].Length)
                            {
                                selected = -1;
                            }
                        }
                        else
                        {
                            if (selected >= currentListLength) selected = -1;
                        }
                    }
                    // For other tabs (Top, Friend, Enemy, Archive, etc.), validate against actual size
                    else if (type == 15) // Top tab
                    {
                        if (selected >= vTop.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 11) // Friend tab
                    {
                        if (selected >= vFriend.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 16) // Enemy tab
                    {
                        if (selected >= vEnemy.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 9) // Archive tab
                    {
                        if (Char.myCharz().arrArchive != null && selected >= Char.myCharz().arrArchive.Length)
                        {
                            selected = -1;
                        }
                        else if (selected >= currentListLength)
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 10) // PlayerMenu tab
                    {
                        if (selected >= vPlayerMenu.size())
                        {
                            selected = -1;
                        }
                    }
                    else if (type == 12) // Combine tab
                    {
                        if (selected >= vItemCombine.size() + 1) // +1 for combine button
                        {
                            selected = -1;
                        }
                    }
                    else
                    {
                        if (selected >= currentListLength) selected = -1;
                    }
                }
            }
            checkOptionSelect();
            pointerDownTime = 0;
            waitToPerform = 10;
            SoundMn.gI().panelClick();
            if (flag)
            {
                cSelected = 0;
            }
            else if (!(isClanOption && clansOption != null && clansOption.Length > 0))
            {
                cSelected = 0;
            }
        }
        else if (selected != -1 && pointerDownTime > 6)
        {
            pointerDownTime = 0;
            waitToPerform = 1;
        }
        else if (selected == -1 && !isDownWhenRunning)
        {
            if (cmy < 0)
            {
                cmtoY = 0;
            }
            else if (cmy > cmyLim)
            {
                cmtoY = cmyLim;
            }
            else
            {
                int num3 = GameCanvas.py - pointerDownLastX[0] + (pointerDownLastX[0] - pointerDownLastX[1]) + (pointerDownLastX[1] - pointerDownLastX[2]);
                num3 = ((num3 > 10) ? 10 : ((num3 < -10) ? (-10) : 0));
                cmRun = -num3 * 100;
            }
        }
        // KHÔNG reset selected = 0 khi click vào khoảng trống trong scroll area
        // Chỉ reset khi click vào vùng tab (phía trên yScroll) và không phải trong scroll area
        if (isTabInven() && selected == -1)
        {
            // Chỉ reset khi click vào vùng tab (phía trên yScroll), và KHÔNG nằm trong vùng scroll
            bool isInScrollArea = (GameCanvas.px >= xScroll && GameCanvas.px <= xScroll + wScroll &&
                                   GameCanvas.py >= yScroll && GameCanvas.py <= yScroll + hScroll);
            // Chỉ reset khi click vào vùng tab thực sự (phía trên yScroll) và không trong scroll area
            if (!isInScrollArea && GameCanvas.py < yScroll && GameCanvas.px >= xScroll && GameCanvas.px <= xScroll + wScroll)
            {
                selected = 0;
                updateKeyInvenTab();
            }
            // Nếu click trong scroll area nhưng không trúng item, giữ selected = -1 (không reset về 0)
        }
        pointerIsDowning = false;
        pointerDownTime = 0;
        GameCanvas.isPointerJustRelease = false;
    }

    private void updateKeyInTabBar()
    {
        if ((scroll != null && scroll.pointerIsDowning) || pointerIsDowning)
        {
            return;
        }
        int num = currentTabIndex;
        if (isTabInven() && isnewInventory)
        {
            if (selected == -1)
            {
                if (GameCanvas.keyPressed[6])
                {
                    currentTabIndex++;
                    if (currentTabIndex >= currentTabName.Length)
                    {
                        if (GameCanvas.panel2 != null)
                        {
                            currentTabIndex = currentTabName.Length - 1;
                            GameCanvas.isFocusPanel2 = true;
                        }
                        else
                        {
                            currentTabIndex = 0;
                        }
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
                if (GameCanvas.keyPressed[4])
                {
                    currentTabIndex--;
                    if (currentTabIndex < 0)
                    {
                        currentTabIndex = currentTabName.Length - 1;
                    }
                    if (GameCanvas.isFocusPanel2)
                    {
                        GameCanvas.isFocusPanel2 = false;
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
            }
            else if (selected > 0)
            {
                if (newSelected == 0)
                {
                    nTableItem = Char.myCharz().arrItemBody.Length;
                }
                else
                {
                    int totalItems = Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length;
                    int itemsInCurrentTab = 20; // Mỗi tab có 20 items
                    int totalTabs = (totalItems / 20) + ((totalItems % 20 > 0) ? 1 : 0);
                    
                    if (newSelected >= totalTabs - 1)
                    {
                        int itemsInLastTab = totalItems % 20;
                        if (itemsInLastTab == 0 && totalItems > 0)
                            itemsInLastTab = 20; 
                        nTableItem = itemsInLastTab;
                    }
                    else
                    {
                        nTableItem = itemsInCurrentTab;
                    }
                }
                
                if (GameCanvas.keyPressed[8])
                {
                    if (newSelected == 0)
                    {
                        sellectInventory++;
                    }
                    else
                    {
                        sellectInventory += 5;
                    }
                }
                else if (GameCanvas.keyPressed[2])
                {
                    if (newSelected == 0)
                    {
                        sellectInventory--;
                    }
                    else
                    {
                        sellectInventory -= 5;
                    }
                }
                else if (GameCanvas.keyPressed[4])
                {
                    if (newSelected == 0)
                    {
                        sellectInventory -= 5;
                    }
                    else
                    {
                        sellectInventory--;
                    }
                }
                else if (GameCanvas.keyPressed[6])
                {
                    if (newSelected == 0)
                    {
                        sellectInventory += 5;
                    }
                    else
                    {
                        sellectInventory++;
                    }
                }
                
                // Giới hạn sellectInventory trong phạm vi hợp lệ
                if (sellectInventory < 0)
                {
                    sellectInventory = 0;
                }
                else if (sellectInventory >= nTableItem && nTableItem > 0)
                {
                    sellectInventory = nTableItem - 1;
                }
            }
        }
        else if (!IsTabOption())
        {
            if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
            {
                if (isTabInven())
                {
                    if (selected >= 0 && !ModFunc.isInventory)
                    {
                        updateKeyInvenTab();
                        return;
                    }
                    currentTabIndex++;
                    if (currentTabIndex >= currentTabName.Length)
                    {
                        if (GameCanvas.panel2 != null)
                        {
                            currentTabIndex = currentTabName.Length - 1;
                            GameCanvas.isFocusPanel2 = true;
                        }
                        else
                        {
                            currentTabIndex = 0;
                        }
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
                else
                {
                    currentTabIndex++;
                    if (currentTabIndex >= currentTabName.Length)
                    {
                        if (GameCanvas.panel2 != null)
                        {
                            currentTabIndex = currentTabName.Length - 1;
                            GameCanvas.isFocusPanel2 = true;
                        }
                        else
                        {
                            currentTabIndex = 0;
                        }
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
            }
            if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
            {
                currentTabIndex--;
                if (currentTabIndex < 0)
                {
                    currentTabIndex = currentTabName.Length - 1;
                }
                if (GameCanvas.isFocusPanel2)
                {
                    GameCanvas.isFocusPanel2 = false;
                }
                selected = lastSelect[currentTabIndex];
                lastTabIndex[type] = currentTabIndex;
            }
        }
        keyTouchTab = -1;
        for (int i = 0; i < currentTabName.Length; i++)
        {
            if (!GameCanvas.isPointer(startTabPos + i * TAB_W, 52, TAB_W - 1, 25))
            {
                continue;
            }
            keyTouchTab = i;
            if (!GameCanvas.isPointerJustRelease)
            {
                continue;
            }
            if (type == 8)
            {
                ModFunc.DoChatGlobal();
                break;
            }
            currentTabIndex = i;
            lastTabIndex[type] = i;
            GameCanvas.isPointerJustRelease = false;
            selected = lastSelect[currentTabIndex];
            if (num == currentTabIndex && cmRun == 0)
            {
                cmtoY = 0;
                selected = (GameCanvas.isTouch ? (-1) : 0);
            }
            break;
        }
        if (num == currentTabIndex)
        {
            return;
        }
        size_tab = 0;
        SoundMn.gI().panelClick();
        switch (type)
        {
            case 21:
            case 28:
                if (currentTabIndex == 0)
                {
                    setTabPetInventory(type == 28);
                }
                else if (currentTabIndex == 1)
                {
                    setTabPetSkill(type == 28);
                }
                else if (currentTabIndex == 2)
                {
                    setTabPetStatus();
                }
                else if (currentTabIndex == 3)
                {
                    setTabInventory(resetSelect: true);
                }
                break;
            case 0:
                if (currentTabIndex == 0)
                {
                    setTabTask();
                }
                if (currentTabIndex == 1)
                {
                    setTabInventory(resetSelect: true);
                }
                if (currentTabIndex == 2)
                {
                    setTabSkill();
                }
                if (currentTabIndex == 3)
                {
                    if (mainTabName.Length > 4)
                    {
                        setTabClans();
                    }
                    else
                    {
                        setTabTool();
                    }
                }
                if (currentTabIndex == 4)
                {
                    setTabTool();
                }
                break;
            case 2:
                if (currentTabIndex == 0)
                {
                    setTabBox();
                }
                if (currentTabIndex == 1)
                {
                    setTabInventory(resetSelect: true);
                }
                break;
            case 3:
                setTabZone();
                break;
            case 1:
                setTabShop();
                break;
            case 25:
                setTabSpeacialSkill();
                break;
            case 12:
                if (currentTabIndex == 0)
                {
                    setTabCombine();
                }
                if (currentTabIndex == 1)
                {
                    setTabInventory(resetSelect: true);
                }
                break;
            case 13:
                if (currentTabIndex == 0)
                {
                    if (Equals(GameCanvas.panel))
                    {
                        setTabInventory(resetSelect: true);
                    }
                    else if (Equals(GameCanvas.panel2))
                    {
                        setTabGiaoDich(isMe: false);
                    }
                }
                if (currentTabIndex == 1)
                {
                    setTabGiaoDich(isMe: true);
                }
                if (currentTabIndex == 2)
                {
                    setTabGiaoDich(isMe: false);
                }
                break;
        }
        selected = lastSelect[currentTabIndex];
    }

    private void setTabPetStatus()
    {
        currentListLength = strStatus.Length;
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    private void setTabPetSkill(bool isPet2)
    {
        // Lấy đối tượng pet tương ứng
        var pet = isPet2 ? Char.MyPet2z() : Char.myPetz();

        // CHẶN BUG: Nếu pet chưa được nạp, reset thông số cuộn và thoát ngay để không crash
        if (pet == null || pet.arrPetSkill == null)
        {
            currentListLength = 0;
            cmyLim = 0;
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? -1 : 0);
            return;
        }

        // ITEM_HEIGHT = 30;
        // TUYỆT ĐỐI KHÔNG gán currentListLength và cmyLim ở đây nữa vì setTabPetInventory đã tính tổng rồi.

        // Giữ nguyên logic tính vị trí thanh cuộn an toàn từ code gốc
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = cmyLim;
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    private void setTabTool()
    {
        SoundMn.gI().getSoundOption();
        currentListLength = strTool.Length;
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

	public void initTabClans()
	{
		if (isSearchClan)
		{
			currentListLength = ((clans != null) ? (clans.Length + 2) : 2);
			clanInfo = mResources.clan_list;
		}
		else if (isViewMember)
		{
			clanReport = string.Empty;
			currentListLength = ((member != null) ? member.size() : myMember.size()) + 2;
			clanInfo = mResources.member + " " + ((currClan == null) ? Char.myCharz().clan.name : currClan.name);
		}
		else if (isMessage)
		{
			currentListLength = ClanMessage.vMessage.size() + 2;
			clanInfo = mResources.msg;
			clanReport = string.Empty;
		}
		if (Char.myCharz().clan == null)
		{
			clansOption = new string[2][]
			{
				mResources.findClan,
				mResources.createClan
			};
		}
		else if (!isViewMember)
		{
			if (myMember.size() > 1)
			{
				clansOption = new string[3][]
				{
					mResources.chatClan,
					mResources.request_pea2,
					mResources.memberr
				};
			}
			else
			{
				clansOption = new string[1][] { mResources.memberr };
			}
		}
		else if (Char.myCharz().role > 0)
		{
			clansOption = new string[2][]
			{
				mResources.msgg,
				mResources.leaveClan
			};
		}
		else if (myMember.size() > 1)
		{
			clansOption = new string[4][]
			{
				mResources.msgg,
				mResources.leaveClan,
				mResources.khau_hieuu,
				mResources.bieu_tuongg
			};
		}
		else
		{
			clansOption = new string[3][]
			{
				mResources.msgg,
				mResources.khau_hieuu,
				mResources.bieu_tuongg
			};
		}
		cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		cmy = (cmtoY = cmyLast[currentTabIndex]);
		if (cmy < 0)
		{
			cmy = (cmtoY = 0);
		}
		if (cmy > cmyLim)
		{
			cmy = (cmtoY = cmyLim);
		}
	}


 	public void setTabClans()
	{
		GameScr.isNewClanMessage = false;
		ITEM_HEIGHT = 24;
		if (lastSelect != null && lastSelect[3] == 0)
		{
			lastSelect[3] = -1;
		}
		currentListLength = 2;
		if (Char.myCharz().clan != null)
		{
			isMessage = true;
			isViewMember = false;
			isSearchClan = false;
		}
		else
		{
			isMessage = false;
			isViewMember = false;
			isSearchClan = true;
		}
		if (Char.myCharz().clan != null)
		{
			currentListLength = ClanMessage.vMessage.size() + 2;
		}
		initTabClans();
		cSelected = -1;
		if (chatTField == null)
		{
			chatTField = new ChatTextField();
			chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
			chatTField.initChatTextField();
			chatTField.parentScreen = GameCanvas.panel;
		}
		if (Char.myCharz().clan == null)
		{
			clanReport = mResources.findingClan;
			Service.gI().searchClan(string.Empty);
		}
		selected = lastSelect[currentTabIndex];
		if (GameCanvas.isTouch)
		{
			selected = -1;
		}
	}


    public void initLogMessage()
    {
        currentListLength = logChat.size() + 1;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        cmx = (cmtoX = 0);
    }

    private void setTabMessage()
    {
        ITEM_HEIGHT = 24;
        initLogMessage();
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public void setTabShop()
    {
        ITEM_HEIGHT = 29;
        if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null && typeShop != 2)
        {
            currentListLength = checkCurrentListLength(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length);
        }
        else
        {
            if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
            {
                currentListLength = Char.myCharz().arrItemShop[currentTabIndex].Length;
            }
            else
            {
                currentListLength = 0;
            }
        }
        if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
        {
            if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
            {
                cmyLim = Char.myCharz().arrItemShop[currentTabIndex].Length * ITEM_HEIGHT - hScroll;
            }
            else
            {
                cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            }
        }
        else
        {
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        }
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    private void setTabSkill()
    {
        ITEM_HEIGHT = 30;
        currentListLength = Char.myCharz().nClass.skillTemplates.Length + 6;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = cmyLim;
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    private void setTabMapTrans()
    {
        ITEM_HEIGHT = 29;
        currentListLength = mapNames.Length;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = cmyLim;
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    private void setTabZone()
    {
        ITEM_HEIGHT = 29;
        currentListLength = GameScr.gI().zones.Length;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        cmy = (cmtoY = 0);
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    private void setTabBox()
    {
        currentListLength = checkCurrentListLength(Char.myCharz().arrItemBox.Length);
        ITEM_HEIGHT = 29;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 9;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }
    private void setTabPetInventory(bool isPet2)
    {   
        ITEM_HEIGHT = 30;
        var pet = isPet2 ? Char.MyPet2z() : Char.myPetz();

        if (pet == null)
        {
            Debug.LogWarning("Pet đang null! isPet2 = " + isPet2);
            currentListLength = 0;
            cmyLim = 0;
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? -1 : 0);
            return;
        }

        Item[] arrItemBody = pet.arrItemBody ?? new Item[0]; // Tránh arrItemBody null
        
        // --- ĐOẠN SỬA ĐỔI: Lấy thêm mảng Skill và check null ---
        Skill[] arrPetSkill = pet.arrPetSkill ?? new Skill[0]; 

        // Tổng số dòng = Số ô trang bị + Số chiêu thức (Bỏ hoàn toàn 8 dòng chỉ số HP, MP)
        currentListLength = arrItemBody.Length + arrPetSkill.Length;
        // -----------------------------------------------------

        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = 0);
        }
        selected = (GameCanvas.isTouch ? -1 : 0);
    }

    public void setTabInventory(bool resetSelect)
    {
        if (!ModFunc.isInventory)
        {
            currentListLength = checkCurrentListLength(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length);
            ITEM_HEIGHT = 29;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            // QUAN TRỌNG: Khi chuyển tab, LUÔN reset scroll về đầu trang (cmy = 0)
            // Thay vì restore từ cmyLast[currentTabIndex]
            cmy = (cmtoY = 0);
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (resetSelect)
            {
                selected = (GameCanvas.isTouch ? (-1) : 0);
            }
        }
        else
        {
            currentListLength = checkCurrentListLength(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length / 6);
            ITEM_HEIGHT = 29;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            // QUAN TRỌNG: Khi chuyển tab, LUÔN reset scroll về đầu trang (cmy = 0)
            // Thay vì restore từ cmyLast[currentTabIndex]
            cmy = (cmtoY = 0);
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (resetSelect)
            {
                selected = (GameCanvas.isTouch ? (-1) : 0);
            }
        }
    }

    private void setTabMap()
    {
        if (!isPaintMap)
        {
            return;
        }
        if (TileMap.lastPlanetId != TileMap.planetID)
        {
            Res.outz("LOAD TAM HINH");
            imgMap = GameCanvas.loadImageRMS("/img/map" + TileMap.planetID + ".png");
            TileMap.lastPlanetId = TileMap.planetID;
        }
        cmxMap = getXMap() - wScroll / 2;
        cmyMap = getYMap() + yScroll - (yScroll + hScroll / 2);
        pa1 = cmxMap;
        pa2 = cmyMap;
        cmxMapLim = 250 - wScroll;
        cmyMapLim = 220 - hScroll;
        if (cmxMapLim < 0)
        {
            cmxMapLim = 0;
        }
        if (cmyMapLim < 0)
        {
            cmyMapLim = 0;
        }
        for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
        {
            if (TileMap.mapID == mapId[TileMap.planetID][i])
            {
                xMove = mapX[TileMap.planetID][i] + xScroll;
                yMove = mapY[TileMap.planetID][i] + yScroll + 5;
                break;
            }
        }
        xMap = getXMap() + xScroll;
        yMap = getYMap() + yScroll;
        xMapTask = getXMapTask() + xScroll;
        yMapTask = getYMapTask() + yScroll;
        Resources.UnloadUnusedAssets();
        GC.Collect();
    }

    private void setTabTask()
    {
        cmyQuest = 0;
    }

    public void moveCamera()
    {
        if (timeShow > 0)
        {
            timeShow--;
        }
        if (justRelease && Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1)
        {
            if (cmy < -50)
            {
                InfoDlg.showWait();
                justRelease = false;
                if (currPageShop[currentTabIndex] <= 0)
                {
                    Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                }
                else
                {
                    Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                }
            }
            else if (cmy > cmyLim + 50)
            {
                justRelease = false;
                InfoDlg.showWait();
                if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                {
                    Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                }
                else
                {
                    Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                }
            }
        }
        if (cmx != cmtoX && !pointerIsDowning)
        {
            cmvx = cmtoX - cmx << 2;
            cmdx += cmvx;
            cmx += cmdx >> 3;
            cmdx &= 15;
        }
        if (Math.abs(cmtoX - cmx) < 10)
        {
            cmx = cmtoX;
        }
        if (isClose)
        {
            isClose = false;
            cmtoX = wScroll;
        }
        if (cmtoX >= wScroll - 10 && cmx >= wScroll - 10 && position == 0)
        {
            isShow = false;
            cleanCombine();
            if (isChangeZone)
            {
                isChangeZone = false;
                if (Char.myCharz().cHP > 0.0 && Char.myCharz().statusMe != 14)
                {
                    InfoDlg.showWait();
                    if (type == 3)
                    {
                        Service.gI().requestChangeZone(selected, -1);
                    }
                    else if (type == 14)
                    {
                        AutoXmap.SelectMapTrans(selected);
                    }
                }
            }
            if (isSelectPlayerMenu)
            {
                isSelectPlayerMenu = false;
                int num = vPlayerMenu.size() - vPlayerMenu_id.size();
                if (Char.myCharz().charFocus != null)
                {
                    if (selected - num < 0)
                    {
                        Char.myCharz().charFocus.menuSelect = selected;
                    }
                    else
                    {
                        Char.myCharz().charFocus.menuSelect = short.Parse((string)vPlayerMenu_id.elementAt(selected - num));
                    }
                }
                ((Command)vPlayerMenu.elementAt(selected)).performAction();
            }
            vPlayerMenu.removeAllElements();
            charMenu = null;
        }
        if (cmRun != 0 && !pointerIsDowning)
        {
            cmtoY += cmRun / 100;
            if (cmtoY < 0)
            {
                cmtoY = 0;
            }
            else if (cmtoY > cmyLim)
            {
                cmtoY = cmyLim;
            }
            else
            {
                cmy = cmtoY;
            }
            cmRun = cmRun * 9 / 10;
            if (cmRun < 100 && cmRun > -100)
            {
                cmRun = 0;
            }
        }
        if (cmy != cmtoY && !pointerIsDowning)
        {
            cmvy = cmtoY - cmy << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        cmyLast[currentTabIndex] = cmy;
    }

    public void paintDetail(mGraphics g)
    {
        if (cp == null || cp.says == null)
        {
            return;
        }
        cp.paint(g);
        int num = cp.cx + 13;
        int num2 = cp.cy + 11;
        if (type == 15)
        {
            num += 5;
            num2 += 26;
        }
        if (type == 0 && currentTabIndex == 3)
        {
            if (isSearchClan)
            {
                num -= 5;
            }
            else if (partID != null || charInfo != null)
            {
                num = cp.cx + 21;
                num2 = cp.cy + 40;
            }
        }
        if (partID != null)
        {
            Part part = GameScr.parts[partID[0]];
            Part part2 = GameScr.parts[partID[1]];
            Part part3 = GameScr.parts[partID[2]];
            SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + Char.CharInfo[0][0][1] + part.pi[Char.CharInfo[0][0][0]].dx, num2 - Char.CharInfo[0][0][2] + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
            SmallImage.drawSmallImage(g, part2.pi[Char.CharInfo[0][1][0]].id, num + Char.CharInfo[0][1][1] + part2.pi[Char.CharInfo[0][1][0]].dx, num2 - Char.CharInfo[0][1][2] + part2.pi[Char.CharInfo[0][1][0]].dy, 0, 0);
            SmallImage.drawSmallImage(g, part3.pi[Char.CharInfo[0][2][0]].id, num + Char.CharInfo[0][2][1] + part3.pi[Char.CharInfo[0][2][0]].dx, num2 - Char.CharInfo[0][2][2] + part3.pi[Char.CharInfo[0][2][0]].dy, 0, 0);
        }
        else if (charInfo != null)
        {
            charInfo.paintCharBody(g, num + 5, num2 + 25, 1, 0, isPaintBag: true);
        }
        else if (idIcon != -1)
        {
            SmallImage.drawSmallImage(g, idIcon, num, num2, 0, 3);
        }
        if (currItem != null && currItem.template.type != 5)
        {
            if (currItem.compare > 0)
            {
                g.drawImage(imgUp, num - 7, num2 + 13, 3);
                mFont.tahoma_7b_green.drawString(g, Res.abs(currItem.compare) + string.Empty, num + 1, num2 + 8, 0);
            }
            else if (currItem.compare < 0 && currItem.compare != -1)
            {
                g.drawImage(imgDown, num - 7, num2 + 13, 3);
                mFont.tahoma_7b_red.drawString(g, Res.abs(currItem.compare) + string.Empty, num + 1, num2 + 8, 0);
            }
        }
    }

    public void paintTop(mGraphics g)
    {
        // Ensure currentListLength is correct before painting
        int actualSize = vTop.size();
        currentListLength = actualSize;
        cmyLim = actualSize * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (currentListLength == 0)
        {
            return;
        }
        int num = (cmy + hScroll) / 24 + 1;
        if (num < hScroll / 24 + 1)
        {
            num = hScroll / 24 + 1;
        }
        if (num > currentListLength)
        {
            num = currentListLength;
        }
        int num2 = cmy / 24;
        if (num2 >= num)
        {
            num2 = num - 1;
        }
        if (num2 < 0)
        {
            num2 = 0;
        }
        for (int i = num2; i < num; i++)
        {
            int num3 = xScroll;
            int num4 = yScroll + i * ITEM_HEIGHT;
            int num5 = 29;
            int h = ITEM_HEIGHT - 1;
            int num6 = xScroll + num5;
            int num7 = yScroll + i * ITEM_HEIGHT;
            int num8 = wScroll - num5;
            int num9 = ITEM_HEIGHT - 1;
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num6, num7, num8, num9);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num3, num4, num5, h);
            TopInfo topInfo = (TopInfo)vTop.elementAt(i);
            if (topInfo.headICON != -1)
            {
                SmallImage.drawSmallImage(g, topInfo.headICON, num3, num4, 0, 0);
            }
            else
            {
                Part part = GameScr.parts[topInfo.headID];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num3 + part.pi[Char.CharInfo[0][0][0]].dx, num4 + num9 - 1, 0, mGraphics.BOTTOM | mGraphics.LEFT);
            }
            g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
            if (topInfo.pId != Char.myCharz().charID)
            {
                mFont.tahoma_7b_green.drawString(g, topInfo.name, num6 + 5, num7, 0);
            }
            else
            {
                mFont.tahoma_7b_red.drawString(g, topInfo.name, num6 + 5, num7, 0);
            }
            mFont.tahoma_7_blue.drawString(g, topInfo.info, num6 + num8 - 5, num7 + 11, 1);
            mFont.tahoma_7_green2.drawString(g, mResources.rank + ": " + topInfo.rank + string.Empty, num6 + 5, num7 + 11, 0);
        }
        paintScrollArrow(g);
    }

    public void paint(mGraphics g)
    {
        g.translate(-g.getTranslateX(), -g.getTranslateY() + mGraphics.addYWhenOpenKeyBoard);
        g.translate(-cmx, 0);
        g.translate(X, Y);
        if (GameCanvas.panel.combineSuccess != -1)
        {
            if (Equals(GameCanvas.panel))
            {
                paintCombineEff(g);
            }
            return;
        }
        GameCanvas.paintz.paintFrameSimple(X, Y, W, H, g);
        paintTopInfo(g);
        paintBottomMoneyInfo(g);
        paintTab(g);
        switch (type)
        {
            case 9:
                paintArchivement(g);
                break;
            case 21:
            case 28:
                if (currentTabIndex == 0)
                {
                    paintPetInventory(g, type == 28);
                    paintPetSkill(g, type == 28);
                }
                else if (currentTabIndex == 1)
                {
                    paintPetStatus(g);
                }
                else if (currentTabIndex == 2)
                {
                    paintInventory(g);
                }
                break;
            case 24:
                paintGameSubInfo(g);
                break;
            case 23:
                paintGameInfo(g);
                break;
            case 32:
                this.paintListBosses(g);
                break;
            case 33:
                this.paintTabBoss(g);
                break;
            case 0:
                if (currentTabIndex == 0)
                {
                    paintTask(g);
                }
                if (currentTabIndex == 1)
                {
                    paintInventory(g);
                }
                if (currentTabIndex == 2)
                {
                    paintSkill(g);
                }
                if (currentTabIndex == 3)
                {
                    if (mainTabName.Length == 4)
                    {
                        paintTools(g);
                    }
                    else
                    {
                        paintClans(g);
                    }
                }
                if (currentTabIndex == 4)
                {
                    paintTools(g);
                }
                break;
            case 2:
                if (currentTabIndex == 0)
                {
                    paintBox(g);
                }
                if (currentTabIndex == 1)
                {
                    paintInventory(g);
                }
                break;
            case 3:
                paintZone(g);
                break;
            case 1:
                paintShop(g);
                break;
            case 25:
                paintSpeacialSkill(g);
                break;
            case 4:
                paintMap(g);
                break;
            case 7:
                paintInventory(g);
                break;
            case 17:
                paintShop(g);
                break;
            case 8:
                paintLogChat(g);
                break;
            case 10:
                paintPlayerMenu(g);
                break;
            case 11:
                paintFriend(g);
                break;
            case 16:
                paintEnemy(g);
                break;
            case 15:
                paintTop(g);
                break;
            case 12:
                if (currentTabIndex == 0)
                {
                    paintCombine(g);
                }
                if (currentTabIndex == 1)
                {
                    paintInventory(g);
                }
                break;
            case 13:
                if (currentTabIndex == 0)
                {
                    if (Equals(GameCanvas.panel))
                    {
                        paintInventory(g);
                    }
                    else
                    {
                        paintGiaoDich(g, isMe: false);
                    }
                }
                if (currentTabIndex == 1)
                {
                    paintGiaoDich(g, isMe: true);
                }
                if (currentTabIndex == 2)
                {
                    paintGiaoDich(g, isMe: false);
                }
                break;
            case 14:
                paintMapTrans(g);
                break;
            case 18:
                paintFlagChange(g);
                break;
            case 19:
                paintOption(g);
                break;
            case 20:
                paintAccount(g);
                break;
            case 22:
                paintAuto(g);
                break;
            case 29:
                paintHotkeySettings(g);
                break;
            case 26:
                PaintModFunc(g);
                break;
            case 27:
                paintPlayerInfo(g);
                break;
        }
        GameScr.resetTranslate(g);
        paintDetail(g);
        if (cmx == cmtoX)
        {
            cmdClose.paint(g);
        }
        if (tabIcon != null && tabIcon.isShow)
        {
            tabIcon.paint(g);
        }
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        g.translate(X, Y);
        g.translate(-cmx, 0);
    }
    private void paintShop(mGraphics g)
    {
        try
        {
            // Ensure currentListLength is correct for shop tabs before painting
            if (type == 1 && currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
            {
                if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                {
                    int actualLength = Char.myCharz().arrItemShop[currentTabIndex].Length;
                    // Force update currentListLength to actual length
                    currentListLength = actualLength;
                    // Also update cmyLim
                    cmyLim = actualLength * ITEM_HEIGHT - hScroll;
                    if (cmyLim < 0)
                    {
                        cmyLim = 0;
                    }
                }
            }
            if (type == 1 && currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null && typeShop != 2)
            {
                paintInventory(g);
                return;
            }
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            if (typeShop == 2 && Equals(GameCanvas.panel))
            {
                if (currentTabIndex <= 3 && GameCanvas.isTouch)
                {
                    if (cmy < -50)
                    {
                        GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + 30, g);
                    }
                    else if (cmy < 0)
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.getDown, xScroll + wScroll / 2, yScroll + 15, 2);
                    }
                    else if (cmyLim >= 0)
                    {
                        if (cmy > cmyLim + 50)
                        {
                            GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + hScroll - 30, g);
                        }
                        else if (cmy > cmyLim)
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.getUp, xScroll + wScroll / 2, yScroll + hScroll - 25, 2);
                        }
                    }
                }
                if (Char.myCharz().arrItemShop[currentTabIndex].Length == 0 && type != 17)
                {
                    mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                    return;
                }
            }
            g.translate(0, -cmy);
            Item[] array = Char.myCharz().arrItemShop[currentTabIndex];
            if (typeShop == 2 && (currentTabIndex == 4 || type == 17))
            {
                array = Char.myCharz().arrItemShop[4];
                if (array.Length == 0)
                {
                    mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                    return;
                }
            }
            int num = array.Length;
            for (int i = 0; i < num; i++)
            {
                int num2 = xScroll + 26;
                int num3 = yScroll + i * ITEM_HEIGHT;
                int num4 = wScroll - 26;
                int h = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = 24;
                int num8 = ITEM_HEIGHT - 1;
                if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num2, num3, num4, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num5, num6, num7, num8);
                Item item = array[i];
                if (item != null)
                {
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (item.isMe != 0 && typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2) && item.template.name.Length < 20)
                    {
                        mFont2 = mFont.tahoma_7b_green;
                    }
                    if (item.itemOption != null)
                    {
                        for (int j = 0; j < item.itemOption.Length; j++)
                        {
                            if (item.itemOption[j].optionTemplate.id == 72)
                            {
                                text = " [+" + item.itemOption[j].param + "]";
                            }
                            if (item.itemOption[j].optionTemplate.id == 41)
                            {
                                if (item.itemOption[j].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                else if (item.itemOption[j].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (item.itemOption[j].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (item.itemOption[j].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                    string text2 = string.Empty;
                    if (item.itemOption != null && item.itemOption.Length >= 1)
                    {
                        if (item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                        {
                            text2 += item.itemOption[0].getOptionString();
                        }
                        mFont mFont3 = mFont.tahoma_7_blue;
                        if (item.compare < 0 && item.template.type != 5)
                        {
                            mFont3 = mFont.tahoma_7_red;
                        }
                        if (typeShop == 2 && item.itemOption.Length > 1 && item.buyType != -1)
                        {
                            text2 += string.Empty;
                        }
                        if (typeShop != 2 || (typeShop == 2 && item.buyType <= 1))
                        {
                            mFont3.drawString(g, text2, num2 + 5, num3 + 11, 0);
                        }
                    }
                    if (item.buySpec > 0)
                    {
                        SmallImage.drawSmallImage(g, item.iconSpec, num2 + num4 - 7, num3 + 9, 0, 3);
                        mFont.tahoma_7b_blue.drawString(g, Res.formatNumber(item.buySpec), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                    }
                    if (item.buyCoin != 0 || item.buyGold != 0)
                    {
                        if (typeShop != 2 && item.powerRequire == 0)
                        {
                            if (item.buyCoin > 0 && item.buyGold > 0)
                            {
                                if (item.buyCoin > 0)
                                {
                                    g.drawImage(imgThoivang, num2 + num4 - 7, num3 + 7, 3);
                                    mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                                if (item.buyGold > 0)
                                {
                                    g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                    mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                }
                            }
                            else
                            {
                                if (item.buyCoin > 0)
                                {
                                    g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                    mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                                if (item.buyGold > 0)
                                {
                                    g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                    mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                            }
                        }
                        if (typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2))
                        {
                            if (item.buyCoin > 0 && item.buyGold > 0)
                            {
                                if (item.buyCoin > 0)
                                {
                                    g.drawImage(imgThoivang, num2 + num4 - 7, num3 + 7, 3);
                                    mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                    mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                                if (item.buyGold > 0)
                                {
                                    g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                    mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                    mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                }
                            }
                            else
                            {
                                if (item.buyCoin > 0)
                                {
                                    g.drawImage(imgThoivang, num2 + num4 - 7, num3 + 7, 3);
                                    mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                    mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                                if (item.buyGold > 0)
                                {
                                    g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                    mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                    mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                }
                                try
                                {
                                    mFont2 = mFont.tahoma_7b_green;
                                    if (!Char.myCharz().cName.Equals(item.nameNguoiKyGui))
                                    {
                                        mFont2 = mFont.tahoma_7b_green;
                                    }
                                    mFont2.drawString(g, item.nameNguoiKyGui, num2 + num4, num3 + 1 + mFont.tahoma_7b_red.getHeight(), mFont.RIGHT);
                                }
                                catch (Exception)
                                {
                                }
                            }
                        }
                    }
                    SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                    if (item.newItem && GameCanvas.gameTick % 10 > 5)
                    {
                        g.drawImage(imgNew, num5 + num7 / 2, num3 + 19, 3);
                    }
                }
                if (typeShop != 2 || (!Equals(GameCanvas.panel2) && currentTabIndex != 4) || item.buyType == 0)
                {
                    continue;
                }
                if (item.buyType == 1)
                {
                    mFont.tahoma_7_green.drawString(g, mResources.dangban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                    if (item.buyCoin != -1)
                    {
                        g.drawImage(imgThoivang, num2 + num4 - 7, num3 + 19, 3);
                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 13, mFont.RIGHT);
                    }
                    else if (item.buyGold != -1)
                    {
                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 17, 3);
                        mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                    }
                }
                else if (item.buyType == 2)
                {
                    mFont.tahoma_7b_blue.drawString(g, mResources.daban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                    if (item.buyCoin != -1)
                    {
                        g.drawImage(imgThoivang, num2 + num4 - 7, num3 + 17, 3);
                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                    }
                    else if (item.buyGold != -1)
                    {
                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 17, 3);
                        mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                    }
                }
            }
            paintScrollArrow(g);
        }
        catch (Exception)
        {
        }
    }

    private void paintHotkeySettings(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        int hotkeyCount = ModFunc.hotkeyDefinitions.Length;
        for (int i = 0; i < currentListLength; i++)
        {
            int rowY = yScroll + i * ITEM_HEIGHT;
            if (rowY - cmy > yScroll + hScroll || rowY - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            bool isSelected = i == selected;
            g.setColor(isSelected ? 16383818 : 15196114);
            g.fillRect(xScroll, rowY, wScroll - 1, ITEM_HEIGHT - 1);
            if (i < hotkeyCount)
            {
                string keyName = (i == hotkeyListeningIndex) ? "..." : ModFunc.GetHotkeyName(ModFunc.GI().GetDraftHotkey(i));
                int keyWidth = 58;
                g.setColor(isSelected ? 9541120 : 9993045);
                g.fillRect(xScroll, rowY, keyWidth, ITEM_HEIGHT - 1);
                mFont.tahoma_7b_white.drawString(g, "[" + keyName + "]", xScroll + keyWidth / 2, rowY + 6, mFont.CENTER);
                mFont.tahoma_7b_dark.drawString(g, ModFunc.hotkeyDefinitions[i].label, xScroll + keyWidth + 6, rowY + 6, mFont.LEFT);
            }
            else
            {
                string caption = (i == hotkeyCount) ? "Cài lại mặc định" : ((i == hotkeyCount + 1) ? "Lưu" : "Hủy");
                mFont.tahoma_7b_dark.drawString(g, caption, xScroll + wScroll / 2, rowY + 6, mFont.CENTER);
            }
        }
        paintScrollArrow(g);
    }

    private void paintAuto(mGraphics g)
    {
    }

    private void paintPetStatus(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < strStatus.Length; i++)
        {
            int x = xScroll;
            int num = yScroll + i * ITEM_HEIGHT;
            int num2 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(x, num, num2, h);
                mFont.tahoma_7b_dark.drawString(g, strStatus[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
            }
        }
        paintScrollArrow(g);
    }
    private void paintPetSkill(mGraphics g, bool isPet2)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        
        Char pet = (isPet2 ? Char.MyPet2z() : Char.myPetz());
        if (pet == null || pet.arrPetSkill == null) return;

        // --- ĐOẠN ĐIỀU CHỈNH: Đẩy vị trí dòng bắt đầu của Skill ra sau ô trang bị ---
        int startSkillIndex = (pet.arrItemBody != null) ? pet.arrItemBody.Length : 0;
        int num = pet.arrPetSkill.Length; // Chỉ chạy vòng lặp theo số lượng Skill thực tế

        for (int i = 0; i < num; i++)
        {
            // Vị trí dòng thực tế trên Panel (được đôn xuống dưới hàng trang bị)
            int actualLine = startSkillIndex + i;

            int num2 = xScroll + 30;
            int num3 = yScroll + actualLine * ITEM_HEIGHT; // Tính toán Y theo dòng thực tế
            int num4 = wScroll - 30;
            int h = ITEM_HEIGHT - 1;
            int num5 = xScroll;
            int num6 = yScroll + actualLine * ITEM_HEIGHT; // Tính toán Y theo dòng thực tế
            
            // Kiểm tra xem dòng này có nằm trong vùng hiển thị cuộn hay không (giữ nguyên gốc)
            if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }

            // Đổi màu nền khi dòng được chọn (i + startSkillIndex chính là chỉ số selected tổng)
            g.setColor((actualLine != selected) ? 16765060 : 16776068);
            g.fillRect(num2, num3, num4, h);
            
            // Vẽ icon khung Skill nền
            g.drawImage(GameScr.imgSkill2, num5, num6, 0);

            // Lấy chiêu thức thực tế từ mảng (chạy tuyến tính từ 0 đến hết mảng skill)
            Skill skill = pet.arrPetSkill[i];
            if (skill != null)
            {
                if (skill.template != null)
                {
                    mFont.tahoma_7_blue.drawString(g, skill.template.name, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, mResources.level + ": " + skill.point, num2 + 5, num3 + 15, 0);
                    SmallImage.drawSmallImage(g, skill.template.iconId, num5 + 4, num6 + 4, 0, 0);
                }
                else
                {
                    mFont.tahoma_7_green2.drawString(g, skill.moreInfo, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, mResources.level + ": " + 0, num2 + 5, num3 + 15, 0);
                    SmallImage.drawSmallImage(g, GameScr.efs[98].arrEfInfo[0].idImg, num5 + 8, num6 + 7, 0, 0);
                }
            }
        }
        paintScrollArrow(g);
    }
    private void paintPetInventory(mGraphics g, bool isPet2)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        Item[] arrItemBody = (isPet2 ? Char.MyPet2z() : Char.myPetz()).arrItemBody;
        for (int i = 0; i < arrItemBody.Length; i++)
        {
            int num = i;
            _ = arrItemBody.Length;
            int num3 = xScroll + 29;
            int num4 = yScroll + i * ITEM_HEIGHT;
            int num5 = wScroll - 29;
            int h = ITEM_HEIGHT - 1;
            int num6 = xScroll;
            int num7 = yScroll + i * ITEM_HEIGHT;
            int num8 = ITEM_HEIGHT - 1;
            int num9 = ITEM_HEIGHT - 1;
            if (num4 - cmy > yScroll + hScroll || num4 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            Item item = arrItemBody[num];
            g.setColor((i == selected) ? 16383818 : 15196114);
            g.fillRect(num3, num4, num5, h, 5);
            g.setColor((i == selected) ? 9541120 : 9993045);
            if (item != null)
            {
                for (int j = 0; j < item.itemOption.Length; j++)
                {
                    if (item.itemOption[j].optionTemplate.id == 72 && item.itemOption[j].param > 0)
                    {
                        sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                        if (GetColor_ItemBg(color_Item_Upgrade) != -1)
                        {
                            g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                        }
                    }
                }
            }
            g.setColor(6047789, 0.5f);
            g.fillRect(num6, num7, num8, num9, 5);
            paintEffectItem(g, item, num6, num7);
            if (item != null && item.isSelect && GameCanvas.panel.type == 12)
            {
                g.setColor((i != selected) ? 6047789 : 7040779);
                g.fillRect(num6, num7, num8, num9, 5);
            }
            if (item == null)
            {
                continue;
            }
            string text = string.Empty;
            mFont mFont2 = mFont.tahoma_7_green2;
            if (item.itemOption != null)
            {
                for (int k = 0; k < item.itemOption.Length; k++)
                {
                    if (item.itemOption[k].optionTemplate.id == 72)
                    {
                        text = " [+" + item.itemOption[k].param + "]";
                    }
                    if (item.itemOption[k].optionTemplate.id == 225)
                    {
                        text = " [+" + item.itemOption[k].param + "]";
                    }
                    if (item.itemOption[k].optionTemplate.id == 225)
                    {
                        if (item.itemOption[k].param >= 1 && item.itemOption[k].param <= 2)
                        {
                            mFont2 = GetFont(0);
                        }
                        else if (item.itemOption[k].param >= 3 && item.itemOption[k].param <= 4)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[k].param >= 5 && item.itemOption[k].param <= 6)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[k].param >= 7 && item.itemOption[k].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                    if (item.itemOption[k].optionTemplate.id == 72)
                    {
                        if (item.itemOption[k].param >= 1 && item.itemOption[k].param <= 5)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[k].param >= 6 && item.itemOption[k].param <= 7)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[k].param >= 8 && item.itemOption[k].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                }
            }
            if (ModFunc.isShowID)
            {
                mFont2.drawString(g, "[" + item.template.id + "] " + item.template.name + text, num3 + 5, num4 + 1, 0);
            }
            else
            {
                mFont2.drawString(g, item.template.name + text, num3 + 5, num4 + 1, 0);
            }
            string text2 = string.Empty;
            if (item.itemOption != null)
            {
                if (item.itemOption.Length != 0 && item.itemOption[0] != null && item.itemOption[0].IsValidOption())
                {
                    text2 += item.itemOption[0].getOptionString();
                }
                mFont mFont3 = mFont.tahoma_7_blue;
                if (item.compare < 0 && item.template.type != 5)
                {
                    mFont3 = mFont.tahoma_7_red;
                }
                if (item.itemOption.Length > 1)
                {
                    for (int l = 1; l < Math.min(item.itemOption.Length, 3); l++)
                    {
                        if (item.itemOption[l] != null && item.itemOption[l].IsValidOption())
                        {
                            text2 = text2 + ", " + item.itemOption[l].getOptionString();
                        }
                    }
                }
                mFont3.drawString(g, text2, num3 + 5, num4 + 10, mFont.LEFT);
            }
            SmallImage.drawSmallImage(g, item.template.iconID, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
            if (item.itemOption != null)
            {
                for (int m = 0; m < item.itemOption.Length; m++)
                {
                    paintOptItemInventory(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num6, num7, num8, num9, item);
                }
                for (int n = 0; n < item.itemOption.Length; n++)
                {
                    paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num6, num7, num8, num9);
                }
            }
            if (item.quantity > 1)
            {
                mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num6 + num8, num7 + num9 - mFont.tahoma_7_yellow.getHeight(), 1);
            }
        }
        paintScrollArrow(g);
    }

    private void paintScrollArrow(mGraphics g)
    {
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        if ((cmy > 24 && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
        {
            g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
        }
        if ((cmy < cmyLim && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
        {
            g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 8, 0);
        }
    }

    private void paintTools(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < strTool.Length; i++)
        {
            int num = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num, num2, num3, h);
            mFont.tahoma_7b_dark.drawString(g, strTool[i], xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
            if (!strTool[i].Equals(mResources.gameInfo))
            {
                continue;
            }
            for (int j = 0; j < vGameInfo.size(); j++)
            {
                if (!((GameInfo)vGameInfo.elementAt(j)).hasRead)
                {
                    if (GameCanvas.gameTick % 20 > 10)
                    {
                        g.drawImage(imgNew, num + 10, num2 + 10, 3);
                    }
                    break;
                }
            }
        }
        paintScrollArrow(g);
    }

    private void paintGameSubInfo(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < contenInfo.Length; i++)
        {
            _ = xScroll;
            int num2 = yScroll + i * 15;
            _ = wScroll;
            _ = ITEM_HEIGHT;
            if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
            {
                mFont.tahoma_7b_dark.drawString(g, contenInfo[i], xScroll + 5, num2 + 6, mFont.LEFT);
            }
        }
        paintScrollArrow(g);
    }

    private void paintPlayerInfo(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < contenInfo.Length; i++)
        {
            _ = xScroll;
            int num2 = yScroll + i * 15;
            _ = wScroll;
            _ = ITEM_HEIGHT;
            if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
            {
                mFont.tahoma_7b_dark.drawString(g, contenInfo[i], xScroll + 5, num2 + 6, mFont.LEFT);
            }
        }
        paintScrollArrow(g);
    }
    public void setTabBossNotification()
    {
        this.currentListLength = BossCustom.getBossCustom.bosses.Count;
        this.ITEM_HEIGHT = 24;
        this.selected = (GameCanvas.isTouch ? -1 : 0);
        this.cmyLim = this.currentListLength * this.ITEM_HEIGHT - this.hScroll;
        if (this.cmyLim < 0)
        {
            this.cmyLim = 0;
        }
        if (this.cmy < 0)
        {
            this.cmtoY = 0;
            this.cmy = 0;
        }
        if (this.cmy > this.cmyLim)
        {
            this.cmy = (this.cmtoY = this.cmyLim);
        }
    }
    public int indexBoss;
    public void setTabInfoBoss()
    {
        this.currentListLength = BossCustom.getBossCustom.bosses.ElementAt(this.indexBoss).Value.Count;
        this.ITEM_HEIGHT = 48;
        this.selected = (GameCanvas.isTouch ? -1 : 0);
        this.cmyLim = this.currentListLength * this.ITEM_HEIGHT - this.hScroll;
        if (this.cmyLim < 0)
        {
            this.cmyLim = 0;
        }
        if (this.cmy < 0)
        {
            this.cmtoY = 0;
            this.cmy = 0;
        }
        if (this.cmy > this.cmyLim)
        {
            this.cmy = (this.cmtoY = this.cmyLim);
        }
    }
    private void setTypeInfoBoss()//thông báo boss
    {
        this.currentListLength = BossCustom.getBossCustom.bosses.Count;
        this.ITEM_HEIGHT = 24;
        this.selected = (GameCanvas.isTouch ? -1 : 0);
        this.cmyLim = this.currentListLength * this.ITEM_HEIGHT - this.hScroll;
        if (this.cmyLim < 0)
        {
            this.cmyLim = 0;
        }
        if (this.cmy < 0)
        {
            this.cmtoY = 0;
            this.cmy = 0;
        }
        if (this.cmy > this.cmyLim)
        {
            this.cmy = (this.cmtoY = this.cmyLim);
        }
        this.type = 32;
        this.setType(0);
    }
    private void setBossType()
    {
        if (this.selected != -1)
        {
            this.indexBoss = this.selected;
            this.setTypeBossesInfo();
        }
    }
    private void setTypeBossesInfo()
    {
        try
        {
            this.type = 33;
            this.currentListLength = BossCustom.getBossCustom.bosses.ElementAt(this.indexBoss).Value.Count;
            this.ITEM_HEIGHT = 48;
            this.selected = (GameCanvas.isTouch ? -1 : 0);
            this.cmyLim = this.currentListLength * this.ITEM_HEIGHT - this.hScroll;
            if (this.cmyLim < 0)
            {
                this.cmyLim = 0;
            }
            if (this.cmy < 0)
            {
                this.cmtoY = 0;
                this.cmy = 0;
            }
            if (this.cmy > this.cmyLim)
            {
                this.cmy = (this.cmtoY = this.cmyLim);
            }

        }
        catch (Exception e) { UnityEngine.Debug.LogError(e.ToString()); }
    }
    private void paintTabBoss(mGraphics gclass100_0)
    {

        gclass100_0.setClip(this.xScroll, this.yScroll, this.wScroll, this.hScroll);
        gclass100_0.translate(0, -this.cmy);
        List<CustomBossNotification> value = BossCustom.getBossCustom.bosses.ElementAt(this.indexBoss).Value;
        for (int i = 0; i < value.Count; i++)
        {
            CustomBossNotification customBossNotification = value[i];
            int x = this.xScroll;
            int num = this.yScroll + i * this.ITEM_HEIGHT;
            int num2 = this.wScroll - 1;
            int h = this.ITEM_HEIGHT - 1;
            if (num - this.cmy <= this.yScroll + this.hScroll && num - this.cmy >= this.yScroll - this.ITEM_HEIGHT)
            {
                gclass100_0.setColor((i != this.selected) ? 15196114 : 16383818);
                gclass100_0.fillRect(x, num, num2, h);
                mFont.tahoma_7.drawString(gclass100_0, "Xuất hiện: " + customBossNotification.getTimeStartBoss(), this.xScroll + 5, num + 1, 0);
                mFont.tahoma_7.drawString(gclass100_0, "Map: " + customBossNotification.getMapBoss(), this.xScroll + 5, num + 12, 0);
                mFont.tahoma_7.drawString(gclass100_0, "Người tiêu diệt: " + customBossNotification.getBossKiller(), this.xScroll + 5, num + 23, 0);
                mFont.tahoma_7.drawString(gclass100_0, "Thời gian chết: " + customBossNotification.getTimeBossDie(), this.xScroll + 5, num + 34, 0);
            }
        }
        this.paintScrollArrow(gclass100_0);
    }
    private void paintListBosses(mGraphics gclass100_0)
    {
        gclass100_0.setClip(this.xScroll, this.yScroll, this.wScroll, this.hScroll);
        gclass100_0.translate(0, -this.cmy);
        for (int i = 0; i < BossCustom.getBossCustom.bosses.Count; i++)
        {
            int x = this.xScroll;
            int num = this.yScroll + i * this.ITEM_HEIGHT;
            int num2 = this.wScroll - 1;
            int h = this.ITEM_HEIGHT - 1;
            if (num - this.cmy <= this.yScroll + this.hScroll && num - this.cmy >= this.yScroll - this.ITEM_HEIGHT)
            {
                gclass100_0.setColor((i != this.selected) ? 15196114 : 16383818);
                gclass100_0.fillRect(x, num, num2, h);
                mFont.tahoma_7b_dark.drawString(gclass100_0, BossCustom.getBossCustom.bosses.ElementAt(i).Key, this.xScroll + this.wScroll / 2, num + 6, mFont.CENTER);
            }
        }
        this.paintScrollArrow(gclass100_0);
    }

    private void paintGameInfo(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < vGameInfo.size(); i++)
        {
            GameInfo gameInfo = (GameInfo)vGameInfo.elementAt(i);
            int num = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                mFont.tahoma_7b_dark.drawString(g, gameInfo.main, xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
                if (!gameInfo.hasRead && GameCanvas.gameTick % 20 > 10)
                {
                    g.drawImage(imgNew, num + 10, num2 + 10, 3);
                }
            }
        }
        paintScrollArrow(g);
    }

    private void paintSkill(mGraphics g)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        int num = Char.myCharz().nClass.skillTemplates.Length;
        for (int i = 0; i < num + 6; i++)
        {
            int num2 = xScroll + 30;
            int num3 = yScroll + i * ITEM_HEIGHT;
            int num4 = wScroll - 30;
            int h = ITEM_HEIGHT - 1;
            int num5 = xScroll;
            int num6 = yScroll + i * ITEM_HEIGHT;
            _ = ITEM_HEIGHT;
            if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            g.setColor((i != selected) ? 15196114 : 16383818);
            if (i == 5)
            {
                g.setColor((i != selected) ? 16765060 : 16776068);
            }
            g.fillRect(num2, num3, num4, h);
            g.drawImage(GameScr.imgSkill, num5, num6, 0);
            if (i == 0)
            {
                SmallImage.drawSmallImage(g, 567, num5 + 4, num6 + 4, 0, 0);
                string st = mResources.HP + " " + mResources.root + ": " + NinjaUtil.getMoneys((long)Char.myCharz().cHPGoc);
                mFont.tahoma_7b_blue.drawString(g, st, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((long)Char.myCharz().cHPGoc + 1000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().hpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
            }
            if (i == 1)
            {
                SmallImage.drawSmallImage(g, 569, num5 + 4, num6 + 4, 0, 0);
                string st2 = mResources.KI + " " + mResources.root + ": " + NinjaUtil.getMoneys((long)Char.myCharz().cMPGoc);
                mFont.tahoma_7b_blue.drawString(g, st2, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((long)Char.myCharz().cMPGoc + 1000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().mpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
            }
            if (i == 2)
            {
                SmallImage.drawSmallImage(g, 568, num5 + 4, num6 + 4, 0, 0);
                string st3 = mResources.hit_point + " " + mResources.root + ": " + NinjaUtil.getMoneys((long)Char.myCharz().cDamGoc);
                mFont.tahoma_7b_blue.drawString(g, st3, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((long)Char.myCharz().cDamGoc * 100) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().damFrom1000TiemNang, num2 + 5, num3 + 15, 0);
            }
            if (i == 3)
            {
                SmallImage.drawSmallImage(g, 721, num5 + 4, num6 + 4, 0, 0);
                string st4 = mResources.armor + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cDefGoc);
                mFont.tahoma_7b_blue.drawString(g, st4, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(500000 + Char.myCharz().cDefGoc * 100000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().defFrom1000TiemNang, num2 + 5, num3 + 15, 0);
            }
            if (i == 4)
            {
                SmallImage.drawSmallImage(g, 719, num5 + 4, num6 + 4, 0, 0);
                string st5 = mResources.critical + " " + mResources.root + ": " + Char.myCharz().cCriticalGoc + "%";
                int num10 = Char.myCharz().cCriticalGoc;
                if (num10 > t_tiemnang.Length - 1)
                {
                    num10 = t_tiemnang.Length - 1;
                }
                long num11 = t_tiemnang[num10];
                mFont.tahoma_7b_blue.drawString(g, st5, num2 + 5, num3 + 3, 0);
                long number = num11;
                mFont.tahoma_7_green2.drawString(g, Res.formatNumber2(number) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().criticalFrom1000Tiemnang, num2 + 5, num3 + 15, 0);
            }
            if (i == 5)
            {
                if (specialInfo != null)
                {
                    SmallImage.drawSmallImage(g, spearcialImage, num5 + 4, num6 + 4, 0, 0);
                    string[] array = mFont.tahoma_7.splitFontArray(specialInfo, 120);
                    for (int j = 0; j < array.Length; j++)
                    {
                        mFont.tahoma_7_green2.drawString(g, array[j], num2 + 5, num3 + 3 + j * 12, 0);
                    }
                }
                else
                {
                    mFont.tahoma_7_green2.drawString(g, string.Empty, num2 + 5, num3 + 9, 0);
                }
            }
            if (i < 6)
            {
                continue;
            }
            int num12 = i - 6;
            SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[num12];
            SmallImage.drawSmallImage(g, skillTemplate.iconId, num5 + 4, num6 + 4, 0, 0);
            Skill skill = Char.myCharz().getSkill(skillTemplate);
            if (skill != null)
            {
                mFont.tahoma_7b_blue.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_blue.drawString(g, mResources.level + ": " + skill.point, num2 + num4 - 5, num3 + 3, mFont.RIGHT);
                if (skill.point == skillTemplate.maxPoint)
                {
                    mFont.tahoma_7_green2.drawString(g, mResources.max_level_reach, num2 + 5, num3 + 15, 0);
                }
                else if (skill.template.isSkillSpec())
                {
                    string text = mResources.proficiency + ": ";
                    int x = mFont.tahoma_7_green2.getWidthExactOf(text) + num2 + 5;
                    int num13 = num3 + 15;
                    mFont.tahoma_7_green2.drawString(g, text, num2 + 5, num13, 0);
                    mFont.tahoma_7_green2.drawString(g, "(" + skill.strCurExp() + ")", num2 + num4 - 5, num13, mFont.RIGHT);
                    num13 += 4;
                    g.setColor(7169134);
                    g.fillRect(x, num13, 50, 5);
                    int num14 = skill.curExp * 50 / 1000;
                    g.setColor(11992374);
                    g.fillRect(x, num13, num14, 5);
                    if (skill.curExp >= 1000)
                    {
                    }
                }
                else
                {
                    Skill skill2 = skillTemplate.skills[skill.point];
                    mFont.tahoma_7_green2.drawString(g, mResources.level + " " + (skill.point + 1) + " " + mResources.need + " " + Res.formatNumber2(skill2.powRequire) + " " + mResources.potential, num2 + 5, num3 + 15, 0);
                }
            }
            else
            {
                Skill skill3 = skillTemplate.skills[0];
                mFont.tahoma_7b_green.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                mFont.tahoma_7_green2.drawString(g, mResources.need_upper + " " + Res.formatNumber2(skill3.powRequire) + " " + mResources.potential_to_learn, num2 + 5, num3 + 15, 0);
            }
        }
        paintScrollArrow(g);
    }

    private void paintMapTrans(mGraphics g)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < mapNames.Length; i++)
        {
            _ = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            _ = wScroll;
            int h = ITEM_HEIGHT - 1;
            _ = xScroll;
            _ = yScroll;
            _ = ITEM_HEIGHT;
            _ = ITEM_HEIGHT;
            _ = ITEM_HEIGHT;
            if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(xScroll, num2, wScroll, h);
                mFont.tahoma_7b_blue.drawString(g, mapNames[i], 5, num2 + 1, 0);
                mFont.tahoma_7_grey.drawString(g, planetNames[i], 5, num2 + 11, 0);
            }
        }
        paintScrollArrow(g);
    }

    private void paintZone(mGraphics g)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        int[] zones = GameScr.gI().zones;
        int[] pts = GameScr.gI().pts;
        for (int i = 0; i < pts.Length; i++)
        {
            int num = xScroll + 29;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = wScroll - 29;
            int h = ITEM_HEIGHT - 1;
            int num4 = xScroll;
            int y = yScroll + i * ITEM_HEIGHT;
            int num5 = 26;
            int h2 = ITEM_HEIGHT - 1;
            if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num, num2, num3, h);
            g.setColor(zoneColor[pts[i]]);
            g.fillRect(num4, y, num5, h2);
            if (zones[i] != -1)
            {
                if (pts[i] != 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                }
                mFont.tahoma_7_green2.drawString(g, GameScr.gI().numPlayer[i] + "/" + GameScr.gI().maxPlayer[i], num + 5, num2 + 6, 0);
            }
            if (GameScr.gI().rankName1[i] != null)
            {
                mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName1[i] + "(Top " + GameScr.gI().rank1[i] + ")", num + num3 - 2, num2 + 1, mFont.RIGHT);
                mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName2[i] + "(Top " + GameScr.gI().rank2[i] + ")", num + num3 - 2, num2 + 11, mFont.RIGHT);
            }
        }
        paintScrollArrow(g);
    }

    private void paintSpeacialSkill(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (currentListLength == 0)
        {
            return;
        }
        int num = (cmy + hScroll) / 24 + 1;
        if (num < hScroll / 24 + 1)
        {
            num = hScroll / 24 + 1;
        }
        if (num > currentListLength)
        {
            num = currentListLength;
        }
        int num2 = cmy / 24;
        if (num2 >= num)
        {
            num2 = num - 1;
        }
        if (num2 < 0)
        {
            num2 = 0;
        }
        for (int i = num2; i < num; i++)
        {
            int num3 = xScroll;
            int num4 = yScroll + i * ITEM_HEIGHT;
            int num5 = 24;
            int num6 = ITEM_HEIGHT - 1;
            int num7 = xScroll + num5;
            int num8 = yScroll + i * ITEM_HEIGHT;
            int num9 = wScroll - num5;
            int h = ITEM_HEIGHT - 1;
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num7, num8, num9, h);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num3, num4, num5, num6);
            SmallImage.drawSmallImage(g, Char.myCharz().imgSpeacialSkill[currentTabIndex][i], num3 + num5 / 2, num4 + num6 / 2, 0, 3);
            string[] array = mFont.tahoma_7_grey.splitFontArray(Char.myCharz().infoSpeacialSkill[currentTabIndex][i], 140);
            for (int j = 0; j < array.Length; j++)
            {
                mFont.tahoma_7_grey.drawString(g, array[j], num7 + 5, num8 + 1 + j * 11, 0);
            }
        }
        paintScrollArrow(g);
    }

    private void paintBox222222(mGraphics g)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        try
        {
            Item[] arrItemBox = Char.myCharz().arrItemBox;
            currentListLength = checkCurrentListLength(arrItemBox.Length);
            int num = arrItemBox.Length / 20 + ((arrItemBox.Length % 20 > 0) ? 1 : 0);
            TAB_W_NEW = wScroll / num;
            for (int i = 0; i < currentListLength; i++)
            {
                int num2 = xScroll + 30;
                int num3 = yScroll + i * ITEM_HEIGHT;
                int num4 = wScroll - 30;
                int h = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = ITEM_HEIGHT - 1;
                int num8 = ITEM_HEIGHT - 1;
                if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                if (i == 0)
                {
                    for (int j = 0; j < num; j++)
                    {
                        int num9 = ((j == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                        g.setColor((j != newSelected) ? 15723751 : 16383818);
                        g.fillRect(xScroll + j * TAB_W_NEW, num3 + num9, TAB_W_NEW - 1, 20);
                        mFont.tahoma_7_grey.drawString(g, string.Empty + j, xScroll + j * TAB_W_NEW + TAB_W_NEW / 2, yScroll + num9 + 2, mFont.CENTER);
                    }
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num2, num3, num4, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                int inventorySelect_body = GetInventorySelect_body(i, newSelected);
                Item item = arrItemBox[inventorySelect_body];
                if (item != null)
                {
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                        {
                            sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[k].param);
                            if (GetColor_ItemBg(color_Item_Upgrade) != -1)
                            {
                                g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                            }
                        }
                    }
                }
                g.setColor(6047789, 0.5f);
                g.fillRect(num5, num6, num7, num8);
                paintEffectItem(g, item, num5, num6);
                if (item == null)
                {
                    continue;
                }
                string text = string.Empty;
                mFont mFont2 = mFont.tahoma_7_green2;
                if (item.itemOption != null)
                {
                    for (int l = 0; l < item.itemOption.Length; l++)
                    {
                        if (item.itemOption[l].optionTemplate.id == 72)
                        {
                            text = " [+" + item.itemOption[l].getOptionString() + "]";
                        }
                        if (item.itemOption[l].optionTemplate.id == 225)
                        {
                            if (item.itemOption[l].param >= 1 && item.itemOption[l].param <= 2)
                            {
                                mFont2 = GetFont(0);
                            }
                            else if (item.itemOption[l].param >= 3 && item.itemOption[l].param <= 4)
                            {
                                mFont2 = GetFont(2);
                            }
                            else if (item.itemOption[l].param >= 5 && item.itemOption[l].param <= 6)
                            {
                                mFont2 = GetFont(8);
                            }
                            else if (item.itemOption[l].param >= 7 && item.itemOption[l].param <= 10)
                            {
                                mFont2 = GetFont(7);
                            }
                        }
                    }
                }
                if (ModFunc.isShowID)
                {
                    mFont2.drawString(g, "[" + item.template.id + "] " + item.template.name + text, num2 + 5, num3 + 1, 0);
                }
                else
                {
                    mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                }
                string text2 = string.Empty;
                if (item.itemOption != null)
                {
                    if (item.itemOption.Length != 0 && item.itemOption[0] != null)
                    {
                        text2 += item.itemOption[0].getOptionString();
                    }
                    mFont mFont3 = mFont.tahoma_7_blue;
                    if (item.compare < 0 && item.template.type != 5)
                    {
                        mFont3 = mFont.tahoma_7_red;
                    }
                    if (item.itemOption.Length > 1)
                    {
                        for (int m = 1; m < Math.min(item.itemOption.Length, 3); m++)
                        {
                            if (item.itemOption[m] != null && item.itemOption[m].IsValidOption())
                            {
                                text2 = text2 + ", " + item.itemOption[m].getOptionString();
                            }
                        }
                    }
                    mFont3.drawString(g, text2, num2 + 5, num3 + 10, mFont.LEFT);
                }
                SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                if (item.itemOption != null)
                {
                    for (int n = 0; n < item.itemOption.Length; n++)
                    {
                        paintOptItemInventory(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8, item);
                    }
                    for (int num10 = 0; num10 < item.itemOption.Length; num10++)
                    {
                        paintOptSlotItem(g, item.itemOption[num10].optionTemplate.id, item.itemOption[num10].param, num5, num6, num7, num8);
                    }
                }
                if (item.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
        }
        catch (Exception)
        {
        }
        paintScrollArrow(g);
    }



    private void paintBox(mGraphics g)
    {
        if (!ModFunc.isInventory)
        {
            paintBox222222(g);
            return;
        }

        g.setColor(11771523); // màu nền tổng thể
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);

        try
        {
            Item[] arrItemBox = Char.myCharz().arrItemBox;
            currentListLength = checkCurrentListLength(arrItemBox.Length / 6);

            // Cập nhật cmyLim nếu currentListLength thay đổi
            int newCmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (newCmyLim < 0)
            {
                newCmyLim = 0;
            }
            if (newCmyLim != cmyLim)
            {
                cmyLim = newCmyLim;
                // Đảm bảo cmy không vượt quá giới hạn mới
                if (cmy > cmyLim)
                {
                    cmy = cmyLim;
                    cmtoY = cmyLim;
                }
            }

            TAB_W_NEW = 1;
            int columns = 6;
            int itemWidth = 32;
            int itemHeight = ITEM_HEIGHT;

            for (int i = 0; i < arrItemBox.Length; i++)
            {
                int row = i / columns;
                int col = i % columns;
                int x = xScroll + col * (itemWidth + 1);
                int y = yScroll + row * itemHeight;
                int w = itemWidth;
                int h = itemHeight - 1;
                if (y - cmy > yScroll + hScroll || y - cmy < yScroll - itemHeight)
                    continue;
                Item item = arrItemBox[i];
                g.setColor(11771523);
                g.fillRect(x, y, w, h, 4);
                if (item != null)
                {
                    g.setColor(10587766);
                    g.fillRect(x + 2, y + 2, w - 4, h - 4, 4);

                    // Nếu có nâng cấp (option ID = 72)
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                        {
                            byte idColor = (byte)GetColor_Item_Upgrade(item.itemOption[k].param);
                            if (GetColor_ItemBg(idColor) != -1)
                            {
                                g.setColor(GetColor_ItemBg(idColor));
                                g.fillRect(x, y, w, h, 4);
                            }
                        }
                    }
                    paintEffectItem(g, item, x, y);
                    SmallImage.drawSmallImage(g, item.template.iconID, x + w / 2, y + h / 2, 0, mGraphics.VCENTER | mGraphics.HCENTER);

                    if (item.itemOption != null)
                    {
                        for (int n = 0; n < item.itemOption.Length; n++)
                        {
                            paintOptItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, x, y, w, h);
                            paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, x, y, w, h);
                        }
                    }
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, NinjaUtil.getMoneys(item.quantity),
                            x + w, y + h - mFont.tahoma_7_yellow.getHeight(), mFont.RIGHT, mFont.tahoma_7_yellow);
                    }
                }

                if (i == selected)
                {
                    g.setColor(0xCC6600); // RGB ~ vàng cam đậm
                    g.drawRect(x - 2, y - 2, w + 3, h + 3);

                    g.setColor(0xFFCC66); // RGB ~ vàng sáng nổi bật
                    g.drawRect(x - 1, y - 1, w + 1, h + 1);
                }
            }
        }
        catch (Exception e)
        {
            Debug.LogException(e);
        }

        paintScrollArrow(g);
    }


    public Member getCurrMember()
    {
        if (selected < 2)
        {
            return null;
        }
        if (selected > ((member == null) ? myMember.size() : member.size()) + 1)
        {
            return null;
        }
        if (member != null)
        {
            return (Member)member.elementAt(selected - 2);
        }
        return (Member)myMember.elementAt(selected - 2);
    }

    public ClanMessage getCurrMessage()
    {
        if (selected < 2)
        {
            return null;
        }
        if (selected > ClanMessage.vMessage.size() + 1)
        {
            return null;
        }
        return (ClanMessage)ClanMessage.vMessage.elementAt(selected - 2);
    }

    public Clan getCurrClan()
    {
        if (selected < 2)
        {
            return null;
        }
        if (selected > clans.Length + 1)
        {
            return null;
        }
        return clans[selected - 2];
    }


    private void paintLogChat(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (logChat.size() == 0)
        {
            mFont.tahoma_7_green2.drawString(g, mResources.no_msg, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2 + 24, 2);
        }
        for (int i = 0; i < currentListLength; i++)
        {
            int num = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = 24;
            int h = ITEM_HEIGHT - 1;
            int num4 = xScroll + num3;
            int num5 = yScroll + i * ITEM_HEIGHT;
            int num6 = wScroll - num3;
            int num7 = ITEM_HEIGHT - 1;
            if (i == 0)
            {
                g.setColor(15196114);
                g.fillRect(num, num5, wScroll, num7);
                g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num5 + 2, StaticObj.TOP_RIGHT);
                ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, (!isViewChatServer) ? mResources.on : mResources.off, xScroll + wScroll - 22, num5 + 7, 2);
                mFont.tahoma_7_grey.drawString(g, (!isViewChatServer) ? mResources.onPlease : mResources.offPlease, xScroll + 5, num5 + num7 / 2 - 4, mFont.LEFT);
                continue;
            }
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num4, num5, num6, num7);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num, num2, num3, h);
            InfoItem infoItem = (InfoItem)logChat.elementAt(i - 1);
            if (infoItem.charInfo.headICON != -1)
            {
                SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
            }
            else
            {
                Part part = GameScr.parts[infoItem.charInfo.head];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
            }
            g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
            mFont tahoma_7b_dark = mFont.tahoma_7b_dark;
            tahoma_7b_dark = mFont.tahoma_7b_green2;
            tahoma_7b_dark.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
            if (!infoItem.isChatServer)
            {
                mFont.tahoma_7_blue.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
            }
            else
            {
                mFont.tahoma_7_red.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
            }
        }
        paintScrollArrow(g);
    }

    private void paintFlagChange(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        for (int i = 0; i < currentListLength; i++)
        {
            int num = xScroll + 26;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = wScroll - 26;
            int h = ITEM_HEIGHT - 1;
            int num4 = xScroll;
            int num5 = yScroll + i * ITEM_HEIGHT;
            int num6 = 24;
            int num7 = ITEM_HEIGHT - 1;
            if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num, num2, num3, h);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num4, num5, num6, num7);
            Item item = (Item)vFlag.elementAt(i);
            if (item == null)
            {
                continue;
            }
            mFont.tahoma_7_green2.drawString(g, item.template.name, num + 5, num2 + 1, 0);
            string text = string.Empty;
            if (item.itemOption != null && item.itemOption.Length >= 1)
            {
                if (item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                {
                    text += item.itemOption[0].getOptionString();
                }
                mFont.tahoma_7_blue.drawString(g, text, num + 5, num2 + 11, 0);
                SmallImage.drawSmallImage(g, item.template.iconID, num4 + num6 / 2, num5 + num7 / 2, 0, 3);
            }
        }
        paintScrollArrow(g);
    }

    private void paintEnemy(mGraphics g)
    {
        // Ensure currentListLength is correct before painting
        int actualSize = vEnemy.size();
        currentListLength = actualSize;
        cmyLim = actualSize * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (currentListLength == 0)
        {
            mFont.tahoma_7_green2.drawString(g, mResources.no_enemy, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
            return;
        }
        for (int i = 0; i < currentListLength; i++)
        {
            int num = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = 24;
            int h = ITEM_HEIGHT - 1;
            int num4 = xScroll + num3;
            int num5 = yScroll + i * ITEM_HEIGHT;
            int num6 = wScroll - num3;
            int h2 = ITEM_HEIGHT - 1;
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num4, num5, num6, h2);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num, num2, num3, h);
            InfoItem infoItem = (InfoItem)vEnemy.elementAt(i);
            if (infoItem.charInfo.headICON != -1)
            {
                SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
            }
            else
            {
                Part part = GameScr.parts[infoItem.charInfo.head];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
            }
            g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
            if (infoItem.isOnline)
            {
                mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
            }
            else
            {
                mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
            }
        }
        paintScrollArrow(g);
    }

    private void paintFriend(mGraphics g)
    {
        // Ensure currentListLength is correct before painting
        int actualSize = vFriend.size();
        currentListLength = actualSize;
        cmyLim = actualSize * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (currentListLength == 0)
        {
            mFont.tahoma_7_green2.drawString(g, mResources.no_friend, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
            return;
        }
        for (int i = 0; i < currentListLength; i++)
        {
            int num = xScroll;
            int num2 = yScroll + i * ITEM_HEIGHT;
            int num3 = 24;
            int h = ITEM_HEIGHT - 1;
            int num4 = xScroll + num3;
            int num5 = yScroll + i * ITEM_HEIGHT;
            int num6 = wScroll - num3;
            int h2 = ITEM_HEIGHT - 1;
            g.setColor((i != selected) ? 15196114 : 16383818);
            g.fillRect(num4, num5, num6, h2);
            g.setColor((i != selected) ? 9993045 : 9541120);
            g.fillRect(num, num2, num3, h);
            InfoItem infoItem = (InfoItem)vFriend.elementAt(i);
            if (infoItem.charInfo.headICON != -1)
            {
                SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
            }
            else
            {
                Part part = GameScr.parts[infoItem.charInfo.head];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
            }
            g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
            if (infoItem.isOnline)
            {
                mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
            }
            else
            {
                mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
            }
        }
        paintScrollArrow(g);
    }

    public void paintPlayerMenu(mGraphics g)
    {
        // Ensure currentListLength is correct before painting
        int actualSize = vPlayerMenu.size();
        currentListLength = actualSize;
        cmyLim = actualSize * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < vPlayerMenu.size(); i++)
        {
            int x = xScroll;
            int num = yScroll + i * ITEM_HEIGHT;
            int num2 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
            {
                Command command = (Command)vPlayerMenu.elementAt(i);
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(x, num, num2, h);
                if (command.caption2.Equals(string.Empty))
                {
                    mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 6, mFont.CENTER);
                    continue;
                }
                mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 1, mFont.CENTER);
                mFont.tahoma_7b_dark.drawString(g, command.caption2, xScroll + wScroll / 2, num + 11, mFont.CENTER);
            }
        }
        paintScrollArrow(g);
    }

    private void paintClans(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(-cmx, -cmy);
        g.setColor(0);
        int num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
        if (currentListLength == 2)
        {
            mFont.tahoma_7_green2.drawString(g, clanReport, xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
            if (isMessage && myMember.size() == 1)
            {
                for (int i = 0; i < mResources.clanEmpty.Length; i++)
                {
                    mFont.tahoma_7b_dark.drawString(g, mResources.clanEmpty[i], xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mResources.clanEmpty.Length * 12 / 2 + i * 12, mFont.CENTER);
                }
            }
        }
        if (isMessage)
        {
            currentListLength = ClanMessage.vMessage.size() + 2;
        }
        for (int j = 0; j < currentListLength; j++)
        {
            int num2 = xScroll;
            int num3 = yScroll + j * ITEM_HEIGHT;
            int num4 = 24;
            int num5 = ITEM_HEIGHT - 1;
            int num6 = xScroll + num4;
            int num7 = yScroll + j * ITEM_HEIGHT;
            int num8 = wScroll - num4;
            int num9 = ITEM_HEIGHT - 1;
            if (num7 - cmy > yScroll + hScroll || num7 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            switch (j)
            {
                case 0:
                    {
                        for (int k = 0; k < clansOption.Length; k++)
                        {
                            g.setColor((k != cSelected || j != selected) ? 15723751 : 16383818);
                            g.fillRect(num + k * TAB_W, num7, TAB_W - 1, 23);
                            for (int l = 0; l < clansOption[k].Length; l++)
                            {
                                mFont.tahoma_7_grey.drawString(g, clansOption[k][l], num + k * TAB_W + TAB_W / 2, yScroll + l * 11, mFont.CENTER);
                            }
                        }
                        continue;
                    }
                case 1:
                    g.setColor((j != selected) ? 15196114 : 16383818);
                    g.fillRect(xScroll, num7, wScroll, num9);
                    if (clanInfo != null)
                    {
                        mFont.tahoma_7b_dark.drawString(g, clanInfo, xScroll + wScroll / 2, num7 + 6, mFont.CENTER);
                    }
                    continue;
            }
            if (isSearchClan)
            {
                if (clans == null || clans.Length == 0)
                {
                    continue;
                }
                g.setColor((j != selected) ? 15196114 : 16383818);
                g.fillRect(num6, num7, num8, num9);
                g.setColor((j != selected) ? 9993045 : 9541120);
                g.fillRect(num2, num3, num4, num5);
                if (ClanImage.isExistClanImage(clans[j - 2].imgID))
                {
                    if (ClanImage.getClanImage((short)clans[j - 2].imgID).idImage != null)
                    {
                        SmallImage.drawSmallImage(g, ClanImage.getClanImage((short)clans[j - 2].imgID).idImage[0], num2 + num4 / 2, num3 + num5 / 2, 0, StaticObj.VCENTER_HCENTER);
                    }
                }
                else
                {
                    ClanImage clanImage = new ClanImage();
                    clanImage.ID = clans[j - 2].imgID;
                    if (!ClanImage.isExistClanImage(clanImage.ID))
                    {
                        ClanImage.addClanImage(clanImage);
                    }
                }
                string st = ((clans[j - 2].name.Length <= 23) ? clans[j - 2].name : (clans[j - 2].name.Substring(0, 23) + "..."));
                mFont.tahoma_7b_green2.drawString(g, st, num6 + 5, num7, 0);
                g.setClip(num6, num7, num8 - 10, num9);
                mFont.tahoma_7_blue.drawString(g, clans[j - 2].slogan, num6 + 5, num7 + 11, 0);
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                mFont.tahoma_7_green2.drawString(g, clans[j - 2].currMember + "/" + clans[j - 2].maxMember, num6 + num8 - 5, num7, mFont.RIGHT);
                continue;
            }
            if (isViewMember)
            {
                g.setColor((j != selected) ? 15196114 : 16383818);
                g.fillRect(num6, num7, num8, num9);
                g.setColor((j != selected) ? 9993045 : 9541120);
                g.fillRect(num2, num3, num4, num5);
                Member member = ((this.member == null) ? ((Member)myMember.elementAt(j - 2)) : ((Member)this.member.elementAt(j - 2)));
                if (member.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, member.headICON, num2, num3, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[member.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num2 + part.pi[Char.CharInfo[0][0][0]].dx, num3 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                mFont mFont2 = mFont.tahoma_7b_dark;
                if (member.role == 0)
                {
                    mFont2 = mFont.tahoma_7b_red;
                }
                else if (member.role == 1)
                {
                    mFont2 = mFont.tahoma_7b_green;
                }
                else if (member.role == 2)
                {
                    mFont2 = mFont.tahoma_7b_green2;
                }
                mFont2.drawString(g, member.name, num6 + 5, num7, 0);
                mFont.tahoma_7_blue.drawString(g, mResources.power + ": " + member.powerPoint, num6 + 5, num7 + 11, 0);
                SmallImage.drawSmallImage(g, 7223, num6 + num8 - 7, num7 + 12, 0, 3);
                mFont.tahoma_7_blue.drawString(g, string.Empty + member.clanPoint, num6 + num8 - 15, num7 + 6, mFont.RIGHT);
                continue;
            }
            if (!isMessage || ClanMessage.vMessage.size() == 0)
            {
                continue;
            }
            ClanMessage clanMessage = (ClanMessage)ClanMessage.vMessage.elementAt(j - 2);
            g.setColor((j != selected || clanMessage.option != null) ? 15196114 : 16383818);
            g.fillRect(num2, num3, num8 + num4, num9);
            clanMessage.paint(g, num2, num3);
            if (clanMessage.option == null)
            {
                continue;
            }
            int num10 = xScroll + wScroll - 2 - clanMessage.option.Length * 40;
            for (int m = 0; m < clanMessage.option.Length; m++)
            {
                if (m == cSelected && j == selected)
                {
                    g.drawImage(GameScr.imgLbtnFocus2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                    mFont.tahoma_7b_green2.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                }
                else
                {
                    g.drawImage(GameScr.imgLbtn2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                    mFont.tahoma_7b_dark.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                }
            }
        }
        paintScrollArrow(g);
    }

    private void paintArchivement(mGraphics g)
    {
        // Ensure currentListLength is correct before painting
        if (Char.myCharz().arrArchive != null)
        {
            int actualSize = Char.myCharz().arrArchive.Length;
            currentListLength = actualSize;
            cmyLim = actualSize * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
        }
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        g.setColor(0);
        if (currentListLength == 0)
        {
            mFont.tahoma_7_green2.drawString(g, mResources.no_mission, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
        }
        else
        {
            if (Char.myCharz().arrArchive == null || Char.myCharz().arrArchive.Length != currentListLength)
            {
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll;
                int num4 = ITEM_HEIGHT - 1;
                Archivement archivement = Char.myCharz().arrArchive[i];
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, num4);
                if (archivement != null)
                {
                    if (!archivement.isFinish)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_red.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                        mFont.tahoma_7_green.drawString(g, archivement.money + " Ngọc Xanh", num + num3 - 5, num2, mFont.RIGHT);
                    }
                    else if (archivement.isFinish && !archivement.isRecieve)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_blue.drawString(g, mResources.reward_mission + archivement.money + " Ngọc Xanh", num + 5, num2 + 11, 0);
                        g.drawImage((i == selected) ? GameScr.imgLbtnFocus2 : GameScr.imgLbtn2, num + num3 - 20, num2 + num4 / 2, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_dark.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                    }
                    else if (archivement.isFinish && archivement.isRecieve)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_red.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                        mFont.tahoma_7_green.drawString(g, mResources.received, num + num3 - 5, num2, mFont.RIGHT);
                    }
                }
            }
            paintScrollArrow(g);
        }
    }

    private void paintCombine(mGraphics g)
    {
        g.setColor(16711680);
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        if (vItemCombine.size() == 0)
        {
            if (combineInfo != null)
            {
                for (int i = 0; i < combineInfo.Length; i++)
                {
                    mFont.tahoma_7b_dark.drawString(g, combineInfo[i], xScroll + wScroll / 2, yScroll + hScroll / 2 - combineInfo.Length * 14 / 2 + i * 14 + 5, 2);
                }
            }
            return;
        }
        for (int j = 0; j < vItemCombine.size() + 1; j++)
        {
            int num = xScroll + 29;
            int num2 = yScroll + j * ITEM_HEIGHT;
            int num3 = wScroll - 29;
            int num4 = ITEM_HEIGHT - 1;
            int num5 = xScroll;
            int num6 = yScroll + j * ITEM_HEIGHT;
            int num7 = ITEM_HEIGHT - 1;
            int num8 = ITEM_HEIGHT - 1;
            if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
            {
                continue;
            }
            if (j == vItemCombine.size())
            {
                if (vItemCombine.size() > 0)
                {
                    if (!GameCanvas.isTouch && j == selected)
                    {
                        g.setColor(16383818);
                        g.fillRect(num5, num2, wScroll, num4 + 2);
                    }
                    if ((j == selected && keyTouchCombine == 1) || (!GameCanvas.isTouch && j == selected))
                    {
                        g.drawImage(GameScr.imgLbtnFocus, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_green2.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    else
                    {
                        g.drawImage(GameScr.imgLbtn, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                }
                continue;
            }
            g.setColor((j != selected) ? 15196114 : 16383818);
            g.fillRect(num, num2, num3, num4);
            g.setColor((j != selected) ? 9993045 : 9541120);
            Item item = (Item)vItemCombine.elementAt(j);
            if (item != null)
            {
                for (int k = 0; k < item.itemOption.Length; k++)
                {
                    if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                    {
                        sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[k].param);
                        if (GetColor_ItemBg(color_Item_Upgrade) != -1)
                        {
                            g.setColor((j != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                        }
                    }
                }
            }
            g.fillRect(num5, num6, num7, num8);
            paintEffectItem(g, item, num5, num6);
            if (item == null)
            {
                continue;
            }
            string text = string.Empty;
            mFont mFont2 = mFont.tahoma_7_green2;
            if (item.itemOption != null)
            {
                for (int l = 0; l < item.itemOption.Length; l++)
                {
                    if (item.itemOption[l].optionTemplate.id == 72)
                    {
                        text = " [+" + item.itemOption[l].getOptionString() + "]";
                    }
                    if (item.itemOption[l].optionTemplate.id == 225)
                    {
                        text = " [+" + item.itemOption[l].getOptionString() + "]";
                    }
                    if (item.itemOption[l].optionTemplate.id == 225)
                    {
                        if (item.itemOption[l].param >= 1 && item.itemOption[l].param <= 2)
                        {
                            mFont2 = GetFont(0);
                        }
                        else if (item.itemOption[l].param >= 3 && item.itemOption[l].param <= 4)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[l].param >= 5 && item.itemOption[l].param <= 6)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[l].param >= 7 && item.itemOption[l].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                    if (item.itemOption[l].optionTemplate.id == 72)
                    {
                        if (item.itemOption[l].param >= 1 && item.itemOption[l].param <= 5)
                        {
                            mFont2 = GetFont(2);
                        }
                        else if (item.itemOption[l].param >= 6 && item.itemOption[l].param <= 7)
                        {
                            mFont2 = GetFont(8);
                        }
                        else if (item.itemOption[l].param >= 8 && item.itemOption[l].param <= 10)
                        {
                            mFont2 = GetFont(7);
                        }
                    }
                }
            }
            if (ModFunc.isShowID)
            {
                mFont2.drawString(g, "[" + item.template.id + "] " + item.template.name + text, num + 5, num2 + 1, 0);
            }
            else
            {
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
            }
            string text2 = string.Empty;
            if (item.itemOption != null)
            {
                if (item.itemOption.Length != 0 && item.itemOption[0] != null && item.itemOption[0].IsValidOption())
                {
                    text2 += item.itemOption[0].getOptionString();
                }
                mFont mFont3 = mFont.tahoma_7_blue;
                if (item.compare < 0 && item.template.type != 5)
                {
                    mFont3 = mFont.tahoma_7_red;
                }
                if (item.itemOption.Length > 1)
                {
                    for (int m = 1; m < item.itemOption.Length; m++)
                    {
                        if (item.itemOption[m] != null && item.itemOption[m].IsValidOption())
                        {
                            text2 = text2 + ", " + item.itemOption[m].getOptionString();
                        }
                    }
                }
                mFont3.drawString(g, text2, num + 5, num2 + 10, mFont.LEFT);
            }
            SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
            if (item.itemOption != null)
            {
                for (int n = 0; n < item.itemOption.Length; n++)
                {
                    paintOptItemInventory(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8, item);
                }
                for (int num9 = 0; num9 < item.itemOption.Length; num9++)
                {
                    paintOptSlotItem(g, item.itemOption[num9].optionTemplate.id, item.itemOption[num9].param, num5, num6, num7, num8);
                }
            }
            if (item.quantity > 1)
            {
                mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
            }
        }
        paintScrollArrow(g);
    }

    private void paintInventoryNormal(mGraphics g)
    {
        // Reload background mỗi lần vẽ inventory để đảm bảo đúng gender
        LoadInventoryBackground();

        g.setColor(16711680);
        Item[] arrItemBody2 = Char.myCharz().arrItemBody;
        Item[] arrItemBag2 = Char.myCharz().arrItemBag;
        currentListLength = checkCurrentListLength(arrItemBody2.Length + arrItemBag2.Length);
        int num18 = (arrItemBody2.Length + arrItemBag2.Length) / 20 + (((arrItemBody2.Length + arrItemBag2.Length) % 20 > 0) ? 1 : 0);
        TAB_W_NEW = wScroll / num18;
        for (int i = 0; i < num18; i++)
        {
            int num20 = ((i == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
            g.setColor((i != newSelected) ? 15723751 : 16383818);
            g.fillRect(xScroll + i * TAB_W_NEW, 89 + num20 - 10, TAB_W_NEW - 1, 21);
            if (i == newSelected)
            {
                g.setColor(13524492);
                int x3 = xScroll + i * TAB_W_NEW;
                int num21 = 89 + num20 - 10 + 21;
                g.fillRect(x3, num21 - 3, TAB_W_NEW - 1, 3);
            }
            mFont.tahoma_7_grey.drawString(g, string.Empty + (i + 1), xScroll + i * TAB_W_NEW + TAB_W_NEW / 2, 91 + num20 - 10, mFont.CENTER);
        }
        g.setClip(xScroll, yScroll + 21, wScroll, hScroll - 21);
        g.translate(0, -cmy);
        try
        {
            for (int j = 1; j < currentListLength; j++)
            {
                int num23 = xScroll + ITEM_HEIGHT;
                int num24 = yScroll + j * ITEM_HEIGHT;
                int num25 = wScroll - ITEM_HEIGHT;
                int h2 = ITEM_HEIGHT - 1;
                int num26 = xScroll;
                int num27 = yScroll + j * ITEM_HEIGHT;
                int num28 = ITEM_HEIGHT;
                int num29 = ITEM_HEIGHT - 1;
                if (num24 - cmy > yScroll + hScroll || num24 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                bool inventorySelect_isbody = GetInventorySelect_isbody(j, newSelected, Char.myCharz().arrItemBody);
                int inventorySelect_body = GetInventorySelect_body(j, newSelected);
                int inventorySelect_bag = GetInventorySelect_bag(j, newSelected, Char.myCharz().arrItemBody);
                g.setColor((j == selected) ? 16383818 : ((!inventorySelect_isbody) ? 15723751 : 15196114));
                g.fillRect(num23, num24, num25, h2, 5);
                g.setColor((j == selected) ? 9541120 : ((!inventorySelect_isbody) ? 11837316 : 9993045));
                Item item3 = ((!inventorySelect_isbody) ? arrItemBag2[inventorySelect_bag] : arrItemBody2[inventorySelect_body]);
                if (item3 != null)
                {
                    for (int k = 0; k < item3.itemOption.Length; k++)
                    {
                        if (item3.itemOption[k].optionTemplate.id == 72 && item3.itemOption[k].param > 0)
                        {
                            byte id = (byte)GetColor_Item_Upgrade(item3.itemOption[k].param);
                            if (GetColor_ItemBg(id) != -1)
                            {
                                g.setColor((j != selected) ? GetColor_ItemBg(id) : GetColor_ItemBg(id));
                            }
                        }
                    }
                }
                g.fillRect(num26, num27, num28, num29, 5);
                if (item3 != null && item3.isSelect && GameCanvas.panel.type == 12)
                {
                    g.setColor((j != selected) ? 6047789 : 7040779);
                    g.fillRect(num26, num27, num28, num29, 5);
                }
                if (item3 != null && ModFunc.GI().listItemAuto.Any((ItemAuto itemAuto) => item3.template.id == itemAuto.id && item3.template.iconID == itemAuto.iconID))
                {
                    g.setColor(52224);
                    g.fillRect(num26, num27, num28, num29, 5);
                }
                if (item3 != null && ModFunc.listFilterItems.Exists((ItemAutoFilter filterItem) => filterItem.id == item3.template.id))
                {
                    g.setColor(16711680);
                    g.fillRect(num26, num27, num28, num29, 5);
                }
                if (item3 == null)
                {
                    continue;
                }
                paintEffectItem(g, item3, num26, num27);
                string text3 = string.Empty;
                mFont mFont4 = mFont.tahoma_7_green2;
                if (item3.itemOption != null)
                {
                    for (int l = 0; l < item3.itemOption.Length; l++)
                    {
                        if (item3.itemOption[l].optionTemplate.id == 72)
                        {
                            text3 = " [+" + item3.itemOption[l].param + "]";
                        }
                        if (item3.itemOption[l].optionTemplate.id == 41)
                        {
                            if (item3.itemOption[l].param == 1)
                            {
                                mFont4 = GetFont(0);
                            }
                            else if (item3.itemOption[l].param == 2)
                            {
                                mFont4 = GetFont(2);
                            }
                            else if (item3.itemOption[l].param == 3)
                            {
                                mFont4 = GetFont(8);
                            }
                            else if (item3.itemOption[l].param == 4)
                            {
                                mFont4 = GetFont(7);
                            }
                        }
                    }
                }
                if (ModFunc.isShowID)
                {
                    mFont4.drawString(g, "[" + item3.template.id + "] " + item3.template.name + text3, num23 + 5, num24 + 1, 0);
                }
                else
                {
                    mFont4.drawString(g, item3.template.name + text3, num23 + 5, num24 + 1, 0);
                }
                string text4 = string.Empty;
                if (item3.itemOption != null)
                {
                    if (item3.itemOption.Length != 0 && item3.itemOption[0] != null && item3.itemOption[0].optionTemplate.id != 102 && item3.itemOption[0].optionTemplate.id != 107)
                    {
                        text4 += item3.itemOption[0].getOptionString();
                    }
                    mFont mFont5 = mFont.tahoma_7_blue;
                    if (item3.compare < 0 && item3.template.type != 5)
                    {
                        mFont5 = mFont.tahoma_7_red;
                    }
                    if (item3.itemOption.Length > 1)
                    {
                        for (int m = 1; m < 2; m++)
                        {
                            if (item3.itemOption[m] != null && item3.itemOption[m].optionTemplate.id != 102 && item3.itemOption[m].optionTemplate.id != 107)
                            {
                                text4 = text4 + "," + item3.itemOption[m].getOptionString();
                            }
                        }
                    }
                    mFont5.drawString(g, text4, num23 + 5, num24 + 11, mFont.LEFT);
                }
                SmallImage.drawSmallImage(g, item3.template.iconID, num26 + num28 / 2, num27 + num29 / 2, 0, 3);
                if (item3.itemOption != null)
                {
                    for (int n = 0; n < item3.itemOption.Length; n++)
                    {
                        paintOptItem(g, item3.itemOption[n].optionTemplate.id, item3.itemOption[n].param, num26, num27, num28, num29);
                    }
                    for (int num30 = 0; num30 < item3.itemOption.Length; num30++)
                    {
                        paintOptSlotItem(g, item3.itemOption[num30].optionTemplate.id, item3.itemOption[num30].param, num26, num27, num28, num29);
                    }
                }
                if (item3.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item3.quantity, num26 + num28, num27 + num29 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
        }
        catch (Exception)
        {
        }
        paintScrollArrow(g);
    }

    private void paintInventory(mGraphics g)
    {
        if (!ModFunc.isInventory)
        {
            paintInventoryNormal(g);
            return;
        }
        LoadInventoryBackground();

        g.setColor(16711680);
        Item[] arrItemBody = Char.myCharz().arrItemBody;
        Item[] arrItemBag = Char.myCharz().arrItemBag;
        int itemBoxHeight = 28;
        int gap = 2;
        int bodyH = itemBoxHeight + gap;
        int bodyItemsPerColumn = (arrItemBody.Length + 1) / 2; // Số item mỗi cột body

        // Chiều cao body: bodyItemsPerColumn hàng
        int bodyHeight = bodyItemsPerColumn * bodyH;

        // Khoảng cách giữa body và bag
        int bodyBagGap = 10;

        // Tính số hàng bag cần thiết (mỗi hàng 6 items)
        int bagRows = (arrItemBag.Length + 5) / 6; // Làm tròn lên
        if (bagRows < 0) bagRows = 0;

        // Chiều cao bag: số hàng * ITEM_HEIGHT
        int bagHeight = bagRows * ITEM_HEIGHT;

        int totalContentHeight = bodyHeight + bodyBagGap + bagHeight;

        int newCmyLim = totalContentHeight - hScroll;
        if (newCmyLim < 0)
        {
            newCmyLim = 0;
        }

        if (newCmyLim != cmyLim)
        {
            cmyLim = newCmyLim;
            if (cmy > cmyLim)
            {
                cmy = cmyLim;
                cmtoY = cmyLim;
            }
        }

        currentListLength = checkCurrentListLength(arrItemBody.Length + bagRows);

        TAB_W_NEW = 1;
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        try
        {
            int centerX = xScroll + wScroll / 2;
            int itemBoxWidth = 28;
            int leftColumnX = xScroll + 2; // Căn sát lề trái
            int rightColumnX = xScroll + wScroll - itemBoxWidth - 2; // Căn sát lề phải
            int originalX = Char.myCharz().cx;
            int originalY = Char.myCharz().cy;
            int originalXsd = Char.myCharz().xSd;
            int originalYsd = Char.myCharz().ySd;
            int originalStatusMe = Char.myCharz().statusMe;
            int charX = centerX;
            int charY = yScroll + bodyItemsPerColumn * (itemBoxHeight + 2) / 2 + 30;
            int shadowY = charY + 2;

            Char.myCharz().cx = charX;
            Char.myCharz().cy = charY;
            Char.myCharz().xSd = charX;
            Char.myCharz().ySd = shadowY;
            // Đảm bảo statusMe là stand hoặc nothing để aura hiển thị
            if (Char.myCharz().statusMe != 1 && Char.myCharz().statusMe != 6)
            {
                Char.myCharz().statusMe = 1; // Stand
            }
            Char.myCharz().updEffChar();

            if (imgInventoryBackground != null)
            {
                Image bgToDraw = imgInventoryBackground;

                if (isChangeBackground && inventoryBgFrames[0] != null)
                {
                    int frameIndex = GameCanvas.gameTick / 2 % FrameInventoryGif;
                    if (inventoryBgFrames[frameIndex] != null)
                    {
                        bgToDraw = inventoryBgFrames[frameIndex];
                    }
                }
                int bgWidth = mGraphics.getImageWidth(bgToDraw);
                int bgHeight = mGraphics.getImageHeight(bgToDraw);
                int bgX = charX - bgWidth / 2;
                int bgY = charY - Char.myCharz().ch - bgHeight / 2;
                g.drawImage(bgToDraw, bgX, bgY, 0);
            }

            if (Char.myCharz().isShadown && !Char.myCharz().isMabuHold && Char.myCharz().head != 377 && Char.myCharz().leg != 471 && !Char.myCharz().isTeleport && !Char.myCharz().isFlyUp)
            {
                if (TileMap.bong != null)
                {
                    g.drawImage(TileMap.bong, charX, shadowY, 3);
                }
            }
            Char.myCharz().paintEffBehind(g);

            if (Char.myCharz().idEff_Set_Item != -1)
            {
                Char.myCharz().paintEff_Lvup_behind(g);
            }

            Char.myCharz().paintCharBody(g, charX, charY, Char.myCharz().cdir, Char.myCharz().cf, isPaintBag: true);
            Char.myCharz().paintEffFront(g);

            if (Char.myCharz().idEff_Set_Item != -1)
            {
                Char.myCharz().paintEff_Lvup_front(g);
            }

            if (Char.myCharz().danhHieuEff)
            {
                string badgeInfo = (string)GameCanvas.danhHieu.get(Char.myCharz().charID + string.Empty);
                if (badgeInfo != null)
                {
                    try
                    {
                        string[] badgeData = Res.split(badgeInfo.Trim(), ",", 0);
                        if (badgeData.Length >= 2)
                        {
                            short badgeId = short.Parse(badgeData[0]);
                            short badgeSec = short.Parse(badgeData[1]);
                            long currentTime = mSystem.currentTimeMillis();
                            long badgeEndTime = badgeSec * 1000 + currentTime;

                            Effect badgeEffect = new Effect(badgeId, charX, charY - Char.myCharz().ch - 20, 1, -1, -1);
                            badgeEffect.timeExist = badgeEndTime;
                            badgeEffect.x = charX;
                            badgeEffect.y = charY - Char.myCharz().ch - 20;
                            badgeEffect.update();
                            if (badgeEffect.timeExist > currentTime)
                            {
                                badgeEffect.paint(g);
                            }
                        }
                    }
                    catch (Exception)
                    {
                    }
                }
            }

            Char.myCharz().cx = originalX;
            Char.myCharz().cy = originalY;
            Char.myCharz().xSd = originalXsd;
            Char.myCharz().ySd = originalYsd;
            Char.myCharz().statusMe = originalStatusMe;

            for (int j = 0; j < 5; j++)
            {
                int x = leftColumnX;
                int y = yScroll + j * (itemBoxHeight + 2);

                if (y - cmy > yScroll + hScroll || y - cmy < yScroll - itemBoxHeight)
                    continue;

                bool isSelected = (selected == j);

                Item item = (j < arrItemBody.Length) ? arrItemBody[j] : null;
                if (isSelected)
                    g.setColor(16383818);
                else
                    g.setColor(15723751);

                g.fillRect(x, y, itemBoxWidth, itemBoxHeight, 4);

                g.setColor(isSelected ? 9541120 : 11837316);
                g.fillRect(x + 1, y + 1, itemBoxWidth - 2, itemBoxHeight - 2, 3);

                paintEffectItem(g, item, x + 1, y + 1);

                if (item != null && item.isSelect && GameCanvas.panel.type == 12)
                {
                    g.setColor(isSelected ? 7040779 : 6047789);
                    g.fillRect(x + 1, y + 1, itemBoxWidth - 2, itemBoxHeight - 2, 3);
                }

                if (item != null)
                {
                    SmallImage.drawSmallImage(g, item.template.iconID, x + itemBoxWidth / 2, y + itemBoxHeight / 2, 0, 3);

                    if (item.itemOption != null)
                    {
                        for (int n = 0; n < item.itemOption.Length; n++)
                            paintOptItemInventory(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, x, y, itemBoxWidth, itemBoxHeight, item);

                        for (int m = 0; m < item.itemOption.Length; m++)
                            paintOptSlotItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, x, y, itemBoxWidth, itemBoxHeight);
                    }

                    if (item.quantity > 1)
                    {
                        string formattedQuantity = string.Format("{0:N0}", item.quantity).Replace(",", ".");
                        mFont.tahoma_7_yellow.drawString(g, formattedQuantity, x + itemBoxWidth, y + itemBoxHeight - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }

            int bagStartY = yScroll + bodyItemsPerColumn * (itemBoxHeight + 2) + 10;

            for (int j = 5; j < 9; j++)
            {
                int x = rightColumnX;
                int y = yScroll + (j - 5) * (itemBoxHeight + 2);

                if (y - cmy > yScroll + hScroll || y - cmy < yScroll - itemBoxHeight)
                    continue;
                bool isSelected = (selected == j);

                Item item = (j < arrItemBody.Length) ? arrItemBody[j] : null;

                if (isSelected)
                    g.setColor(16383818);
                else
                    g.setColor(15723751);

                g.fillRect(x, y, itemBoxWidth, itemBoxHeight, 4);

                g.setColor(isSelected ? 9541120 : 11837316);
                g.fillRect(x + 1, y + 1, itemBoxWidth - 2, itemBoxHeight - 2, 3);

                paintEffectItem(g, item, x + 1, y + 1);

                if (item != null && item.isSelect && GameCanvas.panel.type == 12)
                {
                    g.setColor(isSelected ? 7040779 : 6047789);
                    g.fillRect(x + 1, y + 1, itemBoxWidth - 2, itemBoxHeight - 2, 3);
                }

                if (item != null)
                {
                    SmallImage.drawSmallImage(g, item.template.iconID, x + itemBoxWidth / 2, y + itemBoxHeight / 2, 0, 3);

                    if (item.itemOption != null)
                    {
                        for (int n = 0; n < item.itemOption.Length; n++)
                            paintOptItemInventory(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, x, y, itemBoxWidth, itemBoxHeight, item);

                        for (int m = 0; m < item.itemOption.Length; m++)
                            paintOptSlotItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, x, y, itemBoxWidth, itemBoxHeight);
                    }

                    if (item.quantity > 1)
                    {
                        string formattedQuantity = string.Format("{0:N0}", item.quantity).Replace(",", ".");
                        mFont.tahoma_7_yellow.drawString(g, formattedQuantity, x + itemBoxWidth, y + itemBoxHeight - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }

            for (int num11 = 0; num11 < arrItemBag.Length; num11++)
            {
                int num12 = 32;
                int x2 = xScroll + num11 % 6 * (num12 + 1);
                int y2 = bagStartY + num11 / 6 * ITEM_HEIGHT;
                int num13 = x2;
                int num14 = y2;
                int num15 = ITEM_HEIGHT - 1;
                if (y2 - cmy > yScroll + hScroll || y2 - cmy < yScroll - ITEM_HEIGHT)
                    continue;
                bool isBagSelected = (selected >= 9 && (selected - 9) == num11);

                Item item2 = arrItemBag[num11];
                g.setColor(11771523);
                g.fillRect(x2, y2, num12, num15, 5);

                if (item2 != null)
                {
                    g.setColor(10587766);
                    g.fillRect(x2 + 2, y2 + 2, num12 - 4, num15 - 4, 4);
                }
                if (isBagSelected)
                {
                    g.setColor(0xCC6600);
                    g.drawRect(x2 - 2, y2 - 2, num12 + 3, num15 + 3);
                    g.setColor(0xFFCC66); 
                    g.drawRect(x2 - 1, y2 - 1, num12 + 1, num15 + 1);
                }

                if (item2 != null)
                {
                    g.setColor(10587766);
                    g.fillRect(x2 + 2, y2 + 2, num12 - 4, num15 - 4, 4);

                    for (int num16 = 0; num16 < item2.itemOption.Length; num16++)
                    {
                        if (item2.itemOption[num16].optionTemplate.id == 72 && item2.itemOption[num16].param > 0)
                        {
                            byte id2 = (byte)GetColor_Item_Upgrade(item2.itemOption[num16].param);
                            if (GetColor_ItemBg(id2) != -1)
                                g.setColor(GetColor_ItemBg(id2));
                        }
                    }

                    if (ModFunc.listFilterItems.Any((ItemAutoFilter filterItem) => filterItem.id == item2.template.id))
                    {
                        g.setColor(16711680);
                        g.fillRect(x2, y2, num12, num15, 5);
                    }
                    if (ModFunc.GI().listItemAuto.Any((ItemAuto itemAuto) => item2.template.id == itemAuto.id && item2.template.iconID == itemAuto.iconID))
                    {
                        g.setColor(65280);
                        g.fillRect(x2, y2, num12, num15, 5);
                    }

                    if (item2.isSelect && GameCanvas.panel.type == 12)
                    {
                        g.setColor(7040779);
                        g.fillRect(x2, y2, num12, num15, 5);
                    }

                    SmallImage.drawSmallImage(g, item2.template.iconID, x2 + num12 / 2, y2 + ITEM_HEIGHT / 2, 0, 3);

                    if (item2.itemOption != null)
                    {
                        for (int num18 = 0; num18 < item2.itemOption.Length; num18++)
                            paintOptItem(g, item2.itemOption[num18].optionTemplate.id, item2.itemOption[num18].param, num13, num14, num12, num15);

                        for (int num19 = 0; num19 < item2.itemOption.Length; num19++)
                            paintOptSlotItem(g, item2.itemOption[num19].optionTemplate.id, item2.itemOption[num19].param, num13, num14, num12, num15);
                    }

                    if (item2.quantity > 1)
                    {
                        string formattedQuantity = string.Format("{0:N0}", item2.quantity).Replace(",", ".");
                        mFont.tahoma_7_yellow.drawString(g, formattedQuantity, x2 + num12, y2 + num15 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }
        }
        catch (Exception)
        {
        }
        paintScrollArrow(g);
    }

    private void paintItemStar(mGraphics g, string opt, int x, int y)
    {
        if (imgStar == null) return;
        g.drawImage(imgStar, x, y + 1);
        mFont.tahoma_7b_yellow.drawString(g, opt, x - imgStar.getWidth() / 2 - mFont.tahoma_7b_yellow.getWidth(opt) / 2, y, 0, mFont.tahoma_7b_green2);
    }

    private void PaintEff(mGraphics gx, Item item, int x, int y)
    {
        try
        {
            if (item?.itemOption == null || !item.isHaveOption(72))
                return;
            var effectMap = new Dictionary<int, (List<Image> bg, List<Image> frame)>
        {
            { 1, (a_bg, a) },
            { 2, (b_bg, b) },
            { 3, (c_bg, c) },
            { 4, (d_bg, d) },
            { 5, (e_bg, e) },
            { 6, (f_bg, f) },
            { 7, (g_bg, g) },
            { 8, (h_bg, h) }
        };
            int param = (int)(item.itemOption.FirstOrDefault(opt => opt != null && opt.optionTemplate.id == 72)?.param ?? -1);
            if (param < 1 || !effectMap.TryGetValue(Math.min(param, 8), out var images))
                return;

            // Tính chỉ số frame một lần
            int frameIndex = GameCanvas.gameTick / 4 % images.bg.Count;
            Image bgImage = images.bg.Count > 0 ? images.bg[frameIndex] : null;

            if (bgImage != null)
            {
                gx.drawImage(bgImage, x - 1, y - 1);

                Image frameImage = images.frame.Count > 0 ? images.frame[frameIndex % images.frame.Count] : null;
                if (frameImage != null)
                {
                    gx.drawImage(frameImage, x + bgImage.getWidth() / 2, y + bgImage.getHeight() / 2 - 1, 3);
                }
            }
        }
        catch (Exception e)
        {
            Debug.LogException(e);
        }
    }
    private static List<Image> a = new List<Image>(), a_bg = new List<Image>();
    private static List<Image> b = new List<Image>(), b_bg = new List<Image>();
    private static List<Image> c = new List<Image>(), c_bg = new List<Image>();
    private static List<Image> d = new List<Image>(), d_bg = new List<Image>();
    private static List<Image> e = new List<Image>(), e_bg = new List<Image>();
    private static List<Image> f = new List<Image>(), f_bg = new List<Image>();
    private static List<Image> g = new List<Image>(), g_bg = new List<Image>();
    private static List<Image> h = new List<Image>(), h_bg = new List<Image>();
    private static int[][] a_id = new int[][]
   {
        new int[]
        {
        3, 14, 25, 35,
        36, 37, 38, 119
        },
        new int[]
        {
        1, 42, 53, 64,
        75, 86, 97, 108
        }
   };
    private static int[][] b_id = new int[][]
    {
        new int[]
        {
        27, 28, 29, 30,
        31, 32, 33, 34
        },
        new int[]
        {
        8, 19, 20, 21,
        22, 23, 24, 26
        }
    };
    private static int[][] c_id = new int[][]
    {
        new int[]
        {
            48, 49, 50, 51,
            52, 54, 55, 56
        },
        new int[]
        {
            39, 40, 41, 43,
            44, 45, 46, 47
        }
    };
    private static int[][] d_id = new int[][]
    {
        new int[]
        {
            66, 67, 68, 69,
            70, 71, 72, 73
        },
        new int[]
        {
            57, 58, 59, 60,
            61, 62, 63, 65
        }
    };
    private static int[][] e_id = new int[][]
    {
        new int[]
        {
            83, 84, 85, 87,
            88, 89, 90, 91
        },
        new int[]
        {
            74, 76, 77, 78,
            79, 80, 81, 82
        }
    };
    private static int[][] f_id = new int[][]
    {
        new int[]
        {
            101, 102, 103, 104,
            105, 106, 107, 109
        },
        new int[]
        {
            92, 93, 94, 95,
            96, 98, 99, 100
        }
    };
    private static int[][] g_id = new int[][]
    {
        new int[]
        {
            118, 120, 121, 122,
            123, 124, 125, 126
        },
        new int[]
        {
            110, 111, 112, 113,
            114, 115, 116, 117
        }
    };
    private static int[][] h_id = new int[][]
    {
        new int[]
        {
            9, 10, 11, 12,
            13, 15, 16, 17
        },
        new int[]
        {
            4, 5, 6, 7,
            8, 127, 128, 129
        }
    };
    static void loadEff()
    {
        var imageCache = new Dictionary<string, Image>();
        var effectData = new[]
        {
        (bgList: a_bg, fgList: a, idArray: a_id),
        (bgList: b_bg, fgList: b, idArray: b_id),
        (bgList: c_bg, fgList: c, idArray: c_id),
        (bgList: d_bg, fgList: d, idArray: d_id),
        (bgList: e_bg, fgList: e, idArray: e_id),
        (bgList: f_bg, fgList: f, idArray: f_id),
        (bgList: g_bg, fgList: g, idArray: g_id),
        (bgList: h_bg, fgList: h, idArray: h_id)
    };
        Image LoadOrGetImage(string path)
        {
            if (!imageCache.TryGetValue(path, out Image img))
            {
                img = GameCanvas.loadImage(path);
                imageCache[path] = img;
            }
            return img;
        }
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                foreach (var data in effectData)
                {
                    string bgPath = $"/u/{data.idArray[0][j]}";
                    string fgPath = $"/u/{data.idArray[1][j]}";

                    Image bgImage = GameCanvas.loadImage(bgPath + ".png");
                    Image fgImage = GameCanvas.loadImage(fgPath + ".png");

                    data.bgList.Add(LoadOrGetImage(bgPath));
                    data.fgList.Add(LoadOrGetImage(fgPath));
                }
            }
        }
    }

    private void paintTab(mGraphics g)
    {
        if (type == 26)
        {
            for (int i = 0; i < boxMod.Length; i++)
            {
                g.setColor((i != currentTabIndex) ? 16773296 : 6805896);
                PopUp.paintPopUp(g, startTabPos + i * TAB_W, 52, TAB_W - 1, 25, (i == currentTabIndex) ? 1 : 0, isButton: true);
                if (i == keyTouchTab)
                {
                    g.drawImage(ItemMap.imageFlare, startTabPos + i * TAB_W + TAB_W / 2, 62, 3);
                }
                mFont mFont2 = ((i != currentTabIndex) ? mFont.tahoma_7_grey : mFont.tahoma_7_green2);
                if (!boxMod[i][1].Equals(string.Empty))
                {
                    mFont2.drawString(g, boxMod[i][0], startTabPos + i * TAB_W + TAB_W / 2, 53, mFont.CENTER);
                    mFont2.drawString(g, boxMod[i][1], startTabPos + i * TAB_W + TAB_W / 2, 64, mFont.CENTER);
                }
                else
                {
                    mFont2.drawString(g, boxMod[i][0], startTabPos + i * TAB_W + TAB_W / 2, 59, mFont.CENTER);
                }
            }
            if (keyTouchTab != -1)
            {
                currentTabIndex = keyTouchTab;
                loadTabModFunc();
                keyTouchTab = -1;
            }
        }
        if (type == 2 && GameCanvas.panel2 != null)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.chest, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 3)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.select_zone, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 4)
        {
            mFont.tahoma_7b_dark.drawString(g, mResources.map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            return;
        }
        if (type == 7)
        {
            mFont.tahoma_7b_dark.drawString(g, mResources.trangbi, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            return;
        }
        if (type == 8)
        {
            mFont.tahoma_7b_dark.drawString(g, mResources.msg + ModFunc.strClickToChat, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            return;
        }
        if (type == 9)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.achievement_mission, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 10)
        {
            mFont.tahoma_7b_dark.drawString(g, mResources.wat_do_u_want, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            return;
        }
        if (type == 11)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.friend, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 12 && GameCanvas.panel2 != null)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 13 && Equals(GameCanvas.panel2))
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.item_receive2, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 14)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.select_map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 15)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, topName, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 16)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.enemy, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 17)
        {
            mFont.tahoma_7b_dark.drawString(g, mResources.kigui, startTabPos + TAB_W / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            return;
        }
        if (type == 18)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.change_flag, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 19)
        {
            string s = ((type == 19) ? mResources.option : ((type == 26) ? ModFunc.strModFunc : ModFunc.strPlayerInfo));
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, s, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 20)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.account, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 22)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.autoFunction, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 29)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, ModFunc.strHotkeySettings, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 23 || type == 24)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, mResources.gameInfo, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (type == 27)
        {
            g.setColor(13524492);
            g.fillRect(X + 1, 78, W - 2, 1);
            mFont.tahoma_7b_dark.drawString(g, ModFunc.strPlayerInfo, xScroll + wScroll / 2, 59, mFont.CENTER);
            return;
        }
        if (this.type == 32)
        {
            mFont.tahoma_7b_dark.drawString(g, "Thông báo BOSS", xScroll + wScroll / 2, 59, mFont.CENTER);
            g.setColor(13524492);
            g.fillRect(this.X + 1, 78, this.W - 2, 1);
            return;
        }
        if (currentTabIndex == 3 && mainTabName.Length != 4)
        {
            g.translate(-cmx, 0);
        }
        for (int j = 0; j < currentTabName.Length; j++)
        {
            g.setColor((j != currentTabIndex) ? 16773296 : 6805896);
            PopUp.paintPopUp(g, startTabPos + j * TAB_W, 52, TAB_W - 1, 25, (j == currentTabIndex) ? 1 : 0, isButton: true);
            if (j == keyTouchTab)
            {
                g.drawImage(ItemMap.imageFlare, startTabPos + j * TAB_W + TAB_W / 2, 62, 3);
            }
            mFont mFont3 = ((j != currentTabIndex) ? mFont.tahoma_7_grey : mFont.tahoma_7_green2);
            if (!currentTabName[j][1].Equals(string.Empty))
            {
                mFont3.drawString(g, currentTabName[j][0], startTabPos + j * TAB_W + TAB_W / 2, 53, mFont.CENTER);
                mFont3.drawString(g, currentTabName[j][1], startTabPos + j * TAB_W + TAB_W / 2, 64, mFont.CENTER);
            }
            else
            {
                mFont3.drawString(g, currentTabName[j][0], startTabPos + j * TAB_W + TAB_W / 2, 59, mFont.CENTER);
            }
            if (type == 0 && currentTabName.Length == 5 && GameScr.isNewClanMessage && GameCanvas.gameTick % 4 == 0)
            {
                g.drawImage(ItemMap.imageFlare, startTabPos + 3 * TAB_W + TAB_W / 2, 77, mGraphics.BOTTOM | mGraphics.HCENTER);
            }
        }
        g.setColor(13524492);
        g.fillRect(1, 78, W - 2, 1);
    }

    private void paintBottomMoneyInfo(mGraphics g)
    {
        if (type != 13 || (currentTabIndex != 2 && !Equals(GameCanvas.panel2)))
        {
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            g.setColor(11837316);
            g.fillRect(X + 1, H - 15, W - 2, 14);
            g.setColor(13524492);
            g.fillRect(X + 1, H - 15, W - 2, 1);

            string strXu = Char.myCharz().xuStr + "";
            string strLuong = Char.myCharz().luongStr + "";
            // string strLuongKhoa = Char.myCharz().luongKhoaStr + ""; // commented per request, only three currencies

            Item itemThoiVang = FindItemID(457);
            string strThoiVang = "";
            if (itemThoiVang != null && itemThoiVang.quantity > 0)
            {
                strThoiVang = NinjaUtil.getMoneys(itemThoiVang.quantity);
            }

            int iconW = 10;
            int wXu = iconW + mFont.tahoma_7_yellow.getWidth(strXu);
            int wLuong = iconW + mFont.tahoma_7_yellow.getWidth(strLuong);
            // int wLuongKhoa = iconW + mFont.tahoma_7_yellow.getWidth(strLuongKhoa);
            int wLuongKhoa = 0; // keep variable in case other logic references it
            int wThoiVang = 0;
            if (strThoiVang != "")
            {
                wThoiVang = iconW + mFont.tahoma_7_yellow.getWidth(strThoiVang);
            }

            int marginLR = 15; 
            // calculate width without luongKhoa
            int totalContentWidth = wXu + wLuong /*+ wLuongKhoa*/ + wThoiVang;
            int countItem = (strThoiVang != "") ? 3 : 2; // one less currency now
            int availableSpace = (W - (marginLR * 2)) - totalContentWidth;
            int gap = (countItem > 1) ? (availableSpace / (countItem - 1)) : 0;

            int currentX = X + marginLR; 
            int yIcon = H - 8; // Tọa độ Y của Icon
            int yText = H - 13; // Tọa độ Y của Chữ

            g.drawImage(imgXu, currentX, H - 7, 3);
            mFont.tahoma_7_yellow.drawString(g, strXu, currentX + iconW, yText, mFont.LEFT, mFont.tahoma_7_grey);
            currentX += wXu + gap;

            g.drawImage(imgLuong, currentX, yIcon, 3);
            mFont.tahoma_7_yellow.drawString(g, strLuong, currentX + iconW, yText, mFont.LEFT, mFont.tahoma_7_grey);
            currentX += wLuong + gap;

            // g.drawImage(imgLuongKhoa, currentX, yIcon, 3); // luongKhoa removed
            // mFont.tahoma_7_yellow.drawString(g, strLuongKhoa, currentX + iconW, yText, mFont.LEFT, mFont.tahoma_7_grey);
            // currentX += wLuongKhoa + gap;

            if (strThoiVang != "")
            {
                if (imgThoivang != null)
                    g.drawImage(imgThoivang, currentX, yIcon, 3);

                mFont.tahoma_7_yellow.drawString(g, strThoiVang, currentX + iconW, yText, mFont.LEFT, mFont.tahoma_7_grey);
            }
        }
    }

    private Item FindItemID(int id)
    {
        foreach (var item in Char.myCharz().arrItemBag)
        {
            if (item != null && item.template.id == id)
            {
                return item;
            }
        }
        return null;
    }

    private void paintClanInfo(mGraphics g)
    {
        if (Char.myCharz().clan == null)
        {
            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
            mFont.tahoma_7b_white.drawString(g, mResources.not_join_clan, (wScroll - 50) / 2 + 50, 20, mFont.CENTER);
        }
        else if (!isViewMember)
        {
            Clan clan = Char.myCharz().clan;
            if (clan != null)
            {
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                mFont.tahoma_7b_white.drawString(g, clan.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
                mFont.tahoma_7_yellow.drawString(g, mResources.achievement_point + ": " + clan.powerPoint, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, mResources.clan_point + ": " + clan.clanPoint, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, mResources.level + ": " + clan.level, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
                TextInfo.paint(g, clan.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
            }
        }
        else
        {
            Clan clan2 = ((currClan == null) ? Char.myCharz().clan : currClan);
            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
            mFont.tahoma_7b_white.drawString(g, clan2.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, mResources.member + ": " + clan2.currMember + "/" + clan2.maxMember, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.clan_leader + ": " + clan2.leaderName, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            TextInfo.paint(g, clan2.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
        }
    }

    private void paintToolInfo(mGraphics g)
    {
        mFont.tahoma_7b_white.drawString(g, mResources.dragon_ball + " " + GameMidlet.VERSION, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7_yellow.drawString(g, (Char.myCharz().isTichXanh ? "     " : string.Empty) + Char.myCharz().cName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
        if (Char.myCharz().isTichXanh)
        {
            ModFunc.PaintTicks(g, 58, 17);
        }
        string text = ((!GameCanvas.loginScr.tfUser.getText().Equals(string.Empty)) ? GameCanvas.loginScr.tfUser.getText() : mResources.not_register_yet);
        mFont.tahoma_7_yellow.drawString(g, mResources.account_server + " " + ServerListScreen.nameServer[ServerListScreen.ipSelect] + ": " + text, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintGiaoDichInfo(mGraphics g)
    {
        mFont.tahoma_7_yellow.drawString(g, mResources.select_item, 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.lock_trade, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.wait_opp_lock_trade, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.press_done, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintMyInfo(mGraphics g)
    {
        paintCharInfo(g, Char.myCharz());
    }

    private void paintPetInfo(mGraphics g, bool isPet2)
    {
        Char pet = (isPet2 ? Char.MyPet2z() : Char.myPetz());
        mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneysPower(pet.cPower), X + 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
        if (pet.cPower > 0.0)
        {
            mFont.tahoma_7_yellow.drawString(g, (!pet.me) ? pet.currStrLevel : pet.getStrLevel(), X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
        }
        if (pet.cDamFull > 0.0)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + NinjaUtil.getMoneys((long)pet.cDamFull), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        }
        if (pet.cMaxStamina > 0)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
            g.drawImage(GameScr.imgMPLost, X + 100, 41, 0);
            int num = pet.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / pet.cMaxStamina;
            g.setClip(100, X + 41, num, 20);
            g.drawImage(GameScr.imgMP, X + 100, 41, 0);
        }
        g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    }

    private void paintPetSkillInfo(mGraphics g, bool isPet2)
    {
        Char pet = (isPet2 ? Char.MyPet2z() : Char.myPetz());
        mFont.tahoma_7b_white.drawString(g, "HP: " + NinjaUtil.getMoneys((long)pet.cHP) + "/" + NinjaUtil.getMoneys((long)pet.cHPFull), X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7b_white.drawString(g, "MP: " + NinjaUtil.getMoneys((long)pet.cMP) + "/" + NinjaUtil.getMoneys((long)pet.cMPFull), X + 60, 16, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7_yellow.drawString(g, mResources.critical + ": " + pet.cCriticalFull + "   " + mResources.armor + ": " + NinjaUtil.getMoneys((long)pet.cDefull), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.potential2 + ": " + NinjaUtil.getMoneys(pet.cTiemNang), X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintCharInfo(mGraphics g, Char c)
    {
        mFont.tahoma_7b_white.drawString(g, c.cName, X + 60,
            4, mFont.LEFT, mFont.tahoma_7b_dark);


        if (c.cMaxStamina > 0)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            g.drawImage(GameScr.imgMPLost, X + 95, 19, 0);
            int num = c.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / c.cMaxStamina;
            g.setClip(95, X + 19, num, 20);
            g.drawImage(GameScr.imgMP, X + 95, 19, 0);
        }

        g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        if (c.cPower > 0)
        {
            mFont.tahoma_7_yellow.drawString(g, (!c.me) ? c.currStrLevel : c.getStrLevel(), X + 60, 27, mFont.LEFT,
                mFont.tahoma_7_grey);
        }

        mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(c.cPower), X + 60, 38,
            mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintZoneInfo(mGraphics g)
    {
        mFont.tahoma_7b_white.drawString(g, mResources.zone + " " + TileMap.zoneID, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7_yellow.drawString(g, TileMap.mapName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7b_white.drawString(g, TileMap.zoneID + string.Empty, 25, 27, mFont.CENTER);
    }

    public int getCompare(Item item)
    {
        if (item == null)
        {
            return -1;
        }
        if (item.isTypeBody())
        {
            if (item.itemOption == null)
            {
                return -1;
            }
            ItemOption itemOption = item.itemOption[0];
            if (itemOption.optionTemplate.id == 22)
            {
                itemOption.optionTemplate = GameScr.gI().iOptionTemplates[6];
                itemOption.param *= 1000;
            }
            if (itemOption.optionTemplate.id == 23)
            {
                itemOption.optionTemplate = GameScr.gI().iOptionTemplates[7];
                itemOption.param *= 1000;
            }
            Item item2 = null;
            for (int i = 0; i < Char.myCharz().arrItemBody.Length; i++)
            {
                Item item3 = Char.myCharz().arrItemBody[i];
                if (itemOption.optionTemplate.id == 22)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[6];
                    itemOption.param *= 1000;
                }
                if (itemOption.optionTemplate.id == 23)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[7];
                    itemOption.param *= 1000;
                }
                if (item3 != null && item3.itemOption != null && item3.template.type == item.template.type)
                {
                    item2 = item3;
                    break;
                }
            }
            if (item2 == null)
            {
                isUp = true;
                return itemOption.param;
            }
            int num = ((item2 == null || item2.itemOption == null) ? itemOption.param : (itemOption.param - item2.itemOption[0].param));
            if (num < 0)
            {
                isUp = false;
                return num;
            }
            isUp = true;
            return num;
        }
        return 0;
    }

    private void paintMapInfo(mGraphics g)
    {
        mFont.tahoma_7b_white.drawString(g, mResources.MENUGENDER[TileMap.planetID], 60, 4, mFont.LEFT);
        string text = string.Empty;
        if (TileMap.mapID >= 135 && TileMap.mapID <= 138)
        {
            text = " " + mResources.tang + TileMap.zoneID;
        }
        mFont.tahoma_7_yellow.drawString(g, TileMap.mapName + text, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7b_white.drawString(g, mResources.quest_place + ": ", 60, 27, mFont.LEFT);
        if (GameScr.getTaskMapId() >= 0 && GameScr.getTaskMapId() <= TileMap.mapNames.Length - 1)
        {
            mFont.tahoma_7_yellow.drawString(g, TileMap.mapNames[GameScr.getTaskMapId()], 60, 38, mFont.LEFT);
        }
        else
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.random, 60, 38, mFont.LEFT);
        }
    }

    private void paintShopInfo(mGraphics g)
    {
        if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
        {
            paintMyInfo(g);
        }
        else if (selected < 0)
        {
            if (typeShop != 2)
            {
                mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 14, 0);
                mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 26, 0);
                return;
            }
            mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 5, 0);
            mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 17, 0);
            mFont.tahoma_7_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 60, 29, 0);
        }
        else
        {
            if (currentTabIndex < 0 || currentTabIndex > Char.myCharz().arrItemShop.Length - 1 || selected < 0 || selected > Char.myCharz().arrItemShop[currentTabIndex].Length - 1)
            {
                return;
            }
            Item item = Char.myCharz().arrItemShop[currentTabIndex][selected];
            if (item != null)
            {
                if (Equals(GameCanvas.panel) && currentTabIndex <= 3 && typeShop == 2)
                {
                    mFont.tahoma_7b_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 55, 4, 0);
                }
                mFont.tahoma_7b_white.drawString(g, item.template.name, X + 55, 24, 0);
                string st = mResources.pow_request + " " + Res.formatNumber(item.template.strRequire);
                if (item.template.strRequire > Char.myCharz().cPower)
                {
                    mFont.tahoma_7_yellow.drawString(g, st, X + 55, 35, 0);
                }
                else
                {
                    mFont.tahoma_7_green.drawString(g, st, X + 55, 35, 0);
                }
            }
        }
    }

    private void paintItemBoxInfo(mGraphics g)
    {
        string st = mResources.used + ": " + hasUse + "/" + Char.myCharz().arrItemBox.Length + " " + mResources.place;
        mFont.tahoma_7b_white.drawString(g, mResources.chest, 60, 4, 0);
        mFont.tahoma_7_yellow.drawString(g, st, 60, 16, 0);
    }

    private void paintSkillInfo(mGraphics g)
    {
        mFont.tahoma_7_white.drawString(g, "Top " + Char.myCharz().rank, X + 45 + (W - 50) / 2, 2, mFont.CENTER);
        mFont.tahoma_7_yellow.drawString(g, mResources.potential_point, X + 45 + (W - 50) / 2, 14, mFont.CENTER);
        mFont.tahoma_7_white.drawString(g, string.Empty + NinjaUtil.getMoneys(Char.myCharz().cTiemNang), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 26, mFont.CENTER);
        mFont.tahoma_7_yellow.drawString(g, mResources.active_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cNangdong), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 38, mFont.CENTER);
    }

    private void paintItemBodyBagInfo(mGraphics g)
    {
        mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + Char.myCharz().cHP + " / " + Char.myCharz().cHPFull, X + 60, 2, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + Char.myCharz().cMP + " / " + Char.myCharz().cMPFull, X + 60, 14, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + Char.myCharz().cDamFull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", X + 60, 26, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.giamsatthuong + ": " + Char.myCharz().cGiamST + "%, " + mResources.critdame + ": " + Char.myCharz().cCritDameFull + "%", X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintItemBodyBagInfo(mGraphics g, int x, int y)
    {
        mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + Char.myCharz().cHP + " / " + Char.myCharz().cHPFull, x, y + 2, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + Char.myCharz().cMP + " / " + Char.myCharz().cMPFull, x, y + 14, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + Char.myCharz().cDamFull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", x, y + 26, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.giamsatthuong + ": " + Char.myCharz().cGiamST + "%, " + mResources.critdame + ": " + Char.myCharz().cCritDameFull + "%", x, y + 38, mFont.LEFT, mFont.tahoma_7_grey);
    }


    private void paintTopInfo(mGraphics g)
    {
        g.setClip(X + 1, Y, W - 2, yScroll - 2);
        g.setColor(9993045);

        g.fillRect(X, Y, W - 2, 50);
        switch (type)
        {
            case 13:
                if (currentTabIndex == 0 || currentTabIndex == 1)
                {
                    if (Equals(GameCanvas.panel))
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintGiaoDichInfo(g);
                    }
                    if (Equals(GameCanvas.panel2) && charMenu != null)
                    {
                        SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                        paintCharInfo(g, charMenu);
                    }
                }
                if (currentTabIndex == 2 && charMenu != null)
                {
                    SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                    paintCharInfo(g, charMenu);
                }
                break;
            case 12:
                if (currentTabIndex == 0)
                {
                    int id = 1410;
                    for (int i = 0; i < GameScr.vNpc.size(); i++)
                    {
                        Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                        if (npc.template.npcTemplateId == idNPC)
                        {
                            id = npc.avatar;
                        }
                    }
                    SmallImage.drawSmallImage(g, id, X + 25, 50, 0, 33);
                    paintCombineInfo(g);
                }
                if (currentTabIndex == 1)
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                }
                break;
            case 11:
            case 16:
            case 32:
            case 33:
            case 24:
            case 23:
            case 27:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 15:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 9:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 21:
            case 28:
                {
                    Char pet = ((type == 28) ? Char.MyPet2z() : Char.myPetz());
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, pet.avatarz(), X + 25, 50, 0, 33);
                        paintPetInfo(g, type == 28);
                    }
                    else if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, pet.avatarz(), X + 25, 50, 0, 33);
                        paintPetStatusInfo(g, type == 28);
                    }
                    else if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintItemBodyBagInfo(g);
                    }
                    break;
                }
            case 0:
                try
                {
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintMyInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        if (isnewInventory)
                        {
                            paintCharInfo(g, Char.myCharz());
                        }
                        else
                        {
                            paintItemBodyBagInfo(g);
                        }
                    }
                    if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintSkillInfo(g);
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length == 5)
                        {
                            paintClanInfo(g);
                        }
                        else
                        {
                            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                            paintToolInfo(g);
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintToolInfo(g);
                    }
                    break;
                }
                catch (Exception exception)
                {
                    Debug.LogException(exception);
                    break;
                }
            case 25:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 2:
                if (currentTabIndex == 0)
                {
                    SmallImage.drawSmallImage(g, 526, X + 25, 50, 0, 33);
                    paintItemBoxInfo(g);
                }
                if (currentTabIndex == 1)
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintItemBodyBagInfo(g);
                }
                break;
            case 3:
                SmallImage.drawSmallImage(g, 561, X + 25, 50, 0, 33);
                paintZoneInfo(g);
                break;
            case 1:
                if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                }
                else
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().npcFocus.avatar, X + 25, 50, 0, 33);
                }
                paintShopInfo(g);
                break;
            case 4:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMapInfo(g);
                break;
            case 7:
            case 17:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 8:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 10:
                if (charMenu != null)
                {
                    SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                    paintCharInfo(g, charMenu);
                }
                break;
            case 14:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMapInfo(g);
                break;
            case 18:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintMyInfo(g);
                break;
            case 19:
            case 26:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintToolInfo(g);
                break;
            case 20:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintToolInfo(g);
                break;
            case 22:
            case 29:
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                paintToolInfo(g);
                break;
            case 5:
            case 6:
                break;
        }
    }

    private void paintPetStatusInfo(mGraphics g, bool isPet2)
    {
        Char pet = (isPet2 ? Char.MyPet2z() : Char.myPetz());
        mFont.tahoma_7b_white.drawString(g, "HP: " + NinjaUtil.getMoneys((long)pet.cHP) + "/" + NinjaUtil.getMoneys((long)pet.cHPFull), X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7b_white.drawString(g, "MP: " + NinjaUtil.getMoneys((long)pet.cMP) + "/" + NinjaUtil.getMoneys((long)pet.cMPFull), X + 60, 16, mFont.LEFT, mFont.tahoma_7b_dark);
        mFont.tahoma_7_yellow.drawString(g, mResources.critical + ": " + NinjaUtil.getMoneys(pet.cCriticalFull) + "   " + mResources.armor + ": " + NinjaUtil.getMoneys((long)pet.cDefull), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_yellow.drawString(g, mResources.status + ": " + strStatus[pet.petStatus], X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
    }

    private void paintCombineInfo(mGraphics g)
    {
        if (combineTopInfo != null)
        {
            for (int i = 0; i < combineTopInfo.Length; i++)
            {
                mFont.tahoma_7_white.drawString(g, combineTopInfo[i], X + 45 + (W - 50) / 2, 5 + i * 14, mFont.CENTER);
            }
        }
    }

    public void paintMap(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(-cmxMap, -cmyMap);
        g.drawImage(imgMap, xScroll, yScroll, 0);
        int head = Char.myCharz().head;
        Part part = GameScr.parts[head];
        SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, xMap, yMap + 5, 0, 3);
        int align = mFont.CENTER;
        if (xMap <= 40)
        {
            align = mFont.LEFT;
        }
        if (xMap >= 220)
        {
            align = mFont.RIGHT;
        }
        mFont.tahoma_7b_yellow.drawString(g, TileMap.mapName, xMap, yMap - 12, align, mFont.tahoma_7_grey);
        int num = -1;
        if (GameScr.getTaskMapId() != -1)
        {
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (mapId[TileMap.planetID][i] == GameScr.getTaskMapId())
                {
                    num = i;
                    break;
                }
                num = 4;
            }
            if (GameCanvas.gameTick % 4 > 0)
            {
                g.drawImage(ItemMap.imageFlare, xScroll + mapX[TileMap.planetID][num], yScroll + mapY[TileMap.planetID][num], 3);
            }
        }
        if (!GameCanvas.isTouch)
        {
            g.drawImage(imgBantay, xMove, yMove, StaticObj.TOP_RIGHT);
            for (int j = 0; j < mapX[TileMap.planetID].Length; j++)
            {
                int num2 = mapX[TileMap.planetID][j] + xScroll;
                int num3 = mapY[TileMap.planetID][j] + yScroll;
                if (Res.inRect(num2 - 15, num3 - 15, 30, 30, xMove, yMove))
                {
                    align = mFont.CENTER;
                    if (num2 <= 20)
                    {
                        align = mFont.LEFT;
                    }
                    if (num2 >= 220)
                    {
                        align = mFont.RIGHT;
                    }
                    mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][j]], num2, num3 - 12, align, mFont.tahoma_7_grey);
                    break;
                }
            }
        }
        else if (!trans)
        {
            for (int k = 0; k < mapX[TileMap.planetID].Length; k++)
            {
                int num4 = mapX[TileMap.planetID][k] + xScroll;
                int num5 = mapY[TileMap.planetID][k] + yScroll;
                if (Res.inRect(num4 - 15, num5 - 15, 30, 30, pX, pY))
                {
                    align = mFont.CENTER;
                    if (num4 <= 30)
                    {
                        align = mFont.LEFT;
                    }
                    if (num4 >= 220)
                    {
                        align = mFont.RIGHT;
                    }
                    g.drawImage(imgBantay, num4, num5, StaticObj.TOP_RIGHT);
                    mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][k]], num4, num5 - 12, align, mFont.tahoma_7_grey);
                    break;
                }
            }
        }
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        if (num != -1)
        {
            if (mapX[TileMap.planetID][num] + xScroll < cmxMap)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 5, xScroll + 5, yScroll + hScroll / 2 - 4, 0);
            }
            if (cmxMap + wScroll < mapX[TileMap.planetID][num] + xScroll)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 6, xScroll + wScroll - 5, yScroll + hScroll / 2 - 4, StaticObj.TOP_RIGHT);
            }
            if (mapY[TileMap.planetID][num] < cmyMap)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll / 2, yScroll + 5, StaticObj.TOP_CENTER);
            }
            if (mapY[TileMap.planetID][num] > cmyMap + hScroll)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll / 2, yScroll + hScroll - 5, StaticObj.BOTTOM_HCENTER);
            }
        }
    }

    public void paintTask(mGraphics g)
    {
        int num = ((GameCanvas.h <= 300) ? 15 : 20);
        if (isPaintMap && !GameScr.gI().isMapDocNhan() && !GameScr.gI().isMapFize())
        {
            g.drawImage((keyTouchMapButton != 1) ? GameScr.imgLbtn : GameScr.imgLbtnFocus, xScroll + wScroll / 2, yScroll + hScroll - num, 3);
            mFont.tahoma_7b_dark.drawString(g, mResources.map, xScroll + wScroll / 2, yScroll + hScroll - (num + 5), mFont.CENTER);
        }
        xstart = xScroll + 5;
        ystart = yScroll + 14;
        yPaint = ystart;
        g.setClip(xScroll, yScroll, wScroll, hScroll - 35);
        if (scroll != null)
        {
            if (scroll.cmy > 0)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
            }
            if (scroll.cmy < scroll.cmyLim)
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 45, 0);
            }
            g.translate(0, -scroll.cmy);
        }
        indexRowMax = 0;
        if (indexMenu == 0)
        {
            bool flag = false;
            if (Char.myCharz().taskMaint != null)
            {
                for (int i = 0; i < Char.myCharz().taskMaint.names.Length; i++)
                {
                    mFont.tahoma_7_grey.drawString(g, Char.myCharz().taskMaint.names[i], xScroll + wScroll / 2, yPaint - 5 + i * 12, mFont.CENTER);
                    indexRowMax++;
                }
                yPaint += (Char.myCharz().taskMaint.names.Length - 1) * 12;
                int num2 = 0;
                string empty = string.Empty;
                for (int j = 0; j < Char.myCharz().taskMaint.subNames.Length; j++)
                {
                    if (Char.myCharz().taskMaint.subNames[j] != null)
                    {
                        num2 = j;
                        empty = "- " + Char.myCharz().taskMaint.subNames[j];
                        if (Char.myCharz().taskMaint.counts[j] != -1)
                        {
                            if (Char.myCharz().taskMaint.index == j)
                            {
                                if (Char.myCharz().taskMaint.count != Char.myCharz().taskMaint.counts[j])
                                {
                                    string text = empty;
                                    empty = text + " (" + Char.myCharz().taskMaint.count + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                }
                                if (Char.myCharz().taskMaint.count == Char.myCharz().taskMaint.counts[j])
                                {
                                    mFont.tahoma_7.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                                }
                                else
                                {
                                    mFont tahoma_7_grey = mFont.tahoma_7_grey;
                                    if (!flag)
                                    {
                                        flag = true;
                                        tahoma_7_grey = mFont.tahoma_7_blue;
                                        tahoma_7_grey.drawString(g, empty, xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                    else
                                    {
                                        tahoma_7_grey.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                }
                            }
                            else if (Char.myCharz().taskMaint.index > j)
                            {
                                if (Char.myCharz().taskMaint.counts[j] != 1)
                                {
                                    string text2 = empty;
                                    empty = text2 + " (" + Char.myCharz().taskMaint.counts[j] + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                }
                                mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                            }
                            else
                            {
                                if (Char.myCharz().taskMaint.counts[j] != 1)
                                {
                                    empty = empty + " 0/" + Char.myCharz().taskMaint.counts[j];
                                }
                                mFont tahoma_7_grey2 = mFont.tahoma_7_grey;
                                if (!flag)
                                {
                                    flag = true;
                                    tahoma_7_grey2 = mFont.tahoma_7_blue;
                                    tahoma_7_grey2.drawString(g, empty, xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                                else
                                {
                                    tahoma_7_grey2.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                            }
                        }
                        else if (Char.myCharz().taskMaint.index > j)
                        {
                            mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                        }
                        else
                        {
                            mFont tahoma_7_grey3 = mFont.tahoma_7_grey;
                            if (!flag)
                            {
                                flag = true;
                                tahoma_7_grey3 = mFont.tahoma_7_blue;
                                tahoma_7_grey3.drawString(g, empty, xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                            }
                            else
                            {
                                tahoma_7_grey3.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                            }
                        }
                        indexRowMax++;
                    }
                    else if (Char.myCharz().taskMaint.index <= j)
                    {
                        empty = "- " + Char.myCharz().taskMaint.subNames[num2];
                        mFont mFont2 = mFont.tahoma_7_grey;
                        if (!flag)
                        {
                            flag = true;
                            mFont2 = mFont.tahoma_7_blue;
                        }
                        mFont2.drawString(g, empty, xstart + 5 + ((mFont2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                    }
                }
                yPaint += 5;
                for (int k = 0; k < Char.myCharz().taskMaint.details.Length; k++)
                {
                    mFont.tahoma_7_green2.drawString(g, Char.myCharz().taskMaint.details[k], xstart + 5, yPaint += 12, 0);
                    indexRowMax++;
                }
            }
            else
            {
                int taskMapId = GameScr.getTaskMapId();
                sbyte taskNpcId = GameScr.getTaskNpcId();
                string empty2 = string.Empty;
                if (taskMapId == -3 || taskNpcId == -3)
                {
                    empty2 = mResources.DES_TASK[3];
                }
                else if (Char.myCharz().taskMaint == null && Char.myCharz().ctaskId == 9 && Char.myCharz().nClass.classId == 0)
                {
                    empty2 = mResources.TASK_INPUT_CLASS;
                }
                else
                {
                    if (taskNpcId < 0 || taskMapId < 0)
                    {
                        return;
                    }
                    empty2 = mResources.DES_TASK[0] + Npc.arrNpcTemplate[taskNpcId].name + mResources.DES_TASK[1] + TileMap.mapNames[taskMapId] + mResources.DES_TASK[2];
                }
                string[] array = mFont.tahoma_7_white.splitFontArray(empty2, 150);
                for (int l = 0; l < array.Length; l++)
                {
                    if (l == 0)
                    {
                        mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint = ystart, 0);
                    }
                    else
                    {
                        mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint += 12, 0);
                    }
                }
            }
        }
        else if (indexMenu == 1)
        {
            yPaint = ystart - 12;
            for (int m = 0; m < Char.myCharz().taskOrders.size(); m++)
            {
                TaskOrder taskOrder = (TaskOrder)Char.myCharz().taskOrders.elementAt(m);
                mFont.tahoma_7_white.drawString(g, taskOrder.name, xstart + 5, yPaint += 12, 0);
                if (taskOrder.count == taskOrder.maxCount)
                {
                    mFont.tahoma_7_white.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                }
                else
                {
                    mFont.tahoma_7_blue.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                }
                indexRowMax += 3;
                inforW = popupW - 25;
                paintMultiLine(g, mFont.tahoma_7_grey, taskOrder.description, xstart + 5, yPaint += 12, 0);
                yPaint += 12;
            }
        }
        if (scroll == null)
        {
            scroll = new Scroll();
            scroll.setStyle(indexRowMax, 12, xScroll, yScroll, wScroll, hScroll - num - 40, styleUPDOWN: true, 1);
        }
    }

    public void paintMultiLine(mGraphics g, mFont f, string str, int x, int y, int align)
    {
        int num = ((!GameCanvas.isTouch || GameCanvas.w < 320) ? 10 : 20);
        string[] array = f.splitFontArray(str, inforW - num);
        for (int i = 0; i < array.Length; i++)
        {
            if (i == 0)
            {
                f.drawString(g, array[i], x, y, align);
                continue;
            }
            if (i < indexRow + 15 && i > indexRow - 15)
            {
                f.drawString(g, array[i], x, y += 12, align);
            }
            else
            {
                y += 12;
            }
            yPaint += 12;
            indexRowMax++;
        }
    }

    public void cleanCombine()
    {
        for (int i = 0; i < vItemCombine.size(); i++)
        {
            ((Item)vItemCombine.elementAt(i)).isSelect = false;
        }
        vItemCombine.removeAllElements();
    }

    public void hideNow()
    {
        if (timeShow > 0)
        {
            isClose = false;
            return;
        }
        if (isTypeShop())
        {
            Char.myCharz().resetPartTemp();
        }
        if (chatTField != null && type == 13 && chatTField.isShow)
        {
            chatTField = null;
        }
        if (type == 13 && !isAccept)
        {
            Service.gI().giaodich(3, -1, -1, -1);
        }
        SoundMn.gI().buttonClose();
        GameScr.isPaint = true;
        TileMap.lastPlanetId = -1;
        imgMap = null;
        mSystem.gcc();
        isClanOption = false;
        isClose = true;
        cleanCombine();
        Hint.clickNpc();
        GameCanvas.panel2 = null;
        GameCanvas.clearAllPointerEvent();
        GameCanvas.clearKeyPressed();
        pointerDownTime = (pointerDownFirstX = 0);
        pointerIsDowning = false;
        isShow = false;
        if ((Char.myCharz().cHP <= 0.0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
        {
            Command center = new Command(mResources.DIES[0], 11038, GameScr.gI());
            GameScr.gI().center = center;
            Char.myCharz().cHP = 0.0;
        }
    }

    public void hide()
    {
        if (timeShow > 0)
        {
            isClose = false;
            return;
        }
        if (isTypeShop())
        {
            Char.myCharz().resetPartTemp();
        }
        if (chatTField != null && type == 13 && chatTField.isShow)
        {
            chatTField = null;
        }
        if (type == 13 && !isAccept)
        {
            Service.gI().giaodich(3, -1, -1, -1);
        }
        if (type == 15)
        {
            Service.gI().sendThachDau(-1);
        }
        SoundMn.gI().buttonClose();
        GameScr.isPaint = true;
        TileMap.lastPlanetId = -1;
        if (imgMap != null)
        {
            imgMap.texture = null;
            imgMap = null;
        }
        mSystem.gcc();
        isClanOption = false;
        if (type != 4)
        {
            if (type == 24)
            {
                setTypeGameInfo();
            }
            else if (type == 23)
            {
                setTypeMain();
            }
            if (type == 33)
            {
                setTypeInfoBoss();
            }
            else if (type == 3 || type == 14)
            {
                if (isChangeZone || type == 14 || type == 31 || type == 32)
                {
                    isClose = true;
                }
                else
                {
                    setTypeMain();
                    cmx = (cmtoX = 0);
                }
            }
            else if (type == 18 || type == 19 || type == 20 || type == 21 || type == 26 || type == 27 || type == 28 || type == 29)
            {
                setTypeMain();
                cmx = (cmtoX = 0);
            }
            else if (type == 8 || type == 11 || type == 16)
            {
                setTypeAccount();
                cmx = (cmtoX = 0);
            }
            else
            {
                isClose = true;
            }
        }
        else
        {
            setTypeMain();
            cmx = (cmtoX = 0);
        }
        Hint.clickNpc();
        GameCanvas.panel2 = null;
        GameCanvas.clearAllPointerEvent();
        GameCanvas.clearKeyPressed();
        GameCanvas.isFocusPanel2 = false;
        pointerDownTime = (pointerDownFirstX = 0);
        pointerIsDowning = false;
        if ((Char.myCharz().cHP <= 0.0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
        {
            Command center = new Command(mResources.DIES[0], 11038, GameScr.gI());
            GameScr.gI().center = center;
            Char.myCharz().cHP = 0.0;
        }
    }

    private void DoFirePetSkill()
    {
        if (selected < 0)
        {
            return;
        }
        if (selected == 0 || selected == 1 || selected == 2 || selected == 3 || selected == 4)
        {
            long cTiemNang = Char.myPetz().cTiemNang;
            double cHPGoc = (int)Char.myPetz().cHPGoc;
            double cMPGoc = (int)Char.myPetz().cMPGoc;
            double cDamGoc = (int)Char.myPetz().cDamGoc;
            double cDefGoc = (int)Char.myPetz().cDefGoc;
            int cCriticalGoc = Char.myPetz().cCriticalGoc;

            int num2 = 1000;
            if (selected == 0)
            {
                if (cTiemNang < cHPGoc + num2)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + cTiemNang + mResources.not_enough_potential_point2 + (cHPGoc + num2), isError: false);
                    return;
                }
                if (cTiemNang > cHPGoc && cTiemNang < 10 * (2 * (cHPGoc + num2) + 180) / 2)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cHPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().hpFrom1000TiemNang + mResources.for_HP, new Command(mResources.increase_upper, this, 9000, true), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector = new(string.Empty);
                if (cTiemNang >= 10 * (2 * (cHPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cHPGoc + num2) + 1980) / 2)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, true));
                }
                else if (cTiemNang >= 100 * (2 * ((int)cHPGoc + num2) + 1980) / 2)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(100 * (2 * ((int)cHPGoc + num2) + 1980) / 2), this, 9007, true));

                }
                myVector.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + true));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, false);
            }
            if (selected == 1)
            {
                if (cTiemNang < cMPGoc + num2)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + cTiemNang + mResources.not_enough_potential_point2 + (cMPGoc + num2), isError: false);
                    return;
                }
                if (cTiemNang > cMPGoc && cTiemNang < 10 * (2 * (cMPGoc + num2) + 180) / 2)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cMPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().mpFrom1000TiemNang + mResources.for_KI, new Command(mResources.increase_upper, this, 9000, true), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector = new(string.Empty);
                if (cTiemNang >= 10 * (2 * (cMPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, true));
                }
                else if (cTiemNang >= 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2((int)cMPGoc + num2), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * ((int)cMPGoc + num2) + 180) / 2), this, 9006, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(100 * (2 * ((int)cMPGoc + num2) + 1980) / 2), this, 9007, true));
                }
                myVector.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + true));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, false);
            }
            if (selected == 2)
            {
                if (cTiemNang < cDamGoc * Char.myCharz().expForOneAdd)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + cTiemNang + mResources.not_enough_potential_point2 + cDamGoc * 100, isError: false);
                    return;
                }
                if (cTiemNang > cDamGoc && cTiemNang < 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + cDamGoc * 100 + mResources.use_potential_point_for2 + Char.myCharz().damFrom1000TiemNang + mResources.for_hit_point, new Command(mResources.increase_upper, this, 9000, true), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector = new(string.Empty);
                if (cTiemNang >= 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd && cTiemNang < 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2((int)cDamGoc * 100), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * (int)cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, true));
                }
                else if (cTiemNang >= 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2((int)cDamGoc * 100), this, 9000, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * (int)cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, true));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(100 * (2 * (int)cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd), this, 9007, true));
                }
                myVector.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + true));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, false);
            }
            if (selected == 3)
            {
                if (cTiemNang < 50000 + (int)cDefGoc * 1000)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + NinjaUtil.getMoneys(cTiemNang) + mResources.not_enough_potential_point2 + NinjaUtil.getMoneys(50000 + (int)cDefGoc * 1000), isError: false);
                    return;
                }
                long number = 2 * ((int)cDefGoc + 5) / 2L * 100000;
                long number2 = 10L * (2 * ((int)cDefGoc + 5) + 9) / 2 * 100000;
                long number3 = 100L * (2 * ((int)cDefGoc + 5) + 99) / 2 * 100000;
                MyVector myVector = new(string.Empty);
                myVector.addElement(new Command(mResources.increase_upper + "\n1 " + mResources.armor + "\n" + Res.formatNumber2(number), this, 9000, true));
                myVector.addElement(new Command(mResources.increase_upper + "\n10 " + mResources.armor + "\n" + Res.formatNumber2(number2), this, 9006, true));
                myVector.addElement(new Command(mResources.increase_upper + "\n100 " + mResources.armor + "\n" + Res.formatNumber2(number3), this, 9007, true));
                myVector.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + true));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, false);
            }
            else if (selected == 4)
            {
                int crit = cCriticalGoc;
                if (crit > t_tiemnang.Length - 1)
                {
                    crit = t_tiemnang.Length - 1;
                }
                long num3 = t_tiemnang[crit];
                if (cTiemNang < num3)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Res.formatNumber2(cTiemNang) + mResources.not_enough_potential_point2 + Res.formatNumber2(num3), isError: false);
                    return;
                }
                GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + Res.formatNumber(num3) + mResources.use_potential_point_for2 + Char.myCharz().criticalFrom1000Tiemnang + mResources.for_crit, new Command(mResources.increase_upper, this, 9000, true), new Command(mResources.CANCEL, this, 4007, null));
            }
            return;
        }
    }

    public void update()
    {
        if (chatTField != null && chatTField.isShow)
        {
            chatTField.update();
            return;
        }
        if (isKiguiXu)
        {
            delayKigui++;
            if (delayKigui == 10)
            {
                delayKigui = 0;
                isKiguiXu = false;
                chatTField.tfChat.setText(string.Empty);
                chatTField.strChat = mResources.kiguiXuchat + " ";
                chatTField.tfChat.name = mResources.input_money;
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setMaxTextLenght(9);
                if (GameCanvas.isTouch)
                {
                    chatTField.tfChat.doChangeToTextBox();
                }
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                if (!Main.isPC)
                {
                    chatTField.startChat(this, string.Empty);
                }
            }
            return;
        }
        if (isKiguiLuong)
        {
            delayKigui++;
            if (delayKigui == 10)
            {
                delayKigui = 0;
                isKiguiLuong = false;
                chatTField.tfChat.setText(string.Empty);
                chatTField.strChat = mResources.kiguiLuongchat + "  ";
                chatTField.tfChat.name = mResources.input_money;
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setMaxTextLenght(9);
                if (GameCanvas.isTouch)
                {
                    chatTField.tfChat.doChangeToTextBox();
                }
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                if (!Main.isPC)
                {
                    chatTField.startChat(this, string.Empty);
                }
            }
            return;
        }
        if (scroll != null)
        {
            scroll.updatecm();
        }
        if (tabIcon != null && tabIcon.isShow)
        {
            tabIcon.update();
            return;
        }
        moveCamera();
        if (isTabInven() && isnewInventory)
        {
            if (eBanner == null)
            {
                eBanner = new Effect(205, 0, 0, 3, 10, -1);
                eBanner.typeEff = 2;
            }
            if (eBanner != null)
            {
                eBanner.update();
            }
        }
        if (waitToPerform > 0)
        {
            waitToPerform--;
            if (waitToPerform == 0)
            {
                if (tabClickedInPointerDown)
                {
                    tabClickedInPointerDown = false;
                    return;
                }
                lastSelect[currentTabIndex] = selected;
                switch (type)
                {
                    case 23:
                        doFireGameInfo();
                        break;
                    case 21:
                        doFirePetMain();
                        break;
                    case 32:
                        this.setBossType();
                        break;
                    case 28:
                        DoFirePet2Main();
                        break;
                    case 0:
                        doFireMain();
                        break;
                    case 2:
                        doFireBox();
                        break;
                    case 3:
                        doFireZone();
                        break;
                    case 1:
                    case 17:
                        doFireShop();
                        break;
                    case 25:
                        doSpeacialSkill();
                        break;
                    case 4:
                        doFireMap();
                        break;
                    case 14:
                        doFireMapTrans();
                        break;
                    case 7:
                        if (Equals(GameCanvas.panel2) && GameCanvas.panel.type == 2)
                        {
                            doFireBox();
                            return;
                        }
                        doFireInventory();
                        break;
                    case 8:
                        doFireLogMessage();
                        break;
                    case 9:
                        doFireArchivement();
                        break;
                    case 10:
                        doFirePlayerMenu();
                        break;
                    case 11:
                        doFireFriend();
                        break;
                    case 16:
                        doFireEnemy();
                        break;
                    case 15:
                        doFireTop();
                        break;
                    case 12:
                        doFireCombine();
                        break;
                    case 13:
                        doFireGiaoDich();
                        break;
                    case 18:
                        doFireChangeFlag();
                        break;
                    case 19:
                        doFireOption();
                        break;
                    case 20:
                        doFireAccount();
                        break;
                    case 22:
                        doFireAuto();
                        break;
                    case 29:
                        doFireHotkeySettings();
                        break;
                    case 26:
                        DoFireModFunc();
                        break;
                }
            }
        }
        for (int i = 0; i < ClanMessage.vMessage.size(); i++)
        {
            ((ClanMessage)ClanMessage.vMessage.elementAt(i)).update();
        }
        updateCombineEff();
    }

    private void doSpeacialSkill()
    {
        string info = Char.myCharz().infoSpeacialSkill[0][selected];
        MyVector myVector8 = new MyVector();
        myVector8.addElement(new Command(ModFunc.strChooseIntrinsic, this, 8011, info));
        GameCanvas.menu.startAt(myVector8, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
    }

    private void doFireGameInfo()
    {
        if (selected != -1)
        {
            infoSelect = selected;
            ((GameInfo)vGameInfo.elementAt(infoSelect)).hasRead = true;
            Rms.saveRMSInt(((GameInfo)vGameInfo.elementAt(infoSelect)).id + string.Empty, 1);
            setTypeGameSubInfo();
        }
    }

    private void doFireHotkeySettings()
    {
        if (selected < 0)
        {
            return;
        }
        int hotkeyCount = ModFunc.hotkeyDefinitions.Length;
        if (selected < hotkeyCount)
        {
            if (hotkeyListeningIndex >= 0)
            {
                ModFunc.GI().SetDraftHotkey(hotkeyListeningIndex, hotkeyListeningOriginal);
            }
            hotkeyListeningIndex = selected;
            hotkeyListeningOriginal = ModFunc.GI().GetDraftHotkey(selected);
            ModFunc.GI().SetDraftHotkey(selected, KeyCode.None);
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
            return;
        }
        hotkeyListeningIndex = -1;
        if (selected == hotkeyCount)
        {
            ModFunc.GI().ResetHotkeyDraft();
            GameScr.info1.addInfo("Đã cài lại phím mặc định. Bấm Lưu để áp dụng", 0);
            return;
        }
        if (selected == hotkeyCount + 1)
        {
            ModFunc.GI().SaveHotkeyDraft();
            GameScr.info1.addInfo("Đã lưu cài đặt phím tắt", 0);
            setTypeMain();
            cmx = (cmtoX = 0);
            return;
        }
        if (selected == hotkeyCount + 2)
        {
            setTypeMain();
            cmx = (cmtoX = 0);
        }
    }

    private void doFireAuto()
    {
    }

    private void DoFirePet2Main()
    {
        if (currentTabIndex == 0)
        {
            if (selected != -1 && selected <= Char.MyPet2z().arrItemBody.Length - 1)
            {
                MyVector myVector = new MyVector(string.Empty);
                Item item = Char.MyPet2z().arrItemBody[selected];
                currItem = item;
                if (currItem != null)
                {
                    myVector.addElement(new Command(mResources.MOVEOUT, this, 2008, currItem));
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
        }
        else if (currentTabIndex != 1)
        {
            if (currentTabIndex == 2)
            {
                doFirePetStatus();
            }
            else if (currentTabIndex == 3)
            {
                doFireInventory();
            }
        }
    }

    private void doFirePetMain()
    {
        // --- TAB 0 MỚI: BAO GỒM CẢ TRANG BỊ VÀ SKILL/THÔNG TIN ---
        if (currentTabIndex == 0)
        {
            // Kiểm tra xem người chơi có đang click vào các dòng Đồ trang bị hay không
            if (selected != -1 && selected <= Char.myPetz().arrItemBody.Length - 1)
            {
                MyVector myVector = new MyVector(string.Empty);
                Item item = Char.myPetz().arrItemBody[selected];
                currItem = item;
                if (currItem != null)
                {
                    myVector.addElement(new Command(mResources.MOVEOUT, this, 2006, currItem));
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            else
            {
                // Nếu click vượt quá số lượng đồ trang bị, tức là người chơi đang click vào các dòng Skill/Thông tin phía dưới
                DoFirePetSkill();
            }
        }
        // --- ĐÔN TAB 2 CŨ (TRẠNG THÁI) LÊN THÀNH TAB 1 ---
        else if (currentTabIndex == 1)
        {
            doFirePetStatus();
        }
        // --- ĐÔN TAB 3 CŨ (HÀNH TRANG) LÊN THÀNH TAB 2 ---
        else if (currentTabIndex == 2)
        {
            doFireInventory();
        }
    }

    private void doFirePetStatus()
    {
        if (selected == -1)
        {
            return;
        }
        if (selected == 5)
        {
            GameCanvas.startYesNoDlg(mResources.sure_fusion, new Command(mResources.YES, (type == 28) ? 888352 : 888351), new Command(mResources.NO, 2001));
        }
        else if (type == 28)
        {
            Service.gI().pet2Status((sbyte)selected);
            if (selected < 4)
            {
                Char.MyPet2z().petStatus = (sbyte)selected;
            }
        }
        else
        {
            Service.gI().petStatus((sbyte)selected);
            if (selected < 4)
            {
                Char.myPetz().petStatus = (sbyte)selected;
            }
        }
    }

    private void doFireTop()
    {
        if (selected >= 0 && selected < vTop.size())
        {
            if (isThachDau)
            {
                Service.gI().sendTop(topName, (sbyte)selected);
                return;
            }
            MyVector myVector = new MyVector(string.Empty);
            myVector.addElement(new Command(mResources.CHAR_ORDER[0], this, 9999, (TopInfo)vTop.elementAt(selected)));
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addThachDauDetail((TopInfo)vTop.elementAt(selected));
        }
    }

    private void doFireMapTrans()
    {
        doFireZone();
    }

    private void doFireGiaoDich()
    {
        if (currentTabIndex == 0 && Equals(GameCanvas.panel))
        {
            doFireInventory();
            return;
        }
        if ((currentTabIndex == 0 && Equals(GameCanvas.panel2)) || currentTabIndex == 2)
        {
            MyVector friendItems = Equals(GameCanvas.panel2) ? GameCanvas.panel2.vFriendGD : GameCanvas.panel.vFriendGD;
            if (selected < 0 || selected >= friendItems.size())
            {
                cp = null;
                return;
            }
            if (Equals(GameCanvas.panel2))
            {
                currItem = (Item)GameCanvas.panel2.vFriendGD.elementAt(selected);
            }
            else
            {
                currItem = (Item)GameCanvas.panel.vFriendGD.elementAt(selected);
            }
            Res.outz2("toi day select= " + selected);
            MyVector myVector = new MyVector();
            myVector.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
            if (currItem != null)
            {
                int menuY = (ModFunc.isInventory ? (getGiaoDichGridSelectedY(friendItems.size()) + ITEM_HEIGHT) : ((selected + 1) * ITEM_HEIGHT)) - cmy + yScroll;
                GameCanvas.menu.startAt(myVector, X, menuY);
                addItemDetail(currItem);
            }
            else
            {
                cp = null;
            }
        }
        if (currentTabIndex == 1)
        {
            if (selected == currentListLength - 3)
            {
                if (isLock)
                {
                    return;
                }
                putMoney();
            }
            else if (selected == currentListLength - 2)
            {
                if (!isAccept)
                {
                    isLock = !isLock;
                    if (isLock)
                    {
                        Service.gI().giaodich(5, -1, -1, -1);
                    }
                    else
                    {
                        hide();
                        InfoDlg.showWait();
                        Service.gI().giaodich(3, -1, -1, -1);
                    }
                }
                else
                {
                    isAccept = false;
                }
            }
            else if (selected == currentListLength - 1)
            {
                if (isLock && !isAccept && isFriendLock)
                {
                    GameCanvas.startYesNoDlg(mResources.do_u_sure_to_trade, new Command(mResources.YES, this, 7002, null), new Command(mResources.NO, this, 4005, null));
                }
            }
            else
            {
                if (isLock)
                {
                    return;
                }
                currItem = (Item)GameCanvas.panel.vMyGD.elementAt(selected);
                MyVector myVector2 = new MyVector();
                myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
                if (currItem != null)
                {
                    int menuY = (ModFunc.isInventory ? (getGiaoDichGridSelectedY(GameCanvas.panel.vMyGD.size()) + ITEM_HEIGHT) : ((selected + 1) * ITEM_HEIGHT)) - cmy + yScroll;
                    GameCanvas.menu.startAt(myVector2, X, menuY);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
        }
        if (GameCanvas.isTouch)
        {
            selected = -1;
        }
    }

    private void doFireCombine()
    {
        if (currentTabIndex == 0)
        {
            if (selected == -1 || vItemCombine.size() == 0)
            {
                return;
            }
            if (selected == vItemCombine.size())
            {
                keyTouchCombine = -1;
                selected = (GameCanvas.isTouch ? (-1) : 0);
                InfoDlg.showWait();
                Service.gI().combine(1, vItemCombine);
                return;
            }
            if (selected > vItemCombine.size() - 1)
            {
                return;
            }
            currItem = (Item)GameCanvas.panel.vItemCombine.elementAt(selected);
            MyVector myVector = new MyVector();
            myVector.addElement(new Command(mResources.GETOUT, this, 6001, currItem));
            if (ModFunc.GI().isAutoPhaLe)
            {
                myVector.addElement(new Command("Nhập số sao", this, 8010, currItem));
            }
            if (currItem != null)
            {
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addItemDetail(currItem);
            }
            else
            {
                cp = null;
            }
        }
        if (currentTabIndex == 1)
        {
            doFireInventory();
        }
    }

    private void doFirePlayerMenu()
    {
        if (selected != -1)
        {
            isSelectPlayerMenu = true;
            hide();
        }
    }

    private void doFireShop()//tabshop
    {
        currItem = null;
        if (selected < 0)
        {
            return;
        }
        MyVector myVector = new MyVector();
        if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
        {
            // Validate selected is within bounds for shop items
            if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
            {
                if (selected >= 0 && selected < Char.myCharz().arrItemShop[currentTabIndex].Length)
                {
                    currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                }
                else
                {
                    // Selected is out of bounds, clamp to last valid item
                    if (Char.myCharz().arrItemShop[currentTabIndex].Length > 0)
                    {
                        selected = Char.myCharz().arrItemShop[currentTabIndex].Length - 1;
                        currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                    }
                    else
                    {
                        currItem = null;
                    }
                }
            }
            else
            {
                currItem = null;
            }
            if (currItem != null)
            {
                if (currItem.isBuySpec)
                {
                    if (currItem.buySpec > 0)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buySpec), this, 3005, currItem));
                        myVector.addElement(new Command(ModFunc.strAutoBuy, this, 3006, currItem));
                    }
                }
                else if (typeShop == 4)
                {
                    myVector.addElement(new Command(mResources.receive_upper, this, 30001, currItem));
                    myVector.addElement(new Command(mResources.DELETE, this, 30002, currItem));
                    myVector.addElement(new Command(mResources.receive_all, this, 30003, currItem));
                }
                else if (currItem.buyCoin == 0 && currItem.buyGold == 0)
                {
                    if (currItem.powerRequire != 0L)
                    {
                        myVector.addElement(new Command(mResources.learn_with + "\n" + Res.formatNumber(currItem.powerRequire) + " \n" + mResources.potential, this, 3004, currItem));
                    }
                    else
                    {
                        myVector.addElement(new Command(mResources.receive_upper + "\n" + mResources.free, this, 3000, currItem));
                    }
                }
                else if (typeShop == 8)
                {
                    if (currItem.buyCoin > 0)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.XU, this, 30001, currItem));
                    }
                    if (currItem.buyGold > 0)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + mResources.LUONG, this, 30002, currItem));
                    }
                }
                else if (typeShop != 2)
                {
                    if (currItem.buyCoin > 0)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.XU, this, 3000, currItem));
                    }
                    if (currItem.buyGold > 0)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + mResources.LUONG, this, 3001, currItem));
                    }
                    myVector.addElement(new Command(ModFunc.strAutoBuy, this, 3006, currItem));
                }
                else
                {
                    if (currItem.buyCoin != -1)
                    {
                        // đồi bằng Ngọc Xanh
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.THOIVANG, this, 10016, currItem));
                    }
                    if (currItem.buyGold != -1)
                    {
                        myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + mResources.LUONG, this, 10017, currItem));
                    }
                }
            }
        }
        else if (typeShop == 0)
        {
            currItem = null;
            if (!ModFunc.isInventory)
            {
                // Validate selected is within bounds for current tab
                int totalItems = Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length;
                int itemsPerTab = 20;
                int totalTabs = (totalItems / itemsPerTab) + ((totalItems % itemsPerTab > 0) ? 1 : 0);
                int currentTabItemCount = (newSelected == totalTabs - 1 && totalItems % itemsPerTab > 0) 
                    ? (totalItems % itemsPerTab) : itemsPerTab;
                
                // Ensure selected doesn't exceed current tab's item count (selected 0 is tab selector)
                if (selected > currentTabItemCount)
                {
                    selected = currentTabItemCount; // Clamp to last valid item in tab
                }
            }
            
            if (!GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody))
            {
                int bagIndex = GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                if (bagIndex >= 0 && bagIndex < Char.myCharz().arrItemBag.Length)
                {
                    Item item = Char.myCharz().arrItemBag[bagIndex];
                    if (item != null)
                    {
                        currItem = item;
                    }
                }
            }
            else
            {
                int bodyIndex = GetInventorySelect_body(selected, newSelected);
                if (bodyIndex >= 0 && bodyIndex < Char.myCharz().arrItemBody.Length)
                {
                    Item item2 = Char.myCharz().arrItemBody[bodyIndex];
                    if (item2 != null)
                    {
                        currItem = item2;
                    }
                }
            }
            if (currItem != null)
            {
                myVector.addElement(new Command(mResources.SALE, this, 3002, currItem));
            }
        }
        else
        {
            if (type == 17)
            {
                if (4 >= 0 && 4 < Char.myCharz().arrItemShop.Length)
                {
                    if (selected >= 0 && selected < Char.myCharz().arrItemShop[4].Length)
                    {
                        currItem = Char.myCharz().arrItemShop[4][selected];
                    }
                    else
                    {
                        if (Char.myCharz().arrItemShop[4].Length > 0)
                        {
                            selected = Char.myCharz().arrItemShop[4].Length - 1;
                            currItem = Char.myCharz().arrItemShop[4][selected];
                        }
                        else
                        {
                            currItem = null;
                        }
                    }
                }
                else
                {
                    currItem = null;
                }
            }
            else
            {
                if (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length)
                {
                    if (selected >= 0 && selected < Char.myCharz().arrItemShop[currentTabIndex].Length)
                    {
                        currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                    }
                    else
                    {
                        if (Char.myCharz().arrItemShop[currentTabIndex].Length > 0)
                        {
                            selected = Char.myCharz().arrItemShop[currentTabIndex].Length - 1;
                            currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                        }
                        else
                        {
                            currItem = null;
                        }
                    }
                }
                else
                {
                    currItem = null;
                }
            }
            if (currItem.buyType == 0)
            {
                if (currItem.isHaveOption(87))
                {
                    myVector.addElement(new Command(mResources.kiguiLuong, this, 10013, currItem));
                }
                else
                {
                    myVector.addElement(new Command(mResources.kiguiXu, this, 10012, currItem));
                }
            }
            else if (currItem.buyType == 1)
            {
                myVector.addElement(new Command(mResources.huykigui, this, 10014, currItem));
                myVector.addElement(new Command(mResources.upTop, this, 10018, currItem));
            }
            else if (currItem.buyType == 2)
            {
                myVector.addElement(new Command(mResources.nhantien, this, 10015, currItem));
            }
        }
        if (currItem != null)
        {
            Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addItemDetail(currItem);
        }
        else
        {
            cp = null;
        }
    }

    private void doFireArchivement()
    {
        if (selected >= 0 && selected < Char.myCharz().arrArchive.Length && Char.myCharz().arrArchive[selected].isFinish && !Char.myCharz().arrArchive[selected].isRecieve)
        {
            if (!GameCanvas.isTouch)
            {
                Service.gI().getArchivemnt(selected);
            }
            else if (GameCanvas.px > xScroll + wScroll - 40)
            {
                Service.gI().getArchivemnt(selected);
            }
        }
    }

    private void doFireInventory()
    {
        if (tabClickedInPointerDown)
        {
            tabClickedInPointerDown = false; // Reset flag
            return;
        }
        if (Char.myCharz().statusMe == 14)
        {
            GameCanvas.startOKDlg(mResources.can_not_do_when_die);
        }
        else
        {
            if (selected == -1)
            {
                return;
            }
            if (selected == 0 && !ModFunc.isInventory)
            {
                setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, resetSelect: false);
                return;
            }
            currItem = null;
            MyVector myVector = new MyVector();
            if (isnewInventory)
            {
                int itemIndex = -1;
                if (newSelected == 0)
                {
                    // Tab 0: items từ arrItemBody (0-19)
                    itemIndex = sellectInventory;
                    if (itemIndex >= 0 && itemIndex < Char.myCharz().arrItemBody.Length)
                    {
                        itemInvenNew = Char.myCharz().arrItemBody[itemIndex];
                    }
                    else
                    {
                        itemInvenNew = null;
                    }
                }
                else
                {
                    itemIndex = (newSelected - 1) * 20 + sellectInventory;
                    int bagStartIndex = itemIndex - Char.myCharz().arrItemBody.Length;
                    if (bagStartIndex >= 0 && bagStartIndex < Char.myCharz().arrItemBag.Length)
                    {
                        itemInvenNew = Char.myCharz().arrItemBag[bagStartIndex];
                    }
                    else
                    {
                        itemInvenNew = null;
                    }
                }
                currItem = itemInvenNew;
                if (currItem == null)
                {
                    return;
                }
                if (newSelected == 0)
                {
                    myVector.addElement(new Command(mResources.GETOUT, this, 2002, currItem));
                }
                else if (GameCanvas.panel.type == 12)
                {
                    myVector.addElement(new Command(mResources.use_for_combine, this, 6000, currItem));
                }
                else if (GameCanvas.panel.type == 13)
                {
                    myVector.addElement(new Command(mResources.use_for_trade, this, 7000, currItem));
                }
                else if (currItem.isTypeBody())
                {
                    myVector.addElement(new Command(mResources.USE, this, 2000, currItem));
                    if (Char.myCharz().havePet)
                    {
                        myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                    }
                    if (Char.myCharz().havePet2)
                    {
                        myVector.addElement(new Command(ModFunc.strUseForPet2, this, 2007, currItem));
                    }
                }
                else
                {
                    myVector.addElement(new Command(mResources.USE, this, 2001, currItem));
                    if (Char.myCharz().havePet)
                    {
                        myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                    }
                    if (Char.myCharz().havePet2)
                    {
                        myVector.addElement(new Command(ModFunc.strUseForPet2, this, 2007, currItem));
                    }
                }
            }
            else if (!GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody))
            {
                int bagIndex = GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                if (bagIndex >= 0 && bagIndex < Char.myCharz().arrItemBag.Length)
                {
                    Item item = Char.myCharz().arrItemBag[bagIndex];
                    if (item != null)
                    {
                        currItem = item;
                        if (GameCanvas.panel.type == 12)
                        {
                            myVector.addElement(new Command(mResources.use_for_combine, this, 6000, currItem));
                        }
                        else if (GameCanvas.panel.type == 13)
                        {
                            myVector.addElement(new Command(mResources.use_for_trade, this, 7000, currItem));
                        }
                        else if (item.isTypeBody())
                        {
                            myVector.addElement(new Command(mResources.USE, this, 2000, currItem));
                            if (Char.myCharz().havePet)
                            {
                                myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                            }
                            if (Char.myCharz().havePet2)
                            {
                                myVector.addElement(new Command(ModFunc.strUseForPet2, this, 2007, currItem));
                            }
                        }
                        else
                        {
                            myVector.addElement(new Command(mResources.USE, this, 2001, currItem));
                            if (Char.myCharz().havePet)
                            {
                                myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                            }
                            if (Char.myCharz().havePet2)
                            {
                                myVector.addElement(new Command(ModFunc.strUseForPet2, this, 2007, currItem));
                            }
                        }
                    }
                }
            }
            else
            {
                int bodyIndex = GetInventorySelect_body(selected, newSelected);
                if (bodyIndex >= 0 && bodyIndex < Char.myCharz().arrItemBody.Length)
                {
                    Item item2 = Char.myCharz().arrItemBody[bodyIndex];
                    if (item2 != null)
                    {
                        currItem = item2;
                        myVector.addElement(new Command(mResources.GETOUT, this, 2002, currItem));
                    }
                }
            }
            if (currItem != null)
            {
                Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
                if (GameCanvas.panel.type != 12 && GameCanvas.panel.type != 13)
                {
                    if (position == 0)
                    {
                        myVector.addElement(new Command(mResources.MOVEOUT, this, 2003, currItem));
                        if (currItem.template.type == 29 || currItem.template.type == 33 || currItem.template.id == 380 || currItem.quantity >= 2)
                        {
                            if (ModFunc.GI().listItemAuto.Exists((ItemAuto i) => i.id == currItem.template.id))
                            {
                                myVector.addElement(new Command(ModFunc.strRemoveAutoItem, ModFunc.GI(), 501, currItem));
                            }
                            else
                            {
                                myVector.addElement(new Command(ModFunc.strAddAutoItem, ModFunc.GI(), 500, currItem));
                            }
                        }
                    }
                    if (position == 1)
                    {
                        myVector.addElement(new Command(mResources.SALE, this, 3002, currItem));
                    }
                    if (ModFunc.isFilterItem)
                    {
                        if (ModFunc.listFilterItems.Exists((ItemAutoFilter i) => i.id == currItem.template.id))
                        {
                            myVector.addElement(new Command(ModFunc.strRemoveFilterItem, ModFunc.GI(), 503, currItem));
                        }
                        else
                        {
                            myVector.addElement(new Command(ModFunc.strAddFilterItem, ModFunc.GI(), 502, currItem));
                        }
                    }
                }
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addItemDetail(currItem);

            }
            else
            {
                cp = null;
            }
        }
    }


    private void doRada()
    {
        hide();
        if (RadarScr.list == null || RadarScr.list.size() == 0)
        {
            Service.gI().SendRada(0, -1);
            RadarScr.gI().switchToMe();
        }
        else
        {
            RadarScr.gI().switchToMe();
        }
    }

    private void doFireTool()
    {
        if (selected < 0)
        {
            return;
        }
        if (SoundMn.IsDelAcc && selected == strTool.Length - 1)
        {
            Service.gI().sendDelAcc();
            return;
        }
        if (selected >= 0 && selected < strTool.Length && strTool[selected] == ModFunc.strHotkeySettings)
        {
            setTypeHotkeySettings();
            return;
        }
        int toolSelected = selected;
        if (!Char.myCharz().havePet && !Char.myCharz().havePet2)
        {
            switch (toolSelected)
            {
                case 0:
                    setTypeGameInfo();
                    break;
                case 1:
                    hide();
                    Service.gI().openMenu(54);
                    break;
                case 2:
                    SetTypeModFunc();
                    break;
                case 3:
                    setTypeInfoBoss();
                    break;
                case 4:
                    SetTypePlayerInfo();
                    break;
                case 5:
                    doRada();
                    break;
                case 6:
                    Service.gI().getFlag(0, -1);
                    InfoDlg.showWait();
                    break;
                case 7:
                    if (Char.myCharz().statusMe == 14)
                    {
                        GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                        break;
                    }
                    ModFunc.GI().userOpenZones = true;
                    Service.gI().openUIZone();
                    break;
                case 8:
                    ModFunc.DoChatGlobal();
                    break;
                case 9:
                    setTypeAccount();
                    break;
                case 10:
                    setTypeOption();
                    break;
                case 11:
                    GameCanvas.loginScr.backToRegister();
                    break;
            }
            return;
        }
        if (Char.myCharz().havePet && Char.myCharz().havePet2)
        {
            switch (toolSelected)
            {
                case 0:
                    setTypeGameInfo();
                    break;
                case 1:
                    hide();
                    Service.gI().openMenu(54);
                    break;
                case 2:
                    SetTypeModFunc();
                    break;
                case 3:
                    setTypeInfoBoss();
                    break;
                case 4:
                    SetTypePlayerInfo();
                    break;
                case 5:
                    doRada();
                    break;
                case 6:
                    doFirePet();
                    break;
                case 7:
                    doFirePet2();
                    break;
                case 8:
                    Service.gI().getFlag(0, -1);
                    InfoDlg.showWait();
                    break;
                case 9:
                    if (Char.myCharz().statusMe == 14)
                    {
                        GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                        break;
                    }
                    ModFunc.GI().userOpenZones = true;
                    Service.gI().openUIZone();
                    break;
                case 10:
                    ModFunc.DoChatGlobal();
                    break;
                case 11:
                    setTypeAccount();
                    break;
                case 12:
                    setTypeOption();
                    break;
                case 13:
                    GameCanvas.loginScr.backToRegister();
                    break;
            }
            return;
        }
        switch (toolSelected)
        {
            case 0:
                setTypeGameInfo();
                break;
            case 1:
                hide();
                Service.gI().openMenu(54);
                break;
            case 2:
                SetTypeModFunc();
                break;
            case 3:
                setTypeInfoBoss();
                break;
            case 4:
                SetTypePlayerInfo();
                break;
            case 5:
                doRada();
                break;
            case 6:
                if (Char.myCharz().havePet)
                {
                    doFirePet();
                }
                else
                {
                    doFirePet2();
                }
                break;
            case 7:
                Service.gI().getFlag(0, -1);
                InfoDlg.showWait();
                break;
            case 8:
                if (Char.myCharz().statusMe == 14)
                {
                    GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                    break;
                }
                ModFunc.GI().userOpenZones = true;
                Service.gI().openUIZone();
                break;
            case 9:
                ModFunc.DoChatGlobal();
                break;
            case 10:
                setTypeAccount();
                break;
            case 11:
                setTypeOption();
                break;
            case 12:
                GameCanvas.loginScr.backToRegister();
                break;
        }
    }

    private void setTypeGameSubInfo()
    {
        string content = ((GameInfo)vGameInfo.elementAt(infoSelect)).content;
        contenInfo = splitGameInfoContent(content, wScroll - 40);
        currentListLength = contenInfo.Length;
        ITEM_HEIGHT = 16;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        type = 24;
        setType(0);
    }

    private string[] splitGameInfoContent(string content, int lineWidth)
    {
        string normalized = (content ?? string.Empty).Replace("\\r\\n", "\n").Replace("\\n", "\n").Replace("\r\n", "\n").Replace('\r', '\n');
        string[] logicalLines = normalized.Split(new char[1] { '\n' }, StringSplitOptions.None);
        List<string> result = new List<string>();
        for (int i = 0; i < logicalLines.Length; i++)
        {
            string[] wrappedLines = mFont.tahoma_7_grey.splitFontArray(logicalLines[i], lineWidth);
            if (wrappedLines.Length == 0)
            {
                result.Add(string.Empty);
                continue;
            }
            result.AddRange(wrappedLines);
        }
        return result.ToArray();
    }

    private void SetTypePlayerInfo()
    {
        string content = "Tộc: " + ((Char.myCharz().cgender == 0) ? "Trái Đất" : ((Char.myCharz().cgender == 1) ? "Namek" : "Xayda")) + "\nHP: " + NinjaUtil.getMoneys((long)Char.myCharz().cHP) + " / " + NinjaUtil.getMoneys((long)Char.myCharz().cHPFull) + "\nKI: " + NinjaUtil.getMoneys((long)Char.myCharz().cMP) + " / " + NinjaUtil.getMoneys((long)Char.myCharz().cMPFull) + "\nSĐ: " + NinjaUtil.getMoneys((long)Char.myCharz().cDamFull) + "\nChí mạng: " + Char.myCharz().cCriticalFull + "%\nGiảm sát thương: " + Char.myCharz().cGiamST + "%\nST chí mạng: " + Char.myCharz().cCritDameFull + "%\nNé đòn: " + Char.myCharz().tlNeDon + "%\nHút HP: " + Char.myCharz().tlHutHp + "%\nHút KI: " + Char.myCharz().tlHutMp + "%\nGiảm TDHS: " + Char.myCharz().tileGiamTDHS + "%\nGiảm TDHS: " + Char.myCharz().timeGiamTDHS + " giây\nKháng TDHS: " + (Char.myCharz().khangTDHS ? "Có" : "Không") + "\nKháng lạnh: " + (Char.myCharz().isKhongLanh ? "Có" : "Không") + "\nVô hình: " + (Char.myCharz().wearingVoHinh ? "Có" : "Không") + "\nDịch chuyển: " + (Char.myCharz().teleport ? "Có" : "Không") + "\n";
        contenInfo = mFont.tahoma_7_grey.splitFontArray(content, wScroll - 40);
        currentListLength = contenInfo.Length;
        ITEM_HEIGHT = 16;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        type = 27;
        setType(0);
    }

    private void setTypeGameInfo()
    {
        currentListLength = vGameInfo.size();
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        type = 23;
        setType(0);
    }

    public void doFirePet()
    {
        try
        {
            InfoDlg.showWait();
            Service.gI().petInfo();
            timeShow = 20;
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                GameCanvas.panel2 = new Panel();
                GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                GameCanvas.panel2.setTypeBodyOnly();
                GameCanvas.panel2.show();
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
            else
            {
                GameCanvas.panel.tabName[21] = mResources.petMainTab;
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
        }
        catch (Exception exception)
        {
            Debug.LogException(exception);
        }
    }

    private void doFirePet2()
    {
        InfoDlg.showWait();
        Service.gI().PetInfo2();
        timeShow = 20;
    }

    private void searchClan()
    {
        chatTField.strChat = mResources.input_clan_name;
        chatTField.tfChat.name = mResources.clan_name;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.isFocus = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    private void chatClan()
    {
        chatTField.strChat = mResources.chat_clan;
        chatTField.tfChat.name = mResources.CHAT;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.isFocus = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    public void creatClan()
    {
        chatTField.strChat = mResources.input_clan_name_to_create;
        chatTField.tfChat.name = mResources.input_clan_name;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    public void putMoney()
    {
        if (chatTField == null)
        {
            chatTField = new ChatTextField();
            chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
            chatTField.initChatTextField();
            chatTField.parentScreen = GameCanvas.panel;
        }
        chatTField.strChat = mResources.input_money_to_trade;
        chatTField.tfChat.name = mResources.input_money;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
        chatTField.tfChat.setMaxTextLenght(9);
        if (GameCanvas.isTouch)
        {
            chatTField.tfChat.doChangeToTextBox();
        }
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    public void putQuantily()
    {
        if (chatTField == null)
        {
            chatTField = new ChatTextField();
            chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
            chatTField.initChatTextField();
            chatTField.parentScreen = GameCanvas.panel;
        }
        chatTField.strChat = mResources.input_quantity_to_trade;
        chatTField.tfChat.name = mResources.input_quantity;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
        if (GameCanvas.isTouch)
        {
            chatTField.tfChat.doChangeToTextBox();
        }
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    public void chagenSlogan()
    {
        chatTField.strChat = mResources.input_clan_slogan;
        chatTField.tfChat.name = mResources.input_clan_slogan;
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.isFocus = true;
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        if (Main.isWindowsPhone)
        {
            chatTField.tfChat.strInfo = chatTField.strChat;
        }
        if (!Main.isPC)
        {
            chatTField.startChat(this, string.Empty);
        }
    }

    public void changeIcon()
    {
        if (tabIcon == null)
        {
            tabIcon = new TabClanIcon();
        }
        tabIcon.text = chatTField.tfChat.getText();
        tabIcon.show(isGetName: false);
        chatTField.isShow = false;
    }

    private void addFriend(InfoItem info)
    {
        string text = "|0|1|" + info.charInfo.cName;
        text += "\n";
        text = ((!info.isOnline) ? (text + "|3|1|" + mResources.is_offline) : (text + "|4|1|" + mResources.is_online));
        text += "\n--";
        string text2 = text;
        text = text2 + "\n|5|" + mResources.power + ": " + info.s;
        cp = new ChatPopup();
        popUpDetailInit(cp, text);
        charInfo = info.charInfo;
        currItem = null;
    }

    private void doFireEnemy()
    {
        if (selected >= 0 && selected < vEnemy.size() && vEnemy.size() != 0)
        {
            MyVector myVector = new MyVector();
            currInfoItem = selected;
            myVector.addElement(new Command(mResources.REVENGE, this, 10000, (InfoItem)vEnemy.elementAt(currInfoItem)));
            myVector.addElement(new Command(mResources.DELETE, this, 10001, (InfoItem)vEnemy.elementAt(currInfoItem)));
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addFriend((InfoItem)vEnemy.elementAt(selected));
        }
    }

    private void doFireFriend()
    {
        if (selected >= 0 && selected < vFriend.size() && vFriend.size() != 0)
        {
            MyVector myVector = new MyVector();
            currInfoItem = selected;
            InfoItem infoItem = (InfoItem)vFriend.elementAt(currInfoItem);
            myVector.addElement(new Command(mResources.CHAT, this, 8001, infoItem));
            myVector.addElement(new Command(mResources.DELETE, this, 8002, infoItem));
            myVector.addElement(new Command(mResources.den, this, 8004, infoItem.charInfo.charID));
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addFriend((InfoItem)vFriend.elementAt(selected));
        }
    }

    private void doFireChangeFlag()
    {
        if (selected >= 0)
        {
            MyVector myVector = new MyVector();
            currInfoItem = selected;
            myVector.addElement(new Command(mResources.change_flag, this, 10030, null));
            myVector.addElement(new Command(mResources.BACK, this, 10031, null));
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
        }
    }

    private void doFireLogMessage()
    {
        if (selected == 0)
        {
            isViewChatServer = !isViewChatServer;
            Rms.saveRMSInt("viewchat", isViewChatServer ? 1 : 0);
            if (GameCanvas.isTouch)
            {
                selected = -1;
            }
        }
        else if (selected >= 0 && logChat.size() != 0)
        {
            MyVector myVector = new MyVector();
            currInfoItem = selected - 1;
            InfoItem infoItem = (InfoItem)logChat.elementAt(currInfoItem);
            myVector.addElement(new Command(mResources.CHAT, this, 8001, infoItem));
            myVector.addElement(new Command(mResources.make_friend, this, 8003, infoItem));
            myVector.addElement(new Command(ModFunc.strTeleportTo, this, 8004, infoItem.charInfo.charID));
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addLogMessage((InfoItem)logChat.elementAt(selected - 1));
        }
    }

	private void doFireClanOption()
	{
		try
		{
			partID = null;
			charInfo = null;
			Res.outz("cSelect= " + cSelected);
			if (selected < 0)
			{
				cSelected = -1;
				return;
			}
			if (Char.myCharz().clan == null)
			{
				if (selected == 0)
				{
					if (cSelected == 0)
					{
						searchClan();
					}
					else if (cSelected == 1)
					{
						InfoDlg.showWait();
						creatClan();
						Service.gI().getClan(1, -1, null);
					}
				}
				else if (selected != -1)
				{
					if (selected == 1)
					{
						if (isSearchClan)
						{
							Service.gI().searchClan(string.Empty);
						}
						else if (isViewMember && currClan != null)
						{
							GameCanvas.startYesNoDlg(mResources.do_u_want_join_clan + currClan.name, new Command(mResources.YES, this, 4000, currClan), new Command(mResources.NO, this, 4005, currClan));
						}
					}
					else if (isSearchClan)
					{
						currClan = getCurrClan();
						if (currClan != null)
						{
							MyVector myVector = new MyVector();
							myVector.addElement(new Command(mResources.request_join_clan, this, 4000, currClan));
							myVector.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
							GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
							addClanDetail(getCurrClan());
						}
					}
					else if (isViewMember)
					{
						currMem = getCurrMember();
						if (currMem != null)
						{
							MyVector myVector2 = new MyVector();
							myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currClan));
							GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
							GameCanvas.menu.startAt(myVector2, 0, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
							addClanMemberDetail(currMem);
						}
					}
				}
			}
			else if (selected == 0)
			{
				if (isMessage)
				{
					if (cSelected == 0)
					{
						if (myMember.size() > 1)
						{
							chatClan();
						}
						else
						{
							member = null;
							isSearchClan = false;
							isViewMember = true;
							isMessage = false;
							currentListLength = myMember.size() + 2;
							initTabClans();
						}
					}
					if (cSelected == 1)
					{
						Service.gI().clanMessage(1, null, -1);
					}
					if (cSelected == 2)
					{
						member = null;
						isSearchClan = false;
						isViewMember = true;
						isMessage = false;
						currentListLength = myMember.size() + 2;
						initTabClans();
						getCurrClanOtion();
					}
				}
				else if (isViewMember)
				{
					if (cSelected == 0)
					{
						isSearchClan = false;
						isViewMember = false;
						isMessage = true;
						currentListLength = ClanMessage.vMessage.size() + 2;
						initTabClans();
					}
					if (cSelected == 1)
					{
						if (myMember.size() > 1)
						{
							Service.gI().leaveClan();
						}
						else
						{
							chagenSlogan();
						}
					}
					if (cSelected == 2)
					{
						if (myMember.size() > 1)
						{
							chagenSlogan();
						}
						else
						{
							Service.gI().getClan(3, -1, null);
						}
					}
					if (cSelected == 3)
					{
						Service.gI().getClan(3, -1, null);
					}
				}
			}
			else if (selected == 1)
			{
				if (isSearchClan)
				{
					Service.gI().searchClan(string.Empty);
				}
			}
			else if (isSearchClan)
			{
				currClan = getCurrClan();
				if (currClan != null)
				{
					MyVector myVector3 = new MyVector();
					myVector3.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
					GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
					addClanDetail(getCurrClan());
				}
			}
			else if (isViewMember)
			{
				Res.outz("TOI DAY 1");
				currMem = getCurrMember();
				if (currMem != null)
				{
					MyVector myVector4 = new MyVector();
					Res.outz("TOI DAY 2");
					if (member != null)
					{
						myVector4.addElement(new Command(mResources.CLOSE, this, 8000, null));
						Res.outz("TOI DAY 3");
					}
					else if (myMember != null)
					{
						Res.outz("TOI DAY 4");
						Res.outz("my role= " + Char.myCharz().role);
						if (Char.myCharz().charID == currMem.ID || Char.myCharz().role == 2)
						{
							myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
						}
						if (Char.myCharz().role < 2 && Char.myCharz().charID != currMem.ID)
						{
							Res.outz("TOI DAY");
							if (currMem.role == 0 || currMem.role == 1)
							{
								myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
							}
							if (currMem.role == 2)
							{
								myVector4.addElement(new Command(mResources.create_clan_co_leader, this, 5002, currMem));
							}
							if (Char.myCharz().role == 0)
							{
								myVector4.addElement(new Command(mResources.create_clan_leader, this, 5001, currMem));
								if (currMem.role == 1)
								{
									myVector4.addElement(new Command(mResources.disable_clan_mastership, this, 5003, currMem));
								}
							}
						}
						if (Char.myCharz().role < currMem.role)
						{
							myVector4.addElement(new Command(mResources.kick_clan_mem, this, 5004, currMem));
						}
					}
					GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
					addClanMemberDetail(currMem);
				}
			}
			else if (isMessage)
			{
				currMess = getCurrMessage();
				if (currMess != null)
				{
					if (currMess.type == 0)
					{
						MyVector myVector5 = new MyVector();
						myVector5.addElement(new Command(mResources.CLOSE, this, 8000, currMess));
						GameCanvas.menu.startAt(myVector5, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
						addMessageDetail(currMess);
					}
					else if (currMess.type == 1)
					{
						if (currMess.playerId != Char.myCharz().charID && cSelected != -1)
						{
							Service.gI().clanDonate(currMess.id);
						}
					}
					else if (currMess.type == 2 && currMess.option != null)
					{
						if (cSelected == 0)
						{
							Service.gI().joinClan(currMess.id, 1);
						}
						else if (cSelected == 1)
						{
							Service.gI().joinClan(currMess.id, 0);
						}
					}
				}
			}
			if (GameCanvas.isTouch)
			{
				cSelected = -1;
				selected = -1;
			}
		}
		catch (Exception)
		{
			throw;
		}
	}


    private void doFireMain()
    {
        try
        {
            if (currentTabIndex == 0)
            {
                setTypeMap();
            }
            if (currentTabIndex == 1)
            {
                doFireInventory();
            }
            if (currentTabIndex == 2)
            {
                doFireSkill();
            }
            if (currentTabIndex == 3)
            {
                if (mainTabName.Length == 4)
                {
                    doFireTool();
                }
                else
                {
                    doFireClanOption();
                }
            }
            if (currentTabIndex == 4)
            {
                doFireTool();
            }
        }
        catch (Exception ex)
        {
            Res.outz("Throw ex " + ex.StackTrace);
        }
    }

    private void doFireSkill()
    {
        if (selected < 0)
        {
            return;
        }
        if (Char.myCharz().statusMe == 14)
        {
            GameCanvas.startOKDlg(mResources.can_not_do_when_die);
            return;
        }
        if (selected == 0 || selected == 1 || selected == 2 || selected == 3 || selected == 4 || selected == 5)
        {
            long cTiemNang = Char.myCharz().cTiemNang;
            double cHPGoc = (int)Char.myCharz().cHPGoc;
            double cMPGoc = (int)Char.myCharz().cMPGoc;
            double cDamGoc = (int)Char.myCharz().cDamGoc;
            double cDefGoc = (int)Char.myCharz().cDefGoc;
            _ = Char.myCharz().cCriticalGoc;
            int num2 = 1000;
            if (selected == 0)
            {
                if (cTiemNang < (int)Char.myCharz().cHPGoc + num2)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cHPGoc + num2), isError: false);
                    return;
                }
                if (cTiemNang > (int)cHPGoc && cTiemNang < 10.0 * (2.0 * (cHPGoc + num2) + 180.0) / 2.0)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cHPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().hpFrom1000TiemNang + mResources.for_HP, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector = new MyVector(string.Empty);
                if (cTiemNang >= 10.0 * (2.0 * ((int)cHPGoc + num2) + 180.0) / 2.0 && cTiemNang < 100.0 * (2.0 * ((int)cHPGoc + num2) + 1980.0) / 2.0)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, null));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, null));
                }
                else if (cTiemNang >= 100.0 * (2.0 * ((int)cHPGoc + num2) + 1980.0) / 2.0)
                {
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, null));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, null));
                    myVector.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(100 * (2 * ((int)cHPGoc + num2) + 1980) / 2), this, 9007, null));
                }
                myVector.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + false));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, isPet: false);
            }
            if (selected == 1)
            {
                if (Char.myCharz().cTiemNang < Char.myCharz().cMPGoc + num2)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cMPGoc + num2));
                    return;
                }
                if (cTiemNang > cMPGoc && cTiemNang < 10.0 * (2.0 * (cMPGoc + num2) + 180.0) / 2.0)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cMPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().mpFrom1000TiemNang + mResources.for_KI, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector2 = new MyVector(string.Empty);
                if (cTiemNang >= 10.0 * (2.0 * (cMPGoc + num2) + 180.0) / 2.0 && cTiemNang < 100.0 * (2.0 * (cMPGoc + num2) + 1980.0) / 2.0)
                {
                    myVector2.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2((int)cHPGoc + num2), this, 9000, null));
                    myVector2.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * ((int)cHPGoc + num2) + 180) / 2), this, 9006, null));
                }
                else if (cTiemNang >= 100.0 * (2.0 * (cMPGoc + num2) + 1980.0) / 2.0)
                {
                    myVector2.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2((int)cMPGoc + num2), this, 9000, null));
                    myVector2.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * ((int)cMPGoc + num2) + 180) / 2), this, 9006, null));
                    myVector2.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(100 * (2 * ((int)cMPGoc + num2) + 1980) / 2), this, 9007, null));
                }
                myVector2.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + false));
                GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, isPet: false);
            }
            if (selected == 2)
            {
                if (Char.myCharz().cTiemNang < Char.myCharz().cDamGoc * Char.myCharz().expForOneAdd)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + cDamGoc * 100.0);
                    return;
                }
                if (cTiemNang > cDamGoc && cTiemNang < 10.0 * (2.0 * cDamGoc + 9.0) / 2.0 * Char.myCharz().expForOneAdd)
                {
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + cDamGoc * 100.0 + mResources.use_potential_point_for2 + Char.myCharz().damFrom1000TiemNang + mResources.for_hit_point, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                    return;
                }
                MyVector myVector3 = new MyVector(string.Empty);
                if (cTiemNang >= 10.0 * (2.0 * cDamGoc + 9.0) / 2.0 * Char.myCharz().expForOneAdd && cTiemNang < 100.0 * (2.0 * cDamGoc + 99.0) / 2.0 * Char.myCharz().expForOneAdd)
                {
                    myVector3.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2((int)cDamGoc * 100), this, 9000, null));
                    myVector3.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * (int)cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                }
                else if (cTiemNang >= 100.0 * (2.0 * cDamGoc + 99.0) / 2.0 * Char.myCharz().expForOneAdd)
                {
                    myVector3.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2((int)cDamGoc * 100), this, 9000, null));
                    myVector3.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * (int)cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                    myVector3.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(100 * (2 * (int)cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd), this, 9007, null));
                }
                myVector3.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + false));
                GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, isPet: false);
            }
            if (selected == 3)
            {
                if (Char.myCharz().cTiemNang < (50000 + Char.myCharz().cDefGoc * 1000))
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + NinjaUtil.getMoneys(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + NinjaUtil.getMoneys(50000 + Char.myCharz().cDefGoc * 1000));
                    return;
                }
                long number = 2 * ((int)cDefGoc + 5) / 2 * 100000;
                long number2 = 10 * (2 * ((int)cDefGoc + 5) + 9) / 2 * 100000;
                long number3 = 100 * (2 * ((int)cDefGoc + 5) + 99) / 2 * 100000;
                MyVector myVector4 = new MyVector(string.Empty);
                myVector4.addElement(new Command(mResources.increase_upper + "\n1 " + mResources.armor + "\n" + Res.formatNumber2(number), this, 9000, null));
                myVector4.addElement(new Command(mResources.increase_upper + "\n10 " + mResources.armor + "\n" + Res.formatNumber2(number2), this, 9006, null));
                myVector4.addElement(new Command(mResources.increase_upper + "\n100 " + mResources.armor + "\n" + Res.formatNumber2(number3), this, 9007, null));
                myVector4.addElement(new Command(ModFunc.strInCrease, ModFunc.GI(), 100, selected + "-" + false));
                GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addSkillDetail2(selected, isPet: false);
            }
            else if (selected == 4)
            {
                int num4 = Char.myCharz().cCriticalGoc;
                if (num4 > t_tiemnang.Length - 1)
                {
                    num4 = t_tiemnang.Length - 1;
                }
                long num5 = t_tiemnang[num4];
                if (Char.myCharz().cTiemNang < num5)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Res.formatNumber2(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + Res.formatNumber2(num5));
                    return;
                }
                GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + Res.formatNumber(num5) + mResources.use_potential_point_for2 + Char.myCharz().criticalFrom1000Tiemnang + mResources.for_crit, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
            }
            else if (selected == 5)
            {
                Service.gI().speacialSkill(0);
            }
            return;
        }
        int index = selected - 6;
        SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[index];
        Skill skill = Char.myCharz().getSkill(skillTemplate);
        Skill skill2 = null;
        MyVector myVector8 = new MyVector(string.Empty);
        if (skill != null)
        {
            if (skill.point == skillTemplate.maxPoint)
            {
                myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
                myVector8.addElement(new Command(mResources.CLOSE, 2));
            }
            else
            {
                skill2 = skillTemplate.skills[skill.point];
                myVector8.addElement(new Command(mResources.UPGRADE, this, 9002, skill2));
                myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
            }
        }
        else
        {
            skill2 = skillTemplate.skills[0];
            myVector8.addElement(new Command(mResources.learn, this, 9004, skill2));
        }
        GameCanvas.menu.startAt(myVector8, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
        addSkillDetail(skillTemplate, skill, skill2);
    }

    private void addLogMessage(InfoItem info)
    {
        string text = "|0|1|" + info.charInfo.cName;
        text += "\n";
        text += "\n--";
        text = text + "\n|5|" + Res.split(info.s, "|", 0)[2];
        cp = new ChatPopup();
        popUpDetailInit(cp, text);
        charInfo = info.charInfo;
        currItem = null;
    }

    private void addSkillDetail2(int type, bool isPet)
    {
        string empty = string.Empty;
        double num = 0.0;
        Char @char = (isPet ? Char.myPetz() : Char.myCharz());
        if (selected == 0)
        {
            num = @char.cHPGoc + 1000.0;
        }
        if (selected == 1)
        {
            num = @char.cMPGoc + 1000.0;
        }
        if (selected == 2)
        {
            num = @char.cDamGoc * @char.expForOneAdd;
        }
        if (selected == 3)
        {
            num = 500000 + @char.cDefGoc * 100000;
        }
        string text = empty;
        empty = text + "|5|2|" + mResources.USE + " " + num + " " + mResources.potential;
        if (type == 0)
        {
            empty = empty + "\n|5|2|" + mResources.to_gain_20hp;
        }
        if (type == 1)
        {
            empty = empty + "\n|5|2|" + mResources.to_gain_20mp;
        }
        if (type == 2)
        {
            empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
        }
        if (type == 3)
        {
            empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
        }
        currItem = null;
        partID = null;
        charInfo = null;
        idIcon = -1;
        cp = new ChatPopup();
        popUpDetailInit(cp, empty);
    }

    private void doFireMap()
    {
        if (imgMap != null)
        {
            imgMap.texture = null;
            imgMap = null;
        }
        TileMap.lastPlanetId = -1;
        mSystem.gcc();
        SmallImage.loadBigRMS();
        setTypeMain();
        cmx = (cmtoX = 0);
    }

    private void doFireZone()
    {
        if (selected != -1)
        {
            Res.outz("FIRE ZONE");
            isChangeZone = true;
            GameCanvas.panel.hide();
        }
    }

    public void updateRequest(int recieve, int maxCap)
    {
        cp.says[cp.says.Length - 1] = mResources.received + " " + recieve + "/" + maxCap;
    }

    private void doFireBox()
    {
        if (tabClickedInPointerDown)
        {
            tabClickedInPointerDown = false; // Reset flag
            return; // Return ngay để không mở hành trang
        }
        if (selected < 0)
        {
            return;
        }
        currItem = null;
        MyVector myVector = new MyVector();
        if (currentTabIndex == 0 && !Equals(GameCanvas.panel2))
        {
            if (selected == 0 && !ModFunc.isInventory)
            {
                setNewSelected(Char.myCharz().arrItemBox.Length, resetSelect: false);
                return;
            }
            sbyte b = (sbyte)GetInventorySelect_body(selected, newSelected);
            Item item = Char.myCharz().arrItemBox[b];
            if (item != null)
            {
                if (isBoxClan)
                {
                    myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                    myVector.addElement(new Command(mResources.USE, this, 2010, item));
                }
                else if (item.isTypeBody())
                {
                    myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                }
                else
                {
                    myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                }
                currItem = item;
            }
        }
        if (currentTabIndex == 1 || Equals(GameCanvas.panel2))
        {
            if (selected == 0 && !ModFunc.isInventory)
            {
                setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, resetSelect: true);
                return;
            }
            Item[] arrItemBody = Char.myCharz().arrItemBody;
            if (!GetInventorySelect_isbody(selected, newSelected, arrItemBody))
            {
                sbyte b2 = (sbyte)GetInventorySelect_bag(selected, newSelected, arrItemBody);
                Item item2 = Char.myCharz().arrItemBag[b2];
                if (item2 != null)
                {
                    myVector.addElement(new Command(mResources.move_to_chest, this, 1001, item2));
                    if (item2.isTypeBody())
                    {
                        myVector.addElement(new Command(mResources.USE, this, 2000, item2));
                    }
                    else
                    {
                        myVector.addElement(new Command(mResources.USE, this, 2001, item2));
                    }
                    currItem = item2;
                }
            }
            else
            {
                Item item3 = Char.myCharz().arrItemBody[GetInventorySelect_body(selected, newSelected)];
                if (item3 != null)
                {
                    myVector.addElement(new Command(mResources.move_to_chest2, this, 1002, item3));
                    currItem = item3;
                }
            }
        }
        if (currItem != null)
        {
            Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
            if (isBoxClan)
            {
                myVector.addElement(new Command(mResources.MOVEOUT, this, 2011, currItem));
            }
            GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addItemDetail(currItem);
        }
        else
        {
            cp = null;
        }
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
    }

    public void itemRequest(sbyte itemAction, string info, sbyte where, sbyte index)
    {
        GameCanvas.endDlg();
        ItemObject itemObject = new ItemObject();
        itemObject.type = itemAction;
        itemObject.id = index;
        itemObject.where = where;
        if (ModFunc.isFilterItem)
        {
            perform(2004, itemObject);
        }
        else
        {
            GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 2004, itemObject), new Command(mResources.NO, this, 4005, null));
        }
    }

    public void saleRequest(sbyte type, string info, short id)
    {
        ItemObject itemObject = new ItemObject();
        itemObject.type = type;
        itemObject.id = id;
        GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 3003, itemObject), new Command(mResources.NO, this, 4005, null));
    }

    public void perform(int idAction, object p)
    {
        switch (idAction)
        {
            case 8010:
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = this;
                }
                ModFunc.GI().MyChatTextField(chatTField, "Nhập số sao cần đập", "Chỉ được nhập số");
                break;
            case 8011:
                {
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = this;
                    }
                    string infoIntrinsic = (string)p;
                    ModFunc.GI().curSelectIntrinsic = infoIntrinsic;
                    ModFunc.GI().MyChatTextField(chatTField, "Nhập chỉ số mong muốn", "Chỉ nhập số");
                    break;
                }
        }
        if (idAction == 9999)
        {
            TopInfo topInfo = (TopInfo)p;
            Service.gI().sendThachDau(topInfo.pId);
        }
        if (idAction == 170391)
        {
            Rms.clearAll();
            if (mGraphics.zoomLevel > 1)
            {
                Rms.saveRMSInt("levelScreenKN", 1);
            }
            else
            {
                Rms.saveRMSInt("levelScreenKN", 0);
            }
            GameMidlet.instance.exit();
        }
        if (idAction == 6001)
        {
            Item item = (Item)p;
            item.isSelect = false;
            GameCanvas.panel.vItemCombine.removeElement(item);
            if (GameCanvas.panel.currentTabIndex == 0)
            {
                GameCanvas.panel.setTabCombine();
            }
            if (ModFunc.GI().isAutoPhaLe)
            {
                ModFunc.GI().itemPhale = null;
                ModFunc.GI().maxPhale = -1;
                ModFunc.GI().currPhale = -1;
            }
        }
        if (idAction == 6000)
        {
            Item item2 = (Item)p;
            for (int i = 0; i < GameCanvas.panel.vItemCombine.size(); i++)
            {
                if (((Item)GameCanvas.panel.vItemCombine.elementAt(i)).template.id == item2.template.id)
                {
                    GameCanvas.startOKDlg(mResources.already_has_item);
                    return;
                }
            }
            item2.isSelect = true;
            GameCanvas.panel.vItemCombine.addElement(item2);
            if (GameCanvas.panel.currentTabIndex == 0)
            {
                GameCanvas.panel.setTabCombine();
            }
            if (ModFunc.GI().isAutoPhaLe)
            {
                ModFunc.GI().itemPhale = item2;
            }
        }
        if (idAction == 7000)
        {
            if (isLock)
            {
                GameCanvas.startOKDlg(mResources.unlock_item_to_trade);
                return;
            }
            Item item4 = (Item)p;
            for (int j = 0; j < GameCanvas.panel.vMyGD.size(); j++)
            {
                if (((Item)GameCanvas.panel.vMyGD.elementAt(j)).indexUI == item4.indexUI)
                {
                    GameCanvas.startOKDlg(mResources.already_has_item);
                    return;
                }
            }
            if (item4.quantity > 1)
            {
                putQuantily();
                return;
            }
            item4.isSelect = true;
            Item item6 = new Item();
            item6.template = item4.template;
            item6.itemOption = item4.itemOption;
            item6.indexUI = item4.indexUI;
            GameCanvas.panel.vMyGD.addElement(item6);
            Service.gI().giaodich(2, -1, (sbyte)item6.indexUI, item6.quantity);
        }
        if (idAction == 7001)
        {
            Item item7 = (Item)p;
            item7.isSelect = false;
            GameCanvas.panel.vMyGD.removeElement(item7);
            if (GameCanvas.panel.currentTabIndex == 1)
            {
                GameCanvas.panel.setTabGiaoDich(isMe: true);
            }
            Service.gI().giaodich(4, -1, (sbyte)item7.indexUI, -1);
        }
        if (idAction == 7002)
        {
            isAccept = true;
            GameCanvas.endDlg();
            Service.gI().giaodich(7, -1, -1, -1);
            hide();
        }
        if (idAction == 8003)
        {
            InfoItem infoItem = (InfoItem)p;
            Service.gI().friend(1, infoItem.charInfo.charID);
            _ = type;
            _ = 8;
        }
        if (idAction == 8002)
        {
            InfoItem infoItem2 = (InfoItem)p;
            Service.gI().friend(2, infoItem2.charInfo.charID);
        }
        if (idAction == 8004)
        {
            int charID = (int)p;
            ModFunc.GI().TeleportToPlayer(charID);
        }
        if (idAction == 8001)
        {
            InfoItem infoItem4 = (InfoItem)p;
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            chatTField.strChat = mResources.chat_player;
            chatTField.tfChat.name = mResources.chat_with + " " + infoItem4.charInfo.cName;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.isFocus = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat(this, string.Empty);
            }
        }
        if (idAction == 1000)
        {
            Service.gI().getItem(BOX_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
        }
        if (idAction == 1001)
        {
            sbyte id = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
            Service.gI().getItem(BAG_BOX, id);
        }
        if (idAction == 1003)
        {
            hide();
        }
        if (idAction == 1002)
        {
            Service.gI().getItem(BODY_BOX, (sbyte)GetInventorySelect_body(selected, newSelected));
        }
        if (idAction == 2011)
        {
            Service.gI().useItem(1, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
        }
        if (idAction == 2010)
        {
            Service.gI().useItem(0, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
            Item item8 = (Item)p;
            if (item8 != null && (item8.template.id == 193 || item8.template.id == 194))
            {
                GameCanvas.panel.hide();
            }
        }
        if (idAction == 2000)
        {
            Item[] arrItemBody = Char.myCharz().arrItemBody;
            sbyte id2 = (sbyte)GetInventorySelect_bag(selected, newSelected, arrItemBody);
            if (isnewInventory)
            {
                id2 = (sbyte)currItem.indexUI;
            }
            Service.gI().getItem(BAG_BODY, id2);
        }
        if (idAction == 2001)
        {
            Res.outz("use item");
            Item item9 = (Item)p;
            bool inventorySelect_isbody = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
            sbyte b = 0;
            b = (inventorySelect_isbody ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
            if (isnewInventory)
            {
                b = (sbyte)currItem.indexUI;
                sbyte where = 0;
                if (newSelected != 0)
                {
                    where = 1;
                }
                Service.gI().useItem(0, where, b, -1);
            }
            else
            {
                Service.gI().useItem(0, (!inventorySelect_isbody) ? ((sbyte)1) : ((sbyte)0), b, -1);
            }
            if (item9.template.id == 193 || item9.template.id == 194)
            {
                GameCanvas.panel.hide();
            }
        }
        if (idAction == 2002)
        {
            if (isnewInventory)
            {
                Service.gI().getItem(BODY_BAG, (sbyte)sellectInventory);
            }
            else
            {
                Service.gI().getItem(BODY_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
            }
        }
        if (idAction == 2003)
        {
            Res.outz("remove item");
            bool inventorySelect_isbody2 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
            sbyte b2 = 0;
            b2 = (inventorySelect_isbody2 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
            Service.gI().useItem(1, (!inventorySelect_isbody2) ? ((sbyte)1) : ((sbyte)0), b2, -1);
        }
        if (idAction == 2004)
        {
            GameCanvas.endDlg();
            ItemObject itemObject = (ItemObject)p;
            sbyte where2 = (sbyte)itemObject.where;
            sbyte index = (sbyte)itemObject.id;
            Service.gI().useItem((sbyte)((itemObject.type != 0) ? 2 : 3), where2, index, -1);
        }
        if (idAction == 2005)
        {
            sbyte id3 = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
            Service.gI().getItem(BAG_PET, id3);
        }
        if (idAction == 2006)
        {
            sbyte id4 = (sbyte)selected;
            Service.gI().getItem(PET_BAG, id4);
        }
        if (idAction == 2007)
        {
            sbyte id5 = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
            Service.gI().getItem(BAG_PET2, id5);
        }
        if (idAction == 2008)
        {
            sbyte id6 = (sbyte)selected;
            Service.gI().getItem(PET2_BAG, id6);
        }
        if (idAction == 30001)
        {
            Res.outz("nhan do");
            Service.gI().buyItem(0, selected, 0);
        }
        if (idAction == 30002)
        {
            Res.outz("xoa do");
            Service.gI().buyItem(1, selected, 0);
        }
        if (idAction == 30003)
        {
            Res.outz("nhan tat");
            Service.gI().buyItem(2, selected, 0);
        }
        if (idAction == 3000)
        {
            Res.outz("mua do");
            Item item10 = (Item)p;
            Service.gI().buyItem(0, item10.template.id, 0);
        }
        if (idAction == 3001)
        {
            Item item11 = (Item)p;
            GameCanvas.msgdlg.pleasewait();
            Service.gI().buyItem(1, item11.template.id, 0);
        }
        if (idAction == 3002)
        {
            GameCanvas.endDlg();
            bool inventorySelect_isbody3 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
            sbyte b3 = 0;
            b3 = (inventorySelect_isbody3 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
            Service.gI().saleItem(0, (!inventorySelect_isbody3) ? ((sbyte)1) : ((sbyte)0), b3);
        }
        if (idAction == 3003)
        {
            GameCanvas.endDlg();
            ItemObject itemObject2 = (ItemObject)p;
            Service.gI().saleItem(1, (sbyte)itemObject2.type, (short)itemObject2.id);
        }
        if (idAction == 3004)
        {
            Item item12 = (Item)p;
            Service.gI().buyItem(3, item12.template.id, 0);
        }
        if (idAction == 3005)
        {
            Item item13 = (Item)p;
            Service.gI().buyItem(3, item13.template.id, 0);
        }
        if (idAction == 3006)
        {
            Item item14 = (Item)p;
            ModFunc.GI().AutoBuyItem(20, item14);
        }
        if (idAction == 4000)
        {
            Clan clan = (Clan)p;
            if (clan != null)
            {
                GameCanvas.endDlg();
                Service.gI().clanMessage(2, null, clan.ID);
            }
        }
        if (idAction == 4001)
        {
            Clan clan2 = (Clan)p;
            if (clan2 != null)
            {
                InfoDlg.showWait();
                clanReport = mResources.PLEASEWAIT;
                Service.gI().clanMember(clan2.ID);
            }
        }
        if (idAction == 4005)
        {
            GameCanvas.endDlg();
        }
        if (idAction == 4007)
        {
            GameCanvas.endDlg();
        }
        if (idAction == 4006)
        {
            ClanMessage clanMessage = (ClanMessage)p;
            Service.gI().clanDonate(clanMessage.id);
        }
        if (idAction == 5001)
        {
            Member member = (Member)p;
            Service.gI().clanRemote(member.ID, 0);
        }
        if (idAction == 5002)
        {
            Member member2 = (Member)p;
            Service.gI().clanRemote(member2.ID, 1);
        }
        if (idAction == 5003)
        {
            Member member3 = (Member)p;
            Service.gI().clanRemote(member3.ID, 2);
        }
        if (idAction == 5004)
        {
            Member member4 = (Member)p;
            Service.gI().clanRemote(member4.ID, -1);
        }
        if (idAction == 9000)
        {
            bool forPet = p != null && (bool)p;
            Service.gI().upPotential(forPet, selected, 1);
            GameCanvas.endDlg();
            InfoDlg.showWait();
        }
        if (idAction == 9006)
        {
            bool forPet2 = p != null && (bool)p;
            Service.gI().upPotential(forPet2, selected, 10);
            GameCanvas.endDlg();
            InfoDlg.showWait();
        }
        if (idAction == 9007)
        {
            bool forPet3 = p != null && (bool)p;
            Service.gI().upPotential(forPet3, selected, 100);
            GameCanvas.endDlg();
            InfoDlg.showWait();
        }
        if (idAction == 9002)
        {
            Skill skill = (Skill)p;
            if (skill.template.isSkillSpec())
            {
                GameCanvas.startOKDlg(mResources.updSkill);
            }
            else
            {
                GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill.powRequire + mResources.can_buy_from_Uron2 + skill.moreInfo + mResources.can_buy_from_Uron3);
            }
        }
        if (idAction == 9003)
        {
            if (GameCanvas.isTouch && !Main.isPC)
            {
                GameScr.gI().doSetOnScreenSkill((SkillTemplate)p);
            }
            else
            {
                GameScr.gI().doSetKeySkill((SkillTemplate)p);
            }
        }
        if (idAction == 9004)
        {
            Skill skill2 = (Skill)p;
            if (skill2.template.isSkillSpec())
            {
                GameCanvas.startOKDlg(mResources.learnSkill);
            }
            else
            {
                GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill2.powRequire + mResources.can_buy_from_Uron2 + skill2.moreInfo + mResources.can_buy_from_Uron3);
            }
        }
        if (idAction == 10000)
        {
            InfoItem infoItem5 = (InfoItem)p;
            Service.gI().enemy(1, infoItem5.charInfo.charID);
            GameCanvas.panel.hideNow();
        }
        if (idAction == 10001)
        {
            InfoItem infoItem6 = (InfoItem)p;
            Service.gI().enemy(2, infoItem6.charInfo.charID);
            InfoDlg.showWait();
        }
        _ = 10021;
        if (idAction == 10012)
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
            }
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            chatTField.tfChat.setText(string.Empty);
            if (currItem.quantity == 1)
            {
                chatTField.strChat = mResources.kiguiXuchat;
                chatTField.tfChat.name = mResources.input_money;
            }
            else
            {
                chatTField.strChat = mResources.input_quantity + " ";
                chatTField.tfChat.name = mResources.input_quantity;
            }
            chatTField.tfChat.setMaxTextLenght(9);
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            if (GameCanvas.isTouch)
            {
                chatTField.tfChat.doChangeToTextBox();
            }
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat(this, string.Empty);
            }
        }
        if (idAction == 10013)
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
            }
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            chatTField.tfChat.setText(string.Empty);
            if (currItem.quantity == 1)
            {
                chatTField.strChat = mResources.kiguiLuongchat;
                chatTField.tfChat.name = mResources.input_money;
            }
            else
            {
                chatTField.strChat = mResources.input_quantity + "  ";
                chatTField.tfChat.name = mResources.input_quantity;
            }
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            if (GameCanvas.isTouch)
            {
                chatTField.tfChat.doChangeToTextBox();
            }
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat(this, string.Empty);
            }
        }
        if (idAction == 10014)
        {
            Item item15 = (Item)p;
            Service.gI().kigui(1, item15.itemId, -1, -1, -1);
            InfoDlg.showWait();
        }
        if (idAction == 10015)
        {
            Item item16 = (Item)p;
            Service.gI().kigui(2, item16.itemId, -1, -1, -1);
            InfoDlg.showWait();
        }
        if (idAction == 10016)
        {
            Item item17 = (Item)p;
            Service.gI().kigui(3, item17.itemId, 0, item17.buyCoin, -1);
            InfoDlg.showWait();
        }
        if (idAction == 10017)
        {
            Item item18 = (Item)p;
            Service.gI().kigui(3, item18.itemId, 1, item18.buyGold, -1);
            InfoDlg.showWait();
        }
        if (idAction == 10018)
        {
            Item item19 = (Item)p;
            Service.gI().kigui(5, item19.itemId, -1, -1, -1);
            InfoDlg.showWait();
        }
        if (idAction == 10019)
        {
            Session_ME.gI().close();
            Rms.saveRMSString("acc", string.Empty);
            Rms.saveRMSString("pass", string.Empty);
            GameCanvas.acc = string.Empty;
            GameCanvas.pass = string.Empty;
            GameCanvas.loginScr.tfPass.setText(string.Empty);
            GameCanvas.loginScr.tfUser.setText(string.Empty);
            GameCanvas.loginScr.isLogin2 = false;
            GameCanvas.loginScr.switchToMe();
            GameCanvas.endDlg();
            hide();
        }
        if (idAction == 10020)
        {
            GameCanvas.endDlg();
        }
        if (idAction == 10030)
        {
            Service.gI().getFlag(1, (sbyte)selected);
            GameCanvas.panel.hideNow();
        }
        if (idAction == 10031)
        {
            Session_ME.gI().close();
        }
        if (idAction == 11000)
        {
            Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, 1);
            GameCanvas.endDlg();
        }
        if (idAction == 11001)
        {
            Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, currItem.quantilyToBuy);
            GameCanvas.endDlg();
        }
        if (idAction == 11002)
        {
            chatTField.isShow = false;
            GameCanvas.endDlg();
        }
    }

    public void onChatFromMe(string text, string to)
    {
        if (chatTField.strChat == "Nhập chỉ số mong muốn")
        {
            if (chatTField.tfChat.getText() != string.Empty)
            {
                if (int.TryParse(text, out var param))
                {
                    ModFunc.GI().SetAutoIntrinsic(param);
                    isShow = false;
                }
                else
                {
                    GameScr.info1.addInfo("Chỉ số đã nhập không hợp lệ", 0);
                }
                chatTField.isShow = false;
            }
        }
        else if (chatTField.strChat == "Nhập số sao cần đập" && chatTField.tfChat.getText() != string.Empty)
        {
            if (int.TryParse(text, out var maxPhale) && maxPhale > 0)
            {
                GameScr.info1.addInfo("Đập " + ModFunc.GI().itemPhale.template.name + " đến " + maxPhale + " sao", 0);
                ModFunc.GI().maxPhale = maxPhale;
            }
            else
            {
                GameScr.info1.addInfo("Số sao đã nhập không đúng", 0);
            }
            chatTField.isShow = false;
        }
        if (chatTField.tfChat.getText() == null || chatTField.tfChat.getText().Equals(string.Empty) || text.Equals(string.Empty) || text == null)
        {
            chatTField.isShow = false;
            return;
        }
        if (chatTField.strChat.Equals(mResources.input_clan_name))
        {
            InfoDlg.showWait();
            chatTField.isShow = false;
            Service.gI().searchClan(text);
            return;
        }
        if (chatTField.strChat.Equals(mResources.chat_clan))
        {
            InfoDlg.showWait();
            chatTField.isShow = false;
            Service.gI().clanMessage(0, text, -1);
            return;
        }
        if (chatTField.strChat.Equals(mResources.input_clan_name_to_create))
        {
            if (chatTField.tfChat.getText() == string.Empty)
            {
                GameScr.info1.addInfo(mResources.clan_name_blank, 0);
                return;
            }
            if (tabIcon == null)
            {
                tabIcon = new TabClanIcon();
            }
            tabIcon.text = chatTField.tfChat.getText();
            tabIcon.show(isGetName: false);
            chatTField.isShow = false;
            return;
        }
        if (chatTField.strChat.Equals(mResources.input_clan_slogan))
        {
            if (chatTField.tfChat.getText() == string.Empty)
            {
                GameScr.info1.addInfo(mResources.clan_slogan_blank, 0);
                return;
            }
            Service.gI().getClan(4, (sbyte)Char.myCharz().clan.imgID, chatTField.tfChat.getText());
            chatTField.isShow = false;
            return;
        }
        if (chatTField.strChat.Equals(mResources.input_Inventory_Pass))
        {
            try
            {
                int lockInventory = int.Parse(chatTField.tfChat.getText());
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                hide();
                if (chatTField.tfChat.getText().Length != 6 || chatTField.tfChat.getText().Equals(string.Empty))
                {
                    GameCanvas.startOKDlg(mResources.input_Inventory_Pass_wrong);
                }
                else
                {
                    Service.gI().setLockInventory(lockInventory);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    hide();
                }
                return;
            }
            catch (Exception)
            {
                GameCanvas.startOKDlg(mResources.ALERT_PRIVATE_PASS_2);
                return;
            }
        }
        if (chatTField.strChat.Equals(mResources.world_channel_5_luong))
        {
            if (!chatTField.tfChat.getText().Equals(string.Empty))
            {
                Service.gI().chatGlobal(chatTField.tfChat.getText());
                chatTField.isShow = false;
            }
        }
        else if (chatTField.strChat.Equals(mResources.chat_player))
        {
            chatTField.isShow = false;
            InfoItem infoItem = null;
            if (type == 8)
            {
                infoItem = (InfoItem)logChat.elementAt(currInfoItem);
            }
            else if (type == 11)
            {
                infoItem = (InfoItem)vFriend.elementAt(currInfoItem);
            }
            if (infoItem.charInfo.charID != Char.myCharz().charID)
            {
                Service.gI().chatPlayer(text, infoItem.charInfo.charID);
            }
        }
        else if (chatTField.strChat.Equals(mResources.input_quantity_to_trade))
        {
            int num = 0;
            try
            {
                num = int.Parse(chatTField.tfChat.getText());
            }
            catch (Exception)
            {
                GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                return;
            }
            if (num <= 0 || num > currItem.quantity)
            {
                GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                return;
            }
            currItem.isSelect = true;
            Item item = new Item
            {
                template = currItem.template,
                quantity = num,
                indexUI = currItem.indexUI,
                itemOption = currItem.itemOption
            };
            GameCanvas.panel.vMyGD.addElement(item);
            Service.gI().giaodich(2, -1, (sbyte)item.indexUI, item.quantity);
            chatTField.isShow = false;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        }
        else if (chatTField.strChat == mResources.input_money_to_trade)
        {
            int num2;
            try
            {
                num2 = int.Parse(chatTField.tfChat.getText());
            }
            catch (Exception)
            {
                GameCanvas.startOKDlg(mResources.input_money_wrong);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                return;
            }
            if (num2 > Char.myCharz().xu)
            {
                GameCanvas.startOKDlg(mResources.not_enough_money);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            }
            else
            {
                moneyGD = num2;
                Service.gI().giaodich(2, -1, -1, num2);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            }
        }
        else if (chatTField.strChat.Equals(mResources.kiguiXuchat))
        {
            Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), 1);
            chatTField.isShow = false;
        }
        else if (chatTField.strChat.Equals(mResources.kiguiXuchat + " "))
        {
            Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), currItem.quantilyToBuy);
            chatTField.isShow = false;
        }
        else if (chatTField.strChat.Equals(mResources.kiguiLuongchat))
        {
            doNotiRuby(0);
            chatTField.isShow = false;
        }
        else if (chatTField.strChat.Equals(mResources.kiguiLuongchat + "  "))
        {
            doNotiRuby(1);
            chatTField.isShow = false;
        }
        else if (chatTField.strChat.Equals(mResources.input_quantity + " "))
        {
            currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
            if (currItem.quantilyToBuy > currItem.quantity)
            {
                GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                return;
            }
            isKiguiXu = true;
            chatTField.isShow = false;
        }
        else if (chatTField.strChat.Equals(mResources.input_quantity + "  "))
        {
            currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
            if (currItem.quantilyToBuy > currItem.quantity)
            {
                GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                return;
            }
            isKiguiLuong = true;
            chatTField.isShow = false;
        }
    }

    public void onCancelChat()
    {
        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
    }

    public void setCombineEff(int type)
    {
        typeCombine = type;
        rS = 90;
        if (typeCombine == 0)
        {
            iDotS = 5;
            angleS = (angleO = 90);
            time = 2;
            for (int i = 0; i < vItemCombine.size(); i++)
            {
                Item item = (Item)vItemCombine.elementAt(i);
                if (item != null)
                {
                    if (item.template.type == 14)
                    {
                        iconID2 = item.template.iconID;
                    }
                    else
                    {
                        iconID1 = item.template.iconID;
                    }
                }
            }
        }
        else if (typeCombine == 1)
        {
            iDotS = 2;
            angleS = (angleO = 0);
            time = 1;
            for (int j = 0; j < vItemCombine.size(); j++)
            {
                Item item2 = (Item)vItemCombine.elementAt(j);
                if (item2 != null)
                {
                    if (j == 0)
                    {
                        iconID1 = item2.template.iconID;
                    }
                    else
                    {
                        iconID2 = item2.template.iconID;
                    }
                }
            }
        }
        else if (typeCombine == 2)
        {
            iDotS = 7;
            angleS = (angleO = 25);
            time = 1;
            for (int k = 0; k < vItemCombine.size(); k++)
            {
                Item item3 = (Item)vItemCombine.elementAt(k);
                if (item3 != null)
                {
                    iconID1 = item3.template.iconID;
                }
            }
        }
        else if (typeCombine == 3)
        {
            xS = GameCanvas.hw;
            yS = GameCanvas.hh;
            iDotS = 1;
            angleS = (angleO = 1);
            time = 4;
            for (int l = 0; l < vItemCombine.size(); l++)
            {
                Item item4 = (Item)vItemCombine.elementAt(l);
                if (item4 != null)
                {
                    iconID1 = item4.template.iconID;
                }
            }
        }
        else if (typeCombine == 4)
        {
            iDotS = vItemCombine.size();
            iconID = new short[iDotS];
            angleS = (angleO = 25);
            time = 1;
            for (int m = 0; m < vItemCombine.size(); m++)
            {
                Item item5 = (Item)vItemCombine.elementAt(m);
                if (item5 != null)
                {
                    iconID[m] = item5.template.iconID;
                }
            }
        }
        speed = 1;
        isSpeedCombine = true;
        isDoneCombine = false;
        isCompleteEffCombine = false;
        iAngleS = 360 / iDotS;
        xArgS = new int[iDotS];
        yArgS = new int[iDotS];
        xDotS = new int[iDotS];
        yDotS = new int[iDotS];
        setDotStar();
        isPaintCombine = true;
        countUpdate = 10;
        countR = 30;
        countWait = 10;
        addTextCombineNPC(idNPC, mResources.combineSpell);
    }

    private void updateCombineEff()
    {
        countUpdate--;
        if (countUpdate < 0)
        {
            countUpdate = 0;
        }
        countR--;
        if (countR < 0)
        {
            countR = 0;
        }
        if (countUpdate != 0)
        {
            return;
        }
        if (!isCompleteEffCombine)
        {
            if (time > 0)
            {
                if (combineSuccess != -1)
                {
                    if (typeCombine == 3)
                    {
                        if (GameCanvas.gameTick % 10 == 0)
                        {
                            EffecMn.addEff(new Effect(21, xS - 10, yS + 25, 4, 1, 1));
                            time--;
                        }
                    }
                    else
                    {
                        if (GameCanvas.gameTick % 2 == 0)
                        {
                            if (isSpeedCombine)
                            {
                                if (speed < 40)
                                {
                                    speed += 2;
                                }
                            }
                            else if (speed > 10)
                            {
                                speed -= 2;
                            }
                        }
                        if (countR == 0)
                        {
                            if (isSpeedCombine)
                            {
                                if (rS > 0)
                                {
                                    rS -= 5;
                                }
                                else if (GameCanvas.gameTick % 10 == 0)
                                {
                                    isSpeedCombine = false;
                                    time--;
                                    countR = 5;
                                    countWait = 10;
                                }
                            }
                            else if (rS < 90)
                            {
                                rS += 5;
                            }
                            else if (GameCanvas.gameTick % 10 == 0)
                            {
                                isSpeedCombine = true;
                                countR = 10;
                            }
                        }
                        angleS = angleO;
                        angleS -= speed;
                        if (angleS >= 360)
                        {
                            angleS -= 360;
                        }
                        if (angleS < 0)
                        {
                            angleS = 360 + angleS;
                        }
                        angleO = angleS;
                        setDotStar();
                    }
                }
            }
            else if (GameCanvas.gameTick % 20 == 0)
            {
                isCompleteEffCombine = true;
            }
            if (GameCanvas.gameTick % 20 == 0)
            {
                if (typeCombine != 3)
                {
                    EffectPanel.addServerEffect(132, xS, yS, 2);
                }
                EffectPanel.addServerEffect(114, xS, yS + 20, 2);
            }
        }
        else
        {
            if (!isCompleteEffCombine)
            {
                return;
            }
            if (combineSuccess == 1)
            {
                if (countWait == 10)
                {
                    EffecMn.addEff(new Effect(22, xS - 3, yS + 25, 4, 1, 1));
                }
                countWait--;
                if (countWait < 0)
                {
                    countWait = 0;
                }
                if (rS < 300)
                {
                    rS = Res.abs(rS + 10);
                    if (rS == 20)
                    {
                        addTextCombineNPC(idNPC, mResources.combineFail);
                    }
                }
                else if (GameCanvas.gameTick % 20 == 0)
                {
                    if (GameCanvas.w > 2 * WIDTH_PANEL)
                    {
                        GameCanvas.panel2 = new Panel();
                        GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                        GameCanvas.panel2.setTypeBodyOnly();
                        GameCanvas.panel2.show();
                    }
                    combineSuccess = -1;
                    isDoneCombine = true;
                    if (typeCombine == 4)
                    {
                        GameCanvas.panel.hideNow();
                    }
                }
                setDotStar();
            }
            else
            {
                if (combineSuccess != 0)
                {
                    return;
                }
                if (countWait == 10)
                {
                    if (typeCombine == 2)
                    {
                        EffecMn.addEff(new Effect(20, xS - 3, yS + 15, 4, 2, 1));
                    }
                    else
                    {
                        EffecMn.addEff(new Effect(21, xS - 10, yS + 25, 4, 1, 1));
                    }
                    addTextCombineNPC(idNPC, mResources.combineSuccess);
                    isPaintCombine = false;
                }
                if (isPaintCombine)
                {
                    return;
                }
                countWait--;
                if (countWait < -50)
                {
                    countWait = -50;
                    if (typeCombine < 3 && GameCanvas.w > 2 * WIDTH_PANEL)
                    {
                        GameCanvas.panel2 = new Panel();
                        GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                        GameCanvas.panel2.setTypeBodyOnly();
                        GameCanvas.panel2.show();
                    }
                    combineSuccess = -1;
                    isDoneCombine = true;
                    if (typeCombine == 4)
                    {
                        GameCanvas.panel.hideNow();
                    }
                }
            }
        }
    }

    public void paintCombineEff(mGraphics g)
    {
        GameScr.gI().paintBlackSky(g);
        paintCombineNPC(g);
        if (GameCanvas.gameTick % 4 == 0)
        {
            g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
        }
        if (typeCombine == 0)
        {
            for (int i = 0; i < yArgS.Length; i++)
            {
                SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                if (isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID2, xDotS[i], yDotS[i], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
        }
        else if (typeCombine == 1)
        {
            if (!isPaintCombine)
            {
                SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                return;
            }
            for (int j = 0; j < yArgS.Length; j++)
            {
                SmallImage.drawSmallImage(g, iconID1, xDotS[0], yDotS[0], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                SmallImage.drawSmallImage(g, iconID2, xDotS[1], yDotS[1], 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
        }
        else if (typeCombine == 2)
        {
            if (!isPaintCombine)
            {
                SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                return;
            }
            for (int k = 0; k < yArgS.Length; k++)
            {
                SmallImage.drawSmallImage(g, iconID1, xDotS[k], yDotS[k], 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
        }
        else if (typeCombine == 3)
        {
            if (!isPaintCombine)
            {
                SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            else
            {
                SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
        }
        else
        {
            if (typeCombine != 4)
            {
                return;
            }
            if (!isPaintCombine)
            {
                if (iconID3 != -1)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else
            {
                for (int l = 0; l < iconID.Length; l++)
                {
                    SmallImage.drawSmallImage(g, iconID[l], xDotS[l], yDotS[l], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
        }
    }

    private void setDotStar()
    {
        for (int i = 0; i < yArgS.Length; i++)
        {
            if (angleS >= 360)
            {
                angleS -= 360;
            }
            if (angleS < 0)
            {
                angleS = 360 + angleS;
            }
            yArgS[i] = Res.abs(rS * Res.sin(angleS) / 1024);
            xArgS[i] = Res.abs(rS * Res.cos(angleS) / 1024);
            if (angleS < 90)
            {
                xDotS[i] = xS + xArgS[i];
                yDotS[i] = yS - yArgS[i];
            }
            else if (angleS >= 90 && angleS < 180)
            {
                xDotS[i] = xS - xArgS[i];
                yDotS[i] = yS - yArgS[i];
            }
            else if (angleS >= 180 && angleS < 270)
            {
                xDotS[i] = xS - xArgS[i];
                yDotS[i] = yS + yArgS[i];
            }
            else
            {
                xDotS[i] = xS + xArgS[i];
                yDotS[i] = yS + yArgS[i];
            }
            angleS -= iAngleS;
        }
    }

    public void paintCombineNPC(mGraphics g)
    {
        g.translate(-GameScr.cmx, -GameScr.cmy);
        if (typeCombine < 3)
        {
            for (int i = 0; i < GameScr.vNpc.size(); i++)
            {
                Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                if (npc.template.npcTemplateId == idNPC)
                {
                    npc.paint(g);
                    if (npc.chatInfo != null)
                    {
                        npc.chatInfo.paint(g, npc.cx, npc.cy - npc.ch - GameCanvas.transY, npc.cdir);
                    }
                }
            }
        }
        GameCanvas.resetTrans(g);
        if (GameCanvas.gameTick % 4 == 0)
        {
            g.drawImage(ItemMap.imageFlare, xS - 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            g.drawImage(ItemMap.imageFlare, xS + 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
        }
        for (int j = 0; j < Effect2.vEffect3.size(); j++)
        {
            ((Effect2)Effect2.vEffect3.elementAt(j)).paint(g);
        }
    }

    public void addTextCombineNPC(int idNPC, string text)
    {
        if (typeCombine >= 3)
        {
            return;
        }
        for (int i = 0; i < GameScr.vNpc.size(); i++)
        {
            Npc npc = (Npc)GameScr.vNpc.elementAt(i);
            if (npc.template.npcTemplateId == idNPC)
            {
                npc.addInfo(text);
            }
        }
    }

    public void setTypeOption()
    {
        type = 19;
        setType(0);
        setTabOption();
        cmx = (cmtoX = 0);
    }

    public void SetTypeModFunc()
    {
        type = 26;
        tabName[26] = boxMod;
        setType(0);
        SetTabModFunc();
        cmx = (cmtoX = 0);
    }

    private void SetTabModFunc()
    {
        SoundMn.gI().GetStrModFunc();
        currentListLength = strModFunc.Length;
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    private void setTabOption()
    {
        SoundMn.gI().getStrOption();
        currentListLength = strCauhinh.Length;
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    private void paintOption(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < strCauhinh.Length; i++)
        {
            int x = xScroll;
            int num = yScroll + i * ITEM_HEIGHT;
            int num2 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(x, num, num2, h);
                mFont.tahoma_7b_dark.drawString(g, strCauhinh[i], xScroll + 10, num + 6, mFont.LEFT);
            }
        }
        paintScrollArrow(g);
    }

    private void PaintModFunc(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < strModFunc.Length; i++)
        {
            int x = xScroll;
            int num = yScroll + i * ITEM_HEIGHT;
            int num2 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(x, num, num2, h);
                mFont.tahoma_7b_dark.drawString(g, strModFunc[i], xScroll + 10, num + 6, mFont.LEFT);
            }
        }
        paintScrollArrow(g);
    }

    private void doFireOption()
    {
        if (selected >= 0)
        {
            switch (selected)
            {
                case 0:
                    SoundMn.gI().AuraToolOption();
                    break;
                case 1:
                    SoundMn.gI().AuraToolOption2();
                    break;
                case 2:
                    SoundMn.gI().soundToolOption();
                    break;
                case 3:
                    SoundMn.gI().CaseSizeScr();
                    break;
                case 4:
                    SoundMn.gI().CaseAnalog();
                    break;
                case 5:
                    ModFunc.changeStatusEditButton();
                    break;
                case 6:
                    setTypeHotkeySettings();
                    break;
            }
        }
    }

    private void DoFireModFunc()
    {
        if (selected < 0)
        {
            return;
        }
        int currentTab = currentTabIndex;
        int selectedInTab = selected;
        switch (currentTab)
        {
            case 0:
                switch (selectedInTab)
                {
                    case 0:
                        ModFunc.GI().isHighFps = !ModFunc.GI().isHighFps;
                        ModFunc.GI().ChangeFPSTarget();
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().isHighFps ? "bật" : "tắt") + " FPS cao", 0);
                        break;
                    case 1:
                        ModFunc.GI().isUpdateZones = !ModFunc.GI().isUpdateZones;
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().isUpdateZones ? "bật" : "tắt") + " cập nhật khu vực", 0);
                        break;
                    case 2:
                        ModFunc.GI().showCharsInMap = !ModFunc.GI().showCharsInMap;
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().showCharsInMap ? "bật" : "tắt") + " hiển thị người chơi trong bản đồ", 0);
                        break;
                    case 3:
                        ModFunc.GI().showInfoMe = !ModFunc.GI().showInfoMe;
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().showInfoMe ? "bật" : "tắt") + " hiển thị thông tin bản thân", 0);
                        break;
                    case 4:
                        ModFunc.GI().isShowButton = !ModFunc.GI().isShowButton;
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().isShowButton ? "bật" : "tắt") + " hiển thị nút", 0);
                        break;
                }
                break;
            case 1:
                switch (selectedInTab)
                {
                    case 0:
                        ModFunc.GI().isAutoPhaLe = !ModFunc.GI().isAutoPhaLe;
                        if (ModFunc.GI().isAutoPhaLe)
                        {
                            new Thread(ModFunc.GI().AutoPhaLe).Start();
                        }
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().isAutoPhaLe ? "bật" : "tắt") + " tự động pha lê", 0);
                        break;
                    case 1:
                        ModFunc.GI().autoWakeUp = !ModFunc.GI().autoWakeUp;
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().autoWakeUp ? "bật" : "tắt") + " Auto Hồi Sinh", 0);
                        break;
                    case 2:
                        if (ModFunc.isAutoLogin)
                        {
                            ModFunc.isAutoLogin = false;
                            ModFunc.autoLogin = null;
                        }
                        else
                        {
                            ModFunc.isAutoLogin = true;
                            ModFunc.autoLogin = new AutoLogin
                            {
                                accAutoLogin = GameCanvas.loginScr.tfUser.getText()
                            };
                        }
                        GameScr.info1.addInfo("Đã " + (ModFunc.isAutoLogin ? "bật" : "tắt") + " tự động đăng nhập", 0);
                        break;
                }
                break;
            case 2:
                switch (selectedInTab)
                {
                    
                    case 0:
                        ModFunc.changeStatusAnPlayer();
                        GameScr.info1.addInfo("Đã " + (ModFunc.AnPlayer ? "bật" : "tắt") + " ẩn người chơi", 0);
                        break;
                    case 1:
                        ModFunc.changeStatusShowID();
                        GameScr.info1.addInfo("Đã " + (ModFunc.isShowID ? "bật" : "tắt") + " hiển thị ID", 0);
                        break;
                    case 2:
                        ModFunc.chanegStatusInventory();
                        GameScr.info1.addInfo("Đã " + (ModFunc.isInventory ? "bật" : "tắt") + " hiển thị túi đồ", 0);
                        break;
                    case 3:
                        ModFunc.changeStatusEffectInven();
                        GameScr.info1.addInfo("Đã " + (ModFunc.isEffectInven ? "bật" : "tắt") + " hiệu ứng túi đồ", 0);
                        break;
                }
                break;
            case 3:
                switch (selectedInTab)
                {
                    case 0:
                        ModFunc.GI().isIntroOff = !ModFunc.GI().isIntroOff;
                        Rms.saveRMSInt("IntroOff", ModFunc.GI().isIntroOff ? 1 : 0);
                        GameScr.info1.addInfo("Đã " + (ModFunc.GI().isIntroOff ? "bật" : "tắt") + " intro", 0);
                        break;
                    case 1:
                        ModFunc.changeStatusBackground();
                        GameScr.info1.addInfo("Đã " + (ModFunc.GiamDungLuong ? "bật" : "tắt") + " giảm dung lượng", 0);
                        break;
                    case 2:
                        ModFunc.isFilterItem = !ModFunc.isFilterItem;
                        GameScr.info1.addInfo("Đã " + (ModFunc.isFilterItem ? "bật" : "tắt") + " chế độ lọc đồ", 0);
                        break;
                }
                break;
        }
        SoundMn.gI().GetStrModFunc();
    }

    public void setTypeAccount()
    {
        type = 20;
        setType(0);
        setTabAccount();
        cmx = (cmtoX = 0);
    }

    private void setTabAccount()
    {
        if (Main.IphoneVersionApp)
        {
            strAccount = new string[4]
            {
                mResources.inventory_Pass,
                mResources.friend,
                mResources.enemy,
                mResources.msg
            };
            if (GameScr.canAutoPlay)
            {
                strAccount = new string[5]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg,
                    mResources.autoFunction
                };
            }
        }
        else
        {
            strAccount = new string[5]
            {
                mResources.inventory_Pass,
                mResources.friend,
                mResources.enemy,
                mResources.msg,
                mResources.charger
            };
            if (GameScr.canAutoPlay)
            {
                strAccount = new string[6]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg,
                    mResources.charger,
                    mResources.autoFunction
                };
            }
            if ((mSystem.clientType == 2 || mSystem.clientType == 7) && mResources.language != 2)
            {
                strAccount = new string[5]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg,
                    mResources.charger
                };
                if (GameScr.canAutoPlay)
                {
                    strAccount = new string[6]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.charger,
                        mResources.autoFunction
                    };
                }
            }
        }
        currentListLength = strAccount.Length;
        ITEM_HEIGHT = 24;
        selected = (GameCanvas.isTouch ? (-1) : 0);
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
    }

    private void paintAccount(mGraphics g)
    {
        g.setClip(xScroll, yScroll, wScroll, hScroll);
        g.translate(0, -cmy);
        for (int i = 0; i < strAccount.Length; i++)
        {
            int x = xScroll;
            int num = yScroll + i * ITEM_HEIGHT;
            int num2 = wScroll - 1;
            int h = ITEM_HEIGHT - 1;
            if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
            {
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(x, num, num2, h);
                mFont.tahoma_7b_dark.drawString(g, strAccount[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
            }
        }
        paintScrollArrow(g);
    }

    private void doFireAccount()
    {
        if (selected < 0)
        {
            return;
        }
        switch (selected)
        {
            case 0:
                GameCanvas.endDlg();
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = GameCanvas.panel;
                }
                chatTField.tfChat.setText(string.Empty);
                chatTField.strChat = mResources.input_Inventory_Pass;
                chatTField.tfChat.name = mResources.input_Inventory_Pass;
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.isFocus = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                if (GameCanvas.isTouch)
                {
                    chatTField.tfChat.doChangeToTextBox();
                }
                if (!Main.isPC)
                {
                    chatTField.startChat(this, string.Empty);
                }
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                break;
            case 1:
                Service.gI().friend(0, -1);
                InfoDlg.showWait();
                break;
            case 2:
                Service.gI().enemy(0, -1);
                InfoDlg.showWait();
                break;
            case 3:
                setTypeMessage();
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = GameCanvas.panel;
                }
                break;
            case 4:
                if (mResources.language == 2)
                {
                    string url = "http://dragonball.indonaga.com/coda/?username=" + GameCanvas.loginScr.tfUser.getText();
                    hideNow();
                    try
                    {
                        GameMidlet.instance.platformRequest(url);
                        break;
                    }
                    catch (Exception ex)
                    {
                        ex.StackTrace.ToString();
                        break;
                    }
                }
                hideNow();
                if (Char.myCharz().taskMaint.taskId <= 10)
                {
                    GameCanvas.startOKDlg(mResources.finishBomong);
                }
                else
                {
                    MoneyCharge.gI().switchToMe();
                }
                break;
            case 5:
                setTypeAuto();
                break;
        }
    }

    private void updateKeyOption()
    {
        updateKeyScrollView();
    }

    public void setTypeSpeacialSkill()
    {
        type = 25;
        setType(0);
        setTabSpeacialSkill();
        currentTabIndex = 0;
    }

    private void setTabSpeacialSkill()
    {
        ITEM_HEIGHT = 24;
        currentListLength = Char.myCharz().infoSpeacialSkill[currentTabIndex].Length;
        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        if (cmyLim < 0)
        {
            cmyLim = 0;
        }
        cmy = (cmtoY = cmyLast[currentTabIndex]);
        if (cmy < 0)
        {
            cmy = (cmtoY = 0);
        }
        if (cmy > cmyLim)
        {
            cmy = (cmtoY = cmyLim);
        }
        selected = (GameCanvas.isTouch ? (-1) : 0);
    }

    public bool isTypeShop()
    {
        if (type == 1)
        {
            return true;
        }
        return false;
    }

    private void doNotiRuby(int type)
    {
        try
        {
            currItem.buyRuby = int.Parse(chatTField.tfChat.getText());
        }
        catch (Exception)
        {
            GameCanvas.startOKDlg(mResources.input_money_wrong);
            chatTField.isShow = false;
            return;
        }
        Command cmdYes = new Command(mResources.YES, this, (type != 0) ? 11001 : 11000, null);
        Command cmdNo = new Command(mResources.NO, this, 11002, null);
        GameCanvas.startYesNoDlg(mResources.notiRuby, cmdYes, cmdNo);
    }

    public static void paintUpgradeEffect(int x, int y, int wItem, int hItem, int nline, int cl, mGraphics g)
    {
        try
        {
            int num2 = ((wItem << 1) + (hItem << 1)) / nline;
            nsize = sizeUpgradeEff.Length;
            if (nline > 4)
            {
                nsize = 2;
            }
            for (int i = 0; i < nline; i++)
            {
                for (int j = 0; j < nsize; j++)
                {
                    int wSize = ((sizeUpgradeEff[j] <= 1) ? 1 : ((sizeUpgradeEff[j] >> 1) + 1));
                    int x2 = x + upgradeEffectX(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                    int y2 = y + upgradeEffectY(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                    g.setColor(colorUpgradeEffect[cl][j]);
                    g.fillRect(x2, y2, sizeUpgradeEff[j], sizeUpgradeEff[j]);
                }
            }
        }
        catch (Exception)
        {
        }
    }

    private static int upgradeEffectX(int dk, int tick, int wItem, int hitem, int wSize)
    {
        int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
        if (0 <= num && num < wItem)
        {
            return num % wItem;
        }
        if (wItem <= num && num < wItem + hitem)
        {
            return wItem - wSize;
        }
        if (wItem + hitem <= num && num < (wItem << 1) + hitem)
        {
            return wItem - (num - hitem) % wItem - wSize;
        }
        return 0;
    }

    private static int upgradeEffectY(int dk, int tick, int wItem, int hitem, int wSize)
    {
        int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
        if (0 <= num && num < wItem)
        {
            return 0;
        }
        if (wItem <= num && num < wItem + hitem)
        {
            return num % wItem;
        }
        if (wItem + hitem <= num && num < (wItem << 1) + hitem)
        {
            return hitem - wSize;
        }
        return hitem - (num - (wItem << 1)) % hitem - wSize;
    }

    public static int GetColor_ItemBg(int id)
    {
        return id switch
        {
            4 => 1269146,
            1 => 2786816,
            5 => 13279744,
            3 => 12537346,
            2 => 7078041,
            6 => 11599872,
            _ => -1,
        };
    }

    public static sbyte GetColor_Item_Upgrade(int lv)
    {
        if (lv < 0)
        {
            return 0;
        }
        switch (lv)
        {
            case 0:
            case 1:
                return 4;
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
                return 2;
            case 6:
            case 7:
                return 3;
            case 8:
                return 5;
            case 9:
                return 6;
            case 10:
                return 0;
            default:
                return 0;
        }
    }

    public static mFont GetFont(int color)
    {
        mFont result = mFont.tahoma_7;
        switch (color)
        {
            case -1:
                result = mFont.tahoma_7;
                break;
            case 0:
                result = mFont.tahoma_7b_dark;
                break;
            case 1:
                result = mFont.tahoma_7b_green;
                break;
            case 2:
                result = mFont.tahoma_7b_blue;
                break;
            case 3:
                result = mFont.tahoma_7b_blue;
                break;
            case 4:
                result = mFont.tahoma_7b_blue;
                break;
            case 5:
                result = mFont.tahoma_7b_blue;
                break;
            case 7:
                result = mFont.tahoma_7b_red;
                break;
            case 8:
                result = mFont.tahoma_7b_yellow;
                break;
        }
        return result;
    }

    public void paintOptItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
    {
        int num = 12;
        int num2 = 20;
        int num3 = 15;
        switch (idOpt)
        {
            case 102:
                {
                    int maxStar = 8;
                    if (imgo_17 != null)
                    {
                        if (param > 0)
                        {
                            g.drawImage(imgo_17, x - num2 + 1 + w - imgo_17.getWidth(), y - num2 + h - imgo_17.getHeight());
                        }
                        else if (param > maxStar - 3)
                        {
                            g.drawImage(imgo_18, x - num2 + 1 + w - imgo_18.getWidth(), y - num2 + h - imgo_18.getHeight());
                        }
                        mFont.tahoma_7b_white.drawString(g, string.Empty + param, x - num3 + w - imgo_17.getWidth() + 1, y - 20 + h - imgo_17.getHeight(), 1);
                    }
                    else
                    {
                        imgo_17 = mSystem.loadImage("/mainImage/star.png");
                        imgo_18 = mSystem.loadImage("/mainImage/star8.png");
                    }
                    break;
                }
            case 34:
                if (imgo_0 != null)
                {
                    g.drawImage(imgo_0, x + num, y - num + h - imgo_0.getHeight());
                }
                else
                {
                    imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                }
                if (imgo_1 != null)
                {
                    g.drawImage(imgo_1, x + num2, y - num2 + h - imgo_1.getHeight());
                }
                else
                {
                    imgo_1 = mSystem.loadImage("/mainImage/o_1.png");
                }
                break;
            case 35:
                if (imgo_0 != null)
                {
                    g.drawImage(imgo_0, x + num, y - num + h - imgo_0.getHeight());
                }
                else
                {
                    imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                }
                if (imgo_2 != null)
                {
                    g.drawImage(imgo_2, x + num2, y - num2 + h - imgo_2.getHeight());
                }
                else
                {
                    imgo_2 = mSystem.loadImage("/mainImage/o_2.png");
                }
                break;
            case 36:
                if (imgo_0 != null)
                {
                    g.drawImage(imgo_0, x + num, y - num + h - imgo_0.getHeight());
                }
                else
                {
                    imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                }
                if (imgo_3 != null)
                {
                    g.drawImage(imgo_3, x + num2, y - num2 + h - imgo_3.getHeight());
                }
                else
                {
                    imgo_3 = mSystem.loadImage("/mainImage/o_3.png");
                }
                break;
        }
    }

    public void paintOptItemInventory(mGraphics g, int idOpt, int param, int x, int y, int w, int h, Item item)
    {
        int num3 = 145;
        int num4 = 12;
        int num5 = 19;
        try
        {
            if (item == null || item.template == null)
            {
                return;
            }
            switch (idOpt)
            {
                case 102:
                    {
                        int maxStar = 8;
                        if (imgo_17 != null)
                        {
                            if (param > 0)
                            {
                                if (imgo_19 != null)
                                {
                                    for (int l = param; l < maxStar; l++)
                                    {
                                        g.drawImage(imgo_19, x + w - imgo_19.getWidth() + num5 + l * 3, y + h - imgo_19.getHeight());
                                    }
                                }
                                else
                                {
                                    imgo_19 = mSystem.loadImage("/mainImage/starE.png");
                                }
                                for (int i = 0; i < Math.min(param, maxStar - 1); i++)
                                {
                                    g.drawImage(imgo_17, x + w - imgo_17.getWidth() + num5 + i * 3, y + h - imgo_17.getHeight());
                                }
                                if (param > maxStar - 3)
                                {
                                    for (int j = maxStar - 1; j < param; j++)
                                    {
                                        g.drawImage(imgo_18, x + w - imgo_18.getWidth() + num5 + j * 3, y + h - imgo_18.getHeight());
                                    }
                                }
                                mFont.tahoma_7b_dark.drawString(g, string.Empty + param, x + w - imgo_17.getWidth() + num5 + 1, y + h - imgo_17.getHeight() - 1, 1);
                            }
                        }
                        else
                        {
                            imgo_19 = mSystem.loadImage("/mainImage/starE.png");
                            imgo_17 = mSystem.loadImage("/mainImage/star.png");
                            imgo_18 = mSystem.loadImage("/mainImage/star8.png");
                        }
                        break;
                    }
                case 34:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x + w - imgo_0.getWidth() + num3, y + h - imgo_0.getHeight() - num4);
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                    }
                    if (imgo_1 != null)
                    {
                        g.drawImage(imgo_1, x + w - imgo_1.getWidth() + num3, y + h - imgo_1.getHeight() - num5);
                    }
                    else
                    {
                        imgo_1 = mSystem.loadImage("/mainImage/o_1.png");
                    }
                    break;
                case 35:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x + w - imgo_0.getWidth() + num3, y + h - imgo_0.getHeight() - num4);
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                    }
                    if (imgo_2 != null)
                    {
                        g.drawImage(imgo_2, x + w - imgo_2.getWidth() + num3, y + h - imgo_2.getHeight() - num5);
                    }
                    else
                    {
                        imgo_2 = mSystem.loadImage("/mainImage/o_2.png");
                    }
                    break;
                case 36:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x + w - imgo_0.getWidth() + num3, y + h - imgo_0.getHeight() - num4);
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_00.png");
                    }
                    if (imgo_3 != null)
                    {
                        g.drawImage(imgo_3, x + w - imgo_3.getWidth() + num3, y + h - imgo_3.getHeight() - num5);
                    }
                    else
                    {
                        imgo_3 = mSystem.loadImage("/mainImage/o_3.png");
                    }
                    break;
                case 58:
                    if (item.template.type == 0)
                    {
                        if (imgo_5 != null)
                        {
                            g.drawImage(imgo_5, x + w - imgo_5.getWidth() + num3 - 18, y + h - imgo_5.getHeight() + 1);
                        }
                        else
                        {
                            imgo_5 = mSystem.loadImage("/mainImage/11706.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_5.getWidth() + num3 + 8, y + h - imgo_5.getHeight() - 1, 1);
                    }
                    if (item.template.type == 1)
                    {
                        if (imgo_6 != null)
                        {
                            g.drawImage(imgo_6, x + w - imgo_6.getWidth() + num3 - 18, y + h - imgo_6.getHeight() + 1);
                        }
                        else
                        {
                            imgo_6 = mSystem.loadImage("/mainImage/11707.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_6.getWidth() + num3 + 8, y + h - imgo_6.getHeight() - 1, 1);
                    }
                    if (item.template.type == 2)
                    {
                        if (imgo_7 != null)
                        {
                            g.drawImage(imgo_7, x + w - imgo_7.getWidth() + num3 - 18, y + h - imgo_7.getHeight() + 1);
                        }
                        else
                        {
                            imgo_7 = mSystem.loadImage("/mainImage/11708.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_7.getWidth() + num3 + 8, y + h - imgo_7.getHeight() - 1, 1);
                    }
                    if (item.template.type == 3)
                    {
                        if (imgo_8 != null)
                        {
                            g.drawImage(imgo_8, x + w - imgo_8.getWidth() + num3 - 18, y + h - imgo_8.getHeight() + 1);
                        }
                        else
                        {
                            imgo_8 = mSystem.loadImage("/mainImage/11709.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_8.getWidth() + num3 + 8, y + h - imgo_8.getHeight() - 1, 1);
                    }
                    if (item.template.type == 4)
                    {
                        if (imgo_20 != null)
                        {
                            g.drawImage(imgo_20, x + w - imgo_20.getWidth() + num3 - 18, y + h - imgo_20.getHeight() + 1);
                        }
                        else
                        {
                            imgo_20 = mSystem.loadImage("/mainImage/13418.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_20.getWidth() + num3 + 8, y + h - imgo_20.getHeight() - 1, 1);
                    }
                    break;
                case 57:
                    if (item.template.type == 21 || item.template.type == 72 || item.template.type == 11 || item.template.type == 23 || item.template.type == 24)
                    {
                        if (imgo_15 != null)
                        {
                            g.drawImage(imgo_15, x + w - imgo_15.getWidth() + num3 - 17, y + h - imgo_15.getHeight() + 1);
                        }
                        else
                        {
                            imgo_15 = mSystem.loadImage("/mainImage/20613.png");
                        }
                        mFont.tahoma_7b_dark.drawString(g, string.Empty + "lv" + param, x + w - imgo_15.getWidth() + num3 + 11, y + h - imgo_15.getHeight() + 2, 1);
                    }
                    break;
                case 72:
                    if (item.template.type == 32)
                    {
                        if (imgo_00 != null)
                        {
                            g.drawImage(imgo_00, x + w - imgo_00.getWidth() + num3, y + h - imgo_00.getHeight() - num4);
                        }
                        else
                        {
                            imgo_00 = mSystem.loadImage("/mainImage/o_00.png");
                        }
                        if (imgo_23 != null)
                        {
                            g.drawImage(imgo_23, x + w - imgo_23.getWidth() + num3, y + h - imgo_23.getHeight() - num5);
                        }
                        else
                        {
                            imgo_23 = mSystem.loadImage("/mainImage/29505.png");
                        }
                    }
                    if (item.template.type == 5)
                    {
                        if (imgo_00 != null)
                        {
                            g.drawImage(imgo_00, x + w - imgo_00.getWidth() + num3, y + h - imgo_00.getHeight() - num4);
                        }
                        else
                        {
                            imgo_00 = mSystem.loadImage("/mainImage/o_00.png");
                        }
                        if (imgo_4 != null)
                        {
                            g.drawImage(imgo_4, x + w - imgo_4.getWidth() + num3, y + h - imgo_4.getHeight() - num5);
                        }
                        else
                        {
                            imgo_4 = mSystem.loadImage("/mainImage/o_4.png");
                        }
                    }
                    if (item.template.type == 21 || item.template.type == 72 || item.template.type == 11 || item.template.type == 23 || item.template.type == 24)
                    {
                        if (imgo_00 != null)
                        {
                            g.drawImage(imgo_00, x + w - imgo_00.getWidth() + num3, y + h - imgo_00.getHeight() - num4);
                        }
                        else
                        {
                            imgo_00 = mSystem.loadImage("/mainImage/o_00.png");
                        }
                        if (imgo_16 != null)
                        {
                            g.drawImage(imgo_16, x + w - imgo_16.getWidth() + num3, y + h - imgo_16.getHeight() - num5);
                        }
                        else
                        {
                            imgo_16 = mSystem.loadImage("/mainImage/20606.png");
                        }
                    }
                    break;
            }
        }
        catch (Exception exception)
        {
            Debug.LogException(exception);
        }
    }

    public void paintOptSlotItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
    {
        if (idOpt == 102 && param > ChatPopup.numSlot)
        {
            sbyte color_Item_Upgrade = GetColor_Item_Upgrade(param);
            int nline = param - ChatPopup.numSlot;
            paintUpgradeEffect(x, y, w, h, nline, color_Item_Upgrade, g);
        }
    }

    private void loadTabModFunc()
    {
        if (type == 26)
        {
            SoundMn.gI().GetStrModFunc();
            selected = 0;
        }
    }

    public static mFont setTextColor(int id, int type)
    {
        if (type == 0)
        {
            return id switch
            {
                0 => mFont.bigNumber_While,
                1 => mFont.bigNumber_green,
                3 => mFont.bigNumber_orange,
                4 => mFont.bigNumber_blue,
                5 => mFont.bigNumber_yellow,
                6 => mFont.bigNumber_red,
                _ => mFont.bigNumber_While,
            };
        }
        return id switch
        {
            0 => mFont.tahoma_7b_white,
            1 => mFont.tahoma_7b_green,
            3 => mFont.tahoma_7b_yellowSmall2,
            4 => mFont.tahoma_7b_blue,
            5 => mFont.tahoma_7b_yellow,
            6 => mFont.tahoma_7b_red,
            7 => mFont.tahoma_7b_dark,
            _ => mFont.tahoma_7b_white,
        };
    }

    private bool GetInventorySelect_isbody(int select, int subSelect, Item[] arrItem)
    {
        int num = select - ((!ModFunc.isInventory) ? 1 : 0) + subSelect * 20;
        if (subSelect == 0)
        {
            return num < arrItem.Length;
        }
        return false;
    }

    private int GetInventorySelect_body(int select, int subSelect)
    {
        if (ModFunc.isInventory && isTabBox())
        {
            return select;
        }
        if (!ModFunc.isInventory)
        {
            // Calculate total items and validate against current tab's item count
            int totalItems = Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length;
            int itemsPerTab = 20;
            int totalTabs = (totalItems / itemsPerTab) + ((totalItems % itemsPerTab > 0) ? 1 : 0);
            
            // Get the actual number of items in the current tab
            int currentTabItemCount;
            if (subSelect == totalTabs - 1 && totalItems % itemsPerTab > 0)
            {
                // Last tab with remainder
                currentTabItemCount = totalItems % itemsPerTab;
            }
            else
            {
                // Full tab or last tab that's exactly divisible
                currentTabItemCount = itemsPerTab;
            }
            
            // Validate select: row 0 is tab selector, rows 1-currentTabItemCount are items
            int itemIndexInTab = select - 1; // Subtract 1 for tab selector
            if (itemIndexInTab < 0) itemIndexInTab = 0;
            if (itemIndexInTab >= currentTabItemCount)
            {
                // Clamp to last valid item in current tab
                itemIndexInTab = currentTabItemCount - 1;
            }
            
            // Calculate the actual item index
            int calculatedIndex = subSelect * itemsPerTab + itemIndexInTab;
            
            // Final validation: ensure it's within arrItemBody bounds
            if (Char.myCharz().arrItemBody != null)
            {
                if (calculatedIndex < 0) calculatedIndex = 0;
                if (calculatedIndex >= Char.myCharz().arrItemBody.Length)
                {
                    // This item is in bag, return -1 to indicate it's not in body
                    return -1;
                }
            }
            return calculatedIndex;
        }
        return select - ((!ModFunc.isInventory) ? 1 : 0) + subSelect * 20;
    }

    private int GetInventorySelect_bag(int select, int subSelect, Item[] arrItem)
    {
        if (!ModFunc.isInventory)
        {
            // Calculate total items and validate against current tab's item count
            int totalItems = Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length;
            int itemsPerTab = 20;
            int totalTabs = (totalItems / itemsPerTab) + ((totalItems % itemsPerTab > 0) ? 1 : 0);
            
            // Get the actual number of items in the current tab
            int currentTabItemCount;
            if (subSelect == totalTabs - 1 && totalItems % itemsPerTab > 0)
            {
                // Last tab with remainder
                currentTabItemCount = totalItems % itemsPerTab;
            }
            else
            {
                // Full tab or last tab that's exactly divisible
                currentTabItemCount = itemsPerTab;
            }
            
            // Validate select: row 0 is tab selector, rows 1-currentTabItemCount are items
            int itemIndexInTab = select - 1; // Subtract 1 for tab selector
            if (itemIndexInTab < 0) itemIndexInTab = 0;
            if (itemIndexInTab >= currentTabItemCount)
            {
                // Clamp to last valid item in current tab
                itemIndexInTab = currentTabItemCount - 1;
            }
            
            // Calculate the actual item index
            int totalItemIndex = subSelect * itemsPerTab + itemIndexInTab;
            int calculatedIndex = totalItemIndex - arrItem.Length;
            
            // Final validation: ensure it's within arrItemBag bounds
            if (Char.myCharz().arrItemBag != null)
            {
                if (calculatedIndex < 0) calculatedIndex = 0;
                if (calculatedIndex >= Char.myCharz().arrItemBag.Length)
                {
                    calculatedIndex = Char.myCharz().arrItemBag.Length - 1;
                }
            }
            return calculatedIndex;
        }
        return select - ((!ModFunc.isInventory) ? 1 : 0) + subSelect * 20 - arrItem.Length;
    }

    private bool isTabBox()
    {
        if (type == 2 && currentTabIndex == 0)
        {
            return true;
        }
        return false;
    }

    private bool isTabInven()
    {
        if ((type == 0 && currentTabIndex == 1) || (type == 7 && currentTabIndex == 0) || (type == 13 && currentTabIndex == 0))
        {
            return true;
        }
        return false;
    }

    private void updateKeyInvenTab()
    {
        if (selected < 0)
        {
            return;
        }
        if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
        {
            newSelected--;
            if (isnewInventory)
            {
                currentListLength = 5;
            }
            if (newSelected < 0)
            {
                newSelected = 0;
                if (GameCanvas.isFocusPanel2)
                {
                    GameCanvas.isFocusPanel2 = false;
                    GameCanvas.panel.selected = 0;
                }
            }
            if (type == 26)
            {
                currentTabIndex = newSelected;
                loadTabModFunc();
            }
        }
        else
        {
            if (!GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
            {
                return;
            }
            newSelected++;
            if (isnewInventory)
            {
                currentListLength = 5;
            }
            if (newSelected > size_tab - 1)
            {
                newSelected = size_tab - 1;
                if (GameCanvas.panel2 != null)
                {
                    GameCanvas.isFocusPanel2 = true;
                    GameCanvas.panel2.selected = 0;
                }
            }
            if (type == 26)
            {
                currentTabIndex = newSelected;
                loadTabModFunc();
            }
        }
    }

    private void updateKeyInventory()
    {
        int newSelectedBefore = newSelected;
        bool tabClickedBefore = tabClickedInPointerDown;
        updateKeyScrollView();
        int newSelectedAfter = newSelected;
        bool tabClickedAfter = tabClickedInPointerDown;
        if (newSelectedBefore != newSelectedAfter)
        {
            cmtoY = 0;
            cmy = 0;
        }
        if (tabClickedBefore && !tabClickedAfter)
        {
        }
        if (selected == 0 && !tabClickedInPointerDown)
        {
            updateKeyInvenTab();
            int newSelectedAfterTab = newSelected;
            if (newSelectedAfter != newSelectedAfterTab)
            {
            }
        }
        else if (selected == 0 && tabClickedInPointerDown)
        {
        }
    }

    private bool IsTabOption()
    {
        if (size_tab > 0)
        {
            if (currentTabName.Length > 1)
            {
                if (selected == 0)
                {
                    return true;
                }
            }
            else if (selected >= 0)
            {
                return true;
            }
        }
        return false;
    }

    private int checkCurrentListLength(int arrLength)
    {
        if (!ModFunc.isInventory)
        {
            int num = 20;
            int num2 = arrLength / 20 + ((arrLength % 20 > 0) ? 1 : 0);
            size_tab = (sbyte)num2;
            if (newSelected > num2 - 1)
            {
                newSelected = num2 - 1;
            }
            if (arrLength % 20 > 0 && newSelected == num2 - 1)
            {
                num = arrLength % 20;
            }
            return num + 1;
        }
        return arrLength + 1;
    }

    private void setNewSelected(int arrLength, bool resetSelect)
    {
        int num = arrLength / 20 + ((arrLength % 20 > 0) ? 1 : 0);
        int num2 = xScroll;
        newSelected = (GameCanvas.px - num2) / TAB_W_NEW;
        if (newSelected > num - 1)
        {
            newSelected = num - 1;
        }
        if (GameCanvas.px < num2)
        {
            newSelected = 0;
        }
        setTabInventory(resetSelect);
    }
}
