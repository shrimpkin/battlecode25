package V09Micro.Micro;

import V09Micro.Globals;
import battlecode.common.*;

public class SoldierMicro extends Globals{
    static int DEBUG = 0;
    public static boolean doMicro(MapLocation tower) throws GameActionException {
        if(tower == null) return false;
        
        MicroInfo[] microInfo = new MicroInfo[9];
        if(DEBUG == 1) System.out.println("MicroInfo for: " + rc.getLocation().toString());
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(Direction.values()[i]);
            microInfo[i].updateEnemiesTargeting();

            if(DEBUG == 1 && microInfo[i] != null) {
                System.out.println(microInfo[i].toString());
            }
        }

        MicroInfo best = microInfo[8];  
        boolean shouldRunaway = rc.getHealth() <= 30;
        boolean shouldAttack = (rc.getRoundNum() % 2 == 0) && (rc.isActionReady());

        if(shouldRunaway && DEBUG == 1) System.out.println("Run at " + rc.getLocation().toString());
        if(shouldAttack && DEBUG == 1) System.out.println("Attack at " + rc.getLocation().toString());
        if(DEBUG == 1) System.out.println("\n");

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
        MapLocation towerTarget;

        double paintLoss = 0; 
        double healthLoss = 0;

        static int[][] attackPositionValues = {
            {0,0,0,0,0,0,0,0,0},
            {0,0,0,0,5,0,0,0,0},
            {0,0,5,2,1,2,5,0,0},
            {0,0,2,0,0,0,2,0,0},
            {0,5,2,0,0,0,2,5,0},
            {0,0,2,0,0,0,2,0,0},
            {0,0,5,2,1,2,5,0,0},
            {0,0,5,2,1,2,5,0,0},
            {0,0,0,0,0,0,0,0,0},
        };

        static int[][] retreatPositionValues = {
            {0,0,0,1,1,1,0,0,0},
            {0,1,2,4,0,4,2,1,0},
            {0,4,0,0,0,0,0,4,0},
            {1,4,0,0,0,0,0,4,1},
            {1,0,0,0,0,0,0,0,1},
            {1,4,0,0,0,0,0,4,1},
            {0,4,0,0,0,0,0,4,0},
            {0,1,2,4,0,4,2,1,0},
            {0,0,0,1,1,1,0,0,0},
        };

        int attackPositionScore;
        int retreatPositionScore;

        public MicroInfo(Direction dir) throws GameActionException {
            direction = dir;
            location = rc.getLocation().add(dir);

            if (!dir.equals(Direction.CENTER) && !rc.canMove(dir)) canMove = false;
            if(rc.canSenseLocation(location)) {
                paint = rc.senseMapInfo(location).getPaint();
                paintLoss += paint.isEnemy() ? 2 : 0;
                paintLoss += paint.equals(PaintType.EMPTY) ? 1 : 0;
            }

            actionReady = rc.isActionReady();
        }

        public void updateEnemiesTargeting() throws GameActionException {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);
            for(RobotInfo robot : robots) {
                boolean isTower = robot.getType().isTowerType();
                if(isTower) {
                    if(location.isWithinDistanceSquared(robot.getLocation(), robot.getType().actionRadiusSquared)) {
                        healthLoss += robot.getType().aoeAttackStrength + robot.getType().attackStrength;
                        friendlies += rc.senseNearbyRobots(robot.getLocation(), 16, myTeam).length;
                    }

                    if(towerHealth > robot.getHealth()) {
                        towerHealth = robot.getHealth();
                        towerTarget = robot.getLocation();
                    }
                }

                //mopper will attack so we would lose paint
                if(robot.getType().equals(UnitType.MOPPER) && location.isWithinDistanceSquared(robot.getLocation(), 10)) {
                    paintLoss += (10 / 3);
                }
            }

            if(towerTarget != null) {
                int x = location.x - towerTarget.x + 4;
                int y = location.y - towerTarget.y + 4;

                x = clamp(x, 0, 8);
                y = clamp(y, 0, 8);
                attackPositionScore = attackPositionValues[x][y];
                retreatPositionScore = retreatPositionValues[x][y];
            }
        }

        public boolean isBetterAttack(MicroInfo m) {
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            if(towersTargeting == 1 && m.towersTargeting != 1) return true;
            if(towersTargeting != 1 && m.towersTargeting == 1) return false;

            if(Double.compare(healthLoss, m.healthLoss) > 0) return true;
            if(Double.compare(healthLoss, m.healthLoss) < 0) return false;

            if(towerHealth < m.towerHealth) return true;
            if(towerHealth > m.towerHealth) return false;
            
            if(attackPositionScore > m.attackPositionScore) return true;
            if(attackPositionScore < m.attackPositionScore) return false;

            if(Double.compare(paintLoss, m.paintLoss) < 0) return true; 
            if(Double.compare(paintLoss, m.paintLoss) > 0) return false;

            if(friendlies > m.friendlies) return true;
            if(friendlies < m.friendlies) return false;

            return true;
        }

        public boolean isBetterRetreat(MicroInfo m) {
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            if(Double.compare(healthLoss, m.healthLoss) < 0) return true;
            if(Double.compare(healthLoss, m.healthLoss) > 0) return false;

            if(retreatPositionScore > m.retreatPositionScore) return true;
            if(retreatPositionScore < m.retreatPositionScore) return false;
            
            if(Double.compare(paintLoss, m.paintLoss) < 0) return true; 
            if(Double.compare(paintLoss, m.paintLoss) > 0) return false;
            
            return true;
        }

        public boolean isBetterRunaway(MicroInfo m) {
            if(canMove && !m.canMove) return true;
            if(!canMove && m.canMove) return false;

            if(Double.compare(healthLoss, m.healthLoss) > 0) return true;
            if(Double.compare(healthLoss, m.healthLoss) < 0) return false;

            if(distanceToTower > m.distanceToTower) return true;
            if(distanceToTower < m.distanceToTower) return false;

            if(moppersTargeting < m.moppersTargeting) return true;
            if(moppersTargeting > m.moppersTargeting) return false;
            
            if(Double.compare(paintLoss, m.paintLoss) > 0) return true; 
            if(Double.compare(paintLoss, m.paintLoss) < 0) return false;

            return true;
        }

        public String toString() {
            String rslt = "";
            rslt += location.toString() + ": ";
            rslt += canMove + ", " + healthLoss + ", ";
            rslt += paintLoss + ", " + attackPositionScore + ", ";
            rslt += retreatPositionScore + ".";
            return rslt;
        }
    }
}
