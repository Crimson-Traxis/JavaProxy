package ProxyGUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import javax.swing.text.Style;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
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

                HTTPObject clientHttpRequest = new HTTPObject(request.getInputStream());

                String hostIP = InetAddress.getByName(new URL(("http://" + clientHttpRequest.HeaderParameter("Host").replace(" ",""))).getHost()).getHostAddress();

                if(clientHttpRequest.Method().toUpperCase().equals("GET")) {
                    Platform.runLater(new UpdateTextField(textClientRequests,"Handling a new request: " + ID + "\n" + Arrays.toString(clientHttpRequest.GetBytes()) + "\n\n"));

                    Socket toServer = new Socket(hostIP,80);

                    DataOutputStream outToServer = new DataOutputStream(toServer.getOutputStream());

                    Platform.runLater(new UpdateTextField(textServerRequests,"Sending a new request: " + ID + "\n" + Arrays.toString(clientHttpRequest.GetBytes()) + "\n\n"));

                    outToServer.write(clientHttpRequest.GetBytes());

                    HTTPObject serverHttpResponse = new HTTPObject(toServer.getInputStream());

                    Platform.runLater(new UpdateTextField(textSeverResponses,"Receiving a new response: " + ID + "\n" + Arrays.toString(serverHttpResponse.GetBytes()) + "\n\n"));

                    request.getOutputStream().write(serverHttpResponse.GetBytes());

                    Platform.runLater(new UpdateTextField(textClientResponses,"Sending a new response: " + ID + "\n" + Arrays.toString(serverHttpResponse.GetBytes()) + "\n\n"));

                    toServer.close();
                    fromClient.close();

                }

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
            fromClient.close();
        }catch (Exception ex) {

        }
    }

    public void OnClose() {
        try {
            fromClient.close();
            System.out.println("Closed");
        }
        catch (Exception ex) {

        }
    }
}

