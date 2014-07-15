package players;

import game.Board;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class MiniMax implements Player {

    private int piece;
    private static int WINSCORE = Integer.MAX_VALUE - 1000;
    private int maxDepth = 3;
    private int numberOfMovesMadeBeforeSearch;

    // Scores
    private int forcedMovesScore = 500;
    private int forcedMovesPenalty = 400;

    public MiniMax(int piece) {
        this.piece = piece;
    }

    @Override
    public int doMove(Board board) {
        int[] moves = board.getAllowedMoves();
//        System.out.println("Got " + moves.length + " moves on board\n" + board.toString());

        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
//        for (int move : moves) {
//            System.out.print(move + " ");
//        }
        System.out.println("\n================================");
        numberOfMovesMadeBeforeSearch = board.numberOfMovesMade;
        for (int move : moves) {
//            System.out.print("next");
            board.doMove(move);
            System.out.print("\tmove " + move);
            int score = negamax(board, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
            System.out.println(" got score  " + score);
            board.undoMoveWithCheck(move);
            if (score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }
        System.out.println("playing move " + bestMove + " with score " + bestScore);
        return bestMove;
    }

    public int negamax(Board board, int depth, double alpha, double beta) {
        if (board.isGameOver() || depth == 0) {
            return evaluate(board);
        }
        int score = Integer.MIN_VALUE;
        int[] moves = board.getAllowedMoves();
        for (int child : moves) {
            board.doMove(child);
            int value = -negamax(board, depth - 1, -beta, -alpha);
            board.undoMoveWithCheck(child);

            if (value > score) {
                score = value;
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
        int firstMove = 0;
        while (board.movesMade[firstMove] == 0) {
            firstMove++;
        }
        firstMove++;
        firstMove++;
        for (int i = firstMove; i < board.numberOfMovesMade; i++) {
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
                score = -WINSCORE - board.numberOfMovesMade;
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
            }
            if (piece == board.WHITE) {
                score += board.forcedMovesByWhite.size() * forcedMovesScore;
            } else {
                score -= board.forcedMovesByBlack.size() * forcedMovesPenalty;
            }
        }
        if (board.turn == piece) {
            score *= -1;
        }
        return score;
    }
}
