package V05tower.Units;

import V05tower.Comms;
import V05tower.Unit;
import battlecode.common.*;

public class Tower extends Unit {
    static private enum Modes {NONE, NEW, STABLE, UNDER_ATTACK, NEAR_DEATH};

    static int timeSinceBuilt = 0;
    static int timeSinceAttacked = 0;
    static int timeSinceLastSoldier = 0;

    static boolean spawnSplasherFirst = true;
    static private Modes mode;

    public static void run() throws GameActionException {
        indicator = "";
        updateMode();

        indicator += "[" + mode + "] ";
        
        spawn();
        attack();
        upgradeTower();

        rc.setIndicatorString(indicator);
    }

    /** Determines if the tower is under attack or not */
    public static void updateMode() throws GameActionException {
        timeSinceBuilt++;
        timeSinceAttacked++;

        if (mode == Modes.NEAR_DEATH)
            return; // keep spawn patterns when the tower is near death

        if (isUnderAttack())
            return; // if true, mode is determined by health of tower

        if (timeSinceAttacked > 150) {
            mode = Modes.STABLE; // backline tower that is not threatened
        } else {
            if (timeSinceBuilt <= 75) {
                mode = Modes.NEW;
            } else {
                mode = Modes.NONE; // not new, not under attack, but was recently attacked
            }
        }

        indicator = timeSinceAttacked + "; ";
    }

    /** Determines what units to spawn based on current mode */
    public static void spawn() throws GameActionException {
        System.out.println(((60 - mapHeight + 60 - mapWidth) / 2));
        if (mode == Modes.NEW || timeSinceBuilt < ((60 - mapHeight + 60 - mapWidth) / 2)) {
            buildRobotType(UnitType.SOLDIER);
            buildRobotType(UnitType.SOLDIER);
            return;
        }

        if (mode == Modes.NEAR_DEATH) {
            if (timeSinceAttacked < 2) {
                buildRobotType(UnitType.SOLDIER);
                buildRobotType(UnitType.SOLDIER);
            } 
        }

        if (mode == Modes.UNDER_ATTACK) {
            if (rc.getRoundNum() % rc.getNumberTowers() == 0) {
                buildRobotType(UnitType.SOLDIER);
            } else {
                spawnDefense();
            }
        }

        if (mode == Modes.NONE || mode == Modes.STABLE) {
            spawnDefense();

            if (rc.getRoundNum() % 20 == 0) {
                spawnOffense();
            } 
        }
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
        // if (rc.getRoundNum() < 200) {
        //     if (rc.getRoundNum() % 75 != 0 || rc.getChips() < 1000) 
        //         return; // only launch offense every 100 rounds in early game
        // } else {
        //     if (rc.getRoundNum() % 10 != 0 || rc.getChips() < 1000) 
        //         return; // launch offense every 20 rounds in mid/late game
        // }

        if (spawnSplasherFirst) {
            buildRobotType(UnitType.SPLASHER);
            buildRobotType(UnitType.MOPPER);
        } else {
            buildRobotType(UnitType.MOPPER);
            buildRobotType(UnitType.SPLASHER);
        }
        spawnSplasherFirst = !spawnSplasherFirst;
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

    /** Send a message to the specified location, telling it to go to a target location */
    public static void tell(MapLocation loc, MapLocation target) throws GameActionException {
        // if (rc.canSendMessage(loc)) {
        //     rc.sendMessage(loc, Comms.encodeMsg(target.x, target.y));
        // }
    }

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

            int msg = Comms.encodeMsg(rc.getLocation().x, rc.getLocation().y);
            int calledMoppers = 2;

            for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                if (robot.type == UnitType.MOPPER) {
                    if (rc.canSendMessage(robot.location)) {
                        rc.sendMessage(robot.location, msg);
                        calledMoppers--;
                        
                        if (calledMoppers == 0)
                            return; // called enough moppers home
                    }    
                }
            }
        
        } else { // spawn new defensive moppers
            indicator += "want new; ";

            int newMoppers = Math.max(0, (int) Math.round(numEnemies / 3.0));
            for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) { 
                int msg = Comms.encodeMsg(robot.getLocation().x, robot.getLocation().y);

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

    /** Checks if there are any enemy units nearby and resets attack timer */
    public static boolean isUnderAttack() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, opponentTeam);
        if (nearbyEnemies.length > 0) {
            timeSinceAttacked = 0;
            mode = Modes.UNDER_ATTACK;

            if (rc.getHealth() <= 100) {
                mode = Modes.NEAR_DEATH;
            }
            return true;
        }
        return false;
    }

    /** Attempt to build robot of specified type on first available square */
    public static MapLocation buildRobotType(UnitType type) throws GameActionException {
        for (MapInfo neighborSquare : rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
            if (rc.canBuildRobot(type, neighborSquare.getMapLocation())) {
                rc.buildRobot(type, neighborSquare.getMapLocation());
                return neighborSquare.getMapLocation();
            }
        }
        return null;
    }
}
