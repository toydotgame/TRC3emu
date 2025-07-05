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
import net.toydotgame.utils.Log;
import net.toydotgame.utils.Package;
import net.toydotgame.utils.Utils;

@SuppressWarnings("serial") // No intent on serialisation
@Package
public class Terminal extends JPanel {
	// Instance fields:
	@Package TerminalManager parent; // Owner of this instance
	private JScrollPane scroll;      // Scroll pane for this specific view
	private JTextArea content;       // Text of this terminal
	private TerminalInput input      // Passes input between key listener and
		= new TerminalInput(this);   //     whenever the Emulator requests one
	@Package boolean unread;         // This is set true when this terminal is written to but not active
	@Package boolean active;         // True when this is the currently viewed terminal
	
	// Constants:
	private static final int PADDING = 30;                        // Padding around view, etc
	private static final int SCROLLBAR_WIDTH = 15;                // Width of the vertical scroll bar
	private static final boolean DISPLAY_ASCII_CONTROL = true;    // Whether to print code points < 0x20 as replacement chars (if false, prints nothing)
	/**
	 * This option enables the support of the ASCII control characters {@code
	 * 0x8} (<i>Backspace</i>) {@code 7F} (<i>Delete</i>), and {@code 0xA}
	 * (<i>Line feed</i>), which are useful characters for fancy printing in the
	 * terminal. {@link #print(int)} cares about this before it cares about
	 * {@link #DISPLAY_ASCII_CONTROL} (where, if this is {@code true}, display
	 * will be handled specially as described on the tin, and if {@code false},
	 * <i>then</i> {@code DISPLAY_ASCII_CONTROL} takes hold.<br>
	 * <br>
	 * <ul>
	 * 	<li>For {@code 0x8} and {@code 0x7F}, when the system prints this to a
	 * port, the last character present in that Terminal's text buffer will be
	 * removed. If the length of said buffer is {@code 0}, then nothing will be
	 * done</li>
	 * 	<li>For {@code 0xA}, nothing will <i>technically</i> be done—since Swing
	 * works well with {@code \n} in {@linkplain javax.swing.JTextArea
	 * JTextArea}s—but instead this option being {@code true} will mean that the
	 * later check for {@code DISPLAY_ASCII_CONTROL} won't fail on a LF (since
	 * it is a control character after all, and would be replaced by a
	 * replacement char/nothing otherwise)</li>
	 * </ul>
	 * <br>
	 * <b>It should be noted that TRC3 only natively supports the <i>ASCII
	 * display character</i> range specifically: {@code 0x20}–{@code 0x7E}
	 * inclusive.</b> A Minecraft terminal implementation would either have to
	 * handle BS and LF in some dedicated way, or final TRC3 software must be
	 * written to work in some way that doesn't depend on these.<br>
	 * <br>
	 * Of note is that, for a Minecraft implementation, the echo or other such
	 * print loop in the assembly can—instead of blindly pushing from a register
	 * to the GPIO—check the value of the char in the register against these
	 * three values, and handle it accordingly.
	 * @see #parseCharForPrinting(int)
	 */
	private static final boolean ASCII_CONTROL_EXCEPTIONS = true; // Whether to handle BS, DEL, and LF
	
	/**
	 * Creates a new Terminal instance. Requires a name for this terminal.
	 * @param parent Parent {@link TerminalManager} instance who created and
	 * owns this object
	 * @param title Tab title of this terminal
	 */
	@Package Terminal(TerminalManager parent, String title) {
		this.parent = parent;
		
		// Init JPanel:
		setBackground(Color.BLACK);
		GridLayout grid = new GridLayout(1,1); // Only one element
		grid.setHgap(PADDING);
		grid.setVgap(PADDING);
		setLayout(grid);
		setName(title);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Overwrite the pointer cursor set for the tabs
		setFocusable(true); // Not focusable by default??
		addKeyListener(new KeyAdapter() { // KeyAdapter over KeyListener to avoid defining unused methods
			private boolean altDown; // Ignore Alt+key inputs
			
			@Override public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ALT) altDown = true;
				if(altDown) return;
				
				// If not an Alt+key input, parse:
				int keyCode = (int)e.getKeyChar();
				if(keyCode == KeyEvent.VK_ESCAPE) System.exit(0);
				if(keyCode == 0x0 || keyCode > 0x7F) return;
				
				// Valid keystroke, so input it as it is:
				input.give(keyCode);
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
			PADDING/2, PADDING, PADDING/2, PADDING-SCROLLBAR_WIDTH
		));
		add(content);
		
