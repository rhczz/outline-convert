package dev.hc.convert.parser.impl;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;

import java.io.*;
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
            String content = readFileContent(file);
            return parseMarkdownContent(content, file.getName());
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) throws ParsingException {
        try {
            String content = readStreamContent(inputStream);
            return parseMarkdownContent(content, filename);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + filename, e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) throws ParsingException {
        try {
            String content = new String(data, StandardCharsets.UTF_8);
            return parseMarkdownContent(content, filename);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"md", "markdown"};
    }
    
    /**
     * 解析Markdown内容
     */
    private OutlineDocument parseMarkdownContent(String content, String filename) throws ParsingException {
        try {
            Document document = PARSER.parse(content);
            return buildOutlineDocument(document, filename);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse Markdown structure", e);
        }
    }
    
    /**
     * 构建大纲文档
     */
    private OutlineDocument buildOutlineDocument(Document mdDocument, String filename) {
        OutlineDocument outlineDoc = new OutlineDocument();
        outlineDoc.setSourceFormat("Markdown");
        
        // 使用栈来跟踪标题层级
        Stack<OutlineNode> nodeStack = new Stack<>();
        OutlineNode currentNode = null;
        
        for (Node child : mdDocument.getChildren()) {
            if (child instanceof Heading heading) {
                int level = heading.getLevel();
                String title = extractTextContent(heading);
                
                OutlineNode headingNode = new OutlineNode(title);
                headingNode.setAttribute("headingLevel", level);
                
                // 根据层级调整节点关系
                adjustNodeHierarchy(nodeStack, headingNode, level, outlineDoc);
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
                            currentNode.setContent(existingContent + "\n\n" + content);
                        }
                    } else {
                        // 没有标题时，创建默认节点
                        OutlineNode contentNode = new OutlineNode("Content");
                        contentNode.setContent(content);
                        outlineDoc.addRootNode(contentNode);
                    }
                }
            }
        }
        
        // 如果没有解析出任何节点，创建一个默认节点
        if (outlineDoc.isEmpty()) {
            String allContent = extractTextContent(mdDocument);
            if (!allContent.trim().isEmpty()) {
                outlineDoc.addRootNode(new OutlineNode("Document", allContent));
            }
        }
        
        return outlineDoc;
    }
    
    /**
     * 调整节点层级关系
     */
    private void adjustNodeHierarchy(Stack<OutlineNode> nodeStack, OutlineNode newNode, 
                                   int level, OutlineDocument document) {
        // 清理栈中层级大于等于当前层级的节点
        while (!nodeStack.isEmpty()) {
            OutlineNode stackTop = nodeStack.peek();
            Integer stackLevel = stackTop.getAttribute("headingLevel", 1);
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
            return "";
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
            content.append(((Text) node).getChars());
        } else if (node instanceof Code) {
            content.append("`").append(((Code) node).getChars()).append("`");
        } else if (node instanceof Emphasis) {
            content.append("*").append(extractTextContent(node)).append("*");
        } else if (node instanceof StrongEmphasis) {
            content.append("**").append(extractTextContent(node)).append("**");
        } else if (node instanceof Link link) {
            content.append("[").append(extractTextContent(node)).append("](")
                   .append(link.getUrl()).append(")");
        } else if (node instanceof FencedCodeBlock codeBlock) {
            content.append("```");
            if (codeBlock.getInfo() != null && !codeBlock.getInfo().isEmpty()) {
                content.append(codeBlock.getInfo());
            }
            content.append("\n").append(codeBlock.getContentChars()).append("\n```");
        } else if (node instanceof BulletList || node instanceof OrderedList) {
            handleListNode(node, content, 0);
        } else if (node instanceof BlockQuote) {
            String blockContent = extractTextContent(node);
            if (!blockContent.isEmpty()) {
                content.append("> ").append(blockContent.replace("\n", "\n> "));
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
                content.append("  ".repeat(Math.max(0, indent)));
                
                // 添加列表标记
                if (isOrdered) {
                    content.append(itemIndex++).append(". ");
                } else {
                    content.append("- ");
                }
                
                // 提取列表项内容
                String itemContent = extractTextContent(child);
                content.append(itemContent);
                
                // 处理嵌套列表
                for (Node grandChild : child.getChildren()) {
                    if (grandChild instanceof BulletList || grandChild instanceof OrderedList) {
                        content.append("\n");
                        handleListNode(grandChild, content, indent + 1);
                    }
                }
                
                content.append("\n");
            }
        }
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
    
    /**
     * 读取流内容
     */
    private String readStreamContent(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}