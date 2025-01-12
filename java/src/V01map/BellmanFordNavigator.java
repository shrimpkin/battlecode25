package V01map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


/**
 *   Unrolled Bellman Ford Algorithm, altered from camel_case
 */
public class BellmanFordNavigator extends Globals {
    public static Direction getBestDirection(MapLocation target) throws GameActionException {

        MapLocation location1 = rc.adjacentLocation(Direction.WEST);
        boolean canVisit1 = rc.canMove(Direction.WEST);

        MapLocation location2 = rc.adjacentLocation(Direction.EAST);
        boolean canVisit2 = rc.canMove(Direction.EAST);

        MapLocation location3 = rc.adjacentLocation(Direction.SOUTH);
        boolean canVisit3 = rc.canMove(Direction.SOUTH);

        MapLocation location4 = rc.adjacentLocation(Direction.NORTH);
        boolean canVisit4 = rc.canMove(Direction.NORTH);

        MapLocation location5 = rc.adjacentLocation(Direction.SOUTHWEST);
        boolean canVisit5 = rc.canMove(Direction.SOUTHWEST);

        MapLocation location6 = rc.adjacentLocation(Direction.NORTHWEST);
        boolean canVisit6 = rc.canMove(Direction.NORTHWEST);

        MapLocation location7 = rc.adjacentLocation(Direction.SOUTHEAST);
        boolean canVisit7 = rc.canMove(Direction.SOUTHEAST);

        MapLocation location8 = rc.adjacentLocation(Direction.NORTHEAST);
        boolean canVisit8 = rc.canMove(Direction.NORTHEAST);

        if (canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit7 && !canVisit8) {
            return Direction.WEST;
        }

        if (canVisit2 && !canVisit1 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit7 && !canVisit8) {
            return Direction.EAST;
        }

        if (canVisit3 && !canVisit1 && !canVisit2 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit7 && !canVisit8) {
            return Direction.SOUTH;
        }

        if (canVisit4 && !canVisit1 && !canVisit2 && !canVisit3 && !canVisit5 && !canVisit6 && !canVisit7 && !canVisit8) {
            return Direction.NORTH;
        }

        if (canVisit5 && !canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit6 && !canVisit7 && !canVisit8) {
            return Direction.SOUTHWEST;
        }

        if (canVisit6 && !canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit7 && !canVisit8) {
            return Direction.NORTHWEST;
        }

        if (canVisit7 && !canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit8) {
            return Direction.SOUTHEAST;
        }

        if (canVisit8 && !canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit7) {
            return Direction.NORTHEAST;
        }

        if (!canVisit1 && !canVisit2 && !canVisit3 && !canVisit4 && !canVisit5 && !canVisit6 && !canVisit7 && !canVisit8) {
            return null;
        }
        
        int distance1 = 1_000_000;
        Direction direction1 = null;
        int weight1 = canVisit1 ? 1 : 1_000_000;

        int distance2 = 1_000_000;
        Direction direction2 = null;
        int weight2 = canVisit2 ? 1 : 1_000_000;

        int distance3 = 1_000_000;
        Direction direction3 = null;
        int weight3 = canVisit3 ? 1 : 1_000_000;

        int distance4 = 1_000_000;
        Direction direction4 = null;
        int weight4 = canVisit4 ? 1 : 1_000_000;

        int distance5 = 1_000_000;
        Direction direction5 = null;
        int weight5 = canVisit5 ? 1 : 1_000_000;

        int distance6 = 1_000_000;
        Direction direction6 = null;
        int weight6 = canVisit6 ? 1 : 1_000_000;

        int distance7 = 1_000_000;
        Direction direction7 = null;
        int weight7 = canVisit7 ? 1 : 1_000_000;

        int distance8 = 1_000_000;
        Direction direction8 = null;
        int weight8 = canVisit8 ? 1 : 1_000_000;

        MapLocation location9 = location1.add(Direction.WEST);
        boolean canVisit9 = rc.canSenseLocation(location9) && (rc.sensePassability(location9));
        int distance9 = 1_000_000;
        Direction direction9 = null;
        int weight9 = canVisit9 ? 1 : 1_000_000;

        MapLocation location12 = location2.add(Direction.EAST);
        boolean canVisit12 = rc.canSenseLocation(location12) && (rc.sensePassability(location12));
        int distance12 = 1_000_000;
        Direction direction12 = null;
        int weight12 = canVisit12 ? 1 : 1_000_000;

        MapLocation location15 = location3.add(Direction.SOUTH);
        boolean canVisit15 = rc.canSenseLocation(location15) && (rc.sensePassability(location15));
        int distance15 = 1_000_000;
        Direction direction15 = null;
        int weight15 = canVisit15 ? 1 : 1_000_000;

        MapLocation location18 = location4.add(Direction.NORTH);
        boolean canVisit18 = rc.canSenseLocation(location18) && (rc.sensePassability(location18));
        int distance18 = 1_000_000;
        Direction direction18 = null;
        int weight18 = canVisit18 ? 1 : 1_000_000;

        MapLocation location10 = location1.add(Direction.SOUTHWEST);
        boolean canVisit10 = rc.canSenseLocation(location10) && (rc.sensePassability(location10));
        int distance10 = 1_000_000;
        Direction direction10 = null;
        int weight10 = canVisit10 ? 1 : 1_000_000;

        MapLocation location11 = location1.add(Direction.NORTHWEST);
        boolean canVisit11 = rc.canSenseLocation(location11) && (rc.sensePassability(location11));
        int distance11 = 1_000_000;
        Direction direction11 = null;
        int weight11 = canVisit11 ? 1 : 1_000_000;

        MapLocation location13 = location2.add(Direction.SOUTHEAST);
        boolean canVisit13 = rc.canSenseLocation(location13) && (rc.sensePassability(location13));
        int distance13 = 1_000_000;
        Direction direction13 = null;
        int weight13 = canVisit13 ? 1 : 1_000_000;

        MapLocation location14 = location2.add(Direction.NORTHEAST);
        boolean canVisit14 = rc.canSenseLocation(location14) && (rc.sensePassability(location14));
        int distance14 = 1_000_000;
        Direction direction14 = null;
        int weight14 = canVisit14 ? 1 : 1_000_000;

        MapLocation location16 = location3.add(Direction.SOUTHWEST);
        boolean canVisit16 = rc.canSenseLocation(location16) && (rc.sensePassability(location16));
        int distance16 = 1_000_000;
        Direction direction16 = null;
        int weight16 = canVisit16 ? 1 : 1_000_000;

        MapLocation location17 = location3.add(Direction.SOUTHEAST);
        boolean canVisit17 = rc.canSenseLocation(location17) && (rc.sensePassability(location17));
        int distance17 = 1_000_000;
        Direction direction17 = null;
        int weight17 = canVisit17 ? 1 : 1_000_000;

        MapLocation location19 = location4.add(Direction.NORTHWEST);
        boolean canVisit19 = rc.canSenseLocation(location19) && (rc.sensePassability(location19));
        int distance19 = 1_000_000;
        Direction direction19 = null;
        int weight19 = canVisit19 ? 1 : 1_000_000;

        MapLocation location20 = location4.add(Direction.NORTHEAST);
        boolean canVisit20 = rc.canSenseLocation(location20) && (rc.sensePassability(location20));
        int distance20 = 1_000_000;
        Direction direction20 = null;
        int weight20 = canVisit20 ? 1 : 1_000_000;

        MapLocation location21 = location5.add(Direction.SOUTHWEST);
        boolean canVisit21 = rc.canSenseLocation(location21) && (rc.sensePassability(location21));
        int distance21 = 1_000_000;
        Direction direction21 = null;
        int weight21 = canVisit21 ? 1 : 1_000_000;

        MapLocation location22 = location6.add(Direction.NORTHWEST);
        boolean canVisit22 = rc.canSenseLocation(location22) && (rc.sensePassability(location22));
        int distance22 = 1_000_000;
        Direction direction22 = null;
        int weight22 = canVisit22 ? 1 : 1_000_000;

        MapLocation location23 = location7.add(Direction.SOUTHEAST);
        boolean canVisit23 = rc.canSenseLocation(location23) && (rc.sensePassability(location23));
        int distance23 = 1_000_000;
        Direction direction23 = null;
        int weight23 = canVisit23 ? 1 : 1_000_000;

        MapLocation location24 = location8.add(Direction.NORTHEAST);
        boolean canVisit24 = rc.canSenseLocation(location24) && (rc.sensePassability(location24));
        int distance24 = 1_000_000;
        Direction direction24 = null;
        int weight24 = canVisit24 ? 1 : 1_000_000;

        MapLocation location25 = location9.add(Direction.WEST);
        boolean canVisit25 = rc.canSenseLocation(location25) && (rc.sensePassability(location25));
        int distance25 = 1_000_000;
        Direction direction25 = null;
        int weight25 = canVisit25 ? 1 : 1_000_000;

        MapLocation location30 = location12.add(Direction.EAST);
        boolean canVisit30 = rc.canSenseLocation(location30) && (rc.sensePassability(location30));
        int distance30 = 1_000_000;
        Direction direction30 = null;
        int weight30 = canVisit30 ? 1 : 1_000_000;

        MapLocation location35 = location15.add(Direction.SOUTH);
        boolean canVisit35 = rc.canSenseLocation(location35) && (rc.sensePassability(location35));
        int distance35 = 1_000_000;
        Direction direction35 = null;
        int weight35 = canVisit35 ? 1 : 1_000_000;

        MapLocation location40 = location18.add(Direction.NORTH);
        boolean canVisit40 = rc.canSenseLocation(location40) && (rc.sensePassability(location40));
        int distance40 = 1_000_000;
        Direction direction40 = null;
        int weight40 = canVisit40 ? 1 : 1_000_000;

        MapLocation location26 = location9.add(Direction.SOUTHWEST);
        boolean canVisit26 = rc.canSenseLocation(location26) && (rc.sensePassability(location26));
        int distance26 = 1_000_000;
        Direction direction26 = null;
        int weight26 = canVisit26 ? 1 : 1_000_000;

        MapLocation location27 = location9.add(Direction.NORTHWEST);
        boolean canVisit27 = rc.canSenseLocation(location27) && (rc.sensePassability(location27));
        int distance27 = 1_000_000;
        Direction direction27 = null;
        int weight27 = canVisit27 ? 1 : 1_000_000;

        MapLocation location31 = location12.add(Direction.SOUTHEAST);
        boolean canVisit31 = rc.canSenseLocation(location31) && (rc.sensePassability(location31));
        int distance31 = 1_000_000;
        Direction direction31 = null;
        int weight31 = canVisit31 ? 1 : 1_000_000;

        MapLocation location32 = location12.add(Direction.NORTHEAST);
        boolean canVisit32 = rc.canSenseLocation(location32) && (rc.sensePassability(location32));
        int distance32 = 1_000_000;
        Direction direction32 = null;
        int weight32 = canVisit32 ? 1 : 1_000_000;

        MapLocation location36 = location15.add(Direction.SOUTHWEST);
        boolean canVisit36 = rc.canSenseLocation(location36) && (rc.sensePassability(location36));
        int distance36 = 1_000_000;
        Direction direction36 = null;
        int weight36 = canVisit36 ? 1 : 1_000_000;

        MapLocation location37 = location15.add(Direction.SOUTHEAST);
        boolean canVisit37 = rc.canSenseLocation(location37) && (rc.sensePassability(location37));
        int distance37 = 1_000_000;
        Direction direction37 = null;
        int weight37 = canVisit37 ? 1 : 1_000_000;

        MapLocation location41 = location18.add(Direction.NORTHWEST);
        boolean canVisit41 = rc.canSenseLocation(location41) && (rc.sensePassability(location41));
        int distance41 = 1_000_000;
        Direction direction41 = null;
        int weight41 = canVisit41 ? 1 : 1_000_000;

        MapLocation location42 = location18.add(Direction.NORTHEAST);
        boolean canVisit42 = rc.canSenseLocation(location42) && (rc.sensePassability(location42));
        int distance42 = 1_000_000;
        Direction direction42 = null;
        int weight42 = canVisit42 ? 1 : 1_000_000;

        MapLocation location28 = location10.add(Direction.SOUTHWEST);
        boolean canVisit28 = rc.canSenseLocation(location28) && (rc.sensePassability(location28));
        int distance28 = 1_000_000;
        Direction direction28 = null;
        int weight28 = canVisit28 ? 1 : 1_000_000;

        MapLocation location29 = location11.add(Direction.NORTHWEST);
        boolean canVisit29 = rc.canSenseLocation(location29) && (rc.sensePassability(location29));
        int distance29 = 1_000_000;
        Direction direction29 = null;
        int weight29 = canVisit29 ? 1 : 1_000_000;

        MapLocation location33 = location13.add(Direction.SOUTHEAST);
        boolean canVisit33 = rc.canSenseLocation(location33) && (rc.sensePassability(location33));
        int distance33 = 1_000_000;
        Direction direction33 = null;
        int weight33 = canVisit33 ? 1 : 1_000_000;

        MapLocation location34 = location14.add(Direction.NORTHEAST);
        boolean canVisit34 = rc.canSenseLocation(location34) && (rc.sensePassability(location34));
        int distance34 = 1_000_000;
        Direction direction34 = null;
        int weight34 = canVisit34 ? 1 : 1_000_000;

        MapLocation location38 = location16.add(Direction.SOUTHWEST);
        boolean canVisit38 = rc.canSenseLocation(location38) && (rc.sensePassability(location38));
        int distance38 = 1_000_000;
        Direction direction38 = null;
        int weight38 = canVisit38 ? 1 : 1_000_000;

        MapLocation location39 = location17.add(Direction.SOUTHEAST);
        boolean canVisit39 = rc.canSenseLocation(location39) && (rc.sensePassability(location39));
        int distance39 = 1_000_000;
        Direction direction39 = null;
        int weight39 = canVisit39 ? 1 : 1_000_000;

        MapLocation location43 = location19.add(Direction.NORTHWEST);
        boolean canVisit43 = rc.canSenseLocation(location43) && (rc.sensePassability(location43));
        int distance43 = 1_000_000;
        Direction direction43 = null;
        int weight43 = canVisit43 ? 1 : 1_000_000;

        MapLocation location44 = location20.add(Direction.NORTHEAST);
        boolean canVisit44 = rc.canSenseLocation(location44) && (rc.sensePassability(location44));
        int distance44 = 1_000_000;
        Direction direction44 = null;
        int weight44 = canVisit44 ? 1 : 1_000_000;

        MapLocation location49 = location25.add(Direction.WEST);
        boolean canVisit49 = rc.canSenseLocation(location49) && (rc.sensePassability(location49));
        int distance49 = 1_000_000;
        Direction direction49 = null;
        int weight49 = canVisit49 ? 1 : 1_000_000;

        MapLocation location54 = location30.add(Direction.EAST);
        boolean canVisit54 = rc.canSenseLocation(location54) && (rc.sensePassability(location54));
        int distance54 = 1_000_000;
        Direction direction54 = null;
        int weight54 = canVisit54 ? 1 : 1_000_000;

        MapLocation location59 = location35.add(Direction.SOUTH);
        boolean canVisit59 = rc.canSenseLocation(location59) && (rc.sensePassability(location59));
        int distance59 = 1_000_000;
        Direction direction59 = null;
        int weight59 = canVisit59 ? 1 : 1_000_000;

        MapLocation location64 = location40.add(Direction.NORTH);
        boolean canVisit64 = rc.canSenseLocation(location64) && (rc.sensePassability(location64));
        int distance64 = 1_000_000;
        Direction direction64 = null;
        int weight64 = canVisit64 ? 1 : 1_000_000;

        MapLocation location50 = location25.add(Direction.SOUTHWEST);
        boolean canVisit50 = rc.canSenseLocation(location50) && (rc.sensePassability(location50));
        int distance50 = 1_000_000;
        Direction direction50 = null;
        int weight50 = canVisit50 ? 1 : 1_000_000;

        MapLocation location51 = location25.add(Direction.NORTHWEST);
        boolean canVisit51 = rc.canSenseLocation(location51) && (rc.sensePassability(location51));
        int distance51 = 1_000_000;
        Direction direction51 = null;
        int weight51 = canVisit51 ? 1 : 1_000_000;

        MapLocation location55 = location30.add(Direction.SOUTHEAST);
        boolean canVisit55 = rc.canSenseLocation(location55) && (rc.sensePassability(location55));
        int distance55 = 1_000_000;
        Direction direction55 = null;
        int weight55 = canVisit55 ? 1 : 1_000_000;

        MapLocation location56 = location30.add(Direction.NORTHEAST);
        boolean canVisit56 = rc.canSenseLocation(location56) && (rc.sensePassability(location56));
        int distance56 = 1_000_000;
        Direction direction56 = null;
        int weight56 = canVisit56 ? 1 : 1_000_000;

        MapLocation location60 = location35.add(Direction.SOUTHWEST);
        boolean canVisit60 = rc.canSenseLocation(location60) && (rc.sensePassability(location60));
        int distance60 = 1_000_000;
        Direction direction60 = null;
        int weight60 = canVisit60 ? 1 : 1_000_000;

        MapLocation location61 = location35.add(Direction.SOUTHEAST);
        boolean canVisit61 = rc.canSenseLocation(location61) && (rc.sensePassability(location61));
        int distance61 = 1_000_000;
        Direction direction61 = null;
        int weight61 = canVisit61 ? 1 : 1_000_000;

        MapLocation location65 = location40.add(Direction.NORTHWEST);
        boolean canVisit65 = rc.canSenseLocation(location65) && (rc.sensePassability(location65));
        int distance65 = 1_000_000;
        Direction direction65 = null;
        int weight65 = canVisit65 ? 1 : 1_000_000;

        MapLocation location66 = location40.add(Direction.NORTHEAST);
        boolean canVisit66 = rc.canSenseLocation(location66) && (rc.sensePassability(location66));
        int distance66 = 1_000_000;
        Direction direction66 = null;
        int weight66 = canVisit66 ? 1 : 1_000_000;

        MapLocation location45 = location21.add(Direction.SOUTHWEST);
        boolean canVisit45 = rc.canSenseLocation(location45) && (rc.sensePassability(location45));
        int distance45 = 1_000_000;
        Direction direction45 = null;
        int weight45 = canVisit45 ? 1 : 1_000_000;

        MapLocation location46 = location22.add(Direction.NORTHWEST);
        boolean canVisit46 = rc.canSenseLocation(location46) && (rc.sensePassability(location46));
        int distance46 = 1_000_000;
        Direction direction46 = null;
        int weight46 = canVisit46 ? 1 : 1_000_000;

        MapLocation location47 = location23.add(Direction.SOUTHEAST);
        boolean canVisit47 = rc.canSenseLocation(location47) && (rc.sensePassability(location47));
        int distance47 = 1_000_000;
        Direction direction47 = null;
        int weight47 = canVisit47 ? 1 : 1_000_000;

        MapLocation location48 = location24.add(Direction.NORTHEAST);
        boolean canVisit48 = rc.canSenseLocation(location48) && (rc.sensePassability(location48));
        int distance48 = 1_000_000;
        Direction direction48 = null;
        int weight48 = canVisit48 ? 1 : 1_000_000;

        MapLocation location52 = location26.add(Direction.SOUTHWEST);
        boolean canVisit52 = rc.canSenseLocation(location52) && (rc.sensePassability(location52));
        int distance52 = 1_000_000;
        Direction direction52 = null;
        int weight52 = canVisit52 ? 1 : 1_000_000;

        MapLocation location53 = location27.add(Direction.NORTHWEST);
        boolean canVisit53 = rc.canSenseLocation(location53) && (rc.sensePassability(location53));
        int distance53 = 1_000_000;
        Direction direction53 = null;
        int weight53 = canVisit53 ? 1 : 1_000_000;

        MapLocation location57 = location31.add(Direction.SOUTHEAST);
        boolean canVisit57 = rc.canSenseLocation(location57) && (rc.sensePassability(location57));
        int distance57 = 1_000_000;
        Direction direction57 = null;
        int weight57 = canVisit57 ? 1 : 1_000_000;

        MapLocation location58 = location32.add(Direction.NORTHEAST);
        boolean canVisit58 = rc.canSenseLocation(location58) && (rc.sensePassability(location58));
        int distance58 = 1_000_000;
        Direction direction58 = null;
        int weight58 = canVisit58 ? 1 : 1_000_000;

        MapLocation location62 = location36.add(Direction.SOUTHWEST);
        boolean canVisit62 = rc.canSenseLocation(location62) && (rc.sensePassability(location62));
        int distance62 = 1_000_000;
        Direction direction62 = null;
        int weight62 = canVisit62 ? 1 : 1_000_000;

        MapLocation location63 = location37.add(Direction.SOUTHEAST);
        boolean canVisit63 = rc.canSenseLocation(location63) && (rc.sensePassability(location63));
        int distance63 = 1_000_000;
        Direction direction63 = null;
        int weight63 = canVisit63 ? 1 : 1_000_000;

        MapLocation location67 = location41.add(Direction.NORTHWEST);
        boolean canVisit67 = rc.canSenseLocation(location67) && (rc.sensePassability(location67));
        int distance67 = 1_000_000;
        Direction direction67 = null;
        int weight67 = canVisit67 ? 1 : 1_000_000;

        MapLocation location68 = location42.add(Direction.NORTHEAST);
        boolean canVisit68 = rc.canSenseLocation(location68) && (rc.sensePassability(location68));
        int distance68 = 1_000_000;
        Direction direction68 = null;
        int weight68 = canVisit68 ? 1 : 1_000_000;

        if (canVisit1) {
            distance1 = weight1;
            direction1 = Direction.WEST;
        }

        if (canVisit2) {
            distance2 = weight2;
            direction2 = Direction.EAST;
        }

        if (canVisit3) {
            distance3 = weight3;
            direction3 = Direction.SOUTH;
        }

        if (canVisit4) {
            distance4 = weight4;
            direction4 = Direction.NORTH;
        }

        if (canVisit5) {
            distance5 = weight5;
            direction5 = Direction.SOUTHWEST;
        }

        if (canVisit6) {
            distance6 = weight6;
            direction6 = Direction.NORTHWEST;
        }

        if (canVisit7) {
            distance7 = weight7;
            direction7 = Direction.SOUTHEAST;
        }

        if (canVisit8) {
            distance8 = weight8;
            direction8 = Direction.NORTHEAST;
        }

        if (canVisit9) {
            if (distance1 + weight9 < distance9) {
                distance9 = distance1 + weight9;
                direction9 = direction1;
            }

            if (distance5 + weight9 < distance9) {
                distance9 = distance5 + weight9;
                direction9 = direction5;
            }

            if (distance6 + weight9 < distance9) {
                distance9 = distance6 + weight9;
                direction9 = direction6;
            }
        }

        if (canVisit12) {
            if (distance2 + weight12 < distance12) {
                distance12 = distance2 + weight12;
                direction12 = direction2;
            }

            if (distance7 + weight12 < distance12) {
                distance12 = distance7 + weight12;
                direction12 = direction7;
            }

            if (distance8 + weight12 < distance12) {
                distance12 = distance8 + weight12;
                direction12 = direction8;
            }
        }

        if (canVisit15) {
            if (distance3 + weight15 < distance15) {
                distance15 = distance3 + weight15;
                direction15 = direction3;
            }

            if (distance5 + weight15 < distance15) {
                distance15 = distance5 + weight15;
                direction15 = direction5;
            }

            if (distance7 + weight15 < distance15) {
                distance15 = distance7 + weight15;
                direction15 = direction7;
            }
        }

        if (canVisit18) {
            if (distance4 + weight18 < distance18) {
                distance18 = distance4 + weight18;
                direction18 = direction4;
            }

            if (distance6 + weight18 < distance18) {
                distance18 = distance6 + weight18;
                direction18 = direction6;
            }

            if (distance8 + weight18 < distance18) {
                distance18 = distance8 + weight18;
                direction18 = direction8;
            }
        }

        if (canVisit10) {
            if (distance5 + weight10 < distance10) {
                distance10 = distance5 + weight10;
                direction10 = direction5;
            }

            if (distance9 + weight10 < distance10) {
                distance10 = distance9 + weight10;
                direction10 = direction9;
            }

            if (distance1 + weight10 < distance10) {
                distance10 = distance1 + weight10;
                direction10 = direction1;
            }
        }

        if (canVisit11) {
            if (distance6 + weight11 < distance11) {
                distance11 = distance6 + weight11;
                direction11 = direction6;
            }

            if (distance9 + weight11 < distance11) {
                distance11 = distance9 + weight11;
                direction11 = direction9;
            }

            if (distance1 + weight11 < distance11) {
                distance11 = distance1 + weight11;
                direction11 = direction1;
            }
        }

        if (canVisit13) {
            if (distance7 + weight13 < distance13) {
                distance13 = distance7 + weight13;
                direction13 = direction7;
            }

            if (distance12 + weight13 < distance13) {
                distance13 = distance12 + weight13;
                direction13 = direction12;
            }

            if (distance2 + weight13 < distance13) {
                distance13 = distance2 + weight13;
                direction13 = direction2;
            }
        }

        if (canVisit14) {
            if (distance8 + weight14 < distance14) {
                distance14 = distance8 + weight14;
                direction14 = direction8;
            }

            if (distance12 + weight14 < distance14) {
                distance14 = distance12 + weight14;
                direction14 = direction12;
            }

            if (distance2 + weight14 < distance14) {
                distance14 = distance2 + weight14;
                direction14 = direction2;
            }
        }

        if (canVisit16) {
            if (distance15 + weight16 < distance16) {
                distance16 = distance15 + weight16;
                direction16 = direction15;
            }

            if (distance5 + weight16 < distance16) {
                distance16 = distance5 + weight16;
                direction16 = direction5;
            }

            if (distance3 + weight16 < distance16) {
                distance16 = distance3 + weight16;
                direction16 = direction3;
            }
        }

        if (canVisit17) {
            if (distance15 + weight17 < distance17) {
                distance17 = distance15 + weight17;
                direction17 = direction15;
            }

            if (distance7 + weight17 < distance17) {
                distance17 = distance7 + weight17;
                direction17 = direction7;
            }

            if (distance3 + weight17 < distance17) {
                distance17 = distance3 + weight17;
                direction17 = direction3;
            }
        }

        if (canVisit19) {
            if (distance18 + weight19 < distance19) {
                distance19 = distance18 + weight19;
                direction19 = direction18;
            }

            if (distance6 + weight19 < distance19) {
                distance19 = distance6 + weight19;
                direction19 = direction6;
            }

            if (distance4 + weight19 < distance19) {
                distance19 = distance4 + weight19;
                direction19 = direction4;
            }
        }

        if (canVisit20) {
            if (distance18 + weight20 < distance20) {
                distance20 = distance18 + weight20;
                direction20 = direction18;
            }

            if (distance8 + weight20 < distance20) {
                distance20 = distance8 + weight20;
                direction20 = direction8;
            }

            if (distance4 + weight20 < distance20) {
                distance20 = distance4 + weight20;
                direction20 = direction4;
            }
        }

        if (canVisit21) {
            if (distance16 + weight21 < distance21) {
                distance21 = distance16 + weight21;
                direction21 = direction16;
            }

            if (distance10 + weight21 < distance21) {
                distance21 = distance10 + weight21;
                direction21 = direction10;
            }

            if (distance5 + weight21 < distance21) {
                distance21 = distance5 + weight21;
                direction21 = direction5;
            }
        }

        if (canVisit22) {
            if (distance19 + weight22 < distance22) {
                distance22 = distance19 + weight22;
                direction22 = direction19;
            }

            if (distance11 + weight22 < distance22) {
                distance22 = distance11 + weight22;
                direction22 = direction11;
            }

            if (distance6 + weight22 < distance22) {
                distance22 = distance6 + weight22;
                direction22 = direction6;
            }
        }

        if (canVisit23) {
            if (distance17 + weight23 < distance23) {
                distance23 = distance17 + weight23;
                direction23 = direction17;
            }

            if (distance13 + weight23 < distance23) {
                distance23 = distance13 + weight23;
                direction23 = direction13;
            }

            if (distance7 + weight23 < distance23) {
                distance23 = distance7 + weight23;
                direction23 = direction7;
            }
        }

        if (canVisit24) {
            if (distance20 + weight24 < distance24) {
                distance24 = distance20 + weight24;
                direction24 = direction20;
            }

            if (distance14 + weight24 < distance24) {
                distance24 = distance14 + weight24;
                direction24 = direction14;
            }

            if (distance8 + weight24 < distance24) {
                distance24 = distance8 + weight24;
                direction24 = direction8;
            }
        }

        if (canVisit25) {
            if (distance9 + weight25 < distance25) {
                distance25 = distance9 + weight25;
                direction25 = direction9;
            }

            if (distance10 + weight25 < distance25) {
                distance25 = distance10 + weight25;
                direction25 = direction10;
            }

            if (distance11 + weight25 < distance25) {
                distance25 = distance11 + weight25;
                direction25 = direction11;
            }
        }

        if (canVisit30) {
            if (distance12 + weight30 < distance30) {
                distance30 = distance12 + weight30;
                direction30 = direction12;
            }

            if (distance13 + weight30 < distance30) {
                distance30 = distance13 + weight30;
                direction30 = direction13;
            }

            if (distance14 + weight30 < distance30) {
                distance30 = distance14 + weight30;
                direction30 = direction14;
            }
        }

        if (canVisit35) {
            if (distance15 + weight35 < distance35) {
                distance35 = distance15 + weight35;
                direction35 = direction15;
            }

            if (distance16 + weight35 < distance35) {
                distance35 = distance16 + weight35;
                direction35 = direction16;
            }

            if (distance17 + weight35 < distance35) {
                distance35 = distance17 + weight35;
                direction35 = direction17;
            }
        }

        if (canVisit40) {
            if (distance18 + weight40 < distance40) {
                distance40 = distance18 + weight40;
                direction40 = direction18;
            }

            if (distance19 + weight40 < distance40) {
                distance40 = distance19 + weight40;
                direction40 = direction19;
            }

            if (distance20 + weight40 < distance40) {
                distance40 = distance20 + weight40;
                direction40 = direction20;
            }
        }

        if (canVisit26) {
            if (distance10 + weight26 < distance26) {
                distance26 = distance10 + weight26;
                direction26 = direction10;
            }

            if (distance25 + weight26 < distance26) {
                distance26 = distance25 + weight26;
                direction26 = direction25;
            }

            if (distance21 + weight26 < distance26) {
                distance26 = distance21 + weight26;
                direction26 = direction21;
            }

            if (distance9 + weight26 < distance26) {
                distance26 = distance9 + weight26;
                direction26 = direction9;
            }
        }

        if (canVisit27) {
            if (distance11 + weight27 < distance27) {
                distance27 = distance11 + weight27;
                direction27 = direction11;
            }

            if (distance25 + weight27 < distance27) {
                distance27 = distance25 + weight27;
                direction27 = direction25;
            }

            if (distance9 + weight27 < distance27) {
                distance27 = distance9 + weight27;
                direction27 = direction9;
            }

            if (distance22 + weight27 < distance27) {
                distance27 = distance22 + weight27;
                direction27 = direction22;
            }
        }

        if (canVisit31) {
            if (distance13 + weight31 < distance31) {
                distance31 = distance13 + weight31;
                direction31 = direction13;
            }

            if (distance30 + weight31 < distance31) {
                distance31 = distance30 + weight31;
                direction31 = direction30;
            }

            if (distance23 + weight31 < distance31) {
                distance31 = distance23 + weight31;
                direction31 = direction23;
            }

            if (distance12 + weight31 < distance31) {
                distance31 = distance12 + weight31;
                direction31 = direction12;
            }
        }

        if (canVisit32) {
            if (distance14 + weight32 < distance32) {
                distance32 = distance14 + weight32;
                direction32 = direction14;
            }

            if (distance30 + weight32 < distance32) {
                distance32 = distance30 + weight32;
                direction32 = direction30;
            }

            if (distance12 + weight32 < distance32) {
                distance32 = distance12 + weight32;
                direction32 = direction12;
            }

            if (distance24 + weight32 < distance32) {
                distance32 = distance24 + weight32;
                direction32 = direction24;
            }
        }

        if (canVisit36) {
            if (distance35 + weight36 < distance36) {
                distance36 = distance35 + weight36;
                direction36 = direction35;
            }

            if (distance16 + weight36 < distance36) {
                distance36 = distance16 + weight36;
                direction36 = direction16;
            }

            if (distance21 + weight36 < distance36) {
                distance36 = distance21 + weight36;
                direction36 = direction21;
            }

            if (distance15 + weight36 < distance36) {
                distance36 = distance15 + weight36;
                direction36 = direction15;
            }
        }

        if (canVisit37) {
            if (distance35 + weight37 < distance37) {
                distance37 = distance35 + weight37;
                direction37 = direction35;
            }

            if (distance17 + weight37 < distance37) {
                distance37 = distance17 + weight37;
                direction37 = direction17;
            }

            if (distance15 + weight37 < distance37) {
                distance37 = distance15 + weight37;
                direction37 = direction15;
            }

            if (distance23 + weight37 < distance37) {
                distance37 = distance23 + weight37;
                direction37 = direction23;
            }
        }

        if (canVisit41) {
            if (distance40 + weight41 < distance41) {
                distance41 = distance40 + weight41;
                direction41 = direction40;
            }

            if (distance19 + weight41 < distance41) {
                distance41 = distance19 + weight41;
                direction41 = direction19;
            }

            if (distance22 + weight41 < distance41) {
                distance41 = distance22 + weight41;
                direction41 = direction22;
            }

            if (distance18 + weight41 < distance41) {
                distance41 = distance18 + weight41;
                direction41 = direction18;
            }
        }

        if (canVisit42) {
            if (distance40 + weight42 < distance42) {
                distance42 = distance40 + weight42;
                direction42 = direction40;
            }

            if (distance20 + weight42 < distance42) {
                distance42 = distance20 + weight42;
                direction42 = direction20;
            }

            if (distance18 + weight42 < distance42) {
                distance42 = distance18 + weight42;
                direction42 = direction18;
            }

            if (distance24 + weight42 < distance42) {
                distance42 = distance24 + weight42;
                direction42 = direction24;
            }
        }

        if (canVisit28) {
            if (distance21 + weight28 < distance28) {
                distance28 = distance21 + weight28;
                direction28 = direction21;
            }

            if (distance26 + weight28 < distance28) {
                distance28 = distance26 + weight28;
                direction28 = direction26;
            }

            if (distance10 + weight28 < distance28) {
                distance28 = distance10 + weight28;
                direction28 = direction10;
            }
        }

        if (canVisit29) {
            if (distance22 + weight29 < distance29) {
                distance29 = distance22 + weight29;
                direction29 = direction22;
            }

            if (distance27 + weight29 < distance29) {
                distance29 = distance27 + weight29;
                direction29 = direction27;
            }

            if (distance11 + weight29 < distance29) {
                distance29 = distance11 + weight29;
                direction29 = direction11;
            }
        }

        if (canVisit33) {
            if (distance23 + weight33 < distance33) {
                distance33 = distance23 + weight33;
                direction33 = direction23;
            }

            if (distance31 + weight33 < distance33) {
                distance33 = distance31 + weight33;
                direction33 = direction31;
            }

            if (distance13 + weight33 < distance33) {
                distance33 = distance13 + weight33;
                direction33 = direction13;
            }
        }

        if (canVisit34) {
            if (distance24 + weight34 < distance34) {
                distance34 = distance24 + weight34;
                direction34 = direction24;
            }

            if (distance32 + weight34 < distance34) {
                distance34 = distance32 + weight34;
                direction34 = direction32;
            }

            if (distance14 + weight34 < distance34) {
                distance34 = distance14 + weight34;
                direction34 = direction14;
            }
        }

        if (canVisit38) {
            if (distance36 + weight38 < distance38) {
                distance38 = distance36 + weight38;
                direction38 = direction36;
            }

            if (distance21 + weight38 < distance38) {
                distance38 = distance21 + weight38;
                direction38 = direction21;
            }

            if (distance16 + weight38 < distance38) {
                distance38 = distance16 + weight38;
                direction38 = direction16;
            }
        }

        if (canVisit39) {
            if (distance37 + weight39 < distance39) {
                distance39 = distance37 + weight39;
                direction39 = direction37;
            }

            if (distance23 + weight39 < distance39) {
                distance39 = distance23 + weight39;
                direction39 = direction23;
            }

            if (distance17 + weight39 < distance39) {
                distance39 = distance17 + weight39;
                direction39 = direction17;
            }
        }

        if (canVisit43) {
            if (distance41 + weight43 < distance43) {
                distance43 = distance41 + weight43;
                direction43 = direction41;
            }

            if (distance22 + weight43 < distance43) {
                distance43 = distance22 + weight43;
                direction43 = direction22;
            }

            if (distance19 + weight43 < distance43) {
                distance43 = distance19 + weight43;
                direction43 = direction19;
            }
        }

        if (canVisit44) {
            if (distance42 + weight44 < distance44) {
                distance44 = distance42 + weight44;
                direction44 = direction42;
            }

            if (distance24 + weight44 < distance44) {
                distance44 = distance24 + weight44;
                direction44 = direction24;
            }

            if (distance20 + weight44 < distance44) {
                distance44 = distance20 + weight44;
                direction44 = direction20;
            }
        }

        if (canVisit49) {
            if (distance25 + weight49 < distance49) {
                distance49 = distance25 + weight49;
                direction49 = direction25;
            }

            if (distance26 + weight49 < distance49) {
                distance49 = distance26 + weight49;
                direction49 = direction26;
            }

            if (distance27 + weight49 < distance49) {
                distance49 = distance27 + weight49;
                direction49 = direction27;
            }
        }

        if (canVisit54) {
            if (distance30 + weight54 < distance54) {
                distance54 = distance30 + weight54;
                direction54 = direction30;
            }

            if (distance31 + weight54 < distance54) {
                distance54 = distance31 + weight54;
                direction54 = direction31;
            }

            if (distance32 + weight54 < distance54) {
                distance54 = distance32 + weight54;
                direction54 = direction32;
            }
        }

        if (canVisit59) {
            if (distance35 + weight59 < distance59) {
                distance59 = distance35 + weight59;
                direction59 = direction35;
            }

            if (distance36 + weight59 < distance59) {
                distance59 = distance36 + weight59;
                direction59 = direction36;
            }

            if (distance37 + weight59 < distance59) {
                distance59 = distance37 + weight59;
                direction59 = direction37;
            }
        }

        if (canVisit64) {
            if (distance40 + weight64 < distance64) {
                distance64 = distance40 + weight64;
                direction64 = direction40;
            }

            if (distance41 + weight64 < distance64) {
                distance64 = distance41 + weight64;
                direction64 = direction41;
            }

            if (distance42 + weight64 < distance64) {
                distance64 = distance42 + weight64;
                direction64 = direction42;
            }
        }

        if (canVisit50) {
            if (distance26 + weight50 < distance50) {
                distance50 = distance26 + weight50;
                direction50 = direction26;
            }

            if (distance49 + weight50 < distance50) {
                distance50 = distance49 + weight50;
                direction50 = direction49;
            }

            if (distance28 + weight50 < distance50) {
                distance50 = distance28 + weight50;
                direction50 = direction28;
            }

            if (distance25 + weight50 < distance50) {
                distance50 = distance25 + weight50;
                direction50 = direction25;
            }
        }

        if (canVisit51) {
            if (distance27 + weight51 < distance51) {
                distance51 = distance27 + weight51;
                direction51 = direction27;
            }

            if (distance49 + weight51 < distance51) {
                distance51 = distance49 + weight51;
                direction51 = direction49;
            }

            if (distance25 + weight51 < distance51) {
                distance51 = distance25 + weight51;
                direction51 = direction25;
            }

            if (distance29 + weight51 < distance51) {
                distance51 = distance29 + weight51;
                direction51 = direction29;
            }
        }

        if (canVisit55) {
            if (distance31 + weight55 < distance55) {
                distance55 = distance31 + weight55;
                direction55 = direction31;
            }

            if (distance54 + weight55 < distance55) {
                distance55 = distance54 + weight55;
                direction55 = direction54;
            }

            if (distance33 + weight55 < distance55) {
                distance55 = distance33 + weight55;
                direction55 = direction33;
            }

            if (distance30 + weight55 < distance55) {
                distance55 = distance30 + weight55;
                direction55 = direction30;
            }
        }

        if (canVisit56) {
            if (distance32 + weight56 < distance56) {
                distance56 = distance32 + weight56;
                direction56 = direction32;
            }

            if (distance54 + weight56 < distance56) {
                distance56 = distance54 + weight56;
                direction56 = direction54;
            }

            if (distance30 + weight56 < distance56) {
                distance56 = distance30 + weight56;
                direction56 = direction30;
            }

            if (distance34 + weight56 < distance56) {
                distance56 = distance34 + weight56;
                direction56 = direction34;
            }
        }

        if (canVisit60) {
            if (distance59 + weight60 < distance60) {
                distance60 = distance59 + weight60;
                direction60 = direction59;
            }

            if (distance36 + weight60 < distance60) {
                distance60 = distance36 + weight60;
                direction60 = direction36;
            }

            if (distance38 + weight60 < distance60) {
                distance60 = distance38 + weight60;
                direction60 = direction38;
            }

            if (distance35 + weight60 < distance60) {
                distance60 = distance35 + weight60;
                direction60 = direction35;
            }
        }

        if (canVisit61) {
            if (distance59 + weight61 < distance61) {
                distance61 = distance59 + weight61;
                direction61 = direction59;
            }

            if (distance37 + weight61 < distance61) {
                distance61 = distance37 + weight61;
                direction61 = direction37;
            }

            if (distance35 + weight61 < distance61) {
                distance61 = distance35 + weight61;
                direction61 = direction35;
            }

            if (distance39 + weight61 < distance61) {
                distance61 = distance39 + weight61;
                direction61 = direction39;
            }
        }

        if (canVisit65) {
            if (distance64 + weight65 < distance65) {
                distance65 = distance64 + weight65;
                direction65 = direction64;
            }

            if (distance41 + weight65 < distance65) {
                distance65 = distance41 + weight65;
                direction65 = direction41;
            }

            if (distance43 + weight65 < distance65) {
                distance65 = distance43 + weight65;
                direction65 = direction43;
            }

            if (distance40 + weight65 < distance65) {
                distance65 = distance40 + weight65;
                direction65 = direction40;
            }
        }

        if (canVisit66) {
            if (distance64 + weight66 < distance66) {
                distance66 = distance64 + weight66;
                direction66 = direction64;
            }

            if (distance42 + weight66 < distance66) {
                distance66 = distance42 + weight66;
                direction66 = direction42;
            }

            if (distance40 + weight66 < distance66) {
                distance66 = distance40 + weight66;
                direction66 = direction40;
            }

            if (distance44 + weight66 < distance66) {
                distance66 = distance44 + weight66;
                direction66 = direction44;
            }
        }

        if (canVisit45) {
            if (distance38 + weight45 < distance45) {
                distance45 = distance38 + weight45;
                direction45 = direction38;
            }

            if (distance28 + weight45 < distance45) {
                distance45 = distance28 + weight45;
                direction45 = direction28;
            }

            if (distance21 + weight45 < distance45) {
                distance45 = distance21 + weight45;
                direction45 = direction21;
            }
        }

        if (canVisit46) {
            if (distance43 + weight46 < distance46) {
                distance46 = distance43 + weight46;
                direction46 = direction43;
            }

            if (distance29 + weight46 < distance46) {
                distance46 = distance29 + weight46;
                direction46 = direction29;
            }

            if (distance22 + weight46 < distance46) {
                distance46 = distance22 + weight46;
                direction46 = direction22;
            }
        }

        if (canVisit47) {
            if (distance39 + weight47 < distance47) {
                distance47 = distance39 + weight47;
                direction47 = direction39;
            }

            if (distance33 + weight47 < distance47) {
                distance47 = distance33 + weight47;
                direction47 = direction33;
            }

            if (distance23 + weight47 < distance47) {
                distance47 = distance23 + weight47;
                direction47 = direction23;
            }
        }

        if (canVisit48) {
            if (distance44 + weight48 < distance48) {
                distance48 = distance44 + weight48;
                direction48 = direction44;
            }

            if (distance34 + weight48 < distance48) {
                distance48 = distance34 + weight48;
                direction48 = direction34;
            }

            if (distance24 + weight48 < distance48) {
                distance48 = distance24 + weight48;
                direction48 = direction24;
            }
        }

        if (canVisit52) {
            if (distance28 + weight52 < distance52) {
                distance52 = distance28 + weight52;
                direction52 = direction28;
            }

            if (distance50 + weight52 < distance52) {
                distance52 = distance50 + weight52;
                direction52 = direction50;
            }

            if (distance45 + weight52 < distance52) {
                distance52 = distance45 + weight52;
                direction52 = direction45;
            }

            if (distance26 + weight52 < distance52) {
                distance52 = distance26 + weight52;
                direction52 = direction26;
            }
        }

        if (canVisit53) {
            if (distance29 + weight53 < distance53) {
                distance53 = distance29 + weight53;
                direction53 = direction29;
            }

            if (distance51 + weight53 < distance53) {
                distance53 = distance51 + weight53;
                direction53 = direction51;
            }

            if (distance27 + weight53 < distance53) {
                distance53 = distance27 + weight53;
                direction53 = direction27;
            }

            if (distance46 + weight53 < distance53) {
                distance53 = distance46 + weight53;
                direction53 = direction46;
            }
        }

        if (canVisit57) {
            if (distance33 + weight57 < distance57) {
                distance57 = distance33 + weight57;
                direction57 = direction33;
            }

            if (distance55 + weight57 < distance57) {
                distance57 = distance55 + weight57;
                direction57 = direction55;
            }

            if (distance47 + weight57 < distance57) {
                distance57 = distance47 + weight57;
                direction57 = direction47;
            }

            if (distance31 + weight57 < distance57) {
                distance57 = distance31 + weight57;
                direction57 = direction31;
            }
        }

        if (canVisit58) {
            if (distance34 + weight58 < distance58) {
                distance58 = distance34 + weight58;
                direction58 = direction34;
            }

            if (distance56 + weight58 < distance58) {
                distance58 = distance56 + weight58;
                direction58 = direction56;
            }

            if (distance32 + weight58 < distance58) {
                distance58 = distance32 + weight58;
                direction58 = direction32;
            }

            if (distance48 + weight58 < distance58) {
                distance58 = distance48 + weight58;
                direction58 = direction48;
            }
        }

        if (canVisit62) {
            if (distance60 + weight62 < distance62) {
                distance62 = distance60 + weight62;
                direction62 = direction60;
            }

            if (distance38 + weight62 < distance62) {
                distance62 = distance38 + weight62;
                direction62 = direction38;
            }

            if (distance45 + weight62 < distance62) {
                distance62 = distance45 + weight62;
                direction62 = direction45;
            }

            if (distance36 + weight62 < distance62) {
                distance62 = distance36 + weight62;
                direction62 = direction36;
            }
        }

        if (canVisit63) {
            if (distance61 + weight63 < distance63) {
                distance63 = distance61 + weight63;
                direction63 = direction61;
            }

            if (distance39 + weight63 < distance63) {
                distance63 = distance39 + weight63;
                direction63 = direction39;
            }

            if (distance37 + weight63 < distance63) {
                distance63 = distance37 + weight63;
                direction63 = direction37;
            }

            if (distance47 + weight63 < distance63) {
                distance63 = distance47 + weight63;
                direction63 = direction47;
            }
        }

        if (canVisit67) {
            if (distance65 + weight67 < distance67) {
                distance67 = distance65 + weight67;
                direction67 = direction65;
            }

            if (distance43 + weight67 < distance67) {
                distance67 = distance43 + weight67;
                direction67 = direction43;
            }

            if (distance46 + weight67 < distance67) {
                distance67 = distance46 + weight67;
                direction67 = direction46;
            }

            if (distance41 + weight67 < distance67) {
                distance67 = distance41 + weight67;
                direction67 = direction41;
            }
        }

        if (canVisit68) {
            if (distance66 + weight68 < distance68) {
                distance68 = distance66 + weight68;
                direction68 = direction66;
            }

            if (distance44 + weight68 < distance68) {
                distance68 = distance44 + weight68;
                direction68 = direction44;
            }

            if (distance42 + weight68 < distance68) {
                distance68 = distance42 + weight68;
                direction68 = direction42;
            }

            if (distance48 + weight68 < distance68) {
                distance68 = distance48 + weight68;
                direction68 = direction48;
            }
        }

        if (canVisit48) {
            if (distance68 + weight48 < distance48) {
                distance48 = distance68 + weight48;
                direction48 = direction68;
            }

            if (distance58 + weight48 < distance48) {
                distance48 = distance58 + weight48;
                direction48 = direction58;
            }
        }

        if (canVisit47) {
            if (distance63 + weight47 < distance47) {
                distance47 = distance63 + weight47;
                direction47 = direction63;
            }

            if (distance57 + weight47 < distance47) {
                distance47 = distance57 + weight47;
                direction47 = direction57;
            }
        }

        if (canVisit46) {
            if (distance53 + weight46 < distance46) {
                distance46 = distance53 + weight46;
                direction46 = direction53;
            }

            if (distance67 + weight46 < distance46) {
                distance46 = distance67 + weight46;
                direction46 = direction67;
            }
        }

        if (canVisit45) {
            if (distance52 + weight45 < distance45) {
                distance45 = distance52 + weight45;
                direction45 = direction52;
            }

            if (distance62 + weight45 < distance45) {
                distance45 = distance62 + weight45;
                direction45 = direction62;
            }
        }

        if (canVisit66) {
            if (distance68 + weight66 < distance66) {
                distance66 = distance68 + weight66;
                direction66 = direction68;
            }
        }

        if (canVisit65) {
            if (distance67 + weight65 < distance65) {
                distance65 = distance67 + weight65;
                direction65 = direction67;
            }
        }

        if (canVisit61) {
            if (distance63 + weight61 < distance61) {
                distance61 = distance63 + weight61;
                direction61 = direction63;
            }
        }

        if (canVisit60) {
            if (distance62 + weight60 < distance60) {
                distance60 = distance62 + weight60;
                direction60 = direction62;
            }
        }

        if (canVisit56) {
            if (distance58 + weight56 < distance56) {
                distance56 = distance58 + weight56;
                direction56 = direction58;
            }
        }

        if (canVisit55) {
            if (distance57 + weight55 < distance55) {
                distance55 = distance57 + weight55;
                direction55 = direction57;
            }
        }

        if (canVisit51) {
            if (distance53 + weight51 < distance51) {
                distance51 = distance53 + weight51;
                direction51 = direction53;
            }
        }

        if (canVisit50) {
            if (distance52 + weight50 < distance50) {
                distance50 = distance52 + weight50;
                direction50 = direction52;
            }
        }

        if (canVisit64) {
            if (distance65 + weight64 < distance64) {
                distance64 = distance65 + weight64;
                direction64 = direction65;
            }

            if (distance66 + weight64 < distance64) {
                distance64 = distance66 + weight64;
                direction64 = direction66;
            }
        }

        if (canVisit59) {
            if (distance60 + weight59 < distance59) {
                distance59 = distance60 + weight59;
                direction59 = direction60;
            }

            if (distance61 + weight59 < distance59) {
                distance59 = distance61 + weight59;
                direction59 = direction61;
            }
        }

        if (canVisit54) {
            if (distance55 + weight54 < distance54) {
                distance54 = distance55 + weight54;
                direction54 = direction55;
            }

            if (distance56 + weight54 < distance54) {
                distance54 = distance56 + weight54;
                direction54 = direction56;
            }
        }

        if (canVisit49) {
            if (distance50 + weight49 < distance49) {
                distance49 = distance50 + weight49;
                direction49 = direction50;
            }

            if (distance51 + weight49 < distance49) {
                distance49 = distance51 + weight49;
                direction49 = direction51;
            }
        }

        if (canVisit44) {
            if (distance48 + weight44 < distance44) {
                distance44 = distance48 + weight44;
                direction44 = direction48;
            }

            if (distance68 + weight44 < distance44) {
                distance44 = distance68 + weight44;
                direction44 = direction68;
            }

            if (distance66 + weight44 < distance44) {
                distance44 = distance66 + weight44;
                direction44 = direction66;
            }

            if (distance34 + weight44 < distance44) {
                distance44 = distance34 + weight44;
                direction44 = direction34;
            }
        }

        if (canVisit43) {
            if (distance46 + weight43 < distance43) {
                distance43 = distance46 + weight43;
                direction43 = direction46;
            }

            if (distance67 + weight43 < distance43) {
                distance43 = distance67 + weight43;
                direction43 = direction67;
            }

            if (distance29 + weight43 < distance43) {
                distance43 = distance29 + weight43;
                direction43 = direction29;
            }

            if (distance65 + weight43 < distance43) {
                distance43 = distance65 + weight43;
                direction43 = direction65;
            }
        }

        if (canVisit39) {
            if (distance47 + weight39 < distance39) {
                distance39 = distance47 + weight39;
                direction39 = direction47;
            }

            if (distance63 + weight39 < distance39) {
                distance39 = distance63 + weight39;
                direction39 = direction63;
            }

            if (distance61 + weight39 < distance39) {
                distance39 = distance61 + weight39;
                direction39 = direction61;
            }

            if (distance33 + weight39 < distance39) {
                distance39 = distance33 + weight39;
                direction39 = direction33;
            }
        }

        if (canVisit38) {
            if (distance45 + weight38 < distance38) {
                distance38 = distance45 + weight38;
                direction38 = direction45;
            }

            if (distance62 + weight38 < distance38) {
                distance38 = distance62 + weight38;
                direction38 = direction62;
            }

            if (distance28 + weight38 < distance38) {
                distance38 = distance28 + weight38;
                direction38 = direction28;
            }

            if (distance60 + weight38 < distance38) {
                distance38 = distance60 + weight38;
                direction38 = direction60;
            }
        }

        if (canVisit34) {
            if (distance58 + weight34 < distance34) {
                distance34 = distance58 + weight34;
                direction34 = direction58;
            }

            if (distance48 + weight34 < distance34) {
                distance34 = distance48 + weight34;
                direction34 = direction48;
            }

            if (distance44 + weight34 < distance34) {
                distance34 = distance44 + weight34;
                direction34 = direction44;
            }

            if (distance56 + weight34 < distance34) {
                distance34 = distance56 + weight34;
                direction34 = direction56;
            }
        }

        if (canVisit33) {
            if (distance57 + weight33 < distance33) {
                distance33 = distance57 + weight33;
                direction33 = direction57;
            }

            if (distance47 + weight33 < distance33) {
                distance33 = distance47 + weight33;
                direction33 = direction47;
            }

            if (distance39 + weight33 < distance33) {
                distance33 = distance39 + weight33;
                direction33 = direction39;
            }

            if (distance55 + weight33 < distance33) {
                distance33 = distance55 + weight33;
                direction33 = direction55;
            }
        }

        if (canVisit29) {
            if (distance53 + weight29 < distance29) {
                distance29 = distance53 + weight29;
                direction29 = direction53;
            }

            if (distance46 + weight29 < distance29) {
                distance29 = distance46 + weight29;
                direction29 = direction46;
            }

            if (distance51 + weight29 < distance29) {
                distance29 = distance51 + weight29;
                direction29 = direction51;
            }

            if (distance43 + weight29 < distance29) {
                distance29 = distance43 + weight29;
                direction29 = direction43;
            }
        }

        if (canVisit28) {
            if (distance52 + weight28 < distance28) {
                distance28 = distance52 + weight28;
                direction28 = direction52;
            }

            if (distance45 + weight28 < distance28) {
                distance28 = distance45 + weight28;
                direction28 = direction45;
            }

            if (distance50 + weight28 < distance28) {
                distance28 = distance50 + weight28;
                direction28 = direction50;
            }

            if (distance38 + weight28 < distance28) {
                distance28 = distance38 + weight28;
                direction28 = direction38;
            }
        }

        if (canVisit42) {
            if (distance44 + weight42 < distance42) {
                distance42 = distance44 + weight42;
                direction42 = direction44;
            }

            if (distance66 + weight42 < distance42) {
                distance42 = distance66 + weight42;
                direction42 = direction66;
            }

            if (distance64 + weight42 < distance42) {
                distance42 = distance64 + weight42;
                direction42 = direction64;
            }

            if (distance68 + weight42 < distance42) {
                distance42 = distance68 + weight42;
                direction42 = direction68;
            }
        }

        if (canVisit41) {
            if (distance43 + weight41 < distance41) {
                distance41 = distance43 + weight41;
                direction41 = direction43;
            }

            if (distance65 + weight41 < distance41) {
                distance41 = distance65 + weight41;
                direction41 = direction65;
            }

            if (distance67 + weight41 < distance41) {
                distance41 = distance67 + weight41;
                direction41 = direction67;
            }

            if (distance64 + weight41 < distance41) {
                distance41 = distance64 + weight41;
                direction41 = direction64;
            }
        }

        if (canVisit37) {
            if (distance39 + weight37 < distance37) {
                distance37 = distance39 + weight37;
                direction37 = direction39;
            }

            if (distance61 + weight37 < distance37) {
                distance37 = distance61 + weight37;
                direction37 = direction61;
            }

            if (distance59 + weight37 < distance37) {
                distance37 = distance59 + weight37;
                direction37 = direction59;
            }

            if (distance63 + weight37 < distance37) {
                distance37 = distance63 + weight37;
                direction37 = direction63;
            }
        }

        if (canVisit36) {
            if (distance38 + weight36 < distance36) {
                distance36 = distance38 + weight36;
                direction36 = direction38;
            }

            if (distance60 + weight36 < distance36) {
                distance36 = distance60 + weight36;
                direction36 = direction60;
            }

            if (distance62 + weight36 < distance36) {
                distance36 = distance62 + weight36;
                direction36 = direction62;
            }

            if (distance59 + weight36 < distance36) {
                distance36 = distance59 + weight36;
                direction36 = direction59;
            }
        }

        if (canVisit32) {
            if (distance56 + weight32 < distance32) {
                distance32 = distance56 + weight32;
                direction32 = direction56;
            }

            if (distance34 + weight32 < distance32) {
                distance32 = distance34 + weight32;
                direction32 = direction34;
            }

            if (distance54 + weight32 < distance32) {
                distance32 = distance54 + weight32;
                direction32 = direction54;
            }

            if (distance58 + weight32 < distance32) {
                distance32 = distance58 + weight32;
                direction32 = direction58;
            }
        }

        if (canVisit31) {
            if (distance55 + weight31 < distance31) {
                distance31 = distance55 + weight31;
                direction31 = direction55;
            }

            if (distance33 + weight31 < distance31) {
                distance31 = distance33 + weight31;
                direction31 = direction33;
            }

            if (distance57 + weight31 < distance31) {
                distance31 = distance57 + weight31;
                direction31 = direction57;
            }

            if (distance54 + weight31 < distance31) {
                distance31 = distance54 + weight31;
                direction31 = direction54;
            }
        }

        if (canVisit27) {
            if (distance51 + weight27 < distance27) {
                distance27 = distance51 + weight27;
                direction27 = direction51;
            }

            if (distance29 + weight27 < distance27) {
                distance27 = distance29 + weight27;
                direction27 = direction29;
            }

            if (distance49 + weight27 < distance27) {
                distance27 = distance49 + weight27;
                direction27 = direction49;
            }

            if (distance53 + weight27 < distance27) {
                distance27 = distance53 + weight27;
                direction27 = direction53;
            }
        }

        if (canVisit26) {
            if (distance50 + weight26 < distance26) {
                distance26 = distance50 + weight26;
                direction26 = direction50;
            }

            if (distance28 + weight26 < distance26) {
                distance26 = distance28 + weight26;
                direction26 = direction28;
            }

            if (distance52 + weight26 < distance26) {
                distance26 = distance52 + weight26;
                direction26 = direction52;
            }

            if (distance49 + weight26 < distance26) {
                distance26 = distance49 + weight26;
                direction26 = direction49;
            }
        }

        if (canVisit40) {
            if (distance41 + weight40 < distance40) {
                distance40 = distance41 + weight40;
                direction40 = direction41;
            }

            if (distance42 + weight40 < distance40) {
                distance40 = distance42 + weight40;
                direction40 = direction42;
            }

            if (distance64 + weight40 < distance40) {
                distance40 = distance64 + weight40;
                direction40 = direction64;
            }

            if (distance65 + weight40 < distance40) {
                distance40 = distance65 + weight40;
                direction40 = direction65;
            }

            if (distance66 + weight40 < distance40) {
                distance40 = distance66 + weight40;
                direction40 = direction66;
            }
        }

        if (canVisit35) {
            if (distance36 + weight35 < distance35) {
                distance35 = distance36 + weight35;
                direction35 = direction36;
            }

            if (distance37 + weight35 < distance35) {
                distance35 = distance37 + weight35;
                direction35 = direction37;
            }

            if (distance59 + weight35 < distance35) {
                distance35 = distance59 + weight35;
                direction35 = direction59;
            }

            if (distance60 + weight35 < distance35) {
                distance35 = distance60 + weight35;
                direction35 = direction60;
            }

            if (distance61 + weight35 < distance35) {
                distance35 = distance61 + weight35;
                direction35 = direction61;
            }
        }

        if (canVisit30) {
            if (distance54 + weight30 < distance30) {
                distance30 = distance54 + weight30;
                direction30 = direction54;
            }

            if (distance31 + weight30 < distance30) {
                distance30 = distance31 + weight30;
                direction30 = direction31;
            }

            if (distance32 + weight30 < distance30) {
                distance30 = distance32 + weight30;
                direction30 = direction32;
            }

            if (distance55 + weight30 < distance30) {
                distance30 = distance55 + weight30;
                direction30 = direction55;
            }

            if (distance56 + weight30 < distance30) {
                distance30 = distance56 + weight30;
                direction30 = direction56;
            }
        }

        if (canVisit25) {
            if (distance49 + weight25 < distance25) {
                distance25 = distance49 + weight25;
                direction25 = direction49;
            }

            if (distance26 + weight25 < distance25) {
                distance25 = distance26 + weight25;
                direction25 = direction26;
            }

            if (distance27 + weight25 < distance25) {
                distance25 = distance27 + weight25;
                direction25 = direction27;
            }

            if (distance50 + weight25 < distance25) {
                distance25 = distance50 + weight25;
                direction25 = direction50;
            }

            if (distance51 + weight25 < distance25) {
                distance25 = distance51 + weight25;
                direction25 = direction51;
            }
        }

        if (canVisit24) {
            if (distance34 + weight24 < distance24) {
                distance24 = distance34 + weight24;
                direction24 = direction34;
            }

            if (distance44 + weight24 < distance24) {
                distance24 = distance44 + weight24;
                direction24 = direction44;
            }

            if (distance42 + weight24 < distance24) {
                distance24 = distance42 + weight24;
                direction24 = direction42;
            }

            if (distance32 + weight24 < distance24) {
                distance24 = distance32 + weight24;
                direction24 = direction32;
            }

            if (distance48 + weight24 < distance24) {
                distance24 = distance48 + weight24;
                direction24 = direction48;
            }
        }

        if (canVisit23) {
            if (distance33 + weight23 < distance23) {
                distance23 = distance33 + weight23;
                direction23 = direction33;
            }

            if (distance39 + weight23 < distance23) {
                distance23 = distance39 + weight23;
                direction23 = direction39;
            }

            if (distance37 + weight23 < distance23) {
                distance23 = distance37 + weight23;
                direction23 = direction37;
            }

            if (distance47 + weight23 < distance23) {
                distance23 = distance47 + weight23;
                direction23 = direction47;
            }

            if (distance31 + weight23 < distance23) {
                distance23 = distance31 + weight23;
                direction23 = direction31;
            }
        }

        if (canVisit22) {
            if (distance29 + weight22 < distance22) {
                distance22 = distance29 + weight22;
                direction22 = direction29;
            }

            if (distance43 + weight22 < distance22) {
                distance22 = distance43 + weight22;
                direction22 = direction43;
            }

            if (distance27 + weight22 < distance22) {
                distance22 = distance27 + weight22;
                direction22 = direction27;
            }

            if (distance46 + weight22 < distance22) {
                distance22 = distance46 + weight22;
                direction22 = direction46;
            }

            if (distance41 + weight22 < distance22) {
                distance22 = distance41 + weight22;
                direction22 = direction41;
            }
        }

        if (canVisit21) {
            if (distance28 + weight21 < distance21) {
                distance21 = distance28 + weight21;
                direction21 = direction28;
            }

            if (distance38 + weight21 < distance21) {
                distance21 = distance38 + weight21;
                direction21 = direction38;
            }

            if (distance45 + weight21 < distance21) {
                distance21 = distance45 + weight21;
                direction21 = direction45;
            }

            if (distance26 + weight21 < distance21) {
                distance21 = distance26 + weight21;
                direction21 = direction26;
            }

            if (distance36 + weight21 < distance21) {
                distance21 = distance36 + weight21;
                direction21 = direction36;
            }
        }

        if (canVisit20) {
            if (distance24 + weight20 < distance20) {
                distance20 = distance24 + weight20;
                direction20 = direction24;
            }

            if (distance42 + weight20 < distance20) {
                distance20 = distance42 + weight20;
                direction20 = direction42;
            }

            if (distance40 + weight20 < distance20) {
                distance20 = distance40 + weight20;
                direction20 = direction40;
            }

            if (distance14 + weight20 < distance20) {
                distance20 = distance14 + weight20;
                direction20 = direction14;
            }

            if (distance44 + weight20 < distance20) {
                distance20 = distance44 + weight20;
                direction20 = direction44;
            }
        }

        if (canVisit19) {
            if (distance22 + weight19 < distance19) {
                distance19 = distance22 + weight19;
                direction19 = direction22;
            }

            if (distance41 + weight19 < distance19) {
                distance19 = distance41 + weight19;
                direction19 = direction41;
            }

            if (distance11 + weight19 < distance19) {
                distance19 = distance11 + weight19;
                direction19 = direction11;
            }

            if (distance43 + weight19 < distance19) {
                distance19 = distance43 + weight19;
                direction19 = direction43;
            }

            if (distance40 + weight19 < distance19) {
                distance19 = distance40 + weight19;
                direction19 = direction40;
            }
        }

        if (canVisit17) {
            if (distance23 + weight17 < distance17) {
                distance17 = distance23 + weight17;
                direction17 = direction23;
            }

            if (distance37 + weight17 < distance17) {
                distance17 = distance37 + weight17;
                direction17 = direction37;
            }

            if (distance35 + weight17 < distance17) {
                distance17 = distance35 + weight17;
                direction17 = direction35;
            }

            if (distance39 + weight17 < distance17) {
                distance17 = distance39 + weight17;
                direction17 = direction39;
            }

            if (distance13 + weight17 < distance17) {
                distance17 = distance13 + weight17;
                direction17 = direction13;
            }
        }

        if (canVisit16) {
            if (distance21 + weight16 < distance16) {
                distance16 = distance21 + weight16;
                direction16 = direction21;
            }

            if (distance36 + weight16 < distance16) {
                distance16 = distance36 + weight16;
                direction16 = direction36;
            }

            if (distance38 + weight16 < distance16) {
                distance16 = distance38 + weight16;
                direction16 = direction38;
            }

            if (distance10 + weight16 < distance16) {
                distance16 = distance10 + weight16;
                direction16 = direction10;
            }

            if (distance35 + weight16 < distance16) {
                distance16 = distance35 + weight16;
                direction16 = direction35;
            }
        }

        if (canVisit14) {
            if (distance32 + weight14 < distance14) {
                distance14 = distance32 + weight14;
                direction14 = direction32;
            }

            if (distance24 + weight14 < distance14) {
                distance14 = distance24 + weight14;
                direction14 = direction24;
            }

            if (distance20 + weight14 < distance14) {
                distance14 = distance20 + weight14;
                direction14 = direction20;
            }

            if (distance30 + weight14 < distance14) {
                distance14 = distance30 + weight14;
                direction14 = direction30;
            }

            if (distance34 + weight14 < distance14) {
                distance14 = distance34 + weight14;
                direction14 = direction34;
            }
        }

        if (canVisit13) {
            if (distance31 + weight13 < distance13) {
                distance13 = distance31 + weight13;
                direction13 = direction31;
            }

            if (distance23 + weight13 < distance13) {
                distance13 = distance23 + weight13;
                direction13 = direction23;
            }

            if (distance17 + weight13 < distance13) {
                distance13 = distance17 + weight13;
                direction13 = direction17;
            }

            if (distance33 + weight13 < distance13) {
                distance13 = distance33 + weight13;
                direction13 = direction33;
            }

            if (distance30 + weight13 < distance13) {
                distance13 = distance30 + weight13;
                direction13 = direction30;
            }
        }

        if (canVisit11) {
            if (distance27 + weight11 < distance11) {
                distance11 = distance27 + weight11;
                direction11 = direction27;
            }

            if (distance22 + weight11 < distance11) {
                distance11 = distance22 + weight11;
                direction11 = direction22;
            }

            if (distance25 + weight11 < distance11) {
                distance11 = distance25 + weight11;
                direction11 = direction25;
            }

            if (distance29 + weight11 < distance11) {
                distance11 = distance29 + weight11;
                direction11 = direction29;
            }

            if (distance19 + weight11 < distance11) {
                distance11 = distance19 + weight11;
                direction11 = direction19;
            }
        }

        if (canVisit10) {
            if (distance26 + weight10 < distance10) {
                distance10 = distance26 + weight10;
                direction10 = direction26;
            }

            if (distance21 + weight10 < distance10) {
                distance10 = distance21 + weight10;
                direction10 = direction21;
            }

            if (distance28 + weight10 < distance10) {
                distance10 = distance28 + weight10;
                direction10 = direction28;
            }

            if (distance25 + weight10 < distance10) {
                distance10 = distance25 + weight10;
                direction10 = direction25;
            }

            if (distance16 + weight10 < distance10) {
                distance10 = distance16 + weight10;
                direction10 = direction16;
            }
        }

        if (canVisit18) {
            if (distance19 + weight18 < distance18) {
                distance18 = distance19 + weight18;
                direction18 = direction19;
            }

            if (distance20 + weight18 < distance18) {
                distance18 = distance20 + weight18;
                direction18 = direction20;
            }

            if (distance40 + weight18 < distance18) {
                distance18 = distance40 + weight18;
                direction18 = direction40;
            }

            if (distance41 + weight18 < distance18) {
                distance18 = distance41 + weight18;
                direction18 = direction41;
            }

            if (distance42 + weight18 < distance18) {
                distance18 = distance42 + weight18;
                direction18 = direction42;
            }
        }

        if (canVisit15) {
            if (distance16 + weight15 < distance15) {
                distance15 = distance16 + weight15;
                direction15 = direction16;
            }

            if (distance17 + weight15 < distance15) {
                distance15 = distance17 + weight15;
                direction15 = direction17;
            }

            if (distance35 + weight15 < distance15) {
                distance15 = distance35 + weight15;
                direction15 = direction35;
            }

            if (distance36 + weight15 < distance15) {
                distance15 = distance36 + weight15;
                direction15 = direction36;
            }

            if (distance37 + weight15 < distance15) {
                distance15 = distance37 + weight15;
                direction15 = direction37;
            }
        }

        if (canVisit12) {
            if (distance30 + weight12 < distance12) {
                distance12 = distance30 + weight12;
                direction12 = direction30;
            }

            if (distance13 + weight12 < distance12) {
                distance12 = distance13 + weight12;
                direction12 = direction13;
            }

            if (distance14 + weight12 < distance12) {
                distance12 = distance14 + weight12;
                direction12 = direction14;
            }

            if (distance31 + weight12 < distance12) {
                distance12 = distance31 + weight12;
                direction12 = direction31;
            }

            if (distance32 + weight12 < distance12) {
                distance12 = distance32 + weight12;
                direction12 = direction32;
            }
        }

        if (canVisit9) {
            if (distance25 + weight9 < distance9) {
                distance9 = distance25 + weight9;
                direction9 = direction25;
            }

            if (distance10 + weight9 < distance9) {
                distance9 = distance10 + weight9;
                direction9 = direction10;
            }

            if (distance11 + weight9 < distance9) {
                distance9 = distance11 + weight9;
                direction9 = direction11;
            }

            if (distance26 + weight9 < distance9) {
                distance9 = distance26 + weight9;
                direction9 = direction26;
            }

            if (distance27 + weight9 < distance9) {
                distance9 = distance27 + weight9;
                direction9 = direction27;
            }
        }

        if (canVisit8) {
            if (distance14 + weight8 < distance8) {
                distance8 = distance14 + weight8;
                direction8 = direction14;
            }

            if (distance20 + weight8 < distance8) {
                distance8 = distance20 + weight8;
                direction8 = direction20;
            }

            if (distance18 + weight8 < distance8) {
                distance8 = distance18 + weight8;
                direction8 = direction18;
            }

            if (distance12 + weight8 < distance8) {
                distance8 = distance12 + weight8;
                direction8 = direction12;
            }

            if (distance24 + weight8 < distance8) {
                distance8 = distance24 + weight8;
                direction8 = direction24;
            }
        }

        if (canVisit7) {
            if (distance13 + weight7 < distance7) {
                distance7 = distance13 + weight7;
                direction7 = direction13;
            }

            if (distance17 + weight7 < distance7) {
                distance7 = distance17 + weight7;
                direction7 = direction17;
            }

            if (distance15 + weight7 < distance7) {
                distance7 = distance15 + weight7;
                direction7 = direction15;
            }

            if (distance23 + weight7 < distance7) {
                distance7 = distance23 + weight7;
                direction7 = direction23;
            }

            if (distance12 + weight7 < distance7) {
                distance7 = distance12 + weight7;
                direction7 = direction12;
            }
        }

        if (canVisit6) {
            if (distance11 + weight6 < distance6) {
                distance6 = distance11 + weight6;
                direction6 = direction11;
            }

            if (distance19 + weight6 < distance6) {
                distance6 = distance19 + weight6;
                direction6 = direction19;
            }

            if (distance9 + weight6 < distance6) {
                distance6 = distance9 + weight6;
                direction6 = direction9;
            }

            if (distance22 + weight6 < distance6) {
                distance6 = distance22 + weight6;
                direction6 = direction22;
            }

            if (distance18 + weight6 < distance6) {
                distance6 = distance18 + weight6;
                direction6 = direction18;
            }
        }

        if (canVisit5) {
            if (distance10 + weight5 < distance5) {
                distance5 = distance10 + weight5;
                direction5 = direction10;
            }

            if (distance16 + weight5 < distance5) {
                distance5 = distance16 + weight5;
                direction5 = direction16;
            }

            if (distance21 + weight5 < distance5) {
                distance5 = distance21 + weight5;
                direction5 = direction21;
            }

            if (distance9 + weight5 < distance5) {
                distance5 = distance9 + weight5;
                direction5 = direction9;
            }

            if (distance15 + weight5 < distance5) {
                distance5 = distance15 + weight5;
                direction5 = direction15;
            }
        }

        if (canVisit4) {
            if (distance6 + weight4 < distance4) {
                distance4 = distance6 + weight4;
                direction4 = direction6;
            }

            if (distance8 + weight4 < distance4) {
                distance4 = distance8 + weight4;
                direction4 = direction8;
            }

            if (distance18 + weight4 < distance4) {
                distance4 = distance18 + weight4;
                direction4 = direction18;
            }

            if (distance1 + weight4 < distance4) {
                distance4 = distance1 + weight4;
                direction4 = direction1;
            }

            if (distance19 + weight4 < distance4) {
                distance4 = distance19 + weight4;
                direction4 = direction19;
            }

            if (distance2 + weight4 < distance4) {
                distance4 = distance2 + weight4;
                direction4 = direction2;
            }

            if (distance20 + weight4 < distance4) {
                distance4 = distance20 + weight4;
                direction4 = direction20;
            }
        }

        if (canVisit3) {
            if (distance5 + weight3 < distance3) {
                distance3 = distance5 + weight3;
                direction3 = direction5;
            }

            if (distance7 + weight3 < distance3) {
                distance3 = distance7 + weight3;
                direction3 = direction7;
            }

            if (distance15 + weight3 < distance3) {
                distance3 = distance15 + weight3;
                direction3 = direction15;
            }

            if (distance16 + weight3 < distance3) {
                distance3 = distance16 + weight3;
                direction3 = direction16;
            }

            if (distance1 + weight3 < distance3) {
                distance3 = distance1 + weight3;
                direction3 = direction1;
            }

            if (distance17 + weight3 < distance3) {
                distance3 = distance17 + weight3;
                direction3 = direction17;
            }

            if (distance2 + weight3 < distance3) {
                distance3 = distance2 + weight3;
                direction3 = direction2;
            }
        }

        if (canVisit2) {
            if (distance12 + weight2 < distance2) {
                distance2 = distance12 + weight2;
                direction2 = direction12;
            }

            if (distance7 + weight2 < distance2) {
                distance2 = distance7 + weight2;
                direction2 = direction7;
            }

            if (distance8 + weight2 < distance2) {
                distance2 = distance8 + weight2;
                direction2 = direction8;
            }

            if (distance3 + weight2 < distance2) {
                distance2 = distance3 + weight2;
                direction2 = direction3;
            }

            if (distance4 + weight2 < distance2) {
                distance2 = distance4 + weight2;
                direction2 = direction4;
            }

            if (distance13 + weight2 < distance2) {
                distance2 = distance13 + weight2;
                direction2 = direction13;
            }

            if (distance14 + weight2 < distance2) {
                distance2 = distance14 + weight2;
                direction2 = direction14;
            }
        }

        if (canVisit1) {
            if (distance9 + weight1 < distance1) {
                distance1 = distance9 + weight1;
                direction1 = direction9;
            }

            if (distance5 + weight1 < distance1) {
                distance1 = distance5 + weight1;
                direction1 = direction5;
            }

            if (distance6 + weight1 < distance1) {
                distance1 = distance6 + weight1;
                direction1 = direction6;
            }

            if (distance10 + weight1 < distance1) {
                distance1 = distance10 + weight1;
                direction1 = direction10;
            }

            if (distance11 + weight1 < distance1) {
                distance1 = distance11 + weight1;
                direction1 = direction11;
            }

            if (distance3 + weight1 < distance1) {
                distance1 = distance3 + weight1;
                direction1 = direction3;
            }

            if (distance4 + weight1 < distance1) {
                distance1 = distance4 + weight1;
                direction1 = direction4;
            }
        }

        if (canVisit5) {
            if (distance3 + weight5 < distance5) {
                distance5 = distance3 + weight5;
                direction5 = direction3;
            }

            if (distance1 + weight5 < distance5) {
                distance5 = distance1 + weight5;
                direction5 = direction1;
            }
        }

        if (canVisit6) {
            if (distance4 + weight6 < distance6) {
                distance6 = distance4 + weight6;
                direction6 = direction4;
            }

            if (distance1 + weight6 < distance6) {
                distance6 = distance1 + weight6;
                direction6 = direction1;
            }
        }

        if (canVisit7) {
            if (distance3 + weight7 < distance7) {
                distance7 = distance3 + weight7;
                direction7 = direction3;
            }

            if (distance2 + weight7 < distance7) {
                distance7 = distance2 + weight7;
                direction7 = direction2;
            }
        }

        if (canVisit8) {
            if (distance4 + weight8 < distance8) {
                distance8 = distance4 + weight8;
                direction8 = direction4;
            }

            if (distance2 + weight8 < distance8) {
                distance8 = distance2 + weight8;
                direction8 = direction2;
            }
        }

        if (canVisit9) {
            if (distance1 + weight9 < distance9) {
                distance9 = distance1 + weight9;
                direction9 = direction1;
            }

            if (distance5 + weight9 < distance9) {
                distance9 = distance5 + weight9;
                direction9 = direction5;
            }

            if (distance6 + weight9 < distance9) {
                distance9 = distance6 + weight9;
                direction9 = direction6;
            }
        }

        if (canVisit12) {
            if (distance2 + weight12 < distance12) {
                distance12 = distance2 + weight12;
                direction12 = direction2;
            }

            if (distance7 + weight12 < distance12) {
                distance12 = distance7 + weight12;
                direction12 = direction7;
            }

            if (distance8 + weight12 < distance12) {
                distance12 = distance8 + weight12;
                direction12 = direction8;
            }
        }

        if (canVisit15) {
            if (distance3 + weight15 < distance15) {
                distance15 = distance3 + weight15;
                direction15 = direction3;
            }

            if (distance5 + weight15 < distance15) {
                distance15 = distance5 + weight15;
                direction15 = direction5;
            }

            if (distance7 + weight15 < distance15) {
                distance15 = distance7 + weight15;
                direction15 = direction7;
            }
        }

        if (canVisit18) {
            if (distance4 + weight18 < distance18) {
                distance18 = distance4 + weight18;
                direction18 = direction4;
            }

            if (distance6 + weight18 < distance18) {
                distance18 = distance6 + weight18;
                direction18 = direction6;
            }

            if (distance8 + weight18 < distance18) {
                distance18 = distance8 + weight18;
                direction18 = direction8;
            }
        }

        if (canVisit10) {
            if (distance5 + weight10 < distance10) {
                distance10 = distance5 + weight10;
                direction10 = direction5;
            }

            if (distance9 + weight10 < distance10) {
                distance10 = distance9 + weight10;
                direction10 = direction9;
            }

            if (distance1 + weight10 < distance10) {
                distance10 = distance1 + weight10;
                direction10 = direction1;
            }
        }

        if (canVisit11) {
            if (distance6 + weight11 < distance11) {
                distance11 = distance6 + weight11;
                direction11 = direction6;
            }

            if (distance9 + weight11 < distance11) {
                distance11 = distance9 + weight11;
                direction11 = direction9;
            }

            if (distance1 + weight11 < distance11) {
                distance11 = distance1 + weight11;
                direction11 = direction1;
            }
        }

        if (canVisit13) {
            if (distance7 + weight13 < distance13) {
                distance13 = distance7 + weight13;
                direction13 = direction7;
            }

            if (distance12 + weight13 < distance13) {
                distance13 = distance12 + weight13;
                direction13 = direction12;
            }

            if (distance2 + weight13 < distance13) {
                distance13 = distance2 + weight13;
                direction13 = direction2;
            }
        }

        if (canVisit14) {
            if (distance8 + weight14 < distance14) {
                distance14 = distance8 + weight14;
                direction14 = direction8;
            }

            if (distance12 + weight14 < distance14) {
                distance14 = distance12 + weight14;
                direction14 = direction12;
            }

            if (distance2 + weight14 < distance14) {
                distance14 = distance2 + weight14;
                direction14 = direction2;
            }
        }

        if (canVisit16) {
            if (distance15 + weight16 < distance16) {
                distance16 = distance15 + weight16;
                direction16 = direction15;
            }

            if (distance5 + weight16 < distance16) {
                distance16 = distance5 + weight16;
                direction16 = direction5;
            }

            if (distance3 + weight16 < distance16) {
                distance16 = distance3 + weight16;
                direction16 = direction3;
            }
        }

        if (canVisit17) {
            if (distance15 + weight17 < distance17) {
                distance17 = distance15 + weight17;
                direction17 = direction15;
            }

            if (distance7 + weight17 < distance17) {
                distance17 = distance7 + weight17;
                direction17 = direction7;
            }

            if (distance3 + weight17 < distance17) {
                distance17 = distance3 + weight17;
                direction17 = direction3;
            }
        }

        if (canVisit19) {
            if (distance18 + weight19 < distance19) {
                distance19 = distance18 + weight19;
                direction19 = direction18;
            }

            if (distance6 + weight19 < distance19) {
                distance19 = distance6 + weight19;
                direction19 = direction6;
            }

            if (distance4 + weight19 < distance19) {
                distance19 = distance4 + weight19;
                direction19 = direction4;
            }
        }

        if (canVisit20) {
            if (distance18 + weight20 < distance20) {
                distance20 = distance18 + weight20;
                direction20 = direction18;
            }

            if (distance8 + weight20 < distance20) {
                distance20 = distance8 + weight20;
                direction20 = direction8;
            }

            if (distance4 + weight20 < distance20) {
                distance20 = distance4 + weight20;
                direction20 = direction4;
            }
        }

        if (canVisit21) {
            if (distance16 + weight21 < distance21) {
                distance21 = distance16 + weight21;
                direction21 = direction16;
            }

            if (distance10 + weight21 < distance21) {
                distance21 = distance10 + weight21;
                direction21 = direction10;
            }

            if (distance5 + weight21 < distance21) {
                distance21 = distance5 + weight21;
                direction21 = direction5;
            }
        }

        if (canVisit22) {
            if (distance19 + weight22 < distance22) {
                distance22 = distance19 + weight22;
                direction22 = direction19;
            }

            if (distance11 + weight22 < distance22) {
                distance22 = distance11 + weight22;
                direction22 = direction11;
            }

            if (distance6 + weight22 < distance22) {
                distance22 = distance6 + weight22;
                direction22 = direction6;
            }
        }

        if (canVisit23) {
            if (distance17 + weight23 < distance23) {
                distance23 = distance17 + weight23;
                direction23 = direction17;
            }

            if (distance13 + weight23 < distance23) {
                distance23 = distance13 + weight23;
                direction23 = direction13;
            }

            if (distance7 + weight23 < distance23) {
                distance23 = distance7 + weight23;
                direction23 = direction7;
            }
        }

        if (canVisit24) {
            if (distance20 + weight24 < distance24) {
                distance24 = distance20 + weight24;
                direction24 = direction20;
            }

            if (distance14 + weight24 < distance24) {
                distance24 = distance14 + weight24;
                direction24 = direction14;
            }

            if (distance8 + weight24 < distance24) {
                distance24 = distance8 + weight24;
                direction24 = direction8;
            }
        }

        if (canVisit25) {
            if (distance9 + weight25 < distance25) {
                distance25 = distance9 + weight25;
                direction25 = direction9;
            }

            if (distance10 + weight25 < distance25) {
                distance25 = distance10 + weight25;
                direction25 = direction10;
            }

            if (distance11 + weight25 < distance25) {
                distance25 = distance11 + weight25;
                direction25 = direction11;
            }
        }

        if (canVisit30) {
            if (distance12 + weight30 < distance30) {
                distance30 = distance12 + weight30;
                direction30 = direction12;
            }

            if (distance13 + weight30 < distance30) {
                distance30 = distance13 + weight30;
                direction30 = direction13;
            }

            if (distance14 + weight30 < distance30) {
                distance30 = distance14 + weight30;
                direction30 = direction14;
            }
        }

        if (canVisit35) {
            if (distance15 + weight35 < distance35) {
                distance35 = distance15 + weight35;
                direction35 = direction15;
            }

            if (distance16 + weight35 < distance35) {
                distance35 = distance16 + weight35;
                direction35 = direction16;
            }

            if (distance17 + weight35 < distance35) {
                distance35 = distance17 + weight35;
                direction35 = direction17;
            }
        }

        if (canVisit40) {
            if (distance18 + weight40 < distance40) {
                distance40 = distance18 + weight40;
                direction40 = direction18;
            }

            if (distance19 + weight40 < distance40) {
                distance40 = distance19 + weight40;
                direction40 = direction19;
            }

            if (distance20 + weight40 < distance40) {
                distance40 = distance20 + weight40;
                direction40 = direction20;
            }
        }

        if (canVisit26) {
            if (distance10 + weight26 < distance26) {
                distance26 = distance10 + weight26;
                direction26 = direction10;
            }

            if (distance25 + weight26 < distance26) {
                distance26 = distance25 + weight26;
                direction26 = direction25;
            }

            if (distance21 + weight26 < distance26) {
                distance26 = distance21 + weight26;
                direction26 = direction21;
            }

            if (distance9 + weight26 < distance26) {
                distance26 = distance9 + weight26;
                direction26 = direction9;
            }
        }

        if (canVisit27) {
            if (distance11 + weight27 < distance27) {
                distance27 = distance11 + weight27;
                direction27 = direction11;
            }

            if (distance25 + weight27 < distance27) {
                distance27 = distance25 + weight27;
                direction27 = direction25;
            }

            if (distance9 + weight27 < distance27) {
                distance27 = distance9 + weight27;
                direction27 = direction9;
            }

            if (distance22 + weight27 < distance27) {
                distance27 = distance22 + weight27;
                direction27 = direction22;
            }
        }

        if (canVisit31) {
            if (distance13 + weight31 < distance31) {
                distance31 = distance13 + weight31;
                direction31 = direction13;
            }

            if (distance30 + weight31 < distance31) {
                distance31 = distance30 + weight31;
                direction31 = direction30;
            }

            if (distance23 + weight31 < distance31) {
                distance31 = distance23 + weight31;
                direction31 = direction23;
            }

            if (distance12 + weight31 < distance31) {
                distance31 = distance12 + weight31;
                direction31 = direction12;
            }
        }

        if (canVisit32) {
            if (distance14 + weight32 < distance32) {
                distance32 = distance14 + weight32;
                direction32 = direction14;
            }

            if (distance30 + weight32 < distance32) {
                distance32 = distance30 + weight32;
                direction32 = direction30;
            }

            if (distance12 + weight32 < distance32) {
                distance32 = distance12 + weight32;
                direction32 = direction12;
            }

            if (distance24 + weight32 < distance32) {
                distance32 = distance24 + weight32;
                direction32 = direction24;
            }
        }

        if (canVisit36) {
            if (distance35 + weight36 < distance36) {
                distance36 = distance35 + weight36;
                direction36 = direction35;
            }

            if (distance16 + weight36 < distance36) {
                distance36 = distance16 + weight36;
                direction36 = direction16;
            }

            if (distance21 + weight36 < distance36) {
                distance36 = distance21 + weight36;
                direction36 = direction21;
            }

            if (distance15 + weight36 < distance36) {
                distance36 = distance15 + weight36;
                direction36 = direction15;
            }
        }

        if (canVisit37) {
            if (distance35 + weight37 < distance37) {
                distance37 = distance35 + weight37;
                direction37 = direction35;
            }

            if (distance17 + weight37 < distance37) {
                distance37 = distance17 + weight37;
                direction37 = direction17;
            }

            if (distance15 + weight37 < distance37) {
                distance37 = distance15 + weight37;
                direction37 = direction15;
            }

            if (distance23 + weight37 < distance37) {
                distance37 = distance23 + weight37;
                direction37 = direction23;
            }
        }

        if (canVisit41) {
            if (distance40 + weight41 < distance41) {
                distance41 = distance40 + weight41;
                direction41 = direction40;
            }

            if (distance19 + weight41 < distance41) {
                distance41 = distance19 + weight41;
                direction41 = direction19;
            }

            if (distance22 + weight41 < distance41) {
                distance41 = distance22 + weight41;
                direction41 = direction22;
            }

            if (distance18 + weight41 < distance41) {
                distance41 = distance18 + weight41;
                direction41 = direction18;
            }
        }

        if (canVisit42) {
            if (distance40 + weight42 < distance42) {
                distance42 = distance40 + weight42;
                direction42 = direction40;
            }

            if (distance20 + weight42 < distance42) {
                distance42 = distance20 + weight42;
                direction42 = direction20;
            }

            if (distance18 + weight42 < distance42) {
                distance42 = distance18 + weight42;
                direction42 = direction18;
            }

            if (distance24 + weight42 < distance42) {
                distance42 = distance24 + weight42;
                direction42 = direction24;
            }
        }

        if (canVisit28) {
            if (distance21 + weight28 < distance28) {
                distance28 = distance21 + weight28;
                direction28 = direction21;
            }

            if (distance26 + weight28 < distance28) {
                distance28 = distance26 + weight28;
                direction28 = direction26;
            }

            if (distance10 + weight28 < distance28) {
                distance28 = distance10 + weight28;
                direction28 = direction10;
            }
        }

        if (canVisit29) {
            if (distance22 + weight29 < distance29) {
                distance29 = distance22 + weight29;
                direction29 = direction22;
            }

            if (distance27 + weight29 < distance29) {
                distance29 = distance27 + weight29;
                direction29 = direction27;
            }

            if (distance11 + weight29 < distance29) {
                distance29 = distance11 + weight29;
                direction29 = direction11;
            }
        }

        if (canVisit33) {
            if (distance23 + weight33 < distance33) {
                distance33 = distance23 + weight33;
                direction33 = direction23;
            }

            if (distance31 + weight33 < distance33) {
                distance33 = distance31 + weight33;
                direction33 = direction31;
            }

            if (distance13 + weight33 < distance33) {
                distance33 = distance13 + weight33;
                direction33 = direction13;
            }
        }

        if (canVisit34) {
            if (distance24 + weight34 < distance34) {
                distance34 = distance24 + weight34;
                direction34 = direction24;
            }

            if (distance32 + weight34 < distance34) {
                distance34 = distance32 + weight34;
                direction34 = direction32;
            }

            if (distance14 + weight34 < distance34) {
                distance34 = distance14 + weight34;
                direction34 = direction14;
            }
        }

        if (canVisit38) {
            if (distance36 + weight38 < distance38) {
                distance38 = distance36 + weight38;
                direction38 = direction36;
            }

            if (distance21 + weight38 < distance38) {
                distance38 = distance21 + weight38;
                direction38 = direction21;
            }

            if (distance16 + weight38 < distance38) {
                distance38 = distance16 + weight38;
                direction38 = direction16;
            }
        }

        if (canVisit39) {
            if (distance37 + weight39 < distance39) {
                distance39 = distance37 + weight39;
                direction39 = direction37;
            }

            if (distance23 + weight39 < distance39) {
                distance39 = distance23 + weight39;
                direction39 = direction23;
            }

            if (distance17 + weight39 < distance39) {
                distance39 = distance17 + weight39;
                direction39 = direction17;
            }
        }

        if (canVisit43) {
            if (distance41 + weight43 < distance43) {
                distance43 = distance41 + weight43;
                direction43 = direction41;
            }

            if (distance22 + weight43 < distance43) {
                distance43 = distance22 + weight43;
                direction43 = direction22;
            }

            if (distance19 + weight43 < distance43) {
                distance43 = distance19 + weight43;
                direction43 = direction19;
            }
        }

        if (canVisit44) {
            if (distance42 + weight44 < distance44) {
                distance44 = distance42 + weight44;
                direction44 = direction42;
            }

            if (distance24 + weight44 < distance44) {
                distance44 = distance24 + weight44;
                direction44 = direction24;
            }

            if (distance20 + weight44 < distance44) {
                distance44 = distance20 + weight44;
                direction44 = direction20;
            }
        }

        if (canVisit49) {
            if (distance25 + weight49 < distance49) {
                distance49 = distance25 + weight49;
                direction49 = direction25;
            }

            if (distance26 + weight49 < distance49) {
                distance49 = distance26 + weight49;
                direction49 = direction26;
            }

            if (distance27 + weight49 < distance49) {
                distance49 = distance27 + weight49;
                direction49 = direction27;
            }
        }

        if (canVisit54) {
            if (distance30 + weight54 < distance54) {
                distance54 = distance30 + weight54;
                direction54 = direction30;
            }

            if (distance31 + weight54 < distance54) {
                distance54 = distance31 + weight54;
                direction54 = direction31;
            }

            if (distance32 + weight54 < distance54) {
                distance54 = distance32 + weight54;
                direction54 = direction32;
            }
        }

        if (canVisit59) {
            if (distance35 + weight59 < distance59) {
                distance59 = distance35 + weight59;
                direction59 = direction35;
            }

            if (distance36 + weight59 < distance59) {
                distance59 = distance36 + weight59;
                direction59 = direction36;
            }

            if (distance37 + weight59 < distance59) {
                distance59 = distance37 + weight59;
                direction59 = direction37;
            }
        }

        if (canVisit64) {
            if (distance40 + weight64 < distance64) {
                distance64 = distance40 + weight64;
                direction64 = direction40;
            }

            if (distance41 + weight64 < distance64) {
                distance64 = distance41 + weight64;
                direction64 = direction41;
            }

            if (distance42 + weight64 < distance64) {
                distance64 = distance42 + weight64;
                direction64 = direction42;
            }
        }

        if (canVisit50) {
            if (distance26 + weight50 < distance50) {
                distance50 = distance26 + weight50;
                direction50 = direction26;
            }

            if (distance49 + weight50 < distance50) {
                distance50 = distance49 + weight50;
                direction50 = direction49;
            }

            if (distance28 + weight50 < distance50) {
                distance50 = distance28 + weight50;
                direction50 = direction28;
            }

            if (distance25 + weight50 < distance50) {
                distance50 = distance25 + weight50;
                direction50 = direction25;
            }
        }

        if (canVisit51) {
            if (distance27 + weight51 < distance51) {
                distance51 = distance27 + weight51;
                direction51 = direction27;
            }

            if (distance49 + weight51 < distance51) {
                distance51 = distance49 + weight51;
                direction51 = direction49;
            }

            if (distance25 + weight51 < distance51) {
                distance51 = distance25 + weight51;
                direction51 = direction25;
            }

            if (distance29 + weight51 < distance51) {
                distance51 = distance29 + weight51;
                direction51 = direction29;
            }
        }

        if (canVisit55) {
            if (distance31 + weight55 < distance55) {
                distance55 = distance31 + weight55;
                direction55 = direction31;
            }

            if (distance54 + weight55 < distance55) {
                distance55 = distance54 + weight55;
                direction55 = direction54;
            }

            if (distance33 + weight55 < distance55) {
                distance55 = distance33 + weight55;
                direction55 = direction33;
            }

            if (distance30 + weight55 < distance55) {
                distance55 = distance30 + weight55;
                direction55 = direction30;
            }
        }

        if (canVisit56) {
            if (distance32 + weight56 < distance56) {
                distance56 = distance32 + weight56;
                direction56 = direction32;
            }

            if (distance54 + weight56 < distance56) {
                distance56 = distance54 + weight56;
                direction56 = direction54;
            }

            if (distance30 + weight56 < distance56) {
                distance56 = distance30 + weight56;
                direction56 = direction30;
            }

            if (distance34 + weight56 < distance56) {
                distance56 = distance34 + weight56;
                direction56 = direction34;
            }
        }

        if (canVisit60) {
            if (distance59 + weight60 < distance60) {
                distance60 = distance59 + weight60;
                direction60 = direction59;
            }

            if (distance36 + weight60 < distance60) {
                distance60 = distance36 + weight60;
                direction60 = direction36;
            }

            if (distance38 + weight60 < distance60) {
                distance60 = distance38 + weight60;
                direction60 = direction38;
            }

            if (distance35 + weight60 < distance60) {
                distance60 = distance35 + weight60;
                direction60 = direction35;
            }
        }

        if (canVisit61) {
            if (distance59 + weight61 < distance61) {
                distance61 = distance59 + weight61;
                direction61 = direction59;
            }

            if (distance37 + weight61 < distance61) {
                distance61 = distance37 + weight61;
                direction61 = direction37;
            }

            if (distance35 + weight61 < distance61) {
                distance61 = distance35 + weight61;
                direction61 = direction35;
            }

            if (distance39 + weight61 < distance61) {
                distance61 = distance39 + weight61;
                direction61 = direction39;
            }
        }

        if (canVisit65) {
            if (distance64 + weight65 < distance65) {
                distance65 = distance64 + weight65;
                direction65 = direction64;
            }

            if (distance41 + weight65 < distance65) {
                distance65 = distance41 + weight65;
                direction65 = direction41;
            }

            if (distance43 + weight65 < distance65) {
                distance65 = distance43 + weight65;
                direction65 = direction43;
            }

            if (distance40 + weight65 < distance65) {
                distance65 = distance40 + weight65;
                direction65 = direction40;
            }
        }

        if (canVisit66) {
            if (distance64 + weight66 < distance66) {
                distance66 = distance64 + weight66;
                direction66 = direction64;
            }

            if (distance42 + weight66 < distance66) {
                distance66 = distance42 + weight66;
                direction66 = direction42;
            }

            if (distance40 + weight66 < distance66) {
                distance66 = distance40 + weight66;
                direction66 = direction40;
            }

            if (distance44 + weight66 < distance66) {
                distance66 = distance44 + weight66;
                direction66 = direction44;
            }
        }

        if (canVisit45) {
            if (distance38 + weight45 < distance45) {
                distance45 = distance38 + weight45;
                direction45 = direction38;
            }

            if (distance28 + weight45 < distance45) {
                distance45 = distance28 + weight45;
                direction45 = direction28;
            }

            if (distance21 + weight45 < distance45) {
                distance45 = distance21 + weight45;
                direction45 = direction21;
            }
        }

        if (canVisit46) {
            if (distance43 + weight46 < distance46) {
                distance46 = distance43 + weight46;
                direction46 = direction43;
            }

            if (distance29 + weight46 < distance46) {
                distance46 = distance29 + weight46;
                direction46 = direction29;
            }

            if (distance22 + weight46 < distance46) {
                distance46 = distance22 + weight46;
                direction46 = direction22;
            }
        }

        if (canVisit47) {
            if (distance39 + weight47 < distance47) {
                distance47 = distance39 + weight47;
                direction47 = direction39;
            }

            if (distance33 + weight47 < distance47) {
                distance47 = distance33 + weight47;
                direction47 = direction33;
            }

            if (distance23 + weight47 < distance47) {
                distance47 = distance23 + weight47;
                direction47 = direction23;
            }
        }

        if (canVisit48) {
            if (distance44 + weight48 < distance48) {
                distance48 = distance44 + weight48;
                direction48 = direction44;
            }

            if (distance34 + weight48 < distance48) {
                distance48 = distance34 + weight48;
                direction48 = direction34;
            }

            if (distance24 + weight48 < distance48) {
                distance48 = distance24 + weight48;
                direction48 = direction24;
            }
        }

        if (canVisit52) {
            if (distance28 + weight52 < distance52) {
                distance52 = distance28 + weight52;
                direction52 = direction28;
            }

            if (distance50 + weight52 < distance52) {
                distance52 = distance50 + weight52;
                direction52 = direction50;
            }

            if (distance45 + weight52 < distance52) {
                distance52 = distance45 + weight52;
                direction52 = direction45;
            }

            if (distance26 + weight52 < distance52) {
                distance52 = distance26 + weight52;
                direction52 = direction26;
            }
        }

        if (canVisit53) {
            if (distance29 + weight53 < distance53) {
                distance53 = distance29 + weight53;
                direction53 = direction29;
            }

            if (distance51 + weight53 < distance53) {
                distance53 = distance51 + weight53;
                direction53 = direction51;
            }

            if (distance27 + weight53 < distance53) {
                distance53 = distance27 + weight53;
                direction53 = direction27;
            }

            if (distance46 + weight53 < distance53) {
                distance53 = distance46 + weight53;
                direction53 = direction46;
            }
        }

        if (canVisit57) {
            if (distance33 + weight57 < distance57) {
                distance57 = distance33 + weight57;
                direction57 = direction33;
            }

            if (distance55 + weight57 < distance57) {
                distance57 = distance55 + weight57;
                direction57 = direction55;
            }

            if (distance47 + weight57 < distance57) {
                distance57 = distance47 + weight57;
                direction57 = direction47;
            }

            if (distance31 + weight57 < distance57) {
                distance57 = distance31 + weight57;
                direction57 = direction31;
            }
        }

        if (canVisit58) {
            if (distance34 + weight58 < distance58) {
                distance58 = distance34 + weight58;
                direction58 = direction34;
            }

            if (distance56 + weight58 < distance58) {
                distance58 = distance56 + weight58;
                direction58 = direction56;
            }

            if (distance32 + weight58 < distance58) {
                distance58 = distance32 + weight58;
                direction58 = direction32;
            }

            if (distance48 + weight58 < distance58) {
                distance58 = distance48 + weight58;
                direction58 = direction48;
            }
        }

        if (canVisit62) {
            if (distance60 + weight62 < distance62) {
                distance62 = distance60 + weight62;
                direction62 = direction60;
            }

            if (distance38 + weight62 < distance62) {
                distance62 = distance38 + weight62;
                direction62 = direction38;
            }

            if (distance45 + weight62 < distance62) {
                distance62 = distance45 + weight62;
                direction62 = direction45;
            }

            if (distance36 + weight62 < distance62) {
                distance62 = distance36 + weight62;
                direction62 = direction36;
            }
        }

        if (canVisit63) {
            if (distance61 + weight63 < distance63) {
                distance63 = distance61 + weight63;
                direction63 = direction61;
            }

            if (distance39 + weight63 < distance63) {
                distance63 = distance39 + weight63;
                direction63 = direction39;
            }

            if (distance37 + weight63 < distance63) {
                distance63 = distance37 + weight63;
                direction63 = direction37;
            }

            if (distance47 + weight63 < distance63) {
                distance63 = distance47 + weight63;
                direction63 = direction47;
            }
        }

        if (canVisit67) {
            if (distance65 + weight67 < distance67) {
                distance67 = distance65 + weight67;
                direction67 = direction65;
            }

            if (distance43 + weight67 < distance67) {
                distance67 = distance43 + weight67;
                direction67 = direction43;
            }

            if (distance46 + weight67 < distance67) {
                distance67 = distance46 + weight67;
                direction67 = direction46;
            }

            if (distance41 + weight67 < distance67) {
                distance67 = distance41 + weight67;
                direction67 = direction41;
            }
        }

        if (canVisit68) {
            if (distance66 + weight68 < distance68) {
                distance68 = distance66 + weight68;
                direction68 = direction66;
            }

            if (distance44 + weight68 < distance68) {
                distance68 = distance44 + weight68;
                direction68 = direction44;
            }

            if (distance42 + weight68 < distance68) {
                distance68 = distance42 + weight68;
                direction68 = direction42;
            }

            if (distance48 + weight68 < distance68) {
                distance68 = distance48 + weight68;
                direction68 = direction48;
            }
        }

        Direction bestDirection = Direction.CENTER;
        double maxScore = 0;
        int currentDistance = rc.getLocation().distanceSquaredTo(target);

        double score28 = (double) (currentDistance - location28.distanceSquaredTo(target)) / (double) distance28;
        if (score28 > maxScore) {
            bestDirection = direction28;
            maxScore = score28;
        }

        double score29 = (double) (currentDistance - location29.distanceSquaredTo(target)) / (double) distance29;
        if (score29 > maxScore) {
            bestDirection = direction29;
            maxScore = score29;
        }

        double score33 = (double) (currentDistance - location33.distanceSquaredTo(target)) / (double) distance33;
        if (score33 > maxScore) {
            bestDirection = direction33;
            maxScore = score33;
        }

        double score34 = (double) (currentDistance - location34.distanceSquaredTo(target)) / (double) distance34;
        if (score34 > maxScore) {
            bestDirection = direction34;
            maxScore = score34;
        }

        double score38 = (double) (currentDistance - location38.distanceSquaredTo(target)) / (double) distance38;
        if (score38 > maxScore) {
            bestDirection = direction38;
            maxScore = score38;
        }

        double score39 = (double) (currentDistance - location39.distanceSquaredTo(target)) / (double) distance39;
        if (score39 > maxScore) {
            bestDirection = direction39;
            maxScore = score39;
        }

        double score43 = (double) (currentDistance - location43.distanceSquaredTo(target)) / (double) distance43;
        if (score43 > maxScore) {
            bestDirection = direction43;
            maxScore = score43;
        }

        double score44 = (double) (currentDistance - location44.distanceSquaredTo(target)) / (double) distance44;
        if (score44 > maxScore) {
            bestDirection = direction44;
            maxScore = score44;
        }

        double score45 = (double) (currentDistance - location45.distanceSquaredTo(target)) / (double) distance45;
        if (score45 > maxScore) {
            bestDirection = direction45;
            maxScore = score45;
        }

        double score46 = (double) (currentDistance - location46.distanceSquaredTo(target)) / (double) distance46;
        if (score46 > maxScore) {
            bestDirection = direction46;
            maxScore = score46;
        }

        double score47 = (double) (currentDistance - location47.distanceSquaredTo(target)) / (double) distance47;
        if (score47 > maxScore) {
            bestDirection = direction47;
            maxScore = score47;
        }

        double score48 = (double) (currentDistance - location48.distanceSquaredTo(target)) / (double) distance48;
        if (score48 > maxScore) {
            bestDirection = direction48;
            maxScore = score48;
        }

        double score49 = (double) (currentDistance - location49.distanceSquaredTo(target)) / (double) distance49;
        if (score49 > maxScore) {
            bestDirection = direction49;
            maxScore = score49;
        }

        double score50 = (double) (currentDistance - location50.distanceSquaredTo(target)) / (double) distance50;
        if (score50 > maxScore) {
            bestDirection = direction50;
            maxScore = score50;
        }

        double score51 = (double) (currentDistance - location51.distanceSquaredTo(target)) / (double) distance51;
        if (score51 > maxScore) {
            bestDirection = direction51;
            maxScore = score51;
        }

        double score52 = (double) (currentDistance - location52.distanceSquaredTo(target)) / (double) distance52;
        if (score52 > maxScore) {
            bestDirection = direction52;
            maxScore = score52;
        }

        double score53 = (double) (currentDistance - location53.distanceSquaredTo(target)) / (double) distance53;
        if (score53 > maxScore) {
            bestDirection = direction53;
            maxScore = score53;
        }

        double score54 = (double) (currentDistance - location54.distanceSquaredTo(target)) / (double) distance54;
        if (score54 > maxScore) {
            bestDirection = direction54;
            maxScore = score54;
        }

        double score55 = (double) (currentDistance - location55.distanceSquaredTo(target)) / (double) distance55;
        if (score55 > maxScore) {
            bestDirection = direction55;
            maxScore = score55;
        }

        double score56 = (double) (currentDistance - location56.distanceSquaredTo(target)) / (double) distance56;
        if (score56 > maxScore) {
            bestDirection = direction56;
            maxScore = score56;
        }

        double score57 = (double) (currentDistance - location57.distanceSquaredTo(target)) / (double) distance57;
        if (score57 > maxScore) {
            bestDirection = direction57;
            maxScore = score57;
        }

        double score58 = (double) (currentDistance - location58.distanceSquaredTo(target)) / (double) distance58;
        if (score58 > maxScore) {
            bestDirection = direction58;
            maxScore = score58;
        }

        double score59 = (double) (currentDistance - location59.distanceSquaredTo(target)) / (double) distance59;
        if (score59 > maxScore) {
            bestDirection = direction59;
            maxScore = score59;
        }

        double score60 = (double) (currentDistance - location60.distanceSquaredTo(target)) / (double) distance60;
        if (score60 > maxScore) {
            bestDirection = direction60;
            maxScore = score60;
        }

        double score61 = (double) (currentDistance - location61.distanceSquaredTo(target)) / (double) distance61;
        if (score61 > maxScore) {
            bestDirection = direction61;
            maxScore = score61;
        }

        double score62 = (double) (currentDistance - location62.distanceSquaredTo(target)) / (double) distance62;
        if (score62 > maxScore) {
            bestDirection = direction62;
            maxScore = score62;
        }

        double score63 = (double) (currentDistance - location63.distanceSquaredTo(target)) / (double) distance63;
        if (score63 > maxScore) {
            bestDirection = direction63;
            maxScore = score63;
        }

        double score64 = (double) (currentDistance - location64.distanceSquaredTo(target)) / (double) distance64;
        if (score64 > maxScore) {
            bestDirection = direction64;
            maxScore = score64;
        }

        double score65 = (double) (currentDistance - location65.distanceSquaredTo(target)) / (double) distance65;
        if (score65 > maxScore) {
            bestDirection = direction65;
            maxScore = score65;
        }

        double score66 = (double) (currentDistance - location66.distanceSquaredTo(target)) / (double) distance66;
        if (score66 > maxScore) {
            bestDirection = direction66;
            maxScore = score66;
        }

        double score67 = (double) (currentDistance - location67.distanceSquaredTo(target)) / (double) distance67;
        if (score67 > maxScore) {
            bestDirection = direction67;
            maxScore = score67;
        }

        double score68 = (double) (currentDistance - location68.distanceSquaredTo(target)) / (double) distance68;
        if (score68 > maxScore) {
            bestDirection = direction68;
            maxScore = score68;
        }

        return bestDirection;
    }
}
