package cat.michal.catbase.server.config;

import cat.michal.catbase.server.config.entities.ExchangeConfigurationEntity;
import cat.michal.catbase.server.config.entities.QueueConfigurationEntity;
import cat.michal.catbase.server.config.entities.UserConfigurationEntity;

public class ConfigurationInstance {
    public int port = 52137;
    public QueueConfigurationEntity[] queues = new QueueConfigurationEntity[]{};
    public ExchangeConfigurationEntity[] exchanges = new ExchangeConfigurationEntity[]{};
    public UserConfigurationEntity[] users = new UserConfigurationEntity[]{};
}
