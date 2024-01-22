// Server.java
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Server {
    public static void main(String[] args) {
        int[] ports = { 8000, 8001, 8002 };  // 指定每个端口号
        int numStorageVMs = ports.length;
        String chunkDistributionFile = "D:\\Code\\javaProject\\DBLP\\chunk_distribution.txt";

        // 读取块分布信息
        ChunkDistributionReader distributionReader = new ChunkDistributionReader(chunkDistributionFile);
        List<Map<Integer, String>> chunkDistribution = distributionReader.readChunkDistribution();

        for (int i = 0; i < numStorageVMs; i++) {
            final int port = ports[i];
            final Map<Integer, String> vmChunkDistribution = chunkDistribution.get(i);
            new Thread(() -> startServer(port, vmChunkDistribution, "D:\\Code\\javaProject\\DBLP\\chunkdata\\")).start();
        }
    }

    private static void startServer(int port, Map<Integer, String> chunkDistribution, String filePath) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, chunkDistribution, filePath, port)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
