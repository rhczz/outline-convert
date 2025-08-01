package dev.hc.convert.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown节点模型
 */
public class MarkdownNode {
    private String title;
    private int level;
    private List<MarkdownNode> children;
    private MarkdownNode parent;

    public MarkdownNode() {
        this.children = new ArrayList<>();
    }

    public MarkdownNode(String title, int level) {
        this.title = title;
        this.level = level;
        this.children = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<MarkdownNode> getChildren() {
        return children;
    }

    public void setChildren(List<MarkdownNode> children) {
        this.children = children;
    }

    public MarkdownNode getParent() {
        return parent;
    }

    public void setParent(MarkdownNode parent) {
        this.parent = parent;
    }

    public void addChild(MarkdownNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public String toString() {
        return "MarkdownNode{" +
                "title='" + title + '\'' +
                ", level=" + level +
                ", children=" + children.size() +
                '}';
    }
}
