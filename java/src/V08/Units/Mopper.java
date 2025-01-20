package V08.Units;

import V08.Unit;
import V08.Micro.MopperMicro;
import V08.Nav.Navigator;
import V08.Tools.FastLocSet;
import battlecode.common.*;

public class Mopper extends Unit {
    static FastLocSet enemyTowers = new FastLocSet(), enemyRobots = new FastLocSet();
    static FastLocSet enemyPaint = new FastLocSet(), allyPaint = new FastLocSet();
    static FastLocSet allies = new FastLocSet(), emptyRuins = new FastLocSet();
    static FastLocSet enemyMarks = new FastLocSet();
    /// records allyPaint - enemyPaint
    static int paintBalance;
    static MapLocation avgMe, avgEnemy;
    static Message[] messages;
    static final Direction[] cardinal = Direction.cardinalDirections();
    private static boolean wasWandering = false;

    static MopperMicro micro = new MopperMicro();

    public static void run() throws GameActionException {
        indicator = "";
        updateTowerLocations();
        updateSurroundings();
        micro.computeMicroArray(true, paintTowerLocations, enemyPaint, allyPaint);
        messages = rc.readMessages(-1);
        move();
        doAction();
        refill();
        debug();
    }

    public static void move() throws GameActionException {
        if (rc.getPaint() < 40 && paintTowerLocations.size > 0) {
            Navigator.moveTo(getClosestLocation(paintTowerLocations));
            wasWandering = false;
        } else if (micro.doMicro()) {
            indicator += "{did micro}";
            wasWandering = false;
        } else if (rc.isMovementReady()) {
            wander(wasWandering);
            wasWandering = true;
        }
    }

