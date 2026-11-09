
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

/**
 * Handles user input for configurable polygon and star shapes.
 * Validates the number of sides or points entered by the user.
 */
public final class ShapePrompts {

    /**
     * Private constructor prevents creating ShapePrompts objects.
     */
    private ShapePrompts() {}

    /**
     * Prompts the user to enter the number of sides for a polygon.
     * The polygon must have at least three sides.
     *
     * @param previous the previously selected number of polygon sides
     * @return the selected number of sides, or the previous value if canceled or invalid
     */
    public static int askPolygonSides(int previous) {
        return ask("Polygon", "Number of sides (3 or more):", "Invalid Sides",
            "Invalid number of sides", 3, previous);
    }

    /**
     * Prompts the user to enter the number of points for a star.
     * The star must have at least four points.
     *
     * @param previous the previously selected number of star points
     * @return the selected number of points, or the previous value if canceled or invalid
     */
    public static int askStarPoints(int previous) {
        return ask("Star", "Number of points (4 or more):", "Invalid Points",
            "Invalid number of points", 4, previous);
    }

    /**
     * Displays an input dialog and validates the entered number.
     * Shows an error message if the input is invalid.
     *
     * @param title the title of the input dialog
     * @param header the instructions displayed in the dialog
     * @param errorTitle the title of the error dialog
     * @param errorHeader the error dialog heading
     * @param minimum the minimum acceptable integer
     * @param previous the previously selected value
     * @return the validated number, or the previous value if canceled or invalid
     */
    private static int ask(String title, String header, String errorTitle, String errorHeader, int minimum, int previous) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(previous));
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        var answer = dialog.showAndWait();

        if (answer.isEmpty()) {
            return previous;
        }

        try {
            int value = Integer.parseInt(answer.get().trim());

            if (value >= minimum) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // Invalid input is handled by the error dialog below.
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(errorTitle);
        alert.setHeaderText(errorHeader);
        alert.setContentText(
            "Please enter a whole number that is " + minimum + " or more."
        );
        alert.showAndWait();

        return previous;
    }
}
