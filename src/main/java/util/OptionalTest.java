package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OptionalTest {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "a");


        method01(map);

        method2(map);
    }

    private static void method2(Map<String, Object> map) {
        Optional.ofNullable(map.get("a")).ifPresent(System.out::println);
    }

    private static void method01(Map<String, Object> map) {
        Object a = map.get("a");
        if (a != null) {
            System.out.println(a);
        }
    }
}
