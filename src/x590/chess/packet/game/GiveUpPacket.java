package x590.chess.packet.game;

import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.packet.SingletonPacket;
import x590.chess.playingside.RemotePlayingSide;

public final class GiveUpPacket extends SingletonPacket {

	public static final String NAME = "GiveUp";

	public static final GiveUpPacket INSTANCE = new GiveUpPacket();

	private GiveUpPacket() {
		super(NAME);
	}

	public static GiveUpPacket getInstance() {
		return INSTANCE;
	}

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		GuiUtil.showPlainMessageDialog(playingSide.getName() + " сдался");
		playingSide.onGameEnd();
	}
}
