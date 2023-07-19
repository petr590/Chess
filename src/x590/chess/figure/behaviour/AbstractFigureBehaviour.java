package x590.chess.figure.behaviour;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Pos;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.step.IStep;
import x590.util.function.TriPredicate;

import java.util.Collection;

public abstract class AbstractFigureBehaviour implements FigureBehaviour {

	protected enum StepGettingType {
		POSSIBLE_STEPS((board, side, pos) -> {
			Figure figure = board.getFigure(pos);
			return figure == null || figure.canBeTookBy(side);
		}),

		CONTROLLED_FIELDS((board, side, pos) -> true);

		private final TriPredicate<ChessBoard, Side, Pos> includeField;

		StepGettingType(TriPredicate<ChessBoard, Side, Pos> includeField) {
			this.includeField = includeField;
		}

		public boolean includeField(ChessBoard board, Side side, Pos pos) {
			return includeField.test(board, side, pos);
		}
	}

	public Collection<? extends IStep> getPossibleSteps(ChessBoard board, Side side, Pos current) {
		return getSteps(board, side, current, StepGettingType.POSSIBLE_STEPS);
	}

	public Collection<Pos> getControlledFields(ChessBoard board, Side side, Pos current) {
		return getSteps(board, side, current, StepGettingType.CONTROLLED_FIELDS);
	}

	protected abstract Collection<Pos> getSteps(ChessBoard board, Side side, Pos current, StepGettingType type);
}
