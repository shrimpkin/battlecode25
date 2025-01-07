package V1.Units;

import java.util.Random;
import V1.*;
import battlecode.common.*;

public class Soldier extends Globals {
    static MapLocation tower_build_target = new MapLocation(0, 0);
    static final Random rng = new Random(6147);
    static String indicator;

    public static void run() throws GameActionException{
        indicator = "";
        //find_ruin(rc);
        
        if(tower_build_target != null) {
            indicator += "has target" + "(" + tower_build_target.x + "," + tower_build_target.y + ")";

            rc.setIndicatorString(indicator);

            try{
                Direction d = BellmanFordNavigator.getBestDirection(tower_build_target);
                indicator += "\nBellman done";
                
                if(rc.canMove(d)) {
                    rc.move(d);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            
        } else {
            indicator += "looking for target";

            //TODO: replace with exploration algorithm
            int random_direction = rng.nextInt(8);
            if(rc.canMove(Utils.directions[random_direction])) {
                rc.move(Utils.directions[random_direction]);
            }
        }


        rc.setIndicatorString(indicator);
    }

    //this robot will be used to repeated rebuild a paint tower
    public static void paint_tower(RobotController rc) {
        boolean is_paint_robot = false;

        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                is_paint_robot = true;
            }
        }

        if(!is_paint_robot) return;


    }

    public static void find_ruin(RobotController rc) {
        if(tower_build_target != null) return;

        MapInfo[] info = rc.senseNearbyMapInfos();
        for(MapInfo tile : info) {
            if(tile.hasRuin()) tower_build_target = tile.getMapLocation();
        }
    }
}