    public static void doAction() throws GameActionException {
        if (!rc.isActionReady()) return;
        indicator += "[doing action: ";
        var swingDir = getBestMopSwingDir();
        // decide between swinging mop and painting a tile -- could be more thorough with conditions here
        if (swingDir != -1) {
            var dir = cardinal[swingDir];
            rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dir), 100, 100, 100);
            rc.mopSwing(dir);
            indicator += "swung]";
        } else {
            // get best tile to paint
            MapLocation paintTile = null;
            var nearbyRuins = rc.senseNearbyRuins(-1);
            int importance = 0;
            for (var tile : rc.senseNearbyMapInfos(2)) {
                if (!tile.getPaint().isEnemy() || !tile.isPassable()) continue;
                // save ourselves from enemy penalty
                var loc = tile.getMapLocation();
                if (!rc.canAttack(loc)) continue;
                rc.setIndicatorDot(loc, 0,0, 0);
                if (loc == rc.getLocation()) {
                    paintTile = loc;
                    importance = 6;
                    break;
                }
                // put enemies moppers off safe ground
                if (importance < 5 && rc.canSenseRobotAtLocation(loc) && rc.senseRobotAtLocation(loc).getType() == UnitType.MOPPER) {
                    paintTile = loc;
                    importance = 5;
                }
                // put enemies of other types off safe ground
                if (importance < 4 && rc.canSenseRobotAtLocation(loc)) {
                    paintTile = loc;
                    importance = 4;
                }
                // if they marked it, it should be important
                if (importance < 3 && tile.getMark().isEnemy()) {
                    paintTile = loc;
                    importance = 3;
                }
                // clear enemy paint around a ruin
                if (importance < 2) {
                    for (var ruin : nearbyRuins) {
                        if (ruin.isWithinDistanceSquared(loc, 8) && !rc.canSenseRobotAtLocation(ruin)) {
                            paintTile = loc;
                            importance = 2;
                            break;
                        }
                    }
                }
                if (importance < 1)
                    paintTile = loc;
            }
            if (paintTile != null && rc.canAttack(paintTile)) {
                rc.attack(paintTile);
                rc.setIndicatorDot(paintTile, 220, 250, 100);
                indicator += "painted]";
            } else if (rc.getPaint() > 51){
                // can't paint any tile -- try to transfer paint (maybe? idk if this is good in current scheme?)
                var allies = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, rc.getTeam());
                var amount = rc.getPaint() - 51;
                for (var robot : allies) {
                    var loc = robot.getLocation();
                    if (robot.getPaintAmount() < robot.getPaintAmount() / 3 && rc.canTransferPaint(loc, amount)) {
                        rc.transferPaint(loc, amount);
                    }
                }
                indicator += "transferred paint]";
            }
        }
    }

    // update positions of nearby paint -- tracking the average position of allied/enemy paint
    public static void updateSurroundings() throws GameActionException {
        enemyPaint.clear();
        allyPaint.clear();
        // update surrounding ruins and robots
        var nearbyRobots = rc.senseNearbyRobots(-1);
        var nearbyRuins = rc.senseNearbyRuins(-1);
        for (var ruin : nearbyRuins) emptyRuins.add(ruin);
        for (var robot : nearbyRobots) {
            var loc = robot.getLocation();
            if (emptyRuins.contains(loc)) emptyRuins.remove(loc);
            var type = robot.getType();
            if (robot.getTeam() == myTeam) {
                if (type.isRobotType()) allies.add(loc);
            } else {
                if (type.isRobotType()) enemyRobots.add(loc);
                else enemyTowers.add(loc);
            }
        }
        // update surrounding radius (for bytecode -- use 5x5 for now)
        int myX = 0, myY = 0, enX = 0, enY = 0;
        for (var tile : rc.senseNearbyMapInfos(12)) {
            var loc = tile.getMapLocation();
            if (tile.getPaint().isAlly()) {
                allyPaint.add(loc);
                myX += loc.x;
                myY += loc.y;
            } else if (tile.getPaint().isEnemy()) {
                enemyPaint.add(loc);
                enX += loc.x;
                enY += loc.y;
                rc.setIndicatorDot(loc, 255, 255, 255);
            }
            if (tile.getMark().isAlly()) enemyMarks.add(loc);
        }
        int allyCnt = allyPaint.size, enemCnt = enemyPaint.size;
        paintBalance = allyPaint.size - enemyPaint.size;
        // reset values if needed
        var myLoc = rc.getLocation();
        if (avgMe != null && myLoc.isWithinDistanceSquared(avgMe, 8)) {
            avgMe = null;
        }
        if (avgEnemy != null && myLoc.isWithinDistanceSquared(avgEnemy, 8)) {
            avgEnemy = null;
        }
        if (allyCnt > 0) {
            avgMe = new MapLocation((int) Math.round((double) myX / allyCnt), (int) Math.round((double) myY / allyCnt));
        }
        if (enemCnt > 0) {
            avgEnemy = new MapLocation((int) Math.round((double) enX / enemCnt), (int) Math.round((double) enY / enemCnt));
        }
    }

    public static void refill() throws GameActionException {
        var nearbyTowers = rc.senseNearbyRuins(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
        for (var ruin : nearbyTowers) {
            if (!rc.canSenseRobotAtLocation(ruin)) continue;
            var robot = rc.senseRobotAtLocation(ruin);
            if (robot.getTeam() == myTeam) {
                requestPaint(robot.getLocation(), 100 - rc.getPaint());
            }
        }
    }

    /**
     * Returns cardinal direction with the most enemies, null otherwise
     */
    public static int getBestMopSwingDir() throws GameActionException {
        int[] numEnemies = {0, 0, 0, 0}; // N E S W
        // TODO: see verify mop swing area of effect
        FastLocSet enemies = new FastLocSet();
        for (var robot : rc.senseNearbyRobots(8, opponentTeam)) {
            if (robot.paintAmount > 0)
                enemies.add(robot.getLocation());
        }
        MapLocation currLoc = rc.getLocation();
        if (enemies.size == 0) return -1; // no nearby enemy robots
        for (int dir = 0; dir < 4; dir++) { // check each direction
            for (int i = 0; i < 6; i++) { // check each impact square in this direction
                if (enemies.contains(currLoc.translate(dxMop[dir][i], dyMop[dir][i]))) {
                    numEnemies[dir]++;
                    break;
                }
            }
        }
        int bestDir = 0;
        int bestDirIdx = -1;
        for (int i = 0; i < 4; i++) {
            if (numEnemies[i] >= bestDir) {
                bestDir = numEnemies[i];
                bestDirIdx = i;
            }
        }
        if (bestDir == 0) return -1; // no hit-able enemies in any direction
        return bestDirIdx;
    }

    public static void debug() throws GameActionException {
//        if (avgMe != null) {
//            rc.setIndicatorDot(avgMe, 0, 255, 0);
////            indicator += " [avgMe:" + avgMe + "]";
//        }
//        if (avgEnemy != null) {
//            rc.setIndicatorDot(avgEnemy, 255, 0, 0);
////            indicator += " [avgEnemy:" + avgEnemy + "]";
//        }
        indicator += "[paint balance: " + paintBalance + "]";
        if (avgEnemy != null && avgMe != null) {
            rc.setIndicatorDot(
                    new MapLocation((avgMe.x + avgEnemy.x) / 2, (avgMe.y + avgEnemy.y) / 2),
                    255, 255, 0
            );
        }
        rc.setIndicatorString(indicator);
    }
}
