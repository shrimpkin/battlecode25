package V08bug.Nav;

import V08bug.Globals;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


public class Navigator extends Globals {
    public static void moveTo(MapLocation target) throws GameActionException {
        BugNavigator.moveTo(target);
    }
}