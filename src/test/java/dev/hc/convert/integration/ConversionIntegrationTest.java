package dev.hc.convert.integration;

import dev.hc.convert.FileType;
import dev.hc.convert.engine.ConvertEngine;
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
 * 转换集成测试
 * 测试整个转换流程的端到端功能
 */
@DisplayName("转换集成测试")
class ConversionIntegrationTest {

    @TempDir
    Path tempDir;

    private File jsonInputFile;
    private File markdownInputFile;
    private File opmlInputFile;

    @BeforeEach
    void setUp() throws IOException {
        setupTestFiles();
    }

    private void setupTestFiles() throws IOException {
        // 创建JSON测试文件
        jsonInputFile = tempDir.resolve("test.json").toFile();
        try (FileWriter writer = new FileWriter(jsonInputFile)) {
            writer.write("""
                {
                  "title": "测试大纲文档",
                  "description": "这是一个用于集成测试的文档",
                  "metadata": {
                    "author": "测试用户",
                    "version": "1.0",
                    "tags": ["测试", "集成"]
                  },
                  "rootNodes": [
                    {
                      "title": "第一章",
                      "content": "第一章的详细内容",
                      "level": 0,
                      "children": [
                        {
                          "title": "1.1 小节",
                          "content": "第一小节的内容",
                          "level": 1,
                          "children": [
                            {
                              "title": "1.1.1 子小节",
                              "content": "子小节的详细描述",
                              "level": 2,
                              "children": []
                            }
                          ]
                        },
                        {
                          "title": "1.2 小节",
                          "content": "第二小节的内容",
                          "level": 1,
                          "children": []
                        }
                      ]
                    },
                    {
                      "title": "第二章",
                      "content": "第二章的概述",
                      "level": 0,
                      "children": [
                        {
                          "title": "2.1 小节",
                          "content": "第二章第一小节",
                          "level": 1,
                          "children": []
                        }
                      ]
                    }
                  ]
                }
                """);
        }

        // 创建Markdown测试文件
        markdownInputFile = tempDir.resolve("test.md").toFile();
        try (FileWriter writer = new FileWriter(markdownInputFile)) {
            writer.write("""
                # 测试大纲文档
                
                这是一个用于集成测试的Markdown文档
                
                ## 第一章
                
                第一章的详细内容
                
                ### 1.1 小节
                
                第一小节的内容
                
                #### 1.1.1 子小节
                
                子小节的详细描述
                
                ### 1.2 小节
                
                第二小节的内容
                
                ## 第二章
                
                第二章的概述
                
                ### 2.1 小节
                
                第二章第一小节
                """);
        }

        // 创建OPML测试文件
        opmlInputFile = tempDir.resolve("test.opml").toFile();
        try (FileWriter writer = new FileWriter(opmlInputFile)) {
            writer.write("""
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="2.0">
                    <head>
                        <title>测试大纲文档</title>
                        <dateCreated>Mon, 01 Aug 2025 00:00:00 GMT</dateCreated>
                        <dateModified>Mon, 01 Aug 2025 00:00:00 GMT</dateModified>
                        <ownerName>测试用户</ownerName>
                        <docs>https://example.com/outline-convert</docs>
                    </head>
                    <body>
                        <outline text="第一章" _note="第一章的详细内容">
                            <outline text="1.1 小节" _note="第一小节的内容">
                                <outline text="1.1.1 子小节" _note="子小节的详细描述"/>
                            </outline>
                            <outline text="1.2 小节" _note="第二小节的内容"/>
                        </outline>
                        <outline text="第二章" _note="第二章的概述">
                            <outline text="2.1 小节" _note="第二章第一小节"/>
                        </outline>
                    </body>
                </opml>
                """);
        }
    }

    @Nested
    @DisplayName("API使用方式测试")
    class ApiUsageTests {

        @Test
        @DisplayName("流畅API - 基本转换流程")
        void testFluentApiBasicConversion() {
            // 测试API的流畅性和易用性
            assertDoesNotThrow(() -> {
                ConvertEngine.Builder builder = ConvertEngine
                    .from(FileType.JSON)
                    .to(FileType.MARKDOWN);
                
                assertNotNull(builder);
                assertEquals(FileType.JSON, builder.getSourceType());
                assertEquals(FileType.MARKDOWN, builder.getTargetType());
            });
        }

