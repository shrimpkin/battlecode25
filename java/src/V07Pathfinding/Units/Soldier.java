package V07Pathfinding.Units;

import V07Pathfinding.FastIntSet;
import V07Pathfinding.Nav.Navigator;
import V07Pathfinding.Unit;
import battlecode.common.*;

public class Soldier extends Unit {
    static FastIntSet symmetryLocations = new FastIntSet();
    static MapLocation targetLocation = null;
    static int lastRefillEnd;
    // RUSH
    static boolean addedPaintTowerSymmetryLocs = false;
    static boolean addedMoneyTowerSymmetryLocs = false;

    // BOOM
    static MapLocation ruinTarget;
    static MapLocation lastRuinTarget;
    static int roundNum;

    public static void run() throws GameActionException {
        indicator = "";
        Modes prev = mode;
        roundNum = rc.getRoundNum();

        updateMode();
        if (prev == Modes.REFILL && mode != Modes.REFILL) lastRefillEnd = roundNum;
        updateTowerLocations();

        if (mode == Modes.REFILL) {
            targetLocation = getClosestLocation(paintTowerLocations);
            move();
            // TODO: betterize this
            if (rc.getPaint() >= 15 && targetLocation != null && rc.getLocation().distanceSquaredTo(targetLocation) < rc.getPaint() + 10) {
                paintBelow();
            }
            requestPaint(targetLocation, 200);
        }

        if (mode == Modes.ATTACK) {
            markOneRuinTile();
            targetLocation = getRushMoveTarget();
            move();
            paintBelow();
        }

        if (mode == Modes.BOOM) {
            if (lastRuinTarget != null && !rc.canSenseLocation(lastRuinTarget)) {
                targetLocation = lastRuinTarget;
                rc.setIndicatorDot(lastRuinTarget, 255, 255, 0);
            } else {
                lastRuinTarget = null;
                findValidTowerPosition(); // 1030 bytecode
                markOneRuinTile();
                targetLocation = getClosestUnpaintedTarget();
            }
            // NAV NOT CLOSE ENOUGH
            if (targetLocation != null && rc.canMove(rc.getLocation().directionTo(targetLocation))) {
                rc.move(rc.getLocation().directionTo(targetLocation));
            }
            paintTowerPattern(); // 1614 bytecode
            move();
            paintBelow();
        }
        updateSeen();
        attack();

        if (rc.getChips() < 800 && mode != Modes.REFILL || rc.getPaint() > 150 ) { // not interfering with ruin construction
            tessellate(); // 2600 bytecode

        }

        if (rc.getNumberTowers() > 4 && rc.getChips() > 1200) {
            canCompletePattern();
        }

        debug();
    }

    public static boolean shouldBeSecondary(MapLocation loc) {
        return pattern[loc.x % 4][loc.y % 4] == 1;
    }

    public static boolean paintSRP(MapInfo tile) throws GameActionException {
        MapLocation loc = tile.getMapLocation();
        boolean isSecondary = shouldBeSecondary(loc);
        PaintType idealPaint = isSecondary ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
        PaintType paintType = tile.getPaint();

        if(!rc.canAttack(loc)) return false;
        if (paintType.isEnemy()) return false;
        if (tile.hasRuin()) return false;
        if (roundNum < 50 && paintType.isAlly()) return false;
        if(paintType.equals(idealPaint)) return false;

        for (MapLocation ruin : nearbyRuins) {
            if (rc.canSenseRobotAtLocation(ruin)) continue; // only consider unbuilt ruins
            if (ruin.isWithinDistanceSquared(loc, 8)) {
                return false; // inside the 5x5 area of an unbuilt ruin -- don't tessellate here
            }
        }

        rc.setIndicatorDot(loc, 40, 40, 128);
        rc.attack(loc, isSecondary);
        return true;        
    }

    /** Paint SRP patterns (tmp) */
    public static void tessellate() throws GameActionException {
        for (MapInfo tile : rc.senseNearbyMapInfos(rc.getType().actionRadiusSquared)) {
            if (paintSRP(tile)) return;
        }
    }

    /** Changes mode based on criteria I haven't quite figured out yet @aidan */
    public static void updateMode() throws GameActionException {
        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
            mode = Modes.ATTACK;
            return;
        }

        // intermittent rushing in midgame
        var span = Math.max(mapHeight, mapWidth);
        if (roundNum > 200 && rc.getPaint() > 2*span && roundNum % 100 < span) {
            mode = Modes.ATTACK;
            return;
        }

        if(rc.getPaint() <= 40 && roundNum - lastRefillEnd > 10) {
            mode = Modes.REFILL;
            if (ruinTarget != null) {
                lastRuinTarget = ruinTarget;
                indicator += "last ruin target: " + ruinTarget;
            }
            return;
        }

        if(enemyTowerLocations.size > 0 && unusedRuinLocations.size == 0) {
            mode = Modes.ATTACK;
            return;
        }

        mode = Modes.BOOM;
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
            if (!rc.canSenseLocation(ruinLocation)) continue;

            MapInfo[] squaresToMark = rc.senseNearbyMapInfos(ruinLocation, 8);
            for (MapInfo info : squaresToMark) {
                PaintType paint = info.getPaint();
                if (paint.isAlly()) return;

                if (paint.equals(PaintType.EMPTY) && rc.canAttack(info.getMapLocation())) {
                    locationToMark = info.getMapLocation();
                    break;
                }
            }

