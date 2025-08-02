package dev.hc.convert.parser.impl;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * XMind格式解析器
 * 支持XMind思维导图格式解析为大纲文档
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class XMindParser implements FileParser {
    
    /**
     * 创建线程安全的SAXReader实例
     * 每次调用都创建新实例，避免线程安全问题
     */
    private SAXReader createSAXReader() {
        SAXReader reader = new SAXReader();
        // 禁用外部实体解析，提高安全性
        try {
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception e) {
            // 忽略不支持的特性
        }
        return reader;
    }
    
    @Override
    public OutlineDocument parse(File file) throws ParsingException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return parseFromInputStream(fis, file.getName());
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) throws ParsingException {
        return parseFromInputStream(inputStream, filename);
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) throws ParsingException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            return parseFromInputStream(bais, filename);
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"xmind"};
    }
    
    /**
     * 从输入流解析XMind文件
     */
    private OutlineDocument parseFromInputStream(InputStream inputStream, String filename) throws ParsingException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, StandardCharsets.UTF_8.name())) {
            
            Document contentDoc = null;
            Document metaDoc = null;
            
            ZipArchiveEntry entry;
            while ((entry = zis.getNextZipEntry()) != null) {
                String entryName = entry.getName();
                
                if ("content.xml".equals(entryName)) {
                    // 读取主要内容文件
                    byte[] contentData = readEntryData(zis);
                    SAXReader saxReader = createSAXReader();
                    contentDoc = saxReader.read(new ByteArrayInputStream(contentData));
                } else if ("meta.xml".equals(entryName)) {
                    // 读取元数据文件
                    byte[] metaData = readEntryData(zis);
                    SAXReader saxReader = createSAXReader();
                    metaDoc = saxReader.read(new ByteArrayInputStream(metaData));
                }
                // 其他文件（如样式、附件等）暂时忽略
            }
            
            if (contentDoc == null) {
                throw ConversionExceptionFactory.invalidFileStructure("Missing content.xml in XMind file");
            }
            
            return parseXMindContent(contentDoc, metaDoc, filename);
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to read XMind file: " + filename, e);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind XML: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind file: " + filename, e);
        }
    }
    
    /**
     * 读取ZIP条目数据
     */
    private byte[] readEntryData(ZipArchiveInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
    
    /**
     * 解析XMind内容
     */
    private OutlineDocument parseXMindContent(Document contentDoc, Document metaDoc, String filename) 
            throws ParsingException {
        
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat("XMind");
        
        // 解析元数据
        if (metaDoc != null) {
            parseMeta(metaDoc, document);
        }
        
        // 解析内容
        parseContent(contentDoc, document);
        
        return document;
    }
    
    /**
     * 解析元数据
     */
    private void parseMeta(Document metaDoc, OutlineDocument document) {
        Element root = metaDoc.getRootElement();
        if (root != null) {
            // 解析创建者信息
            Element creator = root.element("Creator");
            if (creator != null) {
                String name = creator.elementTextTrim("Name");
                if (name != null) {
                    document.setMetadata("creator", name);
                }
                String version = creator.elementTextTrim("Version");
                if (version != null) {
                    document.setMetadata("creatorVersion", version);
                }
            }
            
            // 解析时间信息
            String createTime = root.elementTextTrim("CreateTime");
            if (createTime != null) {
                document.setMetadata("xmindCreateTime", createTime);
            }
            
            String modifyTime = root.elementTextTrim("ModifyTime");
            if (modifyTime != null) {
                document.setMetadata("xmindModifyTime", modifyTime);
            }
        }
    }
    
    /**
     * 解析内容
     */
    private void parseContent(Document contentDoc, OutlineDocument document) throws ParsingException {
        Element root = contentDoc.getRootElement();
        if (root == null) {
            throw ConversionExceptionFactory.invalidFileStructure("XMind content.xml root element is empty");
        }
        
        // 查找工作簿
        List<Element> workbooks = root.elements("workbook");
        
        for (Element workbook : workbooks) {
            parseWorkbook(workbook, document);
        }
        
        // 如果没有找到工作簿，尝试直接解析根节点
        if (document.isEmpty()) {
            parseWorkbook(root, document);
        }
    }
    
    /**
     * 解析工作簿
     */
    private void parseWorkbook(Element workbook, OutlineDocument document) {
        // 解析工作表
        List<Element> sheets = workbook.elements("sheet");
        
        for (Element sheet : sheets) {
            parseSheet(sheet, document);
        }
    }
    
    /**
     * 解析工作表
     */
    private void parseSheet(Element sheet, OutlineDocument document) {
        // 获取工作表标题
        String title = sheet.attributeValue("name");
        if (title != null && !title.trim().isEmpty()) {
            document.setTitle(title);
        }
        
        // 查找主题（根主题）
        Element topic = sheet.element("topic");
        if (topic != null) {
            OutlineNode rootNode = parseTopic(topic, 0);
            document.addRootNode(rootNode);
        }
    }
    
    /**
     * 解析主题节点
     */
    private OutlineNode parseTopic(Element topic, int level) {
        // 获取主题标题
        String title = "Untitled";
        Element titleElement = topic.element("title");
        if (titleElement != null) {
            title = titleElement.getTextTrim();
            if (title == null || title.isEmpty()) {
                title = "Untitled";
            }
        }
        
        OutlineNode node = new OutlineNode(title);
        node.setLevel(level);
        
        // 获取主题ID
        String id = topic.attributeValue("id");
        if (id != null) {
            node.setAttribute("xmindId", id);
        }
        
        // 获取主题样式ID
        String styleId = topic.attributeValue("style-id");
        if (styleId != null) {
            node.setAttribute("styleId", styleId);
        }
        
        // 解析笔记
        Element notes = topic.element("notes");
        if (notes != null) {
            Element plain = notes.element("plain");
            if (plain != null) {
                String noteText = plain.getTextTrim();
                if (noteText != null && !noteText.isEmpty()) {
                    node.setContent(noteText);
                }
            }
        }
        
        // 解析标签
        Element labels = topic.element("labels");
        if (labels != null) {
            List<Element> labelList = labels.elements("label");
            StringBuilder labelText = new StringBuilder();
            for (Element label : labelList) {
                String labelValue = label.getTextTrim();
                if (labelValue != null && !labelValue.isEmpty()) {
                    if (!labelText.isEmpty()) {
                        labelText.append(", ");
                    }
                    labelText.append(labelValue);
                }
            }
            if (!labelText.isEmpty()) {
                node.setAttribute("labels", labelText.toString());
            }
        }
        
        // 解析子主题
        Element children = topic.element("children");
        if (children != null) {
            Element topics = children.element("topics");
            if (topics != null) {
                List<Element> childTopics = topics.elements("topic");
                for (Element childTopic : childTopics) {
                    OutlineNode childNode = parseTopic(childTopic, level + 1);
                    node.addChild(childNode);
                }
            }
        }
        
        return node;
    }
}