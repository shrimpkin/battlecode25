package V08.Units;

import V08.Comms;
import V08.Unit;
import V08.Micro.MopperMicro;
import V08.Nav.Navigator;
import V08.Tools.CommType;
import V08.Tools.FastLocSet;
import battlecode.common.*;

public class Mopper extends Unit {
    static final Direction[] cardinal = Direction.cardinalDirections();
    static FastLocSet enemyPaint = new FastLocSet(), allyPaint = new FastLocSet();
    static FastLocSet enemyMarks = new FastLocSet();
    static RobotInfo refillingTower = null;
    static MopperMicro micro = new MopperMicro();
    private static boolean wasWandering = false;

    public static void run() throws GameActionException {
        indicator = "";
        updateTowerLocations();
        updateSurroundings();
        micro.computeMicroArray(true, paintTowerLocations, enemyPaint, allyPaint);
        read();
        move();
        doAction();
        refill();
        debug();
    }

    /// reads msgs to target enemy
    public static void read() throws GameActionException {
        Message[] msgs = rc.readMessages(rc.getRoundNum());
        for (Message msg : msgs) {
            if (Comms.getType(msg.getBytes()) == CommType.TargetEnemy) {
                MapLocation enemyLoc = Comms.getLocation(msg.getBytes());
                rc.setIndicatorDot(enemyLoc, 200, 100, 200);
                if (rc.getLocation().distanceSquaredTo(enemyLoc) <= rc.getType().actionRadiusSquared && rc.canMopSwing(rc.getLocation().directionTo(enemyLoc))) {
                    // System.out.println("MOPPER attacking based off msg");
                    rc.mopSwing(rc.getLocation().directionTo(enemyLoc));
                }
            } else if (Comms.getType(msg.getBytes()) == CommType.RebuildTower) {
                // TODO: focus on mopping up paint
            }
        }
    }

    public static void move() throws GameActionException {
        if (rc.getPaint() < 40 && paintTowerLocations.size > 0 && refillingTower == null) {
            var target = getClosestLocation(paintTowerLocations);
            MapLocation best = target;
            int bestDistance = Integer.MAX_VALUE;
            boolean hasAllyPaint = false;
            for (var dir : cardinal) {
                var neighbor = target.add(dir.rotateLeft());
                var dist = rc.getLocation().distanceSquaredTo(neighbor);
                boolean neighborHasAllyPaint = rc.canSenseLocation(neighbor) && rc.senseMapInfo(neighbor).getPaint().isAlly();
                if (!hasAllyPaint) {
                    if (neighborHasAllyPaint || dist < bestDistance) {
                        best = neighbor;
                        bestDistance = dist;
                        hasAllyPaint = neighborHasAllyPaint;
                    }
                } else {
                    if (neighborHasAllyPaint && dist < bestDistance) {
                        best = neighbor;
                        bestDistance = dist;
                    }
                }
            }
            Navigator.moveTo(best);
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
        if (!rc.isActionReady() || rc.getPaint() < 40 && refillingTower != null) return;
        indicator += "[doing action: ";
        // get best direction to swing
        var swingDir = getBestMopSwingDir();
        // get best tile to paint
        MapLocation paintTile = null;
        var nearbyRuins = rc.senseNearbyRuins(-1);
        int importance = 0;
        // TODO: maybe reorder the importance of these -- and write them in a cleaner manner
        for (var tile : rc.senseNearbyMapInfos(2)) {
            if (!tile.getPaint().isEnemy() || !tile.isPassable()) continue;
            var loc = tile.getMapLocation();
            if (!rc.canAttack(loc)) continue;
            // put enemies off safe ground -- and steal their paint in the process
            var robot = rc.senseRobotAtLocation(loc);
            if (robot != null) {
                if (importance < 6 && robot.getTeam() == opponentTeam) {
                    paintTile = loc;
                    importance = 6;
                }
                // save ourselves and our team from enemy penalty
                if (importance < 5 && (loc == rc.getLocation() || robot.getTeam() == myTeam)) {
                    paintTile = loc;
                    importance = 5;
                }
            }
            // clear enemy paint around an unfinished ruin
            if (importance < 4) {
                for (var ruin : nearbyRuins) {
                    if (ruin.isWithinDistanceSquared(loc, 8) && !rc.canSenseRobotAtLocation(ruin)) {
                        paintTile = loc;
                        importance = 4;
                        break;
                    }
                }
            }
            // if they marked it, it should be important
            if (importance < 3 && tile.getMark().isEnemy()) {
                paintTile = loc;
                importance = 3;
            }
            if (importance < 2) {
                paintTile = loc;
                importance = 2;
            }
        }
        if (importance >= 5 && rc.canAttack(paintTile)) {
            // mopping tile with enemy has highest priority
            rc.attack(paintTile);
        } else if (swingDir != -1) {
            // otherwise swing has priority
            rc.mopSwing(cardinal[swingDir]);
        } else if (paintTile != null && rc.canAttack(paintTile)){
            // all other forms of mopping
            rc.attack(paintTile);
        } else if (rc.getPaint() > 51) {
            // can't mop any tile -- try to transfer paint (maybe? idk if this is good in current scheme?)
            var allies = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, rc.getTeam());
            var amount = rc.getPaint() - 51;
            for (var robot : allies) {
                if (!robot.getType().isRobotType()) continue;
                var loc = robot.getLocation();
                if (robot.getPaintAmount() < robot.getType().paintCapacity / 3 && rc.canTransferPaint(loc, amount)) {
                    rc.transferPaint(loc, amount);
                    indicator += "transferred paint]";
                    break;
                }
            }
        }
    }

    // update positions of nearby paint -- tracking the average position of allied/enemy paint
    public static void updateSurroundings() throws GameActionException {
        refillingTower = null;
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
        // check for valid towers to refill up to full
        for (var tower : rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, myTeam)) {
            if (tower.getType().isTowerType() && (tower.getPaintAmount() >= 100 - rc.getPaint() || rc.getPaint() <= 10)) {
                refillingTower = tower;
                break;
            }
        }
    }

    /// refill at nearby paint towers -- but
    public static void refill() throws GameActionException {
        if (!rc.isActionReady() || refillingTower == null) return;
        requestPaint(refillingTower.getLocation(), 100 - rc.getPaint());
    }

    /// Returns cardinal direction with the most enemies, null otherwise
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
