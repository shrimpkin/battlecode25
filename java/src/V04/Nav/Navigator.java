package V04.Nav;

import V04.Globals;
import battlecode.common.*;

public class Navigator extends Globals {
    public static void moveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if (!rc.isMovementReady())
            return; // move cooldown
        if (myLocation.equals(target))
            return; // already there

        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        Direction bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
        if (bellmanFordDirection != null) {
            if (rc.canMove(bellmanFordDirection)) {
                rc.move(bellmanFordDirection);
            }
        }
    }
}
