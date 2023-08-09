package x590.chess.figure.behaviour;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Direction;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VectorFigureBehaviour extends AbstractFigureBehaviour {

	private final Set<Direction> possibleDirections;

	public VectorFigureBehaviour(Set<Direction> possibleDirections) {
		this.possibleDirections = possibleDirections;
	}


	@Override
	public List<Pos> getSteps(ChessBoard board, Side side, Pos current, StepGettingType type) {
		List<Pos> possibleSteps = new ArrayList<>();

		for (Direction direction : possibleDirections) {
			for (var pos = current.relative(direction); pos != null; pos = pos.relative(direction)) {

				if (type.includeField(board, side, pos)) {
					possibleSteps.add(pos);
				}

				if (board.hasFigure(pos)) {
					break;
				}
			}
		}

		return possibleSteps;
	}
}
