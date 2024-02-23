package cat.michal.catbase.server.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventDispatcher {

    private final List<Consumer<Event>> hooks;

    public EventDispatcher() {
        this.hooks = new ArrayList<>();
    }

    @SuppressWarnings("UNSAFE")
    public <T extends Event> void hook(Consumer<T> callback) {
        this.hooks.add((Consumer<Event>) callback);
    }

    @SuppressWarnings("unsafe")
    public <T extends Event> void dispatch(Event event, Class<T> clazz) {
        hooks.stream()
                .filter(hook -> hook.getClass().getGenericInterfaces()[0].getClass().equals(event.getClass()))
                .forEach(hook -> {
                    hook.accept(clazz.cast(event));
                });
    }
}
