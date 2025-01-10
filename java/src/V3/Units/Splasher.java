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
        //splash();
        splash_if_good();
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

    public static void splash_if_good() throws GameActionException {
        int[][] paint = new int[7][7];

        MapLocation my_loc = rc.getLocation();
        for(int i = -3; i <= 3; i++) {
            for(int j = -3; j <= 3; j++) {
                if(rc.canSenseLocation(my_loc.translate(i, j)) && !rc.senseMapInfo(my_loc.translate(i, j)).hasRuin()) {
                    MapInfo info = rc.senseMapInfo(my_loc.translate(i, j));
                    int x = i + 3;
                    int y = j + 3;
                    switch(info.getPaint()) {
                        case ALLY_PRIMARY: paint[x][y] = 0;
                            break;
                        case ALLY_SECONDARY: paint[x][y] = 0;
                            break;
                        case EMPTY: paint[x][y] = 1;
                            break;
                        case ENEMY_PRIMARY: paint[x][y] = 3;
                            break;
                        case ENEMY_SECONDARY: paint[x][y] = 3;
                            break;
                        default:
                            break;
                    }
                } else {
                    paint[i + 3][j + 3] = 0;
                }
            }
        }

        String paint_output = "\n";
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                paint_output += paint[i][j] + ", ";
            }
            paint_output += "\n";
        }

        int[][] scores = new int[5][5];
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                scores[i][j] = 0;
                for(int k = -1; k <= 1; k++) {
                    for(int l = -1; l <= 1; l++) {
                        int x = i - k + 1;
                        int y = j - l + 1;
                        scores[i][j] += paint[x][y];
                    }
                }
            }
        }
        System.out.println(paint_output);


        String scores_output = "\n";
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                scores_output += scores[i][j] + ", ";
            }
            scores_output += "\n";
        }
        System.out.println(scores_output);

        int best_score = 0;
        MapLocation best_location = null;
        for(int i = 0; i <5; i++) {
            for(int j = 0; j <  5; j++) {
                if(scores[i][j] > best_score) {
                    MapLocation loc = rc.getLocation().translate(i - 2, j - 2);
                    if(rc.canAttack(loc)) {
                        best_location = loc;
                        best_score = scores[i][j];
                    }
                }
            }
        }

        if(best_location == null) {
            System.out.println("No best Location");
        } else {
            System.out.println(best_location.toString() + ", ");
        }
        if(best_location != null && rc.canAttack(best_location)) {
            rc.attack(best_location);
        }
    }
}
