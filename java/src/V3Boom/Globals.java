package V3Boom;

import battlecode.common.*;

/**
 * Contains methods and fields that everything should have access too 
 */
public class Globals {
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

    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static int pack(MapLocation loc) {
        return loc.x * Globals.mapHeight + loc.y;
    }

    public static MapLocation unpack(int loc) {
        return new MapLocation(
                loc / Globals.mapHeight, loc % Globals.mapHeight
        );
    }

    public static boolean isPaintTower(UnitType robotType) {
        return robotType.equals(UnitType.LEVEL_ONE_PAINT_TOWER)
                || robotType.equals(UnitType.LEVEL_TWO_PAINT_TOWER)
                || robotType.equals(UnitType.LEVEL_THREE_PAINT_TOWER);
    }

    public static boolean isMoneyTower(UnitType robotType) {
        return robotType.equals(UnitType.LEVEL_ONE_MONEY_TOWER)
                || robotType.equals(UnitType.LEVEL_TWO_MONEY_TOWER)
                || robotType.equals(UnitType.LEVEL_THREE_MONEY_TOWER);
    }

    public static boolean isFriendlyPaint(PaintType type) {
        return type.equals(PaintType.ALLY_PRIMARY)
                || type.equals(PaintType.ALLY_SECONDARY);
    }
}
