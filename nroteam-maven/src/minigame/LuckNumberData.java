/*
 * Copyright by SOULMATE
 */

package minigame;

public class LuckNumberData {
    public long id;
    public int number;
    public boolean isGem;
    public boolean isReward;
    public static int timeGame;
    public static int timeDelay;
    public static int timeGameDefalue = 50;

    public static class LuckyNumberResul {
        public long id;
        public long money;
        public int number;
        public String text;
    }
}
