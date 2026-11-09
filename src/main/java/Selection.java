import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;

/**
 * Public class that handles selecting, moving, copying, and pasting
 * pieces of the drawing in GRPaint.
 */
public class Selection {

    private Canvas canvas;
    private GraphicsContext gc;
    private Canvas overlay;
    private GraphicsContext overlayGc;

    private WritableImage selectedImage;
    private WritableImage floatBase;

    private double selX, selY, selW, selH;
    private double selStartX, selStartY;
    private double moveGrabX, moveGrabY;

    private boolean selecting = false;
    private boolean moving = false;
    private boolean floating = false;

    private static WritableImage clipboardImage;
    private static double clipW, clipH;

    private Runnable saveUndo;
    private Runnable onChange;

    /**
     * Creates the selection tool using the drawing's canvases.
     * @param canvas the main drawing canvas
     * @param gc the graphics context for the drawing
     * @param overlay the canvas for the selection outline
     * @param saveUndo the function that saves an undo state
     * @param onChange the function called when the drawing changes
     */
    public Selection(Canvas canvas, GraphicsContext gc, Canvas overlay, Runnable saveUndo, Runnable onChange) {
        this.canvas = canvas;
        this.gc = gc;
        this.overlay = overlay;
        this.overlayGc = overlay.getGraphicsContext2D();
        this.saveUndo = saveUndo;
        this.onChange = onChange;
    }

    /**
     * Checks if a point is inside the current selection.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return true if the point is inside the selection
     */
    private boolean insideSelection(double x, double y) {
        return x >= selX && x <= selX + selW
            && y >= selY && y <= selY + selH;
    }

