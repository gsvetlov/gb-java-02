package ru.svetlov.chatClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class testChatClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/clientMainStage.fxml"));
        primaryStage.setTitle("testChatClientApp");
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
}
