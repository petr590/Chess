package x590.chess.gui.board;

import x590.chess.Main;
import x590.chess.config.GameConfig;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.chess.figure.step.IStep;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.step.StepResult;
import x590.chess.gui.game.GamePanel;
import x590.chess.gui.GuiUtil;
import x590.chess.playingside.PlayingSide;
import x590.chess.playingside.remote.RemotePlayingSide;
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

	/** Сторона, за которую играет игрок */
	private final Side thisSide;

	private final boolean isWithSelf;

	/** Поля шахматной доски */
	private final FieldPanel[][] panels = new FieldPanel[SIZE][SIZE];

	/** Текущее выбранное поле */
	private @Nullable FieldPanel selected;

	/** Последний ход, на котором была предложена ничья */
	private int lastStepDrawHasBeenOffered = -1;

	private boolean gameEnded;


	public BoardPanel(ChessBoard chessBoard, Supplier<Side> sideSupplier, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide, GamePanel gamePanel, boolean isWithSelf) {
		super(new GridBagLayout());

		this.chessBoard = chessBoard;
		this.gamePanel = gamePanel;

		thisPlayingSide.setup(this);
		opponentPlayingSide.setup(this);

		Side side = this.thisSide = sideSupplier.get(); // sideSupplier.get() должен вызываться только после вызова PlayingSide.setup(BoardPanel)

		this.whitePlayingSide = side.choose(thisPlayingSide, opponentPlayingSide);
		this.blackPlayingSide = side.choose(opponentPlayingSide, thisPlayingSide);

		this.remotePlayingSide = opponentPlayingSide instanceof RemotePlayingSide remote ? remote : null;

		this.currentPlayingSide = chessBoard.currentSide().choose(whitePlayingSide, blackPlayingSide);

		this.isWithSelf = isWithSelf;

		chessBoard.setup(currentPlayingSide);


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
		add(new CornerPanel(0, 0,  1, 0,  1, 1), GuiUtil.constraintsWithCoords(constraints, GRID_START, GRID_END));
		add(new CornerPanel(0, 0,  0, 1,  1, 1), GuiUtil.constraintsWithCoords(constraints, GRID_END,   GRID_START));
		add(new CornerPanel(0, 0,  0, 1,  1, 0), GuiUtil.constraintsWithCoords(constraints, GRID_END,   GRID_END));
	}


	public PlayingSide getCurrentPlayingSide() {
		return currentPlayingSide;
	}


	public PlayingSide getThisPlayingSide() {
		return thisSide.choose(whitePlayingSide, blackPlayingSide);
	}

	public PlayingSide getOpponentPlayingSide() {
		return thisSide.choose(blackPlayingSide, whitePlayingSide);
	}

	public void select(FieldPanel fieldPanel) {
		var selected = this.selected;

		if (selected != null) {
			selected.unselect();
			clearPossibleSteps();
		}

		this.selected = fieldPanel;
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

	private @Nullable Pos unselect() {
		var selected = this.selected;

		if (selected == null)
			return null;

		this.selected = null;
		selected.unselect();
		return selected.getPos();
	}

	public boolean canSelect(FieldPanel fieldPanel) {
		return !gameEnded && currentPlayingSide.canSelectField() && fieldPanel != selected;
	}

	public FieldPanel getFieldPanel(Pos pos) {
		return panels[pos.getY()][pos.getX()];
	}

	public ChessBoard getChessBoard() {
		return chessBoard;
	}

	/**
	 * @return Сторону, за которую играет игрок
	 */
	public Side getThisSide() {
		return thisSide;
	}

	public Side currentSide() {
		return chessBoard.currentSide();
	}

	public GamePanel getGamePanel() {
		return gamePanel;
	}

	public void setOpponentName(String name) {
		gamePanel.setOpponentName(name);
	}

	private static final String
			SAVE_GAME = "Сохранить партию",
			NEW_GAME = "Новая партия",
			EXIT = "Выйти";


	private PlayingSide opponentPlayingSide() {
		return currentSide().choose(blackPlayingSide, whitePlayingSide);
	}


	public void makeStep(IStep step) {
		if (currentPlayingSide.canMakeMove() && opponentPlayingSide().ready()) {
			clearVisualMarking();
			makeMove(step.asMove(unselect(), chessBoard));
		}
	}

	public void makeStep(Pos startPos, IStep step) {
		if (currentPlayingSide.canMakeMove() && opponentPlayingSide().ready()) {
			clearVisualMarking();
			makeMove(step.asMove(startPos, chessBoard));
		}
	}


	/**
	 * Осуществляет ход и обновляет состояние доски
	 */
	public void makeMove(IMove move) {
		makeMove(move, PlayingSide::onMoveMake, PlayingSide::onMoveMade, gamePanel -> gamePanel.onMoveMake(move));
	}

	/**
	 * Повторяет последний отменённый ход и обновляет состояние доски
	 */
	public void repeatLastCanceledMove(IMove move) {
		clearVisualMarkingAndUnselect();
		makeMove(move, PlayingSide::onLastCanceledMoveRepeat, (p, m) -> {}, GamePanel::onLastCanceledMoveRepeat);
	}

	private void makeMove(IMove move,
						  BiConsumer<PlayingSide, IMove> playingSideOnMoveMake,
						  BiConsumer<PlayingSide, IMove> playingSideOnMoveMade,
						  Consumer<GamePanel> gamePanelOnMoveMake) {

		PlayingSide oppositePlayingSide = chessBoard.currentSide().choose(blackPlayingSide, whitePlayingSide);

		StepResult result = chessBoard.makeMove(move, currentPlayingSide, oppositePlayingSide);
		currentPlayingSide = oppositePlayingSide;

		repaintChanged(move);
		updateCursors();

		playingSideOnMoveMake.accept(currentPlayingSide, move);
		gamePanelOnMoveMake.accept(gamePanel);
		updateDrawOfferButton();

		if (result.isKingAttacked()) {
			getFieldPanel(chessBoard.getKingPos()).setAttacked(true);
		}

		if (!result.canContinueGame()) {
			String message = result.getMessage(chessBoard.currentSide());

			endGame(message);

			String chose = GuiUtil.showOptionDialog(message, "",
							SAVE_GAME, NEW_GAME, EXIT);

			if (chose != null) {
				switch (chose) {
					case NEW_GAME -> {
						chessBoard.resetAllToDefault();
						clearVisualMarkingAndUnselect();
						repaintAll();
					}

					case SAVE_GAME -> {
						// TODO
					}

					case EXIT -> Main.exitNormally();
				}
			}

		} else {
			playingSideOnMoveMade.accept(currentPlayingSide, move);
		}
	}


	public boolean canCancelMove() {
		return !gameEnded && currentPlayingSide.canCancelMove();
	}

	public boolean canRepeatLastCanceledMove() {
		return !gameEnded;
	}

	public void cancelMove(IMove move, @Nullable IStep prevStep) {
		clearVisualMarkingAndUnselect();

		PlayingSide oppositePlayingSide = chessBoard.currentSide().choose(blackPlayingSide, whitePlayingSide);

		chessBoard.cancelMove(move, prevStep, oppositePlayingSide);

		currentPlayingSide.onMoveCancel(move, prevStep);
		currentPlayingSide = oppositePlayingSide;

		repaintChanged(move);
		updateCursors();

		if (chessBoard.isKingAttacked()) {
			getFieldPanel(chessBoard.getKingPos()).setAttacked(true);
		}
	}

	private void clearVisualMarking() {
		forEachPanel(panel -> {
			panel.setPossibleStep(null);
			panel.setAttacked(false);
		});
	}

	private void clearPossibleSteps() {
		forEachPanel(panel -> panel.setPossibleStep(null));
	}

	private void clearVisualMarkingAndUnselect() {
		clearVisualMarking();
		unselect();
	}

	private void updateCursors() {
		forEachPanel(FieldPanel::updateCursor);
	}

	private void repaintChanged(IMove move) {
		getFieldPanel(move.startPos()).repaint();

		Pos targetPos = move.targetPos(),
			takePos = move.takePos();

		getFieldPanel(targetPos).repaint();

		if (!targetPos.equals(takePos)) {
			getFieldPanel(takePos).repaint();
		}

		@Nullable IExtraMove extraMove = move.extraMove();

		if (extraMove != null) {
			repaintChanged(extraMove);
		}
	}

	private void repaintAll() {
		forEachPanel(FieldPanel::repaint);
	}


	private void forEachPanel(Consumer<FieldPanel> eachFunction) {
		for (FieldPanel[] row : panels) {
			for (FieldPanel panel : row) {
				eachFunction.accept(panel);
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

	public void lockGameEndButtons() {
		gamePanel.lockGameEndButtons();
	}

	public void unlockGameEndButtons() {
		gamePanel.unlockGameEndButtons();
	}

	public int getOfferADrawTimeout() {
		return remotePlayingSide != null ?
				remotePlayingSide.getOfferADrawTimeout() :
				GameConfig.DEFAULT_TIMEOUT;
	}

	public void offerADraw() {
		if (remotePlayingSide != null) {
			remotePlayingSide.offerADraw();
			lastStepDrawHasBeenOffered = chessBoard.getStepNumber();
		}
	}

	public void giveUp() {
		if (remotePlayingSide != null) {
			remotePlayingSide.onGiveUp();
		}

		endGame((isWithSelf ? currentSide() : thisSide).getGiveUpMessage());
	}

	/**
	 * Завершает игру и устанавливает текст строки состояния
	 */
	public void endGame(String state) {
		gameEnded = true;
		gamePanel.endGame(state);
		whitePlayingSide.onGameEnd();
		blackPlayingSide.onGameEnd();
		clearVisualMarkingAndUnselect();
		updateCursors();
	}
}
