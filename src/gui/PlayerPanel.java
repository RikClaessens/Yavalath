package gui;

import players.Human;
import players.Player;
import players.PlayerSettings;
import players.ai.AIPlayer;
import util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by rikclaessens on 20/07/14.
 */
public class PlayerPanel extends JPanel {

    private String[] players = new String[]{"Human", "AIPlayer"};
    private static int[] DEF_PLAYERS = new int[]{0, 1};
    private JComboBox comboPlayerList;
    private JCheckBox checkTT;
    private JCheckBox checkMoveOrdering;
    private JCheckBox checkPVS;
    private JCheckBox checkNullMove;
    private JCheckBox checkQuiescence;
    private JCheckBox checkKillerMoves;
    private JCheckBox checkRelativeHistoryHeuristic;
    private JCheckBox checkAspirationSearch;
    private JSpinner spinnerMaxDepth;
    private JSpinner spinnerNullMoveR;
    private JSpinner spinnerKillerMoves;
    private JSpinner spinnerAspirationWindow;
    private static int DEF_MAX_DEPTH = 5;
    private static int DEF_NULL_MOVE_R = 2;
    private static int DEF_KILLER_MOVES= 3;
    private static int DEF_ASPIRATION_WINDOW = 500;
    private static boolean DEF_USE_NULL_MOVE = false;
    private static boolean DEF_USE_QUIESCENE = true;
    private static boolean DEF_USE_KILLER_MOVES = true;
    private static boolean DEF_USE_PVS = true;
    private static boolean DEF_USE_TT = true;
    private static boolean DEF_USE_MOVE_ORDERING = true;
    private static boolean DEF_USE_RELATIVE_HISTORY_HEURISTIC = true;
    private static boolean DEF_USE_ASPIRATION_SEARCH = true;
    private int piece;

    public PlayerPanel(int piece) {
        this.piece = piece;
        setLayout(new GridLayout(0, 2));
        setBorder(new TitledBorder(Util.piecePlayer(piece)));

        add(new JLabel("AI"));
        comboPlayerList = new JComboBox(players);
        add(comboPlayerList);
        comboPlayerList.setSelectedIndex(DEF_PLAYERS[piece - 1]);

        SpinnerModel maxDepthSpinnerModel = new SpinnerNumberModel(DEF_MAX_DEPTH, 1, 100, 1);
        spinnerMaxDepth = new JSpinner(maxDepthSpinnerModel);
        add(new JLabel("Max Depth"));
        add(spinnerMaxDepth);
        ((JSpinner.DefaultEditor) spinnerMaxDepth.getEditor()).getTextField().setEditable(false);

        checkTT = createCheckBox("TT", DEF_USE_TT);
        add(checkTT);

        checkMoveOrdering = createCheckBox("Move Ordering", DEF_USE_MOVE_ORDERING);
        add(checkMoveOrdering);

        checkPVS = createCheckBox("PVS", DEF_USE_PVS);
        add(checkPVS);

        checkQuiescence = createCheckBox("Quiescence", DEF_USE_QUIESCENE);
        add(checkQuiescence);

        checkNullMove = createCheckBox("Null move", DEF_USE_NULL_MOVE);
        add(checkNullMove);

        SpinnerModel nullMoveRSpinnerModel = new SpinnerNumberModel(DEF_NULL_MOVE_R, 0, 5, 1);
        spinnerNullMoveR = new JSpinner(nullMoveRSpinnerModel);
        add(new JLabel("Null Move R"));
        add(spinnerNullMoveR);
        ((JSpinner.DefaultEditor) spinnerNullMoveR.getEditor()).getTextField().setEditable(false);

        checkKillerMoves = createCheckBox("Killer Moves", DEF_USE_KILLER_MOVES);
        add(checkKillerMoves);

        SpinnerModel killerMoveSpinnerModel = new SpinnerNumberModel(DEF_KILLER_MOVES, 1, 60, 1);
        spinnerKillerMoves = new JSpinner(killerMoveSpinnerModel);
        add(new JLabel("# of Killer Moves"));
        add(spinnerKillerMoves);
        ((JSpinner.DefaultEditor) spinnerKillerMoves.getEditor()).getTextField().setEditable(false);

        checkRelativeHistoryHeuristic = createCheckBox("Rel. His. Heuristic", DEF_USE_RELATIVE_HISTORY_HEURISTIC);
        add(checkRelativeHistoryHeuristic);

        checkAspirationSearch = createCheckBox("Aspiration Search", DEF_USE_ASPIRATION_SEARCH);
        add(checkAspirationSearch);

        SpinnerModel aspirationWindowSpinnerModel = new SpinnerNumberModel(DEF_ASPIRATION_WINDOW, 1, 1000000, 1);
        spinnerAspirationWindow = new JSpinner(aspirationWindowSpinnerModel);
        add(new JLabel("Aspiration Window"));
        add(spinnerAspirationWindow);
        ((JSpinner.DefaultEditor) spinnerAspirationWindow.getEditor()).getTextField().setEditable(true);
    }

    public JCheckBox createCheckBox(String label, boolean defaultValue) {
        add(new JLabel(label));
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(defaultValue);
        return checkBox;
    }

    public Player getPlayer() {
        Player player;
        if (comboPlayerList.getSelectedIndex() == 0) {
            player = new Human();
        } else {
            PlayerSettings playerSettings = new PlayerSettings();
            playerSettings.piece = piece;
            playerSettings.useTT = checkTT.isSelected();
            playerSettings.usePVS = checkPVS.isSelected();
            playerSettings.useMoveOrdering = checkMoveOrdering.isSelected();
            playerSettings.useQuiescence = checkQuiescence.isSelected();
            playerSettings.useKillerMoves = checkKillerMoves.isSelected();
            playerSettings.maxDepth = (Integer) spinnerMaxDepth.getValue();
            playerSettings.useNullMove = checkNullMove.isSelected();
            playerSettings.nullMoveR = (Integer) spinnerNullMoveR.getValue();
            playerSettings.numberOfKillerMoves = (Integer) spinnerKillerMoves.getValue();
            playerSettings.useRelativeHistoryHeuristic = checkRelativeHistoryHeuristic.isSelected();
            playerSettings.useAspirationSearch = checkAspirationSearch.isSelected();
            playerSettings.aspirationWindow = (Integer) spinnerAspirationWindow.getValue();

            switch (comboPlayerList.getSelectedIndex()) {
                case 1:
                    player = new AIPlayer(playerSettings); break;
                default:
                    player = new Human(); break;
            }
        }
        return player;
    }
}
