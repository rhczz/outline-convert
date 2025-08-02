package dev.hc.convert.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConversionErrorCode 单元测试
 */
@DisplayName("转换错误代码测试")
class ConversionErrorCodeTest {

    @Nested
    @DisplayName("枚举常量测试")
    class EnumConstantsTests {

        @Test
        @DisplayName("文件名为空错误")
        void testFileNameEmpty() {
            ConversionErrorCode error = ConversionErrorCode.FILE_NAME_EMPTY;
            
            assertEquals("CONV_400", error.getCode());
            assertEquals("File name cannot be empty", error.getMessageTemplate());
        }

        @Test
        @DisplayName("文件未找到错误")
        void testFileNotFound() {
            ConversionErrorCode error = ConversionErrorCode.FILE_NOT_FOUND;
            
            assertEquals("CONV_001", error.getCode());
            assertEquals("File not found: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("文件读取错误")
        void testFileReadError() {
            ConversionErrorCode error = ConversionErrorCode.FILE_READ_ERROR;
            
            assertEquals("CONV_002", error.getCode());
            assertEquals("Failed to read file: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("文件写入错误")
        void testFileWriteError() {
            ConversionErrorCode error = ConversionErrorCode.FILE_WRITE_ERROR;
            
            assertEquals("CONV_003", error.getCode());
            assertEquals("Failed to write file: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("不支持的文件格式错误")
        void testUnsupportedFileFormat() {
            ConversionErrorCode error = ConversionErrorCode.UNSUPPORTED_FILE_FORMAT;
            
            assertEquals("CONV_004", error.getCode());
            assertEquals("Unsupported file format: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("解析错误")
        void testParseError() {
            ConversionErrorCode error = ConversionErrorCode.PARSE_ERROR;
            
            assertEquals("CONV_101", error.getCode());
            assertEquals("Failed to parse file: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("无效文件结构错误")
        void testInvalidFileStructure() {
            ConversionErrorCode error = ConversionErrorCode.INVALID_FILE_STRUCTURE;
            
            assertEquals("CONV_102", error.getCode());
            assertEquals("Invalid file structure: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("不支持的转换类型错误")
        void testConversionNotSupported() {
            ConversionErrorCode error = ConversionErrorCode.CONVERSION_NOT_SUPPORTED;
            
            assertEquals("CONV_201", error.getCode());
            assertEquals("Unsupported conversion type: %s -> %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("转换失败错误")
        void testConversionFailed() {
            ConversionErrorCode error = ConversionErrorCode.CONVERSION_FAILED;
            
            assertEquals("CONV_202", error.getCode());
            assertEquals("Conversion failed: %s", error.getMessageTemplate());
        }

        @Test
        @DisplayName("系统错误")
        void testSystemError() {
            ConversionErrorCode error = ConversionErrorCode.SYSTEM_ERROR;
            
            assertEquals("CONV_901", error.getCode());
            assertEquals("System error: %s", error.getMessageTemplate());
        }
    }

    @Nested
    @DisplayName("消息格式化测试")
    class MessageFormattingTests {

        @Test
        @DisplayName("无参数消息格式化")
        void testFormatMessageNoArgs() {
            String message = ConversionErrorCode.FILE_NAME_EMPTY.formatMessage();
            assertEquals("File name cannot be empty", message);
        }

        @Test
        @DisplayName("单参数消息格式化")
        void testFormatMessageSingleArg() {
            String message = ConversionErrorCode.FILE_NOT_FOUND.formatMessage("/path/to/file.json");
            assertEquals("File not found: /path/to/file.json", message);
        }

        @Test
        @DisplayName("多参数消息格式化")
        void testFormatMessageMultipleArgs() {
            String message = ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage("JSON", "XML");
            assertEquals("Unsupported conversion type: JSON -> XML", message);
        }

        @Test
        @DisplayName("null参数消息格式化")
        void testFormatMessageNullArg() {
            String message = ConversionErrorCode.FILE_NOT_FOUND.formatMessage((Object) null);
            assertEquals("File not found: null", message);
        }

        @Test
        @DisplayName("空字符串参数消息格式化")
        void testFormatMessageEmptyArg() {
            String message = ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.formatMessage("");
            assertEquals("Unsupported file format: ", message);
        }

        @Test
        @DisplayName("特殊字符参数消息格式化")
        void testFormatMessageSpecialChars() {
            String message = ConversionErrorCode.PARSE_ERROR.formatMessage("文件包含特殊字符: %s & <>");
            assertEquals("Failed to parse file: 文件包含特殊字符: %s & <>", message);
        }

        @Test
        @DisplayName("数字参数消息格式化")
        void testFormatMessageNumericArg() {
            String message = ConversionErrorCode.SYSTEM_ERROR.formatMessage(404);
            assertEquals("System error: 404", message);
        }

        @Test
        @DisplayName("布尔参数消息格式化")
        void testFormatMessageBooleanArg() {
            String message = ConversionErrorCode.SYSTEM_ERROR.formatMessage(true);
            assertEquals("System error: true", message);
        }
    }

    @Nested
    @DisplayName("枚举方法测试")
    class EnumMethodsTests {

        @Test
        @DisplayName("values方法应该返回所有枚举值")
        void testValues() {
            ConversionErrorCode[] values = ConversionErrorCode.values();
            
            assertEquals(9, values.length);
            assertTrue(java.util.Arrays.asList(values).contains(ConversionErrorCode.FILE_NAME_EMPTY));
            assertTrue(java.util.Arrays.asList(values).contains(ConversionErrorCode.FILE_NOT_FOUND));
            assertTrue(java.util.Arrays.asList(values).contains(ConversionErrorCode.SYSTEM_ERROR));
        }

        @Test
        @DisplayName("valueOf方法应该正确返回枚举值")
        void testValueOf() {
            assertEquals(ConversionErrorCode.FILE_NOT_FOUND, 
                ConversionErrorCode.valueOf("FILE_NOT_FOUND"));
            assertEquals(ConversionErrorCode.PARSE_ERROR, 
                ConversionErrorCode.valueOf("PARSE_ERROR"));
            assertEquals(ConversionErrorCode.SYSTEM_ERROR, 
                ConversionErrorCode.valueOf("SYSTEM_ERROR"));
        }

        @Test
        @DisplayName("valueOf不存在的值应该抛出异常")
        void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> {
                ConversionErrorCode.valueOf("INVALID_ERROR");
            });
        }
    }

    @Nested
    @DisplayName("错误代码分类测试")
    class ErrorCodeCategoryTests {

        @Test
        @DisplayName("文件相关错误代码应该以CONV_0开头")
        void testFileErrorCodes() {
            assertTrue(ConversionErrorCode.FILE_NOT_FOUND.getCode().startsWith("CONV_0"));
            assertTrue(ConversionErrorCode.FILE_READ_ERROR.getCode().startsWith("CONV_0"));
            assertTrue(ConversionErrorCode.FILE_WRITE_ERROR.getCode().startsWith("CONV_0"));
            assertTrue(ConversionErrorCode.UNSUPPORTED_FILE_FORMAT.getCode().startsWith("CONV_0"));
        }

        @Test
        @DisplayName("解析相关错误代码应该以CONV_1开头")
        void testParsingErrorCodes() {
            assertTrue(ConversionErrorCode.PARSE_ERROR.getCode().startsWith("CONV_1"));
            assertTrue(ConversionErrorCode.INVALID_FILE_STRUCTURE.getCode().startsWith("CONV_1"));
        }

        @Test
        @DisplayName("转换相关错误代码应该以CONV_2开头")
        void testConversionErrorCodes() {
            assertTrue(ConversionErrorCode.CONVERSION_NOT_SUPPORTED.getCode().startsWith("CONV_2"));
            assertTrue(ConversionErrorCode.CONVERSION_FAILED.getCode().startsWith("CONV_2"));
        }

        @Test
        @DisplayName("系统相关错误代码应该以CONV_9开头")
        void testSystemErrorCodes() {
            assertTrue(ConversionErrorCode.SYSTEM_ERROR.getCode().startsWith("CONV_9"));
        }

        @Test
        @DisplayName("参数相关错误代码应该以CONV_4开头")
        void testParameterErrorCodes() {
            assertTrue(ConversionErrorCode.FILE_NAME_EMPTY.getCode().startsWith("CONV_4"));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("错误代码不应该为空")
        void testErrorCodeNotEmpty() {
            for (ConversionErrorCode errorCode : ConversionErrorCode.values()) {
                assertNotNull(errorCode.getCode());
                assertFalse(errorCode.getCode().isEmpty());
            }
        }

        @Test
        @DisplayName("消息模板不应该为空")
        void testMessageTemplateNotEmpty() {
            for (ConversionErrorCode errorCode : ConversionErrorCode.values()) {
                assertNotNull(errorCode.getMessageTemplate());
                assertFalse(errorCode.getMessageTemplate().isEmpty());
            }
        }

        @Test
        @DisplayName("错误代码应该是唯一的")
        void testErrorCodesUnique() {
            ConversionErrorCode[] values = ConversionErrorCode.values();
            java.util.Set<String> codes = new java.util.HashSet<>();
            
            for (ConversionErrorCode errorCode : values) {
                assertTrue(codes.add(errorCode.getCode()), 
                    "Duplicate error code found: " + errorCode.getCode());
            }
            
            assertEquals(values.length, codes.size());
        }

        @Test
        @DisplayName("过多参数的消息格式化")
        void testFormatMessageTooManyArgs() {
            // 模板只有一个占位符，但传入多个参数
            String message = ConversionErrorCode.FILE_NOT_FOUND.formatMessage("arg1", "arg2", "arg3");
            assertEquals("File not found: arg1", message);
        }

        @Test
        @DisplayName("参数不足的消息格式化")
        void testFormatMessageTooFewArgs() {
            // 模板有两个占位符，但只传入一个参数
            assertThrows(java.util.MissingFormatArgumentException.class, () -> {
                ConversionErrorCode.CONVERSION_NOT_SUPPORTED.formatMessage("JSON");
            });
        }
    }
}