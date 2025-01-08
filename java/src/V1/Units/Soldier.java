package V1.Units;

import java.util.Random;
import V1.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation ruin_location = null;
    static final Random rng = new Random(6147);
    static String indicator;

    public static void run() throws GameActionException{
        indicator = "";
        find_ruin(rc);
        
        if(ruin_location != null) {
            indicator += "has target" + "(" + ruin_location.x + "," + ruin_location.y + ")";
            rc.setIndicatorString(indicator);

            try{
                Direction d = BellmanFordNavigator.getBestDirection(ruin_location);
                indicator += "\nBellman done";
                
                if(rc.canMove(d)) {
                    rc.move(d);
                }
                
                paint_tower();

            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            indicator += "looking for target";
            wander();
        }

        rc.setIndicatorString(indicator);
    }

    //this robot will be used to repeated rebuild a paint tower
    public static void paint_tower() throws GameActionException{
        UnitType tower = has_tower_marked(ruin_location);
        
        if(ruin_location == null) return;

        if( (tower == null || !tower.equals(UnitType.LEVEL_ONE_PAINT_TOWER))
            && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location)) {

            rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location);
        }

        MapInfo[] locations = rc.senseNearbyMapInfos();
        for(MapInfo info : locations) {
            paint_mark(info.getMapLocation());
        }
    }

    /**
     * Paints the given color at the given location
     */
    public static boolean paint(MapLocation location, PaintType type) throws GameActionException {
        if(!rc.canSenseLocation(location)) return false;
        if(!rc.canAttack(location)) return false;

        MapInfo info = rc.senseMapInfo(location);
        if(info.getPaint() == type) {
             return true;
        } 

        if(type.equals(PaintType.ALLY_PRIMARY)) {
            rc.attack(location, false);
            return true;
        } else {
            rc.attack(location, true);
            return true;
        }
    }

    /**
     * Paints whatever is marked at that location
     */
    public static boolean paint_mark(MapLocation location) throws GameActionException {
        if(!rc.canSenseLocation(location)) return false;
        if(!rc.canAttack(location)) return false;
        
        MapInfo info = rc.senseMapInfo(location);
        if(info.getMark() == PaintType.EMPTY) return true; //vacuously painted
        else return paint(location, info.getMark());

    }

    public static void find_ruin(RobotController rc) {
        if(ruin_location != null) return;

        MapInfo[] info = rc.senseNearbyMapInfos();
        for(MapInfo tile : info) {
            if(tile.hasRuin()) ruin_location = tile.getMapLocation();
        }
    }
}
