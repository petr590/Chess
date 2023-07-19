package x590.chess.figure.step;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Pos;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.Move;
import x590.util.annotation.Nullable;

public record Step(Pos targetPos, Pos takePos, Type type,
				   @Nullable Figure resultFigure, @Nullable IExtraStep extraStep) implements IStep {

	@Override
	public IMove asMove(Pos startPos, ChessBoard board) {
		return new Move(startPos, targetPos, takePos, type,
				board.getNonNullFigure(startPos), resultFigure, board.getNonNullFigure(takePos),
				extraStep.asMove(board));
	}
}
