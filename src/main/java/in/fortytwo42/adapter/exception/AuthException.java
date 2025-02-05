package in.fortytwo42.adapter.exception;

public class AuthException extends Exception {
	 /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Throwable throwable;

    private final Long errorCode;

    private final String message;

    public AuthException(Throwable throwable, Long errorCode, String message) {
        super();
        this.throwable = throwable;
        this.errorCode = errorCode;
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
