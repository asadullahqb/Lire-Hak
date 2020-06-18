package EngineControllers;

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
    private void chooseImg() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void Searching() throws IOException {
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
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }

                    IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get("indexPath")));
                    ImageSearcher searcher = new GenericFastImageSearcher(30, CEDD.class);
                    ImageSearchHits hits = searcher.search(img, ir);

                    for (int i = 0; i < hits.length(); i++) {
                        String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                        System.out.println(hits.score(i) + ": \t" + fileName);
                        results.append(hits.score(i) + ": \t" + fileName);
                        Thread.sleep(100);
                    }

                    updateProgress(100, 100);
                    return results.toString();
                }
            };

        }
        };
        // TODO: Add bg controls.
//        testOutput.textProperty().
        bgThread.start();
    }

    private void handleDragDropped(DragEvent event){
        Dragboard db = event.getDragboard();
        File file = db.getFiles().get(0);
    }

    public void outputImages() {

    }
}
