package V08tower.Units;

import V08tower.Comms;
import V08tower.Unit;
import V08tower.Tools.CommType;
import V08tower.Tools.FastLocSet;
import battlecode.common.*;

public class Tower extends Unit {
    static int timeSinceBuilt = 0;
    static int timeSinceAttacked = 0;
    // mode multipliers
    static double tileMultiplier = ((double) mapHeight * mapWidth) / (3600.0 - 400.0); // 400-3600
    static int minNEW = 5, maxNEW = 25;
    static int minSTABLE = 50, maxSTABLE = 250;
    static boolean spawnSplasherFirst = true;
    // all the paint towers this tower knows of:
    static FastLocSet knownPaintTowers = new FastLocSet();
    static MapLocation closestPaintTower = null;
    static private Modes mode;

    public static void run() throws GameActionException {
        indicator = "";
        updateMode();
        indicator += "canbroadcast: " +  rc.canBroadcastMessage() + " ";
        broadcastAndRead();
        indicator += "[" + mode + "] ";

        spawn();
        attack();
        upgradeTower();
        debugDisplay();
        rc.setIndicatorString(indicator);
    }

    /// Determines if the tower is under attack or not
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

    /// Determines what units to spawn based on current mode
    public static void spawn() throws GameActionException {
        if (rc.senseNearbyRobots(-1, rc.getTeam()).length > 8) 
            return; // prevents crowding

        if (timeSinceBuilt <= (maxNEW - minNEW) * tileMultiplier) { // upon tower spawn
            if (rc.senseNearbyRobots(-1, rc.getTeam()).length < 2) {
                buildRobotType(UnitType.SOLDIER);
            }
            return;
        }

        switch (mode) {
            case Modes.NEAR_DEATH -> {
                if (rc.getHealth() <= 100 && rc.canBroadcastMessage()) {
                    buildRobotType(UnitType.SOLDIER);
                    rc.broadcastMessage(Comms.encodeMessage(CommType.RequestSoldiers, rc.getLocation()));
                }
            }

            case Modes.UNDER_ATTACK -> { 
                // womp womp womp 
            }

            case Modes.NONE -> {
                if (rng.nextDouble() >= rc.getHealth() / rc.getType().health / 10) {
                    buildRobotType(UnitType.SOLDIER);
                } else {
                    spawnOffense();
                }
            }

            case Modes.STABLE -> {
                spawnOffense();
            }
        }
    }

