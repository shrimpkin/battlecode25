package V04.Units;

import V04.*;
import V04.Nav.*;
import battlecode.common.*;

public class Soldier extends Unit {
    static FastIntSet symmetryLocations = new FastIntSet();
    static MapLocation targetLocation = null;

    // RUSH
    static boolean addedPaintTowerSymmetryLocs = false;
    static boolean addedMoneyTowerSymmetryLocs = false;

    // BOOM
    static MapLocation ruinTarget;
    static MapLocation eastOfRuinTarget;
    static MapLocation westOfRuinTarget;
    static MapLocation northOfRuinTarget;

    public static void run() throws GameActionException {
        indicator = "";

        updateMode();
        updateTowerLocations();

        if (mode == Modes.REFILL) {
            targetLocation = getClosestLocation(paintTowerLocations);
            move();
        }

        if (mode == Modes.RUSH) {
            getRushTargetsBySymmetry();
            updateSymmetryTargets();
            markOneRuinTile();
            targetLocation = getRushMoveTarget();
            move();
        }

        if (mode == Modes.ATTACK) {
            markOneRuinTile();
            targetLocation = getRushMoveTarget();
            move();
            paintBelow();
        }

        if (mode == Modes.BOOM) {
            findValidTowerPosition();
            paintTowerPattern();
            markOneRuinTile();

            if (ruinTarget != null) {
                targetLocation = new MapLocation(ruinTarget.x - 2 + (nextInt() % 4),
                        ruinTarget.y - 2 + (nextInt() % 4));
            } else {
                targetLocation = null;
            }

            move();
            paintBelow();
        }

        attack();
        tesselate();
        canCompletePattern();
        debug();
    }

    public static boolean shouldBeSecondary(MapLocation loc) {
        int x = loc.x;
        int y = loc.y;

        if (y%3 == 0 || y %3 == 1) {
            return (x+y)%2 == 0;
        } else {
            int offset = (y-2)/3;
            return ((x+y)%4 == ((offset*2) % 4));
        }
    }

    /** Paint SRP patterns (tmp) */
    public static void tesselate() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            if (tile.getPaint().isAlly() && rc.getNumberTowers() < 10)
                continue; // already painted by us; prevents overriding tower patterns
            if (tile.getPaint().isEnemy())
                continue; // can't paint over enemy paint

            MapLocation loc = tile.getMapLocation();
            // int x = loc.x;
            // int y = loc.y;
            boolean isSecondary = shouldBeSecondary(loc);
            // if(y % 3 == 0 || y % 3 == 1) {
            //     isSecondary = ((x + y) % 2 == 0);
            // } 

            // if(y % 3 == 2) {
            //     int offset = (y - 2) / 3;
            //     isSecondary = ((x + y) % 4 == ((offset * 2) % 4));
            // }
            
            if(tile.getPaint().isSecondary() == isSecondary) continue;
            
