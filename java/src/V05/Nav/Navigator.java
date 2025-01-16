package V05.Nav;

import V05.Globals;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


public class Navigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static int roundsSinceMovingCloserToTarget;

    public static void moveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (myLocation.equals(target)) {
            return;
        }

        if (currentTarget == null || !currentTarget.equals(target)) {
            reset();
        }

        currentTarget = target;

        int distanceToTarget = myLocation.distanceSquaredTo(target);
        if (distanceToTarget < minDistanceToTarget) {
            minDistanceToTarget = distanceToTarget;
            roundsSinceMovingCloserToTarget = 0;
        } else {
            roundsSinceMovingCloserToTarget++;
        }

        if (roundsSinceMovingCloserToTarget < 3) {
            Direction bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
            if (bellmanFordDirection != null) {
                
                if (rc.canMove(bellmanFordDirection)) {
                    System.out.println("Attempting Bellman Ford Direction.");
                    rc.move(bellmanFordDirection);
                }

                return;
            } 
        } 

        if (!rc.isMovementReady()) {
            return;
        }

        System.out.println("Attempting Bugnav Move.");
        BugNavigator.move(target);
    }

    public static void reset() {
        currentTarget = null;

        minDistanceToTarget = Integer.MAX_VALUE;
        roundsSinceMovingCloserToTarget = 0;
    }
}