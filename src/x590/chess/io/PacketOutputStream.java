package x590.chess.io;

import x590.chess.packet.Packet;
import x590.chess.packet.PacketOutputStreamWritable;
import x590.chess.packet.PacketOutputStreamWritableWithTag;
import x590.util.Logger;
import x590.util.annotation.Nullable;
import x590.util.function.throwing.ThrowingBiConsumer;

import java.io.*;

public class PacketOutputStream extends FilterOutputStream implements DataOutput {

	private final DataOutputStream out;

	public PacketOutputStream(OutputStream out) {
		super(out);
		this.out = out instanceof DataOutputStream dataOut ? dataOut : new DataOutputStream(out);
	}

	public PacketOutputStream(DataOutputStream out) {
		super(out);
		this.out = out;
	}


	/**
	 * Записывает флаг, означающий, является ли объект {@code null}.
	 * Затем вызывает функцию {@code writer}, если объект не {@code null}
	 */
	public <T> void writeNullable(@Nullable T object, ThrowingBiConsumer<T, PacketOutputStream, IOException> writer) throws IOException {
		if (object == null) {
			write(Tag.TAG_NULL);
		} else {
			write(Tag.TAG_NONNULL);
			writer.accept(object, this);
		}
	}


	/**
	 * Записывает объект, реализующий интерфейс {@link PacketOutputStreamWritable},
	 * путём вызова {@link PacketOutputStreamWritable#writeTo(PacketOutputStream)}
	 */
	public void write(PacketOutputStreamWritable writable) throws IOException {
		writable.writeTo(this);
	}

	/**
	 * Записывает флаг, означающий, является ли объект {@code null}.
	 * Затем записывает сам объект, если он не {@code null}
	 */
	public void writeNullable(@Nullable PacketOutputStreamWritable writable) throws IOException {
		writeNullable(writable, PacketOutputStreamWritable::writeTo);
	}


	/**
	 * Записывает тег, означающий тип объекта.
	 * Затем записывает сам объект
	 */
	public void writeWithTag(PacketOutputStreamWritableWithTag writable) throws IOException {
		write(writable.getTag());
		writable.writeTo(this);
	}


	/**
	 * Записывает тег, означающий тип объекта, или {@link Tag#TAG_NULL}, если объект {@code null}.
	 * Затем записывает сам объект, если он не {@code null}
	 */
	public void writeNullableWithTag(@Nullable PacketOutputStreamWritableWithTag writable) throws IOException {
		if (writable == null) {
			write(Tag.TAG_NULL);
		} else {
			writeWithTag(writable);
		}
	}


	/**
	 * Записывает пакет и отправляет его путём очищения потока методом {@link #flush()}
	 */
	public void writeAndSendPacket(Packet packet) throws IOException {
		packet.writeTo(this);
		flush();
		Logger.logf("Packet \"%s\" has been sent", packet.getName());
	}

	/**
	 * Записывает имя enum-значения
	 */
	public void writeEnum(Enum<?> value) throws IOException {
		out.writeUTF(value.name());
	}

	/**
	 * Записывает имя enum-значения или пустую строку, если он {@code null}
	 */
	public void writeNullableEnum(@Nullable Enum<?> value) throws IOException {
		out.writeUTF(value == null ? "" : value.name());
	}


	@Override
	public void writeBoolean(boolean v) throws IOException {
		out.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		out.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		out.writeShort(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		out.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		out.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		out.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		out.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		out.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		out.writeUTF(s);
	}
}
