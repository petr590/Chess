package x590.chess.figure.step;

import x590.chess.figure.Pos;
import x590.chess.board.ChessBoard;
import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.PacketOutputStreamWritableWithTag;
import x590.util.annotation.Nullable;

import java.io.IOException;

import static x590.chess.io.Tag.*;

/**
 * Представляет отдельный ход (изначальная позиция задаются из контекста, в котором этот ход используется)
 */
public interface IStep extends PacketOutputStreamWritableWithTag {

	/**
	 * Тип хода
	 */
	enum Type {
		PLAIN,
		CASTLING,
		DOUBLE_PAWN_STEP,
		TAKING_PAWN_ON_THE_PASS,
		TURNING_A_PAWN
	}

	interface IExtraStep extends IStep {

		Pos startPos();

		@Override
		default Pos takePos() {
			return targetPos();
		}

		IExtraMove asMove(ChessBoard board);

		@Override
		default IExtraMove asMove(Pos startPos, ChessBoard board) {
			return asMove(board);
		}

		@Override
		default int getTag() {
			return TAG_EXTRA_STEP;
		}

		static IExtraStep read(PacketInputStream in) throws IOException {
			return new ExtraStep(
					Pos.read(in),
					Pos.read(in),
					Pos.read(in),
					in.readEnum(Type.class),
					in.readNullableEnum(Figure.class),
					in.readNullableByTag(EXTRA_STEP)
			);
		}

		@Override
		default void writeTo(PacketOutputStream out) throws IOException {
			out.write(startPos());
			IStep.super.writeTo(out);
		}
	}

	/**
	 * @return Целевую позицию. Должна совпадать с {@link #takePos()} или должна быть свободной
	 */
	Pos targetPos();

	/**
	 * @return Позицию взятия фигуры. Если фактического взятия не происходит,
	 * должен быть равен {@link #targetPos()}
	 */
	Pos takePos();

	/**
	 * @return Тип хода
	 */
	Type type();

	/**
	 * @return Фигуру в результате хода или {@code null}, если фигура та же самая.
	 */
	default @Nullable Figure resultFigure() {
		return null;
	}

	/**
	 * Запрашивает фигуру у пользователя, если необходимо.
	 * Должен вызываться только при фактическом выполнении хода, в остальных случаях вызывайте {@link #resultFigure()}
	 * @return Фигуру в результате хода или {@code null}, если фигура та же самая.
	 * @see x590.chess.figure.move.TurningAPawnMove#queryResultFigure(Side)
	 */
	default @Nullable Figure queryResultFigure(Side side) {
		return resultFigure();
	}

	/**
	 * @return Дополнительный ход. Некоторые ходы (например, рокировка) требуют перемещения двух фигур.
	 * Дополнительный ход не должен брать никакую фигуру.
	 */
	default @Nullable IExtraStep extraStep() {
		return null;
	}

	/**
	 * @return Ход как экземпляр {@link IMove}
	 */
	IMove asMove(Pos startPos, ChessBoard board);


	static boolean equals(@Nullable IStep step1, @Nullable IStep step2) {
		return equalsIgnoreResultFigure(step1, step2) &&
				(step1 == null || step1 == step2 || step1.resultFigure() == step2.resultFigure());
	}


	static boolean equalsIgnoreResultFigure(@Nullable IStep step1, @Nullable IStep step2) {
		if (step1 == null || step2 == null) {
			return step1 == step2;
		}

		return  step1 == step2 ||
				step1.type() == step2.type() &&
				step1.targetPos().equals(step2.targetPos()) &&
				step1.takePos().equals(step2.takePos()) &&
				equals(step1.extraStep(), step2.extraStep());
	}

	@Override
	default int getTag() {
		return TAG_STEP;
	}

	static IStep read(PacketInputStream in) throws IOException {
		return new Step(
				Pos.read(in),
				Pos.read(in),
				in.readEnum(Type.class),
				in.readNullableEnum(Figure.class),
				in.readNullableByTag(EXTRA_STEP)
		);
	}

	@Override
	default void writeTo(PacketOutputStream out) throws IOException {
		out.write(targetPos());
		out.write(takePos());
		out.writeEnum(type());
		out.writeNullableEnum(resultFigure());
		out.writeNullableWithTag(extraStep());
	}
}
