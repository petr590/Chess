package x590.chess.packet;

import x590.chess.io.PacketOutputStream;

public abstract class SingletonPacket extends AbstractPacket {

	public SingletonPacket(String name) {
		super(name);
	}

	@Override
	public void writeData(PacketOutputStream out) {}
}
