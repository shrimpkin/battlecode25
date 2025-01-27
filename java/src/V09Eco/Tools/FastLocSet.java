package V09Eco.Tools;

import battlecode.common.MapLocation;

public class FastLocSet {
    public StringBuilder keys;
    public int size;

    public FastLocSet() {
        keys = new StringBuilder();
        size = 0;
    }

    public void add(MapLocation loc) {
        String key = "^" + (char) (loc.x) + (char) (loc.y);
        if (keys.indexOf(key) == -1) {
            keys.append(key);
            size++;
        }
    }

    public void add(int x, int y) {
        String key = "^" + (char) x + (char) y;
        if (keys.indexOf(key) == -1) {
            keys.append(key);
            size++;
        }
    }

    public void remove(MapLocation loc) {
        String key = "^" + (char) (loc.x) + (char) (loc.y);
        int index;
        if ((index = keys.indexOf(key)) >= 0) {
            keys.delete(index, index + 3);
            size--;
        }
    }

    public void remove(int x, int y) {
        String key = "^" + (char) x + (char) y;
        int index;
        if ((index = keys.indexOf(key)) >= 0) {
            keys.delete(index, index + 3);
            size--;
        }
    }

    public boolean contains(MapLocation loc) {
        return keys.indexOf("^" + (char) (loc.x) + (char) (loc.y)) >= 0;
    }

    public boolean contains(int x, int y) {
        return keys.indexOf("^" + (char) x + (char) y) >= 0;
    }

    public void clear() {
        size = 0;
        keys = new StringBuilder();
    }

    public void replace(String newSet) {
        keys.replace(0, keys.length(), newSet);
        size = newSet.length() / 3;
    }

    public void union(FastLocSet s) {
        for (int i = 1; i < s.keys.length(); i += 3) {
            add((int) s.keys.charAt(i), (int) s.keys.charAt(i + 1));
        }
    }

    public MapLocation[] getKeys() {
        // note that this op is expensive
        MapLocation[] locs = new MapLocation[size];
        for (int i = 1; i < keys.length(); i += 3) {
            locs[i / 3] = new MapLocation(keys.charAt(i), keys.charAt(i + 1));
        }
        return locs;
    }

    public MapLocation pop() {
        if (size == 0)
            return null;
        MapLocation loc = new MapLocation(keys.charAt(1), keys.charAt(2));
        remove(loc);
        return loc;
    }
}