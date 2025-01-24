package V09;

import V09.Units.Mopper;
import V09.Units.Soldier;
import V09.Units.Splasher;
import V09.Units.Tower;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            if (rc.getRoundNum() > 1000) rc.resign();
            turnCount += 1;
            if (turnCount == 1) init(rc);
            try {
                switch (rc.getType()) {
                    case SOLDIER:  Soldier.run();  break;
                    case MOPPER:   Mopper.run();   break;
                    case SPLASHER: Splasher.run(); break;
                    default:       Tower.run();    break;
                }
            } catch (GameActionException e) {
                rc.setIndicatorString("exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static void init(RobotController rc) {
        Globals.init(rc);
    }
}
