package V3.Units;

import battlecode.common.*;
import V3.*;

public class Tower extends Unit {
    public static int num_built_soldier = 0;
    public static int num_built_mopper = 0;

    public static void run() throws GameActionException {
        indicator = "";
        
        if(rc.getRoundNum() <= 4) rush_spawn();
        else run_paint();

        give_paint();
        attack();

        rc.setIndicatorString(indicator);
    }

    //TODO: Make this smart, ie attack least health robots 
    // or other criteria 
    // I also don't know how aoe attacks work
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

    public static void give_paint() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robotInfo) {
            int transfer_amount = Math.min(rc.getPaint(), robot.getType().paintCapacity - robot.getPaintAmount());
            indicator += transfer_amount + ", ";
            indicator += robot.getLocation().toString() + ", ";
            if(rc.canTransferPaint(robot.getLocation(), transfer_amount)) {
                indicator += "transferred, ";
                rc.transferPaint(robot.getLocation(), transfer_amount);
            }
        }
    }
    /**
     * Special behavior for paint tower
     * @param rc
     */
    public static void run_paint() throws GameActionException {
        //building a solider to do some paint testing
        if(num_built_soldier * 500 < rc.getRoundNum()) {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
                num_built_soldier++;
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
            }
        }

        if(num_built_mopper * 500 < rc.getRoundNum()) {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
                num_built_mopper++;
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
            }
        }
        
        if(rc.canBuildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST))) {
            rc.buildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST));
        }        
    }

    
    public static void rush_spawn() throws GameActionException {
        if(rc.getRoundNum() <= 4) {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
            }
        }
    }
}
