package x590.chess;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.gui.*;
import x590.chess.gui.board.CornerPanel;
import x590.chess.gui.board.FieldPanel;
import x590.chess.gui.board.IndexPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Main {

	public static final String NAME = "Шахматы ♛";

	private static JFrame frame;

	public static JFrame getFrame() {
		return frame;
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


		Side side = GuiUtil.showOptionDialog("Выберите цвет", NAME,
				new String[] { "Белые", "Чёрные", "Случайно" },
				Side.WHITE, Side.BLACK, Side.randomSide()
		);

		if (side == null) {
			return;
		}


		JFrame frame = Main.frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setResizable(true);

		frame.add(new GamePanel(ChessBoard.defaultPlacement(), side));

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

		frame.setVisible(true);
	}
}
