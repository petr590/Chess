package x590.chess.gui.board;

import x590.chess.gui.ResizeableObject;

import javax.swing.*;
import java.awt.*;

/**
 * Угол доски
 */
public class CornerPanel extends JPanel {

	private static final ResizeableObject<Dimension> PREFERRED_SIZE = ResizeableObject.newDimension(s -> s / 2);

	private static final int SIZE = 3;

	private final int[] xPoints, yPoints;

	/**
	 * Параметры - это пары координат от 0 до 1,
	 * где 0 - левый край для x или верхний край для y,
	 * а 1 - правый край для x или нижний край для y.
	 * @throws IllegalArgumentException если одна из координат не соответствует ограничениям
	 */
	public CornerPanel(int x1, int y1, int x2, int y2, int x3, int y3) {
		this.xPoints = new int[] { checkCoord(x1, "x1"), checkCoord(x2, "x2"), checkCoord(x3, "x3") };
		this.yPoints = new int[] { checkCoord(y1, "y1"), checkCoord(y2, "y2"), checkCoord(y3, "y3") };

		setPreferredSize(PREFERRED_SIZE.get());
	}

	private static int checkCoord(int coord, String name) {
		if (coord >= 0 && coord <= 1) {
			return coord;
		}

		throw new IllegalArgumentException(name + " is out of range: " + coord);
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);

		int width = getWidth(),
			height = getHeight();

		for (int i = 0; i < SIZE; i++) {
			xPoints[i] *= width;
			yPoints[i] *= height;
		}

		graphics.setColor(BoardPanel.BORDER_COLOR);
		graphics.fillPolygon(xPoints, yPoints, SIZE);

		for (int i = 0; i < SIZE; i++) {
			xPoints[i] /= width;
			yPoints[i] /= height;
		}
	}
}
