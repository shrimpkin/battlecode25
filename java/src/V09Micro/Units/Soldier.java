package V09Micro.Units;

import V09Micro.Comms;
import V09Micro.Unit;
import V09Micro.Micro.SoldierMicro;
import V09Micro.Nav.Navigator;
import V09Micro.Tools.*;
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
    static MapLocation refillTarget;
    static MapLocation enemyTower;
    static int roundNum;
    static int DEBUG = 1;

    static FastIntSet possibleSRPLocations = new FastIntSet();
    static boolean hasGeneratedSRPLocations = false;
    // to avoid ping ponging between towers once enemy paint goes out of range -- ignore for ROUND_COOLDOWN rounds
    private static final char ROUND_COOLDOWN = 10;
    private static char[] ignore = "\0".repeat(4096).toCharArray();
    SoldierMicro micro = new SoldierMicro();

    public static void run() throws GameActionException {
        indicator = "";
        Modes prev = mode;
        roundNum = rc.getRoundNum();

        //doing all the updates
        updateTowerLocations();
        updateBuildTarget();
        enemyTower = getClosestEnemyTowerLocation();
        updateAttackTarget();
        updateCommTarget();
        updateRefillTarget();
        updateMode();
        updateMoveTarget();

        // throwing
        refill();
        attack();
        move();
        attack();

        // marking
        markOneRuinTile();

        // keeping track
        if (prev == Modes.REFILL && mode != Modes.REFILL) lastRefillEnd = roundNum;

        if (mode == Modes.BOOM) {
            paintBuildTarget(); 
            completeBuiltTarget();
        }
        
        // painting
        paintSRPBelow();
        updateSeen();
        roundendComms();
        debug();
    }

     /************************************************************************\
    |*                                 Build                                *|
    \************************************************************************/

    /** Updates the buildTarget and bMode fields */
    public static void updateBuildTarget() throws GameActionException {
        // see if we were refilling and need to go back
        if (lastRuinTarget != null) {
            buildTarget = lastRuinTarget;
            if (rc.canSenseLocation(lastRuinTarget)) {
                lastRuinTarget = null;
            }
            return;
        }

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

    /** Attempts to build a tower on nearby empty ruins */
    public static void findValidTowerPosition() throws GameActionException {
        ruinTarget = null;
        MapLocation[] ruinLocations = rc.senseNearbyRuins(-1);

        MapLocation bestLocation = null;
        int distance = Integer.MAX_VALUE;
        for (MapLocation ruin : ruinLocations) {
            if ((rc.getRoundNum()+ROUND_COOLDOWN) - ignore[pack(ruin)] < 10) continue;
            RobotInfo ruinRobot = rc.senseRobotAtLocation(ruin);    
            if (ruinRobot != null) continue;

            int numNearbySoliders = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(ruin, 8, myTeam)) {
                if(robot.getType().equals(UnitType.SOLDIER)) numNearbySoliders++;
            }
            if(numNearbySoliders >= 2) continue;

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
            if(info.getPaint().isEnemy()) {
                ignore[pack(loc)] = (char)(rc.getRoundNum() + ROUND_COOLDOWN);
                return false;
            }
        }
        return true;
    }

    /** Updates SRPTarget field. Sets it to be the nearest valid location for building an SRP */
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

        // if (rc.getNumberTowers() < 5 && rc.getRoundNum() <= 150) return;

        for(MapInfo info : rc.senseNearbyMapInfos()) {
            MapLocation loc = info.getMapLocation();
            if (loc.x % 4 != 2 || loc.y % 4 != 2) continue; // not a center location
            if(info.isResourcePatternCenter()) continue; //already an SRP center

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
        
        // location is to close to the edge
        if(loc.x <= 1 || loc.y <= 1 || loc.x >= mapWidth - 2 || loc.y >= mapHeight - 2) {
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

        if(shouldDefend && rc.getChips() >= 3500) {
            towerType = UnitType.LEVEL_ONE_DEFENSE_TOWER;
        } else if (rc.getNumberTowers() <= 3) {
            towerType = UnitType.LEVEL_ONE_MONEY_TOWER;
        } else if (rc.getNumberTowers() == 4) {
            towerType = UnitType.LEVEL_ONE_PAINT_TOWER;
        } else if (nextDouble() <= 0.8 && rc.getNumberTowers() <= 20) {
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
        // rotate if adjacent to the build target
        if (rc.getLocation().isWithinDistanceSquared(buildTarget, 1)){
            var robots = rc.senseNearbyRobots(buildTarget, 1, myTeam);
            if (robots.length > 0 && robots[0].getType() == UnitType.SOLDIER) {
                var robot = robots[0];
                var tdir = buildTarget.directionTo(robot.getLocation()).opposite();
                return buildTarget.add(tdir);
            } else {
                var pdir = buildTarget.directionTo(rc.getLocation());
                return buildTarget.add(pdir.rotateRight().rotateRight());
            }
        } else if (rc.getLocation().isAdjacentTo(buildTarget)) {
            return buildTarget.add(buildTarget.directionTo(rc.getLocation()).rotateRight());
        } else{
            // get roughly the closest slot to the robot
            return buildTarget.add(buildTarget.directionTo(rc.getLocation()));
        }
    }


    static int []dx = {
            -2, 2, 2,-2,  -1, 2, 1, -2,  1, 2, -1, -2,  0, 2, 0,-2,      1, 1, -1, -1,    1, 0,-1, 0, 0
    };
    static int []dy = {
            2, 2,-2,-2,   2, 1,-2,-1,     2,-1,-2, 1,    2, 0,-2, 0,   1,-1,-1, 1 , 0,-1, 0, 1, 0
    };
    // ok the below layout isn't accurate anymore -- it was tweaked to avoid looking like contemporary iconography
    // 01 05 09 13 02
    // 16 20 24 17 06
    // 12 23    21 10
    // 08 19 22 18 14
    // 04 15 11 07 03
    /** Paints incorrectly painted tiles around our build target */
    public static void paintBuildTarget() throws GameActionException {
        //We have not decided on a build target so we can't paint it
        if(buildTarget == null || bMode == BuildMode.NONE) return;
        if (!rc.canSenseLocation(buildTarget)) return;
        if (!rc.isActionReady()) return;

        //Determines what paint pattern we need to paint based on bMode and tower type
        boolean[][] paintPattern;
        if(bMode == BuildMode.BUILD_SRP) {
            paintPattern = rc.getResourcePattern();
        } else {
            //needs to choose what tower it wants to paint
            if(rc.getChips() >= 10000) {
                buildType = UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                buildType = getTowerMark();
            }
            if(buildType == null) return;
            paintPattern = rc.getTowerPattern(buildType);
        } 
        // paint move target when rotating around the tower to avoid empty tile penalty
        if (moveTarget != null && moveTarget.isWithinDistanceSquared(buildTarget, 2)) {
            indicator += "[painttarget]";
            if (rc.senseMapInfo(moveTarget).getPaint() == PaintType.EMPTY && rc.canAttack(moveTarget)) {
                rc.attack(moveTarget);
                return;
            }
        }

        //Iterates through locations, paints it if it had the incorrect paint
        for (int i = 0; i < 25; i ++) {
            MapLocation loc = buildTarget.translate(dx[i], dy[i]);
            if(!rc.canAttack(loc)) continue;
            var info = rc.senseMapInfo(loc);
            if(!info.isPassable()) continue;
            int x = buildTarget.x - loc.x + 2;
            int y = buildTarget.y - loc.y + 2;
            if(info.getPaint().equals(PaintType.EMPTY) || info.getPaint().isSecondary() != paintPattern[x][y]) {
                //correcting paint, returning because we can only attack once a turn
                rc.attack(loc, paintPattern[x][y]);
                return;
            }
        }
    }

    /** Will complete the SRP and tower pattern at build target if possible */
    public static void completeBuiltTarget() throws GameActionException {
        //no build target to complete
        if(buildTarget == null || bMode == BuildMode.NONE) return;
        if (!rc.canSenseLocation(buildTarget)) return;
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

    /* End Build */

    public static void updateRefillTarget() throws GameActionException {
        MapLocation[] locations = rc.senseNearbyRuins(-1);
        
        for(MapLocation loc : locations) {
            RobotInfo robotInfo = rc.senseRobotAtLocation(loc);
            if(robotInfo == null) continue;
            if(robotInfo.getTeam() != myTeam) continue;

            if(robotInfo.getType().isTowerType() && robotInfo.getPaintAmount() > 0) {
                refillTarget = robotInfo.getLocation();
            }
        }

        refillTarget =  getClosestLocation(paintTowerLocations);
    }

    public static void updateMoveTarget() throws GameActionException {
        switch(mode) {
            case ATTACK: moveTarget = attackTarget;
                break;
            case BOOM: moveTarget = rotateAroundBuiltTarget();
                break;
            case REFILL: moveTarget = refillTarget;
                break;
            default: moveTarget = null;
                break;
        }
    }

    public static void updateCommTarget() {
        for (var msg : rc.readMessages(rc.getRoundNum())) {
            var code = msg.getBytes();
            if (Comms.getType(code) != CommType.TargetEnemy) continue;
            var loc = rc.getLocation();
            var enemyLoc = Comms.getLocation(code);
            var dist = loc.distanceSquaredTo(enemyLoc);
            if (commTarget == null || dist < loc.distanceSquaredTo(commTarget)) {
                commTarget = loc;
            }
        }
    }

    //will attempt to refill on any nearby towers
    public static void refill() throws GameActionException {
        for(MapLocation ruin : rc.senseNearbyRuins(-1)) {
            RobotInfo robot = rc.senseRobotAtLocation(ruin);
            if(robot == null) continue;
            if(robot.getType().isTowerType()) requestPaint(robot.getLocation(), UnitType.SOLDIER.paintCapacity-rc.getPaint());
        }
    }
    
    /** Changes mode based on criteria I haven't quite figured out yet @aidan */
    public static void updateMode() throws GameActionException {
        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
            mode = Modes.ATTACK;
            return;
        }

        if(bMode == BuildMode.BUILD_TOWER && buildTarget != null) {
            if (rc.getPaint() < 10 && paintTowerLocations.size != 0) {
                mode = Modes.REFILL;
                lastRuinTarget = buildTarget;
            } else {
                mode = Modes.BOOM;
            }
            return;
        }

        if(enemyTower != null && rc.getLocation().distanceSquaredTo(enemyTower) <= 25) {
            mode = Modes.ATTACK;
            return;
        }

        if(rc.getPaint() <= 40 && roundNum - lastRefillEnd > 10 && paintTowerLocations.size != 0) {
            mode = Modes.REFILL;
            if (buildTarget != null) {
                lastRuinTarget = buildTarget;
                indicator += "last build target: " + buildTarget;
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

    public static void updateAttackTarget() throws GameActionException {
        if(enemyTower == null) {
            attackTarget = null;
            return;
        } else if(enemyTower.distanceSquaredTo(rc.getLocation()) > 16) {
            attackTarget = enemyTower;
            return;
        } else {
            SoldierMicro.doMicro(enemyTower);
        }
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

    /************************************************************************\
    |*                                DEBUG                                 *|
    \************************************************************************/

    /** Prints all debug info */
    public static void debug() {
        if (DEBUG == 0) return;
        indicator += "[Mode:" + mode + "]";
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
        int towersTargeting = 0;
        int moppersTargeting = 0;
        int towersOneMoveAway = 0;
        boolean canMove = true;
        int minHealth = Integer.MAX_VALUE;
        boolean actionReady;
        PaintType paint; 
        int towerHealth = Integer.MAX_VALUE;
        int friendlies = 0;
        int distanceToTower = Integer.MAX_VALUE;

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
                boolean isTower = robot.getType().isTowerType();
                if(isTower) {
                    if(location.isWithinDistanceSquared(robot.getLocation(), 16)) {
                        towersOneMoveAway++;
                    }
                    if(location.isWithinDistanceSquared(robot.getLocation(), robot.getType().actionRadiusSquared)) {
                        towersTargeting++;
                        towerHealth = towerHealth < robot.getHealth() ? towerHealth : robot.getHealth();
                        friendlies += rc.senseNearbyRobots(robot.getLocation(), 16, myTeam).length;
                    }
                    
                    distanceToTower = Math.min(distanceToTower, location.distanceSquaredTo(robot.getLocation()));
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

            if(friendlies > m.friendlies) return true;
            if(friendlies < m.friendlies) return false;

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
            if(towersOneMoveAway < m.towersOneMoveAway) return false;

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

        public boolean isBetterRunaway(MicroInfo m) {
            //if we can't move there ain't no point in the rest
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            //both squares are useless to us
            if(!canMove && !m.canMove) return true;

            //wants to get out of range of any towers
            if(towersTargeting < m.towersTargeting) return true;
            if(towersTargeting > m.towersTargeting) return false;

            if(distanceToTower > m.distanceToTower) return true;
            if(distanceToTower < m.distanceToTower) return false;

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

        public String toString() {
            String rslt = "";
            rslt += location.toString() + ": ";
            rslt += canMove + ", " + towersTargeting + ", ";
            rslt += towersOneMoveAway + ", " + towerHealth + ", ";
            rslt += friendlies + ".";
            return rslt;
        }
    }
}
