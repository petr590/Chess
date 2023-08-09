package x590.chess.board;

import x590.chess.figure.Pos;
import x590.chess.figure.FigureType;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.IExtraStep;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.step.StepResult;
import x590.chess.playingside.PlayingSide;
import x590.util.ArrayUtil;
import x590.util.annotation.Immutable;
import x590.util.annotation.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static x590.chess.figure.Figure.*;
import static x590.chess.board.SimpleAttackState.*;

/**
 * Представляет собой шахматную доску
 */
public class ChessBoard {

	/** Размер шахматной доски */
	public static final int SIZE = 8;

	/** Первая координата шахматной доски */
	public static final int START = 0;

	/** Последняя координата шахматной доски */
	public static final int END = SIZE - 1;


	private static final Figure[][] DEFAULT_BOARD = {
		{ WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK },
		{ WHITE_PAWN, WHITE_PAWN,   WHITE_PAWN,   WHITE_PAWN,  WHITE_PAWN, WHITE_PAWN,   WHITE_PAWN,   WHITE_PAWN },
		new Figure[SIZE],
		new Figure[SIZE],
		new Figure[SIZE],
		new Figure[SIZE],
		{ BLACK_PAWN, BLACK_PAWN,   BLACK_PAWN,   BLACK_PAWN,  BLACK_PAWN, BLACK_PAWN,   BLACK_PAWN,   BLACK_PAWN },
		{ BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK },
	};

	private static final AttackState[][] DEFAULT_ATTACK_STATES = {
		{      NOT_ATTACKED, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE,      NOT_ATTACKED },
		{ ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE },
		{ ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE, ATTACKED_BY_WHITE },
		{      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED },
		{      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED,      NOT_ATTACKED },
		{ ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK },
		{ ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK },
		{      NOT_ATTACKED, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK, ATTACKED_BY_BLACK,      NOT_ATTACKED },
	};

	private static final Pos
			DEFULT_WHITE_KING_POS = Pos.of(4, START),
			DEFULT_BLACK_KING_POS = Pos.of(4, END);


	private final Figure[][] board;
	private AttackState[][] attackStates;

	/** Кэшированные варианты ходов */
	private final Map<Pos, @Immutable List<? extends IStep>> cachedSteps = new HashMap<>();

	/** Кэшированные снимки позиций ходов */
	private final Map<Pos, @Immutable Map<IStep, Snapshot>> cachedSnapshots = new HashMap<>();
	private @Nullable @Immutable Map<Pos, @Immutable Map<IStep, Snapshot>> immutableCachedSnapshots;

	private final SideData whiteData, blackData;

	private Side currentSide;

	private SideData currentData;

	private @Nullable IStep lastStep;


	private ChessBoard(Side initalSide, Figure[][] board, AttackState[][] attackStates, Pos whiteKingPos, Pos blackKingPos) {
		this.currentSide = initalSide;
		this.board = board;
		this.attackStates = attackStates;
		this.whiteData = new SideData(whiteKingPos);
		this.blackData = new SideData(blackKingPos);
		this.currentData = whiteData;
	}

	/**
	 * @return Новый экземпляр {@link ChessBoard} с расстановкой фигур по умолчанию
	 */
	public static ChessBoard defaultPlacement() {
		var instance = new ChessBoard(
				Side.WHITE,
				new Figure[SIZE][SIZE],
				new AttackState[SIZE][SIZE],
				DEFULT_WHITE_KING_POS,
				DEFULT_BLACK_KING_POS
		);

		instance.setDefaultBoardAndAttackStates();

		return instance;
	}

	private void setDefaultBoardAndAttackStates() {
		for (int i = 0; i < SIZE; i++) {
			System.arraycopy(DEFAULT_BOARD[i],         0, board[i],        0, SIZE);
			System.arraycopy(DEFAULT_ATTACK_STATES[i], 0, attackStates[i], 0, SIZE);
		}
	}

	public void resetAllToDefault() {
		setDefaultBoardAndAttackStates();
		cachedSteps.clear();
		cachedSnapshots.clear();
		currentSide = Side.WHITE;
		whiteData.resetAllToDefault(DEFULT_WHITE_KING_POS);
		blackData.resetAllToDefault(DEFULT_BLACK_KING_POS);
		currentData = whiteData;
		lastStep = null;
	}


	public void setup(PlayingSide currentPlayingSide) {
		computeAllPossibleSteps(currentPlayingSide.shouldMakeSnapshots());
	}


	/**
	 * @return Фигуру на позиции {@code pos}
	 */
	public @Nullable Figure getFigure(Pos pos) {
		return board[pos.getY()][pos.getX()];
	}

