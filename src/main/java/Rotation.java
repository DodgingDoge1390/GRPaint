import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

/**
 * Handles rotating and flipping images in GRPaint.
 * Can change the whole canvas or a piece selected by the user.
 */
public class Rotation {

    /**
     * Rotates an image clockwise by 90, 180, or 270 degrees.
     * @param image the image to rotate
     * @param degrees the clockwise angle
     * @return the rotated image
     */
    public static WritableImage rotateImage(Image image, int degrees) {
        int angle = Math.floorMod(degrees, 360);
        if (angle != 0 && angle != 90 && angle != 180 && angle != 270) {
            throw new IllegalArgumentException("Rotation must be 90, 180, or 270 degrees.");
        }
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        boolean swap = angle == 90 || angle == 270;
        WritableImage result = new WritableImage(swap ? height : width, swap ? width : height);
        PixelReader source = image.getPixelReader();
        PixelWriter destination = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = x;
                int newY = y;
                switch (angle) {
                    case 90 -> { newX = height - 1 - y; newY = x; }
                    case 180 -> { newX = width - 1 - x; newY = height - 1 - y; }
                    case 270 -> { newX = y; newY = width - 1 - x; }
                    default -> { }
                }
                destination.setArgb(newX, newY, source.getArgb(x, y));
            }
        }
        return result;
    }

    /**
     * Mirrors an image left-to-right or top-to-bottom.
     * @param image the image to flip
     * @param horizontal true to switch left and right, false to switch top and bottom
     * @return the flipped image
     */
    public static WritableImage flipImage(Image image, boolean horizontal) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader source = image.getPixelReader();
        PixelWriter destination = result.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = horizontal ? width - 1 - x : x;
                int newY = horizontal ? y : height - 1 - y;
                destination.setArgb(newX, newY, source.getArgb(x, y));
            }
        }
        return result;
    }

    /**
     * Rotates all the pixels on the canvas.
     * A quarter turn also switches the canvas width and height.
     * @param canvas the canvas to rotate
     * @param degrees the clockwise angle
     */
    public static void rotateCanvas(Canvas canvas, int degrees) {
        replaceCanvas(canvas, rotateImage(snapshot(canvas), degrees));
    }

    /**
     * Flips the entire canvas without changing its size.
     * @param canvas the canvas to flip
     * @param horizontal whether to flip the left and right sides
     */
    public static void flipCanvas(Canvas canvas, boolean horizontal) {
        replaceCanvas(canvas, flipImage(snapshot(canvas), horizontal));
    }

    /**
     * Captures the pixels at the canvas's logical size, independent of screen scale.
     * @param canvas the canvas being copied
     * @return the canvas pixels
     */
    private static WritableImage snapshot(Canvas canvas) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.WHITE);
        parameters.setTransform(Transform.scale(1, 1));
        return canvas.snapshot(parameters, null);
    }

    /**
     * Replaces the canvas pixels and updates its dimensions.
     * @param canvas the destination canvas
     * @param image the image to draw on it
     */
    private static void replaceCanvas(Canvas canvas, WritableImage image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        javafx.scene.paint.Paint stroke = gc.getStroke();
        javafx.scene.paint.Paint fill = gc.getFill();
        double lineWidth = gc.getLineWidth();
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(image, 0, 0);
        gc.setStroke(stroke);
        gc.setFill(fill);
        gc.setLineWidth(lineWidth);
    }
}
