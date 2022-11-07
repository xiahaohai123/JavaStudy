package unclassified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CollectionsTest {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        String collect = strings.stream().collect(Collectors.joining(","));
        System.out.println("collect " + collect);

        String[] split = ",1,2, ".split(",");
        System.out.println(Arrays.toString(split));
    }
}
