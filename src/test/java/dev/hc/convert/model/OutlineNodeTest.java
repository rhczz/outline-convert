package dev.hc.convert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OutlineNode 单元测试
 */
@DisplayName("大纲节点测试")
class OutlineNodeTest {

    private OutlineNode rootNode;
    private OutlineNode childNode1;
    private OutlineNode childNode2;
    private OutlineNode grandChild;

    @BeforeEach
    void setUp() {
        rootNode = new OutlineNode("根节点", "根节点内容");
        childNode1 = new OutlineNode("子节点1", "子节点1内容");
        childNode2 = new OutlineNode("子节点2");
        grandChild = new OutlineNode("孙节点", "孙节点内容");
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数应该创建空节点")
        void testDefaultConstructor() {
            OutlineNode node = new OutlineNode();
            
            assertNull(node.getTitle());
            assertNull(node.getContent());
            assertEquals(0, node.getLevel());
            assertTrue(node.getChildren().isEmpty());
            assertTrue(node.getAttributes().isEmpty());
            assertNull(node.getParent());
        }

        @Test
        @DisplayName("带标题的构造函数应该设置标题")
        void testTitleConstructor() {
            OutlineNode node = new OutlineNode("测试标题");
            
            assertEquals("测试标题", node.getTitle());
            assertNull(node.getContent());
            assertEquals(0, node.getLevel());
            assertTrue(node.getChildren().isEmpty());
        }

        @Test
        @DisplayName("带标题和内容的构造函数应该设置标题和内容")
        void testTitleAndContentConstructor() {
            OutlineNode node = new OutlineNode("测试标题", "测试内容");
            
            assertEquals("测试标题", node.getTitle());
            assertEquals("测试内容", node.getContent());
            assertEquals(0, node.getLevel());
        }
    }

    @Nested
    @DisplayName("子节点管理测试")
    class ChildManagementTests {

        @Test
        @DisplayName("添加子节点应该设置父子关系和级别")
        void testAddChild() {
            rootNode.addChild(childNode1);
            
            assertEquals(1, rootNode.getChildrenCount());
            assertTrue(rootNode.hasChildren());
            assertEquals(rootNode, childNode1.getParent());
            assertEquals(1, childNode1.getLevel());
            assertEquals(childNode1, rootNode.getChildren().get(0));
        }

        @Test
        @DisplayName("添加null子节点应该被忽略")
        void testAddNullChild() {
            rootNode.addChild((OutlineNode) null);
            
            assertEquals(0, rootNode.getChildrenCount());
            assertFalse(rootNode.hasChildren());
        }

        @Test
        @DisplayName("添加字符串子节点应该创建新节点")
        void testAddChildByString() {
            rootNode.addChild("字符串子节点");
            
            assertEquals(1, rootNode.getChildrenCount());
            assertEquals("字符串子节点", rootNode.getChildren().get(0).getTitle());
        }

        @Test
        @DisplayName("添加带内容的字符串子节点应该创建新节点")
        void testAddChildByStringWithContent() {
            rootNode.addChild("标题", "内容");
            
            assertEquals(1, rootNode.getChildrenCount());
            OutlineNode child = rootNode.getChildren().get(0);
            assertEquals("标题", child.getTitle());
            assertEquals("内容", child.getContent());
        }

        @Test
        @DisplayName("移除子节点应该断开父子关系")
        void testRemoveChild() {
            rootNode.addChild(childNode1);
            boolean removed = rootNode.removeChild(childNode1);
            
            assertTrue(removed);
            assertEquals(0, rootNode.getChildrenCount());
            assertNull(childNode1.getParent());
        }

        @Test
        @DisplayName("移除不存在的子节点应该返回false")
        void testRemoveNonExistentChild() {
            boolean removed = rootNode.removeChild(childNode1);
            
            assertFalse(removed);
        }

        @Test
        @DisplayName("移除null子节点应该返回false")
        void testRemoveNullChild() {
            boolean removed = rootNode.removeChild(null);
            
            assertFalse(removed);
        }

        @Test
        @DisplayName("多级嵌套应该正确设置级别")
        void testMultiLevelNesting() {
            rootNode.addChild(childNode1);
            childNode1.addChild(grandChild);
            
            assertEquals(0, rootNode.getLevel());
            assertEquals(1, childNode1.getLevel());
            assertEquals(2, grandChild.getLevel());
        }

