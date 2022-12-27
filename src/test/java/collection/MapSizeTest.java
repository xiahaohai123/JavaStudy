package collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MapSizeTest {

    private final Log log = LogFactory.getLog(this.getClass());

    @Test
    void testAddSize() {
        Map<String, Object> map = new HashMap<>();
        int originSize = map.toString().getBytes().length;
        log.info(String.format("origin map size: %d Byte", originSize));

        String toPutKey = "aaa";
        String toPutValue = "一二三四五六七八九十";
        int toPutSize = toPutValue.getBytes().length + toPutKey.getBytes().length;
        log.info(String.format("to put size: %d Byte", toPutSize));

        map.put(toPutKey, toPutValue);
        int currentSize = map.toString().getBytes().length;
        log.info(String.format("current map size: %d Byte", currentSize));
    }
}
