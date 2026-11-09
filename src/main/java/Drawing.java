import java.util.Optional;
import java.util.function.Supplier;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
/**
 * Public Class for the Drawing functionality in GRPaint
 * Defines and calculates what each Tool and Shape does
 * when the user clicks on it
 */
public class Drawing {
    private Canvas canvas;
    private GraphicsContext gc;
    private double startX;
    private double startY;
    private BorderPane drawingPane;
    private HBox toolbar;
    private String currentTool = "";
    private String lastTool = "pencil";
    private boolean lineStarted = false;
    private boolean dashed = false;
    private ColorPicker colorPicker;
    private Label widthLabel;
    private Label toolLabel;
    private WritableImage shapeSnapshot;
    private DrawingHistory history = new DrawingHistory();

    private int numberOfSides = 5;
    private int numberOfStarPoints = 5;

    private Runnable onChange = () -> {};
    private Supplier<String> loggingSource = () -> "new tab";

    // Overlay canvas that only draws the dashed selection outline
    private Canvas overlay;

    // Handles selecting, moving, copying, and pasting pieces
    private Selection selection;

    /**
     * Method that sets the Tool to the current selected tool
     * @param tool the current tool selected by user
     */
    public void setTool(String tool) {
        if (!tool.equals("colorGrabber")) {
            lastTool = tool;
        }

        currentTool = tool;
        toolLabel.setText("Current Tool: " + currentTool);
        lineStarted = false;
        logAction(tool + " tool selected");

        if (!tool.equals("eraser")) {
            gc.setStroke(colorPicker.getValue());
        }

        // The lifted selection is put down when leaving selection tools
        if (!tool.equals("select") && !tool.equals("move")) {
            selection.putDown();
        }

        // Polygon asks how many sides it should have
        if (tool.equals("polygon")) {
            promptPolygonSides();
        }

        // Star asks how many points it should have
        if (tool.equals("star")) {
            promptStarPoints();
        }

        // Only show the selection outline while select/move is active
        if (tool.equals("select") || tool.equals("move")) {
            selection.redrawOutline();
        } else {
            selection.clearOutline();
        }
    }

    /**
     * Method that checks if the user selected a shape
     * @param tool the current tool selected by the user
     */
    private boolean isShapeTool(String tool) {
        return tool.equals("square") || tool.equals("circle")
            || tool.equals("rectangle") || tool.equals("ellipse")
            || tool.equals("triangle") || tool.equals("star")
            || tool.equals("righttriangle") || tool.equals("diamond")
            || tool.equals("polygon");
    }

    /**
     * Gets how many real screen pixels there are per canvas unit.
     * @return the render scale
     */
    private double getRenderScale() {
        if (canvas.getScene() != null
                && canvas.getScene().getWindow() != null) {
            return canvas.getScene().getWindow().getOutputScaleX();
        }

        return Screen.getPrimary().getOutputScaleX();
    }

    /**
     * Takes a snapshot of the canvas at the real screen resolution.
     * @return a full resolution snapshot of the canvas
     */
    private WritableImage takeSnapshot() {
        double scale = getRenderScale();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(Transform.scale(scale, scale));

        return canvas.snapshot(params, null);
    }

