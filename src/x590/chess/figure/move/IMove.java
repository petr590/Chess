package x590.chess.figure.move;

import x590.chess.Pos;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.step.IStep;
import x590.util.annotation.Nullable;

public interface IMove extends IStep {

	interface IExtraMove extends IMove, IExtraStep {

		@Override
		default IExtraMove asMove(Pos startPos, ChessBoard board) {
			return this;
		}

		@Override
		default IExtraMove asMove(ChessBoard board) {
			return this;
		}
	}

	/**
	 * @return Стартовую позицию хода
	 */
	Pos startPos();

	/**
	 * @return Фигуру, которая совершает ход
	 */
	Figure figure();

	@Override
	default IMove asMove(Pos startPos, ChessBoard board) {
		return this;
	}


	default @Nullable IExtraMove extraStep() {
		return extraMove();
	}


	default @Nullable IExtraMove extraMove() {
		return null;
	}


	default String toConvenientString() {

		var figure = figure();
		var resultFigure = resultFigure();

		StringBuilder str = new StringBuilder()
				.append(startPos()).append('-').append(targetPos()).append(' ').append(figure.getEmoji());

		if (resultFigure != null && figure != resultFigure) {
			str.append(" \uD83E\uDC12 ").append(resultFigure.getEmoji());
		}

		var extraMove = extraMove();

		if (extraMove != null) {
			str.append(", ").append(extraMove);
		}

		return str.toString();
	}
}
