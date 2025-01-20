package V08copy.Units;

import V08copy.Unit;
import V08copy.Nav.Navigator;
import battlecode.common.*;

public class Splasher extends Unit {
    private static MapLocation TargetLoc;
    private static int NumRoundsSinceSplash = 0;

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

        MapLocation bestLoc = splash();
        if (bestLoc != null && TargetLoc == null) {
            TargetLoc = bestLoc;
        }
        move();

        if (rc.getNumberTowers() > 4 && rc.getChips() > 1200)
            canCompletePattern();
    }

    /********************
     ** CORE FUNCTIONS **
     ********************/

    /** Splashes highest value location, if it's above min score value */
    public static MapLocation splash() throws GameActionException {
        MapLocation best = null;
        int mostUtil = -1;
        for (var tile : rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared)) {
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
        if (best != null) {
            if(mostUtil >= MinUtil || NumRoundsSinceSplash >= 10) {
                rc.attack(best);
                NumRoundsSinceSplash = 0;
                return best;
            }
        }
        return null;
    }

    /** Movement and wandering logic */
    public static void move() throws GameActionException {
        if (TargetLoc != null) {
            boolean atLocation = TargetLoc.equals(rc.getLocation());
            boolean cannotReachTarget = rc.canSenseLocation(TargetLoc) && !rc.senseMapInfo(TargetLoc).isPassable();
            boolean isCloseToTarget = rc.getLocation().distanceSquaredTo(TargetLoc) < 2;

            if (atLocation || (cannotReachTarget && isCloseToTarget)) {
                TargetLoc = null;
                recenter();
            } else {
                Navigator.moveTo(TargetLoc);
            }
        } else {
            wander(true);
        }
    }

    /*************
     ** HELPERS **
     *************/

    /** Sets target location to be the direction with the most enemy paint */
    // public static void findPaintTrail() throws GameActionException {
    //     if (TargetLoc != null)
    //         return; // already have somewhere to go

    //     int[] weights = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    //     Direction[] dirs = Direction.allDirections();
    //     MapLocation currLoc = rc.getLocation();

    //     for (MapInfo tile : rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared)) {
    //         if (tile.getPaint().isEnemy()) {
    //             for (int i = 0; i < dirs.length; i++) {
    //                 if (currLoc.directionTo(tile.getMapLocation()) == dirs[i]) {
    //                     weights[i]++;
    //                     break;
    //                 }
    //             }
    //         }
    //     }

    //     boolean found = false;
    //     for (int weight : weights) {
    //         if (weight > 0) {
    //             found = true;
    //             break;
    //         }
    //     }

    //     if (!found)
    //         return; // didn't spot enemy paint nearby; no trail to follow

    //     int maxPaintNum = -1;
    //     for (int i = 0; i < dirs.length; i++) {
    //         if (weights[i] > maxPaintNum) {
    //             maxPaintNum = weights[i];
    //             TargetLoc = currLoc.add(dirs[i]);
    //         }
    //     }
    // }

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
