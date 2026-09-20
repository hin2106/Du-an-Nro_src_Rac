using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

namespace Mail
{



    public class MailUIController : MonoBehaviour
    {
        [SerializeField] private Transform mailListContainer;
        [SerializeField] private GameObject mailListItemPrefab;
        [SerializeField] private Transform mailDetailPanel;
        [SerializeField] private TextMeshProUGUI mailTitleText;
        [SerializeField] private TextMeshProUGUI mailContentText;
        [SerializeField] private TextMeshProUGUI mailExpiryText;
        [SerializeField] private Transform rewardListContainer;
        [SerializeField] private GameObject rewardItemPrefab;
        [SerializeField] private Button closeDetailButton;
        [SerializeField] private Button claimAllRewardsButton;
        [SerializeField] private TextMeshProUGUI unreadCountText;
        [SerializeField] private TextMeshProUGUI mailCountText;

        private MailListItem[] _listItems;
        private Dictionary<long, MailListItem> _mailItemsMap = new Dictionary<long, MailListItem>();
        private List<RewardUIItem> _rewardUIItems = new List<RewardUIItem>();

        private void Start()
        {
            InitializeUI();
            SubscribeToEvents();
        }

        private void OnDestroy()
        {
            UnsubscribeFromEvents();
        }

        private void InitializeUI()
        {
            if (closeDetailButton != null)
            {
                closeDetailButton.onClick.AddListener(() =>
                {
                    SoundMn.gI().buttonClick();
                    CloseMailDetail();
                });
            }

            if (claimAllRewardsButton != null)
            {
                claimAllRewardsButton.onClick.AddListener(() =>
                {
                    SoundMn.gI().buttonClick();
                    ClaimAllRewards();
                });
            }


            if (mailDetailPanel != null)
            {
                mailDetailPanel.gameObject.SetActive(false);
            }
        }

        private void SubscribeToEvents()
        {
            if (MailManager.Instance != null)
            {
                MailManager.Instance.OnMailListUpdated += UpdateMailList;
                MailManager.Instance.OnMailOpened += DisplayMailDetail;
                MailManager.Instance.OnMailDeleted += OnMailDeleted;
                MailManager.Instance.OnRewardsClaimed += OnRewardsClaimed;
                MailManager.Instance.OnMailCountUpdated += UpdateMailCount;
                MailManager.Instance.OnMailError += OnMailError;
            }
        }

        private void UnsubscribeFromEvents()
        {
            if (MailManager.Instance != null)
            {
                MailManager.Instance.OnMailListUpdated -= UpdateMailList;
                MailManager.Instance.OnMailOpened -= DisplayMailDetail;
                MailManager.Instance.OnMailDeleted -= OnMailDeleted;
                MailManager.Instance.OnRewardsClaimed -= OnRewardsClaimed;
                MailManager.Instance.OnMailCountUpdated -= UpdateMailCount;
                MailManager.Instance.OnMailError -= OnMailError;
            }
        }




        private void UpdateMailList(List<MailData> mails)
        {

            foreach (Transform child in mailListContainer)
            {
                Destroy(child.gameObject);
            }
            _mailItemsMap.Clear();


            foreach (var mail in mails)
            {
                CreateMailListItem(mail);
            }
        }




        private void CreateMailListItem(MailData mail)
        {
            if (mailListItemPrefab == null)
                return;

            GameObject itemGo = Instantiate(mailListItemPrefab, mailListContainer);
            MailListItem item = itemGo.GetComponent<MailListItem>();

            if (item != null)
            {
                item.Initialize(mail, () =>
                {
                    MailManager.Instance.OpenMail(mail.id);
                });

                _mailItemsMap[mail.id] = item;
            }
        }




        private void DisplayMailDetail(MailData mail)
        {
            if (mailDetailPanel == null)
                return;


            mailDetailPanel.gameObject.SetActive(true);


            if (mailTitleText != null)
                mailTitleText.text = mail.title;

            if (mailContentText != null)
                mailContentText.text = mail.content;


            if (mailExpiryText != null)
            {
                if (mail.IsExpired())
                {
                    mailExpiryText.text = "Hết hạn";
                    mailExpiryText.color = Color.red;
                }
                else
                {
                    int daysLeft = mail.GetDaysUntilExpire();
                    mailExpiryText.text = $"Hết hạn trong {daysLeft} ngày";
                    mailExpiryText.color = daysLeft <= 1 ? Color.yellow : Color.white;
                }
            }


            DisplayRewards(mail.rewards);


            if (claimAllRewardsButton != null)
            {
                bool hasUnclaimed = mail.HasUnclaimedRewards();
                claimAllRewardsButton.interactable = hasUnclaimed;
            }
        }




        private void DisplayRewards(List<RewardData> rewards)
        {

            foreach (var item in _rewardUIItems)
            {
                if (item != null && item.gameObject != null)
                    Destroy(item.gameObject);
            }
            _rewardUIItems.Clear();

            if (rewardListContainer == null || rewardItemPrefab == null)
                return;


            foreach (var reward in rewards)
            {
                CreateRewardUIItem(reward);
            }


            if (rewards.Count == 0)
            {
                TextMeshProUGUI noRewardText = rewardListContainer.GetComponentInChildren<TextMeshProUGUI>();
                if (noRewardText == null)
                {
                    GameObject textGo = new GameObject("NoRewardsText");
                    textGo.transform.SetParent(rewardListContainer);
                    noRewardText = textGo.AddComponent<TextMeshProUGUI>();
                    noRewardText.text = "Không có reward";
                    noRewardText.alignment = TextAlignmentOptions.Center;
                }
            }
        }




