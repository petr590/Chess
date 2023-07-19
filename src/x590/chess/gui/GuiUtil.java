package x590.chess.gui;

import x590.chess.Main;
import x590.chess.figure.Figure;
import x590.chess.gui.board.CornerPanel;
import x590.chess.gui.board.FieldPanel;
import x590.chess.gui.board.IndexPanel;
import x590.util.annotation.Nullable;
import x590.util.annotation.RemoveIfNotUsed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.IntFunction;

public final class GuiUtil {

	private GuiUtil() {}


	@SafeVarargs
	public static <T> @Nullable T showOptionDialog(String text, String title, T... values) {
		return showOptionDialog(text, title, values, values);
	}

	public static <T> @Nullable T showOptionDialog(String text, String title, Object[] options, List<? extends T> values) {
		checkLengthsEquals(options.length, values.size());
		return showOptionDialog(text, title, options, values::get);
	}

	@SafeVarargs
	public static <T> @Nullable T showOptionDialog(String text, String title, Object[] options, T... values) {
		checkLengthsEquals(options.length, values.length);
		return showOptionDialog(text, title, options, choose -> values[choose]);
	}

	private static <T> @Nullable T showOptionDialog(String text, String title, Object[] options, IntFunction<T> elementGetter) {
		int chose = JOptionPane.showOptionDialog(
				Main.getFrame(), new JLabel(text, SwingConstants.CENTER), title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, null
		);

		return chose == JOptionPane.CLOSED_OPTION ? null : elementGetter.apply(chose);
	}

	public static boolean showYesNoOptionDialog(String text, String title) {
		return showYesNoOptionDialog(text, title, "Да", "Нет");
	}

	public static boolean showYesNoOptionDialog(String text, String title, Object... options) {
		int chose = JOptionPane.showOptionDialog(
				Main.getFrame(), new JLabel(text, SwingConstants.CENTER), title,
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, null
		);

		return chose == JOptionPane.YES_OPTION;
	}

	public static int showYesNoCancelOptionDialog(String text, String title, Object... options) {
		return JOptionPane.showOptionDialog(
				Main.getFrame(), new JLabel(text, SwingConstants.CENTER), title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, null
		);
	}

	private static void checkLengthsEquals(int optionsLength, int valuesLength) {
		if (optionsLength != valuesLength) {
			throw new IllegalArgumentException("options length not matches to values length");
		}
	}


	public static void showPlainMessageDialog(JFrame frame, String message) {
		JOptionPane.showMessageDialog(frame, new JLabel(message, SwingConstants.CENTER), "", JOptionPane.PLAIN_MESSAGE);
	}

	public static void showPlainMessageDialog(JFrame frame, Throwable ex) {
		showPlainMessageDialog(frame, ex.getClass().getCanonicalName() + ": " + ex.getLocalizedMessage());
	}

	public static void showPlainMessageDialog(String message) {
		showPlainMessageDialog(Main.getFrame(), message);
	}

	public static void showPlainMessageDialog(Throwable ex) {
		showPlainMessageDialog(Main.getFrame(), ex);
	}

	public static GridBagConstraints constraintsWithCoords(GridBagConstraints constraints, int x, int y) {
		constraints.gridx = x;
		constraints.gridy = y;
		return constraints;
	}


	private static final int TOOLTIP_PADDING = 8;

	public static void setup(JFrame frame) {
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				FieldPanel.updateSize(frame);
				IndexPanel.updateSize();
				CornerPanel.updateSize();
				Figure.updateSize();
				TakenFiguresPanel.updateSize();
			}
		});


		UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
				UIManager.getBorder("ToolTip.border"),
				BorderFactory.createEmptyBorder(TOOLTIP_PADDING, TOOLTIP_PADDING, TOOLTIP_PADDING, TOOLTIP_PADDING)
		));
	}
}
