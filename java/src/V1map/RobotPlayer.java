package V1map;

import V1map.Units.Mopper;
import V1map.Units.Soldier;
import V1map.Units.Splasher;
import V1map.Units.*;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while(true) {
            turnCount += 1;
            if(turnCount == 1) {
                init(rc);
            }
//            if (turnCount > 300 && rc.getLocation() != null) {
//                rc.resign(); // debug
//            }
            try {
                switch (rc.getType()){
                    case SOLDIER: Soldier.run(); break;
                    case MOPPER:  Mopper.run(); break;
                    case SPLASHER: Splasher.run(); break;
                    default: 
                        Tower.run(); 
                        break;
                }
            } catch(GameActionException e) {
                rc.setIndicatorString("exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }   

    public static void init(RobotController rc) {
        Unit.init(rc); // this doesn't do anything
        BellmanFordNavigator.init(rc);
    }
}
