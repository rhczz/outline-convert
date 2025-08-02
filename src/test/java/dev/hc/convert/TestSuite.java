package dev.hc.convert;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 完整测试套件
 * 运行所有测试用例
 */
@Suite
@SuiteDisplayName("Outline Convert 完整测试套件")
@SelectPackages({
    "dev.hc.convert.model",
    "dev.hc.convert.engine",
    "dev.hc.convert.factory",
    "dev.hc.convert.exception",
    "dev.hc.convert.integration"
})
public class TestSuite {
    // 测试套件类不需要任何方法
    // 所有配置都通过注解完成
}