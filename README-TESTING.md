# Outline Convert 项目测试指南

本项目包含完善的单元测试、集成测试和测试覆盖率配置。

## 测试结构

```
src/test/java/
├── dev/hc/convert/
│   ├── model/                    # 模型类测试
│   │   ├── OutlineDocumentTest.java
│   │   └── OutlineNodeTest.java
│   ├── engine/                   # 转换引擎测试
│   │   └── ConvertEngineTest.java
│   ├── factory/                  # 工厂类测试
│   │   ├── ConverterFactoryTest.java
│   │   └── ParserFactoryTest.java
│   ├── exception/                # 异常处理测试
│   │   ├── ConversionErrorCodeTest.java
│   │   ├── ConversionExceptionFactoryTest.java
│   │   ├── ConversionFailureExceptionTest.java
│   │   ├── FileOperationExceptionTest.java
│   │   ├── ParsingExceptionTest.java
│   │   └── ConvertExceptionTest.java
│   ├── integration/              # 集成测试
│   │   └── ConversionIntegrationTest.java
│   ├── FileTypeTest.java         # 文件类型枚举测试
│   └── TestSuite.java           # 完整测试套件
└── resources/                    # 测试资源
    ├── sample.json
    ├── sample.md
    ├── sample.opml
    ├── invalid.json
    └── empty.md
```

## 运行测试

### 1. 运行所有单元测试
```bash
mvn test
```

### 2. 运行集成测试
```bash
mvn integration-test
```

### 3. 运行完整测试套件（单元测试 + 集成测试）
```bash
mvn verify
```

### 4. 运行特定测试类
```bash
# 运行单个测试类
mvn test -Dtest=OutlineDocumentTest

# 运行特定测试方法
mvn test -Dtest=OutlineDocumentTest#testDefaultConstructor

# 运行测试套件
mvn test -Dtest=TestSuite
```

### 5. 运行集成测试
```bash
mvn test -Dtest=ConversionIntegrationTest
```

## 测试覆盖率

### 1. 生成覆盖率报告
```bash
mvn clean verify
```

覆盖率报告将生成在以下位置：
- 单元测试覆盖率：`target/site/jacoco/index.html`
- 集成测试覆盖率：`target/site/jacoco-it/index.html`
- 合并覆盖率：`target/site/jacoco-merged/index.html`

### 2. 查看覆盖率要求
本项目设置的覆盖率要求：
- 包级别：行覆盖率 ≥ 80%，分支覆盖率 ≥ 75%
- 类级别：行覆盖率 ≥ 70%

### 3. 覆盖率检查
在 `mvn verify` 阶段会自动检查覆盖率要求，如果不满足要求构建将失败。

## 测试分类

### 单元测试
- **模型类测试**：测试 `OutlineDocument` 和 `OutlineNode` 的所有方法
- **引擎测试**：测试 `ConvertEngine` 的API设计和验证逻辑
- **工厂类测试**：测试转换器和解析器工厂的缓存和线程安全
- **文件类型测试**：测试文件类型推断和支持的扩展名
- **异常处理测试**：测试所有异常类的创建、继承和序列化

### 集成测试
- **API使用测试**：测试流畅API的易用性
- **错误处理集成测试**：测试端到端的错误处理流程
- **文件验证集成测试**：测试文件验证逻辑
- **并发安全性测试**：测试多线程环境下的安全性
- **性能基准测试**：测试API的性能表现
- **内存使用测试**：测试内存使用的合理性

## 测试特性

### 1. 全面的测试覆盖
- ✅ 构造函数测试
- ✅ 方法行为测试
- ✅ 边界条件测试
- ✅ 异常情况测试
- ✅ 并发安全测试
- ✅ 内存使用测试
- ✅ 序列化测试

### 2. 测试数据
项目提供了完整的测试数据文件：
- `sample.json`：标准JSON格式测试数据
- `sample.md`：标准Markdown格式测试数据
- `sample.opml`：标准OPML格式测试数据
- `invalid.json`：无效JSON用于错误测试
- `empty.md`：空文件用于边界测试

### 3. 国际化支持
测试包含对中文文件名、Unicode字符、特殊字符的全面支持。

### 4. JUnit 5 特性
- 使用 `@Nested` 组织测试结构
- 使用 `@DisplayName` 提供中文测试描述
- 使用 `@TempDir` 管理测试文件
- 使用 `@BeforeEach` 和 `@AfterEach` 管理测试状态

## 静态代码分析

### SpotBugs 分析
```bash
mvn spotbugs:check
```

分析报告位置：`target/site/spotbugs.html`

## 测试报告

### 生成完整项目站点
```bash
mvn site
```

项目站点将包含：
- 测试报告
- 覆盖率报告
- SpotBugs分析报告
- JavaDoc文档

站点位置：`target/site/index.html`

### 调试技巧

1. **单独运行失败的测试**
   ```bash
   mvn test -Dtest=ClassName#methodName
   ```

2. **启用详细日志**
   ```bash
   mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
   ```

3. **跳过集成测试（仅运行单元测试）**
   ```bash
   mvn test -DskipITs=true
   ```

4. **跳过覆盖率检查**
   ```bash
   mvn verify -Djacoco.skip=true
   ```