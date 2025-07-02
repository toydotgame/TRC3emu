package net.toydotgame.TRC3emu.emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial") // No intent on doing serialisation
public class Terminal extends JFrame {
	// TODO: Multi-tab terminals for each port pair? Notification for unread tabs? Think tty0-7
	private JPanel panel; // Global only for dim() method
	private JTextArea text;
	private JScrollPane scroll;
	private boolean waitingForInput;
	private char input;
	private static final String windowTitle = "Terminal - TRC3emu";
	
	public Terminal() {
		final Dimension size = new Dimension(800, 600);
		final int padding = 30;
		final int scrollbarWidth = 15;
		
		// Init JFrame:
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(windowTitle);
		setSize(size);
		setLocationRelativeTo(null);
		setResizable(false);
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				parseKeyEvent(e);
			}
			
			// Here just because compiler:
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		// Set theme:
		try { // Prefer Nimbus…
			for(LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
				if(i.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(i.getClassName());
					break;
				}
			}
		} catch(Exception e) {} // …tolerate Metal
		// Init JPanel:
		panel = new JPanel();
		panel.setBackground(Color.BLACK);
		GridLayout grid = new GridLayout(1,1); // Only one element
		grid.setHgap(padding);
		grid.setVgap(padding);
		panel.setLayout(grid);
		//panel.requestFocus(); // This + the Unix one at the bottom is needed for Windows
		add(panel);
		
		// Init JTextArea
		text = new JTextArea("");
		text.setLineWrap(true); // We want to wrap on each char only, so don't set word wrap
		text.setForeground(Color.WHITE);
		text.setBackground(Color.BLACK);
		text.setFont(new Font(Font.MONOSPACED, Font.BOLD, 21)); // TODO: Looks good on Linux?
		text.setEditable(false);
		text.setTabSize(6); // This yields a 4 char tab for some reason
		text.setFocusable(false);
		text.setBorder(new EmptyBorder(
			padding/2, padding, padding/2, padding-scrollbarWidth
		));
		panel.add(text);
		// Add scroll bar:
		scroll = new JScrollPane(text,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setPreferredSize(
			new Dimension(scrollbarWidth, 0) // Manually force scroll bar size
		);
		panel.add(scroll);
				
		setVisible(true);
		//panel.requestFocusInWindow(); // This alone works for Unix
	}
	
	/**
	 * Take in a keyEvent from user input and handle it to the terminal text
	 * buffer (held in {@link #text}). If {@link #in()} is not active currently,
	 * the input will be dropped.
	 * @param Character input by the user
	 */
	private void parseKeyEvent(KeyEvent e) {
		if(e.getKeyCode() == 0x1B) System.exit(0); // User hit escape
		
		if(!this.waitingForInput) return; // Do nothing if we aren't looking for input
		if(e.getKeyCode() < 0x20 || e.getKeyCode() > 0x7E) return; // TODO: Keep DEL char (127) too?
		
		input = Character.toUpperCase(e.getKeyChar()); // TODO: Do we really want this?
		
		waitingForInput = false;
		//setTitle(windowTitle);
	}
	
	/**
	 * Opens the terminal's input stream. This sets a flag that means the next
	 * call of {@link #parseKeyEvent(char)} <i>will</i> write a character to the
	 * terminal stream.
	 */
	public int in() {
		waitingForInput = true;
		//setTitle("Awaiting Input - "+windowTitle);

		while(waitingForInput) { // TODO: Busy loop :(
			try {
				Thread.sleep(1); // Busy loop too fast otherwise
			} catch (InterruptedException e) {}
		}
		
		return (int)input; // Simple type cast to yield ASCII
	}
	
	/**
	 * Directly cast the input integer to a {@code char}, and print that.
	 * @param data (Hopefully ASCII) value to print
	 */
	public void out(int data) {
		text.append(String.valueOf((char)data));
		scroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE); // Hack to scroll as we type
	}
	
	/**
	 * Called on a halt.
	 */
	public void dim() {
		Color darkGray = new Color(28, 28, 28);
		panel.setBackground(darkGray);
		text.setBackground(darkGray);
		
		setTitle("Halted - TRC3emu");
	}
}
