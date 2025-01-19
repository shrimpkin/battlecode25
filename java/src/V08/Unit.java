package V08;

import V08.Nav.Navigator;
import V08.Tools.FastIntSet;
import V08.Tools.LocMap;
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
        // rc.setIndicatorDot(wanderTarget, 255, 0, 255);
        Navigator.moveTo(wanderTarget);

        if ((rc.getRoundNum()- lastSeenUpdateTime) >= 5 && Clock.getBytecodesLeft() > 8000) {
            updateSeen();
            lastSeenUpdateTime = rc.getRoundNum();
        }
    }

    /**
     * Overloaded version: set paintless to true if you want to avoid stepping off paint
     */
    public static void wander(boolean wasWandering, boolean paintless) throws GameActionException {
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
        // rc.setIndicatorDot(wanderTarget, 255, 0, 255);
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

    /** Tries to take paint from last recorded paint tower */
    public static boolean requestPaint(MapLocation tower, int amount) throws GameActionException {
        if (tower == null)
            return false; // no paint tower to go to
        if (!rc.canSenseLocation(tower))
            return false; // cannot sense paint tower
        if(rc.senseRobotAtLocation(tower) == null) 
            return false; // no longer a paint tower there

        int amtPaintInTower = rc.senseRobotAtLocation(tower).getPaintAmount();
        int amtToTransfer = Math.min(amtPaintInTower, amount);

        rc.setIndicatorString("transfering paint");
        if (rc.canTransferPaint(tower, -amtToTransfer)) {
            rc.transferPaint(tower, -amtToTransfer);
            return true;
        }
        return false;
    }

    /** Checks nearby allies and paints and moves away from them to mitigate crowd penalty */
    // TODO: this eats a lot of bytecode i think...
    // I think we are under 1500 now, this completely depends on robot density though 
    public static void recenter() throws GameActionException {
        MapInfo[] adjacentLocations = rc.senseNearbyMapInfos(rc.getLocation(), 2);
        RobotInfo[] robots = rc.senseNearbyRobots(8);
        MapLocation currLoc = rc.getLocation();
        int[] weights = {0, 0, 0, 0, 0, 0, 0, 0, 0};

        // weight neutral and enemy paints
        for (int i = 0; i < adjacentLocations.length; i++) {
            MapInfo info = adjacentLocations[i];
            MapLocation loc = info.getMapLocation();
            Direction dir = currLoc.directionTo(loc);

            if (!rc.canMove(dir)) {
                weights[i] = 2000;
            } else if (info.getPaint().isEnemy()) {
                weights[i] += GameConstants.PENALTY_ENEMY_TERRITORY;
            } else if (info.getPaint() == PaintType.EMPTY) {
                weights[i] += GameConstants.PENALTY_NEUTRAL_TERRITORY;
            }     
        }

        // weight robots
        for(RobotInfo robot : robots) {
            for (int i = 0; i < adjacentLocations.length; i++) {
                MapInfo info = adjacentLocations[i];
                if (info.getMapLocation().distanceSquaredTo(robot.getLocation()) <= 2) {
                    weights[i] += 1 * (rc.getTeam() == myTeam ? 1 : 2); //I don't know where the adjacent robot penalty is 
                }
            }
        }

        boolean hasMeaningfulWeights = false;
        for (int weight : weights) {
            if (weight != 0 && weight < 2000) {
                hasMeaningfulWeights = true;
                break;
            }
        }

        if (!hasMeaningfulWeights)
            return; // no enemies nearby, all ally paint, no need to recenter

        boolean should_print = false;
        String weight_info = "Weights around: " + rc.getLocation().toString();
        for (int i = 0; i < adjacentLocations.length; i++) {
            weight_info += "\n" + adjacentLocations[i].getMapLocation().toString() + "--" + weights[i];
        }
        if(should_print) System.out.println(weight_info);

        int minWeight = Integer.MAX_VALUE;
        int minWeightIdx = -1;

        for (int i = 0; i < adjacentLocations.length; i++) {
            if (weights[i] <= minWeight) {
                minWeight = weights[i];
                minWeightIdx = i;
            }
        }

        if (minWeightIdx > -1 && minWeightIdx < adjacentLocations.length) {
            Direction dir = currLoc.directionTo(adjacentLocations[minWeightIdx].getMapLocation());
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }
    

    /// /////////////////////////////////////////////

    // update seen locations in visited set
    public static void updateSeen() throws GameActionException {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            vis.mark(loc.getMapLocation());
        }
    }

    // get an exploration target
    public static MapLocation getExploreTarget() {
        MapLocation ret = null;
        for (int i = 10; i-- > 0; ) {
            ret = new MapLocation(
                    (int) (nextDouble() * rc.getMapWidth()), (int) (nextDouble() * rc.getMapHeight())
            );
            if (!rc.canSenseLocation(ret) && vis.get(ret) == 0) return ret;
        }
        return ret;
    }

    public static MapLocation getExploreTargetClose() {
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

    protected static int[][] pattern = {
            {1,1,0,1,1},
            {1,0,0,0,1},
            {0,0,1,0,0},
            {1,0,0,0,1},
            {1,1,0,1,1}
    };

    public static void canCompletePattern() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            MapLocation loc = tile.getMapLocation();
            if(loc.x % 4 != 2 || loc.y % 4 != 2) 
                continue; // not a center location
            if (rc.canCompleteResourcePattern(loc)) {
                rc.completeResourcePattern(loc);
                return; // complete 1st available pattern and return
            }
        }
    }

    public enum Modes {RUSH, BOOM, SIT, NONE, REFILL, ATTACK, DEFEND}
}
