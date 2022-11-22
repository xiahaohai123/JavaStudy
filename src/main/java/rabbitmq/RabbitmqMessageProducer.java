package rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitmqMessageProducer {

    private static final String username = "guest";
    private static final String password = "guest";
    private static final String host = "10.12.30.53";
    private static final int port = 5672;
    private static final String exchangeName = "xxxxx.authTopic";
    private static final String routingKey = "advancedSettings.request";

    public static void main(String[] args) throws Exception {
        RabbitmqMessageProducer producer = new RabbitmqMessageProducer();
        producer.publishByTLS();
    }

    private void basicPublish() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost("/");
        factory.setHost(host);
        factory.setPort(port);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> request = new HashMap<>();
        request.put("msg", "hello");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestString = objectMapper.writeValueAsString(request);
        channel.basicPublish(exchangeName, routingKey, null, requestString.getBytes(StandardCharsets.UTF_8));

        channel.close();
        connection.close();
    }

    private void publishByTLS() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost("/");
        factory.setHost(host);
        factory.setPort(port);
        factory.useSslProtocol(SSLContextUtil.getSSLContext());

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> request = new HashMap<>();
        request.put("msg", "hello");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestString = objectMapper.writeValueAsString(request);
        channel.basicPublish(exchangeName, routingKey, null, requestString.getBytes(StandardCharsets.UTF_8));

        channel.close();
        connection.close();
    }
}
