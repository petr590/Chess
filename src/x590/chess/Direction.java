package x590.chess;

import x590.util.annotation.Immutable;

import java.util.Set;

public enum Direction {
	UP         (Dir.NONE, Dir.UP),
	UP_RIGHT   (Dir.RIGHT, Dir.UP),
	RIGHT      (Dir.RIGHT, Dir.NONE),
	DOWN_RIGHT (Dir.RIGHT, Dir.DOWN),
	DOWN       (Dir.NONE, Dir.DOWN),
	DOWN_LEFT  (Dir.LEFT, Dir.DOWN),
	LEFT       (Dir.LEFT, Dir.NONE),
	UP_LEFT    (Dir.LEFT, Dir.UP),

	KNIGHT_UP_LEFT    (Dir.LEFT, Dir.UP2),
	KNIGHT_UP_RIGHT   (Dir.RIGHT, Dir.UP2),
	KNIGHT_RIGHT_UP   (Dir.RIGHT2, Dir.UP),
	KNIGHT_RIGHT_DOWN (Dir.RIGHT2, Dir.DOWN),
	KNIGHT_DOWN_RIGHT (Dir.RIGHT, Dir.DOWN2),
	KNIGHT_DOWN_LEFT  (Dir.LEFT,  Dir.DOWN2),
	KNIGHT_LEFT_UP    (Dir.LEFT2, Dir.UP),
	KNIGHT_LEFT_DOWN  (Dir.LEFT2, Dir.DOWN);

	private static class Dir {
		private static final int
				NONE   =  0,
				UP     =  1,
				DOWN   = -1,
				LEFT   = -1,
				RIGHT  =  1,
				UP2    =  2,
				DOWN2  = -2,
				LEFT2  = -2,
				RIGHT2 =  2;
	}

	public static final @Immutable Set<Direction>
			VERTICAL_AND_HORIZONTAL = Set.of(UP, LEFT, DOWN, RIGHT),
			DIAGONAL = Set.of(UP_RIGHT, DOWN_RIGHT, DOWN_LEFT, UP_LEFT),
			VERTICAL_HORIZONTAL_AND_DIAGONAL = Set.of(UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT),
			KNIGHT = Set.of(KNIGHT_UP_LEFT, KNIGHT_UP_RIGHT, KNIGHT_RIGHT_UP, KNIGHT_RIGHT_DOWN, KNIGHT_DOWN_RIGHT, KNIGHT_DOWN_LEFT, KNIGHT_LEFT_UP, KNIGHT_LEFT_DOWN);

	private final int dx, dy;

	Direction(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	public int getXOffset() {
		return dx;
	}

	public int getYOffset() {
		return dy;
	}
}
