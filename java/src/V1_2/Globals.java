package V1_2;

import java.util.Random;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;
    public static final Random rng = new Random(6147);

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
    }

    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    // matrices for figuring out optimal splasher locations
    public static final double[][] mp = {{2/13.0, -3/13.0}, {3/13.0, 2/13.0}};
    public static final int[][] rmap = {{2, 3}, {-3, 2}};

    public static int[] mvmul(int[][] mat, int[] vect){
        return new int[]{
                mat[0][0] * vect[0] + mat[0][1] * vect[1],
                mat[1][0] * vect[0] + mat[1][1] * vect[1]
        };
    }

    public static int nextInt(int maxExclusive) {
        return (int) Math.floor(Math.random() * maxExclusive);
    }

    public static int clamp(int value, int min, int max){
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
