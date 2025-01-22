package V08.Units;

import V08.Comms;
import V08.Unit;
import V08.Nav.Navigator;
import V08.Tools.FastIntSet;
import battlecode.common.*;

public class Soldier extends Unit {
    static MapLocation moveTarget = null;
    static MapLocation commTarget = null;
    static int lastRefillEnd;

    public enum BuildMode {BUILD_SRP, BUILD_TOWER, NONE}
    static BuildMode bMode = BuildMode.NONE;
    static MapLocation buildTarget = null;
    static UnitType buildType = null;

    // BOOM
    static MapLocation ruinTarget;
    static MapLocation SRPTarget;
    static MapLocation lastRuinTarget;
    static MapLocation attackTarget;
    static int roundNum;
    static int DEBUG = 1;

    static FastIntSet possibleSRPLocations = new FastIntSet();
    static boolean hasGeneratedSRPLocations = false;

    public static void run() throws GameActionException {

        indicator = "";
        Modes prev = mode;
        roundNum = rc.getRoundNum();

        // if(!hasGeneratedSRPLocations) {
        //     generateSRPLocations();
        //     hasGeneratedSRPLocations = true;
        // }
        // updateSRPLocations();

        //computations ahead of choosing mode
        updateTowerLocations();
        setBuildTarget();
        attackTarget = getAttackMove();

        read();
        updateMode();
        
        if (prev == Modes.REFILL && mode != Modes.REFILL) lastRefillEnd = roundNum;

        if (mode == Modes.REFILL) {
            moveTarget = getClosestLocation(paintTowerLocations);
            requestPaint(moveTarget, 200 - rc.getPaint());
        }

        if (mode == Modes.BOOM) {
            moveTarget = rotateAroundBuiltTarget();
            paintBuildTarget(); 
            completeBuiltTarget();
        }

        if (mode == Modes.ATTACK) {
            moveTarget = attackTarget;
        }

        for(RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
            if(robot.getType().isTowerType()) requestPaint(robot.getLocation(), 200-rc.getPaint());
        }

        attack();
        move();
        attack();
        
        tessellate(); 
        updateSeen();
        debug();
    }


    //generates all the squares we might want to place at SRP on
    //and holds them in possibleSRPLocations
    public static void generateSRPLocations() throws GameActionException {
        for(int i = 2; i < mapWidth; i += 4) {
            for(int j = 2; j < mapWidth; j += 4) {
                int x = pack(new MapLocation(i, j));
                possibleSRPLocations.add(x);
            }  
        }
    }

    //removes SRP locations from possibleSRPLocations if we can sense them
    public static void updateSRPLocations() throws GameActionException {
        for(MapInfo info : rc.senseNearbyMapInfos()) {
            MapLocation loc = info.getMapLocation();
            if(loc.x % 4 != 2) continue;
            if(loc.y % 4 != 2) continue;

            if(rc.canSenseLocation(loc)) {
                possibleSRPLocations.remove(pack(loc));
            }
        }
    }

    public static void read() throws GameActionException {
        Message[] msgs = rc.readMessages(-1);
        if (msgs.length > 0) {
            if (commTarget == null) {
                commTarget = Comms.getLocation(msgs[0].getBytes());
            }
        }
    }

    /** Changes mode based on criteria I haven't quite figured out yet @aidan */
    public static void updateMode() throws GameActionException {
        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
            mode = Modes.ATTACK;
            return;
        }

        if(bMode == BuildMode.BUILD_TOWER && buildTarget != null) {
            mode = Modes.BOOM;
            return;
        }

        MapLocation nearTower = getClosestEnemyTowerLocation();
        if(nearTower != null && attackTarget.distanceSquaredTo(nearTower) <= 25) {
            mode = Modes.ATTACK;
            return;
        }

        if(rc.getPaint() <= 40 && roundNum - lastRefillEnd > 10 && paintTowerLocations.size != 0) {
            mode = Modes.REFILL;
            if (ruinTarget != null) {
                lastRuinTarget = ruinTarget;
                indicator += "last ruin target: " + ruinTarget;
            }
            return;
        }

