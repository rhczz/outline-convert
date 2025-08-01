package dev.hc.convert.exception;

/**
 * 文件操作异常
 *
 * @author Leo
 * @since 2025/8/1 19:45
 */
public class FileOperationException extends ConversionFailureException {

    public FileOperationException(ConversionErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    public FileOperationException(ConversionErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
    }

}
