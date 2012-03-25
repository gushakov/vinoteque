package vinoteque.exceptions;

/**
 *
 * @author George Ushakov
 */
public class IncorrectFileFormatException extends Exception {

    public IncorrectFileFormatException() {
    }

    public IncorrectFileFormatException(Throwable cause) {
        super(cause);
    }

    public IncorrectFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectFileFormatException(String message) {
        super(message);
    }
    
}