        @Test
        @DisplayName("获取子节点列表应该返回不可修改的副本")
        void testGetChildrenIsImmutable() {
            rootNode.addChild(childNode1);
            List<OutlineNode> children = rootNode.getChildren();
            
            assertThrows(UnsupportedOperationException.class, () -> {
                children.add(childNode2);
            });
        }
    }

    @Nested
    @DisplayName("节点状态测试")
    class NodeStateTests {

        @Test
        @DisplayName("根节点应该正确识别")
        void testIsRoot() {
            assertTrue(rootNode.isRoot());
            
            rootNode.addChild(childNode1);
            assertFalse(childNode1.isRoot());
        }

        @Test
        @DisplayName("叶子节点应该正确识别")
        void testIsLeaf() {
            assertTrue(rootNode.isLeaf());
            
            rootNode.addChild(childNode1);
            assertFalse(rootNode.isLeaf());
            assertTrue(childNode1.isLeaf());
        }

        @Test
        @DisplayName("子节点数量应该正确统计")
        void testChildrenCount() {
            assertEquals(0, rootNode.getChildrenCount());
            
            rootNode.addChild(childNode1);
            assertEquals(1, rootNode.getChildrenCount());
            
            rootNode.addChild(childNode2);
            assertEquals(2, rootNode.getChildrenCount());
        }

        @Test
        @DisplayName("hasChildren应该正确判断")
        void testHasChildren() {
            assertFalse(rootNode.hasChildren());
            
            rootNode.addChild(childNode1);
            assertTrue(rootNode.hasChildren());
        }
    }

    @Nested
    @DisplayName("属性管理测试")
    class AttributeTests {

        @Test
        @DisplayName("设置和获取属性")
        void testSetAndGetAttribute() {
            rootNode.setAttribute("key1", "value1");
            rootNode.setAttribute("key2", 123);
            
            assertEquals("value1", rootNode.getAttribute("key1"));
            assertEquals(Integer.valueOf(123), rootNode.getAttribute("key2"));
        }

        @Test
        @DisplayName("获取不存在的属性应该返回null")
        void testGetNonExistentAttribute() {
            assertNull(rootNode.getAttribute("nonexistent"));
        }

        @Test
        @DisplayName("获取属性带默认值")
        void testGetAttributeWithDefault() {
            assertEquals("default", rootNode.getAttribute("nonexistent", "default"));
            
            rootNode.setAttribute("existing", "value");
            assertEquals("value", rootNode.getAttribute("existing", "default"));
        }

        @Test
        @DisplayName("移除属性")
        void testRemoveAttribute() {
            rootNode.setAttribute("key", "value");
            Object removed = rootNode.removeAttribute("key");
            
            assertEquals("value", removed);
            assertNull(rootNode.getAttribute("key"));
        }

        @Test
        @DisplayName("移除不存在的属性应该返回null")
        void testRemoveNonExistentAttribute() {
            Object removed = rootNode.removeAttribute("nonexistent");
            assertNull(removed);
        }

        @Test
        @DisplayName("获取所有属性应该返回不可修改副本")
        void testGetAttributesIsImmutable() {
            rootNode.setAttribute("key", "value");
            var attributes = rootNode.getAttributes();
            
            assertThrows(UnsupportedOperationException.class, () -> {
                attributes.put("newkey", "newvalue");
            });
        }
    }

    @Nested
    @DisplayName("节点操作测试")
    class NodeOperationTests {

        @Test
        @DisplayName("查找子节点")
        void testFindChild() {
            rootNode.addChild(childNode1);
            rootNode.addChild(childNode2);
            
            Optional<OutlineNode> found = rootNode.findChild("子节点1");
            assertTrue(found.isPresent());
            assertEquals(childNode1, found.get());
            
            Optional<OutlineNode> notFound = rootNode.findChild("不存在的节点");
            assertFalse(notFound.isPresent());
        }

        @Test
        @DisplayName("查找null标题子节点")
        void testFindChildWithNullTitle() {
            OutlineNode nullTitleNode = new OutlineNode();
            rootNode.addChild(nullTitleNode);
            
            Optional<OutlineNode> found = rootNode.findChild(null);
            assertTrue(found.isPresent());
            assertEquals(nullTitleNode, found.get());
        }