    /**
     * Draws a snapshot from takeSnapshot() back over the whole canvas.
     * @param image the snapshot to draw
     */
    private void drawSnapshot(WritableImage image) {
        gc.drawImage(image, 0, 0,
                     canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Public method for setting the drawing for the canvas and the tools.
     * Sets toolbar, colorpicker, and refresh button.
     * @param width takes the width of the drawing
     * @param height takes the height of the drawing
     */
    public Drawing(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        // Overlay canvas for the selection outline
        overlay = new Canvas(width, height);
        overlay.setMouseTransparent(true);
        // The selection outline follows 90 and 270 degree canvas rotations.
        canvas.widthProperty().addListener((obs, oldValue, value) -> overlay.setWidth(value.doubleValue()));
        canvas.heightProperty().addListener((obs, oldValue, value) -> overlay.setHeight(value.doubleValue()));

        // Sets the tools color and width
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // Blank white starting image
        fillWhite();

        // Creates the selection class
        selection = new Selection(
            canvas,
            gc,
            overlay,
            this::saveUndoState,
            this::markChanged
        );

        // Sets the colorpicker
        colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setTooltip(
            new javafx.scene.control.Tooltip("Choose drawing color")
        );
        colorPicker.setOnAction(e -> {
            gc.setStroke(colorPicker.getValue());
        });

        // Sets the slider and adds a listener
        widthLabel = new Label("Outline Width: 2px");
        Slider widthSlider = new Slider(0, 50, 2);
        widthSlider.setTooltip(
            new javafx.scene.control.Tooltip("Change line width")
        );
        widthSlider.setShowTickMarks(true);
        widthSlider.setShowTickLabels(true);
        widthSlider.setMajorTickUnit(5);

        widthSlider.valueProperty().addListener(
            (observable, oldValue, newValue) -> {
                gc.setLineWidth(newValue.doubleValue());
                widthLabel.setText(
                    "Outline Width: " + newValue.intValue() + "px"
                );
            }
        );

        // Adds a checkbox for the dashed outline
        CheckBox dashedCheckBox = new CheckBox("Dashed Outline");
        dashedCheckBox.setOnAction(
            e -> dashed = dashedCheckBox.isSelected()
        );

        // Adds a refresh button
        Button refreshButton = new Button("↻");
        refreshButton.setTooltip(
            new javafx.scene.control.Tooltip("Clear canvas")
        );
        refreshButton.setOnAction(e -> refresh());

        // Undo and redo buttons in the toolbar
        Button undoButton = new Button("↶");
        undoButton.setTooltip(
            new javafx.scene.control.Tooltip("Undo (Ctrl+Z)")
        );
        undoButton.setOnAction(e -> undo());

        Button redoButton = new Button("↷");
        redoButton.setTooltip(
            new javafx.scene.control.Tooltip("Redo (Ctrl+Y)")
        );
        redoButton.setOnAction(e -> redo());

        // Tool icons are vector drawings, so separate PNG resources are not required.
        Button pencilButton = createToolButton("pencil", "Pencil: draw freehand", "M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z M20.7 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        Button lineButton = createToolButton("line", "Line: click two points", "M4 19L19 4l1 1L5 20z");
        Button eraserButton = createToolButton("eraser", "Eraser: remove parts of drawing", "M15.14 3l5.86 5.86a2 2 0 0 1 0 2.83L12.69 20H5a2 2 0 0 1-1.41-.59L1 16.83a2 2 0 0 1 0-2.83L12.31 3a2 2 0 0 1 2.83 0z M6 18h5.86L19.6 10.3l-5.86-5.86L2.4 15.41z");
        Button textButton = createToolButton("text", "Text: click to add text", "M4 4h16v4h-2V6h-5v12h3v2H8v-2h3V6H6v2H4z");
        Button grabberButton = createToolButton("colorGrabber", "Color Grabber: pick a color from the canvas", "M20.71 3.29a1 1 0 0 0-1.42 0L16.5 6.08l-1.59-1.59-1.41 1.41L15.09 7.5l-9.9 9.9a3 3 0 0 0-.83 1.55L4 21l2.05-.36a3 3 0 0 0 1.55-.83l9.9-9.9 1.59 1.59 1.41-1.41-1.59-1.59 2.79-2.79a1 1 0 0 0 0-1.42z");
        Button selectButton = createToolButton("select", "Select: drag to select part of the image", "M3 3h7v2H5v5H3z M14 3h7v7h-2V5h-5z M3 14h2v5h5v2H3z M19 14h2v7h-7v-2h5z");
        Button moveButton = createToolButton("move", "Move: drag the selected piece", "M12 2l4 4h-3v5h5V8l4 4-4 4v-3h-5v5h3l-4 4-4-4h3v-5H6v3l-4-4 4-4v3h5V6H8z");

        // Shapes dropdown in the toolbar, each shape has its own icon.
        MenuButton shapesButton = new MenuButton();
        shapesButton.setGraphic(makeIcon("M3 3h8v8H3z M14 3a4 4 0 1 1 0 8a4 4 0 1 1 0-8z"));
        shapesButton.setTooltip(new Tooltip("Choose a shape to draw"));
        String[][] shapes = {
            {"Square", "square", "M3 3h18v18H3z"},
            {"Circle", "circle", "M12 2a10 10 0 1 1 0 20a10 10 0 1 1 0-20z"},
            {"Rectangle", "rectangle", "M2 5h20v14H2z"},
            {"Ellipse", "ellipse", "M12 4a11 8 0 1 1 0 16a11 8 0 1 1 0-16z"},
            {"Triangle", "triangle", "M12 2L23 21H1z"},
            {"Right Triangle", "righttriangle", "M3 3v18h18z"},
            {"Star", "star", "M12 2l3 7h7l-5.5 5 2.1 8L12 17.8 5.4 22l2.1-8L2 9h7z"},
            {"Diamond", "diamond", "M12 2l10 10-10 10L2 12z"},
            {"Polygon", "polygon", "M8 2h8l7 8-3 11H4L1 10z"}
        };
        for (String[] shape : shapes) {
            MenuItem shapeItem = new MenuItem(shape[0]);
            shapeItem.setGraphic(makeIcon(shape[2]));
            shapeItem.setOnAction(e -> setTool(shape[1]));
            shapesButton.getItems().add(shapeItem);
        }

        // Adds label to show current tool
        toolLabel = new Label("Current Tool: " + currentTool);

        // Adds the styling for the toolbar
        toolbar = new HBox(5);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(6));
        toolbar.setStyle("-fx-background-color: #d3d3d3;");
        widthSlider.setPrefWidth(100);
        colorPicker.setPrefWidth(105);

        // Adds all the items into the main toolbar
        toolbar.getChildren().addAll(
            pencilButton, lineButton, eraserButton, textButton,
            grabberButton, selectButton, moveButton,
            refreshButton, undoButton, redoButton, shapesButton,
            widthLabel, widthSlider, colorPicker, dashedCheckBox
        );
        setTool("pencil");

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (currentTool.equals("eraser")) {
                gc.setStroke(Color.WHITE);
            }

            if (currentTool.equals("pencil")
                    || currentTool.equals("eraser")) {
                saveUndoState();
                startX = e.getX();
                startY = e.getY();
            }

            // Gets the X and Y and takes a snapshot for the shape
            if (isShapeTool(currentTool)) {
                saveUndoState();
                startX = e.getX();
                startY = e.getY();
                shapeSnapshot = takeSnapshot();
            }

            // Sends selection mouse presses to Selection.java
            if (currentTool.equals("select") || currentTool.equals("move")) {
                selection.mousePressed(e.getX(), e.getY());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (currentTool.equals("eraser")) {
                gc.setStroke(Color.WHITE);
            }

            if (currentTool.equals("pencil")
                    || currentTool.equals("eraser")) {
                gc.setLineDashes((double[]) null);

                gc.strokeLine(
                    startX,
                    startY,
                    e.getX(),
                    e.getY()
                );

                startX = e.getX();
                startY = e.getY();
                markChanged();
            }

            // Calls the preview function for the current shape
            if (isShapeTool(currentTool)) {
                previewShape(e.getX(), e.getY());
            }

            // Sends selection dragging to Selection.java
            if (currentTool.equals("select")
                    || currentTool.equals("move")) {
                selection.mouseDragged(e.getX(), e.getY());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (currentTool.equals("pencil") || currentTool.equals("eraser")) {
                logAction(currentTool + " stroke drawn");
            }
            if (isShapeTool(currentTool)) {
                previewShape(e.getX(), e.getY());
                markChanged();
                logAction(currentTool + " drawn");
            }

            // Finishes selecting or moving a piece
            if (currentTool.equals("select")
                    || currentTool.equals("move")) {
                selection.mouseReleased(e.getX(), e.getY());
                logAction(currentTool + " interaction");
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentTool.equals("line")) {
                if (!lineStarted) {
                    startX = e.getX();
                    startY = e.getY();
                    lineStarted = true;
                } else {
                    saveUndoState();

                    gc.setLineDashes(
                        dashed ? new double[]{10, 10} : null
                    );

                    gc.strokeLine(
                        startX,
                        startY,
                        e.getX(),
                        e.getY()
                    );

                    gc.setLineDashes((double[]) null);
                    lineStarted = false;
                    markChanged();
                    logAction("Line drawn");
                }
            }

            // Text tool
            if (currentTool.equals("text")) {
                double textX = e.getX();
                double textY = e.getY();

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add Text");
                dialog.setHeaderText("Enter text:");

                dialog.showAndWait().ifPresent(text -> {
                    saveUndoState();

                    gc.setFill(colorPicker.getValue());
                    gc.setFont(Font.font(24));
                    gc.fillText(text, textX, textY);

                    markChanged();
                    logAction("Text inserted");
                });
            }

            // Color grabber
            if (currentTool.equals("colorGrabber")) {
                WritableImage image = takeSnapshot();
                PixelReader pixelReader = image.getPixelReader();

                double scaleX = image.getWidth() / canvas.getWidth();
                double scaleY = image.getHeight() / canvas.getHeight();

                int px = (int) Math.min(
                    image.getWidth() - 1,
                    Math.max(0, e.getX() * scaleX)
                );

                int py = (int) Math.min(
                    image.getHeight() - 1,
                    Math.max(0, e.getY() * scaleY)
                );

                Color pickedColor = pixelReader.getColor(px, py);

                colorPicker.setValue(pickedColor);
                gc.setStroke(pickedColor);

                setTool(lastTool);
            }
        });

        // Creates a drawing pane to organize the canvas and toolbar
        drawingPane = new BorderPane();
        drawingPane.setTop(toolbar);
        drawingPane.setCenter(new StackPane(canvas, overlay));
    }

    /**
     * Private method that draws a preview of the selected shape.
     * @param curX the current x coordinate
     * @param curY the current y coordinate
     */
    private void previewShape(double curX, double curY) {
        if (shapeSnapshot != null) {
            drawSnapshot(shapeSnapshot);
        }

        gc.setLineDashes(dashed ? new double[]{10, 10} : null);
        gc.setStroke(colorPicker.getValue());

        double x = Math.min(startX, curX);
        double y = Math.min(startY, curY);
        double w = Math.abs(curX - startX);
        double h = Math.abs(curY - startY);

        Shapes.drawShape(
            gc,
            currentTool,
            x,
            y,
            w,
            h,
            numberOfSides,
            numberOfStarPoints
        );

        gc.setLineDashes((double[]) null);
    }

    /**
     * Asks the user how many sides the polygon tool should draw.
     */
    public void promptPolygonSides() {
        TextInputDialog dialog =
            new TextInputDialog(String.valueOf(numberOfSides));

        dialog.setTitle("Polygon");
        dialog.setHeaderText("Number of sides (3 or more):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int sides = Integer.parseInt(input.trim());

                if (sides >= 3) {
                    numberOfSides = sides;
                } else {
                    showSidesError();
                }
            } catch (NumberFormatException ex) {
                showSidesError();
            }
        });
    }