	/**
	 * @return Фигуру на позиции {@code pos}
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры
	 */
	public Figure getNonNullFigure(Pos pos) {
		Figure figure = getFigure(pos);
		if (figure != null)
			return figure;

		throw new IllegalArgumentException("There is no figure at " + pos);
	}

	/**
	 * @return Фигуру на позиции {@code pos}, принадлежащую стороне {@code side}
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры
	 * или она принадлежит другой стороне.
	 */
	private Figure getFigureWithSide(Pos pos, Side side) {
		Figure figure = getNonNullFigure(pos);

		if (figure.getSide() == side) {
			return figure;
		}

		throw new IllegalArgumentException("Figure at " + pos + " has wrong side");
	}

	/**
	 * @return Фигуру на позиции {@code pos}, принадлежащую стороне {@link #currentSide}
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры
	 * или она принадлежит другой стороне.
	 */
	public Figure getFigureWithCurrentSide(Pos pos) {
		return getFigureWithSide(pos, currentSide);
	}


	private void setFigure(Pos pos, @Nullable Figure figure) {
		board[pos.getY()][pos.getX()] = figure;
	}


	/**
	 * @return {@code true}, если на указанной позиции есть фигура
	 */
	public boolean hasFigure(Pos pos) {
		return getFigure(pos) != null;
	}

	/**
	 * @return {@code true}, если на указанной позиции нет фигуры
	 */
	public boolean freeAt(Pos pos) {
		return getFigure(pos) == null;
	}

	public Side currentSide() {
		return currentSide;
	}

	public @Nullable IStep getLastStep() {
		return lastStep;
	}


	/**
	 * @return Варианты ходов для фигуры на позиции {@code pos}.
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры или она принадлежит другой стороне.
	 */
	public @Immutable List<? extends IStep> getPossibleSteps(Pos atPos) {
		return getOrThrow(cachedSteps, atPos, pos -> "There is no steps at pos " + pos);
	}

	private static <K, V> V getOrThrow(@Immutable Map<K, V> map, K key, Function<K, String> messageGetter) {
		V value = map.get(key);
		if (value != null)
			return value;

		throw new IllegalArgumentException(messageGetter.apply(key));
	}

	/**
	 * Рассчитывает все возможные шаги. Также создаёт скриншоты, если необходимо
	 */
	private void computeAllPossibleSteps(boolean shouldMakeSnapshots) {
		forEachPosWithCurrentFigure(pos -> computePossibleSteps(pos, shouldMakeSnapshots));
		immutableCachedSnapshots = shouldMakeSnapshots ?
				Collections.unmodifiableMap(cachedSnapshots) :
				null;
	}

	private void computePossibleSteps(Pos pos, boolean shouldMakeSnapshots) {

		List<? extends IStep> steps = getFigureWithCurrentSide(pos).getPossibleSteps(this, pos);

		var savedAttackStates = saveAttackStates();

		Map<IStep, Snapshot> stepSnapshots = shouldMakeSnapshots ? new HashMap<>() : null;

		steps.removeIf(step -> {
			Figure originalFigure = getFigureWithCurrentSide(pos);
			Figure taken = performStep(pos, step, step.resultFigure(), false);

			updateAttackStates(shouldMakeSnapshots ?
					ExtendedAttackState.NOT_ATTACKED :
					SimpleAttackState.NOT_ATTACKED);

			Snapshot snapshot = shouldMakeSnapshots ?
					new Snapshot(pos, step, board, attackStates) :
					null;

			// Запоминаем позицию короля, так как вызов cancelStep вернёт её обратно
			Pos kingPos = currentData.kingPos;

			cancelStep(pos, step, originalFigure, taken);

			boolean remove = isAttackedBySide(kingPos, currentSide.opposite());

			if (shouldMakeSnapshots && !remove) {
				stepSnapshots.put(step, snapshot);
			}

			return remove;
		});

		if (stepSnapshots != null) {
			cachedSnapshots.put(pos, Collections.unmodifiableMap(stepSnapshots));
		}

		restoreAttackStates(savedAttackStates);

		cachedSteps.put(pos, Collections.unmodifiableList(steps));
	}


	/**
	 * Применяет переданную функцию к каждой позиции, на которой есть фигура текущей стороны,
	 * и к списку ходов, доступных на этой позиции
	 */
	public void forEachPossibleSteps(BiConsumer<Pos, @Immutable List<? extends IStep>> eachFunction) {
		forEachPosWithCurrentFigure(pos -> eachFunction.accept(pos, getPossibleSteps(pos)));
	}

