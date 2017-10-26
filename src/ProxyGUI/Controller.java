package ProxyGUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Controller implements Initializable {

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

    private ExecutorService executor;
    private ServerSocket fromClient;
    private final AtomicLong sequenceNumber = new AtomicLong(0);

    @Override
    public void initialize(URL locations, ResourceBundle resources) {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void StarServer() {
        buttonStart.setDisable(true);
        try {
            fromClient = new ServerSocket(Integer.parseInt(textPort.getText()));
            executor.execute(() ->{
                while(true) {
                    try {
                        Socket request = fromClient.accept();
                        HandleConnection(request,executor);
                    }
                    catch (Exception ex) {
                        ResetServer();
                    }
                }
            });

            serverStatus.setFill(Color.GREEN);
        }
        catch (Exception ex) {
            ResetServer();
            System.out.println(ex);
        }
    }

    public void HandleConnection(Socket request,ExecutorService executor) {
        executor.execute(() -> {
            try {
                long ID = sequenceNumber.getAndIncrement();

                BufferedReader httpRequestStream = new BufferedReader(new InputStreamReader(request.getInputStream()));

                String httpClientRequestString = "";
                String line;
                while((line = httpRequestStream.readLine()) != null) {
                    if(line.length() == 0) {
                        break;
                    }
                    httpClientRequestString += line + "\r\n";
                }
                httpClientRequestString += "\r\n";

                HTTPRequest clientHttpRequest = new HTTPRequest(httpClientRequestString);

                if(clientHttpRequest.Method().toUpperCase().equals("GET")) {
                    Platform.runLater(new UpdateTextField(textClientRequests,"Handling a new request: " + ID + "\n" + httpClientRequestString + "\n\n"));
                }
                else {

                }

                HTTPResponse httpResponse = new HTTPResponse();

                httpResponse.AddHeader("Connection","close");

                httpResponse.SendResponse(request.getOutputStream());

                httpRequestStream.close();
                request.close();

            } catch (Exception ex) {
                System.out.println(ex);
            }
        });
    }

    private void SaveObject() {

    }

    private void GetObject() {

    }

    private void ResetServer() {
        try {
            buttonStart.setDisable(false);
            serverStatus.setFill(Color.RED);
            serverSocket.close();
        }catch (Exception ex) {

        }
    }

    public void OnClose() {
        try {
            serverSocket.close();
            System.out.println("Closed");
        }
        catch (Exception ex) {

        }
    }
}

