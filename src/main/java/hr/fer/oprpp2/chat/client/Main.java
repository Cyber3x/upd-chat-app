package hr.fer.oprpp2.chat.client;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ParsedArgs parsedArgs = parseArgs(args);

        ChatClient chatClient = new ChatClient(parsedArgs.hostname, parsedArgs.port, parsedArgs.senderName);

        SwingUtilities.invokeLater(() -> {
            new GUI(chatClient).setVisible(true);
        });
    }

    private static ParsedArgs parseArgs(String[] args) {
        // TODO: check the format of args here
        if (args.length != 3) {
            System.out.println("Invalid arguments, expected format: <hostname> <port> <sender_name>");
            System.exit(1);
        }
        String hostname = args[0];
        String port = args[1];
        String senderName = args[2];

        return new ParsedArgs(hostname, Integer.parseInt(port), senderName);
    }

    private record ParsedArgs(String hostname, int port, String senderName) {
    }
}
