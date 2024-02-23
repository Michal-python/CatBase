package cat.michal.catbase.common.exception;

public final class CatBaseException extends RuntimeException {
    public CatBaseException(String message) {
        super(message);
    }

    public CatBaseException(Throwable cause) {
        super(cause);
    }
}
