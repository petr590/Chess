package x590.chess.figure.move;

import x590.chess.figure.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.FigureType;
import x590.chess.figure.Side;
import x590.util.annotation.Nullable;

public record Move(Pos startPos, Pos targetPos, Pos takePos, Type type,
				   Figure figure, @Nullable Figure resultFigure, @Nullable Figure takenFigure, @Nullable IExtraMove extraMove) implements IMove {

	public Move(Pos startPos, Pos targetPos, Pos takePos, Type type, Figure figure) {
		this(startPos, targetPos, takePos, type, figure, null, null, null);
	}

	public Move(Pos startPos, Pos targetPos, Pos takePos, Type type, Figure figure, @Nullable Figure resultFigure) {
		this(startPos, targetPos, takePos, type, figure, resultFigure, null, null);
	}

	public Move(Pos startPos, Pos targetPos, Pos takePos, Type type, Figure figure, @Nullable Figure resultFigure, @Nullable Figure takenFigure) {
		this(startPos, targetPos, takePos, type, figure, resultFigure, takenFigure, null);
	}

	public static Move castling(Pos startPos, Pos kingTargetPos, Pos rookStartPos, Pos rookTargetPos, Side side) {
		return new Move(
				startPos,
				kingTargetPos,
				kingTargetPos,
				Type.CASTLING,
				Figure.valueOf(side, FigureType.KING),
				null, null,
				new SimpleExtraMove(rookStartPos, rookTargetPos, Type.CASTLING, Figure.valueOf(side, FigureType.ROOK))
		);
	}
}
