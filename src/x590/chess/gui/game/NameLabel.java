package x590.chess.gui.game;

import x590.chess.gui.ResizeableObject;
import x590.chess.gui.linked.LinkedLabel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class NameLabel extends LinkedLabel {

	private static final ResizeableObject<Integer>
			MAX_WIDTH = ResizeableObject.newIntegerSize(s -> s * 2),
			MAX_HEIGHT = ResizeableObject.newIntegerSize(s -> s / 2);

	private static final Color BORDER_COLOR = Color.GRAY;

	private static final int
			BORDER_THICKNESS = 1,
			PADDING = 8;

	private static final Border BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
			BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
	);

	public NameLabel(String name) {
		super(name, SwingConstants.CENTER);
		setBorder(BORDER);
		setFont(getFont().deriveFont(Font.BOLD));
	}

	@Override
	public Dimension originalPreferredSize() {
		Dimension preferredSize = super.originalPreferredSize();
		preferredSize.width = Math.min(preferredSize.width, MAX_WIDTH.get());
		preferredSize.height = Math.min(preferredSize.height, MAX_HEIGHT.get());
		return preferredSize;
	}
}
