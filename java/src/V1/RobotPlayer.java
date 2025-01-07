package V1;

import V1.Units.*;
import battlecode.common.*;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while(true) {
            turnCount += 1;

            try {
                switch (rc.getType()){
                    case SOLDIER: Soldier.run(rc); break; 
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

    public static void runTower(RobotController rc) {

    }
}
