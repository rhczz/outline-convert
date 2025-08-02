package dev.hc.convert.converter.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import dev.hc.convert.converter.FormatConverter;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;

/**
 * OPML格式转换器
 * 将大纲文档转换为OPML格式输出
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class OpmlFormatConverter implements FormatConverter {
    
    // 使用线程安全的DateTimeFormatter替代SimpleDateFormat
    private static final DateTimeFormatter RFC822_FORMAT = 
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
                         .withZone(ZoneId.systemDefault());
    
    @Override
    public void convert(OutlineDocument document, File outputFile) {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            
            Document opmlDoc = convertToOpmlDocument(document);
            writeXmlDocument(opmlDoc, osw);
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileWriteError(outputFile, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to OPML", e);
        }
    }
    
    @Override
    public void convert(OutlineDocument document, OutputStream outputStream) {
        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            Document opmlDoc = convertToOpmlDocument(document);
            writeXmlDocument(opmlDoc, osw);
        } catch (IOException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to write OPML to stream", e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to OPML", e);
        }
    }
    
    @Override
    public byte[] convertToBytes(OutlineDocument document) {
        try {
            Document opmlDoc = convertToOpmlDocument(document);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
                writeXmlDocument(opmlDoc, osw);
            }
            
            return baos.toByteArray();
        } catch (IOException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to OPML", e);
        }
    }
    
    @Override
    public String getFormatName() {
        return "OPML";
    }
    
    @Override
    public String getDefaultExtension() {
        return "opml";
    }
    
    @Override
    public String getMimeType() {
        return "text/x-opml";
    }
    
    /**
     * 将大纲文档转换为OPML文档
     */
    private Document convertToOpmlDocument(OutlineDocument document) {
        Document opmlDoc = DocumentHelper.createDocument();
        
        // 创建根元素
        Element root = opmlDoc.addElement("opml");
        root.addAttribute("version", "2.0");
        
        // 创建头部
        Element head = root.addElement("head");
        createHead(head, document);
        
        // 创建正文
        Element body = root.addElement("body");
        createBody(body, document);
        
        return opmlDoc;
    }
    
    /**
     * 创建OPML头部
     */
    private void createHead(Element head, OutlineDocument document) {
        // 标题
        if (document.getTitle() != null && !document.getTitle().trim().isEmpty()) {
            head.addElement("title").setText(document.getTitle());
        }
        
        // 描述
        if (document.getDescription() != null && !document.getDescription().trim().isEmpty()) {
            head.addElement("docs").setText(document.getDescription());
        }
        
        // 时间信息
        Date now = new Date();
        String currentTime = RFC822_FORMAT.format(now.toInstant());
        
        // 创建时间
        String dateCreated = document.getMetadata("dateCreated");
        if (dateCreated != null) {
            head.addElement("dateCreated").setText(dateCreated);
        } else if (document.getCreateTime() > 0) {
            head.addElement("dateCreated")
                .setText(RFC822_FORMAT.format(Instant.ofEpochMilli(document.getCreateTime())));
        } else {
            head.addElement("dateCreated").setText(currentTime);
        }
        
        // 修改时间
        String dateModified = document.getMetadata("dateModified");
        if (dateModified != null) {
            head.addElement("dateModified").setText(dateModified);
        } else if (document.getModifyTime() > 0) {
            head.addElement("dateModified")
                .setText(RFC822_FORMAT.format(Instant.ofEpochMilli(document.getModifyTime())));
        } else {
            head.addElement("dateModified").setText(currentTime);
        }
        
        // 其他元数据
        addMetadataElement(head, document, "ownerName");
        addMetadataElement(head, document, "ownerEmail");
        addMetadataElement(head, document, "expansionState");
        addMetadataElement(head, document, "vertScrollState");
        addMetadataElement(head, document, "windowTop");
        addMetadataElement(head, document, "windowLeft");
        addMetadataElement(head, document, "windowBottom");
        addMetadataElement(head, document, "windowRight");
    }
    
    /**
     * 添加元数据元素
     */
    private void addMetadataElement(Element parent, OutlineDocument document, String metadataKey) {
        String value = document.getMetadata(metadataKey);
        if (value != null && !value.trim().isEmpty()) {
            parent.addElement(metadataKey).setText(value);
        }
    }
    
    /**
     * 创建OPML正文
     */
    private void createBody(Element body, OutlineDocument document) {
        for (OutlineNode rootNode : document.getRootNodes()) {
            createOutlineElement(body, rootNode);
        }
    }
    
    /**
     * 创建outline元素
     */
    private void createOutlineElement(Element parent, OutlineNode node) {
        Element outline = parent.addElement("outline");
        
        // 文本内容
        if (node.getTitle() != null && !node.getTitle().trim().isEmpty()) {
            outline.addAttribute("text", node.getTitle());
        }
        
        // 内容作为描述
        if (node.getContent() != null && !node.getContent().trim().isEmpty()) {
            outline.addAttribute("description", node.getContent());
        }
        
        // 添加属性
        Map<String, Object> attributes = node.getAttributes();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过内部属性
            if ("headingLevel".equals(key)) {
                continue;
            }
            
            if (value != null) {
                outline.addAttribute(key, value.toString());
            }
        }
        
        // 递归创建子节点
        for (OutlineNode child : node.getChildren()) {
            createOutlineElement(outline, child);
        }
    }
    
    /**
     * 写入XML文档
     */
    private void writeXmlDocument(Document document, Writer writer) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setIndentSize(2);
        
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(document);
        xmlWriter.flush();
    }
}