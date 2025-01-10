package V1_2.Units;

import V1_2.*;
import battlecode.common.*;
import java.util.Random;

public class Splasher extends Globals {
    // how many tiles need to be enemy tiles to splash
    private static final int minNumEnemySquares = 2;
    // max number of ally tiles that will be overridden in a splash
    private static final int maxNumAllySquares = 5;

    // corner locations to check for paint coverage
    static MapLocation[] targets = new MapLocation[4];

    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();

        Message[] msgs = rc.readMessages(-1);
        if (msgs.length > 0) {    
            checkCorners();
        }

        splash();
        refill();

        Unit.wander();
    }

    public static void splash() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int[] currPaintStats = getNearbyPaintStats(); // enemy, ally, obstacle
        boolean shouldSplash = currPaintStats[0] >= minNumEnemySquares || currPaintStats[1] < maxNumAllySquares;
        if (shouldSplash && rc.canAttack(currLoc)) {
            rc.attack(currLoc);
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

    public static void checkCorners() throws GameActionException {
        MapLocation currLoc = rc.getLocation();

        MapLocation vert = new MapLocation(mapWidth - 1, currLoc.y);
        MapLocation hort = new MapLocation(currLoc.x, mapHeight - 1);
        MapLocation mirr = new MapLocation(mapWidth - 1, mapHeight - 1);
        MapLocation home = new MapLocation(1, 1);

        targets[0] = vert;
        targets[1] = mirr;
        targets[2] = hort;
        targets[3] = home;

        Random rng = new Random();
        Unit.wanderTarget = targets[rng.nextInt(4)];
    }

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
}
