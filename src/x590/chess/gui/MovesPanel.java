package x590.chess.gui;

import x590.chess.figure.move.IMove;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MovesPanel extends JPanel {

	private final List<IMove> moves = new ArrayList<>();

	private final JPanel container;

	public MovesPanel() {
		this.container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		JScrollPane scrollPane = new JScrollPane(container);
		add(scrollPane);
		scrollPane.setLayout(new ScrollPaneLayout());
	}

	public void addMove(IMove move) {
		moves.add(move);
		container.add(new MovePanel(move));
	}


	private static class MovePanel extends JPanel {

		private static final Color BORDER_COLOR = new Color(0x555555);

		public MovePanel(IMove move) {
			add(new JLabel(move.toConvenientString()));
			setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 3));
		}
	}
}
