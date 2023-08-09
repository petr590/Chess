package x590.chess.config;

import x590.chess.Main;
import x590.chess.figure.Side;
import x590.chess.gui.GuiUtil;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.PacketOutputStreamWritable;
import x590.util.annotation.Immutable;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * Настройки сетевой игры
 */
@Immutable
public class GameConfig implements PacketOutputStreamWritable {

	public static final boolean DEFAULT_ALLOW_MOVE_CANCEL = false;

	public static final int
			MIN_TIMEOUT = 0,
			MAX_TIMEOUT = 0xFF,
			DEFAULT_TIMEOUT = 16;

	private final Side serverSide;

	private final boolean allowMoveCancel;

	// Несмотря на название, измеряется не в секундах, а в количестве ходов
	private final int offerADrawTimeout;


	public GameConfig(Side serverSide, boolean allowMoveCancel, int offerADrawTimeout) {
		if (offerADrawTimeout < MIN_TIMEOUT || offerADrawTimeout > MAX_TIMEOUT) {
			throw new IllegalArgumentException("offerADrawTimeout is out of range " + MIN_TIMEOUT + ".." + MAX_TIMEOUT);
		}

		this.serverSide = serverSide;
		this.allowMoveCancel = allowMoveCancel;
		this.offerADrawTimeout = offerADrawTimeout;
	}

	public Side serverSide() {
		return serverSide;
	}

	public boolean allowMoveCancel() {
		return allowMoveCancel;
	}

	public int offerADrawTimeout() {
		return offerADrawTimeout;
	}


	private static final int PADDING = 12;
	private static final Dimension PADDING_DIMENSION = new Dimension(0, PADDING);

	private static final String CREATE_GAME = "Создать игру";

	public static GameConfig askUserGameConfig() {

		var defaultGameConfig = Main.getConfig().defaultGameConfig();

		var panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));


		var nameField = new JTextField(Main.getConfig().getName());

		var namePanel = new JPanel();
		namePanel.add(new JLabel("Имя: "));
		namePanel.add(nameField);
		panel.add(namePanel);


		panel.add(Box.createRigidArea(PADDING_DIMENSION));


		var whiteSide = new JRadioButton("Белые");
		var blackSide = new JRadioButton("Чёрные");
		var randomSide = new JRadioButton("Случайно");

		var sideGroup = new ButtonGroup();
		sideGroup.add(whiteSide);
		sideGroup.add(blackSide);
		sideGroup.add(randomSide);

		defaultGameConfig.serverSide.choose(whiteSide, blackSide).setSelected(true);

		panel.add(whiteSide);
		panel.add(blackSide);
		panel.add(randomSide);


		panel.add(Box.createRigidArea(PADDING_DIMENSION));


		var allowMoveCancel = new JCheckBox("Разрешить отмену ходов");
		allowMoveCancel.setSelected(defaultGameConfig.allowMoveCancel);
		panel.add(allowMoveCancel);


		panel.add(Box.createRigidArea(PADDING_DIMENSION));


		var format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);

		var numberFormatter = new NumberFormatter(format);
		numberFormatter.setMinimum(MIN_TIMEOUT);
		numberFormatter.setMaximum(MAX_TIMEOUT);
		numberFormatter.setAllowsInvalid(false);

		var offerADrawTimeout = new JFormattedTextField(numberFormatter);
		offerADrawTimeout.setValue(defaultGameConfig.offerADrawTimeout);

		var offerADrawTimeoutPanel = new JPanel();
		offerADrawTimeoutPanel.add(new JLabel("Блокировать кнопку \"Предложить ничью\" на"));
		offerADrawTimeoutPanel.add(offerADrawTimeout);
		offerADrawTimeoutPanel.add(new JLabel("ходов"));
		panel.add(offerADrawTimeoutPanel);


		String result = GuiUtil.showOptionDialog(panel, CREATE_GAME, CREATE_GAME);

		if (result == null) {
			Main.exitNormally();
		}

		var sideGetter = whiteSide.isSelected() ? SideGetter.WHITE :
					blackSide.isSelected() ? SideGetter.BLACK : SideGetter.RANDOM;

		var gameConfig = new SerializedGameConfig(sideGetter, allowMoveCancel.isSelected(), (Integer) offerADrawTimeout.getValue());

		Main.getConfig().setAndTrySave(nameField.getText(), gameConfig);

		return gameConfig;
	}

	public static GameConfig read(PacketInputStream in) throws IOException {
		return new GameConfig(
				in.readEnum(Side.class),
				in.readBoolean(),
				in.readUnsignedByte()
		);
	}

	@Override
	public void writeTo(PacketOutputStream out) throws IOException {
		out.writeEnum(serverSide);
		out.writeBoolean(allowMoveCancel);
		out.writeByte(offerADrawTimeout);
	}
}
