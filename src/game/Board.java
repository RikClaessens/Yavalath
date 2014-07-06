package game;

import players.Player;

import java.util.HashSet;

/**
 * Created by rhmclaessens on 23-06-2014.
 */
public class Board {

    // byte array containing a 1 for fields that are part of the game board
    public static final byte[] cells = {
            0,0,1,1,1,1,1,0,0,
            0,1,1,1,1,1,1,0,0,
            0,1,1,1,1,1,1,1,0,
            1,1,1,1,1,1,1,1,0,
            1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,0,
            0,1,1,1,1,1,1,1,0,
            0,1,1,1,1,1,1,0,0,
            0,0,1,1,1,1,1,0,0,
    };
    // distances to the edge of the board
    public static final byte[] distances = {
            0,0,0,0,0,0,0,0,0,
            0,0,1,1,1,1,0,0,0,
            0,0,1,2,2,2,1,0,0,
            0,1,2,3,3,2,1,0,0,
            0,1,2,3,4,3,2,1,0,
            0,1,2,3,3,2,1,0,0,
            0,0,1,2,2,2,1,0,0,
            0,0,1,1,1,1,0,0,0,
            0,0,0,0,0,0,0,0,0,
    };

    // number of entries in the byte array cells
    public static final int numberOfCells = 81;
    // number of actual fields on the game board
    public static final int numberOfFields = 61;
    // row major order used for checking in what row a field is
    public static final int rowMajorOrder = 9;
    // the actual board, containing the pieces
    public Field[] board = new Field[numberOfCells];
    // colors of the players, p1 = white, p2 = black, p3 = red, 0 means a free cell
    public static final int FREE = 0, WHITE = 1, BLACK = 2, RED = 3;
    // turn of the game
    public int turn;
    // number of players in the game, range [2,3]
    public int numberOfPlayers = 2;
    // counter and list of players still in the game
    public int numberOfPlayersAlive;
    public boolean[] playersAlive = new boolean[]{false, false, false, false};
    // flag for game over
    public int gameWon = FREE;
    // players
    Player[] players;

    // array for easily construction of neighboring fields, first index is the difference
    // in the cells array, second index is the difference in rows
    private final int[][]
            neighborsInOddRow   = {{ -9, -1}, {-8, -1}, {1, 0}, {10, 1}, {9, 1}, {-1, 0}},
            neighborsInEvenRow  = {{-10, -1}, {-9, -1}, {1, 0}, { 9, 1}, {8, 1}, {-1, 0}};
    // easy lookup for neighbors in specific direction
    public static final int neighborNW = 0, neighborNE = 1, neighborE = 2, neighborSE = 3, neighborSW = 4, neighborW = 5;
    public static final int[] neighborDirections = new int[]{neighborNW, neighborNE, neighborE, neighborSE, neighborSW, neighborW};
    // number used for checking the opposite direction, opposite direction = (direction + oppositeDirection) % numberOfDirections
    public static final int oppositeDirection = 3;
    public static final int numberOfDirections = 6;

    // number of moves that have been made so far
    public int numberOfMovesMade = 0;
    // list of moves that have been made so far
    public int[] movesMade;
    // list of forced moves for each turn
    public HashSet<Integer>[] forcedMovesList;
    // list of forced moves by each player
    public HashSet<Integer> forcedMovesByWhite;
    public HashSet<Integer> forcedMovesByBlack;

    public Board() {
        initBoard();
    }

