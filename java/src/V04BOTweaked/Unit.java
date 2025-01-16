package V04BOTweaked;

import V04BOTweaked.Nav.Navigator;
import battlecode.common.*;

public class Unit extends Globals {
    public static Modes mode = Modes.NONE;
    public static MapLocation wanderTarget; // TODO: emily
    public static FastIntSet paintTowerLocations = new FastIntSet();
    public static FastIntSet enemyTowerLocations = new FastIntSet();
    public static FastIntSet unusedRuinLocations = new FastIntSet();
    public static String indicator;
    public static LocMap vis = new LocMap(rc);
    static int spawnRound = rc.getRoundNum();
    // round which the last wander target was chosen -- to check timeout
    private static int lastWanderTargetTime = rc.getRoundNum();
    // last time location mapping was updated -- for staggering every 4 rounds
    private static int lastSeenUpdateTime = rc.getRoundNum();

    /**  Look nearby for a ruin */
    public static MapLocation findRuin() throws GameActionException {
        MapLocation[] tiles = rc.senseNearbyRuins(-1);
        for (MapLocation tile : tiles) {
            if (rc.senseRobotAtLocation(tile) == null)
                return tile;
        }
        return null;
    }

    public static void wander(boolean wasWandering) throws GameActionException {
        if (!rc.isMovementReady())
            return; // cannot move yet

        // pick a new place to go if we don't have one -- or tried to go somewhere for too long
        if (!wasWandering
                || wanderTarget != null && wanderTarget.isWithinDistanceSquared(rc.getLocation(), 9)
                || (rc.getRoundNum() - lastWanderTargetTime > 20)
        ) {
            wanderTarget = null;
        }
        if (wanderTarget == null) {
            wanderTarget = (rc.getRoundNum() - spawnRound < 50) ? getExploreTargetClose() : getExploreTarget();
            lastWanderTargetTime = rc.getRoundNum();
        }
        //rc.setIndicatorDot(wanderTarget, 255, 0, 255);
        Navigator.moveTo(wanderTarget);


        if ((rc.getRoundNum()- lastSeenUpdateTime) >= 5 && Clock.getBytecodesLeft() > 8000) {
            updateSeen();
            lastSeenUpdateTime = rc.getRoundNum();
        }
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

    /**
     * Gets closest location to robot in provided location set
     */
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

    /// /////////////////////////////////////////////

    // update seen locations in visited set
    private static void updateSeen() throws GameActionException {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            vis.mark(loc.getMapLocation());
        }
    }

    // get an exploration target
    private static MapLocation getExploreTarget() {
        MapLocation ret = null;
        for (int i = 10; i-- > 0; ) {
            ret = new MapLocation(
                    (int) (nextDouble() * rc.getMapWidth()), (int) (nextDouble() * rc.getMapHeight())
            );
            if (!rc.canSenseLocation(ret) && vis.get(ret) == 0) return ret;
        }
        return ret;
    }

    private static MapLocation getExploreTargetClose() {
        MapLocation current = rc.getLocation();
        MapLocation ret = current;
        for (int i = 10; i-- > 0; ) {
            ret = current.translate(
                    (int) (24.0 * nextDouble() - 12),
                    (int) (24.0 * nextDouble() - 12)
            );
            if (!rc.onTheMap(ret)) continue;
            if (!rc.canSenseLocation(ret) && vis.get(ret) == 0) return ret;
        }
        return ret;
    }

    // piss poor impl of new resource patterns
    public static void canCompletePattern() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos(GameConstants.RESOURCE_PATTERN_RADIUS_SQUARED)) {
            MapLocation loc = tile.getMapLocation();
            if(loc.x % 4 != 2 || loc.y % 4 != 2) return;
            if (rc.canCompleteResourcePattern(loc)) {
                rc.completeResourcePattern(loc);
            }
        }
    }

    private static final int[][] pattern = {
            {1,1,0,1,1},
            {1,0,0,0,1},
            {0,0,1,0,0},
            {1,0,0,0,1},
            {1,1,0,1,1}
    };
    public static boolean shouldBeSecondary(MapLocation loc) {
        return pattern[loc.x % 4][loc.y % 4] == 1;
    }

    public enum Modes {RUSH, BOOM, SIT, NONE, REFILL, ATTACK}
}
