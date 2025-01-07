package V1.Units;

import battlecode.common.*;

public class Tower {
    public static int turn_num = 0;

    public static void run(RobotController rc) throws GameActionException {
        turn_num++;
        rc.setIndicatorString(rc.getPaint() + " ");
        
        if(rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            run_paint(rc);
        }
    }

    public static void run_money(RobotController rc) {

    }

    public static void run_defense(RobotController rc) {

    }

    /**
     * Special behavior for paint tower
     * @param rc
     */
    public static void run_paint(RobotController rc) throws GameActionException {
        //building a solider to do some paint testing
        if(turn_num == 1) {
            rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
        }

        //current going to self destruct if it can be immediately rebuilt
        
    }
}
