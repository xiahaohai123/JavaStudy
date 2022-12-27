package collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MapSplitter {

    public Collection<Map<String, Object>> splitMapBySize(Map<String, Object> srcMap, int limitation) {
        if (CollectionUtils.isEmpty(srcMap) || limitation <= 0) {
            return Collections.emptyList();
        }

        Stack<Map<String, Object>> collector = new Stack<>();
        HashMap<String, Object> collectorMap = new HashMap<>();
        collector.push(collectorMap);
        doSplit(srcMap, collector, new IntegerWrapper(0), limitation, new Stack<>(), 0);
        return collector;
    }

    static class IntegerWrapper {
        Integer value;

        public IntegerWrapper(int value) {
            this.value = value;
        }
    }

    @SuppressWarnings("unchecked")
    private void doSplit(Map<String, Object> srcMap,
                         Stack<Map<String, Object>> result, IntegerWrapper accumulation,
                         int limitation, Stack<String> keyIter,
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
                String valueStr = value.toString();
                String trimmedValueStr = trimString(valueStr, limitation);
                int newValueSize = trimmedValueStr.getBytes().length;
                if (newValueSize + accumulation.value > limitation) {
                    result.push(new HashMap<>());
                    deepPutValue(result.peek(), keyIter, entryKey, trimmedValueStr);
                    accumulation.value = newValueSize;
                } else {
                    deepPutValue(result.peek(), keyIter, entryKey, trimmedValueStr);
                    accumulation.value += newValueSize;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void deepPutValue(Map<String, Object> collectorMap, Collection<String> keyIter, String finalKey, Object value) {
        Map<String, Object> currentMap = collectorMap;
        for (String key : keyIter) {
            currentMap = (Map<String, Object>) currentMap.computeIfAbsent(key, k -> new HashMap<>());
        }
        currentMap.put(finalKey, value);
    }

    private String trimString(String src, int limitation) {
        if (StringUtils.isBlank(src) || src.getBytes().length <= limitation) {
            return src;
        }
        int stepLength = limitation / 30;
        int realLimitation = limitation - 3;
        while (src.getBytes().length > realLimitation) {
            src = src.substring(0, src.length() - stepLength);
        }
        return src + "...";
    }

    public Collection<Map<String, Object>> splitMapBySizeSyn(Map<String, Object> srcMap1, Map<String, Object> srcMap2, int limitation) {
        if (limitation <= 0) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(srcMap1)) {
            return splitMapBySize(srcMap2, limitation);
        } else if (CollectionUtils.isEmpty(srcMap2)) {
            return splitMapBySize(srcMap1, limitation);
        }

        Stack<Map<String, Object>> collector = new Stack<>();
        HashMap<String, Object> collectorMap = new HashMap<>();
        collector.push(collectorMap);
        doSynSplit(srcMap1, srcMap2, collector, new IntegerWrapper(0), limitation, new Stack<>(), 0);
        return collector;

    }

    private void doSynSplit(Map<String, Object> srcMap1, Map<String, Object> srcMap2,
                            Stack<Map<String, Object>> result, IntegerWrapper accumulation,
                            int limitation, Stack<String> keyIter,
                            int depth) {

        for (Map.Entry<String, Object> entry : srcMap1.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            // TODO: 2022/12/27 先处理 map1 再单独处理 map2
            if (entryValue == null) {
                deepPutValue(result.peek(), keyIter, entryKey, null);
            }
//            if (entryValue instanceof Map && depth < 10) {
//                keyIter.push(entryKey);
//                Map<String, Object> toSplitMap = (Map<String, Object>) value;
//                doSplit(toSplitMap, result, accumulation, limitation, keyIter, depth + 1);
//                keyIter.pop();
//            } else {
//                String valueStr = value.toString();
//                String trimmedValueStr = trimString(valueStr, limitation);
//                int newValueSize = trimmedValueStr.getBytes().length;
//                if (newValueSize + accumulation.value > limitation) {
//                    result.push(new HashMap<>());
//                    deepPutValue(result.peek(), keyIter, entryKey, trimmedValueStr);
//                    accumulation.value = newValueSize;
//                } else {
//                    deepPutValue(result.peek(), keyIter, entryKey, trimmedValueStr);
//                    accumulation.value += newValueSize;
//                }
//            }
        }

    }
}
