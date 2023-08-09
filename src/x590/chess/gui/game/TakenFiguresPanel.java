package x590.chess.gui.game;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.gui.ResizeableObject;
import x590.util.annotation.Immutable;

import javax.swing.*;
import java.util.List;

import static x590.chess.board.ChessBoard.SIZE;

/**
 * Панель, которая содержит взятые фигуры
 */
public class TakenFiguresPanel extends JPanel {

	private final @Immutable List<Figure> takenFigures;

	private static final float
			WIDTH_COEFFICIENT = 1.08f, // Подобрано экспериментально
			HEIGHT_COEFFICIENT = 1.05f; // Подобрано экспериментально

	public TakenFiguresPanel(ChessBoard chessBoard, Side takenFiguresSide, boolean isTop) {
		this.takenFigures = chessBoard.getTakenFigures(takenFiguresSide);

		setAlignmentY(isTop ? BOTTOM_ALIGNMENT : TOP_ALIGNMENT);


		var preferredSize = ResizeableObject.newDimension(
				w -> (int) (w * (WIDTH_COEFFICIENT * SIZE)),
				h -> (int) (h * (HEIGHT_COEFFICIENT * 2))
		).immediatelyUpdate();

		setPreferredSize(preferredSize.get());
		setMaximumSize(preferredSize.get());
	}

	public void updateFiguresList() {
		removeAll();
		takenFigures.stream().map(Figure::getMiniIcon).map(JLabel::new).forEachOrdered(this::add);
		repaint();
	}
}
