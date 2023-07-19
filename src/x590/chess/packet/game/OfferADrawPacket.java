package x590.chess.packet.game;

import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.packet.SingletonPacket;
import x590.chess.playingside.RemotePlayingSide;

/**
 * Предложение ничьи
 */
public final class OfferADrawPacket extends SingletonPacket {

	public static final String NAME = "OfferADraw";

	private static final OfferADrawPacket INSTANCE = new OfferADrawPacket();

	private OfferADrawPacket() {
		super(NAME);
	}

	public static OfferADrawPacket getInstance() {
		return INSTANCE;
	}

	private static final String
			AGREE = "Согласиться",
			REFUSE = "Отказаться";

	@Override
	public void handle(RemotePlayingSide playingSide, BoardPanel boardPanel) {
		boolean agree = GuiUtil.showYesNoOptionDialog(playingSide.getName() + " предложил ничью", "", AGREE, REFUSE);
		playingSide.sendPacket(new DrawOfferResponsePacket(agree));

		if (agree) {
			playingSide.onGameEnd();
		}
	}
}
