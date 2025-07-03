package net.toydotgame.TRC3emu.emulator.terminal;

import net.toydotgame.utils.Package;

public class Terminal {
	@Package boolean pendingInput; // If the terminal is waiting for an input
	private String title;          // Terminal title
	@Package String content;       // String holding the full text of this terminal
	
	/**
	 * Creates a new Terminal instance. Requires a name for this terminal.
	 * @param title
	 */
	@Package Terminal(String title) {
		this.title = title;
	}
	
	/**
	 * Returns the title value of this terminal, plus extra I/O status fluff.
	 * @return Terminal title for this instance
	 */
	@Package String title() {
		String prefix = "", suffix = "";
		if(pendingInput) prefix = "! ";
		//if(unread) suffix = "*"; // TODO
		
		return prefix+title+suffix;
	}
}
