// Server.java
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static List<DataPartition>[] dataPartitions;  // 使用数组存储每个虚拟机的数据
    static {
        // 初始化数据块分区
        int numStorageVMs = 3;
        dataPartitions = new ArrayList[numStorageVMs];

        for (int i = 0; i < numStorageVMs; i++) {
            dataPartitions[i] = new ArrayList<>();
        }
    }
    public static void main(String[] args) {
        int[] ports = { 8000, 8001, 8002 };  // 指定每个端口号
        int numStorageVMs = ports.length;

        for (int i = 0; i < numStorageVMs; i++) {
            final int port = ports[i];
            new Thread(() -> startServer(port)).start();
        }
    }
    private static void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, dataPartitions[port - 8000], port)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
