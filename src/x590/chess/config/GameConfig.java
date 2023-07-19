package x590.chess.config;

import x590.chess.Main;
import x590.chess.figure.Side;
import x590.chess.io.PacketInputStream;
import x590.chess.io.PacketOutputStream;
import x590.chess.packet.PacketOutputStreamWritable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * Настройки игры. Как правило, сетевой
 */
public record GameConfig(Side serverSide, boolean allowMoveCancel, int offerADrawTimeout /* Несмотря на название, измеряется не в секундах, а в количестве ходов */)
		implements PacketOutputStreamWritable {

	public static final int
			MIN_TIMEOUT = 0,
			MAX_TIMEOUT = 0xFF,
			DEFAULT_TIMEOUT = 16;

	public GameConfig {
		if (offerADrawTimeout < MIN_TIMEOUT || offerADrawTimeout > MAX_TIMEOUT) {
			throw new IllegalArgumentException("offerADrawTimeout is out of range " + MIN_TIMEOUT + ".." + MAX_TIMEOUT);
		}
	}

	private static final int PADDING = 12;
	private static final Dimension PADDING_DIMENSION = new Dimension(0, PADDING);

	private static final String CREATE_GAME = "Создать игру";

	public static GameConfig askUserGameConfig() {

		var panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		var whiteSide = new JRadioButton("Белые");
		var blackSide = new JRadioButton("Чёрные");
		var randomSide = new JRadioButton("Случайно");

		var sideGroup = new ButtonGroup();
		sideGroup.add(whiteSide);
		sideGroup.add(blackSide);
		sideGroup.add(randomSide);
		whiteSide.setSelected(true);

		panel.add(whiteSide);
		panel.add(blackSide);
		panel.add(randomSide);


		panel.add(Box.createRigidArea(PADDING_DIMENSION));


		var allowMoveCancel = new JCheckBox("Разрешить отмену ходов");
		panel.add(allowMoveCancel);


		panel.add(Box.createRigidArea(PADDING_DIMENSION));


		var format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);

		var numberFormatter = new NumberFormatter(format);
		numberFormatter.setMinimum(MIN_TIMEOUT);
		numberFormatter.setMaximum(MAX_TIMEOUT);
		numberFormatter.setAllowsInvalid(false);

		var offerADrawTimeout = new JFormattedTextField(numberFormatter);
		offerADrawTimeout.setValue(DEFAULT_TIMEOUT);

		var offerADrawTimeoutPanel = new JPanel();
		offerADrawTimeoutPanel.add(new JLabel("Блокировать кнопку \"Предложить ничью\" на"));
		offerADrawTimeoutPanel.add(offerADrawTimeout);
		offerADrawTimeoutPanel.add(new JLabel("ходов"));
		panel.add(offerADrawTimeoutPanel);


		int result = JOptionPane.showOptionDialog(Main.getFrame(), panel, CREATE_GAME,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				new String[] { CREATE_GAME }, CREATE_GAME
		);

		if (result == -1) {
			System.exit(0);
		}

		Side side = whiteSide.isSelected() ? Side.WHITE :
					blackSide.isSelected() ? Side.BLACK : Side.randomSide();

		return new GameConfig(side, allowMoveCancel.isSelected(), (Integer) offerADrawTimeout.getValue());
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
