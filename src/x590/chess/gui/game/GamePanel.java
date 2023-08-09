package x590.chess.gui.game;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.gui.linked.LinkedPanel;
import x590.chess.playingside.PlayingSide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Панель, на которой располагаются все элементы игры
 */
public class GamePanel extends JPanel {

	private static final String
			CANCEL_LAST_MOVE = "cancelLastMove",
			REPEAT_LAST_CANCELED_MOVE = "repeatLastCanceledMove";

	private final BoardPanel boardPanel;

	private final TakenFiguresPanel whiteTakenFiguresPanel, blackTakenFiguresPanel;

	private final NameLabel thisName, opponentName;
	private final MovesPanel movesPanel;
	private final GameButtonsPanel gameButtonsPanel;
	private final StatePanel statePanel;

	public GamePanel(ChessBoard board, Supplier<Side> sideSupplier, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide, boolean isWithSelf) {
		super(new GridBagLayout());
		setAlignmentY(CENTER_ALIGNMENT);

		this.boardPanel = new BoardPanel(board, sideSupplier, thisPlayingSide, opponentPlayingSide, this, isWithSelf);

		Side side = boardPanel.getThisSide();

		this.whiteTakenFiguresPanel = new TakenFiguresPanel(board, Side.WHITE, side == Side.WHITE);
		this.blackTakenFiguresPanel = new TakenFiguresPanel(board, Side.BLACK, side == Side.BLACK);

		this.thisName = new NameLabel(boardPanel.getThisPlayingSide().getName());
		this.opponentName = new NameLabel(boardPanel.getOpponentPlayingSide().getName());
		thisName.linkTo(opponentName);
		this.movesPanel = new MovesPanel(boardPanel);
		LinkedPanel rightPanel = createRightPanel();

		this.gameButtonsPanel = new GameButtonsPanel(this, rightPanel, boardPanel.getOpponentPlayingSide().shouldLockGameEndButtons());
		this.statePanel = new StatePanel(opponentPlayingSide.getInitialState(), opponentPlayingSide.getAdditionalInitialState());

		var constraints = new GridBagConstraints();

		add(gameButtonsPanel,                                            GuiUtil.constraintsWithCoords(constraints, 0, 2));
		add(side.choose(whiteTakenFiguresPanel, blackTakenFiguresPanel), GuiUtil.constraintsWithCoords(constraints, 1, 0));
		add(statePanel,                                                  GuiUtil.constraintsWithCoords(constraints, 1, 1));
		add(boardPanel,                                                  GuiUtil.constraintsWithCoords(constraints, 1, 2));
		add(side.choose(blackTakenFiguresPanel, whiteTakenFiguresPanel), GuiUtil.constraintsWithCoords(constraints, 1, 3));
		add(rightPanel,                                                  GuiUtil.constraintsWithCoords(constraints, 2, 2));


		var inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), CANCEL_LAST_MOVE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REPEAT_LAST_CANCELED_MOVE);

		var actionMap = getActionMap();

		actionMap.put(CANCEL_LAST_MOVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (boardPanel.canCancelMove()) {
					cancelMove();
				}
			}
		});

		actionMap.put(REPEAT_LAST_CANCELED_MOVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (boardPanel.canRepeatLastCanceledMove()) {
					repeatLastCanceledMove();
				}
			}
		});
	}

	private LinkedPanel createRightPanel() {
		LinkedPanel rightPanel = new LinkedPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		rightPanel.add(thisName);
		rightPanel.add(opponentName);
		rightPanel.add(movesPanel);

		return rightPanel;
	}

	public void setOpponentName(String name) {
		if (opponentName != null)
			opponentName.setText(name);
	}

	/**
	 * Вызывается при совершении хода
	 */
	public void onMoveMake(IMove move) {
		updateTakenFigures(false);
		movesPanel.addMove(move);
		updateState();
	}

	/**
	 * Вызывается при повторении отменённого хода
	 */
	public void onLastCanceledMoveRepeat() {
		updateTakenFigures(false);
		updateState();
	}

	/**
	 * Отменяет последний ход, если он есть
	 */
	public void cancelMove() {
		cancelMove(lastMove -> {}, prevStep -> {});
	}

	/**
	 * Отменяет последний ход. Проверяет, что отменённый ход равен {@code receivedLastMove}
	 * и предыдущий ход равен {@code receivedPrevStep}
	 * @throws IllegalArgumentException если одна из пар ходов не совпадает
	 */
	public void cancelMove(IMove receivedLastMove, IStep receivedPrevStep) {
		cancelMove(
				lastMove -> checkReceivedLastMove(lastMove, receivedLastMove),
				prevStep -> checkReceivedPrevStep(prevStep, receivedPrevStep)
		);
	}

	private void cancelMove(Consumer<IMove> lastMoveChecker, Consumer<IStep> prevStepChecker) {
		IMove lastMove = movesPanel.cancelLastMove();
		IStep prevStep = movesPanel.getLastMove();

		lastMoveChecker.accept(lastMove);
		prevStepChecker.accept(prevStep);

		if (lastMove != null) {
			boardPanel.cancelMove(lastMove, prevStep);
			updateTakenFigures(true);
			updateState();
		}
	}

	/**
	 * Повторяет последний отменённый ход, если он есть
	 */
	public void repeatLastCanceledMove() {
		repeatLastCanceledMove(move -> {});
	}

	/**
	 * Повторяет последний отменённый ход. Проверяет, что отменённый ход равен {@code receivedMove}
	 * @throws IllegalArgumentException если ходы не совпадают
	 */
	public void repeatLastCanceledMove(IMove receivedMove) {
		repeatLastCanceledMove(move -> checkReceivedMove(move, receivedMove));
	}

	private void repeatLastCanceledMove(Consumer<IMove> moveChecker) {
		IMove move = movesPanel.repeatLastCanceledMove();

		moveChecker.accept(move);

		if (move != null) {
			boardPanel.repeatLastCanceledMove(move);
		}
	}

	private static void checkReceivedLastMove(IMove lastMove, IMove receivedLastMove) {
		if (!IMove.equals(lastMove, receivedLastMove)) {
			throw new IllegalStateException("lastMove and receivedLastMove are not matches." +
					" lastMove: " + lastMove + ", receivedLastMove: " + receivedLastMove);
		}
	}

	private static void checkReceivedPrevStep(IStep prevStep, IStep receivedPrevStep) {
		if (!IStep.equals(prevStep, receivedPrevStep)) {
			throw new IllegalStateException("prevStep and receivedPrevStep are not matches." +
					" prevStep: " + prevStep + ", receivedPrevStep: " + receivedPrevStep);
		}
	}

	private static void checkReceivedMove(IMove move, IMove receivedMove) {
		if (!IMove.equals(move, receivedMove)) {
			throw new IllegalStateException("move and receivedMove are not matches." +
					" move: " + move + ", receivedMove: " + receivedMove);
		}
	}

	/**
	 * Обновляет текст в {@link #statePanel}
	 */
	public void updateState() {
		var boardPanel = this.boardPanel;
		var currentPlayingSide = boardPanel.getCurrentPlayingSide();

		statePanel.setState(
				currentPlayingSide.getState(boardPanel),
				currentPlayingSide.getStateIcon(boardPanel)
		);
	}

	/**
	 * Обновляет список взятых фигур у текущей стороны, если {@code opposite} равен {@code false},
	 * или у противоположной стороны, если {@code opposite} равен {@code true}
	 */
	private void updateTakenFigures(boolean opposite) {
		var chessBoard = boardPanel.getChessBoard();

		Side side = chessBoard.currentSide().oppositeIf(opposite);

		if (chessBoard.takenFiguresListChanged(side)) {
			side.choose(whiteTakenFiguresPanel, blackTakenFiguresPanel).updateFiguresList();
		}
	}

	/**
	 * @return {@code true}, если это игра по сети
	 */
	public boolean isRemote() {
		return boardPanel.isRemote();
	}

	public void lockDrawOfferButton() {
		gameButtonsPanel.lockDrawOfferButton();
	}

	public void unlockDrawOfferButton() {
		gameButtonsPanel.unlockDrawOfferButton();
	}

	public void lockGameEndButtons() {
		gameButtonsPanel.lockGameEndButtons();
	}

	public void unlockGameEndButtons() {
		gameButtonsPanel.unlockGameEndButtons();
	}

	public void offerADraw() {
		boardPanel.offerADraw();
	}

	public void giveUp() {
		boardPanel.giveUp();
	}

	/**
	 * Завершает игру и устанавливает текст {@link #statePanel}
	 */
	public void endGame(String state) {
		statePanel.setState(state, null);
		gameButtonsPanel.onGameEnd();
	}
}
