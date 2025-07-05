package net.toydotgame.TRC3emu.emulator.terminal;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.toydotgame.utils.Package;

@SuppressWarnings("serial") // No intent on serialisation
@Package
public class Terminal extends JPanel {
	// Instance fields:
	private boolean pendingInput; // If the terminal is waiting for an input
	private JTextArea content;    // Text of this terminal
	private char input;           // User key input to this panel, read when value changes
	@Package boolean active;      // True when this is the currently viewed terminal
	@Package boolean unread;      // This is set true when this terminal is written to but not active
	private TerminalManager parent; // Owner of this instance
	private JScrollPane scroll;     // Scroll pane for this specific view
	
	// Constants:
	private static final int padding = 30;        // Padding around view, etc
	private static final int scrollbarWidth = 15; // Width of the vertical scroll bar
	
	/**
	 * Creates a new Terminal instance. Requires a name for this terminal.
	 * @param title Tab title of this terminal
	 */
	@Package Terminal(TerminalManager parent, String title) {
		this.parent = parent;
		
		// Init JPanel:
		setBackground(Color.BLACK);
		GridLayout grid = new GridLayout(1,1); // Only one element
		grid.setHgap(padding);
		grid.setVgap(padding);
		setLayout(grid);
		setName(title);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Overwrite the pointer cursor set for the tabs
		setFocusable(true); // Not focusable by default??
		addKeyListener(new KeyAdapter() { // KeyAdapter over KeyListener to avoid defining unused methods
			private boolean altDown; // Ignore Alt+key inputs
			
			@Override public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ALT) altDown = true;
				
				if(!altDown) parseKey(e);
			}
			
			@Override public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ALT) altDown = false; 
			}
		});
		
		// Init JTextArea:
		content = new JTextArea();
		content.setLineWrap(true); // We want to wrap on each char only, so don't set word wrap
		content.setForeground(Color.WHITE);
		content.setBackground(Color.BLACK);
		content.setFont(new Font(Font.MONOSPACED, Font.BOLD, 21));
		content.setEditable(false);
		content.getCaret().setVisible(true); // Override .setEditable(false) and force caret to appear
		content.setTabSize(4);
		content.setFocusable(false);
		content.setBorder(new EmptyBorder(
			// Imperfect because on the very last line our vertical scroll activates, but ugh oh well:
			padding/2, padding, padding/2, padding-scrollbarWidth
		));
		add(content);
		
		// Add scroll bar to JTextArea:
		scroll = new JScrollPane(content,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setPreferredSize(
			new Dimension(scrollbarWidth, 0) // Manually force scroll bar size
		);
		add(scroll);
	}
	
	/**
	 * Returns the title value of this terminal, plus extra I/O status fluff.
	 * @return Terminal title for this instance
	 */
	@Override public String getName() { // Gotta be public because overriding super.getName()
		String title = super.getName();
		
		String prefix = "  ", suffix = " ";
		if(unread) prefix = "! ";
		if(pendingInput) suffix = "*";
		
		return prefix+title+suffix;
	}
	
	/**
	 * Returns the key code of the {@code char} the user input, once the input
	 * has actually occurred. This method will cause this specified Terminal
	 * instance to go into a psueodo-waiting/prompting-for-input state.
	 * @return Key code of the input character
	 * @see #parseKey(KeyEvent)
	 */
	public int read() {
		pendingInput = true; // Unlock terminal
		parent.refresh(); // Refresh tab titles to show that this terminal wants attention
		
		while(pendingInput) { // When #parseKey(KeyEvent) re-locks this, we can continue
			try { // Busy loop sucks, but CPU emulation is sequential anyway
				Thread.sleep(1); // No time for EDT to check user input otherwise
				// TODO: Terminal manager on separate thread for optimisations,
				// non-busy loop implementation therefore
			} catch(InterruptedException e) {}
		} // Exits when #parseKey(KeyEvent) locks the terminal again, so return:
		
		parent.refresh(); // Refresh tab titles to reflect we got our input and we're happy
		return (int)input; // Simple type cast to yield ASCII
	}
	
	/**
	 * Print the ASCII representation of the input {@code int} to this Terminal.
	 * @param charCode Numeric code point of the character to print
	 */
	public void print(int charCode) {
		content.append(
			String.valueOf((char)charCode)
		);
		
		// Auto-scroll and move caret to text insert position: You can get by
		// with just the caret position causing a scroll, but setting the scroll
		// pane incorporates the bottom padding too (looks good)
		scroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
		content.getCaret().setDot(Integer.MAX_VALUE);
		
		if(!active) {
			unread = true;
			parent.refresh();
		}
	}
	
	/**
	 * Dim the terminal screen for this instance.
	 */
	@Package void halt() {
		Color darkGray = new Color(28, 28, 28);
		setBackground(darkGray);
		content.setBackground(darkGray);
	}
	
	/**
	 * Takes a {@link java.awt.event.KeyEvent KeyEvent} in, and if the following
	 * conditions are met, passes a {@code char} of the input KeyEvent through to
	 * a {@link Terminal} instance.
	 * <ol>
	 * 	<li>This (focused) terminal panel is actually waiting for an input</li>
	 * 	<li>The value of the key code is within the range {@code 0x20}–{@code
	 * 0x7E} (inclusive)</li>
	 * </ol>
	 * @param e {@link java.awt.event.KeyEvent KeyEvent} to parse and input
	 * @see #read()
	 */
	private void parseKey(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_ESCAPE) System.exit(0);
		
		// Don't pass key to terminal who doesn't want it:
		if(!pendingInput) return;
		
		// Don't input non-ASCII/desired:	
		if(keyCode == 0x0 || keyCode > 0x7F) return;
		// TODO: IF including full ASCII in the future, then check that
		// e.getKeyChar() __DOESN'T__ misbehave
		// TODO: F-keys input ÿ???
		
		input = Character.toUpperCase(e.getKeyChar()); // TODO: Allow lowercase?
		pendingInput = false; // VERY IMPORTANT! Re-lock terminal once input sent
	}
}
