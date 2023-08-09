package x590.chess.playingside;

import x590.chess.Main;

/**
 * Локальный игрок на текущем устройстве
 */
public class LocalPlayer implements PlayingSide {

	@Override
	public String getName() {
		return Main.getConfig().getName();
	}

	@Override
	public boolean canSelectField() {
		return true;
	}
}
