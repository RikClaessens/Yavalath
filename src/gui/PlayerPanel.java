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
    private JComboBox comboPlayerList;
    private JCheckBox checkTT;
    private JCheckBox checkMoveOrdering;
    private JCheckBox checkPVS;
    private JSpinner spinnerMaxDepth;
    private static int defaultMaxDepth = 5;
    private int piece;

    public PlayerPanel(int piece) {
        this.piece = piece;
        setLayout(new GridLayout(0, 2));
        setBorder(new TitledBorder(Util.piecePlayer(piece)));

        add(new JLabel("AI"));
        comboPlayerList = new JComboBox(players);
        add(comboPlayerList);
        comboPlayerList.setSelectedIndex(1);

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

        SpinnerModel maxDepthSpinnerModel = new SpinnerNumberModel(defaultMaxDepth, 1, 100, 1);
        spinnerMaxDepth = new JSpinner(maxDepthSpinnerModel);
        add(new JLabel("Max Depth"));
        add(spinnerMaxDepth);
        ((JSpinner.DefaultEditor) spinnerMaxDepth.getEditor()).getTextField().setEditable(false);
    }

    public Player getPlayer() {
        Player player;
        if (comboPlayerList.getSelectedIndex() == 0) {
            player = new Human();
        } else {
            PlayerSettings playerSettings = new PlayerSettings();
            playerSettings.piece = piece;
            playerSettings.transpositionTable = checkTT.isSelected();
            playerSettings.principalVariation = checkPVS.isSelected();
            playerSettings.orderMoves = checkMoveOrdering.isSelected();
            playerSettings.maxDepth = (Integer) spinnerMaxDepth.getValue();
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
