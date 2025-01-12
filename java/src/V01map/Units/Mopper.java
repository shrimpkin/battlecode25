package V01map.Units;


import V01map.Navigator;
import V01map.Unit;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;

public class Mopper extends Unit {
    static MapLocation nearby_paint = null;

    public static void run() throws GameActionException{
        indicator = "";
        nearby_paint = null;
        remove_enemy_paint();

        if(nearby_paint != null) {
            Navigator.moveTo(nearby_paint);
            if(rc.canMove(rc.getLocation().directionTo(nearby_paint))) {
                rc.move(rc.getLocation().directionTo(nearby_paint));
            }
        } else {
            wander();
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
