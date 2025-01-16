package V05;

import battlecode.common.*;

public class Comms extends Globals {
    public static int encodeMsg(int towerX, int towerY, int targetX, int targetY) throws GameActionException {
        int msg = 0;

        msg |= towerX;
        msg |= (towerY << 6);
        msg |= (targetX << 12);
        msg |= (targetY << 18);

        return msg;
    }

    public static int[] decodeMsg(int msg) {
        int[] decodedMsg = {
            msg & 0x3F, 
            (msg & 0xFC0) >> 6, 
            (msg & 0x3F000) >> 12,
            (msg & 0x3FC000) >> 18
        };
        return decodedMsg;
    }
}
