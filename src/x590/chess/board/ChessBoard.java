package x590.chess.board;

import x590.chess.Pos;
import x590.chess.figure.FigureType;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.Type;
import x590.chess.figure.step.IStep.IExtraStep;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.step.StepResult;
import x590.util.annotation.Immutable;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static x590.chess.figure.Figure.*;
import static x590.chess.board.AttackState.*;

/**
 * Представляет собой шахматную доску
 */
public class ChessBoard {

	/** Размер шахматной доски */
	public static final int SIZE = 8;
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
			DEFULT_WHITE_KING_POS = Pos.of(4, 0),
			DEFULT_BLACK_KING_POS = Pos.of(4, 7);


	private final Figure[][] board;
	private AttackState[][] attackStates;
	private @Nullable AttackState[][] savedAttackStates;

	/** Кэшированные варианты ходов */
	private final Map<Pos, @Immutable Collection<? extends IStep>> steps = new HashMap<>();

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
			System.arraycopy(DEFAULT_BOARD[i], 0, board[i], 0, SIZE);
			System.arraycopy(DEFAULT_ATTACK_STATES[i], 0, attackStates[i], 0, SIZE);
		}
	}

	public void resetAllToDefault() {
		setDefaultBoardAndAttackStates();
		steps.clear();
		currentSide = Side.WHITE;
		whiteData.resetAllToDefault(DEFULT_WHITE_KING_POS);
		blackData.resetAllToDefault(DEFULT_BLACK_KING_POS);
		currentData = whiteData;
		lastStep = null;
	}


	public @Nullable Figure getFigure(Pos pos) {
		return board[pos.getY()][pos.getX()];
	}

	public boolean hasFigure(Pos pos) {
		return getFigure(pos) != null;
	}

	public boolean freeAt(Pos pos) {
		return getFigure(pos) == null;
	}

	/**
	 * @return Фигуру на позиции {@code pos}, принадлежащую стороне {@link #currentSide}
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры
	 * или она принадлежит другой стороне.
	 */
	public Figure getFigureWithCurrentSide(Pos pos) {
		Figure figure = getFigure(pos);

		if (figure == null) {
			throw new IllegalArgumentException("There is no figure at " + pos);
		}

		if (figure.getSide() != currentSide) {
			throw new IllegalArgumentException("Figure at " + pos + " has wrong side");
		}

		return figure;
	}

	public Side currentSide() {
		return currentSide;
	}

	public @Nullable IStep getLastStep() {
		return lastStep;
	}

	/**
	 * @return Варианты ходов для фигуры на позиции {@code atPos}
	 */
	public @Immutable Collection<? extends IStep> getPossibleSteps(Pos atPos) {
		return steps.computeIfAbsent(atPos,
				pos -> {
					Collection<? extends IStep> steps = getFigureWithCurrentSide(pos).getPossibleSteps(this, pos);

					saveAttackStates();

					steps.removeIf(step -> {
						Figure originalFigure = getFigureWithCurrentSide(pos);
						Figure taken = performStep(pos, step, step.resultFigure(), false);
						updateAttackStates();

						// Запоминаем позицию короля, так как вызов cancelStep вернёт её обратно
						Pos kingPos = currentData.kingPos;

						cancelStep(pos, step, originalFigure, taken);
						return isAttackedBySide(kingPos, currentSide.opposite());
					});

					restoreAttackStates();

					return Collections.unmodifiableCollection(steps);
				});
	}

	private void saveAttackStates() {
		final var attackStates = this.attackStates;
		final var savedAttackStates = this.savedAttackStates = attackStates.clone();

		for (int i = 0; i < SIZE; i++) {
			savedAttackStates[i] = attackStates[i].clone();
		}
	}

	private void restoreAttackStates() {
		attackStates = savedAttackStates;
		savedAttackStates = null;
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
	 * @return Позицию короля
	 */
	public Pos getKingPos() {
		return currentData.kingPos;
	}

	/**
	 * @return {@code true}, если король текущей стороны делал ход
	 */
	public boolean isKingWalked() {
		return currentData.kingWalked;
	}

	/**
	 * @return {@code true}, если ладья на вертикали A делала ход
	 */
	public boolean isARookWalked() {
		return currentData.aRookWalked;
	}

	/**
	 * @return {@code true}, если ладья на вертикали H делала ход
	 */
	public boolean isHRookWalked() {
		return currentData.hRookWalked;
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
	public boolean takenFiguresListChanged() {
		return currentData.takenFiguresListChanged;
	}

	/**
	 * Совершает ход фигурой текущей стороны с {@code startPos}.
	 * Затем меняет сторону.
	 * @throws IllegalArgumentException если на указанной позиции нет фигуры,
	 * она принадлежит другой стороне или ход невозможно совершить.
	 */
	public StepResult step(Pos startPos, IStep step) {
		@Immutable Collection<? extends IStep> steps = getPossibleSteps(startPos);

		Figure newFigure;

		if (step.type() == Type.TURNING_A_PAWN) {
			Object[] icons = Figure.getPawnTurningIcons(currentSide);

			int figureIndex;

			do {
				figureIndex = JOptionPane.showOptionDialog(
						null, new JLabel("Выберите фигуру для превращения", SwingConstants.CENTER), "",
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						icons, icons[0]
				);
			} while (figureIndex == -1);

			newFigure = currentSide.choose(WHITE_PAWN_TURNING_FIGURES, BLACK_PAWN_TURNING_FIGURES).get(figureIndex);

		} else {
			newFigure = step.resultFigure();
		}

		if (!steps.contains(step)) {
			throw new IllegalArgumentException("Cannot perform step from " + startPos + " to " + step);
		}

		Figure takenFigure = performStep(startPos, step, newFigure, true);

		updateAttackStates();
		this.steps.clear();

		final var currentSide = this.currentSide = this.currentSide.opposite();
		currentData = currentSide.choose(whiteData, blackData);

		lastStep = step;

		if (takenFigure != null) {
			currentData.addTakenFigure(takenFigure);
			currentData.takenFiguresListChanged = true;
		} else {
			currentData.takenFiguresListChanged = false;
		}

		boolean kingAttacked = isAttackedBySide(currentData.kingPos, currentSide.opposite());

		if (isEachFigure((figure, pos) ->
				figure == null || figure.getSide() != currentSide || getPossibleSteps(pos).isEmpty()
		)) {
			return kingAttacked ? StepResult.CHECKMATE : StepResult.STALEMATE;
		}

		return kingAttacked ? StepResult.CHECK : StepResult.CONTINUE;
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

		if (figure.getType() == FigureType.KING && startPos.equals(currentData.kingPos)) {
			currentData.kingPos = targetPos;
			if (updateWalkedFlags)
				currentData.kingWalked = true;

		} else if (figure.getType() == FigureType.ROOK && updateWalkedFlags) {
			if (!currentData.aRookWalked && startPos.equals(0, currentSide.getStartY())) {
				currentData.aRookWalked = true;
			} else if (!currentData.hRookWalked && startPos.equals(7, currentSide.getStartY())) {
				currentData.hRookWalked = true;
			}
		}

		IExtraStep extraStep = step.extraStep();

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

		IExtraStep extraStep = step.extraStep();

		if (extraStep != null) {
			cancelStep(extraStep.startPos(), extraStep, getFigureWithCurrentSide(extraStep.targetPos()), null);
		}
	}

	private void updateAttackStates() {
		final var attackStates = this.attackStates;

		for (int y = 0; y < SIZE; y++) {
			final var row = attackStates[y];

			for (int x = 0; x < SIZE; x++) {
				row[x] = NOT_ATTACKED;
			}
		}

		isEachFigure((figure, pos) -> {

			if (figure != null) {
				@Immutable Collection<Pos> controlledFields = figure.getControlledFields(this, pos);

				for (Pos controlledPos : controlledFields) {
					int cx = controlledPos.getX(),
						cy = controlledPos.getY();

					attackStates[cy][cx] = attackStates[cy][cx].attackedBy(figure.getSide());
				}
			}
		});
	}

	private void isEachFigure(BiConsumer<Figure, Pos> eachFunction) {
		isEachFigure((figure, pos) -> {
			eachFunction.accept(figure, pos);
			return true;
		});
	}

	/**
	 * @param eachFunction Функция, которая применяется к каждой фигуре.
	 *                     Если она возврашает {@code false}, итерация прерывается
	 * @return {@code true} если для всех фигур {@code eachFunction} вернул {@code true}
	 */
	private boolean isEachFigure(BiPredicate<Figure, Pos> eachFunction) {
		final var board = this.board;

		for (int y = 0; y < SIZE; y++) {
			Figure[] row = board[y];

			for (int x = 0; x < SIZE; x++) {
				if (!eachFunction.test(row[x], Pos.of(x, y))) {
					return false;
				}
			}
		}

		return true;
	}

	private void setFigure(Pos pos, @Nullable Figure figure) {
		board[pos.getY()][pos.getX()] = figure;
	}
}
