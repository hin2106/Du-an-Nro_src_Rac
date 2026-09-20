using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Mod;
using Mod.XMAP;
using UnityEngine;
using UnityEngine.Networking;

public class ModFunc : IActionListener
{
    public class HotkeyDefinition
    {
        public string id;

        public string label;

        public KeyCode defaultKey;

        public HotkeyDefinition(string id, string label, KeyCode defaultKey)
        {
            this.id = id;
            this.label = label;
            this.defaultKey = defaultKey;
        }
    }

    public const string HOTKEY_MOVE_LEFT = "move_left";
    public const string HOTKEY_MOVE_UP = "move_up";
    public const string HOTKEY_MOVE_DOWN = "move_down";
    public const string HOTKEY_MOVE_RIGHT = "move_right";
    public const string HOTKEY_FRIEND = "friend";
    public const string HOTKEY_AUTO_ZONE = "auto_zone";
    public const string HOTKEY_AUTO_ATTACK = "auto_attack";
    public const string HOTKEY_MOD_MENU = "mod_menu";
    public const string HOTKEY_FUSION = "fusion";
    public const string HOTKEY_CAPSULE = "capsule";
    public const string HOTKEY_ZONE = "zone";
    public const string HOTKEY_AUTO_TRAINING = "auto_training";
    public const string HOTKEY_AUTO_PICK = "auto_pick";
    public const string HOTKEY_MAP_LEFT = "map_left";
    public const string HOTKEY_MAP_CENTER = "map_center";
    public const string HOTKEY_MAP_RIGHT = "map_right";
    public const string HOTKEY_TRADE = "trade";
    public const string HOTKEY_PET = "pet";
    public const string HOTKEY_CHAT = "chat";
    public const string HOTKEY_PICK_ITEM = "pick_item";
    public const string HOTKEY_USE_HP = "use_hp";
    public const string HOTKEY_ACCEPT = "accept";
    public const string HOTKEY_SKILL_PREFIX = "skill_";

    private const string HOTKEY_RMS_KEY = "custom_hotkeys_v1";

    public static readonly HotkeyDefinition[] hotkeyDefinitions = CreateHotkeyDefinitions();

    private readonly Dictionary<string, KeyCode> hotkeys = new Dictionary<string, KeyCode>();

    private Dictionary<string, KeyCode> hotkeyDraft = new Dictionary<string, KeyCode>();

    private string pendingHotkeyId;

    private static HotkeyDefinition[] CreateHotkeyDefinitions()
    {
        List<HotkeyDefinition> result = new List<HotkeyDefinition>
        {
            new HotkeyDefinition(HOTKEY_MOVE_LEFT, "Dịch trái 100 pixel", KeyCode.A),
            new HotkeyDefinition(HOTKEY_MOVE_UP, "Dịch lên 100 pixel", KeyCode.W),
            new HotkeyDefinition(HOTKEY_MOVE_DOWN, "Dịch xuống 100 pixel", KeyCode.S),
            new HotkeyDefinition(HOTKEY_MOVE_RIGHT, "Dịch phải 100 pixel", KeyCode.D),
            new HotkeyDefinition(HOTKEY_FRIEND, "Mở danh sách bạn bè", KeyCode.E),
            new HotkeyDefinition(HOTKEY_AUTO_ZONE, "Bật/tắt tự cập nhật khu", KeyCode.H),
            new HotkeyDefinition(HOTKEY_AUTO_ATTACK, "Bật/tắt tự đánh", KeyCode.U),
            new HotkeyDefinition(HOTKEY_MOD_MENU, "Mở menu MOD", KeyCode.X),
            new HotkeyDefinition(HOTKEY_FUSION, "Dùng Bông tai Porata", KeyCode.F),
            new HotkeyDefinition(HOTKEY_CAPSULE, "Dùng Capsule đặc biệt", KeyCode.C),
            new HotkeyDefinition(HOTKEY_ZONE, "Mở danh sách khu", KeyCode.M),
            new HotkeyDefinition(HOTKEY_AUTO_TRAINING, "Dùng tự động luyện tập", KeyCode.T),
            new HotkeyDefinition(HOTKEY_AUTO_PICK, "Bật/tắt tự động nhặt", KeyCode.N),
            new HotkeyDefinition(HOTKEY_MAP_LEFT, "Đi cửa map bên trái", KeyCode.J),
            new HotkeyDefinition(HOTKEY_MAP_CENTER, "Đi cửa map ở giữa", KeyCode.K),
            new HotkeyDefinition(HOTKEY_MAP_RIGHT, "Đi cửa map bên phải", KeyCode.L),
            new HotkeyDefinition(HOTKEY_TRADE, "Mời giao dịch mục tiêu", KeyCode.G),
            new HotkeyDefinition(HOTKEY_PET, "Mở tab Đệ tử", KeyCode.P),
            new HotkeyDefinition(HOTKEY_CHAT, "Mở chat", KeyCode.R),
            new HotkeyDefinition(HOTKEY_PICK_ITEM, "Nhặt vật phẩm gần", KeyCode.Q),
            new HotkeyDefinition(HOTKEY_USE_HP, "Dùng vật phẩm hồi phục", KeyCode.Space),
            new HotkeyDefinition(HOTKEY_ACCEPT, "Đồng ý thông báo", KeyCode.Y)
        };
        for (int i = 1; i <= 8; i++)
        {
            result.Add(new HotkeyDefinition(HOTKEY_SKILL_PREFIX + i, "Dùng kỹ năng " + i, (KeyCode)((int)KeyCode.Alpha0 + i)));
        }
        return result.ToArray();
    }

    public class Point
    {
        public int x;

        public int y;

        public Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
    public static int iconSize = 20;
    public static int iconSpacing => iconSize + 4; 
    public static int iconY = 3;
    public static int realMenuX;
    public static int realChatX;
    public static int realMailX;

    private static ModFunc Instance;

    public static string homeUrl = "HinzCutee";
    public static bool ModNotLogo = true;

    public static bool ModNotLogoGif = false;

    public enum DataType
    {
        Int = 0,
        Double = 1,
        Long = 2
    }

    public static DataType currentDataType = DataType.Long;

    public static bool isReadInt = false;

    public static bool isReadDouble = false;

    public static bool isActiveTamBao = true;

    public static bool isActiveNapTuan = true;

    public static bool isEncryptIcon = false;

    public static bool isVietnamese = false;

    public static bool isShowMenuChat = false;

    public static bool isShowMailPanel = false;
    private static int mailPanelPage = 0;
    private static int mailPanelSelected = -1;
    private static bool mailDetailLoaded = false;
    private static string mailNotifyText = null;
    private static float mailNotifyTimer = 0f;
    private static string mailClaimPopupTitle = null;
    private static string mailClaimPopupDetail = null;
    private static float mailClaimPopupTimer = 0f;
    private static readonly PopupTransition mailClaimPopupTransition = new PopupTransition(PopupTransitionType.Reward);
    private static List<Mail.RewardData> mailClaimPopupRewards = new List<Mail.RewardData>();
    private static List<Mail.RewardData> mailPendingClaimRewards = new List<Mail.RewardData>();
    private static bool mailPreloaded = false;
    private static long mailPreloadStartTime = 0L;
    private static bool mailBulkDeleteInProgress = false;
    private static bool mailBulkDeleteWaitingDetail = false;
    private static bool mailBulkDeleteWaitingDelete = false;
    private static Queue<long> mailBulkDeleteNeedDetailIds = new Queue<long>();
    private static Queue<long> mailBulkDeleteReadyDeleteIds = new Queue<long>();
    private static long mailBulkDeleteCurrentId = -1;
    private static int mailBulkDeleteDeletedCount = 0;
    private static int mailBulkDeleteCandidateCount = 0;
    private static string mailBulkDeleteStatusText = null;
    private static float mailBulkDeleteStatusTimer = 0f;
    private const int MailBadgeColor = 0xFF3B30;
    private const byte MailRewardTypeItem = 1;
    private const byte MailRewardTypeGold = 2;
    private const byte MailRewardTypeGem = 3;
    private const int MailContentMaxLines = 6;
    private const int MailItemSlotSize = 28;
    private const int MailItemSlotGap = 6;
    private const int MailItemSlotColumns = 5;
    private const int MailItemPreviewWidth = 158;
    private const int MailItemPreviewMinHeight = 120;
    private const int MailItemPreviewMaxDescLines = 5;
    private const int MailButtonHeight = 24;
    private const int MailButtonMinWidth = 46;
    private const int MailPreloadDelayMs = 1000;
    private const int MailItemBgColor = 0xD8D8D8;
    private const int MailItemBgHighlightColor = 0xF4F4F4;
    private const int MailItemBorderColor = 0x8E8E8E;
    private const float MailClaimPopupDuration = 3f;
    private const int MailClaimPopupWidth = 150;
    private const int MailClaimPopupHeight = 58;
    private const int MailClaimPopupItemColumns = 4;
    private const int MailClaimPopupTextGap = 8;

    private static Mail.RewardData mailPreviewReward = null;
    private static readonly PopupTransition mailItemPreviewTransition = new PopupTransition(PopupTransitionType.ItemInfo);
    private static bool mailConfirmDelete = false;

    public static bool isMenuVisible = false;

    public static float arrowRotation = 0f;

    public static float menuX = 0f;

    public static float targetMenuX = 0f;

    public static float targetArrowRotation = 0f;

    public static float ANIMATION_SPEED = 0.1f;

    private static bool isDebugEnable = false;

    private static long lastTimeLog = 0L;

    public static int gameServerPing = -1;

    private static long lastPingRequest;

    private static long pendingPingToken;

    private const long PING_INTERVAL_MS = 2000L;

    private const long PING_TIMEOUT_MS = 10000L;

    public bool canUpdate;

    public static Command cmdAccManager;

    public static bool isOpenAccMAnager = false;

    public static List<Account> accounts = new List<Account>();

    public List<Command> cmdsChooseAcc = new List<Command>();

    public List<Command> cmdsDelAcc = new List<Command>();
    
    public List<Command> cmdsEditAcc = new List<Command>();

    public static Command cmdCloseAccManager;
    
    public Scroll scrAccManager = new Scroll();
    
    private int accManagerScrollY = 0;

    private static int modKeyPosX;

    private static int modKeyPosY;

    public static bool isAutoLogin = false;

    public static bool dangLogin = false;

    public static AutoLogin autoLogin;

    public static bool isAutoNoitai = false;

    public bool autoAttack;

    public bool autoWakeUp;

    public long lastAutoWakeUp;

    public bool isAutoPhaLe;

    public bool isQKeyPressed;

    private long lastAutoAttack;

    private readonly List<Skill> listSkillsAuto = new List<Skill>();

    public List<ItemAuto> listItemAuto = new List<ItemAuto>();

    private static bool isAutoChat = false;

    private static string textAutoChat = string.Empty;

    private static bool isAutoChatTG = false;

    private static string textAutoChatTG = string.Empty;

    public static bool startAutoItem = false;

    private long lastAutoChat;

    private long lastAutoChatTG;

    public static bool isFilterItem = false;

    public static bool isAutoFilterItem = false;

    public static List<ItemAutoFilter> listFilterItems = new List<ItemAutoFilter>();

    public static bool isShowFilterList = false;

    private bool isResizing;

    private int lastMouseX;

    private int lastMouseY;

    private int lastPanelX;

    private int lastPanelY;

    private int lastPanelW;

    private int lastPanelH;

    private int lastScrollY;

    private bool isScrolling;

    private int scrollY;

    private readonly int MAX_ITEMS_VISIBLE = 10;

    private long lastFilterTime;

    public static bool notifBoss = false;

    public static bool notifKillBoss = false;

    private bool lineToBoss;

    private bool focusBoss;

    private long lastFocusBoss;

    public static MyVector activeBossNotif = new MyVector();

    public static MyVector killedBossNotif = new MyVector();

    public bool showCharsInMap = true;

    public bool userOpenZones;

    public bool isUpdateZones;

    private long lastUpdateZones;

    public MyVector charsInMap = new MyVector();

    private sealed class MapPlayerSnapshot
    {
        public Char character;
        public long lastSeen;
    }

    private const long MAP_PLAYER_GRACE_MS = 1500L;
    private static readonly Dictionary<int, MapPlayerSnapshot> mapPlayerSnapshots = new Dictionary<int, MapPlayerSnapshot>();
    private static int snapshotMapId = -1;
    private static int snapshotZoneId = -1;

    public static int zoneMacDinh;

    public static bool isdoBoss;

    private static long currDoBoss;

    public static string bossCanDo;

    public Item itemPhale;

    public int maxPhale = -1;

    public int currPhale = -1;

    private static int currentPage = 0;

    private int ChiSoNoiTai = -1;

    public string curSelectIntrinsic = "";

    private string CurrentNoiTai = "";

    private long lastTimeUpdateNoiTai;

    private string currentPlayerNoiTai = "";

    private int CurrentParamNoitaiPlayer;

    public MyVector listNotifTichXanh = new MyVector();

    private bool startChat;

    private int xNotif;

    private long lastUpdateNotif;

    public bool isPeanPet;

    private long lastPeanPet;

    public static bool autoPointForPet = false;

    public static bool userOpenPet = false;

    public static int indexAutoPoint = -1;

    public static int pointIncrease = 0;

    public bool showInfoMe;

    private long lastUpdateInfoMe;

    public bool isShowButton = true;

    public bool isIntroOff;

    public static bool isInventory = true;

    public static bool isEffectInven = false;

    public static bool isLogo = true;

    public static bool isLogoGif = false;

    public static bool GiamDungLuong = false;

    public static bool AnPlayer = false;

    public bool isHighFps;

    public static bool isShowID = false;

    private static int FrameGif = 15;

    private static int FrameGifMenu = 16;

    public static Image[] ticks = new Image[20];

    private static Image logo = new Image();

    private static Image[] logos = new Image[FrameGif];

    private static Image[] logosMenu = new Image[FrameGifMenu];

    public static Image imgLogoBig = null;

    public static Image imgBg = null;

    public static bool isShortOptionTemp = false;

    public static Image imgMenuChat = null;

    public static Image imgCloseButton = null;

    public static Image imgNextPage = null;

    public static Image imgNextPage2 = null;

    public static Image imgPrevPage = null;

    public static Image imgPrevPage2 = null;

    private static Image imgMailPopup = null;

    private static Image imgMailPopup2 = null;

    public static int musicCount = 0;

    public static bool loadedMusic = false;

    public static bool isPlayingMusic = false;

    public static List<AudioClip> musics = new List<AudioClip>();

    private static string backgroundColor = "0.6 0.8 0.9";

    public static bool isEditButton = false;

    private static Dictionary<string, Point> buttonPositions = new Dictionary<string, Point>();

    private static bool isDragging = false;

    public static string ipServer = "Đổi IP";

    public static bool isLockFocus = false;

    public static string strAddAutoItem = "Thêm vào\nAutoItem";

    public static string strRemoveAutoItem = "Xoá khỏi\nAutoItem";

    public static string strAddFilterItem = "Thêm vào\nDS lọc";

    public static string strRemoveFilterItem = "Xóa khỏi\nDS lọc";

    public static string strTeleportTo = "Dịch\nchuyển tới";

    public static string strAutoBuy = "Mua 20 lần";

    public static string strChooseIntrinsic = "Chọn chỉ số";

    public static string strInCrease = "Tăng\ntới\nmức";

    public static string[] strPointTypes = new string[5] { "HP", "MP", "Sức Đánh", "Giáp", "Chí mạng" };

    public static string strAccManager = "Q.L.T.K";

    public static string strModFunc = "Chức Năng MOD";

    public static string strHotkeySettings = "Cài Đặt Phím Tắt";

    public static string strUpdateZones = "Cập Nhật Khu";

    public static string strCharsInMap = "Nhân Vật Trong Khu";

    public static string strInfoMe = "Thông Tin Bản Thân";

    public static string strAutoPhaLe = "Tự Động Pha Lê Hóa";

    public static string strAutoWakeUp = "Tự Động Hồi Sinh";

    public static string strAutoLogin = "Tự Động Đăng Nhập";

    public static string strShowButton = "Hiện Nút Trợ Năng";

    public static string strChangeType = "Change Type";

    public static string strIntroOff = "Tắt Intro";

    public static string strInventoryOFF = "Hiện Hành Trang Lưới";

    public static string strEffectOff = "Tắt Hiệu Ứng Hành Trang";

    public static string strHighFps = "FPS Cao";

    public static string strClickToChat = " [Ấn để chat]";

    public static string strPlayerInfo = "Thông tin player";

    public static string strPet2 = "Người iuu";

    public static string strUseForPet2 = "Sử dụng\ncho\nNg.iuu";

    public static string strLogo = "Ẩn / hiện Logo";

    public static string strGiamDungLuong = "Giảm Dung Lượng";

    public static string strAnPlayer = "Ẩn Player";

    public static string strLogoGif = "Logo động";

    public static string strShowID = "Hiện ID Item/NPC";

    public static string strEditButton = "Tùy chỉnh giao diện";

    public static string strVietnamese = "Gõ Tiếng Việt";

    public static string strShowMenuChat = "Thông Tin Lệnh Chat";

    private static readonly Dictionary<string, Point> defaultButtonPositions = new Dictionary<string, Point>
    {
        {
            "Capsule",
            new Point(20, -26)
        },
        {
            "Fusion",
            new Point(-21, 21)
        },
        {
            "Zone",
            new Point(-66, 62)
        },
        {
            "MapLeft",
            new Point(-106, 62)
        },
        {
            "MapCenter",
            new Point(-66, 21)
        },
        {
            "MapRight",
            new Point(-21, -26)
        }
    };

    private sealed class HudElementState
    {
        public int x;
        public int y;
        public int scale = 100;
        public int opacity = 100;
        public bool anchorRight;
        public int rightMargin;
        public bool anchorInitialized;

        public HudElementState Clone()
        {
            return new HudElementState
            {
                x = x,
                y = y,
                scale = scale,
                opacity = opacity,
                anchorRight = anchorRight,
                rightMargin = rightMargin,
                anchorInitialized = anchorInitialized
            };
        }
    }

    private sealed class HudRegion
    {
        public string id;
        public string label;
        public int sourceX;
        public int sourceY;
        public int width;
        public int height;
    }

    public struct HudPaintState
    {
        public mGraphics.LayoutTransformState transform;
        public mGraphics.OpacityState opacity;
        public mGraphics.ClipState clip;
        public int translateX;
        public int translateY;
        public mGraphics graphics;
    }

    public struct HudPointerState
    {
        public int px;
        public int py;
        public int pxLast;
        public int pyLast;
        public int pxFirst;
        public int pyFirst;
    }

    private static readonly Dictionary<string, HudElementState> hudPc = new Dictionary<string, HudElementState>();
    private static readonly Dictionary<string, HudElementState> hudVirtual = new Dictionary<string, HudElementState>();
    private static Dictionary<string, HudElementState> hudDraft;
    private static readonly Dictionary<string, HudRegion> hudRegions = new Dictionary<string, HudRegion>();
    private static bool hudPcLoaded;
    private static bool hudVirtualLoaded;
    private static string selectedHudElement;
    private static bool hudDragging;
    private static int hudDragOffsetX;
    private static int hudDragOffsetY;
    private static bool hudEditingVirtual;
    private static bool hudActionsDragging;
    private static bool hudActionsMoved;
    private static int hudActionsDragOffsetX;
    private static int hudActionsDragOffsetY;
    private static int hudActionsPressX;
    private static int hudActionsPressY;

    public const string HUD_INFO = "info";
    public const string HUD_BOSS = "boss";
    public const string HUD_PLAYERS = "players";
    public const string HUD_SKILLS = "skills";
    public const string HUD_CHAT = "chat";
    public const string HUD_BEAN = "bean";
    public const string HUD_WORLD_CHAT = "world_chat";
    public const string HUD_FIRE = "fire";
    public const string HUD_TARGET = "target";
    public const string HUD_JOYSTICK = "joystick";
    public const string HUD_REQUEST = "request";
    public const string HUD_SHOW_LIST = "show_list";
    private const string HUD_EDITOR_ACTIONS = "editor_actions";
    private const int HUD_ACTION_BUTTON_WIDTH = 52;
    private const int HUD_ACTION_BUTTON_HEIGHT = 21;
    private const int HUD_ACTION_GAP = 2;

    private static int panelX = GameCanvas.w / 3 + 25;

    private static int panelY = 15;

    private static int panelW = 200;

    private static int panelH = 170;

    public static void InitButtonPositions()
    {
        if (buttonPositions.Count != 0)
        {
            return;
        }
        foreach (KeyValuePair<string, Point> kvp in defaultButtonPositions)
        {
            buttonPositions[kvp.Key] = new Point(kvp.Value.x, kvp.Value.y);
        }
    }

    public static ModFunc GI()
    {
        if (Instance == null)
        {
            Instance = new ModFunc();
            LoadDataTypeConfig();
        }
        return Instance;
    }

    public void OpenMenu()
    {
        MyVector myVector = new MyVector();
        myVector.addElement(new Command("Bản đồ", 883));
        myVector.addElement(new Command("Luyện tập", 45));
        myVector.addElement(new Command("Nhặt đồ", 89));
        myVector.addElement(new Command("Đệ tử", 16));
        myVector.addElement(new Command("BOSS", 32));
        myVector.addElement(new Command("Khác", 53));
        GameCanvas.menu.startAt(myVector, 4);
    }

    public static Color GetColor()
    {
        string[] array = backgroundColor.Split(' ');
        return new Color(float.Parse(array[0]), float.Parse(array[1]), float.Parse(array[2]));
    }

    public void LoadHotkeys()
    {
        hotkeys.Clear();
        foreach (HotkeyDefinition definition in hotkeyDefinitions)
        {
            hotkeys[definition.id] = definition.defaultKey;
        }
        string saved = Rms.loadRMSString(HOTKEY_RMS_KEY);
        if (string.IsNullOrEmpty(saved))
        {
            return;
        }
        string[] entries = saved.Split(';');
        foreach (string entry in entries)
        {
            string[] parts = entry.Split('=');
            if (parts.Length != 2 || !hotkeys.ContainsKey(parts[0]))
            {
                continue;
            }
            if (int.TryParse(parts[1], out int keyValue))
            {
                KeyCode key = (KeyCode)keyValue;
                if (key == KeyCode.None || IsAssignableHotkey(key))
                {
                    hotkeys[parts[0]] = key;
                }
            }
        }
        RemoveDuplicateHotkeys(hotkeys);
    }

    public void BeginHotkeyDraft()
    {
        hotkeyDraft = new Dictionary<string, KeyCode>(hotkeys);
    }

    public KeyCode GetDraftHotkey(int index)
    {
        if (index < 0 || index >= hotkeyDefinitions.Length)
        {
            return KeyCode.None;
        }
        return hotkeyDraft.TryGetValue(hotkeyDefinitions[index].id, out KeyCode key) ? key : KeyCode.None;
    }

    public void SetDraftHotkey(int index, KeyCode key)
    {
        if (index < 0 || index >= hotkeyDefinitions.Length)
        {
            return;
        }
        string id = hotkeyDefinitions[index].id;
        if (key != KeyCode.None)
        {
            foreach (HotkeyDefinition definition in hotkeyDefinitions)
            {
                if (definition.id != id && hotkeyDraft.TryGetValue(definition.id, out KeyCode existing) && existing == key)
                {
                    hotkeyDraft[definition.id] = KeyCode.None;
                }
            }
        }
        hotkeyDraft[id] = key;
    }

    public void ResetHotkeyDraft()
    {
        hotkeyDraft.Clear();
        foreach (HotkeyDefinition definition in hotkeyDefinitions)
        {
            hotkeyDraft[definition.id] = definition.defaultKey;
        }
    }

    private static Dictionary<string, HudElementState> ActiveHudLayout
    {
        get
        {
            bool virtualKeyboard = GameScr.isAnalog != 0;
            EnsureHudLayoutLoaded(virtualKeyboard);
            if (isEditButton && hudDraft != null && hudEditingVirtual == virtualKeyboard)
            {
                return hudDraft;
            }
            return virtualKeyboard ? hudVirtual : hudPc;
        }
    }

    private static string HudStorageKey(bool virtualKeyboard)
    {
        return virtualKeyboard ? "hud_layout_virtual_v2" : "hud_layout_pc_v2";
    }

    private static void EnsureHudLayoutLoaded(bool virtualKeyboard)
    {
        if ((virtualKeyboard && hudVirtualLoaded) || (!virtualKeyboard && hudPcLoaded))
        {
            return;
        }
        Dictionary<string, HudElementState> target = virtualKeyboard ? hudVirtual : hudPc;
        target.Clear();
        string raw = Rms.loadRMSString(HudStorageKey(virtualKeyboard));
        if (!string.IsNullOrEmpty(raw))
        {
            string[] entries = raw.Split(';');
            for (int i = 0; i < entries.Length; i++)
            {
                string[] parts = entries[i].Split(',');
                int x;
                int y;
                int scale;
                int opacity;
                if (parts.Length >= 5 && int.TryParse(parts[1], out x) && int.TryParse(parts[2], out y) && int.TryParse(parts[3], out scale) && int.TryParse(parts[4], out opacity))
                {
                    HudElementState loaded = new HudElementState
                    {
                        x = x,
                        y = y,
                        scale = System.Math.Max(30, System.Math.Min(200, scale)),
                        opacity = System.Math.Max(5, System.Math.Min(100, opacity))
                    };
                    int rightMargin;
                    int anchorValue;
                    if (parts.Length >= 7 && int.TryParse(parts[5], out anchorValue) && int.TryParse(parts[6], out rightMargin))
                    {
                        loaded.anchorRight = anchorValue == 1;
                        loaded.rightMargin = System.Math.Max(0, rightMargin);
                        loaded.anchorInitialized = true;
                    }
                    target[parts[0]] = loaded;
                }
            }
        }
        if (virtualKeyboard)
        {
            hudVirtualLoaded = true;
        }
        else
        {
            hudPcLoaded = true;
        }
    }

    private static Dictionary<string, HudElementState> CloneHudLayout(Dictionary<string, HudElementState> source)
    {
        Dictionary<string, HudElementState> clone = new Dictionary<string, HudElementState>();
        foreach (KeyValuePair<string, HudElementState> pair in source)
        {
            clone[pair.Key] = pair.Value.Clone();
        }
        return clone;
    }

    private static void SaveHudLayout(bool virtualKeyboard, Dictionary<string, HudElementState> source)
    {
        string raw = string.Empty;
        foreach (KeyValuePair<string, HudElementState> pair in source)
        {
            HudElementState value = pair.Value;
            raw += pair.Key + "," + value.x + "," + value.y + "," + value.scale + "," + value.opacity
                + "," + (value.anchorRight ? 1 : 0) + "," + value.rightMargin + ";";
        }
        Rms.saveRMSString(HudStorageKey(virtualKeyboard), raw);
    }

    private static HudElementState GetHudState(string id, int defaultX, int defaultY)
    {
        Dictionary<string, HudElementState> layout = ActiveHudLayout;
        HudElementState state;
        if (!layout.TryGetValue(id, out state))
        {
            state = new HudElementState { x = defaultX, y = defaultY };
            layout[id] = state;
        }
        return state;
    }

    public static HudPaintState BeginHudElement(mGraphics g, string id, string label, int x, int y, int width, int height)
    {
        HudElementState state = GetHudState(id, x, y);
        hudRegions[id] = new HudRegion { id = id, label = label, sourceX = x, sourceY = y, width = System.Math.Max(1, width), height = System.Math.Max(1, height) };
        EnsureHudHorizontalAnchor(state, width);
        ApplyHudHorizontalAnchor(state, width);
        ClampHudState(state, width, height);
        int translateX = state.x - x;
        int translateY = state.y - y;
        g.translate(translateX, translateY);
        HudPaintState paintState = new HudPaintState
        {
            clip = g.PushUnclipped(),
            transform = mGraphics.PushLayoutTransform(state.x, state.y, state.x, state.y, state.scale / 100f),
            opacity = mGraphics.PushOpacity(state.opacity / 100f),
            translateX = translateX,
            translateY = translateY,
            graphics = g
        };
        return paintState;
    }

