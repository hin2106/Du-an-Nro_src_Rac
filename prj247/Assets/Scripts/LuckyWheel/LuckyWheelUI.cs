using System;
using System.Collections.Generic;

namespace LuckyWheel
{
    public static class LuckyWheelUI
    {
        private static readonly int[,] CellPositions = new int[,]
        {
            {0,0},{1,0},{2,0},{3,0},{4,0},{4,1},{4,2},{4,3},
            {4,4},{3,4},{2,4},{1,4},{0,4},{0,3},{0,2},{0,1},
            {2,1},{1,3},{3,3}
        };
        private static readonly List<RewardData> spinRewards = new List<RewardData>();
        private static readonly HashSet<int> finishedPositions = new HashSet<int>();
        private static readonly Scroll milestoneScroll = new Scroll();

        public static bool HasPointerCapture
        {
            get { return milestoneScroll.pointerIsDowning; }
        }
        private static Image milestoneChest;
        private static Image[] rankChests;
        private static bool spinning;
        private static int spinIndex;
        private static int currentPosition;
        private static long spinStartedAt;
        private static bool holdingTarget;
        private static long targetHoldStartedAt;
        private static bool configDirty;
        private static RankRewardData openedRank;
        private static MilestoneData openedMilestone;
        private static int leftX, leftY, leftW, leftH, rightX, rightY, rightW, rightH;
        private static int gridX, gridY, cellSize, cellGap;
        private const int RankTableOffsetY = 58;
        private const int RankRowHeight = 18;
        private const int MilestoneOffsetFromTable = 116;

        public static bool IsSpinning { get { return spinning; } }
        public static bool IsBusy { get { return spinning || Manager.Instance.Submitting; } }

        public static void Open()
        {
            EnsureAssets();
            Network.GetState();
        }

        public static void Close()
        {
            openedRank = null;
            openedMilestone = null;
            spinning = false;
            holdingTarget = false;
            milestoneScroll.clear();
            spinRewards.Clear();
            finishedPositions.Clear();
        }

        public static void OnConfigChanged()
        {
            if (spinning) configDirty = true;
            else Network.GetState();
        }

        public static void OnStateReloaded() { }

        public static void StartSpin(List<RewardData> rewards)
        {
            spinRewards.Clear();
            spinRewards.AddRange(rewards);
            finishedPositions.Clear();
            spinIndex = 0;
            currentPosition = 1;
            spinStartedAt = mSystem.currentTimeMillis();
            holdingTarget = false;
            spinning = spinRewards.Count > 0;
            if (!spinning) ShowRewards(rewards, "Nhận thưởng thành công");
        }

        public static void ShowRewards(List<RewardData> rewards, string text)
        {
            DragonPass.Manager manager = DragonPass.Manager.Instance;
            manager.ClaimedRewards.Clear();
            List<RewardData> merged = new List<RewardData>();
            Dictionary<string, RewardData> byItemAndOptions = new Dictionary<string, RewardData>();
            for (int i = 0; i < rewards.Count; i++)
            {
                RewardData source = rewards[i];
                string key = source.itemId + "|" + (source.options ?? "[]");
                RewardData combined;
                if (byItemAndOptions.TryGetValue(key, out combined))
                {
                    combined.quantity += source.quantity;
                    continue;
                }
                combined = new RewardData();
                combined.position = source.position; combined.itemId = source.itemId; combined.quantity = source.quantity;
                combined.options = source.options; combined.rateUnits = source.rateUnits;
                byItemAndOptions.Add(key, combined); merged.Add(combined);
            }
            for (int i = 0; i < merged.Count; i++)
            {
                RewardData source = merged[i];
                DragonPass.RewardData target = new DragonPass.RewardData();
                target.configured = true; target.itemId = source.itemId; target.quantity = source.quantity;
                target.options = source.options; target.rewardType = source.rewardType; manager.ClaimedRewards.Add(target);
            }
            manager.RewardPopupText = text;
            manager.RewardPopupSuccess = true;
            manager.RewardPopupSeconds = 5f;
        }

