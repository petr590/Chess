package x590.chess.playingside;

import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.board.BoardPanel;

/**
 * Локальный игрок на текущем устройстве
 */
public class LocalPlayer implements PlayingSide {

	@Override
	public void setup(BoardPanel boardPanel) {}

	@Override
	public void onMoveMake(IMove move) {}

	@Override
	public void onMoveCancel(IMove move, IStep prevStep) {}

	@Override
	public void onLastCanceledMoveRepeat(IMove move) {}

	@Override
	public void onGameEnd() {}

	@Override
	public boolean canMakeMove() {
		return true;
	}

	@Override
	public boolean canSelectField() {
		return true;
	}

	@Override
	public boolean canCancelMove() {
		return false;
	}
}
