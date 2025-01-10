package V3.Units;

import java.util.Map;

import V3.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation[] targets = new MapLocation[3];
    static Boolean[] valid_targets = {true, true, true};
    static MapLocation tower_location = null;

    public static void run() throws GameActionException {
        indicator = "";

        get_rush_targets();
        update_rush_targets();
        move();
        attack();

        for(int i = 0; i < 3; i++) {
            if(valid_targets[i]) {
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

        if(targets[0] == null) {
            wander();
            return;
        }

        for(int i = 0; i < 3; i++) {
            if(!valid_targets[i]) continue;

            Navigator.moveTo(targets[i], true);
        }
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
