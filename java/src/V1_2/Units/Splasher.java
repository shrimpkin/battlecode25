package V1_2.Units;

import V1_2.*;
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

        // try to splash current location
        int[] currPaintStats = getNearbyPaintStats(); // enemy, ally, obstacle
        boolean shouldSplash = currPaintStats[0] >= minNumEnemySquares || currPaintStats[1] < maxNumAllySquares;
        if (currLoc.equals(optLoc) && shouldSplash) {
            if (rc.canAttack(currLoc)) {
                rc.attack(currLoc);
            }     
            return;
        }

        // move and try to splash
        Navigator.moveTo(optLoc);
        
        currPaintStats = getNearbyPaintStats(); // enemy, ally, obstacle
        shouldSplash = currPaintStats[0] >= minNumEnemySquares || currPaintStats[1] < maxNumAllySquares;
        if (shouldSplash) {
            if (rc.canAttack(optLoc)) {
                rc.attack(optLoc);
            }
        }
    }

    public static void refill() throws GameActionException {
        if (rc.getPaint() >= 50) {
            return;
        }

        Navigator.moveTo(Unit.paint_tower);
        Unit.acquire_paint();
        Unit.wanderTarget = null;
    }

    public static int[] getNearbyPaintStats() throws GameActionException {
        MapInfo[] tiles = rc.senseNearbyMapInfos();
        MapLocation currLoc = rc.getLocation();
        int[] statuses = {0, 0, 0}; // enemy paint, ally paint, obstacle

        // count the number of enemy/ally tiles that would be covered by splash
        for (int i = 0; i < tiles.length; i++) {
            if (!tiles[i].isPassable()) {
                statuses[2]++;
                continue;
            }

            // in range of enemy splash
            if (tiles[i].getMapLocation().distanceSquaredTo(currLoc) <= 2) {
                PaintType paintType = tiles[i].getPaint();
                if (paintType == PaintType.ENEMY_PRIMARY || paintType == PaintType.ENEMY_SECONDARY) {
                    statuses[0]++;
                }
            }
            
            // in range of ally splash
            if (tiles[i].getMapLocation().distanceSquaredTo(currLoc) <= 4) {
                PaintType paintType = tiles[i].getPaint();
                if (paintType == PaintType.ALLY_PRIMARY || paintType == PaintType.ALLY_SECONDARY) {
                    statuses[1]++;
                }
            }
        }

        return statuses;
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
