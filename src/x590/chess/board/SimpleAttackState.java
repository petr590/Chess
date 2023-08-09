package x590.chess.board;

import x590.chess.figure.Figure;
import x590.chess.figure.Side;

/**
 * Показывает, какой стороной атаковано поле
 */
public enum SimpleAttackState implements AttackState {
	NOT_ATTACKED      (false, false),
	ATTACKED_BY_WHITE (true, false),
	ATTACKED_BY_BLACK (false, true),
	ATTACKED_BY_BOTH  (true, true);

	static {
		NOT_ATTACKED.attackedByWhite = ATTACKED_BY_WHITE;
		NOT_ATTACKED.attackedByBlack = ATTACKED_BY_BLACK;
		ATTACKED_BY_WHITE.attackedByBlack = ATTACKED_BY_BOTH;
		ATTACKED_BY_BLACK.attackedByWhite = ATTACKED_BY_BOTH;
	}

	private final boolean isAttackedByWhite, isAttackedByBlack;
	private SimpleAttackState attackedByWhite = this, attackedByBlack = this;

	SimpleAttackState(boolean isAttackedByWhite, boolean isAttackedByBlack) {
		this.isAttackedByWhite = isAttackedByWhite;
		this.isAttackedByBlack = isAttackedByBlack;
	}

	@Override
	public boolean isAttackedBy(Side side) {
		return side.choose(isAttackedByWhite, isAttackedByBlack);
	}

	@Override
	public SimpleAttackState attackedBy(Figure figure) {
		return figure.getSide().choose(attackedByWhite, attackedByBlack);
	}
}
