package x590.chess.figure.move;

import x590.chess.figure.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.move.IMove.IExtraMove;

public record SimpleExtraMove(Pos startPos, Pos targetPos, Type type, Figure figure) implements IExtraMove {}
