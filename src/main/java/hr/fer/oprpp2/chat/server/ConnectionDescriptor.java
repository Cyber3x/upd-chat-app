package hr.fer.oprpp2.chat.server;

public record ConnectionDescriptor(
        long clientsRandKey,
        long UID,

        String clientName,
        ConnectionThread connectionThread
) {
}
