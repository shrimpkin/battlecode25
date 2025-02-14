package V01.Units;

import V01.*;
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
