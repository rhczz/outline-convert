package dev.hc.convert.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大纲文档数据模型
 * 代表整个文档的结构和元数据
 * 
 * @author Leo
 * @since 2025/8/1
 */
public class OutlineDocument {
    
    /** 文档标题 */
    private String title;
    
    /** 文档描述 */
    private String description;
    
    /** 文档根节点列表 */
    private final List<OutlineNode> rootNodes;
    
    /** 文档元数据 */
    private final Map<String, Object> metadata;
    
    /** 原始文件格式 */
    private String sourceFormat;
    
    /** 创建时间 */
    private long createTime;
    
    /** 修改时间 */
    private long modifyTime;
    
    public OutlineDocument() {
        this.rootNodes = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.createTime = System.currentTimeMillis();
        this.modifyTime = this.createTime;
    }
    
    public OutlineDocument(String title) {
        this();
        this.title = title;
    }
    
    /**
     * 添加根节点
     */
    public OutlineDocument addRootNode(OutlineNode node) {
        if (node != null) {
            node.setLevel(0);
            this.rootNodes.add(node);
            this.modifyTime = System.currentTimeMillis();
        }
        return this;
    }
    
    /**
     * 添加根节点
     */
    public OutlineDocument addRootNode(String title) {
        return addRootNode(new OutlineNode(title));
    }
    
    /**
     * 添加根节点
     */
    public OutlineDocument addRootNode(String title, String content) {
        return addRootNode(new OutlineNode(title, content));
    }
    
    /**
     * 移除根节点
     */
    public boolean removeRootNode(OutlineNode node) {
        boolean removed = rootNodes.remove(node);
        if (removed) {
            this.modifyTime = System.currentTimeMillis();
        }
        return removed;
    }
    
    /**
     * 获取所有根节点
     */
    public List<OutlineNode> getRootNodes() {
        return new ArrayList<>(rootNodes);
    }
    
    /**
     * 判断是否为空文档
     */
    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }
    
    /**
     * 获取根节点数量
     */
    public int getRootNodesCount() {
        return rootNodes.size();
    }
    
    /**
     * 计算文档总节点数
     */
    public int getTotalNodesCount() {
        int count = 0;
        for (OutlineNode root : rootNodes) {
            count += countNodes(root);
        }
        return count;
    }
    
    private int countNodes(OutlineNode node) {
        int count = 1; // 当前节点
        for (OutlineNode child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }
    
    /**
     * 计算文档最大深度
     */
    public int getMaxDepth() {
        int maxDepth = 0;
        for (OutlineNode root : rootNodes) {
            maxDepth = Math.max(maxDepth, getNodeDepth(root));
        }
        return maxDepth;
    }
    
    private int getNodeDepth(OutlineNode node) {
        if (node.getChildren().isEmpty()) {
            return 1;
        }
        int maxChildDepth = 0;
        for (OutlineNode child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, getNodeDepth(child));
        }
        return 1 + maxChildDepth;
    }
    
    /**
     * 遍历所有节点
     */
    public void traverse(OutlineNode.NodeVisitor visitor) {
        for (OutlineNode root : rootNodes) {
            root.traverse(visitor);
        }
    }
    
    /**
     * 设置元数据
     */
    public OutlineDocument setMetadata(String key, Object value) {
        this.metadata.put(key, value);
        this.modifyTime = System.currentTimeMillis();
        return this;
    }
    
    /**
     * 获取元数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }
    
    /**
     * 获取元数据，带默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        T value = (T) metadata.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 清空所有内容
     */
    public void clear() {
        rootNodes.clear();
        metadata.clear();
        this.modifyTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.modifyTime = System.currentTimeMillis();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.modifyTime = System.currentTimeMillis();
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public String getSourceFormat() {
        return sourceFormat;
    }
    
    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getModifyTime() {
        return modifyTime;
    }
    
    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
    
    @Override
    public String toString() {
        return "OutlineDocument{" +
                "title='" + title + '\'' +
                ", rootNodes=" + rootNodes.size() +
                ", totalNodes=" + getTotalNodesCount() +
                ", maxDepth=" + getMaxDepth() +
                '}';
    }
}