    // initializes an empty board, assign neighboring fields for easy lookup
    public void initBoard() {
        // all participating players are alive at the start of the game
        numberOfPlayersAlive = 0;
        for (int i = 1; i <= numberOfPlayers; i++) {
            playersAlive[i] = true;
            numberOfPlayersAlive++;
        }
        // clear all the fields
        freeFields.clear();
        // initialize the board with empty fields for each cell that is part of the board
        for (int i = 0; i < numberOfCells; i++) {
            if (cells[i] == 0) {
                continue;
            }
            board[i] = new Field(i);
        }
        // computes the neighbors of each field on the board for easy access
        for (int i = 0; i < numberOfCells; i++) {
            if (cells[i] == 0) {
                continue;
            }
            // lookup array for the neighboring fields
            int row = row(i);
            int [][] neighborsInRow = (row) % 2 == 0 ? neighborsInEvenRow : neighborsInOddRow;
            for (int j = 0; j < neighborsInRow.length; j++) {
                int indexOffset = neighborsInRow[j][0];
                int rowOffset = neighborsInRow[j][1];

                // check if the indexOffset ends up in the right row
                // in fact this is checking that no neighbors are assigned that would be on the
                // next or previous row, but on the opposite side of the board
                if (row + rowOffset != row(i + indexOffset)) {
                    continue;
                }

                // if the field is also on the board, then it is a valid neighbor
                if (isOnTheBoard(i + indexOffset)) {
                    board[i].neighbors[j] = board[i + indexOffset];
                }
            }
            // add the index to the list of allowed moves
            freeFields.add(i);

            // reset a number of variables
            // WHITE begins and BLACK wins!
            turn = WHITE;
            // nobody won the game if we just started
            gameWon = FREE;
            // player list is an empty list, index 0 is not used, 1, 2 & 3 stand for WHITE, BLACK, RED
            players = new Player[4];
            // the number of moves made thus far is 0
            numberOfMovesMade = 0;
            // clear the list of moves made thus far
            movesMade = new int[numberOfFields];
            // create a new list of forced moves for each turn
            // there can never be more forced moves sets than there are empty fields on the board
            forcedMovesList = new HashSet[numberOfFields];
            // the list of forced moves for the first moves if empty
            forcedMovesList[numberOfMovesMade] = new HashSet<Integer>(numberOfFields);
            // clear the sets of forced moves by each player
            forcedMovesByWhite = new HashSet<Integer>(numberOfFields);
            forcedMovesByBlack = new HashSet<Integer>(numberOfFields);
        }

        for (int i = 0; i < numberOfCells; i++) {
            if (cells[i] == 0) {
                continue;
            }
            // initialize rows of four, as these neighboring are used very often for doMove checking, win checks etc.
            // search in 3 directions to cover all rows of four: E, SE, SW
            int[] directionsToSearch = new int[]{neighborE, neighborSE, neighborSW};
            A: for (int directionToSearch : directionsToSearch) {
                // search 3 fields in each direction
                Field[] fieldsInRow = new Field[4];
                // add the current field to the row of four
                fieldsInRow[0] = board[i];
                Field field = board[i];
                for (int r = 1; r < 4; r++) {
                    Field neighbor = field.neighbors[directionToSearch];
                    // if the neighbor is not on the board, then there is no row of four here
                    if (neighbor == null) {
                        continue A;
                    } else {
                        fieldsInRow[r] = neighbor;
                    }
                    field = neighbor;
                }
                new RowOfFour(fieldsInRow);
            }
        }
    }

    public int row(int i) {
        return i / rowMajorOrder;
    }

    public int col(int i) {
        return i % rowMajorOrder;
    }

    public boolean isOnTheBoard(int i) {
        return i > 0 && i < cells.length && cells[i] == 1;
    }

    public void doMove(int i) {
        // check if the move is allowed
        if (!getAllowedMoveSet().contains(i)) {
            System.err.println("Move " + i + " not allowed on board\n" + this.toString());
            return;
        }

        // if it is allowed, set the piece on the board
        board[i].piece = turn;
        // save it in the list of moves made so far
        movesMade[numberOfMovesMade] = i;
        // increase the counter of number of moves made
        numberOfMovesMade++;
        // for quickly undoing move, we save the forced moves in a list as well
        // NOTE, these contain the forced moves for THIS turn
        // thus, if white just moved, the list of forced moves he has, were moves forced by black and vice versa

        // the list of forced moves after placing a piece at position i is initialized
        forcedMovesList[numberOfMovesMade] = new HashSet<Integer>();
        // position i is removed from the list of forced moves by each player
        forcedMovesByWhite.remove(i);
        forcedMovesByBlack.remove(i);

        // remove the field on which the piece was put on from the free field list
        freeFields.remove(i);
        // compute the allowed moves for the next turn
        computeAllowedMoves(i);
        // advance the turn to the next player
        advanceTurn();
    }

//    public void undoMove() {
//        if (numberOfMovesMade == 0)
//            return;
//        numberOfMovesMade--;
//        int position = movesMade[numberOfMovesMade];
//        board[position].piece = FREE;
//        freeFields.add(position);
//        forcedMoves = forcedMovesList[numberOfMovesMade];
//        rewindTurn();
//    }

