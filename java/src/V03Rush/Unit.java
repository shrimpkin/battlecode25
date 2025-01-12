package V03Rush;

import V03Rush.Nav.Navigator;
import battlecode.common.*;

public class Unit extends Globals {
    // temporary variable representing how many rounds the unit should wander for
    public static final int SETUP_ROUNDS = 100;
    
    public static FastIntSet paintTowerLocations = new FastIntSet();
    public static FastIntSet enemyTowerLocations = new FastIntSet();
    public static FastIntSet unusedRuinLocations = new FastIntSet();

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
                    clamp((int) (current.x + Math.cos(angle) * dist), 0, mapWidth - 1),
                    clamp((int) (current.y + Math.sin(angle) * dist), 0, mapHeight - 1)
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
     * Maintains correctness of tower sets
     * Specifically: paintTowerLocations, unusedRuinLocations, enemyTowerLocations
     */
    public static void updateTowerLocations() throws GameActionException {
        MapLocation[] ruinLocations = rc.senseNearbyRuins(-1);

        for(MapLocation ruin : ruinLocations) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);
            int packedLocation = pack(ruin);
            if(robot == null) {
                unusedRuinLocations.add(packedLocation);

                if(enemyTowerLocations.contains(packedLocation)) {
                    enemyTowerLocations.remove(packedLocation);
                }

                if(paintTowerLocations.contains(packedLocation)) {
                    paintTowerLocations.remove(packedLocation);
                }

                continue;
            }

            UnitType type = robot.getType();
            if(robot.getTeam().equals(opponentTeam)) {
                if(isPaintTower(type) || isMoneyTower(type)) {
                    enemyTowerLocations.add(pack(ruin));
                }
                continue;
            }

            if(isPaintTower(type)) {
                paintTowerLocations.add(pack(ruin));
            }            
        }
    }

    public static MapLocation closestPaintTower() throws GameActionException {
        MapLocation closest = null;
        int best = Integer.MAX_VALUE;
        //int bnum = Clock.getBytecodeNum();
        var loc = rc.getLocation();
        for (int i = 0; i < paintTowerLocations.size; i++) {
            MapLocation tower = unpack(paintTowerLocations.keys.charAt(i));
            rc.setIndicatorDot(tower, 0, 255, 0);
            var dist = tower.distanceSquaredTo(loc);
            if (dist < best) {
                best = dist;
                closest = tower;
            }
        }
        //int bnum2 = Clock.getBytecodeNum();
        //System.out.printf("with set size: %d, getting closest took %d bytecode instructions\n", paintTowerLocations.size, bnum2 - bnum);
        return closest;
    }

    public static MapLocation closestUnusedRuin() throws GameActionException {
        MapLocation closest = null;
        int best = Integer.MAX_VALUE;
        //int bnum = Clock.getBytecodeNum();
        var loc = rc.getLocation();
        for (int i = 0; i < unusedRuinLocations.size; i++) {
            MapLocation tower = unpack(unusedRuinLocations.keys.charAt(i));
            rc.setIndicatorDot(tower, 0, 255, 0);
            var dist = tower.distanceSquaredTo(loc);
            if (dist < best) {
                best = dist;
                closest = tower;
            }
        }
        //int bnum2 = Clock.getBytecodeNum();
        //System.out.printf("with set size: %d, getting closest took %d bytecode instructions\n", unusedRuinLocations.size, bnum2 - bnum);
        return closest;
    }

    
}