    /**
     * Takes a snapshot of the canvas at the screen's render scale.
     * @return a snapshot of the canvas
     */
    private WritableImage takeSnapshot() {
        double scale = 1.0;

        if (canvas.getScene() != null
                && canvas.getScene().getWindow() != null) {
            scale = canvas.getScene().getWindow().getOutputScaleX();
        } else {
            scale = Screen.getPrimary().getOutputScaleX();
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(Transform.scale(scale, scale));

        return canvas.snapshot(params, null);
    }

    /**
     * Draws a snapshot back over the canvas.
     * @param image the image to draw
     */
    private void drawSnapshot(WritableImage image) {
        if (image != null) {
            gc.drawImage(image, 0, 0,
                         canvas.getWidth(), canvas.getHeight());
        }
    }

    /**
     * Starts selecting an area or moving an existing selection.
     * @param x the mouse x coordinate
     * @param y the mouse y coordinate
     */
    public void mousePressed(double x, double y) {
        if (selectedImage != null && insideSelection(x, y)) {
            startMove(x, y);
        } else {
            floating = false;
            selStartX = x;
            selStartY = y;
            selecting = true;
        }
    }

    /**
     * Updates the selection box or moves the selected image while dragging.
     * @param x the current mouse x coordinate
     * @param y the current mouse y coordinate
     */
    public void mouseDragged(double x, double y) {
        if (selecting) {
            drawOutline(
                Math.min(selStartX, x),
                Math.min(selStartY, y),
                Math.abs(x - selStartX),
                Math.abs(y - selStartY)
            );
        }

        if (moving) {
            selX = x - moveGrabX;
            selY = y - moveGrabY;

            drawSnapshot(floatBase);
            gc.drawImage(selectedImage, selX, selY, selW, selH);
            drawOutline(selX, selY, selW, selH);
        }
    }

    /**
     * Finishes selecting an area or moving a selected image.
     * @param x the mouse release x coordinate
     * @param y the mouse release y coordinate
     */
    public void mouseReleased(double x, double y) {
        if (selecting) {
            selecting = false;
            finishSelection(x, y);
        }

        if (moving) {
            moving = false;
            onChange.run();
        }
    }

    /**
     * Copies the pixels inside the selection into a separate image.
     * @param endX the x coordinate where the selection ends
     * @param endY the y coordinate where the selection ends
     */
    private void finishSelection(double endX, double endY) {
        double x0 = Math.max(0, Math.min(selStartX, endX));
        double y0 = Math.max(0, Math.min(selStartY, endY));
        double x1 = Math.min(canvas.getWidth(),
                             Math.max(selStartX, endX));
        double y1 = Math.min(canvas.getHeight(),
                             Math.max(selStartY, endY));

        if (x1 - x0 < 1 || y1 - y0 < 1) {
            clearSelection();
            return;
        }

        WritableImage snap = takeSnapshot();
        PixelReader reader = snap.getPixelReader();

        double scaleX = snap.getWidth() / canvas.getWidth();
        double scaleY = snap.getHeight() / canvas.getHeight();

        int px = (int) Math.round(x0 * scaleX);
        int py = (int) Math.round(y0 * scaleY);

        int pw = Math.max(1, Math.min(
            (int) Math.round((x1 - x0) * scaleX),
            (int) snap.getWidth() - px
        ));

        int ph = Math.max(1, Math.min(
            (int) Math.round((y1 - y0) * scaleY),
            (int) snap.getHeight() - py
        ));

        selectedImage = new WritableImage(reader, px, py, pw, ph);

        selX = x0;
        selY = y0;
        selW = pw / scaleX;
        selH = ph / scaleY;

        floating = false;
        redrawOutline();
    }

    /**
     * Starts moving the selected image.
     * @param mouseX the mouse x coordinate
     * @param mouseY the mouse y coordinate
     */
    private void startMove(double mouseX, double mouseY) {
        saveUndo.run();

        if (!floating) {
            gc.setFill(Color.WHITE);
            gc.fillRect(selX, selY, selW, selH);

            floatBase = takeSnapshot();

            gc.drawImage(selectedImage, selX, selY, selW, selH);
            floating = true;
        }

        moveGrabX = mouseX - selX;
        moveGrabY = mouseY - selY;
        moving = true;
    }

    /**
     * Copies the current selection to the clipboard.
     */
    public void copySelection() {
        if (selectedImage != null) {
            clipboardImage = selectedImage;
            clipW = selW;
            clipH = selH;
        }
    }

    /**
     * Pastes the copied image onto the canvas.
     * The pasted image can be moved to a new location.
     */
    public void pasteSelection() {
        if (clipboardImage == null) {
            return;
        }

        saveUndo.run();

        floatBase = takeSnapshot();
        selectedImage = clipboardImage;

        selX = 200;
        selY = 300;
        selW = clipW;
        selH = clipH;

        gc.drawImage(selectedImage, selX, selY, selW, selH);

        floating = true;
        redrawOutline();
        onChange.run();
    }

    /**
     * Checks whether the user currently has a selected piece.
     * @return true if there is a selected image
     */
    public boolean hasSelection() {
        return selectedImage != null;
    }

    /**
     * Rotates only the selected piece clockwise.
     * @param degrees the rotation angle (90, 180, or 270)
     */
    public void rotateSelected(int degrees) {
        if (selectedImage == null) return;
        WritableImage image = Rotation.rotateImage(selectedImage, degrees);
        boolean swap = Math.floorMod(degrees, 180) == 90;
        transformSelected(image, swap);
    }

    /**
     * Mirrors only the selected piece of the canvas.
     * @param horizontal true for left/right, false for top/bottom
     */
    public void flipSelected(boolean horizontal) {
        if (selectedImage == null) return;
        transformSelected(Rotation.flipImage(selectedImage, horizontal), false);
    }

    /**
     * Removes the old selected pixels and puts its transformed image back.
     * The transformation can still be undone or moved after it is applied.
     * @param image the changed selection
     * @param swap true if width and height should be exchanged
     */
    private void transformSelected(WritableImage image, boolean swap) {
        saveUndo.run();
        if (floating && floatBase != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            drawSnapshot(floatBase);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillRect(selX, selY, selW, selH);
        }

        double oldWidth = selW;
        if (swap) {
            selW = selH;
            selH = oldWidth;
        }
        selectedImage = image;
        floatBase = takeSnapshot();
        gc.drawImage(selectedImage, selX, selY, selW, selH);
        floating = true;
        redrawOutline();
        onChange.run();
    }

    /**
     * Clears the current selection and hides its outline.
     */
    public void clearSelection() {
        selectedImage = null;
        floating = false;
        moving = false;
        selecting = false;
        clearOverlay();
    }

    /**
     * Clears the selection outline canvas.
     */
    private void clearOverlay() {
        overlayGc.clearRect(
            0, 0, overlay.getWidth(), overlay.getHeight()
        );
    }

    /**
     * Draws the dashed outline around the selection.
     * @param x the x coordinate of the outline
     * @param y the y coordinate of the outline
     * @param w the width of the outline
     * @param h the height of the outline
     */
    private void drawOutline(double x, double y, double w, double h) {
        clearOverlay();
        overlayGc.setStroke(Color.GREEN);
        overlayGc.setLineWidth(1);
        overlayGc.setLineDashes(5, 5);
        overlayGc.strokeRect(x, y, w, h);
    }

    /**
     * Redraws the outline around the current selection.
     */
    public void redrawOutline() {
        if (selectedImage != null) {
            drawOutline(selX, selY, selW, selH);
        } else {
            clearOverlay();
        }  
    }

    /**
     * Puts down the current floating selection.
     */
    public void putDown() {
        floating = false;
    }

    /**
     * Clears the selection outline.
     */
    public void clearOutline() {
        clearOverlay();
    }
}

