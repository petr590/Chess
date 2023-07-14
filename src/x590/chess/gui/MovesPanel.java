package x590.chess.gui;

import x590.chess.figure.move.IMove;
import x590.chess.gui.board.BoardPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MovesPanel extends JPanel {

	private static final Color BORDER_COLOR = BoardPanel.BORDER_COLOR;

	private static final int BORDER_THICKNESS = 2;

	private final List<IMove> moves = new ArrayList<>();

	private final JPanel container;

	private final BoardPanel boardPanel;

	public MovesPanel(BoardPanel boardPanel) {
		this.container = new JPanel();

		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
						"Ходы", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION
				)
		);

		this.boardPanel = boardPanel;

		JScrollPane scrollPane = new JScrollPane(container) {
			@Override
			public Dimension getPreferredSize() {
				if (moves.isEmpty()) {
					return new Dimension(0, 0);
				}

				Dimension preferredSize = super.getPreferredSize();
				preferredSize.width += getVerticalScrollBar().getWidth();
				preferredSize.height = Math.min(preferredSize.height, boardPanel.getHeight());
				return preferredSize;
			}
		};

		add(scrollPane);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.height = boardPanel.getHeight();
		return preferredSize;
	}

	public void addMove(IMove move) {
		container.add(new MovePanel(move, moves.isEmpty()));
		moves.add(move);
	}

	private static class MovePanel extends JPanel {

		public MovePanel(IMove move, boolean first) {
			add(new JLabel(move.toConvenientString()));

			if (!first) {
				setBorder(BorderFactory.createMatteBorder(BORDER_THICKNESS, 0, 0, 0, BORDER_COLOR));
			}
		}
	}
}