        private void CreateRewardUIItem(RewardData reward)
        {
            if (rewardItemPrefab == null)
                return;

            GameObject itemGo = Instantiate(rewardItemPrefab, rewardListContainer);
            RewardUIItem item = itemGo.GetComponent<RewardUIItem>();

            if (item != null)
            {
                item.Initialize(reward, () =>
                {
                    if (!reward.claimed)
                    {
                        MailManager.Instance.ClaimRewards(reward.id);
                    }
                });

                _rewardUIItems.Add(item);
            }
        }




        private void ClaimAllRewards()
        {
            var mail = MailManager.Instance.GetCurrentOpenedMail();
            if (mail == null)
                return;

            var unclaimedIds = new List<long>();
            foreach (var reward in mail.rewards)
            {
                if (!reward.claimed)
                    unclaimedIds.Add(reward.id);
            }

            if (unclaimedIds.Count > 0)
            {
                MailManager.Instance.ClaimRewards(unclaimedIds);
            }
        }




        private void CloseMailDetail()
        {
            if (mailDetailPanel != null)
            {
                mailDetailPanel.gameObject.SetActive(false);
            }
        }




        private void OnMailDeleted(long mailId)
        {
            if (_mailItemsMap.TryGetValue(mailId, out var item))
            {
                Destroy(item.gameObject);
                _mailItemsMap.Remove(mailId);
            }

            CloseMailDetail();
        }




        private void OnRewardsClaimed(int claimedCount, string message)
        {

            foreach (var rewardUI in _rewardUIItems)
            {
                if (rewardUI != null)
                {
                    rewardUI.UpdateUI();
                }
            }


            if (claimedCount > 0)
            {
                ShowNotification(message, Color.green);
            }
        }




        private void UpdateMailCount(int total)
        {
            if (mailCountText != null)
                mailCountText.text = $"Tổng: {total}";
        }




        private void OnMailError(string error)
        {
            ShowNotification($"Lỗi: {error}", Color.red);
        }




        private void ShowNotification(string message, Color color)
        {
            Debug.Log($"[Mail UI] {message}");

        }




        public void RefreshMailList()
        {
            if (MailManager.Instance != null)
            {
                MailManager.Instance.RefreshMailList();
            }
        }




        public void DeleteCurrentMail()
        {
            var mail = MailManager.Instance.GetCurrentOpenedMail();
            if (mail != null)
            {

                MailManager.Instance.DeleteMail(mail.id);
            }
        }
    }




    public class MailListItem : MonoBehaviour
    {
        [SerializeField] private TextMeshProUGUI titleText;
        [SerializeField] private TextMeshProUGUI previewText;
        [SerializeField] private TextMeshProUGUI dateText;
        [SerializeField] private UnityEngine.UI.Image unreadIndicator;
        [SerializeField] private UnityEngine.UI.Image hasRewardIndicator;
        [SerializeField] private Button clickButton;

        private MailData _mail;

        public void Initialize(MailData mail, Action onClicked)
        {
            _mail = mail;

            if (titleText != null)
                titleText.text = mail.title;

            if (previewText != null)
                previewText.text = mail.content.Length > 50 ? 
                    mail.content.Substring(0, 50) + "..." : mail.content;

            if (dateText != null)
            {
                DateTime date = new DateTime(mail.expiredAt);
                dateText.text = date.ToString("dd/MM/yyyy");
            }

            if (unreadIndicator != null)
                unreadIndicator.gameObject.SetActive(!mail.isRead);

            if (hasRewardIndicator != null)
                hasRewardIndicator.gameObject.SetActive(mail.HasUnclaimedRewards());

            if (clickButton != null)
            {
                clickButton.onClick.AddListener(() =>
                {
                    SoundMn.gI().buttonClick();
                    onClicked?.Invoke();
                });
            }
        }

        public void UpdateUI()
        {
            if (_mail == null)
                return;

            if (unreadIndicator != null)
                unreadIndicator.gameObject.SetActive(!_mail.isRead);

            if (hasRewardIndicator != null)
                hasRewardIndicator.gameObject.SetActive(_mail.HasUnclaimedRewards());
        }
    }




    public class RewardUIItem : MonoBehaviour
    {
        [SerializeField] private TextMeshProUGUI rewardTypeText;
        [SerializeField] private TextMeshProUGUI amountText;
        [SerializeField] private Button claimButton;
        [SerializeField] private UnityEngine.UI.Image claimedIndicator;

        private RewardData _reward;

        public void Initialize(RewardData reward, Action onClaimClicked)
        {
            _reward = reward;
            UpdateUI();

            if (claimButton != null)
            {
                claimButton.onClick.AddListener(() =>
                {
                    SoundMn.gI().buttonClick();
                    onClaimClicked?.Invoke();
                });
                claimButton.interactable = !reward.claimed;
            }
        }

        public void UpdateUI()
        {
            if (_reward == null)
                return;

            if (rewardTypeText != null)
                rewardTypeText.text = _reward.GetRewardTypeName();

            if (amountText != null)
                amountText.text = $"x{_reward.amount}";

            if (claimedIndicator != null)
                claimedIndicator.gameObject.SetActive(_reward.claimed);

            if (claimButton != null)
            {
                claimButton.interactable = !_reward.claimed;
                claimButton.GetComponentInChildren<TextMeshProUGUI>().text = 
                    _reward.claimed ? "Đã nhận" : "Nhận";
            }
        }
    }
}
