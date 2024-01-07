// Client.java
package client;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = Integer.parseInt(args[0]);  // 通过命令行参数指定服务器端口号

        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (true) {
                // Send a query request
                writer.println("Query from Client");

                // Receive server response
                String serverResponse = reader.readLine();
                System.out.println("Received from server: " + serverResponse);

                // Introduce a delay between requests (you can adjust the delay as needed)
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
