# Outline Convert

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一个高性能、线程安全的大纲格式转换工具，支持多种常见大纲格式之间的相互转换。基于现代Java设计模式，提供简洁流畅的API接口。

## ✨ 特性

- **🚀 高性能**: 懒加载设计，只在需要时创建转换器实例，采用ConcurrentHashMap缓存优化
- **🎯 易用性**: 流畅的API设计，支持链式调用，自动文件类型检测
- **📤 多输出**: 支持文件、输出流、字节数组三种输出方式，满足不同场景需求
- **🔧 扩展性**: 基于工厂模式和接口设计，易于添加新格式支持
- **🛡️ 健壮性**: 完善的异常处理机制，文件大小限制，安全验证
- **⚡ 轻量级**: 核心库体积小，依赖精简，启动快速
- **🔒 线程安全**: 全面的并发安全设计，支持多线程环境使用

## 📋 支持格式

| 格式 | 读取 | 写入 | 扩展名 | 描述 |
|------|------|------|--------|------|
| JSON | ✅ | ✅ | `.json` | 结构化数据格式 |
| Markdown | ✅ | ✅ | `.md`, `.markdown` | 标记语言格式 |
| XMind | ✅ | ✅ | `.xmind` | 思维导图格式 |
| OPML | ✅ | ✅ | `.opml` | 大纲处理标记语言 |

## 🚀 快速开始

### 安装依赖

```xml
<dependency>
    <groupId>dev.hc</groupId>
    <artifactId>outline-convert</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import dev.hc.convert.FileType;
import dev.hc.convert.engine.ConvertEngine;
import java.io.File;

// 方式1: 使用枚举链式调用
File inputFile = new File("document.md");
File jsonOutput = FileType.MARKDOWN.toJson().convert(inputFile);

// 方式2: 使用转换引擎
ConvertEngine.from(FileType.MARKDOWN)
            .to(FileType.XMIND)
            .convert(inputFile, new File("output.xmind"));

// 方式3: 自动检测文件类型
ConvertEngine.from(inputFile)
            .toOpml()
            .convert(inputFile, "output.opml");
```

### 高级用法

```java
// 转换为字节数组
byte[] data = FileType.MARKDOWN.toJson().convertToBytes(inputFile);

// 转换到输出流
try (FileOutputStream fos = new FileOutputStream("output.json")) {
    FileType.MARKDOWN.toJson().convertToStream(inputFile, fos);
}

// 从输入流转换
try (FileInputStream fis = new FileInputStream("input.md")) {
    ConvertEngine.from(FileType.MARKDOWN)
                .toJson()
                .convert(fis, "input.md", new File("output.json"));
}

// 从字节数组转换
byte[] markdownData = Files.readAllBytes(Paths.get("input.md"));
ConvertEngine.from(FileType.MARKDOWN)
            .toXMind()
            .convert(markdownData, "input.md", new File("output.xmind"));
```

## 🏗️ 项目架构

### 核心模块

```
outline-convert/
├── engine/           # 转换引擎（流畅API入口）
├── model/           # 数据模型（OutlineDocument, OutlineNode）
├── parser/          # 解析器（各格式 → 通用模型）
├── converter/       # 转换器（通用模型 → 各格式）
├── factory/         # 工厂类（解析器、转换器创建）
├── exception/       # 异常处理（完整异常体系）
└── constant/        # 常量定义（语法、验证规则等）
```

### 设计模式

- **工厂模式**: `ParserFactory`、`ConverterFactory` 管理解析器和转换器实例
- **构建者模式**: `ConvertEngine.Builder` 提供流畅的链式API
- **策略模式**: 通过接口 `FileParser`、`FormatConverter` 实现不同格式策略
- **单例模式**: 工厂类采用懒加载单例，缓存实例提高性能

### 数据流转

```
输入文件 → FileParser → OutlineDocument → FormatConverter → 输出文件
```

## 📊 性能特性

### 内存管理
- **文件大小限制**: 单个文件最大100MB，ZIP条目最大50MB
- **懒加载设计**: 解析器和转换器实例按需创建和缓存
- **流式处理**: 支持输入流和输出流，减少内存占用

