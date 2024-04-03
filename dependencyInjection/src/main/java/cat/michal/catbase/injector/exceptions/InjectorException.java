package cat.michal.catbase.injector.exceptions;

/**
 * Universal exception for exceptions while performing actions on injector
 *
 * @author Michał
 */
public class InjectorException extends RuntimeException {

    public InjectorException(String message) {
        super(message);
    }

    public InjectorException(String message, Throwable cause) {
        super(message, cause);
    }
}