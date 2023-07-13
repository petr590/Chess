package x590.chess.gui;

import x590.chess.board.ChessBoard;
import x590.chess.figure.Side;
import x590.chess.figure.move.IMove;
import x590.chess.gui.board.BoardPanel;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

	private final BoardPanel boardPanel;

	private final TakenFiguresPanel whitePanel, blackPanel;

	private final MovesPanel movesPanel = new MovesPanel();

	public GamePanel(ChessBoard board, Side side) {
		super(new GridBagLayout());
		setAlignmentY(CENTER_ALIGNMENT);

		this.boardPanel = new BoardPanel(board, side, this);

		this.whitePanel = new TakenFiguresPanel(board, Side.WHITE, side == Side.WHITE);
		this.blackPanel = new TakenFiguresPanel(board, Side.BLACK, side == Side.BLACK);

		var constraints = new GridBagConstraints();

		add(side.choose(whitePanel, blackPanel), GuiUtil.constraintsWithCoords(constraints, 0, 0));
		add(boardPanel,                          GuiUtil.constraintsWithCoords(constraints, 0, 1));
		add(side.choose(blackPanel, whitePanel), GuiUtil.constraintsWithCoords(constraints, 0, 2));
		add(movesPanel, GuiUtil.constraintsWithCoords(constraints, 1, 0,  0, 2));
	}

	public void makeMove(IMove move) {
		var chessBoard = boardPanel.getChessBoard();

		if (chessBoard.takenFiguresListChanged()) {
			chessBoard.currentSide().choose(whitePanel, blackPanel).updateFiguresList();
		}

		movesPanel.addMove(move);
	}

//	@Override
//	public Dimension getPreferredSize() {
//		var boardSize = boardPanel.getPreferredSize();
//		var whitePanelSize = whitePanel.getPreferredSize();
//		var blackPanelSize = blackPanel.getPreferredSize();
//
//		return new Dimension(
//				Math.max(boardSize.width, Math.max(whitePanelSize.width, blackPanelSize.width)),
//				boardSize.height + whitePanelSize.height + blackPanelSize.height
//		);
//	}
}
