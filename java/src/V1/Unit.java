package V1;

import battlecode.common.*;

public class Unit extends Globals {
    // temporary variable representing how many rounds the unit should wander for
    public static final int SETUP_ROUNDS = 100;

    
    private static MapLocation wanderTarget;
    private static MapLocation spawnLocation;

    public static void run() throws GameActionException {
        spawnLocation = rc.getLocation();
        wander();
    }

    /**
     * Unit picks a random location and moves towards it
     */
    public static void wander() throws GameActionException {
        if (!rc.isMovementReady()) {
            return;
        }

        // pick a new place to go if we don't have one
        while (wanderTarget == null
            || rc.canSenseLocation(wanderTarget)
            || (rc.getRoundNum() < SETUP_ROUNDS && spawnLocation.distanceSquaredTo(wanderTarget) > maxDistance)) {
            wanderTarget = new MapLocation(nextInt(mapWidth), nextInt(mapHeight));
        }

        // attempt to move there TODO should be refactored
        try {
            Direction d = BellmanFordNavigator.getBestDirection(wanderTarget);
            if (rc.canMove(d)) {
                rc.move(d);
            }
        } catch (Exception e) {
            System.out.println("ERROR in Unit.wander");
            e.printStackTrace();
        }
    }

    /**
     * Picks random coordinate given the upper bound
     */
    public static int nextInt(int maxExclusive) {
        return (int) Math.floor(Math.random() * maxExclusive);
    }

    /**
     * Checks whether a tile has a specific pattern painted around it
     * @return the UnitType of the tower if it does and null otherwise
     */
    public static UnitType has_tower_marked(MapLocation location) throws GameActionException{
        System.out.println("In has_tower_marked.");
        MapInfo[] locations = rc.senseNearbyMapInfos(location, 8);

        for(MapInfo info : locations) {
            rc.setIndicatorDot(info.getMapLocation(), 0, 0, 0);
        }

        if(locations.length != 25) {
            System.out.println("Can't see all locations.");
            return null;
        }

        boolean[][] pattern = new boolean[5][5];

        for(MapInfo info : locations) {
            int x = (location.x - info.getMapLocation().x) + 2;
            int y = (location.y - info.getMapLocation().y) + 2;

            switch(info.getMark()) {
                case PaintType.ALLY_PRIMARY : 
                    pattern[x][y] = false;
                    break;
                case PaintType.ALLY_SECONDARY : 
                    pattern[x][y] = true;
                    break;
                default: 
                    if(info.getMapLocation().equals(location)) {
                        continue;
                    } else {
                        System.out.println("At least one tile is not marked."); 
                        return null;
                    }
                    
            }
        }

        if(does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER))){
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
        if(does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER))){
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        }
        if(does_tower_pattern_match(pattern, rc.getTowerPattern(UnitType.LEVEL_THREE_MONEY_TOWER))){
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }

        return null;
    }

    /**
     * Checks to see if a 5 by 5 resource pattern is the same as another
     * Ignores the center square since that doesn't matter for towers
     */
    private static boolean does_tower_pattern_match(boolean[][] pattern1, boolean[][] pattern2) {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(i == 2 && j == 2) continue;
                if(pattern1[i][j] != pattern2[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }
}
