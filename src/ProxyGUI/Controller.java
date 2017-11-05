package ProxyGUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import javax.swing.text.FieldView;
import javax.swing.text.Style;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The controller that handles the events from the gui
 *
 * @author  Trent Killinger
 * @version 1.0
 * @since   9-27-2017
 */

public class Controller {

    @FXML
    private TextArea textCache;

    @FXML
    private TextArea textClientRequests;

    @FXML
    private TextArea textServerRequests;

    @FXML
    private TextArea textClientResponses;

    @FXML
    private TextArea textSeverResponses;

    @FXML
    private Button buttonStart;

    @FXML
    private Label labelLocalHost;

    @FXML
    private Circle serverStatus;

    @FXML
    private TextField textPort;
    /*Manages threads*/
    private ExecutorService executor;
    /*Socket Server*/
    private ServerSocket fromClient;
    /*Thread ID tracker*/
    private final AtomicLong sequenceNumber = new AtomicLong(0);

    /**
     * Starts the server
     * @param none
     * @return Nothing
     */
    public void StarServer() {
        buttonStart.setDisable(true);
        try {
            fromClient = new ServerSocket(Integer.parseInt(textPort.getText()));
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            executor.execute(() -> {
                while (true) {
                    try {
                        Socket request = fromClient.accept();
                        HandleConnection(request, executor);
                    } catch (Exception ex) {

                    }
                }
            });

            serverStatus.setFill(Color.GREEN);
        } catch (Exception ex) {
            System.out.println(ex);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Proxy has crashed.");
            alert.setHeaderText("Your proxy has crashed");
            alert.setContentText("Please close and reopen the program");
            alert.show();
        }
    }

    /**
     * Handles the individual connection
     * @param request
     * @param executor
     * @return Nothing
     */
    public void HandleConnection(Socket request, ExecutorService executor) {
        executor.execute(() -> {
            try {
                long ID = sequenceNumber.getAndIncrement();

                HTTPObject clientHttpRequest = new HTTPObject(request.getInputStream());

                String host = clientHttpRequest.HeaderParameter("Host").replace(" ", "");

                String hostIP = InetAddress.getByName(new URL(("http://" + host)).getHost()).getHostAddress();

                if (clientHttpRequest.Method().toUpperCase().equals("GET")) {
                    Platform.runLater(new UpdateTextField(textClientRequests, "Handling a new request to " + host + " from client, Thread ID: " + ID + "\n"));

                    Socket toServer = new Socket(hostIP, 80);

                    DataOutputStream outToServer = new DataOutputStream(toServer.getOutputStream());

                    Platform.runLater(new UpdateTextField(textServerRequests, "Sending a new request to server IP " + hostIP + ", Thread ID: " + ID + "\n"));

                    if (ObjectExistInCache(clientHttpRequest.Path().replace('.', '-').split("\\?")[0])) {
                        clientHttpRequest.AddHeaderParameter("If-Modified-Since", CachedObjectLastModifiedScince(clientHttpRequest.Path().replace('.', '-').split("\\?")[0]));
                    }

                    outToServer.write(clientHttpRequest.GetBytes());

                    HTTPObject serverHttpResponse = new HTTPObject(toServer.getInputStream());

                    Platform.runLater(new UpdateTextField(textSeverResponses, "Receiving a new response from server, Thread ID: " + ID + "\n"));

                    DataOutputStream outToClient = new DataOutputStream(request.getOutputStream());

                    if (serverHttpResponse.GeneralHeader().equals("HTTP/1.1 304 Not Modified")) {
                        serverHttpResponse.Body(GetObjectFromCache(clientHttpRequest.Path().replace('.', '-').split("\\?")[0]));
                    } else {
                        SaveObjectToCache(clientHttpRequest.Path().replace('.', '-').split("\\?")[0], serverHttpResponse.Body());
                    }
                    outToClient.write(serverHttpResponse.GetBytes());

                    Platform.runLater(new UpdateTextField(textClientResponses, "Sending a new response to client, Thread ID " + ID + "\n"));

                    outToServer.close();
                    toServer.close();
                    outToClient.close();

                }

                request.close();

            } catch (Exception ex) {


                System.out.println(ex);
            }
        });
    }

    /**
     * Saves the object to the cache
     * @param path
     * @param data
     * @return Nothing
     */
    private void SaveObjectToCache(String path, byte[] data) {
        try {

            File cache = new File(System.getProperty("java.io.tmpdir") + "\\" + path);
            cache.getParentFile().mkdirs();

            FileOutputStream cacheWriter = new FileOutputStream(cache.getPath());
            cacheWriter.write(data);
            cacheWriter.close();

            Platform.runLater(new UpdateTextField(textCache, "Saving file " + path + " to cache\n"));
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {

        }

    }

    /**
     * Formats the cached file date
     * @param path
     * @return string
     */
    private String CachedObjectLastModifiedScince(String path) {
        File cache = new File(System.getProperty("java.io.tmpdir") + "\\" + path);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        Date lastModified = new Date(cache.lastModified());
        return outputFormat.format(lastModified);
    }

    /**
     * Determines if the cached file exists
     * @param path
     * @return boolean
     */
    private boolean ObjectExistInCache(String path) {
        File cache = new File(System.getProperty("java.io.tmpdir") + "\\" + path);
        return cache.exists();
    }

    /**
     * Gets the object form the cache
     * @param path
     * @return byte[]
     */
    private byte[] GetObjectFromCache(String path) {
        try {
            File cache = new File(System.getProperty("java.io.tmpdir") + "\\" + path);
            Platform.runLater(new UpdateTextField(textCache, "Loading file " + path + " from cache\n"));
            return Files.readAllBytes(cache.toPath());
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {

        }
        return new byte[0];
    }

    /**
     * Closes the TCP server
     * @param none
     * @return Nothing
     */
    public void OnClose() {
        try {
            fromClient.close();
            System.out.println("Closed");
        } catch (Exception ex) {

        }
    }
}

