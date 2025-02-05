
package in.fortytwo42.adapter.jar.exception;

public class BadDataException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Long errorCode;

    private final String message;

    /**
     * Default Constructor
     */
    public BadDataException(Long errorCode, String message) {
        super();
        this.errorCode = errorCode;
        this.message = message;
    }

//    public BadDataException(long errorCode, String message) {
//        super(message);
//        this.errorCode = errorCode;
//    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

}