    // undo's a move
    public void undoMoveWithCheck(int i) {
        // if there are no moves been made so far we can not go back further
        if (numberOfMovesMade == 0)
            return;
        // check if the right move is being undone
        if (movesMade[numberOfMovesMade - 1] != i) {
            System.err.println("Trying to undo the wrong move " + movesMade[numberOfMovesMade - 1] + " != " + i);
        }
        // lower the number of moves made
        numberOfMovesMade--;
        // the position last played
        int position = movesMade[numberOfMovesMade];
        int piece = board[position].piece;

        // if the player died after the last move, bring him back to life!
        if (!playersAlive[piece]) {
            playersAlive[piece] = true;
            numberOfPlayersAlive++;
        }
        // set the position to free
        board[position].piece = FREE;
        // add the field to the list of free fields
        freeFields.add(position);

        // reset the list of forced moves
        // if we're undoing a WHITE move, the list of forced moves was forced by black
        if (piece == WHITE) {
            forcedMovesByBlack = forcedMovesList[numberOfMovesMade];
        } else {
            forcedMovesByWhite = forcedMovesList[numberOfMovesMade];
        }

        rewindTurn();
    }

    public void doTurn() {
        if (players[turn] != null) {
            doMove(players[turn].doMove(this.copy()));
        }
    }

    public void rewindTurn() {
        turn = turn - 1;
        if (turn == 0) {
            turn = numberOfPlayers;
        }
        gameWon = FREE;
    }

    public void advanceTurn() {
        turn = turn % numberOfPlayers + 1;
        if (!playersAlive[turn]) {
            turn = turn % numberOfPlayers + 1;
            if (!playersAlive[turn]) {
                System.err.println("Game is over");
                return;
            }
        }
    }

    private HashSet<Integer> freeFields = new HashSet<Integer>();

    // Computes the allowed moves on the board after a piece has been placed at position i
    public void computeAllowedMoves(int i) {
        // save the played piece
        int piece = board[i].piece;
        // remove the just played move from the forcedmoves lists
        forcedMovesList[numberOfMovesMade].remove(i);

        // check for lines of three, four and three with 1 in between in 3 directions > NW - SE, NE - SW, E - W
        for (RowOfFour rowOfFour : board[i].rowsOfFour) {
            computeAllowedMovesInRow(piece, rowOfFour.fields);
        }
        if (piece == WHITE) {
            if (forcedMovesByWhite.size() > 0) {
                System.out.println("There were still " + forcedMovesByWhite.size() + " moves forced by white remaining");
            }
            forcedMovesList[numberOfMovesMade].addAll(forcedMovesByWhite);
            forcedMovesByWhite.addAll(forcedMovesList[numberOfMovesMade]);
        } else {
            if (forcedMovesByBlack.size() > 0) {
                System.out.println("There were still " + forcedMovesByBlack.size() + " moves forced by black remaining");
            }
            forcedMovesList[numberOfMovesMade].addAll(forcedMovesByBlack);
            forcedMovesByBlack.addAll(forcedMovesList[numberOfMovesMade]);
        }
    }

