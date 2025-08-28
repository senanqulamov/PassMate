package com.passmate.controllers;

import com.passmate.controllers.components.CategoryListController;
import com.passmate.controllers.components.PasswordDetailController;
import com.passmate.controllers.components.PasswordEditorController;
import com.passmate.controllers.components.PasswordListController;
import com.passmate.models.Category;
import com.passmate.models.Password;
import com.passmate.services.PasswordService;
import com.passmate.services.VaultService;
import com.passmate.utils.ClipboardUtil;
import com.passmate.utils.ToastUtil;
import com.passmate.utils.WindowsProfileUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainController {

    @FXML private StackPane rightStack; // For toasts

    // Included component controllers via fx:id + 'Controller' convention
    @FXML private com.passmate.controllers.components.SidebarController sidebarController; // from fx:id="sidebar"
    @FXML private PasswordListController passwordListController; // from fx:id="passwordList"
    @FXML private PasswordDetailController passwordDetailController; // from fx:id="passwordDetail"
    @FXML private PasswordEditorController passwordEditorController; // from fx:id="passwordEditor"

    private VaultService vaultService;
    private PasswordService passwordService;

    private Category activeCategory; // null means "All passwords"
    private Password selectedPassword;
    private String searchQuery = "";

    public void initialize() { }

    /** DI entry point from App.java */
    public void initData(VaultService vaultService, PasswordService passwordService) {
        this.vaultService = vaultService;
        this.passwordService = passwordService;

        seedDefaultCategories();
        setupSidebar();
        setupPasswordList();
        setupDetail();
        setupEditor();

        // Default selection: All Passwords (activeCategory = null)
        activeCategory = null;
        refreshAll();
    }

    private void seedDefaultCategories() {
        if (vaultService.getCategories().isEmpty()) {
            for (String name : List.of("Personal", "Work", "School")) {
                if (!vaultService.existsByNameIgnoreCase(name)) {
                    try { vaultService.createCategory(name); } catch (Exception ignored) {}
                }
            }
        }
    }

    private void setupSidebar() {
        if (sidebarController == null) return;
        // Set user info
        sidebarController.setUsername(com.passmate.utils.WindowsProfileUtil.getUsername());
        sidebarController.setProfileImage(com.passmate.utils.WindowsProfileUtil.getUserImage());
        // Existing SidebarController exposes CategoryListController + add input
        CategoryListController cl = sidebarController.getCategoryListController();
        if (cl != null) {
            cl.setHandlers(
                    this::onSelectCategory,
                    (cat, newName) -> { vaultService.renameCategory(cat.getId(), newName); refreshCategories(cat.getId()); },
                    cat -> { vaultService.deleteCategory(cat.getId()); if (activeCategory != null && activeCategory.getId().equals(cat.getId())) activeCategory = null; refreshAll(); ToastUtil.show(rightStack, "Folder deleted", ToastUtil.Type.SUCCESS); }
            );
        }
        if (sidebarController.getAddCategoryBtn() != null) {
            sidebarController.getAddCategoryBtn().setOnAction(e -> {
                String name = sidebarController.getCategoryInput().getText();
                try {
                    if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
                    var created = vaultService.createCategory(name.trim());
                    sidebarController.getCategoryInput().clear();
                    refreshCategories(created.getId());
                    ToastUtil.show(rightStack, "Folder created", ToastUtil.Type.SUCCESS);
                } catch (IllegalArgumentException ex) {
                    ToastUtil.show(rightStack, "Invalid name", ToastUtil.Type.ERROR);
                } catch (Exception ex) {
                    ToastUtil.show(rightStack, "Folder exists", ToastUtil.Type.ERROR);
                }
            });
        }
        refreshCategories(activeCategory == null ? null : activeCategory.getId());
    }

    private void setupPasswordList() {
        if (passwordListController == null) return;
        passwordListController.setHandlers(
                q -> { searchQuery = q == null ? "" : q; refreshList(); },
                this::onSelectPassword,
                this::onCreateRequested,
                this::onEditRequested,
                this::onDuplicateRequested,
                this::onDeleteRequested
        );
    }

    private void setupDetail() {
        if (passwordDetailController == null) return;
        passwordDetailController.setHandlers(
                (p, mk) -> {
                    try {
                        String plain = passwordService.decrypt(p, mk);
                        ClipboardUtil.copyAndClear(plain, 12_000);
                        ToastUtil.show(rightStack, "Password copied", ToastUtil.Type.SUCCESS);
                    } catch (Exception ex) {
                        ToastUtil.show(rightStack, "Wrong master key", ToastUtil.Type.ERROR);
                    } finally {
                        if (mk != null) java.util.Arrays.fill(mk, '\0');
                    }
                },
                this::onEditRequested,
                this::onDuplicateRequested,
                this::onDeleteRequested
        );
    }

    private void setupEditor() {
        if (passwordEditorController == null) return;
        passwordEditorController.setHandlers(
                (title, user, plain) -> {
                    try {
                        UUID catId = activeCategory == null ? getFallbackCategoryId() : activeCategory.getId();
                        var entry = passwordService.create(title, user, plain, catId, passwordEditorControllerMasterKey());
                        ToastUtil.show(rightStack, "Password added", ToastUtil.Type.SUCCESS);
                        // Select new
                        selectedPassword = entry;
                        passwordEditorController.setVisible(false);
                        refreshAll();
                    } catch (Exception ex) {
                        ToastUtil.show(rightStack, "Failed to save password", ToastUtil.Type.ERROR);
                    }
                },
                (model, title, user, newPlain) -> {
                    try {
                        var updated = passwordService.update(model, title, user, newPlain, passwordEditorControllerMasterKey());
                        ToastUtil.show(rightStack, "Saved", ToastUtil.Type.SUCCESS);
                        selectedPassword = updated;
                        passwordEditorController.setVisible(false);
                        refreshAll();
                    } catch (Exception ex) {
                        ToastUtil.show(rightStack, "Failed to save", ToastUtil.Type.ERROR);
                    }
                },
                () -> {
                    passwordEditorController.setVisible(false);
                    refreshRight();
                }
        );
    }

    private char[] passwordEditorControllerMasterKey() {
        // Read from the controller's masterKey field by reflection-safe approach: expose a method would be ideal.
        try {
            var f = PasswordEditorController.class.getDeclaredField("masterKeyField");
            f.setAccessible(true);
            javafx.scene.control.PasswordField pf = (javafx.scene.control.PasswordField) f.get(passwordEditorController);
            String v = pf.getText();
            return v == null ? new char[0] : v.toCharArray();
        } catch (Exception ignored) {
            return new char[0];
        }
    }

    private UUID getFallbackCategoryId() {
        var cats = vaultService.getCategories();
        if (!cats.isEmpty()) return cats.get(0).getId();
        // Create a default category if none exist
        var created = vaultService.createCategory("Personal");
        return created.getId();
    }

    // Handlers
    private void onSelectCategory(Category c) {
        activeCategory = c; selectedPassword = null; refreshAll();
    }
    private void onSelectPassword(Password p) {
        selectedPassword = p; refreshRight();
    }
    private void onCreateRequested() {
        if (passwordEditorController != null) {
            passwordEditorController.editNew();
            refreshRight();
        }
    }
    private void onEditRequested(Password p) {
        if (passwordEditorController != null) {
            passwordEditorController.editExisting(p);
            refreshRight();
        }
    }
    private void onDuplicateRequested(Password p) {
        if (p == null) return;
        // Duplicate by re-adding with a new id but same encrypted payload
        Password clone = new Password(p.getTitle() + " (copy)", p.getUsername(), p.getPasswordHash(), p.getCategoryId());
        vaultService.addPassword(clone);
        ToastUtil.show(rightStack, "Duplicated", ToastUtil.Type.SUCCESS);
        selectedPassword = clone;
        refreshAll();
    }
    private void onDeleteRequested(Password p) {
        if (p == null) return;
        passwordService.delete(p.getId());
        ToastUtil.show(rightStack, "Deleted", ToastUtil.Type.SUCCESS);
        if (selectedPassword != null && selectedPassword.getId().equals(p.getId())) selectedPassword = null;
        refreshAll();
    }

    // Refresh helpers
    private void refreshAll() { refreshCategories(activeCategory == null ? null : activeCategory.getId()); refreshList(); refreshRight(); }

    private void refreshCategories(UUID selectedId) {
        if (sidebarController == null) return;
        var cl = sidebarController.getCategoryListController();
        if (cl != null) cl.setCategories(vaultService.getCategories(), selectedId);
    }

    private void refreshList() {
        if (passwordListController == null) return;
        List<Password> source = new ArrayList<>();
        if (activeCategory == null) {
            source.addAll(vaultService.getCategories().stream()
                    .flatMap(c -> vaultService.getPasswordsByCategory(c.getId()).stream())
                    .collect(Collectors.toList()));
        } else {
            source.addAll(passwordService.listByCategory(activeCategory.getId()));
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            String q = searchQuery.toLowerCase();
            source = source.stream().filter(p ->
                    (p.getTitle() != null && p.getTitle().toLowerCase().contains(q)) ||
                    (p.getUsername() != null && p.getUsername().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }
        UUID selectedId = selectedPassword == null ? null : selectedPassword.getId();
        passwordListController.setItems(source, selectedId);
    }

    private void refreshRight() {
        if (passwordEditorController != null) {
            // If editor is visible, keep it on top; else show detail
            // Ensure detail visible when editor hidden
            boolean editorVisible = ((javafx.scene.layout.Region) rightStack.getChildren().get(1)).isVisible();
            if (!editorVisible) {
                passwordDetailController.setModel(selectedPassword);
            }
        }
    }
}
