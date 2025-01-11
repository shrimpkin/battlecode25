package V3.Units;

import battlecode.common.*;
import V3.*;

public class Tower extends Unit {
    public static int num_built_soldier = 0;
    public static int num_built_mopper = 0;
    public static int num_built_splasher = 0;

    public static void run() throws GameActionException {
        indicator = "";
        
        if(rc.getRoundNum() <= 4) rushBuild();
        else boomBuild();
        
        givePaint();
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

    //TODO: This doesn't work
    public static void givePaint() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robotInfo) {
            int transfer_amount = Math.min(rc.getPaint(), robot.getType().paintCapacity - robot.getPaintAmount());
            indicator += "t: " + transfer_amount + ", ";
            
            MapLocation loc = robot.getLocation();
            indicator += loc.toString() + ", ";
            if(rc.canTransferPaint(loc, transfer_amount)) {
                indicator += "transferred, ";
                rc.transferPaint(loc, transfer_amount);
            }
        }
    }

    /**
     * Special behavior for paint tower
     * @param rc
     */
    public static void boomBuild() throws GameActionException {        
        if(num_built_splasher >= num_built_soldier) {
            if(rc.canBuildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST))) {
                rc.buildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST));
                num_built_soldier++;
            } 
        } else {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.WEST))) {
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.WEST));
                num_built_splasher++;
            } 
        }               
    }

    public static void rushBuild() throws GameActionException {
        if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
            num_built_soldier++;
            rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
        }
    }
}
