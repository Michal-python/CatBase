package cat.michal.catbase.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventDispatcher {

    private static EventDispatcher instance;
    private final List<EventListener<Event>> hooks;

    public EventDispatcher() {
        this.hooks = new ArrayList<>();
        instance = this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public synchronized <T extends Event> void hook(Class<T> clazz, Consumer<T> callback) {
        this.hooks.add(new EventListener(callback, clazz));
    }

    public synchronized <T extends Event> void unhook(Consumer<T> callback) {
        this.hooks.removeIf(l -> l.consumer == callback);
    }

    public synchronized <T extends Event> void dispatch(T event) {
        hooks.stream()
                .filter(hook -> hook.clazz == event.getClass())
                .forEach(hook -> hook.consumer.accept(event));
    }

    private record EventListener<T extends Event>(
            Consumer<T> consumer,
            Class<T> clazz
    ) {}

    public static @NotNull EventDispatcher getInstance() {
        return instance;
    }
}