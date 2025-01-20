package V08copy.Nav;

import V08copy.Globals;
import V08copy.Tools.FastLocSet;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class BugNavigator extends Globals {
    static final int LEFT = 0, RIGHT = 1;
    public static int stuckCnt;
    private static MapLocation target = null;

    public static void move(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady() || loc == null) return;
        target = loc;
        var dir = BugNav.getMoveDir();
        if (dir == null) return;
        if (rc.canMove(dir)) rc.move(dir);
    }

    public class BugNav {
        static final int MAX_DEPTH = 20, BC_CUTOFF = 6000;
        static DirStack stk = new DirStack();
        static MapLocation prevTarget = null;
        static FastLocSet vis = new FastLocSet();
        static int currentTurnDir = 0;
        static int stkDepthCutoff = 8;
        static int lastMoveRound = -1;

        static void resetPathfinding() {
            stkDepthCutoff = 8;
            stk.clear();
            stuckCnt = 0;
            vis.clear();
        }

        static Direction turn(Direction dir) {
            return currentTurnDir == 0 ? dir.rotateLeft() : dir.rotateRight();
        }

        static Direction turn(Direction dir, int turnDir) {
            return turnDir == 0 ? dir.rotateLeft() : dir.rotateRight();
        }

        static Direction getMoveDir() throws GameActionException {
            if (rc.getRoundNum() == lastMoveRound) return null;
            lastMoveRound = rc.getRoundNum();
            if (prevTarget == null || target.distanceSquaredTo(prevTarget) < 2) resetPathfinding();
            if (vis.contains(rc.getLocation())) stuckCnt++;
            else {
                stuckCnt = 0;
                vis.add(rc.getLocation());
            }

            if (stk.size == 0) {
                stkDepthCutoff = 8;
                var dir = rc.getLocation().directionTo(target);
                if (rc.canMove(dir)) return dir;
                if (rc.canSenseLocation(rc.getLocation().add(dir))) {
                    Direction dl = dir.rotateLeft(), dr = dir.rotateRight();
                    MapLocation l = rc.getLocation().add(dl), r = rc.getLocation().add(dr);
                    if (target.distanceSquaredTo(l) < target.distanceSquaredTo(r)) {
                        if (rc.canMove(dl)) return dl;
                        if (rc.canMove(dr)) return dr;
                    } else {
                        if (rc.canMove(dr)) return dr;
                        if (rc.canMove(dl)) return dl;
                    }
                }
                currentTurnDir = getTurnDir(dir);
                while (!rc.canMove(dir) && stk.size < 8) {
                    if (!rc.onTheMap(rc.getLocation().add(dir))) {
                        currentTurnDir ^= 1;
                        stk.clear();
                        return null;
                    }
                    stk.push(dir);
                    dir = turn(dir);
                }
                if (stk.size != 8) return dir;
            } else {
                if (stk.size > 1 && rc.canMove(stk.top(2))) {
                    stk.pop(2);
                } else if (stk.size == 1 && rc.canMove(turn(stk.top(), 1 - currentTurnDir))) {
                    Direction d = turn(stk.top(), 1 - currentTurnDir);
                    stk.pop();
                    return d;
                }
                while (stk.size > 0 && rc.canMove(stk.top())) {
                    stk.pop();
                }
                if (stk.size == 0) {
                    var dir = rc.getLocation().directionTo(target);
                    if (rc.canMove(dir)) return dir;
                    if (rc.canSenseLocation(rc.getLocation().add(dir))) {
                        Direction dl = dir.rotateLeft(), dr = dir.rotateRight();
                        MapLocation l = rc.getLocation().add(dl), r = rc.getLocation().add(dr);
                        if (target.distanceSquaredTo(l) < target.distanceSquaredTo(r)) {
                            if (rc.canMove(dl)) return dl;
                            if (rc.canMove(dr)) return dr;
                        } else {
                            if (rc.canMove(dr)) return dr;
                            if (rc.canMove(dl)) return dl;
                        }
                    }
                    stk.push(dir);
                }
                Direction curDir;
                int stkSizeLim = Math.min(stk.size + 8, DirStack.STACK_SIZE);
                while (stk.size > 0 && !rc.canMove(curDir = stk.top())) {
                    if (!rc.onTheMap(rc.getLocation().add(curDir))) {
                        currentTurnDir ^= 1;
                        stk.clear();
                        return null;
                    }
                    stk.push(curDir);
                    if (stk.size == stkSizeLim) {
                        stk.clear();
                        return null;
                    }
                }
                if (stk.size >= stkDepthCutoff) {
                    int cutoff = stkDepthCutoff + 8;
                    stk.clear();
                    stkDepthCutoff = cutoff;
                }
                Direction moveDir = stk.size == 0 ? stk.dirs[0] : turn(stk.top());
                if (rc.canMove(moveDir)) return moveDir;
            }
            return null;
        }

        static int getTurnDir(Direction dir) throws GameActionException {
            return nextInt() % 2;
        }
    }
}


class DirStack {
    static int STACK_SIZE = 60;
    int size = 0;
    Direction[] dirs = new Direction[STACK_SIZE];

    final void clear() {
        size = 0;
    }

    final void push(Direction d) {
        dirs[size++] = d;
    }

    final Direction top() {
        return dirs[size - 1];
    }

    /// gets the nth element from the top
    final Direction top(int n) {
        return dirs[size - n];
    }

    final void pop() {
        size--;
    }

    final void pop(int n) {
        size -= n;
    }
}