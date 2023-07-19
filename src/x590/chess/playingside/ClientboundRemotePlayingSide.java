package x590.chess.playingside;

import x590.chess.config.GameConfig;
import x590.chess.figure.Side;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.util.Logger;
import x590.util.Timer;
import x590.util.annotation.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientboundRemotePlayingSide extends RemotePlayingSide {

	private final InetAddress address;
	private final int port;

	private volatile @Nullable GameConfig serverGameConfig;

	public ClientboundRemotePlayingSide(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Ждёт, пока {@link #serverGameConfig} не инициализируется из другого потока
	 * @return Сторону, за которую играет этот игрок
	 */
	public Side querySide() {
		Logger.log("Client waiting for GameConfigPacket receiving");
		Timer timer = Timer.startNewTimer();

		while (serverGameConfig == null) {
			Thread.onSpinWait();
		}

		timer.logElapsed("Client waiting");

		return serverGameConfig.serverSide().opposite();
	}

	public int getOfferADrawTimeout() {
		return serverGameConfig.offerADrawTimeout();
	}

	public void setServerGameConfig(GameConfig gameConfig) {
		this.serverGameConfig = gameConfig;
	}

	@Override
	public void setup(BoardPanel boardPanel) {
		Logger.log("ClientboundRemotePlayingSide setup");
		super.setup(boardPanel);
	}

	@Override
	protected void run(BoardPanel boardPanel) throws IOException {
		try {
			this.socket = new Socket(address, port);
			this.in = new PacketInputStream(socket.getInputStream());
			this.out = new PacketOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		sendStartPackets();
		receivePackets(boardPanel, socket, in);
	}

	@Override
	public boolean canCancelMove() {
		return serverGameConfig.allowMoveCancel();
	}
}
