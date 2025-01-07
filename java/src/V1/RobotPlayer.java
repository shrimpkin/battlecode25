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
                    case MOPPER: Mopper.run(rc); break;
                    case SPLASHER: Splasher.run(rc); break; 
                    default: Tower.run(rc); break;
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
        Soldier.init(rc);
        Mopper.init(rc);
        Splasher.init(rc);
        BellmanFordNavigator.init(rc);
    }
}
