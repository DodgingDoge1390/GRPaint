import java.util.Stack;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

/**
 * Initializes the drawing history manager.
 */
public class DrawingHistory {
    private record State(WritableImage image, double width, double height) {}
    private final Stack<State> undoStack = new Stack<>();
    private final Stack<State> redoStack = new Stack<>();
    private static final int MAX_UNDO = 50;

    /**
     * Public method that saves the current canvas before a change.
     * Clears the redo history when a new action is made.
     * @param canvas the canvas being saved
     */
    public void saveState(Canvas canvas) {
        undoStack.push(takeSnapshot(canvas));
        if (undoStack.size() > MAX_UNDO) undoStack.remove(0);
        redoStack.clear();
    }

    /**
     * Public method that undoes the last action through taking a snapshot.
     * @param canvas the canvas being restored
     */
    public void undo(Canvas canvas) {
        if (!undoStack.empty()) {
            redoStack.push(takeSnapshot(canvas));
            drawSnapshot(canvas, undoStack.pop());
        }
    }

    /**
     * Public method that redoes the last undone action through taking a snapshot.
     * @param canvas the canvas being restored
     */
    public void redo(Canvas canvas) {
        if (!redoStack.empty()) {
            undoStack.push(takeSnapshot(canvas));
            drawSnapshot(canvas, redoStack.pop());
        }
    }

    /**
     * Captures the canvas and its size, at logical canvas resolution.
     * @param canvas the canvas to copy
     * @return the state used for undo and redo
     */
    private State takeSnapshot(Canvas canvas) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(Transform.scale(1, 1));
        return new State(canvas.snapshot(params, null), canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Restores an image and the original canvas dimensions.
     * @param canvas the canvas to restore
     * @param state the saved image and dimensions
     */
    private void drawSnapshot(Canvas canvas, State state) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        javafx.scene.paint.Paint stroke = gc.getStroke();
        javafx.scene.paint.Paint fill = gc.getFill();
        double lineWidth = gc.getLineWidth();
        canvas.setWidth(state.width());
        canvas.setHeight(state.height());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(state.image(), 0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(stroke);
        gc.setFill(fill);
        gc.setLineWidth(lineWidth);
    }
}
