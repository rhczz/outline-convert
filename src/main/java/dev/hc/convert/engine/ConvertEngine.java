package dev.hc.convert.engine;

import dev.hc.convert.FileType;
import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ConversionFailureException;
import dev.hc.convert.factory.ConverterFactory;
import dev.hc.convert.factory.ParserFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.parser.FileParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件格式转换引擎
 * 提供流畅的API设计，支持多种输入输出方式
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class ConvertEngine {
    
    /** 私有构造器，防止直接实例化 */
    private ConvertEngine() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * 创建转换构建器，指定源格式
     * 
     * @param sourceType 源文件格式
     * @return 转换构建器
     */
    public static FromBuilder from(FileType sourceType) {
        return new FromBuilder(sourceType);
    }
    
    /**
     * 自动检测文件格式并创建转换构建器
     * 
     * @param sourceFile 源文件
     * @return 转换构建器
     */
    public static FromBuilder from(File sourceFile) {
        FileType sourceType = FileType.fromFile(sourceFile);
        return new FromBuilder(sourceType);
    }
    
    /**
     * 自动检测文件格式并创建转换构建器
     * 
     * @param filename 文件名
     * @return 转换构建器
     */
    public static FromBuilder from(String filename) {
        FileType sourceType = FileType.fromFilename(filename);
        return new FromBuilder(sourceType);
    }
    
    /**
     * 源格式构建器
     */
    public static class FromBuilder {
        private final FileType sourceType;
        
        private FromBuilder(FileType sourceType) {
            this.sourceType = sourceType;
        }
        
        /**
         * 指定目标格式
         * 
         * @param targetType 目标格式
         * @return 转换构建器
         */
        public Builder to(FileType targetType) {
            return new Builder(sourceType, targetType);
        }
        
        /**
         * 转换为JSON格式
         */
        public Builder toJson() {
            return to(FileType.JSON);
        }
        
        /**
         * 转换为Markdown格式
         */
        public Builder toMarkdown() {
            return to(FileType.MARKDOWN);
        }
        
        /**
         * 转换为XMind格式
         */
        public Builder toXMind() {
            return to(FileType.XMIND);
        }
        
        /**
         * 转换为OPML格式
         */
        public Builder toOpml() {
            return to(FileType.OPML);
        }
    }
    
    /**
     * 转换构建器
     */
    public static class Builder {
        private final FileType sourceType;
        private final FileType targetType;
        
        private Builder(FileType sourceType, FileType targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
            
            if (sourceType == targetType) {
                throw ConversionExceptionFactory.systemError(
                    "Source format and target format cannot be the same: " + sourceType.getDisplayName(), null);
            }
        }
        
        /**
         * 转换文件并保存到指定路径
         * 
         * @param inputFile 输入文件
         * @param outputFile 输出文件
         * @throws ConversionFailureException 转换失败异常
         */
        public void convert(File inputFile, File outputFile) throws ConversionFailureException {
            validateInputFile(inputFile);
            
            try {
                // 解析输入文件
                FileParser parser = ParserFactory.getParser(sourceType);
                OutlineDocument document = parser.parse(inputFile);
                
                // 转换为目标格式
                FormatConverter converter = ConverterFactory.getConverter(targetType);
                converter.convert(document, outputFile);
                
            } catch (Exception e) {
                throw ConversionExceptionFactory.conversionFailed(
                                String.format("Failed to convert from %s to %s",
                sourceType.getDisplayName(), targetType.getDisplayName()), e);
            }
        }
        
        /**
         * 转换文件并保存到同目录下，使用目标格式的默认扩展名
         * 
         * @param inputFile 输入文件
         * @return 输出文件
         * @throws ConversionFailureException 转换失败异常
         */
        public File convert(File inputFile) throws ConversionFailureException {
            validateInputFile(inputFile);
            
            // 生成输出文件名
            String baseName = getFileNameWithoutExtension(inputFile.getName());
            String outputFileName = baseName + "." + targetType.getDefaultExtension();
            File outputFile = new File(inputFile.getParent(), outputFileName);
            
            convert(inputFile, outputFile);
            return outputFile;
        }
        
        /**
         * 转换文件并输出到指定路径字符串
         * 
         * @param inputFile 输入文件
         * @param outputPath 输出路径
         * @throws ConversionFailureException 转换失败异常
         */
        public void convert(File inputFile, String outputPath) throws ConversionFailureException {
            convert(inputFile, new File(outputPath));
        }
        
        /**
         * 转换文件并写入输出流
         * 
         * @param inputFile 输入文件
         * @param outputStream 输出流
         * @throws ConversionFailureException 转换失败异常
         */
        public void convertToStream(File inputFile, OutputStream outputStream) throws ConversionFailureException {
            validateInputFile(inputFile);
            
            try {
                // 解析输入文件
                FileParser parser = ParserFactory.getParser(sourceType);
                OutlineDocument document = parser.parse(inputFile);
                
                // 转换为目标格式
                FormatConverter converter = ConverterFactory.getConverter(targetType);
                converter.convert(document, outputStream);
                
            } catch (Exception e) {
                throw ConversionExceptionFactory.conversionFailed(
                                String.format("Failed to convert from %s to %s",
                sourceType.getDisplayName(), targetType.getDisplayName()), e);
            }
        }
        
        /**
         * 转换文件并返回字节数组
         * 
         * @param inputFile 输入文件
         * @return 转换后的字节数组
         * @throws ConversionFailureException 转换失败异常
         */
        public byte[] convertToBytes(File inputFile) throws ConversionFailureException {
            validateInputFile(inputFile);
            
            try {
                // 解析输入文件
                FileParser parser = ParserFactory.getParser(sourceType);
                OutlineDocument document = parser.parse(inputFile);
                
                // 转换为目标格式
                FormatConverter converter = ConverterFactory.getConverter(targetType);
                return converter.convertToBytes(document);
                
            } catch (Exception e) {
                throw ConversionExceptionFactory.conversionFailed(
                                String.format("Failed to convert from %s to %s",
                sourceType.getDisplayName(), targetType.getDisplayName()), e);
            }
        }
        
        /**
         * 从输入流转换并保存到文件
         * 
         * @param inputStream 输入流
         * @param filename 输入文件名（用于格式推断）
         * @param outputFile 输出文件
         * @throws ConversionFailureException 转换失败异常
         */
        public void convert(InputStream inputStream, String filename, File outputFile) throws ConversionFailureException {
            validateInputStream(inputStream);
            
            try {
                // 解析输入流
                FileParser parser = ParserFactory.getParser(sourceType);
                OutlineDocument document = parser.parse(inputStream, filename);
                
                // 转换为目标格式
                FormatConverter converter = ConverterFactory.getConverter(targetType);
                converter.convert(document, outputFile);
                
            } catch (Exception e) {
                throw ConversionExceptionFactory.conversionFailed(
                                String.format("Failed to convert from %s to %s",
                sourceType.getDisplayName(), targetType.getDisplayName()), e);
            }
        }
        
        /**
         * 从字节数组转换并保存到文件
         * 
         * @param data 输入数据
         * @param filename 输入文件名（用于格式推断）
         * @param outputFile 输出文件
         * @throws ConversionFailureException 转换失败异常
         */
        public void convert(byte[] data, String filename, File outputFile) throws ConversionFailureException {
            validateData(data);
            
            try {
                // 解析输入数据
                FileParser parser = ParserFactory.getParser(sourceType);
                OutlineDocument document = parser.parse(data, filename);
                
                // 转换为目标格式
                FormatConverter converter = ConverterFactory.getConverter(targetType);
                converter.convert(document, outputFile);
                
            } catch (Exception e) {
                throw ConversionExceptionFactory.conversionFailed(
                                String.format("Failed to convert from %s to %s",
                sourceType.getDisplayName(), targetType.getDisplayName()), e);
            }
        }
        
        /**
         * 获取源文件类型
         */
        public FileType getSourceType() {
            return sourceType;
        }
        
        /**
         * 获取目标文件类型
         */
        public FileType getTargetType() {
            return targetType;
        }

        private void validateInputFile(File inputFile) {
            if (inputFile == null) {
                throw ConversionExceptionFactory.systemError("Input file cannot be null", null);
            }
            if (!inputFile.exists()) {
                throw ConversionExceptionFactory.fileNotFound(inputFile);
            }
            if (!inputFile.isFile()) {
                throw ConversionExceptionFactory.systemError("Input path is not a file: " + inputFile.getAbsolutePath(), null);
            }
            if (!inputFile.canRead()) {
                throw ConversionExceptionFactory.fileReadError(inputFile, 
                    new IOException("File is not readable"));
            }
        }
        
        private void validateInputStream(InputStream inputStream) {
            if (inputStream == null) {
                throw ConversionExceptionFactory.systemError("Input stream cannot be null", null);
            }
        }
        
        private void validateData(byte[] data) {
            if (data == null || data.length == 0) {
                throw ConversionExceptionFactory.systemError("Input data cannot be null", null);
            }
        }
        
        private String getFileNameWithoutExtension(String filename) {
            if (filename == null || filename.isEmpty()) {
                return "untitled";
            }
            
            int lastDot = filename.lastIndexOf('.');
            if (lastDot > 0) {
                return filename.substring(0, lastDot);
            }
            return filename;
        }
    }
}