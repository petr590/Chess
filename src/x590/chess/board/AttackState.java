package x590.chess.board;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.util.annotation.Immutable;
import x590.util.annotation.Nullable;

/**
 * Показывает, кем атаковано поле
 */
public interface AttackState {

	/**
	 * @return {@code true}, если поле атаковано указанной стороной
	 */
	boolean isAttackedBy(Side side);

	/**
	 * @return {@link AttackState}, атакованное переданной фигурой
	 */
	AttackState attackedBy(Figure figure);

	/**
	 * @return Список всех фигур, которыми было атаковано поле
	 *         или {@code null}, если запоминание фигур не поддерживается
	 */
	default @Nullable @Immutable Object2IntMap<Figure> getFigures() {
		return null;
	}
}
