package EngineControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class QueryController implements Initializable {
    Stage window;
    private volatile Service<String> bgThread;

    @FXML
    private Button chooseImgBtn;

    @FXML
    private TextField imgPath;

    @FXML
    private TextArea testOutput;

//    @FXML
//    private ProgressBar queryProgressBar;

    @FXML
    public void chooseImg() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(window);
        if (file == null)
            System.out.println("No file chosen.");
        else {
            System.out.println(file.getAbsolutePath());
            imgPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void startQuery() {
         try {
             Searching();
         } catch (Exception e) {
             System.err.println(e);
         }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void Searching() {
        bgThread = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();
                    final String path = imgPath.getText();

                    @Override
                    protected String call() throws Exception {
                        BufferedImage img = null;
                        File f = new File(path);
                        if (f.exists()) {
                            try {
                                img = ImageIO.read(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get("indexPath")));
                        ImageSearcher searcher = new GenericFastImageSearcher(30, CEDD.class);
                        ImageSearchHits hits = searcher.search(img, ir);
                        System.out.println(hits);

                        for (int i = 0; i < hits.length(); i++) {
                            String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                            System.out.println(hits.score(i) + ": \t" + fileName);
                            results.append(hits.score(i) + ": \t" + fileName + "\n");
                            updateValue(results.toString());
                            Thread.sleep(50);
                        }

                        results.append("Finished Querying Matched Images.");
                        return results.toString();
                    }
                };
            }
        };
        testOutput.textProperty().bind(bgThread.valueProperty());
        testOutput.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                testOutput.setScrollTop(Double.MIN_VALUE);
            }
        });
        bgThread.start();
    }

    private void handleDragDropped(DragEvent event){
        Dragboard db = event.getDragboard();
        File file = db.getFiles().get(0);
    }

    public void outputImages() {

    }
}
