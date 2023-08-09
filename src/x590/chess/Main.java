package x590.chess;

import x590.chess.board.ChessBoard;
import x590.chess.config.Config;
import x590.chess.figure.Side;
import x590.chess.gui.*;
import x590.chess.gui.game.GamePanel;
import x590.chess.playingside.*;
import x590.chess.playingside.bot.LogicLocalBot;
import x590.chess.playingside.bot.RandomLocalBot;
import x590.chess.playingside.remote.ClientboundRemotePlayingSide;
import x590.chess.playingside.remote.ServerboundRemotePlayingSide;

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

	private static void gameOnThisComputer() {
		Supplier<PlayingSide> monopolisticLocalPlayerCreator = MonopolisticLocalPlayer::new;

		Supplier<PlayingSide> opponentSideCreator = GuiUtil.showOptionDialog("Играть:", "",
				new String[] { "С собой", "С рандомным ботом", "С линейным ботом" },
				monopolisticLocalPlayerCreator, RandomLocalBot::new, LogicLocalBot::new);

		if (opponentSideCreator == null) {
			exitNormally();
		}

		runGame(TITLE_OFFLINE,
				new MonopolisticLocalPlayer(),
				opponentSideCreator.get(),
				Main::askUserForSide,
				opponentSideCreator == monopolisticLocalPlayerCreator
		);
	}

	private static Side askUserForSide() {
		Side side = GuiUtil.showOptionDialog("Выберите цвет", TITLE,
				new String[] { "Белые", "Чёрные", "Случайно" },
				Side.WHITE, Side.BLACK, Side.randomSide()
		);

		if (side == null) {
			exitNormally();
		}

		return side;
	}

	private static void createGameInLocalNet() {
		try {
			var serverboundPlayingSide = new ServerboundRemotePlayingSide();
			runGame(TITLE_LOCAL_SERVER,
					new LocalPlayer(),
					serverboundPlayingSide,
					() -> serverboundPlayingSide.getGameConfig().serverSide(),
					false
			);

		} catch (UncheckedIOException ex) {
			JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage());
			ex.printStackTrace();
			exitWithError();
		}
	}


	private static final String ILLEGAL_ADDRESS_OR_PORT_MESSAGE = "Неверный формат адреса или порта";

	private static void connectToGameInLocalNet() {
		String result = JOptionPane.showInputDialog(
				frame, "Введите адрес и порт хоста:",
				null, JOptionPane.PLAIN_MESSAGE
		);

		if (result == null) {
			exitNormally();
		}

		String[] addressAndPort = result.split(":");

		if (addressAndPort.length != 2) {
			GuiUtil.showPlainMessageDialog(ILLEGAL_ADDRESS_OR_PORT_MESSAGE);
			exitNormally();
		}

		InetAddress inetAddress;
		int port;

		try {
			inetAddress = InetAddress.getByName(addressAndPort[0]);
		} catch (UnknownHostException ex) {
			GuiUtil.showPlainMessageDialog(ex);
			exitNormally();
			return;
		}

		try {
			port = Integer.parseInt(addressAndPort[1]);
		} catch (NumberFormatException ex) {
			GuiUtil.showPlainMessageDialog(ILLEGAL_ADDRESS_OR_PORT_MESSAGE);
			exitNormally();
			return;
		}

		try {
			var clientboundPlayingSide = new ClientboundRemotePlayingSide(inetAddress, port);

			runGame(TITLE_CLIENT,
					new LocalPlayer(),
					clientboundPlayingSide,
					clientboundPlayingSide::querySide,
					false
			);

		} catch (UncheckedIOException ex) {
			GuiUtil.showPlainMessageDialog(ex.getCause());
			ex.printStackTrace();
			exitWithError();
		}
	}

	private static void runGame(String title, PlayingSide thisPlayingSide, PlayingSide opponentPlayingSide,
								Supplier<Side> sideSupplier, boolean isWithSelf) {

		frame.setTitle(title);
		frame.add(new GamePanel(ChessBoard.defaultPlacement(), sideSupplier, thisPlayingSide, opponentPlayingSide, isWithSelf));
		frame.setVisible(true);
	}

	public static void exitNormally() {
		System.exit(0);
	}

	public static void exitWithError() {
		System.exit(1);
	}
}
