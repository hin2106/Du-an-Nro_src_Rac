using System;
using System.Collections.Generic;
using UnityEngine;

namespace Mail
{



    [System.Serializable]
    public class RewardData
    {
        public long id;
        public byte rewardType;
        public long rewardId;
        public int amount;
        public bool claimed;
        public string optionsData;

        public RewardData() { }

        public RewardData(long id, byte rewardType, long rewardId, int amount, bool claimed, string optionsData = "")
        {
            this.id = id;
            this.rewardType = rewardType;
            this.rewardId = rewardId;
            this.amount = amount;
            this.claimed = claimed;
            this.optionsData = optionsData ?? "";
        }

        public string GetRewardTypeName()
        {
            return rewardType switch
            {
                1 => "Item",
                2 => "Vàng",
                3 => "Ngọc",
                4 => "Sức chịu đựng",
                5 => "Kinh nghiệm",
                6 => "VIP Point",
                _ => "Unknown"
            };
        }

        public override string ToString()
        {
            return $"RewardData{{id={id}, type={GetRewardTypeName()}, rewardId={rewardId}, amount={amount}, claimed={claimed}}}";
        }
    }




    [System.Serializable]
    public class MailData
    {
        public long id;
        public string title;
        public string content;
        public bool isRead;
        public bool hasReward;
        public long expiredAt;
        public List<RewardData> rewards = new List<RewardData>();

        public MailData() { }

        public MailData(long id, string title, bool isRead, bool hasReward, long expiredAt)
        {
            this.id = id;
            this.title = title;
            this.isRead = isRead;
            this.hasReward = hasReward;
            this.expiredAt = expiredAt;
        }







        private static long GetCurrentUnixMillis()
        {
            return (long)(DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds;
        }

        public bool IsExpired()
        {
            return GetCurrentUnixMillis() > expiredAt;
        }




        public int GetDaysUntilExpire()
        {
            long now = GetCurrentUnixMillis();
            long remainingMs = expiredAt - now;
            if (remainingMs <= 0) return 0;
            return (int)(remainingMs / (1000L * 60 * 60 * 24)) + 1;
        }




        public bool HasUnclaimedRewards()
        {
            foreach (var reward in rewards)
            {
                if (!reward.claimed)
                    return true;
            }
            return false;
        }




        public List<RewardData> GetUnclaimedRewards()
        {
            var unclaimed = new List<RewardData>();
            foreach (var reward in rewards)
            {
                if (!reward.claimed)
                    unclaimed.Add(reward);
            }
            return unclaimed;
        }

        public void AddReward(RewardData reward)
        {
            if (reward != null)
                rewards.Add(reward);
        }

        public override string ToString()
        {
            return $"MailData{{id={id}, title='{title}', isRead={isRead}, expiredAt={expiredAt}, rewardCount={rewards.Count}}}";
        }
    }
}
