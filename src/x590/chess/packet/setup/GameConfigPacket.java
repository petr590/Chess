package x590.chess.packet.setup;

import x590.chess.config.GameConfig;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.AbstractPacket;
import x590.chess.playingside.ClientboundRemotePlayingSide;
import x590.chess.playingside.RemotePlayingSide;

import java.io.IOException;

public class GameConfigPacket extends AbstractPacket {

	public static final String NAME = "GameConfig";

	private final GameConfig gameConfig;

	public GameConfigPacket(GameConfig gameConfig) {
		super(NAME);
		this.gameConfig = gameConfig;
	}

	public GameConfigPacket(PacketInputStream in) throws IOException {
		super(NAME);
		this.gameConfig = GameConfig.read(in);
	}


	@Override
	public void writeData(PacketOutputStream out) throws IOException {
		out.write(gameConfig);
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		if (playingSide instanceof ClientboundRemotePlayingSide client) {
			client.setServerGameConfig(gameConfig);
		}
	}
}
