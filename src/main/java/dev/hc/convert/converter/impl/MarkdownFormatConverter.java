package dev.hc.convert.converter.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;

/**
 * Markdown格式转换器
 * 将大纲文档转换为Markdown格式输出
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class MarkdownFormatConverter implements FormatConverter {
    
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    @Override
    public void convert(OutlineDocument document, File outputFile) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            
            String markdownContent = convertToMarkdown(document);
            writer.write(markdownContent);
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileWriteError(outputFile, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to Markdown", e);
        }
    }
    
    @Override
    public void convert(OutlineDocument document, OutputStream outputStream) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            String markdownContent = convertToMarkdown(document);
            writer.write(markdownContent);
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to write Markdown to stream", e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to Markdown", e);
        }
    }
    
    @Override
    public byte[] convertToBytes(OutlineDocument document) {
        try {
            String markdownContent = convertToMarkdown(document);
            return markdownContent.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to Markdown", e);
        }
    }
    
    @Override
    public String getFormatName() {
        return "Markdown";
    }
    
    @Override
    public String getDefaultExtension() {
        return "md";
    }
    
    @Override
    public String getMimeType() {
        return "text/markdown";
    }
    
    /**
     * 将大纲文档转换为Markdown字符串
     */
    private String convertToMarkdown(OutlineDocument document) {
        StringBuilder markdown = new StringBuilder();
        
        // 添加文档标题
        if (document.getTitle() != null && !document.getTitle().trim().isEmpty()) {
            markdown.append("# ").append(document.getTitle()).append(LINE_SEPARATOR);
            markdown.append(LINE_SEPARATOR);
        }
        
        // 添加文档描述
        if (document.getDescription() != null && !document.getDescription().trim().isEmpty()) {
            markdown.append(document.getDescription()).append(LINE_SEPARATOR);
            markdown.append(LINE_SEPARATOR);
        }
        
        // 转换根节点
        for (OutlineNode rootNode : document.getRootNodes()) {
            convertNodeToMarkdown(rootNode, markdown, 1);
        }
        
        // 移除末尾多余的空行
        String result = markdown.toString();
        while (result.endsWith(LINE_SEPARATOR + LINE_SEPARATOR + LINE_SEPARATOR)) {
            result = result.substring(0, result.length() - LINE_SEPARATOR.length());
        }
        
        return result;
    }
    
    /**
     * 将大纲节点转换为Markdown
     */
    private void convertNodeToMarkdown(OutlineNode node, StringBuilder markdown, int level) {
        // 添加标题
        if (node.getTitle() != null && !node.getTitle().trim().isEmpty()) {
            // 根据节点的原始标题级别或使用传入的级别
            Integer originalLevel = node.getAttribute("headingLevel");
            int headingLevel = originalLevel != null ? originalLevel : Math.min(level, 6);
            
            // Markdown最多支持6级标题
            headingLevel = Math.min(headingLevel, 6);

            markdown.append("#".repeat(Math.max(0, headingLevel)));
            markdown.append(" ").append(node.getTitle()).append(LINE_SEPARATOR);
            markdown.append(LINE_SEPARATOR);
        }
        
        // 添加内容
        if (node.getContent() != null && !node.getContent().trim().isEmpty()) {
            String content = node.getContent().trim();
            
            // 处理内容中的特殊格式
            content = processContentFormatting(content);
            
            markdown.append(content).append(LINE_SEPARATOR);
            markdown.append(LINE_SEPARATOR);
        }
        
        // 递归处理子节点
        for (OutlineNode child : node.getChildren()) {
            convertNodeToMarkdown(child, markdown, level + 1);
        }
    }
    
    /**
     * 处理内容格式
     */
    private String processContentFormatting(String content) {
        // 如果内容已经包含Markdown格式，保持原样
        if (containsMarkdownFormatting(content)) {
            return content;
        }
        
        // 处理多行内容，确保段落间有适当的空行
        String[] lines = content.split("\\r?\\n");
        StringBuilder formatted = new StringBuilder();
        
        boolean inCodeBlock = false;
        boolean inList = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();
            
            // 检测代码块
            if (trimmedLine.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                formatted.append(line).append(LINE_SEPARATOR);
                continue;
            }
            
            // 在代码块内部，保持原格式
            if (inCodeBlock) {
                formatted.append(line).append(LINE_SEPARATOR);
                continue;
            }
            
            // 检测列表项
            boolean isListItem = trimmedLine.matches("^[-*+]\\s+.*") || 
                                trimmedLine.matches("^\\d+\\.\\s+.*");
            
            if (isListItem && !inList) {
                // 列表开始前添加空行
                if (!formatted.isEmpty() &&
                    !formatted.toString().endsWith(LINE_SEPARATOR + LINE_SEPARATOR)) {
                    formatted.append(LINE_SEPARATOR);
                }
                inList = true;
            } else if (!isListItem && inList) {
                // 列表结束后添加空行
                inList = false;
                if (!formatted.isEmpty()) {
                    formatted.append(LINE_SEPARATOR);
                }
            }
            
            formatted.append(line);
            
            // 添加行分隔符
            if (i < lines.length - 1) {
                formatted.append(LINE_SEPARATOR);
                
                // 在段落之间添加额外的空行
                if (!inList && !trimmedLine.isEmpty()
                        && i + 1 < lines.length
                        && !lines[i + 1].trim().isEmpty()
                        && !lines[i + 1].trim().startsWith(">")) {
                    
                    // 检查下一行是否是列表或标题
                    String nextLine = lines[i + 1].trim();
                    if (!nextLine.matches("^[-*+]\\s+.*") && 
                        !nextLine.matches("^\\d+\\.\\s+.*") &&
                        !nextLine.startsWith("#")) {
                        formatted.append(LINE_SEPARATOR);
                    }
                }
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * 检查内容是否已包含Markdown格式
     */
    private boolean containsMarkdownFormatting(String content) {
        // 检查常见的Markdown格式标记
        return content.contains("**") ||  // 粗体
               content.contains("*") ||   // 斜体
               content.contains("`") ||   // 代码
               content.contains("[") ||   // 链接
               content.contains("![") ||  // 图片
               content.contains("```") || // 代码块
               content.contains("> ") ||  // 引用
               content.matches(".*^[-*+]\\s+.*") || // 无序列表
               content.matches(".*^\\d+\\.\\s+.*"); // 有序列表
    }
}