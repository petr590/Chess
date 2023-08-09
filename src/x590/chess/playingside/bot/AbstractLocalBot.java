package x590.chess.playingside.bot;

import x590.chess.figure.Figure;
import x590.chess.figure.Side;
import x590.chess.gui.ResizeableObject;
import x590.chess.gui.board.BoardPanel;
import x590.chess.gui.board.FieldPanel;
import x590.chess.io.ResourceLoader;
import x590.chess.playingside.PlayingSide;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Представляет бота
 */
public abstract class AbstractLocalBot implements PlayingSide {

	private BoardPanel boardPanel;

	@Override
	public String getName() {
		return "Бот";
	}

	@Override
	public void setup(BoardPanel boardPanel) {
		this.boardPanel = boardPanel;
	}


	private static final Image WAITING_IMAGE = ResourceLoader.loadTexture("waiting.gif");

	private static final ResizeableObject<ImageIcon> WAITING_IMAGE_ICON = ResizeableObject.constant(
			new ImageIcon(),
			icon -> {
				int size = FieldPanel.getPreferredSizeValue() / 2;
				icon.setImage(WAITING_IMAGE.getScaledInstance(size, size, Image.SCALE_SMOOTH));
			}
	);

	@Override
	public @Nullable Icon getStateIcon(BoardPanel boardPanel) {
		return WAITING_IMAGE_ICON.get();
	}


	private long startTime;

	/**
	 * Должен вызываться в начале вычисления хода.
	 * Записывает текущее время в {@link #startTime}.
	 */
	protected void recordStartTime() {
		startTime = System.currentTimeMillis();
	}

	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Должен вызываться в конце вычисления хода перед его осуществлением.
	 * Ждёт столько времени, чтобы после ожидания от {@link #startTime} прошло
	 * ровно такое время, которое вернёт {@link #getTimeout()}, затем выполняет
	 * переданный callback в фоновом потоке.
	 * Если {@link #startTime} равен {@code 0}, то ждёт ровно {@link #getTimeout()}
	 */
	protected void waitAndThen(Runnable command) {
		long startTime = this.startTime;
		this.startTime = 0;

		long delay = getTimeout() - (startTime == 0 ?
				0 :
				System.currentTimeMillis() - startTime
		);

		service.schedule(command, delay, TimeUnit.MILLISECONDS);
	}


	private static final int DEFAULT_TIMEOUT = 500;

	/**
	 * @return Время ожидания в миллисекундах. По умолчанию {@value #DEFAULT_TIMEOUT}
	 */
	protected int getTimeout() {
		return DEFAULT_TIMEOUT;
	}


	public BoardPanel getBoardPanel() {
		return boardPanel;
	}

	@Override
	public boolean canSelectField() {
		return false;
	}

	@Override
	public boolean canCancelMove() {
		return true;
	}

	@Override
	public abstract Figure queryPawnTurningFigure(Side side);
}
