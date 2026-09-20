using System;
using System.Collections.Generic;
using UnityEngine;

namespace Mail
{





    public class MailManager : MonoBehaviour
    {
        private static MailManager _instance;
        public static MailManager Instance
        {
            get
            {
                if (_instance == null)
                {
                    GameObject go = new GameObject("MailManager");
                    _instance = go.AddComponent<MailManager>();
                }
                return _instance;
            }
        }


        public event Action<List<MailData>> OnMailListUpdated;
        public event Action<MailData> OnMailOpened;
        public event Action<long> OnMailDeleted;
        public event Action<bool, string> OnDeleteResult;
        public event Action<int, string> OnRewardsClaimed;
        public event Action<string> OnMailError;
        public event Action<int> OnMailCountUpdated;
        public event Action<string> OnNewMailNotify;


        private List<MailData> _mailList = new List<MailData>();
        private MailData _currentOpenedMail;
        private int _totalMailCount = 0;


        private int pageSize = 20;
        private float refreshInterval = 60f;

        private float _lastRefreshTime = 0f;
        private int _currentPage = 0;

        private void Awake()
        {
            if (_instance != null && _instance != this)
            {
                Destroy(gameObject);
                return;
            }

            _instance = this;
            DontDestroyOnLoad(gameObject);
        }

        private void Update()
        {
            _lastRefreshTime += Time.deltaTime;
            if (_lastRefreshTime >= refreshInterval)
            {
                _lastRefreshTime = 0f;
                LoadMailList(_currentPage);
            }
        }








        public void HandleServerMessage(Message msg)
        {
            try
            {
                byte action = (byte)msg.reader().readByte();
                switch (action)
                {
                    case MailNetworkManager.ACTION_GET_LIST:
                        HandleMailList(msg);
                        break;
                    case MailNetworkManager.ACTION_GET_DETAIL:
                        HandleMailDetail(msg);
                        break;
                    case 3:
                        HandleClaimResult(msg);
                        break;
                    case MailNetworkManager.ACTION_DELETE:
                        HandleDeleteResult(msg);
                        break;
                    case 5:
                        HandleMailCount(msg);
                        break;
                    case 6:
                        HandleNewMailNotify(msg);
                        break;
                }
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error handling server message: " + e.Message);
            }
        }

        private void HandleMailList(Message msg)
        {
            try
            {
                int page = msg.reader().readByte();
                int totalCount = msg.reader().readShort();
                int count = msg.reader().readByte();

                _totalMailCount = totalCount;
                _mailList.Clear();

                for (int i = 0; i < count; i++)
                {
                    MailData mail = new MailData();
                    mail.id = msg.reader().readInt();
                    mail.title = msg.reader().readUTF();
                    mail.isRead = msg.reader().readBoolean();
                    mail.hasReward = msg.reader().readBoolean();
                    mail.expiredAt = msg.reader().readLong();
                    _mailList.Add(mail);
                }

                OnMailListUpdated?.Invoke(_mailList);
                OnMailCountUpdated?.Invoke(_totalMailCount);
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing mail list: " + e.Message);
            }
        }

        private void HandleMailDetail(Message msg)
        {
            try
            {
                MailData mail = new MailData();
                mail.id = msg.reader().readInt();
                mail.title = msg.reader().readUTF();
                mail.content = msg.reader().readUTF();
                mail.isRead = msg.reader().readBoolean();
                mail.expiredAt = msg.reader().readLong();
                int rewardCount = msg.reader().readByte();

                for (int i = 0; i < rewardCount; i++)
                {
                    RewardData reward = new RewardData();
                    reward.id = msg.reader().readInt();
                    reward.rewardType = (byte)msg.reader().readByte();
                    reward.rewardId = msg.reader().readInt();
                    reward.amount = msg.reader().readInt();
                    reward.claimed = msg.reader().readBoolean();

                    // Packet mail cu ket thuc moi reward ngay sau truong claimed.
                    // Packet moi co them UTF optionsData. Giu kha nang doc ca hai
                    // phien ban de client khong bi EOF khi server cu chua khoi dong lai.
                    int remainingRewardCount = rewardCount - i - 1;
                    const int legacyRewardSize = 14; // int + byte + int + int + boolean
                    int minimumLegacyBytes = remainingRewardCount * legacyRewardSize;
                    reward.optionsData = msg.reader().available() > minimumLegacyBytes
                        ? msg.reader().readUTF()
                        : string.Empty;
                    mail.rewards.Add(reward);
                }

                _currentOpenedMail = mail;


                for (int i = 0; i < _mailList.Count; i++)
                {
                    if (_mailList[i].id == mail.id)
                    {
                        _mailList[i].isRead = true;
                        break;
                    }
                }

                OnMailOpened?.Invoke(mail);
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing mail detail: " + e.Message);
            }
        }

        private void HandleClaimResult(Message msg)
        {
            try
            {
                int claimedCount = msg.reader().readByte();
                string message = msg.reader().readUTF();
                OnRewardsClaimed?.Invoke(claimedCount, message);


                if (_currentOpenedMail != null)
                {
                    OpenMail(_currentOpenedMail.id);
                }
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing claim result: " + e.Message);
            }
        }

        private void HandleMailCount(Message msg)
        {
            try
            {
                _totalMailCount = msg.reader().readShort();
                OnMailCountUpdated?.Invoke(_totalMailCount);
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing mail count: " + e.Message);
            }
        }

        private void HandleNewMailNotify(Message msg)
        {
            try
            {
                string title = msg.reader().readUTF();
                OnNewMailNotify?.Invoke(title);

                LoadMailList(_currentPage);
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing new mail notify: " + e.Message);
            }
        }





        public void LoadMailList(int page = 0)
        {
            _currentPage = page;
            MailNetworkManager.RequestMailList(page, pageSize);
        }

        public void RefreshMailList()
        {
            LoadMailList(_currentPage);
        }

        public void OpenMail(long mailId)
        {
            MailNetworkManager.RequestMailDetail(mailId);
        }

        public void MarkMailAsRead(long mailId)
        {
            MailNetworkManager.RequestMarkRead(mailId);
        }

        public void ClaimRewards(long rewardId)
        {
            MailNetworkManager.RequestClaimRewards(new long[] { rewardId });
        }

        public void ClaimRewards(List<long> rewardIds)
        {
            MailNetworkManager.RequestClaimRewards(rewardIds.ToArray());
        }

        public void DeleteMail(long mailId)
        {

            MailNetworkManager.RequestDeleteMail(mailId);
        }

        private void HandleDeleteResult(Message msg)
        {
            try
            {
                bool success = msg.reader().readBoolean();
                string message = msg.reader().readUTF();
                if (success && _currentOpenedMail != null)
                {
                    long deletedId = _currentOpenedMail.id;
                    _mailList.RemoveAll(m => m.id == deletedId);
                    _currentOpenedMail = null;
                    OnMailDeleted?.Invoke(deletedId);
                }
                OnDeleteResult?.Invoke(success, message);
            }
            catch (Exception e)
            {
                Debug.LogError("[Mail] Error parsing delete result: " + e.Message);
            }
        }


        public List<MailData> GetMailList() => _mailList;
        public MailData GetCurrentOpenedMail() => _currentOpenedMail;
        public int GetTotalMailCount() => _totalMailCount;

        public int GetUnreadMailCount()
        {
            int unreadCount = 0;
            for (int i = 0; i < _mailList.Count; i++)
            {
                if (!_mailList[i].isRead)
                {
                    unreadCount++;
                }
            }

            return unreadCount;
        }

        public bool HasUnreadMail() => GetUnreadMailCount() > 0;
    }
}
