package com.example.go;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.lang.reflect.Field;

public class BoardTest {
    private Board board;
    
 
    /** Load an NxN board using rows of 'B', 'W', '.' (each row length must be N). */
    private void loadBoardN(String[] rows, int size, int capturedByBlack, int capturedByWhite) {
        assertEquals(size, rows.length, "Need " + size + " rows for a " + size + "x" + size + " board");
        for (int i = 0; i < size; i++) {
            assertEquals(size, rows[i].length(), "Each row must be " + size + " chars (row " + i + ")");
        }

        try {
            Field pieceArrayField = Board.class.getDeclaredField("pieceArray");
            pieceArrayField.setAccessible(true);

            Piece[][] arr = new Piece[size][size]; // [col][row]

            for (int row = 0; row < size; row++) {
                String line = rows[row];
                for (int col = 0; col < size; col++) {
                    char c = line.charAt(col);
                    if (c == 'B') {
                        arr[col][row] = new Piece(board, col, row, Color.BLACK, false);
                    } else if (c == 'W') {
                        arr[col][row] = new Piece(board, col, row, Color.WHITE, true);
                    } else if (c != '.') {
                        fail("Invalid char '" + c + "' at (" + col + "," + row + ")");
                    }
                }
            }

            pieceArrayField.set(board, arr);

            Field capB = Board.class.getDeclaredField("capturedByBlack");
            Field capW = Board.class.getDeclaredField("capturedByWhite");
            capB.setAccessible(true);
            capW.setAccessible(true);
            capB.setInt(board, capturedByBlack);
            capW.setInt(board, capturedByWhite);

        } catch (Exception e) {
            throw new RuntimeException("Reflection failed loading board", e);
        }
    }

    @BeforeEach
    void initBoard() {
        board = new Board(9, 9);
    }

    // ---------- Helpers ----------
    private void play(int col, int row) {
        assertDoesNotThrow(() -> board.makeMove(new Move(board, col, row)),
                "Move should be legal: (" + col + "," + row + ")");
    }

    private void passTwiceAndScore() {
        board.pass();
        board.pass();
//        board.calculateJapaneseScoring();
    }


    private void playPairs(int[]... moves) {
        for (int[] m : moves) {
            if (m == null || m.length != 2) fail("Each move must be int[]{col,row}");
            play(m[0], m[1]);
        }
    }

    // ---------- CAPTURE TESTS ----------
    @Test
    void testCaptureSingleWhiteStone() {
        // Build a simple capture around (2,1) on a 9x9 without weird edge effects
        playPairs(
                new int[]{1, 1}, // B
                new int[]{2, 1}, // W (target)
                new int[]{2, 0}, // B
                new int[]{0, 0}, // W filler
                new int[]{3, 1}, // B
                new int[]{0, 1}, // W filler
                new int[]{2, 2}  // B -> captures W at (2,1) by removing last liberty at (2,2)
        );

        assertNull(board.getPiece(2, 1), "White stone at (2,1) should be captured");
    }

    @Test
    void testCaptureGroupRemoval() {
        // White group of 2: (2,1) and (2,2). Black surrounds and captures.
        playPairs(
                new int[]{1, 1}, // B
                new int[]{2, 1}, // W
                new int[]{1, 2}, // B
                new int[]{2, 2}, // W  (group)
                new int[]{2, 0}, // B
                new int[]{0, 0}, // W filler
                new int[]{3, 1}, // B
                new int[]{0, 1}, // W filler
                new int[]{3, 2}, // B
                new int[]{0, 2}, // W filler
                new int[]{2, 3}  // B closes last liberty -> should capture the group
        );

        assertNull(board.getPiece(2, 1), "White stone at (2,1) should be captured");
        assertNull(board.getPiece(2, 2), "White stone at (2,2) should be captured");
    }

    // ---------- MOVE VALIDATION TESTS ----------
    @Test
    void testPlaceStoneOnEmptyIntersection() {
        play(0, 0);
        assertNotNull(board.getPiece(0, 0));
    }

    @Test
    void testCannotPlaceOnOccupied() {
        play(1, 1);
        assertThrows(Exception.class, () -> board.makeMove(new Move(board, 1, 1)));
    }

    @Test
    void testTurnsAlternateCorrectly() {
        assertTrue(board.isBlackTurn());
        play(0, 0);
        assertFalse(board.isBlackTurn());
        play(0, 1);
        assertTrue(board.isBlackTurn());
    }

    @Test
    void testInvalidOutOfBoundsMove() {
        // Your makeMove() currently indexes pieceArray BEFORE checking bounds,
        // so this will throw an ArrayIndexOutOfBoundsException (still an Exception).
        assertThrows(Exception.class, () -> board.makeMove(new Move(board, -1, 0)));
        assertThrows(Exception.class, () -> board.makeMove(new Move(board, 9, 9)));
    }

    // ---------- JAPANESE SCORING TESTS ----------
    @Test
    void testNoTerritoryOnUnsettledTinyPosition() {
        // This is not a settled endgame position; we only assert "no free territory"
        // in a tiny symmetric situation.
        playPairs(
                new int[]{4, 3}, // B
                new int[]{3, 4}, // W
                new int[]{4, 5}, // B
                new int[]{5, 4}  // W
        );

        passTwiceAndScore();

        assertEquals(0, board.getBlackScore(), "No enclosed territory should exist here");
        assertEquals(0, board.getWhiteScore(), "No enclosed territory should exist here");
    }

