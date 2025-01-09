package V2;

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

    public static int[] mvmul(int[][] mat, int[] vect){
        return new int[]{
                mat[0][0] * vect[0] + mat[0][1] * vect[1],
                mat[1][0] * vect[0] + mat[1][1] * vect[1]
        };
    }
}
