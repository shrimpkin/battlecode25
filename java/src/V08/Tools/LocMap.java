package V08.Tools;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class LocMap {
    private char[] marked;
    private final RobotController rc;

    private static final String repr = "\0".repeat(4096); // very cheap for some reason
    private static final int SEEN_BIT = 1, WALL_BIT = 2, RUIN_BIT = 4;

    // additional possible additions:
    // ENEMY_ROBOT_BIT = 8, ENEMY_TOWER_BIT = 16, ALLY_ROBOT_BIT = 32, ALLY_TOWER_BIT = 64

    /** New map initialized with height and width */
    public LocMap(RobotController rc) {
        marked = repr.toCharArray(); // also very cheap -- interesting
        this.rc = rc;
    }

    /** Checks if location is NOT marked -- assumes location is on the map*/
    public char get(MapLocation loc) {
        return marked[loc.x * rc.getMapHeight() + loc.y];
    }

    /** Marks location */
    public void mark(MapLocation loc) throws GameActionException {
        char v = SEEN_BIT;
        var mapinfo = rc.senseMapInfo(loc);
        if (mapinfo.hasRuin()) v |= RUIN_BIT;
        if (mapinfo.isWall()) v |= WALL_BIT;

        marked[loc.x * rc.getMapHeight() + loc.y] = v;
    }

    /** Removes all marks */
    public void clearAll() { marked = repr.toCharArray(); }

    public boolean isPassible(MapLocation loc) {
        // seen, but neither wall nor ruin
        return (marked[loc.x * rc.getMapHeight() + loc.y] & 0b111) == SEEN_BIT;
    }
}
