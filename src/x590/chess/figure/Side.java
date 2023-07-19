package x590.chess.figure;

import x590.chess.board.ChessBoard;

import java.util.function.BiFunction;

/**
 * Сторона, за которую играет игрок (или бот)
 */
public enum Side {
	WHITE("white", "Белые",  "Ход белых",  (whiteChoose, blackChoose) -> whiteChoose, ChessBoard.START, ChessBoard.END),
	BLACK("black", "Чёрные", "Ход чёрных", (whiteChoose, blackChoose) -> blackChoose, ChessBoard.END, ChessBoard.START);

	private final String directory;

	private final String localizedName, state;

	private final BiFunction<Object, Object, Object> chooser;

	private final int startY, endY;

	Side(String directory, String localizedName, String state, BiFunction<Object, Object, Object> chooser, int startY, int endY) {
		this.directory = directory;
		this.localizedName = localizedName;
		this.state = state;
		this.chooser = chooser;
		this.startY = startY;
		this.endY = endY;
	}

	public static Side randomSide() {
		return Math.random() < 0.5 ? Side.WHITE : Side.BLACK;
	}

	public String getDirectory() {
		return directory;
	}

	public String getLocalizedName() {
		return localizedName;
	}

	public String getState() {
		return state;
	}

	public Side opposite() {
		return choose(BLACK, WHITE);
	}

	public Side oppositeIf(boolean isOpposite) {
		return isOpposite ? opposite() : this;
	}

	/**
	 * Удобный метод для выбора значения, которое зависит от текущей стороны
	 * @return {@code whiteChoose} для {@link #WHITE} и
	 *         {@code blackChoose} для {@link #BLACK}
	 */
	@SuppressWarnings("unchecked")
	public <T> T choose(T whiteChoose, T blackChoose) {
		return (T) chooser.apply(whiteChoose, blackChoose);
	}

	/**
	 * Работает так же, как и {@link #choose(Object, Object)}, только для {@code boolean}
	 */
	public boolean choose(boolean whiteChoose, boolean blackChoose) {
		return this == WHITE ? whiteChoose : blackChoose;
	}

	/**
	 * Работает так же, как и {@link #choose(Object, Object)}, только для {@code int}
	 */
	public int choose(int whiteChoose, int blackChoose) {
		return this == WHITE ? whiteChoose : blackChoose;
	}


	public int getStartY() {
		return startY;
	}

	public int getEndY() {
		return endY;
	}
}
