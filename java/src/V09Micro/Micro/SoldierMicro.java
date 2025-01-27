package V09Micro.Micro;

import V09Micro.*;
import battlecode.common.*;

public class SoldierMicro extends Globals{
    
    public static boolean doMicro(MapLocation tower) throws GameActionException {
        if(tower == null) return false;
        
        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(Direction.values()[i]);
            microInfo[i].updateEnemiesTargeting();
        }

        MicroInfo best = microInfo[8];  
        boolean shouldRunaway = rc.getHealth() <= 30;
        boolean shouldAttack = (rc.getRoundNum() % 2 == 0) && (rc.isActionReady());

        //if(shouldRunaway) System.out.println("Run at " + rc.getLocation().toString());
        //if(shouldAttack) System.out.println("Attack at " + rc.getLocation().toString());
        for (int i = 0; i < 8; i++) {

            if(shouldRunaway) {
                if(microInfo[i].isBetterRunaway(best)) best = microInfo[i];
                continue;
            } 
            
            if (shouldAttack) {
                if(microInfo[i].isBetterAttack(best)) best = microInfo[i];
                continue;
            } 
            
            if (microInfo[i].isBetterRetreat(best)) {
                best = microInfo[i];
            }
        }

        if(rc.canMove(best.direction)) {
            rc.move(best.direction);
            return true;
        }

        return false;
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
        boolean toClose = false;

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
                    if(location.isWithinDistanceSquared(robot.getLocation(), 2)) {
                        toClose = true;
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

            if(!toClose && m.toClose) return true;
            if(toClose && !m.toClose) return false;

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