    /**
     * Asks the user how many points the star tool should draw.
     */
    public void promptStarPoints() {
        TextInputDialog dialog =
            new TextInputDialog(String.valueOf(numberOfStarPoints));

        dialog.setTitle("Star");
        dialog.setHeaderText("Number of points (4 or more):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int points = Integer.parseInt(input.trim());

                if (points >= 4) {
                    numberOfStarPoints = points;
                } else {
                    showStarPointsError();
                }
            } catch (NumberFormatException ex) {
                showStarPointsError();
            }
        });
    }

    /**
     * Error box for a bad star point count.
     */
    private void showStarPointsError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Points");
        alert.setHeaderText("Invalid number of points");
        alert.setContentText(
            "Please enter a whole number that is 4 or more."
        );
        alert.showAndWait();
    }

    /**
     * Error box for a bad polygon side count.
     */
    private void showSidesError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Sides");
        alert.setHeaderText("Invalid number of sides");
        alert.setContentText(
            "Please enter a whole number that is 3 or more."
        );
        alert.showAndWait();
    }

    /**
     * Fills the whole canvas with white so it is a blank image.
     */
    private void fillWhite() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Refresh button to get rid of everything on the canvas.
     * Also asks the user for confirmation first.
     */
    public void refresh() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will clear the entire canvas.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveUndoState();
            selection.clearSelection();
            fillWhite();
            markChanged();
            logAction("Canvas cleared");
        }
    }

    /**
     * Saves the current canvas onto the undo stack.
     */
    private void saveUndoState() {
        history.saveState(canvas);
    }

    /**
     * Public method that undoes the last action.
     */
    public void undo() {
        history.undo(canvas);
        selection.clearSelection();
        logAction("Undo");
        markChanged();
    }

    /**
     * Public method that redoes the last undone action.
     */
    public void redo() {
        history.redo(canvas);
        selection.clearSelection();
        logAction("Redo");
        markChanged();
    }

    /**
     * Public method that copies the current selection.
     */
    public void copySelection() {
        selection.copySelection();
        logAction("Copy selection");
    }

    /**
     * Public method that pastes the copied selection.
     */
    public void pasteSelection() {
        selection.pasteSelection();
        logAction("Paste selection");
        setTool("select");
    }

    /**
     * Private method that clears the current selection.
     */
    private void clearSelection() {
        selection.clearSelection();
    }

    /**
     * Rotates the selected piece or the entire canvas.
     * @param degrees clockwise angle in degrees
     * @param selectedOnly true to change just a selection
     */
    public void rotate(int degrees, boolean selectedOnly) {
        if (selectedOnly && !selection.hasSelection()) return;
        if (selectedOnly) {
            selection.rotateSelected(degrees);
        } else {
            saveUndoState();
            selection.clearSelection();
            Rotation.rotateCanvas(canvas, degrees);
            markChanged();
        }
        logAction("Rotate " + (selectedOnly ? "selection" : "canvas") + " " + degrees + " degrees");
    }

    /**
     * Flips the selected piece or the entire canvas.
     * @param horizontal true for left/right, false for top/bottom
     * @param selectedOnly true to change just a selection
     */
    public void flip(boolean horizontal, boolean selectedOnly) {
        if (selectedOnly && !selection.hasSelection()) return;
        if (selectedOnly) {
            selection.flipSelected(horizontal);
        } else {
            saveUndoState();
            selection.clearSelection();
            Rotation.flipCanvas(canvas, horizontal);
            markChanged();
        }
        logAction("Flip " + (selectedOnly ? "selection" : "canvas")
                + (horizontal ? " horizontally" : " vertically"));
    }

    /**
     * Checks whether a selection exists in this drawing.
     * @return true if there is a selected piece
     */
    public boolean hasSelection() {
        return selection.hasSelection();
    }

    /**
     * Sets the filename or tab name used when logging drawing events.
     * @param source function that returns the current file or tab name
     */
    public void setLoggingSource(Supplier<String> source) {
        loggingSource = source == null ? () -> "new tab" : source;
    }

    /**
     * Writes one user event to the background logging thread.
     * @param action the user action to log
     */
    private void logAction(String action) {
        Logging.log(loggingSource.get(), action);
    }

    /**
     * Creates a small vector graphic for tool buttons.
     * @param path the SVG path for the icon
     * @return the icon used on the button
     */
    private SVGPath makeIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.web("#215f42"));
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);
        return icon;
    }

    /**
     * Creates a drawing tool icon button with its hover tooltip.
     * @param tool the drawing tool to activate
     * @param tooltip text shown when the mouse is over the button
     * @param path the SVG path for its icon
     * @return the configured button
     */
    private Button createToolButton(String tool, String tooltip, String path) {
        Button button = new Button();
        button.setGraphic(makeIcon(path));
        button.setTooltip(new Tooltip(tooltip));
        button.setPrefSize(34, 34);
        button.setMinSize(34, 34);
        button.setOnAction(e -> setTool(tool));
        return button;
    }

    /**
     * Public method for setting the function that marks the drawing as changed.
     * @param onChange the function called after a drawing change
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    /**
     * Private method that tells the tab when the canvas has changed.
     */
    private void markChanged() {
        onChange.run();
    }

    /**
     * Public method that gets the toolbar above the canvas.
     * @return the toolbar for this drawing
     */
    public HBox getToolbar() {
        return toolbar;
    }

    /**
     * Public method for getting the DrawingPane.
     * @return the drawing pane
     */
    public BorderPane getDrawingPane() {
        return drawingPane;
    }

    /**
     * Public method for getting the canvas.
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Public method for loading an image after refreshing the canvas.
     * @param image the image that is being loaded
     */
    public void loadImage(Image image) {
        saveUndoState();
        selection.clearSelection();
        fillWhite();
        gc.drawImage(image, 0, 0);
        markChanged();
    }
}