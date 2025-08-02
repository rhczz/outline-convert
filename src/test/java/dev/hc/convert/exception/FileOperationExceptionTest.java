package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileOperationException 单元测试
 */
@DisplayName("文件操作异常测试")
class FileOperationExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("错误代码和消息构造函数")
        void testErrorCodeAndMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.FILE_NOT_FOUND;
            String message = "测试文件未找到";
            
            FileOperationException exception = new FileOperationException(errorCode, message);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("完整构造函数（包含原因）")
        void testFullConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.FILE_READ_ERROR;
            String message = "文件读取失败";
            RuntimeException cause = new RuntimeException("IO异常");
            
            FileOperationException exception = new FileOperationException(errorCode, message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("null消息构造函数")
        void testNullMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.FILE_NOT_FOUND;
            
            FileOperationException exception = new FileOperationException(errorCode, null);
            
            assertNull(exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("null原因构造函数")
        void testNullCauseConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.FILE_WRITE_ERROR;
            String message = "写入失败";
            
            FileOperationException exception = new FileOperationException(errorCode, message, null);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("异常继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("应该继承自ConversionFailureException")
        void testExtendsConversionFailureException() {
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, "测试");
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("应该正确设置错误代码")
        void testErrorCodeFromConversionErrorCode() {
            ConversionErrorCode errorCode = ConversionErrorCode.FILE_READ_ERROR;
            FileOperationException exception = new FileOperationException(errorCode, "测试消息");
            
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("不同错误代码测试")
    class DifferentErrorCodeTests {

        @Test
        @DisplayName("文件名为空错误")
        void testFileNameEmptyError() {
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NAME_EMPTY, "文件名不能为空");
            
            assertEquals(ConversionErrorCode.FILE_NAME_EMPTY.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains("文件名"));
        }

        @Test
        @DisplayName("文件未找到错误")
        void testFileNotFoundError() {
            String filename = "/path/to/missing/file.json";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, 
                ConversionErrorCode.FILE_NOT_FOUND.formatMessage(filename));
            
            assertEquals(ConversionErrorCode.FILE_NOT_FOUND.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(filename));
        }

        @Test
        @DisplayName("文件读取错误")
        void testFileReadError() {
            String filename = "/path/to/file.json";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR,
                ConversionErrorCode.FILE_READ_ERROR.formatMessage(filename));
            
            assertEquals(ConversionErrorCode.FILE_READ_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(filename));
        }

        @Test
        @DisplayName("文件写入错误")
        void testFileWriteError() {
            String filename = "/path/to/output.json";
            java.io.IOException cause = new java.io.IOException("磁盘空间不足");
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_WRITE_ERROR,
                ConversionErrorCode.FILE_WRITE_ERROR.formatMessage(filename),
                cause);
            
            assertEquals(ConversionErrorCode.FILE_WRITE_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(filename));
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("不支持的文件格式错误")
        void testUnsupportedFileFormatError() {
            String format = "txt";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.UNSUPPORTED_FILE_FORMAT,
                ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.formatMessage(format));
            
            assertEquals(ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(format));
        }
    }

    @Nested
    @DisplayName("异常链测试")
    class ExceptionChainingTests {

        @Test
        @DisplayName("IOException原因链")
        void testIOExceptionCause() {
            java.io.IOException ioException = new java.io.IOException("无法访问文件");
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR, "文件读取失败", ioException);
            
            assertEquals(ioException, exception.getCause());
            assertTrue(exception.getCause() instanceof java.io.IOException);
        }

        @Test
        @DisplayName("SecurityException原因链")
        void testSecurityExceptionCause() {
            SecurityException securityException = new SecurityException("没有文件访问权限");
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR, "权限不足", securityException);
            
            assertEquals(securityException, exception.getCause());
            assertTrue(exception.getCause() instanceof SecurityException);
        }

        @Test
        @DisplayName("多级异常链")
        void testMultiLevelExceptionChain() {
            java.io.FileNotFoundException fileNotFound = new java.io.FileNotFoundException("文件不存在");
            java.io.IOException ioException = new java.io.IOException("IO操作失败", fileNotFound);
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR, "读取操作失败", ioException);
            
            assertEquals(ioException, exception.getCause());
            assertEquals(fileNotFound, exception.getCause().getCause());
        }
    }

    @Nested
    @DisplayName("消息格式化测试")
    class MessageFormattingTests {

        @Test
        @DisplayName("使用ErrorCode格式化消息")
        void testMessageFormattingWithErrorCode() {
            String filename = "测试文件.json";
            String formattedMessage = ConversionErrorCode.FILE_NOT_FOUND.formatMessage(filename);
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, formattedMessage);
            
            assertTrue(exception.getMessage().contains(filename));
            assertTrue(exception.getMessage().contains("File not found"));
        }

        @Test
        @DisplayName("中文路径消息")
        void testChinesePathMessage() {
            String chinesePath = "/用户/文档/测试文件.json";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND,
                ConversionErrorCode.FILE_NOT_FOUND.formatMessage(chinesePath));
            
            assertTrue(exception.getMessage().contains(chinesePath));
        }

        @Test
        @DisplayName("特殊字符路径消息")
        void testSpecialCharacterPathMessage() {
            String specialPath = "/path with spaces/file-name_test.json";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR,
                ConversionErrorCode.FILE_READ_ERROR.formatMessage(specialPath));
            
            assertTrue(exception.getMessage().contains(specialPath));
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("异常应该是可序列化的")
        void testSerializable() {
            assertTrue(java.io.Serializable.class.isAssignableFrom(FileOperationException.class));
        }

        @Test
        @DisplayName("序列化和反序列化")
        void testSerializationRoundTrip() throws Exception {
            FileOperationException original = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR, 
                "测试序列化",
                new java.io.IOException("原始IO异常"));
            
            // 序列化
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            // 反序列化
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            FileOperationException deserialized = (FileOperationException) ois.readObject();
            ois.close();
            
            // 验证
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("toString和调试信息测试")
    class ToStringAndDebuggingTests {

        @Test
        @DisplayName("toString应该包含关键信息")
        void testToString() {
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, "文件未找到: test.json");
            
            String str = exception.toString();
            assertTrue(str.contains("FileOperationException"));
            assertTrue(str.contains("文件未找到"));
            assertTrue(str.contains("test.json"));
        }

        @Test
        @DisplayName("printStackTrace应该显示完整信息")
        void testPrintStackTrace() {
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR,
                "读取失败",
                new java.io.IOException("底层IO异常"));
            
            assertDoesNotThrow(() -> {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                exception.printStackTrace(pw);
                
                String stackTrace = sw.toString();
                assertTrue(stackTrace.contains("FileOperationException"));
                assertTrue(stackTrace.contains("读取失败"));
                assertTrue(stackTrace.contains("IOException"));
                assertTrue(stackTrace.contains("底层IO异常"));
            });
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空消息")
        void testEmptyMessage() {
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, "");
            
            assertEquals("", exception.getMessage());
            assertEquals(ConversionErrorCode.FILE_NOT_FOUND.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("长消息")
        void testLongMessage() {
            String longMessage = "错误详情：" + "A".repeat(10000);
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_READ_ERROR, longMessage);
            
            assertEquals(longMessage, exception.getMessage());
            assertTrue(exception.getMessage().length() > 10000);
        }

        @Test
        @DisplayName("多行消息")
        void testMultiLineMessage() {
            String multiLineMessage = "第一行错误\n第二行详情\n第三行建议";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_WRITE_ERROR, multiLineMessage);
            
            assertEquals(multiLineMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("\n"));
        }

        @Test
        @DisplayName("Unicode消息")
        void testUnicodeMessage() {
            String unicodeMessage = "文件操作失败: 测试文件_テスト_🔄.json";
            FileOperationException exception = new FileOperationException(
                ConversionErrorCode.FILE_NOT_FOUND, unicodeMessage);
            
            assertEquals(unicodeMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("🔄"));
        }
    }
}