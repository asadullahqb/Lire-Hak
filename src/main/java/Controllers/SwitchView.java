package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Objects;

public class SwitchView {
    private Pane view;
    public enum CurrentPane {
        INDEX,
        QUERY,
        SIMILAR
    }

    public Pane getPane(CurrentPane pane) {
        try {
            switch(pane) {
                case INDEX:
                    view = FXMLLoader.load((getClass().getClassLoader().getResource("IndexView.fxml")));
                    break;
                case QUERY:
                    view = FXMLLoader.load((getClass().getClassLoader().getResource("QueryView.fxml")));
                    break;
                case SIMILAR:
                    view = FXMLLoader.load((getClass().getClassLoader().getResource("MatchView.fxml")));
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return view;
    }

//    public Pane getPane(String fileName) {
//        try {
//            URL fileUrl = Main.class.getResource("/resources/" + fileName + ".fxml");
//            if (fileUrl == null) {
//                throw new java.io.FileNotFoundException("File not found.");
//            }
//            view = new FXMLLoader().load(fileUrl);
//
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        return view;
//    }
}