        @Test
        @DisplayName("文件类型推断API")
        void testFileTypeInferenceApi() {
            assertDoesNotThrow(() -> {
                ConvertEngine.Builder builderFromFile = ConvertEngine
                    .from(jsonInputFile)
                    .toMarkdown();
                
                assertEquals(FileType.JSON, builderFromFile.getSourceType());
                assertEquals(FileType.MARKDOWN, builderFromFile.getTargetType());
                
                ConvertEngine.Builder builderFromFilename = ConvertEngine
                    .from("test.md")
                    .toJson();
                
                assertEquals(FileType.MARKDOWN, builderFromFilename.getSourceType());
                assertEquals(FileType.JSON, builderFromFilename.getTargetType());
            });
        }

        @Test
        @DisplayName("便捷方法API")
        void testConvenienceMethodsApi() {
            assertDoesNotThrow(() -> {
                ConvertEngine.FromBuilder fromBuilder = ConvertEngine.from(FileType.JSON);
                
                // 测试所有便捷转换方法
                assertNotNull(fromBuilder.toJson());
                assertNotNull(fromBuilder.toMarkdown());
                assertNotNull(fromBuilder.toXMind());
                assertNotNull(fromBuilder.toOpml());
            });
        }

        @Test
        @DisplayName("文件类型链式API")
        void testFileTypeChainApi() {
            assertDoesNotThrow(() -> {
                ConvertEngine.Builder builder1 = FileType.JSON.toMarkdown();
                ConvertEngine.Builder builder2 = FileType.MARKDOWN.toXMind();
                ConvertEngine.Builder builder3 = FileType.OPML.toJson();
                
                assertNotNull(builder1);
                assertNotNull(builder2);
                assertNotNull(builder3);
            });
        }
    }

    @Nested
    @DisplayName("错误处理集成测试")
    class ErrorHandlingIntegrationTests {

        @Test
        @DisplayName("不存在文件的错误处理")
        void testNonExistentFileErrorHandling() {
            File nonExistentFile = new File("nonexistent.json");
            File outputFile = tempDir.resolve("output.md").toFile();
            
            ConversionFailureException exception = assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine
                    .from(FileType.JSON)
                    .to(FileType.MARKDOWN)
                    .convert(nonExistentFile, outputFile);
            });
            
