package hr.fer.oprpp2.chat.common;

import hr.fer.oprpp2.chat.common.messages.Message;

public record SentMessageMetadata(Message message, int numberOfRetransmissionLeft) {
}


