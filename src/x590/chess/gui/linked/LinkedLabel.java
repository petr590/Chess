package x590.chess.gui.linked;

import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * LinkedLabel - лейбл, связанный с другим лейблом,
 * т.е. их размер будет всегда одинаковым, равным наибольшему из их изначальных размеров.
 * Для связывания двух панелей друг с другом достаточно передать одну панель в конструктор другой,
 * или вызвать метод {@link #linkTo(JComponent)} у одной из них
 */
public class LinkedLabel extends JLabel implements ILinkedComponent<LinkedLabel> {

	private @Nullable LinkedLabel other;

	public LinkedLabel() {}

	public LinkedLabel(LinkedLabel other) {
		linkTo(other);
	}

	public LinkedLabel(String name, int horizontalAlignment) {
		super(name, horizontalAlignment);
	}

	@Override
	public @Nullable LinkedLabel getLinked() {
		return other;
	}

	@Override
	public void setLinked(LinkedLabel other) {
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
