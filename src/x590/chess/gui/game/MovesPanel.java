package x590.chess.gui.game;

import x590.chess.figure.move.IMove;
import x590.chess.gui.board.BoardPanel;
import x590.util.Util;
import x590.util.annotation.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Представляет панель с прокручиваемым списком ходов
 */
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
				if (container.getComponentCount() == 0) {
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
		removeCanceledMoves();
		container.add(new MovePanel(move, moves.isEmpty()));
		moves.add(move);
		revalidate();
	}

	private void removeCanceledMoves() {
		int componentsCount = container.getComponentCount();
		int movesCount = moves.size();

		for (int i = componentsCount; i > movesCount; ) {
			container.remove(--i);
		}
	}

	public @Nullable IMove getLastMove() {
		int lastIndex = moves.size() - 1;
		return lastIndex == -1 ? null : moves.get(lastIndex);
	}

	public @Nullable IMove repeatLastCanceledMove() {
		int movesCount = moves.size();

		if (container.getComponentCount() > movesCount) {
			MovePanel movePanel = (MovePanel) container.getComponent(movesCount);
			movePanel.makeUncanceled();
			return Util.addAndGetBack(moves, movePanel.getMove());
		}

		return null;
	}


	/**
	 * @return Последний неотменённый ход или {@code null}, если таких ходов нет
	 */
	public @Nullable IMove cancelLastMove() {
		if (moves.isEmpty()) {
			return null;
		}

		int lastIndex = moves.size() - 1;
		((MovePanel)container.getComponent(lastIndex)).makeCanceled();
		return moves.remove(lastIndex);
	}

	private static class MovePanel extends JPanel {

		private static final Color CANCELED_COLOR = new Color(0xB3B3B3);

		private final IMove move;

		public MovePanel(IMove move, boolean first) {
			this.move = move;
			add(new JLabel(move.toConvenientString()));

			if (!first) {
				setBorder(BorderFactory.createMatteBorder(BORDER_THICKNESS, 0, 0, 0, BORDER_COLOR));
			}
		}

		public IMove getMove() {
			return move;
		}

		public void makeCanceled() {
			getComponent(0).setForeground(CANCELED_COLOR);
		}

		public void makeUncanceled() {
			getComponent(0).setForeground(null);
		}
	}
}
