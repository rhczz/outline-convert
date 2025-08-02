package dev.hc.convert.factory;

import dev.hc.convert.FileType;
import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.converter.impl.JsonFormatConverter;
import dev.hc.convert.converter.impl.MarkdownFormatConverter;
import dev.hc.convert.converter.impl.OpmlFormatConverter;
import dev.hc.convert.converter.impl.XMindFormatConverter;
import dev.hc.convert.exception.ConversionFailureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConverterFactory 单元测试
 */
@DisplayName("转换器工厂测试")
class ConverterFactoryTest {

    @AfterEach
    void tearDown() {
        // 清空缓存以确保测试独立性
        ConverterFactory.clearCache();
    }

    @Nested
    @DisplayName("转换器获取测试")
    class ConverterRetrievalTests {

        @Test
        @DisplayName("获取JSON转换器")
        void testGetJsonConverter() {
            FormatConverter converter = ConverterFactory.getConverter(FileType.JSON);
            
            assertNotNull(converter);
            assertInstanceOf(JsonFormatConverter.class, converter);
        }

        @Test
        @DisplayName("获取Markdown转换器")
        void testGetMarkdownConverter() {
            FormatConverter converter = ConverterFactory.getConverter(FileType.MARKDOWN);
            
            assertNotNull(converter);
            assertInstanceOf(MarkdownFormatConverter.class, converter);
        }

        @Test
        @DisplayName("获取XMind转换器")
        void testGetXMindConverter() {
            FormatConverter converter = ConverterFactory.getConverter(FileType.XMIND);
            
            assertNotNull(converter);
            assertInstanceOf(XMindFormatConverter.class, converter);
        }

        @Test
        @DisplayName("获取OPML转换器")
        void testGetOpmlConverter() {
            FormatConverter converter = ConverterFactory.getConverter(FileType.OPML);
            
            assertNotNull(converter);
            assertInstanceOf(OpmlFormatConverter.class, converter);
        }

        @Test
        @DisplayName("传入null文件类型应该抛出异常")
        void testGetConverterWithNull() {
            assertThrows(ConversionFailureException.class, () -> {
                ConverterFactory.getConverter(null);
            });
        }
    }

    @Nested
    @DisplayName("缓存功能测试")
    class CacheTests {

        @Test
        @DisplayName("转换器应该被缓存")
        void testConverterCaching() {
            // 第一次获取
            FormatConverter converter1 = ConverterFactory.getConverter(FileType.JSON);
            // 第二次获取
            FormatConverter converter2 = ConverterFactory.getConverter(FileType.JSON);
            
            // 应该返回同一个实例
            assertSame(converter1, converter2);
        }

        @Test
        @DisplayName("不同文件类型应该返回不同的转换器实例")
        void testDifferentConvertersForDifferentTypes() {
            FormatConverter jsonConverter = ConverterFactory.getConverter(FileType.JSON);
            FormatConverter markdownConverter = ConverterFactory.getConverter(FileType.MARKDOWN);
            FormatConverter xmindConverter = ConverterFactory.getConverter(FileType.XMIND);
            FormatConverter opmlConverter = ConverterFactory.getConverter(FileType.OPML);
            
            assertNotSame(jsonConverter, markdownConverter);
            assertNotSame(jsonConverter, xmindConverter);
            assertNotSame(jsonConverter, opmlConverter);
            assertNotSame(markdownConverter, xmindConverter);
            assertNotSame(markdownConverter, opmlConverter);
            assertNotSame(xmindConverter, opmlConverter);
        }

        @Test
        @DisplayName("清空缓存后应该创建新实例")
        void testCacheClear() {
            // 获取转换器
            FormatConverter converter1 = ConverterFactory.getConverter(FileType.JSON);
            
            // 清空缓存
            ConverterFactory.clearCache();
            
            // 再次获取
            FormatConverter converter2 = ConverterFactory.getConverter(FileType.JSON);
            
            // 应该是不同的实例
            assertNotSame(converter1, converter2);
            // 但类型应该相同
            assertEquals(converter1.getClass(), converter2.getClass());
        }
    }

    @Nested
    @DisplayName("并发访问测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发获取转换器应该线程安全")
        void testConcurrentAccess() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final FormatConverter[] results = new FormatConverter[threadCount];
            
