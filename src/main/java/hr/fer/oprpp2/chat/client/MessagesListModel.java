package hr.fer.oprpp2.chat.client;

import hr.fer.oprpp2.chat.common.messages.InMsg;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

public class MessagesListModel implements ListModel<String> {

    ArrayList<String> messages = new ArrayList<>();
    ArrayList<ListDataListener> listeners = new ArrayList<>();

    public void addMessage(String serverHostname, int port, InMsg messageData) {
        String formattedMessage = String.format("[%s:%d] Poruka od korisnika: %s",
                serverHostname, port, messageData.senderName());


        messages.add(0, formattedMessage);
        messages.add(1, messageData.messageText());
        messages.add(2, "\n");

        for (ListDataListener l : listeners) {
            l.intervalAdded(
                    new ListDataEvent(
                            this,
                            ListDataEvent.INTERVAL_ADDED,
                            0,
                            2
                    )
            );
        }
    }

    @Override
    public int getSize() {
        return messages.size();
    }

    @Override
    public String getElementAt(int index) {
        return messages.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
}
