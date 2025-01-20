package V08copy;

import V08copy.Units.Mopper;
import V08copy.Units.Soldier;
import V08copy.Units.Splasher;
import V08copy.Units.Tower;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
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
