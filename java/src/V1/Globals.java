package V1;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;

    public static Team myTeam;
    public static Team opponentTeam;

    public static void init(RobotController robotController) {
        rc = robotController;
        
        myTeam = rc.getTeam();
        opponentTeam = rc.getTeam().opponent();
    }
}
