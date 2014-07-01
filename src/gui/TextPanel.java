package gui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class TextPanel extends JPanel {

    /**
     * Create the panel.
     */

    private static JTextArea txtArea;
    public TextPanel() {
        txtArea = new JTextArea();
        txtArea.setRows(26);
        txtArea.setColumns(20);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        txtArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) txtArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scroll = new JScrollPane(txtArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scroll);
    }

    public static void logMove(int turn, int player, String cellLabel, boolean undo) {
        log((undo ? "Undo turn: " : "Turn: ") + (undo ? turn - 1 : turn) + "\tP" + player + ": " + cellLabel);
    }

    public static void log(String log) {
        txtArea.append(log + "\n");
    }

    public static void splitLine() {
        log("_________________________________");
    }

    public static void logWin(int player) {
        log("Player " + player + " won!");
    }
}