        @Test
        @DisplayName("遍历节点")
        void testTraverse() {
            // 构建树结构
            rootNode.addChild(childNode1);
            rootNode.addChild(childNode2);
            childNode1.addChild(grandChild);
            
            AtomicInteger visitCount = new AtomicInteger(0);
            StringBuilder visitOrder = new StringBuilder();
            
            rootNode.traverse(node -> {
                visitCount.incrementAndGet();
                visitOrder.append(node.getTitle()).append(",");
            });
            
            assertEquals(4, visitCount.get());
            assertEquals("根节点,子节点1,孙节点,子节点2,", visitOrder.toString());
        }

        @Test
        @DisplayName("复制节点（浅复制）")
        void testCopy() {
            rootNode.setAttribute("attr", "value");
            rootNode.setLevel(5);
            
            OutlineNode copy = rootNode.copy();
            
            assertEquals(rootNode.getTitle(), copy.getTitle());
            assertEquals(rootNode.getContent(), copy.getContent());
            assertEquals(rootNode.getLevel(), copy.getLevel());
            assertEquals("value", copy.getAttribute("attr"));
            assertNull(copy.getParent());
            assertTrue(copy.getChildren().isEmpty());
        }

        @Test
        @DisplayName("深度复制节点")
        void testDeepCopy() {
            rootNode.addChild(childNode1);
            childNode1.addChild(grandChild);
            rootNode.setAttribute("attr", "value");
            
            OutlineNode deepCopy = rootNode.deepCopy();
            
            assertEquals(rootNode.getTitle(), deepCopy.getTitle());
            assertEquals(1, deepCopy.getChildrenCount());
            assertEquals(childNode1.getTitle(), deepCopy.getChildren().get(0).getTitle());
            assertEquals(1, deepCopy.getChildren().get(0).getChildrenCount());
            assertEquals(grandChild.getTitle(), 
                deepCopy.getChildren().get(0).getChildren().get(0).getTitle());
            
            // 验证是不同的对象实例
            assertNotSame(rootNode, deepCopy);
            assertNotSame(childNode1, deepCopy.getChildren().get(0));
        }
    }

    @Nested
    @DisplayName("Getters和Setters测试")
    class GettersSettersTests {

        @Test
        @DisplayName("标题设置和获取")
        void testTitleGetterSetter() {
            rootNode.setTitle("新标题");
            assertEquals("新标题", rootNode.getTitle());
        }

        @Test
        @DisplayName("内容设置和获取")
        void testContentGetterSetter() {
            rootNode.setContent("新内容");
            assertEquals("新内容", rootNode.getContent());
        }

        @Test
        @DisplayName("级别设置和获取")
        void testLevelGetterSetter() {
            rootNode.setLevel(5);
            assertEquals(5, rootNode.getLevel());
        }

        @Test
        @DisplayName("父节点获取")
        void testGetParent() {
            assertNull(rootNode.getParent());
            
            rootNode.addChild(childNode1);
            assertEquals(rootNode, childNode1.getParent());
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同内容的节点应该相等")
        void testEquals() {
            OutlineNode node1 = new OutlineNode("标题", "内容");
            node1.setLevel(1);
            
            OutlineNode node2 = new OutlineNode("标题", "内容");
            node2.setLevel(1);
            
            assertEquals(node1, node2);
            assertEquals(node1.hashCode(), node2.hashCode());
        }

        @Test
        @DisplayName("不同内容的节点应该不相等")
        void testNotEquals() {
            OutlineNode node1 = new OutlineNode("标题1", "内容");
            OutlineNode node2 = new OutlineNode("标题2", "内容");
            
            assertNotEquals(node1, node2);
        }

        @Test
        @DisplayName("与null比较应该返回false")
        void testEqualsWithNull() {
            assertNotEquals(rootNode, null);
        }

        @Test
        @DisplayName("与不同类型对象比较应该返回false")
        void testEqualsWithDifferentType() {
            assertNotEquals(rootNode, "string");
        }

        @Test
        @DisplayName("与自身比较应该返回true")
        void testEqualsWithSelf() {
            assertEquals(rootNode, rootNode);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString应该包含关键信息")
        void testToString() {
            rootNode.addChild(childNode1);
            rootNode.addChild(childNode2);
            rootNode.setLevel(1);
            
            String str = rootNode.toString();
            
            assertTrue(str.contains("根节点"));
            assertTrue(str.contains("level=1"));
            assertTrue(str.contains("children=2"));
        }
    }
}