package dev.hc.convert.exception;

/**
 * 解析异常
 *
 * @author Leo
 * @since 2025/8/1 19:46
 */
public class ParsingException extends ConversionFailureException {

    public ParsingException(ConversionErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    public ParsingException(ConversionErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
    }

}
