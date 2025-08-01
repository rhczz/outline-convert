package dev.hc.convert;

import dev.hc.convert.converter.*;
import dev.hc.convert.exception.ConversionFailureException;

import java.io.File;

/**
 * @author Leo
 * @since 2025/8/1 19:18
 */
public enum FileType {

    /** json 文件类型 */
    JSON(new JsonConverter()),

    /** md文件类型 */
    MARKDOWN(new MarkdownConverter()),

    /** xmind文件类型 */
    XMIND(new XMindConverter()),

    /** opml文件类型 */
    OPML(new OpmlConverter());

    private final FileConverter fileConverter;

    FileType(final FileConverter fileConverter) {
        this.fileConverter = fileConverter;
    }

    /**
     * Convert file to json
     * @param file input file
     * @return json file
     */
    public File toJson(File file) throws ConversionFailureException {
        return fileConverter.toJson(file, this);
    }

    /**
     * Convert file to markdown
     * @param jsonFile input json file
     * @return markdown file
     */
    public File toMd(File jsonFile) throws ConversionFailureException {
        return fileConverter.toMarkdown(jsonFile, this);
    }

    /**
     * Convert file to xmind
     * @param jsonFile input json file
     * @return xmind file
     */
    public File toXmind(File jsonFile) throws ConversionFailureException {
        return fileConverter.toXMind(jsonFile, this);
    }

    /**
     * Convert file to opml
     * @param jsonFile input json file
     * @return opml file
     */
    public File toOpml(File jsonFile) throws ConversionFailureException {
        return fileConverter.toOpml(jsonFile, this);
    }

}
