// ClientHandler.java
package server;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String filePath;
    private Map<Integer, String> chunkDistribution;
    private String vmName;
    private int port;

    public ClientHandler(Socket clientSocket, Map<Integer, String> chunkDistribution, String filePath, int port) {
        this.clientSocket = clientSocket;
        this.chunkDistribution = chunkDistribution;
        this.filePath = filePath;
        this.port = port;
        this.vmName = "StorageVM" + (port - 8000); // Calculate vmName based on port
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // 处理客户端请求
            String clientMessage = reader.readLine();
            System.out.println("Received from client: " + clientMessage);
            // 根据请求选择相应的数据块进行处理
            String response = processData(clientMessage);
            // 发送响应给客户端
            writer.println(response);
        } catch (IOException e) {
            System.out.println("Error in client handler: " + e.getMessage());
        }
    }

    private String processData(String clientMessage) {

        String[] keyValue = clientMessage.split("=", 2);
        if (keyValue.length != 2) {
            return "Invalid query format. Expected format: key=value";
        }

        String queryType = keyValue[0].trim();
        String queryValue = keyValue[1].trim();

        if ("author".equalsIgnoreCase(queryType)) {
            System.out.println("Now Querying Author Name: " + queryValue);
            return processAuthorQuery(queryValue);
        } else if ("year".equalsIgnoreCase(queryType)) {
            System.out.println("Now Querying Year: " + queryValue);
            return "Year query is not fully implemented yet.";
        } else {
            return "Invalid query type. Only 'author' and 'year' are supported.";
        }
    }

    private String processAuthorQuery(String authorName) {
        int totalPublications = 0;

        for (int chunkIndex : chunkDistribution.keySet()) {
            String vmName = chunkDistribution.get(chunkIndex);
            if (vmName.equals(this.vmName)) {
                String fileName = filePath + vmName + "_chunk_" + chunkIndex + "_primary.dat";
                totalPublications += processFile(authorName, fileName);
            }
        }

        return "Total publications for author " + authorName + ": " + totalPublications;
    }

    private String processYearQuery(String authorName, String yearCondition) {
        int totalPublications = 0;

        for (int chunkIndex : chunkDistribution.keySet()) {
            String vmName = chunkDistribution.get(chunkIndex);
            if (vmName.equals(this.vmName)) {
                String fileName = filePath + vmName + "_chunk_" + chunkIndex + "_primary.dat";
                totalPublications += processFileWithYearCondition(authorName, fileName, yearCondition);
            }
        }

        return "Total publications for author " + authorName + " in the specified year range: " + totalPublications;
    }

    private int processFileWithYearCondition(String authorName, String fileName, String yearCondition) {
        int publicationCount = 0;

        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            StringBuilder publication = new StringBuilder();

            while ((line = fileReader.readLine()) != null) {
                if (line.contains("</publication>")) {
                    // Process the completed publication
                    if (containsAuthor(publication.toString(), authorName) && containsYear(publication.toString(), yearCondition)) {
                        publicationCount++;
                    }
                    publication.setLength(0);  // Reset StringBuilder for the next publication
                } else {
                    // Continue building the publication content
                    publication.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publicationCount;
    }

    private boolean containsYear(String publication, String yearCondition) {
        try {
            int year = extractYear(publication);
            if (year >= 0) {
                // Assuming yearCondition is in the format "startYear-endYear"
                String[] years = yearCondition.split("-");
                int startYear = Integer.parseInt(years[0]);
                int endYear = Integer.parseInt(years[1]);

                return (year >= startYear && year <= endYear);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int processFile(String authorName, String fileName) {
        int publicationCount = 0;

        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            StringBuilder publication = new StringBuilder();

            while ((line = fileReader.readLine()) != null) {
                if (line.contains("</publication>")) {
                    // Process the completed publication
                    if (containsAuthor(publication.toString(), authorName)) {
                        publicationCount++;
                    }
                    publication.setLength(0);  // Reset StringBuilder for the next publication
                } else {
                    // Continue building the publication content
                    publication.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publicationCount;
    }


    private boolean containsAuthor(String publication, String authorName) {
        return publication.contains("<author>" + authorName + "</author>");
    }

    private int extractYear(String publication) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(publication)));

            // 解析年份信息
            Element yearElement = (Element) document.getElementsByTagName("year").item(0);
            if (yearElement != null) {
                return Integer.parseInt(yearElement.getTextContent().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

}
