using System;
using System.Collections.Generic;

namespace Mail
{




    public static class MailNetworkManager
    {
        public const sbyte CMD_MAIL = -48;

        public const byte ACTION_GET_LIST = 0;
        public const byte ACTION_GET_DETAIL = 1;
        public const byte ACTION_MARK_READ = 2;
        public const byte ACTION_CLAIM_REWARDS = 3;
        public const byte ACTION_DELETE = 4;




        public static void RequestMailList(int page, int pageSize)
        {
            Message message = null;
            try
            {
                message = new Message(CMD_MAIL);
                message.writer().writeByte(ACTION_GET_LIST);
                message.writer().writeByte(page);
                message.writer().writeByte(pageSize);
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally
            {
                if (message != null) message.cleanup();
            }
        }




        public static void RequestMailDetail(long mailId)
        {
            Message message = null;
            try
            {
                message = new Message(CMD_MAIL);
                message.writer().writeByte(ACTION_GET_DETAIL);
                message.writer().writeInt((int)mailId);
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally
            {
                if (message != null) message.cleanup();
            }
        }




        public static void RequestMarkRead(long mailId)
        {
            Message message = null;
            try
            {
                message = new Message(CMD_MAIL);
                message.writer().writeByte(ACTION_MARK_READ);
                message.writer().writeInt((int)mailId);
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally
            {
                if (message != null) message.cleanup();
            }
        }




        public static void RequestClaimRewards(long[] rewardIds)
        {
            Message message = null;
            try
            {
                message = new Message(CMD_MAIL);
                message.writer().writeByte(ACTION_CLAIM_REWARDS);
                message.writer().writeByte(rewardIds.Length);
                for (int i = 0; i < rewardIds.Length; i++)
                {
                    message.writer().writeInt((int)rewardIds[i]);
                }
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally
            {
                if (message != null) message.cleanup();
            }
        }




        public static void RequestDeleteMail(long mailId)
        {
            Message message = null;
            try
            {
                message = new Message(CMD_MAIL);
                message.writer().writeByte(ACTION_DELETE);
                message.writer().writeInt((int)mailId);
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally
            {
                if (message != null) message.cleanup();
            }
        }
    }
}
