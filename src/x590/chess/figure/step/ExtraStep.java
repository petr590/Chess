package x590.chess.figure.step;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Pos;
import x590.chess.figure.move.ExtraMove;
import x590.chess.figure.step.IStep.IExtraStep;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.util.annotation.Nullable;

public record ExtraStep(Pos startPos, Pos targetPos, Pos takePos, Type type,
						@Nullable Figure resultFigure, @Nullable IExtraStep extraStep) implements IExtraStep {

	@Override
	public IExtraMove asMove(ChessBoard board) {
		return new ExtraMove(startPos, targetPos, takePos, type,
				board.getNonNullFigure(startPos), resultFigure, board.getNonNullFigure(takePos),
				extraStep.asMove(board));
	}
}
