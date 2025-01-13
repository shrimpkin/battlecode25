package V02;

import battlecode.common.Direction;

public class Utils {
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
}
