package app.tradeflows.api.api_gateway.exceptions;

public class AccessDeniedException extends Exception{
    public AccessDeniedException(String message) {
        super(message);
    }
}
