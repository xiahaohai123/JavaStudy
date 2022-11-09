import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.junit.jupiter.api.Test;

public class Log4jTest {

    private final Log log = LogFactory.getLog(this.getClass());

    @Test
    void testLog() {
        log.fatal("fatal");
        log.error("error");
        log.warn("warn");
        log.info("info");
        log.debug("debug");
        log.trace("trace");
        log.info(this.getClass().getResource("/"));
        System.out.println(System.getProperty("org.apache.commons.logging.Log"));
    }
}