    // computes the allowed moves in a row of four
    // there are possibilities
    // 1] there is a line of 3, but not of 4 > player is dead
    // 2] there is a line of 4 > player wins
    // 3] player made a configuration of xx.x or x.xx > player forces a move
    public void computeAllowedMovesInRow(int piece, Field[] fields) {
        // check for line of 3 and 4
        // first check for lines of 3

        // check if .xx. is the case, if not there is not line of 3 or 4
        if (fields[1].piece == piece
                && fields[2].piece == piece
                ) {
            // if we have .xx., check if xxxx is the case
            if (fields[0].piece == piece && fields[3].piece == piece) {
                // line of 4 found
                gameWon(piece);
            } else if (fields[0].piece == piece || fields[3].piece == piece) {
                // no line of 4 found, but either xxx. is the case or .xxx
                // line of 3 found
                killPlayer(piece);
            }
        }

        if (fields[0].piece == piece
                && fields[1].piece == piece
                && fields[2].piece == piece
                ) {
            // line of 3 found
            if (fields[3].piece == piece) {
                // line of 4 found
                gameWon(piece);
            } else {
                // line of 3 found, but no line of 4
                killPlayer(piece);
            }
            return;
        }
        // here we know there are no lines of three or four, but we still need to check for forced moves
        if (fields[0].piece == piece && fields[3].piece == piece) {
            // 2 cases: xx.x or x.xx, where x is a piece of the same color and . is a free field
            if (fields[1].piece == piece && fields[2].piece == FREE) {
                forcedMovesList[numberOfMovesMade].add(fields[2].position);
            } else if (fields[2].piece == piece && fields[1].piece == FREE) {
                forcedMovesList[numberOfMovesMade].add(fields[1].position);
            }
        }
    }

    // kills the player
    public void killPlayer(int piece) {
        if (isGameOver()) {
            return;
        }
        if (playersAlive[piece]) {
            playersAlive[piece] = false;
            numberOfPlayersAlive--;
            if (numberOfPlayersAlive == 1) {
                gameWon();
            }
        }
    }

    public void gameWon(int piece) {
        // kill the other players
        killPlayer((piece % 3) + 1);
        killPlayer(((piece + 1) % 3) + 1);
        gameWon();
    }

    public void gameWon() {
        if (playersAlive[WHITE]) {
            gameWon = WHITE;
        } else if (playersAlive[BLACK]) {
            gameWon = BLACK;
        } else if (playersAlive[RED]) {
            gameWon = RED;
        }
    }

    public boolean isGameOver() {
        return gameWon != FREE;
    }

    public HashSet<Integer> getAllowedMoveSet() {
        if (isGameOver()) {
            return new HashSet<Integer>();
        }
        return forcedMovesList[numberOfMovesMade] == null || forcedMovesList[numberOfMovesMade].size()  == 0 ? freeFields : forcedMovesList[numberOfMovesMade];
    }

    public int[] getAllowedMoves() {
        if (isGameOver()) {
            return new int[]{};
        }
        if (forcedMovesList[numberOfMovesMade].size() == 0) {
            return toIntArray(freeFields);
        } else {
            return toIntArray(forcedMovesList[numberOfMovesMade]);
        }
    }

    public int[] toIntArray(HashSet<Integer> set) {
        int[] a = new int[set.size()];
        int i = 0;
        for (Integer val : set) a[i++] = val;
        return a;
    }

    public void setPlayer(int piece, Player player) {
        if (piece == WHITE) {
            players[WHITE] = player;
        } else if (piece == BLACK) {
            players[BLACK] = player;
        }
    }

    public Board copy() {
        Board copy = new Board();
        if (numberOfMovesMade == 0) {
            return copy;
        }
        for (int i = 0; i < numberOfMovesMade - 1; i++) {
            System.out.print("[" + movesMade[i] + "] ");
            copy.board[movesMade[i]].piece = board[movesMade[i]].piece;
            copy.freeFields.remove(movesMade[i]);
            copy.numberOfMovesMade++;
            copy.advanceTurn();
        }
        copy.doMove(movesMade[numberOfMovesMade - 1]);
        System.out.println("[" + movesMade[numberOfMovesMade - 1] + "] ");
        return copy;
    }

    public boolean isHumanMove() {
        return players[turn] == null;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < numberOfCells; i++) {
            if (cells[i] == 0) {
                stringBuffer.append(".");
            } else {
                stringBuffer.append(board[i].piece);
            }
            if ((i + 1) % rowMajorOrder == 0) {
                stringBuffer.append("\n");
            }
        }
        return stringBuffer.toString();
    }
}
