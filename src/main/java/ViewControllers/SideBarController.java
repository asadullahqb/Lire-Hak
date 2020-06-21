package ViewControllers;

import EngineControllers.IndexController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    private Button indexButtonId;

    @FXML
    private Button queryButtonId;

    @FXML
    private Button similarButtonId;


    @FXML
    private void indexBtn(ActionEvent event) {
        System.out.println("Indexing pane is clicked.");

        //Set only this button disabled.
        indexButtonId.setDisable(true);
        queryButtonId.setDisable(false);
        similarButtonId.setDisable(false);

        Pane v = switchView.getPane(SwitchView.CurrentPane.INDEX);
//        Pane v = switchView.getPane("Indexing");
        mainPane.setCenter(v);
    }

    @FXML
    private void queryBtn(ActionEvent event) {
        System.out.println("QueryController pane is clicked.");

        //Set only this button disabled.
        indexButtonId.setDisable(false);
        queryButtonId.setDisable(true);
        similarButtonId.setDisable(false);

        Pane v = switchView.getPane(SwitchView.CurrentPane.QUERY);
//        Pane v = switchView.getPane("QueryController");
        mainPane.setCenter(v);
    }
    
    @FXML
    private void similarBtn(ActionEvent event) {
        System.out.println("FeatureSimilarity pane is clicked.");

        //Set only this button disabled.
        indexButtonId.setDisable(false);
        queryButtonId.setDisable(false);
        similarButtonId.setDisable(true);

        Pane v = switchView.getPane(SwitchView.CurrentPane.SIMILAR);
//        Pane v = switchView.getPane("QueryController");
        mainPane.setCenter(v);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("Application started.");

        //Set only index button disabled.
        indexButtonId.setDisable(true);
        queryButtonId.setDisable(false);
        similarButtonId.setDisable(false);

        //Initialize with the index view.
        Pane v = switchView.getPane(SwitchView.CurrentPane.INDEX);
        mainPane.setCenter(v);
    }


}
