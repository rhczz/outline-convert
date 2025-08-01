package dev.hc.convert.exception;

/**
 * 转换失败异常
 *
 * @author Leo
 * @since 2025/8/1 19:39
 */
public class ConversionFailureException extends RuntimeException {

    private final String errorCode;

    public ConversionFailureException(String message) {
        super(message);
        this.errorCode = null;
    }

    public ConversionFailureException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConversionFailureException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
