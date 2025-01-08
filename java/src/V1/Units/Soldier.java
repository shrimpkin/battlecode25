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
        if(ruin_location != null && has_tower_marked(ruin_location) == -1 && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruin_location)) {
            rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location);
        }

        MapInfo[] locations = rc.senseNearbyMapInfos();
        for(MapInfo info : locations) {
            paint_mark(info.getMapLocation());
        }
    }

    /**
     * Checks whether a tile has a specific pattern painted around it
     * Returns 0 if the robot doesn't have enough information
     * Returns 1 if the pattern is marked
     * Returns -1 if the pattern is not marked
     */
    public static int has_tower_marked(MapLocation location) throws GameActionException{
        System.out.println("In has_tower_marked.");
        MapInfo[] locations = rc.senseNearbyMapInfos(location, 8);

        for(MapInfo info : locations) {
            rc.setIndicatorDot(info.getMapLocation(), 0, 0, 0);
        }

        if(locations.length != 25) {
            System.out.println("Can't see all locations.");
            return 0;
        }

        boolean[][] pattern = new boolean[5][5];

        for(MapInfo info : locations) {
            int x = (location.x - info.getMapLocation().x) + 2;
            int y = (location.y - info.getMapLocation().y) + 2;

            switch(info.getMark()) {
                case PaintType.ALLY_PRIMARY : 
                    pattern[x][y] = true;
                    break;
                case PaintType.ALLY_SECONDARY : 
                    pattern[x][y] = false;
                    break;
                default: 
                    if(info.getMapLocation().equals(location)) {
                        continue;
                    } else {
                        System.out.println("At least one tile is not marked."); 
                        return -1;
                    }
                    
            }
        }

        boolean[][] paint_pattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);

        String output = "\n";
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                output += pattern[i][j] + ", ";
            }
            output += "\n";
        }
        System.out.println(output);

        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(i == 2 && j == 2) continue;
                if(paint_pattern[i][j] == pattern[i][j]) {
                    System.out.println(i + ", " + j + "\n");
                    return -1;
                }
            }
        }
        System.out.println("Confirmed paint tower.");

        return 1;
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
