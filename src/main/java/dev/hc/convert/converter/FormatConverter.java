package dev.hc.convert.converter;

import java.io.File;
import java.io.OutputStream;

import dev.hc.convert.exception.ConvertException;
import dev.hc.convert.model.OutlineDocument;

/**
 * 格式转换器接口
 * 负责将通用的OutlineDocument数据模型转换为具体格式的输出
 * 
 * @author Leo
 * @since 2025/8/1
 */
public interface FormatConverter {
    
    /**
     * 将大纲文档转换并保存到文件
     * 
     * @param document 大纲文档
     * @param outputFile 输出文件
     * @throws ConvertException 转换异常
     */
    void convert(OutlineDocument document, File outputFile);
    
    /**
     * 将大纲文档转换并写入输出流
     * 
     * @param document 大纲文档
     * @param outputStream 输出流
     * @throws ConvertException 转换异常
     */
    void convert(OutlineDocument document, OutputStream outputStream);
    
    /**
     * 将大纲文档转换为字节数组
     * 
     * @param document 大纲文档
     * @return 转换后的字节数组
     * @throws ConvertException 转换异常
     */
    byte[] convertToBytes(OutlineDocument document);
    
    /**
     * 获取输出格式名称
     * 
     * @return 格式名称
     */
    String getFormatName();
    
    /**
     * 获取默认文件扩展名
     * 
     * @return 文件扩展名（不包含点号）
     */
    String getDefaultExtension();
    
    /**
     * 获取MIME类型
     * 
     * @return MIME类型字符串
     */
    String getMimeType();
    
    /**
     * 生成默认的输出文件名
     * 
     * @param baseName 基础文件名（不包含扩展名）
     * @return 包含扩展名的完整文件名
     */
    default String generateFileName(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "untitled";
        }
        
        // 移除原有扩展名
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = baseName.substring(0, lastDot);
        }
        
        return baseName + "." + getDefaultExtension();
    }
}