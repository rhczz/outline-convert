package dev.hc.convert;

import dev.hc.convert.engine.ConvertEngine;
import dev.hc.convert.exception.ConversionFailureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileType 单元测试
 */
@DisplayName("文件类型测试")
class FileTypeTest {

    @Nested
    @DisplayName("枚举常量测试")
    class EnumConstantsTests {

        @Test
        @DisplayName("JSON文件类型属性")
        void testJsonFileType() {
            assertEquals("json", FileType.JSON.getDefaultExtension());
            assertEquals("application/json", FileType.JSON.getMimeType());
            assertEquals("JSON", FileType.JSON.getDisplayName());
            assertEquals("untitled.json", FileType.JSON.getDefaultFileName());
            assertArrayEquals(new String[]{"json"}, FileType.JSON.getExtensions());
        }

        @Test
        @DisplayName("Markdown文件类型属性")
        void testMarkdownFileType() {
            assertEquals("md", FileType.MARKDOWN.getDefaultExtension());
            assertEquals("text/markdown", FileType.MARKDOWN.getMimeType());
            assertEquals("Markdown", FileType.MARKDOWN.getDisplayName());
            assertEquals("untitled.md", FileType.MARKDOWN.getDefaultFileName());
            assertArrayEquals(new String[]{"md", "markdown"}, FileType.MARKDOWN.getExtensions());
        }

        @Test
        @DisplayName("XMind文件类型属性")
        void testXMindFileType() {
            assertEquals("xmind", FileType.XMIND.getDefaultExtension());
            assertEquals("application/xmind", FileType.XMIND.getMimeType());
            assertEquals("XMind", FileType.XMIND.getDisplayName());
            assertEquals("untitled.xmind", FileType.XMIND.getDefaultFileName());
            assertArrayEquals(new String[]{"xmind"}, FileType.XMIND.getExtensions());
        }

        @Test
        @DisplayName("OPML文件类型属性")
        void testOpmlFileType() {
            assertEquals("opml", FileType.OPML.getDefaultExtension());
            assertEquals("text/x-opml", FileType.OPML.getMimeType());
            assertEquals("OPML", FileType.OPML.getDisplayName());
            assertEquals("untitled.opml", FileType.OPML.getDefaultFileName());
            assertArrayEquals(new String[]{"opml"}, FileType.OPML.getExtensions());
        }
    }

    @Nested
    @DisplayName("扩展名支持测试")
    class ExtensionSupportTests {

        @Test
        @DisplayName("JSON支持的扩展名")
        void testJsonSupportedExtensions() {
            assertTrue(FileType.JSON.supportsExtension("json"));
            assertTrue(FileType.JSON.supportsExtension("JSON"));
            assertTrue(FileType.JSON.supportsExtension(".json"));
            assertTrue(FileType.JSON.supportsExtension(".JSON"));
            assertFalse(FileType.JSON.supportsExtension("md"));
            assertFalse(FileType.JSON.supportsExtension("txt"));
        }

        @Test
        @DisplayName("Markdown支持的扩展名")
        void testMarkdownSupportedExtensions() {
            assertTrue(FileType.MARKDOWN.supportsExtension("md"));
            assertTrue(FileType.MARKDOWN.supportsExtension("markdown"));
            assertTrue(FileType.MARKDOWN.supportsExtension("MD"));
            assertTrue(FileType.MARKDOWN.supportsExtension("MARKDOWN"));
            assertTrue(FileType.MARKDOWN.supportsExtension(".md"));
            assertTrue(FileType.MARKDOWN.supportsExtension(".markdown"));
            assertFalse(FileType.MARKDOWN.supportsExtension("json"));
            assertFalse(FileType.MARKDOWN.supportsExtension("txt"));
        }

        @Test
        @DisplayName("XMind支持的扩展名")
        void testXMindSupportedExtensions() {
            assertTrue(FileType.XMIND.supportsExtension("xmind"));
            assertTrue(FileType.XMIND.supportsExtension("XMIND"));
            assertTrue(FileType.XMIND.supportsExtension(".xmind"));
            assertFalse(FileType.XMIND.supportsExtension("json"));
        }

