package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ParsingException 单元测试
 */
@DisplayName("解析异常测试")
class ParsingExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("错误代码和消息构造函数")
        void testErrorCodeAndMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.PARSE_ERROR;
            String message = "JSON解析失败";
            
            ParsingException exception = new ParsingException(errorCode, message);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("完整构造函数（包含原因）")
        void testFullConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.INVALID_FILE_STRUCTURE;
            String message = "文件结构无效";
            RuntimeException cause = new RuntimeException("解析异常");
            
            ParsingException exception = new ParsingException(errorCode, message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("null消息构造函数")
        void testNullMessageConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.PARSE_ERROR;
            
            ParsingException exception = new ParsingException(errorCode, null);
            
            assertNull(exception.getMessage());
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("null原因构造函数")
        void testNullCauseConstructor() {
            ConversionErrorCode errorCode = ConversionErrorCode.INVALID_FILE_STRUCTURE;
            String message = "结构错误";
            
            ParsingException exception = new ParsingException(errorCode, message, null);
            
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
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "测试");
            
            assertTrue(exception instanceof ConversionFailureException);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("应该正确设置错误代码")
        void testErrorCodeFromConversionErrorCode() {
            ConversionErrorCode errorCode = ConversionErrorCode.PARSE_ERROR;
            ParsingException exception = new ParsingException(errorCode, "测试消息");
            
            assertEquals(errorCode.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("解析相关错误代码测试")
    class ParsingErrorCodeTests {

        @Test
        @DisplayName("解析错误")
        void testParseError() {
            String errorDetails = "第3行第15列: 意外的字符 '}'";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR,
                ConversionErrorCode.PARSE_ERROR.formatMessage(errorDetails));
            
            assertEquals(ConversionErrorCode.PARSE_ERROR.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(errorDetails));
        }

        @Test
        @DisplayName("无效文件结构错误")
        void testInvalidFileStructureError() {
            String structureError = "缺少必需的 'rootNodes' 字段";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE,
                ConversionErrorCode.INVALID_FILE_STRUCTURE.formatMessage(structureError));
            
            assertEquals(ConversionErrorCode.INVALID_FILE_STRUCTURE.getCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(structureError));
        }
    }

    @Nested
    @DisplayName("异常链测试")
    class ExceptionChainingTests {

        @Test
        @DisplayName("JSON解析异常链")
        void testJsonParseExceptionCause() {
            com.fasterxml.jackson.core.JsonParseException jsonException = 
                new com.fasterxml.jackson.core.JsonParseException(null, "JSON语法错误");
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "JSON解析失败", jsonException);
            
            assertEquals(jsonException, exception.getCause());
            assertTrue(exception.getCause() instanceof com.fasterxml.jackson.core.JsonParseException);
        }

        @Test
        @DisplayName("XML解析异常链")
        void testXmlParseExceptionCause() {
            org.xml.sax.SAXException saxException = new org.xml.sax.SAXException("XML格式错误");
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "XML解析失败", saxException);
            
            assertEquals(saxException, exception.getCause());
            assertTrue(exception.getCause() instanceof org.xml.sax.SAXException);
        }

        @Test
        @DisplayName("NumberFormatException异常链")
        void testNumberFormatExceptionCause() {
            NumberFormatException numberException = new NumberFormatException("无效的数字格式");
            ParsingException exception = new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE, "数字解析失败", numberException);
            
            assertEquals(numberException, exception.getCause());
            assertTrue(exception.getCause() instanceof NumberFormatException);
        }

        @Test
        @DisplayName("多级解析异常链")
        void testMultiLevelParsingExceptionChain() {
            IllegalArgumentException argException = new IllegalArgumentException("参数无效");
            RuntimeException runtimeException = new RuntimeException("运行时错误", argException);
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "复合解析错误", runtimeException);
            
            assertEquals(runtimeException, exception.getCause());
            assertEquals(argException, exception.getCause().getCause());
        }
    }

    @Nested
    @DisplayName("不同文件格式解析错误测试")
    class DifferentFormatParsingTests {

        @Test
        @DisplayName("JSON解析错误消息")
        void testJsonParsingErrorMessage() {
            String jsonError = "JSON第5行: 缺少逗号分隔符";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR,
                ConversionErrorCode.PARSE_ERROR.formatMessage(jsonError));
            
            assertTrue(exception.getMessage().contains("JSON"));
            assertTrue(exception.getMessage().contains("第5行"));
            assertTrue(exception.getMessage().contains("逗号"));
        }

        @Test
        @DisplayName("Markdown解析错误消息")
        void testMarkdownParsingErrorMessage() {
            String markdownError = "Markdown标题层级错误: 从H1直接跳到H3";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE,
                ConversionErrorCode.INVALID_FILE_STRUCTURE.formatMessage(markdownError));
            
            assertTrue(exception.getMessage().contains("Markdown"));
            assertTrue(exception.getMessage().contains("标题层级"));
            assertTrue(exception.getMessage().contains("H1"));
            assertTrue(exception.getMessage().contains("H3"));
        }

        @Test
        @DisplayName("XML/OPML解析错误消息")
        void testXmlOpmlParsingErrorMessage() {
            String xmlError = "OPML第10行: 未闭合的<outline>标签";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR,
                ConversionErrorCode.PARSE_ERROR.formatMessage(xmlError));
            
            assertTrue(exception.getMessage().contains("OPML"));
            assertTrue(exception.getMessage().contains("第10行"));
            assertTrue(exception.getMessage().contains("outline"));
        }

        @Test
        @DisplayName("XMind解析错误消息")
        void testXMindParsingErrorMessage() {
            String xmindError = "XMind文件损坏: 无法读取主题结构";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE,
                ConversionErrorCode.INVALID_FILE_STRUCTURE.formatMessage(xmindError));
            
            assertTrue(exception.getMessage().contains("XMind"));
            assertTrue(exception.getMessage().contains("损坏"));
            assertTrue(exception.getMessage().contains("主题结构"));
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("异常应该是可序列化的")
        void testSerializable() {
            assertTrue(java.io.Serializable.class.isAssignableFrom(ParsingException.class));
        }

        @Test
        @DisplayName("序列化和反序列化")
        void testSerializationRoundTrip() throws Exception {
            ParsingException original = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, 
                "测试解析异常",
                new RuntimeException("原始异常"));
            
            // 序列化
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            // 反序列化
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            ParsingException deserialized = (ParsingException) ois.readObject();
            ois.close();
            
            // 验证
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("调试和诊断测试")
    class DebuggingTests {

        @Test
        @DisplayName("toString应该包含解析相关信息")
        void testToString() {
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "JSON解析失败: 语法错误");
            
            String str = exception.toString();
            assertTrue(str.contains("ParsingException"));
            assertTrue(str.contains("JSON解析失败"));
            assertTrue(str.contains("语法错误"));
        }

        @Test
        @DisplayName("printStackTrace应该显示解析堆栈")
        void testPrintStackTrace() {
            com.fasterxml.jackson.core.JsonParseException jsonException = 
                new com.fasterxml.jackson.core.JsonParseException(null, "JSON错误");
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "解析失败", jsonException);
            
            assertDoesNotThrow(() -> {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                exception.printStackTrace(pw);
                
                String stackTrace = sw.toString();
                assertTrue(stackTrace.contains("ParsingException"));
                assertTrue(stackTrace.contains("解析失败"));
                assertTrue(stackTrace.contains("JsonParseException"));
            });
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空解析错误消息")
        void testEmptyParsingMessage() {
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, "");
            
            assertEquals("", exception.getMessage());
            assertEquals(ConversionErrorCode.PARSE_ERROR.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("长解析错误消息")
        void testLongParsingMessage() {
            String longMessage = "解析错误详情：" + "字符".repeat(5000);
            ParsingException exception = new ParsingException(
                ConversionErrorCode.INVALID_FILE_STRUCTURE, longMessage);
            
            assertEquals(longMessage, exception.getMessage());
            assertTrue(exception.getMessage().length() > 5000);
        }

        @Test
        @DisplayName("包含特殊字符的解析错误")
        void testSpecialCharacterParsingMessage() {
            String specialMessage = "解析错误: 发现非法字符 \\x00, \\n, \\r, \\t";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, specialMessage);
            
            assertTrue(exception.getMessage().contains("\\x00"));
            assertTrue(exception.getMessage().contains("\\n"));
            assertTrue(exception.getMessage().contains("\\r"));
            assertTrue(exception.getMessage().contains("\\t"));
        }

        @Test
        @DisplayName("Unicode解析错误消息")
        void testUnicodeParsingMessage() {
            String unicodeMessage = "解析错误: 不支持的Unicode字符 🔄📄🚫";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR, unicodeMessage);
            
            assertEquals(unicodeMessage, exception.getMessage());
            assertTrue(exception.getMessage().contains("🔄"));
            assertTrue(exception.getMessage().contains("📄"));
            assertTrue(exception.getMessage().contains("🚫"));
        }

        @Test
        @DisplayName("行列位置信息")
        void testLineColumnPositionInfo() {
            String positionMessage = "第42行第15列: 意外的结束符";
            ParsingException exception = new ParsingException(
                ConversionErrorCode.PARSE_ERROR,
                ConversionErrorCode.PARSE_ERROR.formatMessage(positionMessage));
            
            assertTrue(exception.getMessage().contains("第42行"));
            assertTrue(exception.getMessage().contains("第15列"));
            assertTrue(exception.getMessage().contains("意外的结束符"));
        }
    }
}