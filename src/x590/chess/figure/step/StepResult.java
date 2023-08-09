package x590.chess.figure.step;

import x590.chess.figure.Side;

import java.util.function.Function;

/**
 * Результат хода
 */
public enum StepResult {
	/** Игра продолжается */
	CONTINUE  (true, false, Side::getState),

	/** Шах */
	CHECK     (true, true, side -> side.getState() + ". Шах"),

	/** Мат */
	CHECKMATE (false, true, side -> side.getLocalizedName() + " выиграли. Шах и мат"),

	/** Пат */
	STALEMATE (false, false, side -> "Пат");

	private final boolean canContinueGame, isKingAttacked;

	private final Function<Side, String> messageFactory;

	StepResult(boolean canContinueGame, boolean isKingAttacked, Function<Side, String> messageFactory) {
		this.canContinueGame = canContinueGame;
		this.isKingAttacked = isKingAttacked;
		this.messageFactory = messageFactory;
	}

	public boolean canContinueGame() {
		return canContinueGame;
	}

	public boolean isKingAttacked() {
		return isKingAttacked;
	}

	public String getMessage(Side side) {
		return messageFactory.apply(side);
	}
}
