package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConvertException 单元测试
 */
@DisplayName("转换异常测试")
class ConvertExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("错误代码和消息构造函数")
        void testErrorCodeAndMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.CONVERSION_FAILED;
            String message = "转换操作失败";
            
            ConvertException exception = new ConvertException(errorCode, message);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("完整构造函数（包含原因）")
        void testFullConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.CONVERSION_NOT_SUPPORTED;
            String message = "不支持的转换类型";
            RuntimeException cause = new RuntimeException("转换器异常");
            
            ConvertException exception = new ConvertException(errorCode, message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("null消息构造函数")
        void testNullMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.CONVERSION_FAILED;
            
            ConvertException exception = new ConvertException(errorCode, null);
            
            assertNull(exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("null原因构造函数")
        void testNullCauseConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.CONVERSION_NOT_SUPPORTED;
            String message = "转换失败";
            
            ConvertException exception = new ConvertException(errorCode, message, null);
            
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
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "测试");
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("应该正确设置错误代码")
        void testErrorCodeFromConversionErrorCode() {
            ConversionErrorCode errorCode = ConversionErrorCode.CONVERSION_FAILED;
            ConvertException exception = new ConvertException(errorCode, "测试消息");
            
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("转换相关错误代码测试")
    class ConversionErrorCodeTests {

        @Test
        @DisplayName("不支持的转换类型错误")
        void testConversionNotSupportedError() {
            String sourceFormat = "JSON";
            String targetFormat = "XML";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED,
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage(sourceFormat, targetFormat));
            
            assertEquals(ConversionErrorCode.CONVERSION_NOT_SUPPORTED.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(sourceFormat));
            assertTrue(exception.getMessage().contains(targetFormat));
        }

        @Test
        @DisplayName("转换失败错误")
        void testConversionFailedError() {
            String failureDetails = "内存不足，无法完成转换";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(failureDetails));
            
            assertEquals(ConversionErrorCode.CONVERSION_FAILED.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(failureDetails));
        }
    }

    @Nested
    @DisplayName("异常链测试")
    class ExceptionChainingTests {

        @Test
        @DisplayName("OutOfMemoryError异常链")
        void testOutOfMemoryErrorCause() {
            OutOfMemoryError memoryError = new OutOfMemoryError("Java堆空间不足");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "内存不足导致转换失败", memoryError);
            
            assertEquals(memoryError, exception.getCause());
            assertTrue(exception.getCause() instanceof OutOfMemoryError);
        }

        @Test
        @DisplayName("UnsupportedOperationException异常链")
        void testUnsupportedOperationExceptionCause() {
            UnsupportedOperationException unsupportedException = 
                new UnsupportedOperationException("该格式转换未实现");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED, "转换操作不支持", unsupportedException);
            
            assertEquals(unsupportedException, exception.getCause());
            assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        }

        @Test
        @DisplayName("IOException异常链")
        void testIOExceptionCause() {
            java.io.IOException ioException = new java.io.IOException("输出文件写入失败");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "转换过程中IO错误", ioException);
            
            assertEquals(ioException, exception.getCause());
            assertTrue(exception.getCause() instanceof java.io.IOException);
        }

        @Test
        @DisplayName("多级转换异常链")
        void testMultiLevelConversionExceptionChain() {
            IllegalStateException stateException = new IllegalStateException("转换器状态异常");
            RuntimeException runtimeException = new RuntimeException("运行时转换错误", stateException);
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "复合转换错误", runtimeException);
            
            assertEquals(runtimeException, exception.getCause());
            assertEquals(stateException, exception.getCause().getCause());
        }
    }

    @Nested
    @DisplayName("不同转换场景测试")
    class DifferentConversionScenariosTests {

        @Test
        @DisplayName("JSON到Markdown转换错误")
        void testJsonToMarkdownConversionError() {
            String errorMessage = "JSON到Markdown转换失败: 无法处理嵌套对象结构";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(errorMessage));
            
            assertTrue(exception.getMessage().contains("JSON"));
            assertTrue(exception.getMessage().contains("Markdown"));
            assertTrue(exception.getMessage().contains("嵌套对象"));
        }

        @Test
        @DisplayName("Markdown到XMind转换错误")
        void testMarkdownToXMindConversionError() {
            String errorMessage = "Markdown到XMind转换失败: 标题层级过深";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(errorMessage));
            
            assertTrue(exception.getMessage().contains("Markdown"));
            assertTrue(exception.getMessage().contains("XMind"));
            assertTrue(exception.getMessage().contains("标题层级"));
        }

        @Test
        @DisplayName("OPML到JSON转换错误")
        void testOpmlToJsonConversionError() {
            String errorMessage = "OPML到JSON转换失败: 属性映射冲突";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(errorMessage));
            
            assertTrue(exception.getMessage().contains("OPML"));
            assertTrue(exception.getMessage().contains("JSON"));
            assertTrue(exception.getMessage().contains("属性映射"));
        }

        @Test
        @DisplayName("不支持的转换路径")
        void testUnsupportedConversionPath() {
            String sourceFormat = "JSON";
            String targetFormat = "PDF";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED,
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage(sourceFormat, targetFormat));
            
            assertTrue(exception.getMessage().contains(sourceFormat));
            assertTrue(exception.getMessage().contains(targetFormat));
            assertTrue(exception.getMessage().contains("->"));
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("异常应该是可序列化的")
        void testSerializable() {
            assertTrue(java.io.Serializable.class.isAssignableFrom(ConvertException.class));
        }

        @Test
        @DisplayName("序列化和反序列化")
        void testSerializationRoundTrip() throws Exception {
            ConvertException original = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, 
                "测试转换异常",
                new RuntimeException("原始转换异常"));
            
            // 序列化
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            // 反序列化
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            ConvertException deserialized = (ConvertException) ois.readObject();
            ois.close();
            
            // 验证
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("性能和资源管理测试")
    class PerformanceAndResourceTests {

        @Test
        @DisplayName("大文件转换失败异常")
        void testLargeFileConversionFailure() {
            String errorMessage = "转换失败: 文件大小超过100MB限制";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED,
                ConversionErrorCode.CONVERSION_FAILED.formatMessage(errorMessage));
            
            assertTrue(exception.getMessage().contains("100MB"));
            assertTrue(exception.getMessage().contains("限制"));
        }

        @Test
        @DisplayName("内存不足转换异常")
        void testOutOfMemoryConversionException() {
            OutOfMemoryError memoryError = new OutOfMemoryError("无法分配更多内存");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, 
                "内存不足，转换中断", 
                memoryError);
            
            assertTrue(exception.getMessage().contains("内存不足"));
            assertEquals(memoryError, exception.getCause());
        }

        @Test
        @DisplayName("超时转换异常")
        void testTimeoutConversionException() {
            java.util.concurrent.TimeoutException timeoutException = 
                new java.util.concurrent.TimeoutException("转换操作超时");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, 
                "转换超时，操作被中断", 
                timeoutException);
            
            assertTrue(exception.getMessage().contains("超时"));
            assertTrue(exception.getCause() instanceof java.util.concurrent.TimeoutException);
        }
    }

    @Nested
    @DisplayName("调试和诊断测试")
    class DebuggingTests {

        @Test
        @DisplayName("toString应该包含转换相关信息")
        void testToString() {
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "JSON转换失败: 数据结构复杂");
            
            String str = exception.toString();
            assertTrue(str.contains("ConvertException"));
            assertTrue(str.contains("JSON转换失败"));
            assertTrue(str.contains("数据结构复杂"));
        }

        @Test
        @DisplayName("printStackTrace应该显示转换堆栈")
        void testPrintStackTrace() {
            UnsupportedOperationException unsupportedException = 
                new UnsupportedOperationException("转换器未实现");
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED, "转换不支持", unsupportedException);
            
            assertDoesNotThrow(() -> {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                exception.printStackTrace(pw);
                
                String stackTrace = sw.toString();
                assertTrue(stackTrace.contains("ConvertException"));
                assertTrue(stackTrace.contains("转换不支持"));
                assertTrue(stackTrace.contains("UnsupportedOperationException"));
            });
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空转换错误消息")
        void testEmptyConversionMessage() {
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, "");
            
            assertEquals("", exception.getMessage());
            assertEquals(ConversionErrorCode.CONVERSION_FAILED.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("长转换错误消息")
        void testLongConversionMessage() {
            String longMessage = "转换错误详情：" + "详细信息".repeat(2000);
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, longMessage);
            
            assertEquals(longMessage, exception.getMessage());
            assertTrue(exception.getMessage().length() > 8000);
        }

        @Test
        @DisplayName("多行转换错误消息")
        void testMultiLineConversionMessage() {
            String multiLineMessage = """
                转换失败详情:
                - 源格式: JSON
                - 目标格式: XMind
                - 错误位置: 第42个节点
                - 错误原因: 属性冲突
                """;
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, multiLineMessage);
            
            assertTrue(exception.getMessage().contains("源格式"));
            assertTrue(exception.getMessage().contains("目标格式"));
            assertTrue(exception.getMessage().contains("第42个节点"));
            assertTrue(exception.getMessage().contains("\n"));
        }

        @Test
        @DisplayName("包含特殊字符的转换错误")
        void testSpecialCharacterConversionMessage() {
            String specialMessage = "转换错误: 文件名包含特殊字符 <>&\"'%$#@!";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, specialMessage);
            
            assertTrue(exception.getMessage().contains("<>&\"'%$#@!"));
        }

        @Test
        @DisplayName("Unicode转换错误消息")
        void testUnicodeConversionMessage() {
            String unicodeMessage = "转换错误: 文档包含特殊符号 ➤➢🔄📝✓✗";
            ConvertException exception = new ConvertException(
                ConversionErrorCode.CONVERSION_FAILED, unicodeMessage);
            
            assertEquals(unicodeMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("➤"));
            assertTrue(exception.getMessage().contains("🔄"));
            assertTrue(exception.getMessage().contains("📝"));
        }

        @Test
        @DisplayName("格式组合错误消息")
        void testFormatCombinationErrorMessage() {
            String[] sourceFormats = {"JSON", "Markdown", "OPML", "XMind"};
            String[] targetFormats = {"XML", "PDF", "DOC", "TXT"};
            
            for (String source : sourceFormats) {
                for (String target : targetFormats) {
                    ConvertException exception = new ConvertException(
                        ConversionErrorCode.CONVERSION_NOT_SUPPORTED,
                        ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage(source, target));
                    
                    assertTrue(exception.getMessage().contains(source));
                    assertTrue(exception.getMessage().contains(target));
                    assertTrue(exception.getMessage().contains("->"));
                }
            }
        }
    }
}