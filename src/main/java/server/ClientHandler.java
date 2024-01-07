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
import java.util.List;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<DataPartition> dataPartitions;
    private int port;

    public ClientHandler(Socket clientSocket, List<DataPartition> dataPartitions, int port) {
        this.clientSocket = clientSocket;
        this.dataPartitions = dataPartitions;
        this.port = port;
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
        String[] queryParts = clientMessage.split(",");
        String queryType = queryParts[0].trim();

        if ("author".equalsIgnoreCase(queryType)) {
            String authorName = queryParts[1].trim().substring(8); // Assuming the format is "author=[AuthorName]"
            return processAuthorQuery(authorName);
        } else if ("year".equalsIgnoreCase(queryType)) {
            String authorName = queryParts[1].trim().substring(8); // Assuming the format is "author=[AuthorName]"
            String yearCondition = queryParts[2].trim();
            return processYearQuery(authorName, yearCondition);
        }

        return "Invalid query";
    }

    private String processAuthorQuery(String authorName) {
        int totalPublications = 0;

        for (DataPartition dataPartition : dataPartitions) {
            for (String publication : dataPartition.getData()) {
                if (containsAuthor(publication, authorName)) {
                    totalPublications++;
                }
            }
        }

        return "Total publications for author " + authorName + ": " + totalPublications;
    }

    private String processYearQuery(String authorName, String yearCondition) {
        int totalPublications = 0;

        for (DataPartition dataPartition : dataPartitions) {
            for (String publication : dataPartition.getData()) {
                if (containsAuthor(publication, authorName) && containsYear(publication, yearCondition)) {
                    totalPublications++;
                }
            }
        }

        return "Total publications for author " + authorName + " in the specified year range: " + totalPublications;
    }

    private boolean containsAuthor(String publication, String authorName) {
        return publication.contains("<author>" + authorName + "</author>");
    }

    private boolean containsYear(String publication, String yearCondition) {
        int year = extractYear(publication);
        if (year >= 0) {
            return true;
        }
        return false;
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
