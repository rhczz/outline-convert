package dev.hc.convert.model;

import java.util.*;

/**
 * 通用大纲节点数据模型
 * 用作各种格式转换的中间数据载体
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class OutlineNode {
    
    /** 节点标题/文本内容 */
    private String title;
    
    /** 节点详细内容/描述 */
    private String content;
    
    /** 节点级别，从0开始 */
    private int level;
    
    /** 子节点列表 */
    private final List<OutlineNode> children;
    
    /** 节点属性，用于存储格式特定的元数据 */
    private final Map<String, Object> attributes;
    
    /** 父节点引用 */
    private OutlineNode parent;
    
    public OutlineNode() {
        this.children = new ArrayList<>();
        this.attributes = new HashMap<>();
        this.level = 0;
    }
    
    public OutlineNode(String title) {
        this();
        this.title = title;
    }
    
    public OutlineNode(String title, String content) {
        this(title);
        this.content = content;
    }
    
    /**
     * 添加子节点
     */
    public OutlineNode addChild(OutlineNode child) {
        if (child != null) {
            child.parent = this;
            child.level = this.level + 1;
            this.children.add(child);
        }
        return this;
    }
    
    /**
     * 添加子节点
     */
    public OutlineNode addChild(String title) {
        return addChild(new OutlineNode(title));
    }
    
    /**
     * 添加子节点
     */
    public OutlineNode addChild(String title, String content) {
        return addChild(new OutlineNode(title, content));
    }
    
    /**
     * 移除子节点
     */
    public boolean removeChild(OutlineNode child) {
        if (child != null && children.remove(child)) {
            child.parent = null;
            return true;
        }
        return false;
    }
    
    /**
     * 获取所有子节点
     */
    public List<OutlineNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    /**
     * 判断是否有子节点
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    /**
     * 获取子节点数量
     */
    public int getChildrenCount() {
        return children.size();
    }
    
    /**
     * 判断是否为根节点
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * 判断是否为叶子节点
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    /**
     * 设置属性
     */
    public OutlineNode setAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }
    
    /**
     * 获取属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * 获取属性，带默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        T value = (T) attributes.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 移除属性
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    /**
     * 获取所有属性
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /**
     * 深度优先遍历所有节点
     */
    public void traverse(NodeVisitor visitor) {
        visitor.visit(this);
        for (OutlineNode child : children) {
            child.traverse(visitor);
        }
    }
    
    /**
     * 查找子节点
     */
    public Optional<OutlineNode> findChild(String title) {
        return children.stream()
                .filter(child -> Objects.equals(child.title, title))
                .findFirst();
    }
    
    /**
     * 创建节点的副本（不包含父子关系）
     */
    public OutlineNode copy() {
        OutlineNode copy = new OutlineNode(this.title, this.content);
        copy.level = this.level;
        copy.attributes.putAll(this.attributes);
        return copy;
    }
    
    /**
     * 深度复制整个子树
     */
    public OutlineNode deepCopy() {
        OutlineNode copy = copy();
        for (OutlineNode child : children) {
            copy.addChild(child.deepCopy());
        }
        return copy;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public OutlineNode getParent() {
        return parent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutlineNode that = (OutlineNode) o;
        return level == that.level &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, content, level);
    }
    
    @Override
    public String toString() {
        return "OutlineNode{" +
                "title='" + title + '\'' +
                ", level=" + level +
                ", children=" + children.size() +
                '}';
    }
    
    /**
     * 节点访问者接口
     */
    @FunctionalInterface
    public interface NodeVisitor {
        void visit(OutlineNode node);
    }
}