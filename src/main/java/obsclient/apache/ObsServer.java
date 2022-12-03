package obsclient.apache;

public class ObsServer {

    /** AK */
    private final String accessKey;
    /** SK */
    private final String securityKey;
    /** 终端节点 */
    private final String endpoint;
    /** 桶地址 */
    private final String bucket;
    /** 访问协议 */
    private final String protocol;
    /** 端口 */
    private final int port;

    /**
     * 构造器
     * @param builder 建造器，该建造器已确认参数没有问题。
     */
    private ObsServer(Builder builder) {
        this.accessKey = builder.accessKey;
        this.securityKey = builder.securityKey;
        this.endpoint = builder.endpoint;
        this.bucket = builder.bucket;
        this.protocol = builder.protocol;
        this.port = builder.port;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public static class Builder {
        /** AK */
        private String accessKey;
        /** SK */
        private String securityKey;
        /** 终端节点 */
        private String endpoint;
        /** 桶地址 */
        private String bucket;
        /** 访问协议 */
        private String protocol = "https";
        /** 端口 */
        private int port = 443;

        /**
         * 设置AK
         * @param accessKey AK
         * @return 建造器
         */
        public Builder setAccessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        /**
         * 设置SK
         * @param securityKey SK
         * @return 建造器
         */
        public Builder setSecurityKey(String securityKey) {
            this.securityKey = securityKey;
            return this;
        }

        /**
         * 设置终端节点
         * @param endpoint 终端节点
         * @return 建造器
         */
        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * 设置桶地址
         * @param bucket 桶地址
         * @return 建造器
         */
        public Builder setBucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * 设置连接协议
         * @param protocol 连接协议
         * @return 建造器
         */
        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * 设置端口
         * @param port 端口
         * @return 建造器
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * 构建对象存储服务器数据对象
         * @return ObsServer
         */
        public ObsServer build() {
            requireValidAK(accessKey);
            requireValidSK(securityKey);
            requireValidEndpoint(endpoint);
            requireValidBucketName(bucket);
            requireValidProtocol(protocol);
            requireValidPort(port);
            return new ObsServer(this);
        }

        /**
         * 确保合法的AK
         * @param ak AK
         */
        private void requireValidAK(String ak) {
            if (isBlank(ak)) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 确保合法的SK
         * @param sk SK
         */
        private void requireValidSK(String sk) {
            if (isBlank(sk)) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 确保合法的终端节点
         * @param endpoint 终端节点
         */
        private void requireValidEndpoint(String endpoint) {
            if (isBlank(endpoint)) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 确保合法的桶地址
         * @param bucket 桶地址
         */
        private void requireValidBucketName(String bucket) {
            if (isBlank(bucket)) {
                throw new IllegalArgumentException();
            }
            int length = bucket.length();
            if (length < 3 || length > 63) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < length; i++) {
                char ch = bucket.charAt(i);
                if (!(ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '.')) {
                    throw new IllegalArgumentException();
                }
            }
        }

        /**
         * 确保合法的连接协议
         * @param protocol 连接协议
         */
        private void requireValidProtocol(String protocol) {
            if (!"https".equals(protocol) && !"http".equals(protocol)) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 确保合法的端口
         * @param port 端口
         */
        private void requireValidPort(int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 是否空白字符串
         * @param cs 受检字符串
         * @return 如果空白或者为null则返回true，否则返回false
         */
        private boolean isBlank(final CharSequence cs) {
            int strLen;
            if (cs == null || (strLen = cs.length()) == 0) {
                return true;
            }
            for (int i = 0; i < strLen; i++) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
