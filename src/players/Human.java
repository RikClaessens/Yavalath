package players;

import game.Board;

/**
 * Created by rikclaessens on 20/07/14.
 */
public class Human implements Player {


    @Override
    public int doMove(Board board) {
        return 0;
    }

    @Override
    public boolean isHuman() {
        return true;
    }
}
