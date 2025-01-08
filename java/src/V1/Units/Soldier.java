package V1.Units;

import java.util.Random;
import V1.*;
import battlecode.common.*;
import battlecode.schema.GameFooter;

public class Soldier extends Globals {
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

                MapInfo cur_info = rc.senseMapInfo(rc.getLocation());
                
                paint_tower();
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            
        } else {
            indicator += "looking for target";

            //TODO: replace with exploration algorithm
            int random_direction = rng.nextInt(8);
            if(rc.canMove(Utils.directions[random_direction])) {
                rc.move(Utils.directions[random_direction]);
            }
        }


        rc.setIndicatorString(indicator);
    }

    //this robot will be used to repeated rebuild a paint tower
    public static void paint_tower() throws GameActionException{
        if(ruin_location != null && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruin_location)) {
            rc.markTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruin_location);
        }

        MapInfo[] locations = rc.senseNearbyMapInfos();
        for(MapInfo info : locations) {
            paint_mark(info.getMapLocation());
        }
    }

    /**
     * Checks whether a tile has a specific pattern painted around it
     * @param location
     * @return
     */
    public static boolean has_tower_marked(MapLocation location) {
        


        return true;
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
