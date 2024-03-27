package hr.fer.oprpp2.chat.server;

public class Main {
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(parseArgs(args));
    }

    private static int parseArgs(String[] args) {
        // TODO: check the format of args here
        if (args.length != 1) {
            System.out.println("Invalid arguments, expected format: <hostname> <port> <sender_name>");
            System.exit(1);
        }
        String port = args[0];


        return Integer.parseInt(port);
    }
}
