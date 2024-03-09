package cat.michal.catbase.server.config;

public class ConfigurationInstance {
    public int port = 52137;
    public QueueConfigurationEntity[] queues = new QueueConfigurationEntity[]{};
    public ExchangeConfigurationEntity[] exchanges = new ExchangeConfigurationEntity[]{};
}
