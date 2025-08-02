package dev.hc.convert.constant;

/**
 * Markdown语法常量定义
 * 包含所有常用的Markdown语法标记和符号
 * 
 * @author Leo
 * @since 2025/8/1
 */
public final class MarkdownSyntax {
    
    /** ========== 标题语法 ========== */
    public static final String HEADING_1 = "#";
    public static final String HEADING_2 = "##";
    public static final String HEADING_3 = "###";
    public static final String HEADING_4 = "####";
    public static final String HEADING_5 = "#####";
    public static final String HEADING_6 = "######";
    public static final String HEADING_PREFIX = "#";
    public static final String HEADING_SPACE = " ";
    
    /** ========== 强调语法 ========== */
    public static final String EMPHASIS_ASTERISK = "*";
    public static final String EMPHASIS_UNDERSCORE = "_";
    public static final String STRONG_EMPHASIS_ASTERISK = "**";
    public static final String STRONG_EMPHASIS_UNDERSCORE = "__";
    public static final String STRIKETHROUGH = "~~";
    
    /** ========== 代码语法 ========== */
    public static final String INLINE_CODE = "`";
    public static final String CODE_BLOCK_BACKTICK = "```";
    public static final String CODE_BLOCK_TILDE = "~~~";
    public static final String INDENTED_CODE_SPACES = "    ";
    public static final String INDENTED_CODE_TAB = "\t";
    
    /** ========== 链接语法 ========== */
    public static final String LINK_TEXT_START = "[";
    public static final String LINK_TEXT_END = "]";
    public static final String LINK_URL_START = "(";
    public static final String LINK_URL_END = ")";
    public static final String REFERENCE_LINK_START = "[";
    public static final String REFERENCE_LINK_END = "]";
    public static final String REFERENCE_LINK_LABEL_START = "[";
    public static final String REFERENCE_LINK_LABEL_END ="]";
    public static final String AUTOLINK_START = "<";
    public static final String AUTOLINK_END = ">";
    
    /** ========== 图片语法 ========== */
    public static final String IMAGE_PREFIX = "!";
    public static final String IMAGE_ALT_START = "[";
    public static final String IMAGE_ALT_END = "]";
    public static final String IMAGE_URL_START = "(";
    public static final String IMAGE_URL_END = ")";
    
    /** ========== 列表语法 ========== */
    public static final String UNORDERED_LIST_ASTERISK = "* ";
    public static final String UNORDERED_LIST_DASH = "- ";
    public static final String UNORDERED_LIST_PLUS = "+ ";
    public static final String ORDERED_LIST_DOT = ". ";
    public static final String ORDERED_LIST_PAREN = ") ";
    public static final String TASK_LIST_UNCHECKED = "- [ ] ";
    public static final String TASK_LIST_CHECKED = "- [x] ";
    public static final String TASK_LIST_CHECKBOX_UNCHECKED = "[ ]";
    public static final String TASK_LIST_CHECKBOX_CHECKED = "[x]";
    
    /** ========== 引用语法 ========== */
    public static final String BLOCKQUOTE = "> ";
    public static final String BLOCKQUOTE_NESTED = ">> ";
    public static final String BLOCKQUOTE_MARKER = ">";
    
    /** ========== 分隔线语法 ========== */
    public static final String HORIZONTAL_RULE_ASTERISK = "***";
    public static final String HORIZONTAL_RULE_DASH = "---";
    public static final String HORIZONTAL_RULE_UNDERSCORE = "___";
    public static final String HORIZONTAL_RULE_LONG_ASTERISK = "*****";
    public static final String HORIZONTAL_RULE_LONG_DASH = "-----";
    public static final String HORIZONTAL_RULE_LONG_UNDERSCORE = "_____";
    
    /** ========== 表格语法 ========== */
    public static final String TABLE_SEPARATOR = "|";
    public static final String TABLE_HEADER_SEPARATOR = "-";
    public static final String TABLE_ALIGN_LEFT = ":--";
    public static final String TABLE_ALIGN_CENTER = ":-:";
    public static final String TABLE_ALIGN_RIGHT = "--:";
    
    /** ========== 换行语法 ========== */
    public static final String LINE_BREAK_DOUBLE_SPACE = "  ";
    public static final String LINE_BREAK_BACKSLASH = "\\";
    public static final String PARAGRAPH_BREAK = "\n\n";
    public static final String HARD_BREAK = "  \n";
    
    /** ========== 转义语法 ========== */
    public static final String ESCAPE_CHAR = "\\";
    public static final String[] ESCAPABLE_CHARS = {
        "\\", "`", "*", "_", "{", "}", "[", "]", 
        "(", ")", "#", "+", "-", ".", "!", "|"
    };
    
    /** ========== HTML语法 ========== */
    public static final String HTML_COMMENT_START = "<!--";
    public static final String HTML_COMMENT_END = "-->";
    public static final String HTML_TAG_START = "<";
    public static final String HTML_TAG_END = ">";
    public static final String HTML_CLOSING_TAG = "</";
    
    /** ========== 数学公式语法（扩展） ========== */
    public static final String MATH_INLINE_DOLLAR = "$";
    public static final String MATH_BLOCK_DOUBLE_DOLLAR = "$$";
    public static final String MATH_INLINE_PARENTHESES = "\\(";
    public static final String MATH_INLINE_PARENTHESES_END = "\\)";
    public static final String MATH_BLOCK_BRACKETS = "\\[";
    public static final String MATH_BLOCK_BRACKETS_END = "\\]";
    
