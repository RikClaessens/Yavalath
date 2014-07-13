package gui;

import com.rush.HexGridCell;
import game.Board;
import game.Field;
import game.RowOfFour;
import players.MiniMax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by rhmclaessens on 30-06-2014.
 */
public class YavalathPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int NUM_HEX_CORNERS = 6;
    private static final int CELL_R = 30;
    private static Board board = new Board();

    private int[] mCornersY = new int[NUM_HEX_CORNERS];
    private int[] mCornersX = new int[NUM_HEX_CORNERS];

    private float mouseX, mouseY;

    private static HexGridCell mCellMetrics = new HexGridCell(CELL_R);

    private Color[] whitePiece = new Color[] {
            new Color(250,250,250),
            new Color(180,180,180),
            new Color(170, 170, 170),
            new Color(230, 230, 230)
    };
    private Color[] blackPiece = new Color[] {
            new Color(  0,  0,  0),
            new Color( 40, 40, 40),
            new Color( 60, 60, 60),
            new Color( 30, 30, 30)
    };
    private Color[] redPiece   = new Color[] {
            new Color(174, 11, 18),
            new Color(226,  0,  0),
            new Color(222, 29,  0),
            new Color(201, 31, 12)
    };
    private Color[] fieldColor = new Color[] {
            new Color(237, 230, 0),
            new Color(237, 180, 0),
            new Color(239, 150, 0)
    };
    private Color[] winningColor = new Color[] {
            new Color(46, 204, 64),
            new Color(0, 181, 6)
    };
    private Color[] losingColor = new Color[] {
            new Color(0, 116, 217),
            new Color(0,  75, 145)
    };
    private Color[] fieldOutlineColor = new Color[]{
            new Color(49, 49, 49),
            new Color(0, 181, 6)
    };
    private Color[] pieceOutlineColor = new Color[]{
            new Color( 49, 49, 49),
            new Color(100,100,100)
    };

    public YavalathPanel() {

        addMouseMotionListener(this);
        addMouseListener(this);
        repaint();
    }

    public void newGame() {
        board = new Board();
        board.initBoard();
        board.setPlayer(board.WHITE, new MiniMax(board.WHITE));
//        board.setPlayer(board.BLACK, new MiniMax(board.BLACK));
        while (!board.isHumanMove()) {
            board.doTurn();
            repaint();
            validate();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        board.doMove(76);
//        board.doMove(66);
//        board.doMove(40);
//        board.doMove(58);
//        board.doMove(68);
//        board.doMove(61);
//        board.doMove(51);
//        board.doMove(41);
//        board.doMove(49);
//        board.doMove(59);
    }

    public void undoMove() {
        board.undoMoveWithCheck(board.movesMade[board.numberOfMovesMade - 1]);
        repaint();
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color[] turnColor = board.turn == board.WHITE ? whitePiece : (board.turn == board.BLACK ? blackPiece : redPiece);
        GradientPaint paint = new GradientPaint(
                0,
                0,
                turnColor[1],
                0,
                this.getHeight(),
                turnColor[0],
                true);
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        for (int i = 0; i < board.numberOfCells; i++) {
            if (!board.isOnTheBoard(i))
                continue;
            int col = board.col(i);
            int row = board.row(i);
            int piece = board.board[i].piece;

            mCellMetrics.setCellIndex(row, col);
            mCellMetrics.computeCorners(mCornersY, mCornersX);
            paint = new GradientPaint(
                    mCornersX[0], mCornersY[0], fieldColor[0],
                    mCornersX[3], mCornersY[3], fieldColor[1], true);
            Polygon polygon = new Polygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
            if (polygon.contains(mouseX, mouseY) && piece == board.FREE) {
                paint = new GradientPaint(
                        mCornersX[0], mCornersY[0], fieldColor[1],
                        mCornersX[3], mCornersY[3], fieldColor[2], true);
            } else {
                boolean paintNeighbors = false;
                if (paintNeighbors) {
                    for (Field neighbor : board.board[i].neighbors) {
                        if (neighbor == null)
                            continue;
                        mCellMetrics.setCellIndex(board.row(neighbor.position), board.col(neighbor.position));
                        mCellMetrics.computeCorners(mCornersY, mCornersX);
                        polygon = new Polygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
                        if (polygon.contains(mouseX, mouseY)) {
                            paint = new GradientPaint(
                                    mCornersX[0], mCornersY[0], fieldColor[0].darker(),
                                    mCornersX[3], mCornersY[3], fieldColor[1].darker(), true);
                        }
                    }
                    mCellMetrics.setCellIndex(row, col);
                    mCellMetrics.computeCorners(mCornersY, mCornersX);
                }
                boolean paintRowsOfFour = false;
                if (paintRowsOfFour) {
                    for (RowOfFour rowOfFour : board.board[i].rowsOfFour) {
                        for (Field neighbor : rowOfFour.fields) {
                            if (neighbor == null)
                                continue;
                            mCellMetrics.setCellIndex(board.row(neighbor.position), board.col(neighbor.position));
                            mCellMetrics.computeCorners(mCornersY, mCornersX);
                            polygon = new Polygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
                            if (polygon.contains(mouseX, mouseY)) {
                                paint = new GradientPaint(
                                        mCornersX[0], mCornersY[0], fieldColor[0].darker(),
                                        mCornersX[3], mCornersY[3], fieldColor[1].darker(), true);
                            }
                        }
                    }
                    mCellMetrics.setCellIndex(row, col);
                    mCellMetrics.computeCorners(mCornersY, mCornersX);
                }
            }
            g2d.setPaint(paint);
            if (!board.getAllowedMoveSet().contains(i)) {
                g2d.setPaint(new GradientPaint(paint.getPoint1(), paint.getColor1().darker(), paint.getPoint2(), paint.getColor2().darker()));
            }
            g2d.fillPolygon(mCornersX, mCornersY, NUM_HEX_CORNERS);

            g2d.setColor(fieldOutlineColor[0]);
            g2d.drawPolygon(mCornersX, mCornersY, NUM_HEX_CORNERS);

            g2d.setFont(new Font("Arial", Font.BOLD, (int) (11 * 0.9)));
            g2d.drawString(Integer.toString(i), mCornersX[5] - 3, mCornersY[5] + 11);
//            g2d.drawString(Integer.toString(board.distances[i]), mCornersX[5] - 3, mCornersY[5] + 20);
            int stoneSize = (int) (0.75 * (CELL_R + 1 * (CELL_R / Math.sqrt(2))));
            int smallCircleSize = (int) (0.5 * stoneSize);
            if (piece == board.WHITE) {
                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        whitePiece[0],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize) + stoneSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize) + stoneSize,
                        whitePiece[1], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                g2d.setColor(pieceOutlineColor[0]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        whitePiece[2],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        whitePiece[3], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);

                g2d.setColor(pieceOutlineColor[1]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);
            }
            if (piece == board.BLACK) {
                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        blackPiece[0],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize) + stoneSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize) + stoneSize,
                        blackPiece[1], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                g2d.setColor(pieceOutlineColor[0]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        blackPiece[2],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        blackPiece[3], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);

                g2d.setColor(pieceOutlineColor[1]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);
            }
            if (piece == board.RED) {
                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        redPiece[0],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize) + stoneSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize) + stoneSize,
                        redPiece[1], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                g2d.setColor(pieceOutlineColor[0]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * stoneSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * stoneSize),
                        stoneSize, stoneSize);

                paint = new GradientPaint(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        redPiece[2],
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize) + smallCircleSize,
                        redPiece[3], true);
                g2d.setPaint(paint);
                g2d.fillOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);

                g2d.setColor(pieceOutlineColor[1]);
                g2d.drawOval(
                        (int) ((mCornersX[0] + mCornersX[3]) / 2 - 0.5 * smallCircleSize),
                        (int) ((mCornersY[2] + mCornersY[5]) / 2 - 0.5 * smallCircleSize),
                        smallCircleSize, smallCircleSize);
            }
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX  = e.getX();
        mouseY = e.getY();
        repaint();
    }


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (board.isGameOver() || !board.isHumanMove())
            return;
        for (int i = 0; i < board.numberOfCells; i++) {
            if (!board.isOnTheBoard(i))
                continue;
            int col = board.col(i);
            int row = board.row(i);
            int piece = board.board[i].piece;

            mCellMetrics.setCellIndex(row, col);
            mCellMetrics.computeCorners(mCornersY, mCornersX);
            Polygon polygon = new Polygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
            if (polygon.contains(e.getX(), e.getY())) {
                board.doMove(i);
                repaint();
//                System.out.println(">>>> Move " + i + " game over = " + board.isGameOver() + " player " + board.gameWon + " won.");
                if (!board.isGameOver()) {
                    board.doTurn();
                } else {
                    System.out.print("GAME OVER");
                    if (board.gameWon == board.BLACK) {
                        System.out.println("\tBLACK won");
                    } else if (board.gameWon == board.WHITE) {
                        System.out.println("\tWHITE won");
                    }
                }
                break;
            }
        }
        repaint();
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {

    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }
}
