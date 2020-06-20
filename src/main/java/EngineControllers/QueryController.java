package EngineControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QueryController implements Initializable {
    Stage window;
    private volatile Service<String> bgThread;
    private final List<String> imageArray = new ArrayList<>();

    int count = 0;
    private int nRows = 3;
    private int nCols = 3;
    private static final double ELEMENT_SIZE = 100;
    private static final double GAP = ELEMENT_SIZE / 10;

    @FXML
    private Button chooseImgBtn;

    @FXML
    private TextField imgPath;

    @FXML
    private TextArea testOutput;

    @FXML
    private ProgressBar queryProgressBar;

    @FXML
    private TilePane tilePane;

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

    @FXML
    private void loadImages() {
        createElements();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tilePane.setPrefColumns(nCols);
//        tilePane.setPrefRows(nRows);
        tilePane.setHgap(GAP);
        tilePane.setVgap(GAP);
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
                            imageArray.add(fileName);
                            results.append(hits.score(i) + ": \t" + fileName + "\n");
                            updateValue(results.toString());
                            Thread.sleep(50);
                        }

                        updateValue(results.toString());

                        updateProgress(100, 100);
                        results.append("Finished Querying Matched Images. ");
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
        queryProgressBar.progressProperty().bind(bgThread.progressProperty());
        bgThread.start();
    }

    // For image loading
    private void createElements() {

        // TODO: Display all images in an array instead.
        tilePane.getChildren().clear();
        int colCalc = imageArray.size();
        for (int i = 0; i < nCols; i++) {
            for (int j = 0; j < nRows; j++) {
                tilePane.getChildren().add(createPage(count));
                count++;
            }
        }
    }

    // TODO: Image array
    public VBox createPage(int index) {
        System.out.println(imageArray.get(index));
        ImageView imageView = new ImageView();
        File file = new File(imageArray.get(index));
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageView.setFitHeight(ELEMENT_SIZE);
            imageView.setFitWidth(ELEMENT_SIZE);
            imageView.setSmooth(true);
            imageView.setCache(true);
        } catch (IOException e) {
            System.out.println(e);
        }

        VBox pageBox = new VBox();
        pageBox.getChildren().add(imageView);
        pageBox.setStyle("-fx-border-color: #383838");

        // Destroy
        imageView = null;
        return pageBox;
    }
}
