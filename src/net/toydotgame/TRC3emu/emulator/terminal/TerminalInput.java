package net.toydotgame.TRC3emu.emulator.terminal;

import java.util.concurrent.SynchronousQueue;
import net.toydotgame.utils.Log;
import net.toydotgame.utils.Package;

/**
 * This class acts as a simple wrapper to somewhat invert the typical {@linkplain
 * java.util.concurrent.SynchronousQueue SynchronousQueue} logic. In <u>this</u>
 * implementation:
 * <ol>
 * 	<li>The <i>consumer</i> can {@code get()} whenever it likes, which blocks the
 * invoking thread until the producer produces something</li>
 * 	<li>The <i>producer</i> can call {@code give()} when it produces something
 * <u>whenever</u>, even when the consumer isn't ready. In the event the consumer
 * does not want this value, execution is cancelled. Otherwise, a {@code give()}
 * will un-block a waiting consumer thread</li>
 * </ol>
 * <br>
 * This <i>give-and-get</i> setup allows for backwards-from-normal use cases of
 * the SynchronousQueue class. For TRC3emu specifically, it is quite useful for
 * {@code give()}ing key press events at a whim whenever the user types, and
 * allowing the emulator package to call {@code get()} when it is actually
 * requesting a key code.<br>
 * <br>
 * Further to this, this class' hard-coded usage of the {@link java.lang.Integer
 * Integer} data type is because this class' sole purpose is for that
 * aforementioned key press information exchange.
 * @see java.util.concurrent.SynchronousQueue
 */
@SuppressWarnings("serial") // This class is not serialised in this project
@Package class TerminalInput extends SynchronousQueue<Integer> {
	// Instance fields:
	private Terminal terminal;
	/**
	 * Stores the state of whether or not {@link #get()} has been called but has
	 * not yet returned a value ({@code true}, i.e: <i>This instance </i>is<i>
	 * pending some value to be input</i>)—or {@code false} otherwise (i.e: When
	 * {@code .get()} has either never been called yet or has most recently
	 * completely returned already).
	 * @see #get()
	 */
	@Package boolean pending;
	
	/**
	 * Create a new {@link TerminalInput} instance.
	 * @param term {@link Terminal} instance that this queue is attached to.
	 * This class ideally (and in fact is only tested for) works with one Terminal
	 * per {@code TerminalInput} 
	 */
	// Only constructor we need:
    @Package TerminalInput(Terminal term) {
        super(false);
    		terminal = term;
    }
    
    /**
     * Marks this {@link TerminalInput} instance as "pending" an input, calls the
     * parent {@link TerminalManager#refresh()} method to reflect this, and then
     * blocks until {@link java.util.concurrent.SynchronousQueue#take()
     * super.take()} resolves.<br>
     * <br>
     * {@code super.take()} will not resolve in this implementation
     * <b><i>unless</i></b> the following order of operations occurs in this order:
     * <ol>
     * 	<li>Someone calls {@code .get()} to request a value</li>
     * 	<li>Something calls {@code .give()} to give {@code .get()} what it
     * wants</li>
     * 	<li>Repeat</li>
     * </ol>
     * <br>
     * In this manner, calls to {@code .give()} before {@code .get()}, or calls to
     * {@code get()} that go completely unanswered—both mean that {@code .get()}
     * and its thread will remain blocked until later.<br>
     * <br>
     * This method <i>can</i> crash the program if a {@link
     * java.lang.InterruptedException} is hit. This is likely due to a Ctrl+C or
     * similar manual intervention, however, so it likely doesn't matter and it'd
     * be okay to give up.
     * @return The first value that Something gave {@code .give()} <u>after</u>
     * the time this method was invoked
     * @see TerminalInput
     */
	@Package Integer get() {
		try {
			pending = true;                 // Unlock the terminal and this queue
			terminal.parent.refresh();      // Update GUI
			return super.take();            // .take() block, blocks thread until done
		} catch(InterruptedException e) {   // User probably killed us, but die anyway:
			Log.fatalError(e.getMessage());
			return -1;                      // Make compiler happy
		} finally {                         // Practically will only run on success…
			pending = false;                // This code will actually run _after_
			terminal.parent.refresh();      //     the return does! Use it to
			                                //     close once .take() resolves
		}
    }
	
	/**
	 * Calls {@link java.util.concurrent.SynchronousQueue#put(E)
	 * super.put(Integer)}. The case where {@code super.put(E)} blocks the thread
	 * until {@code super.take()} is called cannot occur, because in instances
	 * where {@code .give(Integer)} (abstractly a wrapper for {@code
	 * super.put(E)}) is called <i>before</i> {@code .get()} (abstractly a wrapper
	 * for {@code super.take()}), the value of {@link #pending} would be {@code
	 * false}—meaning this method would quit anyway and we wouldn't even have the
	 * chance to block even for a second.<br>
	 * <br>
	 * Due to this, {@code .give(Integer)} can be called however many times you
	 * want, as often as you want, when you want—you get the picture. It will
	 * disregard everything until the moment {@code .get()} makes its request.
	 * @param i Value to give to {@code .get()}
	 * @see #get()
	 * @see TerminalInput
	 */
	@Package void give(Integer i) {
		if(!pending) return;
		
		try {
			super.put(i);
		} catch(InterruptedException e) { // User probably killed us, so give up:
			Log.fatalError(e.getMessage());
		}
	}
}
