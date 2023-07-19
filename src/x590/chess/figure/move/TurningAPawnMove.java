package x590.chess.figure.move;

import x590.chess.figure.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.FigureType;
import x590.chess.figure.Side;
import x590.chess.gui.GuiUtil;
import x590.util.annotation.Nullable;

import java.util.List;

/**
 * Представляет ход превращения пешки в какую-либо фигуру
 */
public class TurningAPawnMove implements IMove {

	private final Pos startPos, targetPos;
	private final Figure pawn, takenFigure;
	private @Nullable Figure resultFigure;


	public TurningAPawnMove(Pos startPos, Pos targetPos, Side pawnSide, Figure takenFigure) {
		this.startPos = startPos;
		this.targetPos = targetPos;
		this.pawn = Figure.valueOf(pawnSide, FigureType.PAWN);
		this.takenFigure = takenFigure;
	}


	@Override
	public Pos startPos() {
		return startPos;
	}

	@Override
	public Pos targetPos() {
		return targetPos;
	}

	@Override
	public Pos takePos() {
		return targetPos;
	}

	@Override
	public Figure figure() {
		return pawn;
	}

	@Override
	public @Nullable Figure takenFigure() {
		return takenFigure;
	}

	@Override
	public Type type() {
		return Type.TURNING_A_PAWN;
	}

	@Override
	public @Nullable Figure resultFigure() {
		return resultFigure;
	}

	@Override
	public Figure queryResultFigure(Side side) {
		Figure resultFigure = this.resultFigure;

		if (resultFigure != null) {
			return resultFigure;
		}

		Object[] icons = Figure.getPawnTurningIcons(side).toArray();
		List<Figure> figures = Figure.getPawnTurningFigures(side);

		do {
			resultFigure = GuiUtil.showOptionDialog("Выберите фигуру для превращения", "", icons, figures);
		} while (resultFigure == null);

		return this.resultFigure = resultFigure;
	}
}