        @Test
        @DisplayName("OPML支持的扩展名")
        void testOpmlSupportedExtensions() {
            assertTrue(FileType.OPML.supportsExtension("opml"));
            assertTrue(FileType.OPML.supportsExtension("OPML"));
            assertTrue(FileType.OPML.supportsExtension(".opml"));
            assertFalse(FileType.OPML.supportsExtension("xml"));
        }

        @Test
        @DisplayName("null扩展名应该返回false")
        void testNullExtension() {
            assertFalse(FileType.JSON.supportsExtension(null));
            assertFalse(FileType.MARKDOWN.supportsExtension(null));
            assertFalse(FileType.XMIND.supportsExtension(null));
            assertFalse(FileType.OPML.supportsExtension(null));
        }

        @Test
        @DisplayName("空字符串扩展名应该返回false")
        void testEmptyExtension() {
            assertFalse(FileType.JSON.supportsExtension(""));
            assertFalse(FileType.MARKDOWN.supportsExtension(""));
        }
    }

    @Nested
    @DisplayName("文件类型推断测试")
    class FileTypeInferenceTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("从文件名推断JSON类型")
        void testFromFilenameJson() {
            assertEquals(FileType.JSON, FileType.fromFilename("test.json"));
            assertEquals(FileType.JSON, FileType.fromFilename("TEST.JSON"));
            assertEquals(FileType.JSON, FileType.fromFilename("file.with.dots.json"));
        }

        @Test
        @DisplayName("从文件名推断Markdown类型")
        void testFromFilenameMarkdown() {
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("test.md"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("test.markdown"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("TEST.MD"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("TEST.MARKDOWN"));
        }

        @Test
        @DisplayName("从文件名推断XMind类型")
        void testFromFilenameXMind() {
            assertEquals(FileType.XMIND, FileType.fromFilename("test.xmind"));
            assertEquals(FileType.XMIND, FileType.fromFilename("TEST.XMIND"));
        }

        @Test
        @DisplayName("从文件名推断OPML类型")
        void testFromFilenameOpml() {
            assertEquals(FileType.OPML, FileType.fromFilename("test.opml"));
            assertEquals(FileType.OPML, FileType.fromFilename("TEST.OPML"));
        }

        @Test
        @DisplayName("null文件名应该抛出异常")
        void testFromFilenameNull() {
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFilename(null);
            });
        }

