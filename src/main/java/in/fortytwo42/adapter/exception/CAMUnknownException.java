package in.fortytwo42.adapter.exception;

public class CAMUnknownException extends Exception{

    private static final long serialVersionUID = 1L;

    private final Throwable throwable;


    public CAMUnknownException(Throwable throwable) {
        this.throwable = throwable;
    }
}
