package players;

import game.Board;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class MiniMax implements Player {
    @Override
    public int doMove(Board board) {
        System.out.println("Minimax baby");
        HashSet<Integer> moveSet = board.getAllowedMoves();
        System.out.println("Got " + moveSet.size() + " moves");
        Integer[] moves = moveSet.toArray(new Integer[moveSet.size()]);
        int move = moves[new Random().nextInt(moves.length)];
        System.out.println("playing doMove " + move);
        return move;
    }
}
