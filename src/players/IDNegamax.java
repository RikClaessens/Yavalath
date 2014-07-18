package players;

import game.Board;
import game.RowOfFour;
import util.Util;

import java.util.HashSet;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class IDNegamax implements Player {

    private int piece;
    private int opponentPiece;
    private int globalMaxDepth = 4;
    private int numberOfMovesMadeBeforeSearch;
    private int bestMove = -1;

    private int nodesVisited;

    // Scores
    private static int WINSCORE = Integer.MAX_VALUE - 1000;
    private static int forcedMovesScore = 500;
    private static int forcedMovesPenalty = 800;
    private static int canForceMoveScore = 200;
    private static int canBeForcedToMoveScore = 450;

    // Iterative Depth related variables
    private int[][] principalVariation;
    private boolean orderMoves;
    private int bestPVScore;

    public IDNegamax(int piece, int globalMaxDepth, boolean orderMoves) {
        this.piece = piece;
        this.opponentPiece = piece == Board.WHITE ? Board.BLACK : Board.WHITE;
        this.globalMaxDepth = globalMaxDepth;
        this.orderMoves = orderMoves;
    }

    @Override
    public int doMove(Board board) {
        nodesVisited = 0;
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        principalVariation = new int[globalMaxDepth][globalMaxDepth];

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
        if (board.isGameOver() || depth == 0) {
            int value = color * evaluate(board);
            if (value > bestPVScore && depth == 0) {
//                System.out.println("Saving a new PV");
                System.arraycopy(board.movesMade, numberOfMovesMadeBeforeSearch, principalVariation[currentMaxDepth - 1], 0, board.numberOfMovesMade - numberOfMovesMadeBeforeSearch);
                bestPVScore = value;
            }
            return value;
        }
        int score = Integer.MIN_VALUE;
        int[] moves = orderMoves ? orderPVMoves(currentMaxDepth - depth, board, currentMaxDepth) : board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int value = -negamax(board, depth - 1, -beta, -alpha, -color, currentMaxDepth);
            board.undoMoveWithCheck(child);
            if (value > score) {
                score = value;
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
            for (int i = 0; i < Board.numberOfCells; i++) {
                if (Board.cells[i] == 0) {
                    continue;
                } else if (board.board[i].piece == piece) {
                    score += Board.distances[i];
                }
                for (RowOfFour rowOfFour : board.board[i].rowsOfFour) {
                    if (rowOfFour.fields[0].position != i) {
                        continue;
                    }
                    if (rowOfFour.fields[0].piece == piece
                            && rowOfFour.fields[1].piece == Board.FREE
                            && rowOfFour.fields[2].piece == Board.FREE
                            && rowOfFour.fields[3].piece == piece) {
                        score += canForceMoveScore;
                    } else if (rowOfFour.fields[0].piece == opponentPiece
                            && rowOfFour.fields[1].piece == Board.FREE
                            && rowOfFour.fields[2].piece == Board.FREE
                            && rowOfFour.fields[3].piece == opponentPiece) {
                        score -= canBeForcedToMoveScore;
                    }
                }
            }
            if (piece == Board.WHITE) {
                score += board.forcedMovesByWhite.size() * forcedMovesScore;
            } else {
                score -= board.forcedMovesByBlack.size() * forcedMovesPenalty;
            }
        }
        return score;
    }
}
