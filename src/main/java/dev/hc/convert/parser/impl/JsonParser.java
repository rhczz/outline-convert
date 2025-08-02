package dev.hc.convert.parser.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hc.convert.FileType;
import dev.hc.convert.constant.FileValid;
import dev.hc.convert.constant.JsonSyntax;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;

/**
 * JSON格式解析器
 * 支持层次结构的JSON数据解析为大纲文档
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class JsonParser implements FileParser {

    private final ObjectMapper objectMapper;
    
    public JsonParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public OutlineDocument parse(File file) {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(file, "Input file cannot be null");
            Validate.isTrue(file.exists(), "File does not exist: %s", file);
            Validate.isTrue(file.isFile(), "Path is not a file: %s", file.getAbsolutePath());
            
            // 使用commons-io安全读取文件，防止内存溢出
            long fileSize = FileUtils.sizeOf(file);
            Validate.isTrue(fileSize <= FileValid.MAX_FILE_SIZE,
                "JSON file too large: %d bytes (max: %d bytes)", fileSize, FileValid.MAX_FILE_SIZE);
            
            // 安全读取文件内容
            String jsonContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            JsonNode rootNode = objectMapper.readTree(jsonContent);

            return parseJsonNode(rootNode);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("JSON file validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(inputStream, "Input stream cannot be null");
            
            // 使用commons-io安全读取流内容，防止内存溢出
            byte[] jsonData = IOUtils.toByteArray(inputStream, FileValid.MAX_FILE_SIZE);
            
            JsonNode rootNode = objectMapper.readTree(jsonData);

            return parseJsonNode(rootNode);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("JSON stream validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("exceeds the maximum")) {
                throw ConversionExceptionFactory.systemError("JSON stream too large (exceeds 50MB limit)", e);
            }
            throw ConversionExceptionFactory.parseError("Failed to read JSON stream: " + filename, e);
        } catch (ParsingException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + filename, e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) {
        try {
            Validate.notNull(data, "Input data cannot be null");
            Validate.isTrue(data.length > 0, "Input data cannot be empty");
            
            Validate.isTrue(data.length <= FileValid.MAX_FILE_SIZE,
                "JSON data too large: %d bytes (max: %d bytes)", data.length, FileValid.MAX_FILE_SIZE);
            
            JsonNode rootNode = objectMapper.readTree(data);

            return parseJsonNode(rootNode);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("JSON data validation failed: " + e.getMessage(), e);
        } catch (ParsingException | IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return FileType.JSON.getExtensions();
    }
    
    /**
     * 解析JSON节点为大纲文档
     */
    private OutlineDocument parseJsonNode(JsonNode jsonNode) {
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat(FileType.JSON.getDisplayName());
        
        try {
            if (jsonNode.isObject()) {
                // 处理对象格式的JSON
                parseObjectNode(jsonNode, document);
            } else if (jsonNode.isArray()) {
                // 处理数组格式的JSON
                parseArrayNode(jsonNode, document);
            } else {
                // 简单值类型，创建单个节点
                String title = jsonNode.isTextual() ? jsonNode.asText() : jsonNode.toString();
                document.addRootNode(new OutlineNode(title));
            }
            
            return document;
            
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON structure", e);
        }
    }
    
    /**
     * 解析JSON对象节点
     */
    private void parseObjectNode(JsonNode objectNode, OutlineDocument document) {
        // 检查是否是标准的大纲文档格式
        if (objectNode.has(JsonSyntax.TITLE_FIELD) || objectNode.has(JsonSyntax.ROOT_NODES_FIELD)) {
            parseDocumentFormat(objectNode, document);
        } else {
            // 通用对象格式，每个键值对作为一个节点
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = objectNode.get(fieldName);
                
                OutlineNode node = new OutlineNode(fieldName);
                parseNodeValue(fieldValue, node);
                document.addRootNode(node);
            }
        }
    }
    
    /**
     * 解析JSON数组节点
     */
    private void parseArrayNode(JsonNode arrayNode, OutlineDocument document) {
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode item = arrayNode.get(i);
            
            OutlineNode node = new OutlineNode(JsonSyntax.ARRAY_ITEM_PREFIX + (i + 1));
            parseNodeValue(item, node);
            document.addRootNode(node);
        }
    }
    
    /**
     * 解析标准大纲文档格式的JSON
     */
    private void parseDocumentFormat(JsonNode docNode, OutlineDocument document) {
        // 解析文档基本信息
        if (docNode.has(JsonSyntax.TITLE_FIELD)) {
            document.setTitle(docNode.get(JsonSyntax.TITLE_FIELD).asText());
        }
        if (docNode.has(JsonSyntax.DESCRIPTION_FIELD)) {
            document.setDescription(docNode.get(JsonSyntax.DESCRIPTION_FIELD).asText());
        }
        
        // 解析根节点
        JsonNode rootNodesArray = docNode.get(JsonSyntax.ROOT_NODES_FIELD);
        if (rootNodesArray != null && rootNodesArray.isArray()) {
            for (JsonNode nodeJson : rootNodesArray) {
                OutlineNode node = parseOutlineNode(nodeJson);
                if (node != null) {
                    document.addRootNode(node);
                }
            }
        }
    }
    
    /**
     * 解析大纲节点
     */
    private OutlineNode parseOutlineNode(JsonNode nodeJson) {
        if (!nodeJson.isObject()) {
            return null;
        }
        
        String title = nodeJson.has(JsonSyntax.TITLE_FIELD) ? nodeJson.get(JsonSyntax.TITLE_FIELD).asText() : StringUtils.EMPTY;
        String content = nodeJson.has(JsonSyntax.CONTENT_FIELD) ? nodeJson.get(JsonSyntax.CONTENT_FIELD).asText() : null;
        
        OutlineNode node = new OutlineNode(title, content);
        
        // 解析属性
        if (nodeJson.has(JsonSyntax.ATTRIBUTES_FIELD)) {
            JsonNode attributesNode = nodeJson.get(JsonSyntax.ATTRIBUTES_FIELD);
            if (attributesNode.isObject()) {
                Iterator<String> fieldNames = attributesNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode fieldValue = attributesNode.get(fieldName);
                    node.setAttribute(fieldName, convertJsonValue(fieldValue));
                }
            }
        }
        
        // 解析子节点
        JsonNode childrenNode = nodeJson.get(JsonSyntax.CHILDREN_FIELD);
        if (childrenNode != null && childrenNode.isArray()) {
            for (JsonNode childJson : childrenNode) {
                OutlineNode child = parseOutlineNode(childJson);
                if (child != null) {
                    node.addChild(child);
                }
            }
        }
        
        return node;
    }
    
    /**
     * 解析节点值
     */
    private void parseNodeValue(JsonNode valueNode, OutlineNode parentNode) {
        if (valueNode.isObject()) {
            // 对象类型，每个字段作为子节点
            Iterator<String> fieldNames = valueNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = valueNode.get(fieldName);
                
                OutlineNode childNode = new OutlineNode(fieldName);
                parseNodeValue(fieldValue, childNode);
                parentNode.addChild(childNode);
            }
        } else if (valueNode.isArray()) {
            // 数组类型，每个元素作为子节点
            for (int i = 0; i < valueNode.size(); i++) {
                JsonNode item = valueNode.get(i);
                OutlineNode childNode = new OutlineNode(JsonSyntax.ARRAY_ITEM_PREFIX + (i + 1));
                parseNodeValue(item, childNode);
                parentNode.addChild(childNode);
            }
        } else {
            // 简单值类型，设置为内容
            String content = valueNode.isTextual() ? valueNode.asText() : valueNode.toString();
            parentNode.setContent(content);
        }
    }
    
    /**
     * 转换JSON值为Java对象
     */
    private Object convertJsonValue(JsonNode valueNode) {
        if (valueNode.isTextual()) {
            return valueNode.asText();
        } else if (valueNode.isNumber()) {
            if (valueNode.isInt()) {
                return valueNode.asInt();
            } else if (valueNode.isLong()) {
                return valueNode.asLong();
            } else {
                return valueNode.asDouble();
            }
        } else if (valueNode.isBoolean()) {
            return valueNode.asBoolean();
        } else if (valueNode.isNull()) {
            return null;
        } else {
            return valueNode.toString();
        }
    }
}