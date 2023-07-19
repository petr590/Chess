package x590.chess.board;

import x590.chess.figure.Side;

/**
 * Показывает, какой стороной атаковано поле
 */
public enum AttackState {
	NOT_ATTACKED      (false, false),
	ATTACKED_BY_WHITE (true, false),
	ATTACKED_BY_BLACK (false, true),
	ATTACKED_BY_BOTH  (true, true);

	static {
		NOT_ATTACKED.stateAttackedByWhite = ATTACKED_BY_WHITE;
		NOT_ATTACKED.stateAttackedByBlack = ATTACKED_BY_BLACK;
		ATTACKED_BY_WHITE.stateAttackedByBlack = ATTACKED_BY_BOTH;
		ATTACKED_BY_BLACK.stateAttackedByWhite = ATTACKED_BY_BOTH;
	}

	private final boolean attackedByWhite, attackedByBlack;
	private AttackState stateAttackedByWhite = this, stateAttackedByBlack = this;

	AttackState(boolean attackedByWhite, boolean attackedByBlack) {
		this.attackedByWhite = attackedByWhite;
		this.attackedByBlack = attackedByBlack;
	}

	public boolean isAttackedBy(Side side) {
		return side.choose(attackedByWhite, attackedByBlack);
	}

	public AttackState attackedBy(Side side) {
		return side.choose(stateAttackedByWhite, stateAttackedByBlack);
	}
}
