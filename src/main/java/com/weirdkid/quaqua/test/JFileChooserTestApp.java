package com.weirdkid.quaqua.test;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class JFileChooserTestApp {

	public static void main(String[] args) {
		// Set the custom look and feel (replace 'YourCustomLookAndFeel' with your
		// class)
		try {
			UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create the main frame
		JFrame frame = new JFrame("JFileChooser Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);

		// Create a panel with buttons
		JPanel panel = new JPanel();
		frame.add(panel);

		// Button to open a basic file chooser
		JButton openButton = new JButton("Open FileChooser");
		openButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(frame);
		});
		panel.add(openButton);

		// Button to open a save dialog
		JButton saveButton = new JButton("Save FileChooser");
		saveButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showSaveDialog(frame);
		});
		panel.add(saveButton);

		// Button to open a custom file chooser
		JButton customButton = new JButton("Custom FileChooser");
		customButton.addActionListener(e -> {
			JFileChooser customChooser = new JFileChooser();
			// Custom configurations for customChooser
			customChooser.showOpenDialog(frame);
		});
		panel.add(customButton);

		// Display the frame
		frame.setVisible(true);
	}
}
