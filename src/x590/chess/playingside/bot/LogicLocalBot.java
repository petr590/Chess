package x590.chess.playingside.bot;

import x590.chess.board.Snapshot;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.util.Logger;
import x590.util.Util;

import java.util.List;

/**
 * Представляет бота, который анализирует позицию и выбирает лучший ход
 */
public class LogicLocalBot extends AbstractLocalBot {

	@Override
	public void onMoveMade(IMove move) {
		recordStartTime();

		var chessBoard = getBoardPanel().getChessBoard();
		var currentSide = chessBoard.currentSide();
		var snapshots = chessBoard.getAllSnapshots();

		int maxNice = snapshots.values().stream()
				.flatMap(stepSnapshots -> stepSnapshots.values().stream())
				.mapToInt(snapshot -> snapshot.getNice(currentSide))
				.max().orElse(Snapshot.NONE_NICE);

		Logger.debug(maxNice);

		if (maxNice == Snapshot.NONE_NICE) {
			Logger.debug("У бота не ходов :(");
			return;
		}

		List<Snapshot> theMostNiceSnapshots = snapshots.values().stream()
				.flatMap(stepSnapshots -> stepSnapshots.values().stream())
				.filter(snapshot -> snapshot.getNice(currentSide) == maxNice)
				.toList();

		long size = snapshots.values().stream()
				.mapToLong(stepSnapshots -> stepSnapshots.values().size()).sum();

		waitAndThen(() -> {
			Logger.debug(size, theMostNiceSnapshots.size(), theMostNiceSnapshots);

			Snapshot randomSnapshot = Util.getRandomByThreadLocalRandom(theMostNiceSnapshots);
			getBoardPanel().makeStep(randomSnapshot.getPos(), randomSnapshot.getStep());
		});
	}

	@Override
	public Figure queryPawnTurningFigure(Side side) {
		return Util.getRandomByThreadLocalRandom(Figure.getPawnTurningFigures(side));
	}

	@Override
	public boolean shouldMakeSnapshots() {
		return true;
	}
}
