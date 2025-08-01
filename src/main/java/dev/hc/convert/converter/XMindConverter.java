package dev.hc.convert.converter;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;
import dev.hc.convert.model.MarkdownNode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

/**
 * XMind转换器
 * XMind文件本质上是一个ZIP文件，包含XML结构的思维导图数据
 */
public class XMindConverter implements FileConverter {

    /**
     * 将Markdown节点树转换为XMind文件
     */
    public void convertToXMind(MarkdownNode rootNode, Path outputPath) throws IOException {
        // 创建ZIP输出流
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile());
             ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(fos)) {

            // 添加content.xml文件
            String contentXml = generateContentXml(rootNode);
            addFileToZip(zipOut, "content.xml", contentXml.getBytes("UTF-8"));

            // 添加META-INF/manifest.xml文件
            String manifestXml = generateManifestXml();
            addFileToZip(zipOut, "META-INF/manifest.xml", manifestXml.getBytes("UTF-8"));

            // 添加meta.xml文件
            String metaXml = generateMetaXml();
            addFileToZip(zipOut, "meta.xml", metaXml.getBytes("UTF-8"));

            zipOut.finish();
        }
    }

    /**
     * 生成content.xml内容
     */
    private String generateContentXml(MarkdownNode rootNode) throws IOException {
        Document document = DocumentHelper.createDocument();
        document.addDocType("xmap-content", null, "urn:xmind:xmap:xmlns:content:2.0");

        Element root = document.addElement("xmap-content")
                .addAttribute("xmlns", "urn:xmind:xmap:xmlns:content:2.0")
                .addAttribute("xmlns:fo", "http://www.w3.org/1999/XSL/Format")
                .addAttribute("xmlns:svg", "http://www.w3.org/2000/svg")
                .addAttribute("xmlns:xhtml", "http://www.w3.org/1999/xhtml")
                .addAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
                .addAttribute("version", "2.0");

        Element sheet = root.addElement("sheet")
                .addAttribute("id", "sheet1")
                .addAttribute("theme", "robust");

        Element topic = sheet.addElement("topic")
                .addAttribute("id", "root")
                .addAttribute("structure-class", "org.xmind.ui.map.radial");

        // 设置根主题标题
        if (!rootNode.getChildren().isEmpty()) {
            MarkdownNode firstChild = rootNode.getChildren().get(0);
            topic.addElement("title").setText(firstChild.getTitle());

            // 添加子主题
            Element children = topic.addElement("children");
            Element topics = children.addElement("topics")
                    .addAttribute("type", "attached");

            for (MarkdownNode child : firstChild.getChildren()) {
                addTopicElement(topics, child, 1);
            }
        } else {
            topic.addElement("title").setText("空白思维导图");
        }

        return formatXml(document);
    }

    /**
     * 递归添加主题元素
     */
    private void addTopicElement(Element parent, MarkdownNode node, int depth) {
        Element topic = parent.addElement("topic")
                .addAttribute("id", "topic_" + node.hashCode());

        topic.addElement("title").setText(node.getTitle());

        if (!node.getChildren().isEmpty()) {
            Element children = topic.addElement("children");
            Element topics = children.addElement("topics")
                    .addAttribute("type", "attached");

            for (MarkdownNode child : node.getChildren()) {
                addTopicElement(topics, child, depth + 1);
            }
        }
    }

    /**
     * 生成manifest.xml内容
     */
    private String generateManifestXml() throws IOException {
        Document document = DocumentHelper.createDocument();
        Element manifest = document.addElement("manifest")
                .addAttribute("xmlns", "urn:xmind:xmap:xmlns:manifest:1.0");

        manifest.addElement("file-entry")
                .addAttribute("full-path", "content.xml")
                .addAttribute("media-type", "text/xml");

        manifest.addElement("file-entry")
                .addAttribute("full-path", "META-INF/")
                .addAttribute("media-type", "");

        manifest.addElement("file-entry")
                .addAttribute("full-path", "meta.xml")
                .addAttribute("media-type", "text/xml");

        return formatXml(document);
    }

    /**
     * 生成meta.xml内容
     */
    private String generateMetaXml() throws IOException {
        Document document = DocumentHelper.createDocument();
        Element meta = document.addElement("meta")
                .addAttribute("xmlns", "urn:xmind:xmap:xmlns:meta:2.0")
                .addAttribute("version", "2.0");

        Element author = meta.addElement("Author");
        author.addElement("Name").setText("XMind Converter");

        Element create = meta.addElement("Create");
        create.addElement("Time").setText(String.valueOf(System.currentTimeMillis()));

        Element creator = meta.addElement("Creator");
        creator.addElement("Name").setText("XMind Converter");
        creator.addElement("Version").setText("1.0.0");

        return formatXml(document);
    }

    /**
     * 格式化XML文档
     */
    private String formatXml(Document document) throws IOException {
        StringWriter stringWriter = new StringWriter();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(stringWriter, format);
        writer.write(document);
        writer.close();
        return stringWriter.toString();
    }

    /**
     * 添加文件到ZIP
     */
    private void addFileToZip(ZipArchiveOutputStream zipOut, String fileName, byte[] content) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
        entry.setSize(content.length);
        zipOut.putArchiveEntry(entry);
        zipOut.write(content);
        zipOut.closeArchiveEntry();
    }

    @Override
    public File convert(File file, FileType source, FileType target) throws ConversionFailureException {
        return null;
    }

    @Override
    public File toJson(File file, FileType source) throws ConversionFailureException {
        return null;
    }

    @Override
    public File toMarkdown(File file, FileType source) throws ConversionFailureException {
        return null;
    }

    @Override
    public File toXMind(File file, FileType source) throws ConversionFailureException {
        return null;
    }

    @Override
    public File toOpml(File file, FileType source) throws ConversionFailureException {
        return null;
    }
}
