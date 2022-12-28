package collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapSplitter {

    public static Collection<Map<String, Object>> splitMapBySize(Map<String, Object> srcMap, int limitation) {
        if (CollectionUtils.isEmpty(srcMap) || limitation <= 0) {
            return Collections.emptyList();
        }

        Deque<Map<String, Object>> collector = new ArrayDeque<>();
        HashMap<String, Object> collectorMap = new HashMap<>();
        collector.push(collectorMap);
        doSplit(srcMap, collector, new IntegerWrapper(0), limitation, new ArrayDeque<>(), 0);
        return collector;
    }

    static class IntegerWrapper {
        Integer value;

        public IntegerWrapper(int value) {
            this.value = value;
        }
    }

    static class SplitProgress {
        Deque<Map<String, Object>> result = new ArrayDeque<>();
        IntegerWrapper accumulation = new IntegerWrapper(0);
        Integer limitation;

        public SplitProgress(int limitation) {
            result.push(new HashMap<>());
            this.limitation = limitation;
        }
    }

    @SuppressWarnings("unchecked")
    private static void doSplit(Map<String, Object> srcMap,
                                Deque<Map<String, Object>> result, IntegerWrapper accumulation,
                                int limitation, Deque<String> keyIter,
                                int depth) {
        for (Map.Entry<String, Object> entry : srcMap.entrySet()) {
            Object value = entry.getValue();
            String entryKey = entry.getKey();
            if (value == null) {
                continue;
            }
            if (value instanceof Map && depth < 10) {
                keyIter.push(entryKey);
                Map<String, Object> toSplitMap = (Map<String, Object>) value;
                doSplit(toSplitMap, result, accumulation, limitation, keyIter, depth + 1);
                keyIter.pop();
            } else {
                castString2PutValue(result, accumulation, limitation, keyIter, entryKey, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void deepPutValue(Map<String, Object> collectorMap, Deque<String> keyIter, String finalKey, Object value) {
        Map<String, Object> currentMap = collectorMap;
        Iterator<String> iter = keyIter.descendingIterator();
        while (iter.hasNext()) {
            currentMap = (Map<String, Object>) currentMap.computeIfAbsent(iter.next(), k -> new HashMap<>());
        }
        currentMap.put(finalKey, value);
    }

    private static String trimString(String src, int limitation) {
        if (StringUtils.isBlank(src) || src.getBytes().length <= limitation) {
            return src;
        }
        int stepLength = limitation / 30;
        if (stepLength == 0) {
            stepLength = 1;
        }
        int realLimitation = limitation - 3;
        while (src.getBytes().length > realLimitation) {
            src = src.substring(0, src.length() - stepLength);
        }
        return src + "...";
    }

    public static Collection<Map<String, Object>> splitMapBySizeSyn(String key1, Map<String, Object> srcMap1,
                                                                    String key2, Map<String, Object> srcMap2,
                                                                    int limitation) {
        if (limitation <= 0) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(srcMap1)) {
            return splitMapBySize(srcMap2, limitation);
        } else if (CollectionUtils.isEmpty(srcMap2)) {
            return splitMapBySize(srcMap1, limitation);
        }

        SplitProgress progress1 = new SplitProgress(limitation / 2);
        SplitProgress progress2 = new SplitProgress(limitation / 2);
        doSynSplit(srcMap1, srcMap2, progress1, progress2, new ArrayDeque<>(), 0);

        Deque<Map<String, Object>> result1 = progress1.result;
        Deque<Map<String, Object>> result2 = progress2.result;
        List<Map<String, Object>> result = new ArrayList<>();
        while (!result1.isEmpty()) {
            Map<String, Object> map1 = result1.pop();
            Map<String, Object> map2 = result2.pop();
            Map<String, Object> layer = new HashMap<>();
            layer.put(key1, map1);
            layer.put(key2, map2);
            result.add(layer);
        }
        while (!result2.isEmpty()) {
            Map<String, Object> map2 = result2.pop();
            Map<String, Object> layer = new HashMap<>();
            layer.put(key2, map2);
            result.add(layer);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void doSynSplit(Map<String, Object> srcMap1, Map<String, Object> srcMap2,
                                   SplitProgress progress1, SplitProgress progress2, Deque<String> keyIter,
                                   int depth) {
        for (Map.Entry<String, Object> entry : srcMap1.entrySet()) {
            String entryKey = entry.getKey();
            Object value1 = entry.getValue();
            Object value2 = srcMap2.get(entryKey);
            if (value1 instanceof Map && value2 instanceof Map && depth < 10) {
                keyIter.push(entryKey);
                Map<String, Object> toSplitMap1 = (Map<String, Object>) value1;
                Map<String, Object> toSplitMap2 = (Map<String, Object>) value2;
                doSynSplit(toSplitMap1, toSplitMap2, progress1, progress2, keyIter, depth + 1);
                keyIter.pop();
            } else {
                String valueStr1 = value1 == null ? null : value1.toString();
                String trimmedValueStr1 = trimString(valueStr1, progress1.limitation);
                int newValueSize1 = trimmedValueStr1 == null ? 0 : trimmedValueStr1.getBytes().length;
                String valueStr2 = value2 == null ? null : value2.toString();
                String trimmedValueStr2 = trimString(valueStr2, progress1.limitation);
                int newValueSize2 = trimmedValueStr2 == null ? 0 : trimmedValueStr2.getBytes().length;
                if (newValueSize1 + progress1.accumulation.value > progress1.limitation
                        || newValueSize2 + progress2.accumulation.value > progress2.limitation) {
                    progress1.result.push(new HashMap<>());
                    progress2.result.push(new HashMap<>());
                    progress1.accumulation.value = 0;
                    progress2.accumulation.value = 0;
                }
                deepPutValue(progress1.result.peek(), keyIter, entryKey, trimmedValueStr1);
                progress1.accumulation.value += newValueSize1;
                deepPutValue(progress2.result.peek(), keyIter, entryKey, trimmedValueStr2);
                progress2.accumulation.value += newValueSize2;
            }
        }

        srcMap2.keySet().removeAll(srcMap1.keySet());
        srcMap1.clear();
        if (!srcMap2.isEmpty()) {
            doSynSplit(srcMap2, srcMap1, progress2, progress1, keyIter, depth + 1);
        }
    }

    private static void castString2PutValue(Deque<Map<String, Object>> result, IntegerWrapper accumulation, int limitation,
                                            Deque<String> keyIter, String key, Object value) {
        String valueStr = value.toString();
        String trimmedValueStr = trimString(valueStr, limitation);
        int newValueSize = trimmedValueStr.getBytes().length;
        if (newValueSize + accumulation.value > limitation) {
            result.push(new HashMap<>());
            deepPutValue(result.peek(), keyIter, key, trimmedValueStr);
            accumulation.value = newValueSize;
        } else {
            deepPutValue(result.peek(), keyIter, key, trimmedValueStr);
            accumulation.value += newValueSize;
        }
    }
}