        @Test
        @DisplayName("空文件名应该抛出异常")
        void testFromFilenameEmpty() {
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFilename("");
            });
            
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFilename("   ");
            });
        }

        @Test
        @DisplayName("不支持的文件扩展名应该抛出异常")
        void testFromFilenameUnsupported() {
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFilename("test.txt");
            });
            
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFilename("test.doc");
            });
        }

        @Test
        @DisplayName("从文件推断类型")
        void testFromFile() throws IOException {
            File jsonFile = tempDir.resolve("test.json").toFile();
            assertTrue(jsonFile.createNewFile());
            
            assertEquals(FileType.JSON, FileType.fromFile(jsonFile));
        }

        @Test
        @DisplayName("null文件应该抛出异常")
        void testFromFileNull() {
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFile(null);
            });
        }

        @Test
        @DisplayName("不存在的文件应该抛出异常")
        void testFromFileNotExists() {
            File nonExistentFile = new File("nonexistent.json");
            
            assertThrows(ConversionFailureException.class, () -> {
                FileType.fromFile(nonExistentFile);
            });
        }
    }

    @Nested
    @DisplayName("转换器创建测试")
    class ConverterCreationTests {

        @Test
        @DisplayName("创建到JSON的转换器")
        void testToJson() {
            ConvertEngine.Builder builder = FileType.MARKDOWN.toJson();
            
            assertNotNull(builder);
            assertEquals(FileType.MARKDOWN, builder.getSourceType());
            assertEquals(FileType.JSON, builder.getTargetType());
        }

        @Test
        @DisplayName("创建到Markdown的转换器")
        void testToMarkdown() {
            ConvertEngine.Builder builder = FileType.JSON.toMarkdown();
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("创建到XMind的转换器")
        void testToXMind() {
            ConvertEngine.Builder builder = FileType.MARKDOWN.toXMind();
            
            assertNotNull(builder);
            assertEquals(FileType.MARKDOWN, builder.getSourceType());
            assertEquals(FileType.XMIND, builder.getTargetType());
        }

        @Test
        @DisplayName("创建到OPML的转换器")
        void testToOpml() {
            ConvertEngine.Builder builder = FileType.JSON.toOpml();
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.OPML, builder.getTargetType());
        }

        @Test
        @DisplayName("创建任意格式转换器")
        void testToArbitraryFormat() {
            ConvertEngine.Builder builder = FileType.JSON.to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("JSON的toString格式")
        void testJsonToString() {
            String str = FileType.JSON.toString();
            assertEquals("JSON (*.json)", str);
        }

        @Test
        @DisplayName("Markdown的toString格式")
        void testMarkdownToString() {
            String str = FileType.MARKDOWN.toString();
            assertEquals("Markdown (*.md)", str);
        }

        @Test
        @DisplayName("XMind的toString格式")
        void testXMindToString() {
            String str = FileType.XMIND.toString();
            assertEquals("XMind (*.xmind)", str);
        }

        @Test
        @DisplayName("OPML的toString格式")
        void testOpmlToString() {
            String str = FileType.OPML.toString();
            assertEquals("OPML (*.opml)", str);
        }
    }

    @Nested
    @DisplayName("枚举方法测试")
    class EnumMethodsTests {

        @Test
        @DisplayName("values方法应该返回所有枚举值")
        void testValues() {
            FileType[] values = FileType.values();
            
            assertEquals(4, values.length);
            assertTrue(java.util.Arrays.asList(values).contains(FileType.JSON));
            assertTrue(java.util.Arrays.asList(values).contains(FileType.MARKDOWN));
            assertTrue(java.util.Arrays.asList(values).contains(FileType.XMIND));
            assertTrue(java.util.Arrays.asList(values).contains(FileType.OPML));
        }

        @Test
        @DisplayName("valueOf方法应该正确返回枚举值")
        void testValueOf() {
            assertEquals(FileType.JSON, FileType.valueOf("JSON"));
            assertEquals(FileType.MARKDOWN, FileType.valueOf("MARKDOWN"));
            assertEquals(FileType.XMIND, FileType.valueOf("XMIND"));
            assertEquals(FileType.OPML, FileType.valueOf("OPML"));
        }

        @Test
        @DisplayName("valueOf不存在的值应该抛出异常")
        void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> {
                FileType.valueOf("INVALID");
            });
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("带多个点的文件名")
        void testFileNameWithMultipleDots() {
            assertEquals(FileType.JSON, FileType.fromFilename("file.name.with.dots.json"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("readme.zh.md"));
        }

        @Test
        @DisplayName("只有扩展名的文件")
        void testExtensionOnlyFile() {
            assertEquals(FileType.JSON, FileType.fromFilename(".json"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename(".md"));
        }

        @Test
        @DisplayName("大小写混合的扩展名")
        void testMixedCaseExtension() {
            assertEquals(FileType.JSON, FileType.fromFilename("test.Json"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("test.Md"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("test.MarkDown"));
        }

        @Test
        @DisplayName("长文件名")
        void testLongFileName() {
            String longName = "a".repeat(100) + ".json";
            assertEquals(FileType.JSON, FileType.fromFilename(longName));
        }

        @Test
        @DisplayName("中文文件名")
        void testChineseFileName() {
            assertEquals(FileType.JSON, FileType.fromFilename("中文文件名.json"));
            assertEquals(FileType.MARKDOWN, FileType.fromFilename("测试文档.md"));
        }
    }
}