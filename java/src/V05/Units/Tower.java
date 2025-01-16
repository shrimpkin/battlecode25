package V05.Units;

import V05.Comms;
import V05.Unit;
import battlecode.common.*;

public class Tower extends Unit {
    static int timeSinceBuilt = 20;

    public static void run() throws GameActionException {
        indicator = "";

        defend();
        unitBuild();
        attack();
        upgradeTower();

        rc.setIndicatorString(indicator);
    }

    /** Checks if there are enemy units nearby, and spawns defensive moppers if so */
    // check
    // 1. presense of enemy robots 
    // TODO: 2. presense of enemy paint
    // spawn moppers when either one gets too high
    public static void defend() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
        int numMoppers = 0;
        int numEnemies = 0;

        for (RobotInfo robot : nearbyRobots) {
            if (robot.type == UnitType.MOPPER) {
                numMoppers++;
            }
            if (robot.team == opponentTeam) {
                numEnemies++;
            }
        }

        if (numEnemies == 0) {
            return;
        } else {
            indicator += "saw enemy; ";
        }

        if (numMoppers > numEnemies) { // tell nearby moppers to come back
            indicator += "sharing return msg; ";
            int msg = Comms.encodeMsg(UnitType.MOPPER, rc.getLocation().x, rc.getLocation().y);

            for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                if (rc.canSendMessage(robot.location)) {
                    rc.sendMessage(robot.location, msg);
                }
            }   

            rc.broadcastMessage(msg);
        
        } else { // spawn new defensive moppers
            int newMoppers = Math.max(1, (int) Math.round(numEnemies / 3.0));
            
            // TODO: maybe rewrite this
            for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {    
                for (MapInfo spawnTile : rc.senseNearbyMapInfos(robot.getLocation(), 2)) {
                    if (rc.canBuildRobot(UnitType.MOPPER, spawnTile.getMapLocation())) {
                        indicator += "spawn defense mopper; ";
                        rc.buildRobot(UnitType.MOPPER, spawnTile.getMapLocation());
    
                        if (rc.canSendMessage(spawnTile.getMapLocation())) {
                            indicator += "sent msg; ";
                            // rc.sendMessage(spawnTile.getMapLocation(), 0);
                        }   

                        newMoppers--;
                        if (newMoppers == 0)
                            return; // spawned all moppers
                    }
                }
            }
        }
    }

    // TODO: overhaul this
    public static void unitBuild() throws GameActionException {
        if (timeSinceBuilt++ < 5) return;

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

    /*************
     ** HELPERS **
     *************/

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
}
