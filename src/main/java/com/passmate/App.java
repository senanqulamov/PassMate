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

        // Load stylesheets safely
        addStylesheetIfPresent(scene, "/css/style.css");
        addStylesheetIfPresent(scene, "/css/theme.css");

        stage.setTitle("PassMate - Password Manager");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);

        // Set application icon safely
        try {
            var iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
            } else {
                System.out.println("Icon resource not found: /icons/icon.png");
            }
        } catch (Exception e) {
            System.out.println("Could not load application icon: " + e.getMessage());
        }

        stage.show();
    }

    private void addStylesheetIfPresent(Scene scene, String path) {
        try {
            var url = getClass().getResource(path);
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            } else {
                System.out.println("Stylesheet not found: " + path);
            }
        } catch (Exception e) {
            System.out.println("Failed to load stylesheet " + path + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
