package gui;

import players.Human;
import players.Player;
import players.PlayerSettings;
import players.ai.IDNegamax;
import players.ai.MiniMax;
import util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by rikclaessens on 20/07/14.
 */
public class PlayerPanel extends JPanel {

    private String[] players = new String[]{"Human", "IDNegamax"};
    private static int[] DEF_PLAYERS = new int[]{0, 1};
    private JComboBox comboPlayerList;
    private JCheckBox checkTT;
    private JCheckBox checkMoveOrdering;
    private JCheckBox checkPVS;
    private JCheckBox checkNullMove;
    private JCheckBox checkQuiescence;
    private JSpinner spinnerMaxDepth;
    private JSpinner spinnerNullMoveR;
    private static int DEF_MAX_DEPTH = 5;
    private static int DEF_NULL_MOVE_R = 2;
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

        add(new JLabel("TT"));
        checkTT = new JCheckBox();
        checkTT.setSelected(true);
        add(checkTT);

        add(new JLabel("Move Ordering"));
        checkMoveOrdering = new JCheckBox();
        checkMoveOrdering.setSelected(true);
        add(checkMoveOrdering);

        add(new JLabel("PVS"));
        checkPVS = new JCheckBox();
        checkPVS.setSelected(true);
        add(checkPVS);

        add(new JLabel("Null move"));
        checkNullMove = new JCheckBox();
        checkNullMove.setSelected(true);
        add(checkNullMove);

        add(new JLabel("Quiescence"));
        checkQuiescence = new JCheckBox();
        checkQuiescence.setSelected(true);
        add(checkQuiescence);

        SpinnerModel nullMoveRSpinnerModel = new SpinnerNumberModel(DEF_NULL_MOVE_R, 0, 5, 1);
        spinnerNullMoveR = new JSpinner(nullMoveRSpinnerModel);
        add(new JLabel("Null Move R"));
        add(spinnerNullMoveR);
        ((JSpinner.DefaultEditor) spinnerNullMoveR.getEditor()).getTextField().setEditable(false);
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
            playerSettings.maxDepth = (Integer) spinnerMaxDepth.getValue();
            playerSettings.useNullMove = checkNullMove.isSelected();
            playerSettings.nullMoveR = (Integer) spinnerNullMoveR.getValue();

            switch (comboPlayerList.getSelectedIndex()) {
                case 1:
                    player = new IDNegamax(playerSettings); break;
                case 2:
                    player = new MiniMax(playerSettings); break;
                default:
                    player = new Human(); break;
            }
        }
        return player;
    }
}
