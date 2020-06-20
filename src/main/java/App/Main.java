package App;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class Main extends Application {
    Stage window;
    Button closeButton, fileButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("App.fxml")));
        window = primaryStage;
        window.setTitle("DonutRanger");

        window.setOnCloseRequest(event -> {
            event.consume();
            closeProgram();
        });

        window.setScene(new Scene(root, 1100, 600));
        window.show();
    }

    private void closeProgram() {
        Boolean answer = ConfirmBox.display("Sure you want to exit?", "Exit?");
        if (answer)
            window.close();
    }

    @FXML
    private void chooseFile() {
        DirectoryChooser dc = new DirectoryChooser();
        File directory = dc.showDialog(window);
        if (directory == null) System.out.println("No directory chosen");
        else System.out.println(directory.getAbsolutePath());
    }
}
