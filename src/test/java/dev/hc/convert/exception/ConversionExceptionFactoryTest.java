package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConversionExceptionFactory 单元测试
 */
@DisplayName("转换异常工厂测试")
class ConversionExceptionFactoryTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("工厂类特性测试")
    class FactoryClassTests {

        @Test
        @DisplayName("工厂类不应该被实例化")
        void testCannotInstantiate() {
            assertThrows(AssertionError.class, () -> {
                java.lang.reflect.Constructor<ConversionExceptionFactory> constructor = 
                    ConversionExceptionFactory.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });
        }

        @Test
        @DisplayName("所有方法都应该是静态的")
        void testAllMethodsAreStatic() {
            java.lang.reflect.Method[] methods = ConversionExceptionFactory.class.getDeclaredMethods();
            
            for (java.lang.reflect.Method method : methods) {
                assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                    "Method " + method.getName() + " should be static");
            }
        }
    }

    @Nested
    @DisplayName("文件操作异常创建测试")
    class FileOperationExceptionTests {

        @Test
        @DisplayName("创建文件名为空异常")
        void testFileNameEmpty() {
            FileOperationException exception = ConversionExceptionFactory.fileNameEmpty();
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.FILE_NAME_EMPTY.getCode(), exception.getErrorCode());
            assertEquals(ConversionErrorCode.FILE_NAME_EMPTY.getMessageTemplate(), exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("创建文件未找到异常")
        void testFileNotFound() throws IOException {
            File testFile = tempDir.resolve("nonexistent.json").toFile();
            FileOperationException exception = ConversionExceptionFactory.fileNotFound(testFile);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.FILE_NOT_FOUND.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(testFile.getAbsolutePath()));
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("创建文件为null异常")
        void testFileIsNull() {
            FileOperationException exception = ConversionExceptionFactory.fileIsNull();
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.FILE_READ_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains("File is null"));
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("创建文件读取错误异常")
        void testFileReadError() throws IOException {
            File testFile = tempDir.resolve("test.json").toFile();
            assertTrue(testFile.createNewFile());
            RuntimeException cause = new RuntimeException("读取失败");
            
            FileOperationException exception = ConversionExceptionFactory.fileReadError(testFile, cause);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.FILE_READ_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(testFile.getAbsolutePath()));
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("创建文件写入错误异常")
        void testFileWriteError() throws IOException {
            File testFile = tempDir.resolve("test.json").toFile();
            assertTrue(testFile.createNewFile());
            IOException cause = new IOException("写入失败");
            
            FileOperationException exception = ConversionExceptionFactory.fileWriteError(testFile, cause);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.FILE_WRITE_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(testFile.getAbsolutePath()));
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("转换失败异常创建测试")
    class ConversionFailureExceptionTests {

        @Test
        @DisplayName("创建不支持的文件格式异常")
        void testUnsupportedFileFormat() {
            String format = "txt";
            ConversionFailureException exception = ConversionExceptionFactory.unsupportedFileFormat(format);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(format));
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("创建系统错误异常")
        void testSystemError() {
            String details = "内存不足";
            RuntimeException cause = new RuntimeException("系统异常");
            
            ConversionFailureException exception = ConversionExceptionFactory.systemError(details, cause);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.SYSTEM_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(details));
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("创建系统错误异常（无原因）")
        void testSystemErrorNoCause() {
            String details = "配置错误";
            
            ConversionFailureException exception = ConversionExceptionFactory.systemError(details, null);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.SYSTEM_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(details));
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("解析异常创建测试")
    class ParsingExceptionTests {

        @Test
        @DisplayName("创建解析错误异常")
        void testParseError() {
            String details = "JSON格式无效";
            RuntimeException cause = new RuntimeException("解析失败");
            
            ParsingException exception = ConversionExceptionFactory.parseError(details, cause);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.PARSE_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(details));
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("创建无效文件结构异常")
        void testInvalidFileStructure() {
            String details = "缺少必需的根节点";
            
            ParsingException exception = ConversionExceptionFactory.invalidFileStructure(details);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.INVALID_FILE_STRUCTURE.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(details));
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("转换异常创建测试")
    class ConvertExceptionTests {

        @Test
        @DisplayName("创建不支持的转换异常")
        void testConversionNotSupported() {
            String sourceFormat = "JSON";
            String targetFormat = "XML";
            
            ConvertException exception = ConversionExceptionFactory.conversionNotSupported(sourceFormat, targetFormat);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.CONVERSION_NOT_SUPPORTED.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(sourceFormat));
            assertTrue(exception.getMessage().contains(targetFormat));
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("创建转换失败异常")
        void testConversionFailed() {
            String details = "转换过程中发生错误";
            RuntimeException cause = new RuntimeException("转换异常");
            
            ConvertException exception = ConversionExceptionFactory.conversionFailed(details, cause);
            
            assertNotNull(exception);
            assertEquals(ConversionErrorCode.CONVERSION_FAILED.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(details));
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("异常继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("FileOperationException应该继承自ConversionFailureException")
        void testFileOperationExceptionInheritance() {
            FileOperationException exception = ConversionExceptionFactory.fileNameEmpty();
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("ParsingException应该继承自ConversionFailureException")
        void testParsingExceptionInheritance() {
            ParsingException exception = ConversionExceptionFactory.parseError("测试", null);
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("ConvertException应该继承自ConversionFailureException")
        void testConvertExceptionInheritance() {
            ConvertException exception = ConversionExceptionFactory.conversionFailed("测试", null);
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null参数处理")
        void testNullParameters() {
            // 测试各种null参数的处理
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.unsupportedFileFormat(null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.systemError(null, null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.parseError(null, null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.invalidFileStructure(null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.conversionNotSupported(null, null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.conversionFailed(null, null);
            });
        }

        @Test
        @DisplayName("空字符串参数处理")
        void testEmptyStringParameters() {
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.unsupportedFileFormat("");
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.systemError("", null);
            });
            
            assertDoesNotThrow(() -> {
                ConversionExceptionFactory.parseError("", null);
            });
        }

        @Test
        @DisplayName("特殊字符参数处理")
        void testSpecialCharacterParameters() {
            String specialChars = "特殊字符 <>& \"'";
            
            assertDoesNotThrow(() -> {
                ConversionFailureException exception = ConversionExceptionFactory.unsupportedFileFormat(specialChars);
                assertTrue(exception.getMessage().contains(specialChars));
            });
        }

        @Test
        @DisplayName("长字符串参数处理")
        void testLongStringParameters() {
            String longString = "A".repeat(1000);
            
            assertDoesNotThrow(() -> {
                ConversionFailureException exception = ConversionExceptionFactory.systemError(longString, null);
                assertTrue(exception.getMessage().contains(longString));
            });
        }

        @Test
        @DisplayName("Unicode字符参数处理")
        void testUnicodeParameters() {
            String unicodeString = "测试文件_テスト_🔄";
            
            assertDoesNotThrow(() -> {
                ConversionFailureException exception = ConversionExceptionFactory.unsupportedFileFormat(unicodeString);
                assertTrue(exception.getMessage().contains(unicodeString));
            });
        }
    }

    @Nested
    @DisplayName("异常消息验证测试")
    class MessageValidationTests {

        @Test
        @DisplayName("所有异常都应该有非空消息")
        void testAllExceptionsHaveNonEmptyMessages() {
            assertFalse(ConversionExceptionFactory.fileNameEmpty().getMessage().isEmpty());
            
            File testFile = tempDir.resolve("test.json").toFile();
            assertFalse(ConversionExceptionFactory.fileNotFound(testFile).getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.fileIsNull().getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.fileReadError(testFile, new RuntimeException()).getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.fileWriteError(testFile, new RuntimeException()).getMessage().isEmpty());
            
            assertFalse(ConversionExceptionFactory.unsupportedFileFormat("txt").getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.systemError("error", null).getMessage().isEmpty());
            
            assertFalse(ConversionExceptionFactory.parseError("error", null).getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.invalidFileStructure("error").getMessage().isEmpty());
            
            assertFalse(ConversionExceptionFactory.conversionNotSupported("json", "xml").getMessage().isEmpty());
            assertFalse(ConversionExceptionFactory.conversionFailed("error", null).getMessage().isEmpty());
        }

        @Test
        @DisplayName("异常消息应该包含相关信息")
        void testExceptionMessagesContainRelevantInfo() {
            String format = "txt";
            ConversionFailureException formatException = ConversionExceptionFactory.unsupportedFileFormat(format);
            assertTrue(formatException.getMessage().toLowerCase().contains(format));
            
            String sourceFormat = "json";
            String targetFormat = "xml";
            ConvertException conversionException = ConversionExceptionFactory.conversionNotSupported(sourceFormat, targetFormat);
            assertTrue(conversionException.getMessage().toLowerCase().contains(sourceFormat.toLowerCase()));
            assertTrue(conversionException.getMessage().toLowerCase().contains(targetFormat.toLowerCase()));
        }
    }
}