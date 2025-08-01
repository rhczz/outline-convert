package dev.hc.convert.exception;

import java.io.File;

/**
 * @author Leo
 * @since 2025/8/1 19:47
 */
public class ConversionExceptionFactory {

    /** 文件未找到异常 */
    public static FileOperationException fileNotFound(File file) {
        return new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND,
                ConversionErrorCode.FILE_NOT_FOUND.formatMessage(file.getAbsolutePath())
        );
    }

    /** 文件读取失败异常 */
    public static FileOperationException fileReadError(File file, Throwable cause) {
        return new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR,
                ConversionErrorCode.FILE_READ_ERROR.formatMessage(file.getAbsolutePath()),
                cause
        );
    }

    /** 文件写入失败异常 */
    public static FileOperationException fileWriteError(File file, Throwable cause) {
        return new FileOperationException(
                ConversionErrorCode.FILE_WRITE_ERROR,
                ConversionErrorCode.FILE_WRITE_ERROR.formatMessage(file.getAbsolutePath()),
                cause
        );
    }

    /** 不支持的文件格式异常 */
    public static ConversionFailureException unsupportedFileFormat(String format) {
        return new ConversionFailureException(
                ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.getCode(),
                ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.formatMessage(format)
        );
    }

    /** 解析错误 */
    public static ParsingException parseError(String details, Throwable cause) {
        return new ParsingException(
                ConversionErrorCode.PARSE_ERROR,
                ConversionErrorCode.PARSE_ERROR.formatMessage(details),
                cause
        );
    }

    /** 无效的文件结构 */
    public static ParsingException invalidFileStructure(String details) {
        return new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE,
                ConversionErrorCode.INVALID_FILE_STRUCTURE.formatMessage(details)
        );
    }

    /** 不支持的转换 */
    public static ConvertException conversionNotSupported(String sourceFormat, String targetFormat) {
        return new ConvertException(
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED,
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage(sourceFormat, targetFormat)
        );
    }

    /** 转换失败 */
    public static ConvertException conversionFailed(String details, Throwable cause) {
        return new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(details),
                cause
        );
    }

    /** 系统错误 */
    public static ConversionFailureException systemError(String details, Throwable cause) {
        return new ConversionFailureException(
                ConversionErrorCode.SYSTEM_ERROR.getCode(),
                ConversionErrorCode.SYSTEM_ERROR.formatMessage(details),
                cause
        );
    }

}
