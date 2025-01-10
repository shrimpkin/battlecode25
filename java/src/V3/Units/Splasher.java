package V3.Units;

import V3.*;
import battlecode.common.*;

public class Splasher extends Globals {
    private static final int LOWEST_SCORE = 8;
    
    public static void run() throws GameActionException {
        Unit.update_paint_tower_loc();
        splash();
        refill();
        Unit.wander(false);        
    }

    public static void refill() throws GameActionException {
        if (rc.getPaint() >= 50) return;

        Navigator.moveTo(Unit.paint_tower, false);
        Unit.acquire_paint(250);
        Unit.wanderTarget = null;
    }

    /**
     * Finds the best location for splashing using the get_score heuristic
     * Splashes the best tile if it has a better score than LOWEST_SCORE 
     */
    public static void splash() throws GameActionException {
        MapLocation best_location = null;
        int best_score = 0;

        MapInfo[] locations = rc.senseNearbyMapInfos(rc.getLocation(), 4);

        for(MapInfo loc : locations) {
            int score = get_score(loc.getMapLocation());
            if(score > best_score) {
                best_score = score;
                best_location = loc.getMapLocation();
            }
        }

        if(best_location != null && rc.canAttack(best_location) && best_score >= LOWEST_SCORE) {
            rc.attack(best_location);
        }
    }

    /**
     * Gets the score of each tile, where each adjacent empty tile is one
     * And each adjacent tile with enemy paint is two
     */
    public static int get_score(MapLocation center) throws GameActionException {
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
