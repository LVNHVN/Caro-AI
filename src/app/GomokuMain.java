package app;

import controller.BoardController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.BoardModel;
import resources.BoardView;

public class GomokuMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        BoardModel model = new BoardModel();
        BoardView view = new BoardView();
        new BoardController(model, view);

        Scene scene = new Scene(view, 800, 640);
        primaryStage.setTitle("Danh Co 5 Nuoc");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}