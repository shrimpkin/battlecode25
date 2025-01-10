package V1_2;

import java.util.Random;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;
    public static final Random rng = new Random(6147);

    public static Team myTeam;
    public static Team opponentTeam;
    public static int mapWidth;
    public static int mapHeight;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
    }
}
