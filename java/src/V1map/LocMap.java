package V1map;

import battlecode.common.MapLocation;

import java.util.Arrays;

public class LocMap {
    private int width, height;
    private boolean[] marked;
    public LocMap(int w, int h) {
        width = w;
        height = h;
        marked = new boolean[width * height];
    }
    public boolean available(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;
        return !marked[x * height + y];
    }

    public void mark(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= width || y < 0 || y >= height)
            return;
        marked[x * height + y] = true;
    }

    public void clearAll() {
        Arrays.fill(marked, false);
    }
}
