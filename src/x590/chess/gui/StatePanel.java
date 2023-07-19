package x590.chess.gui;

import javax.swing.*;

public class StatePanel extends JLabel {

	private static final int PADDING = 16;

	public StatePanel(String initialState) {
		super(initialState, SwingConstants.CENTER);
		setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
	}
}
