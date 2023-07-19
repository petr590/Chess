package x590.chess.figure;

import x590.chess.board.ChessBoard;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.io.Tag;
import x590.chess.packet.PacketOutputStreamWritable;
import x590.util.annotation.Immutable;
import x590.util.annotation.Nullable;

import java.io.IOException;

import static x590.chess.board.ChessBoard.SIZE;

/**
 * Неизменяемый класс, который представляет позицию поля на шахматной доске
 */
@Immutable
public final class Pos implements IStep, PacketOutputStreamWritable {

	private static final Pos[][] INSTANCES = new Pos[SIZE][SIZE];

	static {
		for (int y = 0; y < SIZE; y++) {
			Pos[] row = INSTANCES[y];

			for (int x = 0; x < SIZE; x++) {
				row[x] = new Pos(x, y);
			}
		}
	}

	private final int x, y;

	private Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return Экземпляр позиции с координатами {@code x} и {@code y}
	 * @throws IllegalArgumentException если одна из координат меньше нуля или больше {@link ChessBoard#SIZE}
	 */
	public static Pos of(int x, int y) {
		if (isInvalid(x) || isInvalid(y)) {
			throw new IllegalArgumentException("Pos is out of bounds: x = " + x + ", y = " + y);
		}

		return INSTANCES[y][x];
	}

	private static boolean isInvalid(int coord) {
		return coord < 0 || coord >= SIZE;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/**
	 * @return Позицию, смещённую по направлению {@code direction}
	 * или {@code null}, если она выходит за пределы доски
	 */
	public @Nullable Pos relative(Direction direction) {
		int x = this.x + direction.getXOffset();
		if (isInvalid(x)) {
			return null;
		}

		int y = this.y + direction.getYOffset();
		if (isInvalid(y)) {
			return null;
		}

		return of(x, y);
	}

	@Override
	public Pos targetPos() {
		return this;
	}

	@Override
	public Pos takePos() {
		return this;
	}

	@Override
	public Type type() {
		return Type.PLAIN;
	}

	@Override
	public IMove asMove(Pos startPos, ChessBoard board) {
		return new PosMove(startPos, this, board.getFigure(startPos), board.getFigure(this));
	}

	@Override
	public int getTag() {
		return Tag.TAG_POS;
	}

	public static Pos read(PacketInputStream in) throws IOException {
		return of(in.readByte(), in.readByte());
	}

	@Override
	public void writeTo(PacketOutputStream out) throws IOException {
		out.writeByte(x);
		out.writeByte(y);
	}


	private record PosMove(Pos startPos, Pos targetPos, Figure figure, @Nullable Figure takenFigure) implements IMove {

		@Override
		public Pos takePos() {
			return targetPos;
		}

		@Override
		public Type type() {
			return Type.PLAIN;
		}
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return this == other; // Все экземпляры кэшируются, так что нет необходимости сравнивать поля
	}

	public boolean equals(int x, int y) {
		return this.x == x && this.y == y;
	}

	@Override
	public int hashCode() {
		return (31 + x) * 31 + y;
	}

	@Override
	public String toString() {
		return String.valueOf((char)('A' + x)) + (y + 1);
	}
}
