package dev.hc.convert.converter;

import dev.hc.convert.FileType;
import dev.hc.convert.exception.ConversionFailureException;

import java.io.File;

/**
 * @author Leo
 * @since 2025/8/1 19:29
 */
public interface FileConverter {

    /**
     * 转换文件
     * @param file      文件
     * @param source    源文件类型
     * @param target    目标文件类型
     * @return          目标文件
     */
    File convert(File file, FileType source, FileType target) throws ConversionFailureException;

    File toJson(File file, FileType source) throws ConversionFailureException;

    File toMarkdown(File file, FileType source) throws ConversionFailureException;

    File toXMind(File file, FileType source) throws ConversionFailureException;

    File toOpml(File file, FileType source) throws ConversionFailureException;

}
