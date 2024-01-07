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

            // 发送查询请求
            writer.println("Query from Client");

            // 接收服务器响应
            String serverResponse = reader.readLine();
            System.out.println("Received from server: " + serverResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
