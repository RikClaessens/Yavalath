package util;

import game.Board;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.HashSet;

/**
 * Created by rikclaessens on 18/07/14.
 */
public class Util {

    private static Util instance = new Util();
    private Util() {
        mersenneTwister = new MersenneTwister(0);
    }

    public static Util getInstance() {
        return instance;
    }

    public static int[] toIntArray(HashSet<Integer> set) {
        int[] a = new int[set.size()];
        int i = 0;
        for (Integer val : set) a[i++] = val;
        return a;
    }

    private static MersenneTwister mersenneTwister;
    public static long nextLong() {
        return mersenneTwister.nextLong();
    }

    public static String piecePlayer(int piece) {
        switch (piece) {
            case Board.BLACK: return "BLACK";
            case Board.WHITE: return "WHITE";
            case Board.FREE: return "FREE";
            default: return "";
        }
    }

    public static String piecePlayerLabel(int piece) {
        switch (piece) {
            case Board.BLACK: return "B";
            case Board.WHITE: return "W";
            case Board.FREE: return ".";
            default: return "";
        }
    }
}