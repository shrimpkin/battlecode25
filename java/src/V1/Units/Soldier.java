package V1.Units;

import V1.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation ruin_location = null;
    static MapLocation target_location = null;
    static boolean can_paint_tower = true;

    public static void run() throws GameActionException {
        indicator = rc.getRoundNum() + ": ";

        try {
            update_paint_tower_loc();
            acquire_paint();
            get_target_location();
            move();
            paint();
            complete_patterns();
            attack();
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
        if(rc.getPaint() < 50 && paint_tower != null) {
            target_location = paint_tower;
            indicator += "looking for paint, ";
            return;
        }
        
        boolean has_enemy_paint = false;
        if(ruin_location != null && rc.canSenseLocation(ruin_location)) {
            MapInfo[] locations = rc.senseNearbyMapInfos(ruin_location, 8);
            if(locations.length == 25) {
                for(MapInfo loc : locations) {
                    if(loc.getPaint().equals(PaintType.ENEMY_PRIMARY) 
                        || loc.getPaint().equals(PaintType.ENEMY_SECONDARY)) {
                            indicator += "has enemy paint";
                            has_enemy_paint = true;
                    }
                }
            }
        }
        

        if (ruin_location == null 
            || (rc.canSenseLocation(ruin_location)
                && rc.senseRobotAtLocation(ruin_location) != null)
            || has_enemy_paint) {
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
        if(target_location == null || rc.getMoney() < 1000) {
            indicator += "wandering, ";
            wander(true);
        } else {
            indicator += "move to target, ";
            Navigator.moveTo(target_location, true);
        }
    }

    /**
     * Contains all paint logic
     */
    public static void paint() throws GameActionException {
        //currently focuses on painting below the robot as fast as possible to reduce paint loss
        mark_tower(false);
        paint_marks();
        paint_below();
    }

    /**
     * Paints below the robot if possible
     */
    public static void paint_below() throws GameActionException {
        MapLocation loc = new MapLocation(rc.getLocation().x - 1 + rng.nextInt(2) , rc.getLocation().y - 1 + rng.nextInt(2));
        MapInfo info = rc.senseMapInfo(loc);

        switch (info.getPaint()) {
            case PaintType.EMPTY:
                if (rc.canAttack(loc)) {
                    indicator += "pt below, ";
                    // paints the tile, if it can paint the mark it paints that
                    if (!paint_mark(loc)) {
                        rc.attack(loc);
                    }
                }
                break;
            default: break;
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
        if(!rc.canSenseLocation(ruin_location)) return;
        if(!rc.canSenseLocation(ruin_location)) return;
        
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

        //TODO: this is hard coded and it shouldn't be
        if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location)) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruin_location);
            ruin_location = null;
        }
    }

    /**
     * Attacks any nearby towers if possible
     */
    public static void attack() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();

        //TODO: update this to attack lower health tower?
        //I don't think the robot will generally be able to sense multipe towers so eh?
        for(RobotInfo robot : robots) {
            if(rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
                rc.setIndicatorDot(ruin_location, 255, 0, 0);
            }
        }
    }
}
