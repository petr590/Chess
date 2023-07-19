package x590.chess.playingside;

import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.board.BoardPanel;
import x590.util.annotation.Nullable;

/**
 * Реализует логику работы играющей стороны -
 * это может быть игрок на текущем устройстве, игрок в локальной сети или, например, бот
 */
public interface PlayingSide {

	/**
	 * Вызывается один раз при инициализации игры
	 */
	void setup(BoardPanel boardPanel);

	/**
	 * Вызывается при совершении хода
	 */
	void onMoveMake(IMove move);

	/**
	 * Вызывается при отмене хода
	 */
	void onMoveCancel(IMove move, @Nullable IStep prevStep);

	/**
	 * Вызывается при повторении отменённого хода
	 */
	void onLastCanceledMoveRepeat(IMove move);

	/**
	 * Вызывается по окончании игры
	 */
	void onGameEnd();


	/**
	 * @return {@code true}, если мы можем совершить ход
	 */
	boolean canMakeMove();

	/**
	 * @return {@code true}, если мы можем выбрать поле на доске
	 */
	boolean canSelectField();

	/**
	 * @return {@code true}, если мы можем отменить ход
	 */
	boolean canCancelMove();

	/**
	 * @return Строку состояния в начале игры
	 */
	default String getInitialState() {
		return Side.WHITE.getState();
	}
}
