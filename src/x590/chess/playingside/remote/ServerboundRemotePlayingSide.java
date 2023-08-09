package x590.chess.playingside.remote;

import x590.chess.Main;
import x590.chess.config.GameConfig;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.setup.GameConfigPacket;
import x590.util.Logger;
import x590.util.annotation.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class ServerboundRemotePlayingSide extends RemotePlayingSide {

	private final ServerSocket serverSocket;

	private final String address;

	private final GameConfig gameConfig;

	private volatile boolean ready;

	public ServerboundRemotePlayingSide() {

		InetSocketAddress randomSocketAddress = new InetSocketAddress(0);

		try {
			this.serverSocket = new ServerSocket();
			serverSocket.bind(randomSocketAddress);

			this.address = InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		this.gameConfig = GameConfig.askUserGameConfig();
	}

	public GameConfig getGameConfig() {
		return gameConfig;
	}

	public int getOfferADrawTimeout() {
		return gameConfig.offerADrawTimeout();
	}

	@Override
	public void setup(BoardPanel boardPanel) {
		Logger.log("ServerboundRemotePlayingSide setup");
		super.setup(boardPanel);
	}


	@Override
	protected void run(BoardPanel boardPanel) throws IOException {
		showIPAddress(address);

		Logger.log("Socket accepting...");

		try {
			this.socket = serverSocket.accept();
		} catch (SocketException ex) {
			// Если во время ожидания сокет закрылся, нужно просто завершить выполнение потока
			if ("Socket closed".equals(ex.getMessage())) {
				Logger.log("Server socket closed");
				return;
			}

			throw ex;
		}

		Logger.log("Socket accepted");

		this.in = new PacketInputStream(socket.getInputStream());
		this.out = new PacketOutputStream(socket.getOutputStream());

		sendStartPackets();

		this.ready = true;
		boardPanel.getGamePanel().updateState();
		boardPanel.unlockGameEndButtons();

		receivePackets(boardPanel, socket, in);
	}

	@Override
	protected void sendStartPackets() throws IOException {
		out.writeAndSendPacket(new GameConfigPacket(gameConfig));
		super.sendStartPackets();
	}


	private static final int PADDING = 8;
	private static final Dimension PADDING_DIMENSION = new Dimension(PADDING, 0);

	public static void showIPAddress(String address) {
		var dialog = new JDialog(Main.getFrame(), Main.TITLE, true);

		dialog.setContentPane(createIPAddressPanel(address, () -> dialog.setVisible(false)));
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}


	private static JPanel createIPAddressPanel(String address, Runnable onClick) {
		var panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		panel.add(new JLabel("<html>Локальный IP адрес: <i>" + address + "</i></html>", SwingConstants.CENTER));

		panel.add(Box.createRigidArea(PADDING_DIMENSION));

		var copyButton = new JButton("⎘");
		copyButton.setToolTipText("Копировать IP");
		copyButton.addActionListener(
				event -> {
					Toolkit.getDefaultToolkit()
							.getSystemClipboard()
							.setContents(new StringSelection(address), null);

					onClick.run();
				}
		);

		panel.add(copyButton);

		return panel;
	}


	@Override
	public void onGameEnd() {
		super.onGameEnd();

		try {
			serverSocket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean canCancelMove() {
		return gameConfig.allowMoveCancel();
	}

	@Override
	public void onGameEnd(boolean closeFrame) {
		if (serverSocket != null) {
			showMessageOnException = false;

			try {
				serverSocket.close();
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

		super.onGameEnd(closeFrame);
	}

	@Override
	public boolean ready() {
		return ready;
	}

	@Override
	public String getInitialState() {
		return "Ожидание подключения...";
	}

	@Override
	public @Nullable JComponent getAdditionalInitialState() {
		return createIPAddressPanel(address, () -> {});
	}

	@Override
	public boolean shouldLockGameEndButtons() {
		return socket == null; // Если соединения ещё нет, значит мы должны заблокировать кнопки
	}
}
