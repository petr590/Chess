package x590.chess;

import x590.chess.board.ChessBoard;
import x590.chess.config.Config;
import x590.chess.figure.Side;
import x590.chess.gui.*;
import x590.chess.playingside.*;

import javax.swing.*;
import java.awt.*;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

public class Main {

	public static final String
			TITLE = "Шахматы ♛",
			TITLE_OFFLINE = TITLE + " (оффлайн)",
			TITLE_LOCAL_SERVER = TITLE + " (локальный сервер)",
			TITLE_CLIENT = TITLE + " (клиент)";

	private static Config config;

	private static JFrame frame;

	public static JFrame getFrame() {
		return frame;
	}

	public static Config getConfig() {
		return config;
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		config = Config.readOrCreateConfig(Config.DEFAULT_PATH);


		JFrame frame = Main.frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setResizable(true);
		frame.setVisible(true);

		GuiUtil.setup(frame);

		Runnable gameRunner = GuiUtil.showOptionDialog("", TITLE,
				new String[] { "Оффлайн", "Создать игру в локальной сети", "Подключиться к игре" },
				Main::gameOnThisComputer, Main::createGameInLocalNet, Main::connectToGameInLocalNet);

		if (gameRunner != null) {
			gameRunner.run();
		} else {
			exitNormally();
		}
	}

	public static void exit() {
		exitNormally();
	}

	private static void gameOnThisComputer() {
		runGame(TITLE_OFFLINE, new MonopolisticLocalPlayer(), new MonopolisticLocalPlayer(), Main::askUserForSide);
	}

	private static Side askUserForSide() {
		Side side = GuiUtil.showOptionDialog("Выберите цвет", TITLE,
				new String[] { "Белые", "Чёрные", "Случайно" },
				Side.WHITE, Side.BLACK, Side.randomSide()
		);

		if (side == null) {
			System.exit(0);
		}

		return side;
	}

	private static void createGameInLocalNet() {
		try {
			var serverboundPlayingSide = new ServerboundRemotePlayingSide();
			runGame(TITLE_LOCAL_SERVER,
					new LocalPlayer(),
					serverboundPlayingSide,
					() -> serverboundPlayingSide.getGameConfig().serverSide()
			);

		} catch (UncheckedIOException ex) {
			JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage());
			ex.printStackTrace();
			System.exit(1);
		}
	}


	private static final String ILLEGAL_ADDRESS_OR_PORT_MESSAGE = "Неверный формат адреса или порта";

	private static void connectToGameInLocalNet() {
		String nonSplittedAddressAndPort = JOptionPane.showInputDialog(
				frame, "Введите адрес и порт хоста:",
				null, JOptionPane.PLAIN_MESSAGE
		);

		if (nonSplittedAddressAndPort == null) {
			System.exit(0);
		}

		String[] addressAndPort = nonSplittedAddressAndPort.split(":");

		if (addressAndPort.length != 2) {
			GuiUtil.showPlainMessageDialog(ILLEGAL_ADDRESS_OR_PORT_MESSAGE);
			System.exit(0);
		}

		InetAddress inetAddress;
		int port;

		try {
			inetAddress = InetAddress.getByName(addressAndPort[0]);
		} catch (UnknownHostException ex) {
			GuiUtil.showPlainMessageDialog(ex);
			System.exit(0);
			return;
		}

		try {
			port = Integer.parseInt(addressAndPort[1]);
		} catch (NumberFormatException ex) {
			GuiUtil.showPlainMessageDialog(ILLEGAL_ADDRESS_OR_PORT_MESSAGE);
			System.exit(0);
			return;
		}

		try {
			var clientboundPlayingSide = new ClientboundRemotePlayingSide(inetAddress, port);

			runGame(TITLE_CLIENT,
					new LocalPlayer(),
					clientboundPlayingSide,
					clientboundPlayingSide::querySide
			);

		} catch (UncheckedIOException ex) {
			GuiUtil.showPlainMessageDialog(ex.getCause());
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static void runGame(String title, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide,
								Supplier<Side> sideSupplier) {

		frame.setTitle(title);
		frame.add(new GamePanel(ChessBoard.defaultPlacement(), sideSupplier, thisPlayingSide, opponentPlayingSide));
		frame.setVisible(true);
	}

	public static void exitNormally() {
		System.exit(0);
	}

	public static void exitWithError() {
		System.exit(1);
	}
}
