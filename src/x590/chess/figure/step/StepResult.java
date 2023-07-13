package x590.chess.figure.step;

/**
 * Результат хода
 */
public enum StepResult {
	/** Игра продолжается */
	CONTINUE(true, false),
	/** Шах */
	CHECK(true, true),
	/** Мат */
	CHECKMATE(false, true),
	/** Пат */
	STALEMATE(false, false);

	private final boolean canContinueGame, isKingAttacked;

	StepResult(boolean canContinueGame, boolean isKingAttacked) {
		this.canContinueGame = canContinueGame;
		this.isKingAttacked = isKingAttacked;
	}

	public boolean canContinueGame() {
		return canContinueGame;
	}

	public boolean isKingAttacked() {
		return isKingAttacked;
	}
}
