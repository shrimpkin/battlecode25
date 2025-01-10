package V1map;

import V1map.Units.LocMap;
import battlecode.common.*;

public class Unit extends Globals {
    // temporary variable representing how many rounds the unit should wander for
    public static final int SETUP_ROUNDS = 100;
    //TODO: This should be an array of all known paint towers
    //Should find the closest one for refuel
    public static MapLocation paint_tower = null;
    public static String indicator;
    private static MapLocation wanderTarget;
    private static MapLocation spawnLocation;
    private static LocMap vis = new LocMap(mapWidth, mapHeight);

    public static void run() throws GameActionException {
        spawnLocation = rc.getLocation();
        wander();
    }

    /**
     * Returns the location of a ruin in the unit's sensing range if it exists,
     * otherwise returns null
     */
    public static MapLocation findRuin() throws GameActionException {
        MapInfo[] tiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : tiles) {
            if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null) {
                return tile.getMapLocation();
            }
        }
        return null;
    }

    private static void updateSeen() {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            vis.mark(loc.getMapLocation());
        }
    }


    private static MapLocation getExploreTarget() {
        MapLocation ret = null;
        MapLocation current = rc.getLocation();
        for (int i = 0; i < 5; i++) {
            var dist = 4 * nextDouble() + 6;
            var angle = 2 * Math.PI * nextDouble();
            if (dist > 10 || dist < 0) {
                System.out.println(dist);
                rc.resign();
            }
            ret = new MapLocation(
                    Utils.clamp((int) (current.x + Math.cos(angle) * dist), 0, mapWidth - 1),
                    Utils.clamp((int) (current.y + Math.sin(angle) * dist), 0, mapHeight - 1)
            );
            if (vis.available(ret)) return ret;
        }
        return ret;
    }

    // e.g. when it needs to do smth else like acquire paint
    public static void resetWanderTarget() {
        wanderTarget = null;
    }

    /**
     * Unit picks a random location and moves towards it
     */
    public static void wander() throws GameActionException {
        if (!rc.isMovementReady()) {
            return;
        }
        // 'refresh' the visited nodes every few hundred rounds to account for map changes over time
        if (rc.getRoundNum() % 200 == 0) vis.clearAll();
        // pick a new place to go if we don't have one
        if (wanderTarget != null && rc.canSenseLocation(wanderTarget)) wanderTarget = null;
        if (wanderTarget == null) {
            wanderTarget = getExploreTarget();
            rc.setIndicatorDot(wanderTarget, 255, 0, 255);
        } else {
            rc.setIndicatorString("existing wander " + wanderTarget);
            rc.setIndicatorDot(wanderTarget, 0, 0, 128);
        }
        Navigator.moveTo(wanderTarget);
        if (rc.getRoundNum() % 4 == 0) updateSeen();
    }

    /**
     * Checks whether a tile has a specific pattern painted around it
     *
     * @return the UnitType of the tower if it does and null otherwise
     */
    public static UnitType has_tower_marked(MapLocation location) throws GameActionException {
        MapInfo[] locations = rc.senseNearbyMapInfos(location, 8);
        if (locations.length != 25) {
            return null;
        }

        //contains true if the paint type is secondary
        //false if the paint type is primary
        boolean[][] pattern = new boolean[5][5];

        for (MapInfo info : locations) {
            int x = (location.x - info.getMapLocation().x) + 2;
            int y = (location.y - info.getMapLocation().y) + 2;

            switch (info.getMark()) {
                case PaintType.ALLY_PRIMARY:
                    pattern[x][y] = false;
                    break;
                case PaintType.ALLY_SECONDARY:
                    pattern[x][y] = true;
                    break;
                default:
                    if (info.getMapLocation().equals(location)) {
                        //the center of the tower need not be marked
                        continue;
                    } else {
                        //at least one tile is not marked
                        return null;
                    }
            }
        }

        if (does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER))) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
        if (does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER))) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        }
        if (does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_THREE_MONEY_TOWER))) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }

        return null;
    }

    /**
     * Checks to see if a 5 by 5 resource pattern is the same as another
     * Ignores the center square since that doesn't matter for towers
     */
    private static boolean does_tower_pattern_match(boolean[][] pattern1, boolean[][] pattern2) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;
                if (pattern1[i][j] != pattern2[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Updates location of last seen paint tower
     */
    public static void update_paint_tower_loc() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
        for (RobotInfo robot : robots) {
            if (robot.type.equals(UnitType.LEVEL_ONE_PAINT_TOWER)
                    || robot.type.equals(UnitType.LEVEL_TWO_PAINT_TOWER)
                    || robot.type.equals(UnitType.LEVEL_THREE_PAINT_TOWER)) {
                paint_tower = robot.getLocation();
            }
        }
    }

    /**
     * Will attempt to grab paint from paint towers
     * Only will grab if it has 100 or less paint
     */
    public static void acquire_paint() throws GameActionException {
        if (paint_tower == null) return;
        if (rc.getPaint() > 100) return;

        indicator += "trying to transfer paint at " + paint_tower.toString() + ", ";
        rc.setIndicatorDot(paint_tower, 0, 255, 0);

        int paint_in_tower = 0;
        if (rc.canSenseLocation(paint_tower)) {
            paint_in_tower = rc.senseRobotAtLocation(paint_tower).getPaintAmount();
        }

        Direction dir = rc.getLocation().directionTo(paint_tower);
        if (rc.canMove(dir)) rc.move(dir);

        int amount_to_transfer = Math.max(rc.getPaint() - 200, -paint_in_tower);

        indicator += amount_to_transfer + ", ";
        indicator += rc.canTransferPaint(paint_tower, -10);

        if (rc.canTransferPaint(paint_tower, amount_to_transfer)) {
            indicator += "can, ";
            rc.transferPaint(paint_tower, amount_to_transfer);
        }
    }
}
