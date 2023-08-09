package x590.chess.figure.behaviour;

import x590.chess.figure.step.IStep;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;

import java.util.List;

/**
 * Описывает поведение фигуры
 */
public interface FigureBehaviour {

	/** @return Список ходов, которые может совершить фигура.
	 * Каждая позиция не должна выходить за пределы доски.
	 * @param board шахматная доска
	 * @param side сторона фигуры
	 * @param current текущая позиция фигуры */
	List<? extends IStep> getPossibleSteps(ChessBoard board, Side side, Pos current);

	/** @return Список полей, которые контролирует фигура
	 * (в том числе и занятые своими или вражескими фигурами).
	 * Каждая позиция не должна выходить за пределы доски.
	 * @param board шахматная доска
	 * @param side сторона фигуры
	 * @param current текущая позиция фигуры */
	List<Pos> getControlledFields(ChessBoard board, Side side, Pos current);
}