### 并发性能
- **线程安全**: 使用`ConcurrentHashMap`缓存，支持高并发访问
- **无状态设计**: 核心组件无状态，避免并发冲突
- **实例复用**: 缓存机制减少对象创建开销

### 安全特性
- **XML安全**: 禁用外部实体解析，防止XXE攻击
- **输入验证**: 文件名、大小、格式严格验证
- **异常隔离**: 完善的异常处理，不会因单个文件错误影响整体

## 🔧 扩展新格式

### 1. 实现解析器接口

```java
public class CustomParser implements FileParser {
    @Override
    public OutlineDocument parse(File file) throws ParsingException {
        // 实现自定义格式解析逻辑
        // 将文件内容转换为OutlineDocument
    }
    
    @Override
    public OutlineDocument parse(InputStream inputStream, String filename) 
            throws ParsingException {
        // 实现流式解析
    }
    
    @Override
    public OutlineDocument parse(byte[] data, String filename) 
            throws ParsingException {
        // 实现字节数组解析
    }
}
```

### 2. 实现转换器接口

```java
public class CustomFormatConverter implements FormatConverter {
    @Override
    public void convert(OutlineDocument document, File outputFile) 
            throws ConvertException {
        // 实现转换逻辑：OutlineDocument → 自定义格式
    }
    
    @Override
    public void convert(OutlineDocument document, OutputStream outputStream) 
            throws ConvertException {
        // 实现流式转换
    }
    
    @Override
    public byte[] convertToBytes(OutlineDocument document) 
            throws ConvertException {
        // 实现字节数组转换
    }
    
    @Override
    public String getFormatName() { return "Custom Format"; }
    
    @Override
    public String getDefaultExtension() { return "custom"; }
    
    @Override
    public String getMimeType() { return "application/custom"; }
}
```

### 3. 注册新格式

在 `FileType` 枚举中添加新类型：
```java
CUSTOM("custom", "application/custom", "Custom Format");
```

在工厂类中添加创建逻辑：
```java
// ParserFactory.createParser()
case CUSTOM -> new CustomParser();

// ConverterFactory.createConverter()  
case CUSTOM -> new CustomFormatConverter();
```

## 📚 API 文档

### ConvertEngine 转换引擎

转换引擎是项目的核心入口，提供流畅的链式API：

```java
// 静态工厂方法
ConvertEngine.from(FileType sourceType)           // 指定源格式
ConvertEngine.from(File sourceFile)               // 自动检测源格式
ConvertEngine.from(String filename)               // 从文件名检测格式

// 链式调用方法
.to(FileType targetType)                          // 指定目标格式
.toJson() / .toMarkdown() / .toXMind() / .toOpml() // 便捷目标格式方法

// 转换执行方法
.convert(File input, File output)                 // 文件转文件
.convert(File input, String outputPath)           // 文件转路径
.convert(File input)                              // 文件转同目录
.convertToStream(File input, OutputStream out)    // 文件转流
.convertToBytes(File input)                       // 文件转字节数组
.convert(InputStream in, String filename, File out) // 流转文件
.convert(byte[] data, String filename, File out)  // 字节数组转文件
```

### FileType 文件类型枚举

提供文件类型管理和便捷转换方法：

```java
// 静态方法
FileType.fromFile(File file)                      // 从文件推断类型
FileType.fromFilename(String filename)            // 从文件名推断类型

// 实例方法
.supportsExtension(String extension)              // 检查扩展名支持
.getExtensions()                                  // 获取支持的扩展名
.getDefaultExtension()                            // 获取默认扩展名
.getMimeType()                                    // 获取MIME类型
.getDisplayName()                                 // 获取显示名称

// 链式转换方法
.toJson() / .toMarkdown() / .toXMind() / .toOpml() // 转换为目标格式
.to(FileType targetType)                          // 转换为指定格式
```

### 数据模型