    public static void BeginHudFrame()
    {
        hudRegions.Clear();
    }

    public static void EndHudElement(HudPaintState state)
    {
        mGraphics.PopOpacity(state.opacity);
        mGraphics.PopLayoutTransform(state.transform);
        state.graphics.PopClip(state.clip);
        if (state.translateX != 0 || state.translateY != 0)
        {
            state.graphics.translate(-state.translateX, -state.translateY);
        }
    }

    public static void GetHudBounds(string id, int defaultX, int defaultY, int width, int height, out int x, out int y, out int scaledWidth, out int scaledHeight)
    {
        HudElementState state = GetHudState(id, defaultX, defaultY);
        EnsureHudHorizontalAnchor(state, width);
        ApplyHudHorizontalAnchor(state, width);
        ClampHudState(state, width, height);
        x = state.x;
        y = state.y;
        scaledWidth = System.Math.Max(1, width * state.scale / 100);
        scaledHeight = System.Math.Max(1, height * state.scale / 100);
    }

    public static bool IsHudPointerIn(string id, int defaultX, int defaultY, int totalWidth, int totalHeight, int localX, int localY, int width, int height)
    {
        HudElementState state = GetHudState(id, defaultX, defaultY);
        EnsureHudHorizontalAnchor(state, totalWidth);
        ApplyHudHorizontalAnchor(state, totalWidth);
        ClampHudState(state, totalWidth, totalHeight);
        int x = state.x + localX * state.scale / 100;
        int y = state.y + localY * state.scale / 100;
        int scaledWidth = System.Math.Max(1, width * state.scale / 100);
        int scaledHeight = System.Math.Max(1, height * state.scale / 100);
        return IsPointerIn(x, y, scaledWidth, scaledHeight);
    }

    public static bool IsHudImagePointerIn(string id, int defaultX, int defaultY, int totalWidth, int totalHeight, int localX, int localY, int width, int height, Image image)
    {
        if (image == null || !IsHudPointerIn(id, defaultX, defaultY, totalWidth, totalHeight, localX, localY, width, height))
        {
            return false;
        }
        HudElementState state = GetHudState(id, defaultX, defaultY);
        float sourceX = defaultX + (GameCanvas.px - state.x) * 100f / state.scale;
        float sourceY = defaultY + (GameCanvas.py - state.y) * 100f / state.scale;
        float u = (sourceX - defaultX - localX) / System.Math.Max(1, width);
        float v = 1f - (sourceY - defaultY - localY) / System.Math.Max(1, height);
        try
        {
            return image.texture.GetPixelBilinear(UnityEngine.Mathf.Clamp01(u), UnityEngine.Mathf.Clamp01(v)).a > 0.05f;
        }
        catch
        {
            return true;
        }
    }

    public static bool IsHudPointerInRegistered(string id, int localX, int localY, int width, int height)
    {
        HudRegion region;
        if (!hudRegions.TryGetValue(id, out region))
        {
            return false;
        }
        return IsHudPointerIn(id, region.sourceX, region.sourceY, region.width, region.height, localX, localY, width, height);
    }

    public static bool IsHudPointerInRegisteredRow(string id, int localY, int height)
    {
        HudRegion region;
        if (!hudRegions.TryGetValue(id, out region))
        {
            return false;
        }
        return IsHudPointerIn(id, region.sourceX, region.sourceY, region.width, region.height, 0, localY, region.width, height);
    }

    public static HudPointerState PushHudPointer(string id)
    {
        HudPointerState pointerState = new HudPointerState
        {
            px = GameCanvas.px,
            py = GameCanvas.py,
            pxLast = GameCanvas.pxLast,
            pyLast = GameCanvas.pyLast,
            pxFirst = GameCanvas.pxFirst,
            pyFirst = GameCanvas.pyFirst
        };
        HudRegion region;
        if (!hudRegions.TryGetValue(id, out region))
        {
            return pointerState;
        }
        HudElementState state = GetHudState(id, region.sourceX, region.sourceY);
        GameCanvas.px = region.sourceX + (GameCanvas.px - state.x) * 100 / state.scale;
        GameCanvas.py = region.sourceY + (GameCanvas.py - state.y) * 100 / state.scale;
        GameCanvas.pxLast = region.sourceX + (GameCanvas.pxLast - state.x) * 100 / state.scale;
        GameCanvas.pyLast = region.sourceY + (GameCanvas.pyLast - state.y) * 100 / state.scale;
        GameCanvas.pxFirst = region.sourceX + (GameCanvas.pxFirst - state.x) * 100 / state.scale;
        GameCanvas.pyFirst = region.sourceY + (GameCanvas.pyFirst - state.y) * 100 / state.scale;
        return pointerState;
    }

    public static void PopHudPointer(HudPointerState state)
    {
        GameCanvas.px = state.px;
        GameCanvas.py = state.py;
        GameCanvas.pxLast = state.pxLast;
        GameCanvas.pyLast = state.pyLast;
        GameCanvas.pxFirst = state.pxFirst;
        GameCanvas.pyFirst = state.pyFirst;
    }

    private static void ClampHudState(HudElementState state, int width, int height)
    {
        int scaledWidth = System.Math.Max(1, width * state.scale / 100);
        int scaledHeight = System.Math.Max(1, height * state.scale / 100);
        state.x = System.Math.Max(0, System.Math.Min(System.Math.Max(0, GameCanvas.w - scaledWidth), state.x));
        state.y = System.Math.Max(0, System.Math.Min(System.Math.Max(0, GameCanvas.h - scaledHeight), state.y));
    }

    private static void EnsureHudHorizontalAnchor(HudElementState state, int width)
    {
        if (state.anchorInitialized)
        {
            return;
        }
        int scaledWidth = System.Math.Max(1, width * state.scale / 100);
        state.anchorRight = state.x + scaledWidth / 2 >= GameCanvas.w / 2;
        state.rightMargin = System.Math.Max(0, GameCanvas.w - state.x - scaledWidth);
        state.anchorInitialized = true;
    }

    private static void ApplyHudHorizontalAnchor(HudElementState state, int width)
    {
        if (!state.anchorRight)
        {
            return;
        }
        int scaledWidth = System.Math.Max(1, width * state.scale / 100);
        state.x = GameCanvas.w - scaledWidth - state.rightMargin;
    }

    private static void UpdateHudHorizontalAnchor(HudElementState state, int width)
    {
        int scaledWidth = System.Math.Max(1, width * state.scale / 100);
        state.anchorRight = state.x + scaledWidth / 2 >= GameCanvas.w / 2;
        state.rightMargin = System.Math.Max(0, GameCanvas.w - state.x - scaledWidth);
        state.anchorInitialized = true;
    }

    private static void KeepHudElementBelow(string id, int defaultX, int defaultY, int width, int height, string upperId, int gap)
    {
        HudRegion upperRegion;
        if (!hudRegions.TryGetValue(upperId, out upperRegion))
        {
            return;
        }
        HudElementState upperState = GetHudState(upperId, upperRegion.sourceX, upperRegion.sourceY);
        HudElementState state = GetHudState(id, defaultX, defaultY);
        int upperBottom = upperState.y + System.Math.Max(1, upperRegion.height * upperState.scale / 100);
        if (state.y < upperBottom + gap)
        {
            state.y = upperBottom + gap;
        }
        ClampHudState(state, width, height);
        if (state.y < upperBottom + gap)
        {
            int scaledHeight = System.Math.Max(1, height * state.scale / 100);
            state.y = System.Math.Max(0, upperState.y - gap - scaledHeight);
        }
    }

    public void SaveHotkeyDraft()
    {
        hotkeys.Clear();
        StringBuilder data = new StringBuilder();
        foreach (HotkeyDefinition definition in hotkeyDefinitions)
        {
            KeyCode key = hotkeyDraft.TryGetValue(definition.id, out KeyCode draftKey) ? draftKey : KeyCode.None;
            hotkeys[definition.id] = key;
            if (data.Length > 0)
            {
                data.Append(';');
            }
            data.Append(definition.id).Append('=').Append((int)key);
        }
        Rms.saveRMSString(HOTKEY_RMS_KEY, data.ToString());
    }

    private static void RemoveDuplicateHotkeys(Dictionary<string, KeyCode> values)
    {
        HashSet<KeyCode> used = new HashSet<KeyCode>();
        foreach (HotkeyDefinition definition in hotkeyDefinitions)
        {
            if (!values.TryGetValue(definition.id, out KeyCode key) || key == KeyCode.None)
            {
                continue;
            }
            if (!used.Add(key))
            {
                values[definition.id] = KeyCode.None;
            }
        }
    }

    public static bool IsAssignableHotkey(KeyCode key)
    {
        if (key == KeyCode.None || key == KeyCode.Escape || key == KeyCode.UpArrow || key == KeyCode.DownArrow || key == KeyCode.LeftArrow || key == KeyCode.RightArrow || key == KeyCode.Return || key == KeyCode.KeypadEnter || key == KeyCode.Backspace || key == KeyCode.Tab || key == KeyCode.F1 || key == KeyCode.F2 || key == KeyCode.LeftShift || key == KeyCode.RightShift || key == KeyCode.LeftControl || key == KeyCode.RightControl || key == KeyCode.LeftAlt || key == KeyCode.RightAlt || key == KeyCode.LeftWindows || key == KeyCode.RightWindows)
        {
            return false;
        }
        return key < KeyCode.Mouse0 || key > KeyCode.Mouse6;
    }

    public static string GetHotkeyName(KeyCode key)
    {
        if (key == KeyCode.None)
        {
            return "—";
        }
        if (key >= KeyCode.Alpha0 && key <= KeyCode.Alpha9)
        {
            return ((int)key - (int)KeyCode.Alpha0).ToString();
        }
        if (key >= KeyCode.Keypad0 && key <= KeyCode.Keypad9)
        {
            return "Num" + ((int)key - (int)KeyCode.Keypad0);
        }
        return key switch
        {
            KeyCode.Space => "Space",
            KeyCode.Return => "Enter",
            KeyCode.KeypadEnter => "NumEnter",
            KeyCode.UpArrow => "↑",
            KeyCode.DownArrow => "↓",
            KeyCode.LeftArrow => "←",
            KeyCode.RightArrow => "→",
            _ => key.ToString()
        };
    }

    public bool IsConfiguredHotkey(KeyCode key)
    {
        return key != KeyCode.None && hotkeys.ContainsValue(key);
    }

    public KeyCode GetHotkey(string id)
    {
        return hotkeys.TryGetValue(id, out KeyCode key) ? key : KeyCode.None;
    }

    public bool TryQueueHotkey(KeyCode key)
    {
        if (!Main.isPC || GameCanvas.currentScreen != GameScr.instance || GameCanvas.currentDialog != null || GameCanvas.menu.showMenu || GameCanvas.panel.isShow || ChatTextField.gI().isShow || InfoDlg.isLock || Controller.isStopReadMessage || Char.myCharz().isTeleport || Char.myCharz().isPaintNewSkill || !TField.isQwerty)
        {
            return false;
        }
        foreach (HotkeyDefinition definition in hotkeyDefinitions)
        {
            if (hotkeys.TryGetValue(definition.id, out KeyCode configuredKey) && configuredKey == key)
            {
                pendingHotkeyId = definition.id;
                return true;
            }
        }
        return false;
    }

    public bool IsHotkeyHeld(string id)
    {
        return hotkeys.TryGetValue(id, out KeyCode key) && key != KeyCode.None && Input.GetKey(key);
    }

    public string ConsumePendingHotkey()
    {
        string result = pendingHotkeyId;
        pendingHotkeyId = null;
        return result;
    }

    public bool ExecuteHotkey(string id, GameScr gameScr)
    {
        if (string.IsNullOrEmpty(id))
        {
            return false;
        }
        if (id.StartsWith(HOTKEY_SKILL_PREFIX))
        {
            if (int.TryParse(id.Substring(HOTKEY_SKILL_PREFIX.Length), out int skillNumber))
            {
                int skillIndex = skillNumber - 1;
                if (skillIndex >= 0 && skillIndex < GameScr.keySkill.Length && GameScr.keySkill[skillIndex] != null)
                {
                    gameScr.doSelectSkill(GameScr.keySkill[skillIndex], isShortcut: true);
                }
            }
            return true;
        }
        switch (id)
        {
            case HOTKEY_MOVE_LEFT:
                MoveTo(Char.myCharz().cx - 100, Char.myCharz().cy);
                break;
            case HOTKEY_MOVE_UP:
                MoveTo(Char.myCharz().cx, Char.myCharz().cy - 100);
                break;
            case HOTKEY_MOVE_DOWN:
                MoveTo(Char.myCharz().cx, Char.myCharz().cy + 100);
                break;
            case HOTKEY_MOVE_RIGHT:
                MoveTo(Char.myCharz().cx + 100, Char.myCharz().cy);
                break;
            case HOTKEY_FRIEND:
                Service.gI().friend(0, -1);
                InfoDlg.showWait();
                break;
            case HOTKEY_AUTO_ZONE:
                GameScr.gI().onChatFromMe("ukhu", string.Empty);
                break;
            case HOTKEY_AUTO_ATTACK:
                perform(42, null);
                break;
            case HOTKEY_MOD_MENU:
                OpenMenu();
                break;
            case HOTKEY_FUSION:
                UsePorata();
                break;
            case HOTKEY_CAPSULE:
                UseItem(194);
                break;
            case HOTKEY_ZONE:
                userOpenZones = true;
                Service.gI().openUIZone();
                break;
            case HOTKEY_AUTO_TRAINING:
                UseItem(521);
                break;
            case HOTKEY_AUTO_PICK:
                PickMob.IsAutoPickItems = !PickMob.IsAutoPickItems;
                GameScr.info1.addInfo("Tự động nhặt: " + (PickMob.IsAutoPickItems ? "Bật" : "Tắt"), 0);
                break;
            case HOTKEY_MAP_LEFT:
                ManualXmap.GI().LoadMapLeft();
                break;
            case HOTKEY_MAP_CENTER:
                ManualXmap.GI().LoadMapCenter();
                break;
            case HOTKEY_MAP_RIGHT:
                ManualXmap.GI().LoadMapRight();
                break;
            case HOTKEY_TRADE:
                if (Char.myCharz().charFocus != null)
                {
                    Service.gI().giaodich(0, Char.myCharz().charFocus.charID, -1, -1);
                    GameScr.info1.addInfo("Đã gửi lời mời giao dịch đến " + Char.myCharz().charFocus.cName, 0);
                }
                break;
            case HOTKEY_PET:
                if (Char.myCharz().havePet)
                {
                    GameCanvas.panel.doFirePet();
                }
                else
                {
                    GameScr.info1.addInfo("Bạn chưa có đệ tử", 0);
                }
                break;
            case HOTKEY_CHAT:
                ChatTextField.gI().startChat(gameScr, string.Empty);
                break;
            case HOTKEY_PICK_ITEM:
                Char.myCharz().searchItem();
                if (Char.myCharz().itemFocus != null)
                {
                    gameScr.pickItem();
                }
                break;
            case HOTKEY_USE_HP:
                gameScr.doUseHP();
                break;
            case HOTKEY_ACCEPT:
                if (gameScr.popUpYesNo != null)
                {
                    gameScr.popUpYesNo.cmdYes.performAction();
                }
                else if (GameScr.info2.info.info != null && GameScr.info2.info.info.charInfo != null)
                {
                    GameCanvas.panel.setTypeMessage();
                    GameCanvas.panel.show();
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void LoadGame()
    {
        LoadHotkeys();
        if (!loadedMusic)
        {
            InitMusic();
            loadedMusic = true;
        }
        Time.timeScale = 1.5f;
        listSkillsAuto.Clear();
        listItemAuto.Clear();
        isHighFps = Rms.loadRMSInt("isHighFps") != 0;
        int inventoryValue = Rms.loadRMSInt("inventory");
        if (inventoryValue == -1)
        {
            isInventory = true;
            Rms.saveRMSInt("inventory", 1);
        }
        else
        {
            isInventory = inventoryValue == 1;
        }
        isEffectInven = Rms.loadRMSInt("effectinven") == 1;
        GiamDungLuong = Rms.loadRMSInt("background") == 1;
        AnPlayer = Rms.loadRMSInt("anplayer") == 1;
        autoWakeUp = Rms.loadRMSInt("autoWakeUp") == 1;
        if (!ModNotLogo)
        {
            isLogo = Rms.loadRMSInt("logo") == 1;
            isLogoGif = Rms.loadRMSInt("logoGif") == 1;
            if (isLogo)
            {
                if (isLogoGif)
                {
                    LoadLogoGif();
                }
                else
                {
                    LoadLogoImages();
                }
            }
        }
        ChangeFPSTarget();
        if (autoWakeUp)
        {
            GameScr.info1.addInfo("Tự động hồi sinh [Bật]", 0);
        }
        mailPreloaded = false;
        mailPreloadStartTime = mSystem.currentTimeMillis();

        TryPreloadMailList();
        DragonPass.Network.GetState();
        LoadButtonPositions();
        QuaNapTuan.isNapTuan = false;
    }

    private static void TryPreloadMailList()
    {
        if (mailPreloaded)
        {
            return;
        }

        if (mailPreloadStartTime <= 0L)
        {
            mailPreloadStartTime = mSystem.currentTimeMillis();
            return;
        }

        if (mSystem.currentTimeMillis() - mailPreloadStartTime < MailPreloadDelayMs)
        {
            return;
        }

        if (Mail.MailManager.Instance == null)
        {
            return;
        }

        mailPreloaded = true;
        Mail.MailManager.Instance.LoadMailList(0);
    }

    public void MoveTo(int x, int y)
    {
        Char.myCharz().cx = x;
        Char.myCharz().cy = y;
        Service.gI().charMove();
        if (!ItemTime.isExistItem(4387))
        {
            Char.myCharz().cx = x;
            Char.myCharz().cy = y + 1;
            Service.gI().charMove();
            Char.myCharz().cx = x;
            Char.myCharz().cy = y;
            Service.gI().charMove();
        }
    }

    public void GotoNpc(int npcID)
    {
        for (int i = 0; i < GameScr.vNpc.size(); i++)
        {
            Npc npc = (Npc)GameScr.vNpc.elementAt(i);
            if (npc.template.npcTemplateId == npcID && Math.abs(npc.cx - Char.myCharz().cx) >= 50)
            {
                MoveTo(npc.cx, npc.cy - 1);
                Char.myCharz().FocusManualTo(npc);
                break;
            }
        }
    }

    public int FindItemIndex(int idItem)
    {
        if (Char.myCharz().arrItemBag == null)
        {
            return -1;
        }
        for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
        {
            if (Char.myCharz().arrItemBag[i] != null && Char.myCharz().arrItemBag[i].template.id == idItem)
            {
                return Char.myCharz().arrItemBag[i].indexUI;
            }
        }
        return -1;
    }

    private void AttackChar()
    {
        try
        {
            MyVector myVector = new MyVector();
            myVector.addElement(Char.myCharz().charFocus);
            Service.gI().sendPlayerAttack(new MyVector(), myVector, 2);
        }
        catch
        {
        }
    }

    public void AttackMob(Mob mob)
    {
        try
        {
            MyVector myVector = new MyVector();
            myVector.addElement(mob);
            Service.gI().sendPlayerAttack(myVector, new MyVector(), 1);
        }
        catch
        {
        }
    }

    public void AutoAttack()
    {
        Char @char = Char.myCharz();
        if (!Char.isLoadingMap && !@char.stone && !@char.meDead && @char.statusMe != 14 && @char.statusMe != 5 && @char.myskill.template.type == 1 && @char.myskill.template.id != 10 && @char.myskill.template.id != 11 && !@char.myskill.paintCanNotUseSkill && mSystem.currentTimeMillis() - lastAutoAttack > 500)
        {
            if (GameScr.gI().isMeCanAttackMob(@char.mobFocus) && Res.abs(@char.mobFocus.xFirst - @char.cx) < @char.myskill.dx * 2)
            {
                AttackMob(@char.mobFocus);
                SetUsedSkill(@char.myskill);
            }
            else if (@char.isMeCanAttackOtherPlayer(@char.charFocus) && Res.abs(@char.charFocus.cx - @char.cx) < @char.myskill.dx * 2)
            {
                AttackChar();
                SetUsedSkill(@char.myskill);
            }
            lastAutoAttack = mSystem.currentTimeMillis();
        }
    }

    public void SetUsedSkill(Skill skill)
    {
        skill.paintCanNotUseSkill = true;
        skill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
    }

    public void UsePorata()
    {
        int[] array = new int[3] { 454, 921, 1884 };
        foreach (int num in array)
        {
            int index = FindItemIndex(num);
            if (index != -1)
            {
                Service.gI().useItem(0, 1, (sbyte)index, -1);
                Service.gI().petStatus(3);
                return;
            }
        }
        GameScr.info1.addInfo("Bạn không có bông tai", 0);
    }

    public void AutoFocusBoss()
    {
        for (int i = 0; i < GameScr.vCharInMap.size(); i++)
        {
            Char @char = (Char)GameScr.vCharInMap.elementAt(i);
            if (@char != null && @char.charID < 0 && @char.cTypePk == 5 && !@char.cName.StartsWith("Đ"))
            {
                Char.myCharz().FocusManualTo(@char);
                break;
            }
        }
    }

    public int GetMapID(string mapName)
    {
        int result = -1;
        for (int i = 0; i < XmapController.mapNames.Length; i++)
        {
            if (XmapController.mapNames[i].Trim().ToLower().Equals(mapName.Trim().ToLower()))
            {
                result = i;
            }
        }
        return result;
    }

    private string CharGender(Char @char)
    {
        if (@char.cTypePk == 5)
        {
            return "BOSS";
        }
        if (@char.cgender == 0)
        {
            return "TĐ";
        }
        if (@char.cgender == 1)
        {
            return "NM";
        }
        if (@char.cgender == 2)
        {
            return "XD";
        }
        return "";
    }

    public void UseItem(int itemId)
    {
        int index = FindItemIndex(itemId);
        if (index != -1)
        {
            Service.gI().useItem(0, 1, (sbyte)index, -1);
        }
        else
        {
            GameScr.info1.addInfo("Không tìm thấy vật phẩm", 0);
        }
    }

    public void UseItemAuto()
    {
        if (listItemAuto.Count <= 0 || !startAutoItem)
        {
            if (!startAutoItem)
            {
                System.Threading.Tasks.Task.Delay(200).ContinueWith((System.Threading.Tasks.Task t) => startAutoItem = true);
            }
            return;
        }
        if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {
            listItemAuto.Clear();
            GameScr.info1.addInfo("Đã dừng auto sử dụng Item", 0);
            return;
        }
        for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
        {
            Item item = Char.myCharz().arrItemBag[i];
            if (item == null)
            {
                continue;
            }
            foreach (ItemAuto itemAuto in listItemAuto)
            {
                if (item.template.iconID == itemAuto.iconID && item.template.id == itemAuto.id && !ItemTime.isExistItem(item.template.iconID))
                {
                    Service.gI().useItem(0, 1, (sbyte)FindItemIndex(item.template.id), -1);
                    if (listItemAuto.Count == 1)
                    {
                        return;
                    }
                    break;
                }
            }
        }
    }

    private void AutoHoiSinh()
    {
        if (Char.myCharz().cHP <= 0.0 || Char.myCharz().meDead || Char.myCharz().statusMe == 14)
        {
            Service.gI().wakeUpFromDead();
        }
    }

    public static int GetCurrPhaLe(Item item)
    {
        for (int i = 0; i < item.itemOption.Length; i++)
        {
            if (item.itemOption[i].optionTemplate.id == 107)
            {
                return item.itemOption[i].param;
            }
        }
        return 0;
    }

    public void AutoPhaLe()
    {
        while (isAutoPhaLe)
        {
            if (TileMap.mapID != 5)
            {
                GameScr.info1.addInfo("Cần đến Đảo Kame để sử dụng Tự động Pha lê hóa", 0);
                Thread.Sleep(500);
                break;
            }
            if (currPhale >= maxPhale && itemPhale != null && currPhale >= 0 && maxPhale > 0)
            {
                Sound.start(1f, Sound.l1);
                GameScr.info1.addInfo("Đã đạt đến số sao yêu cầu", 0);
                maxPhale = -1;
                itemPhale = null;
            }
            if (Char.myCharz().xu > 10000000000L)
            {
                GotoNpc(21);
                if (itemPhale != null && maxPhale > 0)
                {
                    while (!GameCanvas.menu.showMenu)
                    {
                        Service.gI().combine(1, GameCanvas.panel.vItemCombine);
                        Thread.Sleep(100);
                    }
                    Service.gI().confirmMenu(21, 0);
                    GameCanvas.menu.doCloseMenu();
                    GameCanvas.panel.currItem = null;
                    GameCanvas.panel.chatTField.isShow = false;
                }
            }
            else if (itemPhale != null)
            {
                BanVang();
            }
            Thread.Sleep(500);
        }
    }

    private void BanVang()
    {
        if (TileMap.mapID != 5)
        {
            GameScr.info1.addInfo("Cần đến Đảo Kame để Tự động bán vàng", 0);
            Thread.Sleep(1000);
            return;
        }
        if (isQKeyPressed)
        {
            GameScr.info1.addInfo("Dừng bán vàng", 0);
            return;
        }
        while (Char.myCharz().xu <= 60000000000L && !isQKeyPressed)
        {
            if (FindItemIndex(457) == -1)
            {
                GameScr.info1.addInfo("Không tìm thấy thỏi vàng", 0);
                if (isAutoPhaLe)
                {
                    isAutoPhaLe = false;
                    GameScr.info1.addInfo("Vàng không đủ, đã tắt Tự động Pha lê hóa", 0);
                }
                return;
            }
            Service.gI().useItem(0, 1, (sbyte)FindItemIndex(457), -1);
            GameScr.info1.addInfo("Đang bán thỏi vàng", 0);
            Thread.Sleep(500);
        }
        GameScr.info1.addInfo("Đã bán xong", 0);
        Thread.Sleep(500);
    }

    public static Item FindItemBagWithIndexUI(int index)
    {
        Item[] arrItemBag = Char.myCharz().arrItemBag;
        foreach (Item item in arrItemBag)
        {
            if (item != null && item.indexUI == index)
            {
                return item;
            }
        }
        return null;
    }

    public bool Chat(string text)
    {
        switch (text)
        {
            case "ahs":
                autoWakeUp = !autoWakeUp;
                GameScr.info1.addInfo("Tụ động hồi sinh: " + (autoWakeUp ? "ON" : "OFF"), 0);
                return true;
            case "anhat":
                perform(89, null);
                return true;
            case "loadskill":
                perform(57, null);
                return true;
            case "ak":
                perform(42, null);
                return true;
            case "ts":
                perform(44, null);
                return true;
            case "tsnguoi":
                perform(48, null);
                return true;
            case "ukhu":
                isUpdateZones = !isUpdateZones;
                GameScr.info1.addInfo("Tự động cập nhật khu: " + (isUpdateZones ? "Bật" : "Tắt"), 0);
                return true;
            default:
                if (text.StartsWith("k "))
                {
                    if (int.TryParse(text.Replace("k ", ""), out var khu) && khu >= 0)
                    {
                        Service.gI().requestChangeZone(khu, -1);
                    }
                    return true;
                }
                if (text.StartsWith("s "))
                {
                    ChangeGameSpeed(text.Replace("s ", ""));
                    return true;
                }
                if (text.StartsWith("atc "))
                {
                    textAutoChat = text.Replace("atc ", "");
                    return true;
                }
                if (text.StartsWith("atctg "))
                {
                    textAutoChatTG = text.Replace("atctg ", "");
                    return true;
                }
                if (text.StartsWith("do "))
                {
                    bossCanDo = text.Replace("do ", "");
                    GameScr.info1.addInfo("Boss cần dò: " + bossCanDo, 0);
                    return true;
                }
                if (text == "dbx")
                {
                    isdoBoss = !isdoBoss;
                    GameScr.info1.addInfo("Tự động dò boss: " + (isdoBoss ? "Bật" : "Tắt"), 0);
                    return true;
                }
                if (text == "gtv")
                {
                    isVietnamese = !isVietnamese;
                    GameScr.info1.addInfo("Gõ Tiếng Việt: " + (isVietnamese ? "Bật" : "Tắt"), 0);
                    return true;
                }
                return false;
        }
    }

    private void UpdateTouch()
    {
        if (isEditButton)
        {
            HandleHudEditorInput();
            return;
        }
        if (GameScr.gI().isNotPaintTouchControl())
        {
            return;
        }

        int boxSize = 23; 
        int boxSpacing = boxSize + 2; 
        int boxY = 5; // Cách mép trên 5px đúng yêu cầu

        // Gốc tọa độ bắt đầu từ mép trái + 180px theo yêu cầu của bạn
        int startMenuX = 160; 

        // NÚT MỞ MENU: Khi đóng, nút mũi tên nằm ngay tại vị trí 180px
        if (!isMenuVisible)
        {
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSize, boxSize, 0, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isMenuVisible = true;
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }
        }

        if (isMenuVisible)
        {
            // ĐẢO CHIỀU: Bung từ TRÁI sang PHẢI tính từ mốc startMenuX (180)
            int btnArrowCloseX = startMenuX; // Nút đóng nằm ngoài cùng bên trái thanh menu
            int btnModX        = btnArrowCloseX + boxSpacing; // Dịch dần sang phải
            int btnChatX       = btnModX + boxSpacing;
            int btnMailX       = btnChatX + boxSpacing;
            int btnPassX       = btnMailX + boxSpacing;

            // NÚT 1: Bấm mũi tên để ĐÓNG MENU
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, btnArrowCloseX - startMenuX, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isMenuVisible = false;
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }

            // NÚT 2: Ô vuông ModFunc
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, btnModX - startMenuX, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                OpenMenu();
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }

            // NÚT 3: Ô vuông Chat
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, btnChatX - startMenuX, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isShowMenuChat = true;
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }

            // NÚT 4: Ô vuông Mail
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, btnMailX - startMenuX, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isShowMailPanel = !isShowMailPanel;
                if (isShowMailPanel && Mail.MailManager.Instance != null)
                {
                    mailPanelSelected = -1;
                    mailDetailLoaded = false;
                    mailPanelPage = 0;
                    Mail.MailManager.Instance.RefreshMailList();
                }
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }

