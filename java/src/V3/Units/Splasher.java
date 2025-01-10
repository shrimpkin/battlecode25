package V3.Units;

import V3.*;
import battlecode.common.*;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 4;

    private static MapLocation currLoc;
    private static MapInfo[] nearbyTiles;
    private static int paintLvl;

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();

        // update round specific information
        currLoc = rc.getLocation();
        paintLvl = rc.getPaint();
        nearbyTiles = rc.senseNearbyMapInfos();

        // action sequence
        splash();
        refill();

        Unit.wander();
    }

    public static void splash() throws GameActionException {
        MapLocation splashLoc = getClosestOptimalSplashLoc(currLoc);
        Navigator.moveTo(splashLoc, true);

        if (splashLoc != null && paintLvl > 50
                && shouldSplash()
                && rc.canAttack(splashLoc)) { 
            rc.attack(splashLoc);
        }
    }

    public static void refill() throws GameActionException {
        if (paintLvl >= 50)
            return;

        Navigator.moveTo(Unit.paint_tower, false);
        Unit.acquire_paint(250);
        Unit.wanderTarget = null;
    }

    public static boolean shouldSplash() throws GameActionException {
        if (checkAllyCoverageOk() || checkEnemyCoverageOk()) {
            return true;
        }
        return false;
    }

    public static boolean checkAllyCoverageOk() throws GameActionException {
        int numAllySquares = 0;
        for (MapInfo tile : nearbyTiles) {
            if (tile.getMapLocation().distanceSquaredTo(currLoc) <= 4 && tile.isWall()) {
                continue;
            }
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ALLY_PRIMARY || paintType == PaintType.ALLY_SECONDARY) {
                numAllySquares++;
            }
        }
        return numAllySquares < maxNumAllySquares;
    }

    public static boolean checkEnemyCoverageOk() throws GameActionException {
        int numEnemySquares = 0;
        for (MapInfo tile : nearbyTiles) {
            if (tile.getMapLocation().distanceSquaredTo(currLoc) <= 2 && tile.isWall()) {
                continue;
            }
            PaintType paintType = tile.getPaint();
            if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
                numEnemySquares++;
            }
        }
        return numEnemySquares >= minNumEnemySquares;
    }

    public static MapLocation getClosestOptimalSplashLoc(MapLocation currLoc) throws GameActionException {
        int[] pt = {
                (int) Math.round(Utils.mp[0][0] * currLoc.x + Utils.mp[0][1] * currLoc.y),
                (int) Math.round(Utils.mp[1][0] * currLoc.x + Utils.mp[1][1] * currLoc.y),
        };

        int[] remapped = Utils.mvmul(Utils.rmap, pt);
        return new MapLocation(remapped[0], remapped[1]);
    }
}
