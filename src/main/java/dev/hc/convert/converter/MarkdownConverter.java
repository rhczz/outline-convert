package dev.hc.convert.converter;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;

import java.io.File;

/**
 * @author Leo
 * @since 2025/8/1 19:29
 */
public class MarkdownConverter implements FileConverter {

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
