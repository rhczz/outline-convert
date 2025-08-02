package dev.hc.convert.factory;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.parser.FileParser;
import dev.hc.convert.parser.impl.JsonParser;
import dev.hc.convert.parser.impl.MarkdownParser;
import dev.hc.convert.parser.impl.OpmlParser;
import dev.hc.convert.parser.impl.XMindParser;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析器工厂类
 * 负责管理和创建各种格式的解析器实例，实现懒加载
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class ParserFactory {
    
    /** 解析器实例缓存 */
    private static final Map<FileType, FileParser> PARSER_CACHE = new ConcurrentHashMap<>();
    
    /** 私有构造器，防止实例化 */
    private ParserFactory() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * 根据文件类型获取解析器实例
     * 
     * @param fileType 文件类型
     * @return 解析器实例
     */
    public static FileParser getParser(FileType fileType) {
        if (fileType == null) {
            throw ConversionExceptionFactory.systemError("File type cannot be null", null);
        }
        
        return PARSER_CACHE.computeIfAbsent(fileType, ParserFactory::createParser);
    }
    
    /**
     * 根据文件获取解析器实例
     * 
     * @param file 文件
     * @return 解析器实例
     * @throws dev.hc.convert.exception.ConversionFailureException 如果无法确定文件类型
     */
    public static FileParser getParser(File file) {
        FileType fileType = FileType.fromFile(file);
        return getParser(fileType);
    }
    
    /**
     * 根据文件名获取解析器实例
     * 
     * @param filename 文件名
     * @return 解析器实例
     * @throws dev.hc.convert.exception.ConversionFailureException 如果无法确定文件类型
     */
    public static FileParser getParser(String filename) {
        FileType fileType = FileType.fromFilename(filename);
        return getParser(fileType);
    }
    
    /**
     * 创建解析器实例
     */
    private static FileParser createParser(FileType fileType) {
        return switch (fileType) {
            case JSON -> new JsonParser();
            case MARKDOWN -> new MarkdownParser();
            case XMIND -> new XMindParser();
            case OPML -> new OpmlParser();
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
            getParser(fileType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查是否支持指定的文件
     * 
     * @param file 文件
     * @return 是否支持
     */
    public static boolean isSupported(File file) {
        try {
            getParser(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 清空缓存（主要用于测试）
     */
    static void clearCache() {
        PARSER_CACHE.clear();
    }
}