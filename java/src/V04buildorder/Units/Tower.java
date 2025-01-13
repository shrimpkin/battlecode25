package V04buildorder.Units;

import V04buildorder.*;
import battlecode.common.*;

public class Tower extends Unit {
    public static int numSoldiers = 0;
    public static int numMoppers = 0;
    public static int numSplashers = 0;

    public static void run() throws GameActionException {
        indicator = "";
        
        
        boomBuild();
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
    static int timeSinceBuilt = 20;
    public static void boomBuild() throws GameActionException { 
        if(timeSinceBuilt++ < 20) return;

        if(rc.getRoundNum() <= 50 || nextDouble() <= .9) {
            buildRobotType(UnitType.SOLDIER);
            timeSinceBuilt = 0;
        } else if(nextDouble() >= .5){
            buildRobotType(UnitType.SPLASHER);
            timeSinceBuilt = 0;
        } else {
            buildRobotType(UnitType.MOPPER);
            timeSinceBuilt = 0;
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
