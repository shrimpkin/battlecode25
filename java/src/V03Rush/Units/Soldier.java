package V03Rush.Units;

import V03Rush.*;
import V03Rush.Nav.Navigator;
import battlecode.common.*;

public class Soldier extends Unit {
    enum Modes {RUSH, BOOM, SIT, NONE, GET_PAINT, ATTACK};

    static FastIntSet symmetryLocations = new FastIntSet();

    static Modes mode = Modes.NONE;
    static int turnsAlive = 0;
    static MapLocation targetLocation = null;

    public static void run() throws GameActionException {    
        turnsAlive++;
        indicator = "";

        updateMode();
        updateTowerLocations();

        if(mode == Modes.GET_PAINT) {
            targetLocation = closestPaintTower();
            move();
        }

        if(mode == Modes.RUSH) {
            getRushTargetsBySymmetry();
            updateSymmetryTargets();
            markRuins();
            targetLocation = getRushMoveTarget();
            move();
        }  

        if(mode == Modes.ATTACK) {
            markRuins();
            targetLocation = getRushMoveTarget();
            move();
            paintBelow();
        }
        
        if(mode == Modes.BOOM) {
            findValidTowerPosition();
            paintTowerPattern();
            markRuins();
        
            if(ruinTarget != null) {
                targetLocation = new MapLocation(ruinTarget.x - 2 + (nextInt() % 4), ruinTarget.y - 2 + (nextInt() % 4));
            } else {
                targetLocation = null;
            }

            move();
            paintBelow();
        } 
        
        if(mode == Modes.SIT) {
            sit();
        }

        attack();
        debug();
    }  

    /**
     * Changes mode based on criteria I haven't quite figured out yet
     */
    public static void updateMode() throws GameActionException {
        if(rc.getPaint() <= 2) {
            mode = Modes.SIT; 
            return;
        }

        // if(rc.getPaint() <= 50) {
        //     mode = Modes.GET_PAINT;
        //     return;
        // }

        if(rc.getNumberTowers() == 25) {
            mode = Modes.ATTACK;
            return;
        }

        if(rc.getRoundNum() <= rc.getMapHeight() + rc.getMapWidth()) {
            mode = Modes.RUSH;
            return;
        }

        if(rc.getRoundNum() >= rc.getMapHeight() + rc.getMapWidth()) {
            mode = Modes.BOOM;
            return;
        }
    }

    //==================================================================\\ 
    //                           Sit                                     \\

    public static void sit() throws GameActionException {
        MapInfo info = rc.senseMapInfo(rc.getLocation());

        if(info.getPaint() == PaintType.ALLY_PRIMARY 
            || info.getPaint() == PaintType.ALLY_SECONDARY) return;

        
        if(rc.canAttack(rc.getLocation())) {
            rc.attack(rc.getLocation());
        }
    }

    //==================================================================\\ 
    //                           Rush                                    \\

