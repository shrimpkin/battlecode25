package V01resource.Units;

import V01resource.*;
import battlecode.common.*;

public class Tower extends Globals {
    // round to round info
    private static int roundsSinceBuilt = 0;
    private static int globalRoundNum = rc.getRoundNum();

    // tower information that stays the same from round to round
    private static MapLocation towerLoc;
    private static UnitType towerType;

    /** Generic tower action sequence. */
    public static void run() throws GameActionException {
        roundsSinceBuilt++;
        globalRoundNum++;

        if (roundsSinceBuilt == 1) {
            setup();
        }

        switch (towerType) {
            case LEVEL_ONE_DEFENSE_TOWER:
            case LEVEL_TWO_DEFENSE_TOWER:
            case LEVEL_THREE_DEFENSE_TOWER:
                break;
                // System.out.println("Defense tower not implemented");
            case LEVEL_ONE_MONEY_TOWER:
            case LEVEL_TWO_MONEY_TOWER:
            case LEVEL_THREE_MONEY_TOWER:
                break;
                // System.out.println("Money tower not implemented");
            case LEVEL_ONE_PAINT_TOWER:
            case LEVEL_TWO_PAINT_TOWER:
            case LEVEL_THREE_PAINT_TOWER:
                runPaintTower();
            case MOPPER:
            case SOLDIER:
            case SPLASHER:
            default:
                // System.out.println("TOWER.java HAS WRONG TYPE");
        }

        attack();
    }

    /** Sets info when tower is created. */
    public static void setup() throws GameActionException {
        towerLoc = rc.getLocation();
        towerType = rc.getType();
    }

    /** Attacks any nearby robots. */
    public static void attack() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, opponentTeam);
        for (RobotInfo robot : robotInfo) {
            while (rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    /** Paint tower action sequence. */
    public static void runPaintTower() throws GameActionException {
        if (rc.canBuildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST))) {
            rc.buildRobot(UnitType.SOLDIER, rc.getLocation().add(Direction.EAST));
        }
    }
}
