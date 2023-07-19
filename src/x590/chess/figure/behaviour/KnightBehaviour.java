package x590.chess.figure.behaviour;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Direction;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightBehaviour extends AbstractFigureBehaviour {

	@Override
	public Collection<Pos> getSteps(ChessBoard board, Side side, Pos current, StepGettingType type) {
		List<Pos> possibleSteps = new ArrayList<>();

		for (Direction direction : Direction.KNIGHT) {
			var pos = current.relative(direction);
			if (pos != null && type.includeField(board, side, pos)) {
				possibleSteps.add(pos);
			}
		}

		return possibleSteps;
	}
}
