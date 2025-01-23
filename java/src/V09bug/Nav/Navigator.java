package V09bug.Nav;

import V09bug.Globals;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


public class Navigator extends Globals {
    public static void moveTo(MapLocation target) throws GameActionException {
        BugNavigator.moveTo(target);
    }
}