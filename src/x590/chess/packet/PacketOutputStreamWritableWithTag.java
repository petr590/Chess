package x590.chess.packet;

import x590.chess.io.Tag;

public interface PacketOutputStreamWritableWithTag extends PacketOutputStreamWritable {

	/**
	 * @return Тег, определяющий тип записываемых данных.
	 * Должен помещаться в 1 байт. Не должен быть равен {@link Tag#TAG_NULL}.
	 * Обычно одно из значений из класса {@link Tag}
	 */
	int getTag();
}
