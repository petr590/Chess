package x590.chess.board;

import x590.chess.figure.Pos;
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

	private int step;

	private int kingWalkedStep, aRookWalkedStep, hRookWalkedStep;

	SideData(Pos kingPos) {
		this.kingPos = kingPos;
	}

	void addTakenFigure(Figure takenFigure) {
		final var takenFigures = this.takenFigures;
		takenFigures.add(takenFigure);
		takenFigures.sort(Comparator.naturalOrder());
	}

	int getStepNumber() {
		return step;
	}

	void removeTakenFigure(Figure takenFigure) {
		takenFigures.remove(takenFigure);
	}

	void makeStep() {
		step++;
	}

	void cancelStep() {
		step--;
		if (kingWalkedStep > step) {
			kingWalkedStep = 0;
		}
		if (aRookWalkedStep > step) {
			aRookWalkedStep = 0;
		}
		if (hRookWalkedStep > step) {
			hRookWalkedStep = 0;
		}
	}

	void setKingWalked() {
		if (kingWalkedStep != 0) {
			kingWalkedStep = step;
		}
	}

	void setARookWalked() {
		if (aRookWalkedStep != 0) {
			aRookWalkedStep = step;
		}
	}

	void setHRookWalked() {
		if (hRookWalkedStep != 0) {
			hRookWalkedStep = step;
		}
	}

	boolean isKingWalked() {
		return kingWalkedStep != 0;
	}

	boolean isARookWalked() {
		return aRookWalkedStep != 0;
	}

	boolean isHRookWalked() {
		return hRookWalkedStep != 0;
	}

	void resetAllToDefault(Pos defaultKingPos) {
		takenFigures.clear();
		kingPos = defaultKingPos;
		kingWalkedStep = aRookWalkedStep = hRookWalkedStep = -1;
	}
}
