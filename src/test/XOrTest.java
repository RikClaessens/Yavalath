package test;

import game.Board;
import util.Util;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class XOrTest {
    public static void main(String[] args) {
        XOrTest xOrTest = new XOrTest();
//        xOrTest.test();
//        xOrTest.test2();
//        xOrTest.test3();
        xOrTest.test4();
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

    public void test3() {
        Board board = new Board();
        board.doMove(40); // W
        board.doMove(2);
        board.doMove(42); // W
        board.doMove(3);
        board.doMove(48); // W
        board.doMove(74);
        board.doMove(67); // W
        board.doMove(78);
        board.doMove(39); // W
        board.doMove(41);

        System.out.println(board.hashKey);
        board.doNullMove();
        System.out.println(board.hashKey);
        board.doMove(58);
        System.out.println(board.hashKey);
        board.undoMoveWithCheck(58);
        System.out.println(board.hashKey);
        board.undoNullMove();
        System.out.println(board.hashKey);
        board.doMove(36);
        System.out.println(board.hashKey);
    }

    public void test4() {
        System.out.println(Integer.MIN_VALUE);
        System.out.println(Integer.MAX_VALUE);
        System.out.println(-Integer.MIN_VALUE);
        System.out.println(-Integer.MAX_VALUE);
    }
}
