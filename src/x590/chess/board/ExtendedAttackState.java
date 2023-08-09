package x590.chess.board;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.util.annotation.Immutable;

/**
 * Хранит список фигур, которыми атаковано поле
 */
@Immutable
public final class ExtendedAttackState implements AttackState {

	public static final ExtendedAttackState NOT_ATTACKED = new ExtendedAttackState(Object2IntMaps.emptyMap());

	private final @Immutable Object2IntMap<Figure> figures;

	private final boolean isAttackedByWhite, isAttackedByBlack;

	private ExtendedAttackState(@Immutable Object2IntMap<Figure> figures) {
		this.figures = figures;
		this.isAttackedByWhite = figures.keySet().stream().anyMatch(figure -> figure.getSide() == Side.WHITE);
		this.isAttackedByBlack = figures.keySet().stream().anyMatch(figure -> figure.getSide() == Side.BLACK);
	}

	@Override
	public boolean isAttackedBy(Side side) {
		return side.choose(isAttackedByWhite, isAttackedByBlack);
	}

	@Override
	public AttackState attackedBy(Figure figure) {
		if (figures.isEmpty()) {
			return new ExtendedAttackState(Object2IntMaps.singleton(figure, 1));
		}

		var newFigures = new Object2IntArrayMap<>(figures);
		newFigures.put(figure, newFigures.getInt(figure) + 1);
		return new ExtendedAttackState(Object2IntMaps.unmodifiable(newFigures));
	}

	@Override
	public @Immutable Object2IntMap<Figure> getFigures() {
		return figures;
	}
}
