package x590.chess.board;

import x590.chess.Pos;
import x590.chess.figure.Figure;
import x590.util.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class SideData {
	final List<Figure> takenFigures = new ArrayList<>(15); // Максимум 15 фигур мы можем срубить
	final @Immutable List<Figure> immutableTakenFigures = Collections.unmodifiableList(takenFigures);

	boolean takenFiguresListChanged;

	Pos kingPos;

	boolean kingWalked, aRookWalked, hRookWalked;

	SideData(Pos kingPos) {
		this.kingPos = kingPos;
	}

	public void addTakenFigure(Figure takenFigure) {
		final var takenFigures = this.takenFigures;
		takenFigures.add(takenFigure);
		takenFigures.sort(Comparator.naturalOrder());
	}

	public void resetAllToDefault(Pos defaultKingPos) {
		takenFigures.clear();
		kingPos = defaultKingPos;
		kingWalked = aRookWalked = hRookWalked = false;
	}
}
