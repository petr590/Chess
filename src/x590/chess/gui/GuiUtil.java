package x590.chess.gui;

import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

public final class GuiUtil {

	private GuiUtil() {}


	@SafeVarargs
	public static <T> @Nullable T showOptionDialog(String text, String title, T... values) {
		return showOptionDialog(text, title, values, values);
	}

	@SafeVarargs
	public static <T> @Nullable T showOptionDialog(String text, String title, Object[] options, T... values) {
		if (options.length != values.length) {
			throw new IllegalArgumentException("options.length not matches to values.length");
		}

		int chose = JOptionPane.showOptionDialog(
				null, new JLabel(text, SwingConstants.CENTER), title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, null
		);

		return chose == -1 ? null : values[chose];
	}


	public static GridBagConstraints constraintsWithCoords(GridBagConstraints constraints, int x, int y) {
		constraints.gridx = x;
		constraints.gridy = y;
		return constraints;
	}

	public static GridBagConstraints constraintsWithCoords(GridBagConstraints constraints, int x, int y, int gridWidth, int gridHeight) {
		constraints.gridwidth = gridWidth;
		constraints.gridheight = gridHeight;
		return constraintsWithCoords(constraints, x, y);
	}
}