        mode = Modes.BOOM;
    }

    /************************************************************************\
    |*                                 Attack                               *|
    \************************************************************************/

    /** Returns location of a tower that has been seen otherwise null*/
    public static MapLocation getClosestEnemyTowerLocation() throws GameActionException {
        MapLocation bestLocation = null;
        int bestDistance = Integer.MAX_VALUE;

        if (enemyTowerLocations.size > 0) {
            MapLocation tower =  unpack(enemyTowerLocations.keys.charAt(0));
            //System.out.println("Tower at: " + tower.toString() + " robot at: " + rc.getLocation().toString());
            int distance = rc.getLocation().distanceSquaredTo(tower);
            if(distance < bestDistance) {
                bestDistance = distance;
                bestLocation = tower;
            }
        }

        return bestLocation;
    }


    public static MapLocation getAttackMove() throws GameActionException {
        if(getClosestEnemyTowerLocation() == null) return null;

        if(getClosestEnemyTowerLocation().distanceSquaredTo(rc.getLocation()) >= 25) {
            return getClosestEnemyTowerLocation();
        }

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(Direction.values()[i]);
            microInfo[i].updateEnemiesTargeting();
        }

        MicroInfo best = microInfo[8];
        for (int i = 0; i < 8; i++) {
            if (rc.getRoundNum() % 2 == 0 && microInfo[i].isBetterAttack(best)) {
                best = microInfo[i];
            }
            
            if (rc.getRoundNum() % 2 == 1 && microInfo[i].isBetterRetreat(best)) {
                best = microInfo[i];
            }
        }

        return best.location;
    }

    /** Moves to target location, if no target wanders */
    private static boolean wasWandering = false;
    public static void move() throws GameActionException {
        if (moveTarget != null) {
            Navigator.moveTo(moveTarget);
            wasWandering = false;

        } else if (commTarget != null) {
            Navigator.moveTo(commTarget);
            if (rc.canSenseLocation(commTarget)) {
                commTarget = null;
            }
            wasWandering = false;

        } else if(possibleSRPLocations.size != 0) {
            System.out.println("Using SRP locations.");
            MapLocation loc = unpack(possibleSRPLocations.keys.charAt(0));
            rc.setIndicatorDot(loc, 50, mapWidth, mapHeight);

            Navigator.moveTo(loc);
            wasWandering = false;
        } else {
            wander(wasWandering);
            wasWandering = true;
        }
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
    |*                                 Build                                *|
    \************************************************************************/

    /** Attempts to build a tower on nearby empty ruins */
    public static void findValidTowerPosition() throws GameActionException {
        ruinTarget = null;
        MapLocation[] ruinLocations = rc.senseNearbyRuins(-1);

        MapLocation bestLocation = null;
        int distance = Integer.MAX_VALUE;
        for (MapLocation ruin : ruinLocations) {
            RobotInfo ruinRobot = rc.senseRobotAtLocation(ruin);    
            if (ruinRobot != null) continue;

            int numNearbySoliders = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(ruin, 8, myTeam)) {
                if(robot.getType().equals(UnitType.SOLDIER)) numNearbySoliders++;
            }
            if(numNearbySoliders > 2) continue;

            if(!canStillComplete(ruin)) continue;

            if(ruin != null) {
                if(bestLocation == null) {
                    bestLocation = ruin;
                    distance = rc.getLocation().distanceSquaredTo(ruin);
                } else if(rc.getLocation().distanceSquaredTo(ruin) < distance){
                    bestLocation = ruin;
                    distance = rc.getLocation().distanceSquaredTo(ruin);
                }
            }
        }

        ruinTarget = bestLocation;
    }

    /** Checks if the enemey has painted in our build target */
    public static boolean canStillComplete(MapLocation loc) throws GameActionException {
        for(MapInfo info : rc.senseNearbyMapInfos(loc, 8)) {
            if(info.getPaint().isEnemy()) return false;
        }
        
        return true;
    }

    /** Updates SRPTarget field. Sets it to be the nearest valid location for building an SRP.
     *  If it doesn't have any such valid locations then it will choose the closest unknown SRP location.
     */
    public static void updateSRPTarget() throws GameActionException {
        //we have a valid SRP 
        if(SRPTarget != null 
            && rc.canSenseLocation(SRPTarget)
            && rc.senseMapInfo(SRPTarget).getMark() == PaintType.ALLY_PRIMARY
            && !rc.senseMapInfo(SRPTarget).isResourcePatternCenter()
            && isValidSRPPosition(SRPTarget)) {
                return;
        }

        SRPTarget = null;

        for(MapInfo info : rc.senseNearbyMapInfos()) {

            MapLocation loc = info.getMapLocation();
            if (loc.x % 4 != 2 || loc.y % 4 != 2) continue; // not a center location
            if(info.isResourcePatternCenter()) {
                continue; //already a SRP center
            }

            //building to close to an unbuilt ruin
            MapLocation[] ruinLocation = rc.senseNearbyRuins(-1);
            boolean toClose = false;
            for(MapLocation ruin : ruinLocation) {
                RobotInfo robotInfo = rc.senseRobotAtLocation(ruin);
                if(robotInfo == null) toClose = true;
            }
            if(toClose) continue;

            int numNearbySoliders = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(loc, 8, myTeam)) {
                if(robot.getType().equals(UnitType.SOLDIER)) numNearbySoliders++;
            }
            if(numNearbySoliders > 1) continue;

            boolean isValid = isValidSRPPosition(loc);
            if(info.getMark().equals(PaintType.ALLY_PRIMARY) && isValid) {
                SRPTarget = loc;
            } else if(info.getMark().equals(PaintType.ALLY_SECONDARY) || !info.isPassable()) {
                continue;
            } else {
                //if this is a new SRP location that is valid to complete we set it as target
                //or if we have no other valid targets we will also set it
                if(isValid) {
                    SRPTarget = loc;
                }
            }
        }
    }

    /** @return true if we can complete a SRP pattern at this location */
    public static boolean isValidSRPPosition(MapLocation loc) throws GameActionException {
        MapInfo[] infos = rc.senseNearbyMapInfos(loc, 8);
        if(infos.length != 25) return false; //can't sense all tiles around the SRP
        
        //location is to close to the edge
        if(loc.x <= 1 || loc.y <= 1
            || loc.x >= mapWidth - 1 || loc.y >= mapHeight - 1) {
                if(rc.canMark(loc)) rc.mark(loc, true);
                return false;    
        }

        for(MapInfo info : infos) {
            //isPassable determines if there is a wall or ruin on this square
            //if there is a wall or ruin we can't paint it, hence don't paint there
            if(!info.isPassable()) {
                if(rc.canMark(loc)) rc.mark(loc, true);
                return false;
            }

            if(info.getPaint().isEnemy()) {
                return false;
            }
        }

        if(rc.canMark(loc)) rc.mark(loc, false);
        return true;
    }

    //east corresponds to paint tower
    //west corresponds to money tower
    //north corresponds to a clock tower
    public static UnitType getTowerMark() throws GameActionException {
        if(ruinTarget == null) return null;
        
        MapLocation east = ruinTarget.add(Direction.EAST);
        MapLocation west = ruinTarget.add(Direction.WEST);
        MapLocation north = ruinTarget.add(Direction.NORTH);

        if(!rc.canSenseLocation(west)) return null;
        if(!rc.canSenseLocation(east)) return null;
        if(!rc.canSenseLocation(north)) return null;

        MapInfo eastInfo = rc.senseMapInfo(east);
        MapInfo westInfo = rc.senseMapInfo(west);
        MapInfo northInfo = rc.senseMapInfo(north);

        if(eastInfo.getMark().isAlly()) return UnitType.LEVEL_ONE_PAINT_TOWER;
        if(westInfo.getMark().isAlly()) return UnitType.LEVEL_ONE_MONEY_TOWER;
        if(northInfo.getMark().isAlly()) return UnitType.LEVEL_ONE_DEFENSE_TOWER;

        UnitType towerType;

        boolean shouldDefend = false;
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, opponentTeam);
        
        if(enemies.length > 5) shouldDefend = true;
        for(RobotInfo enemy : enemies) {
            if(isPaintTower(enemy.getType()) || isMoneyTower(enemy.getType())) {
                shouldDefend = true;
            }
        }

        if(shouldDefend) {
            towerType = UnitType.LEVEL_ONE_DEFENSE_TOWER;
        } else if((rc.getNumberTowers() == 2 || nextDouble() < .66) && rc.getNumberTowers() < 12) {
            towerType = UnitType.LEVEL_ONE_MONEY_TOWER;
        } else {
            towerType = UnitType.LEVEL_ONE_PAINT_TOWER;
        }
        

        if(isPaintTower(towerType)) {
            if(rc.canMark(east)) {
                rc.mark(east, false);
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            }
        } else if(isMoneyTower(towerType)) {
            if(rc.canMark(west)) {
                rc.mark(west, false);
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            }
        } else {
            if(rc.canMark(north)) {
                rc.mark(north, false);
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        }

        return null;
    }

    /** @return MapLocations that rotate around the build target */
    public static MapLocation rotateAroundBuiltTarget() throws GameActionException {
        if(buildTarget == null) return null;

        if(rc.getRoundNum() % 4 == 0) {
            return buildTarget.add(Direction.NORTH);
        } else if(rc.getRoundNum() % 4 == 1) {
            return buildTarget.add(Direction.EAST);
        } else if(rc.getRoundNum() % 4 == 2) {
            return buildTarget.add(Direction.SOUTH);
        } else {
            return buildTarget.add(Direction.WEST);
        }
    }

    /** Paints incorrectly painted tiles around our build target */
    public static void paintBuildTarget() throws GameActionException {
        //We have not decided on a build target so we can't paint it
        if(buildTarget == null || bMode == BuildMode.NONE) return;

        //Determines what paint pattern we need to paint based on bMode and tower type
        boolean[][] paintPattern;
        if(bMode == BuildMode.BUILD_SRP) {
            paintPattern = rc.getResourcePattern();
        } else {
            //needs to choose what tower it wants to paint
            buildType = getTowerMark();
            if(buildType == null) return;
            paintPattern = rc.getTowerPattern(buildType);
        } 

        //Iterates through locations, paints it if it had the incorrect paint
        for(MapInfo info : rc.senseNearbyMapInfos(buildTarget, 8)) {
            MapLocation loc = info.getMapLocation();
            if(!rc.canAttack(loc)) continue;
            if(!info.isPassable()) continue;

            //converting the location in the offsets we need use paint pattern
            int x = buildTarget.x - loc.x + 2;
            int y = buildTarget.y - loc.y + 2;

            if(info.getPaint().isEnemy()) {
                //can't attack enemy paint with soldiers
                continue;
            } else if(info.getPaint().equals(PaintType.EMPTY) 
                        || info.getPaint().isSecondary() != paintPattern[x][y]) {
                //correcting paint, returning because we can only attack once a turn
                rc.attack(loc, paintPattern[x][y]);
                return;
            }
        }
    }

    /** Updates the buildTarget and bMode fields */
    public static void setBuildTarget() throws GameActionException {
        //first looks for nearby ruins to build towers on
        findValidTowerPosition();
        if(ruinTarget != null) {
            buildTarget = ruinTarget;
            bMode = BuildMode.BUILD_TOWER;
            return;
        } else if(ruinTarget == null && bMode == BuildMode.BUILD_TOWER) {
            //we have the lost the target to build on so we reset:
            buildTarget = ruinTarget;
            bMode = BuildMode.NONE;
        }

        updateSRPTarget();
        if(SRPTarget != null) {
            buildTarget = SRPTarget;
            bMode = BuildMode.BUILD_SRP;
            rc.setIndicatorDot(buildTarget, 0, mapWidth, mapHeight);            
            return;
        } else if(SRPTarget == null) {
            //we no longer have a valid SRP
            buildTarget = null;
            bMode = BuildMode.NONE;
        }
    }

    /** Will complete the SRP and tower pattern at build target if possible */
    public static void completeBuiltTarget() throws GameActionException {
        //no build target to complete
        if(buildTarget == null || bMode == BuildMode.NONE) return;
        //haven't decided on tower type yet
        if(bMode == BuildMode.BUILD_TOWER && buildType == null) return;
        if (!rc.canSenseLocation(buildTarget)) return;
        
        if(bMode == BuildMode.BUILD_TOWER) {
            //a tower has been built or we can complete the tower
            boolean robotIsNull = rc.senseRobotAtLocation(buildTarget) != null;
            if(robotIsNull || rc.canCompleteTowerPattern(buildType, buildTarget)) {
                if(!robotIsNull) {
                    rc.completeTowerPattern(buildType, buildTarget);
                }
                
                buildTarget = null;
                buildType = null;
                bMode = BuildMode.NONE;
            }
        } else if(bMode == BuildMode.BUILD_SRP) {
            MapInfo buildInfo = rc.senseMapInfo(buildTarget);
            //a resource pattern has been built or we can complete it
            boolean isCenter = buildInfo.isResourcePatternCenter();
            if(isCenter ||  (rc.canCompleteResourcePattern(buildTarget) && rc.getMoney() >= 200)) {
                if(!isCenter) rc.completeResourcePattern(buildTarget);

                buildTarget = null;
                bMode = BuildMode.NONE;
            }
        }
    }

    /************************************************************************\
    |*                                SRP                                   *|
    \************************************************************************/

    /** Determines if tile should be secondary to complete SRP pattern */
    public static boolean shouldBeSecondary(MapLocation loc) {
        return pattern[loc.x % 4][loc.y % 4] == 1;
    }

    /** Will paint the correct SRP paint type on the given tile */
    public static boolean paintSRP(MapInfo tile) throws GameActionException {
        MapLocation loc = tile.getMapLocation();
        boolean isSecondary = shouldBeSecondary(loc);
        PaintType idealPaint = isSecondary ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
        PaintType paintType = tile.getPaint();

        if (!rc.canAttack(loc)) return false;
        if (paintType.isEnemy()) return false;
        if (tile.hasRuin()) return false;
        if (roundNum < 50 && paintType.isAlly()) return false;
        if (paintType.equals(idealPaint)) return false;

        for (MapLocation ruin : rc.senseNearbyRuins(-1)) {
            if (rc.canSenseRobotAtLocation(ruin)) continue; // only consider unbuilt ruins
            if (ruin.isWithinDistanceSquared(loc, 8)) {
                return false; // inside the 5x5 area of an unbuilt ruin -- don't tessellate here
            }
        }

        rc.setIndicatorDot(loc, 40, 40, 128);
        rc.attack(loc, isSecondary);
        return true;        
    }

    /** Paints square below unit as SRP pattern*/
    public static void paintSRPBelow() throws GameActionException {
        if(rc.getPaint() < 15) return; //conserve paint
        if(moveTarget != null && rc.getLocation().distanceSquaredTo(moveTarget) >= rc.getPaint() + 10) return; //conserve paint

        paintSRP(rc.senseMapInfo(rc.getLocation()));
    }

    /** Paints one tile of every adjacent ruin */
    public static void markOneRuinTile() throws GameActionException {
        MapLocation locationToMark = null;
        MapLocation[] ruins = rc.senseNearbyRuins(-1);

        for (MapLocation ruinLocation : ruins) {
            RobotInfo robot = rc.senseRobotAtLocation(ruinLocation);
            if(robot != null) continue; 

            MapInfo[] squaresToMark = rc.senseNearbyMapInfos(ruinLocation, 8);
            for (MapInfo info : squaresToMark) {
                PaintType paint = info.getPaint();
                if (paint.isAlly()) {
                    locationToMark = null;
                    break;
                }

                if (paint.equals(PaintType.EMPTY) && rc.canAttack(info.getMapLocation())) {
                    locationToMark = info.getMapLocation();
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
   
    /**
     * Handles all the ways we paint SRP patterns. In order of priority:
     *      1. Marks one ruin tile with an SRP pattern
     *      2. Paints the SRP pattern below the robot
     */
    public static void tessellate() throws GameActionException {
        markOneRuinTile();
        paintSRPBelow();
    }

    /************************************************************************\
    |*                                DEBUG                                 *|
    \************************************************************************/

    /** Prints all debug info */
    public static void debug() {
        if (DEBUG == 0) return;

        if(buildTarget != null) {
            indicator += bMode + " at: " + buildTarget.toString() +  " MT: " + moveTarget + "\n";
        } else if(attackTarget != null) {
            indicator += "Attack at: " + attackTarget.toString() +  " MT: " + moveTarget + "\n";
        } else {
            indicator += "No build target: " + bMode + " MT: " + moveTarget + "\n";
        }

        rc.setIndicatorString(indicator);
    }

    public static class MicroInfo {
        Direction direction;
        MapLocation location;
        int minDistanceToEnemy = Integer.MAX_VALUE;
        double towersTargeting = 0;
        double moppersTargeting = 0;
        double towersOneMoveAway = 0;
        boolean canMove = true;
        int minHealth = Integer.MAX_VALUE;
        boolean actionReady;
        PaintType paint; 
        int towerHealth = Integer.MAX_VALUE;

        public MicroInfo(Direction dir) throws GameActionException {
            direction = dir;
            location = rc.getLocation().add(dir);

            if (!dir.equals(Direction.CENTER) && !rc.canMove(dir)) canMove = false;
            if(rc.canSenseLocation(location)) paint = rc.senseMapInfo(location).getPaint();

            actionReady = rc.isActionReady();
        }

        public void updateEnemiesTargeting() throws GameActionException {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);
            for(RobotInfo robot : robots) {
                if(robot.getType().isTowerType() && location.isWithinDistanceSquared(robot.getLocation(), 9)) {
                    towersTargeting++;
                    towerHealth = towerHealth < robot.getHealth() ? towerHealth : robot.getHealth();
                }

                if(robot.getType().isTowerType() && location.isWithinDistanceSquared(robot.getLocation(), 16)) {
                    towersOneMoveAway++;
                }

                if(robot.getType().equals(UnitType.MOPPER) && location.isWithinDistanceSquared(robot.getLocation(), 10)) {
                    moppersTargeting++;
                }
            }
        }

        public boolean isBetterAttack(MicroInfo m) {
            //if we can't move there ain't no point in the rest
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            //both squares are useless to us
            if(!canMove && !m.canMove) return true;

            //wants to be within range of exactly one tower
            if(towersTargeting == 1 && m.towersTargeting != 1) return true;
            if(towersTargeting != 1 && m.towersTargeting == 1) return false;

            //avoids walking into two towers at the same time
            if(towersTargeting < m.towersTargeting) return true;
            if(towersTargeting > m.towersTargeting) return false;

            //go for lower health towers, prevents targetting switching
            if(towerHealth < m.towerHealth) return true;
            if(towerHealth > m.towerHealth) return false;

            //avoids moppers 
            if(moppersTargeting < m.moppersTargeting) return true;
            if(moppersTargeting > m.moppersTargeting) return false;

            //steps into ally paint if possible
            if(paint.isAlly() && !m.paint.isAlly()) return true;
            if(!paint.isAlly() && m.paint.isAlly()) return false;

            //now tries to step into empty paint
            if(paint.equals(PaintType.EMPTY) && !m.paint.equals(PaintType.EMPTY)) return true;
            if(!paint.equals(PaintType.EMPTY) && m.paint.equals(PaintType.EMPTY)) return true;

            return true;
        }

        public boolean isBetterRetreat(MicroInfo m) {
            //if we can't move there ain't no point in the rest
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            //both squares are useless to us
            if(!canMove && !m.canMove) return true;

            //wants to get out of range of any towers
            if(towersTargeting < m.towersTargeting) return true;
            if(towersTargeting > m.towersTargeting) return false;

            //tries to maintain the ability to attack towers soon
            if(towersOneMoveAway > m.towersOneMoveAway) return true;
            if(towersOneMoveAway < m.towersOneMoveAway) return true;

            //avoids moppers 
            if(moppersTargeting < m.moppersTargeting) return true;
            if(moppersTargeting > m.moppersTargeting) return false;
            
            //steps into ally paint if possible
            if(paint.isAlly() && !m.paint.isAlly()) return true;
            if(!paint.isAlly() && m.paint.isAlly()) return false;

            //now tries to step into empty paint
            if(paint.equals(PaintType.EMPTY) && !m.paint.equals(PaintType.EMPTY)) return true;
            if(!paint.equals(PaintType.EMPTY) && m.paint.equals(PaintType.EMPTY)) return true;

            return true;
        }
    }

}
