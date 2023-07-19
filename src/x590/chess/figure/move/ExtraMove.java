package x590.chess.figure.move;

import x590.chess.figure.Figure;
import x590.chess.figure.Pos;
import x590.util.annotation.Nullable;

public record ExtraMove(Pos startPos, Pos targetPos, Pos takePos, Type type,
						Figure figure, @Nullable Figure resultFigure, @Nullable Figure takenFigure,
						@Nullable IExtraMove extraMove) implements IMove.IExtraMove {}
