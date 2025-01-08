package V1.Units;

import V1.*;
import battlecode.common.*;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 4;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 3;

    public static void run() throws GameActionException {
        if (shouldSplash()) {
            rc.attack(rc.getLocation());
        }
        
        Unit.wander();
    }

    public static boolean shouldSplash() throws GameActionException {
        if (canOptimalSplash())
            return true;
        
        if (canSplashEnemies())
            return true;

        return false;
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
