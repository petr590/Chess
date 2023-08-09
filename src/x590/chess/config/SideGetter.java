package x590.chess.config;

import x590.chess.LowercaseEnumJsonSerializable;
import x590.chess.figure.Side;

import java.util.function.Supplier;

public enum SideGetter implements LowercaseEnumJsonSerializable {

	WHITE(() -> Side.WHITE),
	BLACK(() -> Side.BLACK),
	RANDOM(Side::randomSide);

	private final Supplier<Side> getter;

	SideGetter(Supplier<Side> getter) {
		this.getter = getter;
	}

	public Side getSide() {
		return getter.get();
	}
}