            if (rc.canAttack(loc)) {
                rc.attack(loc, isSecondary);
            }    
        }
    }

    public static void canCompletePattern() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            MapLocation loc = tile.getMapLocation();
            if(loc.y % 3 != 2) continue;
            

            if (rc.canCompleteResourcePattern(loc)) {
                rc.completeResourcePattern(loc);
            }
        }
    }

    /** Changes mode based on criteria I haven't quite figured out yet @aidan */
    public static void updateMode() throws GameActionException {
        // if(rc.getPaint() <= 50) {
        // mode = Modes.GET_PAINT;
        // return;
        // }

        if (rc.getNumberTowers() == 25) {
            mode = Modes.ATTACK;
            return;
        }

        if (rc.getRoundNum() <= rc.getMapHeight() + rc.getMapWidth() 
            && rc.getMapHeight() <= 25 && rc.getMapWidth() <= 25) {
            mode = Modes.RUSH;
            return;
        }

        mode = Modes.BOOM;
        return;
        
    }

    /************************************************************************\
    |*                                 SIT                                  *|
    \************************************************************************/

    /************************************************************************\
    |*                                 RUSH                                 *|
    \************************************************************************/

    /** Uses map symmetry and our tower positions to generate possible locations for enemy towers */
    public static void getRushTargetsBySymmetry() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for (RobotInfo robot : robots) {
            boolean isPaintTower = isPaintTower(robot.getType());
            boolean isMoneyTower = isMoneyTower(robot.getType());

            if (!(isPaintTower || isMoneyTower))
                return;
            if (isPaintTower && addedPaintTowerSymmetryLocs)
                continue;
            if (isMoneyTower && addedMoneyTowerSymmetryLocs)
                continue;

            int x = robot.getLocation().x;
            int y = robot.getLocation().y;

            MapLocation vert = new MapLocation(mapWidth - x - 1, y);
            MapLocation hort = new MapLocation(x, mapHeight - y - 1);
            MapLocation mirr = new MapLocation(mapWidth - x - 1, mapHeight - y - 1);

            symmetryLocations.add(pack(vert));
            symmetryLocations.add(pack(hort));
            symmetryLocations.add(pack(mirr));

            if (isPaintTower)
                addedPaintTowerSymmetryLocs = true;
            if (isMoneyTower)
                addedMoneyTowerSymmetryLocs = true;
        }
    }

    /** Updates targets in symmetryLocations by removing them if we know they aren't towers */
    public static void updateSymmetryTargets() throws GameActionException {
        for (int i = 0; i < symmetryLocations.size; i++) {
            MapLocation tower = unpack(symmetryLocations.keys.charAt(i));

            if (!rc.canSenseLocation(tower))
                continue;
            RobotInfo info = rc.senseRobotAtLocation(tower);

            // there is no unit or the unit is not a paint or money tower
            // hence the tower is not there and we should remove
            if (info == null || !(isPaintTower(info.getType()) || isMoneyTower(info.getType()))) {
                symmetryLocations.remove(pack(tower));
            }
        }
    }

    /** Returns location of a tower that has been seen, otherwise nearest tower location */
    public static MapLocation getRushMoveTarget() throws GameActionException {
        int minDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        if (enemyTowerLocations.size > 0) {
            return unpack(enemyTowerLocations.keys.charAt(0));
        }

        for (int i = 0; i < symmetryLocations.size; i++) {
            MapLocation tower = unpack(symmetryLocations.keys.charAt(i));
            int distanceToTower = tower.distanceSquaredTo(rc.getLocation());

            if (distanceToTower < minDistance) {
                minDistance = distanceToTower;
                bestLocation = tower;
            }
        }

        return bestLocation;
    }

    /** Paints one tile of every adjacent ruin */
    public static void markOneRuinTile() throws GameActionException {
        MapLocation locationToMark = null;
        for (int i = 0; i < unusedRuinLocations.size; i++) {
            MapLocation ruinLocation = unpack(unusedRuinLocations.keys.charAt(i));

            if (!rc.canSenseLocation(ruinLocation))
                continue;

            MapInfo[] squaresToMark = rc.senseNearbyMapInfos(ruinLocation, 8);
            for (MapInfo info : squaresToMark) {
                PaintType paint = info.getPaint();
                if (paint.isAlly())
                    return;

                if (paint.equals(PaintType.EMPTY) && rc.canAttack(info.getMapLocation())) {
                    locationToMark = info.getMapLocation();
                    break;
                }
            }

            if (locationToMark != null)
                break;
        }

        if (locationToMark != null) {
            rc.attack(locationToMark);
        }
    }

    /** Moves to target location, if no target wanders */
    public static void move() throws GameActionException {
        if (targetLocation != null) {
            Navigator.moveTo(targetLocation);
        } else {
            wander();
        }
    }

    /************************************************************************\
    |*                                 BOOM                                 *|
    \************************************************************************/

    /** Attempts to build clock tower on nearby empty ruins */
    public static void findValidTowerPosition() throws GameActionException {
        ruinTarget = null;
        MapLocation[] ruinLocations = rc.senseNearbyRuins(-1);

        for (MapLocation ruin : ruinLocations) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);

            if (robot != null)
                continue;

            MapInfo[] ruinSurroundings = rc.senseNearbyMapInfos(ruin, 8);
            boolean hasEnemyPaint = false;
            // Checks for nearby enemy paint that would prevent building a clock tower
            for (MapInfo loc : ruinSurroundings) {
                if (loc.getPaint().isEnemy()) {
                    hasEnemyPaint = true;
                    break;
                }
            }

            if (!hasEnemyPaint) {
                ruinTarget = ruin;
                break;
            }
        }
    }

    /** Determines which tower type to build */
    public static UnitType decideTowerType() throws GameActionException {
        if (ruinTarget == null)
            return null; // no ruin to build on

        eastOfRuinTarget = ruinTarget.add(Direction.EAST);
        westOfRuinTarget = ruinTarget.add(Direction.WEST);
        northOfRuinTarget = ruinTarget.add(Direction.NORTH);

        if (!rc.canSenseLocation(northOfRuinTarget)
                || !rc.canSenseLocation(eastOfRuinTarget)
                || !rc.canSenseLocation(westOfRuinTarget))
            return null;

        if (rc.senseMapInfo(northOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY))
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;

        if (rc.senseMapInfo(eastOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY))
            return UnitType.LEVEL_ONE_MONEY_TOWER;

        if (rc.senseMapInfo(westOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY))
            return UnitType.LEVEL_ONE_PAINT_TOWER;

        return null;
    }

    /** Picks a tower to build and builds it */
    public static void paintTowerPattern() throws GameActionException {
        if (ruinTarget == null)
            return; // no ruin to build on 
        if (!rc.canSenseLocation(ruinTarget))
            return; // not close enough to build

        UnitType towerType = decideTowerType();

        northOfRuinTarget = ruinTarget.add(Direction.NORTH);
        eastOfRuinTarget = ruinTarget.add(Direction.EAST);
        westOfRuinTarget = ruinTarget.add(Direction.WEST);

        if (towerType == null) { // TODO: logic for what tower to make
            int key = rc.getNumberTowers() % 2;
            if (key < 1) {
                if (rc.canMark(eastOfRuinTarget)) {
                    towerType = UnitType.LEVEL_ONE_MONEY_TOWER;
                    rc.mark(eastOfRuinTarget, false);
                }
            } else if (key < 2) {
                if (rc.canMark(westOfRuinTarget)) {
                    towerType = UnitType.LEVEL_ONE_PAINT_TOWER;
                    rc.mark(westOfRuinTarget, false);
                }
            } else {
                if (rc.canMark(northOfRuinTarget)) {
                    towerType = UnitType.LEVEL_ONE_DEFENSE_TOWER;
                    rc.mark(northOfRuinTarget, false);
                }
            }

            if (towerType == null) 
                return; // can't mark tower type
        }

        MapInfo[] ruinSurroundings = rc.senseNearbyMapInfos(ruinTarget, 8);
        boolean[][] paintPattern = rc.getTowerPattern(towerType);

        if (rc.canCompleteTowerPattern(towerType, ruinTarget)) {
            rc.completeTowerPattern(towerType, ruinTarget);
        }

        for (MapInfo info : ruinSurroundings) {
            MapLocation loc = info.getMapLocation();
            if (!rc.canAttack(loc))
                continue; // unable to attack
            if (info.hasRuin())
                continue; // can't paint ruin

            int x = ruinTarget.x - loc.x + 2;
            int y = ruinTarget.y - loc.y + 2;

            PaintType paint = info.getPaint();
            if (info.getPaint().isEnemy())
                continue; // can't paint enemy paint

            if (paintPattern[x][y] && paint != PaintType.ALLY_SECONDARY) {
                rc.attack(loc, true);
                indicator += loc.toString();
            } else if (!paintPattern[x][y] && paint != PaintType.ALLY_PRIMARY) {
                rc.attack(loc, false);
                indicator += loc.toString();
            }
        }
    }

    /** Paints square below unit */
    public static void paintBelow() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if (!rc.senseMapInfo(myLocation).getPaint().equals(PaintType.EMPTY))
            return; // tile is already painted

        if (rc.canAttack(myLocation)) {
            System.out.println("painting below!");
            rc.attack(myLocation, shouldBeSecondary(myLocation));
        }
    }

    /** Attacks money and paint towers */
    public static void attack() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, opponentTeam)) {
            if ((isPaintTower(robot.getType())
                    || isMoneyTower(robot.getType()))
                    && rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    /************************************************************************\
    |*                                DEBUG                                 *|
    \************************************************************************/

    /** Prints all debug info */
    public static void debug() {
        if (!in_debug)
            return;

        switch (mode) {
            case BOOM:
                indicator += "Boom: ";
                break;
            case NONE:
                indicator += "None: ";
                break;
            case RUSH:
                indicator += "Rush: ";
                break;
            case SIT:
                indicator += "Sit: ";
                break;
            case REFILL:
                indicator += "Getting Paint: ";
                break;
            default:
                break;
        }

        indicator += "Move Target: ";
        if (targetLocation != null) {
            indicator += targetLocation.toString() + "\n";
        } else {
            indicator += "null target\n";
        }

        if (mode == Modes.RUSH) {
            for (int i = 0; i < enemyTowerLocations.size; i++) {
                indicator += unpack(enemyTowerLocations.keys.charAt(i)).toString() + ", \n";
            }

            for (int i = 0; i < symmetryLocations.size; i++) {
                indicator += unpack(symmetryLocations.keys.charAt(i)).toString() + ", \n";
            }
        }

        if (mode == Modes.BOOM) {
            if (ruinTarget != null)
                indicator += ruinTarget.toString();
        }

        rc.setIndicatorString(indicator);
    }
}
