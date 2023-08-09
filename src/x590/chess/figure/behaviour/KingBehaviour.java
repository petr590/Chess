package x590.chess.figure.behaviour;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Direction;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.move.Move;
import x590.chess.figure.step.IStep;

import java.util.ArrayList;
import java.util.List;

public class KingBehaviour extends AbstractFigureBehaviour {

	@Override
	public List<Pos> getSteps(ChessBoard board, Side side, Pos current, StepGettingType type) {
		List<Pos> possibleSteps = new ArrayList<>();

		for (Direction direction : Direction.VERTICAL_HORIZONTAL_AND_DIAGONAL) {
			var pos = current.relative(direction);

			if (pos != null && type.includeField(board, side, pos) && !board.isAttackedBySide(pos, side.opposite())) {
				possibleSteps.add(pos);
			}
		}

		return possibleSteps;
	}

	@Override
	public List<? extends IStep> getPossibleSteps(ChessBoard board, Side side, Pos current) {

		@SuppressWarnings("unchecked")
		List<IStep> possibleSteps = (List<IStep>) super.getPossibleSteps(board, side, current);

		int startY = side.getStartY();

		if (!board.isKingWalked() && current.equals(4, startY)) {
			if (!board.isARookWalked()) {
				var rookCastlingPos = Pos.of(3, startY);
				var kingCastlingPos = Pos.of(2, startY);
				var freePos = Pos.of(1, startY);

				Side opposite = side.opposite();

				if (!board.isAttackedBySide(current, opposite) &&
					canDoCastling(board, rookCastlingPos, opposite) &&
					canDoCastling(board, kingCastlingPos, opposite) &&
					board.freeAt(freePos)
				) {
					possibleSteps.add(Move.castling(current, kingCastlingPos, Pos.of(0, startY), rookCastlingPos, side));
				}
			}

			if (!board.isHRookWalked()) {
				var rookCastlingPos = Pos.of(5, startY);
				var kingCastlingPos = Pos.of(6, startY);

				Side opposite = side.opposite();

				if (!board.isAttackedBySide(current, opposite) &&
					canDoCastling(board, rookCastlingPos, opposite) &&
					canDoCastling(board, kingCastlingPos, opposite)
				) {
					possibleSteps.add(Move.castling(current, kingCastlingPos, Pos.of(7, startY), rookCastlingPos, side));
				}
			}
		}

		return possibleSteps;
	}

	private static boolean canDoCastling(ChessBoard board, Pos pos, Side opposite) {
		return board.freeAt(pos) && !board.isAttackedBySide(pos, opposite);
	}
}
