package V3.Units;

import V3.*;
import battlecode.common.*;

public class Soldier extends Unit {
    enum Presence {NOT_SEEN, NOT_THERE, IS_THERE};
    enum Modes {RUSH, BOOM, SIT, NONE, GET_PAINT};

    static MapLocation[] targets = new MapLocation[6];
    static Presence[] targetPresence = new Presence[6];
    static Modes mode = Modes.NONE;
    static int turnsAlive = 0;

    boolean hasCompletedInit = false;

    static MapLocation targetLocation = null;

    public static void run() throws GameActionException {    
        if(turnsAlive == 0) init();

        turnsAlive++;
        indicator = "";

        updateMode();
        updatePaintTowerLocations();

        if(mode == Modes.GET_PAINT) {
            targetLocation = closestPaintTower();
            move();
            getPaint(UnitType.SOLDIER.paintCapacity);
        }

        if(mode == Modes.RUSH) {
            getRushTargets();
            updateRushTargets();
            targetLocation = getRushMoveTarget();
            move();
        }  
        
        if(mode == Modes.BOOM) {
            find_valid_tower_pos();
            paint_clock();

            if(ruin_target != null) {
                MapLocation around_ruin = new MapLocation(ruin_target.x - 2 + (nextInt() % 4), ruin_target.y - 2 + (nextInt() % 4));
                Navigator.moveTo(around_ruin, false);
            } else {
                wander(false);
            }
        } 
        
        if(mode == Modes.SIT) {
            sit();
        }

        attack();
        debug();
    }

    public static void init() {
        for(int i = 0; i < targetPresence.length; i++) {
            targetPresence[i] = Presence.NOT_SEEN;
        }
    }    

    /**
     * Changes mode based on criteria I haven't quite figured out yet
     */
    public static void updateMode() throws GameActionException {
        if(rc.getPaint() <= 50) {
            mode = Modes.GET_PAINT;
            return;
        }

        if(rc.getRoundNum() <= rc.getMapHeight() + rc.getMapWidth()) {
            mode = Modes.RUSH;
            return;
        }

        if(rc.getRoundNum() >= rc.getMapHeight() + rc.getMapWidth()) {
            mode = Modes.BOOM;
            return;
        }
    }

    //==================================================================\\ 
    //                           Sit                                     \\

    public static void sit() throws GameActionException {
        MapInfo info = rc.senseMapInfo(rc.getLocation());

        if(info.getPaint() == PaintType.ALLY_PRIMARY 
            || info.getPaint() == PaintType.ALLY_SECONDARY) return;

        
        if(rc.canAttack(rc.getLocation())) {
            rc.attack(rc.getLocation());
        }
    }

    //==================================================================\\ 
    //                           Rush                                    \\

