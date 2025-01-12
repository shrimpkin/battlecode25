package V3Boom.Nav;

import V3Boom.Globals;
import battlecode.common.*;


public class Navigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static int roundsSinceMovingCloserToTarget;

    public static void moveTo(MapLocation target, boolean cheap) throws GameActionException {
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

        Direction bellmanFordDirection;
        if(cheap) {
            bellmanFordDirection = BellmanFordNavigatorCheap.getBestDirection(target);
        } else {
            bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
        }
        
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
