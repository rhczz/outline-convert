package dev.hc.convert.converter.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
 * XMind格式转换器
 * 将大纲文档转换为XMind思维导图格式输出
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class XMindFormatConverter implements FormatConverter {
    
    // 使用线程安全的DateTimeFormatter替代SimpleDateFormat
    private static final DateTimeFormatter XMIND_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH)
                         .withZone(ZoneId.of("UTC"));
    
    @Override
    public void convert(OutlineDocument document, File outputFile) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            convertToStream(document, fos);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileWriteError(outputFile, e);
        }
    }
    
    @Override
    public void convert(OutlineDocument document, OutputStream outputStream) {
        convertToStream(document, outputStream);
    }
    
    @Override
    public byte[] convertToBytes(OutlineDocument document) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        convertToStream(document, baos);
        return baos.toByteArray();
    }
    
    @Override
    public String getFormatName() {
        return "XMind";
    }
    
    @Override
    public String getDefaultExtension() {
        return "xmind";
    }
    
    @Override
    public String getMimeType() {
        return "application/xmind";
    }
    
    /**
     * 转换为输出流
     */
    private void convertToStream(OutlineDocument document, OutputStream outputStream) {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream)) {
            zos.setEncoding(StandardCharsets.UTF_8.name());
            
            // 创建content.xml
            Document contentDoc = createContentDocument(document);
            addXmlToZip(zos, "content.xml", contentDoc);
            
            // 创建meta.xml
            Document metaDoc = createMetaDocument(document);
            addXmlToZip(zos, "meta.xml", metaDoc);
            
            // 创建manifest.xml
            Document manifestDoc = createManifestDocument();
            addXmlToZip(zos, "META-INF/manifest.xml", manifestDoc);
            
            zos.finish();
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to write XMind file", e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.conversionFailed("Failed to convert to XMind", e);
        }
    }
    
    /**
     * 创建内容文档
     */
    private Document createContentDocument(OutlineDocument document) {
        Document doc = DocumentHelper.createDocument();
        
        // 创建根元素
        Element root = doc.addElement("xmap-content")
                          .addAttribute("xmlns", "urn:xmind:xmap:xmlns:content:2.0")
                          .addAttribute("xmlns:fo", "http://www.w3.org/1999/XSL/Format");
        
        // 创建工作簿
        Element sheet = root.addElement("sheet")
                           .addAttribute("id", generateId())
                           .addAttribute("name", document.getTitle() != null ? document.getTitle() : "Sheet1");
        
        // 如果只有一个根节点，将其作为中心主题
        if (document.getRootNodesCount() == 1) {
            OutlineNode rootNode = document.getRootNodes().getFirst();
            Element topic = createTopicElement(rootNode);
            sheet.add(topic);
        } else {
            // 多个根节点时，创建一个虚拟的中心主题
            Element topic = sheet.addElement("topic")
                                .addAttribute("id", generateId());
            
            Element title = topic.addElement("title");
            title.setText(document.getTitle() != null ? document.getTitle() : "Central Topic");
            
            Element children = topic.addElement("children");
            Element topics = children.addElement("topics")
                                   .addAttribute("type", "attached");
            
            for (OutlineNode rootNode : document.getRootNodes()) {
                Element childTopic = createTopicElement(rootNode);
                topics.add(childTopic);
            }
        }
        
        return doc;
    }
    
    /**
     * 创建主题元素
     */
    private Element createTopicElement(OutlineNode node) {
        Element topic = DocumentHelper.createElement("topic")
                                     .addAttribute("id", generateId());
        
        // 添加标题
        Element title = topic.addElement("title");
        title.setText(node.getTitle() != null ? node.getTitle() : "");
        
        // 添加笔记
        if (node.getContent() != null && !node.getContent().trim().isEmpty()) {
            Element notes = topic.addElement("notes");
            Element plain = notes.addElement("plain");
            plain.setText(node.getContent());
        }
        
        // 添加标签
        String labels = node.getAttribute("labels");
        if (labels != null && !labels.trim().isEmpty()) {
            Element labelsElement = topic.addElement("labels");
            String[] labelArray = labels.split(",\\s*");
            for (String label : labelArray) {
                if (!label.trim().isEmpty()) {
                    labelsElement.addElement("label").setText(label.trim());
                }
            }
        }
        
        // 添加子主题
        if (node.hasChildren()) {
            Element children = topic.addElement("children");
            Element topics = children.addElement("topics")
                                   .addAttribute("type", "attached");
            
            for (OutlineNode child : node.getChildren()) {
                Element childTopic = createTopicElement(child);
                topics.add(childTopic);
            }
        }
        
        return topic;
    }
    
    /**
     * 创建元数据文档
     */
    private Document createMetaDocument(OutlineDocument document) {
        Document doc = DocumentHelper.createDocument();
        
        Element meta = doc.addElement("meta")
                         .addAttribute("xmlns", "urn:xmind:xmap:xmlns:meta:2.0");
        
        // 创建者信息
        Element creator = meta.addElement("Creator");
        creator.addElement("Name").setText("Outline Convert");
        creator.addElement("Version").setText("1.0.0");
        
        // 时间信息
        Date now = new Date();
        String currentTime = XMIND_DATE_FORMAT.format(now.toInstant());
        
        // 创建时间
        String createTime = document.getMetadata("xmindCreateTime");
        if (createTime == null) {
            createTime = document.getCreateTime() > 0 ? 
                XMIND_DATE_FORMAT.format(Instant.ofEpochMilli(document.getCreateTime())) : currentTime;
        }
        meta.addElement("CreateTime").setText(createTime);
        
        // 修改时间
        String modifyTime = document.getMetadata("xmindModifyTime");
        if (modifyTime == null) {
            modifyTime = document.getModifyTime() > 0 ? 
                XMIND_DATE_FORMAT.format(Instant.ofEpochMilli(document.getModifyTime())) : currentTime;
        }
        meta.addElement("ModifyTime").setText(modifyTime);
        
        return doc;
    }
    
    /**
     * 创建清单文档
     */
    private Document createManifestDocument() {
        Document doc = DocumentHelper.createDocument();
        
        Element manifest = doc.addElement("manifest")
                             .addAttribute("xmlns", "urn:xmind:xmap:xmlns:manifest:1.0");
        
        // 添加文件条目
        manifest.addElement("file-entry")
                .addAttribute("full-path", "content.xml")
                .addAttribute("media-type", "text/xml");
        
        manifest.addElement("file-entry")
                .addAttribute("full-path", "META-INF/")
                .addAttribute("media-type", "");
        
        manifest.addElement("file-entry")
                .addAttribute("full-path", "meta.xml")
                .addAttribute("media-type", "text/xml");
        
        return doc;
    }
    
    /**
     * 将XML文档添加到ZIP文件
     */
    private void addXmlToZip(ZipArchiveOutputStream zos, String entryName, Document doc) 
            throws IOException {
        
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        zos.putArchiveEntry(entry);
        
        // 写入XML内容
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            XMLWriter writer = new XMLWriter(osw, format);
            writer.write(doc);
            writer.flush();
        }
        
        zos.write(baos.toByteArray());
        zos.closeArchiveEntry();
    }
    
    /**
     * 生成唯一ID
     */
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}