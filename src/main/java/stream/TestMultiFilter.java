package stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestMultiFilter {

    public static void main(String[] args) {
        String[] strings = {"abb", "abcd", "fegc", "efe", "adfes"};

        List<String> collect = Arrays.stream(strings).filter(s -> {
            System.out.println(s);
            return s.startsWith("a");
        }).filter(s -> {
            System.out.println(s);
            return s.endsWith("s");
        }).collect(Collectors.toList());

        System.out.println(collect);
    }
}
