package x590.chess.io;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import x590.chess.figure.Pos;
import x590.chess.figure.move.IMove;
import x590.chess.figure.move.IMove.IExtraMove;
import x590.chess.figure.step.IStep;
import x590.chess.figure.step.IStep.IExtraStep;
import x590.chess.packet.PacketOutputStreamWritableWithTag;
import x590.util.IntegerUtil;
import x590.util.Util;
import x590.util.annotation.Nullable;
import x590.util.function.throwing.ThrowingFunction;

import java.io.IOException;

/**
 * Некоторые объекты могут записываться в поток по-разному, в зависимости от реализации.
 * Перед такими объектами записывается тег, который определяет, какой именно объект записан.
 */
public final class Tag<T extends PacketOutputStreamWritableWithTag> {

	public interface IReader<T extends PacketOutputStreamWritableWithTag> {

		/**
		 * @return Объект, прочитанный из потока {@code in}
		 */
		T read(PacketInputStream in) throws IOException;

		/**
		 * @return {@code true}, если возвращаемый объект может быть приведён к типу {@code type}
		 */
		boolean canCastTo(Class<? extends PacketOutputStreamWritableWithTag> type);
	}


	public static class Reader<T extends PacketOutputStreamWritableWithTag> implements IReader<T> {
		private final ThrowingFunction<PacketInputStream, T, IOException> reader;

		private final @Nullable Class<T> objectType;

		public Reader(ThrowingFunction<PacketInputStream, T, IOException> reader, @Nullable Class<T> objectType) {
			this.reader = reader;
			this.objectType = objectType;
		}

		@Override
		public T read(PacketInputStream in) throws IOException {
			return reader.accept(in);
		}

		@Override
		public boolean canCastTo(Class<? extends PacketOutputStreamWritableWithTag> type) {
			return objectType == null || type.isAssignableFrom(objectType);
		}
	}

	public static final int
			TAG_NULL       = 0x00,
			TAG_NONNULL    = 0x01,
			TAG_POS        = 0x02,
			TAG_STEP       = 0x03,
			TAG_EXTRA_STEP = 0x04,
			TAG_MOVE       = 0x05,
			TAG_EXTRA_MOVE = 0x06;

	private static final Int2ObjectMap<IReader<?>>
			READERS = Util.make(new Int2ObjectArrayMap<>(), map -> {
				map.put(TAG_NULL,       new Reader<>(in -> null,       null));
				map.put(TAG_POS,        new Reader<>(Pos::read,        Pos.class));
				map.put(TAG_STEP,       new Reader<>(IStep::read,      IStep.class));
				map.put(TAG_EXTRA_STEP, new Reader<>(IExtraStep::read, IExtraStep.class));
				map.put(TAG_MOVE,       new Reader<>(IMove::read,      IMove.class));
				map.put(TAG_EXTRA_MOVE, new Reader<>(IExtraMove::read, IExtraMove.class));
			});

	private final Class<T> clazz;

	private Tag(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * @return {@link IReader}, соответствующий переданному тегу
	 * @throws PacketReadException если переданный тег не соответствует ни одному {@link IReader}
	 *                             или имеет неправильный тип
	 */
	@SuppressWarnings("unchecked")
	public @Nullable IReader<T> readerByTag(int tag) throws IOException {
		IReader<?> reader = READERS.get(tag);

		if (reader == null) {
			throw new PacketReadException("Unknown tag 0x" + IntegerUtil.hex2(tag));
		}

		if (reader.canCastTo(clazz)) {
			return (IReader<T>) reader;
		}

		throw new PacketReadException("Tag 0x" + IntegerUtil.hex2(tag) + " is not applicable for class " + clazz);
	}

	public static final Tag<IStep> STEP = new Tag<>(IStep.class);
	public static final Tag<IExtraStep> EXTRA_STEP = new Tag<>(IExtraStep.class);
	public static final Tag<IMove> MOVE = new Tag<>(IMove.class);
	public static final Tag<IExtraMove> EXTRA_MOVE = new Tag<>(IExtraMove.class);


	@Override
	public String toString() {
		return clazz.getCanonicalName();
	}
}
