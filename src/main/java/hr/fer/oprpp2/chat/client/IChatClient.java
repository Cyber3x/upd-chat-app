package hr.fer.oprpp2.chat.client;

import hr.fer.oprpp2.chat.common.messages.InMsg;

public interface IChatClient {

    void initiateDisconnect();
    void disconnect();

    void sendTextMessage(String messageText);

    void setUID(long UID);

    void handleIncomingMessage(InMsg message);
}