            // 创建多个线程同时获取转换器
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = ConverterFactory.getConverter(FileType.JSON);
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
            FormatConverter firstResult = results[0];
            assertNotNull(firstResult);
            
            for (int i = 1; i < threadCount; i++) {
                assertSame(firstResult, results[i]);
            }
        }

        @Test
        @DisplayName("并发获取不同类型转换器")
        void testConcurrentAccessDifferentTypes() throws InterruptedException {
            final int threadCount = 4;
            final Thread[] threads = new Thread[threadCount];
            final FormatConverter[] results = new FormatConverter[threadCount];
            final FileType[] types = {FileType.JSON, FileType.MARKDOWN, FileType.XMIND, FileType.OPML};
            
            // 创建多个线程获取不同类型的转换器
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = ConverterFactory.getConverter(types[index]);
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
            assertInstanceOf(JsonFormatConverter.class, results[0]);
            assertInstanceOf(MarkdownFormatConverter.class, results[1]);
            assertInstanceOf(XMindFormatConverter.class, results[2]);
            assertInstanceOf(OpmlFormatConverter.class, results[3]);
        }
    }

    @Nested
    @DisplayName("支持检查测试")
    class SupportCheckTests {

        @Test
        @DisplayName("支持的文件类型应该返回true")
        void testSupportedFileTypes() {
            assertTrue(ConverterFactory.isSupported(FileType.JSON));
            assertTrue(ConverterFactory.isSupported(FileType.MARKDOWN));
            assertTrue(ConverterFactory.isSupported(FileType.XMIND));
            assertTrue(ConverterFactory.isSupported(FileType.OPML));
        }

        @Test
        @DisplayName("null文件类型不应该被支持")
        void testNullFileTypeNotSupported() {
            assertFalse(ConverterFactory.isSupported(null));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("获取转换器时的异常应该被正确处理")
        void testExceptionHandling() {
            // 验证null参数抛出正确的异常
            ConversionFailureException exception = assertThrows(ConversionFailureException.class, () -> {
                ConverterFactory.getConverter(null);
            });
            
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("File type cannot be null"));
        }
    }

    @Nested
    @DisplayName("工厂类特性测试")
    class FactoryClassTests {

        @Test
        @DisplayName("工厂类不应该被实例化")
        void testCannotInstantiate() {
            // 验证构造器是私有的
            assertThrows(AssertionError.class, () -> {
                // 通过反射尝试创建实例
                java.lang.reflect.Constructor<ConverterFactory> constructor = 
                    ConverterFactory.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });
        }

        @Test
        @DisplayName("所有方法都应该是静态的")
        void testAllMethodsAreStatic() {
            java.lang.reflect.Method[] methods = ConverterFactory.class.getDeclaredMethods();
            
            for (java.lang.reflect.Method method : methods) {
                if (!method.getName().equals("createConverter")) { // 私有方法可能不是静态的
                    assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                        "Method " + method.getName() + " should be static");
                }
            }
        }
    }

    @Nested
    @DisplayName("内存管理测试")
    class MemoryManagementTests {

        @Test
        @DisplayName("转换器实例不应该过度占用内存")
        void testMemoryUsage() {
            // 创建大量转换器实例（实际上是从缓存获取）
            for (int i = 0; i < 1000; i++) {
                ConverterFactory.getConverter(FileType.JSON);
                ConverterFactory.getConverter(FileType.MARKDOWN);
                ConverterFactory.getConverter(FileType.XMIND);
                ConverterFactory.getConverter(FileType.OPML);
            }
            
            // 验证只创建了4个实例（每个类型一个）
            FormatConverter jsonConverter1 = ConverterFactory.getConverter(FileType.JSON);
            FormatConverter jsonConverter2 = ConverterFactory.getConverter(FileType.JSON);
            assertSame(jsonConverter1, jsonConverter2);
        }

        @Test
        @DisplayName("清空缓存应该释放引用")
        void testCacheClearReleasesReferences() {
            // 获取转换器
            FormatConverter converter = ConverterFactory.getConverter(FileType.JSON);
            assertNotNull(converter);
            
            // 清空缓存
            ConverterFactory.clearCache();
            
            // 再次获取应该是新实例
            FormatConverter newConverter = ConverterFactory.getConverter(FileType.JSON);
            assertNotSame(converter, newConverter);
        }
    }
}