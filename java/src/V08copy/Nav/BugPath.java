package V08copy.Nav;

import V08copy.Globals;
import battlecode.common.*;

/**
 * Bugnav copied from my 2023 code.
 */
public class BugPath extends Globals {
    int bugPathIndex = 0;

    Boolean rotateRight = null; //if I should rotate right or left
    //Boolean rotateRightAux = null;
    MapLocation lastObstacleFound = null; //latest obstacle I've found in my way

    MapLocation lastCurrent = null;
    int minDistToTarget = Integer.MAX_VALUE; //minimum distance I've been to the enemy while going around an obstacle
    MapLocation minLocationToTarget = null;
    MapLocation prevTarget = null; //previous target
    Direction[] dirs = Direction.values();

    int DEBUG_BUGPATH = 0;
    //HashSet<Integer> states = new HashSet<>();

    int[][] states = new int[mapWidth][mapHeight];

    MapLocation myLoc;
    //boolean[] canMoveArray;
    int round;

    int turnsMovingToObstacle = 0;
    final int MAX_TURNS_MOVING_TO_OBSTACLE = 2;
    final int MIN_DIST_RESET = 3;

    void update(){
        if (!rc.isMovementReady()) return;
        myLoc = rc.getLocation();
        round = rc.getRoundNum();
    }

