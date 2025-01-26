package V09MicroImprovements.Micro;

import V09MicroImprovements.Globals;
import V09MicroImprovements.Tools.FastIntSet;
import V09MicroImprovements.Tools.FastLocSet;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

public class SplasherMicro extends Globals {
    Direction[] dirs = Direction.values();
    int DEBUG = 0;
    MicroInfo[] microInfo;

    public void computeMicroArray(MapLocation splashTarget) throws GameActionException {
        
         microInfo = new MicroInfo[]{
                new MicroInfo(dirs[0]),
                new MicroInfo(dirs[1]),
                new MicroInfo(dirs[2]),
                new MicroInfo(dirs[3]),
                new MicroInfo(dirs[4]),
                new MicroInfo(dirs[5]),
                new MicroInfo(dirs[6]),
                new MicroInfo(dirs[7]),
                new MicroInfo(dirs[8])
        };

        microInfo[0].update(splashTarget);
        microInfo[1].update(splashTarget);
        microInfo[2].update(splashTarget);
        microInfo[3].update(splashTarget);
        microInfo[4].update(splashTarget);
        microInfo[5].update(splashTarget);
        microInfo[6].update(splashTarget);
        microInfo[7].update(splashTarget);
        microInfo[8].update(splashTarget);

        if(DEBUG == 1) {
            System.out.println("Printing micro info for robot at: " + rc.getLocation().toString());
            for(int i = 0; i < 8; i++) {
                System.out.println(microInfo[i].toString());
            }
        }
    }

    public boolean doMicro() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        
        MicroInfo bestMicro = microInfo[0];
        for (int i = 8; --i >= 0;) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
        }
        if (bestMicro.direction == Direction.CENTER) return true;

        if (rc.canMove(bestMicro.direction)) {
            rc.move(bestMicro.direction);
            return true;
        }
        return false;
    }

    public class MicroInfo {
        Direction direction;
        MapLocation location;
        int towersTargeting = 0;
        int moppersTargeting = 0;
        boolean canMove = true;
        boolean actionReady;
        PaintType paint; 
        MapLocation bestSplash;
        boolean canSplash;

        public MicroInfo(Direction dir) throws GameActionException {
            direction = dir;
            location = rc.getLocation().add(dir);

            if (!dir.equals(Direction.CENTER) && !rc.canMove(dir)) canMove = false;
            if(rc.canSenseLocation(location)) paint = rc.senseMapInfo(location).getPaint();

            actionReady = rc.isActionReady();
        }
       
        //updates all fields for decision making
        public void update(MapLocation splashTarget) throws GameActionException {
            bestSplash = splashTarget;
            RobotInfo[] robots = rc.senseNearbyRobots(-1, opponentTeam);
            for(RobotInfo robot : robots) {
                boolean isTower = robot.getType().isTowerType();
                if(isTower) {
                    if(location.isWithinDistanceSquared(robot.getLocation(), robot.getType().actionRadiusSquared)) {
                        towersTargeting++;
                    }
                }

                if(robot.getType().equals(UnitType.MOPPER) && location.isWithinDistanceSquared(robot.getLocation(), 10)) {
                    moppersTargeting++;
                }
            }

            if(bestSplash != null) {
                canSplash = bestSplash.isWithinDistanceSquared(location, UnitType.SPLASHER.actionRadiusSquared);
            }
        }

        public boolean isBetter(MicroInfo m) throws GameActionException {
            if(!rc.isActionReady()) return betterRunaway(m);
            return betterPaintSplash(m);
        }

        public boolean betterPaintSplash(MicroInfo m) throws GameActionException {
            if(canSplash && !m.canSplash) return true;
            if(!canSplash && m.canSplash) return false;

            return betterRunaway(null);
        }

        public boolean betterRunaway(MicroInfo m) throws GameActionException {

            if(towersTargeting < m.towersTargeting) return true;
            if(towersTargeting > m.towersTargeting) return false;

            if(moppersTargeting < m.moppersTargeting) return true;
            if(moppersTargeting > m.moppersTargeting) return false;

            if(paint.isAlly() && !m.paint.isAlly()) return true;
            if(!paint.isAlly() && m.paint.isAlly()) return false;

            if(!paint.isEnemy() && m.paint.isEnemy()) return true;
            if(paint.isEnemy() && !m.paint.isEnemy()) return false;

            return true;
        }

    }

}
