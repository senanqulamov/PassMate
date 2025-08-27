package com.passmate;

import com.passmate.controllers.MainController;
import com.passmate.services.VaultService;
import com.passmate.services.impl.VaultServiceImpl;
import com.passmate.services.repo.FileVaultRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/views/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 650);

        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);

        MainController controller = fxmlLoader.getController();
        controller.makeWindowDraggable(scene, stage);

        // Inject services with persistence
        VaultService vaultService = new VaultServiceImpl(new FileVaultRepository());
        controller.initData(vaultService);

        stage.setTitle("PassMate");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}