		// Add scroll bar to JTextArea:
		scroll = new JScrollPane(content,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setPreferredSize(
			new Dimension(SCROLLBAR_WIDTH, 0) // Manually force scroll bar size
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
		/* Add !null check because Nimbus L&F specifically (not Metal) crashes if
		 * the TerminalInput doesn't initialise on time or something? By the time
		 * the Swing window appears, TerminalManager#refresh() is called again and
		 * this issue disappears.
		 * This bug/crash is _only_ for Nimbus and with this patch it doesn't even
		 * manifest by the time the window appears. ¯\_(ツ)_/¯
		 */
		if(input != null && input.pending) suffix = "*";
		
		return prefix+title+suffix;
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
	 * Returns the key code of the {@code char} the user input, once the input
	 * has actually occurred. This method will cause this specified Terminal
	 * instance to go into a psueodo-waiting/prompting-for-input state.
	 * @return Key code of the input character
	 */
	public int read() {
		Log.debug("read(): Running on "+Thread.currentThread().getName());
		
		return input.get();
	}
	
	/**
	 * Print the ASCII representation of the input {@code int} to this Terminal.
	 * @param charCode Numeric code point of the character to print
	 */
	public void print(int charCode) {
		Character c = parseCharForPrinting(charCode);
		if(c == null) return; // Don't print anything
		
		if((int)c == 0x8 || (int)c == 0x7F) { // Handle backspaces graphically:
			String text = content.getText();
			if(text.length() == 0) { // For empty screen, do nothing at all:
				Log.error("("+super.getName()+") Tried to BACKSPACE a 0-length text buffer!");
				return;
			}
			content.setText(text.substring(0, text.length()-1));
			// Do note that the computer has NO IDEA we did this, all it knows
			// is that the last character it sent was 0x8
		} else {
			content.append(String.valueOf(c));
		}
		
		// Auto-scroll and move caret to text insert position: You can get by
		// with just the caret position causing a scroll, but setting the scroll
		// pane incorporates the bottom padding too (looks good)
		scroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
		content.getCaret().setDot(Integer.MAX_VALUE);
		
		if(!active) { // GUI notification when unfocused:
			unread = true;
			parent.refresh();
		}
	}
	
	/**
	 * Takes an integer code point, and returns a value based on the constant
	 * configuration (in the static fields of this class).
	 * @param charCode 1-byte code
	 * @return {@code char} of the requested character, or {@code null} if the
	 * character should not be printed 
	 */
	private Character parseCharForPrinting(int charCode) {
		if(charCode > 0x7F) return null; // Handle extended ASCII
		
		if(charCode < 0x20) {
			if(ASCII_CONTROL_EXCEPTIONS
			&& (charCode == 0x8 || charCode == 0x7F || charCode == 0xA)) { // BS, DEL, or LF
					return (char)charCode;
			} // Else, fall through and parse control character accordingly:
			
			String warning = "Tried to print ASCII control character 0x"+Utils.paddedHex(charCode, 2)+"!";
			if(DISPLAY_ASCII_CONTROL) {
				Log.debug(warning+" Printing replacement character instead.");
				return '\uFFFD'; // Unicode REPLACEMENT CHARACTER
			}
			
			Log.error(warning+" ASCII control is disabled.");
			return null;
		}
		
		// Else, valid character who cares:
		return (char)charCode;
	}
}
