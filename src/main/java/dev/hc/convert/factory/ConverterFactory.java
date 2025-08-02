package dev.hc.convert.factory;

import dev.hc.convert.FileType;
import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.converter.impl.JsonFormatConverter;
import dev.hc.convert.converter.impl.MarkdownFormatConverter;
import dev.hc.convert.converter.impl.OpmlFormatConverter;
import dev.hc.convert.converter.impl.XMindFormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 格式转换器工厂类
 * 负责管理和创建各种格式的转换器实例，实现懒加载
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class ConverterFactory {
    
    /** 转换器实例缓存 */
    private static final Map<FileType, FormatConverter> CONVERTER_CACHE = new ConcurrentHashMap<>();
    
    /** 私有构造器，防止实例化 */
    private ConverterFactory() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * 根据文件类型获取转换器实例
     * 
     * @param fileType 文件类型
     * @return 转换器实例
     */
    public static FormatConverter getConverter(FileType fileType) {
        if (fileType == null) {
            throw ConversionExceptionFactory.systemError("File type cannot be null", null);
        }
        
        return CONVERTER_CACHE.computeIfAbsent(fileType, ConverterFactory::createConverter);
    }
    
    /**
     * 创建转换器实例
     */
    private static FormatConverter createConverter(FileType fileType) {
        return switch (fileType) {
            case JSON -> new JsonFormatConverter();
            case MARKDOWN -> new MarkdownFormatConverter();
            case XMIND -> new XMindFormatConverter();
            case OPML -> new OpmlFormatConverter();
            default -> throw ConversionExceptionFactory.unsupportedFileFormat(fileType.name());
        };
    }
    
    /**
     * 检查是否支持指定的文件类型
     * 
     * @param fileType 文件类型
     * @return 是否支持
     */
    public static boolean isSupported(FileType fileType) {
        try {
            getConverter(fileType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 清空缓存（主要用于测试）
     */
    static void clearCache() {
        CONVERTER_CACHE.clear();
    }
}