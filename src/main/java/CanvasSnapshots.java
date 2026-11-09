
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;

/**
 * Captures high-resolution canvas snapshots for sharper previews.
 */
public final class CanvasSnapshots {

    /** Prevents creating instances of this utility class. */
    private CanvasSnapshots() { }

    /**
     * Captures the canvas at the display's pixel density.
     * @param canvas the canvas to capture
     * @return a high-resolution image of the canvas
     */
    public static WritableImage capture(Canvas canvas) {
        double scale = canvas.getScene() != null && canvas.getScene().getWindow() != null
                ? canvas.getScene().getWindow().getOutputScaleX()
                : Screen.getPrimary().getOutputScaleX();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.WHITE);
        parameters.setTransform(Transform.scale(scale, scale));

        return canvas.snapshot(parameters, null);
    }
}