	/**
	 * Применяет переданную функцию к каждой позиции, на которой есть фигура текущей стороны
	 */
	private void forEachPosWithCurrentFigure(Consumer<Pos> eachFunction) {
		final var currentSide = this.currentSide;

		for (Pos pos = Pos.START; pos != null; pos = pos.nextOrNull()) {
			Figure figure = getFigure(pos);

			if (figure != null && figure.getSide() == currentSide) {
				eachFunction.accept(pos);
			}
		}
	}

	public @Immutable Map<IStep, Snapshot> getSnapshots(Pos pos) {
		return cachedSnapshots.get(pos);
	}

	/**
	 * @return Все кэшированные снимки позиций
	 * @throws IllegalStateException если снимки позиций не были кэшированы
	 */
	public @Immutable Map<Pos, @Immutable Map<IStep, Snapshot>> getAllSnapshots() {
		var snapshots = immutableCachedSnapshots;

		if (snapshots != null) {
			return snapshots;
		}

		throw new IllegalStateException("Snapshots are not cached");
	}

	private AttackState[][] saveAttackStates() {
		return ArrayUtil.clone2dArray(attackStates);
	}

	private void restoreAttackStates(AttackState[][] savedAttackStates) {
		this.attackStates = savedAttackStates;
	}


	/**
	 * @return {@code true}, если фигура на позиции {@code pos} может быть взята стороной {@code side}
	 */
	public boolean canFigureBeTookBy(Pos pos, Side side) {
		Figure figure = getFigure(pos);
		return figure != null && figure.canBeTookBy(side);
	}

	/**
	 * @return Сторону фигуры на позиции {@code pos} или {@code null}, если на этой позиции нет фигуры.
	 */
	public @Nullable Side getFigureSide(Pos pos) {
		Figure figure = getFigure(pos);
		return figure == null ? null : figure.getSide();
	}

	/**
	 * @return {@code true}, если фигура на позиции {@code pos} находится под атакой стороны {@code side}
	 * (сторона фигуры и сторона, с которой идёт атака, могут и не отличаться)
	 */
	public boolean isAttackedBySide(Pos pos, Side side) {
		return attackStates[pos.getY()][pos.getX()].isAttackedBy(side);
	}


	/**
	 * @return Номер текущего хода. Начинается с 0.
	 * Каждые 2 хода (когда белые сделали ход и чёрные сделали ход) номер увеличивается
	 */
	public int getStepNumber() {
		return currentData.getStepNumber();
	}

	/**
	 * @return Позицию короля
	 */
	public Pos getKingPos() {
		return currentData.kingPos;
	}

	/**
	 * @return {@code true}, если король текущей стороны делал ход
	 */
	public boolean isKingWalked() {
		return currentData.isKingWalked();
	}


	public boolean isKingAttacked() {
		return isAttackedBySide(currentData.kingPos, currentSide.opposite());
	}

	/**
	 * @return {@code true}, если ладья на вертикали A делала ход
	 */
	public boolean isARookWalked() {
		return currentData.isARookWalked();
	}

	/**
	 * @return {@code true}, если ладья на вертикали H делала ход
	 */
	public boolean isHRookWalked() {
		return currentData.isHRookWalked();
	}

	/**
	 * @return Все фигуры, взятые у стороны {@code side} в течение партии
	 */
	public @Immutable List<Figure> getTakenFigures(Side side) {
		return side.choose(whiteData, blackData).immutableTakenFigures;
	}

	/**
	 * @return {@code true}, если список взятых фигур был изменён
	 */
	public boolean takenFiguresListChanged(Side side) {
		return side.choose(whiteData, blackData).takenFiguresListChanged;
	}


	/**
	 * Совершает ход фигурой текущей стороны. Затем меняет сторону.
	 * @param move объект, представляющий выполняемый ход.
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры,
	 * она принадлежит другой стороне или ход невозможно совершить.
	 */
	public StepResult makeMove(IMove move, PlayingSide currentPlayingSide, PlayingSide oppositePlayingSide) {
		Pos startPos = move.startPos();

		if (getPossibleSteps(startPos).stream()
				.noneMatch(possibleStep -> IStep.equalsIgnoreResultFigure(move, possibleStep))) {
			throw new IllegalArgumentException("Cannot perform step from " + startPos + " to " + move);
		}

		Figure takenFigure = performStep(startPos, move,
				move.queryResultFigure(currentSide, currentPlayingSide), true);

		updateAttackStates();
		cachedSteps.clear();
		cachedSnapshots.clear();

		final var currentSide = this.currentSide = this.currentSide.opposite();
		currentData = currentSide.choose(whiteData, blackData);

		lastStep = move;

		if (takenFigure != null) {
			currentData.addTakenFigure(takenFigure);
			currentData.takenFiguresListChanged = true;
		} else {
			currentData.takenFiguresListChanged = false;
		}

		computeAllPossibleSteps(oppositePlayingSide.shouldMakeSnapshots());

		if (cachedSteps.values().stream().allMatch(List::isEmpty)) {
			return isKingAttacked() ? StepResult.CHECKMATE : StepResult.STALEMATE;
		}

		return isKingAttacked() ? StepResult.CHECK : StepResult.CONTINUE;
	}

