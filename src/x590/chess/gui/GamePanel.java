package x590.chess.gui;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.board.BoardPanel;
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
			REPEAT_LAST_MOVE = "repeatLastMove";

	private final BoardPanel boardPanel;

	private final TakenFiguresPanel whiteTakenFiguresPanel, blackTakenFiguresPanel;

	private final MovesPanel movesPanel;
	private final GameButtonsPanel gameButtonsPanel;
	private final StatePanel statePanel;

	public GamePanel(ChessBoard board, Supplier<Side> sideSupplier, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide) {
		super(new GridBagLayout());
		setAlignmentY(CENTER_ALIGNMENT);

		this.boardPanel = new BoardPanel(board, sideSupplier, thisPlayingSide, opponentPlayingSide, this);

		Side side = boardPanel.getThisSide();

		this.whiteTakenFiguresPanel = new TakenFiguresPanel(board, Side.WHITE, side == Side.WHITE);
		this.blackTakenFiguresPanel = new TakenFiguresPanel(board, Side.BLACK, side == Side.BLACK);

		this.movesPanel = new MovesPanel(boardPanel);
		this.gameButtonsPanel = new GameButtonsPanel(this, movesPanel);
		this.statePanel = new StatePanel(opponentPlayingSide.getInitialState());

		var constraints = new GridBagConstraints();

		add(gameButtonsPanel,                                            GuiUtil.constraintsWithCoords(constraints, 0, 2));
		add(side.choose(whiteTakenFiguresPanel, blackTakenFiguresPanel), GuiUtil.constraintsWithCoords(constraints, 1, 0));
		add(statePanel,                                                  GuiUtil.constraintsWithCoords(constraints, 1, 1));
		add(boardPanel,                                                  GuiUtil.constraintsWithCoords(constraints, 1, 2));
		add(side.choose(blackTakenFiguresPanel, whiteTakenFiguresPanel), GuiUtil.constraintsWithCoords(constraints, 1, 3));
		add(movesPanel,                                                  GuiUtil.constraintsWithCoords(constraints, 2, 2));


		var inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), CANCEL_LAST_MOVE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REPEAT_LAST_MOVE);

		var actionMap = getActionMap();
		actionMap.put(CANCEL_LAST_MOVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (boardPanel.canCancelMove()) {
					cancelMove();
				}
			}
		});

		actionMap.put(REPEAT_LAST_MOVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				repeatLastCanceledMove();
			}
		});
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
		}

		updateState();
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
		statePanel.setText(boardPanel.currentSide().getState());
	}

	/**
	 * Обновляет список взятых фигур у текущей стороны, если {@code opposite} равен {@code false},
	 * или у противоположной стороны, {@code opposite} равен {@code true}
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

	public void offerADraw() {
		boardPanel.offerADraw();
	}

	public void giveUp() {
		boardPanel.giveUp();
	}
}
