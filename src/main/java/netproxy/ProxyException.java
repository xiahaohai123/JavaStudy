package netproxy;

public class ProxyException extends Exception {

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyException(String message) {
        super(message);
    }
}
