package vinoteque.exceptions;

/**
 *
 * @author George Ushakov
 */
public class CsvLineParseException extends Exception {

    public CsvLineParseException(String message) {
        super(message);
    }

    public CsvLineParseException(Throwable t) {
        super(t);
    }
    
    public CsvLineParseException(Throwable t, String message) {
        super(t);
    }
}
