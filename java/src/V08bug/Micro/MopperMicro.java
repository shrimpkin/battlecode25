package V08bug.Micro;

import V08bug.Globals;
import V08bug.Tools.FastIntSet;
import V08bug.Tools.FastLocSet;
import battlecode.common.*;

public class MopperMicro {
    static final int MAX_MICRO_BYTECODE_REMAINING = 5000;
    static int myRange;
    static int myVisionRange;
    static boolean canAttack;

    final int INF = 1000000;
    Direction[] dirs = Direction.values();
    FastIntSet paintTowers;
    FastLocSet epaint, apaint;
    MapLocation[] enemyPaint, allyPaint;
    int epaintCount = 0, apaintCount = 0;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean needPaint = false;
    RobotController rc;
    int rangeExtended = 10;
    MicroInfo[] microInfo;

    public MopperMicro() {
        this.rc = Globals.rc;
        myRange = 7; // extent of mopper swinging (everything in 5x5 except the corners)
        myVisionRange = GameConstants.VISION_RADIUS_SQUARED;
    }

    boolean lowPaint() {
        return rc.getPaint() <= rc.getType().paintCapacity / 3 && paintTowers.size > 0;
    }

    public boolean doMicro() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        shouldPlaySafe = false;
        needPaint = lowPaint();
        if (!needPaint && rc.getActionCooldownTurns() < 20) {
            var enemiesShort = rc.senseNearbyRobots(rangeExtended, rc.getTeam().opponent());
            if (enemiesShort.length == 0 && enemyPaint.length == 0) {
                return false;
            }
        }
        // when low on paint/ can't act, treat all enemies as dangerous
        alwaysInRange = !rc.isActionReady() || lowPaint();

        MicroInfo bestMicro = microInfo[8];
        for (int i = 8; --i >= 0;) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
        }
        if (bestMicro.dir == Direction.CENTER) return true;