        public static void Paint(mGraphics g, int x, int y, int w, int h)
        {
            State state = Manager.Instance.Current;
            if (Manager.Instance.Submitting && mSystem.currentTimeMillis() - Manager.Instance.SubmitStartedAt > 15000L)
            {
                Manager.Instance.Submitting = false;
                Manager.Instance.ShowResult("Máy chủ phản hồi quá lâu, vui lòng thử lại");
            }
            UpdateAnimation();
            int gap = 6;
            leftX = x; leftY = y; leftW = (w - gap) * 56 / 100; leftH = h;
            rightX = leftX + leftW + gap; rightY = y; rightW = w - leftW - gap; rightH = h;
            g.setColor(0xD99A55); g.fillRect(leftX, leftY, leftW, leftH, 7);
            g.setColor(0xD99A55); g.fillRect(rightX, rightY, rightW, rightH, 7);
            PaintWheel(g, state);
            PaintRankingAndMilestones(g, state);
            if (openedRank != null) PaintRewardsPopup(g, openedRank.rewards, "Phần thưởng Top " + openedRank.rank);
            else if (openedMilestone != null) PaintRewardsPopup(g, openedMilestone.rewards,
                "Quà mốc " + openedMilestone.target );
            if (Manager.Instance.ResultSeconds > 0f && !string.IsNullOrEmpty(Manager.Instance.ResultText))
            {
                Manager.Instance.ResultSeconds -= UnityEngine.Time.deltaTime;
                mFont.tahoma_7b_red.drawStringBorder(g, Manager.Instance.ResultText, x + w / 2, y + h - 13,
                    mFont.CENTER, mFont.tahoma_7_grey);
            }
        }

        private static void PaintWheel(mGraphics g, State state)
        {
            int header = 8, bottom = 42;
            cellGap = 3;
            cellSize = System.Math.Min((leftW - 12 - cellGap * 4) / 5, (leftH - header - bottom - cellGap * 4) / 5);
            cellSize = System.Math.Max(25, cellSize);
            int gridW = cellSize * 5 + cellGap * 4;
            int gridH = gridW;
            gridX = leftX + (leftW - gridW) / 2;
            gridY = leftY + header;
            for (int i = 0; i < 19; i++)
            {
                int cx;
                int cy;
                GetCellCoordinates(i, out cx, out cy);
                RewardData reward = i < state.rewards.Count ? state.rewards[i] : null;
                bool selected = currentPosition == i + 1 || finishedPositions.Contains(i + 1);
                PaintCell(g, cx, cy, cellSize, reward, selected, i == 16);
            }
            int centerY = gridY + (cellSize + cellGap) * 2 + cellSize / 2 - 5;
            string pity = "Quay " + state.pityRemaining + " lần tất nhận";
            mFont.tahoma_7b_white.drawString(g, pity, leftX + leftW / 2, centerY, mFont.CENTER);
            int by = leftY + leftH - 31;
            int bw = (leftW - 18) / 2;
            string tenCaption = Manager.Instance.Submitting ? "Đang xử lý..." : "Quay 10 lần  " + state.priceTen;
            string oneCaption = Manager.Instance.Submitting ? "Đang xử lý..." : "Quay 1 lần  " + state.priceOne;
            if (spinning)
            {
                PaintButton(g, leftX + leftW / 2 - 44, by, 88, "Bỏ qua");
            }
            else
            {
                PaintButton(g, leftX + 6, by, bw, tenCaption);
                PaintButton(g, leftX + 12 + bw, by, leftW - bw - 18, oneCaption);
            }
        }

