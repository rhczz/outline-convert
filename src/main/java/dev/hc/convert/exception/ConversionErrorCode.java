package dev.hc.convert.exception;

/**
 * 转换异常码枚举
 *
 * @author Leo
 * @since 2025/8/1 19:43
 */
public enum ConversionErrorCode {

    /** 文件相关错误 */
    FILE_NOT_FOUND("CONV_001", "文件未找到: %s"),
    FILE_READ_ERROR("CONV_002", "文件读取失败: %s"),
    FILE_WRITE_ERROR("CONV_003", "文件写入失败: %s"),
    UNSUPPORTED_FILE_FORMAT("CONV_004", "不支持的文件格式: %s"),

    /** 解析相关错误 */
    PARSE_ERROR("CONV_101", "文件解析失败: %s"),
    INVALID_FILE_STRUCTURE("CONV_102", "文件结构无效: %s"),

    /** 转换相关错误 */
    CONVERSION_NOT_SUPPORTED("CONV_201", "不支持的转换类型: %s -> %s"),
    CONVERSION_FAILED("CONV_202", "转换失败: %s"),

    /** 系统相关错误 */
    SYSTEM_ERROR("CONV_901", "系统错误: %s");

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
