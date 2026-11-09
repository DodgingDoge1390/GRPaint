
import java.util.function.Consumer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Handles canvas operations for the GRPaint application.
 *
 * This class manages clearing the canvas, loading images,
 * saving drawing history, and performing undo and redo actions.
 *
 * It also communicates with the Selection and DrawingHistory
 * classes to maintain drawing consistency and records user
 * actions through the application's logging system.
 */
public final class CanvasOperations {

    /** The canvas used to display and edit the drawing. */
    private final Canvas canvas;

    /** The graphics context responsible for drawing on the canvas. */
    private final GraphicsContext graphics;

    /** Manages selected regions of the drawing canvas. */
    private final Selection selection;

    /** Stores previous canvas states for undo and redo operations. */
    private final DrawingHistory history;

    /** Callback used to indicate that the canvas has changed. */
    private final Runnable onChange;

    /** Records canvas operations in the application log. */
    private final Consumer<String> log;

    /**
     * Constructs a CanvasOperations object to manage drawing operations.
     *
     * Connects the canvas with its graphics context, selection
     * manager, drawing history, change callback, and logging system.
     *
     * @param canvas the canvas on which drawing operations occur
     * @param graphics the graphics context used to modify the canvas
     * @param selection the selection manager for the drawing
     * @param history the drawing history used for undo and redo
     * @param onChange callback executed whenever the canvas changes
     * @param log consumer responsible for logging user actions
     */
    public CanvasOperations(Canvas canvas, GraphicsContext graphics, Selection selection, DrawingHistory history, Runnable onChange, Consumer<String> log) {
        this.canvas = canvas;
        this.graphics = graphics;
        this.selection = selection;
        this.history = history;
        this.onChange = onChange;
        this.log = log;
    }

    /**
     * Fills the entire canvas with a solid white background.
     *
     * This method is used when initializing a new drawing
     * or clearing an existing image.
     *
     * The method can also be called while a Drawing
     * object is being constructed.
     *
     * @param canvas the canvas whose background will be filled white
     */
    public static void fillWhite(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Displays a confirmation dialog before clearing the canvas.
     *
     * If the user confirms, the current canvas is saved to
     * the undo history, any active selection is cleared,
     * and the entire canvas is filled with white.
     *
     * The method also notifies the application of the
     * change and records the action in the log.
     *
     * If the user cancels, the canvas remains unchanged.
     */
    public void clearWithConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will clear the entire canvas.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        saveUndoState();
        selection.clearSelection();
        fillWhite(canvas);
        onChange.run();
        log.accept("Canvas cleared");
    }

    /**
     * Saves the current canvas state to the undo history.
     *
     * This allows the user to restore the previous
     * drawing state after modifying the canvas.
     *
     * The actual history storage is handled by
     * the DrawingHistory class.
     */
    public void saveUndoState() {
        history.saveState(canvas);
    }

    /**
     * Restores the previous canvas state using the undo history.
     *
     * Delegates the undo operation to DrawingHistory,
     * clears the current selection, records the action,
     * and notifies the application that the canvas changed.
     */
    public void undo() {
        history.undo(canvas);
        selection.clearSelection();
        log.accept("Undo");
        onChange.run();
    }

    /**
     * Reapplies a previously undone canvas state.
     *
     * Delegates the redo operation to DrawingHistory,
     * clears the current selection, records the action,
     * and notifies the application that the canvas changed.
     */
    public void redo() {
        history.redo(canvas);
        selection.clearSelection();
        log.accept("Redo");
        onChange.run();
    }

    /**
     * Loads an image onto the drawing canvas.
     *
     * Saves the current canvas state to the undo history,
     * clears any active selection, fills the background
     * with white, and draws the supplied image.
     *
     * The application is then notified that the
     * canvas has changed.
     *
     * @param image the image to load onto the canvas
     */
    public void loadImage(Image image) {
        saveUndoState();
        selection.clearSelection();
        fillWhite(canvas);
        graphics.drawImage(image, 0, 0);
        onChange.run();
    }
}