//        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(bestMicro.dir), 255, 255, 255);

        if (rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            return true;
        }
        return false;
    }
    /// slim down the paint array by only taking a certain fraction of paint tiles outside a threshold r^2
    private int slimArray(MapLocation[] locs, int thresholdSquared, int factor) {
        if (locs.length < 12) return locs.length; // don't need slimming
        int realIdx = 0;
        for (int i = 0; i < locs.length; i++) {
            var loc = locs[i];
            if (loc.isWithinDistanceSquared(rc.getLocation(), thresholdSquared) || i % factor == 0) {
                locs[realIdx++] = loc;
            }
        }
        return realIdx;
    }

    public void computeMicroArray(boolean allies, FastIntSet paintTowers, FastLocSet enemyPaint, FastLocSet allyPaint) throws GameActionException {
        var units = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        this.paintTowers = paintTowers;
        epaint = enemyPaint;
        apaint = allyPaint;
        this.enemyPaint = enemyPaint.getKeys();
        this.allyPaint = allyPaint.getKeys();
        // slim down both arrays -- maybe tune this later idk
        epaintCount = slimArray(this.enemyPaint, 2, 3);
        apaintCount = slimArray(this.allyPaint, 2, 3);

        canAttack = rc.isActionReady();
        microInfo = new MicroInfo[]{
                new MicroInfo(dirs[0]),
                new MicroInfo(dirs[1]),
                new MicroInfo(dirs[2]),
                new MicroInfo(dirs[3]),
                new MicroInfo(dirs[4]),
                new MicroInfo(dirs[5]),
                new MicroInfo(dirs[6]),
                new MicroInfo(dirs[7]),
                new MicroInfo(dirs[8])
        };

        microInfo[0].updateSurrounding();
        microInfo[1].updateSurrounding();
        microInfo[2].updateSurrounding();
        microInfo[3].updateSurrounding();
        microInfo[4].updateSurrounding();
        microInfo[5].updateSurrounding();
        microInfo[6].updateSurrounding();
        microInfo[7].updateSurrounding();
        microInfo[8].updateSurrounding();

        for (var unit : units) {
            if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
            microInfo[0].updateEnemy(unit);
            microInfo[1].updateEnemy(unit);
            microInfo[2].updateEnemy(unit);
            microInfo[3].updateEnemy(unit);
            microInfo[4].updateEnemy(unit);
            microInfo[5].updateEnemy(unit);
            microInfo[6].updateEnemy(unit);
            microInfo[7].updateEnemy(unit);
            microInfo[8].updateEnemy(unit);
        }
        if (!allies || units.length == 0) return;

        units = rc.senseNearbyRobots(-1, rc.getTeam());
        for (var unit : units) {
            if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
            microInfo[0].updateAlly(unit);
            microInfo[1].updateAlly(unit);
            microInfo[2].updateAlly(unit);
            microInfo[3].updateAlly(unit);
            microInfo[4].updateAlly(unit);
            microInfo[5].updateAlly(unit);
            microInfo[6].updateAlly(unit);
            microInfo[7].updateAlly(unit);
            microInfo[8].updateAlly(unit);
        }
    }

    class MicroInfo {
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;
        int minDistanceToEnemyPaint = INF;
        int minDistanceToAllyPaint = INF;
        int enemiesInRange = 0;
        int enemiesInMoveRange = 0;
        int moppersInRange = 0;
        int moppersInMoveRange = 0;
        int inTowerRange = 0;
        int alliesDist2 = 1;
        int alliesDist10 = 1;
        int paintPenalty = -1;
        boolean canMove = true;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            if (rc.canSenseLocation(location)) {
                var paint = rc.senseMapInfo(location).getPaint();
                if (paint == PaintType.EMPTY) {
                    if (rc.getPaint() > 50) {
                        paintPenalty = 0;
                    } else {
                        paintPenalty = 1;
                    }
                } else if (paint.isEnemy()) {
                    if (rc.getPaint() > 80) {
                        paintPenalty = 2;
                    } else {
                        paintPenalty = 4;
                    }
                }
            }
        }

        int safe() {
            if (!canMove) return -INF; // can't move there
            if (inTowerRange > 0) return -inTowerRange; // moppers are very squishy to towers
            if (moppersInRange > alliesDist10) return 1; // outnumbered by enemy moppers
            if (rc.getPaint() < 20 && (paintPenalty + alliesDist2 - 1 > 0) && !rc.isActionReady()) return 2; // no paint and actively dying
            return 3; // no conditions
        }

        boolean inRange() {
            return alwaysInRange || minDistanceToEnemy <= myRange || minDistanceToEnemyPaint <= 2;
        }

        void updateSurrounding() {
            if (epaint.contains(location)) {
                minDistanceToEnemyPaint = 0;
            } else {
                for (int i = epaintCount; --i >= 0; ) {
                    minDistanceToEnemyPaint = Math.min(enemyPaint[i].distanceSquaredTo(location), minDistanceToEnemyPaint);
                }
            }
            if (apaint.contains(location)) {
                minDistanceToAllyPaint = 0;
            } else {
                for (int i = apaintCount; --i >= 0; ) {
                    minDistanceToAllyPaint = Math.min(allyPaint[i].distanceSquaredTo(location), minDistanceToAllyPaint);
                }
            }
        }

        void updateAlly(RobotInfo unit) {
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist <= 2) ++alliesDist2;
            if (dist <= 5) ++alliesDist10;
        }

        void updateEnemy(RobotInfo unit) {
            int dist = unit.getLocation().distanceSquaredTo(location);
            var type = unit.getType();
            if (type.isTowerType()) {
                // whether its within range of tower (big ruh roh for moppers)
                if (location.isWithinDistanceSquared(unit.location, type.actionRadiusSquared)) {
                    inTowerRange++;
                }
            } else {
                // it's a sitting duck (err bunny)
                if (unit.getPaintAmount() == 0) return;
                if (dist < minDistanceToEnemy) minDistanceToEnemy = dist;
                if (dist < 8) { // should be the extent of a mopper swing
                    ++enemiesInRange;
                    if (type == UnitType.MOPPER) ++moppersInRange;
                }
                if (dist <= rangeExtended) {
                    ++enemiesInMoveRange;
                    if (type == UnitType.MOPPER) ++moppersInMoveRange;
                }
            }
        }

        boolean isBetter(MicroInfo that) {
            if (safe() > that.safe()) return true;
            if (safe() < that.safe()) return false;

            if (inRange() && !that.inRange()) return true;
            if (!inRange() && that.inRange()) return false;

            if (!inRange()) {
                if (minDistanceToEnemy < that.minDistanceToEnemy) return true;
                if (minDistanceToEnemy > that.minDistanceToEnemy) return false;

                // allow for counting sqrt(1) and sqrt(2) as the same
                if (that.minDistanceToEnemyPaint - minDistanceToEnemyPaint > 1) return true;
                if (minDistanceToEnemyPaint - that.minDistanceToEnemyPaint > 1) return false;

                if (minDistanceToAllyPaint < that.minDistanceToAllyPaint) return true;
                if (minDistanceToAllyPaint > that.minDistanceToAllyPaint) return false;
            }

            // to get hit by less moppers
            if (moppersInRange < that.moppersInRange) return true;
            if (moppersInRange > that.moppersInRange) return false;

            // to take less paint terrain penalties
            if (paintPenalty + alliesDist2 < that.paintPenalty + that.alliesDist2) return true;
            if (paintPenalty + alliesDist2 > that.paintPenalty + that.alliesDist2) return false;

            // allow for counting sqrt(1) and sqrt(2) as the same
            if (that.minDistanceToEnemyPaint - minDistanceToEnemyPaint > 1) return true;
            if (minDistanceToEnemyPaint - that.minDistanceToEnemyPaint > 1) return false;

            // to run away from enely mopper swarms
            if (moppersInMoveRange < that.moppersInMoveRange) return true;
            if (moppersInMoveRange > that.moppersInMoveRange) return false;

            // to get close to enemy soldiers/splashers
            if (minDistanceToEnemy < that.minDistanceToEnemy) return true;
            if (minDistanceToEnemy > that.minDistanceToEnemy) return false;

            // to reduce overcrowding
            if (alliesDist2 < that.alliesDist2) return true;
            if (alliesDist2 > that.alliesDist2) return false;

            // secondary distance heuristic as tiebreaker if all else is equal
            if (minDistanceToAllyPaint < that.minDistanceToAllyPaint) return true;
            if (minDistanceToAllyPaint > that.minDistanceToAllyPaint) return false;

            if (dir == Direction.CENTER) return true;
            if (that.dir == Direction.CENTER) return false;

            return true;
        }
    }
}
