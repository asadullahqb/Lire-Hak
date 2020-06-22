// HAK - Haris, Asad, Kyon

package App;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.prefs.Preferences;

public class Main extends Application {
    Stage window;
    Button closeButton, fileButton;
    private volatile Service<String> bgThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("App.fxml")));
        window = primaryStage;
        window.setTitle("LIRE-HAK Application");

        window.setOnCloseRequest(event -> {
            event.consume();
            closeProgram();
        });

        Scene scene = new Scene(root, 1100, 600);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("Theme.css").toExternalForm());

        window.setScene(scene);
        //window.setScene(new Scene(root, 1100, 600));
        window.show();

        bgThread = new Service<String>() {
            @Override
            protected Task<String> createTask()  {
                return new Task<String>() {
                    @Override
                    protected void succeeded() {
                        super.succeeded();

                        //Check and notify of existing index.
                        Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
                        if(prefs.get("indexingFilePath", "") != "")
                            DialogService.DisplayAlert("Note", "You had previously performed indexing. You can immediately\nproceed to querying with the previous index or perform a new\nindex if you wish.");
                    }

                    @Override
                    public String call() throws IOException, InterruptedException {
                        Thread.sleep(1200);
                        return null;
                    }
                };
            }
        };
        bgThread.start();
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
