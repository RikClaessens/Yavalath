package test;

import game.Board;
import util.Util;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class XOrTest {
    public static void main(String[] args) {
        XOrTest xOrTest = new XOrTest();
        xOrTest.test();
        xOrTest.test2();
    }

    public void test() {
        long a = Util.nextLong();
        long b = Util.nextLong();
        long c = a ^ b;
        System.out.println(c);
        c = c ^ b;
        System.out.println(c);
        c = c ^ b;
        System.out.println(c);
    }

    public void test2() {
        System.out.println();
        Board board = new Board();
        board.doMove(40);
        board.doMove(42);
        Board copy = board.copy();
        System.out.println(board.hashKey);
        System.out.println(copy.hashKey);
    }
}
