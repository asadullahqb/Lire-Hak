package App;

import javafx.scene.control.Alert;

public class DialogService {

    public static void DisplayAlert(String title, String content){
        //Used to notify the user.

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