#### OutlineDocument 大纲文档
```java
// 构造方法
new OutlineDocument()                             // 空文档
new OutlineDocument(String title)                // 带标题文档

// 节点管理
.addRootNode(OutlineNode node)                   // 添加根节点
.addRootNode(String title)                       // 添加根节点（标题）
.addRootNode(String title, String content)      // 添加根节点（标题+内容）
.removeRootNode(OutlineNode node)               // 移除根节点
.getRootNodes()                                 // 获取所有根节点

// 文档信息
.isEmpty()                                      // 是否为空
.getRootNodesCount()                            // 根节点数量
.getTotalNodesCount()                           // 总节点数量
.getMaxDepth()                                  // 最大深度
.traverse(NodeVisitor visitor)                  // 遍历所有节点

// 元数据管理
.setMetadata(String key, Object value)          // 设置元数据
.getMetadata(String key)                        // 获取元数据
.getMetadata(String key, Object defaultValue)   // 获取元数据（带默认值）
```

#### OutlineNode 大纲节点
```java
// 构造方法
new OutlineNode()                               // 空节点
new OutlineNode(String title)                  // 带标题节点
new OutlineNode(String title, String content)  // 带标题和内容节点

// 子节点管理
.addChild(OutlineNode child)                   // 添加子节点
.addChild(String title)                        // 添加子节点（标题）
.addChild(String title, String content)       // 添加子节点（标题+内容）
.removeChild(OutlineNode child)                // 移除子节点
.getChildren()                                 // 获取所有子节点

// 节点状态
.hasChildren()                                 // 是否有子节点
.getChildrenCount()                            // 子节点数量
.isRoot()                                      // 是否为根节点
.isLeaf()                                      // 是否为叶子节点

// 属性管理
.setAttribute(String key, Object value)        // 设置属性
.getAttribute(String key)                       // 获取属性
.getAttribute(String key, Object defaultValue) // 获取属性（带默认值）
.removeAttribute(String key)                    // 移除属性

// 节点操作
.traverse(NodeVisitor visitor)                  // 遍历此节点及其子树
.findChild(String title)                        // 查找指定标题的子节点
.copy()                                         // 浅复制节点
.deepCopy()                                     // 深复制节点及其子树
```

## 🔒 线程安全性

### 设计原则
- **无状态设计**: 核心组件（解析器、转换器）无状态，可安全并发使用
- **不可变对象**: 数据模型类采用不可变设计，避免并发修改
- **线程安全缓存**: 使用`ConcurrentHashMap`实现线程安全的实例缓存

### 并发使用
```java
// ✅ 安全：不同线程可以并发使用同一个转换引擎
ExecutorService executor = Executors.newFixedThreadPool(10);
for (File file : files) {
    executor.submit(() -> {
        ConvertEngine.from(file)
                    .toJson()
                    .convert(file);
    });
}

// ✅ 安全：工厂方法线程安全
FileParser parser1 = ParserFactory.getParser(FileType.MARKDOWN); // 线程1
FileParser parser2 = ParserFactory.getParser(FileType.MARKDOWN); // 线程2 (返回相同实例)

// ✅ 安全：数据模型不会在转换过程中被修改
OutlineDocument doc = parser.parse(file);
// doc 对象在多线程环境下只读访问是安全的
```

### 注意事项
- 数据模型对象(`OutlineDocument`, `OutlineNode`)创建后不应在多线程间共享修改
- 自定义扩展时需要确保实现类的线程安全性

## 🛠️ 构建和部署

### 本地构建
```bash
# 克隆项目
git clone https://github.com/yourusername/outline-convert.git
cd outline-convert

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

### 系统要求
- **Java**: 21+（使用了现代Java特性如switch表达式、文本块等）
- **Maven**: 3.6+
- **内存**: 建议512MB以上堆内存（处理大文件时）

### 依赖说明
- **FlexMark**: Markdown解析和渲染
- **Jackson**: JSON序列化和反序列化  
- **DOM4J**: XML文档解析
- **Apache Commons**: IO操作、字符串处理、压缩处理
- **SLF4J**: 日志框架

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范
- 遵循Java命名约定
- 添加适当的注释和文档
- 编写单元测试
- 确保线程安全性

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

感谢以下开源项目的支持：
- [FlexMark](https://github.com/vsch/flexmark-java) - Markdown处理
- [Jackson](https://github.com/FasterXML/jackson) - JSON处理
- [DOM4J](https://github.com/dom4j/dom4j) - XML处理
- [Apache Commons](https://commons.apache.org/) - 工具库集合