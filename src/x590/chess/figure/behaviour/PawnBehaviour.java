package x590.chess.figure.behaviour;

import x590.chess.figure.Figure;
import x590.chess.figure.FigureType;
import x590.chess.figure.move.Move;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.Type;
import x590.chess.board.ChessBoard;
import x590.chess.Direction;
import x590.chess.Pos;
import x590.chess.figure.Side;
import x590.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnBehaviour implements FigureBehaviour {

	private static final int
			DOUBLE_STEP_Y_WHITE = 1,
			DOUBLE_STEP_Y_BLACK = ChessBoard.END - DOUBLE_STEP_Y_WHITE;

	@Override
	public Collection<? extends IStep> getPossibleSteps(ChessBoard board, Side side, Pos current) {
		List<IStep> possibleSteps = new ArrayList<>();

		Direction forward = side.choose(Direction.UP, Direction.DOWN);

		Pos forwardPos = current.relative(forward);

		Figure pawn = Figure.valueOf(side, FigureType.PAWN);

		if (forwardPos != null) {
			if (board.freeAt(forwardPos)) {
				if (forwardPos.getY() != side.getEndY()) {
					possibleSteps.add(forwardPos);
				} else {
					for (FigureType type : FigureType.PAWN_TURNING_TYPES) {
						possibleSteps.add(new Move(
								current,
								forwardPos, forwardPos,
								Type.TURNING_A_PAWN,
								pawn,
								Figure.valueOf(side, type)
						));
					}
				}

				if (current.getY() == side.choose(DOUBLE_STEP_Y_WHITE, DOUBLE_STEP_Y_BLACK)) {
					Pos doubleForwardPos = forwardPos.relative(forward);
					if (doubleForwardPos != null && board.freeAt(doubleForwardPos)) {
						possibleSteps.add(new Move(
								current,
								doubleForwardPos, doubleForwardPos,
								Type.DOUBLE_PAWN_STEP,
								pawn
						));
					}
				}
			}

			addStepIfCanTake(possibleSteps, board, forwardPos.relative(Direction.LEFT), side);
			addStepIfCanTake(possibleSteps, board, forwardPos.relative(Direction.RIGHT), side);

			IStep lastStep = board.getLastStep();

			if (lastStep != null && lastStep.type() == Type.DOUBLE_PAWN_STEP) {
				Pos targetPos = lastStep.targetPos();

				if (targetPos.equals(current.relative(Direction.LEFT)) ||
					targetPos.equals(current.relative(Direction.RIGHT))) {

					possibleSteps.add(new Move(
							current,
							targetPos.relative(forward),
							targetPos,
							Type.TAKING_PAWN_ON_THE_PASS,
							pawn
					));
				}
			}
		}

		return possibleSteps;
	}

	@Override
	public Collection<Pos> getControlledFields(ChessBoard board, Side side, Pos current) {
		List<Pos> possibleSteps = new ArrayList<>();

		Pos forwardPos = current.relative(side.choose(Direction.UP, Direction.DOWN));

		if (forwardPos != null) {
			addStepIfCanTake(possibleSteps, board, forwardPos.relative(Direction.LEFT), side);
			addStepIfCanTake(possibleSteps, board, forwardPos.relative(Direction.RIGHT), side);
		}

		return possibleSteps;
	}

	private static void addStepIfCanTake(Collection<? super Pos> possibleSteps, ChessBoard board, @Nullable Pos pos, Side side) {
		if (pos != null && board.canFigureBeTookBy(pos, side)) {
			possibleSteps.add(pos);
		}
	}
}
