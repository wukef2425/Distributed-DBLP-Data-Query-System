package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkDistributionReader {
    private String filePath;

    public ChunkDistributionReader(String filePath) {
        this.filePath = filePath;
    }

    public List<Map<Integer, String>> readChunkDistribution() {
        List<Map<Integer, String>> chunkDistribution = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String chunkName = parts[0].trim();
                String vmName = parts[1].trim();

                // 检查是否是 primary 节点
                if (chunkName.toLowerCase().contains("primary")) {
                    // 解析 chunk index
                    int chunkIndex = Integer.parseInt(chunkName.replaceAll("[^0-9]", ""));

                    // 创建 map，存储 chunk index 和对应的 VM 名称
                    Map<Integer, String> chunkMap = new HashMap<>();
                    chunkMap.put(chunkIndex, vmName);

                    chunkDistribution.add(chunkMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunkDistribution;
    }
}
