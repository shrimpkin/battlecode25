package V04;

import battlecode.common.*;
import java.util.*;

public class LocMap {
    private int width, height;
    private boolean[] marked;

    /** New map initialized with height and width */
    public LocMap(int w, int h) {
        width = w;
        height = h;
        marked = new boolean[width * height];
    }

    /** Checks if location is NOT marked */
    public boolean available(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;
        return !marked[x * height + y];
    }

    /** Marks location */
    public void mark(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= width || y < 0 || y >= height)
            return;
        marked[x * height + y] = true;
    }

    /** Removes all marks */
    public void clearAll() {
        Arrays.fill(marked, false);
    }
}
