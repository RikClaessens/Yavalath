package players.ai;

import game.Board;
import game.RowOfFour;
import players.Player;
import players.PlayerSettings;
import players.ai.tt.TTEntry;
import util.Util;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class IDNegamax implements Player {

    private int piece;
    private int opponentPiece;
    private int numberOfMovesMadeBeforeSearch;
    private int bestMove = -1;
    private int globalMaxDepth;

    private int nodesVisited;

    // Evaluation Function Scores & Penalties
    private static int WINSCORE = Integer.MAX_VALUE - 1000;
    private static int FORCED_MOVES_SCORE = 500;
    private static int FORCED_MOVES_PENALTY = 800;
    private static int CAN_FORCE_MOVE_SCORE = 200;
    private static int CAN_BE_FORCED_TO_MOVE_PENALTY = 450;

    // Move Ordering related variables
    private boolean orderMoves;

    // Iterative Depth related variables
    private int[][] principalVariation;
    private int bestPVScore;

    // TranspositionTable related variables
    private HashMap<Long, TTEntry> tt;
    private static int TT_SIZE;

    public IDNegamax(PlayerSettings pSettings) {
        this.piece = pSettings.piece;
        this.opponentPiece = pSettings.getOpponentPiece(piece);
        this.opponentPiece = piece == Board.WHITE ? Board.BLACK : Board.WHITE;
        this.globalMaxDepth = pSettings.maxDepth;
        this.orderMoves = pSettings.orderMoves;
    }

    @Override
    public int doMove(Board board) {
        // reset the counter for the number of nodes visited
        nodesVisited = 0;
        // save the number of moves played on the board for which we need to select a move
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        // initialize the principal variation
        principalVariation = new int[globalMaxDepth][globalMaxDepth];
        // initialize the transposition table
        tt = new HashMap<Long, TTEntry>(TT_SIZE);

        int idBestMove = -1;
        for (int depth = 1; depth <= globalMaxDepth; depth++) {
            bestPVScore = Integer.MIN_VALUE;
            int score = negamax(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, depth);
            idBestMove = bestMove;
            System.out.println("Search depth [" + depth + "], best move: " + bestMove);
//            System.out.println("Visited " + nodesVisited + " nodes");
        }
//        System.out.println("Score = " + score + " best move = " + bestMove);

        return idBestMove;
    }

    public int negamax(Board board, int depth, double alpha, double beta, int color, int currentMaxDepth) {
        nodesVisited++;
        double alphaOriginal = alpha;

        // transposition table lookup
        if (tt.containsKey(board.hashKey)) {
            TTEntry ttEntry = tt.get(board.hashKey);
            if (ttEntry.depth >= depth) {
                switch (ttEntry.flag) {
                    case TTEntry.EXACT:
                        return ttEntry.value;
                    case TTEntry.LOWER_BOUND:
                        alpha = Math.max(alpha, ttEntry.value);
                        break;
                    case TTEntry.UPPER_BOUND:
                        beta = Math.min(beta, ttEntry.value);
                        break;
                }
                if (alpha >= beta) {
                    return ttEntry.value;
                }
            }
        }

        // check if terminal node, i.e. game is won or maximum depth has been reached
        if (board.isGameOver() || depth == 0) {
            int value = color * evaluate(board);
            // Save principal variation if a higher score is reached
            if (value > bestPVScore && depth == 0) {
                System.arraycopy(board.movesMade, numberOfMovesMadeBeforeSearch, principalVariation[currentMaxDepth - 1], 0, board.numberOfMovesMade - numberOfMovesMadeBeforeSearch);
                bestPVScore = value;
            }
            return value;
        }
        // standard negamax
        int score = Integer.MIN_VALUE;
        int[] moves = orderMoves ? orderPVMoves(currentMaxDepth - depth, board, currentMaxDepth) : board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int value = -negamax(board, depth - 1, -beta, -alpha, -color, currentMaxDepth);
            board.undoMoveWithCheck(child);
            if (value > score) {
                score = value;
                // keep track of best move found so far
                if (depth == currentMaxDepth) {
//                    System.out.println("\tNew best move " + child);
                    bestMove = child;
                }
            }
            if (value > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }

        // transposition table store
        TTEntry ttEntry = new TTEntry();
        ttEntry.value = score;
        if (score <= alphaOriginal) {
            ttEntry.flag = TTEntry.UPPER_BOUND;
        } else if (score >= beta) {
            ttEntry.flag = TTEntry.LOWER_BOUND;
        } else {
            ttEntry.flag = TTEntry.EXACT;
        }
        ttEntry.depth = depth;
        tt.put(board.hashKey, ttEntry);

        return score;
    }

    public int[] orderPVMoves(int depth, Board board, int currentMaxDepth) {
        if (currentMaxDepth <= 1 || depth == 0) {
            return board.getAllowedMoves();
        }
        HashSet<Integer> allowedMoves = board.getAllowedMoveSet();
        int bestMoveForDepth = principalVariation[currentMaxDepth - 2][currentMaxDepth - depth - 1];
        if (bestMoveForDepth != 0 && allowedMoves.contains(bestMoveForDepth)) {
            int[] moves = new int[allowedMoves.size()];
            moves[0] = bestMoveForDepth;
            int i = 1;
            for (Integer val : allowedMoves) {
                if (val != bestMoveForDepth) {
                    moves[i++] = val;
                }
            }
            return moves;
        }
        return Util.toIntArray(allowedMoves);
    }

    public String printMoves(Board board) {
        StringBuilder moveBuilder = new StringBuilder();
        for (int i = 0; i < numberOfMovesMadeBeforeSearch; i++) {
            String move = "[" + board.movesMade[i] + "]";
            moveBuilder.append(move);
        }
        moveBuilder.append(" - ");
        for (int i = numberOfMovesMadeBeforeSearch; i < board.numberOfMovesMade; i++) {
            String move = "[" + board.movesMade[i] + "]";
            moveBuilder.append(move);
        }
        return moveBuilder.toString();
    }

    public int evaluate(Board board) {
        int score = 0;
        if (board.isGameOver()) {
            if (board.gameWon == piece) {
                score = WINSCORE - board.numberOfMovesMade;
            } else {
                score = -WINSCORE + board.numberOfMovesMade;
            }
        } else {
            for (int i = 0; i < Board.NUMBER_OF_CELLS; i++) {
                if (Board.CELLS[i] == 0) {
                    continue;
                } else if (board.board[i].piece == piece) {
                    score += Board.DISTANCES[i];
                }
                for (RowOfFour rowOfFour : board.board[i].rowsOfFour) {
                    if (rowOfFour.fields[0].position != i) {
                        continue;
                    }
                    if (rowOfFour.fields[0].piece == piece
                            && rowOfFour.fields[1].piece == Board.FREE
                            && rowOfFour.fields[2].piece == Board.FREE
                            && rowOfFour.fields[3].piece == piece) {
                        score += CAN_FORCE_MOVE_SCORE;
                    } else if (rowOfFour.fields[0].piece == opponentPiece
                            && rowOfFour.fields[1].piece == Board.FREE
                            && rowOfFour.fields[2].piece == Board.FREE
                            && rowOfFour.fields[3].piece == opponentPiece) {
                        score -= CAN_BE_FORCED_TO_MOVE_PENALTY;
                    }
                }
            }
            if (piece == Board.WHITE) {
                score += board.forcedMovesByWhite.size() * FORCED_MOVES_SCORE;
            } else {
                score -= board.forcedMovesByBlack.size() * FORCED_MOVES_PENALTY;
            }
        }
        return score;
    }
}
