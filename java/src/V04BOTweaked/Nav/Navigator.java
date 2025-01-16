package V04BOTweaked.Nav;

import V04BOTweaked.Globals;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Navigator extends Globals {
    public static void moveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if (!rc.isMovementReady())
            return; // move cooldown
        if (myLocation.equals(target))
            return; // already there

        if(rc.onTheMap(target)) rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);
        BugNavigator.move(target);
//        Direction bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
//        if (bellmanFordDirection != null) {
//            if (rc.canMove(bellmanFordDirection)) {
//                rc.move(bellmanFordDirection);
//            }
//        }
    }
}
