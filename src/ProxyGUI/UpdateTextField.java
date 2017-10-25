package ProxyGUI;


import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class UpdateTextField implements Runnable {

    TextArea textAreaToUpdate;
    String additionalText;

    public UpdateTextField(TextArea textField, String text) {
        textAreaToUpdate = textField;
        additionalText = text;
    }

    @Override
    public void run(){
        textAreaToUpdate.setText(textAreaToUpdate.getText() + additionalText);
    }
}
