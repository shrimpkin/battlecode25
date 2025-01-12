package V01resource.Units;

import V01resource.*;
import battlecode.common.*;

public class Soldier extends Unit {
    private static int refillThreshold = 50;
    private static MapLocation currPOI;

    private static MapLocation currRuinLoc;
    private static MapLocation currSRPLoc;
    // private static MapLocation target_location;

    /** Generic soldier action sequence */
    public static void run() throws GameActionException {
        // rc.setIndicatorString(rc.getRoundNum() + " ran Soldier.run");
        update_paint_tower_loc();

        initRuin();
        paintResourcePattern(); // these are not being painted immediately

        if (rc.getPaint() < refillThreshold) {
            refill(200);
        }

        if (currPOI != null) {
            Navigator.moveTo(currPOI);
        } else {
            wander();
        }
    }

    /** Initializes nearby ruins to new towers */
    public static void initRuin() throws GameActionException {
        if (currRuinLoc == null) {
            currRuinLoc = findRuin();
        }

        markRuinAs(currRuinLoc, UnitType.LEVEL_ONE_PAINT_TOWER);
        paintNearbyMarks();

        if (completeRuinAs(currRuinLoc, UnitType.LEVEL_ONE_PAINT_TOWER)) {
            currRuinLoc = null;
        }
    }

    // can override ruin-to-tower conversion and it fights over markings
    // BUT it is able to provide SRPs
    public static void paintResourcePattern() throws GameActionException {
        if (rc.getLocation().x % 5 != 0 || rc.getLocation().y % 5 != 0) 
            return;
        if (!rc.canMarkResourcePattern(rc.getLocation())) 
            return;

        currSRPLoc = rc.getLocation();
        currPOI = currSRPLoc;
        rc.markResourcePattern(currSRPLoc);

        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            if (tile.getMark().isAlly()) {
                if (rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
                }
            }
        }

        if (rc.canCompleteResourcePattern(currSRPLoc)) {
            rc.completeResourcePattern(currSRPLoc);
            currSRPLoc = null;
        }
    }

    /** Paint all nearby marked tiles */
    public static void paintNearbyMarks() throws GameActionException {
        boolean missedPaint = false;

        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            PaintType paintType = tile.getPaint();
            if (paintType.isEnemy())
                continue; // can't paint over enemy squares
            if (paintType == tile.getMark() || tile.getMark() == PaintType.EMPTY)
                continue; // already painted the right way or no paint marker

            if (rc.canAttack(tile.getMapLocation())) {
                rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
            } else {
                missedPaint = true;
                currPOI = tile.getMapLocation();
            }
        }

        if (!missedPaint) {
            currPOI = null;
        }
    }
}
