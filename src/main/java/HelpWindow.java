import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/** Displays the scrollable GRPaint help and keyboard shortcut reference. */
public final class HelpWindow {
    private HelpWindow() {}
    
    /**
    * Displays the GRPaint help window.
    * Shows instructions for tools and keyboard shortcuts.
    */
    public static void show() {
        Stage helpStage = new Stage();
        helpStage.setTitle("GRPaint Help");
        Label helpText = new Label(
            "GRPaint Help\n\n" +
            "File:\n" +
            "Open - Opens an image file.\n" +
            "Save - Saves the current image.\n" +
            "Save As - Saves the image as a new file.\n" +
            "New Tab - Opens a new blank canvas tab.\n\n" +
            "Edit:\n" +
            "Undo / Redo - Step backward or forward through your changes.\n" +
            "Copy / Paste - Copies the selected piece. Paste drops it in the top left, drag it where you want.\n" +
            "Clear Canvas - Wipes the canvas (asks first).\n\n" +
            "Tools:\n" +
            "Line - Click twice to draw a line.\n" +
            "Pencil - Allows you to free flow your canvas.\n" +
            "Text - Click on the canvas and type the text to add.\n" +
            "Select - Drag a box around part of the image.\n" +
            "Move Selection - Drag the selected piece to move it.\n\n" +
            "Shapes:\n" +
            "Drag to draw. Triangle is a regular triangle, Right Triangle has a 90 degree corner.\n" +
            "Polygon asks how many sides you want (3 or more).\n\n" +
            "Keyboard Shortcuts:\n" +
            "Ctrl (Command on Mac) + O - Open\n" +
            "Ctrl + S - Save\n" +
            "Ctrl + Shift + S - Save As\n" +
            "Ctrl + T - New Tab\n" +
            "Ctrl + Q - Close\n" +
            "Crtl + Shift + Alt + R - Suprise\n" +
            "Ctrl + R - Resize\n" +
            "Ctrl + Z - Undo\n" +
            "Ctrl + Y - Redo\n" +
            "Ctrl + C - Copy\n" +
            "Ctrl + V - Paste\n" +
            "Ctrl + Shift + N - Clear Canvas\n" +
            "Transforms: Edit > Transform Whole Canvas / Transform Selection.\n" +
            "Select a piece before rotating or flipping just that piece.\n" +
            "Resize: Insert any height or width you want as long as it isn't 0 or below.\n" +
            "Auto Save: Saved tabs automatically save changed images every 30 seconds.\n" +
            "Settings: Enable Autosave and Show Autosave Countdown apply to the selected tab.\n" +
            "Web Server: Choose one or more tabs to share a snapshot at http://127.0.0.1:8080/.\n\n"
        );


        // Put the help text inside a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(helpText);

        // Allow the ScrollPane to resize the Label horizontally
        scrollPane.setFitToWidth(true);

        // Create the scene
        Scene scene = new Scene(scrollPane, 500, 500);

        helpStage.setScene(scene);
        helpStage.show();
    }
}
