package x590.chess.gui.board;

import x590.chess.config.GameConfig;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.chess.figure.step.IStep;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.step.StepResult;
import x590.chess.gui.GamePanel;
import x590.chess.gui.GuiUtil;
import x590.chess.playingside.PlayingSide;
import x590.chess.playingside.RemotePlayingSide;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoardPanel extends JPanel {

	public static final int SIZE = ChessBoard.SIZE;
	public static final int END = ChessBoard.END;

	private static final int
			GRID_START = 0,
			GRID_END = SIZE + 1;


	public static final Color BORDER_COLOR = new Color(0x523F30);

	private final ChessBoard chessBoard;

	private final GamePanel gamePanel;

	private final PlayingSide whitePlayingSide, blackPlayingSide;
	private final @Nullable RemotePlayingSide remotePlayingSide;
	private PlayingSide currentPlayingSide;

	private final Side thisSide;

	private final FieldPanel[][] panels = new FieldPanel[SIZE][SIZE];

	private @Nullable FieldPanel selected;

	/** Последний ход, на котором была предложена ничья */
	private int lastStepDrawHasBeenOffered = -1;


	public BoardPanel(ChessBoard chessBoard, Supplier<Side> sideSupplier, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide, GamePanel gamePanel) {
		super(new GridBagLayout());

		this.chessBoard = chessBoard;
		this.gamePanel = gamePanel;

		thisPlayingSide.setup(this);
		opponentPlayingSide.setup(this);

		Side side = this.thisSide = sideSupplier.get(); // sideSupplier.get() должен вызываться только после вызова PlayingSide.setup(BoardPanel)

		this.whitePlayingSide = side.choose(thisPlayingSide, opponentPlayingSide);
		this.blackPlayingSide = side.choose(opponentPlayingSide, thisPlayingSide);

		this.remotePlayingSide =
				whitePlayingSide instanceof RemotePlayingSide remote ? remote :
				blackPlayingSide instanceof RemotePlayingSide remote ? remote : null;

		this.currentPlayingSide = chessBoard.currentSide().choose(whitePlayingSide, blackPlayingSide);

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

	public Side getThisSide() {
		return thisSide;
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

	public boolean canSelect(FieldPanel fieldPanel) {
		return currentPlayingSide.canSelectField() && fieldPanel != selected;
	}

	public FieldPanel getFieldPanel(Pos pos) {
		return panels[pos.getY()][pos.getX()];
	}

	public ChessBoard getChessBoard() {
		return chessBoard;
	}

	public Side currentSide() {
		return chessBoard.currentSide();
	}

	public GamePanel getGamePanel() {
		return gamePanel;
	}

	private static final String
			SAVE_GAME = "Сохранить партию",
			NEW_GAME = "Новая партия",
			EXIT = "Выйти";


	public void makeStep(IStep step) {
		if (chessBoard.currentSide().choose(blackPlayingSide, whitePlayingSide).canMakeMove()) {
			clearVisualMarking();

			var selected = this.selected;
			this.selected = null;
			selected.unselect();

			Pos startPos = selected.getPos();

			makeMove(step.asMove(startPos, chessBoard));
		}
	}


	public void makeMove(IMove move) {
		makeMove(move, PlayingSide::onMoveMake, gamePanel -> gamePanel.onMoveMake(move));
	}

	public void repeatLastCanceledMove(IMove move) {
		makeMove(move, PlayingSide::onLastCanceledMoveRepeat, GamePanel::onLastCanceledMoveRepeat);
	}

	private void makeMove(IMove move, BiConsumer<PlayingSide, IMove> playingSideOnMoveMake,
						  Consumer<GamePanel> gamePanelOnMoveMake) {

		StepResult result = chessBoard.makeMove(move);
		currentPlayingSide = chessBoard.currentSide().choose(whitePlayingSide, blackPlayingSide);

		repaintChanged(move);
		updateAllFields();

		playingSideOnMoveMake.accept(currentPlayingSide, move);
		gamePanelOnMoveMake.accept(gamePanel);
		updateDrawOfferButton();

		if (result.isKingAttacked()) {
			getFieldPanel(chessBoard.getKingPos()).setAttacked(true);
		}

		if (!result.canContinueGame()) {
			whitePlayingSide.onGameEnd();
			blackPlayingSide.onGameEnd();

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


	public boolean canCancelMove() {
		return currentPlayingSide.canCancelMove();
	}

	public void cancelMove(IMove move, @Nullable IStep prevStep) {
		clearVisualMarking();

		chessBoard.cancelMove(move, prevStep);

		currentPlayingSide.onMoveCancel(move, prevStep);
		currentPlayingSide = chessBoard.currentSide().choose(whitePlayingSide, blackPlayingSide);

		repaintChanged(move);
		updateAllFields();
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

	private void repaintChanged(IMove move) {
		getFieldPanel(move.startPos()).repaint();

		Pos targetPos = move.targetPos(),
			takePos = move.takePos();

		getFieldPanel(targetPos).repaint();

		if (!targetPos.equals(takePos)) {
			getFieldPanel(takePos).repaint();
		}

		@Nullable IExtraMove extraMove = move.extraStep();

		if (extraMove != null) {
			repaintChanged(extraMove);
		}
	}

	private void repaintAll() {
		for (FieldPanel[] row : panels) {
			for (FieldPanel panel : row) {
				panel.repaint();
			}
		}
	}
	public boolean isRemote() {
		return remotePlayingSide != null;
	}

	private void updateDrawOfferButton() {
		if (lastStepDrawHasBeenOffered != -1 && lastStepDrawHasBeenOffered + getOfferADrawTimeout() == chessBoard.getStepNumber()) {
			gamePanel.unlockDrawOfferButton();
		}
	}

	public void lockDrawOfferButton() {
		gamePanel.lockDrawOfferButton();
	}

	public int getOfferADrawTimeout() {
		if (remotePlayingSide != null) {
			return remotePlayingSide.getOfferADrawTimeout();
		}

		return GameConfig.DEFAULT_TIMEOUT;
	}

	public void offerADraw() {
		if (remotePlayingSide != null) {
			remotePlayingSide.offerADraw();
			lastStepDrawHasBeenOffered = chessBoard.getStepNumber();
		}
	}

	public void giveUp() {
		if (remotePlayingSide != null) {
			remotePlayingSide.giveUp();
		}
	}

}
