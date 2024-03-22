package cat.michal.catbase.client.message;

import org.jetbrains.annotations.NotNull;

public final class MessageHandleResult {
    private MessageHandleResult() {
    }

    private MessageResponse response;
    private boolean shouldRespond;

    public static @NotNull MessageHandleResult shouldRespond(boolean val) {
        MessageHandleResult messageHandleResult = new MessageHandleResult();
        messageHandleResult.shouldRespond = val;

        return messageHandleResult;
    }

    public static @NotNull MessageHandleResult response(MessageResponse message) {
        MessageHandleResult messageHandleResult = new MessageHandleResult();
        messageHandleResult.response = message;

        return messageHandleResult;
    }

    public boolean isResponse() {
        return response != null;
    }


    public MessageResponse getResponse() {
        return response;
    }

    public boolean isShouldRespond() {
        return shouldRespond;
    }
}
