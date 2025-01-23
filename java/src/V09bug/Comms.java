package V09bug;

import V09bug.Tools.CommType;
import battlecode.common.MapLocation;
import battlecode.common.UnitType;

public class Comms extends Globals {

//    static final int UNIT_TYPE_OFFSET =16; // use 4 bits for unit type orginal
    static final int LOCATION_OFFSET = 12;
    static final int ROUND_OFFSET = 11;
    static final int COMMTYPE_OFFSET = 28;

    public static int encodeMessage(CommType type, MapLocation loc) {
        return encodeMessage(type, loc, 0);
    }

    public static int encodeMessage(CommType type, MapLocation loc, int round) {
        return (type.ordinal() << COMMTYPE_OFFSET) | (round << 12) | (loc.y << 6) | loc.x;
    }

    public static int encodeMessage(UnitType unit, MapLocation loc) {
        return (CommType.CommunicateType.ordinal() << COMMTYPE_OFFSET)
//                | robotID << UNIT_TYPE_OFFSET
                | unit.ordinal() << LOCATION_OFFSET
                | (loc.y << 6)
                | loc.x;
    }
    // get communication type
    public static CommType getType(int encoded) {
        return CommType.values()[encoded >> COMMTYPE_OFFSET];
    }
    // get stored information
    public static MapLocation getLocation(int encoded) {
        return new MapLocation(encoded & 0x3F, (encoded >> 6) & 0x3F);
    }
    public static UnitType getUnitType(int encoded) {
        return UnitType.values()[(encoded & 0x0FFFFFFF) >> LOCATION_OFFSET];
    }
    public static int getOriginalRound(int encoded) {
        return (encoded >> LOCATION_OFFSET) & 0x7FF;
    }
//    public static int getRobotID(int encoded) {
//        return (encoded & 0x0FFFFFFF) >> UNIT_TYPE_OFFSET;
//    }
}
