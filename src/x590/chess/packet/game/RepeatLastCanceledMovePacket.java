package x590.chess.packet.game;

import x590.chess.figure.move.IMove;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.io.Tag;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.RemotePlayingSide;

import java.io.IOException;

public class RepeatLastCanceledMovePacket extends AbstractPacket {

	public static final String NAME = "RepeatLastCanceledMove";

	private final IMove move;

	public RepeatLastCanceledMovePacket(IMove move) {
		super(NAME);
		this.move = move;
	}

	public RepeatLastCanceledMovePacket(PacketInputStream in) throws IOException {
		super(NAME);
		this.move = in.readByTag(Tag.MOVE);
	}

	@Override
	public void writeData(PacketOutputStream out) throws IOException {
		out.writeWithTag(move);
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		boardPanel.getGamePanel().repeatLastCanceledMove(move);
	}
}
