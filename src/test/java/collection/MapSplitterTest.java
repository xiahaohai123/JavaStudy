package collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class MapSplitterTest {

    private final Log log = LogFactory.getLog(this.getClass());

    private MapSplitter mapSplitter;

    @BeforeEach
    void setUp() {
        mapSplitter = new MapSplitter();
    }

    @Test
    void splitMapBySize() {
        int limitation = 100;

        Map<String, Object> case01 = new HashMap<>();
        Map<String, Object> case02 = new HashMap<String, Object>() {
            {
                StringBuilder str1 = new StringBuilder();
                StringBuilder str2 = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    str1.append("1234567890");
                    str2.append("一二三四五六七八九十");
                }
                put("dao", str2.toString());
                put("dao1", str1.toString());
            }
        };

        Map<String, Object> case03 = new HashMap<String, Object>() {
            {
                put("Q1", new HashMap<String, Object>() {
                    {
                        put("T1", new HashMap<String, Object>() {
                            {
                                put("E1", "1234567890");
                                put("E2", "12345678901234567890");
                                put("E3", "一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十");
                            }
                        });
                        put("T2", "12345678901234567890");
                        put("T3", "123456789012345678901234567890");
                        put("T4", "一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十");
                    }
                });
                put("Q2", "12345678901234567890");
                put("Q3", "123456789012345678901234567890");
                put("Q4", "一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十");
            }
        };

        Collection<Map<String, Object>> result01 = mapSplitter.splitMapBySize(case01, limitation);
        Assertions.assertEquals(0, result01.size());
        Collection<Map<String, Object>> result02 = mapSplitter.splitMapBySize(case02, limitation);
        log.info(result02);
        Assertions.assertEquals(2, result02.size());
        Collection<Map<String, Object>> result03 = mapSplitter.splitMapBySize(case03, limitation);
        log.info(result03);
    }

    @Test
    void splitMapBySizeSyn() {
        int limitation = 100;
        String key1 = "origin_A";
        String key2 = "A";
        Map<String, Object> case01_1 = new HashMap<>();
        Map<String, Object> case01_2 = new HashMap<>();
        Map<String, Object> case02_1 = new HashMap<String, Object>() {{
            put("dao", "1234567890");
        }};
        Map<String, Object> case02_2 = new HashMap<String, Object>() {{
            put("dao", "一二三四五六七八九十");
        }};

        Collection<Map<String, Object>> result01 = mapSplitter.splitMapBySizeSyn(key1, case01_1, key2, case01_2, limitation);
        Assertions.assertEquals(0, result01.size());
        Collection<Map<String, Object>> result02 = mapSplitter.splitMapBySizeSyn(key1, case02_1, key2, case02_2, limitation);
        log.info(result02);

        Map<String, Object> case03_1 = new HashMap<String, Object>() {{
            put("dao", "1234567890");
        }};
        Map<String, Object> case03_2 = new HashMap<String, Object>() {{
            put("dao", "一二三四五六七八九十");
            put("dao1", "一二三四五六七八九十");
        }};
        Collection<Map<String, Object>> result03 = mapSplitter.splitMapBySizeSyn(key1, case03_1, key2, case03_2, limitation);
        log.info(result03);

        Map<String, Object> case04_1 = new HashMap<String, Object>() {{
            put("Q1", new HashMap<String, Object>() {{
                put("W0", "一二三四五六七八九十");
                put("W1", "WBefore");
                put("W2", new HashMap<String, Object>() {{
                    put("E1", "EBefore");
                }});
            }});
        }};
        Map<String, Object> case04_2 = new HashMap<String, Object>() {{
            put("Q1", new HashMap<String, Object>() {{
                put("W1", "WAfter");
                put("W2", new HashMap<String, Object>() {{
                    put("E1", "EAfter");
                    put("E2", "EAfter");
                }});
                put("W3", "WAfter");
            }});
            put("Q2", "QAfter");
        }};

        Collection<Map<String, Object>> result04 = mapSplitter.splitMapBySizeSyn(key1, case04_1, key2, case04_2, 40);
        log.info(result04);
    }
}