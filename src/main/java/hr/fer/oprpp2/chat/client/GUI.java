package hr.fer.oprpp2.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GUI extends JFrame {

    private final IChatClient chatClient;
    private JTextField textField;

    private MessagesListModel messagesListModel;


    public GUI(ChatClient chatClient, MessagesListModel messagesListModel) {
        this.messagesListModel = messagesListModel;
        this.chatClient = chatClient;

        initGUI();
        initListeners();
    }

    private void initGUI() {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize(720, 480);
        this.setLocationRelativeTo(null);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        textField = new JTextField();

        JList<String> list = new JList<>(messagesListModel);
        list.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        JScrollPane scrollPane = new JScrollPane(list);

        container.add(textField, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
    }

    private void initListeners() {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    return;
                }

                String messageText = textField.getText();

                if (messageText.equals("quit")) {
                    chatClient.initiateDisconnect();
                    dispose();
                    return;
                }

                chatClient.sendTextMessage(messageText);

                textField.setText("");
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatClient.initiateDisconnect();
                dispose();
            }
        });
    }
}
