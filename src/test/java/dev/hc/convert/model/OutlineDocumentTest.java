package dev.hc.convert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OutlineDocument 单元测试
 */
@DisplayName("大纲文档测试")
class OutlineDocumentTest {

    private OutlineDocument document;
    private OutlineNode rootNode1;
    private OutlineNode rootNode2;
    private OutlineNode childNode;

    @BeforeEach
    void setUp() {
        document = new OutlineDocument();
        rootNode1 = new OutlineNode("根节点1", "根节点1内容");
        rootNode2 = new OutlineNode("根节点2", "根节点2内容");
        childNode = new OutlineNode("子节点", "子节点内容");
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数应该创建空文档")
        void testDefaultConstructor() {
            OutlineDocument doc = new OutlineDocument();
            
            assertNull(doc.getTitle());
            assertNull(doc.getDescription());
            assertTrue(doc.getRootNodes().isEmpty());
            assertTrue(doc.getMetadata().isEmpty());
            assertTrue(doc.isEmpty());
            assertNotEquals(0, doc.getCreateTime());
            assertNotEquals(0, doc.getModifyTime());
            assertEquals(doc.getCreateTime(), doc.getModifyTime());
        }

        @Test
        @DisplayName("带标题的构造函数应该设置标题")
        void testTitleConstructor() {
            OutlineDocument doc = new OutlineDocument("测试文档");
            
            assertEquals("测试文档", doc.getTitle());
            assertTrue(doc.isEmpty());
        }
    }

    @Nested
    @DisplayName("根节点管理测试")
    class RootNodeManagementTests {

        @Test
        @DisplayName("添加根节点应该设置级别为0")
        void testAddRootNode() {
            document.addRootNode(rootNode1);
            
            assertEquals(1, document.getRootNodesCount());
            assertFalse(document.isEmpty());
            assertEquals(0, rootNode1.getLevel());
            assertEquals(rootNode1, document.getRootNodes().get(0));
        }

        @Test
        @DisplayName("添加null根节点应该被忽略")
        void testAddNullRootNode() {
            document.addRootNode((OutlineNode) null);
            
            assertEquals(0, document.getRootNodesCount());
            assertTrue(document.isEmpty());
        }

        @Test
        @DisplayName("添加字符串根节点应该创建新节点")
        void testAddRootNodeByString() {
            document.addRootNode("字符串根节点");
            
            assertEquals(1, document.getRootNodesCount());
            assertEquals("字符串根节点", document.getRootNodes().get(0).getTitle());
        }

        @Test
        @DisplayName("添加带内容的字符串根节点应该创建新节点")
        void testAddRootNodeByStringWithContent() {
            document.addRootNode("标题", "内容");
            
            assertEquals(1, document.getRootNodesCount());
            OutlineNode root = document.getRootNodes().get(0);
            assertEquals("标题", root.getTitle());
            assertEquals("内容", root.getContent());
        }

        @Test
        @DisplayName("移除根节点")
        void testRemoveRootNode() {
            document.addRootNode(rootNode1);
            boolean removed = document.removeRootNode(rootNode1);
            
            assertTrue(removed);
            assertEquals(0, document.getRootNodesCount());
            assertTrue(document.isEmpty());
        }

        @Test
        @DisplayName("移除不存在的根节点应该返回false")
        void testRemoveNonExistentRootNode() {
            boolean removed = document.removeRootNode(rootNode1);
            
            assertFalse(removed);
        }

        @Test
        @DisplayName("获取根节点列表应该返回副本")
        void testGetRootNodesReturnsCopy() {
            document.addRootNode(rootNode1);
            List<OutlineNode> roots1 = document.getRootNodes();
            List<OutlineNode> roots2 = document.getRootNodes();
            
            assertNotSame(roots1, roots2);
            assertEquals(roots1, roots2);
        }

