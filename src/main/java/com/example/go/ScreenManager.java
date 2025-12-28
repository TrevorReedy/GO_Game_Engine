package com.example.go;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {
    private static JFrame frame;
    private static GridBagConstraints gbc;

     public static void initialize() {
        frame = new JFrame("Go Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null); // Centers window on screen
        
        // NEW: Proper centering constraints
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Balanced padding
        gbc.anchor = GridBagConstraints.CENTER; // Centers components
    }

    public static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(new GridBagLayout());
        
        MainMenuScreen menu = new MainMenuScreen();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(menu, gbc);
        
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true); // Ensure frame is visible
    }

    public static void showGameScreen(Board board) {
    frame.getContentPane().removeAll();
    frame.setLayout(new BorderLayout());
    
    // Create a panel to hold the board with proper padding
    JPanel boardPanel = new JPanel(new BorderLayout());
    boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    boardPanel.add(board, BorderLayout.CENTER);
    
    // Add the board panel to the center
    frame.add(boardPanel, BorderLayout.CENTER);
    
    // Create button panel
    JPanel buttonPanel = new JPanel();
    JButton passButton = new JButton("Pass");
    JButton resignButton = new JButton("Resign");
    JButton menuButton = new JButton("Main Menu");
    
    // ✅ ADD THESE:
    passButton.addActionListener(e -> board.pass());

    resignButton.addActionListener(e -> {
        board.resign();
        showMainMenu();
    });
    
    buttonPanel.add(passButton);
    buttonPanel.add(resignButton);
    buttonPanel.add(menuButton);
    
    // Add button panel to the bottom
    frame.add(buttonPanel, BorderLayout.SOUTH);
    
    frame.revalidate();
    frame.repaint();
}
    public static void showPopup(Exception ex) {
        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
    }
}