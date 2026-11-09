import java.io.File;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;

/**
 * Public class that keeps the drawing and save information for one tab.
 * Each tab has its own autosave timer and current file.
 */
public class PaintTab extends Tab {
    private Drawing drawing;
    private File currentFile;
    private AutoSave autoSave;
    private final Label countdownLabel = new Label("Autosave in: 30 seconds");

    /**
     * Constructs a new tab with a blank drawing.
     * @param title the title shown on the tab
     * @param width the width of the drawing
     * @param height the height of the drawing
     */
    public PaintTab(String title, double width, double height) {
        super(title);
        drawing = new Drawing(width, height);
        drawing.setLoggingSource(this::getLogName);
        countdownLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 8px 0 8px;");
        drawing.getToolbar().getChildren().add(0, countdownLabel);
        setContent(drawing.getDrawingPane());
    }

    /**
     * Returns the name used in the activity log.
     * @return the saved filename or name of the tab
     */
    public String getLogName() {
        return currentFile == null ? getText() : currentFile.getName();
    }

    /**
     * Public method for getting the drawing in this tab.
     * @return the drawing for this tab
     */
    public Drawing getDrawing() { return drawing; }

    /**
     * Public method that replaces the drawing in this tab.
     * Keeps the autosave timer at the top when a canvas is resized.
     * @param drawing the new drawing to use
     */
    public void setDrawing(Drawing drawing) {
        this.drawing.getToolbar().getChildren().remove(countdownLabel);
        this.drawing = drawing;
        drawing.setLoggingSource(this::getLogName);
        drawing.getToolbar().getChildren().add(0, countdownLabel);
        setContent(drawing.getDrawingPane());
        if (autoSave != null) {
            drawing.setOnChange(() -> autoSave.markUnsaved());
            autoSave.markUnsaved();
        }
    }

    /**
     * Public method for getting the file associated with this tab.
     * @return the current image file, or null if it has not been saved
     */
    public File getCurrentFile() { return currentFile; }

    /**
     * Public method for setting the file associated with this tab.
     * @param currentFile the image file to associate with this tab
     */
    public void setCurrentFile(File currentFile) { this.currentFile = currentFile; }

    /**
     * Public method for getting the autosave manager for this tab.
     * @return the autosave manager
     */
    public AutoSave getAutoSave() { return autoSave; }

    /**
     * Public method that sets the autosave manager for this tab.
     * @param autoSave the autosave manager to use
     */
    public void setAutoSave(AutoSave autoSave) {
        this.autoSave = autoSave;
        autoSave.setCountdownLabel(countdownLabel);
        drawing.setOnChange(() -> autoSave.markUnsaved());
    }
}
