package dev.hc.convert.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import dev.hc.convert.FileType;
import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.factory.ConverterFactory;
import dev.hc.convert.factory.ParserFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.parser.FileParser;

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
         */
        public void convert(File inputFile, File outputFile) {
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
         */
        public File convert(File inputFile) {
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
         */
        public void convert(File inputFile, String outputPath) {
            convert(inputFile, new File(outputPath));
        }
        
        /**
         * 转换文件并写入输出流
         * 
         * @param inputFile 输入文件
         * @param outputStream 输出流
         */
        public void convertToStream(File inputFile, OutputStream outputStream) {
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
         */
        public byte[] convertToBytes(File inputFile) {
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
         */
        public void convert(InputStream inputStream, String filename, File outputFile) {
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
         */
        public void convert(byte[] data, String filename, File outputFile) {
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
            try {
                // 使用commons-lang3进行强健的参数验证
                Validate.notNull(inputFile, "Input file cannot be null");
                Validate.isTrue(inputFile.exists(), "File does not exist: %s", inputFile);
                Validate.isTrue(inputFile.isFile(), "Path is not a file: %s", inputFile.getAbsolutePath());
                Validate.isTrue(inputFile.canRead(), "File is not readable: %s", inputFile.getAbsolutePath());
                
                // 检查文件大小（防止处理过大文件导致内存溢出）
                long fileSize = FileUtils.sizeOf(inputFile);
                long maxSize = 100 * 1024 * 1024L; // 100MB限制
                Validate.isTrue(fileSize <= maxSize, 
                    "File too large: %d bytes (max: %d bytes)", fileSize, maxSize);
                
                // 验证文件名安全性
                String filename = inputFile.getName();
                Validate.isTrue(StringUtils.isNotBlank(filename), "File name cannot be blank");
                Validate.isTrue(!StringUtils.containsAny(filename, '\0', '\r', '\n'), 
                    "File name contains invalid characters");
                
            } catch (IllegalArgumentException e) {
                // 将Validate异常转换为我们的业务异常
                throw ConversionExceptionFactory.systemError("File validation failed: " + e.getMessage(), e);
            } catch (Exception e) {
                // 处理FileUtils.sizeOf可能抛出的IOException等异常
                throw ConversionExceptionFactory.fileReadError(inputFile, e);
            }
        }
        
        private void validateInputStream(InputStream inputStream) {
            try {
                // 使用commons-lang3进行输入流验证
                Validate.notNull(inputStream, "Input stream cannot be null");
                
                // 检查输入流是否已关闭（通过available()方法）
                try {
                    inputStream.available();
                } catch (IOException e) {
                    throw ConversionExceptionFactory.systemError("Input stream is closed or invalid", e);
                }
                
            } catch (IllegalArgumentException e) {
                throw ConversionExceptionFactory.systemError("Stream validation failed: " + e.getMessage(), e);
            }
        }

        private void validateData(byte[] data) {
            try {
                // 使用commons-lang3进行数据验证
                Validate.notNull(data, "Input data cannot be null");
                Validate.isTrue(data.length > 0, "Input data cannot be empty");

                // 检查数据大小防止内存溢出
                long maxSize = 50 * 1024 * 1024L; // 50MB内存限制
                Validate.isTrue(data.length <= maxSize,
                        "Data too large: %d bytes (max: %d bytes)", data.length, maxSize);

            } catch (IllegalArgumentException | NullPointerException e) {
                throw ConversionExceptionFactory.systemError("Data validation failed: " + e.getMessage(), e);
            }
        }
        
        private String getFileNameWithoutExtension(String filename) {
            try {
                // 使用commons-lang3进行安全的字符串处理
                String safeFilename = StringUtils.defaultIfBlank(filename, "untitled");
                
                // 清理文件名中的危险字符
                safeFilename = StringUtils.replaceChars(safeFilename, "\0\r\n\t", "____");
                
                // 使用commons-io安全地获取不含扩展名的文件名
                String baseName = FilenameUtils.getBaseName(safeFilename);
                
                // 确保结果不为空
                return StringUtils.defaultIfBlank(baseName, "untitled");
                
            } catch (Exception e) {
                // 如果处理失败，返回安全的默认值
                return "untitled";
            }
        }
    }
}