package x590.chess.packet.setup;

import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.RemotePlayingSide;

import java.io.IOException;

/**
 * Пакет, который сообщает имя игрока.<br>
 *
 * <b>P.S.</b><br>
 * Say my name!<br>
 * You are the magic, マボロシ!<br>
 * Say my name!<br>
 * ...
 */
public class SayNamePacket extends AbstractPacket {

	public static final String NAME = "SayName";

	private final String name;

	public SayNamePacket(String name) {
		super(NAME);
		this.name = name;
	}

	public SayNamePacket(PacketInputStream in) throws IOException {
		super(NAME);
		this.name = in.readUTF();
	}

	@Override
	public void writeData(PacketOutputStream out) throws IOException {
		out.writeUTF(name);
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		playingSide.setName(name);
	}
}
