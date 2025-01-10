package V3.Units;

import V3.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation[] targets = new MapLocation[3];
    static Boolean[] valid_targets = {true, true, true};
    
    static MapLocation[] tower_locations = new MapLocation[3];
    static Boolean[] dead_tower = {false, false, false};

    public static void run() throws GameActionException {
        indicator = "";

        notice_towers();
        get_rush_targets();
        update_rush_targets();
        move();
        attack();

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

        rc.setIndicatorString(indicator);
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

    public static void move() throws GameActionException {
        for(int i = 0; i < tower_locations.length; i++) {
            if(tower_locations[i] != null && !dead_tower[i]) {
                rc.setIndicatorDot(tower_locations[i], 0, 0, 0);
                Navigator.moveTo(tower_locations[i], true);
                return;
            }
            
        }

        for(int i = 0; i < targets.length; i++) {
            if(!valid_targets[i]) continue;
            
            rc.setIndicatorDot(targets[i], 255, 0, 0);
            Navigator.moveTo(targets[i], true);
            return;
        }

        wander();
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
}
