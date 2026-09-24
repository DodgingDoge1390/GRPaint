import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Handles automatic saving, tracking unsaved changes,
 * and confirming whether the user wants to save changes
 * before closing the application.
 */
public class AutoSave {

    private boolean unsavedChanges;
    private File currentFile;
    private Timeline autoSaveTimer;
    private Runnable saveFunction;

    /**
     * Constructs an AutoSave object.
     *
     * @param saveFunction the function used to save the current image
     */
    public AutoSave(Runnable saveFunction) {
        unsavedChanges = false;
        currentFile = null;
        this.saveFunction = saveFunction;
    }

    /**
     * Marks the current document as having unsaved changes.
     */
    public void markUnsaved() {
        unsavedChanges = true;
    }

    /**
     * Marks the current document as saved.
     */
    public void markSaved() {
        unsavedChanges = false;
    }

    /**
     * Checks whether the current document has unsaved changes.
     *
     * @return true if there are unsaved changes, otherwise false
     */
    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    /**
     * Sets the current file being edited.
     *
     * @param file the file to set as the current file
     */
    public void setCurrentFile(File file) {
        currentFile = file;
    }

    /**
     * Gets the current file being edited.
     *
     * @return the current file
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Starts the automatic save timer.
     *
     * <p>The timer runs every 30 seconds and attempts
     * to save the current file if it has unsaved changes.</p>
     */
    public void startAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }

        autoSaveTimer = new Timeline(
            new KeyFrame(
                Duration.seconds(30),
                e -> performAutoSave()
            )
        );

        autoSaveTimer.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimer.play();
    }

    /**
     * Stops the automatic save timer if it is currently running.
     */
    public void stopAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
    }

    /**
     * Performs an automatic save if there are unsaved changes
     * and a current file has been selected.
     */
    private void performAutoSave() {
        if (!unsavedChanges) {
            return;
        }

        if (currentFile == null) {
            return;
        }

        saveFunction.run();
    }

    /**
     * Displays a warning asking the user whether they want
     * to save changes before closing the application.
     *
     * @param stage the current application stage
     * @return true if the application can close, otherwise false
     */
    public boolean confirmClose(Stage stage) {
        if (!unsavedChanges) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");

        alert.setContentText(
            "Would you like to save your changes before closing?"
        );

        ButtonType saveButton = new ButtonType(
            "Save",
            ButtonBar.ButtonData.YES
        );

        ButtonType dontSaveButton = new ButtonType(
            "Don't Save",
            ButtonBar.ButtonData.NO
        );

        ButtonType cancelButton = new ButtonType(
            "Cancel",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert.getButtonTypes().setAll(
            saveButton,
            dontSaveButton,
            cancelButton
        );

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {
            saveFunction.run();
            return !unsavedChanges;
        }

        if (result.isPresent() && result.get() == dontSaveButton) {
            return true;
        }

        return false;
    }
}