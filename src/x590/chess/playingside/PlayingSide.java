package x590.chess.playingside;

import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Реализует логику работы играющей стороны -
 * это может быть игрок на текущем устройстве, игрок в локальной сети или, например, бот
 */
public interface PlayingSide {

	/**
	 * @return Имя игрока
	 */
	String getName();

	/**
	 * Вызывается один раз при инициализации игры
	 */
	default void setup(BoardPanel boardPanel) {}

	/**
	 * Вызывается при совершении хода
	 */
	default void onMoveMake(IMove move) {}

	/**
	 * Вызывается при завершении хода
	 */
	default void onMoveMade(IMove move) {}

	/**
	 * Вызывается при отмене хода
	 */
	default void onMoveCancel(IMove move, @Nullable IStep prevStep) {}

	/**
	 * Вызывается при повторении отменённого хода
	 */
	default void onLastCanceledMoveRepeat(IMove move) {}

	/**
	 * Вызывается по окончании игры
	 */
	default void onGameEnd() {}


	/**
	 * @return {@code true}, если мы можем совершить ход
	 */
	default boolean canMakeMove() {
		return true;
	}


	/**
	 * @return {@code true}, если сторона готова совершить ход
	 */
	default boolean ready() {
		return true;
	}

	/**
	 * @return {@code true}, если мы можем выбрать поле на доске
	 */
	boolean canSelectField();

	/**
	 * @return {@code true}, если мы можем отменить ход
	 */
	default boolean canCancelMove() {
		return false;
	}

	/**
	 * @return Строку состояния в начале игры
	 */
	default String getInitialState() {
		return Side.WHITE.getState();
	}

	/**
	 * @return Строку состояния в конце очередного хода
	 */
	default String getState(BoardPanel boardPanel) {
		return boardPanel.currentSide().getState(boardPanel.getChessBoard().isKingAttacked());
	}

	/**
	 * @return Иконку строки состояния в конце очередного хода
	 */
	default @Nullable Icon getStateIcon(BoardPanel boardPanel) {
		return null;
	}

	/**
	 * @return Дополнительный компонент строки состояния в начале игры
	 */
	default @Nullable JComponent getAdditionalInitialState() {
		return null;
	}

	/**
	 * @return Выбранную фигуру, в которую превратится пешка
	 */
	default Figure queryPawnTurningFigure(Side side) {
		Object[] icons = Figure.getPawnTurningIcons(side).toArray();
		List<Figure> figures = Figure.getPawnTurningFigures(side);

		Figure resultFigure;

		do {
			resultFigure = GuiUtil.showOptionDialog("Выберите фигуру для превращения", "", icons, figures);
		} while (resultFigure == null);

		return resultFigure;
	}

	/**
	 * @return {@code true}, если для текущей стороны необходимо делать снимки позиций для каждого хода
	 */
	default boolean shouldMakeSnapshots() {
		return false;
	}

	default boolean shouldLockGameEndButtons() {
		return false;
	}
}
