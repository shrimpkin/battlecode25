package V01map;

import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.Random;

public class Globals {
    public static RobotController rc;
    public static final Random rng = new Random(6147);

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;
    public static LocMap vis = new LocMap(mapWidth, mapHeight);
    // stealing an LCG cause nextInt is a bytecode hog
    private static long seed = -1;
    static int nextInt() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL;
        return (int) ((seed >>> 16) & 0x7FFFFFFF);
    }
    static double nextDouble() {
        return (double) nextInt() / 0x7FFFFFFF;
    }

    // TODO not entirely sure about this
    // this variable should represent the max dist a bot can move??
    public static int maxDistance;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();

        int halfSize = Math.max(Globals.mapWidth, Globals.mapHeight) / 2;
        maxDistance = halfSize * halfSize;

        seed = rc.getID();
    }
}
