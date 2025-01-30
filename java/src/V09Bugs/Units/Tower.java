package V09Bugs.Units;

import V09Bugs.Comms;
import V09Bugs.Unit;
import V09Bugs.Tools.CommType;
import battlecode.common.*;

public class Tower extends Unit {
    static MapLocation nearestEnemyPaint, knownFrontline;
    static int lastFrontline;
    static int numSpawned = 0;
    static int numTowers = 0;
    static int roundsSinceLastDecrease = 0;

    public static void run() throws GameActionException {
        indicator = "";

        updateSurroundings();
        broadcastAndRead();

        spawn();
        attack();
        upgradeTower();
        disintegrate();
        debugDisplay();

        rc.setIndicatorString(indicator);
    }

    /// Updates tower-specific information at the start of the round
    public static void updateSurroundings() throws GameActionException {
        // Check if we've lost any towers
        if (rc.getNumberTowers() < numTowers) {
            roundsSinceLastDecrease = 0;
        } else {
            roundsSinceLastDecrease++;
        }
        numTowers = rc.getNumberTowers();

        // Check nearby enemy paint to determine frontline
        nearestEnemyPaint = null;
        int bestDist = Integer.MAX_VALUE;
        for (var tile : rc.senseNearbyMapInfos(-1)) {
            if (!tile.getPaint().isEnemy()) continue;
            var loc = tile.getMapLocation();
            var dist = rc.getLocation().distanceSquaredTo(loc);
            if (dist < bestDist) {
                bestDist = dist;
                nearestEnemyPaint = loc;
            }
        }
        if (nearestEnemyPaint != null) {
            knownFrontline = nearestEnemyPaint;
            lastFrontline = rc.getRoundNum();
        }
    }

    /// Propagates and processes incoming messages
    public static void broadcastAndRead() throws GameActionException {
        var msgs = rc.readMessages(-1);
        indicator += " received: " + msgs.length + " messages | ";
        for (var msg : msgs) {
            var code = msg.getBytes();
            switch (Comms.getType(code)) {
                case ReinforceFront -> {
                    if (rc.getRoundNum() - msg.getRound() > 1) continue; // not from this round
                    var loc = Comms.getLocation(code);
                    var round = Comms.getOriginalRound(code);
                    if (
                            knownFrontline == null
                        || (rc.getRoundNum() - lastFrontline > 5 && rc.getRoundNum() - round <= 5)
                        || (rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(knownFrontline))
                    ) {
                        knownFrontline = loc;
                        lastFrontline = round;
                    }
                }
                default -> System.out.println("Tower should not be getting message with comm code: " + code);
            }
        }

        if (knownFrontline != null) {
            if (rc.canSenseLocation(knownFrontline) && !rc.senseMapInfo(knownFrontline).getPaint().isEnemy())
                knownFrontline = null;
            if (rc.getRoundNum() - lastFrontline > 5)
                knownFrontline = null;
        }
        if (knownFrontline != null) {
            if (rc.canBroadcastMessage())
                rc.broadcastMessage(Comms.encodeMessage(
                        CommType.ReinforceFront,
                        knownFrontline,
                        lastFrontline
                ));
        }
    }

    /// Handles spawning logic per tower type
    public static void spawn() throws GameActionException {
        // the first two towers will always build 2 soldiers each
        if (rc.getRoundNum() <= 2) { 
            buildRobotType(UnitType.SOLDIER);
            return;
        }

        switch (rc.getType().getBaseType()) {
            case UnitType.LEVEL_ONE_PAINT_TOWER -> { spawnPaintTower(); }
            case UnitType.LEVEL_ONE_MONEY_TOWER -> { spawnMoneyTower(); }
            case UnitType.LEVEL_ONE_DEFENSE_TOWER -> { spawnDefenseTower(); }
            default -> { /* should never happen */ }
        }
    }

    // Attacks nearest robot and then performs aoe attack
    public static void attack() throws GameActionException {
        int minHealth = Integer.MAX_VALUE;
        MapLocation bestLocation = null;
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            //looks for a robot with the 
            if (rc.canAttack(robot.getLocation()) && robot.getHealth() < minHealth && robot.getPaintAmount() >= 5) {
                minHealth = robot.getHealth();
                bestLocation = robot.getLocation();
            }
        }
        if(bestLocation != null) rc.attack(bestLocation);

