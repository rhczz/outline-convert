package dev.hc.convert.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
import dev.hc.convert.constant.OpmlSyntax;
import dev.hc.convert.constant.XmlSafety;
import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;

/**
 * OPML格式解析器
 * 支持OPML（Outline Processor Markup Language）格式解析为大纲文档
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class OpmlParser implements FileParser {

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
            
            // 检查文件大小，防止处理过大XML文件
            long fileSize = FileUtils.sizeOf(file);
            Validate.isTrue(fileSize <= FileValid.MAX_FILE_SIZE,
                "OPML file too large: %d bytes (max: %d bytes)", fileSize, FileValid.MAX_FILE_SIZE);
            
            // 使用commons-io安全读取文件
            String xmlContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            
            // 验证XML内容不为空
            Validate.isTrue(StringUtils.isNotBlank(xmlContent), "OPML file is empty");
            
            SAXReader saxReader = createSAXReader();
            Document document = saxReader.read(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            return parseOpmlDocument(document);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("OPML file validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.fileReadError(file, e);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + file.getName(), e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) {
        try {
            // 使用commons-lang3进行参数验证
            Validate.notNull(inputStream, "Input stream cannot be null");
            
            // 使用commons-io安全读取流内容，防止内存溢出
            String xmlContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            
            // 验证XML内容
            Validate.isTrue(StringUtils.isNotBlank(xmlContent), "OPML stream is empty");
            Validate.isTrue(xmlContent.length() <= FileValid.MAX_FILE_SIZE,
                "OPML content too large: %d characters", xmlContent.length());
            
            SAXReader saxReader = createSAXReader();
            Document document = saxReader.read(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            return parseOpmlDocument(document);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("OPML stream validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw ConversionExceptionFactory.parseError("Failed to read OPML stream: " + filename, e);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + filename, e);
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
                "OPML data too large: %d bytes (max: %d bytes)", data.length, FileValid.MAX_FILE_SIZE);
            
            SAXReader saxReader = createSAXReader();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            Document document = saxReader.read(inputStream);

            return parseOpmlDocument(document);
        } catch (IllegalArgumentException e) {
            throw ConversionExceptionFactory.systemError("OPML data validation failed: " + e.getMessage(), e);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return FileType.OPML.getExtensions();
    }
    
    /**
     * 解析OPML文档
     */
    private OutlineDocument parseOpmlDocument(Document opmlDoc) {
        Element root = opmlDoc.getRootElement();
        if (root == null || !OpmlSyntax.OPML_ELEMENT.equalsIgnoreCase(root.getName())) {
            throw ConversionExceptionFactory.invalidFileStructure("Not a valid OPML document format");
        }
        
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat(FileType.OPML.getDisplayName());
        
        // 解析头部信息
        parseHead(root, document);
        
        // 解析正文
        parseBody(root, document);
        
        return document;
    }
    
    /**
     * 解析OPML头部信息
     */
    private void parseHead(Element root, OutlineDocument document) {
        Element head = root.element(OpmlSyntax.HEAD_ELEMENT);
        if (head != null) {
            // 标题
            Element title = head.element(OpmlSyntax.TITLE_ELEMENT);
            if (title != null && title.getText() != null) {
                document.setTitle(title.getTextTrim());
            }
            
            // 描述
            Element description = head.element(OpmlSyntax.DOCS_ELEMENT);
            if (description != null && description.getText() != null) {
                document.setDescription(description.getTextTrim());
            }
            
            // 其他元数据
            extractMetadata(head, document, OpmlSyntax.DATE_CREATED_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.DATE_MODIFIED_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.OWNER_NAME_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.OWNER_EMAIL_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.EXPANSION_STATE_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.VERT_SCROLL_STATE_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.WINDOW_TOP_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.WINDOW_LEFT_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.WINDOW_BOTTOM_ELEMENT);
            extractMetadata(head, document, OpmlSyntax.WINDOW_RIGHT_ELEMENT);
        }
    }
    
    /**
     * 提取元数据
     */
    private void extractMetadata(Element parent, OutlineDocument document, String elementName) {
        Element element = parent.element(elementName);
        if (element != null && element.getText() != null) {
            String value = element.getTextTrim();
            if (!value.isEmpty()) {
                document.setMetadata(elementName, value);
            }
        }
    }
    
    /**
     * 解析OPML正文
     */
    private void parseBody(Element root, OutlineDocument document) {
        Element body = root.element(OpmlSyntax.BODY_ELEMENT);
        if (body != null) {
            List<Element> outlineElements = body.elements(OpmlSyntax.OUTLINE_ELEMENT);
            for (Element outlineElement : outlineElements) {
                OutlineNode node = parseOutlineElement(outlineElement, 0);
                document.addRootNode(node);
            }
        }
    }
    
    /**
     * 解析outline元素
     */
    private OutlineNode parseOutlineElement(Element outlineElement, int level) {
        String text = outlineElement.attributeValue(OpmlSyntax.TEXT_ATTRIBUTE);
        if (text == null) {
            text = outlineElement.attributeValue(OpmlSyntax.TITLE_ELEMENT);
        }
        if (text == null) {
            text = OpmlSyntax.DEFAULT_OUTLINE_TEXT;
        }
        
        OutlineNode node = new OutlineNode(text.trim());
        node.setLevel(level);
        
        // 提取其他属性
        String type = outlineElement.attributeValue(OpmlSyntax.TYPE_ATTRIBUTE);
        if (type != null) {
            node.setAttribute(OpmlSyntax.TYPE_ATTRIBUTE, type);
        }
        
        String url = outlineElement.attributeValue(OpmlSyntax.URL_ATTRIBUTE);
        if (url != null) {
            node.setAttribute(OpmlSyntax.URL_ATTRIBUTE, url);
        }
        
        String xmlUrl = outlineElement.attributeValue(OpmlSyntax.XML_URL_ATTRIBUTE);
        if (xmlUrl != null) {
            node.setAttribute(OpmlSyntax.XML_URL_ATTRIBUTE, xmlUrl);
        }
        
        String htmlUrl = outlineElement.attributeValue(OpmlSyntax.HTML_URL_ATTRIBUTE);
        if (htmlUrl != null) {
            node.setAttribute(OpmlSyntax.HTML_URL_ATTRIBUTE, htmlUrl);
        }
        
        String description = outlineElement.attributeValue(OpmlSyntax.DESCRIPTION_ATTRIBUTE);
        if (description != null && !description.trim().isEmpty()) {
            node.setContent(description.trim());
        }
        
        String created = outlineElement.attributeValue(OpmlSyntax.CREATED_ATTRIBUTE);
        if (created != null) {
            node.setAttribute(OpmlSyntax.CREATED_ATTRIBUTE, created);
        }
        
        String category = outlineElement.attributeValue(OpmlSyntax.CATEGORY_ATTRIBUTE);
        if (category != null) {
            node.setAttribute(OpmlSyntax.CATEGORY_ATTRIBUTE, category);
        }
        
        // 解析子节点
        List<Element> childElements = outlineElement.elements(OpmlSyntax.OUTLINE_ELEMENT);
        for (Element childElement : childElements) {
            OutlineNode childNode = parseOutlineElement(childElement, level + 1);
            node.addChild(childNode);
        }
        
        return node;
    }
}