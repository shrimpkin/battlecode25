package V05;

import battlecode.common.*;

public class Comms extends Globals {
    // least significant 2 bits
    // 0 = MOPPER
    // 1 = SPLASHER
    // 2 = SOLDIER
    // 3 = TOWER

    // next 6 bits
    // x coordinate of maplocation

    // next 6 bits
    // y coordinate of maplocation

    // next 3 bits
    // mode ???

    public static int encodeMsg(UnitType receiver, int x, int y) throws GameActionException {
        int msg = 0;
        if (receiver == UnitType.MOPPER) {
            msg |= 0;
        } else if (receiver == UnitType.SPLASHER) {
            msg |= 1;
        } else if (receiver == UnitType.SOLDIER) {
            msg |= 2;
        } else {
            throw new GameActionException(null, "MESSAGE HAS WRONG UNIT TYPE");
        }

        msg |= (x << 2);
        msg |= (y << 6 + 2);

        System.out.println("out msg: " + msg);

        return msg;
    }

    public static int[] decodeMsg(int msg) {
        System.out.println("in msg: " + msg);
        int[] decodedMsg = {msg & 0b11, (msg & 0b11111100) >> 2, (msg & 0b11111100000000) >> (6 + 2)};
        return decodedMsg;
    }

    public static UnitType getUnitType(int val) {
        if (val == 0) 
            return UnitType.MOPPER;
        if (val == 1)
            return UnitType.SPLASHER;
        if (val == 2) 
            return UnitType.SOLDIER;
        return null;
    }
}
