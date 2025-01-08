package V1;

import battlecode.common.*;

public class Unit extends Globals {
    // temporary variable representing how many rounds the unit should wander for
    public static final int SETUP_ROUNDS = 100;

    
    private static MapLocation wanderTarget;
    private static MapLocation spawnLocation;

    public static void run(RobotController rc) throws GameActionException {
        spawnLocation = rc.getLocation();
        wander(rc);
    }

    /**
     * Unit picks a random location and moves towards it
     */
    public static void wander(RobotController rc) throws GameActionException {
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
}
