
package cz.oz.mavenhoe.ex;

/**
 *
 * @author Ondrej Zizka
 */
public class MavenhoeException extends Exception {

	public MavenhoeException(Throwable cause) {
		super(cause);
	}

	public MavenhoeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MavenhoeException(String message) {
		super(message);
	}

	public MavenhoeException() {
	}

}
