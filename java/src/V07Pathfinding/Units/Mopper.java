package V07Pathfinding.Units;

import V07Pathfinding.Comms;
import V07Pathfinding.Nav.Navigator;
import V07Pathfinding.Unit;
import battlecode.common.*;

public class Mopper extends Unit {
    static MapLocation TargetLoc;
    static Modes mode;

    private static int lastCalledHome;
    private static boolean wasWandering = false;

    public static void run() throws GameActionException {
        // determine mode and update info
        read();
        updateMode();
        updateTowerLocations();

        indicator = "[" + mode + "]; ";

        swingMop();
        removeEnemyPaint();
        move();

        if (rc.getNumberTowers() > 4 && rc.getChips() > 1200)
            canCompletePattern();

        _refill();

        rc.setIndicatorString(rc.getRoundNum() + ": {" + TargetLoc + "} " + indicator);
    }

    /********************
     ** CORE FUNCTIONS **
     ********************/

    /** Reads message queue and updates mode and target information */
    public static void read() throws GameActionException {
        Message[] msgs = rc.readMessages(-1);
        if (msgs.length == 0)
            return; // no messages to read

        // TODO: read more than 1 msg
        int[] decodedMsg = Comms.decodeMsg(msgs[0].getBytes());
        MapLocation requestedLoc = new MapLocation(decodedMsg[0], decodedMsg[1]);
        TargetLoc = requestedLoc;
        indicator += "->defense; ";
        lastCalledHome = 0;
        mode = Modes.DEFEND;
    }

    /** Set unit mode based on paint level and current mode */
    public static void updateMode() throws GameActionException {
        lastCalledHome++;

        if (mode == Modes.DEFEND) { // changes from DEFEND -> RUSH if unit has sat a long time
            if (lastCalledHome > 20) {
                mode = Modes.RUSH;
            }
            return; // if defending, can't change modes unless defense wears off
        }

        if (rc.getPaint() <= 20) {
            indicator += "->refill; ";
            mode = Modes.REFILL;

            // new target loc overrides any existing one
            TargetLoc = getClosestLocation(paintTowerLocations);
        } else {
            mode = Modes.NONE;
        }
    }

    /** Attacks direction with most enemies, if possible */
    public static void swingMop() throws GameActionException {
        Direction bestDir = getBestMopSwingDir();
        if (bestDir != null && rc.canMopSwing(bestDir)) {
            indicator += "swung " + bestDir + "; ";
            rc.mopSwing(bestDir);

            if (TargetLoc == null) {
                // move one step towards enemy
                indicator += "->unit; ";
                TargetLoc = rc.getLocation().add(bestDir);
            }
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
                        // remember loc of enemy paint to remove
                        indicator += "->paint; ";
                        TargetLoc = loc.getMapLocation();
                    }
                }
            }
        }
    }

    /** Movement decisions based on unit's target location */
    public static void move() throws GameActionException {
        if (TargetLoc != null) {
            boolean atLocation = TargetLoc.equals(rc.getLocation());
            boolean cannotReachTarget = rc.canSenseLocation(TargetLoc) && !rc.senseMapInfo(TargetLoc).isPassable();
            boolean isCloseToTarget = rc.getLocation().distanceSquaredTo(TargetLoc) < 2;

            if (atLocation || (cannotReachTarget && isCloseToTarget)) {
                // reset the target location if we're at the desired target
                // or if the target is a tower and we can sense the tower
                indicator += "reached target + recenter; ";
                TargetLoc = null;
                recenter();
                
            } else {
                // try to move to the target location
                indicator += "moving; ";
                Navigator.moveTo(TargetLoc);
                rc.setIndicatorLine(rc.getLocation(), TargetLoc, 100, 100, 0);
                wasWandering = false;    
            }

        } else {
            if (mode != Modes.DEFEND && mode != Modes.REFILL) { 
                // if not defending (rushing), then go somewhere new
                indicator += "wandering; ";
                wander(wasWandering);
                wasWandering = true;   

            } else {
                // defenders find optimal sitting spot and don't move for a few rounds
                // refills are also sitting until they successfully refill
                indicator += "defense recenter; ";
                recenter();
            }
        }
    }

    /*****************************
     ** MODE-SPECIFIC FUNCTIONS **
     *****************************/

    /** Attempts to refill */
    public static void _refill() throws GameActionException {
        if (mode != Modes.REFILL)
            return; // not in refill mode

        if (requestPaint(TargetLoc, 100)) {
            // upon successful refill, reset target and mode
            TargetLoc = null;
            mode = Modes.NONE;
        }
    }

    /*************
     ** HELPERS **
     *************/

    /** Returns cardinal direction with the most enemies, null otherwise */
    public static Direction getBestMopSwingDir() throws GameActionException {
        int[] numEnemies = {0, 0, 0, 0}; // N E S W
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
        
        return Direction.cardinalDirections()[bestDirIdx];
    }
}
