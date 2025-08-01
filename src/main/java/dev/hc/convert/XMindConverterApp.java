package dev.hc.convert;

import dev.hc.convert.converter.XMindConverter;
import dev.hc.convert.model.MarkdownNode;
import dev.hc.convert.parser.MarkdownParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * XMind转换器主应用程序
 */
public class XMindConverterApp {
    
    private static final Logger log = LoggerFactory.getLogger(XMindConverterApp.class);

    public static void main(String[] args) {
        try {
            // 参数检查
            if (args.length < 1) {
                System.out.println("使用方法: java -jar outline-convert.jar <markdown文件路径> [输出文件路径]");
                System.out.println("示例: java -jar outline-convert.jar sample.md output.xmind");
                return;
            }

            String inputFile = args[0];
            String outputFile = args.length > 1 ? args[1] : getDefaultOutputFileName(inputFile);

            Path inputPath = Paths.get(inputFile);
            Path outputPath = Paths.get(outputFile);

            log.info("开始转换: {} -> {}", inputPath, outputPath);

            // 创建解析器和转换器
            MarkdownParser parser = new MarkdownParser();
            XMindConverter converter = new XMindConverter();

            // 解析Markdown文件
            log.info("解析Markdown文件...");
            MarkdownNode rootNode = parser.parseFile(inputPath);
            
            // 打印解析结果（调试用）
            log.info("解析完成，结构如下:");
            parser.printTree(rootNode, "");

            // 转换为XMind格式
            log.info("转换为XMind格式...");
            converter.convertToXMind(rootNode, outputPath);

            log.info("转换完成! 输出文件: {}", outputPath.toAbsolutePath());
            System.out.println("转换成功! 输出文件: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            log.error("转换过程中发生错误", e);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成默认输出文件名
     */
    private static String getDefaultOutputFileName(String inputFile) {
        if (inputFile.toLowerCase().endsWith(".md")) {
            return inputFile.substring(0, inputFile.length() - 3) + ".xmind";
        } else {
            return inputFile + ".xmind";
        }
    }

    /**
     * 转换单个文件的便捷方法
     */
    public static void convertFile(String markdownFile, String xmindFile) throws Exception {
        Path inputPath = Paths.get(markdownFile);
        Path outputPath = Paths.get(xmindFile);

        MarkdownParser parser = new MarkdownParser();
        XMindConverter converter = new XMindConverter();

        MarkdownNode rootNode = parser.parseFile(inputPath);
        converter.convertToXMind(rootNode, outputPath);
    }
}
