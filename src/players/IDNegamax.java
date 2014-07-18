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
    private int maxDepth = 4;
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
    private int[] bestMovesPerDepth;
    private boolean orderMoves;

    public IDNegamax(int piece, int maxDepth, boolean orderMoves) {
        this.piece = piece;
        this.opponentPiece = piece == Board.WHITE ? Board.BLACK : Board.WHITE;
        this.maxDepth = maxDepth;
        this.orderMoves = orderMoves;
    }

    @Override
    public int doMove(Board board) {
        nodesVisited = 0;
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        bestMovesPerDepth = new int[maxDepth];
        int idBestMove = -1;
        for (int searchDepth = 1; searchDepth <= maxDepth; searchDepth++) {
            int score = negamax(board, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
            bestMovesPerDepth[searchDepth - 1] = bestMove;
            idBestMove = bestMove;
            System.out.println("Search depth [" + searchDepth + "], best move: " + idBestMove);
        }
//        System.out.println("Score = " + score + " best move = " + bestMove);
        System.out.println("Visited " + nodesVisited + " nodes");

        return idBestMove;
    }

    public int negamax(Board board, int depth, double alpha, double beta, int color) {
        nodesVisited++;
        if (board.isGameOver() || depth == 0) {
            return color * evaluate(board);
        }
        int score = Integer.MIN_VALUE;
        int[] moves = orderMoves ? tryIDMoveFirst(maxDepth - depth, board) : board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int value = -negamax(board, depth - 1, -beta, -alpha, -color);
            boolean undoWasOk = board.checkUndoMove(child);

            if (!undoWasOk) {
                System.out.println("Uh Oh!");
            }
            board.undoMoveWithCheck(child);
            if (value > score) {
                score = value;
                if (depth == maxDepth) {
//                    System.out.println("New best move " + child);
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

    public int[] tryIDMoveFirst(int depth, Board board) {
        HashSet<Integer> allowedMoves = board.getAllowedMoveSet();
        int bestMoveForDepth = bestMovesPerDepth[depth];
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
            String type = "";
            if (board.gameWon == piece) {
                score = WINSCORE - board.numberOfMovesMade;
                type = "win";
            } else {
                score = -WINSCORE + board.numberOfMovesMade;
                type = "loss";
            }
//            System.out.println("Found a " + type + " after " + (board.numberOfMovesMade - numberOfMovesMadeBeforeSearch) + " moves. Score: " + score + " Moves: " + printMoves(board));
        } else {
            for (int i = 0; i < board.numberOfCells; i++) {
                if (board.cells[i] == 0) {
                    continue;
                } else if (board.board[i].piece == piece) {
                    score += board.distances[i];
                }
                for (RowOfFour rowOfFour : board.board[i].rowsOfFour) {
                    if (rowOfFour.fields[0].position != i) {
                        continue;
                    }
                    if (rowOfFour.fields[0].piece == piece
                            && rowOfFour.fields[1].piece == board.FREE
                            && rowOfFour.fields[2].piece == board.FREE
                            && rowOfFour.fields[3].piece == piece) {
                        score += canForceMoveScore;
                    } else if (rowOfFour.fields[0].piece == opponentPiece
                            && rowOfFour.fields[1].piece == board.FREE
                            && rowOfFour.fields[2].piece == board.FREE
                            && rowOfFour.fields[3].piece == opponentPiece) {
                        score -= canBeForcedToMoveScore;
                    }
                }
            }
            if (piece == board.WHITE) {
                score += board.forcedMovesByWhite.size() * forcedMovesScore;
            } else {
                score -= board.forcedMovesByBlack.size() * forcedMovesPenalty;
            }
        }
//        if (board.turn == piece) {
//            score *= -1;
//        }
//        System.out.println("\t\t" + score + "\t" + printMoves(board));
        return score;
    }
}