        rc.attack(null);
    }

    /// Upgrades tower
    public static void upgradeTower() throws GameActionException {
        if (!rc.canUpgradeTower(rc.getLocation()))
            return; // can't upgrade
        if (!isPaintTower(rc.getType()) && rc.getChips() < rc.getType().getNextLevel().moneyCost + 5000)
            return; // prioritize paint tower upgrades

        rc.upgradeTower(rc.getLocation());
    }

    /// Attempt to disintegrate and rebuild when the tower has been sitting
    public static void disintegrate() throws GameActionException {  
        if (rc.senseNearbyRobots(-1, opponentTeam).length > 0) return; // frontline tower, don't destroy
        if (rc.getType().getBaseType() != UnitType.LEVEL_ONE_MONEY_TOWER) return; // dont destroy paint towers?
        if (rc.getType().getNextLevel() == null && rc.getChips() < 10000) return; // might be a waste of chips
        if (nearestEnemyPaint != null  && nearestEnemyPaint.distanceSquaredTo(rc.getLocation()) <= 8) return; // obstructed
        if (roundsSinceLastDecrease <= 175) return; // lost a tower too recently
        //System.out.println("rounds since last decrease: " + roundsSinceLastDecrease);
        int numSoldiers = 0;
        for (var ally : rc.senseNearbyRobots(-1, myTeam)) {
            if (ally.getType() == UnitType.SOLDIER)
                numSoldiers++;
        }
        if (numSoldiers == 0) return;

        rc.disintegrate();
    }

    /*************
     ** HELPERS **
     *************/

    /// Core logic sequence for paint towers
    public static void spawnPaintTower() throws GameActionException {
        int nearbyMoppers = 0;
        for(RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
            if(robot.getType().equals(UnitType.MOPPER)) nearbyMoppers++;
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, opponentTeam);
        int numEnemies = enemies.length;    
        boolean isEnemySoldier = false;
        for (RobotInfo enemy : enemies) {
            if (enemy.type == UnitType.SOLDIER) {
                isEnemySoldier = true;
                break;
            }
        }

        //don't want to continue spamming units if no enemies are nearby
        if (rc.getHealth() <= 200 && numEnemies != 0) {
            buildRobotType(UnitType.SOLDIER);
            buildRobotType(UnitType.MOPPER);
            buildRobotType(UnitType.SPLASHER);
        }

        //attempts to defend the tower if num enemies is in {1,2}, less moppers than enemies, and we have sufficient health
        if(isEnemySoldier && nearbyMoppers < numEnemies && rc.getHealth() >= 500 * numEnemies) {
            buildRobotType(UnitType.MOPPER);
        }

        if(rc.getMoney() < 1200) {
            return;
        }

        UnitType type = null;
        if(rc.getRoundNum() > 100) {
            if(numSpawned % 3 == 2) {
                type = UnitType.SOLDIER;
            } else if(numSpawned % 3 == 0) {
                type = UnitType.MOPPER;
            } else if(numSpawned % 3 == 1) {
                type = UnitType.SPLASHER;
            }
        } else {
            if(numSpawned % 2 == 0) {
                type = UnitType.SOLDIER;
            } else {
                type = UnitType.MOPPER;
            }
        }

        if(buildRobotType(type) != null) numSpawned++;
    }

    /// Core logic sequence for money towers
    public static void spawnMoneyTower() throws GameActionException {
        spawnPaintTower();
        // System.out.println("MONEY TOWER SPAWN NOT IMPL");
    }
    
    /// Core logic sequence for defense towers
    public static void spawnDefenseTower() throws GameActionException {
        spawnPaintTower();
        // System.out.println("DEFENSE TOWER SPAWN NOT IMPL");
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

    /// Build unit and then send it a message
    public static void spawnAndTell(UnitType type, int msg) throws GameActionException {
        MapLocation spawnLoc = buildRobotType(type);
        if (spawnLoc != null && rc.canSendMessage(spawnLoc)) {
            rc.sendMessage(spawnLoc, msg);
        }
    }

    public static void debugDisplay() throws GameActionException {
        if (knownFrontline != null)
            rc.setIndicatorLine(rc.getLocation(), knownFrontline, 100, 50, 100);
    }
}
