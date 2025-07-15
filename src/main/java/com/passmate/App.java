package com.passmate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/views/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());

        stage.setTitle("PassMate");
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png")));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


