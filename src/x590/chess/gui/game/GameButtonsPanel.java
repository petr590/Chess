package x590.chess.gui.game;

import x590.chess.gui.GuiUtil;
import x590.chess.gui.linked.LinkedPanel;
import x590.util.annotation.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Панель с кнопками "Сдаться" и "Предложить ничью" (только в сетевой игре).
 * При нажатии на кнопку "Предложить ничью" она блокируется на определённое
 * количество ходов, заданное при создании игры.
 * При нажатии на кнопку "Сдаться" игра спросит подтверждение
 */
public class GameButtonsPanel extends LinkedPanel {

	private static final int PADDING = 16;

	private final @Nullable JButton drawOfferButton;
	private final JButton giveUpButton;

	public GameButtonsPanel(GamePanel gamePanel, LinkedPanel other, boolean lockGameEndButtons) {
		super(other);

		setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

		JPanel content = new JPanel(new GridLayout(2, 1));

		if (gamePanel.isRemote()) {
			this.drawOfferButton = new JButton("Предложить ничью");
			drawOfferButton.addActionListener(event -> {
				gamePanel.offerADraw();
				drawOfferButton.setEnabled(false);
			});

			content.add(drawOfferButton);

		} else {
			this.drawOfferButton = null;
		}

		this.giveUpButton = new JButton("Сдаться");
		giveUpButton.addActionListener(event -> {
			if (GuiUtil.showYesNoOptionDialog("Вы уверены?", "")) {
				gamePanel.giveUp();
			}
		});

		content.add(giveUpButton);

		add(content);

		if (lockGameEndButtons) {
			lockGameEndButtons();
		}
	}

	/**
	 * Заблокирует кнопку "Предложить ничью"
	 */
	public void lockDrawOfferButton() {
		if (drawOfferButton != null)
			drawOfferButton.setEnabled(false);
	}

	/**
	 * Разблокирует кнопку "Предложить ничью"
	 */
	public void unlockDrawOfferButton() {
		if (drawOfferButton != null)
			drawOfferButton.setEnabled(true);
	}

	/**
	 * Заблокирует кнопки "Сдаться" и "Предложить ничью"
	 */
	public void lockGameEndButtons() {
		lockDrawOfferButton();
		giveUpButton.setEnabled(false);
	}

	/**
	 * Разблокирует кнопки "Сдаться" и "Предложить ничью"
	 */
	public void unlockGameEndButtons() {
		unlockDrawOfferButton();
		giveUpButton.setEnabled(true);
	}

	public void onGameEnd() {
		lockGameEndButtons();
	}
}
