package V01resource.Units;

import V01resource.*;
import battlecode.common.*;

public class Splasher extends Unit {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 5;

    // corner locations to check for paint coverage
    static MapLocation[] targets = new MapLocation[4];

    public static void run() throws GameActionException {
        update_paint_tower_loc();

        // Message[] msgs = rc.readMessages(-1);
        // if (msgs.length > 0) {    
        //     checkCorners();
        // }

        splash();
        if (rc.getPaint() <= 50) {
            refill(300);
        }
        
         wander();
    }

    public static void splash() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int[] currPaintStats = getNearbyPaintStats(); // enemy, ally, obstacle
        
        boolean shouldSplash = currPaintStats[0] >= minNumEnemySquares || currPaintStats[1] < maxNumAllySquares;
        if (currLoc.equals(getClosestOptimalLoc(currLoc)) && shouldSplash && rc.canAttack(currLoc)) {
            rc.attack(currLoc);
        }
    }

    // public static void checkCorners() throws GameActionException {
    //     MapLocation currLoc = rc.getLocation();

    //     MapLocation vert = new MapLocation(mapWidth - 1, currLoc.y);
    //     MapLocation hort = new MapLocation(currLoc.x, mapHeight - 1);
    //     MapLocation mirr = new MapLocation(mapWidth - 1, mapHeight - 1);
    //     MapLocation home = new MapLocation(1, 1);

    //     targets[0] = vert;
    //     targets[1] = mirr;
    //     targets[2] = hort;
    //     targets[3] = home;

    //     Random rng = new Random();
    //     Unit.wanderTarget = targets[rng.nextInt(4)];
    // }

    public static int[] getNearbyPaintStats() throws GameActionException {
        MapInfo[] tiles = rc.senseNearbyMapInfos();
        MapLocation currLoc = rc.getLocation();
        int[] statuses = { 0, 0, 0 }; // enemy paint, ally paint, obstacle

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
                (int) Math.round(mp[0][0] * currLoc.x + mp[0][1] * currLoc.y),
                (int) Math.round(mp[1][0] * currLoc.x + mp[1][1] * currLoc.y),
        };
        int[] remapped = mvmul(rmap, pt);
        return new MapLocation(remapped[0], remapped[1]);
    }
}
