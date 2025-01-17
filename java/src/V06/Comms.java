package V06;

import battlecode.common.*;

public class Comms extends Globals {
    public static int encodeMsg(int targetX, int targetY) throws GameActionException {
        int msg = 0;

        msg |= targetX;
        msg |= (targetY << 6);

        return msg;
    }

    public static int[] decodeMsg(int msg) {
        int[] decodedMsg = {
            msg & 0x3F, 
            (msg & 0xFC0) >> 6, 
        };
        return decodedMsg;
    }
}
