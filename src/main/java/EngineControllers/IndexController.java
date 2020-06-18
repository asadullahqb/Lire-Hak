package EngineControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.semanticmetadata.lire.utils.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

public class IndexController implements Initializable {
    Stage window;
    private volatile Service<String> bgThread;

    @FXML
    private TextField filePath;

    @FXML
    private ProgressBar indexProgressBar;

    @FXML
    private TextArea indexOutput;

    @FXML
    private Button chooseFolderBtn;

    @FXML
    private Button startIndexingBtn;

    @FXML
    private Button stopIndexingBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startIndexingBtn.setDisable(true);
        stopIndexingBtn.setDisable(true);
        chooseFolderBtn.setDisable(false);
    }

    public void chooseFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File directory = dc.showDialog(window);
        if (directory == null)
            System.out.println("No directory chosen");
        else {
            System.out.println(directory.getAbsolutePath());
            filePath.setText(directory.getAbsolutePath());
            startIndexingBtn.setDisable(false);
        }
    }

    public void startIndexing() {
        try {
            startIndexingBtn.setDisable(true);
            stopIndexingBtn.setDisable(false);
            Indexing();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    private void stopIndexing(ActionEvent event) {
        if (event.getSource() == stopIndexingBtn) {
            bgThread.cancel();
        }
    }

    public void Indexing() {
        Service<String> bgThread = new Service<String>() {
            @Override
            protected Task<String> createTask()  {
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();
                    final String path = filePath.getText();
                    @Override
                    public String call() throws IOException, InterruptedException {
                        System.out.println("Path to folder: " + path);
                        ArrayList<String> images = FileUtils.getAllImages(new File(path), true);
                        try {
                            for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                                String imageFilePath = it.next();
                                results.append("Indexing " + imageFilePath + "\n");
                                updateValue(results.toString());
                                Thread.sleep(100);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            e.printStackTrace();
                        }
                        updateProgress(100, 100);
                        results.append("Finished indexing.");
                        return results.toString();
                    }
                };
            }
        };
        indexOutput.textProperty().bind(bgThread.valueProperty());
        indexOutput.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                indexOutput.setScrollTop(Double.MAX_VALUE);
            }
        });
        indexProgressBar.progressProperty().bind(bgThread.progressProperty());
        bgThread.start();
    }
}
