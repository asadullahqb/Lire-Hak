package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class SideBarController implements Initializable {
    SwitchView switchView = new SwitchView();
    IndexController indexController = new IndexController();

    @FXML
    private BorderPane mainPane;


    @FXML
    private void indexBtn(ActionEvent event) {
        System.out.println("Indexing pane is clicked.");
        Pane v = switchView.getPane(SwitchView.CurrentPane.INDEX);
//        Pane v = switchView.getPane("Indexing");
        mainPane.setCenter(v);
    }

    @FXML
    private void queryBtn(ActionEvent event) {
        System.out.println("Query pane is clicked.");
        Pane v = switchView.getPane(SwitchView.CurrentPane.QUERY);
//        Pane v = switchView.getPane("Query");
        mainPane.setCenter(v);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Application started.");
    }


}
