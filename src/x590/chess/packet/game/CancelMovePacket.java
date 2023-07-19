package x590.chess.packet.game;

import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.io.Tag;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.RemotePlayingSide;
import x590.util.annotation.Nullable;

import java.io.IOException;

public class CancelMovePacket extends AbstractPacket {

	public static final String NAME = "CancelMove";

	private final IMove move;
	private final @Nullable IStep prevStep;

	public CancelMovePacket(IMove move, @Nullable IStep prevStep) {
		super(NAME);
		this.move = move;
		this.prevStep = prevStep;
	}

	public CancelMovePacket(PacketInputStream in) throws IOException {
		super(NAME);
		this.move = in.readByTag(Tag.MOVE);
		this.prevStep = in.readNullableByTag(Tag.STEP);
	}

	@Override
	public void writeData(PacketOutputStream out) throws IOException {
		out.writeWithTag(move);
		out.writeNullableWithTag(prevStep);
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		boardPanel.getGamePanel().cancelMove(move, prevStep);
	}
}
