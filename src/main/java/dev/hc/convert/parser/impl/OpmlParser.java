package dev.hc.convert.parser.impl;

import dev.hc.convert.exception.ConversionExceptionFactory;
import dev.hc.convert.exception.ParsingException;
import dev.hc.convert.model.OutlineDocument;
import dev.hc.convert.model.OutlineNode;
import dev.hc.convert.parser.FileParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

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
        try {
            SAXReader saxReader = createSAXReader();
            Document document = saxReader.read(file);
            return parseOpmlDocument(document, file.getName());
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + file.getName(), e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + file.getName(), e);
        }
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) throws ParsingException {
        try {
            SAXReader saxReader = createSAXReader();
            Document document = saxReader.read(inputStream);
            return parseOpmlDocument(document, filename);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + filename, e);
        }
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) throws ParsingException {
        try {
            SAXReader saxReader = createSAXReader();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            Document document = saxReader.read(inputStream);
            return parseOpmlDocument(document, filename);
        } catch (DocumentException e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML document: " + filename, e);
        } catch (Exception e) {
            throw ConversionExceptionFactory.parseError("Failed to parse OPML file: " + filename, e);
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"opml"};
    }
    
    /**
     * 解析OPML文档
     */
    private OutlineDocument parseOpmlDocument(Document opmlDoc, String filename) throws ParsingException {
        Element root = opmlDoc.getRootElement();
        if (root == null || !"opml".equalsIgnoreCase(root.getName())) {
            throw ConversionExceptionFactory.invalidFileStructure("Not a valid OPML document format");
        }
        
        OutlineDocument document = new OutlineDocument();
        document.setSourceFormat("OPML");
        
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
        Element head = root.element("head");
        if (head != null) {
            // 标题
            Element title = head.element("title");
            if (title != null && title.getText() != null) {
                document.setTitle(title.getTextTrim());
            }
            
            // 描述
            Element description = head.element("docs");
            if (description != null && description.getText() != null) {
                document.setDescription(description.getTextTrim());
            }
            
            // 其他元数据
            extractMetadata(head, document, "dateCreated");
            extractMetadata(head, document, "dateModified");
            extractMetadata(head, document, "ownerName");
            extractMetadata(head, document, "ownerEmail");
            extractMetadata(head, document, "expansionState");
            extractMetadata(head, document, "vertScrollState");
            extractMetadata(head, document, "windowTop");
            extractMetadata(head, document, "windowLeft");
            extractMetadata(head, document, "windowBottom");
            extractMetadata(head, document, "windowRight");
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
        Element body = root.element("body");
        if (body != null) {
                    List<Element> outlineElements = body.elements("outline");
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
        String text = outlineElement.attributeValue("text");
        if (text == null) {
            text = outlineElement.attributeValue("title");
        }
        if (text == null) {
            text = "Untitled";
        }
        
        OutlineNode node = new OutlineNode(text.trim());
        node.setLevel(level);
        
        // 提取其他属性
        String type = outlineElement.attributeValue("type");
        if (type != null) {
            node.setAttribute("type", type);
        }
        
        String url = outlineElement.attributeValue("url");
        if (url != null) {
            node.setAttribute("url", url);
        }
        
        String xmlUrl = outlineElement.attributeValue("xmlUrl");
        if (xmlUrl != null) {
            node.setAttribute("xmlUrl", xmlUrl);
        }
        
        String htmlUrl = outlineElement.attributeValue("htmlUrl");
        if (htmlUrl != null) {
            node.setAttribute("htmlUrl", htmlUrl);
        }
        
        String description = outlineElement.attributeValue("description");
        if (description != null && !description.trim().isEmpty()) {
            node.setContent(description.trim());
        }
        
        String created = outlineElement.attributeValue("created");
        if (created != null) {
            node.setAttribute("created", created);
        }
        
        String category = outlineElement.attributeValue("category");
        if (category != null) {
            node.setAttribute("category", category);
        }
        
        // 解析子节点
        List<Element> childElements = outlineElement.elements("outline");
        for (Element childElement : childElements) {
            OutlineNode childNode = parseOutlineElement(childElement, level + 1);
            node.addChild(childNode);
        }
        
        return node;
    }
}