    /**
     * Uses map symmetry and our tower positions to generate possible locations for enemy towers
     */
    public static void getRushTargets() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robots) {
            if(robot.getType() == UnitType.LEVEL_ONE_MONEY_TOWER || robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                int x = robot.getLocation().x;
                int y = robot.getLocation().y;

                MapLocation vert = new MapLocation(mapWidth - x - 1,y);
                MapLocation hort = new MapLocation(x, mapHeight - y - 1);
                MapLocation mirr = new MapLocation(mapWidth - x - 1, mapHeight - y - 1);

                if(robot.getType() == UnitType.LEVEL_ONE_MONEY_TOWER) {
                    targets[0] = vert;
                    targets[1] = mirr;
                    targets[2] = hort;
                } else {
                    targets[3] = vert;
                    targets[4] = mirr;
                    targets[5] = hort;
                }
            }
        }        
    }

    /**
     * Updates array that keeps track of all possible tower positions for enemy towers
     * Changes targetPresence array to reflect any new information
     */
    public static void updateRushTargets() throws GameActionException {
        for(int i = 0; i < targets.length; i++) {
            if(targets[i] == null) continue;

            MapLocation loc = targets[i];
            if(!rc.canSenseLocation(loc)) continue;

            RobotInfo robotInfo = rc.senseRobotAtLocation(loc);
            if(robotInfo == null) {
                targetPresence[i] = Presence.NOT_THERE;
                continue;
            }

            UnitType robotType = robotInfo.getType();
            if(!(isPaintTower(robotType) || isMoneyTower(robotType))) {
                targetPresence[i] = Presence.NOT_THERE;
                return;
            }

            targetPresence[i] = Presence.IS_THERE;
        }
    }

    /**
     * @returns If possible a tower that have seen, otherwise the nearest possible location for a tower
     */
    public static MapLocation getRushMoveTarget() throws GameActionException {
        int minDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        for(int i = 0; i < targets.length; i++) {
            if(targets[i] == null) continue;
            
            if(targetPresence[i] == Presence.IS_THERE) {
                return targets[i];
            }

            int distanceToTarget = rc.getLocation().distanceSquaredTo(targets[i]);
            if(targetPresence[i] == Presence.NOT_SEEN && distanceToTarget < minDistance) {
                minDistance = distanceToTarget;
                bestLocation = targets[i];
            }
        }

        return bestLocation;
    }

    /**
     * Uses getRushMoveTarget() to target towers, if we know of no more possible towers it will wander
     */
    public static void move() throws GameActionException {
        if(targetLocation != null) {
            Navigator.moveTo(targetLocation, true);
        } else {
            wander(true);
        }
    }

     //==================================================================\\ 
    //                           Boom                                     \\

    static final int max_ruins = 144;
    static MapLocation ruin_target = null;

    /**
     * Will attempt to build clock tower on nearby empty ruins
     */
    public static void find_valid_tower_pos() throws GameActionException {
        ruin_target = null;
        MapLocation[] ruin_locations = rc.senseNearbyRuins(-1);

        for(MapLocation ruin : ruin_locations) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);
            
            if(robot != null) continue;

            MapInfo[] ruin_suroundings = rc.senseNearbyMapInfos(ruin, 8);
            boolean has_enemy_paint = false;
            //Checks for nearby enemy paint that would prevent building a clock tower
            for(MapInfo loc : ruin_suroundings) {
                PaintType paint = loc.getPaint();
                if(paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) {
                    System.out.println("has bad paint");
                    has_enemy_paint = true;
                }
            }

            if(!has_enemy_paint) ruin_target = ruin;
            break;
        }
    }

    public static void paint_clock() throws GameActionException {
        if(ruin_target == null) return;
        if(!rc.canSenseLocation(ruin_target)) return;

        //if(rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target)) rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target);

        MapInfo[] ruin_suroundings = rc.senseNearbyMapInfos(ruin_target, 8);
        boolean[][] paint_pattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);

        for(MapInfo info : ruin_suroundings) {
            MapLocation loc = info.getMapLocation();
            if(!rc.canAttack(loc)) continue;
            if(info.hasRuin()) continue;
            

            int x = ruin_target.x - loc.x + 2;
            int y = ruin_target.y - loc.y + 2;

            PaintType paint = info.getPaint();
            if((paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY)) continue;

            if(paint_pattern[x][y] && paint != PaintType.ALLY_SECONDARY) {
                rc.attack(loc, true);
                indicator += loc.toString();
            } else if(!paint_pattern[x][y] && paint != PaintType.ALLY_PRIMARY) {
                rc.attack(loc, false);
                indicator += loc.toString();
            }
        }

        if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target)) 
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target);
    }

    public static void attack() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);

        for(RobotInfo robot : robots) {
            if( (robot.getType() == UnitType.LEVEL_ONE_MONEY_TOWER 
                || robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER)
                && rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    //==========================================================================\\

    /**
     * Prints all debug info
     */
    public static void debug() {
        if(!in_debug) return;

        switch(mode) {
            case BOOM: indicator += "Boom: "; break; 
            case NONE: indicator += "None: "; break;
            case RUSH: indicator += "Rush: "; break;
            case SIT:  indicator +=  "Sit: "; break;
            case GET_PAINT: indicator += "Getting Paint: "; break;
            default: break;
        }

        indicator += "Move Target: ";
        if(targetLocation != null) {
            indicator += targetLocation.toString() + "\n";
        } else {
            indicator += "null target\n";
        }

        if(mode == Modes.RUSH) {
            for(int i = 0; i < targets.length; i++) {
                if(targets[i] == null) continue;

                switch(targetPresence[i]) {
                    case IS_THERE: indicator += "Is at: "; break;
                    case NOT_SEEN: indicator += "Haven't checked: "; break;
                    case NOT_THERE: indicator += "Not at: "; break;
                    default: break;
                }
                
                indicator += targets[i].toString() + ", \n";
                
            }
        }

        if(mode == Modes.BOOM) {
            if(ruin_target != null) indicator += ruin_target.toString();
        }
        
        rc.setIndicatorString(indicator);
        System.out.println(indicator);
    }

    
}
