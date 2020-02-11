package id.ac.ui.clab.dchronochat.chat;

/**
 * Created by yudiandreanp on 08/05/16.
 */

import id.ac.ui.clab.dchronochat.chat.ChatbufProto.ChatMessage;

public class CachedMessage {
    public CachedMessage
            (long sequenceNo, ChatMessage.ChatMessageType messageType, String message, long time)
    {
        sequenceNo_ = sequenceNo;
        messageType_ = messageType;
        message_ = message;
        time_ = time;
    }

    public final long
    getSequenceNo() { return sequenceNo_; }

    public final ChatMessage.ChatMessageType
    getMessageType() { return messageType_; }

    public final String
    getMessage() { return message_; }

    public final long
    getTime() { return time_; }

    private final long sequenceNo_;
    private final ChatMessage.ChatMessageType messageType_;
    private final String message_;
    private final long time_;
};
