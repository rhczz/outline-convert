package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConversionFailureException 单元测试
 */
@DisplayName("转换失败异常测试")
class ConversionFailureExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            String message = "转换失败";
            ConversionFailureException exception = new ConversionFailureException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("错误代码和消息构造函数")
        void testErrorCodeAndMessageConstructor() {
            String errorCode = "CONV_001";
            String message = "文件未找到";
            ConversionFailureException exception = new ConversionFailureException(errorCode, message);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("完整构造函数（包含原因）")
        void testFullConstructor() {
            String errorCode = "CONV_001";
            String message = "文件未找到";
            Throwable cause = new RuntimeException("原始异常");
            ConversionFailureException exception = new ConversionFailureException(errorCode, message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("null消息构造函数")
        void testNullMessageConstructor() {
            ConversionFailureException exception = new ConversionFailureException((String) null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getErrorCode());
        }

        @Test
        @DisplayName("null错误代码构造函数")
        void testNullErrorCodeConstructor() {
            String message = "测试消息";
            ConversionFailureException exception = new ConversionFailureException(null, message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getErrorCode());
        }

        @Test
        @DisplayName("null原因构造函数")
        void testNullCauseConstructor() {
            String errorCode = "CONV_001";
            String message = "测试消息";
            ConversionFailureException exception = new ConversionFailureException(errorCode, message, null);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("异常继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("应该继承自RuntimeException")
        void testExtendsRuntimeException() {
            ConversionFailureException exception = new ConversionFailureException("测试");
            
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("应该是运行时异常（不需要显式捕获）")
        void testIsRuntimeException() {
            // 这个测试验证我们可以抛出异常而不需要在方法签名中声明
            assertDoesNotThrow(() -> {
                throwConversionFailureException();
            });
        }

        private void throwConversionFailureException() {
            throw new ConversionFailureException("测试异常");
        }
    }

    @Nested
    @DisplayName("异常链测试")
    class ExceptionChainingTests {

        @Test
        @DisplayName("异常链应该正确传播")
        void testExceptionChaining() {
            RuntimeException originalException = new RuntimeException("原始异常");
            ConversionFailureException chainedException = 
                new ConversionFailureException("CONV_001", "链式异常", originalException);
            
            assertEquals(originalException, chainedException.getCause());
            assertEquals("原始异常", chainedException.getCause().getMessage());
        }

        @Test
        @DisplayName("多级异常链")
        void testMultiLevelExceptionChaining() {
            IllegalArgumentException level1 = new IllegalArgumentException("第一级异常");
            RuntimeException level2 = new RuntimeException("第二级异常", level1);
            ConversionFailureException level3 = 
                new ConversionFailureException("CONV_001", "第三级异常", level2);
            
            assertEquals(level2, level3.getCause());
            assertEquals(level1, level3.getCause().getCause());
        }

        @Test
        @DisplayName("获取根本原因")
        void testGetRootCause() {
            IllegalArgumentException rootCause = new IllegalArgumentException("根本原因");
            RuntimeException intermediateCause = new RuntimeException("中间异常", rootCause);
            ConversionFailureException topException = 
                new ConversionFailureException("CONV_001", "顶级异常", intermediateCause);
            
            // 通过循环找到根本原因
            Throwable cause = topException;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            
            assertEquals(rootCause, cause);
            assertEquals("根本原因", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("错误代码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误代码应该正确存储和检索")
        void testErrorCodeStorage() {
            String errorCode = "CONV_TEST_001";
            ConversionFailureException exception = new ConversionFailureException(errorCode, "测试消息");
            
            assertEquals(errorCode, exception.getErrorCode());
        }

        @Test
        @DisplayName("空字符串错误代码")
        void testEmptyErrorCode() {
            ConversionFailureException exception = new ConversionFailureException("", "测试消息");
            
            assertEquals("", exception.getErrorCode());
        }

        @Test
        @DisplayName("特殊字符错误代码")
        void testSpecialCharacterErrorCode() {
            String specialErrorCode = "CONV_测试_001_!@#";
            ConversionFailureException exception = new ConversionFailureException(specialErrorCode, "测试消息");
            
            assertEquals(specialErrorCode, exception.getErrorCode());
        }

        @Test
        @DisplayName("长错误代码")
        void testLongErrorCode() {
            String longErrorCode = "CONV_" + "A".repeat(100);
            ConversionFailureException exception = new ConversionFailureException(longErrorCode, "测试消息");
            
            assertEquals(longErrorCode, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("消息测试")
    class MessageTests {

        @Test
        @DisplayName("中文消息")
        void testChineseMessage() {
            String chineseMessage = "转换失败：不支持的文件格式";
            ConversionFailureException exception = new ConversionFailureException(chineseMessage);
            
            assertEquals(chineseMessage, exception.getMessage());
        }

        @Test
        @DisplayName("多行消息")
        void testMultiLineMessage() {
            String multiLineMessage = "第一行错误信息\n第二行错误详情\n第三行解决建议";
            ConversionFailureException exception = new ConversionFailureException(multiLineMessage);
            
            assertEquals(multiLineMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("\n"));
        }

        @Test
        @DisplayName("长消息")
        void testLongMessage() {
            String longMessage = "错误详情：" + "A".repeat(1000);
            ConversionFailureException exception = new ConversionFailureException(longMessage);
            
            assertEquals(longMessage, exception.getMessage());
            assertTrue(exception.getMessage().length() > 1000);
        }

        @Test 
        @DisplayName("特殊字符消息")
        void testSpecialCharacterMessage() {
            String specialMessage = "错误：文件路径包含特殊字符 <>&\"'";
            ConversionFailureException exception = new ConversionFailureException(specialMessage);
            
            assertEquals(specialMessage, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("异常操作测试")
    class ExceptionOperationTests {

        @Test
        @DisplayName("toString应该包含关键信息")
        void testToString() {
            ConversionFailureException exception = 
                new ConversionFailureException("CONV_001", "测试异常");
            
            String str = exception.toString();
            assertTrue(str.contains("ConversionFailureException"));
            assertTrue(str.contains("测试异常"));
        }

        @Test
        @DisplayName("printStackTrace应该正常工作")
        void testPrintStackTrace() {
            ConversionFailureException exception = 
                new ConversionFailureException("CONV_001", "测试异常", new RuntimeException("原因"));
            
            // 测试printStackTrace不会抛出异常
            assertDoesNotThrow(() -> {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                exception.printStackTrace(pw);
                
                String stackTrace = sw.toString();
                assertTrue(stackTrace.contains("ConversionFailureException"));
                assertTrue(stackTrace.contains("测试异常"));
            });
        }

        @Test
        @DisplayName("getStackTrace应该返回堆栈信息")
        void testGetStackTrace() {
            ConversionFailureException exception = 
                new ConversionFailureException("CONV_001", "测试异常");
            
            StackTraceElement[] stackTrace = exception.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("异常应该是可序列化的")
        void testSerializable() {
            assertTrue(java.io.Serializable.class.isAssignableFrom(ConversionFailureException.class));
        }

        @Test
        @DisplayName("序列化和反序列化")
        void testSerializationRoundTrip() throws Exception {
            ConversionFailureException original = 
                new ConversionFailureException("CONV_001", "测试异常", new RuntimeException("原因"));
            
            // 序列化
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            // 反序列化
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            ConversionFailureException deserialized = (ConversionFailureException) ois.readObject();
            ois.close();
            
            // 验证
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空构造函数消息")
        void testEmptyMessage() {
            ConversionFailureException exception = new ConversionFailureException("");
            
            assertEquals("", exception.getMessage());
            assertNull(exception.getErrorCode());
        }

        @Test
        @DisplayName("所有参数为null")
        void testAllNullParameters() {
            ConversionFailureException exception = new ConversionFailureException(null, null, null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("异常自引用（防止无限循环）")
        void testSelfReference() {
            ConversionFailureException exception = new ConversionFailureException("自引用测试");
            
            // 虽然不推荐，但应该处理自引用情况
            assertDoesNotThrow(() -> {
                exception.initCause(exception);
            }, "自引用应该被处理或抛出适当的异常");
        }

        @Test
        @DisplayName("异常循环引用")
        void testCircularReference() {
            ConversionFailureException exception1 = new ConversionFailureException("异常1");
            ConversionFailureException exception2 = new ConversionFailureException("异常2");
            
            assertDoesNotThrow(() -> {
                exception1.initCause(exception2);
                exception2.initCause(exception1);
            }, "循环引用应该被处理或抛出适当的异常");
        }
    }
}