        @Test
        @DisplayName("添加根节点应该更新修改时间")
        void testAddRootNodeUpdatesModifyTime() {
            long originalModifyTime = document.getModifyTime();
            
            try {
                Thread.sleep(1); // 确保时间差异
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            document.addRootNode(rootNode1);
            
            assertTrue(document.getModifyTime() > originalModifyTime);
        }
    }

    @Nested
    @DisplayName("文档统计测试")
    class DocumentStatisticsTests {

        @Test
        @DisplayName("计算总节点数")
        void testGetTotalNodesCount() {
            // 创建复杂的树结构
            rootNode1.addChild(childNode);
            OutlineNode grandChild = new OutlineNode("孙节点");
            childNode.addChild(grandChild);
            
            document.addRootNode(rootNode1);
            document.addRootNode(rootNode2);
            
            // 应该有4个节点: rootNode1, childNode, grandChild, rootNode2
            assertEquals(4, document.getTotalNodesCount());
        }

        @Test
        @DisplayName("空文档的节点数应该为0")
        void testEmptyDocumentTotalNodesCount() {
            assertEquals(0, document.getTotalNodesCount());
        }

        @Test
        @DisplayName("计算最大深度")
        void testGetMaxDepth() {
            // 创建不同深度的树结构
            OutlineNode child1 = new OutlineNode("子节点1");
            OutlineNode child2 = new OutlineNode("子节点2");
            OutlineNode grandChild = new OutlineNode("孙节点");
            OutlineNode greatGrandChild = new OutlineNode("曾孙节点");
            
            rootNode1.addChild(child1);
            rootNode2.addChild(child2);
            child2.addChild(grandChild);
            grandChild.addChild(greatGrandChild);
            
            document.addRootNode(rootNode1);
            document.addRootNode(rootNode2);
            
            // 最大深度应该为4（rootNode2 -> child2 -> grandChild -> greatGrandChild）
            assertEquals(4, document.getMaxDepth());
        }

        @Test
        @DisplayName("空文档的最大深度应该为0")
        void testEmptyDocumentMaxDepth() {
            assertEquals(0, document.getMaxDepth());
        }

        @Test
        @DisplayName("只有根节点的文档最大深度为1")
        void testSingleRootNodeMaxDepth() {
            document.addRootNode(rootNode1);
            assertEquals(1, document.getMaxDepth());
        }
    }

    @Nested
    @DisplayName("文档遍历测试")
    class DocumentTraversalTests {

        @Test
        @DisplayName("遍历所有节点")
        void testTraverse() {
            // 构建树结构
            rootNode1.addChild(childNode);
            document.addRootNode(rootNode1);
            document.addRootNode(rootNode2);
            
            AtomicInteger visitCount = new AtomicInteger(0);
            StringBuilder visitOrder = new StringBuilder();
            
            document.traverse(node -> {
                visitCount.incrementAndGet();
                visitOrder.append(node.getTitle()).append(",");
            });
            
            assertEquals(3, visitCount.get());
            assertEquals("根节点1,子节点,根节点2,", visitOrder.toString());
        }

        @Test
        @DisplayName("遍历空文档")
        void testTraverseEmptyDocument() {
            AtomicInteger visitCount = new AtomicInteger(0);
            
            document.traverse(node -> visitCount.incrementAndGet());
            
            assertEquals(0, visitCount.get());
        }
    }

    @Nested
    @DisplayName("元数据管理测试")
    class MetadataTests {

        @Test
        @DisplayName("设置和获取元数据")
        void testSetAndGetMetadata() {
            document.setMetadata("author", "测试作者");
            document.setMetadata("version", 1.0);
            
            assertEquals("测试作者", document.getMetadata("author"));
            assertEquals(1.0, document.getMetadata("version"));
        }

        @Test
        @DisplayName("获取不存在的元数据应该返回null")
        void testGetNonExistentMetadata() {
            assertNull(document.getMetadata("nonexistent"));
        }

        @Test
        @DisplayName("获取元数据带默认值")
        void testGetMetadataWithDefault() {
            assertEquals("默认值", document.getMetadata("nonexistent", "默认值"));
            
            document.setMetadata("existing", "存在的值");
            assertEquals("存在的值", document.getMetadata("existing", "默认值"));
        }

