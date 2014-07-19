package players.ai.tt;

/**
 * Created by rikclaessens on 19/07/14.
 */
public class TTEntry {
    // the entry's flag can be an exact value, a lower bound or upper bound for the real value
    public static final int EXACT = 0, LOWER_BOUND = 1, UPPER_BOUND = 2;
    // the entry's value, type of value (flag), the best move found for this entry and its depth
    public int value, flag, bestMove, depth;
}
