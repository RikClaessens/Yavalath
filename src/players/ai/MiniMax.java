package players.ai;

import game.Board;
import game.RowOfFour;
import players.Player;
import players.PlayerSettings;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class MiniMax implements Player {

    private int piece;
    private int opponentPiece;
    private int maxDepth = 4;
    private int numberOfMovesMadeBeforeSearch;
    private int bestMove = -1;

    // Scores
    private static int WINSCORE = Integer.MAX_VALUE - 1000;
    private static int forcedMovesScore = 500;
    private static int forcedMovesPenalty = 800;
    private static int canForceMoveScore = 200;
    private static int canBeForcedToMoveScore = 450;

    public MiniMax(PlayerSettings playerSettings) {
        this.piece = playerSettings.piece;
        this.opponentPiece = playerSettings.getOpponentPiece(piece);
        this.maxDepth = playerSettings.maxDepth;
    }

    @Override
    public int doMove(Board board) {
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        int score = negamax(board, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
//        System.out.println("Score = " + score + " best move = " + bestMove);
        return bestMove;
    }

    public int negamax(Board board, int depth, double alpha, double beta, int color) {
        if (board.isGameOver() || depth == 0) {
            return color * evaluate(board);
        }
        int score = Integer.MIN_VALUE;
        int[] moves = board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int value = -negamax(board, depth - 1, -beta, -alpha, -color);
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
//            System.out.println("Found a " + type + " after " + (fields.numberOfMovesMade - numberOfMovesMadeBeforeSearch) + " moves. Score: " + score + " Moves: " + printMoves(fields));
        } else {
            for (int i = 0; i < board.NUMBER_OF_CELLS; i++) {
                if (board.CELLS[i] == 0) {
                    continue;
                } else if (board.fields[i].piece == piece) {
                    score += board.DISTANCES[i];
                }
                for (RowOfFour rowOfFour : board.fields[i].rowsOfFour) {
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
//        if (fields.turn == piece) {
//            score *= -1;
//        }
//        System.out.println("\t\t" + score + "\t" + printMoves(fields));
        return score;
    }

    @Override
    public boolean isHuman() {
        return false;
    }
}
