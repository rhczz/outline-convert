package dev.hc.convert;

import dev.hc.convert.engine.ConvertEngine;
import dev.hc.convert.exception.ConversionExceptionFactory;

import java.io.File;

/**
 * 文件类型枚举
 * 支持的文件格式定义和相关操作
 * 
 * @author Leo
 * @since 2025/8/1 19:18
 */
public enum FileType {

    /** JSON 文件类型 */
    JSON("json", "application/json", "JSON"),

    /** Markdown文件类型 */
    MARKDOWN("md", "text/markdown", "Markdown") {
        @Override
        public String[] getExtensions() {
            return new String[]{"md", "markdown"};
        }
    },

    /** XMind文件类型 */
    XMIND("xmind", "application/xmind", "XMind"),

    /** OPML文件类型 */
    OPML("opml", "text/x-opml", "OPML");

    private static final String DEFAULT_FILENAME = "untitled";
    private final String defaultExtension;
    private final String mimeType;
    private final String displayName;

    FileType(String defaultExtension, String mimeType, String displayName) {
        this.defaultExtension = defaultExtension;
        this.mimeType = mimeType;
        this.displayName = displayName;
    }

    /** 获取默认文件扩展名 */
    public String getDefaultExtension() {
        return defaultExtension;
    }

    /** 获取MIME类型 */
    public String getMimeType() {
        return mimeType;
    }

    /** 获取显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** 获取默认文件名 */
    public String getDefaultFileName() {
        return DEFAULT_FILENAME + "." + getDefaultExtension();
    }

    /** 获取支持的文件扩展名（可被子类重写） */
    public String[] getExtensions() {
        return new String[]{defaultExtension};
    }

    /** 检查是否支持指定的文件扩展名 */
    public boolean supportsExtension(String extension) {
        if (extension == null) {
            return false;
        }
        
        String ext = extension.toLowerCase();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        
        for (String supportedExt : getExtensions()) {
            if (supportedExt.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从文件推断文件类型
     */
    public static FileType fromFile(File file) {
        if (file == null) {
            throw ConversionExceptionFactory.fileIsNull();
        }
        if (!file.exists()) {
            throw ConversionExceptionFactory.fileNotFound(file);
        }
        return fromFilename(file.getName());
    }

    /**
     * 从文件名推断文件类型
     */
    public static FileType fromFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw ConversionExceptionFactory.fileNameEmpty();
        }

        String name = filename.toLowerCase();
        for (FileType type : values()) {
            for (String ext : type.getExtensions()) {
                if (name.endsWith("." + ext.toLowerCase())) {
                    return type;
                }
            }
        }
        
        throw ConversionExceptionFactory.unsupportedFileFormat(filename);
    }

    /**
     * 创建转换引擎，支持链式调用
     * 
     * @return 转换引擎构建器
     */
    public ConvertEngine.Builder to(FileType targetType) {
        return ConvertEngine.from(this).to(targetType);
    }

    /**
     * 转换为JSON格式
     */
    public ConvertEngine.Builder toJson() {
        return to(JSON);
    }

    /**
     * 转换为Markdown格式
     */
    public ConvertEngine.Builder toMarkdown() {
        return to(MARKDOWN);
    }

    /**
     * 转换为XMind格式
     */
    public ConvertEngine.Builder toXMind() {
        return to(XMIND);
    }

    /**
     * 转换为OPML格式
     */
    public ConvertEngine.Builder toOpml() {
        return to(OPML);
    }

    @Override
    public String toString() {
        return displayName + " (*." + defaultExtension + ")";
    }
}
