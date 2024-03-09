package cat.michal.catbase.server.exchange;

import java.util.ArrayList;
import java.util.List;

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
}
