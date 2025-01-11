package V3;

import battlecode.common.*;

public class Unit extends Globals {
    // temporary variable representing how many rounds the unit should wander for
    public static final int SETUP_ROUNDS = 100;
    
    public static FastIntSet paintTowerLocations = new FastIntSet();
    public static FastIntSet enemyTowerLocations = new FastIntSet();
    
    public static String indicator;
    public static MapLocation wanderTarget;
    private static LocMap vis = new LocMap(mapWidth, mapHeight);

    /**
     * Returns the location of a ruin in the unit's sensing range if it exists,
     * otherwise returns null
     */
    public static MapLocation findRuin() throws GameActionException {
        MapLocation[] tiles = rc.senseNearbyRuins(-1);
        for (MapLocation tile : tiles) {
            if (rc.senseRobotAtLocation(tile) == null) return tile;
        }
        return null;
    }

    // update seen locations in visited set
    private static void updateSeen() {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            vis.mark(loc.getMapLocation());
        }
    }

    // get an exploration target
    private static MapLocation getExploreTarget() {
        MapLocation ret = null;
        MapLocation current = rc.getLocation();
        for (int i = 0; i < 5; i++) {
            var dist = 4 * nextDouble() + 6;
            var angle = 2 * Math.PI * nextDouble();
            ret = new MapLocation(
                    Utils.clamp((int) (current.x + Math.cos(angle) * dist), 0, mapWidth - 1),
                    Utils.clamp((int) (current.y + Math.sin(angle) * dist), 0, mapHeight - 1)
            );
            if (vis.available(ret)) return ret;
        }
        return ret;
    }

    /**
     * Unit picks a random location and moves towards it
     */
    public static void wander(boolean cheap) throws GameActionException {
        if (!rc.isMovementReady()) return;
        // 'refresh' the visited nodes every few hundred rounds to account for map changes over time
        if (rc.getRoundNum() % 200 == 0) vis.clearAll();
        // pick a new place to go if we don't have one
        if (wanderTarget != null && rc.canSenseLocation(wanderTarget)) wanderTarget = null;
        if (wanderTarget == null) wanderTarget = getExploreTarget();
        rc.setIndicatorDot(wanderTarget, 255, 0, 255);
        Navigator.moveTo(wanderTarget, cheap);
        // update the visited array every few rounds
        if (rc.getRoundNum() % 4 == 0) updateSeen();
    }

    /**
     * Updates location of last seen paint tower
     */
    public static void updatePaintTowerLocations() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
        for (RobotInfo robot : robots) {
            switch (robot.type) {
                case UnitType.LEVEL_ONE_PAINT_TOWER:
                case UnitType.LEVEL_TWO_PAINT_TOWER:
                case UnitType.LEVEL_THREE_PAINT_TOWER:
                    paintTowerLocations.add(Utils.pack(robot.getLocation()));
                    break;
                default:
                    paintTowerLocations.remove(Utils.pack(robot.getLocation()));
                    break;
            }
        }
    }

    /**
     * Updates the enemyTowerLocations set by adding new towers that are seen
     * and removing towers that are no longer there
     */
    public static void updateEnemyTowerLocations() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);
        for(RobotInfo robot : robots) {
            if(isPaintTower(robot.getType()) || isMoneyTower(robot.getType())) {
                int packedLocation = Utils.pack(robot.getLocation());
                if(!enemyTowerLocations.contains(packedLocation)) {
                    enemyTowerLocations.add(packedLocation);
                }
            }
        }

        for(int i = 0; i < enemyTowerLocations.size; i++) {
            MapLocation tower = Utils.unpack(enemyTowerLocations.keys.charAt(i));
            
            if(!rc.canSenseLocation(tower)) continue;
            RobotInfo info = rc.senseRobotAtLocation(tower);

            //there is no unit or the unit is not a paint or money tower
            //hence the tower is not there and we should remove 
            if(info == null || !(isPaintTower(info.getType()) || isMoneyTower(info.getType()))) {
                enemyTowerLocations.remove(Utils.pack(tower));
            }
        }
    }

    public static MapLocation closestPaintTower() throws GameActionException {
        MapLocation closest = null;
        int best = Integer.MAX_VALUE;
        int bnum = Clock.getBytecodeNum();
        var loc = rc.getLocation();
        for (int i = 0; i < paintTowerLocations.size; i++) {
            MapLocation tower = Utils.unpack(paintTowerLocations.keys.charAt(i));
            rc.setIndicatorDot(tower, 0, 255, 0);
            var dist = tower.distanceSquaredTo(loc);
            if (dist < best) {
                best = dist;
                closest = tower;
            }
        }
        int bnum2 = Clock.getBytecodeNum();
        System.out.printf("with set size: %d, getting closest took %d bytecode instructions\n", paintTowerLocations.size, bnum2 - bnum);
        return closest;
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
}
