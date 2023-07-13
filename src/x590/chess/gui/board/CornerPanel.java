package x590.chess.gui.board;

import x590.chess.gui.IndexPanel;

import javax.swing.*;
import java.awt.*;

public class CornerPanel extends JPanel {

	private static final Dimension PREFERRED_SIZE = new Dimension();

	static {
		updateSize();
	}

	public static void updateSize() {
		PREFERRED_SIZE.width = PREFERRED_SIZE.height = FieldPanel.getPreferredSizeValue() / 2;
	}

	private final int[] xPoints, yPoints;

	public CornerPanel(int x1, int y1, int x2, int y2, int x3, int y3) {
		this.xPoints = new int[] { x1, x2, x3 };
		this.yPoints = new int[] { y1, y2, y3 };

		setPreferredSize(PREFERRED_SIZE);
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);

		int width = getWidth(),
			height = getHeight();

		for (int i = 0; i < 3; i++) {
			xPoints[i] *= width;
			yPoints[i] *= height;
		}

		graphics.setColor(IndexPanel.BACKGROUND_COLOR);
		graphics.fillPolygon(xPoints, yPoints, 3);

		for (int i = 0; i < 3; i++) {
			xPoints[i] /= width;
			yPoints[i] /= height;
		}
	}
}
