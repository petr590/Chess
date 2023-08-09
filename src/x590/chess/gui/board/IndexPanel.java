package x590.chess.gui.board;

import x590.chess.gui.ResizeableObject;

import javax.swing.*;
import java.awt.*;

/**
 * Панель с индексом строки или столбца доски
 */
public class IndexPanel extends JLabel {

	private static final ResizeableObject<Dimension>
			VERTICAL_PREFERRED_SIZE = ResizeableObject.newDimension(IndexPanel::less, IndexPanel::same).immediatelyUpdate(),
			HORIZONTAL_PREFERRED_SIZE = ResizeableObject.newDimension(IndexPanel::same, IndexPanel::less).immediatelyUpdate();

	private static int same(int size) {
		return size;
	}

	private static int less(int size) {
		return size / 2;
	}

	public IndexPanel(int index, boolean isVertical) {
		super(isVertical ? Integer.toString(index + 1) : Character.toString((char)('A' + index)),
				SwingConstants.CENTER);

		setPreferredSize((isVertical ? VERTICAL_PREFERRED_SIZE : HORIZONTAL_PREFERRED_SIZE).get());

		setFont(getFont().deriveFont(Font.BOLD));
		setOpaque(true);
		setForeground(Color.WHITE);
		setBackground(BoardPanel.BORDER_COLOR);
	}
}
