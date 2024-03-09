package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.exchange.ExchangeRegistry;

public class ExchangeDetermineProcedure implements Procedure<Exchange, Message> {
    @Override
    public Exchange proceed(Message arg) {
        return ExchangeRegistry.getExchanges().stream()
                .filter(element -> arg.getExchangeName().equals(element.getName()))
                .findFirst()
                .orElse(null);
    }
}
