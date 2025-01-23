package V08rush.Units;

import V08rush.Unit;
import V08rush.Micro.MopperMicro;
import V08rush.Nav.Navigator;
import V08rush.Tools.FastLocSet;
import battlecode.common.*;

public class Mopper extends Unit {
    static FastLocSet enemyPaint = new FastLocSet(), allyPaint = new FastLocSet();
    static FastLocSet enemyMarks = new FastLocSet();
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
        // decide between swinging mop and painting a tile -- currently just always swing if it can hit a robot with paint
        if (swingDir != -1) {
            rc.mopSwing(cardinal[swingDir]);
            indicator += "swung]";
        } else {
            // get best tile to paint
            MapLocation paintTile = null;
            var nearbyRuins = rc.senseNearbyRuins(-1);
            int importance = 0;
            // TODO: maybe reorder the importance of these -- and write them in a cleaner manner
            for (var tile : rc.senseNearbyMapInfos(2)) {
                if (!tile.getPaint().isEnemy() || !tile.isPassable()) continue;
                // save ourselves from enemy penalty
                var loc = tile.getMapLocation();
                if (!rc.canAttack(loc)) continue;
                if (loc == rc.getLocation()) { // prioritize putting ourselves out of harm's way
                    paintTile = loc;
                    break;
                }
                // clear enemy paint around an unfinished ruin
                if (importance < 5) {
                    for (var ruin : nearbyRuins) {
                        if (ruin.isWithinDistanceSquared(loc, 8) && !rc.canSenseRobotAtLocation(ruin)) {
                            paintTile = loc;
                            importance = 5;
                            break;
                        }
                    }
                }
                // if they marked it, it should be important
                if (importance < 4 && tile.getMark().isEnemy()) {
                    paintTile = loc;
                    importance = 4;
                }
                // put enemies moppers off safe ground
                if (importance < 3 && rc.canSenseRobotAtLocation(loc) && rc.senseRobotAtLocation(loc).getType() == UnitType.MOPPER) {
                    paintTile = loc;
                    importance = 3;
                }
                // put enemies of other types off safe ground
                if (importance < 2 && rc.canSenseRobotAtLocation(loc)) {
                    paintTile = loc;
                    importance = 2;
                }
                if (importance < 1)
                    paintTile = loc;
            }

            if (paintTile != null && rc.canAttack(paintTile)) {
                // try to paint on that tile
                rc.attack(paintTile);
                indicator += "painted]";
            } else if (rc.getPaint() > 51){
                // can't paint any tile -- try to transfer paint (maybe? idk if this is good in current scheme?)
                var allies = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, rc.getTeam());
                var amount = rc.getPaint() - 51;
                for (var robot : allies) {
                    if (!robot.getType().isRobotType()) continue;
                    var loc = robot.getLocation();
                    if (robot.getPaintAmount() < robot.getType().paintCapacity / 3 && rc.canTransferPaint(loc, amount)) {
                        rc.transferPaint(loc, amount);
                        break;
                    }
                }
                indicator += "try paint transfer]";
            }
            // TODO: maybe do some nav movement if micro is deceived by far away paint
        }
    }

    // update positions of nearby paint -- tracking the average position of allied/enemy paint
    public static void updateSurroundings() throws GameActionException {
        enemyPaint.clear();
        allyPaint.clear();
        // update surrounding radius
        for (var tile : rc.senseNearbyMapInfos(12)) {
            var loc = tile.getMapLocation();
            if (tile.getPaint().isAlly()) {
                allyPaint.add(loc);
            } else if (tile.getPaint().isEnemy()) {
                enemyPaint.add(loc);
            }
            if (tile.getMark().isEnemy()) enemyMarks.add(loc);
        }
        // reset values if needed
    }

    public static void refill() throws GameActionException {
        if (Clock.getBytecodesLeft() < 400) return;
        for (var robot : rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, myTeam)) {
            if (rc.getType().isTowerType()) {
                requestPaint(robot.getLocation(), 100 - rc.getPaint());
                break;
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
        rc.setIndicatorString(indicator);
    }
}
