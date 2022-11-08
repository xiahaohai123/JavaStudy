package connections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SingletonMapTest {
    public static void main(String[] args) {
        Map<String, String> map1 = Collections.singletonMap("result", "FAILED");
        Map<String, String> map2 = new HashMap<>();
        map2.put("result", "FAILED");
        System.out.println(map1.equals(map2));
    }
}
