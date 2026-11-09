
import java.util.function.Supplier;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Creates transformation menus for the GRPaint application.
 *
 * Provides options to rotate an image clockwise by 90, 180,
 * or 270 degrees, or flip it horizontally or vertically.
 *
 * Transformations can be applied to either the entire
 * canvas or a selected portion of the image.
 */
public final class TransformMenu {

    /**
     * Private constructor prevents the creation of
     * TransformMenu objects.
     */
    private TransformMenu() { }

    /**
     * Creates a menu containing image rotation and flipping options.
     *
     * Adds menu items for rotating images clockwise by
     * 90, 180, or 270 degrees, as well as flipping images
     * horizontally and vertically.
     *
     * Each menu item is connected to an event handler that
     * performs the selected transformation.
     *
     * @param title the title displayed on the transformation menu
     * @param selectedOnly true to transform only a selected image region,
     *                     false to transform the entire canvas
     * @param currentTab supplier that retrieves the currently active PaintTab
     * @return the configured JavaFX Menu containing transformation options
     */
    public static Menu create(
            String title,
            boolean selectedOnly,
            Supplier<PaintTab> currentTab) {

        Menu menu = new Menu(title);

        for (int angle : new int[]{90, 180, 270}) {
            MenuItem rotate = new MenuItem(
                "Rotate " + angle + "° Clockwise"
            );

            rotate.setOnAction(
                e -> transform(currentTab, selectedOnly, angle, null)
            );

            menu.getItems().add(rotate);
        }

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem horizontal = new MenuItem(
            "Flip Horizontal (Left/Right)"
        );

        MenuItem vertical = new MenuItem(
            "Flip Vertical (Top/Bottom)"
        );

        horizontal.setOnAction(
            e -> transform(currentTab, selectedOnly, null, true)
        );

        vertical.setOnAction(
            e -> transform(currentTab, selectedOnly, null, false)
        );

        menu.getItems().addAll(horizontal, vertical);

        return menu;
    }

    /**
     * Applies a rotation or flip transformation to the active drawing.
     *
     * If selectedOnly is true, the method verifies that an image
     * region has been selected before performing the transformation.
     * If no selection exists, an informational alert is displayed.
     *
     * If no tab is active, the transformation is skipped.
     *
     * @param tabSupplier supplier used to retrieve the active PaintTab
     * @param selectedOnly true to transform a selection,
     *                     false to transform the entire canvas
     * @param degrees the clockwise rotation angle in degrees,
     *                or null when performing a flip
     * @param horizontal true to flip horizontally, false to flip
     *                   vertically, or null when performing a rotation
     */
    private static void transform(
            Supplier<PaintTab> tabSupplier,
            boolean selectedOnly,
            Integer degrees,
            Boolean horizontal) {

        PaintTab tab = tabSupplier.get();

        if (tab == null) {
            return;
        }

        if (selectedOnly && !tab.getDrawing().hasSelection()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("No Selection");
            alert.setHeaderText("Select part of your image first");
            alert.setContentText(
                "Choose the Select tool and drag a rectangle " +
                "before transforming a selection."
            );

            alert.showAndWait();
            return;
        }

        if (degrees != null) {
            tab.getDrawing().rotate(degrees, selectedOnly);
        } else {
            tab.getDrawing().flip(horizontal, selectedOnly);
        }
    }
}
