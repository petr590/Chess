package x590.chess.gui.board;

import x590.chess.board.ChessBoard;
import x590.chess.Main;
import x590.chess.figure.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.step.IStep;
import x590.util.annotation.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FieldPanel extends JPanel {

	private static int PREFERRED_SIZE;
	private static final Dimension PREFERRED_SIZE_DIMENSION = new Dimension();

	private static final float SIZE_COEFFICIENT = 1 / 2.1f;


	private static Border HOVER_BORDER, SELECTED_BORDER, ATTACKED_BORDER, POSSIBLE_TAKE_BORDER;

	private static final float BORDER_THICKNESS_RATIO = 0.1f;

	private static final Cursor
			DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),
			HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	private static final Color
			WHITE_FIELD_COLOR     = new Color(0xDEC09A),
			BLACK_FIELD_COLOR     = new Color(0x854D3C),
			HOVER_BORDER_COLOR    = new Color(0x119011),
			SELECTED_BORDER_COLOR = new Color(0x11C011),
			ATTACKED_BORDER_COLOR = new Color(0xD03333),
			POSSIBLE_STEP_COLOR   = SELECTED_BORDER_COLOR,
			POSSIBLE_TAKE_COLOR   = new Color(0xB72424);



	static {
		updateSize(Main.getFrame());
	}

	public static void updateSize(JFrame frame) {
		PREFERRED_SIZE = (int) (Math.min(frame.getWidth(), frame.getHeight()) * (SIZE_COEFFICIENT / ChessBoard.SIZE));
		PREFERRED_SIZE_DIMENSION.width = PREFERRED_SIZE_DIMENSION.height = PREFERRED_SIZE;

		int thickness = getBorderThickness();

		HOVER_BORDER         = BorderFactory.createLineBorder(HOVER_BORDER_COLOR,    thickness);
		SELECTED_BORDER      = BorderFactory.createLineBorder(SELECTED_BORDER_COLOR, thickness);
		ATTACKED_BORDER      = BorderFactory.createLineBorder(ATTACKED_BORDER_COLOR, thickness);
		POSSIBLE_TAKE_BORDER = BorderFactory.createLineBorder(POSSIBLE_TAKE_COLOR,   thickness);
	}

	public static int getPreferredSizeValue() {
		return PREFERRED_SIZE;
	}

	private static int getBorderThickness() {
		return Math.max((int)(PREFERRED_SIZE * BORDER_THICKNESS_RATIO), 1);
	}

	private final Pos pos;

	private final BoardPanel boardPanel;

	private final ChessBoard chessBoard;

	private @Nullable IStep possibleStep;

	private boolean isSelected, isAttacked;

	public FieldPanel(int x, int y, BoardPanel boardPanel) {
		this.pos = Pos.of(x, y);
		this.boardPanel = boardPanel;
		this.chessBoard = boardPanel.getChessBoard();

		setBackground(((x ^ y) & 0x1) == 0 ? WHITE_FIELD_COLOR : BLACK_FIELD_COLOR);
		setPreferredSize(PREFERRED_SIZE_DIMENSION);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent event) {
				if (canBeSelected()) {
					if (chessBoard.getFigureSide(pos) == chessBoard.currentSide()) {
						boardPanel.select(FieldPanel.this);
					} else if (possibleStep != null) {
						boardPanel.makeStep(possibleStep);
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				if (canBeSelected() && chessBoard.getFigureSide(pos) == chessBoard.currentSide()) {
					setBorder(HOVER_BORDER);
				}
			}

			@Override
			public void mouseExited(MouseEvent event) {
				if (canBeSelected()) {
					updateBorder();
				}
			}
		});

		updateCursor();
	}

	private boolean canBeSelected() {
		return boardPanel.canSelect(this);
	}

	public Pos getPos() {
		return pos;
	}

	public void updateCursor() {
		setCursor(boardPanel.canSelect(this) && chessBoard.getFigureSide(pos) == chessBoard.currentSide() ?
				HAND_CURSOR :
				DEFAULT_CURSOR);
	}

	public void select() {
		if (!isSelected) {
			isSelected = true;
			updateBorder();
		}
	}

	public void unselect() {
		if (isSelected) {
			isSelected = false;
			updateBorder();
		}
	}

	private void updateBorder() {
		setBorder(
				isSelected ? SELECTED_BORDER :
				isAttacked ? ATTACKED_BORDER :
				possibleStep != null && chessBoard.hasFigure(pos) ? POSSIBLE_TAKE_BORDER :
				null
		);
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);

		Figure figure = chessBoard.getFigure(pos);

		int width = getWidth(),
			height = getHeight();

		if (figure != null) {
			graphics.drawImage(figure.getImage(), 0, 0, width, height, null, null);
		}

		if (possibleStep != null && figure == null) {
			graphics.setColor(POSSIBLE_STEP_COLOR);
			graphics.fillOval(width / 3, height / 3, width / 3, height / 3);
		}
	}

	public void setPossibleStep(IStep possibleStep) {
		var oldStep = this.possibleStep;
		this.possibleStep = possibleStep;

		if (oldStep != possibleStep) {
			setCursor(possibleStep != null ? HAND_CURSOR : DEFAULT_CURSOR);
			updateBorder();
			repaint();
		}
	}

	public void setAttacked(boolean isAttacked) {
		var oldValue = this.isAttacked;
		this.isAttacked = isAttacked;

		if (oldValue != isAttacked) {
			updateBorder();
		}
	}
}
