package instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionTest {
    private static final Log log = LogFactory.getLog(CollectionTest.class);

    public static void main(String[] args) {
        List<Integer> a = new ArrayList<>();
        log.info(a instanceof Collection);
        System.out.println(new ArrayList<Integer>() instanceof Collection);
    }
}
