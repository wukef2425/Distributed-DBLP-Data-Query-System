// Chunking.java
package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Chunking {
    private static final String FILE_PATH = "C:/Users/wukef/Downloads/dblp.xml";
    private static final int CHUNK_SIZE = 20 * 1024 * 1024;  // 20MB
    private static final int NUM_STORAGE_VMS = 3;
    private static final int NUM_REPLICAS = 2;

    public static void main(String[] args) {
        try {
            Path filePath = Paths.get(FILE_PATH);

            // 逐块读取文件
            List<String> chunks = processFileChunks(filePath);

            // 保存块的分布情况
            saveChunkDistribution(chunks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> processFileChunks(Path filePath) throws IOException {
        List<String> chunks = new ArrayList<>();
        byte[] buffer = new byte[CHUNK_SIZE];
        int chunkIndex = 0;

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] chunkData = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunkData, 0, bytesRead);

                // 将块分配给不同的存储虚拟机
                String storageVM = assignStorageVM(chunkIndex);
                saveChunkLocally(chunkData, storageVM, chunkIndex);

                // 记录块的分布情况
                chunks.add(storageVM + ": Chunk " + chunkIndex);

                chunkIndex++;
            }
        }

        return chunks;
    }

    private static void saveChunkLocally(byte[] chunkData, String storageVM, int chunkIndex) {
        // 将块保存在本地
        String filename = storageVM + "_chunk_" + chunkIndex + ".dat";
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(chunkData);
        } catch (IOException e) {
            // 记录异常信息
            e.printStackTrace();
        }
    }

    private static void saveChunkDistribution(List<String> chunks) {
        // 将块的分布情况保存在文件中
        try (PrintWriter writer = new PrintWriter(new FileWriter("chunk_distribution.txt"))) {
            for (String chunk : chunks) {
                writer.println(chunk);
            }
        } catch (IOException e) {
            // 记录异常信息
            e.printStackTrace();
        }
    }

    private static String assignStorageVM(int chunkIndex) {
        // 计算主副本所在的虚拟机
        int primaryVMIndex = chunkIndex % NUM_STORAGE_VMS;
        // 计算副本所在的虚拟机，确保不与主副本在同一虚拟机上
        int replicaVMIndex = (primaryVMIndex + 1) % NUM_STORAGE_VMS;
        // 每个虚拟机存储一个副本，主副本和副本在不同的虚拟机上
        return "StorageVM" + (chunkIndex % NUM_REPLICAS == 0 ? primaryVMIndex : replicaVMIndex);
    }
}
