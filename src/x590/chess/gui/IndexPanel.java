package x590.chess.gui;

import x590.chess.gui.board.FieldPanel;

import javax.swing.*;
import java.awt.*;

public class IndexPanel extends JLabel {

	public static final Color BACKGROUND_COLOR = new Color(0x523F30);

	private static final Dimension
			VERTICAL_PREFERRED_SIZE = new Dimension(),
			HORIZONTAL_PREFERRED_SIZE = new Dimension();

	static {
		updateSize();
	}

	public static void updateSize() {
		VERTICAL_PREFERRED_SIZE.width = HORIZONTAL_PREFERRED_SIZE.height = FieldPanel.getPreferredSizeValue() / 2;
		VERTICAL_PREFERRED_SIZE.height = HORIZONTAL_PREFERRED_SIZE.width = FieldPanel.getPreferredSizeValue();
	}

	public IndexPanel(int index, boolean isVertical) {
		super(isVertical ? Integer.toString(index + 1) : Character.toString((char)('A' + index)),
				SwingConstants.CENTER);

		setPreferredSize(isVertical ? VERTICAL_PREFERRED_SIZE : HORIZONTAL_PREFERRED_SIZE);

		setFont(getFont().deriveFont(Font.BOLD));
		setOpaque(true);
		setForeground(Color.WHITE);
		setBackground(BACKGROUND_COLOR);
	}
}