    /** ========== 脚注语法（扩展） ========== */
    public static final String FOOTNOTE_START = "[^";
    public static final String FOOTNOTE_END = "]";
    public static final String FOOTNOTE_DEFINITION = ":";
    
    /** ========== 定义列表语法（扩展） ========== */
    public static final String DEFINITION_TERM_MARKER = "";
    public static final String DEFINITION_DESCRIPTION_MARKER = ":   ";
    public static final String DEFINITION_DESCRIPTION_TILDE = "~   ";
    
    /** ========== 高亮语法（扩展） ========== */
    public static final String HIGHLIGHT = "==";
    public static final String MARK_START = "<mark>";
    public static final String MARK_END = "</mark>";
    
    /** ========== 上标和下标语法（扩展） ========== */
    public static final String SUPERSCRIPT = "^";
    public static final String SUBSCRIPT = "~";
    public static final String SUP_TAG_START = "<sup>";
    public static final String SUP_TAG_END = "</sup>";
    public static final String SUB_TAG_START = "<sub>";
    public static final String SUB_TAG_END = "</sub>";
    
    /** ========== 缩写语法（扩展） ========== */
    public static final String ABBREVIATION_START = "*[";
    public static final String ABBREVIATION_END = "]:";
    
    /** ========== 键盘按键语法（扩展） ========== */
    public static final String KEYBOARD_KEY_START = "<kbd>";
    public static final String KEYBOARD_KEY_END = "</kbd>";
    
    /** ========== 常用标点符号 ========== */
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String NEWLINE = "\n";
    public static final String CARRIAGE_RETURN = "\r";
    public static final String EMPTY_STRING = "";
    
    /** ========== 常用正则表达式模式 ========== */
    public static final String HEADING_PATTERN = "^(#{1,6})\\s+(.*)$";
    public static final String LIST_ITEM_PATTERN = "^(\\s*)([-*+]|\\d+[.)])\\s+(.*)$";
    public static final String BLOCKQUOTE_PATTERN = "^\\s*>\\s*(.*)$";
    public static final String CODE_BLOCK_PATTERN = "^```(\\w*)$";
    public static final String HORIZONTAL_RULE_PATTERN = "^\\s*([-*_]\\s*){3,}$";
    public static final String LINK_PATTERN = "\\[([^\\]]+)\\]\\(([^)]+)\\)";
    public static final String IMAGE_PATTERN = "!\\[([^\\]]*)]\\(([^)]+)\\)";
    public static final String INLINE_CODE_PATTERN = "`([^`]+)`";
    public static final String EMPHASIS_PATTERN = "\\*([^*]+)\\*|_([^_]+)_";
    public static final String STRONG_PATTERN = "\\*\\*([^*]+)\\*\\*|__([^_]+)__";
    
    /** ========== 节点类型常量 ========== */
    public static final String NODE_TYPE_HEADING = "heading";
    public static final String NODE_TYPE_PARAGRAPH = "paragraph";
    public static final String NODE_TYPE_LIST = "list";
    public static final String NODE_TYPE_LIST_ITEM = "list_item";
    public static final String NODE_TYPE_BLOCKQUOTE = "blockquote";
    public static final String NODE_TYPE_CODE_BLOCK = "code_block";
    public static final String NODE_TYPE_HORIZONTAL_RULE = "horizontal_rule";
    public static final String NODE_TYPE_TABLE = "table";
    public static final String NODE_TYPE_IMAGE = "image";
    public static final String NODE_TYPE_LINK = "link";
    
    /** ========== 文档属性常量 ========== */
    public static final String HEADING_LEVEL_ATTRIBUTE = "headingLevel";
    public static final String CONTENT_NODE_TITLE = "Content";
    public static final String DOCUMENT_NODE_TITLE = "Document";
    public static final String LIST_TYPE_ATTRIBUTE = "listType";
    public static final String LIST_TIGHT_ATTRIBUTE = "tight";
    public static final String CODE_LANGUAGE_ATTRIBUTE = "language";
    public static final String LINK_URL_ATTRIBUTE = "url";
    public static final String IMAGE_URL_ATTRIBUTE = "src";
    public static final String IMAGE_ALT_ATTRIBUTE = "alt";
    
    /** ========== 默认值常量 ========== */
    public static final String DEFAULT_HEADING_TITLE = "Untitled";
    public static final String DEFAULT_LIST_TYPE = "unordered";
    public static final String DEFAULT_CODE_LANGUAGE = "";
    public static final int DEFAULT_HEADING_LEVEL = 1;
    public static final int MAX_HEADING_LEVEL = 6;
    public static final int MIN_HEADING_LEVEL = 1;
    
    /** ========== 语法优先级常量 ========== */
    public static final int PRIORITY_HEADING = 100;
    public static final int PRIORITY_CODE_BLOCK = 90;
    public static final int PRIORITY_HORIZONTAL_RULE = 80;
    public static final int PRIORITY_LIST = 70;
    public static final int PRIORITY_BLOCKQUOTE = 60;
    public static final int PRIORITY_PARAGRAPH = 50;
    public static final int PRIORITY_INLINE_CODE = 40;
    public static final int PRIORITY_LINK = 30;
    public static final int PRIORITY_IMAGE = 25;
    public static final int PRIORITY_EMPHASIS = 20;
    public static final int PRIORITY_TEXT = 10;

    /** 私有构造函数，防止实例化 */
    private MarkdownSyntax() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}