package V1_2.Units;

import V1_2.*;
import battlecode.common.*;

public class Soldier extends Unit {
    private static int refillThreshold = 50;
    private static MapLocation currPOI;

    private static MapLocation currRuinLoc;
    private static MapLocation currSRPLoc;
    // private static MapLocation target_location;

    /** Generic soldier action sequence */
    public static void run() throws GameActionException {
        // rc.setIndicatorString(rc.getRoundNum() + " ran Soldier.run");
        update_paint_tower_loc();

        initRuin();
        paintResourcePattern(); // these are not being painted immediately

        if (rc.getPaint() < refillThreshold) {
            refill(200);
        }

        if (currPOI != null) {
            Navigator.moveTo(currPOI);
        } else {
            wander();
        }
    }

    /** Initializes nearby ruins to new towers */
    public static void initRuin() throws GameActionException {
        if (currRuinLoc == null) {
            currRuinLoc = findRuin();
        }

        markRuinAs(currRuinLoc, UnitType.LEVEL_ONE_PAINT_TOWER);
        paintNearbyMarks();

        if (completeRuinAs(currRuinLoc, UnitType.LEVEL_ONE_PAINT_TOWER)) {
            currRuinLoc = null;
        }
    }

    // can override ruin-to-tower conversion and it fights over markings
    // BUT it is able to provide SRPs
    public static void paintResourcePattern() throws GameActionException {
        if (rc.getLocation().x % 5 != 0 || rc.getLocation().y % 5 != 0) 
            return;
        if (!rc.canMarkResourcePattern(rc.getLocation())) 
            return;

        currSRPLoc = rc.getLocation();
        currPOI = currSRPLoc;
        rc.markResourcePattern(currSRPLoc);

        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            if (tile.getMark() == PaintType.ALLY_PRIMARY || tile.getMark() == PaintType.ALLY_SECONDARY) {
                if (rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
                }
            }
        }

