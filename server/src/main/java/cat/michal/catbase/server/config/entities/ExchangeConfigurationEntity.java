package cat.michal.catbase.server.config.entities;

public class ExchangeConfigurationEntity {
    /**
     * Names of already defined queues
     */
    public String[] queues = new String[0];
    public String name = "";
    /**
     * Type of the exchange
     */
    public String type = "direct";
}
