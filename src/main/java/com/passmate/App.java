package com.passmate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/views/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        // Load stylesheets
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());

        stage.setTitle("PassMate - Password Manager");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);

        // Set application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Could not load application icon");
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
