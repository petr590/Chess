package x590.chess.io;

import x590.chess.packet.Packet;
import x590.chess.packet.PacketOutputStreamWritableWithTag;
import x590.chess.packet.PacketReaders;
import x590.util.annotation.Nullable;

import java.io.*;

public class PacketInputStream extends FilterInputStream implements DataInput {

	private final DataInputStream in;

	public PacketInputStream(InputStream in) {
		super(in);
		this.in = in instanceof DataInputStream dataIn ? dataIn : new DataInputStream(in);
	}

	public <T extends PacketOutputStreamWritableWithTag> T readByTag(Tag<T> tag) throws IOException {
		T value = readNullableByTag(tag);

		if (value != null) {
			return value;
		}

		throw new PacketReadException("Value is null");
	}

	public <T extends PacketOutputStreamWritableWithTag> @Nullable T readNullableByTag(Tag<T> tag) throws IOException {
		return tag.readerByTag(read()).read(this);
	}

	public Packet readPacket() throws IOException {
		String name = readUTF();
		var reader = PacketReaders.get(name);

		if (reader == null) {
			throw new PacketReadException("Unknown packet \"" + name + "\"");
		}

		return reader.read(this);
	}

	public <E extends Enum<E>> E readEnum(Class<E> clazz) throws IOException {
		E value = readNullableEnum(clazz);

		if (value != null) {
			return value;
		}

		throw new PacketReadException("Enum constant is null");
	}

	public <E extends Enum<E>> @Nullable E readNullableEnum(Class<E> clazz) throws IOException {
		E[] constants = clazz.getEnumConstants();

		if (constants == null) {
			throw new IllegalArgumentException("Class " + clazz + " is not an enum class");
		}

		String name = in.readUTF();

		if (name.isEmpty()) {
			return null;
		}

		for (E constant : constants) {
			if (constant.name().equals(name)) {
				return constant;
			}
		}

		throw new PacketReadException("Unknown enum constant " + clazz.getName() + "." + name);
	}

	public boolean hasNullFlag() throws IOException {
		return read() == 0;
	}


	@Override
	public void readFully(byte[] b) throws IOException {
		in.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		in.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return in.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	@SuppressWarnings("deprecation")
	public String readLine() throws IOException {
		return in.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}
}