        if (rc.canCompleteResourcePattern(currSRPLoc)) {
            rc.completeResourcePattern(currSRPLoc);
            currSRPLoc = null;
        }
    }

    /** Paint all nearby marked tiles */
    public static void paintNearbyMarks() throws GameActionException {
        boolean missedPaint = false;

        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY)
                continue; // can't paint over enemy squares
            if (paintType == tile.getMark() || tile.getMark() == PaintType.EMPTY)
                continue; // already painted the right way or no paint marker

            if (rc.canAttack(tile.getMapLocation())) {
                rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
            } else {
                missedPaint = true;
                currPOI = tile.getMapLocation();
            }
        }

        if (!missedPaint) {
            currPOI = null;
        }
    }



    // static enum Mode {LOW_PAINT, RUIN_FINDING, RUIN_BUILDING};
    // static Mode mo = Mode.LOW_PAINT;
    // static boolean painter = false;

    // public static void run() throws GameActionException {
    //     if (rc.readMessages(-1).length > 0) {
    //         painter = true;
    //     }

    //     update_paint_tower_loc();
    //     acquire_paint();

    //     if (painter) {
    //         paintNearby();
    //         Unit.wander();

    //     } else {
    //         indicator = rc.getRoundNum() + ": ";

    //         if(paint_tower != null) {
    //             rc.setIndicatorDot(paint_tower, 255, 128, 128);
    //         }
    
    //         try {
    //             update_paint_tower_loc();
    //             acquire_paint();
    //             get_target_location();
                
    //             if(target_location != null) {
    //                 rc.setIndicatorDot(target_location, 0, 255, 0);
    //             } 
    
    //             move();
    //             paint();
    //             complete_patterns();
    
    //         } catch(Exception e) {
    //             e.printStackTrace();
    //         }
            
    //         switch(mo) {
    //             case LOW_PAINT: rc.setIndicatorString("Low Paint");
    //             case RUIN_BUILDING: rc.setIndicatorString("Ruin Building");
    //             case RUIN_FINDING: rc.setIndicatorString("Ruin Finding");
    //         }    
    //     }
    // }

    // public static void paintNearby() throws GameActionException {
    //     MapInfo[] tiles = rc.senseNearbyMapInfos();
    //     for (MapInfo tile : tiles) {
    //         PaintType paintType = tile.getPaint();
    //         if (paintType == PaintType.EMPTY || paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
    //             if (rc.canAttack(tile.getMapLocation())) {
    //                 rc.attack(tile.getMapLocation());
    //             }
    //             return;
    //         }
    //     }
    // }

    // /**
    //  * Finds the robots next target location
    //  * This method should contain all such logic
    //  */
    // public static void get_target_location() throws GameActionException {
    //     if(rc.getPaint() < 50 && paint_tower != null) {
    //         target_location = paint_tower;
    //         mo = Mode.LOW_PAINT;
    //         return;
    //     }
        
    //     boolean has_enemy_paint = false;
    //     if(ruin_location != null && rc.canSenseLocation(ruin_location)) {
    //         MapInfo[] locations = rc.senseNearbyMapInfos(ruin_location, 8);
    //         if(locations.length == 25) {
    //             for(MapInfo loc : locations) {
    //                 if(loc.getPaint().equals(PaintType.ENEMY_PRIMARY) 
    //                     || loc.getPaint().equals(PaintType.ENEMY_SECONDARY)) {
    //                         has_enemy_paint = true;
    //                 }
    //             }
    //         }
    //     }
        

    //     if (ruin_location == null 
    //         || (rc.canSenseLocation(ruin_location)
    //             && rc.senseRobotAtLocation(ruin_location) != null)
    //         || has_enemy_paint) {
    //         ruin_location = Unit.findRuin();
    //     }

    //     if(ruin_location == null) {
    //         target_location = null;
    //         mo = Mode.RUIN_FINDING;
    //         return;
    //     }
        
    //     if (rc.canSenseLocation(ruin_location)) {
    //         indicator += "moving around ruin, ";
    //         target_location = new MapLocation(ruin_location.x - 2 + rng.nextInt(5),
    //                 ruin_location.y - 2 + rng.nextInt(5));
    //     } else {
    //         indicator += "update target to ruin, ";
    //         target_location = ruin_location;
    //     }

    //     mo = Mode.RUIN_BUILDING;
    //     indicator += target_location.toString() + ", ";
    // }

    // /**
    //  * Contains all logic for movement
    //  */
    // public static void move() throws GameActionException {
    //     if(target_location == null) wander();
    //     else Navigator.moveTo(target_location);
    // }

    /**
     * Paints the given color at the given location
     */
    public static boolean paint_at(MapLocation location, PaintType type) throws GameActionException {
        if (!rc.canSenseLocation(location))
            return false;
        if (!rc.canAttack(location))
            return false;
        
        MapInfo info = rc.senseMapInfo(location);
        
        if (info.getPaint() == type) return true;
        
        //soldiers can't paint over enemy paint
        if(info.getPaint() == PaintType.ENEMY_PRIMARY 
            || info.getPaint() == PaintType.ENEMY_SECONDARY) {
                return false;
        }

        if (type.equals(PaintType.ALLY_PRIMARY)) {
            rc.attack(location, false);
            indicator += location.toString() + ", ";
            rc.setIndicatorDot(location, 0, 0, 0);
            return true;
        } else {
            rc.attack(location, true);
            indicator += location.toString() + ", ";
            rc.setIndicatorDot(location, 0, 0, 0);
            return true;
        }
    }

    /**
     * Paints whatever is marked at that location
     */
    public static boolean paint_mark(MapLocation location) throws GameActionException {
        if (!rc.canSenseLocation(location))
            return false;
        if (!rc.canAttack(location)) {
            return false;
        }

        MapInfo info = rc.senseMapInfo(location);
        if (info.getMark() == PaintType.EMPTY)
            return true; // vacuously painted
        else
            return paint_at(location, info.getMark());

    }
}
