using System;
using System.Collections.Generic;

namespace DragonPass
{
    public static class TopBoardUI
    {
        private static byte board;
        private static readonly Scroll listScroll = new Scroll();
        private static readonly Dictionary<int, RewardRowScroll> rowRewardScrolls = new Dictionary<int, RewardRowScroll>();
        private static readonly RewardRowScroll pinnedRewardScroll = new RewardRowScroll();
        private static RewardRowScroll activeRewardScroll;
        private static TopEntryView activeRewardEntry;
        private static readonly Image[] rankIcons = new Image[3];
        private const int RowH = 42;
        private const int RewardCellW = 31;

        public static bool HasPointerCapture
        {
            get { return activeRewardScroll != null || listScroll.pointerIsDowning; }
        }

        public static void Open()
        {
            board = 0;
            listScroll.clear();
            rowRewardScrolls.Clear();
            pinnedRewardScroll.Clear();
            activeRewardScroll = null;
            activeRewardEntry = null;
            Network.GetTop(board);
        }

        public static void OnReloaded(byte loadedBoard)
        {
            if (loadedBoard != board) return;
            listScroll.clear();
            rowRewardScrolls.Clear();
            pinnedRewardScroll.Clear();
            activeRewardScroll = null;
            activeRewardEntry = null;
        }

