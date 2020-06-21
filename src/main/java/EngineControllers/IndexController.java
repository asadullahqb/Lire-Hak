package EngineControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Text;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class IndexController implements Initializable {
    Stage window;
    private volatile Service<String> bgThread;
    private final List<Document> indexField = new ArrayList<>();

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

    @FXML
    private Button clearIndexBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startIndexingBtn.setDisable(true);
        stopIndexingBtn.setDisable(true);
        chooseFolderBtn.setDisable(false);

        Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
        String prevIndexFilePath = prefs.get("indexingFilePath", "");
        if(prevIndexFilePath != "") {
            filePath.setText(prevIndexFilePath);
            startIndexingBtn.setDisable(false);
        }
        else
            clearIndexBtn.setDisable(true);
    }

    @FXML
    public void chooseFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File directory = dc.showDialog(window);
        if (directory == null)
            System.out.println("No directory chosen");
        else {
            //Reset for the new indexing.
            indexOutput.textProperty().unbind();
            indexOutput.textProperty().setValue("");
            indexProgressBar.progressProperty().unbind();
            indexProgressBar.setProgress(0);

            System.out.println(directory.getAbsolutePath());
            filePath.setText(directory.getAbsolutePath());
            startIndexingBtn.setDisable(false);
            startIndexingBtn.requestFocus();
        }
    }

    @FXML
    public void startIndexing() {
        try {
            startIndexingBtn.setDisable(true);
            stopIndexingBtn.setDisable(false);
            chooseFolderBtn.setDisable(true);
            indexOutput.requestFocus();
            Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
            prefs.putBoolean("isIndexing", true);
            Indexing();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    private void stopIndexing(ActionEvent event) {
        try{
            bgThread.cancel();
            Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
            prefs.putBoolean("isIndexing", false);

            DisplayAlert("Note", "You have stopped the indexing. Please start again.");

            clearIndexMethod();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    private void clearIndex(ActionEvent event) throws BackingStoreException {
        clearIndexMethod();
        DisplayAlert("Success", "The index has been cleared.");
    }

    private void clearIndexMethod() throws BackingStoreException {
        //Clear preferences
        Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
        prefs.clear();

        //Reset views.
        startIndexingBtn.setDisable(true);
        stopIndexingBtn.setDisable(true);
        clearIndexBtn.setDisable(true);
        chooseFolderBtn.setDisable(false);
        filePath.setText("");
        indexProgressBar.progressProperty().unbind();
        indexProgressBar.setProgress(0);
        indexOutput.setText("");
    }

    private void DisplayAlert(String title, String content){
        //Used to notify the user.

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void Indexing() {
        bgThread = new Service<String>() {
            @Override
            protected Task<String> createTask()  {
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();
                    final String path = filePath.getText();

                    @Override
                    protected void succeeded() {
                        super.succeeded();

                        Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
                        prefs.putBoolean("isIndexing", false);

                        DisplayAlert("Success", "Indexing has completed.");

                        //Reset all the views.
                        startIndexingBtn.setDisable(false);
                        stopIndexingBtn.setDisable(true);
                        clearIndexBtn.setDisable(false);
                        chooseFolderBtn.setDisable(false);

                        //Store file path in previous index.
                        Preferences prefs2 = Preferences.userRoot().node("/LIRE-HAK/Store");
                        prefs2.put("indexingFilePath", filePath.getText());
                    }

                    @Override
                    protected void failed() {
                        super.failed();

                        Preferences prefs = Preferences.userRoot().node("/LIRE-HAK/Store");
                        prefs.putBoolean("isIndexing", false);

                        DisplayAlert("Error", "A problem occurred while indexing. Please try again.");

                        //Reset all the views.
                        startIndexingBtn.setDisable(true);
                        stopIndexingBtn.setDisable(true);
                        clearIndexBtn.setDisable(false);
                        chooseFolderBtn.setDisable(false);
                        filePath.setText(null);
                    }

                    @Override
                    public String call() throws IOException, InterruptedException {
                        updateProgress(0,100); //Fix as a finite progress bar.
                        System.out.println("Path to folder: " + path);
                        ArrayList<String> images = FileUtils.getAllImages(new File(path), true);
                        int numImages = images.size();
                        // Creating a CEDD document builder;
                        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

                        // TODO: Add more.
                        globalDocumentBuilder.addExtractor(FCTH.class);
                        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);

                        try {
                            int progress = 0;
                            for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                                String imageFilePath = it.next();
                                //results.append("Indexing " + imageFilePath + "\n");
                                //updateValue(results.toString());
                                indexOutput.appendText("Indexing " + imageFilePath + "\n");
                                //Using append directly on the text area has the benefit of auto-scrolling to the end.
                                try {
                                    // Write indexed image features.
                                    BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                                    BufferedImage newimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                                    newimg.createGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                                    Document document = globalDocumentBuilder.createDocument(newimg, imageFilePath);
                                    indexField.add(document);
                                } catch (Exception e) {
                                    System.err.println("Error reading image or indexing it.");
                                    e.printStackTrace();
                                }
                                updateProgress(++progress, numImages);

                                Thread.sleep(50);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        LuceneIndexer();
                        //results.append("Finished indexing.");
                        indexOutput.appendText("Finished indexing.");
                        return results.toString();
                    }
                };
            }
        };
        //indexOutput.textProperty().bind(bgThread.valueProperty());
        indexProgressBar.progressProperty().bind(bgThread.progressProperty());
        bgThread.start();
    }

    private void LuceneIndexer() throws IOException {
        IndexWriter iw = LuceneUtils.createIndexWriter("indexPath", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        for (Document document: indexField) {
            System.out.println(document);
            iw.addDocument(document);
        }
        LuceneUtils.closeWriter(iw);
    }
}
