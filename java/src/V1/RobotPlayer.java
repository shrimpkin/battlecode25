package V1;

import V1.Units.*;
import battlecode.common.*;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while(true) {
            turnCount += 1;
            if(turnCount == 1) {
                init(rc);
            }

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
