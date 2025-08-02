package dev.hc.convert.constant;

/**
 * XML安全特性常量定义
 * 包含XML解析时用于防止XXE攻击等安全配置常量
 * 供OPML和XMind解析器共同使用
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class XmlSafety {
    
    // ========== XML安全特性常量 ==========
    
    /** 禁用DOCTYPE声明特性 */
    public static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    
    /** 外部通用实体特性 */
    public static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    
    /** 外部参数实体特性 */
    public static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    
    // ========== 私有构造器 ==========
    
    /**
     * 私有构造器，防止实例化
     */
    private XmlSafety() {
        throw new AssertionError("XmlSafety is a utility class and should not be instantiated");
    }
}