        private static void PaintCell(mGraphics g, int x, int y, int size, RewardData reward, bool selected, bool pity)
        {
            // Ô đang chạy dùng viền vàng tĩnh. Ô bảo hiểm nhận hiệu ứng
            // ánh sáng chuyển động ở lớp trên để luôn nổi bật.
            ModFunc.PaintInventoryItemSlot(g, x, y, size, size, selected, reward != null);
            if (reward != null)
            {
                ItemTemplate template = ItemTemplates.get(reward.itemId);
                if (template != null) SmallImage.drawSmallImage(g, template.iconID, x + size / 2, y + size / 2 - 2, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
                mFont.tahoma_7b_dark.drawString(g, "x" + reward.quantity, x + size - 3, y + size - 11, mFont.RIGHT);
            }
            else mFont.tahoma_7_grey.drawString(g, "?", x + size / 2, y + size / 2 - 4, mFont.CENTER);
            if (pity) PaintOrbitingBorderParticles(g, x - 1, y - 1, size + 2, size + 2);
        }

        private static void GetCellCoordinates(int index, out int x, out int y)
        {
            x = gridX + CellPositions[index, 0] * (cellSize + cellGap);
            y = gridY + CellPositions[index, 1] * (cellSize + cellGap);
            int inset = System.Math.Max(3, cellSize / 8);
            if (index == 16)
            {
                y += inset;
            }
            else if (index == 17)
            {
                x += inset;
                y -= inset;
            }
            else if (index == 18)
            {
                x -= inset;
                y -= inset;
            }
        }

        private static void PaintOrbitingBorderParticles(mGraphics g, int x, int y, int w, int h)
        {
            int horizontal = System.Math.Max(1, w - 1);
            int vertical = System.Math.Max(1, h - 1);
            int perimeter = 2 * (horizontal + vertical);
            int head = (int)((mSystem.currentTimeMillis() / 9L) % perimeter);
            const int particleCount = 14;
            const int spacing = 2;
            for (int i = particleCount - 1; i >= 0; i--)
            {
                int distance = head - i * spacing;
                while (distance < 0) distance += perimeter;
                distance %= perimeter;
                int px;
                int py;
                if (distance < horizontal)
                {
                    px = x + distance;
                    py = y;
                }
                else if (distance < horizontal + vertical)
                {
                    px = x + horizontal;
                    py = y + distance - horizontal;
                }
                else if (distance < horizontal * 2 + vertical)
                {
                    px = x + horizontal - (distance - horizontal - vertical);
                    py = y + vertical;
                }
                else
                {
                    px = x;
                    py = y + vertical - (distance - horizontal * 2 - vertical);
                }
                float alpha = 0.18f + (particleCount - i) * 0.8f / particleCount;
                int particleSize = 3;
                g.setColor(i == 0 ? 0xF3A000 : 0xFFD95A, alpha);
                g.fillRect(px - particleSize / 2, py - particleSize / 2, particleSize, particleSize, 2);
            }
        }

        private static void PaintRankingAndMilestones(mGraphics g, State state)
        {
            string time = TimeLeft(state.endsAt);
            mFont.tahoma_7b_yellow.drawString(g, "Thời gian còn lại:", rightX + 8, rightY + 5, mFont.LEFT);
            mFont.tahoma_7b_yellow.drawString(g, time, rightX + 8, rightY + 18, mFont.LEFT);
            mFont.tahoma_7b_white.drawString(g, "Thưởng hạng", rightX + 8, rightY + 32, mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, "Đủ 50 lượt được tham gia xếp hạng", rightX + 8, rightY + 44, mFont.LEFT);
            int tableY = rightY + RankTableOffsetY;
            g.setColor(0xB7773D); g.fillRect(rightX + 5, tableY, rightW - 10, 108, 6);
            mFont.tahoma_7b_yellow.drawString(g, "Hạng", rightX + 14, tableY + 6, mFont.LEFT);
            mFont.tahoma_7b_yellow.drawString(g, "Tên", rightX + 54, tableY + 6, mFont.LEFT);
            mFont.tahoma_7b_yellow.drawString(g, "Lượt", rightX + rightW - 70, tableY + 6, mFont.LEFT);
            for (int i = 0; i < 5; i++)
            {
                int ry = tableY + 22 + i * RankRowHeight;
                RankEntry entry = i < state.ranking.Count ? state.ranking[i] : null;
                mFont.tahoma_7b_yellow.drawString(g, (i + 1).ToString(), rightX + 20, ry, mFont.CENTER);
                mFont.tahoma_7_white.drawString(g, entry == null ? "-" : entry.name, rightX + 54, ry, mFont.LEFT);
                mFont.tahoma_7_white.drawString(g, entry == null ? "0" : entry.spins.ToString(), rightX + rightW - 45, ry, mFont.CENTER);
                int chestX = rightX + rightW - 18;
                Image chest = rankChests != null && i < rankChests.Length ? rankChests[i] : null;
                if (chest != null)
                {
                    int normalW = 20;
                    int normalH = ScaledAssetHeight(chest, normalW);
                    int normalX = chestX - normalW / 2;
                    int normalY = ry - normalH / 2 + 3;
                    bool pressed = GameCanvas.isPointerDown
                        && GameCanvas.isPointerHoldIn(normalX, normalY, normalW, normalH);
                    int chestW = pressed ? 17 : 20;
                    int chestH = ScaledAssetHeight(chest, chestW);
                    g.drawImageScaleClipped(chest, chestX - chestW / 2, ry + 3 - chestH / 2, chestW, chestH);
                }
                else mFont.tahoma_7b_yellow.drawString(g, "Quà", chestX, ry, mFont.CENTER);
            }
            int milestoneY = tableY + MilestoneOffsetFromTable;
            mFont.tahoma_7b_white.drawString(g, "Tổng lượt quay: " + state.totalSpins, rightX + 8, milestoneY, mFont.LEFT);
            int count = System.Math.Max(1, state.milestones.Count);
            int box = System.Math.Min(40, (rightW - 14) / System.Math.Min(8, count));
            ConfigureMilestoneScroll(state.milestones.Count, box, milestoneY + 17);
            milestoneScroll.updatecm();
            g.setClip(rightX + 7, milestoneY + 17, rightW - 14, 37);
            for (int i = 0; i < state.milestones.Count; i++)
            {
                MilestoneData milestone = state.milestones[i];
                int mx = rightX + 7 + i * box - milestoneScroll.cmx;
                bool ready = !milestone.claimed && state.totalSpins >= milestone.target;
                int tileSize = System.Math.Max(12, System.Math.Min(28, box - 4));
                int tileX = mx + (box - tileSize) / 2;
                int tileY = milestoneY + 19;
                g.setColor(ready ? 0xFFF2B3 : (milestone.claimed ? 0x969696 : 0xC7894B));
                g.fillRect(tileX, tileY, tileSize, tileSize, 5);
                if (milestoneChest != null)
                {
                    int normalChestW = System.Math.Min(16, tileSize - 5);
                    int normalChestH = ScaledAssetHeight(milestoneChest, normalChestW);
                    int normalChestX = tileX + (tileSize - normalChestW) / 2;
                    int normalChestY = tileY + 1;
                    bool pressed = GameCanvas.isPointerDown
                        && GameCanvas.isPointerHoldIn(normalChestX, normalChestY, normalChestW, normalChestH);
                    int chestW = pressed ? System.Math.Max(10, normalChestW - 2) : normalChestW;
                    int chestH = ScaledAssetHeight(milestoneChest, chestW);
                    int chestX = tileX + (tileSize - chestW) / 2;
                    int chestY = normalChestY + (normalChestH - chestH) / 2;
                    g.drawImageScaleClipped(milestoneChest, chestX, chestY, chestW, chestH);
                }
                mFont.tahoma_7b_yellow.drawString(g, milestone.target.ToString(), tileX + tileSize / 2,
                    tileY + tileSize - 10, mFont.CENTER);
                if (ready) PaintOrbitingBorderParticles(g, tileX, tileY, tileSize, tileSize);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        private static int ScaledAssetHeight(Image image, int displayWidth)
        {
            if (image == null || image.getWidth() <= 0) return displayWidth;
            return System.Math.Max(1, image.getHeight() * displayWidth / image.getWidth());
        }

        private static void PaintRewardsPopup(mGraphics g, List<RewardData> rewards, string title)
        {
            int count = System.Math.Max(1, rewards.Count);
            int columns = System.Math.Min(count, System.Math.Max(3, (GameCanvas.w - 28) / 42));
            int rows = (count + columns - 1) / columns;
            int w = System.Math.Min(GameCanvas.w - 20, System.Math.Max(190, columns * 42 + 20));
            int h = 92 + System.Math.Max(0, rows - 1) * 40;
            int x = (GameCanvas.w - w) / 2, y = (GameCanvas.h - h) / 2;
            ModFunc.PaintMailPopupBackground(g, x, y, w, h, 1);
            mFont.tahoma_7b_dark.drawString(g, title, x + w / 2, y + 8, mFont.CENTER);
            int gap = columns <= 3 ? 58 : System.Math.Max(28, (w - 24) / columns);
            for (int i = 0; i < rewards.Count; i++)
            {
                int row = i / columns, column = i % columns;
                int itemsInRow = System.Math.Min(columns, rewards.Count - row * columns);
                int cx = x + w / 2 + column * gap - (itemsInRow - 1) * gap / 2;
                int cy = y + 48 + row * 40;
                RewardData reward = rewards[i]; ItemTemplate template = ItemTemplates.get(reward.itemId);
                if (template != null) SmallImage.drawSmallImage(g, template.iconID, cx, cy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                mFont.tahoma_7b_dark.drawString(g, "x" + reward.quantity, cx, cy + 18, mFont.CENTER);
            }
            // mFont.tahoma_7_grey.drawString(g, "Bấm từng quà để xem thông tin", x + w / 2, y + h - 12, mFont.CENTER);
        }

        public static bool HandleInput(int x, int y, int w, int h)
        {
            List<RewardData> openedRewards = openedRank != null ? openedRank.rewards
                : (openedMilestone != null ? openedMilestone.rewards : null);
            if (openedRewards != null)
            {
                if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    int count = System.Math.Max(1, openedRewards.Count);
                    int columns = System.Math.Min(count, System.Math.Max(3, (GameCanvas.w - 28) / 42));
                    int rows = (count + columns - 1) / columns;
                    int pw = System.Math.Min(GameCanvas.w - 20, System.Math.Max(190, columns * 42 + 20));
                    int ph = 92 + System.Math.Max(0, rows - 1) * 40;
                    int px = (GameCanvas.w - pw) / 2, py = (GameCanvas.h - ph) / 2;
                    int gap = columns <= 3 ? 58 : System.Math.Max(28, (pw - 24) / columns);
                    for (int i = 0; i < openedRewards.Count; i++)
                    {
                        int row = i / columns, column = i % columns;
                        int itemsInRow = System.Math.Min(columns, openedRewards.Count - row * columns);
                        int cx = px + pw / 2 + column * gap - (itemsInRow - 1) * gap / 2;
                        int cy = py + 48 + row * 40;
                        if (GameCanvas.isPointerHoldIn(cx - 18, cy - 20, 36, 40))
                        {
                            ShowPreview(openedRewards[i]); GameCanvas.clearAllPointerEvent(); return true;
                        }
                    }
                    openedRank = null;
                    openedMilestone = null;
                    GameCanvas.clearAllPointerEvent(); return true;
                }
                return true;
            }
            if (Manager.Instance.Submitting)
            {
                if (GameCanvas.isPointerClick) GameCanvas.clearAllPointerEvent();
                return true;
            }
            if (!spinning && Manager.Instance.Current.milestones.Count > 0)
            {
                int milestoneY = rightY + RankTableOffsetY + MilestoneOffsetFromTable + 17;
                int count = Manager.Instance.Current.milestones.Count;
                int box = System.Math.Min(40, (rightW - 14) / System.Math.Min(8, count));
                ConfigureMilestoneScroll(count, box, milestoneY);
                ScrollResult milestoneResult = milestoneScroll.updateKey();
                milestoneScroll.updatecm();
                if (milestoneResult.isFinish)
                {
                    // Scroll chọn theo cmtoX trong khi asset được vẽ theo cmx. Dò
                    // trực tiếp hình chữ nhật đang hiển thị để bấm đúng cả lúc
                    // danh sách vừa trượt xong.
                    for (int i = 0; i < count; i++)
                    {
                        int mx = rightX + 7 + i * box - milestoneScroll.cmx;
                        int tileSize = System.Math.Max(12, System.Math.Min(28, box - 4));
                        int tileX = mx + (box - tileSize) / 2;
                        int tileY = milestoneY + 2;
                        int chestW = System.Math.Min(16, tileSize - 5);
                        int chestH = ScaledAssetHeight(milestoneChest, chestW);
                        int chestX = tileX + (tileSize - chestW) / 2;
                        int chestY = tileY + 1;
                        if (!ContainsPoint(GameCanvas.px, GameCanvas.py, chestX, chestY, chestW, chestH)) continue;
                        MilestoneData milestone = Manager.Instance.Current.milestones[i];
                        SoundMn.gI().buttonClick();
                        if (!milestone.claimed && Manager.Instance.Current.totalSpins >= milestone.target)
                            Network.ClaimMilestone(milestone.id);
                        else if (milestone.rewards.Count > 0)
                            openedMilestone = milestone;
                        break;
                    }
                    GameCanvas.clearAllPointerEvent(); return true;
                }
                if (milestoneScroll.pointerIsDowning) return true;
            }
            if (!GameCanvas.isPointerClick || !GameCanvas.isPointerJustRelease) return spinning;
            if (!spinning && !GameCanvas.isPointerHoldIn(x, y, w, h)) return false;
            if (spinning)
            {
                int by = leftY + leftH - 31;
                if (GameCanvas.isPointerHoldIn(leftX + leftW / 2 - 44, by, 88, 24))
                {
                    SoundMn.gI().buttonClick(); Skip();
                }
                GameCanvas.clearAllPointerEvent(); return true;
            }
            for (int i = 0; i < 19; i++)
            {
                int cx;
                int cy;
                GetCellCoordinates(i, out cx, out cy);
                if (GameCanvas.isPointerHoldIn(cx, cy, cellSize, cellSize) && i < Manager.Instance.Current.rewards.Count)
                { SoundMn.gI().buttonClick(); ShowPreview(Manager.Instance.Current.rewards[i]); GameCanvas.clearAllPointerEvent(); return true; }
            }
            int buttonY = leftY + leftH - 31, bw = (leftW - 18) / 2;
            if (GameCanvas.isPointerHoldIn(leftX + 6, buttonY, bw, 24)) SubmitSpin(10);
            else if (GameCanvas.isPointerHoldIn(leftX + 12 + bw, buttonY, leftW - bw - 18, 24)) SubmitSpin(1);
            else
            {
                int tableY = rightY + RankTableOffsetY;
                for (int i = 0; i < 5; i++)
                {
                    Image chest = rankChests != null && i < rankChests.Length ? rankChests[i] : null;
                    if (chest == null) continue;
                    int ry = tableY + 22 + i * RankRowHeight;
                    int chestW = 20;
                    int chestH = ScaledAssetHeight(chest, chestW);
                    int chestX = rightX + rightW - 18 - chestW / 2;
                    int chestY = ry + 3 - chestH / 2;
                    if (GameCanvas.isPointerHoldIn(chestX, chestY, chestW, chestH))
                    {
                        if (i < Manager.Instance.Current.rankRewards.Count)
                            openedRank = Manager.Instance.Current.rankRewards[i];
                        SoundMn.gI().buttonClick();
                        GameCanvas.clearAllPointerEvent(); return true;
                    }
                }
            }
            GameCanvas.clearAllPointerEvent(); return true;
        }

        private static bool ContainsPoint(int pointerX, int pointerY, int x, int y, int w, int h)
        {
            return pointerX >= x && pointerX <= x + w && pointerY >= y && pointerY <= y + h;
        }

        private static void SubmitSpin(int count)
        {
            if (Manager.Instance.Submitting) return;
            Manager.Instance.Submitting = true;
            Manager.Instance.SubmitStartedAt = mSystem.currentTimeMillis();
            SoundMn.gI().buttonClick(); Network.Spin(count); GameCanvas.clearAllPointerEvent();
        }

        private static void ConfigureMilestoneScroll(int count, int box, int y)
        {
            milestoneScroll.xPos = rightX + 7;
            milestoneScroll.yPos = y;
            milestoneScroll.width = rightW - 14;
            milestoneScroll.height = 37;
            milestoneScroll.ITEM_SIZE = box;
            milestoneScroll.nITEM = count;
            milestoneScroll.ITEM_PER_LINE = count;
            milestoneScroll.styleUPDOWN = false;
            milestoneScroll.cmxLim = System.Math.Max(0, count * box - milestoneScroll.width);
            if (milestoneScroll.cmtoX > milestoneScroll.cmxLim) milestoneScroll.cmtoX = milestoneScroll.cmxLim;
            if (milestoneScroll.cmx > milestoneScroll.cmxLim) milestoneScroll.cmx = milestoneScroll.cmxLim;
        }

        private static void UpdateAnimation()
        {
            if (!spinning || spinIndex >= spinRewards.Count) return;
            long now = mSystem.currentTimeMillis();
            int target = System.Math.Max(1, spinRewards[spinIndex].position);
            if (holdingTarget)
            {
                currentPosition = target;
                if (now - targetHoldStartedAt < 1500L) return;
                finishedPositions.Add(target);
                spinIndex++;
                holdingTarget = false;
                if (spinIndex >= spinRewards.Count)
                {
                    FinishAnimation();
                }
                else
                {
                    currentPosition = 1;
                    spinStartedAt = now;
                }
                return;
            }
            long elapsed = now - spinStartedAt;
            float progress = System.Math.Min(1f, elapsed / 5000f);
            float eased = 1f - (float)System.Math.Pow(1f - progress, 3f);
            int totalSteps = 19 * 3 + target - 1;
            int step = System.Math.Min(totalSteps, (int)(eased * totalSteps));
            currentPosition = step % 19 + 1;
            if (progress < 1f) return;
            currentPosition = target;
            holdingTarget = true;
            targetHoldStartedAt = now;
        }

        private static void Skip()
        {
            for (int i = 0; i < spinRewards.Count; i++) finishedPositions.Add(spinRewards[i].position);
            FinishAnimation();
        }

        private static void FinishAnimation()
        {
            spinning = false; currentPosition = 0; holdingTarget = false;
            ShowRewards(spinRewards, "Nhận thưởng thành công");
            spinRewards.Clear(); finishedPositions.Clear();
            if (configDirty) { configDirty = false; Network.GetState(); }
            else Network.GetState();
        }

        private static void ShowPreview(RewardData reward)
        {
            DragonPass.RewardData data = new DragonPass.RewardData(); data.configured = true; data.itemId = reward.itemId;
            data.quantity = reward.quantity; data.options = reward.options; data.rewardType = reward.rewardType;
            DragonPass.DragonPassUI.ShowExternalRewardPreview(data);
        }

        private static void PaintButton(mGraphics g, int x, int y, int w, string text)
        {
            bool pressed = GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(x, y, w, 24);
            ModFunc.PaintMailActionButton(g, x, y, w, text, pressed);
        }

        private static string TimeLeft(long endsAt)
        {
            long seconds = System.Math.Max(0L, (endsAt - mSystem.currentTimeMillis()) / 1000L);
            long days = seconds / 86400L; seconds %= 86400L; long hours = seconds / 3600L; long minutes = seconds % 3600L / 60L;
            return days + " ngày " + hours + " giờ " + minutes + " phút";
        }

        private static void EnsureAssets()
        {
            if (milestoneChest == null) milestoneChest = GameCanvas.loadImage("/chest_icons/chest_256_256_1.png");
            if (rankChests == null)
            {
                rankChests = new Image[5];
                rankChests[0] = GameCanvas.loadImage("/chest_icons/chest_256_256_9.png");
                rankChests[1] = GameCanvas.loadImage("/chest_icons/chest_256_256_11.png");
                rankChests[2] = GameCanvas.loadImage("/chest_icons/chest_256_256_3.png");
                rankChests[3] = GameCanvas.loadImage("/chest_icons/chest_256_256_15.png");
                rankChests[4] = GameCanvas.loadImage("/chest_icons/chest_256_256_5.png");
            }
        }
    }
}
