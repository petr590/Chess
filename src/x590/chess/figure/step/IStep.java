package x590.chess.figure.step;

import x590.chess.Pos;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.util.annotation.Nullable;

/**
 * Представляет отдельный ход (изначальная позиция задаются из контекста, в котором этот ход используется)
 */
public interface IStep {

	enum Type {
		PLAIN,
		CASTLING,
		DOUBLE_PAWN_STEP,
		TAKING_PAWN_ON_THE_PASS,
		TURNING_A_PAWN
	}

	interface IExtraStep extends IStep {

		Pos startPos();

		@Override
		default Pos takePos() {
			return targetPos();
		}

		IExtraMove asMove(ChessBoard board);

		@Override
		default IExtraMove asMove(Pos startPos, ChessBoard board) {
			return asMove(board);
		}
	}

	/**
	 * @return Целевую позицию. Должна совпадать с {@link #takePos()} или должна быть свободной
	 */
	Pos targetPos();

	/**
	 * @return Позицию взятия фигуры. Если фактического взятия не происходит,
	 * должен быть равен {@link #targetPos()}
	 */
	Pos takePos();

	/**
	 * @return Тип хода
	 */
	Type type();

	/**
	 * @return Дополнительный ход. Некоторые ходы (например, рокировка) требуют перемещения двух фигур.
	 * Дополнительный ход не должен брать никакую фигуру.
	 */
	default @Nullable IExtraStep extraStep() {
		return null;
	}

	/**
	 * @return Фигуру в результате хода или {@code null}, если фигура та же самая.
	 */
	default @Nullable Figure resultFigure() {
		return null;
	}

	/**
	 * @return Ход как экземпляр {@link IMove}
	 */
	IMove asMove(Pos startPos, ChessBoard board);
}