        @Test
        @DisplayName("获取所有元数据应该返回副本")
        void testGetMetadataReturnsCopy() {
            document.setMetadata("key", "value");
            var metadata1 = document.getMetadata();
            var metadata2 = document.getMetadata();
            
            assertNotSame(metadata1, metadata2);
            assertEquals(metadata1, metadata2);
        }

        @Test
        @DisplayName("设置元数据应该更新修改时间")
        void testSetMetadataUpdatesModifyTime() {
            long originalModifyTime = document.getModifyTime();
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            document.setMetadata("key", "value");
            
            assertTrue(document.getModifyTime() > originalModifyTime);
        }
    }

    @Nested
    @DisplayName("文档清理测试")
    class DocumentClearTests {

        @Test
        @DisplayName("清空文档应该移除所有内容")
        void testClear() {
            document.addRootNode(rootNode1);
            document.setMetadata("key", "value");
            
            document.clear();
            
            assertTrue(document.isEmpty());
            assertEquals(0, document.getRootNodesCount());
            assertTrue(document.getMetadata().isEmpty());
        }

        @Test
        @DisplayName("清空文档应该更新修改时间")
        void testClearUpdatesModifyTime() {
            document.addRootNode(rootNode1);
            long originalModifyTime = document.getModifyTime();
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            document.clear();
            
            assertTrue(document.getModifyTime() > originalModifyTime);
        }
    }

    @Nested
    @DisplayName("Getters和Setters测试")
    class GettersSettersTests {

        @Test
        @DisplayName("标题设置和获取")
        void testTitleGetterSetter() {
            document.setTitle("新标题");
            assertEquals("新标题", document.getTitle());
        }

        @Test
        @DisplayName("设置标题应该更新修改时间")
        void testSetTitleUpdatesModifyTime() {
            long originalModifyTime = document.getModifyTime();
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            document.setTitle("新标题");
            
            assertTrue(document.getModifyTime() > originalModifyTime);
        }

        @Test
        @DisplayName("描述设置和获取")
        void testDescriptionGetterSetter() {
            document.setDescription("新描述");
            assertEquals("新描述", document.getDescription());
        }

        @Test
        @DisplayName("设置描述应该更新修改时间")
        void testSetDescriptionUpdatesModifyTime() {
            long originalModifyTime = document.getModifyTime();
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            document.setDescription("新描述");
            
            assertTrue(document.getModifyTime() > originalModifyTime);
        }

        @Test
        @DisplayName("源格式设置和获取")
        void testSourceFormatGetterSetter() {
            document.setSourceFormat("markdown");
            assertEquals("markdown", document.getSourceFormat());
        }

        @Test
        @DisplayName("创建时间设置和获取")
        void testCreateTimeGetterSetter() {
            long time = System.currentTimeMillis();
            document.setCreateTime(time);
            assertEquals(time, document.getCreateTime());
        }

        @Test
        @DisplayName("修改时间设置和获取")
        void testModifyTimeGetterSetter() {
            long time = System.currentTimeMillis();
            document.setModifyTime(time);
            assertEquals(time, document.getModifyTime());
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString应该包含关键信息")
        void testToString() {
            document.setTitle("测试文档");
            document.addRootNode(rootNode1);
            rootNode1.addChild(childNode);
            document.addRootNode(rootNode2);
            
            String str = document.toString();
            
            assertTrue(str.contains("测试文档"));
            assertTrue(str.contains("rootNodes=2"));
            assertTrue(str.contains("totalNodes=3"));
            assertTrue(str.contains("maxDepth=2"));
        }

        @Test
        @DisplayName("空文档的toString")
        void testEmptyDocumentToString() {
            String str = document.toString();
            
            assertTrue(str.contains("rootNodes=0"));
            assertTrue(str.contains("totalNodes=0"));
            assertTrue(str.contains("maxDepth=0"));
        }
    }
}