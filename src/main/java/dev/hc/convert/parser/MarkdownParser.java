package dev.hc.convert.parser;

import dev.hc.convert.model.MarkdownNode;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

/**
 * Markdown解析器
 */
public class MarkdownParser {

    private final Parser parser;

    public MarkdownParser() {
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
    }

    /**
     * 从文件解析Markdown
     */
    public MarkdownNode parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return parseContent(content);
    }

    /**
     * 从字符串解析Markdown
     */
    public MarkdownNode parseContent(String markdownContent) {
        Node document = parser.parse(markdownContent);
        
        MarkdownNode root = new MarkdownNode("Root", 0);
        Stack<MarkdownNode> stack = new Stack<>();
        stack.push(root);

        // 遍历AST节点
        for (Node child : document.getChildren()) {
            if (child instanceof Heading heading) {
                String title = heading.getText().toString().trim();
                int level = heading.getLevel();

                MarkdownNode node = new MarkdownNode(title, level);

                // 找到合适的父节点
                while (!stack.isEmpty() && stack.peek().getLevel() >= level) {
                    stack.pop();
                }

                if (!stack.isEmpty()) {
                    stack.peek().addChild(node);
                }
                
                stack.push(node);
            }
        }

        return root;
    }

    /**
     * 打印树结构（用于调试）
     */
    public void printTree(MarkdownNode node, String indent) {
        if (node.getLevel() > 0) {
            System.out.println(indent + node.getTitle() + " (Level " + node.getLevel() + ")");
        }
        
        for (MarkdownNode child : node.getChildren()) {
            printTree(child, indent + "  ");
        }
    }
}
