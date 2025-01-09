package V2.Units;

import V2.*;
import battlecode.common.*;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 4;

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();

        MapLocation splashLoc = getSplashLoc();
        if (rc.canAttack(splashLoc) && rc.getPaint() > 50 && splashLoc != null) {
            rc.attack(splashLoc);
        }

        if (rc.getPaint() < 50) {
            Navigator.moveTo(Unit.paint_tower);
            Unit.acquire_paint();
        }

        Unit.wander();
    }

    public static MapLocation isOptimalSplashLoc(MapLocation currLoc) throws GameActionException {
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
            int[] linept = Utils.mvmul(rmap, offsetpt);
            // if (linept[0] >= 0 && linept[1] >= 0 && linept[0] < mapWidth && linept[1] < mapHeight) {
            //     rc.setIndicatorDot(new MapLocation(linept[0], linept[1]), 255, 255, 0);
            // }
        }
        // can also inline the mat-vec mul on this line
        int[] remapped = Utils.mvmul(rmap, pt);
        // String loc = String.format("Opt (mapped space): (%d, %d), Opt (real): (%d, %d)",pt[0],pt[1],remapped[0],remapped[1]);
        // rc.setIndicatorString(loc);
        return new MapLocation(remapped[0], remapped[1]);
    }

    public static MapLocation getSplashLoc() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation newLoc = isOptimalSplashLoc(currLoc);

        if (currLoc.x == newLoc.x && currLoc.y == newLoc.y) {
            if (canOptimalSplash() || canSplashEnemies()) {
                return currLoc;
            }
            return null;
        }

        Navigator.moveTo(newLoc);
        if (canOptimalSplash() || canSplashEnemies()) {
            return newLoc;
        }
        return null;

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
