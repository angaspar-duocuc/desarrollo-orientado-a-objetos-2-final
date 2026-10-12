package util;

public final class AppException extends RuntimeException {
    public AppException(String safeMessage) { super(safeMessage); }
    public AppException(String safeMessage, Throwable cause) { super(safeMessage, cause); }
}