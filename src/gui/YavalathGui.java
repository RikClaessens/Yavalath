package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class YavalathGui extends JFrame implements MouseListener {

    private static YavalathGui frame;
    public YavalathGui() {
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        final YavalathPanel yavalathPanel = new YavalathPanel();
        yavalathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        yavalathPanel.setLayout(new BorderLayout(0, 0));

        JPanel textPanel = new TextPanel();
        textPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);


        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        contentPane.add(yavalathPanel);
        contentPane.add(textPanel);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnGame = new JMenu("Game");
        menuBar.add(mnGame);

        JMenuItem mntmNewGame = new JMenuItem("New Game");
        mnGame.add(mntmNewGame);

        final JMenu p1Menu = new JMenu("P1");
        p1Menu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent item) {
                p1Menu.setName("P1: " + item.toString());
            }
        });
        menuBar.add(p1Menu);

        final JRadioButtonMenuItem rdbtnmntmHuman = new JRadioButtonMenuItem("Human");
        buttonGroup.add(rdbtnmntmHuman);
        p1Menu.add(rdbtnmntmHuman);

        final JRadioButtonMenuItem rdbtnmntmMinimax = new JRadioButtonMenuItem("MiniMax");
        buttonGroup.add(rdbtnmntmMinimax);
        p1Menu.add(rdbtnmntmMinimax);

        final JRadioButtonMenuItem rdbtnmntmMinimaxtt = new JRadioButtonMenuItem("MiniMaxTT");
        rdbtnmntmMinimaxtt.setSelected(true);
        p1Menu.add(rdbtnmntmMinimaxtt);
        buttonGroup.add(rdbtnmntmMinimaxtt);

        JMenu p2Menu = new JMenu("P2");
        menuBar.add(p2Menu);

        final JRadioButtonMenuItem rdbtnmntmHuman_1 = new JRadioButtonMenuItem("Human");
        rdbtnmntmHuman_1.setSelected(true);
        buttonGroup_1.add(rdbtnmntmHuman_1);
        p2Menu.add(rdbtnmntmHuman_1);

        final JRadioButtonMenuItem rdbtnmntmMinimax_1 = new JRadioButtonMenuItem("MiniMax");
        buttonGroup_1.add(rdbtnmntmMinimax_1);
        p2Menu.add(rdbtnmntmMinimax_1);

        final JRadioButtonMenuItem rdbtnmntmMinimaxtt_1 = new JRadioButtonMenuItem("MiniMaxTT");
        rdbtnmntmMinimaxtt_1.setSelected(true);
        p2Menu.add(rdbtnmntmMinimaxtt_1);
        buttonGroup_1.add(rdbtnmntmMinimaxtt_1);

        JButton btnUndoButton = new JButton("Undo");

        menuBar.add(btnUndoButton);

        final JButton btnPause = new JButton("Pause");
        btnPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
//                yavalathPanel.pause(btnPause.getText() == "Pause" ? true : false);
//                btnPause.setText(btnPause.getText() == "Pause" ? "Continue" : "Pause");
//                requestFocus();
//                repaint();
            }
        });

        btnUndoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                yavalathPanel.undoMove();
                requestFocus();
                repaint();
            }
        });
        menuBar.add(btnPause);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 774, 502);

        mntmNewGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                yavalathPanel.newGame();
            }
        });
        setFocusable(true);
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'n') {
                    yavalathPanel.newGame();
                }
                if (e.getKeyChar() == 'u') {
                    yavalathPanel.undoMove();
                }
//                {
//                    String player1Name = "Human";
//                    String player2Name = "Human";
//                    int stone = 1;
//                    Player player1 = new HumanPlayer();
//                    if (rdbtnmntmMinimax.isSelected()) {
//                        player1 = new Minimax(stone, new Evaluator1());
//                        player1Name = "Minimax";
//                    }
//                    if (rdbtnmntmMinimaxtt.isSelected()) {
//                        player1 = new MinimaxTT(stone, new Evaluator1());
//                        player1Name = "MinimaxTT";
//                    }
//
//                    stone = 2;
//                    Player player2 = new HumanPlayer();
//                    if (rdbtnmntmMinimax_1.isSelected()) {
//                        player2 = new Minimax(stone, new Evaluator1());
//                        player2Name = "Minimax";
//                    }
//                    if (rdbtnmntmMinimaxtt_1.isSelected()) {
//                        player2 = new MinimaxTT(stone, new Evaluator1());
//                        player2Name = "MinimaxTT";
//                    }
//
//                    yavalathPanel.newGame(player1, player2);
//                    TextPanel.splitLine();
//                    TextPanel.log("New game: " + player1Name + " vs. " + player2Name);
//                }
//                else if (e.getKeyChar() == 'p' && btnPause.getText() == "Pause") {
//                    yavalathPanel.pause(true);
//                    btnPause.setText(btnPause.getText() == "Pause" ? "Continue" : "Pause");
//                    repaint();
//                }
//                else if (e.getKeyChar() == 'c' && btnPause.getText() == "Continue") {
//                    yavalathPanel.pause(false);
//                    btnPause.setText(btnPause.getText() == "Pause" ? "Continue" : "Pause");
//                    repaint();
//                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }

    private static final long serialVersionUID = -1921481286866231418L;
    private JPanel contentPane;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final ButtonGroup buttonGroup_1 = new ButtonGroup();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frame = new YavalathGui();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
