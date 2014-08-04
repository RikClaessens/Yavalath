package players.ai;

import game.Board;
import game.Field;
import game.RowOfFour;
import players.Player;
import players.PlayerSettings;
import players.ai.tt.TTEntry;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class AIPlayer implements Player {

    private int piece;
    private int opponentPiece;
    private int numberOfMovesMadeBeforeSearch;
    private int bestMove;
    private int bestScore;
    private int globalMaxDepth;

    private int nodesVisited;
    private long[] totalNodesVisited;
    private long[] totalTimeNeeded;

    private static int INF = 2000000000;

    // Evaluation Function Scores & Penalties
    private static int WIN_SCORE = 1000000;
    private static int LOSS_SCORE = -1000000;
    private static int FORCED_MOVES_SCORE = 500;
    private static int FORCED_MOVES_PENALTY = 0;
    private static int CAN_FORCE_MOVE_SCORE = 200;
    private static int CAN_BE_FORCED_TO_MOVE_PENALTY = 0;
    private static int TRIANGLE_OF_TWO_SCORE = 300;
    private static int TRIANGLE_OF_TWO_PENALTY = -900;
    private static int DISTANCE_SCORE = 200;
    private static int TRIANGLE_OF_THREE_SCORE = 400;
    private static int TRIANGLE_OF_THREE_PENALTY = -500;
    private static int TRIANGLE_OF_FOUR_SCORE = 300;
    private static int TRIANGLE_OF_FOUR_PENALTY = -200;

    // Evaluation function related variables
    public static final byte[] FIRST_MOVE_DISTANCES = {
            0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,
            0,0,0,4,4,0,0,0,0,
            0,0,0,4,5,4,0,0,0,
            0,0,0,4,4,0,0,0,0,
            0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,
    };

    // Move Ordering related variables
    private boolean useMoveOrdering;
    public static int[] staticMoveOrder = new int[]{40,
            30,31,41,49,48,39,
            29,21,22,23,32,42,50,59,58,57,47,38,
            28,20,11,12,13,14,24,33,43,51,60,68,67,66,65,56,46,37,
            27,19,10,2,3,4,5,6,15,25,34,44,52,61,69,78,77,76,75,74,64,55,45,36};

    // Principal variation related variables
    private int[][] principalVariationMoves;
    private int bestPVScore;
    private boolean usePC;

    // TranspositionTable related variables
    private boolean useTT;
    private HashMap<Long, TTEntry> tt;
    private static int TT_SIZE = 2^26;

    // Null move related variables
    private boolean useNullMove;
    private int nullMoveR;

    // Quiescence Search related variables
    private boolean useQuiescence;

    // Killer Move related variables
    private boolean useKillerMoves;
    private ArrayList<Integer>[] killerMoves;
    private int numberOfKillerMoves;

    // Relative History Heuristic related variables
    private boolean useRelativeHistoryHeuristic;
    private int[] hhScore;
    private int[] bfScore;
    private int hhIncrement = 1;
    private int bfIncrement = 1;

    // Aspiration search related variables
    private boolean useAspirationSearch;
    private int aspirationWindow;
    private int lastDepthScore;

    private static int WIN_THRESHOLD = WIN_SCORE - 100;

    public AIPlayer(PlayerSettings playerSettings) {
        this.piece = playerSettings.piece;
        this.opponentPiece = playerSettings.getOpponentPiece(piece);
        this.opponentPiece = piece == Board.WHITE ? Board.BLACK : Board.WHITE;
        this.globalMaxDepth = playerSettings.maxDepth;
        this.useMoveOrdering = playerSettings.useMoveOrdering;
        this.useTT = playerSettings.useTT;
        this.usePC = playerSettings.usePC;
        this.useNullMove = playerSettings.useNullMove;
        this.useQuiescence = playerSettings.useQuiescence;
        this.useKillerMoves = playerSettings.useKillerMoves;
        this.numberOfKillerMoves = playerSettings.numberOfKillerMoves;
        this.useRelativeHistoryHeuristic = playerSettings.useRelativeHistoryHeuristic;
        this.useAspirationSearch = playerSettings.useAspirationSearch;
        this.aspirationWindow = playerSettings.aspirationWindow;
        this.totalNodesVisited = new long[this.globalMaxDepth + 1];
        this.totalTimeNeeded = new long[this.globalMaxDepth + 1];
        this.nullMoveR = playerSettings.nullMoveR;
        System.out.println("Initialized an AI player");
        System.out.println("TT: " + useTT);
        System.out.println("Null moves: " + useNullMove + " R = " + nullMoveR);
        System.out.println("Quiescence: " + useQuiescence);
        System.out.println("Killer moves: " + useKillerMoves + " # of killer moves = " + numberOfKillerMoves);
        System.out.println("Relative History Heuristic: " + useRelativeHistoryHeuristic + " # of killer moves = " + numberOfKillerMoves);
        System.out.println("Aspiration Search: " + useAspirationSearch + " aspiration window " + aspirationWindow);
        System.out.println("Move ordering: " + useMoveOrdering);
        System.out.println("Global max depth: " + globalMaxDepth);
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
        // reset the killer moves
        killerMoves = new ArrayList[Board.NUMBER_OF_CELLS - numberOfMovesMadeBeforeSearch];
        // reset the counters for the history and butterfly score
        hhScore = new int[Board.NUMBER_OF_CELLS];
        bfScore = new int[Board.NUMBER_OF_CELLS];

        // reset a number of variables to "start of search" values
        int idBestMove = -1;
        bestMove = -1;
        bestScore = -INF;
        long timeSearchStart = System.currentTimeMillis();
        for (int depth = 1; depth <= globalMaxDepth; depth++) {
            bestPVScore = Integer.MIN_VALUE;

            int alpha = -INF;
            int beta = INF;
            if (useAspirationSearch && depth > 1) {
                alpha = lastDepthScore - aspirationWindow;
                beta = lastDepthScore + aspirationWindow;
            }
            int score = negamax(board, depth, alpha, beta, 1, depth);
            idBestMove = bestMove;
            System.out.println("Search depth [" + depth + "], best move: " + bestMove + " score: " + score + " # of nodes visited " + nodesVisited);
//            System.out.println("Visited " + nodesVisited + " nodes");
            if (score >= WIN_THRESHOLD) {
                break;
            }
            if (useAspirationSearch) {
                if (score >= beta) {
                    System.out.println("Fail high");
                    alpha = score;
                    beta = INF;
                    score = negamax(board, depth, alpha, beta, 1, depth);
                    idBestMove = bestMove;
                    System.out.println("Redid search high [" + depth + "], best move: " + bestMove + " score: " + score + " # of nodes visited " + nodesVisited);
                } else if (score <= alpha) {
                    System.out.println("Fail low");
                    alpha = -INF;
                    beta = score;
                    score = negamax(board, depth, alpha, beta, 1, depth);
                    idBestMove = bestMove;
                    System.out.println("Redid search low [" + depth + "], best move: " + bestMove + " score: " + score + " # of nodes visited " + nodesVisited);
                }
            }
            lastDepthScore = score;
            totalNodesVisited[depth] += nodesVisited;
            totalTimeNeeded[depth] += System.currentTimeMillis() - timeSearchStart;
        }
        System.out.println("Selected move: " + idBestMove + ", # nodes: " + nodesVisited);

        tt.clear();
        totalNodesVisited[0] += nodesVisited;
        totalTimeNeeded[0] += System.currentTimeMillis() - timeSearchStart;
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

        // null moves
        if (useNullMove) {
            // do not use a null move when player is forced to play some moves, because this leads to instant loss
            // also do not allow 2 null moves follow each other
            if (depth >= nullMoveR + 1
                    && currentMaxDepth >= 3
                    && !board.isGameOver()
                    && board.numberOfMovesMade > 2
                    && board.allowedMovesForced()
                    && board.movesMade[board.numberOfMovesMade - 1] != -1) {
                board.doNullMove();
                int value = -negamax(board, depth - nullMoveR - 1, -beta, -alpha, -color, currentMaxDepth);
                board.undoNullMove();
                if (value >= beta) {
                    return value;
                }
            }
        }

        // check if terminal node, i.e. game is won or maximum depth has been reached
        if (board.isGameOver() || depth <= 0) {
            int value;
            // Quiescence Search
            if (useQuiescence) {
                value = quiescence(board, depth, alpha, beta, color, currentMaxDepth);
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
        int[] moves = moveOrdering(currentMaxDepth - depth, board, currentMaxDepth);
        for (int child : moves) {
            board.doMove(child);
//            if (currentMaxDepth < 4)
//                System.out.println(Util.getTabs(board.numberOfMovesMade - numberOfMovesMadeBeforeSearch) + "> " + child);
            int value = -negamax(board, depth - 1, -beta, -alpha, -color, currentMaxDepth);
//            if (currentMaxDepth < 4)
//                System.out.println(Util.getTabs(board.numberOfMovesMade - numberOfMovesMadeBeforeSearch) + "< " + child + " = " + value);
            board.undoMoveWithCheck(child);
            if (value > score) {
                score = value;
                // keep track of best move found so far
                if (depth == currentMaxDepth && score > bestScore) {
//                    System.out.println("\tNew best move " + child + " score " + score);
                    bestMove = child;
                    bestScore = score;
                }
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                saveKillerMove(board);
                hhScore[child] += hhIncrement;
                break;
            } else {
                bfScore[child] += bfIncrement;
            }
        }

        // transposition table store
        if (useTT) {
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
            // replace if depth is greater
            if (tt.containsKey(board.hashKey)) {
                if (board.numberOfMovesMade >= tt.get(board.hashKey).depth) {
                    tt.put(board.hashKey, ttEntry);
                }
            } else {
                tt.put(board.hashKey, ttEntry);
            }
        }
        return score;
    }

    public void saveKillerMove(Board board) {
        int depth = board.numberOfMovesMade - numberOfMovesMadeBeforeSearch;
        if (killerMoves[depth] == null) {
            killerMoves[depth] = new ArrayList<Integer>();
        }
        killerMoves[depth].add(0, board.movesMade[board.numberOfMovesMade - 1]);
        if (numberOfKillerMoves > 0 && killerMoves[depth].size() > numberOfKillerMoves) {
            killerMoves[depth].remove(numberOfKillerMoves);
        }
    }

    public int quiescence(Board board, int depth, int alpha, int beta, int color, int currentMaxDepth) {
        int score = color * evaluate(board);
        if (score >= beta) {
            return score;
        }
        if (score > alpha) {
            alpha = score;
        }
        int[] moves = useMoveOrdering ? moveOrdering(currentMaxDepth, board, currentMaxDepth) : board.getAllowedMoves();
        for (int child : moves) {
            if (!wouldForceAMove(board, child)) {
                continue;
            }
            board.doMove(child);
            int result = -quiescence(board, depth, -beta, -alpha, -color, currentMaxDepth);
            board.undoMoveWithCheck(child);
            if (result > score) {
                score = result;
                if (result >= beta) {
                    break;
                }
                if (result > alpha) {
                    score = alpha;
                }
            }
        }
        return score;
    }

    public boolean wouldForceAMove(Board board, int i) {
        boolean wouldForceAMove = false;
        board.fields[i].piece = board.turn;

        for (RowOfFour rowOfFour : board.fields[i].rowsOfFour) {
            if (wouldForceAMoveInRow(board, board.turn, rowOfFour.fields)) {
                wouldForceAMove = true;
                break;
            }
        }
        board.fields[i].piece = Board.FREE;
        return wouldForceAMove;
    }

    // check quiescent moves, similar to computeAllowedMovesInRow in Board.java
    public boolean wouldForceAMoveInRow(Board board, int piece, Field[] fields) {
        // check if move i would lead to a forced move
        return  fields[0].piece == piece
                &&
                fields[3].piece == piece
                &&
                (
                    (fields[1].piece == piece && fields[2].piece == Board.FREE)
                    ||
                    (fields[2].piece == piece && fields[1].piece == Board.FREE)
                );
    }


    public int[] moveOrdering(int depth, Board board, int currentMaxDepth) {
        if (currentMaxDepth <= 1 || depth == 0) {
            return board.getAllowedMoves();
        }
        HashSet<Integer> allowedMoves = new HashSet<>(board.getAllowedMoveSet());
        ArrayList<Integer> orderedMoves = new ArrayList<>();
        if (useKillerMoves && killerMoves[depth] != null) {
            for (int killerMove : killerMoves[depth]) {
                if (allowedMoves.contains(killerMove)) {
                    orderedMoves.add(killerMove);
                    allowedMoves.remove(killerMove);
                }
            }
        }
        if (usePC && currentMaxDepth - 2 > 0 && currentMaxDepth - depth - 1 > 0) {
            int bestMoveForDepth = principalVariationMoves[currentMaxDepth - 2][currentMaxDepth - depth - 1];
            if (bestMoveForDepth != 0 && allowedMoves.contains(bestMoveForDepth)) {
                orderedMoves.add(bestMoveForDepth);
                allowedMoves.remove(bestMoveForDepth);
            }
        }
        if (useRelativeHistoryHeuristic) {
            int[] scores = new int[Board.NUMBER_OF_FIELDS];
            for (int i = 0; i < staticMoveOrder.length; i++) {
                if (bfScore[staticMoveOrder[i]] != 0) {
                    scores[i] = hhScore[staticMoveOrder[i]] / bfScore[staticMoveOrder[i]];
                } else {
                    scores[i] = 0;
                }
            }
            int[] sorted = sort(scores);
            for (int i = 0; i < sorted.length; i++) {
                if (allowedMoves.contains(sorted[i])) {
                    orderedMoves.add(sorted[i]);
                    allowedMoves.remove(sorted[i]);
                }
            }
        } else if (useMoveOrdering) {
            for (int move = 0; move < staticMoveOrder.length; move++) {
                if (allowedMoves.contains(move)) {
                    orderedMoves.add(move);
                    allowedMoves.remove(move);
                }
            }
        } else {
            orderedMoves.addAll(allowedMoves);
        }
        return Util.toIntArray(orderedMoves);
    }

    public int[] sort(int[] scores) {
        int[] sorted = new int[scores.length];
        for (int i = 0; i < scores.length; i++) {
            sorted[i] = staticMoveOrder[i];
        }
        boolean swapped = true;
        int n = scores.length;
        while (swapped) {
            swapped = false;
            for (int i = 1; i <= n-1; i++) {
                if (scores[i - 1] > scores[i]) {
                    int temp = scores[i];
                    scores[i] = scores[i - 1];
                    scores[i - 1] = temp;

                    temp = sorted[i];
                    sorted[i] = sorted[i - 1];
                    sorted[i - 1] = temp;
                    swapped = true;
                }
            }
        }
        return sorted;
    }

    /*
        procedure bubbleSort( A : list of sortable items )
    n = length(A)
    repeat
       swapped = false
       for i = 1 to n-1 inclusive do
          if A[i-1] > A[i] then
             swap(A[i-1], A[i])
             swapped = true
          end if
       end for
       n = n - 1
    until not swapped
end procedure
     */

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
                score += WIN_SCORE - board.numberOfMovesMade;
            } else {
                score += LOSS_SCORE + board.numberOfMovesMade;
            }
        } else {
            for (int i = 0; i < Board.NUMBER_OF_CELLS; i++) {
                // playing near the center gives a small bonus in the early game, because there is more freedom
                if (Board.CELLS[i] == 0) {
                    continue;
                } else if (board.fields[i].piece == piece) {
                    if (board.movesMade[0] == i) {
                        score -= FIRST_MOVE_DISTANCES[i] * DISTANCE_SCORE;
                    }
                    score += Board.DISTANCES[i] * DISTANCE_SCORE;
                }
                // check for triangle of size four > strong position
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
//                    if (rowOfFour.fields[0].position != i) {
//                        continue;
//                    }
//                    if (rowOfFour.fields[0].piece == piece
//                            && rowOfFour.fields[1].piece == Board.FREE
//                            && rowOfFour.fields[2].piece == piece
//                            && rowOfFour.fields[3].piece == Board.FREE) {
//                        score += CAN_FORCE_MOVE_SCORE;
//                    } else if (rowOfFour.fields[0].piece == opponentPiece
//                            && rowOfFour.fields[1].piece == Board.FREE
//                            && rowOfFour.fields[2].piece == opponentPiece
//                            && rowOfFour.fields[3].piece == Board.FREE) {
//                        score += CAN_BE_FORCED_TO_MOVE_PENALTY;
//                    }
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
        if (score < -10000 && board.numberOfMovesMade -  numberOfMovesMadeBeforeSearch >=5) {
            return score;
        }
        return score;
    }

    public long[] getTotalNodesVisited() {
        return totalNodesVisited;
    }

    public long[] getTotalTimeNeeded() {
        return totalTimeNeeded;
    }
}
