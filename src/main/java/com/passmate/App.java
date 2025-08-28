package com.passmate;

import com.passmate.controllers.MainController;
import com.passmate.controllers.PinController;
import com.passmate.services.EncryptionService;
import com.passmate.services.PasswordService;
import com.passmate.services.PinService;
import com.passmate.services.VaultService;
import com.passmate.services.impl.AESEncryptionService;
import com.passmate.services.impl.LocalPinService;
import com.passmate.services.impl.PasswordServiceImpl;
import com.passmate.services.impl.VaultServiceImpl;
import com.passmate.services.repo.FileVaultRepository;
import com.passmate.utils.WindowsProfileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        showPin(stage);
    }

    private void showPin(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(App.class.getResource("/views/PinView.fxml"));
        Scene scene = new Scene(fxml.load(), 420, 560);
        PinController ctrl = fxml.getController();

        PinService pinService = new LocalPinService();
        Image profile = WindowsProfileUtil.getUserImage();
        ctrl.init(pinService, () -> {
            try {
                showMain(stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, profile);

        stage.setTitle("PassMate - Unlock");
        stage.setScene(scene);
        stage.show();
    }

    private void showMain(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/views/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 680);

        MainController controller = fxmlLoader.getController();

        // Services with persistence + encryption
        VaultService vaultService = new VaultServiceImpl(new FileVaultRepository());
        EncryptionService encryptionService = new AESEncryptionService();
        PasswordService passwordService = new PasswordServiceImpl(encryptionService, vaultService);
        controller.initData(vaultService, passwordService);

        stage.setTitle("PassMate");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}