package dev.hc.convert.parser;

import java.io.File;
import java.io.InputStream;

import dev.hc.convert.exception.ParsingException;
import dev.hc.convert.model.OutlineDocument;

/**
 * 文件解析器接口
 * 负责将具体格式的文件解析为通用的OutlineDocument数据模型
 * 
 * @author Leo
 * @since 2025/8/1
 */
public interface FileParser {
    
    /**
     * 从文件解析为大纲文档
     * 
     * @param file 输入文件
     * @return 解析后的大纲文档
     * @throws ParsingException 解析异常
     */
    OutlineDocument parse(File file);
    
    /**
     * 从输入流解析为大纲文档
     * 
     * @param inputStream 输入流
     * @param filename 文件名（用于错误提示和格式推断）
     * @return 解析后的大纲文档
     * @throws ParsingException 解析异常
     */
    OutlineDocument parse(InputStream inputStream, String filename);
    
    /**
     * 从字节数组解析为大纲文档
     * 
     * @param data 字节数组
     * @param filename 文件名（用于错误提示和格式推断）
     * @return 解析后的大纲文档
     * @throws ParsingException 解析异常
     */
    OutlineDocument parse(byte[] data, String filename);
    
    /**
     * 获取支持的文件扩展名列表
     * 
     * @return 支持的文件扩展名数组
     */
    String[] getSupportedExtensions();
    
    /**
     * 检查是否支持指定的文件
     * 
     * @param file 文件
     * @return 是否支持
     */
    default boolean supports(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String filename = file.getName().toLowerCase();
        for (String ext : getSupportedExtensions()) {
            if (filename.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否支持指定的文件名
     * 
     * @param filename 文件名
     * @return 是否支持
     */
    default boolean supports(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String name = filename.toLowerCase();
        for (String ext : getSupportedExtensions()) {
            if (name.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}