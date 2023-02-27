package unclassified;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassCodeFrom {
    private static final Log log = LogFactory.getLog(ClassCodeFrom.class);

    public static void main(String[] args) {
        log.info("start to test");
        log.info(Math.class.getProtectionDomain().getCodeSource().getLocation());
    }
}
