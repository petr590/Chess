package x590.chess.packet;

import x590.chess.io.PacketOutputStream;

import java.io.IOException;

/**
 * Объект, который можно записать в {@link PacketOutputStream}
 */
public interface PacketOutputStreamWritable {

	/**
	 * Метод записи
	 */
	void writeTo(PacketOutputStream out) throws IOException;
}
