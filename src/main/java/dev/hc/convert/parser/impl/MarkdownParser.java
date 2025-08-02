package dev.hc.convert.parser.impl;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import dev.hc.convert.FileType;
import dev.hc.convert.constant.FileValid;
import dev.hc.convert.constant.MarkdownSyntax;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

/**
 * Markdown格式解析器
 * 支持标准Markdown语法解析为大纲文档
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class MarkdownParser implements FileParser {

    /**
     * 创建线程安全的Flexmark Parser实例
     * 根据Flexmark官方文档，Parser实例是线程安全的，
     * 但为了遵循一致性原则，我们仍然采用恒定配置方式
     */
    private static final Parser PARSER;
    
    static {
        MutableDataSet options = new MutableDataSet();
        PARSER = Parser.builder(options).build();
    }
    
    @Override
    public OutlineDocument parse(File file) throws ParsingException {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(file, "Input file cannot be null");
            Validate.isTrue(file.exists(), "File does not exist: %s", file);
            Validate.isTrue(file.isFile(), "Path is not a file: %s", file.getAbsolutePath());
            
            // 检查文件大小，防止处理过大文件
            long fileSize = FileUtils.sizeOf(file);
            Validate.isTrue(fileSize <= FileValid.MAX_FILE_SIZE,
                "Markdown file too large: %d bytes (max: %d bytes)", fileSize, FileValid.MAX_FILE_SIZE);
            
            // 使用commons-io安全读取文件
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            return parseMarkdownContent(content);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("Markdown file validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) throws ParsingException {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(inputStream, "Input stream cannot be null");
            
            // 使用commons-io安全读取流内容，防止内存溢出
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            
            // 验证内容长度
            Validate.isTrue(content.length() <= FileValid.MAX_FILE_SIZE,
                "Markdown content too large: %d characters (max: %d)", content.length(), FileValid.MAX_FILE_SIZE);

            return parseMarkdownContent(content);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("Markdown stream validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to read Markdown stream: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + filename, e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) throws ParsingException {
        try {
            // 使用commons-lang3进行数据验证
            Validate.notNull(data, "Input data cannot be null");
            Validate.isTrue(data.length > 0, "Input data cannot be empty");
            
            // 检查数据大小，防止内存溢出
            Validate.isTrue(data.length <= FileValid.MAX_FILE_SIZE,
                "Markdown data too large: %d bytes (max: %d bytes)", data.length, FileValid.MAX_FILE_SIZE);
            
            String content = new String(data, StandardCharsets.UTF_8);
            return parseMarkdownContent(content);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("Markdown data validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return FileType.MARKDOWN.getExtensions();
    }
    
    /**
     * 解析Markdown内容
     */
    private OutlineDocument parseMarkdownContent(String content) throws ParsingException {
        try {
            Document document = PARSER.parse(content);
            return buildOutlineDocument(document);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown structure", e);
        }
    }
    
    /**
     * 构建大纲文档
     */
    private OutlineDocument buildOutlineDocument(Document mdDocument) {
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat(FileType.MARKDOWN.getDisplayName());
        
        // 使用栈来跟踪标题层级
        Stack<OutlineNode> nodeStack = new Stack<>();
        OutlineNode currentNode = null;
        
        for (Node child : mdDocument.getChildren()) {
            if (child instanceof Heading heading) {
                int level = heading.getLevel();
                String title = extractTextContent(heading);
                
                OutlineNode headingNode = new OutlineNode(title);
                headingNode.setAttribute(MarkdownSyntax.HEADING_LEVEL_ATTRIBUTE, level);
                
                // 根据层级调整节点关系
                adjustNodeHierarchy(nodeStack, headingNode, level, document);
                currentNode = headingNode;
                
            } else if (child instanceof Paragraph || child instanceof FencedCodeBlock || 
                      child instanceof BulletList || child instanceof OrderedList ||
                      child instanceof BlockQuote) {
                
                String content = extractTextContent(child);
                if (!content.trim().isEmpty()) {
                    if (currentNode != null) {
                        // 将内容添加到当前标题节点
                        String existingContent = currentNode.getContent();
                        if (existingContent == null || existingContent.isEmpty()) {
                            currentNode.setContent(content);
                        } else {
                            currentNode.setContent(existingContent + MarkdownSyntax.PARAGRAPH_BREAK + content);
                        }
                    } else {
                        // 没有标题时，创建默认节点
                        OutlineNode contentNode = new OutlineNode(MarkdownSyntax.CONTENT_NODE_TITLE);
                        contentNode.setContent(content);
                        document.addRootNode(contentNode);
                    }
                }
            }
        }
        
        // 如果没有解析出任何节点，创建一个默认节点
        if (document.isEmpty()) {
            String allContent = extractTextContent(mdDocument);
            if (!allContent.trim().isEmpty()) {
                document.addRootNode(new OutlineNode(MarkdownSyntax.DOCUMENT_NODE_TITLE, allContent));
            }
        }
        
        return document;
    }
    
    /**
     * 调整节点层级关系
     */
    private void adjustNodeHierarchy(Stack<OutlineNode> nodeStack, OutlineNode newNode, int level, OutlineDocument document) {
        // 清理栈中层级大于等于当前层级的节点
        while (!nodeStack.isEmpty()) {
            OutlineNode stackTop = nodeStack.peek();
            Integer stackLevel = stackTop.getAttribute(MarkdownSyntax.HEADING_LEVEL_ATTRIBUTE, 1);
            if (stackLevel >= level) {
                nodeStack.pop();
            } else {
                break;
            }
        }
        
        if (nodeStack.isEmpty() || level == 1) {
            // 作为根节点添加
            document.addRootNode(newNode);
        } else {
            // 作为子节点添加到栈顶节点
            OutlineNode parent = nodeStack.peek();
            parent.addChild(newNode);
        }
        
        // 将新节点压入栈
        nodeStack.push(newNode);
    }
    
    /**
     * 提取节点的文本内容
     */
    private String extractTextContent(Node node) {
        if (node == null) {
            return StringUtils.EMPTY;
        }
        
        StringBuilder content = new StringBuilder();
        extractTextRecursive(node, content);
        return content.toString().trim();
    }
    
    /**
     * 递归提取文本内容
     */
    private void extractTextRecursive(Node node, StringBuilder content) {
        if (node instanceof Text) {
            content.append(node.getChars());
        } else if (node instanceof Code) {
            content.append(MarkdownSyntax.INLINE_CODE)
                    .append(node.getChars())
                    .append(MarkdownSyntax.INLINE_CODE);
        } else if (node instanceof Emphasis) {
            content.append(MarkdownSyntax.EMPHASIS_ASTERISK)
                    .append(extractTextContent(node))
                    .append(MarkdownSyntax.EMPHASIS_ASTERISK);
        } else if (node instanceof StrongEmphasis) {
            content.append(MarkdownSyntax.STRONG_EMPHASIS_ASTERISK)
                    .append(extractTextContent(node))
                    .append(MarkdownSyntax.STRONG_EMPHASIS_ASTERISK);
        } else if (node instanceof Link link) {
            content.append(MarkdownSyntax.LINK_TEXT_START)
                    .append(extractTextContent(node))
                    .append(MarkdownSyntax.LINK_TEXT_END)
                   .append(MarkdownSyntax.LINK_URL_START)
                    .append(link.getUrl())
                    .append(MarkdownSyntax.LINK_URL_END);
        } else if (node instanceof FencedCodeBlock codeBlock) {
            content.append(MarkdownSyntax.CODE_BLOCK_BACKTICK);
            if (codeBlock.getInfo() != null && !codeBlock.getInfo().isEmpty()) {
                content.append(codeBlock.getInfo());
            }
            content.append(MarkdownSyntax.NEWLINE)
                    .append(codeBlock.getContentChars())
                    .append(MarkdownSyntax.NEWLINE)
                    .append(MarkdownSyntax.CODE_BLOCK_BACKTICK);
        } else if (node instanceof BulletList || node instanceof OrderedList) {
            handleListNode(node, content, 0);
        } else if (node instanceof BlockQuote) {
            String blockContent = extractTextContent(node);
            if (!blockContent.isEmpty()) {
                content.append(MarkdownSyntax.BLOCKQUOTE)
                        .append(blockContent.replace(MarkdownSyntax.NEWLINE, MarkdownSyntax.NEWLINE + MarkdownSyntax.BLOCKQUOTE));
            }
        } else {
            // 递归处理子节点
            for (Node child : node.getChildren()) {
                extractTextRecursive(child, content);
            }
        }
    }
    
    /**
     * 处理列表节点
     */
    private void handleListNode(Node listNode, StringBuilder content, int indent) {
        boolean isOrdered = listNode instanceof OrderedList;
        int itemIndex = 1;
        
        for (Node child : listNode.getChildren()) {
            if (child instanceof BulletListItem || child instanceof OrderedListItem) {
                // 添加缩进
                content.append((MarkdownSyntax.SPACE + MarkdownSyntax.SPACE).repeat(Math.max(0, indent)));
                
                // 添加列表标记
                if (isOrdered) {
                    content.append(itemIndex++).append(MarkdownSyntax.ORDERED_LIST_DOT);
                } else {
                    content.append(MarkdownSyntax.UNORDERED_LIST_DASH);
                }
                
                // 提取列表项内容
                String itemContent = extractTextContent(child);
                content.append(itemContent);
                
                // 处理嵌套列表
                for (Node grandChild : child.getChildren()) {
                    if (grandChild instanceof BulletList || grandChild instanceof OrderedList) {
                        content.append(MarkdownSyntax.NEWLINE);
                        handleListNode(grandChild, content, indent + 1);
                    }
                }
                
                content.append(MarkdownSyntax.NEWLINE);
            }
        }
    }

}