package V08tower.Units;

import V08tower.Comms;
import V08tower.Unit;
import V08tower.Tools.CommType;
import V08tower.Tools.FastLocSet;
import battlecode.common.*;

public class Tower extends Unit {
    static int timeSinceBuilt = 0;
    static int timeSinceAttacked = 0;

    static boolean spawnSplasherFirst = true;
    // all the paint towers this tower knows of:
    static FastLocSet knownPaintTowers = new FastLocSet();
    static MapLocation closestPaintTower = null;

    static UnitType towerType = rc.getType();
    static int numSoldiersSpawned = 0;
    static int numMoppersSpawned = 0;

    public static void run() throws GameActionException {
        indicator = "canbroadcast: " +  rc.canBroadcastMessage() + " ";;
        broadcastAndRead();

        spawn();
        attack();
        upgradeTower();
        debugDisplay();
        rc.setIndicatorString(indicator);
    }

    /// Handles spawning logic
    public static void spawn() throws GameActionException {
        if (rc.getRoundNum() <= 2) { // the first two towers will always build 2 soldiers each
            buildRobotType(UnitType.SOLDIER);
            return;
        }

        if (isPaintTower(towerType)) {
            spawnPaintTower();
        } else if (isMoneyTower(towerType)) {
            spawnMoneyTower();
        }
    }

    /// Core logic sequence for paint towers
    public static void spawnPaintTower() throws GameActionException {
        if (numSoldiersSpawned <= 3) {
            buildRobotType(UnitType.SOLDIER); // will always spawn the soldier at the earliest convenience
        }

        if (numSoldiersSpawned == 4 && numMoppersSpawned == 0) {
            buildRobotType(UnitType.MOPPER);
        }

        // spawn splasher if it sees enemies
        if (rc.senseNearbyRobots(-1, opponentTeam).length > 0) {
            if (rc.senseNearbyRobots(-1, myTeam).length == 0 && rc.senseNearbyRobots(rc.getType().actionRadiusSquared, opponentTeam).length > 0) {
                buildRobotType(UnitType.SPLASHER);
            } else if (rc.senseNearbyRobots(-1, myTeam).length == 0) {
                buildRobotType(UnitType.MOPPER);
            }
        }

        // consistently spawn soldiers for a few rounds
        if (numSoldiersSpawned <= 10) {
            buildRobotType(UnitType.SOLDIER);
        }

        // end game - spam splashers?
        if (rc.getRoundNum() > 1500) {
            buildRobotType(UnitType.SPLASHER);
        }

        // mid game - alternate between splasher/mopper, and 1/10th soldiers
        if (rc.getChips() > 1000) {
            if (rc.getRoundNum() % 5 == 0) {
                if (nextDouble() < 0.9) {
                    spawnOffense();
                } else {
                    buildRobotType(UnitType.SOLDIER);
                }
            }
        }
    }

    /// Core logic sequence for money towers
    public static void spawnMoneyTower() throws GameActionException {
        if (rc.senseNearbyRobots(-1, myTeam).length == 0 && rc.senseNearbyRobots(-1, opponentTeam).length > 0) {
            if (rc.getHealth() <= 100 && rc.canBroadcastMessage()) {
                buildRobotType(UnitType.SOLDIER);
                rc.broadcastMessage(Comms.encodeMessage(CommType.RequestSoldiers, rc.getLocation()));
            }

            buildRobotType(UnitType.SPLASHER);
            buildRobotType(UnitType.MOPPER); // this only runs if splasher fails?
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
                    if (rc.getRoundNum() - msg.getRound() > 1) 
                        continue; // not from this round
                    if (!isPaintTower(rc.getType())) {
                        // propagate
                        if (rc.getRoundNum() - Comms.getOriginalRound(msg.getBytes()) < 3) {
                            if (rc.canBroadcastMessage()) {
                                rc.broadcastMessage(msg.getBytes());
                            }
                        }
                        continue; // only paint towers should spawn backup
                    }

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

        for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
            if (rc.canSendMessage(robot.getLocation()) && closestPaintTower != null) {
                rc.sendMessage(robot.getLocation(), Comms.encodeMessage(CommType.NearbyPaintTower, closestPaintTower));
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
        if (rc.getType() == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
            if (rc.canUpgradeTower(rc.getLocation())) {
                rc.upgradeTower(rc.getLocation());
            }
            return;
        }

        if (!rc.canUpgradeTower(rc.getLocation()))
            return; // can't upgrade
        if (!isPaintTower(rc.getType()) && rc.getChips() < rc.getType().getNextLevel().moneyCost + 200)
            return; // prioritize paint tower upgrades

        rc.upgradeTower(rc.getLocation());
    }

    /*************
     ** HELPERS **
     *************/

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

    /// Attempt to build robot of specified type on first available square
    public static MapLocation buildRobotType(UnitType type) throws GameActionException {
        for (MapInfo neighborSquare : rc.senseNearbyMapInfos(GameConstants.BUILD_ROBOT_RADIUS_SQUARED)) {
            if (rc.canBuildRobot(type, neighborSquare.getMapLocation())) {
                rc.buildRobot(type, neighborSquare.getMapLocation());
                if (rc.canSendMessage(neighborSquare.getMapLocation()) && closestPaintTower != null) {
                    rc.sendMessage(neighborSquare.getMapLocation(), Comms.encodeMessage(CommType.NearbyPaintTower, closestPaintTower));
                }

                if (type == UnitType.SOLDIER) {
                    numSoldiersSpawned++;
                } else if (type == UnitType.MOPPER) {
                    numMoppersSpawned++;
                }

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
}