	public void cancelMove(IMove move, @Nullable IStep prevStep, PlayingSide oppositePlayingSide) {
		@Nullable Figure takenFigure = move.takenFigure();

		if (takenFigure != null) {
			currentData.removeTakenFigure(takenFigure);
			currentData.takenFiguresListChanged = true;
		} else {
			currentData.takenFiguresListChanged = false;
		}

		currentSide = currentSide.opposite();
		currentData = currentSide.choose(whiteData, blackData);

		lastStep = prevStep;

		cachedSteps.clear();
		cachedSnapshots.clear();

		cancelStep(move.startPos(), move, move.figure(), takenFigure);

		updateAttackStates();

		computeAllPossibleSteps(oppositePlayingSide.shouldMakeSnapshots());
	}

	private @Nullable Figure performStep(Pos startPos, IStep step, @Nullable Figure newFigure, boolean updateWalkedFlags) {
		Pos targetPos = step.targetPos(),
			takePos = step.takePos();

		if (!targetPos.equals(takePos) && hasFigure(targetPos)) {
			throw new IllegalArgumentException("targetPos " + targetPos + " and takePos " + takePos
					+ " are not matches and targetPos is not free");
		}

		Figure figure = getFigureWithCurrentSide(startPos);
		Figure takenFigure = getFigure(takePos);

		setFigure(startPos, null);
		setFigure(targetPos, newFigure == null ? figure : newFigure);

		if (!targetPos.equals(takePos)) {
			setFigure(takePos, null);
		}

		final var currentData = this.currentData;

		currentData.makeStep();

		if (figure.getType() == FigureType.KING && startPos.equals(currentData.kingPos)) {
			currentData.kingPos = targetPos;
			if (updateWalkedFlags) {
				currentData.setKingWalked();
			}

		} else if (figure.getType() == FigureType.ROOK && updateWalkedFlags) {
			if (!currentData.isARookWalked() && startPos.equals(0, currentSide.getStartY())) {
				currentData.setARookWalked();
			} else if (!currentData.isHRookWalked() && startPos.equals(7, currentSide.getStartY())) {
				currentData.setHRookWalked();
			}
		}

		@Nullable IExtraStep extraStep = step.extraStep();

		if (extraStep != null) {
			if (performStep(extraStep.startPos(), extraStep, extraStep.resultFigure(), updateWalkedFlags) != null) {
				throw new IllegalStateException("Extra step must not take any figure");
			}
		}

		return takenFigure;
	}

	private void cancelStep(Pos startPos, IStep step, Figure originalFigure, @Nullable Figure takenFigure) {
		Pos targetPos = step.targetPos(),
			takePos = step.takePos();

		setFigure(startPos, originalFigure);
		setFigure(takePos, takenFigure);

		if (!targetPos.equals(takePos)) {
			setFigure(targetPos, null);
		}

		if (targetPos.equals(currentData.kingPos)) {
			currentData.kingPos = startPos;
		}

		currentData.cancelStep();

		IExtraStep extraStep = step.extraStep();

		if (extraStep != null) {
			cancelStep(extraStep.startPos(), extraStep, getFigureWithCurrentSide(extraStep.targetPos()), null);
		}
	}

	private void updateAttackStates() {
		updateAttackStates(NOT_ATTACKED);
	}

	private void updateAttackStates(AttackState initialState) {
		final var attackStates = this.attackStates;

		for (int y = 0; y < SIZE; y++) {
			final var row = attackStates[y];

			for (int x = 0; x < SIZE; x++) {
				row[x] = initialState;
			}
		}

		forEachFigure((figure, pos) -> {

			if (figure != null) {
				@Immutable List<Pos> controlledFields = figure.getControlledFields(this, pos);

				for (Pos controlledPos : controlledFields) {
					int cx = controlledPos.getX(),
						cy = controlledPos.getY();

					attackStates[cy][cx] = attackStates[cy][cx].attackedBy(figure);
				}
			}
		});
	}

	/**
	 * @param eachFunction Функция, которая применяется к каждой фигуре
	 */
	private void forEachFigure(BiConsumer<Figure, Pos> eachFunction) {
		final var board = this.board;

		for (int y = 0; y < SIZE; y++) {
			Figure[] row = board[y];

			for (int x = 0; x < SIZE; x++) {
				eachFunction.accept(row[x], Pos.of(x, y));
			}
		}
	}
}
