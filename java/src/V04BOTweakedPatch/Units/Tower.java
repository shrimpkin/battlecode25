package V04BOTweakedPatch.Units;

import V04BOTweakedPatch.Unit;
import battlecode.common.*;

public class Tower extends Unit {
    static int timeSinceBuilt = 20;

    public static void run() throws GameActionException {
        indicator = "";

        defend(); // spawns at most 1 mopper
        unitBuild();
        attack();
        upgradeTower();
        rc.setIndicatorString(indicator);
    }

    /** Checks if there are enemy units nearby, and spawns defensive moppers if so */
    // check
    // 1. presense of enemy robots
    // 2. presense of enemy paint
    // spawn moppers when either one gets too high
    public static void defend() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            indicator += "saw an enemy; ";

            for (MapInfo spawnTile : rc.senseNearbyMapInfos(robot.getLocation(), 2)) {
                if (rc.canBuildRobot(UnitType.MOPPER, spawnTile.getMapLocation())) {
                    indicator += "spawning defense mopper; ";
                    rc.buildRobot(UnitType.MOPPER, spawnTile.getMapLocation());

                    if (rc.canSendMessage(spawnTile.getMapLocation())) {
                        rc.sendMessage(spawnTile.getMapLocation(), 0);
                    }   
                    return; 
                }
            }
        }
    }

    /** Attacks nearest robot and then performs aoe attack */
    public static void attack() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            if (rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
                break;
            }
        }
        rc.attack(null);
    }

    /** Attempt to build robot of specified type on first available square */
    public static boolean buildRobotType(UnitType type) throws GameActionException {
        for (MapInfo neighborSquare : rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
            if (rc.canBuildRobot(type, neighborSquare.getMapLocation())) {
                rc.buildRobot(type, neighborSquare.getMapLocation());
                return true;
            }
        }
        return false;
    }

    // TODO: overhaul this
    public static void unitBuild() throws GameActionException {
        if (timeSinceBuilt++ < 10 ) return;

        if (rc.getRoundNum() <= 50 || rng.nextDouble() <= Math.max(0.25, .75 - (double) rc.getRoundNum() / mapHeight / mapWidth)) {
            buildRobotType(UnitType.SOLDIER);
            if (rc.getMoney() < 3000) timeSinceBuilt = 0;
        } else if (rng.nextDouble() >= .4) {
            buildRobotType(UnitType.SPLASHER);
            if (rc.getMoney() < 3000) timeSinceBuilt = 0;
        } else {
            buildRobotType(UnitType.MOPPER);
            if (rc.getMoney() < 3000) timeSinceBuilt = 0;
        }
    }

    /** Upgrade tower at robot's location */
    public static void upgradeTower() throws GameActionException {
        if (!rc.canUpgradeTower(rc.getLocation()))
            return; // can't upgrade
        if (rc.getNumberTowers() <= 5)
            return; // first wait build clock towers
        if (rc.getMoney() <= 3000)
            return; // BROKE

        rc.upgradeTower(rc.getLocation());
    }
}
