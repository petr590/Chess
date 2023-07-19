package x590.chess.packet;

import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketOutputStream;
import x590.chess.playingside.RemotePlayingSide;

import java.io.IOException;

public interface Packet extends PacketOutputStreamWritable {

	String getName();

	void writeData(PacketOutputStream out) throws IOException;

	@Override
	default void writeTo(PacketOutputStream out) throws IOException {
		out.writeUTF(getName());
		writeData(out);
	}

	void handle(RemotePlayingSide playingSide, BoardPanel boardPanel);
}
