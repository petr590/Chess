package x590.chess.gui.board;

import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.IExtraStep;
import x590.chess.board.ChessBoard;
import x590.chess.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.step.StepResult;
import x590.chess.gui.GamePanel;
import x590.chess.gui.GuiUtil;
import x590.chess.gui.IndexPanel;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {

	public static final int SIZE = ChessBoard.SIZE;
	public static final int END = ChessBoard.END;

	private static final int
			GRID_START = 0,
			GRID_END = SIZE + 1;

	private final ChessBoard chessBoard;

	private final GamePanel gamePanel;

	private final FieldPanel[][] panels = new FieldPanel[SIZE][SIZE];

	private @Nullable FieldPanel selected;

	public BoardPanel(ChessBoard chessBoard, Side side, GamePanel gamePanel) {
		super(new GridBagLayout());

		this.chessBoard = chessBoard;
		this.gamePanel = gamePanel;

		var constraints = new GridBagConstraints();

		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				constraints.gridx = side.choose(x, END - x) + 1;
				constraints.gridy = side.choose(END - y, y) + 1;

				var panel = new FieldPanel(x, y, this);
				panels[y][x] = panel;
				add(panel, constraints);
			}
		}

		for (int i = 0; i < SIZE; i++) {
			int gridIndex = i + 1;

			int letterIndex, numberIndex;

			if (side == Side.WHITE) {
				letterIndex = i;
				numberIndex = END - i;
			} else {
				letterIndex = END - i;
				numberIndex = i;
			}

			add(new IndexPanel(letterIndex, false), GuiUtil.constraintsWithCoords(constraints, gridIndex, GRID_START));
			add(new IndexPanel(letterIndex, false), GuiUtil.constraintsWithCoords(constraints, gridIndex, GRID_END));

			add(new IndexPanel(numberIndex, true), GuiUtil.constraintsWithCoords(constraints, GRID_START, gridIndex));
			add(new IndexPanel(numberIndex, true), GuiUtil.constraintsWithCoords(constraints, GRID_END,   gridIndex));
		}

		add(new CornerPanel(1, 0,  1, 1,  0, 1), GuiUtil.constraintsWithCoords(constraints, GRID_START, GRID_START));
		add(new CornerPanel(0, 0,  0, 1,  1, 1), GuiUtil.constraintsWithCoords(constraints, GRID_END,   GRID_START));
		add(new CornerPanel(0, 0,  1, 0,  1, 1), GuiUtil.constraintsWithCoords(constraints, GRID_START, GRID_END));
		add(new CornerPanel(0, 0,  0, 1,  1, 0), GuiUtil.constraintsWithCoords(constraints, GRID_END,   GRID_END));
	}

	public void select(FieldPanel fieldPanel) {
		if (selected != null) {
			selected.unselect();
			clearVisualMarking();
		}

		selected = fieldPanel;
		fieldPanel.select();

		var pos = fieldPanel.getPos();

		for (IStep step : chessBoard.getPossibleSteps(pos)) {
			Pos targetPos = step.targetPos(),
				takePos = step.takePos();

			getFieldPanel(targetPos).setPossibleStep(step);

			if (!targetPos.equals(takePos)) {
				getFieldPanel(takePos).setPossibleStep(step);
			}
		}
	}

	public FieldPanel getFieldPanel(Pos pos) {
		return panels[pos.getY()][pos.getX()];
	}

	public FieldPanel getSelected() {
		return selected;
	}

	public ChessBoard getChessBoard() {
		return chessBoard;
	}

	private static final String
			SAVE_GAME = "Сохранить партию",
			NEW_GAME = "Новая партия",
			EXIT = "Выйти";


	public void step(IStep step) {
		clearVisualMarking();

		var selected = this.selected;
		selected.unselect();
		this.selected = null;

		Pos startPos = selected.getPos();
		IMove move = step.asMove(startPos, chessBoard);

		StepResult result = chessBoard.step(startPos, step);
		repaintChanged(startPos, step);
		updateAllFields();

		if (result.isKingAttacked()) {
			getFieldPanel(chessBoard.getKingPos()).setAttacked(true);
		}

		gamePanel.makeMove(move);

		if (!result.canContinueGame()) {
			String chose = GuiUtil.showOptionDialog(
					result == StepResult.CHECKMATE ?
							chessBoard.currentSide().getLocalizedName() + " выиграли" :
							"Пат",
					"",
					SAVE_GAME, NEW_GAME, EXIT
			);

			switch (chose) {
				case NEW_GAME -> {
					chessBoard.resetAllToDefault();
					clearVisualMarking();
					repaintAll();
				}

				case SAVE_GAME -> {
					// TODO
				}

				default -> System.exit(0);
			}

		}
	}

	private void clearVisualMarking() {
		for (FieldPanel[] row : panels) {
			for (FieldPanel panel : row) {
				panel.setPossibleStep(null);
				panel.setAttacked(false);
			}
		}
	}

	private void updateAllFields() {
		for (FieldPanel[] row : panels) {
			for (FieldPanel panel : row) {
				panel.update();
			}
		}
	}

	private void repaintChanged(Pos startPos, IStep step) {
		getFieldPanel(startPos).repaint();

		Pos targetPos = step.targetPos(),
			takePos = step.takePos();

		getFieldPanel(targetPos).repaint();

		if (!targetPos.equals(takePos)) {
			getFieldPanel(takePos).repaint();
		}

		IExtraStep extraStep = step.extraStep();

		if (extraStep != null) {
			repaintChanged(extraStep.startPos(), extraStep);
		}
	}

	private void repaintAll() {
		for (FieldPanel[] row : panels) {
			for (FieldPanel panel : row) {
				panel.repaint();
			}
		}
	}
}