    static boolean addedPaintTowerSymmetryLocations = false;
    static boolean addedMoneyTowerSymmetryLocations = false;
    /**
     * Uses map symmetry and our tower positions to generate possible locations for enemy towers
     */
    public static void getRushTargetsBySymmetry() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robots) {
            boolean isPaintTower = isPaintTower(robot.getType());
            boolean isMoneyTower = isMoneyTower(robot.getType());
            
            if(!(isPaintTower || isMoneyTower)) return;
            if(isPaintTower && addedPaintTowerSymmetryLocations) continue;
            if(isMoneyTower && addedMoneyTowerSymmetryLocations) continue;

            int x = robot.getLocation().x;
            int y = robot.getLocation().y;

            MapLocation vert = new MapLocation(mapWidth - x - 1,y);
            MapLocation hort = new MapLocation(x, mapHeight - y - 1);
            MapLocation mirr = new MapLocation(mapWidth - x - 1, mapHeight - y - 1);

            symmetryLocations.add(pack(vert));
            symmetryLocations.add(pack(hort));
            symmetryLocations.add(pack(mirr));

            if(isPaintTower) addedPaintTowerSymmetryLocations = true;
            if(isMoneyTower) addedMoneyTowerSymmetryLocations = true;
        }        
    }

    /**
     * Updates targets in symmetryLocations by removing them if we know they aren't towers
     */
    public static void updateSymmetryTargets() throws GameActionException {
        for (int i = 0; i < symmetryLocations.size; i++) {
            MapLocation tower = unpack(symmetryLocations.keys.charAt(i));
            
            if(!rc.canSenseLocation(tower)) continue;
            RobotInfo info = rc.senseRobotAtLocation(tower);

            //there is no unit or the unit is not a paint or money tower
            //hence the tower is not there and we should remove 
            if(info == null || !(isPaintTower(info.getType()) || isMoneyTower(info.getType()))) {
                symmetryLocations.remove(pack(tower));
            }
        }
    }

    /**
     * @returns If possible a tower that have seen, otherwise the nearest possible location for a tower
     */
    public static MapLocation getRushMoveTarget() throws GameActionException {
        int minDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        if(enemyTowerLocations.size > 0) {
            return unpack(enemyTowerLocations.keys.charAt(0));
        }

        for (int i = 0; i < symmetryLocations.size; i++) {
            MapLocation tower = unpack(symmetryLocations.keys.charAt(i));
            int distanceToTower = tower.distanceSquaredTo(rc.getLocation());

            if(distanceToTower < minDistance) {
                minDistance = distanceToTower;
                bestLocation = tower;
            }
        }

        return bestLocation;
    }

    /**
     * Paints one tile of every adjacent ruin
     */
    public static void markRuins() throws GameActionException {
        MapLocation locationToMark = null;
        for(int i = 0; i < unusedRuinLocations.size; i++) {
            MapLocation ruinLocation = unpack(unusedRuinLocations.keys.charAt(i));

            if(!rc.canSenseLocation(ruinLocation)) continue;

            MapInfo[] squaresToMark = rc.senseNearbyMapInfos(ruinLocation, 8);
            for(MapInfo info : squaresToMark) {
                PaintType paint = info.getPaint();
                if(paint.isAlly()) return;

                if(paint.equals(PaintType.EMPTY) && rc.canAttack(info.getMapLocation())) {
                    locationToMark = info.getMapLocation();
                }
            }
        }

        if(locationToMark != null) {
            rc.attack(locationToMark);
        }
    }

    /**
     * Moves to target location, if no target wanders
     */
    public static void move() throws GameActionException {
        if(targetLocation != null) {
            Navigator.moveTo(targetLocation, true);
        } else {
            wander(true);
        }
    }

     //==================================================================\\ 
    //                           Boom                                     \\

    static MapLocation ruinTarget;
    static MapLocation eastOfRuinTarget;
    static MapLocation westOfRuinTarget;
    static MapLocation northOfRuinTarget;
    /**
     * Will attempt to build clock tower on nearby empty ruins
     */
    public static void findValidTowerPosition() throws GameActionException {
        ruinTarget = null;
        MapLocation[] ruin_locations = rc.senseNearbyRuins(-1);

        for(MapLocation ruin : ruin_locations) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);
            
            if(robot != null) continue;

            MapInfo[] ruin_suroundings = rc.senseNearbyMapInfos(ruin, 8);
            boolean has_enemy_paint = false;
            //Checks for nearby enemy paint that would prevent building a clock tower
            for(MapInfo loc : ruin_suroundings) {
                PaintType paint = loc.getPaint();
                if(paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) {
                    has_enemy_paint = true;
                }
            }

            if(!has_enemy_paint) ruinTarget = ruin;
            break;
        }
    }
    
    /**
     * Helper method for figuring out which type of tower to build
     */
    public static UnitType decideTowerType() throws GameActionException {
        if(ruinTarget == null) return null;

        eastOfRuinTarget = ruinTarget.add(Direction.EAST);
        westOfRuinTarget = ruinTarget.add(Direction.WEST);
        northOfRuinTarget = ruinTarget.add(Direction.NORTH);

        if(!rc.canSenseLocation(northOfRuinTarget) 
            || !rc.canSenseLocation(eastOfRuinTarget)
            || !rc.canSenseLocation(westOfRuinTarget)) return null;

        if(rc.senseMapInfo(northOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY)) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        }

        if(rc.senseMapInfo(eastOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY)) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }

        if(rc.senseMapInfo(westOfRuinTarget).getMark().equals(PaintType.ALLY_PRIMARY)) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }

        return null;
    }

    /**
     * Picks a tower to build and builds it
     */
    public static void paintTowerPattern() throws GameActionException {
        if(ruinTarget == null) return;
        if(!rc.canSenseLocation(ruinTarget)) return;
        
        UnitType towerType = decideTowerType();

        northOfRuinTarget = ruinTarget.add(Direction.NORTH);
        eastOfRuinTarget = ruinTarget.add(Direction.EAST);
        westOfRuinTarget = ruinTarget.add(Direction.WEST);

        if(towerType == null) {
            int key = rc.getNumberTowers() % 10;
            if(key <= 5) {
                if(rc.canMark(eastOfRuinTarget)) {
                    rc.mark(eastOfRuinTarget, false);   
                }
            } else if(key <= 10) {
                if(rc.canMark(westOfRuinTarget)) {
                    rc.mark(westOfRuinTarget, false);   
                }
            } else {
                if(rc.canMark(northOfRuinTarget)) {
                    rc.mark(northOfRuinTarget, false);   
                }
            }
            return;
        }

        MapInfo[] ruin_suroundings = rc.senseNearbyMapInfos(ruinTarget, 8);
        boolean[][] paintPattern = rc.getTowerPattern(towerType);

        if(rc.canCompleteTowerPattern(towerType, ruinTarget)) {
            rc.completeTowerPattern(towerType, ruinTarget);
        }

        for(MapInfo info : ruin_suroundings) {
            MapLocation loc = info.getMapLocation();
            if(!rc.canAttack(loc)) continue;
            if(info.hasRuin()) continue;
            

            int x = ruinTarget.x - loc.x + 2;
            int y = ruinTarget.y - loc.y + 2;

            PaintType paint = info.getPaint();
            if((paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY)) continue;

            if(paintPattern[x][y] && paint != PaintType.ALLY_SECONDARY) {
                rc.attack(loc, true);
                indicator += loc.toString();
            } else if(!paintPattern[x][y] && paint != PaintType.ALLY_PRIMARY) {
                rc.attack(loc, false);
                indicator += loc.toString();
            }
        }
    }

    public static void paintBelow() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if(!rc.senseMapInfo(myLocation).getPaint().equals(PaintType.EMPTY)) return;

        if(rc.canAttack(myLocation)) {
            rc.attack(myLocation);
        }
    }

    public static void attack() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);

        for(RobotInfo robot : robots) {
            if( (isPaintTower(robot.getType())
                || isMoneyTower(robot.getType()))
                && rc.canAttack(robot.getLocation())) {
                rc.attack(robot.getLocation());
            }
        }
    }

    //==========================================================================\\

    /**
     * Prints all debug info
     */
    public static void debug() {
        if(!in_debug) return;

        switch(mode) {
            case BOOM: indicator += "Boom: "; break; 
            case NONE: indicator += "None: "; break;
            case RUSH: indicator += "Rush: "; break;
            case SIT:  indicator +=  "Sit: "; break;
            case GET_PAINT: indicator += "Getting Paint: "; break;
            default: break;
        }

        indicator += "Move Target: ";
        if(targetLocation != null) {
            indicator += targetLocation.toString() + "\n";
        } else {
            indicator += "null target\n";
        }

        if(mode == Modes.RUSH) {
            for(int i = 0; i < enemyTowerLocations.size; i++) {
                indicator += unpack(enemyTowerLocations.keys.charAt(i)).toString() + ", \n";
            }

            for(int i = 0; i < symmetryLocations.size; i++) {
                indicator += unpack(symmetryLocations.keys.charAt(i)).toString() + ", \n";
            }
        }

        if(mode == Modes.BOOM) {
            if(ruinTarget != null) indicator += ruinTarget.toString();
        }
        
        rc.setIndicatorString(indicator);
    }

    
}
