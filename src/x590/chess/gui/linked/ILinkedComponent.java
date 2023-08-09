package x590.chess.gui.linked;

import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

interface ILinkedComponent<C extends JComponent & ILinkedComponent<C>> extends ComponentWithDependentSize {

	@Nullable C getLinked();

	void setLinked(C other);

	private void link(C other) {
		if (getLinked() != null) {
			throw new IllegalStateException(getClass().getSimpleName() + " already linked with other panel");
		}

		setLinked(other);
	}

	@SuppressWarnings("unchecked")
	default void linkTo(C other) {
		this.link(other);
		((ILinkedComponent<C>)other).link((C)this);
	}

	/**
	 * Переопределяйте этот метод вместо {@link #getPreferredSize()}
	 * @return Предпочтительный размер компонента, без учёта связанного с ним компонента
	 */
	Dimension originalPreferredSize();

	default Dimension getPreferredSize() {
		var other = getLinked();

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
