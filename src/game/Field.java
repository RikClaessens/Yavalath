package game;

import java.util.ArrayList;

/**
 * Created by rhmclaessens on 30-06-2014.
 */
public class Field {
    public int piece = Board.FREE;
    public int position;
    public Field[] neighbors = new Field[6];
    public ArrayList<RowOfFour> rowsOfFour;

    public Field(int position) {
        this.position = position;
        this.rowsOfFour = new ArrayList<RowOfFour>();
    }

    public void addRowOfFour(RowOfFour rowOfFour) {
        this.rowsOfFour.add(rowOfFour);
    }

    public String toString() {
        String p = piece == 0 ? "." : (piece == 1 ? "W" : "B");
        return "{" + position + "}[" + p + "]";
    }

}


