package dev.hc.convert.converter;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;

import java.io.File;

/**
 * XMind转换器
 * XMind文件本质上是一个ZIP文件，包含XML结构的思维导图数据
 */
public class XMindConverter implements FileConverter {

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
