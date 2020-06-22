package ViewControllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

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
}
