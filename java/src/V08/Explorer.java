package V08;

import V08.Tools.LocMap;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Explorer {
    RobotController rc = Globals.rc;
    final int w = Globals.mapWidth, h = Globals.mapHeight;
    int targetRound = -100;
    MapLocation exploreLoc = null;
    LocMap visited = new LocMap(rc);

    MapLocation[] checkLocs = {
        new MapLocation(w/2, h/2),
        new MapLocation(0,0),
        new MapLocation(w-1, 0),
        new MapLocation(0, h-1),
        new MapLocation(w-1, h-1)
    };
    static final int VisionRadius = GameConstants.VISION_RADIUS_SQUARED;

    public Explorer() {}

    MapLocation getExplorationTarget() {
        if ( rc.getRoundNum() - targetRound > 40
            || (exploreLoc != null && visited.get(exploreLoc) != 0)
        ) exploreLoc = null;

        if (exploreLoc == null) {
            if (rc.getID() % 2 == 0) {

            } else {

            }
        }
    }
}
