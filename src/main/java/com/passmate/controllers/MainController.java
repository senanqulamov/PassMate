package com.passmate.controllers;

import com.passmate.models.Password;
import com.passmate.models.Category;
import com.passmate.models.Vault;
import com.passmate.services.PasswordService;
import com.passmate.services.VaultService;
import com.passmate.services.impl.VaultServiceImpl;
import com.passmate.services.impl.AESEncryptionService;
import com.passmate.services.exceptions.CryptoException;
import com.passmate.utils.ClipboardUtil;
import com.passmate.utils.ToastUtil;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.security.SecureRandom;

/**
 * Main controller for the Bitwarden-style password manager interface.
 * Implements category management, working buttons, and secure operations.
 */
public class MainController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox passwordListContainer;
    @FXML private VBox sidebarContainer;
    @FXML private VBox detailContainer;
    @FXML private Label selectedPasswordName;
    @FXML private Label sectionTitleLabel;
    @FXML private TextField detailNameField;
    @FXML private TextField detailUsernameField;
    @FXML private TextField detailPasswordDisplay;
    @FXML private TextField detailWebsiteField;
    @FXML private TextArea detailNotesField;
    @FXML private Label createdByLabel;
    @FXML private Label createdDateLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Button copyUsernameBtn;
    @FXML private Button copyPasswordBtn;
    @FXML private Button goToPageBtn;
    @FXML private Button editBtn;
    @FXML private Button duplicateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button togglePasswordBtn;

    // Services and data
    private final PasswordService passwordService;
    private final VaultService vaultService;
    private FilteredList<Password> filteredPasswords;
    private Password selectedPassword;
    private Vault currentVault;
    private VBox selectedPasswordCard;
    private VBox selectedCategoryItem;
    private String currentCategory = "all";
    private boolean isPasswordVisible = false;
    private boolean isEditMode = false;

    public MainController() {
        this.passwordService = PasswordService.getInstance();
        this.vaultService = new VaultServiceImpl(new AESEncryptionService());

        // Initialize with master key and handle any storage errors
        try {
            this.passwordService.setMasterKey("demo_master_key_123".toCharArray());
            System.out.println("Encrypted storage initialized successfully");
        } catch (CryptoException e) {
            System.err.println("Failed to initialize encrypted storage: " + e.getMessage());
            // Continue with in-memory operation for now
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeVault();
        setupSidebar();
        setupPasswordList();
        setupDetailView();
        setupSearchFilter();
    }

    /**
     * Initialize vault with categories and assign passwords to categories
     */
    private void initializeVault() {
        currentVault = vaultService.createVault("My Vault", "Aron Vane");

        // Assign passwords to categories
        var passwords = passwordService.getAllPasswords();
        if (!passwords.isEmpty() && passwords.size() >= 9) {
            passwords.get(0).setCategoryId("personal"); // Adobe Cloud
            passwords.get(1).setCategoryId("work");     // Airtable
            passwords.get(2).setCategoryId("work");     // Webflow
            passwords.get(3).setCategoryId("work");     // Framer
            passwords.get(4).setCategoryId("personal"); // Amazon
            passwords.get(5).setCategoryId("work");     // Google
            passwords.get(6).setCategoryId("personal"); // Apple ID
            passwords.get(7).setCategoryId("work");     // Superhuman
            passwords.get(8).setCategoryId("games");    // Instagram
        }

        filteredPasswords = new FilteredList<>(passwordService.getAllPasswords());
    }

    private void setupSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));

        // All passwords section
        VBox allPasswordsSection = createSidebarItem("All passwords", "folder-icon", "all", currentCategory.equals("all"));

        // Favourites section
        VBox favouritesSection = createSidebarItem("Favourites", "star-icon", "favourites", false);

        // Bin section
        VBox binSection = createSidebarItem("Bin", "bin-icon", "bin", false);

        // By type section
        Label byTypeLabel = new Label("By type");
        byTypeLabel.getStyleClass().add("sidebar-section-header");

        VBox websiteLoginSection = createSidebarItem("Website login", "globe-icon", "login", false);
        VBox cardsSection = createSidebarItem("Cards", "card-icon", "cards", false);
        VBox identitySection = createSidebarItem("Identity", "user-icon", "identity", false);
        VBox lockedNotesSection = createSidebarItem("Locked notes", "note-icon", "notes", false);

        // Folders section with category management
        HBox foldersHeader = new HBox(5);
        foldersHeader.setAlignment(Pos.CENTER_LEFT);
        Label foldersLabel = new Label("Folders");
        foldersLabel.getStyleClass().add("sidebar-section-header");
        Button addFolderBtn = new Button("+");
        addFolderBtn.getStyleClass().add("add-folder-btn");
        addFolderBtn.setOnAction(e -> showAddCategoryModal());
        foldersHeader.getChildren().addAll(foldersLabel, addFolderBtn);

        VBox personalFolder = createCategoryItem("Personal", "folder-personal-icon", "personal", currentCategory.equals("personal"));
        VBox workFolder = createCategoryItem("Work", "folder-work-icon", "work", currentCategory.equals("work"));
        VBox gamesFolder = createCategoryItem("Games", "folder-games-icon", "games", currentCategory.equals("games"));

        sidebar.getChildren().addAll(
            allPasswordsSection,
            favouritesSection,
            binSection,
            new Separator(),
            byTypeLabel,
            websiteLoginSection,
            cardsSection,
            identitySection,
            lockedNotesSection,
            new Separator(),
            foldersHeader,
            personalFolder,
            workFolder,
            gamesFolder
        );

        sidebarContainer.getChildren().clear();
        sidebarContainer.getChildren().add(sidebar);
    }

    private VBox createSidebarItem(String text, String iconClass, String category, boolean isSelected) {
        VBox item = new VBox();
        item.getStyleClass().add("sidebar-item");
        item.setUserData(category);

        if (isSelected) {
            item.getStyleClass().add("sidebar-item-selected");
            selectedCategoryItem = item;
        }

        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);

        Pane icon = new Pane();
        icon.getStyleClass().addAll("sidebar-icon", iconClass);
        icon.setPrefSize(16, 16);

        Label label = new Label(text);
        label.getStyleClass().add("sidebar-text");

        content.getChildren().addAll(icon, label);
        item.getChildren().add(content);

        // Add click handler for filtering
        item.setOnMouseClicked(e -> selectCategory(category, text, item));

        return item;
    }

    private VBox createCategoryItem(String text, String iconClass, String category, boolean isSelected) {
        VBox item = createSidebarItem(text, iconClass, category, isSelected);

        // Add context menu for category management
        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem deleteItem = new MenuItem("Delete");

        renameItem.setOnAction(e -> showRenameCategoryModal(category, text));
        deleteItem.setOnAction(e -> showDeleteCategoryModal(category, text));

        contextMenu.getItems().addAll(renameItem, deleteItem);
        item.setOnContextMenuRequested(e -> contextMenu.show(item, e.getScreenX(), e.getScreenY()));

        return item;
    }

    private void selectCategory(String categoryId, String categoryName, VBox item) {
        // Update selection
        if (selectedCategoryItem != null) {
            selectedCategoryItem.getStyleClass().remove("sidebar-item-selected");
        }
        selectedCategoryItem = item;
        item.getStyleClass().add("sidebar-item-selected");

        currentCategory = categoryId;
        sectionTitleLabel.setText(categoryName);

        // Filter passwords by category
        updatePasswordFilter();
        setupPasswordList();
    }

    private void updatePasswordFilter() {
        filteredPasswords.setPredicate(password -> {
            // Apply search filter
            String searchText = searchField.getText();
            boolean matchesSearch = searchText == null || searchText.trim().isEmpty() ||
                password.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                password.getUsername().toLowerCase().contains(searchText.toLowerCase()) ||
                password.getWebsite().toLowerCase().contains(searchText.toLowerCase());

            // Apply category filter
            boolean matchesCategory = currentCategory.equals("all") ||
                (password.getCategoryId() != null && password.getCategoryId().equals(currentCategory));

            return matchesSearch && matchesCategory;
        });
    }

    private void setupPasswordList() {
        VBox passwordList = new VBox(2);
        passwordList.getStyleClass().add("password-list");
        passwordList.setPadding(new Insets(10));

        for (Password password : filteredPasswords) {
            VBox passwordCard = createPasswordCard(password);
            passwordList.getChildren().add(passwordCard);
        }

        passwordListContainer.getChildren().clear();
        passwordListContainer.getChildren().add(passwordList);

        // Select first password by default
        if (!filteredPasswords.isEmpty()) {
            selectPassword(filteredPasswords.get(0));
        } else {
            detailContainer.setVisible(false);
        }
    }

    private VBox createPasswordCard(Password password) {
        VBox card = new VBox(5);
        card.getStyleClass().add("password-card");
        card.setPadding(new Insets(15));
        card.setOnMouseClicked(e -> {
            selectPassword(password);
            updateSelectedCard(card);
        });

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Service icon
        Circle icon = new Circle(16);
        icon.getStyleClass().addAll("service-icon", getServiceIconClass(password.getName()));

        VBox textContent = new VBox(3);
        Label nameLabel = new Label(password.getName());
        nameLabel.getStyleClass().add("password-name");

        Label usernameLabel = new Label(password.getUsername());
        usernameLabel.getStyleClass().add("password-username");

        textContent.getChildren().addAll(nameLabel, usernameLabel);

        // Copy button
        Button copyBtn = new Button();
        copyBtn.getStyleClass().add("copy-button");
        copyBtn.setText("📋");
        copyBtn.setOnAction(e -> {
            e.consume(); // Prevent card selection
            try {
                String decryptedPassword = passwordService.getDecryptedPassword(password);
                ClipboardUtil.copyPasswordWithTimeout(decryptedPassword, 30);
                showToast("Password copied to clipboard (will clear in 30s)", ToastUtil.Type.SUCCESS);
            } catch (CryptoException ex) {
                showToast("Failed to decrypt password", ToastUtil.Type.ERROR);
            }
        });

        header.getChildren().addAll(icon, textContent);

        HBox cardContent = new HBox();
        cardContent.getChildren().add(header);
        HBox.setHgrow(header, javafx.scene.layout.Priority.ALWAYS);
        cardContent.getChildren().add(copyBtn);

        card.getChildren().add(cardContent);
        return card;
    }

    private String getServiceIconClass(String serviceName) {
        String name = serviceName.toLowerCase();
        if (name.contains("adobe")) return "adobe";
        if (name.contains("airtable")) return "airtable";
        if (name.contains("webflow")) return "webflow";
        if (name.contains("framer")) return "framer";
        if (name.contains("amazon")) return "amazon";
        if (name.contains("google")) return "google";
        if (name.contains("apple")) return "apple";
        if (name.contains("superhuman")) return "superhuman";
        if (name.contains("instagram")) return "instagram";
        return "default";
    }

    private void updateSelectedCard(VBox newSelectedCard) {
        // Remove selection from previous card
        if (selectedPasswordCard != null) {
            selectedPasswordCard.getStyleClass().remove("password-card-selected");
        }

        // Add selection to new card
        selectedPasswordCard = newSelectedCard;
        selectedPasswordCard.getStyleClass().add("password-card-selected");
    }

    private void setupDetailView() {
        // Initially hide detail view
        detailContainer.setVisible(false);
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordFilter();
            setupPasswordList();
        });
    }

    private void selectPassword(Password password) {
        this.selectedPassword = password;
        showPasswordDetail(password);
    }

    private void showPasswordDetail(Password password) {
        detailContainer.setVisible(true);

        if (selectedPasswordName != null) {
            selectedPasswordName.setText("⭐ " + password.getName());
        }

        if (detailNameField != null) {
            detailNameField.setText(password.getName());
        }

        if (detailUsernameField != null) {
            detailUsernameField.setText(password.getUsername());
        }

        if (detailPasswordDisplay != null) {
            // Always show masked password initially
            detailPasswordDisplay.setText("••••••••••");
            isPasswordVisible = false;
            if (togglePasswordBtn != null) {
                togglePasswordBtn.setText("👁");
            }
        }

        if (detailWebsiteField != null) {
            detailWebsiteField.setText(password.getWebsite());
        }

        if (detailNotesField != null) {
            detailNotesField.setText(password.getNotes() != null ? password.getNotes() : "");
        }

        if (createdByLabel != null) {
            createdByLabel.setText("Created by " + password.getCreatedBy());
        }

        if (createdDateLabel != null) {
            createdDateLabel.setText("Created " + password.getCreatedDate());
        }

        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last update " + password.getLastUpdated());
        }

        // Reset edit mode when switching passwords
        if (isEditMode) {
            resetEditMode();
        }
    }

    /**
     * P4 - CRUD Operations: Create new password with encryption
     */
    @FXML
    private void handleCreateNew() {
        showCustomCreateModal();
    }

    /**
     * P4 - CRUD Operations: Update password
     */
    @FXML
    private void handleEdit() {
        if (selectedPassword == null) {
            showToast("No password selected", ToastUtil.Type.WARNING);
            return;
        }

        if (!isEditMode) {
            // Enable edit mode
            isEditMode = true;
            editBtn.setText("💾 Save");
            editBtn.getStyleClass().add("save-btn");

            detailNameField.setEditable(true);
            detailUsernameField.setEditable(true);
            detailPasswordDisplay.setEditable(true);
            detailWebsiteField.setEditable(true);
            detailNotesField.setEditable(true);

            // Show actual password for editing
            if (!isPasswordVisible) {
                handleTogglePassword();
            }

            showToast("Editing mode enabled - make your changes", ToastUtil.Type.INFO);

        } else {
            // Save changes
            try {
                selectedPassword.setName(detailNameField.getText());
                selectedPassword.setUsername(detailUsernameField.getText());
                selectedPassword.setWebsite(detailWebsiteField.getText());
                selectedPassword.setNotes(detailNotesField.getText());

                // Update password if changed
                String newPassword = detailPasswordDisplay.getText();
                if (!newPassword.equals("••••••••••") && !newPassword.isEmpty()) {
                    passwordService.updatePassword(selectedPassword, newPassword);
                } else {
                    passwordService.updatePassword(selectedPassword, null);
                }

                resetEditMode();
                updatePasswordFilter();
                setupPasswordList();
                showToast("Password updated successfully", ToastUtil.Type.SUCCESS);

            } catch (CryptoException e) {
                showToast("Failed to update password: " + e.getMessage(), ToastUtil.Type.ERROR);
            }
        }
    }

    private void resetEditMode() {
        isEditMode = false;
        editBtn.setText("✏ Edit");
        editBtn.getStyleClass().remove("save-btn");

        detailNameField.setEditable(false);
        detailUsernameField.setEditable(false);
        detailPasswordDisplay.setEditable(false);
        detailWebsiteField.setEditable(false);
        detailNotesField.setEditable(false);

        // Hide password after editing
        if (isPasswordVisible) {
            handleTogglePassword();
        }
    }

    /**
     * P4 - CRUD Operations: Duplicate password
     */
    @FXML
    private void handleDuplicate() {
        if (selectedPassword != null) {
            try {
                Password duplicate = passwordService.duplicatePassword(selectedPassword);
                updatePasswordFilter();
                setupPasswordList();
                selectPassword(duplicate);
                showToast("Password duplicated successfully", ToastUtil.Type.SUCCESS);
            } catch (Exception e) {
                showToast("Failed to duplicate password", ToastUtil.Type.ERROR);
            }
        } else {
            showToast("No password selected", ToastUtil.Type.WARNING);
        }
    }

    /**
     * P4 - CRUD Operations: Delete password
     */
    @FXML
    private void handleDelete() {
        if (selectedPassword != null) {
            showCustomDeleteModal(selectedPassword);
        } else {
            showToast("No password selected", ToastUtil.Type.WARNING);
        }
    }

    /**
     * P5 - Secure clipboard: Copy username
     */
    @FXML
    private void handleCopyUsername() {
        if (selectedPassword != null) {
            ClipboardUtil.copyUsername(selectedPassword.getUsername());
            showToast("Username copied to clipboard", ToastUtil.Type.SUCCESS);
        } else {
            showToast("No password selected", ToastUtil.Type.WARNING);
        }
    }

    /**
     * P5 - Secure clipboard: Copy password with auto-clear
     */
    @FXML
    private void handleCopyPassword() {
        if (selectedPassword != null) {
            try {
                String password = passwordService.getDecryptedPassword(selectedPassword);
                ClipboardUtil.copyPasswordWithTimeout(password, 30);
                showToast("Password copied (will clear in 30s)", ToastUtil.Type.SUCCESS);
            } catch (CryptoException e) {
                showToast("Failed to decrypt password", ToastUtil.Type.ERROR);
            }
        } else {
            showToast("No password selected", ToastUtil.Type.WARNING);
        }
    }

    /**
     * P5 - Password visibility toggle (eye button)
     */
    @FXML
    private void handleTogglePassword() {
        if (selectedPassword == null) {
            showToast("No password selected", ToastUtil.Type.WARNING);
            return;
        }

        try {
            if (!isPasswordVisible) {
                // Show password
                String decryptedPassword = passwordService.getDecryptedPassword(selectedPassword);
                detailPasswordDisplay.setText(decryptedPassword);
                togglePasswordBtn.setText("🙈");
                isPasswordVisible = true;
                showToast("Password is now visible", ToastUtil.Type.INFO);
            } else {
                // Hide password
                detailPasswordDisplay.setText("••••••••••");
                togglePasswordBtn.setText("👁");
                isPasswordVisible = false;
                showToast("Password is now hidden", ToastUtil.Type.INFO);
            }
        } catch (CryptoException e) {
            showToast("Failed to decrypt password", ToastUtil.Type.ERROR);
        }
    }

    /**
     * P5 - Generate a secure password and fill the password field
     */
    @FXML
    private void handleGeneratePassword() {
        if (detailPasswordDisplay != null) {
            String generated = generateSecurePassword();
            detailPasswordDisplay.setText(generated);
            isPasswordVisible = true;
            if (togglePasswordBtn != null) {
                togglePasswordBtn.setText("🙈");
            }
            showToast("Generated a secure password", ToastUtil.Type.SUCCESS);
        }
    }

    @FXML
    private void handleGoToPage() {
        if (selectedPassword != null && selectedPassword.getWebsite() != null && !selectedPassword.getWebsite().isEmpty()) {
            try {
                String url = selectedPassword.getWebsite();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                showToast("Opening " + selectedPassword.getWebsite(), ToastUtil.Type.INFO);
            } catch (Exception e) {
                showToast("Failed to open website", ToastUtil.Type.ERROR);
            }
        } else {
            showToast("No website URL available", ToastUtil.Type.WARNING);
        }
    }

    /**
     * Move selected password to Personal category.
     */
    @FXML
    private void handleMoveToPersonal() {
        moveSelectedPasswordToCategory("personal", "Personal");
    }

    /**
     * Move selected password to Work category.
     */
    @FXML
    private void handleMoveToWork() {
        moveSelectedPasswordToCategory("work", "Work");
    }

    /**
     * Move selected password to Games category.
     */
    @FXML
    private void handleMoveToGames() {
        moveSelectedPasswordToCategory("games", "Games");
    }

    /**
     * Helper to move selected password to a category and show toast.
     */
    private void moveSelectedPasswordToCategory(String categoryId, String categoryName) {
        if (selectedPassword != null) {
            selectedPassword.setCategoryId(categoryId);
            try {
                passwordService.updatePassword(selectedPassword, null);
                updatePasswordFilter();
                setupPasswordList();
                showToast("Moved to " + categoryName, ToastUtil.Type.SUCCESS);
            } catch (CryptoException e) {
                showToast("Failed to move password", ToastUtil.Type.ERROR);
            }
        } else {
            showToast("No password selected", ToastUtil.Type.WARNING);
        }
    }

    private void showToast(String message, ToastUtil.Type type) {
        try {
            Stage stage = (Stage) searchField.getScene().getWindow();
            ToastUtil.showToast(stage, message, type);
        } catch (Exception e) {
            System.out.println("Toast: " + message);
        }
    }

    // Custom modal for password creation
    private void showCustomCreateModal() {
        Dialog<Password> dialog = new Dialog<>();
        dialog.setTitle("Create New Password");
        dialog.setHeaderText("Enter password details");

        // Custom CSS for dialog
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            // CSS loading failed, continue without styling
        }

        // GridPane for input fields
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        TextField serviceNameField = new TextField();
        serviceNameField.setPromptText("Service name (e.g., Gmail, Facebook)");
        serviceNameField.setPrefWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or email");
        usernameField.setPrefWidth(300);

        HBox passwordRow = new HBox(10);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(220);
        Button generateBtn = new Button("🔄");
        generateBtn.getStyleClass().add("generate-btn");
        generateBtn.setOnAction(e -> passwordField.setText(generateSecurePassword()));
        passwordRow.getChildren().addAll(passwordField, generateBtn);

        TextField websiteField = new TextField();
        websiteField.setPromptText("Website URL (optional)");
        websiteField.setPrefWidth(300);

        TextArea notesField = new TextArea();
        notesField.setPromptText("Notes (optional)");
        notesField.setPrefRowCount(3);
        notesField.setPrefWidth(300);

        grid.add(new Label("Service Name:"), 0, 0);
        grid.add(serviceNameField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordRow, 1, 2);
        grid.add(new Label("Website:"), 0, 3);
        grid.add(websiteField, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Buttons
        ButtonType createButtonType = new ButtonType("Create Password", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

        // Style buttons
        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.getStyleClass().add("primary-button");

        // Convert the result to a Password object when the dialog is closed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType && !serviceNameField.getText().trim().isEmpty()) {
                return new Password(
                    serviceNameField.getText().trim(),
                    usernameField.getText().trim(),
                    passwordField.getText(),
                    websiteField.getText().trim(),
                    notesField.getText().trim(),
                    currentCategory.equals("all") ? "personal" : currentCategory
                );
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(password -> {
            try {
                passwordService.createPasswordEntry(password);
                updatePasswordFilter();
                setupPasswordList();
                selectPassword(password);
                showToast("Password created successfully", ToastUtil.Type.SUCCESS);
            } catch (CryptoException e) {
                showToast("Failed to create password: " + e.getMessage(), ToastUtil.Type.ERROR);
            }
        });
    }

    // Custom modal for password deletion
    private void showCustomDeleteModal(Password password) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Password");
        dialog.setHeaderText("Are you sure you want to delete this password?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label nameLabel = new Label("Password: " + password.getName());
        nameLabel.getStyleClass().add("delete-item-name");

        Label warningLabel = new Label("⚠️ This action cannot be undone!");
        warningLabel.getStyleClass().add("warning-text");

        content.getChildren().addAll(nameLabel, warningLabel);
        dialog.getDialogPane().setContent(content);

        // Custom CSS for dialog
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            // CSS loading failed, continue without styling
        }

        // Buttons
        ButtonType deleteButtonType = new ButtonType("Delete Password", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        // Style delete button as danger
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("danger-button");

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                passwordService.deletePassword(password);
                detailContainer.setVisible(false);
                selectedPassword = null;
                selectedPasswordCard = null;
                updatePasswordFilter();
                setupPasswordList();
                showToast("Password deleted successfully", ToastUtil.Type.SUCCESS);
            }
        });
    }

    // Modal for adding a new category (folder)
    private void showAddCategoryModal() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create a new category to organize your passwords");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField categoryNameField = new TextField();
        categoryNameField.setPromptText("Category name (e.g., Social Media, Banking)");
        categoryNameField.setPrefWidth(300);

        content.getChildren().add(categoryNameField);
        dialog.getDialogPane().setContent(content);

        // Custom CSS for dialog
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            // CSS loading failed, continue without styling
        }

        // Buttons
        ButtonType createButtonType = new ButtonType("Create Category", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.getStyleClass().add("primary-button");

        // Show the dialog and handle the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType && categoryNameField.getText() != null) {
                return categoryNameField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                String categoryId = name.toLowerCase().replaceAll("\\s+", "_");
                Category newCategory = new Category(categoryId, name, "folder-personal-icon");
                try {
                    passwordService.createCategory(newCategory);
                    showToast("Category '" + name + "' created successfully", ToastUtil.Type.SUCCESS);
                    setupSidebar();
                } catch (Exception e) {
                    showToast("Failed to create category", ToastUtil.Type.ERROR);
                }
            }
        });
    }

    // Modal for renaming a category
    private void showRenameCategoryModal(String categoryId, String currentName) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Rename Category");
        dialog.setHeaderText("Enter new name for category: " + currentName);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField categoryNameField = new TextField(currentName);
        categoryNameField.setPromptText("Category name");
        categoryNameField.setPrefWidth(300);
        categoryNameField.selectAll();

        content.getChildren().add(categoryNameField);
        dialog.getDialogPane().setContent(content);

        // Custom CSS for dialog
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            // CSS loading failed, continue without styling
        }

        // Buttons
        ButtonType renameButtonType = new ButtonType("Rename", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(renameButtonType, cancelButtonType);

        Button renameButton = (Button) dialog.getDialogPane().lookupButton(renameButtonType);
        renameButton.getStyleClass().add("primary-button");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == renameButtonType && categoryNameField.getText() != null) {
                return categoryNameField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty() && !newName.equals(currentName)) {
                try {
                    passwordService.renameCategory(categoryId, newName);
                    showToast("Category renamed to '" + newName + "'", ToastUtil.Type.SUCCESS);
                    setupSidebar();
                } catch (Exception e) {
                    showToast("Failed to rename category", ToastUtil.Type.ERROR);
                }
            }
        });
    }

    // Modal for deleting a category
    private void showDeleteCategoryModal(String categoryId, String displayName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Category");
        dialog.setHeaderText("Are you sure you want to delete this category?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label nameLabel = new Label("Category: " + displayName);
        nameLabel.getStyleClass().add("delete-item-name");

        Label infoLabel = new Label("Passwords in this category will be moved to 'Personal'");
        infoLabel.getStyleClass().add("info-text");

        Label warningLabel = new Label("⚠️ This action cannot be undone!");
        warningLabel.getStyleClass().add("warning-text");

        content.getChildren().addAll(nameLabel, infoLabel, warningLabel);
        dialog.getDialogPane().setContent(content);

        // Custom CSS for dialog
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            // CSS loading failed, continue without styling
        }

        // Buttons
        ButtonType deleteButtonType = new ButtonType("Delete Category", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("danger-button");

        dialog.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                try {
                    passwordService.deleteCategory(categoryId);
                    showToast("Category '" + displayName + "' deleted", ToastUtil.Type.SUCCESS);
                    currentCategory = "all";
                    setupSidebar();
                    updatePasswordFilter();
                    setupPasswordList();
                } catch (Exception e) {
                    showToast("Failed to delete category", ToastUtil.Type.ERROR);
                }
            }
        });
    }

    /**
     * Generate a secure password with improved randomness
     */
    private String generateSecurePassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = uppercase + lowercase + numbers + symbols;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        // Fill remaining length with random characters
        for (int i = 4; i < 16; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        for (int i = password.length() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(j));
            password.setCharAt(j, temp);
        }

        return password.toString();
    }
}
