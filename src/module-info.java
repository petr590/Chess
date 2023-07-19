module chess.main {
	requires java.desktop;
	requires x590.util;
	requires it.unimi.dsi.fastutil;
	requires java.sql; // без этой зависимости gson не работает
	requires gson;

	opens x590.chess.config to gson;
}
