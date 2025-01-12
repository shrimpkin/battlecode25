package V01;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

public class Navigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static int roundsSinceMovingCloserToTarget;

    public static void moveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        // draw line to show where its going
        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (myLocation.equals(target)) {
            return;
        }

        if (currentTarget == null || !currentTarget.equals(target)) {
            reset();
        }

        currentTarget = target;

        //causes it to not move idk
        // MapLocation nextLocation = myLocation.add(myLocation.directionTo(target));
        // if (rc.canSenseLocation(nextLocation)) {
        //     return;
        // }

        int distanceToTarget = myLocation.distanceSquaredTo(target);
        if (distanceToTarget < minDistanceToTarget) {
            minDistanceToTarget = distanceToTarget;
            roundsSinceMovingCloserToTarget = 0;
        } else {
            roundsSinceMovingCloserToTarget++;
        }

        Direction bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
        if (bellmanFordDirection != null) {
            if (rc.canMove(bellmanFordDirection)) {


                rc.move(bellmanFordDirection);
            }

            return;
        }
        

        if (!rc.isMovementReady()) {
            return;
        }
    }

    public static void reset() {
        currentTarget = null;
        minDistanceToTarget = Integer.MAX_VALUE;
        roundsSinceMovingCloserToTarget = 0;
    }
}
