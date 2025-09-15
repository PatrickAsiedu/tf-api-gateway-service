package app.tradeflows.api.api_gateway.exceptions;

public class ConflictException extends Exception{
    public ConflictException(String message) {
        super(message);
    }
}
