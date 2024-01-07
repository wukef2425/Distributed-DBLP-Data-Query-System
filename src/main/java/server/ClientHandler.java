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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<DataPartition> dataPartitions;
    private int port;

    private Logger logger;

    public ClientHandler(Socket clientSocket, List<DataPartition> dataPartitions, int port) {
        this.clientSocket = clientSocket;
        this.dataPartitions = dataPartitions;
        this.port = port;

        // Initialize logger
        logger = Logger.getLogger(ClientHandler.class.getName());
        try {
            FileHandler fileHandler = new FileHandler("server_log_" + port + ".txt");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read client request
            String clientRequest = reader.readLine();
            logger.log(Level.INFO, "Received request on port " + port + ": " + clientRequest);

            String response = "Response from port " + port;
            writer.println(response);
            logger.log(Level.INFO, "Sent response on port " + port + ": " + response);

        } catch (IOException e) {
            e.printStackTrace();
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
