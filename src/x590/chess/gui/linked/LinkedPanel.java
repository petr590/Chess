package x590.chess.gui.linked;

import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * LinkedPanel - панель, связанная с другой панелью,
 * т.е. их размер будет всегда одинаковым, равным наибольшему из их изначальных размеров.
 * Для связывания двух панелей друг с другом достаточно передать одну панель в конструктор другой,
 * или вызвать метод {@link #linkTo(JComponent)} у одной из них
 */
public class LinkedPanel extends JPanel implements ILinkedComponent<LinkedPanel> {

	private @Nullable LinkedPanel other;

	public LinkedPanel() {}

	public LinkedPanel(LinkedPanel other) {
		linkTo(other);
	}

	@Override
	public @Nullable LinkedPanel getLinked() {
		return other;
	}

	@Override
	public void setLinked(LinkedPanel other) {
		this.other = other;
	}

	@Override
	public Dimension originalPreferredSize() {
		return super.getPreferredSize();
	}

	@Override
	public final Dimension getPreferredSize() {
		return ILinkedComponent.super.getPreferredSize();
	}
}
