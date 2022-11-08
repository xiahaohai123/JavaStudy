package json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UnknownDes {
    public static void main(String[] args) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "111");
        map.put("password", "222");
        map.put("port", 222);
        map.put("host", "10.10.10.10");
        map.put("unknown", "10.10.10.10");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(map);
        System.out.println(json);
        Data2 data2 = objectMapper.readValue(json, Data2.class);
        System.out.println(data2);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Data data = objectMapper.readValue(json, Data.class);
        System.out.println(data);
    }

    static class Data {
        private String username;
        private String password;
        private Integer port;
        private String host;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", port=" + port +
                    ", host='" + host + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Data2 {
        private String username;
        private String password;
        private Integer port;
        private String host;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", port=" + port +
                    ", host='" + host + '\'' +
                    '}';
        }
    }
}
