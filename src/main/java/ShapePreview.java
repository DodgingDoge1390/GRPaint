import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Manages a reversible live shape preview while the mouse is dragged. */
public final class ShapePreview {
    private final Canvas canvas;
    private final GraphicsContext graphics;
    private WritableImage beforeShape;

    /**
     * Creates the shape preview helper.
     * @param canvas the drawing canvas
     * @param graphics the canvas graphics context
     */
    public ShapePreview(Canvas canvas, GraphicsContext graphics) {
        this.canvas = canvas;
        this.graphics = graphics;
    }

    /** Saves the original drawing before the preview begins. */
    public void begin() {
        beforeShape = CanvasSnapshots.capture(canvas);
    }

    /**
     * Displays a temporary preview of the selected shape.
     * @param startX starting X coordinate
     * @param startY starting Y coordinate
     * @param curX current X coordinate
     * @param curY current Y coordinate
     * @param dashed true for dashed outlines
     * @param color the selected drawing color
     * @param tool the selected shape tool
     * @param sides number of polygon sides
     * @param starPoints number of star points
     */
    public void preview(double startX, double startY, double curX, double curY,
                        boolean dashed, Color color, String tool, int sides, int starPoints) {
        if (beforeShape != null) {
            graphics.drawImage(beforeShape, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
        graphics.setLineDashes(dashed ? new double[]{10, 10} : null);
        graphics.setStroke(color);
        try {
            double x = Math.min(startX, curX);
            double y = Math.min(startY, curY);
            double width = Math.abs(curX - startX);
            double height = Math.abs(curY - startY);
            Shapes.drawShape(graphics, tool, x, y, width, height, sides, starPoints);
        } finally {
            graphics.setLineDashes((double[]) null);
        }
    }
}
