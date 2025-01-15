package V04BOTweaked.Units;

import V04BOTweaked.Nav.Navigator;
import V04BOTweaked.Unit;
import battlecode.common.*;

// TODO: ahahhh fix all of this
public class Mopper extends Unit {
    static MapLocation TargetLoc = null;
    static Modes mode = Modes.RUSH;

    public static void updateMode() throws GameActionException {
        if (rc.getPaint() < 30 && !rc.senseMapInfo(rc.getLocation()).getPaint().isAlly()) {
            mode = Modes.REFILL;
        } else {
            mode = Modes.RUSH;
        }
    }
    private static boolean wasWandering = false;
    public static void run() throws GameActionException {
        indicator = "";
        TargetLoc = null;
        updateMode();
        updateTowerLocations();
        indicator += "[Mode: " + mode + "] ";

        if (rc.readMessages(-1).length > 0) {
            defend();
        }
        
        if (mode == Modes.REFILL) {
            TargetLoc = getClosestLocation(paintTowerLocations);
            indicator += "(" + paintTowerLocations.size + ")";
        }
        if (mode == Modes.RUSH || TargetLoc == null) {
            removeEnemyPaint();
        }

        if (TargetLoc != null) {
            Navigator.moveTo(TargetLoc);
            rc.setIndicatorLine(rc.getLocation(), TargetLoc, 100, 100, 0);
            // THE NAV DOESN'T GET CLOSE ENOUGH!!!!!!!!!!!!!!
            if (rc.canMove(rc.getLocation().directionTo(TargetLoc))) {
                rc.move(rc.getLocation().directionTo(TargetLoc));
            }
            wasWandering = false;
        } else {
            wander(wasWandering);
            wasWandering = true;
        }

        rc.setIndicatorString(rc.getRoundNum() + ": " + indicator);
    }

    public static void defend() throws GameActionException {
        indicator += "defending; ";
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            if (rc.canMopSwing(rc.getLocation().directionTo(robot.getLocation()))) {
                rc.mopSwing(rc.getLocation().directionTo(robot.getLocation()));
            }
        }
    }

    public static void removeEnemyPaint() throws GameActionException {
        for (MapInfo loc : rc.senseNearbyMapInfos()) {
            if (loc.getPaint().isEnemy()) {
                indicator += loc.getMapLocation().toString() + ", ";

                if (rc.canAttack(loc.getMapLocation())) {
                    rc.attack(loc.getMapLocation());
                    indicator += "attacked";
                } else {
                    TargetLoc = loc.getMapLocation();
                }
            }
        }
    }
}