    @Test
    void testBlackGetsOnePointTerritoryCentered() {
        // Black encloses a single-point territory at (4,4) using a plus-shape:
        // Black stones at (4,3), (3,4), (5,4), (4,5)
        // Empty at (4,4) should be counted as black territory (1 point).
        playPairs(
                new int[]{4, 3}, // B
                new int[]{0, 0}, // W filler
                new int[]{3, 4}, // B
                new int[]{0, 1}, // W filler
                new int[]{5, 4}, // B
                new int[]{0, 2}, // W filler
                new int[]{4, 5}  // B completes enclosure
        );

        passTwiceAndScore();

        assertEquals(1, board.getBlackScore(), "Black should have 1 point of territory at (4,4)");
        assertEquals(0, board.getWhiteScore(), "White should have 0 points here");
    }

    @Test
    void testBlackGetsFourPointTerritoryBox() {
        // Black encloses a 2x2 empty box at:
        // (4,4) (5,4)
        // (4,5) (5,5)
        //
        // Ring stones around it:
        // Top:    (4,3) (5,3)
        // Bottom: (4,6) (5,6)
        // Left:   (3,4) (3,5)
        // Right:  (6,4) (6,5)
        //
        // Result: black territory = 4 points, no captures.
        playPairs(
                new int[]{4, 3}, new int[]{0, 0},
                new int[]{5, 3}, new int[]{0, 1},

                new int[]{3, 4}, new int[]{0, 2},
                new int[]{3, 5}, new int[]{0, 3},

                new int[]{6, 4}, new int[]{0, 4},
                new int[]{6, 5}, new int[]{0, 5},

                new int[]{4, 6}, new int[]{0, 6},
                new int[]{5, 6}, new int[]{0, 7}
        );

        passTwiceAndScore();

        assertEquals(4, board.getBlackScore(), "Black should have 4 points of enclosed territory");
        assertEquals(0, board.getWhiteScore(), "White should have 0 points here");
    }

    @Test
    void testCapturedStonesAreAddedToScoreInSettledPosition() {
        // Make a simple capture (1 prisoner) and ensure scoring includes it.
        // We'll avoid creating any territory: just capture and leave the board open.
        playPairs(
                new int[]{1, 1}, // B
                new int[]{2, 1}, // W target
                new int[]{2, 0}, // B
                new int[]{0, 0}, // W filler
                new int[]{3, 1}, // B
                new int[]{0, 1}, // W filler
                new int[]{2, 2}  // B captures (2,1)
        );

        passTwiceAndScore();

        // Your calculateJapaneseScoring uses:
        // blackScore = blackTerritory + capturedByBlack
        // so we expect at least 1 from the prisoner, and in this open shape territory should be 0.
        assertEquals(2, board.getBlackScore(), "Black should score exactly 2 (one captured white stone and one territory)");
        assertEquals(0, board.getWhiteScore(), "White should score 0 here");
    }
    
    @Test
    void endgame_blackHas4PointBoxTerritory_only() {
        loadBoardN(new String[]{
                "BBBBBBBBB",
                "BBBBBBBBB",
                "BBBBBBBBB",
                "BBBBBBBBB",
                "BBBB..BBB", // row4 col4-5 are empty
                "BBBB..BBB", // row5 col4-5 are empty  => 2x2 territory = 4
                "BBBBBBBBB",
                "BBBBBBBBB",
                "BBBBBBBBB"
        },9, 0, 0);

        board.calculateJapaneseScoring();

        assertEquals(4, board.getBlackScore(), "Black should get exactly 4 territory points");
        assertEquals(0, board.getWhiteScore(), "White should get 0");
    }


    // ------------------------------------------------------------
    // Scenario 2 (multiple regions, more "real game" feel):
    // Black territories: 1-point eye + 3-point pocket => 4 points total
    // White territories: 5-point lane + 2-point pocket => 7 points total
    // Prisoners: Black=3, White=9
    //
    // Expected:
    // Black score = 4 + 3 = 7
    // White score = 7 + 9 = 16
    // ------------------------------------------------------------
    @Test
    void endgame19_multipleTerritories_regionsCountBySize() {
        String[] rows = new String[]{
                "BBBBBBBBBBBBBBBBBBB",
                "BBBBBBBBWWBBBBBBBBB",
                "BBBBBBBBWWBBBBBBBBB",
                "BBB.BBBBBBBBBBBBBBB", // black 1-point eye at (3,3)
                "BBBBBBBBBBBBBBBBBBB",
                "BBBBBBBBBBBBBBBBBBB",
                "BBBBBBBBBBBBBB...BB", // black 3-point pocket at (14,6)(15,6)(16,6)
                "BBBBBBBBBBBBBBBBBBB",
                "BBBBBBBBBBBBBBBBBBB",
                "BBBBBBBBBBBBBBBBBBB",
                "WWWWWWWWWWWWWWWWWWW",
                "WWWWWWWWBBWWWWWWWWW",
                "WWWWWWWWBBWWWWWWWWW",
                "WWWWWWWWWWWWWWWWWWW",
                "WWWWWWW.....WWWWWWW", // white 5-point lane at cols 7-11
                "WWWWWWWWWWWWWWWWWWW",
                "WW..WWWWWWWWWWWWWWW", // white 2-point pocket at cols 2-3
                "WWWWWWWWWWWWWWWWWWW",
                "WWWWWWWWWWWWWWWWWWW"
        };

        loadBoardN(rows, 19, 3, 9);
        board.calculateJapaneseScoring();

        assertEquals(7, board.getBlackScore(), "Black = territory(4) + prisoners(3)");
        assertEquals(16, board.getWhiteScore(), "White = territory(7) + prisoners(9)");
    }
}