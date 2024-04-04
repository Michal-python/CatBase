package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.exchange.ExchangeManager;

@Component
public class ExchangeDetermineProcedure implements Procedure<Exchange, Message> {
    private final ExchangeManager exchangeManager;


    public ExchangeDetermineProcedure(ExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @Override
    public Exchange proceed(Message arg) {
        return exchangeManager.getExchanges().stream()
                .filter(element -> element.getName().equals(arg.getExchangeName()))
                .findFirst()
                .orElse(null);
    }
}
