package net.toydotgame.TRC3emu.emulator.terminal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

/**
 * Holds 8 {@link Terminal} instances in a simple Swing application.
 */
@SuppressWarnings("serial") // No intent on serialisation
public class TerminalManager extends JFrame {
	// TODO: Unread notif
	// TODO: Tabs for all terminals, nicely sized pls
	// TODO: ASCII DEL and pre-32 codepoint support; consts for \r \n newline
	// accepts
	
	// Instance fields:
	private Terminal[] t = new Terminal[8];      // Establish all terminals
	private int viewedTerminal;                  // Currently viewed terminal
	private JPanel panel;                        // JPanel global for #halt() method
	private JTextArea text;                      // JTextArea global for setting terminal
	private JScrollPane scroll;                  // JScrollPane holding vertical scroll bar
	private Character input;                     // Char consumed by reading terminals
	
	// Constants:
	private static final Dimension size
		= new Dimension(800, 600);                // Size of the window
	private static final int padding = 30;        // Padding around view, etc
	private static final int scrollbarWidth = 15; // Width of the vertical scroll bar
	private static final String windowBrand
		= "TRC3emu";                              // Used to set/reset window title
	
	/**
	 * Creates a terminal window with 8 terminals.
	 */
	public TerminalManager() {		
		// Init JFrame:
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Terminal 0 - "+windowBrand);
		setSize(size);
		setResizable(false);
		setLocationRelativeTo(null); // Centre window
		addKeyListener(new KeyAdapter() { // KeyAdapter over KeyListener to avoid defining unused methods
			@Override
			public void keyPressed(KeyEvent e) {
				parseKey(e);
			}
		});
		
		// Set theme: Iterate through L&Fs to find Nimbus, then set and break
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
		add(panel);
		
		// Init JTextArea:
		text = new JTextArea("foofoo\n\tbarbar");
		text.setLineWrap(true); // We want to wrap on each char only, so don't set word wrap
		text.setForeground(Color.WHITE);
		text.setBackground(Color.BLACK);
		text.setFont(new Font(Font.MONOSPACED, Font.BOLD, 21));
		text.setEditable(false);
		text.setTabSize(6); // This yields a 4 char tab for some reason
		text.setFocusable(false);
		text.setBorder(new EmptyBorder(
			padding/2, padding, padding/2, padding-scrollbarWidth
		));
		panel.add(text);
		
		// Add scroll bar to JTextArea:
		scroll = new JScrollPane(text,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setPreferredSize(
			new Dimension(scrollbarWidth, 0) // Manually force scroll bar size
		);
		panel.add(scroll);
				
		// Init Terminal instances:
		for(int i = 0; i < 8; i++) t[i] = new Terminal("Terminal "+i);
		setDisplay(viewedTerminal); // Initialise the default terminal to open to
		setVisible(true);
	}
	
	/**
	 * Takes a {@link java.awt.event.KeyEvent KeyEvent} in, and if the following
	 * conditions are met, passes a {@code char} of the input KeyEvent through to
	 * a {@link Terminal} instance.
	 * <ol>
	 * 	<li>The currently viewed terminal in this window is actually requesting an
	 * input</li>
	 * 	<li>The value of the key code is within the range {@code 0x20}–{@code
	 * 0x7E} (inclusive)</li>
	 * </ol>
	 * @param e {@link java.awt.event.KeyEvent KeyEvent} to parse and input
	 * @throws ArrayIndexOutOfBoundsException If (somehow) the viewed terminal is
	 * not in the list of {@link TerminalManager#t}
	 * @see #input(int)
	 */
	private void parseKey(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == 0x1B) System.exit(0); // User hit escape
		
		Terminal terminal = t[viewedTerminal];
		// Don't pass key to terminal who doesn't want it:
		if(!terminal.pendingInput) return;
		// Don't input non-ASCII/desired:	
		if(keyCode < 0x20 || keyCode > 0x7E) return; // TODO: Keep DEL char (127) too?
		// TODO: IF including full ASCII in the future, then check that
		// e.getKeyChar() __DOESN'T__ misbehave
		// TODO: F-keys input ÿ???
		
		input = Character.toUpperCase(e.getKeyChar()); // TODO: Allow lowercase?
		terminal.pendingInput = false; // VERY IMPORTANT! Re-lock terminal once input sent
	}
	
	/**
	 * Sets the content of this window's text to the desired terminal. Sets the
	 * value of {@link #client}.
	 * @param term {@link Terminal} instance number, 0–7
	 */
	private void setDisplay(int term) {
		Terminal terminal = t[term];
		viewedTerminal = term;
		
		text.setText(terminal.content);
		setTitle(terminal.title()+" - "+windowBrand);
	}
	
	/**
	 * Returns the key code of the {@code char} the user inputs on the specified
	 * {@link Terminal} instance.
	 * @param term {@link Terminal} instance number, 0–7
	 * @return Key code of the input character
	 * @throws ArrayIndexOutOfBoundsException If the terminal requested is not
	 * within the range of 0 through 7 inclusive
	 * @see #parseKey(KeyEvent)
	 */
	public int input(int term) {
		Terminal terminal = t[term];
		
		terminal.pendingInput = true; // Unlock terminal so we can send it stuff
		while(terminal.pendingInput) {
			try { // Busy loop sucks, but CPU emulation is sequential anyway
				Thread.sleep(1); // No time for EDT to check user input otherwise
				// TODO: Terminal manager on separate thread for optimisations,
				// non-busy loop implementation
			} catch(InterruptedException e) {}
		} // Exits when #parseKey(KeyEvent) locks the terminal again, so return:
		
		return (int)input; // Simple type cast to yield ASCII
	}
	
	/**
	 * Outputs an ASCII value of the input character code value, to the desired
	 * port.
	 * @param term {@link Terminal} instance number, 0–7 
	 * @param characterCode Key code of the output character
	 * @throws ArrayIndexOutOfBoundsException If the terminal requested is not
	 * within the range of 0 through 7 inclusive
	 */
	public void output(int term, int characterCode) {
		Terminal terminal = t[term];
		
		char c = (char)characterCode;
		terminal.content += c;
		if(viewedTerminal == term) text.append(String.valueOf(c));
	}
	
	/**
	 * Greys out the terminal screen to indicate the emulator has halted.
	 * @see net.toydotgame.TRC3emu.Main#emulate() Main.emulate()
	 */
	public void halt() {
		Color darkGray = new Color(28, 28, 28);
		panel.setBackground(darkGray);
		text.setBackground(darkGray);
		
		setTitle("Halted - TRC3emu");
	}
}
