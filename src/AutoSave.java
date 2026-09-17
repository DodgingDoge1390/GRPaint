import javafx.animation.Timeline; //JavaFX library for Timeline for the time for AutoSave
import javafx.animation.KeyFrame; //JavaFX library for Keyframe for the what happens during the timeline
import javafx.util.Duration; //JavaFX library for time of the AutoSave
import javafx.scene.control.Alert; //JavaFX library for alerts and warnings
import javafx.scene.control.ButtonType; //JavaFX library for ButtonType for the alert
import javafx.scene.control.ButtonBar; //JavaFX library for how to organize the buttons
import javafx.stage.Stage; //JavaFX library for the stage

import java.io.File; //Java library input output for File
import java.util.Optional; //Java library for optional button

public class AutoSave { //public class for Autosave
    private boolean unsavedChanges; //boolean to set the unsaved changes
    private File currentFile; //sets the current file
    private Timeline autoSaveTimer; //sets the timer for autosave
    private Runnable saveFunction; //Runnable doesnt result in a return which is good for the savefunction as it doesnt end until the program ends

    public AutoSave(Runnable saveFunction) { //public runnable saveFunction for constructing Autosave 
        unsavedChanges = false; //sets unsavedchanges to false
        currentFile = null; //sets current file to nothing
        this.saveFunction = saveFunction; //constructs the saveFunction
    } //end of Autosave

    public void markUnsaved() { //method for markUnsaved
        unsavedChanges = true; //sets it to true
    }

    public void markSaved() { //method for markingSaved
        unsavedChanges = false; //sets it to false
    }

    public boolean hasUnsavedChanges() { //returns the boolean of unsavedChanges
        return unsavedChanges; //return unsaved changes
    }

    public void setCurrentFile(File file) { //method of setting the current file
        currentFile = file; //sets the new file to the currentFile
    }

    public File getCurrentFile() { //gets the currentFile
        return currentFile; //returns it back
    }

    public void startAutoSave() { //start of the AutoSave timer
        if (autoSaveTimer != null) { //if the autoSaveTimer is running
            autoSaveTimer.stop(); //stop the timer
        }

        autoSaveTimer = new Timeline( //creates a new timeline for the timer
            new KeyFrame( //the new timer
                Duration.seconds(30), //duration equals 30
                e -> performAutoSave() //performAutoSave() is called
            ) //end of new keyframe
        ); //end of timeline

        autoSaveTimer.setCycleCount(Timeline.INDEFINITE); //sets the cycle to indefinite, meaning it goes until the program ends
        autoSaveTimer.play(); //plays the timer again
    } //end of startAutoSave()

    public void stopAutoSave() { //stop of AutoSave method
        if (autoSaveTimer != null) { //if the timer is still running
            autoSaveTimer.stop(); //stop autosave timer
        } //end of if
    } //end of method

    private void performAutoSave() { //AutoSave method 
        if (!unsavedChanges) { //if no unsavedChanges
            return; //return to program
        } //end of if
        if (currentFile == null) {  //if the currentFile equals nothing
            return; //return to program
        } //end of if

        saveFunction.run();//runs the saveFunction
    } //end of method

    public boolean confirmClose(Stage stage) { //method for confirming close Alert
        if (!unsavedChanges) { //if no unsaved changes
            return true; //return to the program and close the program
        }

        Alert alert = new Alert(Alert.AlertType.WARNING); //Alert for confirming the close
        alert.setTitle("Unsaved Changes"); //title
        alert.setHeaderText("You have unsaved changes."); //header

        alert.setContentText( //content
            "Would you like to save your changes before closing?"
        );

        ButtonType saveButton = new ButtonType( //Save button
            "Save",
            ButtonBar.ButtonData.YES
        );

        ButtonType dontSaveButton = new ButtonType( //Dont Save button
            "Don't Save",
            ButtonBar.ButtonData.NO
        );

        ButtonType cancelButton = new ButtonType( //Cancel button
            "Cancel",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert.getButtonTypes().setAll( //sets all the buttons in the alert
            saveButton,
            dontSaveButton,
            cancelButton
        );

        Optional<ButtonType> result = alert.showAndWait(); //waits for the user input

        if (result.isPresent() && result.get() == saveButton) { //if the user chooses save
            saveFunction.run(); //runs save function
            return !unsavedChanges; //returns with unsaved changes to not true
        } //end ofif
        if (result.isPresent() && result.get() == dontSaveButton) { //if the user chooses dont save
            return true; //close program
        } //end of if

        return false; //returns false for when the user clicks close
    }//end of confirm close
} //end of AutoSave class
