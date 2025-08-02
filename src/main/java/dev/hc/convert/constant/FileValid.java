package dev.hc.convert.constant;

/**
 * @author Leo
 * @since 2025/8/2 15:28
 */
public final class FileValid {

    /** 大小限制常量 */
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 100MB
    public static final long MAX_ZIP_ENTRY_SIZE = 50 * 1024 * 1024L; // 50MB
    public static final int MAX_FILENAME_LENGTH = 255;

    /** 文件名清理常量 */
    public static final String DANGEROUS_CHARS = "\0\r\n\t";
    public static final String REPLACEMENT_CHARS = "____";

    /** 私有构造函数，防止实例化 */
    private FileValid() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