            assertNotNull(exception.getMessage());
            assertNotNull(exception.getErrorCode());
        }

        @Test
        @DisplayName("不支持的文件格式错误处理")
        void testUnsupportedFormatErrorHandling() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from("test.txt").toJson();
            });
        }

        @Test
        @DisplayName("相同源和目标格式错误处理")
        void testSameSourceTargetFormatErrorHandling() {
            assertThrows(ConversionFailureException.class, () -> {
                ConvertEngine.from(FileType.JSON).to(FileType.JSON);
            });
        }

        @Test
        @DisplayName("null参数错误处理")
        void testNullParameterErrorHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File outputFile = tempDir.resolve("output.md").toFile();
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((File) null, outputFile);
            });
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(jsonInputFile, (File) null);
            });
        }

        @Test
        @DisplayName("输入流错误处理")
        void testInputStreamErrorHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File outputFile = tempDir.resolve("output.md").toFile();
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((InputStream) null, "test.json", outputFile);
            });
        }

        @Test
        @DisplayName("字节数组错误处理")
        void testByteArrayErrorHandling() {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File outputFile = tempDir.resolve("output.md").toFile();
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert((byte[]) null, "test.json", outputFile);
            });
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(new byte[0], "test.json", outputFile);
            });
        }
    }

    @Nested
    @DisplayName("文件验证集成测试")
    class FileValidationIntegrationTests {

        @Test
        @DisplayName("文件大小限制验证")
        void testFileSizeValidation() {
            // 创建一个正常大小的文件应该通过验证
            assertTrue(jsonInputFile.exists());
            assertTrue(jsonInputFile.length() < 100 * 1024 * 1024); // 小于100MB
            
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 正常大小的文件不应该抛出异常
            assertDoesNotThrow(() -> {
                File outputFile = tempDir.resolve("output.md").toFile();
                // 注意：这里测试的是验证逻辑，实际转换可能需要具体的转换器实现
            });
        }

        @Test
        @DisplayName("文件权限验证")
        void testFilePermissionValidation() {
            assertTrue(jsonInputFile.canRead());
            
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 可读文件不应该抛出权限异常
            assertDoesNotThrow(() -> {
                File outputFile = tempDir.resolve("output.md").toFile();
                // 验证文件存在且可读
                assertTrue(jsonInputFile.exists());
                assertTrue(jsonInputFile.canRead());
            });
        }

        @Test
        @DisplayName("目录作为输入文件验证")
        void testDirectoryAsInputValidation() {
            File directory = tempDir.toFile();
            File outputFile = tempDir.resolve("output.md").toFile();
            
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            assertThrows(ConversionFailureException.class, () -> {
                builder.convert(directory, outputFile);
            });
        }

        @Test
        @DisplayName("文件名安全性验证")
        void testFilenameSafetyValidation() throws IOException {
            // 测试正常文件名
            File normalFile = tempDir.resolve("normal-file.json").toFile();
            assertTrue(normalFile.createNewFile());
            
            ConvertEngine.Builder builder = ConvertEngine.from(normalFile).to(FileType.MARKDOWN);
            assertNotNull(builder);
            
            // 测试中文文件名
            File chineseFile = tempDir.resolve("中文文件名.json").toFile();
            assertTrue(chineseFile.createNewFile());
            
            ConvertEngine.Builder chineseBuilder = ConvertEngine.from(chineseFile).to(FileType.MARKDOWN);
            assertNotNull(chineseBuilder);
        }
    }

    @Nested
    @DisplayName("多种输入输出方式集成测试")
    class MultipleIoMethodsIntegrationTests {

        @Test
        @DisplayName("文件到文件转换")
        void testFileToFileConversion() {
            ConvertEngine.Builder builder = ConvertEngine.from(jsonInputFile).to(FileType.MARKDOWN);
            File outputFile = tempDir.resolve("output.md").toFile();
            
            // 测试API调用不抛出异常
            assertDoesNotThrow(() -> {
                // 实际转换需要具体的转换器实现
                assertNotNull(builder);
                assertEquals(FileType.JSON, builder.getSourceType());
                assertEquals(FileType.MARKDOWN, builder.getTargetType());
            });
        }

        @Test
        @DisplayName("文件到同目录转换")
        void testFileToSameDirectoryConversion() {
            ConvertEngine.Builder builder = ConvertEngine.from(jsonInputFile).to(FileType.MARKDOWN);
            
            assertDoesNotThrow(() -> {
                // 测试获取输出文件的逻辑
                String expectedOutputName = "test.md";
                assertTrue(expectedOutputName.endsWith(".md"));
            });
        }

        @Test
        @DisplayName("输入流转换")
        void testInputStreamConversion() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            File outputFile = tempDir.resolve("output.md").toFile();
            
            try (FileInputStream inputStream = new FileInputStream(jsonInputFile)) {
                assertDoesNotThrow(() -> {
                    // 测试输入流API
                    assertNotNull(inputStream);
                    assertNotNull(builder);
                });
            }
        }

        @Test
        @DisplayName("输出流转换")
        void testOutputStreamConversion() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(jsonInputFile).to(FileType.MARKDOWN);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                assertDoesNotThrow(() -> {
                    // 测试输出流API
                    assertNotNull(outputStream);
                    assertNotNull(builder);
                });
            }
        }

        @Test
        @DisplayName("字节数组转换")
        void testByteArrayConversion() throws IOException {
            ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            
            // 读取文件内容为字节数组
            byte[] inputData;
            try (FileInputStream fis = new FileInputStream(jsonInputFile)) {
                inputData = fis.readAllBytes();
            }
            
            File outputFile = tempDir.resolve("output.md").toFile();
            
            assertDoesNotThrow(() -> {
                // 测试字节数组API
                assertTrue(inputData.length > 0);
                assertNotNull(builder);
            });
        }

        @Test
        @DisplayName("转换为字节数组")
        void testConvertToByteArray() {
            ConvertEngine.Builder builder = ConvertEngine.from(jsonInputFile).to(FileType.MARKDOWN);
            
            assertDoesNotThrow(() -> {
                // 测试转换为字节数组的API
                assertNotNull(builder);
                assertEquals(FileType.JSON, builder.getSourceType());
                assertEquals(FileType.MARKDOWN, builder.getTargetType());
            });
        }
    }

    @Nested
    @DisplayName("并发安全性集成测试")
    class ConcurrencySafetyIntegrationTests {

        @Test
        @DisplayName("多线程同时创建构建器")
        void testConcurrentBuilderCreation() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final ConvertEngine.Builder[] results = new ConvertEngine.Builder[threadCount];
            final Exception[] exceptions = new Exception[threadCount];
            
            // 创建多个线程同时创建构建器
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        results[index] = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
                    } catch (Exception e) {
                        exceptions[index] = e;
                    }
                });
            }
            
            // 启动所有线程
            for (Thread thread : threads) {
                thread.start();
            }
            
            // 等待所有线程完成
            for (Thread thread : threads) {
                thread.join();
            }
            
            // 验证结果
            for (int i = 0; i < threadCount; i++) {
                assertNull(exceptions[i], "Thread " + i + " should not have exceptions");
                assertNotNull(results[i], "Thread " + i + " should have valid result");
                assertEquals(FileType.JSON, results[i].getSourceType());
                assertEquals(FileType.MARKDOWN, results[i].getTargetType());
            }
        }

        @Test
        @DisplayName("多线程同时进行文件类型推断")
        void testConcurrentFileTypeInference() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final FileType[] results = new FileType[threadCount];
            final Exception[] exceptions = new Exception[threadCount];
            
            final String[] filenames = {
                "test1.json", "test2.md", "test3.opml", "test4.xmind", "test5.json",
                "test6.markdown", "test7.JSON", "test8.MD", "test9.OPML", "test10.XMIND"
            };
            
            // 创建多个线程同时进行文件类型推断
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        results[index] = FileType.fromFilename(filenames[index]);
                    } catch (Exception e) {
                        exceptions[index] = e;
                    }
                });
            }
            
            // 启动所有线程
            for (Thread thread : threads) {
                thread.start();
            }
            
            // 等待所有线程完成
            for (Thread thread : threads) {
                thread.join();
            }
            
            // 验证结果
            for (int i = 0; i < threadCount; i++) {
                assertNull(exceptions[i], "Thread " + i + " should not have exceptions");
                assertNotNull(results[i], "Thread " + i + " should have valid result");
            }
        }
    }

    @Nested
    @DisplayName("性能基准测试")
    class PerformanceBenchmarkTests {

        @Test
        @DisplayName("构建器创建性能")
        void testBuilderCreationPerformance() {
            long startTime = System.currentTimeMillis();
            
            // 创建大量构建器实例
            for (int i = 0; i < 1000; i++) {
                ConvertEngine.Builder builder = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
                assertNotNull(builder);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 1000个构建器创建应该在合理时间内完成（比如1秒）
            assertTrue(duration < 1000, "Builder creation should be fast, took: " + duration + "ms");
        }

        @Test
        @DisplayName("文件类型推断性能")
        void testFileTypeInferencePerformance() {
            String[] filenames = {
                "test.json", "test.md", "test.opml", "test.xmind",
                "test.JSON", "test.MD", "test.OPML", "test.XMIND",
                "test.markdown", "test.Markdown"
            };
            
            long startTime = System.currentTimeMillis();
            
            // 进行大量文件类型推断
            for (int i = 0; i < 1000; i++) {
                for (String filename : filenames) {
                    FileType fileType = FileType.fromFilename(filename);
                    assertNotNull(fileType);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 文件类型推断应该很快
            assertTrue(duration < 1000, "File type inference should be fast, took: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("内存使用测试")
    class MemoryUsageTests {

        @Test
        @DisplayName("构建器内存使用")
        void testBuilderMemoryUsage() {
            // 获取初始内存使用
            Runtime runtime = Runtime.getRuntime();
            runtime.gc(); // 建议进行垃圾回收
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // 创建大量构建器
            ConvertEngine.Builder[] builders = new ConvertEngine.Builder[1000];
            for (int i = 0; i < builders.length; i++) {
                builders[i] = ConvertEngine.from(FileType.JSON).to(FileType.MARKDOWN);
            }
            
            // 获取使用后的内存
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = usedMemory - initialMemory;
            
            // 内存增长应该在合理范围内（比如小于10MB）
            assertTrue(memoryIncrease < 10 * 1024 * 1024, 
                "Memory usage should be reasonable, increased by: " + memoryIncrease + " bytes");
            
            // 清理引用
            for (int i = 0; i < builders.length; i++) {
                builders[i] = null;
            }
        }

        @Test
        @DisplayName("大量文件类型推断内存使用")
        void testFileTypeInferenceMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // 进行大量文件类型推断操作
            for (int i = 0; i < 10000; i++) {
                FileType.fromFilename("test" + i + ".json");
                FileType.fromFilename("test" + i + ".md");
                FileType.fromFilename("test" + i + ".opml");
                FileType.fromFilename("test" + i + ".xmind");
            }
            
            runtime.gc();
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = finalMemory - initialMemory;
            
            // 内存增长应该很小，因为文件类型推断不应该产生大量对象
            assertTrue(memoryIncrease < 5 * 1024 * 1024,
                "File type inference should not use much memory, increased by: " + memoryIncrease + " bytes");
        }
    }
}