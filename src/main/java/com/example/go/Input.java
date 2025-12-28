package com.example.go;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import com.example.go.exception.InvalidMoveException;
import com.example.go.Piece;





import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Input extends MouseAdapter {
    private Board board;

    public Input(Board board) {
        this.board = board;
    }

    @Override
public void mousePressed(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    // Calculate the closest intersection
    int col = Math.round(x / (float) board.getTileSize());
    int row = Math.round(y / (float) board.getTileSize());

    // Ensure we're within bounds
    col = Math.max(0, Math.min(board.getCols() - 1, col));
    row = Math.max(0, Math.min(board.getRows() - 1, row));

    // Check if the tile is not already occupied
    if (board.getPiece(col, row) ==null) {
        System.out.println(board.isBlackTurn() ? 
            "Black Stone placed at (" + col + ", " + row + ")" : 
            "White Stone placed at (" + col + ", " + row + ")");
        
        Move move = new Move(board, col, row);
        board.makeMove(move);
        board.repaint();
    } else {
        System.out.println("Tile already occupied.");
    }
}



// @Override
//     public void mousePressed(MouseEvent e) {
//         int x = e.getX();
//         int y = e.getY();
        
//         // Calculate grid position (0-18 for 19x19 board)
//         int col = (int)Math.round((float)x / board.getTileSize());
//         int row = (int)Math.round((float)y / board.getTileSize());
        
//         try {
//             // Validate position
//             if (col < 0 || col >= board.getCols() || row < 0 || row >= board.getRows()) {
//                 throw new InvalidPlacementException(col, row, "Outside board");
//             }
            
//             if (board.getTileStates(col, row)) {
//                 throw new InvalidPlacementException(col, row, "Position occupied");
//             }
            
//             // Place stone
//             Move move = new Move(board, col, row);
//             board.makeMove(move);
//             board.repaint();
            
//         } catch (InvalidPlacementException ex) {
//             ScreenManager.showPopup(ex);
//         }
//     }
}
