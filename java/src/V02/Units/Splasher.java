package V02.Units;

import V02.*;
import battlecode.common.*;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 4;

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();

        splash();
        refill();
        
        Unit.wander();
    }

    public static void splash() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation optLoc = getClosestOptimalLoc(currLoc);

        // try to splash now
        if (currLoc.equals(optLoc) && (canOptimalSplash() || canSplashEnemies())) {
            if (rc.canAttack(currLoc)) {
                rc.attack(currLoc);
            }     
            return;
        }

        // move and try to splash
        Navigator.moveTo(optLoc, true);
        if (canOptimalSplash() || canSplashEnemies()) {
            if (rc.canAttack(optLoc)) {
                rc.attack(optLoc);
            }     
        }
    }

    public static void refill() throws GameActionException {
        if (rc.getPaint() >= 50) {
            return;
        }

        Navigator.moveTo(Unit.paint_tower, true);
        Unit.acquire_paint(250);
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

    public static MapLocation getClosestOptimalLoc(MapLocation currLoc) throws GameActionException {
        int[] pt = {
                (int) Math.round(Utils.mp[0][0] * currLoc.x + Utils.mp[0][1] * currLoc.y),
                (int) Math.round(Utils.mp[1][0] * currLoc.x + Utils.mp[1][1] * currLoc.y),
        };
        int[] remapped = Utils.mvmul(Utils.rmap, pt);
        return new MapLocation(remapped[0], remapped[1]);
    }
}
