package dev.hc.convert.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;

import java.io.*;
import java.util.Iterator;

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
    public OutlineDocument parse(File file) throws ParsingException {
        try {
            JsonNode rootNode = objectMapper.readTree(file);
            return parseJsonNode(rootNode, file.getName());
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) throws ParsingException {
        try {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            return parseJsonNode(rootNode, filename);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + filename, e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) throws ParsingException {
        try {
            JsonNode rootNode = objectMapper.readTree(data);
            return parseJsonNode(rootNode, filename);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse JSON file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"json"};
    }
    
    /**
     * 解析JSON节点为大纲文档
     */
    private OutlineDocument parseJsonNode(JsonNode jsonNode, String filename) throws ParsingException {
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat("JSON");
        
        try {
            if (jsonNode.isObject()) {
                // 处理对象格式的JSON
                parseObjectNode(jsonNode, document, filename);
            } else if (jsonNode.isArray()) {
                // 处理数组格式的JSON
                parseArrayNode(jsonNode, document, filename);
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
    private void parseObjectNode(JsonNode objectNode, OutlineDocument document, String filename) {
        // 检查是否是标准的大纲文档格式
        if (objectNode.has("title") || objectNode.has("rootNodes")) {
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
    private void parseArrayNode(JsonNode arrayNode, OutlineDocument document, String filename) {
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode item = arrayNode.get(i);
            
            OutlineNode node = new OutlineNode("Item " + (i + 1));
            parseNodeValue(item, node);
            document.addRootNode(node);
        }
    }
    
    /**
     * 解析标准大纲文档格式的JSON
     */
    private void parseDocumentFormat(JsonNode docNode, OutlineDocument document) {
        // 解析文档基本信息
        if (docNode.has("title")) {
            document.setTitle(docNode.get("title").asText());
        }
        if (docNode.has("description")) {
            document.setDescription(docNode.get("description").asText());
        }
        
        // 解析根节点
        JsonNode rootNodesArray = docNode.get("rootNodes");
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
        
        String title = nodeJson.has("title") ? nodeJson.get("title").asText() : "";
        String content = nodeJson.has("content") ? nodeJson.get("content").asText() : null;
        
        OutlineNode node = new OutlineNode(title, content);
        
        // 解析属性
        if (nodeJson.has("attributes")) {
            JsonNode attributesNode = nodeJson.get("attributes");
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
        JsonNode childrenNode = nodeJson.get("children");
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
                OutlineNode childNode = new OutlineNode("Item " + (i + 1));
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