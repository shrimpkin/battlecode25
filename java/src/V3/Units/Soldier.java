package V3.Units;

import java.util.Map;

import V3.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation[] targets = new MapLocation[6];
    static Boolean[] valid_targets = {true, true, true, true, true, true};
    
    static MapLocation[] tower_locations = new MapLocation[3];
    static Boolean[] dead_tower = {false, false, false};
    
    static boolean has_killed_tower = false;

    enum Modes {RUSH, BOOM, NONE};
    static Modes mode = Modes.NONE;

    public static void run() throws GameActionException {
        indicator = "";
        update_mode();
        update_paint_tower_loc();

        int start = Clock.getBytecodeNum();

        int end = Clock.getBytecodeNum();
        System.out.println("Delta: " + (end - start));

        if(mode == Modes.RUSH) {
            notice_towers();
            get_rush_targets();
            update_rush_targets();
            rush_move();
        } else if(mode == Modes.BOOM) {
            find_valid_tower_pos();
            paint_clock();

            if(rc.getPaint() < 100) {
                acquire_paint(200);
            } else if(ruin_target != null) {
                MapLocation around_ruin = new MapLocation(ruin_target.x - 2 + rng.nextInt(4), ruin_target.y - 2 + rng.nextInt(4));
                Navigator.moveTo(around_ruin, false);
            } else {
                wander(false);
            }
        }

        attack();
        
        switch(mode) {
            case BOOM: indicator += "Boom: ";
                break; 
            case NONE: indicator += "None: ";
                break;
            case RUSH: indicator += "Rush: ";
                break;
            default:
                break;
            
        }

        if(mode == Modes.RUSH) {
            for(int i = 0; i < tower_locations.length; i++) {
                if(tower_locations[i] != null && !dead_tower[i]) {
                    indicator += tower_locations[i].toString() + ". ";
                }
            }
    
            for(int i = 0; i < 3; i++) {
                if(targets[i] != null && valid_targets[i]) {
                    indicator += targets[i].toString() + ", ";
                }
            }
        }

        if(mode == Modes.BOOM) {
            if(ruin_target != null) indicator += ruin_target.toString();
        }
        
        rc.setIndicatorString(indicator);
    }

    /**
     * Will update mode to something new if it is NONE
     */
    public static void update_mode() throws GameActionException {
        if(rc.getRoundNum() <= 10) mode = Modes.RUSH;
        if(rc.getRoundNum() >= 50) mode = Modes.BOOM;
    }

    public static void get_rush_targets() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robots) {
            if(robot.getType() == UnitType.LEVEL_ONE_MONEY_TOWER || robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                int x = robot.getLocation().x;
                int y = robot.getLocation().y;

                MapLocation vert = new MapLocation(mapWidth - x - 1,y);
                MapLocation hort = new MapLocation(x, mapHeight - y - 1);
                MapLocation mirr = new MapLocation(mapWidth - x - 1, mapHeight - y - 1);

                targets[0] = vert;
                targets[1] = mirr;
                targets[2] = hort;
            }
        }        
    }

    public static void notice_towers() throws GameActionException {
        //sensing new towers
        RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);
        for(RobotInfo robot : robots) {
            if(robot.getType() == UnitType.LEVEL_ONE_MONEY_TOWER || robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                for(int i = 0; i < tower_locations.length; i++) {
                    if(tower_locations[i] == null) {

                        boolean should_update = true;
                        for(int j = 0; j < tower_locations.length; j++) {
                            if(tower_locations[j] != null && tower_locations[j] == robot.getLocation()) {
                                should_update = false;
                            } 
                        }

                        if(should_update) {
                            tower_locations[i] = robot.getLocation();
                            break;
                        }
                    }
                }
            }
        }

        //updating dead towers
        MapLocation[] ruins = rc.senseNearbyRuins(-1);
        for(MapLocation loc : ruins) {
            //indicator += "ruin: " + loc.toString();
            for(int i = 0; i < tower_locations.length; i++) {
                
                if(tower_locations[i] != null && tower_locations[i].equals(loc) && rc.senseRobotAtLocation(loc) == null) {
                    System.out.println("Killed tower!");
                    dead_tower[i] = true;
                }
            }
        }
    }

    public static void update_rush_targets() throws GameActionException {
        for(int i = 0; i < 3; i++) {
            MapLocation loc = targets[i];
            if(rc.canSenseLocation(loc) 
                && (rc.senseRobotAtLocation(loc) == null
                || (rc.senseRobotAtLocation(loc).getType() != UnitType.LEVEL_ONE_MONEY_TOWER
                && rc.senseRobotAtLocation(loc).getType() != UnitType.LEVEL_ONE_PAINT_TOWER))) {
                valid_targets[i] = false;
            } 
        }
    }

    public static void rush_move() throws GameActionException {
        for(int i = 0; i < tower_locations.length; i++) {
            if(tower_locations[i] != null && !dead_tower[i]) {
                rc.setIndicatorDot(tower_locations[i], 0, 0, 0);
                Navigator.moveTo(tower_locations[i], true);
                return;
            }
            
        }

        for(int i = 0; i < targets.length; i++) {
            if(!valid_targets[i]) continue;
            if(targets[i] == null) continue;

            rc.setIndicatorDot(targets[i], 255, 0, 0);
            Navigator.moveTo(targets[i], true);
            return;
        }

        wander(false);
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

                if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target)) 
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target);
                return;
            } else if(!paint_pattern[x][y] && paint != PaintType.ALLY_PRIMARY) {
                rc.attack(loc, false);
                indicator += loc.toString();

                if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target)) 
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruin_target);
                return;
            }
        }
    }

}
