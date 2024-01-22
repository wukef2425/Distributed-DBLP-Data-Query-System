// Main.java
import client.Client;
import server.Server;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("server")) {
                Server.main(new String[]{});
            } else if (args[0].equals("client")) {
                if (args.length > 2) {
                    Client.main(new String[]{args[1],args[2]});
                } else {
                    System.out.println("Usage: java Main client [port] [query]");
                }
            } else {
                printUsage();
            }
        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java Main client [port] [query]");
        System.out.println("Usage: java Main server");
    }
}

