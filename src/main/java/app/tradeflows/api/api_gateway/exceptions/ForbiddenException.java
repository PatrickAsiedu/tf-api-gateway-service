package app.tradeflows.api.api_gateway.exceptions;

public class ForbiddenException extends Exception{
    public ForbiddenException(String message) {
        super(message);
    }
}
