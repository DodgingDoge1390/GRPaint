import java.io.File;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Handles automatic saving
 * Tracks unsaved changes
 * Confirms whether the user wants to save changes
 * Before closing the application
 */
public class AutoSave {
    private boolean unsavedChanges;
    private File currentFile;
    private Timeline countdownTimer;
    private Runnable saveFunction;
    private Runnable manualSaveFunction;
    private boolean enabled = false;
    private boolean showCountdown = false;
    private int secondsRemaining = 30;
    private Label countdownLabel;
    private Runnable notificationFunction = () -> {};
    private boolean warnedMissingFile = false;

    /**
     * Constructs an AutoSave object.
     * @param saveFunction the function used to save the current image
     */
    public AutoSave(Runnable saveFunction) {
        this.saveFunction = saveFunction;
    }

    /**
     * Sets the separate Save action used by the close confirmation.
     * This allows new tabs to use Save As instead of skipping the save.
     * @param manualSaveFunction the normal save action
     */
    public void setManualSaveFunction(Runnable manualSaveFunction) {
        this.manualSaveFunction = manualSaveFunction;
    }

    /**
     * Public method that sets the label used to show the timer countdown.
     * @param label the label displayed to the user
     */
    public void setCountdownLabel(Label label) {
        countdownLabel = label;
        updateCountdownLabel();
    }

    /**
     * Public method that sets the function used to display an autosave notification.
     * @param notificationFunction the function used to show a notification
     */
    public void setNotificationFunction(Runnable notificationFunction) {
        this.notificationFunction = notificationFunction == null ? () -> {} : notificationFunction;
    }

    /**
     * Public method that turns automatic saving on or off.
     * @param enabled true to turn autosave on, false to turn it off
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            warnedMissingFile = false;
            resetCountdown();
            startAutoSave();
        } else {
            stopAutoSave();
            updateCountdownLabel();
        }
    }

    /**
     * Public method that checks whether automatic saving is enabled.
     * @return true if autosave is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Public method that chooses whether the countdown is shown to the user.
     * @param visible true to show the countdown, false to hide it
     */
    public void setCountdownVisible(boolean visible) {
        showCountdown = visible;
        updateCountdownLabel();
    }

    /**
     * Public method that checks whether the countdown should be shown.
     * @return true if the countdown is visible
     */
    public boolean isCountdownVisible() {
        return showCountdown;
    }

    /**
     * Marks the current document as having unsaved changes
     */
    public void markUnsaved() {
        unsavedChanges = true;
    }

    /**
     * Marks the current document as saved
     */
    public void markSaved() {
        unsavedChanges = false;
        resetCountdown();
    }

    /**
     * Checks whether the current document has unsaved changes.
     * @return true if there are unsaved changes, otherwise false
     */
    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    /**
     * Sets the current file being edited.
     * @param file the file to set as the current file
     */
    public void setCurrentFile(File file) {
        currentFile = file;
        warnedMissingFile = false;
    }

    /**
     * Gets the current file being edited
     * @return the current file
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Starts the automatic save timer.
     * The timer runs every 30 seconds and checks if the image needs saving.
     */
    public void startAutoSave() {
        if (!enabled) {
            updateCountdownLabel();
            return;
        }
        stopTimers();
        secondsRemaining = 30;
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateCountdownLabel();
            if (secondsRemaining <= 0) {
                performAutoSave();
                secondsRemaining = 30;
                updateCountdownLabel();
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    /**
     * Stops the autosave timer and the countdown timer if they are running.
     */
    public void stopAutoSave() {
        stopTimers();
        updateCountdownLabel();
    }

    /**
     * Private method that stops both timer timelines.
     */
    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
    }

    /**
     * Public method that resets the timer countdown after a save.
     */
    public void resetCountdown() {
        secondsRemaining = 30;
        updateCountdownLabel();
    }

    /**
     * Private method that updates the countdown label when it is visible.
     */
    private void updateCountdownLabel() {
        if (countdownLabel == null) return;
        Platform.runLater(() -> {
            countdownLabel.setVisible(showCountdown);
            countdownLabel.setManaged(showCountdown);
            if (!enabled) {
                countdownLabel.setText("Autosave: Off");
            } else {
                countdownLabel.setText("Autosave in: " + secondsRemaining + " seconds");
            }
        });
    }

    /**
     * Performs an automatic save if there are unsaved changes and a current file has been selected.
     */
    private void performAutoSave() {
        if (!enabled || !unsavedChanges) return;
        if (currentFile == null) {
            // Notify once instead of spamming the OS every 30 seconds.
            if (!warnedMissingFile) {
                notificationFunction.run();
                warnedMissingFile = true;
            }
            return;
        }
        saveFunction.run();
    }

    /**
     * Displays a warning asking the user whether they want to save changes before closing the application.
     * @param stage the current application stage
     * @return true if the application can close, otherwise false
     */
    public boolean confirmClose(Stage stage) {
        if (!unsavedChanges) return true;

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Would you like to save your changes before closing?");
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType dontSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == saveButton) {
            if (manualSaveFunction != null) manualSaveFunction.run();
            else saveFunction.run();
            return !unsavedChanges;
        }
        if (result.isPresent() && result.get() == dontSaveButton) return true;
        return false;
    }
}
