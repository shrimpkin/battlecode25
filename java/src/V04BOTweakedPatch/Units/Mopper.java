package V04BOTweakedPatch.Units;

import V04BOTweakedPatch.Unit;
import V04BOTweakedPatch.Nav.Navigator;
import battlecode.common.*;

// TODO: maybe fix modes

public class Mopper extends Unit {
    static MapLocation TargetLoc = null;
    static Modes mode = Modes.RUSH;
    private static boolean wasWandering = false;

    public static void run() throws GameActionException {
        updateMode();
        updateTowerLocations();

        indicator = "[" + mode + "]; ";

        defend();     
        attack();
        removeEnemyPaint();
        move();

        rc.setIndicatorString(rc.getRoundNum() + ": " + indicator);
    }

    /********************
     ** CORE FUNCTIONS **
     ********************/

    /** Set unit mode based on paint level and nearby paint */
    public static void updateMode() throws GameActionException {
        if (mode == Modes.DEFEND) 
            return;

        if (rc.getPaint() < 30 && !rc.senseMapInfo(rc.getLocation()).getPaint().isAlly()) {
            mode = Modes.REFILL;
        } else {
            mode = Modes.RUSH;
        }
    }

    /** Checks if a nearby tower needs defending */
    public static void defend() throws GameActionException {
        // TODO
        // this should set TargetLoc
    }

    /** Attacks direction with most enemies, if possible */
    public static void attack() throws GameActionException {
        Direction bestDir = getBestMopSwingDir();
        if (bestDir != null && rc.canMopSwing(bestDir)) {
            indicator += "swung " + bestDir + "; ";
            rc.mopSwing(bestDir);
            TargetLoc = rc.getLocation().add(bestDir);
        }
    }

    /** Unit tries to remove enemy paint in its attack-able radius */
    public static void removeEnemyPaint() throws GameActionException {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            if (loc.getPaint().isEnemy()) {
                if (rc.canAttack(loc.getMapLocation())) {
                    rc.attack(loc.getMapLocation());
                } else {
                    if (TargetLoc == null) {
                        // remember loc of enemy paint to remove if there's nothing else to do 
                        TargetLoc = loc.getMapLocation();
                    }
                }
            }
        }
    }

    /** Movement decisions based on unit's target location */
    public static void move() throws GameActionException {
        // TODO: revamp this to use dynamic TargetLoc
        if (TargetLoc != null) {
                Navigator.moveTo(TargetLoc);
                rc.setIndicatorLine(rc.getLocation(), TargetLoc, 100, 100, 0);
                // THE NAV DOESN'T GET CLOSE ENOUGH!!!!!!!!!!!!!!
                if (rc.canMove(rc.getLocation().directionTo(TargetLoc))) {
                    rc.move(rc.getLocation().directionTo(TargetLoc));
                    TargetLoc = null;
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

    /** Returns cardinal direction with the most enemies, null otherwise */
    public static Direction getBestMopSwingDir() throws GameActionException {
        int[] numEnemies = {0, 0, 0, 0};
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, opponentTeam);
        MapLocation currLoc = rc.getLocation();

        if (nearbyRobots.length == 0)
            return null; // no nearby enemy robots

        for (RobotInfo robot : nearbyRobots) {
            for (int dir = 0; dir < 4; dir++) { // check each direction
                for (int i = 0; i < 6; i++) { // check each impact square in this direction
                    if (robot.getLocation().equals(currLoc.translate(dxMop[dir][i], dyMop[dir][i]))) {
                        numEnemies[dir]++;
                        break;
                    }
                }
            }    
        }

        int bestDir = 0;
        int bestDirIdx = -1;

        for (int i = 0; i < 4; i++) {
            if (numEnemies[i] >= bestDir) {
                bestDir = numEnemies[i];
                bestDirIdx = i;
            }
        }

        if (bestDir == 0) 
            return null; // no hit-able enemies in any direction
        
        return Direction.cardinalDirections()[bestDirIdx]; // N E S W
    }
}
