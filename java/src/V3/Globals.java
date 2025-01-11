package V3;

import battlecode.common.*;

public class Globals extends Utils {
    public static RobotController rc;

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;
    public static boolean in_debug = true;
    public static LocMap vis = new LocMap(mapWidth, mapHeight);
    // stealing an LCG cause nextInt is a bytecode hog
    private static long seed = -1;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();

        seed = rc.getID();
    }

    public static int nextInt() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL;
        return (int) ((seed >>> 16) & 0x7FFFFFFF);
    }

    public static double nextDouble() {
        return (double) nextInt() / 0x7FFFFFFF;
    }
}
