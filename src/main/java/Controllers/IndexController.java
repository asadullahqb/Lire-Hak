package Controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

public class IndexController {
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

    public void chooseFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File directory = dc.showDialog(window);
        if (directory == null)
            System.out.println("No directory chosen");
        else {
            System.out.println(directory.getAbsolutePath());
            filePath.setText(directory.getAbsolutePath());
        }
    }

    public void startIndexing() {
        try {
//            Indexing(filePath.getText());
            simulationTask();
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


    public void Indexing(String filePath) throws IOException {
        boolean passed = false;
        Console console = new Console(indexOutput);
        PrintStream pStream = new PrintStream(console, true);
        File f = new File(filePath);
        int progressCap = 100 / getFileCount(filePath) ;
        indexProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        if (f.exists() && f.isDirectory())
            passed = true;
        if (!passed) {
            System.out.println("No directory is found or given as first argument");
            System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
            System.exit(1);
        }
        ArrayList<String> images = FileUtils.getAllImages(new File(filePath), true);
        GlobalDocumentBuilder gdb = new GlobalDocumentBuilder(false, false);

        // Global feature builders
        gdb.addExtractor(CEDD.class);
        gdb.addExtractor(FCTH.class);
        gdb.addExtractor(AutoColorCorrelogram.class);
        gdb.addExtractor(SimpleColorHistogram.class);
        gdb.addExtractor(Tamura.class);
        gdb.addExtractor(EdgeHistogram.class);

        // Creating Lucene IndexWriter
        IndexWriter iw = LuceneUtils.createIndexWriter("index", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);

        // Iterating through images building the low level features
        for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
            String imageFilePath = it.next();

            System.out.println("Indexing " + imageFilePath);
            for (char c : ("Indexing " + imageFilePath + "\n").toCharArray()) {
                console.write(c);
            }
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                Document document = gdb.createDocument(img, imageFilePath);
                // TODO: Indexing image;
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing it. Check: " + imageFilePath);
                indexOutput.appendText("Error reading image or indexing it. Check: " + imageFilePath);
            }
        }

        iw.close();
        System.out.println("Finished indexing.");
        // indexOutput.appendText("Finished indexing.");
        for (char c : ("Finished indexing. \n" ).toCharArray()) {
            console.write(c);
        }
        pStream.close();
        indexProgressBar.setProgress(100);

    }

    public void simulationTask() {
        bgThread = new Service<String>() {
            @Override
            protected Task<String> createTask()  {
                final String path = filePath.getText();
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();
                    @Override
                    public String call() throws IOException, InterruptedException {
                        System.out.println("Path to folder: " + path);
                        ArrayList<String> images = FileUtils.getAllImages(new File(path), true);
                        try {
                            for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                                String imageFilePath = it.next();
//                                System.out.println("Indexing " + imageFilePath + "\n");
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
//        bgThread.setOnCancelled(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent event) {
//
//            }
//        });
//        bgThread.restart();
//
    }

    public static class Console extends OutputStream {
        private TextArea output;
        public Console(TextArea logs) {
            this.output = logs;
        }

        @Override
        public void write(int i) throws IOException {
            output.appendText(String.valueOf((char) i));
        }
    }

    private int getFileCount(String filePath) {
        int countImg = 0;
        File f = new File(filePath);
        File[] files = f.listFiles();
        for(File file : files) {
            ++countImg;
            System.out.println(countImg);
        }
        return countImg;
    }



}
