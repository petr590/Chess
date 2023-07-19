package x590.chess.packet;

public abstract class AbstractPacket implements Packet {

	private final String name;

	public AbstractPacket(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
