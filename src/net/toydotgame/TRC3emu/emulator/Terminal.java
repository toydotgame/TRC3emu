package net.toydotgame.TRC3emu.emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.toydotgame.TRC3emu.Log;

@SuppressWarnings("serial") // No intent on doing serialisation
public class Terminal extends JFrame {
	private JTextArea text;
	
	public Terminal() {
		final Dimension size = new Dimension(800, 600);
		final int padding = 20;
		
		// Init JFrame:
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Terminal - TRC3emu");
		this.setSize(size);
		this.setResizable(false);
		
		// Init JPanel:
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
		GridLayout grid = new GridLayout(1,1); // Only one element
		grid.setHgap(padding);
		grid.setVgap(padding);
		panel.setLayout(grid);
		panel.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				Character input = e.getKeyChar();
				input = Character.toUpperCase(input);
			}
			
			// Here just because compiler:
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		this.add(panel);
		
		// Init JTextArea
		this.text = new JTextArea("Lorem ipsum\nhi");
		this.text.setLineWrap(true);
		this.text.setWrapStyleWord(true);
		this.text.setForeground(Color.WHITE);
		this.text.setBackground(Color.BLACK);
		this.text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 21));
		this.text.setEditable(false);
		this.text.setTabSize(6); // This yields a 4 char tab for some reason
		this.text.setFocusable(false);
		panel.add(this.text);
		
		this.setVisible(true);
		panel.requestFocusInWindow();
	}
	
	public void in() {
		Log.log("hi");
	}
}
