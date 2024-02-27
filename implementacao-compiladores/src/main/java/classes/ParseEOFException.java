package classes;

public class ParseEOFException extends Exception {

    public ParseEOFException() {
        super("Erro de leitura devido a fim de arquivo inesperado.");
    }

    public ParseEOFException(String message) {
        super(message);
    }

    public ParseEOFException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseEOFException(Throwable cause) {
        super(cause);
    }
}
