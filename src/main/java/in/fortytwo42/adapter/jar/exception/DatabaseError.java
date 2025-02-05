
package in.fortytwo42.adapter.jar.exception;

public class DatabaseError extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Throwable throwable;

    /**
     * Default constructor
     */
    public DatabaseError() {
        super();
        this.throwable = null;
    }

    /**
     * 
     * @param throwable
     */
    public DatabaseError(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
