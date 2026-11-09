
import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Factory class responsible for creating vector icons and
 * interactive buttons for the GRPaint drawing toolbar.
 *
 * Uses SVG graphics to display tool icons and JavaFX tooltips
 * to help users identify each drawing tool.
 */
public final class ToolButtonFactory {
    /**
     * Private constructor prevents the creation of
     * ToolButtonFactory objects.
     */
    private ToolButtonFactory() {}

    /**
     * Creates a styled SVG icon for a drawing tool.
     *
     * The icon uses a green fill color and is scaled
     * to fit inside the toolbar button.
     *
     * @param path the SVG path data defining the icon
     * @return the configured SVGPath icon
     */
    public static SVGPath makeIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.web("#215f42"));
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);
        return icon;
    }

    /**
     * Creates an interactive button for a drawing tool.
     *
     * Each button displays an SVG icon and a tooltip
     * explaining its function. When clicked, the button
     * activates the corresponding drawing tool through
     * the supplied event handler.
     * 
     * @param onSelect the handler used to activate the selected tool
     * @param tool the name of the drawing tool to activate
     * @param tooltip the descriptive text displayed when hovering
     *                over the button
     * @param path the SVG path data used to create the tool icon
     * @return the configured JavaFX Button with its icon,
     *         tooltip, and action handler
     */
    public static Button createToolButton(
            Consumer<String> onSelect,
            String tool,
            String tooltip,
            String path) {

        Button button = new Button();

        button.setGraphic(makeIcon(path));
        button.setTooltip(new Tooltip(tooltip));

        button.setPrefSize(34, 34);
        button.setMinSize(34, 34);

        button.setOnAction(e -> onSelect.accept(tool));

        return button;
    }
}
