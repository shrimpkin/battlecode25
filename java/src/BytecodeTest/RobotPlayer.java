package BytecodeTest;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.List;

public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            Integer[] xs = {1,2,3,4,4,56,7,89,0,-21,5,4,42,424,3};
            int bn1 = Clock.getBytecodeNum();
//            for (int x : xs) {}
            for (int i = xs.length; i --> 0;) { var x = xs[i]; }
            int bn2 = Clock.getBytecodeNum();
            System.out.printf("bc count: %d\n", bn2 - bn1 - 1);
            rc.resign();
            Clock.yield();
        }
    }
    private static long seed = -1;
    static int nextInt() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL;
        return (int) ((seed >>> 16) & 0x7FFFFFFF);
    }
    static double nextDouble() {
        return (double) nextInt() / 0x7FFFFFFF;
    }
}
