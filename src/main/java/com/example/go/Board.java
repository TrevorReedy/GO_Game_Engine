package com.example.go;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Queue;
import com.example.go.Piece;
import com.example.go.exception.InvalidMoveException;



public class Board extends JPanel {
    // Game state
    // private boolean[][] tileStates;
    private Piece[][] pieceArray;
    private boolean isBlackTurn;
    private int whiteScore;
    private int blackScore;
    private int clickCount;
    private int passCount;
    private int capturedByBlack = 0;
    private int capturedByWhite = 0;
    
    // Display
    private int tileSize;
    private int stoneSize; // Visual size of stones
    private int stoneDiameter;
    private final int cols;
    private final int rows;
    private static final int minTileSize = 20;
    private static final int maxTileSize = 100;
    private static final int STONE_RATIO = 80; // Percentage of tile size

    public int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Board() {
        this(20, 20);
    }

public Board(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;
    
    this.isBlackTurn = true;
    // Initialize arrays FIRST
    // this.tileStates = new boolean[cols][rows];
    this.pieceArray = new Piece[cols][rows];
    
    // Initialize game state
    resetGameState();
    
    // UI setup
    this.setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
    this.setBackground(new Color(245, 222, 178));
    addMouseListener(new Input(this));
}

private void calculateSizes() {
    // Get available drawing area
    Dimension size = getParent() != null ? getParent().getSize() : 
                    new Dimension(800, 800); // Default fallback

    // Calculate based on grid lines (cols-1 spaces between cols lines)
    int widthTile = size.width / Math.max(1, cols-1);
    int heightTile = size.height / Math.max(1, rows-1);
    
    // Constrain between min/max and keep aspect ratio
    this.tileSize = Math.max(minTileSize, 
                           Math.min(maxTileSize, 
                                   Math.min(widthTile, heightTile)));
    
    // Stone size proportional to tile size (90% of tile size looks good)
    this.stoneSize = (int)(tileSize * 0.9);
    
    // Set exact preferred size (cols-1 spaces × rows-1 spaces)
    setPreferredSize(new Dimension(
        (cols-1) * tileSize, 
        (rows-1) * tileSize
    ));
}

    public  void resetGameState() {
        this.isBlackTurn = true;
        this.whiteScore = 0;
        this.blackScore = 0;

        capturedByBlack = 0;
        capturedByWhite = 0;

        this.clickCount = 0;
        
        this.passCount = 0;
        for (int i = 0; i < cols; i++) {
            // Arrays.fill(tileStates[i], false);
            Arrays.fill(pieceArray[i], null);
        }
        

        

        
    }

@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculateSizes();
        drawGrid((Graphics2D)g);
        drawStones((Graphics2D)g);
    }


    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Draw vertical lines
        for (int col = 0; col < cols; col++) {
            int x = col * tileSize;
            g2d.drawLine(x, 0, x, (rows-1) * tileSize);
        }
        
        // Draw horizontal lines
        for (int row = 0; row < rows; row++) {
            int y = row * tileSize;
            g2d.drawLine(0, y, (cols-1) * tileSize, y);
        }
        
        // Draw star points for standard boards
        if (cols == 19 && rows == 19) {
            drawStarPoints(g2d, new int[]{3, 9, 15});
        }
    }

        private void drawStarPoints(Graphics2D g2d, int[] points) {
        g2d.setColor(Color.BLACK);
        int starSize = tileSize/5;
        for (int col : points) {
            for (int row : points) {
                int x = col * tileSize - starSize/2;
                int y = row * tileSize - starSize/2;
                g2d.fillOval(x, y, starSize, starSize);
            }
        }
    }

        public Point getVertexPosition(int col, int row) {
        return new Point(col * tileSize, row * tileSize);
    }
    
    public int getStoneDiameter() {
        return stoneDiameter;
    }

