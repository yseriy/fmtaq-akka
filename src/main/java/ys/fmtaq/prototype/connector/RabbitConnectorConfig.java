package ys.fmtaq.prototype.connector;

import java.util.Map;

public class RabbitConnectorConfig {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;
    private final QueueConfig mainQueue;
    private final QueueConfig hostQueue;

    public static class QueueConfig {

        private final String queueName;
        private final Boolean durable;
        private final Boolean exclusive;
        private final Boolean autoDelete;
        private final Map<String, Object> arguments;

        public QueueConfig(final String queueName, final Boolean durable, final Boolean exclusive,
                           final Boolean autoDelete, final Map<String, Object> arguments) {
            this.queueName = queueName;
            this.durable = durable;
            this.exclusive = exclusive;
            this.autoDelete = autoDelete;
            this.arguments = arguments;
        }

        public String getQueueName() {
            return queueName;
        }

        public Boolean getDurable() {
            return durable;
        }

        public Boolean getExclusive() {
            return exclusive;
        }

        public Boolean getAutoDelete() {
            return autoDelete;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }
    }

    public RabbitConnectorConfig(final String host, final Integer port, final String username, final String password,
                                 final QueueConfig mainQueue, final QueueConfig hostQueue) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.mainQueue = mainQueue;
        this.hostQueue = hostQueue;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public QueueConfig getMainQueue() {
        return mainQueue;
    }

    public QueueConfig getHostQueue() {
        return hostQueue;
    }
}
