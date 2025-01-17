package V05tower.Nav;

import V05tower.FastIntSet;
import V05tower.Globals;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class BugNavigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static boolean obstacleOnRight;
    private static MapLocation currentObstacle;
    private static FastIntSet visitedStates;
    private static final Direction[] adjacentDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST
    };

    public static void moveTo(MapLocation target) throws GameActionException {
        if (currentTarget == null || !currentTarget.equals(target)) {
            reset();
        }

        boolean hasOptions = false;
        for (int i = adjacentDirections.length; --i >= 0; ) {
            if (rc.canMove(adjacentDirections[i])) {
                hasOptions = true;
                break;
            }
        }

        if (!hasOptions) return;

        MapLocation myLocation = rc.getLocation();

        int distanceToTarget = Math.max(Math.abs(myLocation.x - target.x), Math.abs(myLocation.y - target.y));
        if (distanceToTarget < minDistanceToTarget) {
            reset();
            minDistanceToTarget = distanceToTarget;
        }

        if (currentObstacle != null && rc.canSenseLocation(currentObstacle) && rc.sensePassability(currentObstacle)) {
            reset();
        }

        if (!visitedStates.add(getState(target))) {
            reset();
        }

        currentTarget = target;

        if (currentObstacle == null) {
            Direction forward = myLocation.directionTo(target);
            if (rc.canMove(forward)) {
                rc.move(forward);
                return;
            }

            setInitialDirection();
        }

        followWall(true);
    }

    public static void reset() {
        currentTarget = null;
        minDistanceToTarget = Integer.MAX_VALUE;
        obstacleOnRight = true;
        currentObstacle = null;
        visitedStates = new FastIntSet();
    }

    private static void setInitialDirection() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        Direction forward = myLocation.directionTo(currentTarget);

        Direction left = forward.rotateLeft();
        for (int i = 8; --i >= 0; ) {
            MapLocation location = rc.adjacentLocation(left);
            if (rc.onTheMap(location) && rc.sensePassability(location)) {
                break;
            }

            left = left.rotateLeft();
        }

        Direction right = forward.rotateRight();
        for (int i = 8; --i >= 0; ) {
            MapLocation location = rc.adjacentLocation(right);
            if (rc.onTheMap(location) && rc.sensePassability(location)) {
                break;
            }

            right = right.rotateRight();
        }

        MapLocation leftLocation = rc.adjacentLocation(left);
        MapLocation rightLocation = rc.adjacentLocation(right);

        int leftDistance = Math.max(Math.abs(leftLocation.x - currentTarget.x), Math.abs(leftLocation.y - currentTarget.y));
        int rightDistance = Math.max(Math.abs(rightLocation.x - currentTarget.x), Math.abs(rightLocation.y - currentTarget.y));

        if (leftDistance < rightDistance) {
            obstacleOnRight = true;
        } else if (rightDistance < leftDistance) {
            obstacleOnRight = false;
        } else {
            obstacleOnRight = myLocation.distanceSquaredTo(leftLocation) < myLocation.distanceSquaredTo(rightLocation);
        }

        if (obstacleOnRight) {
            currentObstacle = rc.adjacentLocation(left.rotateRight());
        } else {
            currentObstacle = rc.adjacentLocation(right.rotateLeft());
        }
    }

    private static void followWall(boolean canRotate) throws GameActionException {
        Direction direction = rc.getLocation().directionTo(currentObstacle);

        for (int i = 8; --i >= 0; ) {
            direction = obstacleOnRight ? direction.rotateLeft() : direction.rotateRight();
            if (rc.canMove(direction)) {
                rc.move(direction);
                return;
            }

            MapLocation location = rc.adjacentLocation(direction);
            if (canRotate && !rc.onTheMap(location)) {
                obstacleOnRight = !obstacleOnRight;
                followWall(false);
                return;
            }

            if (rc.onTheMap(location) && !rc.sensePassability(location)) {
                currentObstacle = location;
            }
        }
    }

    private static char getState(MapLocation target) {
        MapLocation myLocation = rc.getLocation();
        Direction direction = myLocation.directionTo(currentObstacle != null ? currentObstacle : target);
        int rotation = obstacleOnRight ? 1 : 0;
        return (char) ((((myLocation.x << 6) | myLocation.y) << 4) | (direction.ordinal() << 1) | rotation);
    }

}