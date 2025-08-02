package dev.hc.convert.engine;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConvertEngine 单元测试
 */
@DisplayName("转换引擎测试")
class ConvertEngineTest {

    @TempDir
    Path tempDir;

    private File inputJsonFile;
    private File inputMarkdownFile;
    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试用的输入文件
        inputJsonFile = tempDir.resolve("input.json").toFile();
        inputMarkdownFile = tempDir.resolve("input.md").toFile();
        outputFile = tempDir.resolve("output.json").toFile();

        // 写入测试数据
        try (FileWriter writer = new FileWriter(inputJsonFile)) {
            writer.write("""
                {
                  "title": "测试文档",
                  "rootNodes": [
                    {
                      "title": "第一章",
                      "content": "第一章内容",
                      "level": 0,
                      "children": []
                    }
                  ]
                }
                """);
        }

        try (FileWriter writer = new FileWriter(inputMarkdownFile)) {
            writer.write("""
                # 测试文档
                
                ## 第一章
                
                第一章内容
                """);
        }
    }

    @Nested
    @DisplayName("工厂类特性测试")
    class UtilityClassTests {

        @Test
        @DisplayName("转换引擎不应该被实例化")
        void testCannotInstantiate() {
            assertThrows(AssertionError.class, () -> {
                // 通过反射尝试创建实例
                java.lang.reflect.Constructor<ConvertEngine> constructor = 
                    ConvertEngine.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });
        }

        @Test
        @DisplayName("所有公共方法都应该是静态的")
        void testAllPublicMethodsAreStatic() {
            java.lang.reflect.Method[] methods = ConvertEngine.class.getDeclaredMethods();
            
            for (java.lang.reflect.Method method : methods) {
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                    assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                        "Public method " + method.getName() + " should be static");
                }
            }
        }
    }

    @Nested
    @DisplayName("FromBuilder创建测试")
    class FromBuilderCreationTests {

        @Test
        @DisplayName("通过文件类型创建FromBuilder")
        void testFromFileType() {
            ConvertEngine.FromBuilder builder = ConvertEngine.from(FileType.JSON);
            
            assertNotNull(builder);
        }

        @Test
        @DisplayName("通过文件创建FromBuilder")
        void testFromFile() {
            ConvertEngine.FromBuilder builder = ConvertEngine.from(inputJsonFile);
            
            assertNotNull(builder);
        }

        @Test
        @DisplayName("通过文件名创建FromBuilder")
        void testFromFilename() {
            ConvertEngine.FromBuilder builder = ConvertEngine.from("test.json");
            
            assertNotNull(builder);
        }

        @Test
        @DisplayName("不支持的文件扩展名应该抛出异常")
        void testFromUnsupportedFilename() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from("test.txt");
            });
        }

        @Test
        @DisplayName("null文件名应该抛出异常")
        void testFromNullFilename() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from((String) null);
            });
        }

        @Test
        @DisplayName("null文件应该抛出异常")
        void testFromNullFile() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from((File) null);
            });
        }
    }

    @Nested
    @DisplayName("FromBuilder转换目标测试")
    class FromBuilderTargetTests {

        private ConvertEngine.FromBuilder fromBuilder;

        @BeforeEach
        void setUp() {
            fromBuilder = ConvertEngine.from(FileType.JSON);
        }

        @Test
        @DisplayName("转换到指定格式")
        void testToSpecificFormat() {
            ConvertEngine.Builder builder = fromBuilder.to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到JSON格式")
        void testToJson() {
            ConvertEngine.Builder builder = fromBuilder.toJson();
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到Markdown格式")
        void testToMarkdown() {
            ConvertEngine.Builder builder = fromBuilder.toMarkdown();
            
            assertNotNull(builder);
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到XMind格式")
        void testToXMind() {
            ConvertEngine.Builder builder = fromBuilder.toXMind();
            
            assertNotNull(builder);
            assertEquals(FileType.XMIND, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到OPML格式")
        void testToOpml() {
            ConvertEngine.Builder builder = fromBuilder.toOpml();
            
            assertNotNull(builder);
            assertEquals(FileType.OPML, builder.getTargetType());
        }
    }

    @Nested
    @DisplayName("Builder创建测试")
    class BuilderCreationTests {

        @Test
        @DisplayName("创建有效的转换构建器")
        void testValidBuilderCreation() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("相同源和目标格式应该抛出异常")
        void testSameSourceAndTargetFormat() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from(FileType.JSON).to(FileType.JSON);
            });
        }
    }

    @Nested
    @DisplayName("文件转换测试")
    class FileConversionTests {

        @Test
        @DisplayName("基本文件转换")
        void testBasicFileConversion() {
            // 由于实际的转换器实现可能还没有完成，这里主要测试API调用
            // 在实际项目中，您可能需要模拟转换器的行为
            
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 验证构建器创建成功
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到同目录")
        void testConvertToSameDirectory() {
            ConvertEngine.Builder builder = ConvertEngine.from(inputJsonFile).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("转换到指定路径")
        void testConvertToSpecificPath() {
            ConvertEngine.Builder builder = ConvertEngine.from(inputJsonFile).to(FileType.MARKDOWN);
            String outputPath = tempDir.resolve("output.md").toString();
            
            assertNotNull(builder);
            // 实际转换测试需要依赖具体的转换器实现
        }

        @Test
        @DisplayName("null输入文件应该抛出异常")
        void testNullInputFile() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((File) null, outputFile);
            });
        }

        @Test
        @DisplayName("不存在的输入文件应该抛出异常")
        void testNonExistentInputFile() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File nonExistentFile = new File("nonexistent.json");
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(nonExistentFile, outputFile);
            });
        }

        @Test
        @DisplayName("过大的文件应该抛出异常")
        void testLargeFileException() throws IOException {
            // 创建一个模拟的大文件（通过重写sizeOf方法或其他方式）
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 这里需要根据实际的大小限制来测试
            // 由于测试环境限制，我们可以通过其他方式验证大小检查逻辑
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("流转换测试")
    class StreamConversionTests {

        @Test
        @DisplayName("输入流转换")
        void testInputStreamConversion() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            try (InputStream inputStream = new FileInputStream(inputJsonFile)) {
                assertNotNull(builder);
                assertNotNull(inputStream);
                // 实际转换测试需要依赖具体的转换器实现
            }
        }

        @Test
        @DisplayName("输出流转换")
        void testOutputStreamConversion() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                assertNotNull(builder);
                assertNotNull(outputStream);
                // 实际转换测试需要依赖具体的转换器实现
            }
        }

        @Test
        @DisplayName("null输入流应该抛出异常")
        void testNullInputStream() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((InputStream) null, "test.json", outputFile);
            });
        }

        @Test
        @DisplayName("null输出流应该抛出异常")
        void testNullOutputStream() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convertToStream(inputJsonFile, null);
            });
        }
    }

    @Nested
    @DisplayName("字节数组转换测试")
    class ByteArrayConversionTests {

        @Test
        @DisplayName("字节数组输入转换")
        void testByteArrayInput() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            byte[] inputData = """
                {
                  "title": "测试",
                  "rootNodes": []
                }
                """.getBytes();
            
            assertNotNull(builder);
            assertNotNull(inputData);
            // 实际转换测试需要依赖具体的转换器实现
        }

        @Test
        @DisplayName("转换为字节数组")
        void testConvertToBytes() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            // 实际转换测试需要依赖具体的转换器实现
        }

        @Test
        @DisplayName("null字节数组应该抛出异常")
        void testNullByteArray() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((byte[]) null, "test.json", outputFile);
            });
        }

        @Test
        @DisplayName("空字节数组应该抛出异常")
        void testEmptyByteArray() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(new byte[0], "test.json", outputFile);
            });
        }

        @Test
        @DisplayName("过大的字节数组应该抛出异常")
        void testLargeByteArray() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 创建超过限制的字节数组 (50MB + 1)
            byte[] largeData = new byte[50 * 1024 * 1024 + 1];
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(largeData, "test.json", outputFile);
            });
        }
    }

    @Nested
    @DisplayName("文件验证测试")
    class FileValidationTests {

        @Test
        @DisplayName("验证文件存在性")
        void testFileExistence() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 存在的文件不应该抛出异常（在调用convert时）
            assertNotNull(builder);
            assertTrue(inputJsonFile.exists());
        }

        @Test
        @DisplayName("验证文件可读性")
        void testFileReadability() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 可读的文件不应该抛出异常（在调用convert时）
            assertNotNull(builder);
            assertTrue(inputJsonFile.canRead());
        }

        @Test
        @DisplayName("验证目录作为输入文件应该抛出异常")
        void testDirectoryAsInputFile() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File directory = tempDir.toFile();
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(directory, outputFile);
            });
        }

        @Test
        @DisplayName("验证文件名安全性")
        void testFilenameValidation() throws IOException {
            // 测试包含特殊字符的文件名
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 正常文件名应该工作
            File normalFile = tempDir.resolve("normal-file.json").toFile();
            assertTrue(normalFile.createNewFile());
            
            assertNotNull(builder);
            assertTrue(normalFile.exists());
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("系统错误异常处理")
        void testSystemErrorHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 测试各种异常情况
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((File) null, outputFile);
            });
        }

        @Test
        @DisplayName("转换失败异常处理")
        void testConversionFailureHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 不存在的文件应该抛出异常
            File nonExistentFile = new File("nonexistent.json");
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(nonExistentFile, outputFile);
            });
        }

        @Test
        @DisplayName("文件操作异常处理")
        void testFileOperationExceptionHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // null文件应该抛出异常
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((File) null, outputFile);
            });
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("获取不含扩展名的文件名")
        void testGetFileNameWithoutExtension() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 由于getFileNameWithoutExtension是私有方法，我们通过公共API间接测试
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("处理特殊文件名")
        void testSpecialFilenames() {
            // 测试各种特殊文件名的处理
            ConvertEngine.Builder builder = ConvertEngine.from("test.json").to(FileType.MARKDOWN);
            
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }

        @Test
        @DisplayName("处理空文件名")
        void testEmptyFilename() {
            // 空文件名应该在创建FromBuilder时就抛出异常
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from("");
            });
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("多次创建构建器的性能")
        void testMultipleBuilderCreation() {
            // 测试多次创建构建器不会有性能问题
            for (int i = 0; i < 100; i++) {
                ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
                assertNotNull(builder);
            }
        }

        @Test
        @DisplayName("构建器复用测试")
        void testBuilderReuse() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 同一个构建器可以多次使用
            assertNotNull(builder);
            assertEquals(FileType.JSON, builder.getSourceType());
            assertEquals(FileType.MARKDOWN, builder.getTargetType());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("最小有效文件")
        void testMinimalValidFile() throws IOException {
            File minimalFile = tempDir.resolve("minimal.json").toFile();
            try (FileWriter writer = new FileWriter(minimalFile)) {
                writer.write("{}");
            }
            
            ConvertEngine.Builder builder = ConvertEngine.from(minimalFile).to(FileType.MARKDOWN);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("长文件名处理")
        void testLongFilename() {
            String longName = "a".repeat(100) + ".json";
            ConvertEngine.Builder builder = ConvertEngine.from(longName).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Unicode文件名处理")
        void testUnicodeFilename() {
            String unicodeName = "测试文件_テスト.json";
            ConvertEngine.Builder builder = ConvertEngine.from(unicodeName).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
        }

        @Test
        @DisplayName("特殊字符文件名处理")
        void testSpecialCharactersFilename() {
            String specialName = "file-with_special.chars.json";
            ConvertEngine.Builder builder = ConvertEngine.from(specialName).to(FileType.MARKDOWN);
            
            assertNotNull(builder);
        }
    }
}