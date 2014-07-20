package players;

import game.Board;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class PlayerSettings {
    public boolean transpositionTable;
    public boolean orderMoves;
    public boolean principalVariation;
    public int maxDepth;
    public int piece;
    public boolean isHuman;
    public static int HUMAN = -1, IDNEGAMAX = 0, MINIMAX = 1;

    public int getOpponentPiece(int piece) {
        return piece == Board.WHITE ? Board.BLACK : Board.WHITE;
    }
}
