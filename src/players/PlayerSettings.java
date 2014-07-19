package players;

import game.Board;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class PlayerSettings {
    public boolean transpositionTable;
    public boolean orderMoves;
    public int maxDepth;
    public int piece;

    public int getOpponentPiece(int piece) {
        return piece == Board.WHITE ? Board.BLACK : Board.WHITE;
    }
}
