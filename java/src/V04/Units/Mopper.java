package V04.Units;

import V04.*;
import V04.Nav.*;
import battlecode.common.*;

public class Mopper extends Unit {
    static MapLocation nearbyEnemyPaint = null;

    public static void run() throws GameActionException {
        indicator = "";
        nearbyEnemyPaint = null;
        removeEnemyPaint();

        if (nearbyEnemyPaint != null) {
            Navigator.moveTo(nearbyEnemyPaint);

            // THE NAV DOESN'T GET CLOSE ENOUGH!!!!!!!!!!!!!!
            if (rc.canMove(rc.getLocation().directionTo(nearbyEnemyPaint))) {
                rc.move(rc.getLocation().directionTo(nearbyEnemyPaint));
            }
        } else {
            wander();
        }

        rc.setIndicatorString(rc.getRoundNum() + ": " + indicator);
    }

    public static void removeEnemyPaint() throws GameActionException {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            if (loc.getPaint().isEnemy()) {
                indicator += loc.getMapLocation().toString() + ", ";

                if (rc.canAttack(loc.getMapLocation())) {
                    rc.attack(loc.getMapLocation());
                    indicator += "attacked";
                } else {
                    nearbyEnemyPaint = loc.getMapLocation();
                }
            }
        }
    }
}
