package V04.Units;

import V04.*;
import battlecode.common.*;

public class Tower extends Unit {
    public static int numSoldiers = 0;
    public static int numMoppers = 0;
    public static int numSplashers = 0;

    public static void run() throws GameActionException {
        indicator = "";
        
        if (rc.getRoundNum() <= 4) {
            rushBuild();
        } else {
            boomBuild();
        }

        attack();
        upgradeTower();
        rc.setIndicatorString(indicator);
    }

    /** Attacks nearest robot and then performs aoe attack */
    public static void attack() throws GameActionException{
        for(RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            if(rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
                break;
            }
        }
        rc.attack(null);
    }

    /** Attempt to build robot of specified type */
    public static boolean buildRobotType(UnitType type) throws GameActionException {
        for (MapInfo neighborSquare : rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
            if (rc.canBuildRobot(type, neighborSquare.getMapLocation())) {
                rc.buildRobot(type, neighborSquare.getMapLocation());
                return true;
            }
        }
        return false;
    }

    /** Build only moppers and soldiers if round < 1500...? Splashers >= 1500 */
    public static void boomBuild() throws GameActionException {    
        if(rc.getRoundNum() >= 1500) {
            if (buildRobotType(UnitType.SPLASHER))
                numSplashers++;
        }    
        
        if(numMoppers < numSoldiers) {
            if (buildRobotType(UnitType.MOPPER))
                numMoppers++;
        } else {
            if (buildRobotType(UnitType.SOLDIER))
                numSoldiers++;
        }               
    }

    /** Build soldier */
    public static void rushBuild() throws GameActionException {
        if (buildRobotType(UnitType.SOLDIER))
            numSoldiers++;
    }

    /** Upgrade tower at robot's location */
    public static void upgradeTower() throws GameActionException {
        if(!rc.canUpgradeTower(rc.getLocation())) 
            return; // can't upgrade
        if(rc.getNumberTowers() <= 5) 
            return; // first wait build clock towers
        if(rc.getMoney() <= 3000) 
            return; // BROKE

        rc.upgradeTower(rc.getLocation());
    }
}
