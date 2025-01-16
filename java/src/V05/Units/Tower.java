package V05.Units;

import V05.Comms;
import V05.Unit;
import battlecode.common.*;

public class Tower extends Unit {
    static int timeSinceBuilt = 20;

    public static void run() throws GameActionException {
        indicator = "";
        
        if (rc.getRoundNum() <= 75 || rng.nextDouble() <= Math.max(0.25, .75 - (double) rc.getRoundNum() / mapHeight / mapWidth)) {
            buildRobotType(UnitType.SOLDIER);
        } else {
            spawnDefense();
        }

        attack();
        spawnOffense();
        upgradeTower();

        rc.setIndicatorString(indicator);
    }

    public static void spawnDefense() throws GameActionException {
        spawnDefenseMopper();
        spawnDefenseSplasher();
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

    public static void spawnOffense() throws GameActionException {
        if (rc.getRoundNum() % 100 != 0) 
            return; // only launch offense every 100 rounds

        buildRobotType(UnitType.SPLASHER);
        buildRobotType(UnitType.MOPPER);
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

    /** Spawns moppers based on presence of enemy units */
    public static void spawnDefenseMopper() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
        int numMoppers = 0;
        int numEnemies = 0;

        for (RobotInfo robot : nearbyRobots) {
            if (robot.team == opponentTeam) {
                numEnemies++;
            } else if (robot.type == UnitType.MOPPER) { // ally moppers only
                numMoppers++;
            }
        }

        if (numEnemies == 0)
            return; // no enemies; no need to spawn moppers
        
        if (numMoppers > numEnemies) { // tell nearby moppers to come back
            indicator += "want return; ";

            int msg = Comms.encodeMsg(rc.getLocation().x, rc.getLocation().y, rc.getLocation().x, rc.getLocation().y);

            for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                if (robot.type == UnitType.MOPPER) {
                    if (rc.canSendMessage(robot.location)) {
                        rc.sendMessage(robot.location, msg);
                    }    
                }
            }
        
        } else { // spawn new defensive moppers
            indicator += "want new; ";

            int newMoppers = Math.max(1, (int) Math.round(numEnemies / 3.0));
            for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) { 
                int msg = Comms.encodeMsg(rc.getLocation().x, rc.getLocation().y, robot.getLocation().x, robot.getLocation().y);

                for (MapInfo spawnTile : rc.senseNearbyMapInfos(robot.getLocation(), 2)) {
                    if (rc.canBuildRobot(UnitType.MOPPER, spawnTile.getMapLocation())) {
                        indicator += "spawn; ";

                        rc.buildRobot(UnitType.MOPPER, spawnTile.getMapLocation());
                        if (rc.canSendMessage(spawnTile.getMapLocation())) {
                            rc.sendMessage(spawnTile.getMapLocation(), msg);
                        }

                        newMoppers--;
                        if (newMoppers == 0) {
                            return; // spawned all moppers
                        }
                    }
                }
            }
        }

    }

    /** Spawns splashers based on presence of enemy paint */
    public static void spawnDefenseSplasher() throws GameActionException {
        if (rc.getRoundNum() < 100)
            return; // too early in the game to spawn defense splashers?

        int numEnemyPaint = 0;
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED);
        for (MapInfo tile : nearbyTiles) {
            if (tile.getPaint().isEnemy()) {
                numEnemyPaint++;
            }
        }

        if ((double) numEnemyPaint / nearbyTiles.length > 0.4) {
            if (rc.canBuildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.EAST))) {
                rc.buildRobot(UnitType.SPLASHER, rc.getLocation().add(Direction.EAST));
            }
        }
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
}
