package players;

import game.Board;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class PlayerSettings {
    public boolean useTT;
    public boolean useMoveOrdering;
    public boolean usePVS;
    public boolean useNullMove;
    public boolean useQuiescence;
    public boolean useKillerMoves;
    public int nullMoveR;
    public int maxDepth;
    public int piece;
    public int numberOfKillerMoves;
    public static int HUMAN = -1, IDNEGAMAX = 0, MINIMAX = 1;

    public int getOpponentPiece(int piece) {
        return piece == Board.WHITE ? Board.BLACK : Board.WHITE;
    }
}
