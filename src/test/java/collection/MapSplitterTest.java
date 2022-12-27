package collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
}