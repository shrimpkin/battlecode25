package V05.Units;

import V05.Comms;
import V05.Unit;
import V05.Nav.Navigator;
import battlecode.common.*;

// TODO: maybe fix modes

public class Mopper extends Unit {
    static MapLocation TargetLoc;
    static Modes mode;
    static MapLocation currTower;

    private static int lastCalledHome;
    private static boolean wasWandering = false;

    public static void run() throws GameActionException {
        updateMode();
        updateTowerLocations();

        indicator = "[" + mode + "]; ";

        read();
        swingMop();
        removeEnemyPaint();
        move();

        rc.setIndicatorString(rc.getRoundNum() + ": {" + TargetLoc + "} " + indicator);
    }

    /********************
     ** CORE FUNCTIONS **
     ********************/

    /** Set unit mode based on paint level and nearby paint (TODO) */
    public static void updateMode() throws GameActionException {
        lastCalledHome++;

        if (mode == Modes.DEFEND) { // changes from DEFEND -> RUSH if unit has sat a long time
            if (lastCalledHome > 30) {
                mode = Modes.RUSH;
            }
        }
    }

    /** Reads message queue and updates mode and target information */
    public static void read() throws GameActionException {
        Message[] msgs = rc.readMessages(-1);
        if (msgs.length == 0)
            return; // no messages to read

        // TODO: read more than 1 msg
        int[] decodedMsg = Comms.decodeMsg(msgs[0].getBytes());
        // MapLocation senderLoc = new MapLocation(decodedMsg[0], decodedMsg[1]);
        MapLocation requestedLoc = new MapLocation(decodedMsg[2], decodedMsg[3]);
        TargetLoc = requestedLoc;
        indicator += "->defense; ";
        lastCalledHome = 0;
        mode = Modes.DEFEND;
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
            if (TargetLoc.equals(rc.getLocation()) || (rc.canSenseLocation(TargetLoc) && !rc.senseMapInfo(TargetLoc).isPassable())) {
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
                // // THE NAV DOESN'T GET CLOSE ENOUGH!!!!!!!!!!!!!!
                // if (rc.canMove(rc.getLocation().directionTo(TargetLoc))) {
                //     rc.move(rc.getLocation().directionTo(TargetLoc));
                // }
                wasWandering = false;    
            }

        } else {
            if (mode != Modes.DEFEND) { 
                // if not defending (rushing), then go somewhere new
                indicator += "wandering; ";
                wander(wasWandering);
                wasWandering = true;   

            } else {
                // defenders find optimal sitting spot and don't move for a few rounds
                indicator += "defense recenter; ";
                recenter();
            }
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