        public static void Paint(mGraphics g, int x, int y, int w, int h)
        {
            TopBoardState state = TopBoardManager.Boards[board];
            int tabW = System.Math.Min(100, (w - 9) / 2);
            PaintButton(g, x + 3, y + 3, tabW, "Siêu Hạng", board == 0);
            PaintButton(g, x + 6 + tabW, y + 3, tabW, "Whis", board == 1);

            int contentX = x + 3;
            int contentW = w - 6;
            int oldRewardW = contentW - contentW / 2;
            int rewardW = System.Math.Max(RewardCellW, oldRewardW * 7 / 10);
            int leftW = contentW - rewardW;
            int rewardX = contentX + leftW;
            int rankW = System.Math.Min(42, System.Math.Max(32, leftW / 5));
            int metricW = board == 0
                ? System.Math.Min(62, System.Math.Max(46, leftW / 4))
                : System.Math.Min(86, System.Math.Max(58, leftW / 3));
            int infoW = System.Math.Max(1, leftW - rankW - metricW);
            int headerY = y + 32;

            g.setColor(0xB7773D);
            g.fillRect(contentX, headerY, contentW, 22, 5);
            mFont.tahoma_7b_white.drawString(g, "Hạng", contentX + rankW / 2, headerY + 6, mFont.CENTER);
            mFont.tahoma_7b_white.drawString(g, "Thông tin", contentX + rankW + infoW / 2, headerY + 6, mFont.CENTER);
            mFont.tahoma_7b_white.drawString(g, board == 0 ? "Điểm" : "Thành tích",
                contentX + rankW + infoW + metricW / 2, headerY + 6, mFont.CENTER);
            mFont.tahoma_7b_white.drawString(g, "Thưởng", rewardX + rewardW / 2, headerY + 6, mFont.CENTER);

            int listY = headerY + 24;
            int pinnedH = state.self == null ? 0 : RowH + 3;
            int listH = System.Math.Max(20, h - (listY - y) - pinnedH - 2);
            ConfigureList(contentX, listY, leftW, listH, state.entries.Count);

            g.setClip(contentX, listY, contentW, listH);
            int first = System.Math.Max(0, listScroll.cmy / RowH - 1);
            int last = System.Math.Min(state.entries.Count, first + listH / RowH + 3);
            for (int i = first; i < last; i++)
            {
                PaintRow(g, state.entries[i], contentX, listY + i * RowH - listScroll.cmy,
                    contentW, leftW, rankW, infoW, metricW, false, listY, listH, GetRowRewardScroll(i));
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            if (state.self != null)
                PaintRow(g, state.self, contentX, y + h - RowH - 1,
                    contentW, leftW, rankW, infoW, metricW, true,
                    y + h - RowH - 1, RowH, pinnedRewardScroll);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        public static bool HandleInput(int x, int y, int w, int h)
        {
            int tabW = System.Math.Min(100, (w - 9) / 2);
            if (activeRewardScroll == null && !listScroll.pointerIsDowning
                && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
            {
                byte selected = 255;
                if (GameCanvas.isPointerHoldIn(x + 3, y + 3, tabW, 24)) selected = 0;
                else if (GameCanvas.isPointerHoldIn(x + 6 + tabW, y + 3, tabW, 24)) selected = 1;
                if (selected <= 1 && selected != board)
                {
                    SoundMn.gI().buttonClick();
                    board = selected;
                    listScroll.clear();
                    rowRewardScrolls.Clear();
                    pinnedRewardScroll.Clear();
                    activeRewardScroll = null;
                    activeRewardEntry = null;
                    Network.GetTop(board);
                    GameCanvas.clearAllPointerEvent();
                    return true;
                }
            }

            TopBoardState state = TopBoardManager.Boards[board];
            int contentX = x + 3, contentW = w - 6;
            int oldRewardW = contentW - contentW / 2;
            int rewardW = System.Math.Max(RewardCellW, oldRewardW * 7 / 10);
            int leftW = contentW - rewardW;
            int rewardX = contentX + leftW;
            int listY = y + 56;
            int pinnedH = state.self == null ? 0 : RowH + 3;
            int listH = System.Math.Max(20, h - 56 - pinnedH);

            // Hàng đang kéo vẫn nhận được sự kiện thả dù chuột đã đi ra ngoài vùng thưởng.
            if (activeRewardScroll != null)
            {
                bool released = GameCanvas.isPointerJustRelease;
                int selected = activeRewardScroll.UpdateDrag(rewardX, rewardW);
                if (!activeRewardScroll.dragging) activeRewardScroll = null;
                if (selected >= 0 && activeRewardEntry != null && selected < activeRewardEntry.rewards.Count)
                {
                    SoundMn.gI().buttonClick();
                    ShowReward(activeRewardEntry.rewards[selected]);
                    activeRewardEntry = null;
                    GameCanvas.clearAllPointerEvent();
                    return true;
                }
                if (selected == RewardRowScroll.Consumed)
                {
                    if (activeRewardScroll == null) activeRewardEntry = null;
                    if (released) GameCanvas.clearAllPointerEvent();
                    return true;
                }
            }

            if (GameCanvas.isPointerJustDown && GameCanvas.px >= rewardX && GameCanvas.px < rewardX + rewardW)
            {
                bool pinned;
                int rowIndex;
                TopEntryView entry = EntryAtPointer(state, listY, listH,
                    y + h - RowH - 1, out pinned, out rowIndex);
                if (entry != null)
                {
                    RewardRowScroll rowScroll = pinned ? pinnedRewardScroll : GetRowRewardScroll(rowIndex);
                    rowScroll.Configure(entry.rewards.Count, rewardW);
                    rowScroll.Begin();
                    activeRewardScroll = rowScroll;
                    activeRewardEntry = entry;
                    return true;
                }
            }

            // Cuộn dọc đã bắt đầu thì giữ chuột tới lúc thả, không cho hàng thưởng nhận lại giữa chừng.
            ConfigureList(contentX, listY, leftW, listH, state.entries.Count);
            bool listWasDragging = listScroll.pointerIsDowning;
            ScrollResult listResult = listScroll.updateKey();
            listScroll.updatecm();
            if (listWasDragging || listResult.isDowning) return true;
            return false;
        }

        private static TopEntryView EntryAtPointer(TopBoardState state, int listY, int listH,
            int pinnedY, out bool pinned, out int rowIndex)
        {
            pinned = false;
            rowIndex = -1;
            if (state.self != null && GameCanvas.py >= pinnedY && GameCanvas.py < pinnedY + RowH)
            {
                pinned = true;
                return state.self;
            }
            if (GameCanvas.py < listY || GameCanvas.py >= listY + listH) return null;
            rowIndex = (GameCanvas.py - listY + listScroll.cmy) / RowH;
            return rowIndex >= 0 && rowIndex < state.entries.Count ? state.entries[rowIndex] : null;
        }

        private static void ShowReward(TopRewardView reward)
        {
            RewardData data = new RewardData();
            data.configured = true;
            data.rewardType = reward.type;
            data.itemId = reward.type == 2 ? (short)76 : (reward.type == 3 ? (short)77 : reward.itemId);
            data.quantity = reward.amount;
            data.options = "";
            DragonPassUI.ShowExternalRewardPreview(data);
        }

        private static void ConfigureList(int x, int y, int w, int h, int count)
        {
            listScroll.setStyle(count, RowH, x, y, w, h, true, 1);
            listScroll.cmyLim = System.Math.Max(0, count * RowH - h);
            if (listScroll.cmtoY > listScroll.cmyLim) listScroll.cmtoY = listScroll.cmyLim;
        }

        private static RewardRowScroll GetRowRewardScroll(int rowIndex)
        {
            RewardRowScroll value;
            if (!rowRewardScrolls.TryGetValue(rowIndex, out value))
            {
                value = new RewardRowScroll();
                rowRewardScrolls[rowIndex] = value;
            }
            return value;
        }

        private static void PaintRow(mGraphics g, TopEntryView entry, int x, int y, int w,
            int leftW, int rankW, int infoW, int metricW, bool pinned, int viewportY, int viewportH,
            RewardRowScroll rowScroll)
        {
            int rowH = RowH - 2;
            int clipTop = System.Math.Max(y, viewportY);
            int clipBottom = System.Math.Min(y + rowH, viewportY + viewportH);
            if (clipBottom <= clipTop) return;
            int rewardX = x + leftW;
            int rewardW = w - leftW;
            int baseColor = pinned ? 0xE4A85E : ((entry.rank & 1) == 0 ? 0xD99A55 : 0xD18D4D);
            g.setClip(x, clipTop, w, clipBottom - clipTop);
            g.setColor(baseColor);
            g.fillRect(x, y, w, rowH, 4);

            // Mỗi hàng có vị trí cuộn riêng. Thưởng được vẽ dưới lớp thông tin cố định.
            int avatarSize = System.Math.Min(30, System.Math.Max(22, infoW - 22));
            int rewardSize = System.Math.Max(1, avatarSize * 9 / 10);
            rowScroll.Configure(entry.rewards.Count, rewardW);
            rowScroll.Tick();
            g.setClip(rewardX, clipTop, rewardW, clipBottom - clipTop);
            for (int i = 0; i < entry.rewards.Count; i++)
                PaintReward(g, entry.rewards[i], rewardX + i * RewardCellW - rowScroll.position, y, rowH, rewardSize);

            // Rank, avatar, thông tin và thành tích là lớp trên, nhưng luôn bị cắt trong viewport danh sách.
            g.setClip(x, clipTop, leftW, clipBottom - clipTop);

            g.setColor(pinned ? 0xD18D4D : 0xC98B4C);
            g.fillRect(x, y, rankW, rowH, 4);
            PaintRank(g, entry.rank, x + rankW / 2, y + rowH / 2);

            int avatarX = x + rankW + 4;
            int avatarY = y + (rowH - avatarSize) / 2;
            g.setColor(pinned ? 0xE8B64B : 0xB7773D);
            g.fillRect(avatarX, avatarY, avatarSize, avatarSize, 4);
            g.setColor(0xE1E2DE);
            g.fillRect(avatarX + 2, avatarY + 2, avatarSize - 4, avatarSize - 4, 3);
            PaintHead(g, entry.head, avatarX + avatarSize / 2, avatarY + avatarSize / 2);

            int infoX = avatarX + avatarSize + 5;
            int infoClipW = System.Math.Max(24, infoW - avatarSize - 11);
            g.setClip(infoX, clipTop, infoClipW, clipBottom - clipTop);
            mFont.tahoma_7b_white.drawString(g, entry.name, infoX, y + 5, mFont.LEFT);
            string guild = string.IsNullOrEmpty(entry.guild) ? "Không bang hội" : "Bang: " + entry.guild;
            mFont.tahoma_7_white.drawString(g, guild, infoX, y + 22, mFont.LEFT);
            g.setClip(x, clipTop, leftW, clipBottom - clipTop);

            int metricX = x + rankW + infoW;
            string metric = board == 0 ? entry.primaryValue + " thắng"
                : "LV " + entry.primaryValue + " • " + FormatTime(entry.timeMs);
            mFont.tahoma_7b_white.drawString(g, metric, metricX + metricW / 2, y + 14, mFont.CENTER);
        }

        private static void PaintReward(mGraphics g, TopRewardView reward, int x, int y, int rowH, int size)
        {
            int boxX = x + (RewardCellW - size) / 2;
            int boxY = y + (rowH - size) / 2;
            ModFunc.PaintInventoryItemSlot(g, boxX, boxY, size, size, false, true);
            int itemId = reward.type == 2 ? 76 : (reward.type == 3 ? 77 : reward.itemId);
            ItemTemplate template = ItemTemplates.get((short)itemId);
            if (template != null)
                SmallImage.drawSmallImage(g, template.iconID, boxX + size / 2, boxY + size / 2 - 1, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
            mFont.tahoma_7b_yellowSmall2.drawString(g, Compact(reward.amount),
                boxX + size - 2, boxY + size - 9, mFont.RIGHT);
        }

        private static void PaintHead(mGraphics g, short head, int centerX, int centerY)
        {
            try
            {
                Part part = GameScr.parts[head];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id,
                    centerX, centerY, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            catch (Exception) { }
        }

        private static void PaintRank(mGraphics g, int rank, int x, int y)
        {
            if (rank < 1) { mFont.tahoma_7b_white.drawString(g, "--", x, y - 4, mFont.CENTER); return; }
            if (rank <= 3)
            {
                EnsureRankIcons();
                Image icon = rankIcons[rank - 1];
                if (icon != null) { g.drawImageScaleClipped(icon, x - 15, y - 15, 30, 30); return; }
            }
            mFont.tahoma_7b_white.drawString(g, rank.ToString(), x, y - 4, mFont.CENTER);
        }

        private sealed class RewardRowScroll
        {
            public const int Consumed = -2;
            public int position;
            public bool dragging;
            private int target;
            private int limit;
            private int startX;
            private int startPosition;
            private int previousX;
            private int releaseSpeed;
            private bool moved;

            public void Clear()
            {
                position = 0;
                target = 0;
                limit = 0;
                dragging = false;
                moved = false;
                releaseSpeed = 0;
            }

            public void Configure(int count, int viewportW)
            {
                limit = System.Math.Max(0, count * RewardCellW - viewportW);
                target = Clamp(target, 0, limit);
                if (!dragging && (position < -18 || position > limit + 18)) position = Clamp(position, 0, limit);
            }

            public void Begin()
            {
                dragging = true;
                moved = false;
                startX = previousX = GameCanvas.px;
                startPosition = position;
                releaseSpeed = 0;
            }

            public int UpdateDrag(int viewportX, int viewportW)
            {
                if (!dragging) return -1;
                if (GameCanvas.isPointerDown)
                {
                    int delta = GameCanvas.px - previousX;
                    releaseSpeed = -delta;
                    previousX = GameCanvas.px;
                    int raw = startPosition - (GameCanvas.px - startX);
                    if (raw < 0) raw /= 3;
                    else if (raw > limit) raw = limit + (raw - limit) / 3;
                    position = raw;
                    if (System.Math.Abs(GameCanvas.px - startX) > 6) moved = true;
                    return Consumed;
                }
                if (!GameCanvas.isPointerJustRelease) return Consumed;

                dragging = false;
                target = Clamp(position + releaseSpeed * 5, 0, limit);
                bool inside = GameCanvas.px >= viewportX && GameCanvas.px < viewportX + viewportW;
                if (!moved && inside)
                    return (position + GameCanvas.px - viewportX) / RewardCellW;
                return Consumed;
            }

            public void Tick()
            {
                if (dragging) return;
                int distance = target - position;
                if (System.Math.Abs(distance) <= 1) position = target;
                else position += distance / 3 == 0 ? (distance > 0 ? 1 : -1) : distance / 3;
                position = Clamp(position, -18, limit + 18);
            }

            private static int Clamp(int value, int min, int max)
            {
                return value < min ? min : (value > max ? max : value);
            }
        }

        private static void EnsureRankIcons()
        {
            if (rankIcons[0] == null) rankIcons[0] = GameCanvas.loadImage("/rank_icons/UI_Rank_Icon_Medal_Gold.png");
            if (rankIcons[1] == null) rankIcons[1] = GameCanvas.loadImage("/rank_icons/UI_Rank_Icon_Medal_Silver.png");
            if (rankIcons[2] == null) rankIcons[2] = GameCanvas.loadImage("/rank_icons/UI_Rank_Icon_Medal_Bronze.png");
        }

        private static void PaintButton(mGraphics g, int x, int y, int w, string text, bool active)
        {
            ModFunc.PaintMailActionButton(g, x, y, w, text,
                active || (GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(x, y, w, 24)));
        }

        private static string FormatTime(long ms) { return (ms / 1000d).ToString("0.0") + "s"; }
        private static string Compact(int n)
        {
            if (n >= 1000000000) return (n / 1000000000f).ToString("0.#") + "B";
            if (n >= 1000000) return (n / 1000000f).ToString("0.#") + "M";
            if (n >= 1000) return (n / 1000f).ToString("0.#") + "K";
            return n.ToString();
        }
    }
}
