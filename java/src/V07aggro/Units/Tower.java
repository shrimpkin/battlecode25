package V07aggro.Units;

import V07aggro.Comms;
import V07aggro.Unit;
import battlecode.common.*;

public class Tower extends Unit {
    static private enum Modes {NONE, STABLE, UNDER_ATTACK, NEAR_DEATH};

    static int timeSinceBuilt = 0;
    static int timeSinceAttacked = 0;

    // mode multipliers
    static double tileMultiplier = ((double) mapHeight * mapWidth) / (3600.0 - 400.0); // 400-3600
    static int minNEW = 5, maxNEW = 25;
    static int minSTABLE = 50, maxSTABLE = 250;

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

        if (isUnderAttack())
            return; // if true, mode is determined by health of tower
        
        if (timeSinceAttacked > (maxSTABLE - minSTABLE) * tileMultiplier) {
            mode = Modes.STABLE; // backline tower that is not threatened
        } else {
            mode = Modes.NONE; // not new, not under attack, but was recently attacked
        }
    }

    /** Determines what units to spawn based on current mode */
    public static void spawn() throws GameActionException {
        if (timeSinceBuilt <= (maxNEW - minNEW) * tileMultiplier) {
            buildRobotType(UnitType.SOLDIER);
            // TODO: maybe build early moppers to get rid of paint marks on ruins
            // need to build moppers with the intent of
            // - staying on ally territory
            // - prioritizing removing enemy paint
        }

        switch (mode) {
            case Modes.NEAR_DEATH:
                if (timeSinceAttacked < 5) {
                    buildRobotType(UnitType.SOLDIER); // for rebuilding
                } else {
                    spawnDefense();
                }
                break;

            case Modes.UNDER_ATTACK:
                buildRobotType(UnitType.SOLDIER);
                spawnDefense();
                break;

            case Modes.NONE:
                if (rng.nextDouble() >= rc.getHealth() / 1000 / 10) {
                    buildRobotType(UnitType.SOLDIER);
                } else {
                    spawnOffense();
                }
                break;

            case Modes.STABLE:
                if (rng.nextDouble() <= rc.getHealth() / 1000) {
                    spawnOffense();
                } else {
                    buildRobotType(UnitType.SOLDIER);
                }
                break;
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
        if (!isPaintTower(rc.getType()))
            return; // only upgrade paint towers
        if (!rc.canUpgradeTower(rc.getLocation()))
            return; // can't upgrade
        if (rc.getMoney() <= 2000)
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

    /** Spawn or call units in reaction to surrounding enemies and paint */
    public static void spawnDefense() throws GameActionException {
        spawnDefenseMopper();
        spawnDefenseSplasher();
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
        int numEnemyPaint = 0;
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED);
        for (MapInfo tile : nearbyTiles) {
            if (tile.getPaint().isEnemy() || tile.getPaint() == PaintType.EMPTY) {
                numEnemyPaint++;
            }
        }

        if ((double) numEnemyPaint / nearbyTiles.length > 0.4) {
            buildRobotType(UnitType.SPLASHER);
        }
    }

    /** Alternate spawning moppers and splashers  */
    public static void spawnOffense() throws GameActionException {
        if (spawnSplasherFirst) {
            if (buildRobotType(UnitType.MOPPER) != null) {
                spawnSplasherFirst = !spawnSplasherFirst;
            }
        } else {
            if (buildRobotType(UnitType.SPLASHER) != null) {
                spawnSplasherFirst = !spawnSplasherFirst;
            }
        }
    }

    /** Checks if there are any enemy units nearby and resets attack timer */
    public static boolean isUnderAttack() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, opponentTeam);
        if (nearbyEnemies.length > 0) {
            timeSinceAttacked = 0;
            mode = Modes.UNDER_ATTACK;

            if (rc.getHealth() <= 200) {
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
