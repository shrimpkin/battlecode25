package V01map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Navigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static int roundsSinceMovingCloserToTarget;

    public static void moveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        // draw line to show where its going -- avoid an exception this way
        if (rc.onTheMap(target)) rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);
        if (myLocation.equals(target)) return;
        if (currentTarget == null || !currentTarget.equals(target)) reset();

        currentTarget = target;

        int distanceToTarget = myLocation.distanceSquaredTo(target);
        if (distanceToTarget < minDistanceToTarget) {
            minDistanceToTarget = distanceToTarget;
            roundsSinceMovingCloserToTarget = 0;
        } else {
            roundsSinceMovingCloserToTarget++;
        }

        Direction bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
        if (bellmanFordDirection != null && rc.canMove(bellmanFordDirection)) {
            rc.move(bellmanFordDirection);
        }
    }

    public static void reset() {
        currentTarget = null;
        minDistanceToTarget = Integer.MAX_VALUE;
        roundsSinceMovingCloserToTarget = 0;
    }
}