private void drawStones(Graphics2D g2d) {
    for (int col = 0; col < cols; col++) {
        for (int row = 0; row < rows; row++) {
            if (pieceArray[col][row] != null) {
                // Calculate stone position centered on intersection
                int x = col * tileSize - stoneSize/2;
                int y = row * tileSize - stoneSize/2;
                
                g2d.setColor(pieceArray[col][row].color);
                g2d.fillOval(x, y, stoneSize, stoneSize);
                
                // Draw stone border for better visibility
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x, y, stoneSize, stoneSize);
            }
        }
    }
}

    // ... (keep all other existing methods exactly as they were)
    // Only changed the constructor, resetGameState(), drawGrid(), and drawStones()

    
    private int getCapturedStones(boolean forBlack) {
        int capturedStones = 0;
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                if ( pieceArray[col][row] != null) {
                    if (pieceArray[col][row].isWhite == forBlack) {
                        capturedStones++;
                    }
                }
            }
        }
        return capturedStones;
    }


    private TerritoryResult determineTerritoryOwner(int col, int row, boolean[][] visited) {
        if (!isValidPosition(col, row) || pieceArray[col][row] != null || visited[col][row]) return null;

        Set<Color> borderingColors = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{col, row});
        visited[col][row] = true;

        boolean touchesEdge = false;
        int regionSize = 0;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            regionSize++;

            for (int[] dir : directions) {
                int nc = cur[0] + dir[0];
                int nr = cur[1] + dir[1];

                if (!isValidPosition(nc, nr)) {
                    // CRITICAL: don't return early — finish flood fill so visited[][] is consistent
                    touchesEdge = true;
                    continue;
                }

                Piece neighbor = pieceArray[nc][nr];
                if (neighbor != null) {
                    borderingColors.add(neighbor.color);
                } else if (!visited[nc][nr]) {
                    visited[nc][nr] = true;
                    queue.add(new int[]{nc, nr});
                }
            }
        }

        // Decide after exploring the entire empty region
        if (touchesEdge) return null;
        if (borderingColors.size() != 1) return null;

        return new TerritoryResult(borderingColors.iterator().next(), regionSize);
    }

 



    private static class TerritoryResult {
        final Color owner;
        final int size;

        TerritoryResult(Color owner, int size) {
            this.owner = owner;
            this.size = size;
        }
    }
private void updateNeighborLiberties(int col, int row) {
    // Check all 4 neighbors
    for (int[] dir : directions) {
        int newCol = col + dir[0];
        int newRow = row + dir[1];
        if (isValidPosition(newCol, newRow)) {
            Piece neighbor = pieceArray[newCol][newRow];
            if (neighbor != null) {
                // Check entire group's liberties, not just this stone
                if (hasNoLiberties(neighbor)) {
                    removeGroup(neighbor);
                }
            }
        }
    }
}

 
    public void pass() {
    	System.out.print("Clicked Pass");
        passCount++;
        System.out.println("pass count: " + passCount);
        if (passCount == 2) {
            calculateJapaneseScoring();
            System.out.println("end of game");
        }
        incrementClicks();
        System.out.printf("Black Score: %d White Score %d\n", blackScore, whiteScore);
    }
    
    public void resign() {
    	System.out.print("Clicked Resign");
        calculateJapaneseScoring();
        System.out.println("end of game");
        System.out.printf("Black Score: %d White Score %d\n", blackScore, whiteScore);

    }
    
    
    public boolean wouldBeSuicide(int col, int row, boolean placingBlack) {
        if (getPiece(col, row) != null) return true; // occupied is "illegal", treat as suicide-ish

        Color myColor = placingBlack ? Color.BLACK : Color.WHITE;
        boolean iAmWhite = !placingBlack;

        Piece placed = new Piece(this, col, row, myColor, iAmWhite);
        pieceArray[col][row] = placed;

        java.util.List<Piece> removed = new java.util.ArrayList<>();

        for (Piece opp : getOpponentNeighbors(placed)) {
            if (hasNoLiberties(opp)) {
                java.util.Set<Piece> group = new java.util.HashSet<>();
                findGroup(opp, group);
                for (Piece p : group) {
                    pieceArray[p.col][p.row] = null;
                    removed.add(p);
                }
            }
        }

        // 3) Now check if the placed stone's group has liberties
        boolean suicide = hasNoLiberties(placed);

        // 4) Restore board (undo simulation)
        pieceArray[col][row] = null;
        for (Piece p : removed) {
            pieceArray[p.col][p.row] = p;
        }

        return suicide;
    }


