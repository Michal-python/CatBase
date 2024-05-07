package cat.michal.catbase.client;

import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.AuthCredentials;
import cat.michal.catbase.common.converter.AbstractMessageConverter;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class CatBaseClientConfig {
    private InetAddress address;
    private int port;
    private int maxRetriesCount;
    private TimeUnit retryInterval;
    private long retryIntervalCount;
    private AbstractMessageConverter abstractMessageConverter;
    private AuthCredentials credentials;
    private List<MessageHandler> handlers;

    CatBaseClientConfig() {
    }


    CatBaseClientConfig(InetAddress address, int port, int maxRetriesCount, AbstractMessageConverter abstractMessageConverter, AuthCredentials credentials, List<MessageHandler> handlers, TimeUnit retryInterval, long retryIntervalCount) {
        this.address = address;
        this.retryIntervalCount = retryIntervalCount;
        this.retryInterval = retryInterval;
        this.port = port;
        this.maxRetriesCount = maxRetriesCount;
        this.abstractMessageConverter = abstractMessageConverter;
        this.credentials = credentials;
        this.handlers = handlers;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRetryInterval(TimeUnit retryInterval) {
        this.retryInterval = retryInterval;
    }

    public void setRetryIntervalCount(long retryIntervalCount) {
        this.retryIntervalCount = retryIntervalCount;
    }

    public void setAbstractMessageConverter(AbstractMessageConverter abstractMessageConverter) {
        this.abstractMessageConverter = abstractMessageConverter;
    }

    public void setCredentials(AuthCredentials credentials) {
        this.credentials = credentials;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getRetryIntervalCount() {
        return retryIntervalCount;
    }

    public TimeUnit getRetryInterval() {
        return retryInterval;
    }

    public int getPort() {
        return port;
    }

    public int getMaxRetriesCount() {
        return maxRetriesCount;
    }

    public void setMaxRetriesCount(int maxRetriesCount) {
        this.maxRetriesCount = maxRetriesCount;
    }

    public AbstractMessageConverter getAbstractMessageConverter() {
        return abstractMessageConverter;
    }

    public AuthCredentials getCredentials() {
        return credentials;
    }


    public List<MessageHandler> getHandlers() {
        return handlers;
    }

    void setHandlers(List<MessageHandler> handlers) {
        this.handlers = handlers;
    }
}
