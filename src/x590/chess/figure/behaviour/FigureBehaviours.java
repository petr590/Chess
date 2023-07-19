package x590.chess.figure.behaviour;

import x590.chess.figure.Direction;

public class FigureBehaviours {

	public static final FigureBehaviour
			KING = new KingBehaviour(),
			QUEEN = new VectorFigureBehaviour(Direction.VERTICAL_HORIZONTAL_AND_DIAGONAL),
			ROOK = new VectorFigureBehaviour(Direction.VERTICAL_AND_HORIZONTAL),
			BISHOP = new VectorFigureBehaviour(Direction.DIAGONAL),
			KNIGHT = new KnightBehaviour(),
			PAWN = new PawnBehaviour();
}