    /// Attacks nearest robot and then performs aoe attack
    public static void attack() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            if (rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
                break;
            }
        }
        rc.attack(null);
    }

    /// 
    public static void broadcastAndRead() throws GameActionException {
        var msgs = rc.readMessages(-1);
        indicator += " received: " + msgs.length + " messages | ";
        int messagesSent = 0;
        for (var msg : msgs) {
            var code = msg.getBytes();
            var sender = msg.getSenderID();
            switch (Comms.getType(code)) {
                case CommunicateType -> { // add paint towers to the known tower rolls
                    var unitType = Comms.getUnitType(code);
                    if (unitType.getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                        var loc = Comms.getLocation(code);
                        knownPaintTowers.add(loc);
                    }
                }
                case RequestPaintTower -> { // try to send all paint towers this one knows about to the robot
                    if (rc.getRoundNum() - msg.getRound() > 1) continue;
                    if (rc.canSenseRobot(sender)) {
                        var senderBot = rc.senseRobot(sender);
                        if (rc.canSendMessage(senderBot.location)) {
                            for (var tower : knownPaintTowers.getKeys()) {
                                if (messagesSent > 12) break;
                                rc.sendMessage(
                                        senderBot.location,
                                        Comms.encodeMessage(
                                                CommType.NearbyPaintTower,
                                                tower
                                        )
                                );
                                messagesSent++;
                            }
                        }
                    }
                }
                case RequestSoldiers -> {
                    if (!isPaintTower(rc.getType()))
                        continue; // only paint towers should spawn backup

                    var towerLoc = Comms.getLocation(code);

                    // request nearby soldiers
                    int numSoldiersCalled = 0;
                    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                        if (robot.type == UnitType.SOLDIER) {
                            if (rc.canSendMessage(robot.location)) {
                                rc.sendMessage(robot.location, Comms.encodeMessage(CommType.RebuildTower, towerLoc));
                                numSoldiersCalled++;

                                if (numSoldiersCalled == 2)
                                    return; // called enough
                            }    
                        }
                    }

                    // build new soldiers
                    MapLocation buildLoc = buildRobotType(UnitType.SOLDIER);
                    if (buildLoc != null && rc.canSendMessage(buildLoc)) {
                        rc.sendMessage(buildLoc, Comms.encodeMessage(CommType.RebuildTower, towerLoc));
                    }
                }
                // TODO: reinforceFront messaging
                default -> System.out.println("Tower should not be getting message with comm code: " + code);
            }
        }

        // inform other towers of current tower's type -- as well as of known paint tower locations
        if (rc.canBroadcastMessage()) rc.broadcastMessage(Comms.encodeMessage(rc.getType(), rc.getLocation()));
        if (rc.getRoundNum() % 5 == 0) { // broadcast known paint towers occasionally
            for (var tower : knownPaintTowers.getKeys()) {
                if (rc.canBroadcastMessage()) {
                    rc.broadcastMessage(Comms.encodeMessage(UnitType.LEVEL_ONE_PAINT_TOWER, tower));
                }
            }
        }
    }

    /// Upgrade tower at robot's location
    public static void upgradeTower() throws GameActionException {
        if (!rc.canUpgradeTower(rc.getLocation()))
            return; // can't upgrade
        if (!isPaintTower(rc.getType()) || rc.getChips() < rc.getType().getNextLevel().moneyCost + 200)
            return; // prioritize paint tower upgrades

        rc.upgradeTower(rc.getLocation());
    }

    /*************
     ** HELPERS **
     *************/

    /// Spawn or call units in reaction to surrounding enemies and paint
    public static void spawnDefense() throws GameActionException {
        spawnDefenseMopper();
        spawnDefenseSplasher();
    }

    /// Spawns moppers based on presence of enemy units
    public static void spawnDefenseMopper() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
        int numMoppers = 0, numEnemies = 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.team == opponentTeam) {
                numEnemies++;
            } else if (robot.type == UnitType.MOPPER) { // allied moppers only
                numMoppers++;
            }
        }
        if (numEnemies == 0) return; // no enemies; no need to spawn moppers

        if (numMoppers > numEnemies) { // tell nearby moppers to come back
            indicator += "want return; ";
            int msg = Comms.encodeMessage(CommType.WantDefenders, rc.getLocation());
            int calledMoppers = 2;

            for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                if (robot.type == UnitType.MOPPER && rc.canSendMessage(robot.location)) {
                    rc.sendMessage(robot.location, msg);
                    calledMoppers--;
                    if (calledMoppers == 0) return; // called enough moppers home
                }
            }
        } else { // spawn new defensive moppers
            indicator += "want new; ";

            int newMoppers = Math.max(0, (int) Math.round(numEnemies / 3.0));
            for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
                int msg = Comms.encodeMessage(CommType.TargetEnemy, robot.getLocation());
                for (MapInfo spawnTile : rc.senseNearbyMapInfos(robot.getLocation(), GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
                    if (rc.canBuildRobot(UnitType.MOPPER, spawnTile.getMapLocation())) {
                        indicator += "spawn; ";
                        rc.buildRobot(UnitType.MOPPER, spawnTile.getMapLocation());
                        if (rc.canSendMessage(spawnTile.getMapLocation())) {
                            rc.sendMessage(spawnTile.getMapLocation(), msg);
                        }
                        newMoppers--;
                        if (newMoppers == 0) return; // spawned all moppers
                    }
                }
            }
        }

    }

    /// Spawns splashers based on presence of enemy paint
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

    /// Alternate spawning moppers and splashers
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

    /// Checks if there are any enemy units nearby and resets attack timer
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

    /// Attempt to build robot of specified type on first available square
    public static MapLocation buildRobotType(UnitType type) throws GameActionException {
        for (MapInfo neighborSquare : rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
            if (rc.canBuildRobot(type, neighborSquare.getMapLocation())) {
                rc.buildRobot(type, neighborSquare.getMapLocation());
                return neighborSquare.getMapLocation();
            }
        }
        return null;
    }

    public static void debugDisplay() throws GameActionException {
        for (var loc : knownPaintTowers.getKeys()) {
            rc.setIndicatorDot(loc, 255, 165, 255);
        }
    }


    static private enum Modes {NONE, STABLE, UNDER_ATTACK, NEAR_DEATH}
}
