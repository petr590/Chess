package x590.chess.figure.move;

import x590.chess.figure.Pos;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.step.IStep;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.io.Tag;
import x590.chess.packet.PacketOutputStreamWritable;
import x590.util.annotation.Nullable;

import java.io.IOException;

public interface IMove extends IStep, PacketOutputStreamWritable {

	interface IExtraMove extends IMove, IExtraStep {

		@Override
		default @Nullable Figure takenFigure() {
			return null;
		}

		@Override
		default IExtraMove asMove(Pos startPos, ChessBoard board) {
			return this;
		}

		@Override
		default IExtraMove asMove(ChessBoard board) {
			return this;
		}

		@Override
		default int getTag() {
			return Tag.TAG_EXTRA_MOVE;
		}

		static IExtraMove read(PacketInputStream in) throws IOException {
			return new ExtraMove(
					Pos.read(in),
					Pos.read(in),
					Pos.read(in),
					in.readEnum(Type.class),
					in.readEnum(Figure.class),
					in.readNullableEnum(Figure.class),
					in.readNullableEnum(Figure.class),
					in.readNullableByTag(Tag.EXTRA_MOVE)
			);
		}

		@Override
		default void writeTo(PacketOutputStream out) throws IOException {
			IMove.super.writeTo(out);
		}
	}

	/**
	 * @return Стартовую позицию хода
	 */
	Pos startPos();

	/**
	 * @return Фигуру, которая совершает ход
	 */
	Figure figure();

	/**
	 * @return Фигуру, которая совершает ход
	 */
	@Nullable Figure takenFigure();


	@Override
	default @Nullable IExtraMove extraStep() {
		return extraMove();
	}


	default @Nullable IExtraMove extraMove() {
		return null;
	}

	@Override
	default IMove asMove(Pos startPos, ChessBoard board) {
		return this;
	}


	default int getTag() {
		return Tag.TAG_MOVE;
	}


	static IMove read(PacketInputStream in) throws IOException {
		return new Move(
				Pos.read(in),
				Pos.read(in),
				Pos.read(in),
				in.readEnum(Type.class),
				in.readEnum(Figure.class),
				in.readNullableEnum(Figure.class),
				in.readNullableEnum(Figure.class),
				in.readNullableByTag(Tag.EXTRA_MOVE)
		);
	}

	@Override
	default void writeTo(PacketOutputStream out) throws IOException {
		out.write(startPos());
		out.write(targetPos());
		out.write(takePos());
		out.writeEnum(type());
		out.writeEnum(figure());
		out.writeNullableEnum(resultFigure());
		out.writeNullableEnum(takenFigure());
		out.writeNullableWithTag(extraMove());
	}

	static boolean equals(@Nullable IMove move1, @Nullable IMove move2) {
		if (move1 == null || move2 == null) {
			return move1 == move2;
		}

		return  move1 == move2 ||
				move1.type() == move2.type() &&
				move1.startPos().equals(move2.startPos()) &&
				move1.targetPos().equals(move2.targetPos()) &&
				move1.takePos().equals(move2.takePos()) &&
				move1.figure() == move2.figure() &&
				move1.resultFigure() == move2.resultFigure() &&
				move1.takenFigure() == move2.takenFigure() &&
				equals(move1.extraMove(), move2.extraMove());
	}

	default String toConvenientString() {

		var figure = figure();
		var resultFigure = resultFigure();

		StringBuilder str = new StringBuilder()
				.append(startPos()).append('-').append(targetPos()).append(' ').append(figure.getEmoji());

		if (resultFigure != null && figure != resultFigure) {
			str.append(" \uD83E\uDC12 ").append(resultFigure.getEmoji());
		}

		var extraMove = extraMove();

		if (extraMove != null) {
			str.append(", ").append(extraMove.toConvenientString());
		}

		return str.toString();
	}
}
