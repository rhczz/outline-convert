package dev.hc.convert.constant;

/**
 * XMind语法和格式常量定义
 * 包含XMind解析器使用的元素名称、属性名称、元数据键等
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class XMindSyntax {
    
    // ========== ZIP条目名称常量 ==========
    
    /** 内容XML文件名 */
    public static final String CONTENT_XML = "content.xml";
    
    /** 元数据XML文件名 */
    public static final String META_XML = "meta.xml";
    
    // ========== XMind元素名称常量 ==========
    
    /** 工作簿元素 */
    public static final String WORKBOOK_ELEMENT = "workbook";
    
    /** 工作表元素 */
    public static final String SHEET_ELEMENT = "sheet";
    
    /** 主题元素 */
    public static final String TOPIC_ELEMENT = "topic";
    
    /** 标题元素 */
    public static final String TITLE_ELEMENT = "title";
    
    /** 笔记元素 */
    public static final String NOTES_ELEMENT = "notes";
    
    /** 纯文本元素 */
    public static final String PLAIN_ELEMENT = "plain";
    
    /** 标签组元素 */
    public static final String LABELS_ELEMENT = "labels";
    
    /** 标签元素 */
    public static final String LABEL_ELEMENT = "label";
    
    /** 子节点元素 */
    public static final String CHILDREN_ELEMENT = "children";
    
    /** 主题组元素 */
    public static final String TOPICS_ELEMENT = "topics";
    
    // ========== XMind属性名称常量 ==========
    
    /** 名称属性 */
    public static final String NAME_ATTRIBUTE = "name";
    
    /** ID属性 */
    public static final String ID_ATTRIBUTE = "id";
    
    /** 样式ID属性 */
    public static final String STYLE_ID_ATTRIBUTE = "style-id";
    
    // ========== XMind元数据元素常量 ==========
    
    /** 创建者元素 */
    public static final String CREATOR_ELEMENT = "Creator";
    
    /** 创建者姓名元素 */
    public static final String CREATOR_NAME_ELEMENT = "Name";
    
    /** 创建者版本元素 */
    public static final String CREATOR_VERSION_ELEMENT = "Version";
    
    /** 创建时间元素 */
    public static final String CREATE_TIME_ELEMENT = "CreateTime";
    
    /** 修改时间元素 */
    public static final String MODIFY_TIME_ELEMENT = "ModifyTime";
    
    // ========== XMind元数据键常量 ==========
    
    /** 创建者元数据键 */
    public static final String CREATOR_METADATA_KEY = "creator";
    
    /** 创建者版本元数据键 */
    public static final String CREATOR_VERSION_METADATA_KEY = "creatorVersion";
    
    /** 创建时间元数据键 */
    public static final String CREATE_TIME_METADATA_KEY = "xmindCreateTime";
    
    /** 修改时间元数据键 */
    public static final String MODIFY_TIME_METADATA_KEY = "xmindModifyTime";
    
    /** XMind ID属性键 */
    public static final String XMIND_ID_ATTRIBUTE_KEY = "xmindId";
    
    /** 样式ID属性键 */
    public static final String STYLE_ID_ATTRIBUTE_KEY = "styleId";
    
    /** 标签属性键 */
    public static final String LABELS_ATTRIBUTE_KEY = "labels";
    
    // ========== XMind默认值常量 ==========
    
    /** 默认主题标题 */
    public static final String DEFAULT_TOPIC_TITLE = "Untitled";
    
    /** 标签分隔符 */
    public static final String LABEL_SEPARATOR = ", ";
    
    // ========== 私有构造器 ==========
    
    /**
     * 私有构造器，防止实例化
     */
    private XMindSyntax() {
        throw new AssertionError("XMindSyntax is a utility class and should not be instantiated");
    }
}