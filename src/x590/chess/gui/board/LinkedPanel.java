package x590.chess.gui.board;

import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * LinkedPanel - панель, связанная с другой панелью,
 * т.е. их размер будет всегда одинаковым, равным наибольшему из их изначальных размеров.
 * Для связывания двух панелей друг с другом достаточно передать одну панель в конструктор другой,
 * или вызвать метод {@link #linkTo(LinkedPanel)} у одной из них
 */
public class LinkedPanel extends JPanel {

	private @Nullable LinkedPanel other;

	public LinkedPanel() {}

	public LinkedPanel(LinkedPanel other) {
		this.other = other;
		other.link(this);
	}

	private void link(LinkedPanel other) {
		if (this.other != null) {
			throw new IllegalStateException("LinkedPanel already linked with other panel");
		}

		this.other = other;
	}

	public void linkTo(LinkedPanel other) {
		this.link(other);
		other.link(this);
	}

	/**
	 * Переопределяйте этот метод вместо {@link #getPreferredSize()}
	 * @return Предпочтительный размер компонента, без учёта связанного с ним компонента
	 */
	protected Dimension originalPreferredSize() {
		return super.getPreferredSize();
	}

	@Override
	public final Dimension getPreferredSize() {
		if (other == null) {
			return originalPreferredSize();
		}

		var size1 = this.originalPreferredSize();
		var size2 = other.originalPreferredSize();

		return new Dimension(
				Math.max(size1.width, size2.width),
				Math.max(size1.height, size2.height)
		);
	}
}
