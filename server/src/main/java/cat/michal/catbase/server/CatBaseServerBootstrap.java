package cat.michal.catbase.server;

import cat.michal.catbase.common.CatBaseConstants;
import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.server.config.ConfigurationDeserializer;
import cat.michal.catbase.server.defaultImpl.DefaultQueue;
import cat.michal.catbase.server.defaultImpl.DirectExchange;
import cat.michal.catbase.server.exchange.ExchangeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CatBaseServerBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(CatBaseServer.class);
    public static void main(String[] args) {
        ConfigurationDeserializer serializer = new ConfigurationDeserializer("server.yaml");
        logger.info("Loading CatBase " + CatBaseConstants.VERSION);
        if(serializer.configExists()) {
            serializer.load();
        } else {
            logger.info("Generating default configuration");
            serializer.generateDefault();
        }

        CatBaseServer server = new CatBaseServer(ConfigurationDeserializer.instance.port);
        var queues = Arrays.stream(ConfigurationDeserializer.instance.queues)
                .map(desc -> new DefaultQueue(desc.size, desc.name))
                .toList();

        Arrays.stream(ConfigurationDeserializer.instance.exchanges).map(
                exchange -> {
                    if (exchange.type.equalsIgnoreCase("direct")) {
                        return new DirectExchange(exchange.name, queues.stream()
                                .filter(queue -> Arrays.stream(exchange.queues).anyMatch(queueElement -> queueElement.equals(queue.getName())))
                                .collect(Collectors.toList())
                        );
                    } else {
                        throw new CatBaseException("Invalid exchange type in configuration");
                    }
                }
        ).forEach(ExchangeRegistry::register);


        new Thread(server::startServer,"Server-Thread").start();
        logger.info("CatBase is listening on port " + server.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));
    }
}