public void makeMove(Move move) throws InvalidMoveException {
    // Validate move first
    if (getPiece(move.newCol, move.newRow)!=null) {
        throw new InvalidMoveException("Intersection already occupied");
    }
    
    if (wouldBeSuicide(move.newCol, move.newRow, isBlackTurn)) {
        throw new InvalidMoveException("Suicide move not allowed");
    }
    
//    // Check ko rule (optional)
//    if (isKoViolation(move.newCol, move.newRow)) {
//        throw new InvalidMoveException("Ko rule violation");
//    }

    // Execute valid move
    Color pieceColor = isBlackTurn ? Color.BLACK : Color.WHITE;
    Piece newPiece = new Piece(this, move.newCol, move.newRow, pieceColor, !isBlackTurn);
    
    pieceArray[move.newCol][move.newRow] = newPiece;
    // tileStates[move.newCol][move.newRow] = true;

    newPiece.updateLiberties();
    captureOpponentStones(newPiece);
    
    // // Handle potential suicide after capture
    // if (hasNoLiberties(newPiece) && !capturedOpponentPieces) {
    //     removeGroup(newPiece);
    //     throw new InvalidMoveException("Suicide move occurred");
    // }

    updateNeighborLiberties(move.newCol, move.newRow);
    isBlackTurn = !isBlackTurn;
    clickCount++;
    repaint();
}

    public void captureOpponentStones(Piece placedPiece) {
        getOpponentNeighbors(placedPiece).stream()
            .filter(opponent -> !opponent.checkedForCapture) // Prevent re-processing
            .filter(this::hasNoLiberties)
            .forEach(this::removeGroup);
    }
    
    private boolean hasNoLiberties(Piece piece) {
        Set<Piece> group = new HashSet<>();
        findGroup(piece, group);
        return group.stream().noneMatch(this::hasEmptyAdjacent);
    }

    private void findGroup(Piece piece, Set<Piece> group) {
        if (group.contains(piece)) return;
        group.add(piece);
        getSameColorNeighbors(piece).forEach(neighbor -> findGroup(neighbor, group));
    }


    // private boolean hasNoLibertiesRecursive(Piece piece, Set<Piece> group) {
    //     if (group.contains(piece)) {
    //         return true;
    //     }
    //     if (hasEmptyAdjacent(piece)) {
    //         return false;
    //     }
    //     group.add(piece);
        


    //     ArrayList<Piece> sameColorNeighbors = getSameColorNeighbors(piece);
    //     for (Piece neighbor : sameColorNeighbors) {
    //         if (!hasNoLibertiesRecursive(neighbor, group)) {
    //             return false;
    //         }
    //     }

    //     return true;
    // }
    
    private boolean hasEmptyAdjacent(Piece piece) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newCol = piece.col + dir[0];
            int newRow = piece.row + dir[1];
            if (isValidPosition(newCol, newRow) && pieceArray[newCol][newRow] == null) {
                return true;
            }
        }
        return false;
    }

    private void removeGroup(Piece piece) {
        Set<Piece> group = new HashSet<>();
        findGroup(piece, group);
        group.forEach(p -> p.checkedForCapture = true); // Mark as processed

        System.out.println("Capturing group of size " + group.size() + " at (" + 
                       piece.col + "," + piece.row + ")");

        for (Piece p : group) {
            pieceArray[p.col][p.row] = null;
            if (p.isWhite) {
                capturedByBlack++;
            } else {
                capturedByWhite++;
            }
}
        group.forEach(p -> updateNeighborLiberties(p.col, p.row));
    }

    private void removeGroupRecursive(Piece piece, Set<Piece> group) {
        if (group.contains(piece)) {
            return;
        }
        group.add(piece);

        ArrayList<Piece> sameColorNeighbors = getSameColorNeighbors(piece);
        for (Piece neighbor : sameColorNeighbors) {
            removeGroupRecursive(neighbor, group);
        }
    }
    
    private void updateLiberties() {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (pieceArray[i][j] != null) {
                    pieceArray[i][j].liberties = calculateLiberties(pieceArray[i][j]);
                }
            }
        }
    }
    
    private int calculateLiberties(Piece piece) {
        int libertyCount = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newCol = piece.col + dir[0];
            int newRow = piece.row + dir[1];
            if (isValidPosition(newCol, newRow) && pieceArray[newCol][newRow] == null) {
                libertyCount++;
            }
        }
        return libertyCount;
    }
    
    public boolean isValidPosition(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }
    
    // Fixed getOpponentNeighbors method
    public ArrayList<Piece> getOpponentNeighbors(Piece piece) {
        ArrayList<Piece> opponents = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newCol = piece.col + dir[0];
            int newRow = piece.row + dir[1];
            
            // Changed bounds check to use cols/rows instead of cols+1/rows+1
            if (newCol >= 0 && newCol < cols && 
                newRow >= 0 && newRow < rows && 
                pieceArray[newCol][newRow] != null &&
                pieceArray[newCol][newRow].isWhite != piece.isWhite) {
                
                opponents.add(pieceArray[newCol][newRow]);
            }
        }
        return opponents;
    }

    public ArrayList<Piece> getSameColorNeighbors(Piece piece) {
        ArrayList<Piece> sameColor = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newCol = piece.col + dir[0];
            int newRow = piece.row + dir[1];
            if (isValidPosition(newCol, newRow) && pieceArray[newCol][newRow] != null &&
                pieceArray[newCol][newRow].isWhite == piece.isWhite) {
                sameColor.add(pieceArray[newCol][newRow]);
            }
        }
        return sameColor;
    }

    // @Override
    // protected void paintComponent(Graphics g) {
    //     super.paintComponent(g);
    //     Graphics2D g2d = (Graphics2D) g;
    //     g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //     drawGrid(g2d);
    //     drawStones(g2d);
    // }

    

