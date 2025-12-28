//move.java
package com.example.go;

import com.example.go.Piece;

public class Move {

	int newCol;
	int newRow;
	
	Piece piece;
//	piece capture;
	public Move(Board board , int newCol, int newRow) {
		this.newCol = newCol;
		this.newRow = newRow;
		board.setPassCount(0);
//		this.capture = board.getPiece(newCol, newRow);
	}
}