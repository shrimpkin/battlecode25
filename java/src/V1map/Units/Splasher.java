package V1map.Units;

import V1map.Globals;
import V1map.Navigator;
import V1map.Unit;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 6;

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();
        MapLocation splashLoc = getSplashLoc();
        if (rc.getPaint() > 50 && splashLoc != null && rc.canAttack(splashLoc)) {
            rc.attack(splashLoc);
        }
        if (rc.getPaint() < 50) {
            Unit.resetWanderTarget();
            Navigator.moveTo(Unit.paint_tower);
            Unit.acquire_paint();
        } else {
            Unit.wander();
        }
    }

    public static MapLocation isOptimalSplashLoc(MapLocation currLoc) {
        int[] pt = {
                (int) Math.round((2.0 * currLoc.x - 3 * currLoc.y) / 13),
                (int) Math.round((3.0 * currLoc.x + 2 * currLoc.y) / 13),
        };
        return new MapLocation(
                2 * pt[0] + 3 * pt[1],
                -3 * pt[0] + 2 * pt[1]
        );
    }

    // TODO: do better
    public static MapLocation getSplashLoc() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation newLoc = isOptimalSplashLoc(currLoc);

        if (currLoc.x == newLoc.x && currLoc.y == newLoc.y) {
            if (canOptimalSplash() || canSplashEnemies()) {
                return currLoc;
            }
            return null;
        }
        return null;
    }

    public static boolean canOptimalSplash() throws GameActionException {
        int numAllySquares = 0;
        for (MapInfo tile : rc.senseNearbyMapInfos(4)) {
            if (tile.isWall()) continue;
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
            if (tile.isWall()) continue;
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
                numEnemySquares++;
            }
        }
        return numEnemySquares >= minNumEnemySquares;
    }
}
