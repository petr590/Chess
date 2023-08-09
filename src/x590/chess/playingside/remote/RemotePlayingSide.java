package x590.chess.playingside.remote;

import x590.chess.Main;
import x590.chess.figure.move.IMove;
import x590.chess.figure.step.IStep;
import x590.chess.gui.GuiUtil;
import x590.chess.gui.board.BoardPanel;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.*;
import x590.chess.packet.game.*;
import x590.chess.packet.setup.SayNamePacket;
import x590.chess.playingside.PlayingSide;
import x590.util.Logger;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Удалённый игрок, подключившийся через локальную сеть
 */
public abstract class RemotePlayingSide implements PlayingSide {

	protected volatile @Nullable Socket socket;
	protected volatile @Nullable PacketInputStream in;
	protected volatile @Nullable PacketOutputStream out;

	private String name = "unknown";

	private @Nullable ExecutorService executor;

	private @Nullable BoardPanel boardPanel;


	private static final long SLEEP_TIME_MILLIS = 1000 / 20; // 20 раз в секунду проверяем, пришёл ли пакет

	private long totalLoopTime, totalActiveTime;

	private final String
			GIVE_UP = "Сдаться",
			OFFER_A_DRAW = "Предложить ничью",
			CANCEL = "Отмена";

	protected boolean showMessageOnException = true;

	public RemotePlayingSide() {
		JFrame frame = Main.getFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (socket == null) { // Нет подключения
					onGameEnd(true);
				} else if (!socket.isClosed()) {
					String result = GuiUtil.showOptionDialog("", "",
							 GIVE_UP, OFFER_A_DRAW, CANCEL);

					if (result != null) {
						switch (result) {
							case GIVE_UP -> {
								onGiveUp();
								onGameEnd(true);
							}

							case OFFER_A_DRAW -> {
								if (boardPanel != null) {
									boardPanel.lockDrawOfferButton();
								}

								offerADraw();
							}
						}
					}
				} else { // Соединение закрыто
					Main.exitNormally();
				}
			}
		});
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		boardPanel.setOpponentName(name);
	}

	public abstract int getOfferADrawTimeout();

	@Override
	public void setup(BoardPanel boardPanel) {
		this.boardPanel = boardPanel;
		this.executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			try {
				run(boardPanel);
			} catch (IOException ex) {
				if (showMessageOnException) {
					GuiUtil.showPlainMessageDialog(ex);
				}
				throw new UncheckedIOException(ex);

			} catch (Throwable ex) {
				if (showMessageOnException) {
					GuiUtil.showPlainMessageDialog(ex);
				}
				throw ex;
			}
		});
	}

	/**
	 * Запускает игру. Вызывается в фоновом потоке.
	 */
	protected abstract void run(BoardPanel boardPanel) throws IOException;

	/**
	 * Отправляет начальные пакеты, такие как {@link SayNamePacket}
	 */
	protected void sendStartPackets() throws IOException {
		out.writeAndSendPacket(new SayNamePacket(Main.getConfig().getName()));
	}

	/**
	 * Принимает пакеты из потока {@code in}. Число проверок в секунду зависит от {@link #SLEEP_TIME_MILLIS}.
	 * Этот метод должен запускаться в фоновом потоке
	 */
	protected void receivePackets(BoardPanel boardPanel, Socket socket, PacketInputStream in) throws IOException {
		long loopStartTime = System.currentTimeMillis();
		totalActiveTime = 0;

		while (!socket.isClosed()) {
			long startTime = System.currentTimeMillis();

			// Дополнительная проверка !socket.isClosed() нужна,
			// так как in.available() может выбрасывать SocketException,
			// даже несмотря на проверку выше
			while (!socket.isClosed() && in.available() > 0) {
				Packet packet = in.readPacket();
				Logger.logf("Packet \"%s\" received", packet.getName());
				packet.handle(this, boardPanel);
			}

			totalActiveTime += System.currentTimeMillis() - startTime;

			try {
				Thread.sleep(SLEEP_TIME_MILLIS); // Работает лучше, чем Thread.onSpinWait()
			} catch (InterruptedException ex) {
				Logger.log(ex.getMessage());
				return;
			}

			totalLoopTime = System.currentTimeMillis() - loopStartTime;
		}
	}


	public void sendPacket(Packet packet) {
		try {
			out.writeAndSendPacket(packet);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public void sendPacketIfPossible(Packet packet) {
		if (out != null) {
			sendPacket(packet);
		}
	}

	public void offerADraw() {
		sendPacketIfPossible(OfferADrawPacket.getInstance());
	}

	public void onGiveUp() {
		sendPacketIfPossible(GiveUpPacket.getInstance());
	}


	@Override
	public void onMoveMake(IMove move) {
		sendPacket(new MakeMovePacket(move));
	}

	@Override
	public void onMoveCancel(IMove move, @Nullable IStep prevStep) {
		sendPacket(new CancelMovePacket(move, prevStep));
	}

	@Override
	public void onLastCanceledMoveRepeat(IMove move) {
		sendPacket(new RepeatLastCanceledMovePacket(move));
	}

	@Override
	public void onGameEnd() {
		onGameEnd(false);
	}

	public void onGameEnd(boolean closeFrame) {
		Logger.log("Closing all streams and socket");

		if (totalLoopTime != 0 && totalActiveTime != 0) {
			Logger.logf("Time = %d / %d = %.2f%%", totalActiveTime, totalLoopTime, 100f * totalActiveTime / totalLoopTime);
		}

		try {
			closeIfNotNull(in);
			closeIfNotNull(out);
			closeIfNotNull(socket);

			if (executor != null) {
				executor.shutdownNow();

				try {
					if (!executor.awaitTermination(10, TimeUnit.MILLISECONDS)) {
						Logger.warning("Failed to stop background thread");
					}
				} catch (InterruptedException ex) {
					Logger.warning("Failed to stop background thread: " + ex);
				}
			}

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} finally {
			if (closeFrame) {
				Main.exitNormally();
			}
		}
	}

	private static void closeIfNotNull(@Nullable Closeable closeable) throws IOException {
		if (closeable != null)
			closeable.close();
	}

	@Override
	public boolean canMakeMove() {
		return false;
	}

	@Override
	public boolean canSelectField() {
		return false;
	}
}
