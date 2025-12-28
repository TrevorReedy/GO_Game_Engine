package com.example.go;
import com.example.go.Board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.awt.*;

import javax.imageio.ImageIO;

import com.example.go.Board;
import java.awt.Color;

public class Piece {
	
	public int col,row;
	public int xPos,yPos;
	public boolean isWhite;
	public Color color;
	public int value;
	public int liberties;
    public boolean checkedForCapture;
	

           Board board;
       	public Piece(Board board, int col, int row, Color color, boolean isWhite) {
    		this.board = board;
    		this.col = col;
    		this.row = row;
    		this.isWhite = isWhite;
    		this.color = color;    		
            this.xPos = col * board.getTileSize();
            this.yPos = row * board.getTileSize();

    	}

        public void paint(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        Point vertex = board.getVertexPosition(col, row);
        int radius = board.getStoneDiameter() / 2;
        
        // Draw with shadow effect
        g2d.setColor(color.darker());
        g2d.fillOval(vertex.x - radius + 1, vertex.y - radius + 1, 
                     board.getStoneDiameter(), board.getStoneDiameter());
        
        g2d.setColor(color);
        g2d.fillOval(vertex.x - radius, vertex.y - radius,
                     board.getStoneDiameter(), board.getStoneDiameter());
    }
           
 public void updateLiberties() {
        liberties = 0;

        
        for (int[] dir : board.directions) {
            int newCol = col + dir[0];
            int newRow = row + dir[1];
            if (board.isValidPosition(newCol, newRow) && 
                board.getPiece(newCol, newRow) == null) {
                liberties++;
            }
        }
    }
}
