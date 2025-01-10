package V1_2.Units;

import V1_2.*;
import battlecode.common.*;

public class Tower extends Globals {
    public static int turn_num = 0;
    public static int num_built_soldier = 0;

    public static void run() throws GameActionException {
        turn_num++;
        rc.setIndicatorString(rc.getPaint() + " ");
        
        //upgrades both our towers on round one,
        //build order type stuff, should be experimented with
        if(rc.getRoundNum() == 1) {
            if(rc.canUpgradeTower(rc.getLocation())) {
                rc.upgradeTower(rc.getLocation());
            }
        }

        if(rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            run_paint();
        }

        attack();
    }

    //TODO: Make this smart, ie attack least health robots 
    // or other criteria 
    public static void attack() throws GameActionException{
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, opponentTeam);
        for(RobotInfo robot : robotInfo) {
            if(rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
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
        if(num_built_soldier * 100 < rc.getRoundNum()) {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
                num_built_soldier++;
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
            }
        }
        
        if(rc.canBuildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST))) {
            rc.buildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST));
        }

        //current going to self destruct if it can be immediately rebuilt
        
    }
}
