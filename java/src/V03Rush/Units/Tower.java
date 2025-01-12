package V03Rush.Units;

import V03Rush.*;
import battlecode.common.*;

public class Tower extends Unit {
    public static int numberOfBuiltSoldiers = 0;
    public static int numberOfBuiltMoppers = 0;
    public static int numberOfBuiltSplashers = 0;

    public static void run() throws GameActionException {
        indicator = "";
        
        if(rc.getRoundNum() <= 4) rushBuild();
        else boomBuild();

        givePaint();
        attack();
        upgradeTower();
        rc.setIndicatorString(indicator);
    }

    /**
     * Attacks nearest robot and performs aoe attack
     */
    public static void attack() throws GameActionException{
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, opponentTeam);
        for(RobotInfo robot : robotInfo) {
            if(rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }

        rc.attack(null);
    }

    //TODO: This doesn't work
    public static void givePaint() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robotInfo) {
            int transferAmount = Math.min(rc.getPaint(), robot.getType().paintCapacity - robot.getPaintAmount());
            indicator += "t: " + transferAmount + ", ";
            
            MapLocation loc = robot.getLocation();
            indicator += loc.toString() + ", ";
            if(rc.canTransferPaint(loc, transferAmount)) {
                indicator += "transferred, ";
                rc.transferPaint(loc, transferAmount);
            }
        }
    }

    /**
     * Building for booming
     */
    public static void boomBuild() throws GameActionException {    
        if(rc.getRoundNum() >= 1500) {
            if(rc.canBuildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST))) {
                rc.buildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.WEST));
                numberOfBuiltSplashers++;
            }
        }    
        
        if(numberOfBuiltMoppers < numberOfBuiltSoldiers) {
            if(rc.canBuildRobot(UnitType.MOPPER, rc.getLocation().add(Direction.WEST))) {
                rc.buildRobot(UnitType.MOPPER, rc.getLocation().add(Direction.WEST));
                numberOfBuiltMoppers++;
            } 
        } else {
            if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.WEST))) {
                rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.WEST));
                numberOfBuiltSoldiers++;
            } 
        }               
    }

    /**
     * Building for rushing
     */
    public static void rushBuild() throws GameActionException {
        if(rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
            numberOfBuiltSoldiers++;
            rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
        }
    }

    public static void upgradeTower() throws GameActionException {
        if(!rc.canUpgradeTower(rc.getLocation())) return;
        if(rc.getNumberTowers() <= 5) return;
        if(rc.getMoney() <= 3000) return;

        rc.upgradeTower(rc.getLocation());
    }
}
