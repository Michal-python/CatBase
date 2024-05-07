package cat.michal.catbase.client;

import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.AuthCredentials;
import cat.michal.catbase.common.converter.AbstractMessageConverter;
import cat.michal.catbase.common.converter.DefaultMessageConverter;
import cat.michal.catbase.common.exception.CatBaseException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public final class CatBaseClientBuilder {
    private CatBaseClientConfig config;

    private CatBaseClientBuilder() {
        this.config = new CatBaseClientConfig();
    }

    public CatBaseClientBuilder address(InetAddress address) {
        this.config.setAddress(address);
        return this;
    }

    public CatBaseClientBuilder port(int port) {
        this.config.setPort(port);
        return this;
    }

    public CatBaseClientBuilder retryProperties(int maxRetriesCount, TimeUnit retryInterval, int retryIntervalCount) {
        this.config.setMaxRetriesCount(maxRetriesCount);
        this.config.setRetryInterval(retryInterval);
        this.config.setRetryIntervalCount(retryIntervalCount);
        return this;
    }

    public CatBaseClientBuilder messageConverter(AbstractMessageConverter abstractMessageConverter) {
        this.config.setAbstractMessageConverter(abstractMessageConverter);
        return this;
    }

    public CatBaseClientBuilder credentials(AuthCredentials credentials) {
        this.config.setCredentials(credentials);
        return this;
    }

    public CatBaseClientBuilder handlers(List<MessageHandler> handlers) {
        this.config.setHandlers(handlers);
        return this;
    }

    public static CatBaseClientBuilder newBuilder() {
        return new CatBaseClientBuilder()
                .retryProperties(10, TimeUnit.SECONDS, 2)
                .messageConverter(new DefaultMessageConverter())
                .handlers(new ArrayList<>())
                .port(-1);
    }

    public CatBaseClient build() {
        if(config.getCredentials() == null) {
            throw new CatBaseException("No credentials provided");
        }
        if(config.getAddress() == null || config.getPort() == -1) {
            throw new CatBaseException("Either port or address not provided");
        }

        return new CatBaseClient(config);
    }
}
