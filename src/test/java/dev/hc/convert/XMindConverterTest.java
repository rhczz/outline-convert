package dev.hc.convert;

import dev.hc.convert.converter.XMindConverter;
import dev.hc.convert.model.MarkdownNode;
import dev.hc.convert.parser.MarkdownParser;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * XMind转换器测试
 */
public class XMindConverterTest {

    @Test
    public void testMarkdownParsing() throws Exception {
        String markdown = """
                # 根节点
                
                ## 第一级
                
                ### 第二级
                
                #### 第三级
                
                ## 另一个第一级
                """;
        
        MarkdownParser parser = new MarkdownParser();
        MarkdownNode root = parser.parseContent(markdown);
        
        assertNotNull(root);
        assertEquals(1, root.getChildren().size());
        
        MarkdownNode firstChild = root.getChildren().get(0);
        assertEquals("根节点", firstChild.getTitle());
        assertEquals(2, firstChild.getChildren().size());
    }

    @Test
    public void testXMindConversion() throws Exception {
        String markdown = """
                # 测试思维导图
                
                ## 分支1
                
                ### 子分支1.1
                
                ### 子分支1.2
                
                ## 分支2
                
                ### 子分支2.1
                """;
        
        MarkdownParser parser = new MarkdownParser();
        MarkdownNode root = parser.parseContent(markdown);
        
        XMindConverter converter = new XMindConverter();
        Path outputPath = Paths.get("test-output.xmind");
        
        converter.convertToXMind(root, outputPath);
        
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
        
        // 清理测试文件
        // Files.deleteIfExists(outputPath);
    }

    @Test
    public void testSampleFileConversion() throws Exception {
        Path samplePath = Paths.get("sample.md");
        if (!Files.exists(samplePath)) {
            // 如果sample.md不存在，跳过测试
            return;
        }
        
        MarkdownParser parser = new MarkdownParser();
        MarkdownNode root = parser.parseFile(samplePath);
        
        assertNotNull(root);
        assertFalse(root.getChildren().isEmpty());
        
        XMindConverter converter = new XMindConverter();
        Path outputPath = Paths.get("sample-test.xmind");
        
        converter.convertToXMind(root, outputPath);
        
        assertTrue(Files.exists(outputPath));
        
        // 清理测试文件
        // Files.deleteIfExists(outputPath);
    }
}
