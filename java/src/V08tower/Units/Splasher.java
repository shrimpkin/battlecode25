package V08tower.Units;

import V08tower.Unit;
import V08tower.Nav.Navigator;
import V08tower.Tools.FastLocIntMap;
import battlecode.common.*;

public class Splasher extends Unit {
    private static final int PaintLimit = UnitType.SPLASHER.paintCapacity;
    // weights for coloring in squares -- and hitting enemy towers as a little bonus
    private static final int AllyWeight = -1, EnemyWeight = 5, EmptyWeight = 1, EnemyTowerWeight = 20;
    // bonus weight for splashing onto enemies
    private static final int EnemyStandingBonusModifier = 1;
    // minimum utility at which the splasher will splash -- maybe make vary with round # / current paint amt
    private static final int MinUtil = 8;
    // thresholds for refilling (currently 300/6 = 50, 300/4 = 75), susceptible to change)
    private static final int RefillStart = PaintLimit / 6, RefillEnd = PaintLimit / 4;
    private static MapLocation TargetLoc;
    private static int NumRoundsSinceSplash = 0;
    private static MapLocation[] nearbyRuins;
    private static FastLocIntMap dp = new FastLocIntMap();
    // whether the splasher is in refilling mode

    public static void run() throws GameActionException {
        updateTowerLocations();
        nearbyRuins = rc.senseNearbyRuins(-1);

        MapLocation bestLoc = splash();
        if (bestLoc != null && TargetLoc == null) {
            TargetLoc = bestLoc;
        }
        move();
        if (rc.getNumberTowers() > 4 && rc.getChips() > 1200) canCompletePattern();
        refill();
    }

    public static void refill() throws GameActionException {
        for (var ally : rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, myTeam)) {
            if (ally.getType().isTowerType()) {
                requestPaint(ally.getLocation(), PaintLimit - rc.getPaint());
                System.out.println("successfully refilled");
                break;
            }
        }
    }

    /********************
     ** CORE FUNCTIONS **
     ********************/

    /**
     * Splashes highest value location, if it's above min score value
     */
    public static MapLocation splash() throws GameActionException {
        if (!rc.isActionReady()) return null;
        MapLocation best = null;
        int mostUtil = -1;
        // avoid expensive repeat computation
        precomputeSplashTileScores();
        for (var tile : rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared)) {
            var loc = tile.getMapLocation();
            if (!rc.canAttack(loc)) continue;
            int score = getSplashScore(loc);
            if (score > mostUtil) {
                best = loc;
                mostUtil = score;
            }
        }

        // maybe: make it less picky over time -- perhaps (minutil - roundNum/N) for some N > 200
        if (best != null) {
            if (mostUtil >= MinUtil || NumRoundsSinceSplash >= 10) {
                rc.attack(best);
//                rc.setIndicatorDot(best, 123, 47, 123);
                NumRoundsSinceSplash = 0;
                return best;
            }
        }
        return null;
    }

    /// Movement and wandering logic
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


    /// precomputes the splash scores for individual tiles -- if they were to be in range -- for bytecode reduction
    private static void precomputeSplashTileScores() throws GameActionException {
        final int MAX_EFFECT_RADIUS_SQUARED = 10;
        dp.clear();
        for (MapInfo info : rc.senseNearbyMapInfos(MAX_EFFECT_RADIUS_SQUARED)) {
//            rc.setIndicatorDot(info.getMapLocation(), 0, 0, 0);
            int util = 0;
            var loc = info.getMapLocation();
            if (!info.isPassable()) continue;
            // get base paint weight for tile
            int paintWeight = switch (info.getPaint()) {
                case EMPTY                          -> EmptyWeight;
                case ENEMY_PRIMARY, ENEMY_SECONDARY -> EnemyWeight;
                case ALLY_PRIMARY, ALLY_SECONDARY   -> AllyWeight;
            };
            // calculate near ruin multiplier / enemy tower damage score
            boolean nearRuin = false;
            for (MapLocation ruin : nearbyRuins) {
                if (ruin.isWithinDistanceSquared(loc, 2)) {
                    nearRuin = true;
                    var robot = rc.senseRobotAtLocation(ruin);
                    if (robot != null && robot.getTeam() == opponentTeam) util += EnemyTowerWeight;
                    break;
                }
            }
            // interfere with enemy build = more good, interfere with ally build = more bad
            if (nearRuin) paintWeight *= 2;
            util += paintWeight;
            // get the bonus from ally/enemy standing on the converted paint
            if (rc.canSenseRobotAtLocation(loc)) {
                if (info.hasRuin()) continue; // don't count towers
                if (info.getPaint() == PaintType.EMPTY) {
                    util += GameConstants.PENALTY_NEUTRAL_TERRITORY;
                } else if (info.getPaint().isEnemy()){
                    util +=  GameConstants.PENALTY_ENEMY_TERRITORY;
                }
            }
            dp.add(loc, util);
        }
    }
    /*
    TODO: this is a very expensive function, if it exceeds bytecode, then consider going through every possible
     affected tile, finding their individual utility, and have the splash score just be a sum of those tiles
     instead of recomputing each time
     */
    /**
     * Calculates the value obtained from a splash
     */
    public static int getSplashScore(MapLocation center) throws GameActionException {
        int util = 0;
        var locs = rc.senseNearbyMapInfos(center, GameConstants.SPLASHER_ATTACK_ENEMY_PAINT_RADIUS_SQUARED);
        for (int  i = locs.length - 1; i --> 0;) {
            util += dp.getVal(locs[i].getMapLocation());
        }
        return util;
    }
}
