package dev.hc.convert.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import dev.hc.convert.FileType;
import dev.hc.convert.constant.FileValid;
import dev.hc.convert.constant.XMindSyntax;
import dev.hc.convert.constant.XmlSafety;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;

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
    @SuppressWarnings("UseSpecificCatch")
    private SAXReader createSAXReader() {
        SAXReader reader = new SAXReader();
        // 禁用外部实体解析，提高安全性
        try {
            reader.setFeature(XmlSafety.DISALLOW_DOCTYPE_DECL, true);
            reader.setFeature(XmlSafety.EXTERNAL_GENERAL_ENTITIES, false);
            reader.setFeature(XmlSafety.EXTERNAL_PARAMETER_ENTITIES, false);
        } catch (Exception e) {
            // 忽略不支持的特性
        }
        return reader;
    }
    
    @Override
    public OutlineDocument parse(File file) {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(file, "Input file cannot be null");
            Validate.isTrue(file.exists(), "File does not exist: %s", file);
            Validate.isTrue(file.isFile(), "Path is not a file: %s", file.getAbsolutePath());
            
            // 检查文件大小，防止处理过大的XMind文件导致内存溢出
            long fileSize = FileUtils.sizeOf(file);
            Validate.isTrue(fileSize <= FileValid.MAX_FILE_SIZE,
                "XMind file too large: %d bytes (max: %d bytes)", fileSize, FileValid.MAX_FILE_SIZE);
            
            // 验证文件名安全性
            String safeFilename = getSafeFilename(file.getName());
            
            try (FileInputStream fis = new FileInputStream(file)) {
                return parseFromInputStream(fis, safeFilename);
            }
            
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("XMind file validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(inputStream, "Input stream cannot be null");
            
            // 验证文件名安全性
            String safeFilename = getSafeFilename(filename);
            
            return parseFromInputStream(inputStream, safeFilename);
            
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("XMind stream validation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) {
        try {
            // 使用commons-lang3进行数据验证
            Validate.notNull(data, "Input data cannot be null");
            Validate.isTrue(data.length > 0, "Input data cannot be empty");
            
            // 检查数据大小，防止内存溢出
            Validate.isTrue(data.length <= FileValid.MAX_FILE_SIZE,
                "XMind data too large: %d bytes (max: %d bytes)", data.length, FileValid.MAX_FILE_SIZE);
            
            // 验证文件名安全性
            String safeFilename = getSafeFilename(filename);
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                return parseFromInputStream(bais, safeFilename);
            }
            
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("XMind data validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind file: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind data: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return FileType.XMIND.getExtensions();
    }
    
    /**
     * 获取安全的文件名，清理危险字符
     */
    private String getSafeFilename(String filename) {
        if (StringUtils.isBlank(filename)) {
            return FileType.XMIND.getDefaultFileName();
        }
        
        // 清理文件名中的危险字符
        String safeFilename = StringUtils.replaceChars(filename, FileValid.DANGEROUS_CHARS, FileValid.REPLACEMENT_CHARS);
        
        // 限制文件名长度
        if (safeFilename.length() > FileValid.MAX_FILENAME_LENGTH) {
            safeFilename = StringUtils.left(safeFilename, FileValid.MAX_FILENAME_LENGTH);
        }
        
        return safeFilename;
    }
    
    /**
     * 从输入流解析XMind文件
     */
    private OutlineDocument parseFromInputStream(InputStream inputStream, String filename) {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, StandardCharsets.UTF_8.name())) {
            
            Document contentDoc = null;
            Document metaDoc = null;
            
            ZipArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                if (XMindSyntax.CONTENT_XML.equals(entryName)) {
                    // 读取主要内容文件
                    byte[] contentData = readEntryData(zis);
                    SAXReader saxReader = createSAXReader();
                    contentDoc = saxReader.read(new ByteArrayInputStream(contentData));
                } else if (XMindSyntax.META_XML.equals(entryName)) {
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
            
            return parseXMindContent(contentDoc, metaDoc);
            
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to read XMind file: " + filename, e);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind XML: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse XMind file: " + filename, e);
        }
    }
    
    /**
     * 安全读取ZIP条目数据，防止ZIP bomb攻击
     */
    private byte[] readEntryData(ZipArchiveInputStream zis) throws IOException {
        try {
            // 使用commons-io安全读取，限制最大读取大小
            return IOUtils.toByteArray(zis, FileValid.MAX_ZIP_ENTRY_SIZE);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("exceeds the maximum")) {
                throw new IOException("ZIP entry too large (exceeds 50MB limit), possible ZIP bomb attack", e);
            }
            throw e;
        }
    }
    
    /**
     * 解析XMind内容
     */
    private OutlineDocument parseXMindContent(Document contentDoc, Document metaDoc) {
        
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat(FileType.XMIND.getDisplayName());
        
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
            Element creator = root.element(XMindSyntax.CREATOR_ELEMENT);
            if (creator != null) {
                String name = creator.elementTextTrim(XMindSyntax.CREATOR_NAME_ELEMENT);
                if (name != null) {
                    document.setMetadata(XMindSyntax.CREATOR_METADATA_KEY, name);
                }
                String version = creator.elementTextTrim(XMindSyntax.CREATOR_VERSION_ELEMENT);
                if (version != null) {
                    document.setMetadata(XMindSyntax.CREATOR_VERSION_METADATA_KEY, version);
                }
            }
            
            // 解析时间信息
            String createTime = root.elementTextTrim(XMindSyntax.CREATE_TIME_ELEMENT);
            if (createTime != null) {
                document.setMetadata(XMindSyntax.CREATE_TIME_METADATA_KEY, createTime);
            }
            
            String modifyTime = root.elementTextTrim(XMindSyntax.MODIFY_TIME_ELEMENT);
            if (modifyTime != null) {
                document.setMetadata(XMindSyntax.MODIFY_TIME_METADATA_KEY, modifyTime);
            }
        }
    }
    
    /**
     * 解析内容
     */
    private void parseContent(Document contentDoc, OutlineDocument document) {
        Element root = contentDoc.getRootElement();
        if (root == null) {
            throw ConversionExceptionFactory.invalidFileStructure("XMind content.xml root element is empty");
        }
        
        // 查找工作簿
        List<Element> workbooks = root.elements(XMindSyntax.WORKBOOK_ELEMENT);
        
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
        List<Element> sheets = workbook.elements(XMindSyntax.SHEET_ELEMENT);
        
        for (Element sheet : sheets) {
            parseSheet(sheet, document);
        }
    }
    
    /**
     * 解析工作表
     */
    private void parseSheet(Element sheet, OutlineDocument document) {
        // 获取工作表标题
        String title = sheet.attributeValue(XMindSyntax.NAME_ATTRIBUTE);
        if (title != null && !title.trim().isEmpty()) {
            document.setTitle(title);
        }
        
        // 查找主题（根主题）
        Element topic = sheet.element(XMindSyntax.TOPIC_ELEMENT);
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
        String title = XMindSyntax.DEFAULT_TOPIC_TITLE;
        Element titleElement = topic.element(XMindSyntax.TITLE_ELEMENT);
        if (titleElement != null) {
            title = titleElement.getTextTrim();
            if (title == null || title.isEmpty()) {
                title = XMindSyntax.DEFAULT_TOPIC_TITLE;
            }
        }
        
        OutlineNode node = new OutlineNode(title);
        node.setLevel(level);
        
        // 获取主题ID
        String id = topic.attributeValue(XMindSyntax.ID_ATTRIBUTE);
        if (id != null) {
            node.setAttribute(XMindSyntax.XMIND_ID_ATTRIBUTE_KEY, id);
        }
        
        // 获取主题样式ID
        String styleId = topic.attributeValue(XMindSyntax.STYLE_ID_ATTRIBUTE);
        if (styleId != null) {
            node.setAttribute(XMindSyntax.STYLE_ID_ATTRIBUTE_KEY, styleId);
        }
        
        // 解析笔记
        Element notes = topic.element(XMindSyntax.NOTES_ELEMENT);
        if (notes != null) {
            Element plain = notes.element(XMindSyntax.PLAIN_ELEMENT);
            if (plain != null) {
                String noteText = plain.getTextTrim();
                if (noteText != null && !noteText.isEmpty()) {
                    node.setContent(noteText);
                }
            }
        }
        
        // 解析标签
        Element labels = topic.element(XMindSyntax.LABELS_ELEMENT);
        if (labels != null) {
            List<Element> labelList = labels.elements(XMindSyntax.LABEL_ELEMENT);
            StringBuilder labelText = new StringBuilder();
            for (Element label : labelList) {
                String labelValue = label.getTextTrim();
                if (labelValue != null && !labelValue.isEmpty()) {
                    if (!labelText.isEmpty()) {
                        labelText.append(XMindSyntax.LABEL_SEPARATOR);
                    }
                    labelText.append(labelValue);
                }
            }
            if (!labelText.isEmpty()) {
                node.setAttribute(XMindSyntax.LABELS_ATTRIBUTE_KEY, labelText.toString());
            }
        }
        
        // 解析子主题
        Element children = topic.element(XMindSyntax.CHILDREN_ELEMENT);
        if (children != null) {
            Element topics = children.element(XMindSyntax.TOPICS_ELEMENT);
            if (topics != null) {
                List<Element> childTopics = topics.elements(XMindSyntax.TOPIC_ELEMENT);
                for (Element childTopic : childTopics) {
                    OutlineNode childNode = parseTopic(childTopic, level + 1);
                    node.addChild(childNode);
                }
            }
        }
        
        return node;
    }
}