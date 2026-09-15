import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class AutoSave {

    private boolean unsavedChanges;
    private File currentFile;

    private Timeline autoSaveTimer;

    private Runnable saveFunction;

    public AutoSave(Runnable saveFunction) {
        unsavedChanges = false;
        currentFile = null;
        this.saveFunction = saveFunction;
    }

    public void markUnsaved() {
        unsavedChanges = true;
    }

    public void markSaved() {
        unsavedChanges = false;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void setCurrentFile(File file) {
        currentFile = file;
    }

    public File getCurrentFile() {
        return currentFile;
    }

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

    public void stopAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
    }

    private void performAutoSave() {
        if (!unsavedChanges) {
            return;
        }
        if (currentFile == null) {
            return;
        }

        saveFunction.run();
    }

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