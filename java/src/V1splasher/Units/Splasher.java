package V1splasher.Units;

import V1splasher.*;
import battlecode.common.*;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 0;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 3;

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();

        MapLocation currLoc = rc.getLocation();
        if (rc.canAttack(currLoc) && rc.getPaint() > 50 && shouldSplash()) {
            rc.attack(currLoc);
        }

        if (rc.getPaint() < 50) {
            Navigator.moveTo(Unit.paint_tower);
            Unit.acquire_paint();
        }

        Unit.wander();
    }

    private static int[] mvmul(int[][] mat, int[] vect){
        return new int[]{
                mat[0][0] * vect[0] + mat[0][1] * vect[1],
                mat[1][0] * vect[0] + mat[1][1] * vect[1]
        };
    }
    public static boolean isOptimalSplashLoc() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        double[][] mp = {{2/13.0, -3/13.0},
                {3/13.0, 2/13.0}};
        int[] pt = {
                (int) Math.round(mp[0][0] * currLoc.x + mp[0][1] * currLoc.y),
                (int) Math.round(mp[1][0] * currLoc.x + mp[1][1] * currLoc.y),
        };
        // display 4 dots in line along with the closest dot as line guide
        int[][] rmap = {{2, 3}, {-3, 2}};
        for (int i = -2; i <= 2; i++) {
            int[] offsetpt = {pt[0], pt[1]+i};
            int[] linept = mvmul(rmap, offsetpt);
            if (linept[0] >= 0 && linept[1] >= 0 && linept[0] < mapWidth && linept[1] < mapHeight) {
                rc.setIndicatorDot(new MapLocation(linept[0], linept[1]), 255, 255, 0);
            }
        }
        // can also inline the mat-vec mul on this line
        int[] remapped = mvmul(rmap, pt);
        String loc = String.format("Opt (mapped space): (%d, %d), Opt (real): (%d, %d)",pt[0],pt[1],remapped[0],remapped[1]);
        rc.setIndicatorString(loc);
        return remapped[0] == currLoc.x && remapped[1] == currLoc.y;
    }

    public static boolean shouldSplash() throws GameActionException {
        return isOptimalSplashLoc();
        // if (canOptimalSplash())
        //     return true;
        
        // if (canSplashEnemies())
        //     return true;

        // return false;
    }

    public static boolean canOptimalSplash() throws GameActionException {
        int numAllySquares = 0;
        for (MapInfo tile : rc.senseNearbyMapInfos(4)) {
            if (tile.isWall()) {
                continue;
            }
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ALLY_PRIMARY || paintType == PaintType.ALLY_SECONDARY) {
                numAllySquares++;
            }
        }
        return numAllySquares < maxNumAllySquares;
    }

    public static boolean canSplashEnemies() throws GameActionException {
        int numEnemySquares = 0;
        for (MapInfo tile : rc.senseNearbyMapInfos(2)) {
            if (tile.isWall()) {
                continue;
            }
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
                numEnemySquares++;
            }
        }
        return numEnemySquares >= minNumEnemySquares;
    }
}
