package V04BOTweakedPatch;

public class FastIntSet {
    public StringBuilder keys;
    public int size;
    
    /** Init new FastIntSet */
    public FastIntSet() {
        keys = new StringBuilder();
        size = 0;
    }

    /** Adds element to set */
    public void add(int i) {
        String key = String.valueOf((char) i);
        if (keys.indexOf(key) < 0) {
            keys.append(key);
            size++;
        }
    }

    /** Removes an element from the set, if it's there */
    public void remove(int i) {
        String key = String.valueOf((char) i);
        int index;
        if ((index = keys.indexOf(key)) >= 0) {
            keys.deleteCharAt(index);
            size--;
        }
    }

    /** Checks if element exists in set */
    public boolean contains(int i) {
        return keys.indexOf(String.valueOf((char) i)) >= 0;
    }

    /** Resets set to have no items */
    public void clear() {
        size = 0;
        keys = new StringBuilder();
    }
}

