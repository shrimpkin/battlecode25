package V08_Emily.Units;

import V08_Emily.Comms;
import V08_Emily.Unit;
import V08_Emily.Tools.CommType;
import V08_Emily.Tools.FastLocSet;
import battlecode.common.*;

public class Tower extends Unit {
    // all the paint towers this tower knows of:
    static FastLocSet knownPaintTowers = new FastLocSet();
    static MapLocation closestPaintTower = null;

    static UnitType towerType = rc.getType();
    static int numSoldiersSpawned = 0;
    static int numMoppersSpawned = 0;

    public static void run() throws GameActionException {
        broadcastAndRead();

        spawn();
        attack();
        upgradeTower();
        debugDisplay();

        rc.setIndicatorString(indicator);
    }

    /// Propagates and processes incoming messages
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
                    int numMoppersCalled = 0;
                    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
                        if (rc.canSendMessage(robot.location)) {
                            if (robot.type == UnitType.SOLDIER && numSoldiersCalled < 2) {
                                rc.sendMessage(robot.location, Comms.encodeMessage(CommType.RebuildTower, towerLoc));
                                numSoldiersCalled++;
                            } else if (robot.type == UnitType.MOPPER && numMoppersCalled < 1) {
                                rc.sendMessage(robot.location, Comms.encodeMessage(CommType.RebuildTower, towerLoc));
                                numMoppersCalled++;
                            }    
                        }
                    }
                    spawnAndTell(UnitType.SOLDIER, Comms.encodeMessage(CommType.RebuildTower, towerLoc));
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
        } else {
            spawnDefenseTower();
        }
    }

    /// Core logic sequence for paint towers
    public static void spawnPaintTower() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, opponentTeam);
        if (rc.getHealth() <= 100 && enemies.length > 0) {
            callNearDeathUnits(enemies);
        }

        // spawn splasher/mopper if it sees enemies
        RobotInfo[] allies = rc.senseNearbyRobots(-1, myTeam);
        if (enemies.length > 0 && allies.length == 0) {
            if (rc.senseNearbyRobots(rc.getType().actionRadiusSquared, opponentTeam).length > 0) {
                spawnAndTell(UnitType.SPLASHER, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location));
            } else {
                spawnAndTell(UnitType.MOPPER, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location));
            }
        } else if (enemies.length > 0 && allies.length > 0) {
            int called = 0;
            for (RobotInfo ally : allies) {
                if (ally.type == UnitType.MOPPER) {
                    if (rc.canSendMessage(ally.location, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location))) {
                        rc.sendMessage(ally.location, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location));
                        called++;

                        if (called >= 2) {
                            break;
                        }
                    }
                }
            }
        }

        // will always spawn the soldier at the earliest convenience
        if (numSoldiersSpawned <= 3) {
            buildRobotType(UnitType.SOLDIER);
            return;
        }

        // spawn super early mopper
        if (numSoldiersSpawned == 4 & numMoppersSpawned == 0) {
            buildRobotType(UnitType.MOPPER);
            return;
        }
        
        // consistently spawn soldiers for a few rounds
        if (rc.getNumberTowers() <= 4 || numSoldiersSpawned <= (8 * mapHeight * mapWidth / 3600.)) {
            buildRobotType(UnitType.SOLDIER);
            return;
        }

        // end game -- spam splashers
        if (rc.getChips() > 100000) {
            if (nextDouble() < 0.95) {
                spawnOffense();
            } else {
                buildRobotType(UnitType.SOLDIER);
            }
            return;
        }

        // mid game -- alternate between splasher/mopper, and 1/10th soldiers
        if (rc.getChips() > 1000 && allies.length < 8) {
            if (nextDouble() < 0.9) {
                spawnOffense();
            } else {
                buildRobotType(UnitType.SOLDIER);
            }
        }
    }

    /// Core logic sequence for money towers
    public static void spawnMoneyTower() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, opponentTeam);
        if (rc.getHealth() <= 100 && enemies.length > 0) {
            callNearDeathUnits(enemies);
        }

        int defenseUnits = 0;
        for (RobotInfo ally : rc.senseNearbyRobots(-1, myTeam)) {
            if (ally.getType() == UnitType.SPLASHER || ally.getType() == UnitType.MOPPER) {
                defenseUnits++;
            }
        }

        if (defenseUnits == 0 && enemies.length > 0) { // spawn defensive units
            spawnAndTell(UnitType.SPLASHER, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location));
            spawnAndTell(UnitType.MOPPER, Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location));
        }
    }
    
    /// Core logic sequence for defense towers (NOT USED OFTEN)
    public static void spawnDefenseTower() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, opponentTeam);
        if (rc.getHealth() <= 100 && enemies.length > 0) {
            callNearDeathUnits(enemies);
        } else if (enemies.length >= 5) {
            buildRobotType(UnitType.SPLASHER);
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

    /// Upgrade tower at robot's location
    public static void upgradeTower() throws GameActionException {
        // immediately upgrade defense towers
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

    /// Signal and spawn units when tower health is >= 100
    public static void callNearDeathUnits(RobotInfo[] enemies) throws GameActionException {
        // check nearby territory covered
        int territoryCovered = 0;
        for (MapInfo tile : rc.senseNearbyMapInfos(rc.getType().actionRadiusSquared)) {
            if (tile.getPaint().isEnemy()) {
                territoryCovered++;
            }
        }
 
        // check if we need moppers/splashers to clean up paint
        boolean callMopper = false;
        boolean callSplasher = false;

        if (territoryCovered != 0) {
            if (territoryCovered > 4) {
                callSplasher = true;
            } else {
                callMopper = true;
            }
        }
        
        // call and build soldiers
        int numSoldiersCalled = 0;
        int rebuildMsg = Comms.encodeMessage(CommType.RebuildTower, rc.getLocation());
        int targetMsg = Comms.encodeMessage(CommType.TargetEnemy, enemies[0].location);

        for (RobotInfo ally : rc.senseNearbyRobots(-1, myTeam)) {
            switch (ally.getType()) {
                case UnitType.SOLDIER -> {
                    if (numSoldiersCalled < 2 && rc.canSendMessage(ally.getLocation())) {
                        rc.sendMessage(ally.getLocation(), rebuildMsg);
                    }
                }
                case UnitType.MOPPER -> {
                    if (callMopper && rc.canSendMessage(ally.getLocation())) {
                        rc.sendMessage(ally.getLocation(), rebuildMsg);
                        callMopper = false;
                    }
                }
                case UnitType.SPLASHER -> {
                    if (callSplasher && rc.canSendMessage(ally.getLocation())) {
                        rc.sendMessage(ally.getLocation(), targetMsg);
                        callSplasher = false;
                    }
                }
                default -> {
                }
            }
        }

        // try to spawn any units if it has paint and request for help
        spawnAndTell(UnitType.SPLASHER, targetMsg);
        spawnAndTell(UnitType.MOPPER, targetMsg);
        spawnAndTell(UnitType.SOLDIER, rebuildMsg);

        // broadcast for backup soldiers if none are nearby
        if (numSoldiersCalled < 2 && rc.canBroadcastMessage()) {
            rc.broadcastMessage(Comms.encodeMessage(CommType.RequestSoldiers, rc.getLocation()));
        }
    }

    /// Probabilistically spawn splashers and moppers
    public static void spawnOffense() throws GameActionException {
        if (nextDouble() < 0.7) {
            buildRobotType(UnitType.SPLASHER);
        } else {
            if (rc.getChips() > 300 && rc.getPaint() > 300) {
                buildRobotType(UnitType.MOPPER);
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

    /// Build unit and then send it a message
    public static void spawnAndTell(UnitType type, int msg) throws GameActionException {
        MapLocation spawnLoc = buildRobotType(type);
        if (spawnLoc != null && rc.canSendMessage(spawnLoc)) {
            rc.sendMessage(spawnLoc, msg);
        }
    }

    public static void debugDisplay() throws GameActionException {
        for (var loc : knownPaintTowers.getKeys()) {
            rc.setIndicatorDot(loc, 255, 165, 255);
        }
    }
}
