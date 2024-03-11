package cat.michal.catbase.server.exchange;

import cat.michal.catbase.server.packets.PacketQueue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExchangeRegistry {
    private static final List<Exchange> exchanges = new ArrayList<>();

    public static void register(Exchange exchange) {
        if(exchanges.stream().anyMatch(element -> element.getName().equals(exchange.getName()))) {
            throw new IllegalArgumentException("Exchanges' names must be unique (" + exchange.getName() + ")");
        }
        exchanges.add(exchange);
    }

    public static List<Exchange> getExchanges() {
        return exchanges;
    }

    public static void unregister(String exchange) {
        exchanges.removeIf(element -> element.getName().equals(exchange));
    }

    public static @NotNull Optional<PacketQueue> findQueue(String queueName) {
        return exchanges.stream()
                .flatMap(exchange -> exchange.queues().stream())
                .filter(queue -> queue.getName().equals(queueName))
                .findFirst();
    }
}