            // Nút sự kiện Dragon Pass
            if (IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, btnPassX - startMenuX, 0, boxSize, boxSize) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isShowMailPanel = false;
                isShowMenuChat = false;
                DragonPass.DragonPassUI.Open();
                SoundMn.gI().buttonClick();
                GameCanvas.clearAllPointerEvent();
                return;
            }

            // Click ra ngoài vùng List ô vuông thì tự động đóng (Tính từ startMenuX kéo dài sang phải)
            if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                int menuWidth = btnPassX + boxSize - startMenuX;
                if (!IsHudPointerIn(HUD_SHOW_LIST, startMenuX, boxY, boxSpacing * 4 + boxSize, boxSize, 0, 0, menuWidth, boxSize))
                {
                    isMenuVisible = false;
                    GameCanvas.clearAllPointerEvent();
                    return;
                }
            }
        }

        if (GameScr.isAnalog == 0 || !isShowButton)
        {
            return;
        }
        foreach (KeyValuePair<string, Point> kvp2 in buttonPositions)
        {
            int num = modKeyPosX + kvp2.Value.x;
            int buttonY2 = modKeyPosY + kvp2.Value.y;
            if (IsHudImagePointerIn("mod_" + kvp2.Key, num - 16, buttonY2 - 16, 32, 32, 1, 1, 30, 30, GetModButtonImage(kvp2.Key)) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                switch (kvp2.Key)
                {
                    case "Capsule":
                        UseItem(194);
                        break;
                    case "Fusion":
                        UsePorata();
                        break;
                    case "Zone":
                        userOpenZones = true;
                        Service.gI().openUIZone();
                        break;
                    case "MapLeft":
                        ManualXmap.GI().LoadMapLeft();
                        break;
                    case "MapCenter":
                        ManualXmap.GI().LoadMapCenter();
                        break;
                    case "MapRight":
                        ManualXmap.GI().LoadMapRight();
                        break;
                }
                GameCanvas.clearAllPointerEvent();
                break;
            }
        }
    }

    public void Update()
    {   

        UpdateConnectionMetrics();

        if (DragonPass.DragonPassUI.BlocksInput)
        {
            DragonPass.DragonPassUI.HandleBlockingInput();
            return;
        }

        // Kiểm tra nếu đang có thông báo Pop-up hiển thị trên màn hình
        if (mailNotifyText != null)
        {
            // Nếu Client phát hiện có thao tác nhấn chuột hoặc chạm màn hình (Pointer Click)
            if (GameCanvas.isPointerClick)
            {
                mailNotifyText = null;          // Xóa chữ thông báo => Đóng Pop-up ngay lập tức
                GameCanvas.isPointerClick = false; // Reset trạng thái click để tránh bị nhấn nhầm vào nút phía dưới nền
                return; // Dừng xử lý các thao tác click khác bên dưới khi Pop-up đang mở
            }
        }
        TryPreloadMailList();
        if (mailBulkDeleteStatusTimer > 0f)
        {
            mailBulkDeleteStatusTimer -= Time.deltaTime;
            if (mailBulkDeleteStatusTimer <= 0f)
            {
                mailBulkDeleteStatusText = null;
                mailBulkDeleteStatusTimer = 0f;
            }
        }
        if (!DragonPass.DragonPassUI.Visible)
        {
            UpdateTouch();
        }

        isQKeyPressed = IsHotkeyHeld(HOTKEY_PICK_ITEM);
        long currentTime = mSystem.currentTimeMillis();
        if (isPeanPet && currentTime - lastPeanPet >= 3000)
        {
            Char pet = Char.myPetz();
            if (!pet.isDie && (pet.cStamina <= pet.cMaxStamina * 20 / 100 || pet.cHP < pet.cHPFull * 20.0 / 100.0 || pet.cMP < pet.cMPFull * 20.0 / 100.0))
            {
                GameScr.gI().doUseHP();
                lastPeanPet = currentTime;
            }
        }
        if (isAutoPhaLe && itemPhale != null)
        {
            currPhale = GetCurrPhaLe(FindItemBagWithIndexUI(itemPhale.indexUI));
        }
        else
        {
            currPhale = -1;
        }
        if (isAutoChat && currentTime - lastAutoChat >= 4000)
        {
            AutoChat();
            lastAutoChat = currentTime;
        }
        if (isAutoChatTG && currentTime - lastAutoChatTG >= 30000)
        {
            AutoChatTG();
            lastAutoChatTG = currentTime;
        }
        if (!TileMap.isOfflineMap() && currentTime - lastUpdateZones >= 1000)
        {
            UseItemAuto();
            if (isUpdateZones)
            {
                Service.gI().openUIZone();
            }
            lastUpdateZones = currentTime;
        }
        if (autoWakeUp && currentTime - lastAutoWakeUp >= 1000)
        {
            AutoHoiSinh();
            lastAutoWakeUp = currentTime;
        }
        if (focusBoss && currentTime - lastFocusBoss >= 500)
        {
            AutoFocusBoss();
            lastFocusBoss = currentTime;
        }
        if (autoAttack)
        {
            AutoAttack();
        }
        UpdateNotifTichXanh();
        if (isAutoNoitai && IsHotkeyHeld(HOTKEY_PICK_ITEM))
        {
            isAutoNoitai = false;
            ChiSoNoiTai = -1;
            curSelectIntrinsic = "";
            GameScr.info1.addInfo("Đã dừng auto mở nội tại", 0);
        }
        if (isAutoFilterItem && currentTime - lastFilterTime >= 500)
        {
            DoFilter();
            lastFilterTime = currentTime;
        }
        if (isdoBoss && mSystem.currentTimeMillis() - currDoBoss >= 1000)
        {
            DoBoss();
            currDoBoss = mSystem.currentTimeMillis();
        }
    }

    private static void UpdateConnectionMetrics()
    {
        long now = mSystem.currentTimeMillis();
        if (!Session_ME.gI().isConnected() || !(GameCanvas.currentScreen is GameScr))
        {
            gameServerPing = -1;
            pendingPingToken = 0L;
            return;
        }
        if (pendingPingToken != 0L && now - pendingPingToken >= PING_TIMEOUT_MS)
        {
            gameServerPing = -1;
            pendingPingToken = 0L;
        }
        if (pendingPingToken == 0L && now - lastPingRequest >= PING_INTERVAL_MS)
        {
            pendingPingToken = now;
            lastPingRequest = now;
            Service.gI().requestLatencyProbe(now);
        }
    }

    public static void OnLatencyProbeResponse(long token)
    {
        if (token != pendingPingToken)
        {
            return;
        }
        long roundTrip = mSystem.currentTimeMillis() - token;
        gameServerPing = (int)System.Math.Max(0L, System.Math.Min(9999L, roundTrip));
        pendingPingToken = 0L;
    }

    public void PaintButton(mGraphics g, int xAnchor, int yAnchor)
    {
        if (GameScr.isAnalog == 0 || !isShowButton || GameCanvas.currentDialog != null || ChatPopup.currChatPopup != null || GameCanvas.menu.showMenu || GameScr.gI().isPaintPopup() || GameCanvas.panel.isShow || Char.myCharz().taskMaint.taskId == 0 || ChatTextField.gI().isShow || GameCanvas.currentScreen == MoneyCharge.instance)
        {
            return;
        }
        modKeyPosX = xAnchor;
        modKeyPosY = yAnchor;
        InitButtonPositions();
        foreach (KeyValuePair<string, Point> kvp in buttonPositions)
        {
            string buttonName = kvp.Key;
            Point pos = kvp.Value;
            int buttonX = xAnchor + pos.x;
            int buttonY = yAnchor + pos.y;
            HudPaintState modButtonState = BeginHudElement(g, "mod_" + buttonName, "Nút " + buttonName, buttonX - 16, buttonY - 16, 32, 32);
            switch (buttonName)
            {
                case "Capsule":
                    g.drawImage(GameScr.imgCapsule, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgCapsuleF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
                case "Fusion":
                    g.drawImage(GameScr.imgFusion, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgFusionF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
                case "Zone":
                    g.drawImage(GameScr.imgChangeZone, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgChangeZoneF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
                case "MapLeft":
                    g.drawImage(GameScr.imgNextLeft, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgNextLeftF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
                case "MapCenter":
                    g.drawImage(GameScr.imgNextCenter, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgNextCenterF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
                case "MapRight":
                    g.drawImage(GameScr.imgNextRight, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    if (GameCanvas.isPointerHoldIn(buttonX - 15, buttonY - 15, 30, 30))
                    {
                        g.drawImage(GameScr.imgNextRightF, buttonX, buttonY, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    break;
            }
            EndHudElement(modButtonState);
        }
    }

    public void Paint(mGraphics g)
    {
        int imgHPWidth = mGraphics.getImageWidth(GameScr.imgHP);
        int imgMPWidth = mGraphics.getImageWidth(GameScr.imgMP);
        mFont.tahoma_7_red.drawStringBorder(g, NinjaUtil.getMoneys((long)Char.myCharz().cHP), 84 + imgHPWidth / 2, 4, mFont.CENTER, mFont.tahoma_7_grey);
        mFont.tahoma_7_blue1.drawStringBorder(g, NinjaUtil.getMoneys((long)Char.myCharz().cMP), 84 + imgMPWidth / 2, 17, mFont.CENTER, mFont.tahoma_7_grey);
        int xText = 5;
        int yText = GameScr.gI().cmdMenu.y + 30;
        if (!showInfoMe && !isAutoNoitai && !QuaNapTuan.isNapTuan)
        {
            string timeText = "Time: " + DateTime.Now.ToString("dd/MM/yyyy | HH:mm:ss");
            string mapText = isShowID
                ? TileMap.mapName + " [" + TileMap.mapID + "]  - Khu: " + TileMap.zoneID
                : TileMap.mapName + "  - Khu: " + TileMap.zoneID;
            string pingText = (gameServerPing >= 0) ? (gameServerPing + "ms") : "--ms";
            string positionText = "X: " + Char.myCharz().cx + " - Y: " + Char.myCharz().cy + " | " + Main.currentFps + " FPS | " + pingText;
            int infoWidth = System.Math.Max(mFont.tahoma_7_red.getWidth(mapText), mFont.tahoma_7_red.getWidth(positionText));
            infoWidth = System.Math.Max(infoWidth, mFont.tahoma_7_yellow.getWidth(timeText)) + 3;
            HudPaintState infoPaintState = BeginHudElement(g, HUD_INFO, "Thông tin map / khu / tọa độ", xText, yText, infoWidth, 32);
            mFont.tahoma_7_yellow.drawStringBorder(g, timeText, xText, yText + 20, mFont.LEFT, mFont.tahoma_7_grey);
            int num2 = 0;
            mFont.tahoma_7_red.drawStringBorder(g, mapText, xText, yText + num2, mFont.LEFT, mFont.tahoma_7_grey);
            num2 += 10;
            mFont.tahoma_7_red.drawStringBorder(g, positionText, xText, yText + num2, mFont.LEFT, mFont.tahoma_7_grey);
            EndHudElement(infoPaintState);
        }
        if (isAutoPhaLe && !isEditButton)
        {
            mFont.tahoma_7b_red.drawString(g, (itemPhale != null) ? itemPhale.template.name : "Chưa Có", GameCanvas.w / 2, 72, mFont.CENTER);
            mFont.tahoma_7b_red.drawString(g, (itemPhale != null) ? ("Số Sao : " + currPhale) : "Số Sao : -1", GameCanvas.w / 2, 82, mFont.CENTER);
            mFont.tahoma_7b_red.drawString(g, "Số Sao Cần Đập : " + maxPhale + " Sao", GameCanvas.w / 2, 92, mFont.CENTER);
        }
        if (isAutoPhaLe && !isEditButton)
        {
            Item tv = FindItemBagWithIndexUI(FindItemIndex(457));
            mFont.tahoma_7b_red.drawString(g, "Ngọc Xanh : " + NinjaUtil.getMoneys(Char.myCharz().luong) + " Ngọc Hồng : " + NinjaUtil.getMoneys(Char.myCharz().luongKhoa), GameCanvas.w / 2, 102, mFont.CENTER);
            mFont.tahoma_7b_red.drawString(g, "Vàng : " + NinjaUtil.getMoneys(Char.myCharz().xu) + " Thỏi Vàng : " + (tv?.quantity ?? 0), GameCanvas.w / 2, 112, mFont.CENTER);
        }
        if (showInfoMe && !isEditButton)
        {
            PaintInfoMe(g, xText, yText);
        }
        if ((notifBoss || isEditButton) && !isFilterItem && !isAutoNoitai && !QuaNapTuan.isNapTuan)
        {
            string bossTitle = "Thông báo Boss";
            int bossWidth = mFont.tahoma_7b_white.getWidth(bossTitle);
            for (int i = 0; i < activeBossNotif.size(); i++)
            {
                ShowBoss boss = (ShowBoss)activeBossNotif.elementAt(i);
                bossWidth = System.Math.Max(bossWidth, mFont.tahoma_7_yellow.getWidth(boss.GetActiveDisplayText()));
            }
            bossWidth += 8;
            int bossHeight = 16 + System.Math.Max(1, activeBossNotif.size()) * 10;
            int bossX = GameCanvas.w - bossWidth - 2;
            int bossY = 27;
            HudPaintState bossPaintState = BeginHudElement(g, HUD_BOSS, "Thông báo Boss", bossX, bossY, bossWidth, bossHeight);
            g.setColor(16776960);
            g.drawRect(bossX, bossY, bossWidth, bossHeight);
            mFont.tahoma_7b_white.drawStringBorder(g, bossTitle, GameCanvas.w - 4, bossY + 2, mFont.RIGHT, mFont.tahoma_7_grey);
            int numX = bossY + 13;
            for (int i = 0; i < activeBossNotif.size(); i++)
            {
                ((ShowBoss)activeBossNotif.elementAt(i)).PaintBoss(g, GameCanvas.w - 4, numX, mFont.RIGHT);
                numX += 10;
            }
            EndHudElement(bossPaintState);
        }
        if (notifKillBoss && !isEditButton && !showInfoMe && !isFilterItem && !isAutoNoitai && !QuaNapTuan.isNapTuan)
        {
            int numXKilledBoss = 65;
            for (int j = 0; j < killedBossNotif.size(); j++)
            {
                ((ShowBoss)killedBossNotif.elementAt(j)).PaintBoss(g, 100, numXKilledBoss, mFont.LEFT);
            }
        }
        if (showCharsInMap || isEditButton)
        {
            PaintCharInMap(g);
        }
        PaintListInfo(g);
        if (lineToBoss)
        {
            for (int k = 0; k < GameScr.vCharInMap.size(); k++)
            {
                Char @char = (Char)GameScr.vCharInMap.elementAt(k);
                if (@char != null && @char.cTypePk == 5 && !@char.cName.StartsWith("Đ"))
                {
                    g.setColor(Color.red);
                    g.drawLine(Char.myCharz().cx - GameScr.cmx, Char.myCharz().cy - GameScr.cmy, @char.cx - GameScr.cmx, @char.cy - GameScr.cmy);
                }
            }
        }
        if (TileMap.mapID != 51 && TileMap.mapID != 52 && TileMap.mapID != 113 && TileMap.mapID != 112 && TileMap.mapID != 129 && TileMap.mapID != 165)
        {
            PaintLogoGif(g, GameCanvas.hw, GameCanvas.hh / 4, 3);
        }
        int boxSize = 23; 
        int boxSpacing = boxSize + 2;
        int boxY = 5;
        int iconPadding = 1; 
        int innerIconSize = boxSize - (iconPadding * 2);
        int radius = 3; // Độ bo tròn góc (px)

        int startMenuX = 160; 

        int showListWidth = isMenuVisible ? boxSpacing * 4 + boxSize : boxSize;
        HudPaintState showListPaintState = BeginHudElement(g, HUD_SHOW_LIST, "Nút MOD / chat / mail / event", startMenuX, boxY, showListWidth, boxSize);

        if (isMenuVisible)
        {
            int btnArrowCloseX = startMenuX;
            int btnModX        = btnArrowCloseX + boxSpacing;
            int btnChatX       = btnModX + boxSpacing;
            int btnMailX       = btnChatX + boxSpacing;
            int btnPassX       = btnMailX + boxSpacing;

            // 1. Nút Đóng mũi
            int arrow2X = btnArrowCloseX + (boxSize - GameScr.imgArrow2.getWidth()) / 2;
            int arrow2Y = boxY + (boxSize - GameScr.imgArrow2.getHeight()) / 2;
            g.drawRegion(GameScr.imgArrow2, 0, 0, GameScr.imgArrow2.getWidth(), GameScr.imgArrow2.getHeight(), 2, arrow2X, arrow2Y, 0);

            // 2. Nút ModFunc
            g.drawImageScale(GameScr.imgModFunc, btnModX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, 0);

            // 3. Nút Chat
            g.drawImageScale(GameScr.imgCommandChat, btnChatX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, 0);

            // 4. Nút Mail
            g.drawImageScale(GameScr.imgMail, btnMailX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, 0);

            if (HasUnreadMail())
            {
                int badgeSize = GetMailBadgeSize();
                PaintMailBadge(g, btnMailX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, badgeSize);
            }

            // 5. Dragon Pass (icon tạm, có thể thay trực tiếp event.png sau này)
            Image passIcon = GameScr.imgDragonPass != null ? GameScr.imgDragonPass : GameScr.imgModFunc;
            g.drawImageScale(passIcon, btnPassX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, 0);
            if (DragonPass.Manager.Instance.Current.hasNotification)
            {
                int badgeSize = GetMailBadgeSize();
                PaintMailBadge(g, btnPassX + iconPadding, boxY + iconPadding, innerIconSize, innerIconSize, badgeSize);
            }
        }
        else
        {
            // Khi đóng Menu: Nút mở mũi tên đơn lẻ

            int arrowX = startMenuX + (boxSize - GameScr.imgArrow.getWidth()) / 2;
            int arrowY = boxY + (boxSize - GameScr.imgArrow.getHeight()) / 2;
            g.drawImage(GameScr.imgArrow, arrowX, arrowY, 0);

            if (HasUnreadMail() || DragonPass.Manager.Instance.Current.hasNotification)
            {
                int badgeSize = GetMailBadgeSize();
                PaintMailBadge(g, startMenuX, boxY, boxSize, boxSize, badgeSize);
            }
        }
        EndHudElement(showListPaintState);
        PaintPlayerTichXanh(g);
        if (isEditButton && GameScr.gI().popUpYesNo == null)
        {
            int requestX = GameCanvas.w - 160;
            int requestY = 45;
            if (GameCanvas.w - 50 > 275)
            {
                requestX = GameCanvas.w - 210;
                requestY = 5;
            }
            const int requestWidth = 155;
            const int requestHeight = 49;
            HudPaintState requestState = BeginHudElement(g, HUD_REQUEST, "Yêu cầu thách đấu / giao dịch", requestX, requestY, requestWidth, requestHeight);
            if (GameScr.imgNut != null)
            {
                g.drawImage(GameScr.imgNut, requestX, requestY, 0);
                mFont.tahoma_7b_dark.drawString(g, mResources.YES, requestX + mGraphics.getImageWidth(GameScr.imgNut) / 2, requestY + mGraphics.getImageHeight(GameScr.imgNut) / 2 - 5, mFont.CENTER);
            }
            int requestBoxX = requestX + 35;
            g.setColor(0, 0.65f);
            g.fillRect(requestBoxX, requestY, 120, 29);
            g.setColor(16776960);
            g.drawRect(requestBoxX, requestY, 120, 29);
            mFont.tahoma_7b_white.drawString(g, "Yêu cầu giao dịch / thách đấu", requestBoxX + 60, requestY + 9, mFont.CENTER);
            EndHudElement(requestState);
        }
        if (isAutoNoitai)
        {
            PaintNoiTai(g);
        }
        if (isShowFilterList)
        {
            ShowFilterList(g);
        }
    }

    private static bool IsPointerIn(int x, int y, int width, int height)
    {
        return GameCanvas.px >= x && GameCanvas.px <= x + width && GameCanvas.py >= y && GameCanvas.py <= y + height;
    }

    private static bool IsHudRegionAvailableForCurrentProfile(HudRegion region)
    {
        if (region == null)
        {
            return false;
        }
        if (GameScr.isAnalog != 0)
        {
            return true;
        }
        return region.id != HUD_FIRE && region.id != HUD_TARGET && region.id != HUD_JOYSTICK && region.id != HUD_CHAT && !region.id.StartsWith("mod_");
    }

    private static void StartHudEditor()
    {
        hudEditingVirtual = GameScr.isAnalog != 0;
        EnsureHudLayoutLoaded(hudEditingVirtual);
        hudDraft = CloneHudLayout(hudEditingVirtual ? hudVirtual : hudPc);
        selectedHudElement = null;
        hudDragging = false;
        hudActionsDragging = false;
        hudActionsMoved = false;
        isEditButton = true;
        GameCanvas.panel.isShow = false;
        GameScr.info1.addInfo("Tùy chỉnh giao diện " + (hudEditingVirtual ? "bàn phím ảo" : "PC"), 0);
    }

    private static void StopHudEditor(bool save)
    {
        if (save && hudDraft != null)
        {
            Dictionary<string, HudElementState> target = hudEditingVirtual ? hudVirtual : hudPc;
            target.Clear();
            foreach (KeyValuePair<string, HudElementState> pair in hudDraft)
            {
                target[pair.Key] = pair.Value.Clone();
            }
            SaveHudLayout(hudEditingVirtual, target);
            GameScr.info1.addInfo("Đã lưu giao diện " + (hudEditingVirtual ? "bàn phím ảo" : "PC"), 0);
        }
        isEditButton = false;
        hudDraft = null;
        selectedHudElement = null;
        hudDragging = false;
        hudActionsDragging = false;
        hudActionsMoved = false;
        GameCanvas.clearAllPointerEvent();
    }

    private static HudElementState GetHudEditorActionsState(out int totalWidth)
    {
        totalWidth = HUD_ACTION_BUTTON_WIDTH * 3 + HUD_ACTION_GAP * 2;
        HudElementState state = GetHudState(HUD_EDITOR_ACTIONS, GameCanvas.w - totalWidth - 8, 4);
        state.scale = 100;
        state.opacity = 100;
        ClampHudState(state, totalWidth, HUD_ACTION_BUTTON_HEIGHT);
        return state;
    }

    private static bool HandleHudEditorActionsInput()
    {
        int totalWidth;
        HudElementState state = GetHudEditorActionsState(out totalWidth);
        bool pointerInside = IsPointerIn(state.x, state.y, totalWidth, HUD_ACTION_BUTTON_HEIGHT);
        if (!hudActionsDragging && GameCanvas.isPointerDown && pointerInside)
        {
            hudActionsDragging = true;
            hudActionsMoved = false;
            hudActionsDragOffsetX = GameCanvas.px - state.x;
            hudActionsDragOffsetY = GameCanvas.py - state.y;
            hudActionsPressX = GameCanvas.px;
            hudActionsPressY = GameCanvas.py;
            GameCanvas.isPointerJustDown = false;
            return true;
        }
        if (!hudActionsDragging)
        {
            return false;
        }
        if (GameCanvas.isPointerDown)
        {
            if (System.Math.Abs(GameCanvas.px - hudActionsPressX) > 2 || System.Math.Abs(GameCanvas.py - hudActionsPressY) > 2)
            {
                hudActionsMoved = true;
            }
            state.x = GameCanvas.px - hudActionsDragOffsetX;
            state.y = GameCanvas.py - hudActionsDragOffsetY;
            ClampHudState(state, totalWidth, HUD_ACTION_BUTTON_HEIGHT);
            GameCanvas.isPointerJustDown = false;
            return true;
        }
        if (GameCanvas.isPointerJustRelease)
        {
            bool activateButton = !hudActionsMoved && GameCanvas.isPointerClick && pointerInside;
            hudActionsDragging = false;
            if (activateButton)
            {
                int relativeX = GameCanvas.px - state.x;
                int buttonIndex = relativeX / (HUD_ACTION_BUTTON_WIDTH + HUD_ACTION_GAP);
                bool overButton = relativeX % (HUD_ACTION_BUTTON_WIDTH + HUD_ACTION_GAP) < HUD_ACTION_BUTTON_WIDTH;
                if (overButton && buttonIndex == 0)
                {
                    StopHudEditor(true);
                    return true;
                }
                if (overButton && buttonIndex == 1)
                {
                    hudDraft.Clear();
                    selectedHudElement = null;
                    GameScr.info1.addInfo("Đã đưa giao diện hiện tại về mặc định", 0);
                }
                else if (overButton && buttonIndex == 2)
                {
                    StopHudEditor(false);
                    return true;
                }
            }
            GameCanvas.clearAllPointerEvent();
            return true;
        }
        return true;
    }

    private static void HandleHudEditorInput()
    {
        if (hudDraft == null || hudEditingVirtual != (GameScr.isAnalog != 0))
        {
            StartHudEditor();
        }
        if (HandleHudEditorActionsInput())
        {
            return;
        }

        HudRegion selectedRegion = null;
        if (selectedHudElement != null)
        {
            hudRegions.TryGetValue(selectedHudElement, out selectedRegion);
        }
        if (selectedRegion != null)
        {
            HudElementState state = GetHudState(selectedRegion.id, selectedRegion.sourceX, selectedRegion.sourceY);
            int arrowsY = GameCanvas.h - 54;
            int arrowsX = 4;
            const int arrowSize = 16;
            int step = 3;
            if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                if (IsPointerIn(arrowsX, arrowsY + 18, arrowSize, arrowSize)) state.x -= step;
                else if (IsPointerIn(arrowsX + 36, arrowsY + 18, arrowSize, arrowSize)) state.x += step;
                else if (IsPointerIn(arrowsX + 18, arrowsY, arrowSize, arrowSize)) state.y -= step;
                else if (IsPointerIn(arrowsX + 18, arrowsY + 36, arrowSize, arrowSize)) state.y += step;
                else goto sliders;
                ClampHudState(state, selectedRegion.width, selectedRegion.height);
                UpdateHudHorizontalAnchor(state, selectedRegion.width);
                GameCanvas.clearAllPointerEvent();
                return;
            }
        sliders:
            int sliderX = 64;
            int sliderW = System.Math.Min(120, System.Math.Max(60, GameCanvas.w - sliderX - 8));
            int scaleY = GameCanvas.h - 31;
            int opacityY = GameCanvas.h - 10;
            if (GameCanvas.isPointerDown && IsPointerIn(sliderX, scaleY - 5, sliderW, 14))
            {
                state.scale = 30 + (GameCanvas.px - sliderX) * 170 / sliderW;
                state.scale = System.Math.Max(30, System.Math.Min(200, state.scale));
                ApplyHudHorizontalAnchor(state, selectedRegion.width);
                ClampHudState(state, selectedRegion.width, selectedRegion.height);
                GameCanvas.isPointerJustDown = false;
                return;
            }
            if (GameCanvas.isPointerDown && IsPointerIn(sliderX, opacityY - 5, sliderW, 14))
            {
                state.opacity = 5 + (GameCanvas.px - sliderX) * 95 / sliderW;
                state.opacity = System.Math.Max(5, System.Math.Min(100, state.opacity));
                GameCanvas.isPointerJustDown = false;
                return;
            }
        }

        int hudActionWidth;
        HudElementState hudActionState = GetHudEditorActionsState(out hudActionWidth);
        bool overControls = IsPointerIn(hudActionState.x, hudActionState.y, hudActionWidth, HUD_ACTION_BUTTON_HEIGHT);
        if (selectedRegion != null)
        {
            int compactSliderW = System.Math.Min(120, System.Math.Max(60, GameCanvas.w - 72));
            overControls = overControls || IsPointerIn(4, GameCanvas.h - 54, 52, 54) || IsPointerIn(64, GameCanvas.h - 41, compactSliderW, 41);
        }
        if (GameCanvas.isPointerDown && !overControls)
        {
            if (!hudDragging)
            {
                HudRegion hit = null;
                foreach (KeyValuePair<string, HudRegion> pair in hudRegions)
                {
                    HudRegion region = pair.Value;
                    if (!IsHudRegionAvailableForCurrentProfile(region))
                    {
                        continue;
                    }
                    HudElementState state = GetHudState(region.id, region.sourceX, region.sourceY);
                    int width = System.Math.Max(1, region.width * state.scale / 100);
                    int height = System.Math.Max(1, region.height * state.scale / 100);
                    if (IsPointerIn(state.x, state.y, width, height))
                    {
                        hit = region;
                    }
                }
                if (hit != null)
                {
                    selectedHudElement = hit.id;
                    HudElementState state = GetHudState(hit.id, hit.sourceX, hit.sourceY);
                    hudDragOffsetX = GameCanvas.px - state.x;
                    hudDragOffsetY = GameCanvas.py - state.y;
                    hudDragging = true;
                    GameCanvas.isPointerJustDown = false;
                }
                else
                {
                    selectedHudElement = null;
                    hudDragging = false;
                    GameCanvas.isPointerJustDown = false;
                }
            }
            else if (selectedHudElement != null && hudRegions.TryGetValue(selectedHudElement, out selectedRegion))
            {
                HudElementState state = GetHudState(selectedRegion.id, selectedRegion.sourceX, selectedRegion.sourceY);
                state.x = GameCanvas.px - hudDragOffsetX;
                state.y = GameCanvas.py - hudDragOffsetY;
                ClampHudState(state, selectedRegion.width, selectedRegion.height);
                UpdateHudHorizontalAnchor(state, selectedRegion.width);
                GameCanvas.isPointerJustDown = false;
            }
            return;
        }
        if (hudDragging && GameCanvas.isPointerJustRelease)
        {
            hudDragging = false;
            GameCanvas.clearAllPointerEvent();
        }
    }

    private static Image GetModButtonImage(string buttonName)
    {
        switch (buttonName)
        {
            case "Capsule": return GameScr.imgCapsule;
            case "Fusion": return GameScr.imgFusion;
            case "Zone": return GameScr.imgChangeZone;
            case "MapLeft": return GameScr.imgNextLeft;
            case "MapCenter": return GameScr.imgNextCenter;
            case "MapRight": return GameScr.imgNextRight;
            default: return null;
        }
    }

    public static void PaintHudEditorOverlay(mGraphics g)
    {
        int totalWidth;
        HudElementState actionState = GetHudEditorActionsState(out totalWidth);
        string[] captions = { "Lưu", "Mặc định", "Hủy" };
        for (int i = 0; i < captions.Length; i++)
        {
            int x = actionState.x + i * (HUD_ACTION_BUTTON_WIDTH + HUD_ACTION_GAP);
            g.setColor(0, 0.78f);
            g.fillRect(x, actionState.y, HUD_ACTION_BUTTON_WIDTH, HUD_ACTION_BUTTON_HEIGHT);
            g.setColor(i == 0 ? 65280 : (i == 1 ? 16776960 : 16711680));
            g.drawRect(x, actionState.y, HUD_ACTION_BUTTON_WIDTH, HUD_ACTION_BUTTON_HEIGHT);
            mFont.tahoma_7b_white.drawString(g, captions[i], x + HUD_ACTION_BUTTON_WIDTH / 2, actionState.y + 5, mFont.CENTER);
        }
        foreach (KeyValuePair<string, HudRegion> pair in hudRegions)
        {
            HudRegion region = pair.Value;
            if (!IsHudRegionAvailableForCurrentProfile(region))
            {
                continue;
            }
            HudElementState state = GetHudState(region.id, region.sourceX, region.sourceY);
            int width = System.Math.Max(1, region.width * state.scale / 100);
            int height = System.Math.Max(1, region.height * state.scale / 100);
            g.setColor(region.id == selectedHudElement ? 16776960 : 12632256);
            g.drawRect(state.x - 1, state.y - 1, width + 2, height + 2);
            if (region.id == selectedHudElement)
            {
                mFont.tahoma_7_yellow.drawStringBorder(g, region.label, state.x, System.Math.Max(27, state.y - 12), mFont.LEFT, mFont.tahoma_7_grey);
            }
        }
        if (selectedHudElement == null || !hudRegions.TryGetValue(selectedHudElement, out HudRegion selectedRegion))
        {
            mFont.tahoma_7b_white.drawStringBorder(g, "Chạm vào thành phần cần chỉnh", 4, GameCanvas.h - 20, mFont.LEFT, mFont.tahoma_7_grey);
            return;
        }
        HudElementState selectedState = GetHudState(selectedRegion.id, selectedRegion.sourceX, selectedRegion.sourceY);
        int arrowsY = GameCanvas.h - 54;
        int arrowsX = 4;
        string[] arrows = { "<", "^", ">", "v" };
        int[] arrowX = { arrowsX, arrowsX + 18, arrowsX + 36, arrowsX + 18 };
        int[] arrowY = { arrowsY + 18, arrowsY, arrowsY + 18, arrowsY + 36 };
        for (int i = 0; i < 4; i++)
        {
            g.setColor(0, 0.75f);
            g.fillRect(arrowX[i], arrowY[i], 16, 16);
            g.setColor(16776960);
            g.drawRect(arrowX[i], arrowY[i], 16, 16);
            mFont.tahoma_7b_white.drawString(g, arrows[i], arrowX[i] + 8, arrowY[i] + 3, mFont.CENTER);
        }
        int sliderX = 64;
        int sliderW = System.Math.Min(120, System.Math.Max(60, GameCanvas.w - sliderX - 8));
        PaintHudSlider(g, "Cỡ " + selectedState.scale + "%", sliderX, GameCanvas.h - 31, sliderW, selectedState.scale - 30, 170);
        PaintHudSlider(g, "Mờ " + selectedState.opacity + "%", sliderX, GameCanvas.h - 10, sliderW, selectedState.opacity - 5, 95);
    }

    private static void PaintHudSlider(mGraphics g, string caption, int x, int y, int width, int value, int maximum)
    {
        mFont.tahoma_7b_white.drawStringBorder(g, caption, x, y - 11, mFont.LEFT, mFont.tahoma_7_grey);
        g.setColor(4210752);
        g.fillRect(x, y, width, 5);
        g.setColor(16776960);
        int fill = System.Math.Max(0, System.Math.Min(width, value * width / maximum));
        g.fillRect(x, y, fill, 5);
        g.fillRect(x + fill - 2, y - 3, 5, 11);
    }

    private void PaintNoiTai(mGraphics g)
    {
        isLogo = false;
        int padding = 5;
        int boxHeight = 15;
        int boxWidth = GameCanvas.w / 2;
        int num = (GameCanvas.w - boxWidth) / 2;
        if (!string.IsNullOrEmpty(currentPlayerNoiTai))
        {
            int y = padding;
            string intrinsicInfo = $"NT: {currentPlayerNoiTai} [{CurrentParamNoitaiPlayer}%]";
            mFont.tahoma_7_yellow.drawStringBorder(g, intrinsicInfo, GameCanvas.w / 2, y + 27, mFont.CENTER, mFont.tahoma_7_grey);
        }
        if (!string.IsNullOrEmpty(CurrentNoiTai))
        {
            int y2 = padding * 2 + boxHeight;
            string autoInfo = $"Auto: {CurrentNoiTai} → {ChiSoNoiTai}%";
            mFont.tahoma_7_red.drawStringBorder(g, autoInfo, GameCanvas.w / 2, y2 + 30, mFont.CENTER, mFont.tahoma_7_grey);
        }
        int buttonX = num + boxWidth + 5;
        int buttonY = padding * 2 + boxHeight * 2 + 10;
        g.drawImage(imgCloseButton, buttonX - imgCloseButton.getWidth() / 2 - 12, buttonY - imgCloseButton.getHeight() / 2);
        if (GameCanvas.isPointerHoldIn(buttonX - imgCloseButton.getWidth() / 2 - 12, buttonY - imgCloseButton.getHeight() / 2, imgCloseButton.getWidth(), imgCloseButton.getHeight()))
        {
            g.setColor(16711680, 0.2f);
            g.fillRect(buttonX - imgCloseButton.getWidth() / 2 - 12, buttonY - imgCloseButton.getHeight() / 2, imgCloseButton.getWidth(), imgCloseButton.getHeight());
            if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                isAutoNoitai = false;
                ChiSoNoiTai = -1;
                curSelectIntrinsic = "";
                CurrentNoiTai = "";
                GameScr.info1.addInfo("Đã dừng auto mở nội tại", 0);
                GameCanvas.clearAllPointerEvent();
            }
        }
    }

    private void PaintCharInMap(mGraphics g)
    {
        charsInMap.removeAllElements();
        List<Char> displayChars = new List<Char>();
        List<string> displayTexts = new List<string>();
        List<Char> candidates = new List<Char>();
        HashSet<int> candidateIds = new HashSet<int>();
        long now = mSystem.currentTimeMillis();
        if (snapshotMapId != TileMap.mapID || snapshotZoneId != TileMap.zoneID)
        {
            mapPlayerSnapshots.Clear();
            snapshotMapId = TileMap.mapID;
            snapshotZoneId = TileMap.zoneID;
        }
        Char me = Char.myCharz();
        if (me != null && candidateIds.Add(me.charID))
        {
            candidates.Add(me);
        }
        for (int i = 0; i < GameScr.vCharInMap.size(); i++)
        {
            Char candidate = (Char)GameScr.vCharInMap.elementAt(i);
            if (candidate != null)
            {
                mapPlayerSnapshots[candidate.charID] = new MapPlayerSnapshot
                {
                    character = candidate,
                    lastSeen = now
                };
                if (candidateIds.Add(candidate.charID))
                {
                    candidates.Add(candidate);
                }
            }
        }
        List<int> expiredSnapshotIds = new List<int>();
        foreach (KeyValuePair<int, MapPlayerSnapshot> pair in mapPlayerSnapshots)
        {
            MapPlayerSnapshot snapshot = pair.Value;
            if (snapshot == null || snapshot.character == null || now - snapshot.lastSeen > MAP_PLAYER_GRACE_MS)
            {
                expiredSnapshotIds.Add(pair.Key);
            }
            else if (candidateIds.Add(pair.Key))
            {
                candidates.Add(snapshot.character);
            }
        }
        for (int i = 0; i < expiredSnapshotIds.Count; i++)
        {
            mapPlayerSnapshots.Remove(expiredSnapshotIds[i]);
        }
        int totalCount = 0;
        for (int i = 0; i < candidates.Count; i++)
        {
            Char char6 = candidates[i];
            if (char6 == null)
            {
                continue;
            }
            string lowerName = char6.cName != null ? char6.cName.Trim().ToLowerInvariant() : string.Empty;
            bool isCurrentPlayer = me != null && (object.ReferenceEquals(char6, me) || char6.charID == me.charID);
            bool isPlayer = isCurrentPlayer || (char6.charID > 0 && !char6.isMob && !char6.isMiniPet && !char6.isPet
                && lowerName.Length > 0 && lowerName != "trọng tài" && !lowerName.StartsWith("$") && !lowerName.StartsWith("#"));
            if (isPlayer)
            {
                totalCount++;
                if (displayChars.Count >= 16)
                {
                    continue;
                }
                int displayIndex = displayChars.Count;
                string[] str = new string[9]
                {
                    (displayIndex + 1 < 10) ? "0" : "",
                    (displayIndex + 1).ToString(),
                    ".[",
                    CharGender(char6),
                    "] ",
                    lowerName.Length > 0 ? char6.cName : "Bản thân",
                    " [ ",
                    NinjaUtil.getMoneys((long)char6.cHP).ToString(),
                    " ]"
                };
                displayChars.Add(char6);
                displayTexts.Add(string.Concat(str));
            }
        }
        if (totalCount == 0)
        {
            return;
        }
        string title = "Số người: " + totalCount;
        int boxWidth = mFont.tahoma_7b_white.getWidth(title);
        for (int i = 0; i < displayTexts.Count; i++)
        {
            boxWidth = System.Math.Max(boxWidth, mFont.tahoma_7_white.getWidth(displayTexts[i]));
        }
        string overflowText = totalCount > displayChars.Count ? totalCount + " ..." : null;
        if (overflowText != null)
        {
            boxWidth = System.Math.Max(boxWidth, mFont.tahoma_7_white.getWidth(overflowText));
        }
        boxWidth += 8;
        int rowCount = displayTexts.Count + (overflowText != null ? 1 : 0);
        int boxHeight = 16 + System.Math.Max(1, rowCount) * 10;
        int boxX = GameCanvas.w - boxWidth - 2;
        int boxY = notifBoss ? 47 + System.Math.Max(1, activeBossNotif.size()) * 10 : 50;
        if (notifBoss)
        {
            KeepHudElementBelow(HUD_PLAYERS, boxX, boxY, boxWidth, boxHeight, HUD_BOSS, 4);
        }
        HudPaintState playerPaintState = BeginHudElement(g, HUD_PLAYERS, "Người chơi trong map", boxX, boxY, boxWidth, boxHeight);
        g.setColor(16776960);
        g.drawRect(boxX, boxY, boxWidth, boxHeight);
        mFont.tahoma_7b_white.drawStringBorder(g, title, GameCanvas.w - 4, boxY + 2, mFont.RIGHT, mFont.tahoma_7_grey);
        int rowY = boxY + 13;
        for (int i = 0; i < displayChars.Count; i++)
        {
            Char player = displayChars[i];
            g.fillRect(boxX + 2, rowY + 1, boxWidth - 4, 10, 2721889, 90);
            mFont font = mFont.tahoma_7_white;
            if (player == Char.myCharz().charFocus) font = mFont.tahoma_7_yellow;
            else if (player.charID < 0 && player.charID > -1000 && player.charID != -114) font = mFont.tahoma_7_red;
            else if (Char.myCharz().clan != null && player.clanID == Char.myCharz().clan.ID) font = mFont.tahoma_7_green;
            font.drawStringBorder(g, displayTexts[i], GameCanvas.w - 4, rowY, mFont.RIGHT, mFont.tahoma_7_grey);
            charsInMap.addElement(player);
            rowY += 10;
        }
        if (overflowText != null)
        {
            g.fillRect(boxX + 2, rowY + 1, boxWidth - 4, 10, 2721889, 90);
            mFont.tahoma_7_white.drawStringBorder(g, overflowText, GameCanvas.w - 4, rowY, mFont.RIGHT, mFont.tahoma_7_grey);
        }
        EndHudElement(playerPaintState);
    }



    private void PaintListInfo(mGraphics g)
    {
        int num4 = 70;
        Char charFocus = Char.myCharz().charFocus;
        if (charFocus != null && Char.myCharz().isMeCanAttackOtherPlayer(charFocus))
        {
              int healthBarWidth = 128;
              int healthBarHeight = 10;
            int healthBarX = GameCanvas.w / 2 - healthBarWidth / 2;
            int healthBarY = num4;
            g.setColor(8421504);
            g.fillRect(healthBarX - 3, healthBarY - 3, healthBarWidth + 6, healthBarHeight + 6, 12);
            g.setColor(2829099);
            g.fillRect(healthBarX - 1, healthBarY - 1, healthBarWidth + 2, healthBarHeight + 2, 10);
            float healthPercentage = (float)((double)(float)charFocus.cHP / charFocus.cHPFull);
            int currentHealthBarWidth = (int)((float)healthBarWidth * healthPercentage);
            if (healthPercentage > 0.5f)
            {
                g.setColor(65280);
            }
            else if (healthPercentage > 0.25f)
            {
                g.setColor(16776960);
            }
            else
            {
                g.setColor(16711680);
            }
            g.fillRect(healthBarX, healthBarY, currentHealthBarWidth, healthBarHeight, 8);
            string hpText = NinjaUtil.lamtronDouble(charFocus.cHP) + "/" + NinjaUtil.getMoneys((long)charFocus.cHPFull);
            mFont.tahoma_7b_white.drawStringBorder(g, hpText, GameCanvas.w / 2 + 1, healthBarY + healthBarHeight / 2 - 6, mFont.CENTER, mFont.tahoma_7_grey);
            num4 += 17;
            if (charFocus.protectEff)
            {
                mFont.tahoma_7b_red.drawString(g, "Đang khiên năng lượng", GameCanvas.w / 2, num4, mFont.CENTER);
                num4 += 10;
            }
            if (charFocus.isMonkey == 1)
            {
                mFont.tahoma_7b_red.drawString(g, "Đang biến khỉ", GameCanvas.w / 2, num4, mFont.CENTER);
                num4 += 10;
            }
            if (charFocus.sleepEff)
            {
                mFont.tahoma_7b_red.drawString(g, "Bị thôi miên", GameCanvas.w / 2, num4, mFont.CENTER);
                num4 += 10;
            }
            if (charFocus.holdEffID != 0)
            {
                mFont.tahoma_7b_red.drawString(g, "Bị trói", GameCanvas.w / 2, num4, mFont.CENTER);
                num4 += 10;
            }
            if (charFocus.isFreez)
            {
                mFont.tahoma_7b_red.drawString(g, "Bị TDHS: " + charFocus.freezSeconds, GameCanvas.w / 2, num4, mFont.CENTER);
                num4 += 10;
            }
            if (charFocus.blindEff)
            {
                mFont.tahoma_7b_red.drawString(g, "Bị choáng", GameCanvas.w / 2, num4, mFont.CENTER);
            }
        }
    }

    private void PaintInfoMe(mGraphics g, int xText, int yText)
    {
        if (mSystem.currentTimeMillis() - lastUpdateInfoMe > 3000)
        {
            Service.gI().petInfo();
            lastUpdateInfoMe = mSystem.currentTimeMillis();
        }
        int num = 10;
        int numy = 64;
        mFont.tahoma_7b_yellow.drawStringBorder(g, "Sư Phụ :", xText, yText, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "SM: " + NinjaUtil.getMoneys(Char.myCharz().cPower), xText, yText + num, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "TN: " + NinjaUtil.getMoneys(Char.myCharz().cTiemNang), xText, yText + 2 * num, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "SĐ: " + NinjaUtil.getMoneys((long)Char.myCharz().cDamFull), xText, yText + 3 * num, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "Giáp: " + NinjaUtil.getMoneys((long)Char.myCharz().cDefull), xText, yText + 4 * num, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7b_yellow.drawStringBorder(g, "Đệ Tử :", xText, yText + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "SM: " + NinjaUtil.getMoneys(Char.myPetz().cPower), xText, yText + num + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "TN: " + NinjaUtil.getMoneys(Char.myPetz().cTiemNang), xText, yText + 2 * num + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "SĐ: " + NinjaUtil.getMoneys((long)Char.myPetz().cDamFull), xText, yText + 3 * num + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "HP : " + NinjaUtil.getMoneys((long)Char.myPetz().cHP), xText, yText + 4 * num + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "MP : " + NinjaUtil.getMoneys((long)Char.myPetz().cMP), xText, yText + 5 * num + numy, mFont.LEFT, mFont.tahoma_7_grey);
        mFont.tahoma_7_white.drawStringBorder(g, "Giáp: " + NinjaUtil.getMoneys((long)Char.myPetz().cDefull), xText, yText + 6 * num + numy, mFont.LEFT, mFont.tahoma_7_grey);
    }

    public void perform(int idAction, object p)
    {
        if (idAction <= 60)
        {
            if (idAction <= 8)
            {
                switch (idAction)
                {
                    default:
                        _ = 8;
                        break;
                    case 1:
                        {
                            string notif;
                            if (int.TryParse((string)p, out var mapId))
                            {
                                XmapController.StartRunToMapId(mapId);
                                notif = "Di chuyển đến boss ở MAP " + mapId;
                            }
                            else
                            {
                                notif = "Địa điểm không hợp lệ!";
                            }
                            GameScr.info1.addInfo(notif, 0);
                            break;
                        }
                    case 2:
                        GameScr.info1.addInfo("Đã huỷ di chuyển đến Boss", 0);
                        break;
                }
                return;
            }
            switch (idAction)
            {
                case 16:
                    {
                        MyVector menuPet = new MyVector();
                        menuPet.addElement(new Command(isPeanPet ? "Buff đậu cho đệ [Bật]" : "Buff đậu cho đệ [Tắt]", 17));
                        GameCanvas.menu.startAt(menuPet, 4);
                        break;
                    }
                case 17:
                    isPeanPet = !isPeanPet;
                    GameScr.info1.addInfo("Buff đậu cho đệ " + (isPeanPet ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 32:
                    {
                        MyVector myVector2 = new MyVector();
                        myVector2.addElement(new Command(notifBoss ? "Thông báo BOSS [Bật]" : "Thông báo BOSS [Tắt]", 46));
                        myVector2.addElement(new Command(notifKillBoss ? "Thông báo tiêu diệt BOSS [Bật]" : "Thông báo tiêu diệt BOSS [Tắt]", 58));
                        myVector2.addElement(new Command(lineToBoss ? "Kẻ đường tới BOSS [Bật]" : "Đường kẻ tới BOSS [Tắt]", 47));
                        myVector2.addElement(new Command(focusBoss ? "Focus BOSS [Bật]" : "Focus BOSS [Tắt]", 52));
                        GameCanvas.menu.startAt(myVector2, 4);
                        break;
                    }
                case 38:
                    PickMob.mapGoback = TileMap.mapID;
                    PickMob.zoneGoback = TileMap.zoneID;
                    PickMob.xGoback = Char.myCharz().cx;
                    PickMob.yGoback = Char.myCharz().cy;
                    PickMob.isGoBack = !PickMob.isGoBack;
                    if (PickMob.isGoBack)
                    {
                        GameScr.info1.addInfo("Map Goback: " + TileMap.mapName + " | Khu: " + TileMap.zoneID, 0);
                        GameScr.info1.addInfo("Tọa độ X: " + PickMob.xGoback + " | Y: " + PickMob.yGoback, 0);
                        if (Char.myCharz().cHP <= 0.0 || Char.myCharz().statusMe == 14)
                        {
                            Service.gI().returnTownFromDead();
                            new Thread(PickMob.GoBack).Start();
                        }
                    }
                    GameScr.info1.addInfo("Goback tọa độ " + (PickMob.isGoBack ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 42:
                    autoAttack = !autoAttack;
                    GameScr.info1.addInfo("Tự đánh " + (autoAttack ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 43:
                    PickMob.neSieuQuai = !PickMob.neSieuQuai;
                    GameScr.info1.addInfo("Né siêu quái " + (PickMob.neSieuQuai ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 44:
                    PickMob.tsPlayer = false;
                    PickMob.tanSat = ((p != null) ? ((bool)p) : (!PickMob.tanSat));
                    GameScr.info1.addInfo("Tàn sát " + (PickMob.tanSat ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 45:
                    {
                        MyVector myVector = new MyVector();
                        MyVector mobIds = new MyVector();
                        for (int i = 0; i < GameScr.vMob.size(); i++)
                        {
                            Mob mob = (Mob)GameScr.vMob.elementAt(i);
                            if (GameScr.gI().isMeCanAttackMob(mob) && !mobIds.contains(mob.templateId) && !PickMob.TypeMobsTanSat.Contains(mob.templateId))
                            {
                                mobIds.addElement(mob.templateId);
                                myVector.addElement(new Command("Tàn sát " + mob.getTemplate().name, 49, mob));
                            }
                        }
                        myVector.addElement(new Command(PickMob.tanSat ? "Tàn sát [Bật]" : "Tàn sát [Tắt]", 44));
                        myVector.addElement(new Command(PickMob.tsPlayer ? "Tàn sát\nngười [Bật]" : "Tàn sát\nngười [Tắt]", 48));
                        myVector.addElement(new Command(autoAttack ? "Tự đánh [Bật]" : "Tự đánh [Tắt]", 42));
                        myVector.addElement(new Command(PickMob.neSieuQuai ? "Né siêu quái [Bật]" : "Né siêu quái [Tắt]", 43));
                        myVector.addElement(new Command(PickMob.vuotDiaHinh ? "Vượt địa hình [Bật]" : "Vượt địa hình [Tắt]", 76));
                        myVector.addElement(new Command(PickMob.telePem ? "Dịch chuyển\n[Bật]" : "Dịch chuyển\n[Tắt]", 80));
                        myVector.addElement(new Command(PickMob.isGoBack ? "Goback Tọa Độ [Bật]" : "Goback Tọa Độ [Tắt]", 38));
                        myVector.addElement(new Command("Xoá danh sách tàn sát", 51));
                        GameCanvas.menu.startAt(myVector, 4);
                        break;
                    }
                case 46:
                    notifBoss = !notifBoss;
                    GameScr.info1.addInfo("Thông báo BOSS " + (notifBoss ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 47:
                    lineToBoss = !lineToBoss;
                    GameScr.info1.addInfo("Kẻ đường tới BOSS " + (lineToBoss ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 48:
                    PickMob.tanSat = false;
                    PickMob.tsPlayer = ((p != null) ? ((bool)p) : (!PickMob.tsPlayer));
                    GameScr.info1.addInfo("Tàn sát người " + (PickMob.tsPlayer ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 49:
                    {
                        Mob mobType = (Mob)p;
                        if (!PickMob.TypeMobsTanSat.Contains(mobType.templateId))
                        {
                            PickMob.TypeMobsTanSat.Add(mobType.templateId);
                        }
                        GameScr.info1.addInfo("Tàn sát " + mobType.getTemplate().name, 0);
                        perform(44, true);
                        break;
                    }
                case 51:
                    PickMob.TypeMobsTanSat.Clear();
                    GameScr.info1.addInfo("Đã xoá danh sách quái tàn sát!", 0);
                    break;
                case 52:
                    focusBoss = !focusBoss;
                    GameScr.info1.addInfo("Focus BOSS " + (focusBoss ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 53:
                    {
                        MyVector menuOthers = new MyVector();
                        menuOthers.addElement(new Command("Tốc độ\nGame", 54));
                        menuOthers.addElement(new Command("Tự động\nChat " + (isAutoChat ? "[Bật]" : "[Tắt]"), 55));
                        menuOthers.addElement(new Command("Tự động\nChat Thế\nGiới " + (isAutoChatTG ? "[Bật]" : "[Tắt]"), 56));
                        menuOthers.addElement(new Command("Load ô\nskill", 57));
                        menuOthers.addElement(new Command(isPlayingMusic ? "Tắt nhạc" : "Bật nhạc", 60));
                        GameCanvas.menu.startAt(menuOthers, 4);
                        break;
                    }
                case 54:
                    MyChatTextField(ChatTextField.gI(), "Nhập tốc độ game", "1 đến 10");
                    break;
                case 55:
                    isAutoChat = !isAutoChat;
                    GameScr.info1.addInfo("Tự động chat " + (isAutoChat ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 56:
                    isAutoChatTG = !isAutoChatTG;
                    GameScr.info1.addInfo("Tự động chat thế giới " + (isAutoChatTG ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 57:
                    LoadSkillToScreen();
                    GameScr.info1.addInfo("Đã load ô skill", 0);
                    break;
                case 58:
                    notifKillBoss = !notifKillBoss;
                    GameScr.info1.addInfo("Thông báo tiêu diệt BOSS " + (notifKillBoss ? "[Bật]" : "[Tắt]"), 0);
                    break;
                case 60:
                    Sound.PlayMusic(UnityEngine.Random.Range(0, 3));
                    Debug.Log("Music " + musics.Count);
                    GameScr.info1.addInfo("Đã bật trình phát nhạc", 0);
                    break;
            }
            return;
        }
        switch (idAction)
        {
            case 76:
                PickMob.vuotDiaHinh = !PickMob.vuotDiaHinh;
                GameScr.info1.addInfo("Vượt địa hình " + (PickMob.vuotDiaHinh ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 80:
                PickMob.telePem = !PickMob.telePem;
                GameScr.info1.addInfo("Dịch chuyển đến quái\n" + (PickMob.telePem ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 89:
                {
                    MyVector menuAutoPick = new MyVector();
                    menuAutoPick.addElement(new Command("Tự động nhặt " + (PickMob.IsAutoPickItems ? "[Bật]" : "[Tắt]"), 90));
                    menuAutoPick.addElement(new Command("Nhặt tất cả " + (PickMob.IsPickItemsAll ? "[Bật]" : "[Tắt]"), 91));
                    menuAutoPick.addElement(new Command("Nhặt xa\n" + (PickMob.IsPickItemsDis ? "[Bật]" : "[Tắt]"), 92));
                    menuAutoPick.addElement(new Command("Xem DS lọc đồ", 93));
                    menuAutoPick.addElement(new Command("Tự động lọc đồ", 94));
                    GameCanvas.menu.startAt(menuAutoPick, 4);
                    break;
                }
            case 90:
                PickMob.IsAutoPickItems = !PickMob.IsAutoPickItems;
                GameScr.info1.addInfo("Tự động nhặt " + (PickMob.IsAutoPickItems ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 91:
                PickMob.IsPickItemsAll = !PickMob.IsPickItemsAll;
                GameScr.info1.addInfo("Nhặt tất cả " + (PickMob.IsPickItemsAll ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 92:
                PickMob.IsPickItemsDis = !PickMob.IsPickItemsDis;
                GameScr.info1.addInfo("Nhặt xa " + (PickMob.IsPickItemsDis ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 93:
                isShowFilterList = !isShowFilterList;
                GameScr.info1.addInfo("Đã mở danh sách lọc đồ", 0);
                break;
            case 94:
                isAutoFilterItem = !isAutoFilterItem;
                GameScr.info1.addInfo("Tự động lọc đồ " + (isAutoFilterItem ? "[Bật]" : "[Tắt]"), 0);
                break;
            case 100:
                {
                    string obj = (string)p;
                    int.TryParse(obj.Split("-")[0], out indexAutoPoint);
                    bool.TryParse(obj.Split("-")[1], out autoPointForPet);
                    GameCanvas.panel.hideNow();
                    MyChatTextField(ChatTextField.gI(), "Tăng đến mức", "VD: 220000");
                    break;
                }
            case 101:
                isOpenAccMAnager = true;
                break;
            case 102:
                {
                    Account account = (Account)p;
                    Rms.saveRMSString("acc", account.getUsername());
                    Rms.saveRMSString("pass", account.getPassword());

                    GameCanvas.acc = account.getUsername();
                    GameCanvas.pass = account.getPassword();
                    if (GameCanvas.loginScr != null)
                    {
                        GameCanvas.loginScr.setCurrentTabAccount(account.getUsername(), account.getPassword());
                        if (GameCanvas.currentScreen == GameCanvas.loginScr)
                        {
                            GameCanvas.loginScr.setUserPass();
                        }
                    }
                    isOpenAccMAnager = false;
                    break;
                }
            case 103:
                {
                    Account accToDelete = (Account)p;
                    int index = -1;
                    for (int i = 0; i < accounts.Count; i++)
                    {
                        if (accounts[i].getUsername() == accToDelete.getUsername() && 
                            accounts[i].getPassword() == accToDelete.getPassword())
                        {
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0)
                    {
                        accounts.RemoveAt(index);
                        cmdsChooseAcc.RemoveAt(index);
                        cmdsDelAcc.RemoveAt(index);
                        cmdsEditAcc.RemoveAt(index);
                        SaveAcc();
                        LoadAcc();
                    }
                    break;
                }
            case 104:
                isOpenAccMAnager = false;
                break;
            case 105:
                {
                    object[] data = (object[])p;
                    int index = (int)data[0];
                    Account oldAccount = (Account)data[1];
                    if (index >= 0 && index < accounts.Count)
                    {
                        Rms.saveRMSInt("editAccIndex", index);
                        string hint = "Nhập: username$password";
                        string defaultValue = oldAccount.getUsername() + "$" + oldAccount.getPassword();
                        MyChatTextField(ChatTextField.gI(), "Sửa tài khoản", defaultValue);
                        Rms.saveRMSInt("isEditingAcc", 1);
                    }
                    break;
                }
                break;
            case 500:
            case 501:
                AddOrRemoveAutoItem((Item)p, idAction == 500);
                break;
            case 883:
                XmapController.ShowXmapMenu();
                break;
            case 502:
            case 503:
                AddOrRemoveFilterItem((Item)p, idAction == 502);
                break;
        }
    }

    public void AutoBuyItem(int num, Item itemBuy)
    {
        new Thread(new ThreadStart(delegate
        {
            for (int i = 0; i < num; i++)
            {
                Service.gI().buyItem(3, itemBuy.template.id, 0);
                Thread.Sleep(200);
            }
            GameScr.info1.addInfo("Đã mua xong " + num + " " + itemBuy.template.name, 0);
        })).Start();
    }

    private void AddOrRemoveAutoItem(Item item, bool isAdd)
    {
        if (isAdd)
        {
            listItemAuto.Add(new ItemAuto(item.template.iconID, item.template.id));
            GameScr.info1.addInfo("Đã thêm " + item.template.name + " vào Auto Item", 0);
            return;
        }
        foreach (ItemAuto itemAuto in listItemAuto)
        {
            if (itemAuto.iconID == item.template.iconID && itemAuto.id == item.template.id)
            {
                listItemAuto.Remove(itemAuto);
                GameScr.info1.addInfo("Đã xóa " + item.template.name + " khỏi Auto Item", 0);
                break;
            }
        }
    }

    public void DoDoubleClickToObj(IMapObject obj)
    {
        if ((obj.Equals(Char.myCharz().npcFocus) || GameScr.gI().mobCapcha == null) && !GameScr.gI().checkClickToBotton(obj))
        {
            GameScr.gI().checkEffToObj(obj, isnew: false);
            Char.myCharz().cancelAttack();
            Char.myCharz().currentMovePoint = null;
            Char.myCharz().cvx = (Char.myCharz().cvy = 0);
            obj.stopMoving();
            GameScr.gI().auto = 10;
            GameScr.gI().doFire(isFireByShortCut: false, skipWaypoint: true);
            GameScr.gI().clickToX = obj.getX();
            GameScr.gI().clickToY = obj.getY();
            GameScr.gI().clickOnTileTop = false;
            GameScr.gI().clickMoving = true;
            GameScr.gI().clickMovingRed = true;
            GameScr.gI().clickMovingTimeOut = 20;
            GameScr.gI().clickMovingP1 = 30;
        }
    }

    public void MyChatTextField(ChatTextField chatTField, string strChat, string strName)
    {
        chatTField.strChat = strChat;
        chatTField.tfChat.name = strName;
        chatTField.tfChat.setText(strName);
        chatTField.to = string.Empty;
        chatTField.isShow = true;
        chatTField.tfChat.isFocus = true;
        if (strChat == "Sửa tài khoản")
        {
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            chatTField.tfChat.setMaxTextLenght(100);
        }
        else
        {
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            chatTField.tfChat.setMaxTextLenght(10);
        }
        if (!Main.isPC)
        {
            chatTField.startChat(GameCanvas.panel, string.Empty);
        }
        else if (GameCanvas.isTouch)
        {
            chatTField.tfChat.doChangeToTextBox();
        }
    }

    public void ChangeGameSpeed(string strSpeed)
    {
        if (int.TryParse(strSpeed, out var speed) && speed > 0 && speed <= 10)
        {
            Time.timeScale = speed;
            GameScr.info1.addInfo("Tốc độ game: " + speed, 0);
        }
        else
        {
            GameScr.info1.addInfo("Chỉ nhập số từ 1 đến 10", 0);
        }
    }

    public void TeleportToPlayer(int charID)
    {
        Service.gI().gotoPlayer(charID);
    }

    public void AddNotifTichXanh(string notif)
    {
        listNotifTichXanh.addElement(notif);
        if (!startChat)
        {
            int halfW = GameCanvas.w / 2;
            startChat = true;
            xNotif = halfW + halfW / 2;
            lastUpdateNotif = mSystem.currentTimeMillis();
        }
    }

    private void PaintPlayerTichXanh(mGraphics g)
    {
        if (listNotifTichXanh.size() != 0)
        {
            string st = (string)listNotifTichXanh.elementAt(0);
            int halfW = GameCanvas.w / 2;
            g.setClip(halfW - halfW / 3, 50, halfW / 3 * 2, 12);
            g.fillRect(halfW - halfW / 3, 50, halfW / 3 * 2, 12, 0, 60);
            mFont.tahoma_7_yellow.drawStringBorder(g, st, xNotif, 50, 0, mFont.tahoma_7_grey);
            PaintTicks(g, xNotif - 12, 51);
        }
    }

    private void UpdateNotifTichXanh()
    {
        if (!startChat || mSystem.currentTimeMillis() - lastUpdateNotif < 10)
        {
            return;
        }
        xNotif--;
        string strChat = (string)listNotifTichXanh.elementAt(0);
        lastUpdateNotif = mSystem.currentTimeMillis();
        if (xNotif < GameCanvas.w / 2 - 100 - mFont.tahoma_7_yellow.getWidth(strChat))
        {
            xNotif = GameCanvas.w / 2 + 100;
            listNotifTichXanh.removeElementAt(0);
            if (listNotifTichXanh.size() == 0)
            {
                startChat = false;
            }
        }
    }

    public void SetAutoIntrinsic(int param)
    {
        if (string.IsNullOrEmpty(curSelectIntrinsic))
        {
            GameScr.info1.addInfo("Vui lòng chọn nội tại trước!", 0);
            return;
        }
        if (!int.TryParse(curSelectIntrinsic.Split("đến ")[1].Split("%")[0], out var maxParam))
        {
            GameScr.info1.addInfo("Có lỗi xảy ra khi đọc chỉ số!", 0);
            return;
        }
        if (param <= 0 || param > maxParam)
        {
            GameScr.info1.addInfo($"Chỉ số phải từ 1 đến {maxParam}!", 0);
            return;
        }
        ChiSoNoiTai = param;
        CurrentNoiTai = curSelectIntrinsic.Split(new string[2] { "+", "dưới " }, StringSplitOptions.RemoveEmptyEntries)[0].Trim();
        if (string.IsNullOrEmpty(CurrentNoiTai))
        {
            GameScr.info1.addInfo("Có lỗi xảy ra khi xử lý nội tại!", 0);
            return;
        }
        isAutoNoitai = true;
        new Thread(DoAutoNoitai).Start();
    }

    private void DoAutoNoitai()
    {
        try
        {
            while (ChiSoNoiTai != -1 && isAutoNoitai)
            {
                if (string.IsNullOrEmpty(currentPlayerNoiTai))
                {
                    Thread.Sleep(500);
                    continue;
                }
                if (currentPlayerNoiTai.Contains(CurrentNoiTai) && CurrentParamNoitaiPlayer >= ChiSoNoiTai)
                {
                    GameScr.info1.addInfo($"Đã đạt nội tại {CurrentNoiTai} +{CurrentParamNoitaiPlayer}%", 0);
                    isAutoNoitai = false;
                    ChiSoNoiTai = -1;
                    curSelectIntrinsic = "";
                    CurrentNoiTai = "";
                    break;
                }
                Thread thread = new Thread(new ThreadStart(delegate
                {
                    try
                    {
                        Service.gI().speacialSkill(0);
                        Thread.Sleep(100);
                        Service.gI().confirmMenu(5, 2);
                        Thread.Sleep(100);
                        Service.gI().confirmMenu(5, 0);
                    }
                    catch (Exception exception)
                    {
                        Debug.LogException(exception);
                    }
                }));
                thread.Start();
                thread.Join();
                Thread.Sleep(200);
            }
        }
        catch (Exception exception2)
        {
            Debug.LogException(exception2);
            isAutoNoitai = false;
            ChiSoNoiTai = -1;
            CurrentNoiTai = "";
        }
    }

    public void SetIncreasePoint(string strPoint)
    {
        if (int.TryParse(strPoint, out var point) && indexAutoPoint != -1 && point > 0)
        {
            pointIncrease = point;
            new Thread(DoAutoIncreasePoint).Start();
            GameScr.info1.addInfo("Tự động tăng " + strPointTypes[indexAutoPoint] + " đến " + point, 0);
        }
        else
        {
            GameScr.info1.addInfo("Có lỗi xảy ra (100)", 0);
        }
    }

    private void DoAutoIncreasePoint()
    {
        while (indexAutoPoint != -1 && pointIncrease > 0)
        {
            Char @char = (autoPointForPet ? Char.myPetz() : Char.myCharz());
            if (indexAutoPoint switch
            {
                0 => @char.cHPGoc,
                1 => @char.cMPGoc,
                2 => @char.cDamGoc,
                3 => @char.cDefGoc,
                4 => @char.cCriticalGoc,
                _ => 0.0,
            } >= (double)pointIncrease)
            {
                indexAutoPoint = -1;
                pointIncrease = 0;
                GameScr.info1.addInfo("Đã đạt chỉ số yêu cầu", 0);
                break;
            }
            Service.gI().upPotential(autoPointForPet, indexAutoPoint, 100);
            Thread.Sleep(500);
        }
    }

    public void LoadAcc()
    {
        string text = Rms.loadRMSString("accManager");
        accounts.Clear();
        cmdsChooseAcc.Clear();
        cmdsDelAcc.Clear();
        cmdsEditAcc.Clear();
        if (text != null && !(text.Trim('|') == string.Empty))
        {
            string[] accs = text.Trim('|').Split('|');
            for (int i = 0; i < accs.Length; i++)
            {
                string[] acc = accs[i].Split('$');
                if (acc.Length >= 2)
                {
                    Account account = new Account(acc[0], acc[1]);
                    accounts.Add(account);
                    Command cmd = new Command("Chọn", this, 102, account);
                    cmd.setType();
                    cmdsChooseAcc.Add(cmd);
                    Command cmdDel = new Command("Xoá", this, 103, account);
                    cmdDel.setTypeDelete();
                    cmdsDelAcc.Add(cmdDel);
                    Command cmdEdit = new Command("Sửa", this, 105, new object[] { i, account });
                    cmdEdit.setType();
                    cmdsEditAcc.Add(cmdEdit);
                }
            }
        }
    }

    public void AddAccount(string user, string pass)
    {
        Account account = new Account(user, pass);
        int index = accounts.IndexOf(account);
        if (index != -1)
        {
            accounts.RemoveAt(index);
        }
        accounts.Insert(0, account);
        for (int i = 20; i < accounts.Count; i++)
        {
            accounts.RemoveAt(i);
        }
        SaveAcc();
        LoadAcc();
    }
    
    public void UpdateAccount(int index, string user, string pass)
    {
        if (index >= 0 && index < accounts.Count)
        {
            Account oldAcc = accounts[index];
            accounts[index] = new Account(user, pass);
            SaveAcc();
            LoadAcc();
        }
    }

    private void SaveAcc()
    {
        string text = "";
        foreach (Account acc in accounts)
        {
            text += string.Join('$', acc.getUsername(), acc.getPassword());
            text += "|";
        }
        Rms.saveRMSString("accManager", text.Trim('|'));
    }

    private void AutoChat()
    {
        if (string.IsNullOrEmpty(textAutoChat))
        {
            GameScr.info1.addInfo("Chưa cài nội dung tự động chat", 0);
        }
        else
        {
            Service.gI().chat(textAutoChat);
        }
    }

    private void AutoChatTG()
    {
        if (string.IsNullOrEmpty(textAutoChatTG))
        {
            GameScr.info1.addInfo("Chưa cài nội dung tự động chat thế giới", 0);
        }
        else
        {
            Service.gI().chatGlobal(textAutoChatTG);
        }
    }

    public static string EncodeStringToByteArrayString(string inputString, string key)
    {
        string byteArrayString = BitConverter.ToString(EncodeToBytes(inputString, key)).Replace("-", "");
        return string.Join("-", SplitByLength(byteArrayString, 2));
    }

    private static byte[] EncodeToBytes(string inputString, string key)
    {
        byte[] inputBytes = Encoding.UTF8.GetBytes(inputString);
        byte[] keyBytes = Encoding.UTF8.GetBytes(key);
        byte[] encodedBytes = new byte[inputBytes.Length];
        for (int i = 0; i < inputBytes.Length; i++)
        {
            encodedBytes[i] = (byte)(inputBytes[i] ^ keyBytes[i % keyBytes.Length]);
        }
        return encodedBytes;
    }

    private static string[] SplitByLength(string str, int length)
    {
        int strLength = str.Length;
        int numSegments = (strLength + length - 1) / length;
        string[] segments = new string[numSegments];
        for (int i = 0; i < numSegments; i++)
        {
            int startIndex = i * length;
            int segmentLength = Math.min(length, strLength - startIndex);
            segments[i] = str.Substring(startIndex, segmentLength);
        }
        return segments;
    }

    public static string DecodeByteArrayString(string byteArrayString, string key)
    {
        try
        {
            string[] hexValues = byteArrayString.Split('-');
            string concatenatedHex = string.Join("", hexValues);
            byte[] encodedBytes = new byte[concatenatedHex.Length / 2];
            for (int i = 0; i < encodedBytes.Length; i++)
            {
                encodedBytes[i] = Convert.ToByte(concatenatedHex.Substring(i * 2, 2), 16);
            }
            return DecodeToString(encodedBytes, key);
        }
        catch (Exception)
        {
            return string.Empty;
        }
    }

    private static string DecodeToString(byte[] encodedBytes, string key)
    {
        byte[] keyBytes = Encoding.UTF8.GetBytes(key);
        byte[] decodedBytes = new byte[encodedBytes.Length];
        for (int i = 0; i < encodedBytes.Length; i++)
        {
            decodedBytes[i] = (byte)(encodedBytes[i] ^ keyBytes[i % keyBytes.Length]);
        }
        return Encoding.UTF8.GetString(decodedBytes);
    }

    public static void Log(string text)
    {
        if (isDebugEnable)
        {
            Debug.Log(text);
        }
    }

    public static void WriteLog(string message)
    {
        if (!isDebugEnable)
        {
            return;
        }
        try
        {
            StreamWriter streamWriter = new StreamWriter(new FileStream("log_" + DateTime.Today.ToString("yyyyMMdd") + ".txt", FileMode.OpenOrCreate));
            streamWriter.WriteLine(DateTime.Today.ToString("HH:mm:ss") + ": " + message);
            streamWriter.Flush();
            streamWriter.Close();
        }
        catch (Exception ex)
        {
            Log(ex.Message);
        }
    }

    private void LoadSkillToScreen()
    {
        int maxSlots = GameCanvas.isTouch && !Main.isPC ? GameScr.onScreenSkill.Length : GameScr.keySkill.Length;
        int maxSkills = System.Math.Min(Char.myCharz().vSkill.size(), maxSlots);
        for (int i = 0; i < maxSkills; i++)
        {
            Skill skill = (Skill)Char.myCharz().vSkill.elementAt(i);
            if (GameCanvas.isTouch && !Main.isPC)
            {
                for (int j = 0; j < GameScr.onScreenSkill.Length; j++)
                {
                    if (GameScr.onScreenSkill[j] == skill)
                    {
                        GameScr.onScreenSkill[j] = null;
                    }
                }
                GameScr.onScreenSkill[i] = skill;
                GameScr.gI().saveonScreenSkillToRMS();
                continue;
            }
            for (int k = 0; k < GameScr.keySkill.Length; k++)
            {
                if (GameScr.keySkill[k] == skill)
                {
                    GameScr.keySkill[k] = null;
                }
            }
            GameScr.keySkill[i] = skill;
            GameScr.gI().saveKeySkillToRMS();
        }
    }

    public static string DecodeByteArrayString(string byteArrayString)
    {
        try
        {
            string[] hexValues = byteArrayString.Split('-');
            string concatenatedHex = string.Join("", hexValues);
            byte[] encodedBytes = new byte[concatenatedHex.Length / 2];
            for (int i = 0; i < encodedBytes.Length; i++)
            {
                encodedBytes[i] = Convert.ToByte(concatenatedHex.Substring(i * 2, 2), 16);
            }
            return DecodeToString(encodedBytes, 69.ToString());
        }
        catch (Exception)
        {
            return string.Empty;
        }
    }

    public static void DoChatGlobal()
    {
        GameCanvas.endDlg();
        if (Char.myCharz().checkLuong() < 5)
        {
            GameCanvas.startOKDlg(mResources.not_enough_luong_world_channel);
            return;
        }
        if (GameCanvas.panel.chatTField == null)
        {
            GameCanvas.panel.chatTField = new ChatTextField();
            GameCanvas.panel.chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
            GameCanvas.panel.chatTField.initChatTextField();
            GameCanvas.panel.chatTField.parentScreen = GameCanvas.panel;
        }
        GameCanvas.panel.chatTField.strChat = mResources.world_channel_5_luong;
        GameCanvas.panel.chatTField.tfChat.name = mResources.CHAT;
        GameCanvas.panel.chatTField.to = string.Empty;
        GameCanvas.panel.chatTField.isShow = true;
        GameCanvas.panel.chatTField.tfChat.isFocus = true;
        GameCanvas.panel.chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        if (Main.isWindowsPhone)
        {
            GameCanvas.panel.chatTField.tfChat.strInfo = GameCanvas.panel.chatTField.strChat;
        }
        if (!Main.isPC)
        {
            GameCanvas.panel.chatTField.startChat(GameCanvas.panel, string.Empty);
        }
        else if (GameCanvas.isTouch)
        {
            GameCanvas.panel.chatTField.tfChat.doChangeToTextBox();
        }
    }

    public void GoToBoss(int mapId)
    {
        MyVector myVector = new MyVector();
        myVector.addElement(new Command("Đi tới\nMAP " + mapId, this, 1, mapId.ToString()));
        myVector.addElement(new Command("Huỷ", this, 2, null));
        GameCanvas.menu.startAt(myVector, 4);
    }

    public void ChangeFPSTarget()
    {
        Rms.saveRMSInt("isHighFps", isHighFps ? 1 : 0);
        if (isHighFps)
        {
            Application.targetFrameRate = 60;
        }
        else
        {
            Application.targetFrameRate = 30;
        }
    }

    public static void changeStatusEffectInven()
    {
        if (isEffectInven)
        {
            isEffectInven = false;
            Rms.saveRMSInt("effectinven", isEffectInven ? 1 : 0);
        }
        else
        {
            isEffectInven = true;
            Rms.saveRMSInt("effectinven", isEffectInven ? 1 : 0);
        }
    }

    public static void chanegStatusInventory()
    {
        if (isInventory)
        {
            isInventory = false;
            Rms.saveRMSInt("inventory", isInventory ? 1 : 0);
            GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
        }
        else
        {
            isInventory = true;
            Rms.saveRMSInt("inventory", isInventory ? 1 : 0);
            GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
        }
    }

    public static void changeStatusLogo()
    {
        if (isLogo)
        {
            isLogo = false;
            imgLogoBig = null;
            logo = null;
            Rms.saveRMSInt("logo", 0);
            if (isLogoGif)
            {
                isLogoGif = false;
                Rms.saveRMSInt("logogif", 0);
            }
        }
        else
        {
            Rms.saveRMSInt("logo", 1);
            isLogo = true;
            if (isLogoGif)
            {
                LoadLogoGif();
                Rms.saveRMSInt("logogif", 1);
            }
            else
            {
                LoadLogoImages();
            }
        }
    }

    public static void changeStatusBackground()
    {
        if (GiamDungLuong)
        {
            GiamDungLuong = false;
            Rms.saveRMSInt("background", GiamDungLuong ? 1 : 0);
        }
        else
        {
            GiamDungLuong = true;
            Rms.saveRMSInt("background", GiamDungLuong ? 1 : 0);
        }
    }

    public static void changeStatusAnPlayer()
    {
        if (AnPlayer)
        {
            AnPlayer = false;
            Rms.saveRMSInt("anplayer", AnPlayer ? 1 : 0);
        }
        else
        {
            AnPlayer = true;
            Rms.saveRMSInt("anplayer", AnPlayer ? 1 : 0);
        }
    }

    public static void changeStatusShowID()
    {
        if (isShowID)
        {
            isShowID = false;
            Rms.saveRMSInt("showid", isShowID ? 1 : 0);
        }
        else
        {
            isShowID = true;
            Rms.saveRMSInt("showid", isShowID ? 1 : 0);
        }
    }

    public static void changeStatusLogoGif()
    {
        if (isLogoGif)
        {
            isLogoGif = false;
            Rms.saveRMSInt("logogif", 0);
        }
        else
        {
            isLogoGif = true;
            Rms.saveRMSInt("logogif", 1);
        }
    }

    public static Npc GetNpcByTempId(int tempId)
    {
        for (int i = 0; i < GameScr.vNpc.size(); i++)
        {
            Npc npc = (Npc)GameScr.vNpc.elementAt(i);
            if (npc.template.npcTemplateId == tempId)
            {
                return npc;
            }
        }
        return null;
    }

    public static void LoadLogoImages()
    {
        imgLogoBig = GameCanvas.loadImage("/logo.png");
        if (imgLogoBig == null)
        {
            GameScr.info1.addInfo("Không thể load logo!", 0);
            isLogo = false;
            Rms.saveRMSInt("logo", 0);
        }
    }

    public static void LoadLogoGif()
    {
        for (int i = 0; i < FrameGif; i++)
        {
            logos[i] = GameCanvas.loadImage("/GifMenu/love-" + i + ".png");
        }
    }

    public static void PaintLogoGif(mGraphics g, int x, int y, int anchor)
    {
        if (!isLogo)
        {
            return;
        }
        if (isLogoGif)
        {
            int id = GameCanvas.gameTick / 2 % FrameGif;
            if (logos[id] != null)
            {
                g.drawImage(logos[id], x, y, anchor);
            }
        }
        else if (imgLogoBig != null)
        {
            g.drawImage(imgLogoBig, x, y, anchor);
        }
    }

    public static void LoadLogoGifMenu()
    {
        for (int i = 0; i < FrameGifMenu; i++)
        {
            logosMenu[i] = GameCanvas.loadImage("/GifMenu/love-" + i + ".png");
        }
    }

    public static void PaintLogoGifMenu(mGraphics g, int x, int y, int anchor)
    {
        int id = GameCanvas.gameTick / 2 % FrameGifMenu;
        if (logosMenu[id] != null)
        {
            g.drawImage(logosMenu[id], x, y, anchor);
        }
    }

    public static void LoadTickImages()
    {
        for (int i = 0; i < 20; i++)
        {
            ticks[i] = GameCanvas.loadImage("/tick/tick_" + i);
        }
    }

    public static void PaintTicks(mGraphics g, int x, int y)
    {
        int id = GameCanvas.gameTick / 4 % 20;
        if (ticks[id] != null)
        {
            g.drawImage(ticks[id], x, y);
        }
    }

    private static IEnumerator LoadFile(string fullPath)
    {
        string fileUri = "file:";
        using UnityWebRequest www = UnityWebRequestMultimedia.GetAudioClip(fileUri, AudioType.OGGVORBIS);
        www.certificateHandler = new BypassCertificateHandler();
        yield return www.SendWebRequest();
        if (www.result != UnityWebRequest.Result.Success)
        {
            Debug.LogError(www.error);
            yield break;
        }
        AudioClip temp = DownloadHandlerAudioClip.GetContent(www);
        musics.Add(temp);
    }

    public static void InitMusic()
    {
        int fromRms = Rms.loadRMSInt("musicSize");
        musicCount = ((fromRms != -1) ? fromRms : 0);
        for (int i = 0; i < musicCount; i++)
        {
            string fullPath = Rms.GetiPhoneDocumentsPath() + "/music_" + i + ".ogg";
            if (File.Exists(fullPath))
            {
                CoroutineRunner.Instance.RunCoroutine(LoadFile(fullPath));
            }
            else
            {
                Debug.LogWarning("File does not exist: " + fullPath);
            }
        }
    }

    public static bool AutoLogin()
    {
        if (autoLogin == null || autoLogin.waitToNextLogin)
        {
            return false;
        }
        if (!Util.CanDoWithTime(autoLogin.lastTimeWait, 500L))
        {
            return false;
        }
        if (ServerListScreen.ipSelect < 0 || ServerListScreen.ipSelect >= ServerListScreen.address.Length || string.IsNullOrEmpty(ServerListScreen.address[ServerListScreen.ipSelect]) || ServerListScreen.testConnect != 2)
        {
            ServerListScreen.LoadIP();
            if (GameCanvas.serverScreen == null)
            {
                GameCanvas.serverScreen = new ServerListScreen();
            }
            GameCanvas.serverScreen.switchToMe();
            autoLogin.lastTimeWait = mSystem.currentTimeMillis();
            return false;
        }
        if (GameCanvas.currentScreen != GameCanvas.loginScr)
        {
            if (GameCanvas.loginScr == null)
            {
                GameCanvas.loginScr = new LoginScr();
            }
            GameCanvas.loginScr.switchToMe();
            autoLogin.lastTimeWait = mSystem.currentTimeMillis();
            return false;
        }
        if (!autoLogin.hasSetUserPass)
        {
            Account account = autoLogin.GetAccWithUsername(accounts);
            if (account.getUsername().Length > 0)
            {
                Rms.saveRMSString("acc", account.getUsername());
                Rms.saveRMSString("pass", account.getPassword());
                GameCanvas.acc = account.getUsername();
                GameCanvas.pass = account.getPassword();
                GameCanvas.loginScr.setUserPass();
                autoLogin.hasSetUserPass = true;
            }
            autoLogin.lastTimeWait = mSystem.currentTimeMillis();
        }
        GameCanvas.loginScr.doLogin();
        autoLogin.waitToNextLogin = true;
        return true;
    }

    public static string Decrypt(string encryptedText, int keys)
    {

        if (string.IsNullOrEmpty(encryptedText))
        {
            return string.Empty;
        }
        int padding;
        for (padding = 0; (encryptedText.Length + padding) % 5 != 0; padding++)
        {
        }
        if (padding > 0)
        {
            encryptedText = encryptedText.PadRight(encryptedText.Length + padding, 'u');
        }
        List<byte> result = new List<byte>();
        for (int i = 0; i < encryptedText.Length; i += 5)
        {
            ulong value = 0uL;
            for (int j = 0; j < 5; j++)
            {
                int charIndex = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&()*+-;<=>?@^_`{|}~".IndexOf(encryptedText[i + j]);
                if (charIndex == -1)
                {

                    Debug.LogError($"Ký tự không hợp lệ trong chuỗi mã hóa: {encryptedText[i + j]} tại vị trí {i + j}");
                    throw new Exception($"Ký tự không hợp lệ trong chuỗi mã hóa: {encryptedText[i + j]}");
                }
                value = value * 85 + (uint)charIndex;
            }
            result.Add((byte)(value >> 24));
            result.Add((byte)(value >> 16));
            result.Add((byte)(value >> 8));
            result.Add((byte)value);
        }
        if (padding > 0)
        {
            result.RemoveRange(result.Count - padding, padding);
        }
        byte[] array = result.ToArray();
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] cipherText = new byte[array.Length - 32];
        Buffer.BlockCopy(array, 0, salt, 0, 16);
        Buffer.BlockCopy(array, 16, iv, 0, 16);
        Buffer.BlockCopy(array, 32, cipherText, 0, cipherText.Length);
        string keysStr = keys.ToString();
        byte[] keyBytes = Encoding.UTF8.GetBytes(keysStr).Concat(salt).ToArray();
        byte[] key = new byte[32];
        for (int k = 0; k < 32; k++)
        {
            key[k] = keyBytes[k % keyBytes.Length];
        }
        using Aes aes = Aes.Create();
        aes.Key = key;
        aes.IV = iv;
        aes.Mode = CipherMode.CBC;
        aes.Padding = PaddingMode.PKCS7;
        using ICryptoTransform decryptor = aes.CreateDecryptor();
        using MemoryStream msDecrypt = new MemoryStream(cipherText);
        using CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read);
        using StreamReader srDecrypt = new StreamReader(csDecrypt);
        return srDecrypt.ReadToEnd();
    }
    public void UpdateIntrinsicInfo(string info)
    {
        if (mSystem.currentTimeMillis() - lastTimeUpdateNoiTai < 500)
        {
            return;
        }
        lastTimeUpdateNoiTai = mSystem.currentTimeMillis();
        try
        {
            if (info.Contains("+"))
            {
                string[] parts = info.Split('+');
                currentPlayerNoiTai = parts[0].Trim();
                if (int.TryParse(parts[1].Split('%')[0], out var value))
                {
                    CurrentParamNoitaiPlayer = value;
                }
            }
            else if (info.Contains("dưới"))
            {
                string[] parts2 = info.Split(new string[1] { "dưới " }, StringSplitOptions.None);
                currentPlayerNoiTai = parts2[0].Trim();
                if (int.TryParse(parts2[1].Split('%')[0], out var value2))
                {
                    CurrentParamNoitaiPlayer = value2;
                }
            }
        }
        catch (Exception exception)
        {
            Debug.LogException(exception);
        }
    }

    private void SaveButtonPositions()
    {
        string posData = "";
        foreach (KeyValuePair<string, Point> kvp in buttonPositions)
        {
            posData += $"{kvp.Key},{kvp.Value.x},{kvp.Value.y};";
        }
        Rms.saveRMSString("buttonPositions", posData);
    }

    private void LoadButtonPositions()
    {
        string posData = Rms.loadRMSString("buttonPositions");
        if (!string.IsNullOrEmpty(posData))
        {
            buttonPositions.Clear();
            string[] array = posData.Split(';');
            foreach (string button in array)
            {
                if (string.IsNullOrEmpty(button))
                {
                    continue;
                }
                string[] parts = button.Split(',');
                if (parts.Length == 3)
                {
                    string name = parts[0];
                    if (int.TryParse(parts[1], out var x) && int.TryParse(parts[2], out var y))
                    {
                        buttonPositions[name] = new Point(x, y);
                    }
                }
            }
        }
        else
        {
            InitButtonPositions();
        }
    }

    public static void changeStatusEditButton()
    {
        if (isEditButton)
        {
            StopHudEditor(false);
            GameScr.info1.addInfo("Đã thoát tùy chỉnh giao diện", 0);
        }
        else
        {
            StartHudEditor();
        }
    }

    private void AddOrRemoveFilterItem(Item item, bool isAdd)
    {
        if (isAdd)
        {
            listFilterItems.Add(new ItemAutoFilter(item.template.iconID, item.template.id, item.template.name));
            GameScr.info1.addInfo("Đã thêm " + item.template.name + " vào DS lọc đồ", 0);
            return;
        }
        foreach (ItemAutoFilter itemFilter in listFilterItems)
        {
            if (itemFilter.iconID == item.template.iconID && itemFilter.id == item.template.id && itemFilter.name == item.template.name)
            {
                listFilterItems.Remove(itemFilter);
                GameScr.info1.addInfo("Đã xóa " + item.template.name + " khỏi DS lọc đồ", 0);
                break;
            }
        }
    }

    private void ShowFilterList(mGraphics g)
    {
        int itemHeight = 25;
        int padding = 10;
        int headerHeight = 30;
        int resizeHandleSize = 40;
        int resizeX = panelX + panelW - resizeHandleSize;
        int resizeY = panelY + panelH - resizeHandleSize;
        if (GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(panelX, panelY, panelW, headerHeight))
        {
            GameCanvas.isPointerJustDown = false;
            if (!isDragging)
            {
                isDragging = true;
                lastMouseX = GameCanvas.px;
                lastMouseY = GameCanvas.py;
                lastPanelX = panelX;
                lastPanelY = panelY;
            }
            else
            {
                float smoothFactor = 1f;
                int deltaX = GameCanvas.px - lastMouseX;
                int deltaY = GameCanvas.py - lastMouseY;
                int targetX = lastPanelX + deltaX;
                int targetY = lastPanelY + deltaY;
                panelX = (int)((float)panelX + (float)(targetX - panelX) * smoothFactor);
                panelY = (int)((float)panelY + (float)(targetY - panelY) * smoothFactor);
                lastMouseX = GameCanvas.px;
                lastMouseY = GameCanvas.py;
                lastPanelX = panelX;
                lastPanelY = panelY;
            }
            panelX = System.Math.Max(0, System.Math.Min(GameCanvas.w - panelW, panelX));
            panelY = System.Math.Max(0, System.Math.Min(GameCanvas.h - panelH, panelY));
        }
        else
        {
            isDragging = false;
        }
        if (GameCanvas.isPointerDown && !isDragging)
        {
            bool num = GameCanvas.px >= panelX + panelW - resizeHandleSize && GameCanvas.px <= panelX + panelW;
            bool isInResizeZoneY = GameCanvas.py >= panelY + panelH - resizeHandleSize && GameCanvas.py <= panelY + panelH;
            if ((num && GameCanvas.py >= panelY + panelH - resizeHandleSize) || (isInResizeZoneY && GameCanvas.px >= panelX + panelW - resizeHandleSize))
            {
                GameCanvas.isPointerJustDown = false;
                if (!isResizing)
                {
                    isResizing = true;
                    lastMouseX = GameCanvas.px;
                    lastMouseY = GameCanvas.py;
                    lastPanelW = panelW;
                    lastPanelH = panelH;
                }
                else
                {
                    float smoothFactor2 = 1f;
                    int deltaX2 = GameCanvas.px - lastMouseX;
                    int deltaY2 = GameCanvas.py - lastMouseY;
                    int targetW = lastPanelW + deltaX2;
                    int targetH = lastPanelH + deltaY2;
                    panelW = (int)((float)panelW + (float)(targetW - panelW) * smoothFactor2);
                    panelH = (int)((float)panelH + (float)(targetH - panelH) * smoothFactor2);
                    lastMouseX = GameCanvas.px;
                    lastMouseY = GameCanvas.py;
                    lastPanelW = panelW;
                    lastPanelH = panelH;
                    panelW = System.Math.Max(180, System.Math.Min(GameCanvas.w - panelX, panelW));
                    panelH = System.Math.Max(120, System.Math.Min(GameCanvas.h - panelY, panelH));
                }
            }
        }
        else if (!GameCanvas.isPointerDown)
        {
            isResizing = false;
        }
        g.setColor(0, 0.7f);
        g.fillRect(panelX, panelY, panelW, panelH, 5);
        for (int i = 0; i < 3; i++)
        {
            g.setColor(16777215, 0.2f - (float)i * 0.05f);
            g.drawRect(panelX + i, panelY + i, panelW - i * 2, panelH - i * 2);
        }
        g.setColor(16777215, 0.8f);
        for (int j = 0; j < 3; j++)
        {
            g.drawLine(resizeX + 5, panelY + panelH - 10 - j * 5, panelX + panelW - 5, panelY + panelH - 10 - j * 5);
        }
        for (int k = 0; k < 3; k++)
        {
            g.drawLine(panelX + panelW - 10 - k * 5, resizeY + 5, panelX + panelW - 10 - k * 5, panelY + panelH - 5);
        }
        int btnSize = 16;
        g.setColor(16733525);
        g.fillRect(panelX + panelW - btnSize - 5, panelY + 5, btnSize, btnSize, 5);
        mFont.tahoma_7b_white.drawString(g, "X", panelX + panelW - btnSize / 2 - 5, panelY + 7, mFont.CENTER);
        int autoFilterBtnW = 80;
        int autoFilterBtnH = 20;
        int autoFilterBtnX = panelX + panelW - 80;
        int autoFilterBtnY = panelY + panelH - autoFilterBtnH + 25;
        g.setColor(isAutoFilterItem ? 65280 : 16711680);
        g.fillRect(autoFilterBtnX, autoFilterBtnY, autoFilterBtnW, autoFilterBtnH, 8);
        string btnText = (isAutoFilterItem ? "Auto: Bật" : "Auto: Tắt");
        mFont.tahoma_7b_white.drawStringBorder(g, btnText, autoFilterBtnX + autoFilterBtnW / 2 + 1, autoFilterBtnY + 6 + 1, mFont.CENTER, mFont.tahoma_7_grey);
        if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease && GameCanvas.isPointerHoldIn(autoFilterBtnX, autoFilterBtnY, autoFilterBtnW, autoFilterBtnH))
        {
            isAutoFilterItem = !isAutoFilterItem;
            GameScr.info1.addInfo(isAutoFilterItem ? "Đã bật auto lọc đồ" : "Đã tắt auto lọc đồ", 0);
            GameCanvas.clearAllPointerEvent();
        }
        string title = "Danh sách vật phẩm lọc";
        int titleY = panelY + padding;
        mFont.tahoma_7b_white.drawString(g, title, panelX + panelW / 2, titleY, mFont.CENTER);
        g.setColor(5987163);
        g.fillRect(panelX + padding, titleY + 12, panelW - padding * 2, 1, 5);
        g.setClip(panelX, panelY + 35, panelW, panelH - 45);
        int contentHeight = listFilterItems.Count * itemHeight;
        int visibleHeight = panelH - 45;
        int maxScroll = System.Math.Max(0, contentHeight - visibleHeight);
        if (GameCanvas.isPointerDown && !isDragging && !isResizing)
        {
            GameCanvas.isPointerJustDown = false;
            if (!isScrolling)
            {
                isScrolling = true;
                lastMouseY = GameCanvas.py;
                lastScrollY = scrollY;
            }
            else
            {
                float smoothFactor3 = 1f;
                int deltaY3 = lastMouseY - GameCanvas.py;
                int targetScroll = lastScrollY + deltaY3;
                scrollY = (int)((float)scrollY + (float)(targetScroll - scrollY) * smoothFactor3);
                lastMouseY = GameCanvas.py;
                lastScrollY = scrollY;
            }
            scrollY = System.Math.Max(0, System.Math.Min(maxScroll, scrollY));
        }
        else if (!GameCanvas.isPointerDown)
        {
            isScrolling = false;
        }
        int num2 = scrollY / itemHeight;
        int endIndex = System.Math.Min(num2 + MAX_ITEMS_VISIBLE, listFilterItems.Count);
        for (int l = num2; l < endIndex; l++)
        {
            ItemAutoFilter item = listFilterItems[l];
            int itemY = panelY + 35 + l * itemHeight - scrollY;
            if (l % 2 == 0)
            {
                g.setColor(2105376, 0.3f);
                g.fillRect(panelX + 5, itemY, panelW - 10, itemHeight - 2, 5);
            }
            string info = item.name ?? "";
            mFont.tahoma_7_white.drawString(g, info, panelX + padding, itemY + 5, 0);
            mFont.tahoma_7_red.drawString(g, $"ID: {item.id}", panelX + padding, itemY + 15, 0);
            int delBtnW = 35;
            int delBtnH = 18;
            int delBtnX = panelX + panelW - delBtnW - padding;
            int delBtnY = itemY + 3;
            g.setColor(16724787);
            g.fillRect(delBtnX, delBtnY, delBtnW, delBtnH, 5);
            mFont.tahoma_7b_white.drawString(g, "Xóa", delBtnX + delBtnW / 2, delBtnY + 4, mFont.CENTER);
            if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                GameCanvas.isPointerJustDown = false;
                if (GameCanvas.isPointerHoldIn(delBtnX, delBtnY, delBtnW, delBtnH))
                {
                    listFilterItems.RemoveAt(l);
                    GameScr.info1.addInfo("Đã xóa vật phẩm khỏi danh sách lọc", 0);
                    GameCanvas.clearAllPointerEvent();
                }
            }
        }
        g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        int scrollBarX = panelX + panelW - 8;
        int scrollBarY = panelY + 35;
        int scrollBarH = panelH - 45;
        int scrollBarW = 4;
        g.setColor(3355443);
        g.fillRect(scrollBarX, scrollBarY, scrollBarW, scrollBarH, 5);
        g.setColor(6710886);
        g.drawRect(scrollBarX, scrollBarY, scrollBarW, scrollBarH);
        if (contentHeight > visibleHeight)
        {
            float scrollRatio = (float)visibleHeight / (float)contentHeight;
            int scrollThumbH = (int)((float)scrollBarH * scrollRatio);
            int scrollThumbY = scrollBarY;
            if (scrollY > 0)
            {
                float scrollPercent = (float)scrollY / (float)maxScroll;
                scrollThumbY = scrollBarY + (int)((float)(scrollBarH - scrollThumbH) * scrollPercent);
            }
            scrollThumbY = System.Math.Max(scrollBarY, System.Math.Min(scrollBarY + scrollBarH - scrollThumbH, scrollThumbY));
            g.setColor(8947848);
            g.fillRect(scrollBarX + 1, scrollThumbY, scrollBarW - 2, scrollThumbH, 5);
            g.setColor(11184810);
            g.fillRect(scrollBarX + 1, scrollThumbY, scrollBarW - 2, 2, 5);
            g.setColor(6710886);
            g.fillRect(scrollBarX + 1, scrollThumbY + scrollThumbH - 2, scrollBarW - 2, 2, 5);
        }
        if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {
            GameCanvas.isPointerJustDown = false;
            if (GameCanvas.isPointerHoldIn(panelX + panelW - btnSize - 5, panelY + 5, btnSize, btnSize))
            {
                isShowFilterList = false;
                scrollY = 0;
                GameCanvas.clearAllPointerEvent();
            }
        }
    }

    private void DoFilter()
    {
        if (!isAutoFilterItem)
        {
            return;
        }
        try
        {
            for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
            {
                Item item = Char.myCharz().arrItemBag[i];
                if (item == null)
                {
                    continue;
                }
                foreach (ItemAutoFilter listFilterItem in listFilterItems)
                {
                    if (listFilterItem.id == item.template.id)
                    {
                        Service.gI().useItem(1, 1, (sbyte)item.indexUI, -1);
                        Thread.Sleep(50);
                        return;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            Debug.LogException(exception);
        }
    }

    public static void DoBoss()
    {
        if (string.IsNullOrEmpty(bossCanDo))
        {
            GameScr.info1.addInfo("Chưa nhập boss cần tìm", 0);
            zoneMacDinh = 0;
            isdoBoss = false;
            return;
        }
        if (GI().IsHotkeyHeld(HOTKEY_PICK_ITEM))
        {
            GameScr.info1.addInfo("Đã tắt auto dò boss", 0);
            isdoBoss = false;
            return;
        }
        for (int i = 0; i < GameScr.vCharInMap.size(); i++)
        {
            Char @char = (Char)GameScr.vCharInMap.elementAt(i);
            if (@char != null && @char.cName.ToLower().Contains(bossCanDo.ToLower()) && @char.cTypePk == 5)
            {
                Sound.start(1f, Sound.l1);
                GameScr.info1.addInfo("Đã tìm thấy boss", 0);
                zoneMacDinh = 0;
                isdoBoss = false;
                return;
            }
        }
        if (GameScr.gI().numPlayer == null || GameScr.gI().numPlayer.Length == 0)
        {
            Service.gI().openUIZone();
            return;
        }
        Service.gI().requestChangeZone(zoneMacDinh, -1);
        if (!Char.isLoadingMap && TileMap.zoneID == zoneMacDinh)
        {
            zoneMacDinh++;
            if (zoneMacDinh >= GameScr.gI().numPlayer.Length)
            {
                zoneMacDinh = 0;
            }
        }
    }

    public static void LoadImgMenuChat()
    {
        imgMenuChat = GameCanvas.loadImage("/mainImage/MenuChat.png");
        imgCloseButton = GameCanvas.loadImage("/mainImage/myTexture2dbtX.png");
        imgNextPage = GameCanvas.loadImage("/mainImage/myTexture2dbtnl.png");
        imgNextPage2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf.png");
        imgPrevPage = GameCanvas.loadImage("/mainImage/myTexture2dbtnl.png");
        imgPrevPage2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf.png");
    }

    private static void EnsureMailPopupBackgroundLoaded()
    {
        if (imgMailPopup == null)
        {
            imgMailPopup = GameCanvas.loadImage("/mainImage/myTexture2dimgPopup.png");
        }
        if (imgMailPopup2 == null)
        {
            imgMailPopup2 = GameCanvas.loadImage("/mainImage/myTexture2dimgPopup2.png");
        }
    }

    public static void PaintMailPopupBackground(mGraphics g, int x, int y, int w, int h, int color)
    {
        EnsureMailPopupBackgroundLoaded();
        Image img = (color != 1) ? imgMailPopup : imgMailPopup2;
        if (img == null)
        {
            g.setColor(0x1A1A1A, 0.95f);
            g.fillRect(x, y, w, h, 8);
            return;
        }

        g.drawRegion(img, 0, 0, 10, 10, 0, x, y, 0);
        g.drawRegion(img, 0, 20, 10, 10, 0, x + w - 10, y, 0);
        g.drawRegion(img, 0, 50, 10, 10, 0, x, y + h - 10, 0);
        g.drawRegion(img, 0, 70, 10, 10, 0, x + w - 10, y + h - 10, 0);

        int topSegments = ((w - 20) % 10 != 0) ? ((w - 20) / 10 + 1) : ((w - 20) / 10);
        int sideSegments = ((h - 20) % 10 != 0) ? ((h - 20) / 10 + 1) : ((h - 20) / 10);

        for (int i = 0; i < topSegments; i++)
        {
            g.drawRegion(img, 0, 10, 10, 10, 0, x + 10 + i * 10, y, 0);
        }
        for (int i = 0; i < sideSegments; i++)
        {
            g.drawRegion(img, 0, 30, 10, 10, 0, x, y + 10 + i * 10, 0);
        }
        for (int i = 0; i < topSegments; i++)
        {
            g.drawRegion(img, 0, 60, 10, 10, 0, x + 10 + i * 10, y + h - 10, 0);
        }
        for (int i = 0; i < sideSegments; i++)
        {
            g.drawRegion(img, 0, 40, 10, 10, 0, x + w - 10, y + 10 + i * 10, 0);
        }

        g.setColor((color != 1) ? 16770503 : 12052656);
        g.fillRect(x + 10, y + 10, w - 20, h - 20);
    }

    public static void PaintInventoryItemSlot(mGraphics g, int x, int y, int w, int h, bool selected, bool hasItem)
    {
        g.setColor(0xB39E83);
        g.fillRect(x, y, w, h, 5);
        if (hasItem && w > 4 && h > 4)
        {
            g.setColor(0xA18E76);
            g.fillRect(x + 2, y + 2, w - 4, h - 4, 4);
        }
        if (selected)
        {
            g.setColor(0xCC6600);
            g.drawRect(x - 2, y - 2, w + 3, h + 3);
            g.setColor(0xFFCC66);
            g.drawRect(x - 1, y - 1, w + 1, h + 1);
        }
    }

    private static int GetMailActionButtonWidth(string caption, int minWidth)
    {
        return System.Math.Max(minWidth, mFont.tahoma_7b_dark.getWidth(caption) + 14);
    }

    public static void PaintMailActionButton(mGraphics g, int x, int y, int width, string caption, bool isFocused)
    {
        if (isFocused)
        {
            Command.paintOngMau(Command.btn1left, Command.btn1mid, Command.btn1right, x, y, width, g);
            mFont.tahoma_7b_green2.drawString(g, caption, x + width / 2, y + 7, mFont.CENTER);
        }
        else
        {
            Command.paintOngMau(Command.btn0left, Command.btn0mid, Command.btn0right, x, y, width, g);
            mFont.tahoma_7b_dark.drawString(g, caption, x + width / 2, y + 7, mFont.CENTER);
        }
    }

    private static bool IsMailClaimPopupVisible()
    {
        return mailClaimPopupTransition.Active && !string.IsNullOrEmpty(mailClaimPopupTitle);
    }

    private static void ShowMailClaimPopup(string title, List<Mail.RewardData> rewards, string detail)
    {
        mailClaimPopupTitle = title;
        mailClaimPopupRewards = rewards ?? new List<Mail.RewardData>();
        mailClaimPopupDetail = detail;
        mailClaimPopupTimer = MailClaimPopupDuration;
        mailClaimPopupTransition.Show();
    }

    private static void CloseMailClaimPopup(bool immediately = false)
    {
        if (!immediately)
        {
            mailClaimPopupTransition.Close();
            return;
        }
        mailClaimPopupTransition.CloseImmediately();
        mailClaimPopupTitle = null;
        mailClaimPopupDetail = null;
        mailClaimPopupTimer = 0f;
        mailClaimPopupRewards.Clear();
        mailPendingClaimRewards.Clear();
    }

    private static List<Mail.RewardData> CloneMailRewards(List<Mail.RewardData> rewards)
    {
        List<Mail.RewardData> clones = new List<Mail.RewardData>();
        if (rewards == null)
        {
            return clones;
        }

        for (int i = 0; i < rewards.Count; i++)
        {
            Mail.RewardData reward = rewards[i];
            if (reward == null)
            {
                continue;
            }
            clones.Add(new Mail.RewardData(reward.id, reward.rewardType, reward.rewardId, reward.amount,
                reward.claimed, reward.optionsData));
        }
        return clones;
    }

    private static string BuildMailClaimRewardText(Mail.RewardData reward)
    {
        if (reward == null)
        {
            return string.Empty;
        }

        string amountText = "x" + FormatMailAmountWithDots(reward.amount);
        if (reward.rewardType == MailRewardTypeGold || reward.rewardType == MailRewardTypeGem)
        {
            return amountText;
        }

        return reward.GetRewardTypeName() + " " + amountText;
    }

    private static string FormatMailAmountWithDots(int amount)
    {
        long safeAmount = System.Math.Max(1, (long)amount);
        string digits = safeAmount.ToString();
        StringBuilder builder = new StringBuilder(digits.Length + digits.Length / 3);
        int groupCount = 0;
        for (int i = digits.Length - 1; i >= 0; i--)
        {
            builder.Insert(0, digits[i]);
            groupCount++;
            if (groupCount == 3 && i > 0)
            {
                builder.Insert(0, '.');
                groupCount = 0;
            }
        }
        return builder.ToString();
    }

    private static Image GetMailRewardIcon(byte rewardType)
    {
        if (rewardType == MailRewardTypeGold)
        {
            return Panel.imgXu;
        }
        if (rewardType == MailRewardTypeGem)
        {
            return Panel.imgLuong;
        }
        return null;
    }

    private static void PaintMailClaimTextRewardLine(mGraphics g, Mail.RewardData reward, int centerX, int textY)
    {
        string text = BuildMailClaimRewardText(reward);
        Image icon = (reward == null) ? null : GetMailRewardIcon(reward.rewardType);
        if (icon == null)
        {
            mFont.tahoma_7_grey.drawString(g, text, centerX, textY, mFont.CENTER);
            return;
        }

        int iconW = icon.getWidth();
        int iconH = icon.getHeight();
        int textW = mFont.tahoma_7_grey.getWidth(text);
        int spacing = 3;
        int totalW = iconW + spacing + textW;
        int startX = centerX - totalW / 2;
        int iconY = textY + 5 - iconH / 2;

        g.drawImage(icon, startX, iconY);
        mFont.tahoma_7_grey.drawString(g, text, startX + iconW + spacing, textY, mFont.LEFT);
    }

    private static void PaintMailClaimPopup(mGraphics g)
    {
        if (!IsMailClaimPopupVisible())
        {
            return;
        }

        List<Mail.RewardData> itemRewards = new List<Mail.RewardData>();
        List<Mail.RewardData> textRewards = new List<Mail.RewardData>();
        List<string> detailTextRewards = new List<string>();
        for (int i = 0; i < mailClaimPopupRewards.Count; i++)
        {
            Mail.RewardData reward = mailClaimPopupRewards[i];
            if (reward == null)
            {
                continue;
            }

            if (reward.rewardType == MailRewardTypeItem && reward.rewardId > 0)
            {
                itemRewards.Add(reward);
            }
            else
            {
                textRewards.Add(reward);
            }
        }

        if (textRewards.Count == 0 && !string.IsNullOrEmpty(mailClaimPopupDetail))
        {
            string[] detailLines = mFont.tahoma_7_grey.splitFontArray(mailClaimPopupDetail, MailClaimPopupWidth + 20);
            for (int i = 0; i < detailLines.Length && i < 3; i++)
            {
                detailTextRewards.Add(detailLines[i]);
            }
        }

        int popupW = MailClaimPopupWidth;
        int popupH = MailClaimPopupHeight;
        if (itemRewards.Count > 0)
        {
            int displayItemCount = System.Math.Min(itemRewards.Count, MailClaimPopupItemColumns * 2);
            int rows = (displayItemCount + MailClaimPopupItemColumns - 1) / MailClaimPopupItemColumns;
            popupH += rows * (MailItemSlotSize + 2);
        }
        int textLineCount = (textRewards.Count > 0) ? textRewards.Count : detailTextRewards.Count;
        if (textLineCount > 0)
        {
            popupH += MailClaimPopupTextGap + textLineCount * 11;
        }
        mailClaimPopupTransition.Update(Time.deltaTime);
        if (!mailClaimPopupTransition.Active)
        {
            CloseMailClaimPopup(immediately: true);
            return;
        }
        int popupX = GameCanvas.w / 2 - popupW / 2;
        int popupY = GameCanvas.h / 2 - popupH / 2;
        mailClaimPopupTransition.TransformBounds(ref popupX, ref popupY, ref popupW, ref popupH);
        mailClaimPopupTransition.PaintDimBackground(g, 0.45f);
        mGraphics.OpacityState opacityState = mGraphics.PushOpacity(mailClaimPopupTransition.Opacity);
        PaintMailPopupBackground(g, popupX, popupY, popupW, popupH, -1);

        int titleY = popupY + 12;
        mFont.tahoma_7b_dark.drawString(g, mailClaimPopupTitle, popupX + popupW / 2, titleY, mFont.CENTER);

        int contentY = titleY + 18;
        if (itemRewards.Count > 0)
        {
            int displayItemCount = System.Math.Min(itemRewards.Count, MailClaimPopupItemColumns * 2);
            int rowWidth = MailClaimPopupItemColumns * MailItemSlotSize + (MailClaimPopupItemColumns - 1) * MailItemSlotGap;
            int slotStartX = popupX + (popupW - rowWidth) / 2;
            for (int i = 0; i < displayItemCount; i++)
            {
                int col = i % MailClaimPopupItemColumns;
                int row = i / MailClaimPopupItemColumns;
                int slotX = slotStartX + col * (MailItemSlotSize + MailItemSlotGap);
                int slotY = contentY + row * (MailItemSlotSize + 2);
                PaintMailRewardItemSlot(g, itemRewards[i], slotX, slotY);
            }

            if (itemRewards.Count > displayItemCount)
            {
                mFont.tahoma_7_grey.drawString(g, "+" + (itemRewards.Count - displayItemCount) + " vật phẩm", popupX + popupW / 2, popupY + popupH - 14, mFont.CENTER);
            }

            int rows = (displayItemCount + MailClaimPopupItemColumns - 1) / MailClaimPopupItemColumns;
            contentY += rows * (MailItemSlotSize + 2);
        }

        if (textRewards.Count > 0 || detailTextRewards.Count > 0)
        {
            contentY += MailClaimPopupTextGap;
            g.setColor(16776960, 0.2f);
            g.fillRect(popupX + 8, contentY - 4, popupW - 16, 1);
            int textY = contentY;
            if (textRewards.Count > 0)
            {
                for (int i = 0; i < textRewards.Count && i < 3; i++)
                {
                    PaintMailClaimTextRewardLine(g, textRewards[i], popupX + popupW / 2, textY);
                    textY += 11;
                }
            }
            else
            {
                for (int i = 0; i < detailTextRewards.Count && i < 3; i++)
                {
                    mFont.tahoma_7_grey.drawString(g, detailTextRewards[i], popupX + popupW / 2, textY, mFont.CENTER);
                    textY += 11;
                }
            }
        }
        mGraphics.PopOpacity(opacityState);
    }

    public static void PaintMenuChat(mGraphics g)
    {
        if (!isShowMenuChat)
        {
            return;
        }
        int menuChatX = (GameCanvas.w - imgMenuChat.getWidth()) / 2;
        int menuChatY = (GameCanvas.h - imgMenuChat.getHeight()) / 2;
        g.drawImage(imgMenuChat, menuChatX, menuChatY);
        g.drawImage(imgCloseButton, menuChatX + imgMenuChat.getWidth() - imgCloseButton.getWidth(), menuChatY);
        string chatInfo = "Nhập lệnh chat tại đây:";
        mFont.tahoma_7b_red.drawString(g, chatInfo, menuChatX + 30, menuChatY + 10, mFont.LEFT);
        Dictionary<string, string> chatCommands = new Dictionary<string, string>
        {
            { "ahs", "Bật/tắt auto hồi sinh" },
            { "loadskill", "Tải lại ô skill" },
            { "ak", "Bật/tắt tự động tấn công" },
            { "ts", "Bật/tắt chế độ tàn sát" },
            { "tsnguoi", "Bật/tắt chế độ tàn sát người" },
            { "ukhu", "Bật/tắt cập nhật khu tự động" },
            { "k X", "Chuyển đến khu X (VD: k 5)" },
            { "s X", "Thay đổi tốc độ game (1-10)" },
            { "atc text", "Thiết lập tin nhắn tự động" },
            { "atctg text", "Thiết lập tin nhắn tự động thế giới" },
            { "do text", "Thiết lập boss cần dò" },
            { "dbx", "Bật/tắt tự động dò boss" },
            { "gtv", "Bật/tắt gõ Tiếng Việt" }
        };
        int totalPages = (int)System.Math.Ceiling((double)chatCommands.Count / 8.0);
        int num = currentPage * 8;
        int endIndex = System.Math.Min(num + 8, chatCommands.Count);
        int commandY = menuChatY + 30;
        for (int i = num; i < endIndex; i++)
        {
            KeyValuePair<string, string> command = chatCommands.ElementAt(i);
            string commandText = command.Key + ": " + command.Value;
            mFont.tahoma_7_yellow.drawStringBorder(g, commandText, menuChatX + 35, commandY, mFont.LEFT, mFont.tahoma_7_grey);
            commandY += 15;
        }
        if (currentPage > 0)
        {
            g.drawImage(imgPrevPage, menuChatX + 30, menuChatY + imgMenuChat.getHeight() - 30);
            mFont.tahoma_7b_white.drawString(g, "Trang trước", menuChatX + 40, menuChatY + imgMenuChat.getHeight() - 22, mFont.LEFT);
        }
        if (currentPage < totalPages - 1)
        {
            g.drawImage(imgNextPage, menuChatX + imgMenuChat.getWidth() - 100, menuChatY + imgMenuChat.getHeight() - 30);
            mFont.tahoma_7b_white.drawString(g, "Trang sau", menuChatX + imgMenuChat.getWidth() - 43, menuChatY + imgMenuChat.getHeight() - 22, mFont.RIGHT);
        }
        PaintLogoGifMenu(g, menuChatX + imgMenuChat.getWidth() - 100, menuChatY + (imgMenuChat.getHeight() - FrameGifMenu) / 2, mFont.CENTER);
        if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {

            if (!GameCanvas.isPointerHoldIn(menuChatX, menuChatY, imgMenuChat.getWidth(), imgMenuChat.getHeight()))
            {
                isShowMenuChat = false;
                GameCanvas.clearAllPointerEvent();
                return;
            }
            if (GameCanvas.isPointerHoldIn(menuChatX + imgMenuChat.getWidth() - imgCloseButton.getWidth(), menuChatY, imgCloseButton.getWidth(), imgCloseButton.getHeight()))
            {
                isShowMenuChat = false;
                GameCanvas.clearAllPointerEvent();
                return;
            }
            if (currentPage > 0 && GameCanvas.isPointerHoldIn(menuChatX + 10, menuChatY + imgMenuChat.getHeight() - 30, 80, 20))
            {
                currentPage--;
            }
            if (currentPage < totalPages - 1 && GameCanvas.isPointerHoldIn(menuChatX + imgMenuChat.getWidth() - 80, menuChatY + imgMenuChat.getHeight() - 30, 80, 20))
            {
                currentPage++;
            }

            GameCanvas.clearAllPointerEvent();
        }
    }

    public static void PaintMailPanel(mGraphics g)
    {
        if (!isShowMailPanel)
        {
            return;
        }

        if (Mail.MailManager.Instance == null)
        {
            CloseMailPanel(resetPage: true);
            return;
        }


        if (GameCanvas.panel != null && GameCanvas.panel.isShow)
        {
            CloseMailPanel(resetPage: true);
            return;
        }

        int panelWidth = 230;
        int panelHeight = 220;
        int mailPanelX = (GameCanvas.w - panelWidth) / 2;
        int mailPanelY = (GameCanvas.h - panelHeight) / 2;
        bool isMailClaimPopupShowing = IsMailClaimPopupVisible();

        if (isMailClaimPopupShowing && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {
            CloseMailClaimPopup();
            GameCanvas.clearAllPointerEvent();
        }

        if (mailPreviewReward != null && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {
            CloseMailItemPreview();
            GameCanvas.clearAllPointerEvent();
        }


        PaintMailPopupBackground(g, mailPanelX, mailPanelY, panelWidth, panelHeight, -1);


        g.drawImage(imgCloseButton, mailPanelX + panelWidth - imgCloseButton.getWidth(), mailPanelY);


        // if (mailNotifyText != null)
        // {
        //     mailNotifyTimer -= Time.deltaTime;
        //     if (mailNotifyTimer <= 0f)
        //     {
        //         mailNotifyText = null;
        //     }
        //     else
        //     {
        //         mFont.tahoma_7_grey.drawString(g, mailNotifyText, mailPanelX + panelWidth / 2, mailPanelY + panelHeight - 12, mFont.CENTER);
        //     }
        // }
        


        if (isMailClaimPopupShowing)
        {
            if (!mailClaimPopupTransition.IsClosing)
            {
                mailClaimPopupTimer -= Time.deltaTime;
                if (mailClaimPopupTimer <= 0f)
                {
                    CloseMailClaimPopup();
                }
            }
        }


        bool isMailItemPreviewShowing = mailPreviewReward != null;

        if (mailPanelSelected >= 0)
        {

            Mail.MailData openedMail = Mail.MailManager.Instance.GetCurrentOpenedMail();
            mFont.tahoma_7b_dark.drawString(g, "Chi tiết thư", mailPanelX + panelWidth / 2, mailPanelY + 8, mFont.CENTER);

            if (openedMail == null || !mailDetailLoaded)
            {
                mFont.tahoma_7b_dark.drawStringBorder(g, "Đang tải...", mailPanelX + panelWidth / 2, mailPanelY + 60, mFont.CENTER, mFont.tahoma_7_grey);
            }
            else
            {
                int cy = mailPanelY + 24;

                mFont.tahoma_7b_dark.drawString(g, openedMail.title, mailPanelX + 8, cy, mFont.LEFT);
                cy += 14;


                int daysLeft = openedMail.GetDaysUntilExpire();
                string expiryStr = daysLeft > 0 ? "Còn " + daysLeft + " ngày" : "Đã hết hạn";
                mFont.tahoma_7_grey.drawString(g, expiryStr, mailPanelX + 8, cy, mFont.LEFT);
                cy += 14;


                g.setColor(16776960, 0.3f);
                g.fillRect(mailPanelX + 5, cy, panelWidth - 10, 1);
                cy += 4;


                if (openedMail.content != null && openedMail.content.Length > 0)
                {
                    int contentWidth = panelWidth - 16;
                    List<string> contentLines = BuildMailContentLines(openedMail.content, contentWidth, MailContentMaxLines);
                    for (int ci = 0; ci < contentLines.Count; ci++)
                    {
                        mFont.tahoma_7_grey.drawString(g, contentLines[ci], mailPanelX + 8, cy, mFont.LEFT);
                        cy += 11;
                    }
                }
                cy += 2;


                if (openedMail.rewards != null && openedMail.rewards.Count > 0)
                {
                    List<Mail.RewardData> itemRewards = new List<Mail.RewardData>();
                    List<Mail.RewardData> otherRewards = new List<Mail.RewardData>();
                    for (int ri = 0; ri < openedMail.rewards.Count; ri++)
                    {
                        Mail.RewardData rw = openedMail.rewards[ri];
                        if (rw.rewardType == MailRewardTypeItem && rw.rewardId > 0)
                        {
                            itemRewards.Add(rw);
                        }
                        else
                        {
                            otherRewards.Add(rw);
                        }
                    }

                    g.setColor(16776960, 0.3f);
                    g.fillRect(mailPanelX + 5, cy, panelWidth - 10, 1);
                    cy += 4;
                    mFont.tahoma_7b_dark.drawString(g, "Quà đính kèm thư:", mailPanelX + 8, cy, mFont.LEFT);
                    cy += 13;

                    int slotStartX = mailPanelX + 10;
                    int slotStartY = cy;
                    int maxDisplayItemSlots = MailItemSlotColumns * 2;
                    int itemSlotCount = System.Math.Min(itemRewards.Count, maxDisplayItemSlots);
                    for (int ri = 0; ri < itemSlotCount; ri++)
                    {
                        int col = ri % MailItemSlotColumns;
                        int row = ri / MailItemSlotColumns;
                        int slotX = slotStartX + col * (MailItemSlotSize + MailItemSlotGap);
                        int slotY = slotStartY + row * (MailItemSlotSize + MailItemSlotGap);
                        PaintMailRewardItemSlot(g, itemRewards[ri], slotX, slotY);
                    }

                    if (itemSlotCount > 0)
                    {
                        int rows = (itemSlotCount + MailItemSlotColumns - 1) / MailItemSlotColumns;
                        cy += rows * (MailItemSlotSize + MailItemSlotGap);
                    }

                    if (itemRewards.Count > itemSlotCount)
                    {
                        mFont.tahoma_7_grey.drawString(g, "+" + (itemRewards.Count - itemSlotCount) + " vật phẩm khác", mailPanelX + 10, cy - 2, mFont.LEFT);
                        cy += 10;
                    }

                    for (int ri = 0; ri < otherRewards.Count && ri < 3; ri++)
                    {
                        Mail.RewardData rw = otherRewards[ri];
                        string rwText = BuildMailClaimRewardText(rw);
                        string prefix = rw.claimed ? "✓ " : "• ";
                        Image rwIcon = GetMailRewardIcon(rw.rewardType);
                        int lineX = mailPanelX + 10;

                        if (rwIcon != null)
                        {
                            mFont.tahoma_7_grey.drawString(g, prefix, lineX, cy, mFont.LEFT);
                            int iconX = lineX + mFont.tahoma_7_grey.getWidth(prefix);
                            int iconY = cy + 5 - rwIcon.getHeight() / 2;
                            g.drawImage(rwIcon, iconX, iconY);
                            mFont.tahoma_7_grey.drawString(g, rwText, iconX + rwIcon.getWidth() + 3, cy, mFont.LEFT);
                        }
                        else
                        {
                            mFont.tahoma_7_grey.drawString(g, prefix + rwText, lineX, cy, mFont.LEFT);
                        }
                        cy += 11;
                    }
                }


                bool hasUnclaimed = false;
                System.Collections.Generic.List<long> unclaimedIds = new System.Collections.Generic.List<long>();
                if (openedMail.rewards != null)
                {
                    for (int ri = 0; ri < openedMail.rewards.Count; ri++)
                    {
                        if (!openedMail.rewards[ri].claimed)
                        {
                            hasUnclaimed = true;
                            unclaimedIds.Add(openedMail.rewards[ri].id);
                        }
                    }
                }

                int btnY = mailPanelY + panelHeight - 30;
                string backCaption = "Quay lại";
                string claimCaption = "Nhận thưởng";
                string deleteCaption = "Xóa";
                int backBtnW = GetMailActionButtonWidth(backCaption, 58);
                int claimBtnW = GetMailActionButtonWidth(claimCaption, 74);
                int deleteBtnW = GetMailActionButtonWidth(deleteCaption, MailButtonMinWidth);
                int backBtnX = mailPanelX + 8;
                int claimBtnX = mailPanelX + (panelWidth - claimBtnW) / 2;
                int deleteBtnX = mailPanelX + panelWidth - deleteBtnW - 8;

                PaintMailActionButton(g, backBtnX, btnY, backBtnW,
                    backCaption,
                    GameCanvas.isPointerHoldIn(backBtnX, btnY, backBtnW, MailButtonHeight));

                if (hasUnclaimed)
                {
                    PaintMailActionButton(g, claimBtnX, btnY, claimBtnW,
                        claimCaption,
                        GameCanvas.isPointerHoldIn(claimBtnX, btnY, claimBtnW, MailButtonHeight));
                }

                PaintMailActionButton(g, deleteBtnX, btnY, deleteBtnW,
                    deleteCaption,
                    GameCanvas.isPointerHoldIn(deleteBtnX, btnY, deleteBtnW, MailButtonHeight));


                if (mailConfirmDelete)
                {
                    int dlgW = 180; int dlgH = 60;
                    int dlgX = mailPanelX + (panelWidth - dlgW) / 2;
                    int dlgY = mailPanelY + (panelHeight - dlgH) / 2;
                    PaintMailPopupBackground(g, dlgX, dlgY, dlgW, dlgH, -1);
                    mFont.tahoma_7b_dark.drawString(g, "Xác nhận xóa thư này?", dlgX + dlgW / 2, dlgY + 14, mFont.CENTER);
                    int dlgBtnY = dlgY + dlgH - 26;
                    int dlgDeleteW = GetMailActionButtonWidth("Xóa", 52);
                    int dlgCancelW = GetMailActionButtonWidth("Hủy", 52);
                    int dlgDeleteX = dlgX + 10;
                    int dlgCancelX = dlgX + dlgW - dlgCancelW - 10;
                    PaintMailActionButton(g, dlgDeleteX, dlgBtnY, dlgDeleteW,
                        "Xóa",
                        GameCanvas.isPointerHoldIn(dlgDeleteX, dlgBtnY, dlgDeleteW, MailButtonHeight));
                    PaintMailActionButton(g, dlgCancelX, dlgBtnY, dlgCancelW,
                        "Hủy",
                        GameCanvas.isPointerHoldIn(dlgCancelX, dlgBtnY, dlgCancelW, MailButtonHeight));

                    if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                    {
                        if (GameCanvas.isPointerHoldIn(dlgDeleteX, dlgBtnY, dlgDeleteW, MailButtonHeight))
                        {

                            mailConfirmDelete = false;
                            long deleteId = openedMail.id;
                            Mail.MailManager.Instance.OnDeleteResult -= OnMailDeleteResult;
                            Mail.MailManager.Instance.OnDeleteResult += OnMailDeleteResult;
                            Mail.MailManager.Instance.DeleteMail(deleteId);
                            mailNotifyText = "Đang xóa...";
                            mailNotifyTimer = 10f;
                            GameCanvas.clearAllPointerEvent();
                            return;
                        }

                        mailConfirmDelete = false;
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                    return;
                }


                if (!isMailClaimPopupShowing && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    if (TryOpenMailRewardItemInfo(openedMail, mailPanelX, mailPanelY, panelWidth))
                    {
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (isMailItemPreviewShowing)
                    {
                        PaintMailItemPreview(g, mailPanelX, mailPanelY, panelWidth, panelHeight);
                        return;
                    }


                    if (GameCanvas.isPointerHoldIn(mailPanelX + panelWidth - imgCloseButton.getWidth(), mailPanelY, imgCloseButton.getWidth(), imgCloseButton.getHeight()))
                    {
                        CloseMailPanel(resetPage: false);
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (GameCanvas.isPointerHoldIn(backBtnX, btnY, backBtnW, MailButtonHeight))
                    {
                        mailPanelSelected = -1;
                        mailDetailLoaded = false;
                        Mail.MailManager.Instance.RefreshMailList();
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (hasUnclaimed && GameCanvas.isPointerHoldIn(claimBtnX, btnY, claimBtnW, MailButtonHeight))
                    {
                        mailPendingClaimRewards = new List<Mail.RewardData>();
                        for (int ri = 0; ri < openedMail.rewards.Count; ri++)
                        {
                            if (!openedMail.rewards[ri].claimed)
                            {
                                mailPendingClaimRewards.Add(new Mail.RewardData(
                                    openedMail.rewards[ri].id,
                                    openedMail.rewards[ri].rewardType,
                                    openedMail.rewards[ri].rewardId,
                                    openedMail.rewards[ri].amount,
                                    openedMail.rewards[ri].claimed,
                                    openedMail.rewards[ri].optionsData));
                            }
                        }
                        Mail.MailManager.Instance.OnRewardsClaimed -= OnMailRewardsClaimed;
                        Mail.MailManager.Instance.OnRewardsClaimed += OnMailRewardsClaimed;
                        Mail.MailManager.Instance.ClaimRewards(unclaimedIds);
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (GameCanvas.isPointerHoldIn(deleteBtnX, btnY, deleteBtnW, MailButtonHeight))
                    {
                        mailConfirmDelete = true;
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
            }
        }
        else
        {

            var mailList = Mail.MailManager.Instance.GetMailList();
            int totalCount = Mail.MailManager.Instance.GetTotalMailCount();
            //(" + totalCount + ")
            mFont.tahoma_7b_dark.drawString(g, "Hộp Thư", mailPanelX + panelWidth / 2, mailPanelY + 8, mFont.CENTER);

            int listStartY = mailPanelY + 24;
            int itemHeight = 22;
            int maxVisibleItems = 7;

            if (mailList == null || mailList.Count == 0)
            {
                 mFont.tahoma_7_grey.drawString(g, "Không có thư nào", mailPanelX + panelWidth / 2, mailPanelY + panelHeight / 2, mFont.CENTER);
            }
            else
            {
                int startIdx = mailPanelPage * maxVisibleItems;
                int endIdx = System.Math.Min(startIdx + maxVisibleItems, mailList.Count);
                int totalPages = (int)System.Math.Ceiling((double)mailList.Count / maxVisibleItems);

                for (int i = startIdx; i < endIdx; i++)
                {
                    Mail.MailData mail = mailList[i];
                    int itemY = listStartY + (i - startIdx) * itemHeight;
                    int itemX = mailPanelX + 4;
                    int itemWidth = panelWidth - 8;
                    int itemBoxHeight = itemHeight - 2;


                    if (!mail.isRead)
                    {
                        g.setColor(0xFFD700, 0.15f);
                        g.fillRect(itemX, itemY, itemWidth, itemBoxHeight, 3);
                    }
                    else
                    {
                        g.setColor(0xFFFFFF, 0.05f);
                        g.fillRect(itemX, itemY, itemWidth, itemBoxHeight, 3);
                    }
                    g.setColor(0x8E8E8E, 0.55f);
                    g.drawRect(itemX, itemY, itemWidth, itemBoxHeight);


                    string icon = mail.hasReward ? "★ " : string.Empty;


                    string title = mail.title;
                    if (title.Length > 22) title = title.Substring(0, 20) + "..";

                    if (!mail.isRead)
                    {
                        mFont.tahoma_7b_dark.drawString(g, icon + title, mailPanelX + 8, itemY + 3, mFont.LEFT);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, icon + title, mailPanelX + 8, itemY + 3, mFont.LEFT);
                    }


                    int days = mail.GetDaysUntilExpire();
                    string dayStr = days > 0 ? days + "d" : "Hết hạn";
                    mFont.tahoma_7_grey.drawString(g, dayStr, mailPanelX + panelWidth - 10, itemY + 3, mFont.RIGHT);

                    if (!mail.isRead)
                    {
                        PaintMailBadge(g, itemX, itemY, itemWidth, itemBoxHeight, 8);
                    }
                }


                int pageY = mailPanelY + panelHeight - 28;
                int pageInfoY = pageY - 11;
                string pagingInfoText = null;
                if (mailBulkDeleteInProgress)
                {
                    pagingInfoText = "Đang xóa thư...";
                }
                else if (mailBulkDeleteStatusTimer > 0f && !string.IsNullOrEmpty(mailBulkDeleteStatusText))
                {
                    pagingInfoText = mailBulkDeleteStatusText;
                }

                if (!string.IsNullOrEmpty(pagingInfoText))
                {
                    mFont.tahoma_7b_dark.drawString(g, pagingInfoText, mailPanelX + panelWidth / 2, pageInfoY, mFont.CENTER);
                }

                if (totalPages > 1)
                {
                    int pageBtnW = 34;
                    int prevBtnX = mailPanelX + 8;
                    int nextBtnX = mailPanelX + panelWidth - pageBtnW - 8;
                    if (string.IsNullOrEmpty(pagingInfoText))
                    {
                        string pageStr = (mailPanelPage + 1) + "/" + totalPages;
                        mFont.tahoma_7b_dark.drawString(g, pageStr, mailPanelX + panelWidth / 2, pageInfoY, mFont.CENTER);
                    }

                    if (mailPanelPage > 0)
                    {
                        PaintMailActionButton(g, prevBtnX, pageY, pageBtnW,
                            "<<",
                            GameCanvas.isPointerHoldIn(prevBtnX, pageY, pageBtnW, MailButtonHeight));
                    }
                    if (mailPanelPage < totalPages - 1)
                    {
                        PaintMailActionButton(g, nextBtnX, pageY, pageBtnW,
                            ">>",
                            GameCanvas.isPointerHoldIn(nextBtnX, pageY, pageBtnW, MailButtonHeight));
                    }
                }
            }

            string bulkDeleteCaption = "Xóa toàn bộ";
            int bulkDeleteBtnW = GetMailActionButtonWidth(bulkDeleteCaption, MailButtonMinWidth);
            int bulkDeleteBtnX = mailPanelX + (panelWidth - bulkDeleteBtnW) / 2;
            int bulkDeleteBtnY = mailPanelY + panelHeight - 28;
            PaintMailActionButton(g, bulkDeleteBtnX, bulkDeleteBtnY, bulkDeleteBtnW,
                bulkDeleteCaption,
                !mailBulkDeleteInProgress && GameCanvas.isPointerHoldIn(bulkDeleteBtnX, bulkDeleteBtnY, bulkDeleteBtnW, MailButtonHeight));


            if (!isMailClaimPopupShowing && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                if (isMailItemPreviewShowing)
                {
                    PaintMailItemPreview(g, mailPanelX, mailPanelY, panelWidth, panelHeight);
                    return;
                }


                if (GameCanvas.isPointerHoldIn(mailPanelX + panelWidth - imgCloseButton.getWidth(), mailPanelY, imgCloseButton.getWidth(), imgCloseButton.getHeight()))
                {
                    CloseMailPanel(resetPage: true);
                    SoundMn.gI().buttonClick();
                    GameCanvas.clearAllPointerEvent();
                    return;
                }

                if (mailList != null && mailList.Count > 0)
                {
                    if (mailBulkDeleteInProgress)
                    {
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    int totalPages = (int)System.Math.Ceiling((double)mailList.Count / maxVisibleItems);
                    int pageY = mailPanelY + panelHeight - 28;
                    int pageBtnW = 34;
                    int prevBtnX = mailPanelX + 8;
                    int nextBtnX = mailPanelX + panelWidth - pageBtnW - 8;


                    if (mailPanelPage > 0 && GameCanvas.isPointerHoldIn(prevBtnX, pageY, pageBtnW, MailButtonHeight))
                    {
                        mailPanelPage--;
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (mailPanelPage < totalPages - 1 && GameCanvas.isPointerHoldIn(nextBtnX, pageY, pageBtnW, MailButtonHeight))
                    {
                        mailPanelPage++;
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }

                    if (!mailBulkDeleteInProgress && GameCanvas.isPointerHoldIn(bulkDeleteBtnX, bulkDeleteBtnY, bulkDeleteBtnW, MailButtonHeight))
                    {
                        StartBulkDeleteReadMails();
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }


                    int startIdx = mailPanelPage * maxVisibleItems;
                    int endIdx = System.Math.Min(startIdx + maxVisibleItems, mailList.Count);
                    for (int i = startIdx; i < endIdx; i++)
                    {
                        int itemY = listStartY + (i - startIdx) * itemHeight;
                        if (GameCanvas.isPointerHoldIn(mailPanelX + 4, itemY, panelWidth - 8, itemHeight - 2))
                        {
                            mailPanelSelected = i;
                            mailDetailLoaded = false;
                            Mail.MailManager.Instance.OpenMail(mailList[i].id);

                            Mail.MailManager.Instance.OnMailOpened -= OnMailDetailLoaded;
                            Mail.MailManager.Instance.OnMailOpened += OnMailDetailLoaded;
                            SoundMn.gI().buttonClick();
                            GameCanvas.clearAllPointerEvent();
                            return;
                        }
                    }
                }
            }
        }

        if (mailNotifyText != null)
        {
            int popupW = 220;
            int popupH = 70;  
            int popupX = (GameCanvas.w - popupW) / 2;
            int popupY = (GameCanvas.h - popupH) / 2; 
            g.setColor(0);
            g.setColor(16777215);
            g.fillRect(popupX, popupY, popupW, popupH);
            
            g.setColor(8421504); 
            g.drawRect(popupX, popupY, popupW, popupH);
            mFont.tahoma_7b_dark.drawString(g, mailNotifyText, GameCanvas.w / 2, popupY + 20, mFont.CENTER);
            mFont.tahoma_7_grey.drawString(g, "(Bấm vị trí bất kỳ để đóng)", GameCanvas.w / 2, popupY + popupH - 18, mFont.CENTER);
        }

        if (isMailClaimPopupShowing)
        {
            PaintMailClaimPopup(g);
            return;
        }

        PaintMailItemPreview(g, mailPanelX, mailPanelY, panelWidth, panelHeight);
    }

    public static bool IsMailPanelVisible()
    {
        return isShowMailPanel;
    }

    private static void OnMailDetailLoaded(Mail.MailData mail)
    {
        mailDetailLoaded = true;
        Mail.MailManager.Instance.OnMailOpened -= OnMailDetailLoaded;
    }

    private static void CloseMailPanel(bool resetPage)
    {
        isShowMailPanel = false;
        mailPanelSelected = -1;
        mailDetailLoaded = false;
        mailConfirmDelete = false;
        mailBulkDeleteStatusText = null;
        mailBulkDeleteStatusTimer = 0f;
        CloseMailClaimPopup(immediately: true);
        CloseMailItemPreview(immediately: true);
        if (resetPage)
        {
            mailPanelPage = 0;
        }
    }

    private static bool MailHasUnclaimedRewards(Mail.MailData mail)
    {
        if (mail == null || mail.rewards == null)
        {
            return false;
        }

        for (int i = 0; i < mail.rewards.Count; i++)
        {
            Mail.RewardData reward = mail.rewards[i];
            if (reward != null && !reward.claimed)
            {
                return true;
            }
        }

        return false;
    }

    private static void StartBulkDeleteReadMails()
    {
        if (Mail.MailManager.Instance == null || mailBulkDeleteInProgress)
        {
            return;
        }

        List<Mail.MailData> mailList = Mail.MailManager.Instance.GetMailList();
        if (mailList == null || mailList.Count == 0)
        {
            mailNotifyText = "Không có thư để xóa.";
            mailNotifyTimer = 2f;
            return;
        }

        mailBulkDeleteNeedDetailIds.Clear();
        mailBulkDeleteReadyDeleteIds.Clear();
        mailBulkDeleteDeletedCount = 0;
        mailBulkDeleteCandidateCount = 0;
        mailBulkDeleteCurrentId = -1;
        mailBulkDeleteWaitingDetail = false;
        mailBulkDeleteWaitingDelete = false;

        for (int i = 0; i < mailList.Count; i++)
        {
            Mail.MailData mail = mailList[i];
            if (mail == null || !mail.isRead)
            {
                continue;
            }

            if (!mail.hasReward)
            {
                mailBulkDeleteReadyDeleteIds.Enqueue(mail.id);
                mailBulkDeleteCandidateCount++;
            }
            else
            {
                mailBulkDeleteNeedDetailIds.Enqueue(mail.id);
            }
        }

        if (mailBulkDeleteReadyDeleteIds.Count == 0 && mailBulkDeleteNeedDetailIds.Count == 0)
        {
            mailNotifyText = "Không có thư đã đọc hợp lệ để xóa.";
            mailNotifyTimer = 2.5f;
            return;
        }

        mailBulkDeleteInProgress = true;
        Mail.MailManager.Instance.OnMailOpened -= OnBulkDeleteMailDetailLoaded;
        Mail.MailManager.Instance.OnMailOpened += OnBulkDeleteMailDetailLoaded;
        Mail.MailManager.Instance.OnDeleteResult -= OnBulkDeleteDeleteResult;
        Mail.MailManager.Instance.OnDeleteResult += OnBulkDeleteDeleteResult;
        mailBulkDeleteStatusText = null;
        mailBulkDeleteStatusTimer = 0f;
        ContinueBulkDeleteReadMails();
    }

    private static void ContinueBulkDeleteReadMails()
    {
        if (!mailBulkDeleteInProgress || Mail.MailManager.Instance == null)
        {
            return;
        }

        if (mailBulkDeleteWaitingDetail || mailBulkDeleteWaitingDelete)
        {
            return;
        }

        if (mailBulkDeleteReadyDeleteIds.Count > 0)
        {
            mailBulkDeleteCurrentId = mailBulkDeleteReadyDeleteIds.Dequeue();
            mailBulkDeleteWaitingDelete = true;
            Mail.MailManager.Instance.DeleteMail(mailBulkDeleteCurrentId);
            return;
        }

        if (mailBulkDeleteNeedDetailIds.Count > 0)
        {
            mailBulkDeleteCurrentId = mailBulkDeleteNeedDetailIds.Dequeue();
            mailBulkDeleteWaitingDetail = true;
            Mail.MailManager.Instance.OpenMail(mailBulkDeleteCurrentId);
            return;
        }

        FinishBulkDeleteReadMails();
    }

    private static void FinishBulkDeleteReadMails()
    {
        if (!mailBulkDeleteInProgress)
        {
            return;
        }

        mailBulkDeleteInProgress = false;
        mailBulkDeleteWaitingDetail = false;
        mailBulkDeleteWaitingDelete = false;
        mailBulkDeleteCurrentId = -1;
        mailBulkDeleteNeedDetailIds.Clear();
        mailBulkDeleteReadyDeleteIds.Clear();

        if (Mail.MailManager.Instance != null)
        {
            Mail.MailManager.Instance.OnMailOpened -= OnBulkDeleteMailDetailLoaded;
            Mail.MailManager.Instance.OnDeleteResult -= OnBulkDeleteDeleteResult;
            Mail.MailManager.Instance.RefreshMailList();
        }

        int skipped = System.Math.Max(0, mailBulkDeleteCandidateCount - mailBulkDeleteDeletedCount);
        mailBulkDeleteStatusText = "Đã xóa " + mailBulkDeleteDeletedCount + " thư" + ((skipped > 0) ? (", bỏ qua " + skipped + " thư") : "") + ".";
        mailBulkDeleteStatusTimer = 3f;
        mailBulkDeleteCandidateCount = 0;
        mailBulkDeleteDeletedCount = 0;
    }

    private static void OnBulkDeleteMailDetailLoaded(Mail.MailData mail)
    {
        if (!mailBulkDeleteInProgress || !mailBulkDeleteWaitingDetail)
        {
            return;
        }

        mailBulkDeleteWaitingDetail = false;

        if (mail != null && mail.id == mailBulkDeleteCurrentId)
        {
            if (!MailHasUnclaimedRewards(mail))
            {
                mailBulkDeleteReadyDeleteIds.Enqueue(mail.id);
                mailBulkDeleteCandidateCount++;
            }
        }

        ContinueBulkDeleteReadMails();
    }

    private static void OnBulkDeleteDeleteResult(bool success, string message)
    {
        if (!mailBulkDeleteInProgress || !mailBulkDeleteWaitingDelete)
        {
            return;
        }

        mailBulkDeleteWaitingDelete = false;
        if (success)
        {
            mailBulkDeleteDeletedCount++;
        }

        ContinueBulkDeleteReadMails();
    }

    private static void OnMailDeleteResult(bool success, string message)
    {
        Mail.MailManager.Instance.OnDeleteResult -= OnMailDeleteResult;
        if (success)
        {
            mailPanelSelected = -1;
            mailDetailLoaded = false;
            mailNotifyText = message;
            mailNotifyTimer = 2f;
            Mail.MailManager.Instance.RefreshMailList();
        }
        else
        {
            mailNotifyText = message;
            mailNotifyTimer = 3f;
        }
    }

    private static void OnMailRewardsClaimed(int claimedCount, string message)
    {
        Mail.MailManager.Instance.OnRewardsClaimed -= OnMailRewardsClaimed;
        if (claimedCount > 0)
        {
            CloseMailItemPreview(immediately: true);
            ShowMailClaimPopup("Bạn nhận được", CloneMailRewards(mailPendingClaimRewards), message);
            mailPendingClaimRewards.Clear();
            return;
        }

        mailPendingClaimRewards.Clear();
        mailNotifyText = message;
        mailNotifyTimer = 3f;
    }

    private static bool HasUnreadMail()
    {
        return Mail.MailManager.Instance != null && Mail.MailManager.Instance.HasUnreadMail();
    }

    private static int GetMailBadgeSize()
    {
        return System.Math.Max(8, 10 / mGraphics.zoomLevel);
    }

    private static void PaintMailBadge(mGraphics g, int x, int y, int w, int h, int badgeSize)
    {
        int badgeX = x + w - badgeSize + 1;
        int badgeY = y - 1;
        int borderSize = badgeSize + 2;

        g.setColor(0xFFFFFF, 0.95f);
        g.FillRoundRect(badgeX - 1, badgeY - 1, borderSize, borderSize, 0, borderSize / 2);
        g.setColor(MailBadgeColor);
        g.FillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 0, badgeSize / 2);
    }

    private static List<string> BuildMailContentLines(string content, int lineWidth, int maxLines)
    {
        List<string> lines = new List<string>();
        bool isTruncated = false;
        if (string.IsNullOrEmpty(content) || maxLines <= 0)
        {
            return lines;
        }

        string normalizedContent = content.Replace("\r", string.Empty);
        string[] paragraphs = normalizedContent.Split('\n');
        for (int i = 0; i < paragraphs.Length && lines.Count < maxLines; i++)
        {
            string paragraph = paragraphs[i];
            if (paragraph.Length == 0)
            {
                lines.Add(string.Empty);
                continue;
            }

            string[] wrapped = mFont.tahoma_7_white.splitFontArray(paragraph, lineWidth);
            int beforeAddCount = lines.Count;
            for (int j = 0; j < wrapped.Length && lines.Count < maxLines; j++)
            {
                lines.Add(wrapped[j]);
            }

            int addedCount = lines.Count - beforeAddCount;
            if (lines.Count >= maxLines && wrapped.Length > addedCount)
            {
                isTruncated = true;
                break;
            }

            if (lines.Count >= maxLines && i < paragraphs.Length - 1)
            {
                isTruncated = true;
                break;
            }
        }

        if (lines.Count == maxLines && isTruncated)
        {
            string lastLine = lines[maxLines - 1];
            while (mFont.tahoma_7_white.getWidth(lastLine + "...") > lineWidth && lastLine.Length > 0)
            {
                lastLine = lastLine.Substring(0, lastLine.Length - 1);
            }
            lines[maxLines - 1] = lastLine + "...";
        }

        return lines;
    }

    private static void PaintMailRewardItemSlot(mGraphics g, Mail.RewardData reward, int x, int y)
    {
        bool isClaimed = reward != null && reward.claimed;
        g.setColor(MailItemBgColor, 0.95f);
        g.fillRect(x, y, MailItemSlotSize, MailItemSlotSize, 4);
        g.setColor(MailItemBgHighlightColor, 0.95f);
        g.fillRect(x + 1, y + 1, MailItemSlotSize - 2, 8, 3);
        g.setColor(isClaimed ? 0x888888 : MailItemBorderColor);
        g.drawRect(x, y, MailItemSlotSize, MailItemSlotSize);

        ItemTemplate itemTemplate = GetMailRewardItemTemplate(reward);
        if (itemTemplate != null)
        {
            SmallImage.drawSmallImage(g, itemTemplate.iconID, x + MailItemSlotSize / 2, y + MailItemSlotSize / 2 - 1, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            if (reward.amount > 1)
            {
                mFont.tahoma_7b_dark.drawString(g, Res.formatNumber2(reward.amount), x + MailItemSlotSize - 1, y + MailItemSlotSize - 8, mFont.RIGHT);
            }
        }
        else
        {
            mFont.tahoma_7b_dark.drawString(g, "?", x + MailItemSlotSize / 2, y + 8, mFont.CENTER);
        }

        if (isClaimed)
        {
            g.setColor(0x000000, 0.45f);
            g.fillRect(x, y, MailItemSlotSize, MailItemSlotSize, 4);
            mFont.tahoma_7b_dark.drawString(g, "✓", x + MailItemSlotSize / 2, y + 9, mFont.CENTER);
        }
    }

    private static bool TryOpenMailRewardItemInfo(Mail.MailData openedMail, int mailPanelX, int mailPanelY, int panelWidth)
    {
        if (openedMail == null || openedMail.rewards == null || openedMail.rewards.Count == 0)
        {
            return false;
        }

        List<Mail.RewardData> itemRewards = new List<Mail.RewardData>();
        for (int i = 0; i < openedMail.rewards.Count; i++)
        {
            Mail.RewardData rw = openedMail.rewards[i];
            if (rw.rewardType == MailRewardTypeItem && rw.rewardId > 0)
            {
                itemRewards.Add(rw);
            }
        }

        if (itemRewards.Count == 0)
        {
            return false;
        }

        int cy = mailPanelY + 24;
        cy += 14;
        cy += 14;
        cy += 5;

        if (!string.IsNullOrEmpty(openedMail.content))
        {
            List<string> contentLines = BuildMailContentLines(openedMail.content, panelWidth - 16, MailContentMaxLines);
            cy += contentLines.Count * 11;
        }
        cy += 2;

        cy += 5;
        cy += 13;

        int slotStartX = mailPanelX + 10;
        int slotStartY = cy;
        int maxDisplayItemSlots = MailItemSlotColumns * 2;
        int itemSlotCount = System.Math.Min(itemRewards.Count, maxDisplayItemSlots);
        for (int ri = 0; ri < itemSlotCount; ri++)
        {
            int col = ri % MailItemSlotColumns;
            int row = ri / MailItemSlotColumns;
            int slotX = slotStartX + col * (MailItemSlotSize + MailItemSlotGap);
            int slotY = slotStartY + row * (MailItemSlotSize + MailItemSlotGap);

            if (GameCanvas.isPointerHoldIn(slotX, slotY, MailItemSlotSize, MailItemSlotSize))
            {
                ShowMailRewardItemInfo(itemRewards[ri]);
                return true;
            }
        }

        return false;
    }

    private static ItemTemplate GetMailRewardItemTemplate(Mail.RewardData reward)
    {
        if (reward == null || reward.rewardType != MailRewardTypeItem || reward.rewardId <= 0)
        {
            return null;
        }

        return ItemTemplates.get((short)reward.rewardId);
    }

    private static void ShowMailRewardItemInfo(Mail.RewardData reward)
    {
        if (GetMailRewardItemTemplate(reward) == null)
        {
            return;
        }
        mailPreviewReward = reward;
        mailItemPreviewTransition.Show();
    }

    private static void CloseMailItemPreview(bool immediately = false)
    {
        if (!immediately)
        {
            mailItemPreviewTransition.Close();
            return;
        }
        mailItemPreviewTransition.CloseImmediately();
        mailPreviewReward = null;
    }

    public static int GetInventoryItemPreviewHeight(ItemTemplate itemTemplate, List<string> optionLines, int width)
    {
        if (itemTemplate == null) return 0;
        int optionCount = optionLines == null ? 0 : optionLines.Count;
        int topHeight = System.Math.Max(50, 24 + optionCount * 12);
        int powerLines = itemTemplate.strRequire > 1 ? 2 : 0;
        string[] descriptions = mFont.tahoma_7b_dark.splitFontArray(itemTemplate.description ?? string.Empty, width - 20);
        int descriptionCount = descriptions.Length;
        return System.Math.Max(72, topHeight + powerLines * 12 + 10 + descriptionCount * 12 + 8);
    }

    public static List<string> GetItemOptionLines(string optionsData, out int upgradeLevel)
    {
        List<string> lines = new List<string>();
        upgradeLevel = 0;
        if (string.IsNullOrEmpty(optionsData)) return lines;

        List<int[]> parsedOptions = new List<int[]>();
        MatchCollection objects = Regex.Matches(optionsData, "\\{[^{}]*\\}");
        foreach (Match itemObject in objects)
        {
            Match idMatch = Regex.Match(itemObject.Value, "\\\"id\\\"\\s*:\\s*(-?\\d+)");
            Match paramMatch = Regex.Match(itemObject.Value, "\\\"param\\\"\\s*:\\s*(-?\\d+)");
            int id, param;
            if (idMatch.Success && paramMatch.Success
                && int.TryParse(idMatch.Groups[1].Value, out id)
                && int.TryParse(paramMatch.Groups[1].Value, out param))
                parsedOptions.Add(new[] { id, param });
        }

        if (parsedOptions.Count == 0)
        {
            MatchCollection arrays = Regex.Matches(optionsData, "\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\]");
            foreach (Match optionArray in arrays)
            {
                int id, param;
                if (int.TryParse(optionArray.Groups[1].Value, out id)
                    && int.TryParse(optionArray.Groups[2].Value, out param))
                    parsedOptions.Add(new[] { id, param });
            }
        }

        for (int i = 0; i < parsedOptions.Count; i++)
        {
            int id = parsedOptions[i][0];
            int param = parsedOptions[i][1];
            if (id == 72 || id == 225)
            {
                upgradeLevel = System.Math.Max(upgradeLevel, param);
                continue;
            }
            if (id == 102 || id == 107) continue;
            ItemOption option = new ItemOption(id, param);
            if (option.optionTemplate == null) continue;
            string text = option.optionTemplate.name.StartsWith("$")
                ? option.getOptiongColor() : option.getOptionString();
            if (!string.IsNullOrEmpty(text)) lines.Add(text);
        }
        return lines;
    }

    private static List<string> GetMailItemOptionLines(Mail.RewardData reward, out int upgradeLevel)
    {
        return GetItemOptionLines(reward == null ? null : reward.optionsData, out upgradeLevel);
    }

    public static void PaintInventoryItemPreview(mGraphics g, int x, int y, int width, int height,
        ItemTemplate itemTemplate, List<string> optionLines, int upgradeLevel, bool showId)
    {
        if (itemTemplate == null) return;
        PopUp.paintPopUp(g, x, y, width, height, 16777215, false);
        SmallImage.drawSmallImage(g, itemTemplate.iconID, x + 17, y + 18, 0,
            mGraphics.VCENTER | mGraphics.HCENTER);

        string itemName = (showId ? "[" + itemTemplate.id + "] " : string.Empty) + itemTemplate.name;
        if (upgradeLevel > 0) itemName += " [+" + upgradeLevel + "]";
        mFont.tahoma_7b_dark.drawString(g, itemName, x + width - 9, y + 8, mFont.RIGHT);

        int optionCount = optionLines == null ? 0 : optionLines.Count;
        for (int i = 0; i < optionCount; i++)
            mFont.tahoma_7b_green.drawString(g, optionLines[i], x + width - 9, y + 20 + i * 12, mFont.RIGHT);

        int contentY = y + System.Math.Max(50, 24 + optionCount * 12);
        if (itemTemplate.strRequire > 1)
        {
            bool notEnoughPower = itemTemplate.strRequire > Char.myCharz().cPower;
            mFont powerFont = notEnoughPower ? mFont.tahoma_7_red : mFont.tahoma_7b_dark;
            powerFont.drawString(g, mResources.pow_request + ": " + itemTemplate.strRequire,
                x + width - 9, contentY, mFont.RIGHT);
            contentY += 12;
            powerFont.drawString(g, mResources.your_pow + ": " + Char.myCharz().cPower,
                x + width - 9, contentY, mFont.RIGHT);
            contentY += 12;
        }

        g.setColor(0);
        g.fillRect(x + 10, contentY + 2, width - 20, 1);
        contentY += 10;
        string[] descriptions = mFont.tahoma_7b_dark.splitFontArray(itemTemplate.description ?? string.Empty, width - 20);
        int descriptionCount = descriptions.Length;
        for (int i = 0; i < descriptionCount; i++)
        {
            mFont.tahoma_7b_dark.drawString(g, descriptions[i], x + width / 2, contentY, mFont.CENTER);
            contentY += 12;
        }
    }

    private static void PaintMailItemPreview(mGraphics g, int mailPanelX, int mailPanelY, int panelWidth, int panelHeight)
    {
        if (mailPreviewReward == null)
        {
            return;
        }

        ItemTemplate itemTemplate = GetMailRewardItemTemplate(mailPreviewReward);
        if (itemTemplate == null)
        {
            CloseMailItemPreview(immediately: true);
            return;
        }

        int popupW = System.Math.Min(MailItemPreviewWidth, GameCanvas.w - 8);
        int upgradeLevel;
        List<string> optionLines = GetMailItemOptionLines(mailPreviewReward, out upgradeLevel);
        int popupH = GetInventoryItemPreviewHeight(itemTemplate, optionLines, popupW);
        int popupX = (GameCanvas.w - popupW) / 2;
        int popupY = (GameCanvas.h - popupH) / 2;

        if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
        {
            if (!GameCanvas.isPointerHoldIn(popupX, popupY, popupW, popupH))
            {
                CloseMailItemPreview();
                GameCanvas.clearAllPointerEvent();
            }
        }
        mailItemPreviewTransition.Update(Time.deltaTime);
        if (!mailItemPreviewTransition.Active)
        {
            CloseMailItemPreview(immediately: true);
            return;
        }
        mailItemPreviewTransition.PaintDimBackground(g, 0.55f);
        Matrix4x4 previousMatrix = mailItemPreviewTransition.PushScale(popupX + popupW / 2, popupY + popupH / 2);
        mGraphics.OpacityState opacityState = mGraphics.PushOpacity(mailItemPreviewTransition.Opacity);
        PaintInventoryItemPreview(g, popupX, popupY, popupW, popupH, itemTemplate, optionLines, upgradeLevel, isShowID);
        mGraphics.PopOpacity(opacityState);
        mailItemPreviewTransition.PopScale(previousMatrix);
    }


    public static void SetDataType(DataType dataType)
    {
        currentDataType = dataType;
        switch (dataType)
        {
            case DataType.Int:
                isReadInt = true;
                isReadDouble = false;
                break;
            case DataType.Double:
                isReadInt = false;
                isReadDouble = true;
                break;
            case DataType.Long:
                isReadInt = false;
                isReadDouble = false;
                break;
        }
        SaveDataTypeConfig();
    }


    public static void SaveDataTypeConfig()
    {
        Rms.saveRMSInt("dataType", (int)currentDataType);
    }


    public static void LoadDataTypeConfig()
    {
        int savedType = Rms.loadRMSInt("dataType");
        if (savedType >= 0 && savedType <= 2)
        {
            currentDataType = (DataType)savedType;

            switch (currentDataType)
            {
                case DataType.Int:
                    isReadInt = true;
                    isReadDouble = false;
                    break;
                case DataType.Double:
                    isReadInt = false;
                    isReadDouble = true;
                    break;
                case DataType.Long:
                    isReadInt = false;
                    isReadDouble = false;
                    break;
            }
        }
    }


    public static void ChangeDataType()
    {
        DataType nextType = (DataType)(((int)currentDataType + 1) % 3);
        SetDataType(nextType);
        GameScr.info1.addInfo("Đã chuyển sang kiểu dữ liệu: " + GetDataTypeString(nextType), 0);
    }


    public static string GetDataTypeString(DataType dataType)
    {
        switch (dataType)
        {
            case DataType.Int:
                return "Int";
            case DataType.Double:
                return "Double";
            case DataType.Long:
                return "Long";
            default:
                return "Unknown";
        }
    }

    // Hàm tự chế vẽ ô vuông bo góc đổ màu (Fill)
    public void fillRoundRectCustom(mGraphics g, int x, int y, int width, int height, int radius)
    {
        // Vẽ phần thân chính giữa (bỏ qua các góc)
        g.fillRect(x + radius, y, width - 2 * radius, height);
        g.fillRect(x, y + radius, width, height - 2 * radius);
    }

    // Hàm tự chế vẽ viền bo góc (Draw)
    public void drawRoundRectCustom(mGraphics g, int x, int y, int width, int height, int radius)
    {
        // Vẽ 4 cạnh thẳng xung quanh
        g.fillRect(x + radius, y, width - 2 * radius, 1); // Cạnh trên
        g.fillRect(x + radius, y + height - 1, width - 2 * radius, 1); // Cạnh dưới
        g.fillRect(x, y + radius, 1, height - 2 * radius); // Cạnh trái
        g.fillRect(x + width - 1, y + radius, 1, height - 2 * radius); // Cạnh phải
        
        // Chấm 4 điểm ở góc tạo độ bo nhẹ gọn gàng
        g.fillRect(x + radius, y + radius, 1, 1);
        g.fillRect(x + width - radius - 1, y + radius, 1, 1);
        g.fillRect(x + radius, y + height - radius - 1, 1, 1);
        g.fillRect(x + width - radius - 1, y + height - radius - 1, 1, 1);
    }
}
