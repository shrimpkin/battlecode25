package V1_2;

import battlecode.common.*;

public class Unit extends Globals {
    public static MapLocation wanderTarget;

    // TODO: This should be an array of all known paint towers
    // Should find the closest one for refuel
    public static MapLocation paint_tower = null;
    public static String indicator;

    /** Look nearby for a ruin */
    public static MapLocation findRuin() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null) {
                return tile.getMapLocation();
            }
        }
        return null;
    }

    /** Marks last seen ruin location as specified tower type */
    public static void markRuinAs(MapLocation ruinLoc, UnitType newTowerType) throws GameActionException {
        if (ruinLoc == null)
            return; // no ruin to check
        if (!rc.canSenseLocation(ruinLoc))
            return; // ruin not within sensing range
        if (rc.canSenseRobotAtLocation(ruinLoc))
            return; // location already contains a tower 

        UnitType currTowerType = getTowerTypeFromPattern(ruinLoc);
        if (currTowerType == null || !currTowerType.equals(newTowerType)) {
            if (rc.canMarkTowerPattern(newTowerType, ruinLoc)) {
                rc.markTowerPattern(newTowerType, ruinLoc);
            }
        }
    }

    public static boolean completeRuinAs(MapLocation ruinLoc, UnitType newTowerType) throws GameActionException {
        if (ruinLoc == null) 
            return false;

        if (rc.canCompleteTowerPattern(newTowerType, ruinLoc)) {
            rc.completeTowerPattern(newTowerType, ruinLoc);
            return true;
        }

        return false;
    }

    /** Unit picks a random location and moves towards it */
    public static void wander() throws GameActionException {
        if (!rc.isMovementReady()) {
            return;
        }

        // pick a new place to go if we don't have one
        while (wanderTarget == null || rc.canSenseLocation(wanderTarget)) {
            wanderTarget = new MapLocation(nextInt(mapWidth), nextInt(mapHeight));
        }

        Navigator.moveTo(wanderTarget);
    }

    /** Moves unit to last recorded paint tower and requests paint */
    public static void refill(int amount) throws GameActionException {
        Navigator.moveTo(paint_tower);
        requestPaint(amount);
    }

    /** Tries to take paint from last recorded paint tower */
    public static void requestPaint(int amount) throws GameActionException {
        if (paint_tower == null) // no paint tower to go to
            return;
        if (!rc.canSenseLocation(paint_tower)) // cannot sense paint tower
            return;

        int amtPaintInTower = rc.senseRobotAtLocation(paint_tower).getPaintAmount();
        int amtToTransfer = Math.min(amtPaintInTower, amount);

        if (rc.canTransferPaint(paint_tower, -amtToTransfer)) {
            rc.transferPaint(paint_tower, -amtToTransfer);
        }
    }

    /** Returns the type of a tower based on the pattern around it */
    public static UnitType getTowerTypeFromPattern(MapLocation location) throws GameActionException {
        MapInfo[] locations = rc.senseNearbyMapInfos(location, 8);
        if (locations.length != 25)
            return null;

        // contains true if the paint type is secondary
        // false if the paint type is primary
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
                        // the center of the tower need not be marked
                        continue;
                    } else {
                        // at least one tile is not marked
                        return null;
                    }
            }
        }

        if (isSameTowerPattern(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER))) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
        if (isSameTowerPattern(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER))) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        }
        if (isSameTowerPattern(pattern, rc.getTowerPattern(UnitType.LEVEL_THREE_MONEY_TOWER))) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }

        return null;
    }

    /** Checks if two 5x5 patterns are the same, ignoring the center square */
    private static boolean isSameTowerPattern(boolean[][] p1, boolean[][] p2) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;
                if (p1[i][j] != p2[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Updates location of last seen paint tower */
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
        if (paint_tower == null)
            return;
        if (rc.getPaint() > 100)
            return;

        indicator += "trying to transfer paint at " + paint_tower.toString() + ", ";
        rc.setIndicatorDot(paint_tower, 0, 255, 0);

        int paint_in_tower = 0;
        if (rc.canSenseLocation(paint_tower)) {
            paint_in_tower = rc.senseRobotAtLocation(paint_tower).getPaintAmount();
        }

        Direction dir = rc.getLocation().directionTo(paint_tower);
        if (rc.canMove(dir))
            rc.move(dir);

        int amount_to_transfer = Math.max(rc.getPaint() - 200, -paint_in_tower);

        indicator += amount_to_transfer + ", ";
        indicator += rc.canTransferPaint(paint_tower, -10);

        if (rc.canTransferPaint(paint_tower, amount_to_transfer)) {
            indicator += "can, ";
            rc.transferPaint(paint_tower, amount_to_transfer);
        }
    }
}
