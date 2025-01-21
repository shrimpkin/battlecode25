package V08.Units;

import V08.Unit;
import V08.Nav.Navigator;
import battlecode.common.*;

public class Splasher extends Unit {
    private static final int PaintLimit = UnitType.SPLASHER.paintCapacity;
    // weights for coloring in squares
    private static final int AllyWeight = -1, EnemyWeight = 5, EmptyWeight = 1;
    // also reward disrupting enemy SRPs and enemy towers
    private static final int EnemyTowerWeight = 20, EnemySRPWeight = 50;
    // arbitrary, but large enough int that tile_util + CHAR_POSITIVE_OFFSET >= 0, so avoid bad int->char->int conv.
    private static final int CHAR_POSITIVE_OFFSET = 90;
    // minimum utility at which the splasher will splash -- maybe make vary with round # / current paint amt
    private static final int MinUtil = 20;
    // thresholds for refilling (currently 300/6 = 50, 300/4 = 75), susceptible to change)
    private static final int RefillStart = PaintLimit / 6, RefillEnd = PaintLimit / 4;
    private static MapLocation TargetLoc;
    private static int NumRoundsSinceSplash = 0;
    private static MapLocation[] nearbyRuins;
    private static char[] scores = "\0".repeat(4096).toCharArray();
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
            if (mostUtil >= Math.max(5, MinUtil - NumRoundsSinceSplash)) {// || NumRoundsSinceSplash >= 10) {
                rc.attack(best);
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
        scores = "\0".repeat(4096).toCharArray();
        for (MapInfo info : rc.senseNearbyMapInfos(MAX_EFFECT_RADIUS_SQUARED)) {
            int util = 0;
            var loc = info.getMapLocation();
            if (!info.isPassable()) continue;
            // get base paint weight for tile
            int paintWeight = switch (info.getPaint()) {
                case EMPTY                          -> EmptyWeight;
                case ENEMY_PRIMARY, ENEMY_SECONDARY -> EnemyWeight;
                case ALLY_PRIMARY, ALLY_SECONDARY   -> AllyWeight;
            };
            // limited check for whether we disrupt enemy SRPs
            if (info.getPaint().isEnemy() && info.isResourcePatternCenter()) {
                util += EnemySRPWeight;
            }
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
            scores[pack(loc)] = (char)(util + CHAR_POSITIVE_OFFSET);
        }
    }

    /// Calculates the value obtained from a splash
    public static int getSplashScore(MapLocation center) throws GameActionException {
        int util = 0;
        var locs = rc.senseNearbyMapInfos(center, GameConstants.SPLASHER_ATTACK_ENEMY_PAINT_RADIUS_SQUARED);
        for (int  i = locs.length; --i >= 0;) {
            util += scores[pack(locs[i].getMapLocation())] - CHAR_POSITIVE_OFFSET;
        }
        return util;
    }
}
