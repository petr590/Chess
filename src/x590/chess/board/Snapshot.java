package x590.chess.board;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import x590.chess.figure.Figure;
import x590.chess.figure.Pos;
import x590.chess.figure.Side;
import x590.chess.figure.step.IStep;
import x590.util.ArrayUtil;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Представляет собой снимок позиции определённого хода
 */
public class Snapshot {

	public static final int NONE_NICE = Integer.MIN_VALUE;

	private static final int
			FIGURE_WORTH_MULTIPLIER = 3,
			STATE_WORTH_MULTIPLIER = 1;

	private final Pos pos;
	private final IStep step;

	private final Figure[][] board;
	private final AttackState[][] attackStates;

	private int nice = NONE_NICE;

	Snapshot(Pos pos, IStep step, Figure[][] board, AttackState[][] attackStates) {
		this.pos = pos;
		this.step = step;
		this.board = ArrayUtil.clone2dArray(board);
		this.attackStates = ArrayUtil.clone2dArray(attackStates);
	}

	public Pos getPos() {
		return pos;
	}

	public IStep getStep() {
		return step;
	}

	public int getNice(Side currentSide) {
		int nice = this.nice;

		if (nice != NONE_NICE) {
			return nice;
		}

		return this.nice = computeNice(currentSide);
	}

	private int computeNice(Side currentSide) {
		final var board = this.board;
		final var attackStates = this.attackStates;

		int nice = 0;

		for (int y = 0; y < ChessBoard.SIZE; y++) {
			final var boardRow = board[y];
			final var attackStatesRow = attackStates[y];

			for (int x = 0; x < ChessBoard.SIZE; x++) {
				Figure figure = boardRow[x];

				if (figure != null) {

					Side figureSide = figure.getSide();

					nice += (figureSide == currentSide ? figure.getWorth() : -figure.getWorth()) * FIGURE_WORTH_MULTIPLIER;

					Object2IntMap<Figure> figures = Objects.requireNonNull(attackStatesRow[x].getFigures());

					if (!figures.isEmpty()) {
						Map<Side, LinkedList<Figure>> groupedFigures = figures.object2IntEntrySet().stream()
								.flatMap(
										entry -> IntStream.range(1, entry.getIntValue()).mapToObj(k -> entry.getKey())
								).collect(Collectors.groupingBy(
										Figure::getSide,
										Collectors.toCollection(LinkedList::new)
								));

						LinkedList<Figure> thisFigures     = groupedFigures.computeIfAbsent(figureSide,            side -> new LinkedList<>());
						LinkedList<Figure> opponentFigures = groupedFigures.computeIfAbsent(figureSide.opposite(), side -> new LinkedList<>());

						thisFigures.add(figure);

						thisFigures.sort(Figure::compareByWorth);
						opponentFigures.sort(Figure::compareByWorth);

						assert thisFigures != opponentFigures;

						int niceForFigure = 0;

						while (!thisFigures.isEmpty() && !opponentFigures.isEmpty()) {
							niceForFigure -= thisFigures.pollLast().getWorth(); // Противник берёт фигуру

							if (thisFigures.isEmpty()) {
								break;
							}

							niceForFigure += opponentFigures.pollLast().getWorth(); // Мы берём фигуру
						}

						nice += (figureSide == currentSide ? niceForFigure : -niceForFigure) * STATE_WORTH_MULTIPLIER;
					}
				}
			}
		}

		return nice;
	}

	@Override
	public String toString() {
		return String.format("Snapshot { %d, %s - %s }", nice, pos, step);
	}
}
