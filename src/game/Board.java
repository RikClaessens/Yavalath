package game;

import org.apache.commons.math3.random.MersenneTwister;
import players.Player;
import util.Util;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by rhmclaessens on 23-06-2014.
 */
public class Board {

    // byte array containing a 1 for fields that are part of the game fields
    public static final byte[] CELLS = {
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
    // distances to the edge of the fields
    public static final byte[] DISTANCES = {
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

    // number of entries in the byte array CELLS
    public static final int NUMBER_OF_CELLS = 81;
    // number of actual fields on the game fields
    public static final int NUMBER_OF_FIELDS = 61;
    // row major order used for checking in what row a field is
    public static final int ROW_MAJOR_ORDER = 9;
    // the actual fields, containing the pieces
    public Field[] fields = new Field[NUMBER_OF_CELLS];
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
    // in the CELLS array, second index is the difference in rows
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

    // zobrist hashKey of the fields
    public long hashKey;
    // random number table to compute the hash key
    private long[][] zobristNumbers;

    public Board() {
        initBoard();
    }

    // initializes an empty fields, assign neighboring fields for easy lookup
    public void initBoard() {
        // all participating players are alive at the start of the game
        numberOfPlayersAlive = 0;
        for (int i = 1; i <= numberOfPlayers; i++) {
            playersAlive[i] = true;
            numberOfPlayersAlive++;
        }
        // clear all the fields
        freeFields.clear();
        // initialize the fields with empty fields for each cell that is part of the fields
        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            if (CELLS[i] == 0) {
                continue;
            }
            fields[i] = new Field(i);
        }
        // computes the neighbors of each field on the fields for easy access
        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            if (CELLS[i] == 0) {
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
                // next or previous row, but on the opposite side of the fields
                if (row + rowOffset != row(i + indexOffset)) {
                    continue;
                }

                // if the field is also on the fields, then it is a valid neighbor
                if (isOnTheBoard(i + indexOffset)) {
                    fields[i].neighbors[j] = fields[i + indexOffset];
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
            movesMade = new int[NUMBER_OF_FIELDS];
            // create a new list of forced moves for each turn
            // there can never be more forced moves sets than there are empty fields on the fields
            forcedMovesList = new HashSet[NUMBER_OF_FIELDS + 1];
            // the list of forced moves for the first moves if empty
            forcedMovesList[numberOfMovesMade] = new HashSet<Integer>(NUMBER_OF_FIELDS);
            // clear the sets of forced moves by each player
            forcedMovesByWhite = new HashSet<Integer>(NUMBER_OF_FIELDS);
            forcedMovesByBlack = new HashSet<Integer>(NUMBER_OF_FIELDS);
        }

        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            if (CELLS[i] == 0) {
                continue;
            }
            // initialize rows of four, as these neighboring are used very often for doMove checking, win checks etc.
            // search in 3 directions to cover all rows of four: E, SE, SW
            int[] directionsToSearch = new int[]{neighborE, neighborSE, neighborSW};
            A: for (int directionToSearch : directionsToSearch) {
                // search 3 fields in each direction
                Field[] fieldsInRow = new Field[4];
                // add the current field to the row of four
                fieldsInRow[0] = fields[i];
                Field field = fields[i];
                for (int r = 1; r < 4; r++) {
                    Field neighbor = field.neighbors[directionToSearch];
                    // if the neighbor is not on the fields, then there is no row of four here
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

        // initialize the random numbers for the transposition table
        // Use a fixed seed to ensure the same hash for copies of the fields
        MersenneTwister mersenneTwister = new MersenneTwister(0);
        zobristNumbers = new long[NUMBER_OF_CELLS][2];
        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            zobristNumbers[i][WHITE - 1] = mersenneTwister.nextLong();
            zobristNumbers[i][BLACK - 1] = mersenneTwister.nextLong();
        }
    }

    public int row(int i) {
        return i / ROW_MAJOR_ORDER;
    }

    public int col(int i) {
        return i % ROW_MAJOR_ORDER;
    }

    public boolean isOnTheBoard(int i) {
        return i > 0 && i < CELLS.length && CELLS[i] == 1;
    }

    public void doMove(int i) {
        // check if the move is allowed
        if (!getAllowedMoveSet().contains(i)) {
//            System.err.println("Move " + i + " not allowed on fields\n" + this.toString());
            return;
        }

//        System.out.print(">" + i + " ");
        // if it is allowed, set the piece on the fields
        fields[i].piece = turn;
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

        // hash the zobristkey of this fields after the last played move
        // first check if the second player used the swap rule
        if (numberOfMovesMade == 2 && movesMade[0] == movesMade[1]) {
            // if so, unhash the position of the first move
            hashKey ^= zobristNumbers[movesMade[0]][WHITE - 1];
        }
        hashKey ^= zobristNumbers[i][turn - 1];
        // advance the turn to the next player
        advanceTurn();
    }

    public void printGameThusFar() {
        System.out.println("\n\tfm :\t" + printForcedMovesArray(forcedMovesList)
                + "\n\tfmw:\t" + printForcedMoves(forcedMovesByWhite)
                + "\n\tfmb:\t" + printForcedMoves(forcedMovesByBlack));
    }

    public String printForcedMovesArray(HashSet<Integer>[] sets) {
        StringBuilder s = new StringBuilder();
        int i = 0;
        for (HashSet<Integer> set : sets) {
            if (set == null) {
                break;
            }
            s.append(i + "|");
            s.append(" -" + movesMade[i] + "- |");
            s.append(printForcedMoves(set));
            s.append("\n\t\t\t");
            i++;
        }
        return s.toString();
    }

    public String printForcedMoves(HashSet<Integer> set) {
        StringBuilder s = new StringBuilder();
        Iterator<Integer> iterator = set.iterator();
        while (iterator.hasNext()) {
            s.append("[" + iterator.next() + "]");
        }
        return s.toString();
    }

    // undo's a move
    public boolean undoMoveWithCheck(int i) {
        // if there are no moves been made so far we can not go back further
        if (numberOfMovesMade == 0)
            return true;
        // lower the number of moves made
        numberOfMovesMade--;
        // the position last played
        int position = movesMade[numberOfMovesMade];
        // check if the right move is being undone
        // **** not necessary, but very useful for detecting bugs and illegal operations by an ai player
        if (position != i) {
            System.err.println("Trying to undo the wrong move " + position + " != " + i);
            return false;
        }
        int piece = fields[position].piece;
        // clear the move made from the list
        movesMade[numberOfMovesMade] = 0;

        // if the player died after the last move, bring him back to life!
        if (!playersAlive[piece]) {
            playersAlive[piece] = true;
            numberOfPlayersAlive++;
        }
        // check if a swap move is being undone
        if (numberOfMovesMade == 1 && movesMade[0] == i) {
            fields[position].piece = WHITE;
        } else {
            // set the position to free
            fields[position].piece = FREE;
            // add the field to the list of free fields
            freeFields.add(position);
        }

        // reset the list of forced moves
        // if we're undoing a WHITE move, the list of forced moves was forced by black
        forcedMovesByBlack.clear();
        forcedMovesByWhite.clear();
        if (piece == WHITE) {
            forcedMovesByBlack.addAll(forcedMovesList[numberOfMovesMade]);
        } else {
            forcedMovesByWhite.addAll(forcedMovesList[numberOfMovesMade]);
        }
        // clear the list of forced moves, forced by this move
        if (numberOfMovesMade + 1 < forcedMovesList.length) {
            forcedMovesList[numberOfMovesMade + 1].clear();
        }

        // un-hash the zobristkey of this fields after the last played move
        // this results in the same hash as before this move
        hashKey ^= zobristNumbers[i][turn - 1];

        rewindTurn();
        return true;
    }

    public boolean checkUndoMove(int i) {
        // if there are no moves been made so far we can not go back further
        if (numberOfMovesMade == 0)
            return true;
//        System.out.print("<" + i + " ");
        // check if the right move is being undone
        if (movesMade[numberOfMovesMade - 1] != i) {
            System.err.println("Trying to undo the wrong move " + movesMade[numberOfMovesMade - 1] + " != " + i);
            return false;
        }
        return true;
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

    // Computes the allowed moves on the fields after a piece has been placed at position i
    public void computeAllowedMoves(int i) {
        // save the played piece
        int piece = fields[i].piece;
        // remove the just played move from the forcedmoves lists
        forcedMovesList[numberOfMovesMade].remove(i);

        // check for lines of three, four and three with 1 in between in 3 directions > NW - SE, NE - SW, E - W
        for (RowOfFour rowOfFour : fields[i].rowsOfFour) {
            computeAllowedMovesInRow(piece, rowOfFour.fields);
        }
        if (piece == WHITE) {
            forcedMovesList[numberOfMovesMade].addAll(forcedMovesByWhite);
            forcedMovesByWhite.addAll(forcedMovesList[numberOfMovesMade]);
        } else {
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
        if (numberOfMovesMade == 1) {
            HashSet<Integer> moves = new HashSet<Integer>(freeFields);
            moves.add(movesMade[numberOfMovesMade - 1]);
            return moves;
        }
        return forcedMovesList[numberOfMovesMade] == null || forcedMovesList[numberOfMovesMade].size()  == 0 ? new HashSet<Integer>(freeFields) : new HashSet<Integer>(forcedMovesList[numberOfMovesMade]);
    }

    public int[] getAllowedMoves() {
        if (isGameOver()) {
            return new int[]{};
        }
        if (forcedMovesList[numberOfMovesMade].size() == 0) {
            if (numberOfMovesMade == 1) {
                HashSet<Integer> moves = new HashSet<Integer>(freeFields);
                moves.add(movesMade[numberOfMovesMade - 1]);
                return Util.toIntArray(moves);
            }
            return Util.toIntArray(freeFields);
        } else {
            return Util.toIntArray(forcedMovesList[numberOfMovesMade]);
        }
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
        for (int i = 0; i < numberOfMovesMade; i++) {
            copy.doMove(movesMade[i]);
        }
//        System.out.println("[" + movesMade[numberOfMovesMade - 1] + "] ");
        return copy;
    }

    public boolean isHumanMove() {
        return players[turn] == null;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < NUMBER_OF_CELLS; i++) {
            if (CELLS[i] == 0) {
                stringBuffer.append(" ");
            } else {
                stringBuffer.append(Util.piecePlayerLabel(fields[i].piece));
            }
            stringBuffer.append(" ");
            if ((i + 1) % ROW_MAJOR_ORDER == 0) {
                stringBuffer.append("\n");
                if (row(i + 1) % 2 == 1) {
                    stringBuffer.append(" ");
                }
            }
        }
        return stringBuffer.toString();
    }
}
