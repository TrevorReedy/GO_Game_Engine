package com.example.go;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainMenuScreen extends JPanel {
    public MainMenuScreen() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        
        
        JButton playButton = new JButton("Start Game");
        playButton.setFont(new Font("Arial", Font.PLAIN, 24));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.addActionListener(e -> ScreenManager.showGameScreen(new Board()));
        add(playButton, gbc);
    }
}