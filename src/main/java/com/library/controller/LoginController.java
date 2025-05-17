package com.library.controller;

import com.library.model.Student;
import com.library.model.StudentProfile;
import com.library.model.User;
import com.library.service.AuthService;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.scene.control.SelectionMode;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button registerButton;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private VBox loadingOverlay;
    @FXML
    private Label loadingText;

    private AuthService authService = new AuthService();
    private StudentService studentService = new StudentService();
    private static final String PREF_NODE = "com.library.login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL_HISTORY = "emailHistory";
    private static final int MAX_HISTORY_SIZE = 5;
    private List<String> emailHistory;

    private ListView<String> suggestionsListView;
    private Popup suggestionsPopup;
    private int selectedIndex = -1;

    @FXML
    public void initialize() {
        // Add items to the role combo box
        roleComboBox.getItems().addAll("Admin", "Student");
        roleComboBox.setValue("Admin"); // Set default value

        // Hide register button since registration will be admin-only
        registerButton.setVisible(false);
        registerButton.setManaged(false);

        // Initialize loading overlay
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);

        // Initialize email history
        loadEmailHistory();

        // Set up email field autocomplete
        setupEmailAutocomplete();

        loadSavedCredentials();
    }

    private void loadEmailHistory() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String historyString = prefs.get(KEY_EMAIL_HISTORY, "");
        emailHistory = new ArrayList<>();

        if (!historyString.isEmpty()) {
            String[] emails = historyString.split(",");
            for (String email : emails) {
                if (!email.isEmpty() && !emailHistory.contains(email)) {
                    emailHistory.add(email);
                    if (emailHistory.size() >= MAX_HISTORY_SIZE) {
                        break;
                    }
                }
            }
        }
    }

    private void saveEmailHistory() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String historyString = String.join(",", emailHistory);
        prefs.put(KEY_EMAIL_HISTORY, historyString);
    }

    /**
     * Set up the email field autocomplete.
     */
    private void setupEmailAutocomplete() {
        emailField.setOnKeyPressed(this::handleEmailKeyPress);

        // Add focus listener to hide suggestions when field loses focus
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // Small delay to allow for mouse clicks on the suggestions
                new Thread(() -> {
                    try {
                        Thread.sleep(100); // Reduced delay
                        javafx.application.Platform.runLater(this::hideEmailSuggestions);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        // Add listener for text changes
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && emailField.getScene() != null) {
                String input = newValue.toLowerCase();
                List<String> suggestions = new ArrayList<>();

                // Find emails that start with the user's input
                for (String email : emailHistory) {
                    if (email.toLowerCase().startsWith(input)) {
                        suggestions.add(email);
                    }
                }

                if (!suggestions.isEmpty()) {
                    showEmailSuggestions(suggestions);
                } else {
                    hideEmailSuggestions();
                }
            } else {
                hideEmailSuggestions();
            }
        });
    }

    private void handleEmailKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN:
                selectNextSuggestion();
                event.consume();
                break;
            case UP:
                selectPreviousSuggestion();
                event.consume();
                break;
            case ENTER:
                applySelectedSuggestion();
                event.consume();
                break;
            case ESCAPE:
                hideEmailSuggestions();
                event.consume();
                break;
        }
    }

    /**
     * Shows the email suggestions list popup below the email field with the
     * given list of suggestions. If the popup is already showing, it will be
     * updated with the new suggestions. The popup is only shown if the scene is
     * available.
     * 
     * @param suggestions The list of email suggestions to show
     */
    private void showEmailSuggestions(List<String> suggestions) {
        if (suggestionsPopup == null) {
            suggestionsPopup = new Popup();
            suggestionsListView = new ListView<>();
            suggestionsListView.setPrefWidth(emailField.getWidth());
            suggestionsListView.setMaxHeight(50);
            suggestionsListView.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #E9ECEF;" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;" +
                            "-fx-padding: 0;" + // Removed padding
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");

            // Style the list cells
            suggestionsListView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(item);
                        setStyle(
                                "-fx-padding: 8 12;" + // Adjusted padding
                                        "-fx-background-radius: 0;" + // Removed cell radius
                                        "-fx-font-size: 13px;" +
                                        "-fx-background-color: transparent;" // Transparent background
                        );
                    }
                }
            });

            // Add hover effect to cells
            suggestionsListView.setOnMouseEntered(e -> {
                suggestionsListView.getSelectionModel().clearSelection();
            });

            suggestionsListView.setOnMouseMoved(e -> {
                int index = suggestionsListView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    suggestionsListView.getSelectionModel().select(index);
                }
            });

            suggestionsPopup.getContent().add(suggestionsListView);
            suggestionsPopup.setAutoHide(true);

            // Initialize selection model
            suggestionsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // Add selection listener
            suggestionsListView.setOnMouseClicked(event -> {
                String selected = suggestionsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    emailField.setText(selected);
                    hideEmailSuggestions();
                }
            });
        }

        suggestionsListView.getItems().setAll(suggestions);
        selectedIndex = -1;

        // Only show popup if the scene is available
        if (emailField.getScene() != null && emailField.getScene().getWindow() != null) {
            // Position the popup below the email field
            double x = emailField.localToScreen(0, 0).getX();
            double y = emailField.localToScreen(0, 0).getY() + emailField.getHeight() + 1; // Reduced gap

            // Update width to match the email field
            suggestionsListView.setPrefWidth(emailField.getWidth());

            suggestionsPopup.show(emailField.getScene().getWindow(), x, y);
        }
    }

    private void hideEmailSuggestions() {
        if (suggestionsPopup != null) {
            suggestionsPopup.hide();
        }
    }

    private void selectNextSuggestion() {
        if (suggestionsPopup != null && suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
            int maxIndex = suggestionsListView.getItems().size() - 1;
            selectedIndex = Math.min(selectedIndex + 1, maxIndex);
            suggestionsListView.getSelectionModel().select(selectedIndex);
            suggestionsListView.scrollTo(selectedIndex);
        }
    }

    private void selectPreviousSuggestion() {
        if (suggestionsPopup != null && suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
            selectedIndex = Math.max(selectedIndex - 1, 0);
            suggestionsListView.getSelectionModel().select(selectedIndex);
            suggestionsListView.scrollTo(selectedIndex);
        }
    }

    private void applySelectedSuggestion() {
        if (suggestionsPopup != null && suggestionsPopup.isShowing() && !suggestionsListView.getItems().isEmpty()) {
            String selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                emailField.setText(selected);
                hideEmailSuggestions();
            }
        }
    }

    private void addToEmailHistory(String email) {
        if (email != null && !email.isEmpty()) {
            emailHistory.remove(email); // Remove if exists
            emailHistory.add(0, email); // Add to beginning
            if (emailHistory.size() > MAX_HISTORY_SIZE) {
                emailHistory = emailHistory.subList(0, MAX_HISTORY_SIZE);
            }
            saveEmailHistory();
        }
    }

    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String savedEmail = prefs.get(KEY_EMAIL, "");
        String savedRole = prefs.get(KEY_ROLE, "");

        if (!savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            rememberMeCheckbox.setSelected(true);

            if (!savedRole.isEmpty()) {
                roleComboBox.setValue(savedRole);
            }
        }
    }

    private void saveCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        if (rememberMeCheckbox.isSelected()) {
            prefs.put(KEY_EMAIL, emailField.getText());
            prefs.put(KEY_ROLE, roleComboBox.getValue());
        } else {
            prefs.remove(KEY_EMAIL);
            prefs.remove(KEY_ROLE);
        }
    }

    private void showLoadingOverlay(String message) {
        loadingText.setText(message);
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loadingOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideLoadingOverlay() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loadingOverlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        });
        fadeOut.play();
    }

    /**
     * Handles the login button click event.
     * <p>
     * First, it checks if the email and password fields are empty. If either is
     * empty,
     * it shows an error alert and returns.
     * <p>
     * Then, it adds the email to the email history on a successful login.
     * <p>
     * After that, it shows a loading overlay and simulates a network delay (1
     * second).
     * <p>
     * Finally, it attempts to log in the user using the provided credentials.
     * If the login is successful, it saves the credentials if the remember me
     * checkbox
     * is checked, and loads the appropriate dashboard based on the role. If the
     * login
     * fails, it shows an error alert.
     * 
     * @param event The login button click event.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue().toLowerCase();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        // Add email to history on successful login
        addToEmailHistory(email);

        showLoadingOverlay("Logging in...");

        // Simulate network delay (remove in production)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate 1 second delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        User user = authService.login(email, password, role);
                        if (user != null) {
                            // Save credentials if remember me is checked
                            if (rememberMeCheckbox.isSelected()) {
                                Preferences prefs = Preferences.userRoot().node(PREF_NODE);
                                prefs.put(KEY_EMAIL, email);
                                prefs.put(KEY_ROLE, user.getRole());
                                prefs.putBoolean("remember", true);
                            }

                            // Load appropriate dashboard based on role
                            String fxmlPath = user.getRole().equalsIgnoreCase("admin") ? "/fxml/main.fxml"
                                    : "/fxml/student_dashboard.fxml";
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                            Parent root = loader.load();

                            if (user.getRole().equalsIgnoreCase("admin")) {
                                MainController controller = loader.getController();
                                controller.setUser(user);
                            } else {
                                StudentDashboardController controller = loader.getController();
                                controller.setUser(user);
                            }

                            Stage stage = (Stage) emailField.getScene().getWindow();
                            Scene scene = new Scene(root);
                            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                            // Fade out current scene
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300),
                                    stage.getScene().getRoot());
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);

                            // Fade in new scene
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);

                            // Play transitions
                            SequentialTransition transition = new SequentialTransition(fadeOut, fadeIn);
                            transition.setOnFinished(e -> {
                                stage.setTitle("Library Management System - " + user.getRole());
                                stage.setScene(scene);
                                stage.show();
                            });
                            transition.play();
                        } else {
                            hideLoadingOverlay();
                            showAlert("Error", "Invalid email or password.");
                        }
                    } catch (Exception e) {
                        hideLoadingOverlay();
                        e.printStackTrace();
                        showAlert("Error", "Login failed: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // We are not using this currently
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_registration.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Fade out current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Fade in new scene
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Play transitions
            SequentialTransition transition = new SequentialTransition(fadeOut, fadeIn);
            transition.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("Student Registration");
                stage.show();
            });
            transition.play();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load registration screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address!");
            return;
        }

        showLoadingOverlay("Sending reset instructions...");

        // Simulate network delay (remove in production)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate 1 second delay
                javafx.application.Platform.runLater(() -> {
                    if (authService.sendPasswordResetEmail(email)) {
                        hideLoadingOverlay();
                        showAlert("Success", "Password reset instructions have been sent to your email.");
                    } else {
                        hideLoadingOverlay();
                        showAlert("Error", "Failed to send password reset email. Please try again.");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}