package V01map;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Utils {
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
}
