package V07Pathfinding.Nav;

import battlecode.common.MapLocation;

/**
 * Pathfinding manager. In this game this class is unnecessary I pretty much only use bugnav.
 */
public class Navigator {
    static BugPath bugPath;

    public Navigator(){
        bugPath = new BugPath();
    }

    public static void moveTo(MapLocation target){
        bugPath.moveTo(target);
    }
}
