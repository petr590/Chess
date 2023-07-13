package x590.chess.gui.board;

import x590.chess.board.ChessBoard;
import x590.chess.Main;
import x590.chess.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.step.IStep;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FieldPanel extends JPanel {

	private static int PREFERRED_SIZE;
	private static final Dimension PREFERRED_SIZE_DIMENSION = new Dimension();

	private static final float SIZE_COEFFICIENT = 1 / 1.75f;

	static {
		updateSize(Main.getFrame());
	}

	public static void updateSize(JFrame frame) {
		PREFERRED_SIZE = (int) (Math.min(frame.getWidth(), frame.getHeight()) * (SIZE_COEFFICIENT / ChessBoard.SIZE));
		PREFERRED_SIZE_DIMENSION.width = PREFERRED_SIZE_DIMENSION.height = PREFERRED_SIZE;
	}

	public static int getPreferredSizeValue() {
		return PREFERRED_SIZE;
	}


	private static final float BORDER_THICKNESS_RATIO = 0.1f;

	private static final Cursor
			DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),
			HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	private static final Color
			WHITE_FIELD_COLOR = new Color(0xDEC09A),
			BLACK_FIELD_COLOR = new Color(0x854D3C),
			HOVER_BORDER_COLOR = new Color(0x119011),
			SELECTED_BORDER_COLOR = new Color(0x11C011),
			POSSIBLE_STEP_COLOR = SELECTED_BORDER_COLOR,
			POSSIBLE_TAKE_COLOR = new Color(0xB72424),
			ATTACKED_FIELD_COLOR = new Color(0xD03333);

	private final Pos pos;

	private final ChessBoard chessBoard;

	private @Nullable IStep possibleStep;

	private boolean isAttacked;

	public FieldPanel(int x, int y, BoardPanel boardPanel) {
		this.pos = Pos.of(x, y);
		this.chessBoard = boardPanel.getChessBoard();

		setBackground(((x ^ y) & 0x1) == 0 ? WHITE_FIELD_COLOR : BLACK_FIELD_COLOR);
		setPreferredSize(PREFERRED_SIZE_DIMENSION);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (boardPanel.getSelected() != FieldPanel.this) {
					if (chessBoard.getFigureSide(pos) == chessBoard.currentSide()) {
						boardPanel.select(FieldPanel.this);
					} else if (possibleStep != null) {
						boardPanel.step(possibleStep);
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				if (boardPanel.getSelected() != FieldPanel.this && chessBoard.getFigureSide(pos) == chessBoard.currentSide()) {
					setBorder(BorderFactory.createLineBorder(HOVER_BORDER_COLOR, getBorderThickness()));
				}
			}

			@Override
			public void mouseExited(MouseEvent event) {
				if (boardPanel.getSelected() != FieldPanel.this) {
					unselect();
				}
			}
		});

		update();
	}

	public Pos getPos() {
		return pos;
	}

	private int getBorderThickness() {
		return (int) Math.max(PREFERRED_SIZE * BORDER_THICKNESS_RATIO, 1);
	}

	public void update() {
		setCursor(chessBoard.getFigureSide(pos) == chessBoard.currentSide() ? HAND_CURSOR : DEFAULT_CURSOR);
	}

	public void select() {
		setBorder(BorderFactory.createLineBorder(SELECTED_BORDER_COLOR, getBorderThickness()));
	}

	public void unselect() {
		if (getBorder() != null) {
			setBorder(null);
		}
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);

		Figure figure = chessBoard.getFigure(pos);

		int width = getWidth(),
			height = getHeight();

		if (figure != null) {
			graphics.drawImage(figure.getImage(), 0, 0, width, height, null, null);
		}

		if (isAttacked && getBorder() == null) {
			paintBorder(graphics, width, height, ATTACKED_FIELD_COLOR);

		} else if (possibleStep != null) {
			if (figure == null) {
				graphics.setColor(POSSIBLE_STEP_COLOR);
				graphics.fillOval(width / 3, height / 3, width / 3, height / 3);
			} else {
				paintBorder(graphics, width, height, POSSIBLE_TAKE_COLOR);
			}
		}
	}

	private void paintBorder(Graphics graphics, int width, int height, Color color) {
		graphics.setColor(color);

		int thickness = getBorderThickness();

		graphics.fillRect(0, 0,                  width, thickness);
		graphics.fillRect(0, height - thickness, width, thickness);

		graphics.fillRect(0,                 thickness, thickness, height);
		graphics.fillRect(width - thickness, thickness, thickness, height);
	}

	public void setPossibleStep(IStep possibleStep) {
		var oldStep = this.possibleStep;
		this.possibleStep = possibleStep;

		if (oldStep != possibleStep) {
			setCursor(possibleStep != null ? HAND_CURSOR : DEFAULT_CURSOR);
			repaint();
		}
	}

	public void setAttacked(boolean isAttacked) {
		var oldValue = this.isAttacked;
		this.isAttacked = isAttacked;

		if (oldValue != isAttacked) {
			repaint();
		}
	}
}
