package x590.chess.packet.game;

import x590.chess.Main;
import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.remote.RemotePlayingSide;

import java.io.IOException;

/**
 * Ответ на предложение ничьи
 */
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

	private static final String
			OK = "Ок",
			EXIT = "Выйти";

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		if (!agree) {
			GuiUtil.showPlainMessageDialog(playingSide.getName() + " отказался от ничьей");
		} else {
			boardPanel.endGame("Ничья");

			String result = GuiUtil.showOptionDialog(playingSide.getName() + " согласился на ничью", "",
					OK, EXIT);

			if (EXIT.equals(result)) {
				Main.exitNormally();
			}
		}
	}
}
