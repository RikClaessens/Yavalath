package util;

import java.util.HashSet;

/**
 * Created by rikclaessens on 18/07/14.
 */
public class Util {

    private static Util instance = new Util();
    private Util() {}

    public static Util getInstance() {
        return instance;
    }

    public static int[] toIntArray(HashSet<Integer> set) {
        int[] a = new int[set.size()];
        int i = 0;
        for (Integer val : set) a[i++] = val;
        return a;
    }
}