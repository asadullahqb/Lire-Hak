package EngineControllers;

import javafx.application.Platform;
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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MatchController implements Initializable {
    Stage window;
    private volatile Service<String> bgThread;
    private final List<String> imageArray = new ArrayList<>();

    int count = 0;
    private int nRows = 3;
    private int nCols = 3;
    private static final double ELEMENT_SIZE = 100;
    private static final double GAP = ELEMENT_SIZE / 10;

    @FXML
    private Button startCalcBtn;

    @FXML
    private Button chooseImgBtn1;

    @FXML
    private Button chooseImgBtn2;

    @FXML
    private ImageView firstImage;

    @FXML
    private ImageView secondImage;

    @FXML
    private TextField imgPath1;

    @FXML
    private TextField imgPath2;

    @FXML
    private TextArea Output;

    @FXML
    public void btnOneHandler() { chooseMatchImg(0); }
    @FXML
    public void btnTwoHandler() { chooseMatchImg(1); }

    public void chooseMatchImg(int x) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(window);

        if (file == null)
            System.out.println("No file chosen.");
        else {
            switch (x) {
                case 0:
                    imgPath1.setText(file.getAbsolutePath());
                    File f1 = new File(imgPath1.getText());
                    Image img1 = new Image(f1.toURI().toString());
                    firstImage.setImage(img1);
                    break;
                case 1:
                    imgPath2.setText(file.getAbsolutePath());
                    File f2 = new File(imgPath2.getText());
                    Image img2 = new Image(f2.toURI().toString());
                    secondImage.setImage(img2);
                    break;
            }
        }
        startCalcBtn.setDisable(imgPath1.getText().isEmpty() || imgPath2.getText().isEmpty());
    }

    @FXML
    public void startCalc() {
        try {
            System.out.println("Calculation has started");
            Output.setText("");

            BufferedImage img1 = ImageIO.read(new FileInputStream(imgPath1.getText()));
            BufferedImage newimg1 = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);
            newimg1.createGraphics().drawImage(img1, 0, 0, img1.getWidth(), img1.getHeight(), null);

            BufferedImage img2 = ImageIO.read(new FileInputStream(imgPath2.getText()));
            BufferedImage newimg2 = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
            newimg2.createGraphics().drawImage(img2, 0, 0, img2.getWidth(), img2.getHeight(), null);

            CEDD obj = new CEDD();
            obj.extract(newimg1);
            double[] arr1 = obj.getFeatureVector();
            obj.extract(newimg2);
            double[] arr2 = obj.getFeatureVector();

            double sumdiff = 0.0;
            double sumsqrdiff = 0.0;
            for (int i = 0; i < arr1.length; i++) {
                sumdiff += Math.abs(arr1[i] - arr2[i]);
                sumsqrdiff += Math.pow((arr1[i] - arr2[i]), 2);
            }
            Output.appendText("Image Manhattan Distance based on CEDD feature: " + sumdiff + "\n");
            Output.appendText("Image Euclidean Distance based on CEDD feature: " + Math.sqrt(sumsqrdiff));
        }
        catch (Exception e) { System.err.println(e); }
    }

    @FXML
    private void imageDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    private void image1DropHandler(DragEvent e) {
        try { setImgOnDrop(e, 1); } catch (Exception ex) { System.err.println(ex); }
    }

    @FXML
    private void image2DropHandler(DragEvent e) {
        try { setImgOnDrop(e, 2); } catch (Exception ex) { System.err.println(ex); }
    }

    private void setImgOnDrop(DragEvent e, int dex) throws FileNotFoundException {
        List<File> file = e.getDragboard().getFiles();
        Image img = new Image(new FileInputStream(file.get(0)));
        switch (dex) {
            case 1: imgPath1.setText(file.get(0).toString()); firstImage.setImage(img); break;
            case 2: imgPath2.setText(file.get(0).toString()); secondImage.setImage(img); break;
        }
        startCalcBtn.setDisable(imgPath1.getText().isEmpty() || imgPath2.getText().isEmpty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startCalcBtn.setDisable(true);
    }
}
