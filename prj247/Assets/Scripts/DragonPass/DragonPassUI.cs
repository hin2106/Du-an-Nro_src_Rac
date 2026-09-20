using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace DragonPass
{
    public static class DragonPassUI
    {
        public static bool Visible;
        private static int tab;
        private static int eventIndex;
        private static byte confirmPassType;
        private static long lastRefresh;
        private static bool alignRewardScrollOnNextState;
        private static readonly Scroll eventScroll = new Scroll();
        private static readonly Scroll rewardScroll = new Scroll();
        private static readonly Scroll taskScroll = new Scroll();
        private const int TaskRowHeight = 40;
        private const int EventRowHeight = 29;
        private const int EventButtonHeight = 24;
        private const int RewardContentOffsetY = 5;
        private static RewardData previewReward;
        private static readonly PopupTransition rewardPreviewTransition = new PopupTransition(PopupTransitionType.ItemInfo);
        private static readonly PopupTransition rewardPopupTransition = new PopupTransition(PopupTransitionType.Reward);
        private static Image claimedCheckmark;
        private static Image closedLockIcon;
        private static TField giftCodeInput;

        public static bool BlocksInput
        {
            get { return Manager.Instance.RewardPopupSeconds > 0f || rewardPopupTransition.Active; }
        }

        public static void HandleBlockingInput()
        {
            if (!BlocksInput) return;
            if (GameCanvas.isPointerClick)
            {
                SoundMn.gI().buttonClick();
                Manager.Instance.RewardPopupSeconds = 0f;
                rewardPopupTransition.Close();
                GameCanvas.clearAllPointerEvent();
            }
        }

        public static void Open()
        {
            Visible = true;
            tab = 0;
            eventIndex = 0;
            eventScroll.clear();
            rewardScroll.clear();
            taskScroll.clear();
            State state = Manager.Instance.Current;
            AlignRewardScroll(state);
            alignRewardScrollOnNextState = true;
            Network.GetState();
            LuckyWheel.Network.GetState();
        }

        public static void Close()
        {
            Visible = false;
            confirmPassType = 0;
            previewReward = null;
            rewardPreviewTransition.CloseImmediately();
            rewardPopupTransition.CloseImmediately();
            LuckyWheel.LuckyWheelUI.Close();
            if (giftCodeInput != null) giftCodeInput.setFocus(false);
        }

        public static void OnStateReloaded()
        {
            if (!alignRewardScrollOnNextState) return;
            AlignRewardScroll(Manager.Instance.Current);
            alignRewardScrollOnNextState = false;
        }

        private static void AlignRewardScroll(State state)
        {
            int currentLevelIndex = System.Math.Max(0, state.level - 1);
            if (state.levels.Count > 0)
                currentLevelIndex = System.Math.Min(currentLevelIndex, state.levels.Count - 1);
            rewardScroll.cmtoX = rewardScroll.cmx = currentLevelIndex * 43;
        }

        public static void Paint(mGraphics g)
        {
            Manager manager = Manager.Instance;
            long now = mSystem.currentTimeMillis();
            if (now - lastRefresh >= 30000L)
            {
                lastRefresh = now;
                Network.GetState();
            }
            if (manager.SeasonChanged)
            {
                manager.SeasonChanged = false;
                Close();
                GameCanvas.startOKDlg("Mùa Dragon Pass mới đã bắt đầu");
                return;
            }
            if (!Visible) return;
            if (GameCanvas.panel != null && GameCanvas.panel.isShow) { Close(); return; }

            bool showRewardPopup = manager.RewardPopupSeconds > 0f || rewardPopupTransition.Active;
            if (showRewardPopup)
            {
                if (manager.RewardPopupSeconds > 0f
                    && (!rewardPopupTransition.Active || rewardPopupTransition.IsClosing))
                {
                    rewardPopupTransition.Show();
                }
                if (!rewardPopupTransition.IsClosing && manager.RewardPopupSeconds > 0f)
                {
                    manager.RewardPopupSeconds -= UnityEngine.Time.deltaTime;
                    if (manager.RewardPopupSeconds <= 0f) rewardPopupTransition.Close();
                }
            }

            int w = PanelWidth();
            int h = PanelHeight();
            int x = (GameCanvas.w - w) / 2;
            int y = (GameCanvas.h - h) / 2;
            State state = manager.Current;

            ModFunc.PaintMailPopupBackground(g, x, y, w, h, -1);

            int sideW = 78;
            ModFunc.PaintMailPopupBackground(g, x + 4, y + 23, sideW - 5, h - 28, 1);
            mFont.tahoma_7b_dark.drawString(g, "Sự kiện", x + sideW / 2, y + 29, mFont.CENTER);
            g.setClip(x + 5, y + 43, sideW - 7, h - 50);
            int eventY = y + 48 - eventScroll.cmy;
            PaintActionButton(g, x + 8, eventY, sideW - 13, "Dragon Pass");
            PaintActionButton(g, x + 8, eventY + EventRowHeight, sideW - 13, "Gift Code");
            PaintActionButton(g, x + 8, eventY + EventRowHeight * 2, sideW - 13, "Top");
            if (LuckyWheel.Manager.Instance.Current.active)
                PaintActionButton(g, x + 8, eventY + EventRowHeight * 3, sideW - 13, "Vòng quay");
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            int cx = x + sideW + 2;
            int cw = w - sideW - 7;
            if (eventIndex == 0)
            {
                PaintHeader(g, cx, y + 5, cw, state);
                int tabW = System.Math.Min(88, cw / 2 - 3);
                PaintActionButton(g, cx + 3, y + 42, tabW, "Thưởng");
                PaintActionButton(g, cx + 6 + tabW, y + 42, tabW, "Nhiệm vụ");
                if (tab == 0) PaintRewards(g, cx, y + 68, cw, h - 72, state);
                else PaintTasks(g, cx, y + 68, cw, h - 72, state);
            }
            else if (eventIndex == 1) PaintGiftCodePage(g, cx, y + 5, cw, h - 10);
            else if (eventIndex == 2) TopBoardUI.Paint(g, cx, y + 5, cw, h - 10);
            else LuckyWheel.LuckyWheelUI.Paint(g, cx, y + 5, cw, h - 10);

            if (manager.ResultSeconds > 0f && !string.IsNullOrEmpty(manager.ResultText))
            {
                manager.ResultSeconds -= UnityEngine.Time.deltaTime;
                mFont.tahoma_7b_green2.drawStringBorder(g, manager.ResultText, x + w / 2, y + h - 13,
                    mFont.CENTER, mFont.tahoma_7_grey);
            }
            // Vẽ sau toàn bộ nền/nội dung chính để nút đóng luôn nằm trên bảng.
            if (confirmPassType != 0) PaintConfirm(g, state);
            if (previewReward != null) PaintRewardPreview(g, previewReward);
            if (showRewardPopup)
            {
                PaintRewardPopup(g, manager);
                if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    SoundMn.gI().buttonClick();
                    manager.RewardPopupSeconds = 0f;
                    rewardPopupTransition.Close();
                    GameCanvas.clearAllPointerEvent();
                }
            }

            // Nút đóng thuộc cửa sổ event cha và luôn được vẽ trên mọi danh mục/event con.
            PaintCloseButton(g, x, y, w);
        }

        public static void UpdateInput()
        {
            if (!Visible) return;
            if (BlocksInput)
            {
                HandleBlockingInput();
                return;
            }
            if (previewReward != null)
            {
                HandleRewardPreviewInput();
                return;
            }
            int w = PanelWidth();
            int h = PanelHeight();
            int x = (GameCanvas.w - w) / 2;
            int y = (GameCanvas.h - h) / 2;
            bool scrollOwnsPointer = eventScroll.pointerIsDowning || rewardScroll.pointerIsDowning
                || taskScroll.pointerIsDowning || TopBoardUI.HasPointerCapture
                || LuckyWheel.LuckyWheelUI.HasPointerCapture;
            if (!scrollOwnsPointer && HandleCloseButton(x, y, w)) return;
            if (eventIndex == 1)
            {
                int sideW = 78;
                int cx = x + sideW + 2;
                int cw = w - sideW - 7;
                int panelX = cx + 5;
                int panelY = y + 5 + 28;
                EnsureGiftCodeInput(panelX + 12, panelY + 48, cw - 10 - 24);
                giftCodeInput.update();
            }
            UpdatePointer(x, y, w, h, Manager.Instance.Current);
        }

        private static void PaintHeader(mGraphics g, int x, int y, int w, State state)
        {
            string phase = state.phase == 0 ? TimeLeft(state.activeUntil)
                : (state.phase == 1 ? "Đang tổng kết" : "Chưa mở");
            mFont.tahoma_7b_dark.drawString(g, "Dragon Pass  •  Cấp " + state.level, x + 3, y + 2, mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, "EXP " + state.exp + "  (" + (state.exp % 100) + "/100) • " + phase,
                x + 3, y + 15, mFont.LEFT);
            // string pass = state.passType == 2 ? "Plus" : (state.passType == 1 ? "Thường" : string.Empty);
            // mFont.tahoma_7b_dark.drawString(g, pass, x + w - 4, y + 2, mFont.RIGHT); 
        }

        private static int PanelWidth()
        {
            return System.Math.Min(720, System.Math.Max(340, GameCanvas.w - 30));
        }

        private static int PanelHeight()
        {
            return System.Math.Min(335, System.Math.Max(232, GameCanvas.h - 26));
        }

        private static void PaintActionButton(mGraphics g, int x, int y, int width, string caption)
        {
            bool pressed = GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(x, y, width, 24);
            ModFunc.PaintMailActionButton(g, x, y, width, caption, pressed);
        }

        private static void EnsureGiftCodeInput(int x, int y, int width)
        {
            if (giftCodeInput == null)
            {
                giftCodeInput = new TField();
                giftCodeInput.name = "Nhập Gift Code";
                giftCodeInput.height = mScreen.ITEM_HEIGHT + 2;
                giftCodeInput.setIputType(TField.INPUT_TYPE_ANY);
                giftCodeInput.setMaxTextLenght(64);
                giftCodeInput.isPaintMouse = false;
            }
            giftCodeInput.x = x;
            giftCodeInput.y = y;
            giftCodeInput.width = width;
        }

        private static void PaintGiftCodePage(mGraphics g, int x, int y, int w, int h)
        {
            int panelX = x + 5;
            int panelY = y + 28;
            int panelW = w - 10;
            int inputX = panelX + 12;
            int inputY = panelY + 48;
            int inputW = panelW - 24;
            EnsureGiftCodeInput(inputX, inputY, inputW);

            mFont.tahoma_7b_dark.drawString(g, "Gift Code", x + w / 2, y + 5, mFont.CENTER);
            g.setColor(0xD99A55);
            g.fillRect(panelX, panelY, panelW, System.Math.Min(125, h - 32), 7);
            g.setColor(0xA95E2D);
            g.fillRect(panelX + 8, panelY + 8, panelW - 16, 26, 5);
            mFont.tahoma_7b_white.drawString(g, "NHẬP MÃ QUÀ TẶNG", x + w / 2, panelY + 15, mFont.CENTER);

            g.setColor(0x8A4B29);
            g.fillRect(inputX - 2, inputY - 2, inputW + 4, giftCodeInput.height + 4, 5);
            g.setColor(giftCodeInput.isFocus ? 0xFFF0CF : 0xE7D7C2);
            g.fillRect(inputX, inputY, inputW, giftCodeInput.height, 4);
            string code = giftCodeInput.getText();
            g.setClip(inputX + 3, inputY, inputW - 6, giftCodeInput.height);
            if (string.IsNullOrEmpty(code))
                mFont.tahoma_7_grey.drawString(g, "Nhập Gift Code", inputX + 6, inputY + 6, mFont.LEFT);
            else
                mFont.tahoma_7b_dark.drawString(g, code, inputX + 6, inputY + 6, mFont.LEFT);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            string caption = Manager.Instance.GiftCodeSubmitting ? "Đang đổi..." : "Đổi";
            PaintActionButton(g, x + w / 2 - 42, panelY + 83, 84, caption);
        }

        public static bool HandleKeyPress(int keyCode)
        {
            if (!Visible || eventIndex != 1 || giftCodeInput == null || !giftCodeInput.isFocus) return false;
            if (keyCode == 10 || keyCode == 13)
            {
                SubmitGiftCode();
                return true;
            }
            giftCodeInput.keyPressed(keyCode);
            return true;
        }

        public static void OnGiftCodeResult(bool success)
        {
            if (success && giftCodeInput != null) giftCodeInput.clearAllText();
        }

        private static void SubmitGiftCode()
        {
            Manager manager = Manager.Instance;
            if (manager.GiftCodeSubmitting) return;
            SoundMn.gI().buttonClick();
            string code = giftCodeInput == null ? string.Empty : giftCodeInput.getText().Trim();
            if (code.Length == 0)
            {
                manager.ClaimedRewards.Clear();
                manager.RewardPopupText = "Vui lòng nhập Gift Code";
                manager.RewardPopupSuccess = false;
                manager.RewardPopupSeconds = 5f;
                return;
            }
            manager.GiftCodeSubmitting = true;
            if (giftCodeInput != null) giftCodeInput.setFocus(false);
            Network.RedeemGiftCode(code);
        }

        private static string TimeLeft(long until)
        {
            long ms = System.Math.Max(0L, until - mSystem.currentTimeMillis());
            long hours = ms / 3600000L;
            long days = hours / 24L;
            return days > 0 ? days + " ngày " + (hours % 24) + " giờ" : hours + " giờ";
        }

        private static int VisibleRewardColumns(int width)
        {
            return System.Math.Max(3, (width + 42) / 43);
        }

        private static void PaintRewards(mGraphics g, int x, int y, int w, int h, State state)
        {
            bool pinned = state.level < 100;
            int labelW = 40;
            int viewportW = w - 8 - labelW - (pinned ? 43 : 0);
            int count = VisibleRewardColumns(viewportW);
            int colW = 43;
            int contentY = y + RewardContentOffsetY;
            g.setColor(0xD99A55);
            g.fillRect(x + 1, y, w - 2, System.Math.Min(h, 154), 6);
            PaintTrackLabels(g, x + 4, contentY, labelW - 2, state);
            int gridX = x + 4 + labelW;
            g.setClip(gridX, contentY - 2, viewportW, 120);
            int first = System.Math.Max(0, rewardScroll.cmx / colW - 1);
            int last = System.Math.Min(state.levels.Count, first + count + 3);
            for (int i = first; i < last; i++)
            {
                PaintRewardColumn(g, gridX + i * colW - rewardScroll.cmx, contentY, colW - 2, state.levels[i], state, false);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            if (pinned && state.levels.Count > state.level)
            {
                int px = x + w - 45;
                g.setColor(0xEFC65A);
                g.fillRect(px, contentY - 2, 43, 118, 4);
                PaintRewardColumn(g, px + 1, contentY, 41, state.levels[state.level], state, true);
            }
            int bottom = contentY + System.Math.Min(h - 33, 122);
            PaintPurchaseButtons(g, x + 4, bottom, w - 8, state);
        }

        private static void PaintTrackLabels(mGraphics g, int x, int y, int w, State state)
        {
            g.setColor(0xD93622);
            g.fillRect(x, y, w, 16, 3);
            g.setColor(0xF1E6C8);
            g.fillRect(x, y + 17, w, 38, 3);
            g.setColor(0xE8B64B);
            g.fillRect(x, y + 58, w, 55, 3);
            mFont.tahoma_7b_white.drawString(g, "Cấp", x + w / 2, y + 3, mFont.CENTER);
            mFont.tahoma_7b_dark.drawString(g, "Thường", x + w / 2, y + 31, mFont.CENTER);
            mFont.tahoma_7b_dark.drawString(g, "Plus", x + w / 2, y + 80, mFont.CENTER);
            if (closedLockIcon == null && state.passType < 2)
                closedLockIcon = GameCanvas.loadImage("/mainimage/lock_closed.png");
            if (closedLockIcon != null && state.passType < 1)
                g.drawImageScaleClipped(closedLockIcon, x + w - 12, y + 19, 10, 10);
            if (closedLockIcon != null && state.passType < 2)
                g.drawImageScaleClipped(closedLockIcon, x + w - 12, y + 60, 10, 10);
        }

        private static void PaintRewardColumn(mGraphics g, int x, int y, int w, LevelData level, State state, bool pinned)
        {
            g.setColor(0xD93622);
            g.fillRect(x, y, w, 16, 3);
            g.setColor(0xF1E6C8);
            g.fillRect(x, y + 17, w, 38, 3);
            g.setColor(0xE8B64B);
            g.fillRect(x, y + 58, w, 55, 3);
            mFont.tahoma_7b_white.drawString(g, "Lv" + level.level, x + w / 2, y + 3, mFont.CENTER);
            if (pinned)
            {
                g.setColor(0xD93622);
                for (int i = 0; i < 5; i++)
                    g.fillRect(x + w / 2 - (5 - i), y + 15 + i, (5 - i) * 2 + 1, 1);
            }
            PaintRewardCell(g, x + 4, y + 20, w - 8, 32, level.normal, level.claimedNormal,
                level.level <= state.level, state.passType < 1);
            PaintPlusCellBorder(g, x + 2, y + 58, w - 4, 27);
            PaintRewardCell(g, x + 4, y + 60, w - 8, 23, level.plus1, level.claimedPlus,
                level.level <= state.level, state.passType < 2);
            PaintPlusCellBorder(g, x + 2, y + 86, w - 4, 27);
            PaintRewardCell(g, x + 4, y + 88, w - 8, 23, level.plus2, level.claimedPlus,
                level.level <= state.level, state.passType < 2);
        }

        private static void PaintPlusCellBorder(mGraphics g, int x, int y, int w, int h)
        {
            g.setColor(0xE4B83F);
            g.fillRect(x, y, w, h, 4);
        }

        private static void PaintRewardCell(mGraphics g, int x, int y, int w, int h, RewardData reward,
            bool claimed, bool earned, bool locked)
        {
            bool readyToClaim = earned && !locked && !claimed;
            ModFunc.PaintInventoryItemSlot(g, x, y, w, h, readyToClaim,
                reward != null && reward.configured);
            if (reward != null && reward.configured)
            {
                ItemTemplate template = ItemTemplates.get(reward.itemId);
                if (template != null) SmallImage.drawSmallImage(g, template.iconID, x + w / 2, y + h / 2 - 2, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
                mFont.tahoma_7b_dark.drawString(g, "x" + reward.quantity, x + w - 2, y + h - 10, mFont.RIGHT);
            }
            else mFont.tahoma_7_grey.drawString(g, "?", x + w / 2, y + h / 2 - 4, mFont.CENTER);
            if (claimed)
            {
                if (claimedCheckmark == null)
                    claimedCheckmark = GameCanvas.loadImage("/mainimage/icon_checkmark.png");
                if (claimedCheckmark != null)
                    g.drawImage(claimedCheckmark, x + 2, y + 2, 0);
            }
            else if (locked)
            {
                if (closedLockIcon == null)
                    closedLockIcon = GameCanvas.loadImage("/mainimage/lock_closed.png");
                if (closedLockIcon != null)
                    g.drawImageScaleClipped(closedLockIcon, x + w - 12, y + 2, 10, 10);
            }
        }

        private static void PaintPurchaseButtons(mGraphics g, int x, int y, int w, State state)
        {
            if (state.passType == 2)
            {
                mFont.tahoma_7b_green2.drawString(g, "Đã kích hoạt Plus", x + w / 2, y + 7, mFont.CENTER);
                return;
            }
            if (state.passType == 1)
            {
                PaintActionButton(g, x, y, w, "Nâng Plus " + (state.plusPrice - state.normalPrice) + " Ngọc");
                return;
            }
            if (state.phase != 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Đã đóng mua Pass", x + w / 2, y + 7, mFont.CENTER);
                return;
            }
            int half = (w - 3) / 2;
            PaintActionButton(g, x, y, half, "Thường  " + state.normalPrice);
            PaintActionButton(g, x + half + 3, y, w - half - 3, "Plus  " + state.plusPrice);
        }

        private static void PaintTasks(mGraphics g, int x, int y, int w, int h, State state)
        {
            int rowH = TaskRowHeight;
            int listH = h - 30;
            g.setClip(x + 3, y, w - 6, listH);
            int viewportBottom = y + listH;
            for (int i = 0; i < state.tasks.Count; i++)
            {
                TaskData task = state.tasks[i];
                int ry = y + i * rowH - taskScroll.cmy;
                int rowBottom = ry + rowH - 2;
                if (ry >= viewportBottom || rowBottom <= y)
                    continue;
                bool completed = task.target > 0 && task.progress >= task.target;
                bool readyToClaim = completed && !task.claimed;
                g.setColor(task.claimed ? 0x969696 : (readyToClaim ? 0xE8B56C : 0xD99A55));
                g.fillRect(x + 3, ry, w - 6, rowH - 2, 3);
                string type = task.type == 0 ? "Ngày" : "Mùa";
                mFont.tahoma_7b_dark.drawString(g, type + " • " + task.name, x + 7, ry + 3, mFont.LEFT);
                string[] descriptionLines = mFont.tahoma_7_grey.splitFontArray(task.description ?? string.Empty, w - 18);
                if (descriptionLines.Length > 0)
                    mFont.tahoma_7_grey.drawString(g, descriptionLines[0], x + 7, ry + 13, mFont.LEFT);
                string progress = task.unlocked ? System.Math.Min(task.progress, task.target) + "/" + task.target : "Chưa mở";
                mFont.tahoma_7_grey.drawString(g, progress + "  +" + task.exp + " EXP", x + 7, ry + 24, mFont.LEFT);
                if (task.unlocked && !task.claimed && task.progress >= task.target)
                    PaintTaskClaimButton(g, x + w - 56, ry + 7, 48, "Nhận");
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            int by = y + h - 26;
            PaintActionButton(g, x + w - 82, by, 79, "Nhận nhanh");
        }

        private static void PaintTaskClaimButton(mGraphics g, int x, int y, int width, string text)
        {
            bool pressed = GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(x, y, width, 24);
            ModFunc.PaintMailActionButton(g, x, y, width, text, pressed);
        }

        private static void ConfigureScroll(Scroll scroll, int x, int y, int width, int height,
            int itemSize, int itemCount, bool vertical)
        {
            scroll.xPos = x;
            scroll.yPos = y;
            scroll.width = width;
            scroll.height = height;
            scroll.ITEM_SIZE = itemSize;
            scroll.nITEM = itemCount;
            scroll.ITEM_PER_LINE = vertical ? 1 : itemCount;
            scroll.styleUPDOWN = vertical;
            if (vertical)
            {
                scroll.cmyLim = System.Math.Max(0, itemCount * itemSize - height);
                if (scroll.cmtoY > scroll.cmyLim) scroll.cmtoY = scroll.cmyLim;
            }
            else
            {
                scroll.cmxLim = System.Math.Max(0, itemCount * itemSize - width);
                if (scroll.cmtoX > scroll.cmxLim) scroll.cmtoX = scroll.cmxLim;
            }
        }

        private static void UpdatePointer(int x, int y, int w, int h, State state)
        {
            if (confirmPassType != 0)
            {
                if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease) HandleConfirmPointer(state);
                return;
            }

            int sideW = 78;
            int eventCount = LuckyWheel.Manager.Instance.Current.active ? 4 : 3;
            // Nếu menu Event đã nhận thao tác kéo thì nó giữ chuột tới lúc thả.
            if (eventScroll.pointerIsDowning && UpdateEventScroll(x, y, h, sideW, eventCount)) return;

            int cx = x + sideW + 2;
            int cw = w - sideW - 7;
            int bodyY = y + 68;
            int bodyH = h - 72;
            if (eventIndex == 3)
            {
                if (LuckyWheel.LuckyWheelUI.HandleInput(cx, y + 5, cw, h - 10)) return;
            }
            else if (eventIndex == 2)
            {
                if (TopBoardUI.HandleInput(cx, y + 5, cw, h - 10)) return;
            }
            if (eventIndex == 0 && tab == 0)
            {
                bool pinned = state.level < 100;
                int labelW = 40;
                int viewportW = cw - 8 - labelW - (pinned ? 43 : 0);
                int colW = 43;
                int contentY = bodyY + RewardContentOffsetY;
                ConfigureScroll(rewardScroll, cx + 4 + labelW, contentY - 2, viewportW, 120, colW,
                    state.levels.Count, false);
                bool wasDragging = rewardScroll.pointerIsDowning;
                ScrollResult result = rewardScroll.updateKey();
                rewardScroll.updatecm();
                if (result.isFinish && result.selected >= 0 && result.selected < state.levels.Count)
                {
                    if (GameCanvas.py >= contentY + 17 && GameCanvas.py < contentY + 55)
                        HandleRewardCell(state.levels[result.selected], 1, 0, state);
                    else if (GameCanvas.py >= contentY + 58 && GameCanvas.py < contentY + 85)
                        HandleRewardCell(state.levels[result.selected], 2, 1, state);
                    else if (GameCanvas.py >= contentY + 86 && GameCanvas.py < contentY + 113)
                        HandleRewardCell(state.levels[result.selected], 2, 2, state);
                    GameCanvas.clearAllPointerEvent();
                }
                // Danh sách thưởng đã bắt đầu kéo sẽ độc quyền chuột, kể cả khi đi qua menu Event.
                if (wasDragging || result.isDowning || result.isFinish) return;
            }
            else if (eventIndex == 0)
            {
                int listH = bodyH - 30;
                ConfigureScroll(taskScroll, cx + 3, bodyY, cw - 6, listH, TaskRowHeight, state.tasks.Count, true);
                bool wasDragging = taskScroll.pointerIsDowning;
                ScrollResult result = taskScroll.updateKey();
                taskScroll.updatecm();
                if (result.isFinish && result.selected >= 0 && result.selected < state.tasks.Count)
                {
                    TaskData task = state.tasks[result.selected];
                    int taskY = bodyY + result.selected * TaskRowHeight - taskScroll.cmy;
                    int claimX = cx + cw - 56;
                    int claimY = taskY + 7;
                    if (task.unlocked && !task.claimed && task.progress >= task.target
                        && GameCanvas.isPointerHoldIn(claimX, claimY, 48, 24))
                    {
                        SoundMn.gI().buttonClick();
                        Network.ClaimTask(task.id);
                    }
                    GameCanvas.clearAllPointerEvent();
                }
                if (wasDragging || result.isDowning || result.isFinish) return;
            }

            // Chỉ cho menu Event nhận một thao tác mới sau khi nội dung hiện tại từ chối nhận nó.
            if (UpdateEventScroll(x, y, h, sideW, eventCount)) return;

            if (!GameCanvas.isPointerClick || !GameCanvas.isPointerJustRelease) return;
            if (eventIndex == 1)
            {
                int panelX = cx + 5;
                int panelY = y + 33;
                int inputX = panelX + 12;
                int inputY = panelY + 48;
                int inputW = cw - 34;
                if (GameCanvas.isPointerHoldIn(inputX - 2, inputY - 2, inputW + 4, giftCodeInput.height + 4))
                {
                    SoundMn.gI().buttonClick();
                    giftCodeInput.setFocusWithKb(true);
                }
                else if (GameCanvas.isPointerHoldIn(cx + cw / 2 - 42, panelY + 83, 84, 24))
                    SubmitGiftCode();
            }
            else if (eventIndex == 0)
            {
                int tabW = System.Math.Min(88, cw / 2 - 3);
                if (GameCanvas.isPointerHoldIn(cx + 3, y + 42, tabW, 24))
                {
                    SoundMn.gI().buttonClick();
                    tab = 0;
                }
                else if (GameCanvas.isPointerHoldIn(cx + 6 + tabW, y + 42, tabW, 24))
                {
                    SoundMn.gI().buttonClick();
                    tab = 1;
                }
                else if (tab == 0) HandleRewardButtons(cx, bodyY, cw, bodyH, state);
                else HandleTaskButtons(cx, bodyY, cw, bodyH);
            }
            GameCanvas.clearAllPointerEvent();
        }

        private static bool UpdateEventScroll(int x, int y, int h, int sideW, int eventCount)
        {
            if (eventIndex == 3 && LuckyWheel.LuckyWheelUI.IsBusy && !eventScroll.pointerIsDowning)
                return false;

            ConfigureScroll(eventScroll, x + 8, y + 48, sideW - 13, h - 55,
                EventRowHeight, eventCount, true);
            bool wasDragging = eventScroll.pointerIsDowning;
            ScrollResult result = eventScroll.updateKey();
            eventScroll.updatecm();
            if (result.isFinish && result.selected >= 0 && result.selected < eventCount)
            {
                int buttonY = y + 48 - eventScroll.cmy + result.selected * EventRowHeight;
                int buttonX = x + 8;
                int buttonW = sideW - 13;
                if (GameCanvas.px >= buttonX && GameCanvas.px <= buttonX + buttonW
                    && GameCanvas.py >= buttonY && GameCanvas.py <= buttonY + EventButtonHeight)
                {
                    SoundMn.gI().buttonClick();
                    eventIndex = result.selected;
                    if (eventIndex != 1 && giftCodeInput != null) giftCodeInput.setFocus(false);
                    if (eventIndex == 2) TopBoardUI.Open();
                    if (eventIndex == 3) LuckyWheel.LuckyWheelUI.Open();
                }
            }
            return wasDragging || result.isDowning || result.isFinish;
        }

        private static void PaintCloseButton(mGraphics g, int x, int y, int w)
        {
            if (ModFunc.imgCloseButton == null) return;
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            g.drawImage(ModFunc.imgCloseButton, x + w - ModFunc.imgCloseButton.getWidth(), y, 0);
        }

        private static bool HandleCloseButton(int x, int y, int w)
        {
            if (ModFunc.imgCloseButton == null
                || !GameCanvas.isPointerClick
                || !GameCanvas.isPointerJustRelease)
                return false;

            int closeW = ModFunc.imgCloseButton.getWidth();
            int closeH = ModFunc.imgCloseButton.getHeight();
            if (!GameCanvas.isPointerHoldIn(x + w - closeW, y, closeW, closeH)) return false;

            SoundMn.gI().buttonClick();
            Close();
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private static void HandleRewardButtons(int x, int y, int w, int h, State state)
        {
            bool pinned = state.level < 100;
            int contentY = y + RewardContentOffsetY;
            if (pinned && state.levels.Count > state.level && GameCanvas.isPointerHoldIn(x + w - 45, contentY + 17, 43, 96))
            {
                int track = GameCanvas.isPointerHoldIn(x + w - 45, contentY + 17, 43, 38) ? 1 : 2;
                int slot = track == 1 ? 0 : (GameCanvas.py < contentY + 86 ? 1 : 2);
                HandleRewardCell(state.levels[state.level], track, slot, state);
                return;
            }
            int bottom = contentY + System.Math.Min(h - 33, 122);
            int bx = x + 4, bw = w - 8;
            if (state.passType == 1 && GameCanvas.isPointerHoldIn(bx, bottom, bw, 24))
            {
                SoundMn.gI().buttonClick();
                confirmPassType = 2;
            }
            else if (state.passType == 0 && state.phase == 0)
            {
                int half = (bw - 3) / 2;
                if (GameCanvas.isPointerHoldIn(bx, bottom, half, 24))
                {
                    SoundMn.gI().buttonClick();
                    confirmPassType = 1;
                }
                else if (GameCanvas.isPointerHoldIn(bx + half + 3, bottom, bw - half - 3, 24))
                {
                    SoundMn.gI().buttonClick();
                    confirmPassType = 2;
                }
            }
        }

        private static void HandleRewardCell(LevelData level, int track, int slot, State state)
        {
            SoundMn.gI().buttonClick();
            RewardData reward = track == 1 ? level.normal : (slot == 2 ? level.plus2 : level.plus1);
            bool configured = reward != null && reward.configured && (track == 1 || (level.plus2 != null && level.plus2.configured));
            if (!configured) { ShowResult("Phần thưởng đang cập nhật"); return; }
            bool earned = level.level <= state.level;
            bool ownsTrack = (track == 1 && state.passType >= 1) || (track == 2 && state.passType >= 2);
            bool claimed = track == 1 ? level.claimedNormal : level.claimedPlus;
            if (!earned || !ownsTrack || claimed)
            {
                ShowRewardPreview(reward);
                return;
            }
            Network.ClaimReward(level.level, (byte)track);
        }

        private static void HandleTaskButtons(int x, int y, int w, int h)
        {
            int by = y + h - 26;
            if (GameCanvas.isPointerHoldIn(x + w - 82, by, 79, 24))
            {
                SoundMn.gI().buttonClick();
                Network.ClaimAllTasks();
            }
        }

        private static void PaintConfirm(mGraphics g, State state)
        {
            int w = 205, h = 76, x = (GameCanvas.w - w) / 2, y = (GameCanvas.h - h) / 2;
            ModFunc.PaintMailPopupBackground(g, x, y, w, h, 1);
            int price = confirmPassType == 1 ? state.normalPrice : (state.passType == 1 ? state.plusPrice - state.normalPrice : state.plusPrice);
            string name = confirmPassType == 1 ? "Dragon Pass thường" : "Dragon Pass Plus";
            mFont.tahoma_7b_dark.drawString(g, "Dùng " + price + " Ngọc để kích hoạt", x + w / 2, y + 13, mFont.CENTER);
            mFont.tahoma_7b_dark.drawString(g, name + "?", x + w / 2, y + 27, mFont.CENTER);
            PaintActionButton(g, x + 28, y + 48, 65, "Đồng ý");
            PaintActionButton(g, x + 112, y + 48, 65, "Hủy");
        }

        private static void HandleConfirmPointer(State state)
        {
            int w = 205, h = 76, x = (GameCanvas.w - w) / 2, y = (GameCanvas.h - h) / 2;
            if (GameCanvas.isPointerHoldIn(x + 28, y + 48, 65, 24))
            {
                SoundMn.gI().buttonClick();
                byte requested = confirmPassType;
                confirmPassType = 0;
                Network.Buy(requested);
            }
            else if (GameCanvas.isPointerHoldIn(x + 112, y + 48, 65, 24))
            {
                SoundMn.gI().buttonClick();
                confirmPassType = 0;
            }
            else if (GameCanvas.isPointerClick) confirmPassType = 0;
            GameCanvas.clearAllPointerEvent();
        }

        private static void ShowResult(string text)
        {
            Manager.Instance.ResultText = text;
            Manager.Instance.ResultSeconds = 2f;
        }

        private static List<string> RewardOptionLines(RewardData reward)
        {
            int ignoredUpgradeLevel;
            return ModFunc.GetItemOptionLines(reward == null ? null : reward.options, out ignoredUpgradeLevel);
        }

        private static int RewardUpgradeLevel(RewardData reward)
        {
            int upgradeLevel;
            ModFunc.GetItemOptionLines(reward == null ? null : reward.options, out upgradeLevel);
            return upgradeLevel;
        }

        private static void RewardPreviewBounds(RewardData reward, out int x, out int y, out int w, out int h,
            out List<string> options, out int upgradeLevel)
        {
            w = System.Math.Min(165, GameCanvas.w - 16);
            ItemTemplate template = reward == null ? null : ItemTemplates.get(reward.itemId);
            options = RewardOptionLines(reward);
            upgradeLevel = RewardUpgradeLevel(reward);
            h = ModFunc.GetInventoryItemPreviewHeight(template, options, w);
            x = (GameCanvas.w - w) / 2;
            y = (GameCanvas.h - h) / 2;
        }

        private static void HandleRewardPreviewInput()
        {
            if (!GameCanvas.isPointerClick || !GameCanvas.isPointerJustRelease) return;
            int x, y, w, h;
            List<string> options;
            int upgradeLevel;
            RewardPreviewBounds(previewReward, out x, out y, out w, out h, out options, out upgradeLevel);
            if (!GameCanvas.isPointerHoldIn(x, y, w, h)) rewardPreviewTransition.Close();
            GameCanvas.clearAllPointerEvent();
        }

        private static void ShowRewardPreview(RewardData reward)
        {
            previewReward = reward;
            rewardPreviewTransition.Show();
        }

        public static void ShowExternalRewardPreview(RewardData reward)
        {
            ShowRewardPreview(reward);
        }

        public static void OnLuckyWheelEnded()
        {
            LuckyWheel.Manager.Instance.Current.active = false;
            if (!Visible || eventIndex != 3) return;
            Close();
            GameCanvas.startOKDlg("Sự kiện Vòng quay may mắn đã kết thúc");
        }

        public static void OnLuckyWheelUnavailable()
        {
            if (!Visible || eventIndex != 3) return;
            eventIndex = 0;
            LuckyWheel.LuckyWheelUI.Close();
            GameCanvas.startOKDlg("Sự kiện Vòng quay may mắn hiện không hoạt động");
        }

        private static void PaintRewardPreview(mGraphics g, RewardData reward)
        {
            ItemTemplate template = reward == null ? null : ItemTemplates.get(reward.itemId);
            if (template == null)
            {
                previewReward = null;
                rewardPreviewTransition.CloseImmediately();
                return;
            }
            int x, y, w, h;
            List<string> options;
            int upgradeLevel;
            RewardPreviewBounds(reward, out x, out y, out w, out h, out options, out upgradeLevel);
            rewardPreviewTransition.Update(UnityEngine.Time.deltaTime);
            if (!rewardPreviewTransition.Active)
            {
                previewReward = null;
                return;
            }
            rewardPreviewTransition.PaintDimBackground(g, 0.55f);
            UnityEngine.Matrix4x4 previousMatrix = rewardPreviewTransition.PushScale(x + w / 2, y + h / 2);
            mGraphics.OpacityState opacityState = mGraphics.PushOpacity(rewardPreviewTransition.Opacity);
            ModFunc.PaintInventoryItemPreview(g, x, y, w, h, template, options, upgradeLevel, ModFunc.isShowID);
            mGraphics.PopOpacity(opacityState);
            rewardPreviewTransition.PopScale(previousMatrix);
        }

        private static void PaintRewardPopup(mGraphics g, Manager manager)
        {
            rewardPopupTransition.Update(UnityEngine.Time.deltaTime);
            if (!rewardPopupTransition.Active)
            {
                manager.RewardPopupSeconds = 0f;
                manager.ClaimedRewards.Clear();
                manager.RewardPopupText = null;
                return;
            }
            int count = manager.ClaimedRewards.Count;
            int columns = count <= 3 ? System.Math.Max(1, count)
                : System.Math.Min(count, System.Math.Max(3, (GameCanvas.w - 28) / 42));
            int rows = count == 0 ? 1 : (count + columns - 1) / columns;
            int w = count <= 3 ? 190 : System.Math.Min(GameCanvas.w - 20, System.Math.Max(190, columns * 42 + 20));
            int h = 105 + System.Math.Max(0, rows - 1) * 40;
            int x = (GameCanvas.w - w) / 2, y = (GameCanvas.h - h) / 2;
            rewardPopupTransition.PaintDimBackground(g, 0.45f);
            rewardPopupTransition.TransformBounds(ref x, ref y, ref w, ref h);
            mGraphics.OpacityState opacityState = mGraphics.PushOpacity(rewardPopupTransition.Opacity);
            ModFunc.PaintMailPopupBackground(g, x, y, w, h, 1);
            string popupText = string.IsNullOrEmpty(manager.RewardPopupText)
                ? "Nhận thưởng thành công" : manager.RewardPopupText;
            if (count == 0)
            {
                mFont popupFont = manager.RewardPopupSuccess ? mFont.tahoma_7b_green2 : mFont.tahoma_7b_red;
                string[] lines = popupFont.splitFontArray(popupText, w - 20);
                int lineCount = System.Math.Min(3, lines.Length);
                int startY = y + 42 - lineCount * 6;
                for (int i = 0; i < lineCount; i++)
                    popupFont.drawString(g, lines[i], x + w / 2, startY + i * 12, mFont.CENTER);
            }
            else
            {
                mFont popupFont = manager.RewardPopupSuccess ? mFont.tahoma_7b_green2 : mFont.tahoma_7b_red;
                popupFont.drawString(g, popupText, x + w / 2, y + 10, mFont.CENTER);
            }
            int rewardSpacing = columns <= 3 ? 58 : System.Math.Max(28, (w - 24) / columns);
            for (int i = 0; i < count; i++)
            {
                RewardData reward = manager.ClaimedRewards[i];
                int row = i / columns, column = i % columns;
                int itemsInRow = System.Math.Min(columns, count - row * columns);
                int sx = x + w / 2 + (column * rewardSpacing) - ((itemsInRow - 1) * rewardSpacing / 2);
                int sy = y + 48 + row * 40;
                if (reward.rewardType == 1)
                {
                    ItemTemplate template = ItemTemplates.get(reward.itemId);
                    if (template != null) SmallImage.drawSmallImage(g, template.iconID, sx, sy, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
                }
                else
                {
                    Image currency = reward.rewardType == 2 ? Panel.imgXu
                        : (reward.rewardType == 3 ? Panel.imgLuong : Panel.imgLuongKhoa);
                    if (currency != null) g.drawImage(currency, sx, sy, mGraphics.VCENTER | mGraphics.HCENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, "x" + reward.quantity, sx, sy + 18, mFont.CENTER);
            }
            int seconds = System.Math.Max(0, (int)System.Math.Ceiling(manager.RewardPopupSeconds));
            mFont.tahoma_7_grey.drawString(g, "Chạm bất kỳ để đóng • " + seconds + "s", x + w / 2, y + h - 17, mFont.CENTER);
            mGraphics.PopOpacity(opacityState);
        }
    }
}
