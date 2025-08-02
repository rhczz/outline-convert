package dev.hc.convert.exception;

/**
 * 转换异常码枚举
 *
 * @author Leo
 * @since 2025/8/1 19:43
 */
public enum ConversionErrorCode {

    /** Input parameter related errors */
    FILE_NAME_EMPTY("CONV_400", "File name cannot be empty"),

    /** File related errors */
    FILE_NOT_FOUND("CONV_001", "File not found: %s"),
    FILE_READ_ERROR("CONV_002", "Failed to read file: %s"),
    FILE_WRITE_ERROR("CONV_003", "Failed to write file: %s"),
    UNSUPPORTED_FILE_FORMAT("CONV_004", "Unsupported file format: %s"),

    /** Parsing related errors */
    PARSE_ERROR("CONV_101", "Failed to parse file: %s"),
    INVALID_FILE_STRUCTURE("CONV_102", "Invalid file structure: %s"),

    /** Conversion related errors */
    CONVERSION_NOT_SUPPORTED("CONV_201", "Unsupported conversion type: %s -> %s"),
    CONVERSION_FAILED("CONV_202", "Conversion failed: %s"),

    /** System related errors */
    SYSTEM_ERROR("CONV_901", "System error: %s");

    private final String code;
    private final String messageTemplate;

    ConversionErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String getCode() {
        return code;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }

}