            if (locationToMark != null)
                break;
        }

        if (locationToMark != null) {
            rc.setIndicatorDot(locationToMark, 255, 0, 0);
            rc.attack(locationToMark);
        }
    }

    /** Moves to target location, if no target wanders */
    private static boolean wasWandering = false;
    public static void move() throws GameActionException {
        if (targetLocation != null) {
            int start = Clock.getBytecodeNum();
            Navigator.moveTo(targetLocation);
            int end = Clock.getBytecodeNum();
            System.out.println("Delta: " + (end - start));
            wasWandering = false;
        } else {
            wander(wasWandering);
            wasWandering = true;
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
            int numNearbySoliders = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(ruin, 8, myTeam)) {
                if(robot.getType().equals(UnitType.SOLDIER)) numNearbySoliders++;
            }
            if(numNearbySoliders > 2) continue;


            RobotInfo robot = rc.senseRobotAtLocation(ruin);

            if (robot != null) continue;

            MapInfo[] ruinSurroundings = rc.senseNearbyMapInfos(ruin, 8);
            boolean hasEnemyPaint = false;
            // Checks for nearby enemy paint that would prevent building a clock tower
            for (MapInfo loc : ruinSurroundings) {
                if (loc.getPaint().isEnemy()) {
                    hasEnemyPaint = true;
                    break;
                }
            }

            boolean isCloser;
            if(ruinTarget != null)
                isCloser = rc.getLocation().distanceSquaredTo(ruin) < rc.getLocation().distanceSquaredTo(ruinTarget);
            else isCloser = true;

//            MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
//            boolean isClosestToCenter;
//            if(ruinTarget != null) isClosestToCenter = center.distanceSquaredTo(ruin) < center.distanceSquaredTo(ruinTarget);
//            else isClosestToCenter = true;

            if (!hasEnemyPaint && isCloser) {
                ruinTarget = ruin;
                break;
            }
        }
    }

    static boolean isSecondary;
    // TODO: overhaul this
    public static UnitType getTowerType() throws GameActionException {
        if(rc.getNumberTowers() % 3 == 0) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        } else {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
    }

    public static MapLocation getClosestUnpaintedTarget() throws GameActionException {
        if(ruinTarget == null) return null;
        if(!rc.canSenseLocation(ruinTarget)) return ruinTarget;

        if(rc.canCompleteTowerPattern(getTowerType(), ruinTarget)) {
            return ruinTarget;
        }

        UnitType towerType = getTowerType();


        MapInfo[] ruinSurroundings = rc.senseNearbyMapInfos(ruinTarget, 8);
        boolean[][] paintPattern = rc.getTowerPattern(towerType);

        for (MapInfo info : ruinSurroundings) {
            MapLocation loc = info.getMapLocation();

            if (info.hasRuin()) continue; // can't paint ruin

            int x = ruinTarget.x - loc.x + 2;
            int y = ruinTarget.y - loc.y + 2;

            PaintType paint = info.getPaint();
            if (info.getPaint().isEnemy())
                continue; // can't paint enemy paint

            if (paintPattern[x][y] && paint != PaintType.ALLY_SECONDARY) {
                isSecondary = true;
                return loc;
            } else if (!paintPattern[x][y] && paint != PaintType.ALLY_PRIMARY) {
                isSecondary = false;
                return loc;
            }
        }

        return ruinTarget;
    }

    /** Picks a tower to build and builds it */
    public static void paintTowerPattern() throws GameActionException {
        if(ruinTarget == null) return;

        if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinTarget)) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinTarget);
        }

        if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinTarget)) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinTarget);
        }

        if(targetLocation == null) return;
        // apparently you can attack bare ruins for some ruin -- good stuff
        if(rc.canAttack(targetLocation) && !rc.senseMapInfo(targetLocation).hasRuin()) {
            rc.attack(targetLocation, isSecondary);
        }
    }

    /** Paints square below unit */
    public static void paintBelow() throws GameActionException {
        paintSRP(rc.senseMapInfo(rc.getLocation()));
    }

    /** Attacks enemy towers */
    public static void attack() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(UnitType.SOLDIER.actionRadiusSquared, opponentTeam)) {
            // tbh not attacking enemy defense towers just makes us sitting ducks, even if they aren't part of the meta
            if (robot.getType().isTowerType() && rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    /************************************************************************\
    |*                                DEBUG                                 *|
    \************************************************************************/

    /** Prints all debug info */
    public static void debug() {
        if (!in_debug) return;

        indicator += mode + ": Move Target: " + targetLocation + "\n";

        if (mode == Modes.RUSH) {
            for (int i = 0; i < enemyTowerLocations.size; i++) {
                indicator += unpack(enemyTowerLocations.keys.charAt(i)) + ", \n";
            }

            for (int i = 0; i < symmetryLocations.size; i++) {
                indicator += unpack(symmetryLocations.keys.charAt(i)) + ", \n";
            }
        }

        if (mode == Modes.BOOM) {
            if (ruinTarget != null)
                indicator += "ruin_target: " + ruinTarget;
        }

        rc.setIndicatorString(indicator);
    }
}
