package x590.chess.gui.game;

import x590.chess.gui.GuiUtil;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Показывает текущее состояние игры
 */
public class StatePanel extends JPanel {

	private static final int PADDING = 16;

	private final JLabel stateLabel;

	private final GridBagConstraints additionalComponentConstraints;

	private @Nullable JComponent additionalComponent;

	public StatePanel(String initialState, @Nullable JComponent additionalComponent) {
		setLayout(new GridBagLayout());

		this.stateLabel = new JLabel(initialState, SwingConstants.CENTER);

		var font = getFont();
		stateLabel.setFont(new Font(font.getName(), Font.BOLD, (int) (font.getSize() * 1.2f)));
		stateLabel.setAlignmentX(CENTER_ALIGNMENT);

		var constraints = new GridBagConstraints();
		add(stateLabel, GuiUtil.constraintsWithCoords(constraints, 0, 0));

		this.additionalComponentConstraints = GuiUtil.constraintsWithCoords(constraints, 0, 1);

		if (additionalComponent != null) {
			this.additionalComponent = additionalComponent;
			additionalComponent.setAlignmentX(CENTER_ALIGNMENT);
			add(additionalComponent, constraints);
		}

		setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
	}

	public void setState(String state, @Nullable Icon icon) {
		setState(state, icon, null);
	}

	public void setState(String state, @Nullable Icon icon, @Nullable JComponent newAdditionalComponent) {
		stateLabel.setText(state);
		stateLabel.setIcon(icon);

		if (additionalComponent != null) {
			remove(additionalComponent);
		}

		if (newAdditionalComponent != null) {
			add(newAdditionalComponent, additionalComponentConstraints);
		}

		additionalComponent = newAdditionalComponent;
	}
}
