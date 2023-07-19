package x590.chess.packet.game;

import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.RemotePlayingSide;

import java.io.IOException;

public class DrawOfferResponsePacket extends AbstractPacket {

	public static final String NAME = "DrawOfferResponse";

	private final boolean agree;

	public DrawOfferResponsePacket(boolean agree) {
		super(NAME);
		this.agree = agree;
	}

	public DrawOfferResponsePacket(PacketInputStream in) throws IOException {
		super(NAME);
		this.agree = in.readBoolean();
	}

	@Override
	public void writeData(PacketOutputStream out) throws IOException {
		out.writeBoolean(agree);
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		GuiUtil.showPlainMessageDialog(playingSide.getName() +
				(agree ? " согласился на ничью" : " отказался от ничьей"));

		if (agree) {
			playingSide.onGameEnd();
		}
	}
}
