package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;

import java.util.UUID;
import java.util.function.Consumer;

public record ReceivedMessageHook(UUID id, UUID correlationId, Consumer<Message> consumer) {
}
