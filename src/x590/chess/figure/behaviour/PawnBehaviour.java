package x590.chess.figure.behaviour;

import x590.chess.figure.Figure;
import x590.chess.figure.FigureType;
import x590.chess.figure.move.Move;
import x590.chess.figure.move.TurningAPawnMove;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.Type;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Direction;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnBehaviour implements FigureBehaviour {

	private static final int
			DOUBLE_STEP_Y_WHITE = ChessBoard.START + 1,
			DOUBLE_STEP_Y_BLACK = ChessBoard.END - 1;

	@Override
	public Collection<? extends IStep> getPossibleSteps(ChessBoard board, Side side, Pos startPos) {
		List<IStep> possibleSteps = new ArrayList<>();

		Direction forward = side.choose(Direction.UP, Direction.DOWN);

		Pos forwardPos = startPos.relative(forward);

		Figure pawn = Figure.valueOf(side, FigureType.PAWN);

		if (forwardPos != null) {
			if (board.freeAt(forwardPos)) {
				addTurningAPawnStepsOrPlainStep(possibleSteps, board, side, startPos, forwardPos);

				if (startPos.getY() == side.choose(DOUBLE_STEP_Y_WHITE, DOUBLE_STEP_Y_BLACK)) {
					Pos doubleForwardPos = forwardPos.relative(forward);
					if (doubleForwardPos != null && board.freeAt(doubleForwardPos)) {
						possibleSteps.add(new Move(
								startPos,
								doubleForwardPos, doubleForwardPos,
								Type.DOUBLE_PAWN_STEP,
								pawn
						));
					}
				}
			}

			addStepIfCanTake(possibleSteps, board, side, startPos, forwardPos.relative(Direction.LEFT));
			addStepIfCanTake(possibleSteps, board, side, startPos, forwardPos.relative(Direction.RIGHT));

			IStep lastStep = board.getLastStep();

			if (lastStep != null && lastStep.type() == Type.DOUBLE_PAWN_STEP) {
				Pos targetPos = lastStep.targetPos();

				if (targetPos.equals(startPos.relative(Direction.LEFT)) ||
					targetPos.equals(startPos.relative(Direction.RIGHT))) {

					possibleSteps.add(new Move(
							startPos,
							targetPos.relative(forward),
							targetPos,
							Type.TAKING_PAWN_ON_THE_PASS,
							pawn, // figure
							null, // resultFigure == figure
							Figure.valueOf(side.opposite(), FigureType.PAWN) // takenFigure
					));
				}
			}
		}

		return possibleSteps;
	}

	@Override
	public Collection<Pos> getControlledFields(ChessBoard board, Side side, Pos startPos) {
		List<Pos> possibleSteps = new ArrayList<>();

		Pos forwardPos = startPos.relative(side.choose(Direction.UP, Direction.DOWN));

		if (forwardPos != null) {
			addPosIfCanTake(possibleSteps, board, side, forwardPos.relative(Direction.LEFT));
			addPosIfCanTake(possibleSteps, board, side, forwardPos.relative(Direction.RIGHT));
		}

		return possibleSteps;
	}

	private static void addStepIfCanTake(Collection<? super IStep> possibleSteps, ChessBoard board, Side side, Pos startPos, @Nullable Pos targetPos) {
		if (targetPos != null && board.canFigureBeTookBy(targetPos, side)) {
			addTurningAPawnStepsOrPlainStep(possibleSteps, board, side, startPos, targetPos);
		}
	}

	private static void addPosIfCanTake(Collection<? super Pos> possibleSteps, ChessBoard board, Side side, @Nullable Pos targetPos) {
		if (targetPos != null && board.canFigureBeTookBy(targetPos, side)) {
			possibleSteps.add(targetPos);
		}
	}

	/** Добавляет в коллекцию ход пешки с превращением, если возможно, иначе просто ход */
	private static void addTurningAPawnStepsOrPlainStep(Collection<? super IStep> steps, ChessBoard board, Side side, Pos startPos, Pos targetPos) {
		steps.add(targetPos.getY() == side.getEndY() ?
				new TurningAPawnMove(startPos, targetPos, side, board.getFigure(targetPos)) :
				targetPos);
	}
}
