package V07.Nav;

import V07.Globals;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


public class Navigator extends Globals {
    
    public static void moveTo(MapLocation target) throws GameActionException {
        BugPath path = new BugPath();
        path.moveTo(target);
    }
    
}