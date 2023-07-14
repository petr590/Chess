package x590.chess.gui;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.gui.board.FieldPanel;
import x590.util.annotation.Immutable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static x590.chess.board.ChessBoard.SIZE;

public class TakenFiguresPanel extends JPanel {

	public static void updateSize() {
		for (TakenFiguresPanel instance : INSTANCES) {
			instance.updateThisSize();
		}
	}

	private static final List<TakenFiguresPanel> INSTANCES = new ArrayList<>();

	private final @Immutable List<Figure> takenFigures;

	private static final float
			WIDTH_COEFFICIENT = 1.08f, // Подобрано экспериментально
			HEIGHT_COEFFICIENT = 1.05f; // Подобрано экспериментально

	private final Dimension preferredSize = new Dimension();

	public TakenFiguresPanel(ChessBoard chessBoard, Side takenFiguresSide, boolean isTop) {
//		var b = Figure.BLACK_QUEEN;
//		this.takenFigures = List.of(b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b,b);
		this.takenFigures = chessBoard.getTakenFigures(takenFiguresSide);

		setAlignmentY(isTop ? BOTTOM_ALIGNMENT : TOP_ALIGNMENT);
		updateThisSize();
		setPreferredSize(preferredSize);
		setMaximumSize(preferredSize);

		setBorder(BorderFactory.createLineBorder(Color.CYAN));

		INSTANCES.add(this);
	}

	public void updateFiguresList() {
		removeAll();
		updateThisSize();
		takenFigures.stream().map(Figure::getMiniIcon).map(JLabel::new).forEachOrdered(this::add);
	}

	private void updateThisSize() {
		int fieldPanelSize = FieldPanel.getPreferredSizeValue();

		preferredSize.width = (int) (fieldPanelSize * (SIZE * WIDTH_COEFFICIENT));// + 2;
		preferredSize.height = (int) (fieldPanelSize * (2 * HEIGHT_COEFFICIENT));// + 2;
	}
}
