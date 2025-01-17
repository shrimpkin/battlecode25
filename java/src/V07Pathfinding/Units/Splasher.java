package V07Pathfinding.Units;

import V07Pathfinding.Unit;
import battlecode.common.*;

public class Splasher extends Unit {
    private static final int PaintLimit = UnitType.SPLASHER.paintCapacity;
    // weights for coloring in enemy and empty squares -- and hitting enemy towers as a little bonus
    private static final int EnemyWeight = 5, EmptyWeight = 1, EnemyTowerWeight = 20;
    // minimum utility at which the splasher will splash -- maybe make vary with round # / current paint amt
    private static final int MinUtil = 8;
    // thresholds for refilling (currently 300/6 = 50, 300/4 = 75), susceptible to change)
    private static final int RefillStart = PaintLimit / 6, RefillEnd = PaintLimit / 4;
    // whether the splasher is in refilling mode

    public static void run() throws GameActionException {
        updateTowerLocations();
        splash();
        wander(true);
        if (rc.getNumberTowers() > 4 && rc.getChips() > 1200)
            canCompletePattern();
    }

    // TODO: use refill function (FROM UNIT.java) and set the mode when relevant

    /** Splashes highest value location, if it's above min score value */
    public static void splash() throws GameActionException {
        if (mode == Modes.REFILL) 
            return; // don't be counterproductive -- can have more complex check here later

        MapLocation best = null;
        int mostUtil = -1;
        for (var tile : rc.senseNearbyMapInfos(4)) {
            var loc = tile.getMapLocation();
            if (!rc.canAttack(loc)) 
                continue;
            int score = getSplashScore(loc);
            if (score > mostUtil) {
                best = loc;
                mostUtil = score;
            }
        }
        // maybe: make it less picky over time -- perhaps (minutil - roundNum/N) for some N > 200
        if (best != null && mostUtil >= MinUtil) {
            rc.attack(best);
            if (rc.getPaint() <= RefillStart) {
                mode = Modes.REFILL;
            }
        }
    }

    /** Calculates the value obtained from a splash */
    public static int getSplashScore(MapLocation center) throws GameActionException {
        int util = 0;
        for (MapInfo info : rc.senseNearbyMapInfos(center, GameConstants.SPLASHER_ATTACK_AOE_RADIUS_SQUARED)) {
            // depending on how things play out -- maybe also count nearby enemy towers as well
            var loc = info.getMapLocation();
            if (info.hasRuin()) {
                var robot = rc.senseRobotAtLocation(loc);
                if (robot != null && robot.getTeam() == opponentTeam) util += EnemyTowerWeight;
            }
            if (!info.isPassable()) continue;
            switch (info.getPaint()) {
                case EMPTY:
                    util += EmptyWeight;
                    break;
                case ENEMY_PRIMARY, ENEMY_SECONDARY:
                    if (info.getMapLocation().isWithinDistanceSquared(center, GameConstants.SPLASHER_ATTACK_ENEMY_PAINT_RADIUS_SQUARED))
                        util += EnemyWeight;
                    break;
                default: 
                    break;
            }
        }
        return util;
    }
}
