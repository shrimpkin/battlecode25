package V01resource.Units;

import V01resource.*;
import battlecode.common.*;

public class Mopper extends Unit {
    static MapLocation nearby_paint;

    public static void run() throws GameActionException {
        indicator = "";
        nearby_paint = null;
        removeNearbyEnemyPaint();

        if (nearby_paint != null) {
            Navigator.moveTo(nearby_paint);
            if (rc.canMove(rc.getLocation().directionTo(nearby_paint))) {
                rc.move(rc.getLocation().directionTo(nearby_paint));
            }
        } else {
            wander();
        }

        rc.setIndicatorString(rc.getRoundNum() + ": " + indicator);
    }

    public static void removeNearbyEnemyPaint() throws GameActionException {
        MapInfo[] locations = rc.senseNearbyMapInfos();

        for (MapInfo loc : locations) {
            PaintType paintType = loc.getPaint();
            if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
                indicator += loc.getMapLocation().toString() + ", ";
                nearby_paint = loc.getMapLocation();

                if (rc.canAttack(loc.getMapLocation())) {
                    rc.attack(loc.getMapLocation());
                    indicator += "attacked";
                }
            }
        }
    }
}
