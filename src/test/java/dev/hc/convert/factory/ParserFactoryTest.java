package dev.hc.convert.factory;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;
import dev.hc.convert.parser.FileParser;
import dev.hc.convert.parser.impl.JsonParser;
import dev.hc.convert.parser.impl.MarkdownParser;
import dev.hc.convert.parser.impl.OpmlParser;
import dev.hc.convert.parser.impl.XMindParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ParserFactory 单元测试
 */
@DisplayName("解析器工厂测试")
class ParserFactoryTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        // 清空缓存以确保测试独立性
        ParserFactory.clearCache();
    }

    @Nested
    @DisplayName("解析器获取测试")
    class ParserRetrievalTests {

        @Test
        @DisplayName("获取JSON解析器")
        void testGetJsonParser() {
            FileParser parser = ParserFactory.getParser(FileType.JSON);
            
            assertNotNull(parser);
            assertInstanceOf(JsonParser.class, parser);
        }

        @Test
        @DisplayName("获取Markdown解析器")
        void testGetMarkdownParser() {
            FileParser parser = ParserFactory.getParser(FileType.MARKDOWN);
            
            assertNotNull(parser);
            assertInstanceOf(MarkdownParser.class, parser);
        }

        @Test
        @DisplayName("获取XMind解析器")
        void testGetXMindParser() {
            FileParser parser = ParserFactory.getParser(FileType.XMIND);
            
            assertNotNull(parser);
            assertInstanceOf(XMindParser.class, parser);
        }

        @Test
        @DisplayName("获取OPML解析器")
        void testGetOpmlParser() {
            FileParser parser = ParserFactory.getParser(FileType.OPML);
            
            assertNotNull(parser);
            assertInstanceOf(OpmlParser.class, parser);
        }

        @Test
        @DisplayName("传入null文件类型应该抛出异常")
        void testGetParserWithNull() {
            assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser((FileType) null);
            });
        }
    }

    @Nested
    @DisplayName("基于文件的解析器获取测试")
    class FileBasedParserRetrievalTests {

        @Test
        @DisplayName("通过文件获取解析器")
        void testGetParserFromFile() throws IOException {
            File jsonFile = tempDir.resolve("test.json").toFile();
            assertTrue(jsonFile.createNewFile());
            
            FileParser parser = ParserFactory.getParser(jsonFile);
            
            assertNotNull(parser);
            assertInstanceOf(JsonParser.class, parser);
        }

        @Test
        @DisplayName("通过文件名获取解析器")
        void testGetParserFromFilename() {
            FileParser jsonParser = ParserFactory.getParser("test.json");
            FileParser markdownParser = ParserFactory.getParser("test.md");
            FileParser xmindParser = ParserFactory.getParser("test.xmind");
            FileParser opmlParser = ParserFactory.getParser("test.opml");
            
            assertInstanceOf(JsonParser.class, jsonParser);
            assertInstanceOf(MarkdownParser.class, markdownParser);
            assertInstanceOf(XMindParser.class, xmindParser);
            assertInstanceOf(OpmlParser.class, opmlParser);
        }

        @Test
        @DisplayName("不支持的文件扩展名应该抛出异常")
        void testUnsupportedFileExtension() {
            assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser("test.txt");
            });
        }

        @Test
        @DisplayName("null文件应该抛出异常")
        void testGetParserFromNullFile() {
            assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser((File) null);
            });
        }

        @Test
        @DisplayName("不存在的文件应该抛出异常")
        void testGetParserFromNonExistentFile() {
            File nonExistentFile = new File("nonexistent.json");
            
            assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser(nonExistentFile);
            });
        }
    }

    @Nested
    @DisplayName("缓存功能测试")
    class CacheTests {

        @Test
        @DisplayName("解析器应该被缓存")
        void testParserCaching() {
            // 第一次获取
            FileParser parser1 = ParserFactory.getParser(FileType.JSON);
            // 第二次获取
            FileParser parser2 = ParserFactory.getParser(FileType.JSON);
            
            // 应该返回同一个实例
            assertSame(parser1, parser2);
        }

        @Test
        @DisplayName("不同文件类型应该返回不同的解析器实例")
        void testDifferentParsersForDifferentTypes() {
            FileParser jsonParser = ParserFactory.getParser(FileType.JSON);
            FileParser markdownParser = ParserFactory.getParser(FileType.MARKDOWN);
            FileParser xmindParser = ParserFactory.getParser(FileType.XMIND);
            FileParser opmlParser = ParserFactory.getParser(FileType.OPML);
            
            assertNotSame(jsonParser, markdownParser);
            assertNotSame(jsonParser, xmindParser);
            assertNotSame(jsonParser, opmlParser);
            assertNotSame(markdownParser, xmindParser);
            assertNotSame(markdownParser, opmlParser);
            assertNotSame(xmindParser, opmlParser);
        }

        @Test
        @DisplayName("清空缓存后应该创建新实例")
        void testCacheClear() {
            // 获取解析器
            FileParser parser1 = ParserFactory.getParser(FileType.JSON);
            
            // 清空缓存
            ParserFactory.clearCache();
            
            // 再次获取
            FileParser parser2 = ParserFactory.getParser(FileType.JSON);
            
            // 应该是不同的实例
            assertNotSame(parser1, parser2);
            // 但类型应该相同
            assertEquals(parser1.getClass(), parser2.getClass());
        }
    }

    @Nested
    @DisplayName("并发访问测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发获取解析器应该线程安全")
        void testConcurrentAccess() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final FileParser[] results = new FileParser[threadCount];
            
            // 创建多个线程同时获取解析器
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = ParserFactory.getParser(FileType.JSON);
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
            
            // 验证所有结果都是同一个实例
            FileParser firstResult = results[0];
            assertNotNull(firstResult);
            
            for (int i = 1; i < threadCount; i++) {
                assertSame(firstResult, results[i]);
            }
        }

        @Test
        @DisplayName("并发获取不同类型解析器")
        void testConcurrentAccessDifferentTypes() throws InterruptedException {
            final int threadCount = 4;
            final Thread[] threads = new Thread[threadCount];
            final FileParser[] results = new FileParser[threadCount];
            final FileType[] types = {FileType.JSON, FileType.MARKDOWN, FileType.XMIND, FileType.OPML};
            
            // 创建多个线程获取不同类型的解析器
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = ParserFactory.getParser(types[index]);
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
            
            // 验证所有结果都不为null且类型正确
            assertInstanceOf(JsonParser.class, results[0]);
            assertInstanceOf(MarkdownParser.class, results[1]);
            assertInstanceOf(XMindParser.class, results[2]);
            assertInstanceOf(OpmlParser.class, results[3]);
        }
    }

    @Nested
    @DisplayName("支持检查测试")
    class SupportCheckTests {

        @Test
        @DisplayName("支持的文件类型应该返回true")
        void testSupportedFileTypes() {
            assertTrue(ParserFactory.isSupported(FileType.JSON));
            assertTrue(ParserFactory.isSupported(FileType.MARKDOWN));
            assertTrue(ParserFactory.isSupported(FileType.XMIND));
            assertTrue(ParserFactory.isSupported(FileType.OPML));
        }

        @Test
        @DisplayName("null文件类型不应该被支持")
        void testNullFileTypeNotSupported() {
            assertFalse(ParserFactory.isSupported((FileType) null));
        }

        @Test
        @DisplayName("支持文件检查")
        void testFileSupport() throws IOException {
            File jsonFile = tempDir.resolve("test.json").toFile();
            File markdownFile = tempDir.resolve("test.md").toFile();
            File unsupportedFile = tempDir.resolve("test.txt").toFile();
            
            assertTrue(jsonFile.createNewFile());
            assertTrue(markdownFile.createNewFile());
            assertTrue(unsupportedFile.createNewFile());
            
            assertTrue(ParserFactory.isSupported(jsonFile));
            assertTrue(ParserFactory.isSupported(markdownFile));
            assertFalse(ParserFactory.isSupported(unsupportedFile));
        }

        @Test
        @DisplayName("null文件不应该被支持")
        void testNullFileNotSupported() {
            assertFalse(ParserFactory.isSupported((File) null));
        }

        @Test
        @DisplayName("不存在的文件不应该被支持")
        void testNonExistentFileNotSupported() {
            File nonExistentFile = new File("nonexistent.json");
            assertFalse(ParserFactory.isSupported(nonExistentFile));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("获取解析器时的异常应该被正确处理")
        void testExceptionHandling() {
            // 验证null参数抛出正确的异常
            ConversionFailureException exception = assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser((FileType) null);
            });
            
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("File type cannot be null"));
        }

        @Test
        @DisplayName("不支持的文件格式应该抛出正确异常")
        void testUnsupportedFormatException() {
            ConversionFailureException exception = assertThrows(ConversionFailureException.class, () -> {
                ParserFactory.getParser("test.txt");
            });
            
            assertNotNull(exception.getMessage());
        }
    }

    @Nested
    @DisplayName("工厂类特性测试")
    class FactoryClassTests {

        @Test
        @DisplayName("工厂类不应该被实例化")
        void testCannotInstantiate() throws NoSuchMethodException {
            // 验证构造器是私有的
            assertThrows(Throwable.class, () -> {
                // 通过反射尝试创建实例
                java.lang.reflect.Constructor<ParserFactory> constructor =
                        ParserFactory.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                try {
                    constructor.newInstance();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    // 如果是InvocationTargetException，则抛出其cause
                    throw e.getCause();
                }
            });

            // 额外验证构造器确实是私有的
            java.lang.reflect.Constructor<ParserFactory> constructor = ParserFactory.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), "构造器应该是私有的");
        }

        @Test
        @DisplayName("所有公共方法都应该是静态的")
        void testAllPublicMethodsAreStatic() {
            java.lang.reflect.Method[] methods = ParserFactory.class.getDeclaredMethods();
            
            for (java.lang.reflect.Method method : methods) {
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && 
                    !method.getName().equals("createParser")) { // 私有方法可能不是静态的
                    assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                        "Public method " + method.getName() + " should be static");
                }
            }
        }
    }

    @Nested
    @DisplayName("内存管理测试")
    class MemoryManagementTests {

        @Test
        @DisplayName("解析器实例不应该过度占用内存")
        void testMemoryUsage() {
            // 创建大量解析器实例（实际上是从缓存获取）
            for (int i = 0; i < 1000; i++) {
                ParserFactory.getParser(FileType.JSON);
                ParserFactory.getParser(FileType.MARKDOWN);
                ParserFactory.getParser(FileType.XMIND);
                ParserFactory.getParser(FileType.OPML);
            }
            
            // 验证只创建了4个实例（每个类型一个）
            FileParser jsonParser1 = ParserFactory.getParser(FileType.JSON);
            FileParser jsonParser2 = ParserFactory.getParser(FileType.JSON);
            assertSame(jsonParser1, jsonParser2);
        }

        @Test
        @DisplayName("清空缓存应该释放引用")
        void testCacheClearReleasesReferences() {
            // 获取解析器
            FileParser parser = ParserFactory.getParser(FileType.JSON);
            assertNotNull(parser);
            
            // 清空缓存
            ParserFactory.clearCache();
            
            // 再次获取应该是新实例
            FileParser newParser = ParserFactory.getParser(FileType.JSON);
            assertNotSame(parser, newParser);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("不同方式获取同类型解析器应该返回相同实例")
        void testSameParserFromDifferentMethods() throws IOException {
            File jsonFile = tempDir.resolve("test.json").toFile();
            assertTrue(jsonFile.createNewFile());
            
            FileParser parser1 = ParserFactory.getParser(FileType.JSON);
            FileParser parser2 = ParserFactory.getParser(jsonFile);
            FileParser parser3 = ParserFactory.getParser("test.json");
            
            assertSame(parser1, parser2);
            assertSame(parser1, parser3);
        }

        @Test
        @DisplayName("大小写混合的文件名")
        void testMixedCaseFilename() {
            FileParser parser1 = ParserFactory.getParser("Test.JSON");
            FileParser parser2 = ParserFactory.getParser("test.Md");
            
            assertInstanceOf(JsonParser.class, parser1);
            assertInstanceOf(MarkdownParser.class, parser2);
        }
    }
}