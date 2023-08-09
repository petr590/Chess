package x590.chess.playingside.bot;

import x590.chess.figure.Figure;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.util.Logger;
import x590.util.Util;
import x590.util.annotation.Immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Представляет бота, который делает случайный ход
 */
public class RandomLocalBot extends AbstractLocalBot {

	@Override
	public void onMoveMade(IMove move) {
		recordStartTime();

		var chessBoard = getBoardPanel().getChessBoard();

		Map<Pos, @Immutable List<? extends IStep>> allSteps = new HashMap<>();
		List<Pos> positions = new ArrayList<>();

		chessBoard.forEachPossibleSteps((pos, possibleSteps) -> {
			if (!possibleSteps.isEmpty()) {
				positions.add(pos);
				allSteps.put(pos, possibleSteps);
			}
		});

		if (allSteps.isEmpty()) {
			Logger.debug("У бота нет ходов :(");
			return;
		}

		waitAndThen(() -> {
			Pos pos = Util.getRandomByThreadLocalRandom(positions);
			getBoardPanel().makeStep(pos, Util.getRandomByThreadLocalRandom(allSteps.get(pos)));
		});
	}

	@Override
	public Figure queryPawnTurningFigure(Side side) {
		return Util.getRandomByThreadLocalRandom(Figure.getPawnTurningFigures(side));
	}
}
