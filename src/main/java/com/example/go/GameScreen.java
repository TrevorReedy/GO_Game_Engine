package com.example.go;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class GameScreen extends JPanel {
    private final Board board;
    
    public GameScreen() {
        setLayout(new BorderLayout());
        
        // Create board with proper constraints
        board = new Board(19, 19);
        
        // Board container with proper filling
        JPanel boardContainer = new JPanel(new GridBagLayout());
        boardContainer.setBackground(new Color(220, 179, 92));
        boardContainer.add(board);
        
        // Scroll pane for large boards
        JScrollPane scrollPane = new JScrollPane(boardContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        add(scrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Proper component listener implementation
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                board.revalidate();
                board.repaint();
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {}
            
            @Override
            public void componentShown(ComponentEvent e) {}
            
            @Override
            public void componentHidden(ComponentEvent e) {}
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));

        // Pass Button
        JButton passButton = new JButton("Pass");
        passButton.setFont(new Font("Arial", Font.BOLD, 14));
        passButton.setPreferredSize(new Dimension(100, 40));
        passButton.addActionListener(e -> board.pass());

        // Resign Button
        JButton resignButton = new JButton("Resign");
        resignButton.setFont(new Font("Arial", Font.BOLD, 14));
        resignButton.setPreferredSize(new Dimension(100, 40));
        resignButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(
                GameScreen.this,
                "Are you sure you want to resign?",
                "Confirm Resignation",
                JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) {
                board.resign();
                ScreenManager.showMainMenu();
            }
        });

        // Main Menu Button
        JButton menuButton = new JButton("Main Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.setPreferredSize(new Dimension(120, 40));
        menuButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(
                GameScreen.this,
                "Return to main menu? Current game will be lost.",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) {
                ScreenManager.showMainMenu();
            }
        });

        // Score Button
        JButton scoreButton = new JButton("Score");
        scoreButton.setFont(new Font("Arial", Font.BOLD, 14));
        scoreButton.setPreferredSize(new Dimension(100, 40));
        scoreButton.addActionListener(e -> board.calculateJapaneseScoring());

        buttonPanel.add(passButton);
        buttonPanel.add(resignButton);
        buttonPanel.add(scoreButton);
        buttonPanel.add(menuButton);

        return buttonPanel;
    }
}