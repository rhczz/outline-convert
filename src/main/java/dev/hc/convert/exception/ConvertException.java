package dev.hc.convert.exception;

/**
 * 转换异常
 *
 * @author Leo
 * @since 2025/8/1 19:46
 */
public class ConvertException extends ConversionFailureException {

    public ConvertException(ConversionErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    public ConvertException(ConversionErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
    }

}
