package x590.chess.gui;

import x590.chess.gui.board.FieldPanel;
import x590.util.IntegerUtil;
import x590.util.annotation.Nullable;
import x590.util.function.ObjIntFunction;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Объект, размеры которого могут быть изменены
 */
public class ResizeableObject<T> implements Supplier<T> {

	private static volatile long lastUpdateTimestamp = System.currentTimeMillis();

	private static final List<WeakReference<ResizeableObject<?>>> IMMEDIATELY_UPDATE_OBJECTS = new ArrayList<>();


	public static void updateSize() {
		lastUpdateTimestamp = System.currentTimeMillis();

		for (var iterator = IMMEDIATELY_UPDATE_OBJECTS.iterator(); iterator.hasNext(); ) {
			var object = iterator.next().get();

			if (object == null) {
				iterator.remove();
			} else {
				object.updateIfNecessary();
			}
		}
	}


	private volatile @Nullable T object;

	private final UnaryOperator<T> updater;

	private volatile long thisUpdateTimestamp;


	private ResizeableObject(T object, UnaryOperator<T> updater) {
		this.object = Objects.requireNonNull(object);
		this.updater = Objects.requireNonNull(updater);
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}.
	 * @param object объект, который будет храниться в {@link ResizeableObject}.
	 * @param updater функция, которая будет запускаться при обновлении.
	 *                Принимает старый объект, возвращает новый (возможно, тот же самый).
	 */
	public static <T> ResizeableObject<T> of(T object, UnaryOperator<T> updater) {
		return new ResizeableObject<>(object, updater);
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}.
	 * @param object объект, который будет храниться в {@link ResizeableObject}.
	 * @param updater функция, которая будет запускаться при обновлении.
	 *                Принимает старый объект и значение {@link FieldPanel#getPreferredSizeValue()},
	 *                возвращает новый (возможно, тот же самый).
	 */
	public static <T> ResizeableObject<T> of(T object, ObjIntFunction<T, T> updater) {
		return of(object,  obj -> updater.apply(obj, FieldPanel.getPreferredSizeValue()));
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}.
	 * @param object объект, который будет храниться в {@link ResizeableObject}.
	 * @param updater функция, которая будет запускаться при обновлении.
	 *                Принимает старый объект и изменяет его.
	 *                Ссылка на объект внутри {@link ResizeableObject} не меняется.
	 */
	public static <T> ResizeableObject<T> constant(T object, Consumer<T> updater) {
		return of(object, obj -> {
			updater.accept(obj);
			return obj;
		});
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}, который хранит объект {@link Dimension}.
	 * @param sizeOperator функция, которая принимает результат {@link FieldPanel#getPreferredSizeValue()}
	 *                     и возвращает высоту {@link Dimension}
	 */
	public static ResizeableObject<Dimension> newDimension(IntUnaryOperator sizeOperator) {
		return newDimension(sizeOperator, sizeOperator);
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}, который хранит объект {@link Dimension}.
	 * @param widthOperator функция, которая принимает результат {@link FieldPanel#getPreferredSizeValue()}
	 *                      и возвращает ширину {@link Dimension}
	 * @param heightOperator функция, которая принимает результат {@link FieldPanel#getPreferredSizeValue()}
	 *                       и возвращает высоту {@link Dimension}
	 */
	public static ResizeableObject<Dimension> newDimension(IntUnaryOperator widthOperator, IntUnaryOperator heightOperator) {
		return of(
				new Dimension(),
				dimension -> {
					var fieldPanelSize = FieldPanel.getPreferredSizeValue();
					dimension.width = widthOperator.applyAsInt(fieldPanelSize);
					dimension.height = heightOperator.applyAsInt(fieldPanelSize);
					return dimension;
				}
		);
	}

	/**
	 * @return Новый неинициализированный экземпляр {@link ResizeableObject}, который хранит объект {@link Integer}.
	 * @param operator функция, которая принимает результат {@link FieldPanel#getPreferredSizeValue()}
	 *                 и возвращает значение объекта
	 */
	public static ResizeableObject<Integer> newIntegerSize(IntUnaryOperator operator) {
		return of(
				IntegerUtil.ZERO,
				value -> operator.applyAsInt(FieldPanel.getPreferredSizeValue())
		);
	}

	/**
	 * Отмечает объект как инициализированный
	 */
	public ResizeableObject<T> initialized() {
		thisUpdateTimestamp = lastUpdateTimestamp;
		return this;
	}

	/**
	 * Заставляет объект обновляться сразу при вызове {@link ResizeableObject#updateSize()}
	 */
	public ResizeableObject<T> immediatelyUpdate() {
		IMMEDIATELY_UPDATE_OBJECTS.add(new WeakReference<>(this));
		return this;
	}

	/**
	 * Обновляет объект, если необходимо
	 */
	private synchronized void updateIfNecessary() {
		if (thisUpdateTimestamp < lastUpdateTimestamp) {
			object = updater.apply(object);
			thisUpdateTimestamp = lastUpdateTimestamp;
		}
	}

	/**
	 * Обновляет объект, если необходимо, и возвращает его
	 */
	@Override
	public synchronized T get() {
		updateIfNecessary();
		return object;
	}
}
