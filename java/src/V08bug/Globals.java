package V08bug;

import battlecode.common.*;

import java.util.Random;

/** Contains methods and fields that everything should have access to */
public class Globals {
    public static RobotController rc;

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;
    public static boolean in_debug = true;
    public static final Random rng = new Random(6147);
    // stealing an LCG cause nextInt is a bytecode hog
    private static long seed = -1;

    // constants for finding best mop swing direction (N, E, S, W)
    public static final int[][] dxMop = {{-1, 0, 1, -1, 0, 1}, {1, 1, 1, 2, 2, 2}, {-1, 0, 1, -1, 0, 1}, {-1, -1, -1, -2, -2, -2}};
    public static final int[][] dyMop = {{1, 1, 1, 2, 2, 2}, {-1, 0, 1, -1, 0, 1}, {-1, -1, -1, -2, -2, -2}, {-1, 0, 1, -1, 0, 1}};

    public static Direction[] adjacentDirections = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST,
        Direction.NORTHEAST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
        Direction.NORTHWEST
    };

    public static void init(RobotController robotController) {
        rc = robotController;

        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();

        seed = rc.getID() * 3607L + rc.getMapHeight() * 61L + rc.getMapWidth() + rc.getRoundNum();
    }

    /** "A Big Line (very steep), modulus" */
    /** Random integer generator between 0 and INT.MAX */
    public static int nextInt() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL;
        return (int) ((seed >>> 16) & 0x7FFFFFFF);
    }

    /** Random double generator between 0 and 1 */
    public static double nextDouble() {
        return (double) nextInt() / 0x7FFFFFFF;
    }

    /** "Clamp" */
    public static int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /** Encodes a map location into an integer */
    public static int pack(MapLocation loc) {
        return loc.x * Globals.mapHeight + loc.y;
    }

    /** Decodes an integer into a MapLocation */
    public static MapLocation unpack(int loc) {
        return new MapLocation(loc / mapHeight, loc % mapHeight);
    }

    /** Returns if the unit is a paint tower */
    public static boolean isPaintTower(UnitType robotType) {
        return robotType.equals(UnitType.LEVEL_ONE_PAINT_TOWER)
                || robotType.equals(UnitType.LEVEL_TWO_PAINT_TOWER)
                || robotType.equals(UnitType.LEVEL_THREE_PAINT_TOWER);
    }

    /** Returns if the unit is a money tower */
    public static boolean isMoneyTower(UnitType robotType) {
        return robotType.equals(UnitType.LEVEL_ONE_MONEY_TOWER)
                || robotType.equals(UnitType.LEVEL_TWO_MONEY_TOWER)
                || robotType.equals(UnitType.LEVEL_THREE_MONEY_TOWER);
    }
}
