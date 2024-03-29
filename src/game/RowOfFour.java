package game;

/**
 * Created by rhmclaessens on 01-07-2014.
 */
public class RowOfFour {
    public Field[] fields;

    public RowOfFour(Field[] fields) {
        this.fields = fields;
        for (Field field : fields) {
            field.addRowOfFour(this);
        }
    }

    public String toString() {
        String s = "";
        for (Field field : fields) {
            s += field.toString();
        }
        return s;
    }
}
