package net.toydotgame.TRC3emu.emulator.terminal;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.toydotgame.utils.Log;
import net.toydotgame.utils.Package;

/**
 * Holds 8 {@link Terminal} instances in a simple Swing application.
 */
@SuppressWarnings("serial") // No intent on serialisation
public class TerminalManager extends JFrame {
	// Instance fields:
	private Terminal[] t = new Terminal[terminalCount]; // Establish all terminals
	private JTabbedPane tabs;                           // Global only for #refreshTitle()
	
	// Constants:
	private static final Dimension size
		= new Dimension(800, 600);              // Size of the window
	private static final String windowBrand
		= "TRC3emu";                            // Used to set/reset window title
	private static final int terminalCount = 8; // Number of terminals to create
	
	/**
	 * Creates a terminal window with 8 terminals.
	 */
	public TerminalManager() {		
		// Init JFrame:
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(windowBrand);
		setSize(size);
		setResizable(false);
		setLocationRelativeTo(null); // Centre window		
		// Set theme: Iterate through L&Fs to find Nimbus, then set and break
		try { // Prefer Nimbus…
			for(LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
				if(i.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(i.getClassName());
					break;
				}
			}
		} catch(Exception e) {} // …tolerate Metal
		
		// Init JTabbedPane:
		tabs = new JTabbedPane();
		tabs.setFocusable(false);
		tabs.addChangeListener(new ChangeListener() { // Change window title when tab changes:
			@Override public void stateChanged(ChangeEvent e) {
				refresh();
			}
		});
		tabs.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(tabs);
		
		// Init Terminal instances:
		for(int i = 0; i < terminalCount; i++) {
			t[i] = new Terminal(this, "Terminal "+i);
			tabs.add(t[i]); // Add tab before adding mnemonic for tab
			if(i < 10) tabs.setMnemonicAt(i, KeyEvent.VK_0+i);
		}
		
		setVisible(true);
		refresh(); // To account for the don't-run-when-not-visible code in this method
		
		Log.debug("Created new TerminalManager GUI on thread "
			+"\""+Thread.currentThread().getName()+"\". "
			+"Is EDT?: "+SwingUtilities.isEventDispatchThread()
		);
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Log.debug("CURRENTLY RUNNING THREADS:");
		for(Thread t : threadSet) Log.debug("* "+t.getName()); // AWT-EventQueue-0 (EDT) doesn't run straight away
	}
	
	/**
	 * Refreshes the title of the current {@linkplain javax.swing.JFrame JFrame}
	 * instance (or, well, {@link TerminalManager}<code> extends JFrame</code>)
	 * using {@link java.awt.Component#getName() Component.getName()}. AFAIK,
	 * the default tab Component that JTabbedPane uses by default inherits/is
	 * the child content {@linkplain javax.swing.JPanel JPanel}('s name), and
	 * therefore JPanel's {@linkplain javax.swing.JPanel#setName(String)
	 * .setName(String)}.<br>
	 * <br>
	 * Additionally, this method serves the dual purpose of setting the {@link
	 * Terminal#active} state of all terminals. ({@code true} for the viewed
	 * Terminal, {@code false} for all else)
	 * @see javax.swing.JPanel#setName(String) JPanel.setName(String)
	 */
	@Package void refresh() {
		if(!isVisible()) return; // Avoid refreshing title before we've even displayed the window
		
		// Update window title:
		Terminal viewedTerminal = (Terminal)tabs.getSelectedComponent();
		setTitle(viewedTerminal.getName()+" - "+windowBrand);
		
		// Mark only the viewed terminal as active:
		for(Terminal term : t) term.active = false;		
		viewedTerminal.active = true;
		
		// The viewed terminal is now read:
		viewedTerminal.unread = false;
		
		// Update tab names:
		for(int i = 0; i < tabs.getTabCount(); i++)
			tabs.setTitleAt(i, t[i].getName());
	}
	
	/**
	 * Get a {@link Terminal} instance held within this instance of {@link
	 * TerminalManager}.
	 * @param term {@link Terminal} instance number
	 * @return Terminal object
	 * @throws ArrayIndexOutOfBoundsException If the terminal requested does not
	 * exist
	 */
	public Terminal get(int term) {
		return t[term];
	}
	
	/**
	 * Greys out the terminal screen to indicate the emulator has halted.
	 * @see net.toydotgame.TRC3emu.Main#emulate() Main.emulate()
	 */
	public void halt() {
		for(Terminal term : t) term.halt();
		
		setTitle("Halted - "+windowBrand);
	}
}
