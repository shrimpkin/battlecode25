package V08.Micro;

import V08.Globals;
import battlecode.common.*;

public class MopperMicro {
    static final int MAX_MICRO_BYTECODE_REMAINING = 5000;
    static int myRange;
    static int myVisionRange;
    static boolean canAttack;

    final int INF = 1000000;
    Direction[] dirs = Direction.values();
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean hurt = false;
    boolean needPaint = false;
    RobotController rc;
    int rangeExtended = 10;
    MicroInfo[] microInfo;

    public MopperMicro() {
        this.rc = Globals.rc;
        myRange = rc.getType().actionRadiusSquared;
        myVisionRange = GameConstants.VISION_RADIUS_SQUARED;
    }

    boolean lowPaint() {
        return rc.getPaint() <= rc.getType().paintCapacity / 3;
    }

    public boolean doMicro() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        shouldPlaySafe = false;
        needPaint = lowPaint();

        if (!needPaint && rc.getActionCooldownTurns() < 20) {
            var enemiesShort = rc.senseNearbyRobots(rangeExtended, rc.getTeam().opponent());
            if (enemiesShort.length == 0) return false;
        }

        // when low on paint/ can't act, treat all enemies as dangerous
        alwaysInRange = !rc.isActionReady() || lowPaint();

        MicroInfo bestMicro = microInfo[8];
        for (int i = 7; i-- > 0; ) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
        }
        if (bestMicro.dir == Direction.CENTER) return true;

        if (rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            return true;
        }
        return false;
    }

    public void computeMicroArray(boolean allies) throws GameActionException {
        var units = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
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
        int enemiesInRange = 0;
        int enemiesInMoveRange = 0;
        int moppersInRange = 0;
        int moppersInMoveRange = 0;
        int inTowerRange = 0;
        int alliesDist2 = 1;
        int alliesDist10 = 1;
        int paintPenalty = 0;
        boolean canMove = true;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            if (rc.canSenseLocation(location)) {
                var paint = rc.senseMapInfo(location).getPaint();
                if (paint == PaintType.EMPTY) paintPenalty = GameConstants.PENALTY_NEUTRAL_TERRITORY;
                else if (paint.isEnemy())
                    paintPenalty = GameConstants.PENALTY_ENEMY_TERRITORY * GameConstants.MOPPER_PAINT_PENALTY_MULTIPLIER;
            }
        }

        int safe() {
            if (inTowerRange > 0)
                return -2; // moppers are very squishy to towers
            if (!canMove)
                return -1; // can't move
            if (alliesDist2 >= 4)
                return 0; // too little / too many allies
            if (moppersInRange > alliesDist10)
                return 1; // outnumbered by enemy moppers
            if (paintPenalty > 0)
                return 2; // penalty for stepping on this tile
            return 3; // no conditions
        }

        boolean inRange() {
            return alwaysInRange || minDistanceToEnemy <= myRange;
        }

        void updateAlly(RobotInfo unit) {
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist <= 2) ++alliesDist2;
            if (dist <= 5) ++alliesDist10;
        }

        void updateEnemy(RobotInfo unit) throws GameActionException {
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
            rc.setIndicatorDot(unit.getLocation(), 200, 200, 100);
        }

        boolean isBetter(MicroInfo that) {
            if (safe() > that.safe()) return true;
            if (safe() < that.safe()) return false;

            if (inRange() && !that.inRange()) return true;
            if (!inRange() && that.inRange()) return true;

            if (!inRange()) {
                if (minDistanceToEnemy < that.minDistanceToEnemy) return true;
                if (minDistanceToEnemy > that.minDistanceToEnemy) return false;
            }

            if (moppersInRange < that.moppersInRange) return true;
            if (moppersInRange > that.moppersInRange) return false;

            if (paintPenalty < that.paintPenalty) return true;
            if (paintPenalty > that.paintPenalty) return false;

            if (moppersInMoveRange < that.moppersInMoveRange) return true;
            if (moppersInMoveRange > that.moppersInMoveRange) return false;

            if (minDistanceToEnemy < that.minDistanceToEnemy) return true;
            if (minDistanceToEnemy > that.minDistanceToEnemy) return false;

            if (alliesDist2 < that.alliesDist2) return true;
            if (alliesDist2 > that.alliesDist2) return false;

            if (dir == Direction.CENTER) return true;
            if (that.dir == Direction.CENTER) return false;

            return true;
        }
    }
}