// private void drawGrid(Graphics2D g2d) {
//     g2d.setColor(Color.BLACK);
//     g2d.setStroke(new BasicStroke(2f));

//     // Vertical lines
//     for (int col = 0; col < cols; col++) {
//         int x = col * tileSize;
//         g2d.drawLine(x, 0, x, (rows-1) * tileSize);
//     }

//     // Horizontal lines
//     for (int row = 0; row < rows; row++) {
//         int y = row * tileSize;
//         g2d.drawLine(0, y, (cols-1) * tileSize, y);
//     }
// }

    public boolean isWhiteTurn() {
        return (getTotalMoves() % 2 == 0);
    }

    public int getTotalMoves() {
        int count = 0;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (pieceArray[i][j] != null) {
                    count++;
                }
            }
        }
        return count;
    }


private Color emptySurroundingColor(ArrayList<int[]> group) {
        Set<Color> surroundingColors = new HashSet<>();
        
        for (int[] coords : group) {
            int col = coords[0];
            int row = coords[1];
            
            checkAdjacentColor(col-1, row, surroundingColors); // left
            checkAdjacentColor(col+1, row, surroundingColors); // right
            checkAdjacentColor(col, row-1, surroundingColors); // up
            checkAdjacentColor(col, row+1, surroundingColors); // down
        }
        
        return surroundingColors.size() == 1 ? surroundingColors.iterator().next() : null;
    }

    private void checkAdjacentColor(int col, int row, Set<Color> surroundingColors) {
        if (isValidPosition(col, row) && pieceArray[col][row] != null) {
            surroundingColors.add(pieceArray[col][row].color);
        }
    }

// private boolean isValidPosition(int col, int row) {
//     return col >= 0 && col < cols && row >= 0 && row < rows;
// }


public void incrementClicks() {
    this.clickCount++;
    isBlackTurn = !isBlackTurn;
}




// public void calculateJapaneseScoring() {
//     int blackTerritory = 0;
//     int whiteTerritory = 0;
//     boolean[][] visited = new boolean[cols][rows];
    
//     for (int col = 0; col < cols; col++) {
//         for (int row = 0; row < rows; row++) {
//             Color owner = determineTerritoryOwner(col, row, visited);
//             if (owner == Color.BLACK) blackTerritory++;
//             else if (owner == Color.WHITE) whiteTerritory++;
//         }
//     }
    
//     // Prisoners were already counted during removeGroup()
//     this.blackScore = blackTerritory + this.getCapturedStones(true);
//     this.whiteScore = whiteTerritory + this.getCapturedStones(false);
// }

public void calculateJapaneseScoring() {
    boolean[][] visited = new boolean[cols][rows];
    int blackTerritory = 0;
    int whiteTerritory = 0;

    // reset final scores
    this.blackScore = 0;
    this.whiteScore = 0;

    for (int col = 0; col < cols; col++) {
        for (int row = 0; row < rows; row++) {
            TerritoryResult result = determineTerritoryOwner(col, row, visited);
            if (result != null) {
                if (result.owner == Color.BLACK) blackTerritory += result.size;
                else if (result.owner == Color.WHITE) whiteTerritory += result.size;
            }
        }
    }

    // prisoners + territory
    this.blackScore = blackTerritory + capturedByBlack;
    this.whiteScore = whiteTerritory + capturedByWhite;
}

public int getTileSize() {
    return tileSize;
}


public int getBlackScore() {
    return blackScore;
}

public int getWhiteScore() {
    return whiteScore;
}

public int getClickCount() {
    return clickCount;
}

public int getPassCount() {
    return passCount;
}

public void setPassCount(int passCount) {
    this.passCount = passCount;
}


public Piece getPiece(int col, int row) {
    return pieceArray[col][row];
}
public boolean isBlackTurn(){
    return isBlackTurn;
}

public int getCols() {
    return cols;
}

public int getRows() {
    return rows;
}

}