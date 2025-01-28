package V09Markless.Units;

import V09Markless.Tools.*;
import V09Markless.Comms;
import V09Markless.Unit;
import V09Markless.Nav.Navigator;
import battlecode.common.*;

public class Splasher extends Unit {
    private static final int PaintLimit = UnitType.SPLASHER.paintCapacity;
    // weights for coloring in squares
    private static final int AllyWeight = -1, EnemyWeight = 4, EmptyWeight = 2;
    // also reward disrupting enemy SRPs and enemy towers
    private static final int EnemyTowerWeight = 20, EnemySRPWeight = 50;
    // arbitrary, but large enough int that tile_util + CHAR_POSITIVE_OFFSET >= 0, so avoid bad int->char->int conv.
    private static final int CHAR_POSITIVE_OFFSET = 90;
    // minimum utility at which the splasher will splash -- maybe make vary with round # / current paint amt
    private static final int MinUtil = 25; // starts off large -- decreases over rounds
    // thresholds for refilling (currently 300/6 = 50, 300/4 = 75), susceptible to change)
    private static final int RefillStart = PaintLimit / 6, RefillEnd = PaintLimit / 4;
    private static MapLocation TargetLoc;
    private static int NumRoundsSinceSplash = 0;
    private static MapLocation[] nearbyRuins;
    private static char[] scores = "\0".repeat(4096).toCharArray();
    private static Modes mode = Modes.NONE;

    private static MapLocation splashTarget;
    private static int splashUtil;

    public static void updateMode() {
        if (rc.getPaint() <= 50) {
            mode = Modes.REFILL;
            indicator += "{refilling}";
        } else {
            mode = Modes.NONE;
            indicator += "{normal}";
        }
    }

    public static void run() throws GameActionException {
        indicator = "";
        updateMode();
        updateTowerLocations();
        read();
        nearbyRuins = rc.senseNearbyRuins(-1);

        updateSplash();
        splash();

        move();
        canCompletePattern();
        refill();
        roundendComms();
        rc.setIndicatorString(indicator);
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

    /// reads msgs to target enemy
     public static void read() throws GameActionException {
         Message[] msgs = rc.readMessages(rc.getRoundNum());
         var loc = rc.getLocation();
         for (Message msg : msgs) {
             if (Comms.getType(msg.getBytes()) == CommType.TargetEnemy) {
                 MapLocation enemyLoc = Comms.getLocation(msg.getBytes());
                 var dist = loc.distanceSquaredTo(enemyLoc);
                 if (TargetLoc == null || dist < loc.distanceSquaredTo(TargetLoc)) {
                     TargetLoc = enemyLoc;
                 }
             }
         }
     }

    /// Splashes highest value location, if it's above min score value -- also don't kill self
    public static MapLocation updateSplash() throws GameActionException {
        splashTarget = null;
        splashUtil = 0;
        
        if (!rc.isActionReady() || rc.getPaint() <= 50) return null;
        // avoid expensive repeat computation
        precomputeSplashTileScores();

        // find best tile in range to splash
        int mostUtil = getSplashScore(rc.getLocation());
        for (var tile : rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared)) {
            var loc = tile.getMapLocation();
            if (!rc.canAttack(loc)) continue;
            int score = getSplashScore(loc);
            if (score > mostUtil) {
                splashTarget = loc;
                splashUtil = score;
            }
        }

        return null;
    }

    public static void splash() throws GameActionException {
        // maybe: make it less picky over time -- perhaps (minutil - roundNum/N) for some N > 200
        if (splashTarget != null) {
            if (splashUtil >= Math.max(2, Math.max(10, MinUtil - rc.getRoundNum()/100) - (NumRoundsSinceSplash-5)/10) && rc.canAttack(splashTarget)) {
                rc.attack(splashTarget);
                NumRoundsSinceSplash = 0;
                return;
            }
        }
        NumRoundsSinceSplash++;
    }

    static boolean wasWandering = false;
    /// Movement and wandering logic
    public static void move() throws GameActionException {
        if (mode == Modes.REFILL) {
            TargetLoc = getClosestLocation(paintTowerLocations);
        }

        if (TargetLoc != null) {
            boolean atLocation = TargetLoc.equals(rc.getLocation());
            boolean cannotReachTarget = rc.canSenseLocation(TargetLoc) && !rc.sensePassability(TargetLoc);
            boolean isCloseToTarget = rc.getLocation().isWithinDistanceSquared(TargetLoc, 2);

            if (atLocation || (cannotReachTarget && isCloseToTarget)) {
                TargetLoc = null;
                recenter();
            } else {
                Navigator.moveTo(TargetLoc, rc.getPaint() < 20);
            }
            wasWandering = false;
        } else {
            wander(wasWandering);
            wasWandering = true;
        }
    }

    /*************
     ** HELPERS **
     *************/
    private static int getTileScore(MapInfo info) throws GameActionException{
        int util = 0;
        var loc = info.getMapLocation();
        if (!info.isPassable()) return util;
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
            if (ruin.isWithinDistanceSquared(loc, 8)) {
                var hasRobot = rc.canSenseRobotAtLocation(ruin);
                if (hasRobot && ruin.isWithinDistanceSquared(loc, GameConstants.SPLASHER_ATTACK_ENEMY_PAINT_RADIUS_SQUARED)) {
                    var robot = rc.senseRobotAtLocation(ruin);
                    if (robot.getTeam() == opponentTeam) util += EnemyTowerWeight;
                }
                if (!hasRobot) nearRuin = true;
                break;
            }
        }
        // interfere with enemy build = more good, interfere with ally build = more bad
        if (nearRuin) paintWeight *= 2;
        util += paintWeight;
        // get the bonus from ally/enemy standing on the converted paint
        if (rc.canSenseRobotAtLocation(loc)) {
            if (info.hasRuin()) return util;
            if (info.getPaint() == PaintType.EMPTY) {
                util += GameConstants.PENALTY_NEUTRAL_TERRITORY;
            } else if (info.getPaint().isEnemy()){
                util +=  GameConstants.PENALTY_ENEMY_TERRITORY;
            }
        }
        return util;
    }

    /// precomputes the splash scores for individual tiles -- if they were to be in range -- for bytecode reduction
    private static void precomputeSplashTileScores() throws GameActionException {
        final int MAX_EFFECT_RADIUS_SQUARED = 10;
        scores = "\0".repeat(4096).toCharArray();
        for (MapInfo info : rc.senseNearbyMapInfos(MAX_EFFECT_RADIUS_SQUARED)) {
            scores[pack(info.getMapLocation())] = (char)(getTileScore(info) + CHAR_POSITIVE_OFFSET);
        }
    }

    /// Calculates the value obtained from a splash
    public static int getSplashScore(MapLocation center) throws GameActionException {
        int util = 0;
        var locs = rc.senseNearbyMapInfos(center, GameConstants.SPLASHER_ATTACK_ENEMY_PAINT_RADIUS_SQUARED);
        for (int i = locs.length; --i >= 0;) {
            util += (int)scores[pack(locs[i].getMapLocation())] - CHAR_POSITIVE_OFFSET;
        }
        return util;
    }
}