    void debugMovement(){
        try{
            for (Direction dir : dirs){
                MapLocation newLoc = myLoc.add(dir);
                if (rc.canSenseLocation(newLoc) && rc.canMove(dir)) rc.setIndicatorDot(newLoc, 0, 0, 255);
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    public void moveTo(MapLocation target){

        //No target? ==> bye!
        if (!rc.isMovementReady()) return;
        if (target == null) target = rc.getLocation();
        //if (Constants.DEBUG == 1)
        //rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 255);

        update();
        //if (target == null) return;


        //different target? ==> previous data does not help!
        if (prevTarget == null){
            if (DEBUG_BUGPATH == 1) System.out.println("Previous target is null! reset!");
            resetPathfinding();
            rotateRight = null;
            //rotateRightAux = null;
        }


        else {
            int distTargets = target.distanceSquaredTo(prevTarget);
            if (distTargets > 0) {
                if (DEBUG_BUGPATH == 1) System.out.println("Different target!! Reset!");
                if (distTargets >= MIN_DIST_RESET){
                    rotateRight = null;
                    //rotateRightAux = null;
                    resetPathfinding();
                }
                else{
                    if (DEBUG_BUGPATH == 1) System.out.println("Different target!! Soft Reset!");
                    softReset(target);
                }
            }
        }


        //Update data
        prevTarget = target;

        checkState();
        myLoc = rc.getLocation();

        int d = myLoc.distanceSquaredTo(target);
        if (d == 0){
            return;
        }

        //If I'm at a minimum distance to the target, I'm free!
        if (d < minDistToTarget){
            if (DEBUG_BUGPATH == 1) System.out.println("resetting on d < mindist");
            resetPathfinding();
            minDistToTarget = d;
            minLocationToTarget = myLoc;
        }

        //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound == null){
            if (tryGreedyMove()){
                if (DEBUG_BUGPATH == 1) System.out.println("No obstacle and could move greedily :)");
                resetPathfinding();
                return;
            }
        }
        else{
            dir = myLoc.directionTo(lastObstacleFound);
            //rc.setIndicatorDot(lastObstacleFound, 0, 255, 0);
            //if (lastCurrent != null) rc.setIndicatorDot(lastCurrent, 255, 0, 0);
        }


        try {

            if (rc.canMove(dir)){
                rc.move(dir);
                if (lastObstacleFound != null) {
                    if (DEBUG_BUGPATH == 1) System.out.println("Could move to obstacle?!");
                    ++turnsMovingToObstacle;
                    lastObstacleFound = rc.getLocation().add(dir);
                    if (turnsMovingToObstacle >= MAX_TURNS_MOVING_TO_OBSTACLE){
                        if (DEBUG_BUGPATH == 1) System.out.println("obstacle reset!!");
                        resetPathfinding();
                    } else if (!rc.onTheMap(lastObstacleFound)){
                        if (DEBUG_BUGPATH == 1) System.out.println("obstacle reset!! - out of the map");
                        resetPathfinding();
                    }
                }
                return;
            } else turnsMovingToObstacle = 0;

            checkRotate(dir);

            if (DEBUG_BUGPATH == 1) System.out.println(rotateRight + " " + dir.name());

            //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
            //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
            int i = 16;
            while (i-- > 0) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    return;
                }
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                    //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
                else lastObstacleFound = newLoc;
                if (rotateRight) dir = dir.rotateRight();
                else dir = dir.rotateLeft();
            }

            if  (rc.canMove(dir)){
                rc.move(dir);
                return;
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    boolean tryGreedyMove(){
        try {
            //if (rotateRightAux != null) return false;
            MapLocation myLoc = rc.getLocation();
            Direction dir = myLoc.directionTo(prevTarget);
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }
            int dist = myLoc.distanceSquaredTo(prevTarget);
            int dist1 = Integer.MAX_VALUE, dist2 = Integer.MAX_VALUE;
            Direction dir1 = dir.rotateRight();
            MapLocation newLoc = myLoc.add(dir1);
            if (rc.canMove(dir1)) dist1 = newLoc.distanceSquaredTo(prevTarget);
            Direction dir2 = dir.rotateLeft();
            newLoc = myLoc.add(dir2);
            if (rc.canMove(dir2)) dist2 = newLoc.distanceSquaredTo(prevTarget);
            if (dist1 < dist && dist1 < dist2) {
                //rotateRightAux = true;
                rc.move(dir1);
                return true;
            }
            if (dist2 < dist && dist2 < dist1) {
                ;//rotateRightAux = false;
                rc.move(dir2);
                return true;
            }
        } catch(Throwable t){
            t.printStackTrace();
        }
        return false;
    }

    //TODO: check remaining cases
    //TODO: move obstacle if can move to obstacle lol
    void checkRotate(Direction dir) throws GameActionException {
        if (rotateRight != null) return;
        Direction dirLeft = dir;
        Direction dirRight = dir;
        int i = 8;
        while (--i >= 0) {
            if (!rc.canMove(dirLeft)) dirLeft = dirLeft.rotateLeft();
            else break;
        }
        i = 8;
        while (--i >= 0){
            if (!rc.canMove(dirRight)) dirRight = dirRight.rotateRight();
            else break;
        }
        int distLeft = myLoc.add(dirLeft).distanceSquaredTo(prevTarget), distRight = myLoc.add(dirRight).distanceSquaredTo(prevTarget);
        if (distRight < distLeft) rotateRight = true;
        else rotateRight = false;
    }

    //clear some of the previous data
    void resetPathfinding(){
        if (DEBUG_BUGPATH == 1) System.out.println("reset!");
        lastObstacleFound = null;
        minDistToTarget = Integer.MAX_VALUE;
        ++bugPathIndex;
        turnsMovingToObstacle = 0;
    }

    void softReset(MapLocation target){
        if (DEBUG_BUGPATH == 1) System.out.println("soft reset!");
        if (minLocationToTarget != null) minDistToTarget = minLocationToTarget.distanceSquaredTo(target);
        else resetPathfinding();
    }

    void checkState(){
        int x,y;
        if (lastObstacleFound == null) {
            x = 61;
            y = 61;
        }
        else{
            x = lastObstacleFound.x;
            y = lastObstacleFound.y;
        }
        int state = (bugPathIndex << 14) | (x << 8) |  (y << 2);
        if (rotateRight != null) {
            if (rotateRight) state |= 1;
            else state |= 2;
        }
        if (states[myLoc.x][myLoc.y] == state){
            resetPathfinding();
        }

        states[myLoc.x][myLoc.y] = state;
    }

}