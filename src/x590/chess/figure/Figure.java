package x590.chess.figure;

import x590.chess.figure.step.IStep;
import x590.chess.board.ChessBoard;
import x590.chess.figure.behaviour.FigureBehaviour;
import x590.chess.figure.behaviour.FigureBehaviours;
import x590.chess.gui.board.FieldPanel;
import x590.util.annotation.Immutable;
import x590.util.annotation.RemoveIfNotUsed;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.List;

public enum Figure {
	WHITE_KING   (Side.WHITE, FigureType.KING,   "♔", FigureBehaviours.KING),
	WHITE_QUEEN  (Side.WHITE, FigureType.QUEEN,  "♕", FigureBehaviours.QUEEN),
	WHITE_ROOK   (Side.WHITE, FigureType.ROOK,   "♖", FigureBehaviours.ROOK),
	WHITE_BISHOP (Side.WHITE, FigureType.BISHOP, "♗", FigureBehaviours.BISHOP),
	WHITE_KNIGHT (Side.WHITE, FigureType.KNIGHT, "♘", FigureBehaviours.KNIGHT),
	WHITE_PAWN   (Side.WHITE, FigureType.PAWN,   "♙", FigureBehaviours.PAWN),

	BLACK_KING   (Side.BLACK, FigureType.KING,   "♚", FigureBehaviours.KING),
	BLACK_QUEEN  (Side.BLACK, FigureType.QUEEN,  "♛", FigureBehaviours.QUEEN),
	BLACK_ROOK   (Side.BLACK, FigureType.ROOK,   "♜", FigureBehaviours.ROOK),
	BLACK_BISHOP (Side.BLACK, FigureType.BISHOP, "♝", FigureBehaviours.BISHOP),
	BLACK_KNIGHT (Side.BLACK, FigureType.KNIGHT, "♞", FigureBehaviours.KNIGHT),
	BLACK_PAWN   (Side.BLACK, FigureType.PAWN,   "♟", FigureBehaviours.PAWN);


	private static final Figure[] VALUES = values();

	private static final @Immutable Map<Side, Map<FigureType, Figure>> FIGURES =
			Map.of(
					Side.WHITE, new EnumMap<>(FigureType.class),
					Side.BLACK, new EnumMap<>(FigureType.class)
			);

	static {
		for (Figure figure : VALUES) {
			FIGURES.get(figure.side).put(figure.type, figure);
		}
	}

	private final Side side;
	private final FigureType type;

	private final String emoji;

	private final Image originalImage;
	private Image image;
	private final ImageIcon icon, miniIcon;

	private final FigureBehaviour behaviour;

	Figure(Side side, FigureType type, String emoji, FigureBehaviour behaviour) {
		this.side = side;
		this.type = type;
		this.emoji = emoji;

		String path = "textures/" + side.getDirectory() + '/' + type.getFileName();
		
		try(var in = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
			this.image = this.originalImage = ImageIO.read(in != null ? in : new FileInputStream(path));

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		this.icon = new ImageIcon(image);
		this.miniIcon = new ImageIcon(image.getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2, Image.SCALE_SMOOTH));

		this.behaviour = behaviour;
	}

	public static Figure valueOf(Side side, FigureType type) {
		return FIGURES.get(side).get(type);
	}

	public static final @Immutable List<Figure> WHITE_PAWN_TURNING_FIGURES, BLACK_PAWN_TURNING_FIGURES;
	public static final @Immutable List<Icon> WHITE_PAWN_TURNING_ICONS, BLACK_PAWN_TURNING_ICONS;


	static {
		int size = FigureType.PAWN_TURNING_TYPES.size();

		List<Figure> whitePawnTurningFigures = new ArrayList<>(size);
		List<Figure> blackPawnTurningFigures = new ArrayList<>(size);
		List<Icon> whitePawnTurningIcons = new ArrayList<>(size);
		List<Icon> blackPawnTurningIcons = new ArrayList<>(size);

		for (FigureType type : FigureType.PAWN_TURNING_TYPES) {
			Figure whiteFigure = valueOf(Side.WHITE, type);
			Figure blackFigure = valueOf(Side.BLACK, type);

			whitePawnTurningFigures.add(whiteFigure);
			blackPawnTurningFigures.add(blackFigure);
			whitePawnTurningIcons.add(whiteFigure.icon);
			blackPawnTurningIcons.add(blackFigure.icon);
		}

		WHITE_PAWN_TURNING_FIGURES = Collections.unmodifiableList(whitePawnTurningFigures);
		BLACK_PAWN_TURNING_FIGURES = Collections.unmodifiableList(blackPawnTurningFigures);
		WHITE_PAWN_TURNING_ICONS = Collections.unmodifiableList(whitePawnTurningIcons);
		BLACK_PAWN_TURNING_ICONS = Collections.unmodifiableList(blackPawnTurningIcons);

		updateSize();
	}


	public static void updateSize() {
		int preferredSize = FieldPanel.getPreferredSizeValue();

		for (Figure figure : VALUES) {
			var image = figure.image;

			if (preferredSize != image.getWidth(null) || preferredSize != image.getHeight(null)) {
				figure.image = figure.originalImage.getScaledInstance(preferredSize, preferredSize, Image.SCALE_SMOOTH);
				figure.icon.setImage(figure.image);
				figure.miniIcon.setImage(figure.image.getScaledInstance(preferredSize / 2, preferredSize / 2, Image.SCALE_SMOOTH));
			}
		}
	}

	public static @Immutable List<Figure> getPawnTurningFigures(Side side) {
		return side.choose(WHITE_PAWN_TURNING_FIGURES, BLACK_PAWN_TURNING_FIGURES);
	}

	public static @Immutable List<Icon> getPawnTurningIcons(Side side) {
		return side.choose(WHITE_PAWN_TURNING_ICONS, BLACK_PAWN_TURNING_ICONS);
	}


	public Side getSide() {
		return side;
	}

	public FigureType getType() {
		return type;
	}

	public int getWorth() {
		return type.getWorth();
	}

	public String getEmoji() {
		return emoji;
	}

	public Image getImage() {
		return image;
	}

	@RemoveIfNotUsed
	public ImageIcon getIcon() {
		return icon;
	}

	public ImageIcon getMiniIcon() {
		return miniIcon;
	}

	public Collection<? extends IStep> getPossibleSteps(ChessBoard board, Pos current) {
		return behaviour.getPossibleSteps(board, side, current);
	}

	public Collection<Pos> getControlledFields(ChessBoard board, Pos current) {
		return behaviour.getControlledFields(board, side, current);
	}

	public boolean canBeTook() {
		return type != FigureType.KING;
	}

	public boolean canBeTookBy(Side side) {
		return this.side != side && canBeTook();
	}
}
