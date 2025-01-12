package V04;

import V04.Nav.*;
import battlecode.common.*;

public class Unit extends Globals {
    public enum Modes {RUSH, BOOM, SIT, NONE, REFILL, ATTACK};
    public static Modes mode = Modes.NONE;

    public static MapLocation wanderTarget; // TODO: emily

    public static FastIntSet paintTowerLocations = new FastIntSet();
    public static FastIntSet enemyTowerLocations = new FastIntSet();
    public static FastIntSet unusedRuinLocations = new FastIntSet();

    private static LocMap vis = new LocMap(mapWidth, mapHeight);

    public static String indicator;

    /** Look nearby for a ruin */
    public static MapLocation findRuin() throws GameActionException {
        MapLocation[] tiles = rc.senseNearbyRuins(-1);
        for (MapLocation tile : tiles) {
            if (rc.senseRobotAtLocation(tile) == null)
                return tile;
        }
        return null;
    }

    /** Unit picks a random location and moves towards it */
    public static void wander() throws GameActionException {
        if (!rc.isMovementReady())
            return; // cannot move yet

        // 'refresh' the visited nodes every few hundred rounds to account for map
        // changes over time
        if (rc.getRoundNum() % 200 == 0)
            // TODO: might be useless
            vis.clearAll();

        // pick a new place to go if we don't have one
        if (wanderTarget != null && rc.canSenseLocation(wanderTarget))
            wanderTarget = null;
        if (wanderTarget == null)
            wanderTarget = getExploreTarget();
        rc.setIndicatorDot(wanderTarget, 255, 0, 255);
        Navigator.moveTo(wanderTarget);

        // update the visited array every few rounds
        if (rc.getRoundNum() % 4 == 0)
            updateSeen();
    }

    /** Maintains correctness of tower sets */
    public static void updateTowerLocations() throws GameActionException {
        MapLocation[] ruinLocations = rc.senseNearbyRuins(-1);

        for (MapLocation ruin : ruinLocations) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);
            int packedLocation = pack(ruin);

            if (robot == null) {
                unusedRuinLocations.add(packedLocation);
                enemyTowerLocations.remove(packedLocation);
                paintTowerLocations.remove(packedLocation);
                continue;
            }

            UnitType type = robot.getType();
            if (robot.getTeam().equals(opponentTeam)) {
                if (isPaintTower(type) || isMoneyTower(type)) {
                    enemyTowerLocations.add(packedLocation);
                }
            } else {
                if (isPaintTower(type)) {
                    paintTowerLocations.add(packedLocation);
                }
            }
        }
    }

    /** Gets closest location to robot in provided location set */
    public static MapLocation getClosestLocation(FastIntSet locType) throws GameActionException {
        MapLocation closest = null;
        MapLocation loc = rc.getLocation();
        int best = Integer.MAX_VALUE;

        for (int i = 0; i < locType.size; i++) {
            MapLocation tower = unpack(locType.keys.charAt(i));
            rc.setIndicatorDot(tower, 0, 255, 0);
            int dist = tower.distanceSquaredTo(loc);
            if (dist < best) {
                best = dist;
                closest = tower;
            }
        }
        return closest;
    }

    /** Moves unit to last recorded paint tower and requests paint */
    public static void refill(int amount) throws GameActionException {
        Navigator.moveTo(getClosestLocation(paintTowerLocations));
        requestPaint(amount);
    }

    /** Tries to take paint from last recorded paint tower */
    public static void requestPaint(int amount) throws GameActionException {
        MapLocation closestPaintTower = getClosestLocation(paintTowerLocations);
        if (closestPaintTower == null)
            return; // no paint tower to go to
        if (!rc.canSenseLocation(closestPaintTower))
            return; // cannot sense paint tower

        int amtPaintInTower = rc.senseRobotAtLocation(closestPaintTower).getPaintAmount();
        int amtToTransfer = Math.min(amtPaintInTower, amount);

        if (rc.canTransferPaint(closestPaintTower, -amtToTransfer)) {
            rc.transferPaint(closestPaintTower, -amtToTransfer);
        }
    }

    ////////////////////////////////////////////////

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
                    clamp((int) (current.y + Math.sin(angle) * dist), 0, mapHeight - 1));
            if (vis.available(ret))
                return ret;
        }
        return ret;
    }
}
