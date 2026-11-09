
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

/**
 * Handles resizing the drawing canvas in the GRPaint application.
 *
 * This class displays input dialogs that allow users to
 * specify a new width and height for their drawing canvas.
 *
 * Both dimensions are validated to ensure they are positive
 * numbers before a new Drawing object is created.
 *
 * After resizing, the application marks the drawing as
 * unsaved and records the resize operation in the log.
 */
public final class CanvasResizeDialog {

    /**
     * Private constructor prevents the creation of
     * CanvasResizeDialog objects.
     *
     * All functionality is provided through static methods.
     */
    private CanvasResizeDialog() {}

    /**
     * Displays input dialogs for resizing the current drawing canvas.
     *
     * The user is first prompted to enter a new canvas width,
     * followed by a prompt for the new canvas height.
     *
     * @param tab the PaintTab containing the drawing canvas
     *            that will be resized
     */
    public static void show(PaintTab tab) {

        TextInputDialog widthDialog = new TextInputDialog("800");
        widthDialog.setTitle("Resize Canvas");
        widthDialog.setHeaderText("Enter new canvas width:");

        widthDialog.showAndWait().ifPresent(widthInput -> {

            TextInputDialog heightDialog = new TextInputDialog("600");
            heightDialog.setTitle("Resize Canvas");
            heightDialog.setHeaderText("Enter new canvas height:");

            heightDialog.showAndWait().ifPresent(heightInput -> {

                try {
                    double width = Double.parseDouble(widthInput);
                    double height = Double.parseDouble(heightInput);

                    if (width <= 0 || height <= 0) {
                        new Alert(
                            Alert.AlertType.ERROR,
                            "Canvas dimensions must be greater than zero."
                        ).showAndWait();

                        return;
                    }

                    tab.setDrawing(new Drawing(width, height));

                    tab.getAutoSave().markUnsaved();

                    Logging.log(
                        tab.getLogName(),
                        "Canvas resized to " + width + " x " + height
                    );

                } catch (NumberFormatException e) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Size");
                    alert.setHeaderText("Invalid canvas size");
                    alert.setContentText(
                        "Please enter numbers for the width and height."
                    );

                    alert.showAndWait();
                }
            });
        });
    }
}
