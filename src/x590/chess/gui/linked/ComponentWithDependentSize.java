package x590.chess.gui.linked;

import java.awt.*;

/**
 * Компонент, размеры которого зависят от других компонентов
 */
public interface ComponentWithDependentSize {

	/**
	 * Должен быть переопределён для вычисления изначального размера компонента
	 */
	Dimension originalPreferredSize();

	/**
	 * Должен быть переопределён для вычисления фактического размера компонента
	 */
	Dimension getPreferredSize();
}
