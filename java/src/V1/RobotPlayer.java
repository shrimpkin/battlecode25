package V1;

import V1.Units.*;
import battlecode.common.*;

public class RobotPlayer {
    static int turnCount = 0;

    public static void run(RobotController rc) throws GameActionException {
        while(true) {
            switch (rc.getType()){
                case SOLDIER: Soldier.run(rc); break; 
                case MOPPER: Mopper.run(rc); break;
                case SPLASHER: Splasher.run(rc); break; 
                default: Tower.run(rc); break;
            }

            turnCount += 1;
            Clock.yield();
        }
    }   

    public static void runTower(RobotController rc) {

    }
}
