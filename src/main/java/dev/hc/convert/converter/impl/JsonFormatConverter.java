package dev.hc.convert.converter.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;

/**
 * JSON格式转换器
 * 将大纲文档转换为JSON格式输出
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class JsonFormatConverter implements FormatConverter {
    
    private final ObjectMapper objectMapper;
    
    public JsonFormatConverter() {
        this.objectMapper = new ObjectMapper();
        // 设置美化输出
        this.objectMapper.writerWithDefaultPrettyPrinter();
    }
    
    @Override
    public void convert(OutlineDocument document, File outputFile) {
        try {
            ObjectNode jsonDocument = convertToJsonNode(document);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, jsonDocument);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileWriteError(outputFile, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to JSON", e);
        }
    }
    
    @Override
    public void convert(OutlineDocument document, OutputStream outputStream) {
        try {
            ObjectNode jsonDocument = convertToJsonNode(document);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, jsonDocument);
        } catch (IOException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to write JSON to stream", e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to JSON", e);
        }
    }
    
    @Override
    public byte[] convertToBytes(OutlineDocument document) {
        try {
            ObjectNode jsonDocument = convertToJsonNode(document);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonDocument);
        } catch (JsonProcessingException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to JSON", e);
        }
    }
    
    @Override
    public String getFormatName() {
        return "JSON";
    }
    
    @Override
    public String getDefaultExtension() {
        return "json";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    /**
     * 将大纲文档转换为JSON节点
     */
    private ObjectNode convertToJsonNode(OutlineDocument document) {
        ObjectNode docNode = objectMapper.createObjectNode();
        
        // 基本信息
        if (document.getTitle() != null) {
            docNode.put("title", document.getTitle());
        }
        
        if (document.getDescription() != null) {
            docNode.put("description", document.getDescription());
        }
        
        // 元数据
        Map<String, Object> metadata = document.getMetadata();
        if (!metadata.isEmpty()) {
            ObjectNode metadataNode = objectMapper.createObjectNode();
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                addValueToNode(metadataNode, entry.getKey(), entry.getValue());
            }
            docNode.set("metadata", metadataNode);
        }
        
        // 时间信息
        docNode.put("createTime", document.getCreateTime());
        docNode.put("modifyTime", document.getModifyTime());
        
        if (document.getSourceFormat() != null) {
            docNode.put("sourceFormat", document.getSourceFormat());
        }
        
        // 根节点
        ArrayNode rootNodesArray = objectMapper.createArrayNode();
        for (OutlineNode rootNode : document.getRootNodes()) {
            ObjectNode nodeJson = convertNodeToJson(rootNode);
            rootNodesArray.add(nodeJson);
        }
        docNode.set("rootNodes", rootNodesArray);
        
        // 统计信息
        docNode.put("totalNodes", document.getTotalNodesCount());
        docNode.put("maxDepth", document.getMaxDepth());
        
        return docNode;
    }
    
    /**
     * 将大纲节点转换为JSON对象
     */
    private ObjectNode convertNodeToJson(OutlineNode node) {
        ObjectNode nodeJson = objectMapper.createObjectNode();
        
        if (node.getTitle() != null) {
            nodeJson.put("title", node.getTitle());
        }
        
        if (node.getContent() != null) {
            nodeJson.put("content", node.getContent());
        }
        
        nodeJson.put("level", node.getLevel());
        
        // 属性
        Map<String, Object> attributes = node.getAttributes();
        if (!attributes.isEmpty()) {
            ObjectNode attributesNode = objectMapper.createObjectNode();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                addValueToNode(attributesNode, entry.getKey(), entry.getValue());
            }
            nodeJson.set("attributes", attributesNode);
        }
        
        // 子节点
        if (node.hasChildren()) {
            ArrayNode childrenArray = objectMapper.createArrayNode();
            for (OutlineNode child : node.getChildren()) {
                ObjectNode childJson = convertNodeToJson(child);
                childrenArray.add(childJson);
            }
            nodeJson.set("children", childrenArray);
        }
        
        return nodeJson;
    }
    
    /**
     * 添加值到JSON节点
     */
    private void addValueToNode(ObjectNode node, String key, Object value) {
        switch (value) {
            case null -> node.putNull(key);
            case String s -> node.put(key, s);
            case Integer i -> node.put(key, i);
            case Long l -> node.put(key, l);
            case Double v -> node.put(key, v);
            case Float v -> node.put(key, v);
            case Boolean aBoolean -> node.put(key, aBoolean);
            default ->
                // 其他类型转换为字符串
                    node.put(key, value.toString());
        }
    }
}