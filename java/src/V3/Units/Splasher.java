package V3.Units;

import V3.*;
import battlecode.common.*;

public class Splasher extends Unit {
    private static final int LOWEST_SCORE = 8;
    
    public static void run() throws GameActionException {
        updatePaintTowerLocations();
        splash();
        refill();
        wander(false);        
    }

    public static void refill() throws GameActionException {
        if (rc.getPaint() >= 50) return;

        Navigator.moveTo(closestPaintTower(), false);
    }

    /**
     * Finds the best location for splashing using the get_score heuristic
     * Splashes the best tile if it has a better score than LOWEST_SCORE 
     */
    public static void splash() throws GameActionException {
        MapLocation bestLocation = null;
        int bestScore = 0;

        MapInfo[] locations = rc.senseNearbyMapInfos(rc.getLocation(), 4);

        for(MapInfo loc : locations) {
            int score = getScore(loc.getMapLocation());
            if(score > bestScore) {
                bestScore = score;
                bestLocation = loc.getMapLocation();
            }
        }

        if(bestLocation != null && rc.canAttack(bestLocation) && bestScore >= LOWEST_SCORE) {
            rc.attack(bestLocation);
        }
    }

    /**
     * Gets the score of each tile, where each adjacent empty tile is one
     * And each adjacent tile with enemy paint is two
     */
    public static int getScore(MapLocation center) throws GameActionException {
        int score = 0;

        MapInfo[] locations = rc.senseNearbyMapInfos(center, 2);
        for(MapInfo loc : locations) {
            if(!rc.senseMapInfo(loc.getMapLocation()).isPassable()) continue;

            switch(loc.getPaint()) {
                case EMPTY: score++; break;
                case ENEMY_PRIMARY: 
                case ENEMY_SECONDARY: 
                    score += 5;
                    break;
                default: break;
            }
        }

        return score;
    }
}
