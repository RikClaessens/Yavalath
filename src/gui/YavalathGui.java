package gui;

import game.Board;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class  YavalathGui extends JFrame implements MouseListener {

    private static YavalathGui frame;
//    private static TextPanel logPanel = new TextPanel();
    public YavalathGui() {
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        final YavalathPanel yavalathPanel = new YavalathPanel();
        yavalathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        yavalathPanel.setLayout(new BorderLayout(0, 0));

//        logPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        contentPane.add(yavalathPanel);
//        contentPane.add(logPanel);

        final PlayerPanel playerPanelWhite = new PlayerPanel(Board.WHITE);
        final PlayerPanel playerPanelBlack = new PlayerPanel(Board.BLACK);

        JPanel settingsPanel = new JPanel();
        JTabbedPane tabbedPane = new JTabbedPane();
//        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        tabbedPane.addTab("White", playerPanelWhite);
        tabbedPane.addTab("Black", playerPanelBlack);
        tabbedPane.setSelectedIndex(1);
        contentPane.add(tabbedPane);

        JButton btnNewGame = new JButton("New Game");
        btnNewGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                yavalathPanel.newGame(playerPanelWhite.getPlayer(), playerPanelBlack.getPlayer());
                requestFocus();
                repaint();
            }
        });

        JButton btnUndoButton = new JButton("Undo");
        btnUndoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                yavalathPanel.undoMove();
                requestFocus();
                repaint();
            }
        });
        JPanel controlPanel = new JPanel();
        controlPanel.add(btnNewGame);
        controlPanel.add(btnUndoButton);

        tabbedPane.add("Game", controlPanel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        setFocusable(true);

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'n') {
                    yavalathPanel.newGame(playerPanelWhite.getPlayer(), playerPanelBlack.getPlayer());
                }
                if (e.getKeyChar() == 'u') {
                    yavalathPanel.undoMove();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
        pack();
        setBounds(100, 100, 800, getHeight());
        setTitle("Yavalath");
    }

    private JPanel contentPane;

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
