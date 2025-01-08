package V1.Units;

import battlecode.common.*;

import V1.*;

public class Tower extends Globals {
    public static int turn_num = 0;

    public static void run() throws GameActionException {
        turn_num++;
        rc.setIndicatorString(rc.getPaint() + " ");
        
        if(rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            run_paint();
        }

        attack();
    }

    public static void attack() throws GameActionException{
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, opponentTeam);
        for(RobotInfo robot : robotInfo) {
            if(rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    public static void run_money() {

    }

    public static void run_defense() {

    }

    /**
     * Special behavior for paint tower
     * @param rc
     */
    public static void run_paint() throws GameActionException {
        //building a solider to do some paint testing
        if(turn_num == 1) {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
            }
        }

        //current going to self destruct if it can be immediately rebuilt
        
    }
}
