package cat.michal.catbase.client;

import java.util.UUID;
import java.util.function.Consumer;

public record ReceivedHook<T>(UUID id, UUID correlationId, Consumer<T> consumer) {
}
