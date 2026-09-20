using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Assets.Scripts.Mod
{

    public class CustomBossNotification
    {

        public CustomBossNotification()
        {
            this.timeStart = null;
            this.timeEnd = null;
        }


        public string getTimeStartBoss()
        {
            if (this.timeStart == null)
            {
                return "Chưa có thông tin";
            }
            TimeSpan timeSpan = DateTime.Now.Subtract(this.timeStart.Value);
            int num = (int)timeSpan.TotalSeconds;
            return string.Concat(new string[]
            {
            this.timeStart.Value.ToString("HH"),
            "h:",
            this.timeStart.Value.ToString("mm"),
            " (",
            (num < 60) ? (num.ToString() + "s") : (timeSpan.Minutes.ToString() + "ph"),
            " trước)"
            });
        }


        public string getMapBoss()
        {
            if (this.map != null && !(this.map == ""))
            {
                return this.map;
            }
            return "Chưa có thông tin";
        }


        public string getTimeBossDie()
        {
            if (this.timeEnd == null)
            {
                return "Chưa có thông tin";
            }
            TimeSpan timeSpan = DateTime.Now.Subtract(this.timeEnd.Value);
            int num = (int)timeSpan.TotalSeconds;
            return string.Concat(new string[]
            {
            this.timeEnd.Value.ToString("HH"),
            "h:",
            this.timeEnd.Value.ToString("mm"),
            " (",
            (num < 60) ? (num.ToString() + "s") : (timeSpan.Minutes.ToString() + "ph"),
            " trước)"
            });
        }


        public string getBossKiller()
        {
            if (this.player != null && !(this.player == ""))
            {
                return this.player;
            }
            return "Chưa có thông tin";
        }


        public string name;


        public string map;


        public int mapId;


        public DateTime? timeStart;


        public DateTime? timeEnd;


        public string player;
    }
}