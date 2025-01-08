package V1.Units;

import V1.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation ruin_location = null;
    static MapLocation target_location = null;
    static String indicator;

    public static void run() throws GameActionException {
        indicator = rc.getRoundNum() + ": ";

        try {
            get_target_location();
            move();
            paint();
            complete_patterns();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        rc.setIndicatorString(indicator);
    }

    /**
     * Finds the robots next target location
     * This method should contain all such logic
     */
    public static void get_target_location() throws GameActionException {
        if (ruin_location == null) {
            indicator += "finding ruin, ";
            ruin_location = Unit.findRuin();
        }

        if(ruin_location == null) {
            target_location = null;
            indicator += "null, ";
            return;
        }
        
        if (rc.canSenseLocation(ruin_location)) {
            indicator += "moving around ruin, ";
            target_location = new MapLocation(ruin_location.x - 2 + rng.nextInt(5),
                    ruin_location.y - 2 + rng.nextInt(5));
        } else {
            indicator += "update target to ruin, ";
            target_location = ruin_location;
        }

        indicator += target_location.toString() + ", ";
    }

    /**
     * Contains all logic for movement
     */
    public static void move() throws GameActionException {
        if(target_location == null) {
            indicator += "wandering, ";
            wander();
        } else {
            indicator += "move to target, ";
            Navigator.moveTo(target_location);
        }
    }

    /**
     * Contains all paint logic
     */
    public static void paint() throws GameActionException {
        //currently focuses on painting below the robot as fast as possible to reduce paint loss
        mark_tower(false);
        paint_below();
        paint_marks();
    }

    /**
     * Paints below the robot if possible
     */
    public static void paint_below() throws GameActionException {
        MapLocation loc = rc.getLocation();
        MapInfo info = rc.senseMapInfo(loc);

        switch (info.getPaint()) {
            case PaintType.ALLY_PRIMARY:
            case PaintType.ALLY_SECONDARY:
                break;
            default:
                if (rc.canAttack(loc)) {
                    // paints the tile, if it can paint the mark it paints that
                    if (!paint_mark(loc)) {
                        rc.attack(loc);
                    }
                }
                break;
        }
    }

    /**
     * Iterates through map and paints a mark if possible
     */
    public static void paint_marks() throws GameActionException {
        MapInfo[] locations = rc.senseNearbyMapInfos();
        for (MapInfo info : locations) {
            paint_mark(info.getMapLocation());
        }
    }

    /**
     * Marks a tower pattern at the ruin_location field if possible
     */
    public static void mark_tower(boolean mark_built_towers) throws GameActionException{
        if(ruin_location == null) return;
        //Checks if a tower is already built there 
        //Since it is on a ruin checking if there is a robot there is sufficient
        if(!mark_built_towers && rc.senseRobotAtLocation(ruin_location) != null) {
            return;
        }
        UnitType tower = has_tower_marked(ruin_location);

        if (ruin_location == null)
            return;

        if ((tower == null || !tower.equals(UnitType.LEVEL_ONE_PAINT_TOWER))
                && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location)) {

            rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location);
        }
    }

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
            if (!rc.canAttack(location))
                return false;
        }

        MapInfo info = rc.senseMapInfo(location);
        if (info.getMark() == PaintType.EMPTY)
            return true; // vacuously painted
        else
            return paint_at(location, info.getMark());

    }

    public static void complete_patterns() throws GameActionException {
        if(ruin_location == null) return;

        if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location)) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location);
            ruin_location = null;
        }
    }
}
