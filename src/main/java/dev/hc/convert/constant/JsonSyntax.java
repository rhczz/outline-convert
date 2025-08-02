package dev.hc.convert.constant;

/**
 * JSON语法和格式常量定义
 * 包含JSON解析器使用的字段名称、格式化常量等
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class JsonSyntax {
    
    // ========== JSON字段名称常量 ==========
    
    /** 标题字段名 */
    public static final String TITLE_FIELD = "title";
    
    /** 内容字段名 */
    public static final String CONTENT_FIELD = "content";
    
    /** 子节点字段名 */
    public static final String CHILDREN_FIELD = "children";
    
    /** 属性字段名 */
    public static final String ATTRIBUTES_FIELD = "attributes";
    
    /** 根节点数组字段名 */
    public static final String ROOT_NODES_FIELD = "rootNodes";
    
    /** 描述字段名 */
    public static final String DESCRIPTION_FIELD = "description";
    
    // ========== JSON格式化常量 ==========
    
    /** 数组项目标题前缀 */
    public static final String ARRAY_ITEM_PREFIX = "Item ";
    
    // ========== 私有构造器 ==========
    
    /**
     * 私有构造器，防止实例化
     */
    private JsonSyntax() {
        throw new AssertionError("JsonSyntax is a utility class and should not be instantiated");
    }
}