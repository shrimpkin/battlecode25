package V01splasher_defunct;

import java.util.Random;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;
    public static final Random rng = new Random(6147);

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;

    // TODO not entirely sure about this
    // this variable should represent the max dist a bot can move??
    public static int maxDistance;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();

        int halfSize = Math.max(Globals.mapWidth, Globals.mapHeight) / 2;
        maxDistance = halfSize * halfSize;
    }
}
