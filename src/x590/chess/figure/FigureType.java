package x590.chess.figure;

import x590.util.annotation.Immutable;

import java.util.List;

public enum FigureType {
	KING   (Worth.KING,   "king.png"),
	QUEEN  (Worth.QUEEN,  "queen.png"),
	ROOK   (Worth.ROOK,   "rook.png"),
	BISHOP (Worth.BISHOP, "bishop.png"),
	KNIGHT (Worth.KNIGHT, "knight.png"),
	PAWN   (Worth.PAWN,   "pawn.png");

	public static final @Immutable List<FigureType> PAWN_TURNING_TYPES =
			List.of(QUEEN, ROOK, BISHOP, KNIGHT);

	private final int worth;
	
	private final String fileName;

	FigureType(int worth, String fileName) {
		this.worth = worth;
		this.fileName = fileName;
	}

	public int getWorth() {
		return worth;
	}

	public String getFileName() {
		return fileName;
	}
}
