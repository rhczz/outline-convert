package dev.hc.convert.constant;

/**
 * OPML语法和格式常量定义
 * 包含OPML解析器使用的元素名称、属性名称、默认值等
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class OpmlSyntax {
    
    // ========== OPML元素名称常量 ==========
    
    /** OPML根元素 */
    public static final String OPML_ELEMENT = "opml";
    
    /** 头部元素 */
    public static final String HEAD_ELEMENT = "head";
    
    /** 正文元素 */
    public static final String BODY_ELEMENT = "body";
    
    /** 大纲元素 */
    public static final String OUTLINE_ELEMENT = "outline";
    
    /** 文档元素 */
    public static final String DOCS_ELEMENT = "docs";
    
    // ========== OPML头部元素常量 ==========
    
    /** 标题元素 */
    public static final String TITLE_ELEMENT = "title";
    
    /** 创建日期元素 */
    public static final String DATE_CREATED_ELEMENT = "dateCreated";
    
    /** 修改日期元素 */
    public static final String DATE_MODIFIED_ELEMENT = "dateModified";
    
    /** 所有者姓名元素 */
    public static final String OWNER_NAME_ELEMENT = "ownerName";
    
    /** 所有者邮箱元素 */
    public static final String OWNER_EMAIL_ELEMENT = "ownerEmail";
    
    /** 展开状态元素 */
    public static final String EXPANSION_STATE_ELEMENT = "expansionState";
    
    /** 垂直滚动状态元素 */
    public static final String VERT_SCROLL_STATE_ELEMENT = "vertScrollState";
    
    /** 窗口顶部元素 */
    public static final String WINDOW_TOP_ELEMENT = "windowTop";
    
    /** 窗口左侧元素 */
    public static final String WINDOW_LEFT_ELEMENT = "windowLeft";
    
    /** 窗口底部元素 */
    public static final String WINDOW_BOTTOM_ELEMENT = "windowBottom";
    
    /** 窗口右侧元素 */
    public static final String WINDOW_RIGHT_ELEMENT = "windowRight";
    
    // ========== OPML属性名称常量 ==========
    
    /** 文本属性 */
    public static final String TEXT_ATTRIBUTE = "text";
    
    /** 类型属性 */
    public static final String TYPE_ATTRIBUTE = "type";
    
    /** URL属性 */
    public static final String URL_ATTRIBUTE = "url";
    
    /** XML URL属性 */
    public static final String XML_URL_ATTRIBUTE = "xmlUrl";
    
    /** HTML URL属性 */
    public static final String HTML_URL_ATTRIBUTE = "htmlUrl";
    
    /** 描述属性 */
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    
    /** 分类属性 */
    public static final String CATEGORY_ATTRIBUTE = "category";
    
    /** 创建时间属性 */
    public static final String CREATED_ATTRIBUTE = "created";
    
    // ========== OPML默认值常量 ==========
    
    /** 默认大纲文本 */
    public static final String DEFAULT_OUTLINE_TEXT = "Untitled";
    
    // ========== 私有构造器 ==========
    
    /**
     * 私有构造器，防止实例化
     */
    private OpmlSyntax() {
        throw new AssertionError("OpmlSyntax is a utility class and should not be instantiated");
    }
}