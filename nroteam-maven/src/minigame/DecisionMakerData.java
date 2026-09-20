/*
 * Copyright by SOULMATE
 */

package minigame;

public class DecisionMakerData {
    public long id;
    public long money;
    public byte type;
    public boolean isNormal;
    public static int timeGame;
    public static int timeDelay;
    public static int timeGameDefalue = 50;

    public static class resulPlayer {
        public String name;
        public long money;
        public byte type;
    }
}
