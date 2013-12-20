/**
 *
 */
package org.imagopole.omero.tools.api;

/**
 * Runtime exception for all error conditions originating
 * from underlying components (eg. server errors) or
 * technical errors.
 *
 * @author seb
 *
 */
public class RtException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /**
     * Vanilla constructor
     */
    public RtException() {
        super();
    }

    /**
     * Parameterized constructor
     *
     * @param message exception message
     */
    public RtException(String message) {
        super(message);
    }

    /**
     * Parameterized constructor
     *
     * @param cause root cause
     */
    public RtException(Throwable cause) {
        super(cause);
    }

    /**
     * Parameterized constructor
     *
     * @param message exception message
     * @param cause root cause
     */
    public RtException(String message, Throwable cause) {
        super(message, cause);
    }

}
