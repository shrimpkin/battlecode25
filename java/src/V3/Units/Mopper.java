package V3.Units;


import battlecode.common.*;
import V3.*;

public class Mopper extends Unit {
    static MapLocation nearby_paint = null;

    public static void run() throws GameActionException{
        indicator = "";
        nearby_paint = null;
        remove_enemy_paint();

        if(nearby_paint != null) {
            Navigator.moveTo(nearby_paint, false);
            if(rc.canMove(rc.getLocation().directionTo(nearby_paint))) {
                rc.move(rc.getLocation().directionTo(nearby_paint));
            }
        } else {
            wander(true);
        }

        rc.setIndicatorString(rc.getRoundNum() + ": " + indicator);
    }

    public static void remove_enemy_paint() throws GameActionException{
        MapInfo[] locations = rc.senseNearbyMapInfos();

        for(MapInfo loc : locations) {
            if(loc.getPaint() == PaintType.ENEMY_PRIMARY
                || loc.getPaint() == PaintType.ENEMY_SECONDARY) {
                    indicator += loc.getMapLocation().toString() + ", ";

                    if(rc.canAttack(loc.getMapLocation())) {
                        rc.attack(loc.getMapLocation());
                        indicator += "attacked";
                    }

                    if(loc.getPaint() == PaintType.ENEMY_PRIMARY
                    || loc.getPaint() == PaintType.ENEMY_SECONDARY) {
                        nearby_paint = loc.getMapLocation();
                    }
            }
        }
    }
}
