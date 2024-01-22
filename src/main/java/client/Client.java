// Client.java
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java Main client [port] [query]");
            return;
        }

        int serverPort = Integer.parseInt(args[0]);  // 通过命令行参数指定服务器端口号
        String queryParameters = args[1];

        try (Socket socket = new Socket("localhost", serverPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 发送查询请求
            writer.println(queryParameters);

            // 接收服务器响应
            String serverResponse = reader.readLine();
            System.out.println("Received from server: " + serverResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
