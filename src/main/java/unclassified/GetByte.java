package unclassified;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;

public class GetByte {
    private static final Log log = LogFactory.getLog(GetByte.class);

    public static void main(String[] args) {
        log.info(".".getBytes(StandardCharsets.UTF_8).length);
        log.info(".".getBytes(StandardCharsets.ISO_8859_1).length);
        log.info(".".getBytes(StandardCharsets.UTF_16).length);
    }
}
