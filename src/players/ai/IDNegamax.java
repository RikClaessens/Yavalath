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
    private static int FORCED_MOVES_PENALTY = -800;
    private static int CAN_FORCE_MOVE_SCORE = 200;
    private static int CAN_BE_FORCED_TO_MOVE_PENALTY = -450;
    private static int TRIANGLE_OF_TWO_SCORE = 600;
    private static int TRIANGLE_OF_TWO_PENALTY = -900;
    private static int DISTANCE_SCORE = 200;

    // Move Ordering related variables
    private boolean useMoveOrdering;

    // Principal variation related variables
    private int[][] principalVariationMoves;
    private int bestPVScore;
    private boolean usePVS;

    // TranspositionTable related variables
    private boolean useTT;
    private HashMap<Long, TTEntry> tt;
    private static int TT_SIZE;

    // Null move related variables
    private boolean useNullMove;
    private int nullMoveR;

    // Quiescence Search related variables
    private boolean useQuiescence;

    public IDNegamax(PlayerSettings playerSettings) {
        this.piece = playerSettings.piece;
        this.opponentPiece = playerSettings.getOpponentPiece(piece);
        this.opponentPiece = piece == Board.WHITE ? Board.BLACK : Board.WHITE;
        this.globalMaxDepth = playerSettings.maxDepth;
        this.useMoveOrdering = playerSettings.useMoveOrdering;
        this.useTT = playerSettings.useTT;
        this.usePVS = playerSettings.usePVS;
        this.useNullMove = playerSettings.useNullMove;
        this.useQuiescence = playerSettings.useQuiescence;
    }

    @Override
    public int doMove(Board board) {
        // reset the counter for the number of nodes visited
        nodesVisited = 0;
        // save the number of moves played on the fields for which we need to select a move
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        // initialize the principal variation
        principalVariationMoves = new int[globalMaxDepth][globalMaxDepth];
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
        System.out.println("Seleted move: " + idBestMove + ", # nodes: " + nodesVisited);

        return idBestMove;
    }

    @Override
    public boolean isHuman() {
        return false;
    }

    public int negamax(Board board, int depth, int alpha, int beta, int color, int currentMaxDepth) {
        nodesVisited++;
        double alphaOriginal = alpha;

        // transposition table lookup
        if (useTT && tt.containsKey(board.hashKey)) {
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

        // null move
        /*
         conduct a null-move search if it is legal and desired
        if (!in check() && null ok()){
            make null move();
         null-move search with minimal window around beta
            value = -search(-beta, -beta + 1, depth - R - 1);
            if (value >= beta) cutoff in case of fail-high
                return value;
        }
         */

        // null moves
        if (useNullMove) {
            // do not use a null move when player is forced to play some moves, because this leads to instant loss
            // also do not allow 2 null moves follow each other
            if (!board.allowedMovesForced() && board.numberOfMovesMade > 2 && board.numberOfMovesMade > Board.NUMBER_OF_FIELDS && board.movesMade[board.numberOfMovesMade - 1] != -1) {
                board.doNullMove();
                int value = -negamax(board, depth - nullMoveR - 1, -beta, -beta + 1, -color, currentMaxDepth);
                board.undoNullMove();
                if (value >= beta) {
                    return value;
                }
            }
        }

        // check if terminal node, i.e. game is won or maximum depth has been reached
        if (board.isGameOver() || depth == 0) {
            int value;
            // Quiescence Search
            if (useQuiescence) {
                value = quiescence(board, alpha, beta, color);
            } else {
                value = color * evaluate(board);
            }
            // Save principal variation if a higher score is reached
            if (value > bestPVScore && depth == 0) {
                System.arraycopy(board.movesMade, numberOfMovesMadeBeforeSearch, principalVariationMoves[currentMaxDepth - 1], 0, board.numberOfMovesMade - numberOfMovesMadeBeforeSearch);
                bestPVScore = value;
            }
            return value;
        }
        // standard negamax
        int score = Integer.MIN_VALUE;
        int[] moves = useMoveOrdering ? orderPVMoves(currentMaxDepth - depth, board, currentMaxDepth) : board.getAllowedMoves();
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

    /*
    QS(s,alpha,beta){
        score = Eval(s);
        if( score >= beta)
            return score;
        if( score > alpha)
            alpha = score;
        for( child = 1; child <= NumQSSuccessors( s ); child++ ) {
            result = -QS( QSSuccessor( child ), -beta, -alpha);
            if( result > score ){
                score = result;
                if( result >= beta )
                    break;
                if( result > alpha )
                    score = alpha;
            }
        }
        return( score );
    }
     */

    public int quiescence(Board board, int alpha, int beta, int color) {
        int score = color * evaluate(board);
        if (score >= beta) {
            return score;
        }
        if (score > alpha) {
            return score;
        }
        int[] moves = board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int result = -quiescence(board, -beta, -alpha, -color);
            board.undoMoveWithCheck(child);
            if (result > score) {
                score = result;
            }
            if (result > alpha) {
                score = alpha;
            }
            if (result >= beta) {
                break;
            }
        }
        return score;
    }


    public int[] orderPVMoves(int depth, Board board, int currentMaxDepth) {
        if (currentMaxDepth <= 1 || depth == 0) {
            return board.getAllowedMoves();
        }
        HashSet<Integer> allowedMoves = board.getAllowedMoveSet();
        int bestMoveForDepth = principalVariationMoves[currentMaxDepth - 2][currentMaxDepth - depth - 1];
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

    // evaluation function
    public int evaluate(Board board) {
        int score = 0;
        // check for a game win, take into account the numberofmovesmade > postponing a loss or getting to a win sooner is better
        if (board.isGameOver()) {
            if (board.gameWon == piece) {
                score = WINSCORE - board.numberOfMovesMade;
            } else {
                score = -WINSCORE + board.numberOfMovesMade;
            }
        } else {
            for (int i = 0; i < Board.NUMBER_OF_CELLS; i++) {
                // playing near the center gives a small bonus in the early game, because there is more freedom
                if (Board.CELLS[i] == 0) {
                    continue;
                } else if (board.fields[i].piece == piece) {
                    score += Board.DISTANCES[i] * DISTANCE_SCORE;
                }
                // check for ros of four > strong position
                for (RowOfFour rowOfFour : board.fields[i].rowsOfFour) {
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
                        score += CAN_BE_FORCED_TO_MOVE_PENALTY;
                    }
                }
                // check for triangles of size 2 > strong position
                // 2 possible options:
                // 1] field i and its south-west and south-east neighbors
                // 2] field i and its east and south-east neighbors
                // optimize by checking field i first and then the south-east neighbor
                // no need to check in last row
                if (board.row(i) < 8 && board.fields[i].piece == piece) {
                    if (board.fields[i].neighbors[Board.neighborSE] != null && board.fields[i].neighbors[Board.neighborSE].piece == piece) {
                        // check for option 1]
                        if (board.fields[i].neighbors[Board.neighborSW] != null && board.fields[i].neighbors[Board.neighborSW].piece == piece) {
                            score += TRIANGLE_OF_TWO_SCORE;
                        }
                        // check for option 2]
                        if (board.fields[i].neighbors[Board.neighborE] != null && board.fields[i].neighbors[Board.neighborE].piece == piece) {
                            score += TRIANGLE_OF_TWO_SCORE;
                        }
                    }
                }
                // decrease the score if opponent gets a triangle of two
                if (board.row(i) < 8 && board.fields[i].piece == opponentPiece) {
                    if (board.fields[i].neighbors[Board.neighborSE] != null && board.fields[i].neighbors[Board.neighborSE].piece == opponentPiece) {
                        // check for option 1]
                        if (board.fields[i].neighbors[Board.neighborSW] != null && board.fields[i].neighbors[Board.neighborSW].piece == opponentPiece) {
                            score += TRIANGLE_OF_TWO_PENALTY;
                        }
                        // check for option 2]
                        if (board.fields[i].neighbors[Board.neighborE] != null && board.fields[i].neighbors[Board.neighborE].piece == opponentPiece) {
                            score += TRIANGLE_OF_TWO_PENALTY;
                        }
                    }
                }
            }
            if (piece == Board.WHITE) {
                score += board.forcedMovesByWhite.size() * FORCED_MOVES_SCORE;
            } else {
                score += board.forcedMovesByBlack.size() * FORCED_MOVES_PENALTY;
            }
        }
        return score;
    }
}
