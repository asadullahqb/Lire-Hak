package App;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    Label generalLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Application started.");
        generalLabel.setText("Awaiting Instructions...");
    }

    public void chooseFile() {

    }

    public void indexImages() {

    }
}
