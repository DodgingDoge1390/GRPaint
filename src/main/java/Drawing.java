import java.util.function.Supplier;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.text.Font;
/**
 * Manages the canvas, drawing tools, and user interactions in GRPaint.
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
    private ShapePreview shapePreview;
    private CanvasOperations canvasOperations;
    private final DrawingHistory history = new DrawingHistory();

    private int numberOfSides = 5;
    private int numberOfStarPoints = 5;

    private Runnable onChange = () -> {};
    private Supplier<String> loggingSource = () -> "new tab";

    // Overlay canvas that only draws the dashed selection outline
    private Canvas overlay;

    // Handles selecting, moving, copying, and pasting pieces
    private Selection selection;

    /**
     * Activates the selected drawing tool.
     * @param tool the name of the tool to select
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
     * Checks whether a tool draws shapes.
     * @param tool the tool name to check
     * @return true if the tool draws shapes
     */
    private boolean isShapeTool(String tool) {
        return tool.equals("square") || tool.equals("circle")
            || tool.equals("rectangle") || tool.equals("ellipse")
            || tool.equals("triangle") || tool.equals("star")
            || tool.equals("righttriangle") || tool.equals("diamond")
            || tool.equals("polygon");
    }

    /**
     * Creates a drawing canvas and its toolbar.
     * @param width the canvas width in pixels
     * @param height the canvas height in pixels
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
        CanvasOperations.fillWhite(canvas);
        shapePreview = new ShapePreview(canvas, gc);

        // Creates the selection class
        selection = new Selection(
            canvas,
            gc,
            overlay,
            this::saveUndoState,
            this::markChanged
        );
        canvasOperations = new CanvasOperations(canvas, gc, selection, history, this::markChanged, this::logAction);

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
        Button pencilButton = ToolButtonFactory.createToolButton(this::setTool, "pencil", "Pencil: draw freehand", "M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z M20.7 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        Button lineButton = ToolButtonFactory.createToolButton(this::setTool, "line", "Line: click two points", "M4 19L19 4l1 1L5 20z");
        Button eraserButton = ToolButtonFactory.createToolButton(this::setTool, "eraser", "Eraser: remove parts of drawing", "M15.14 3l5.86 5.86a2 2 0 0 1 0 2.83L12.69 20H5a2 2 0 0 1-1.41-.59L1 16.83a2 2 0 0 1 0-2.83L12.31 3a2 2 0 0 1 2.83 0z M6 18h5.86L19.6 10.3l-5.86-5.86L2.4 15.41z");
        Button textButton = ToolButtonFactory.createToolButton(this::setTool, "text", "Text: click to add text", "M4 4h16v4h-2V6h-5v12h3v2H8v-2h3V6H6v2H4z");
        Button grabberButton = ToolButtonFactory.createToolButton(this::setTool, "colorGrabber", "Color Grabber: pick a color from the canvas", "M20.71 3.29a1 1 0 0 0-1.42 0L16.5 6.08l-1.59-1.59-1.41 1.41L15.09 7.5l-9.9 9.9a3 3 0 0 0-.83 1.55L4 21l2.05-.36a3 3 0 0 0 1.55-.83l9.9-9.9 1.59 1.59 1.41-1.41-1.59-1.59 2.79-2.79a1 1 0 0 0 0-1.42z");
        Button selectButton = ToolButtonFactory.createToolButton(this::setTool, "select", "Select: drag to select part of the image", "M3 3h7v2H5v5H3z M14 3h7v7h-2V5h-5z M3 14h2v5h5v2H3z M19 14h2v7h-7v-2h5z");
        Button moveButton = ToolButtonFactory.createToolButton(this::setTool, "move", "Move: drag the selected piece", "M12 2l4 4h-3v5h5V8l4 4-4 4v-3h-5v5h3l-4 4-4-4h3v-5H6v3l-4-4 4-4v3h5V6H8z");

        // Shapes dropdown in the toolbar, each shape has its own icon.
        MenuButton shapesButton = new MenuButton();
        shapesButton.setGraphic(ToolButtonFactory.makeIcon("M3 3h8v8H3z M14 3a4 4 0 1 1 0 8a4 4 0 1 1 0-8z"));
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
            shapeItem.setGraphic(ToolButtonFactory.makeIcon(shape[2]));
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
                shapePreview.begin();
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
                WritableImage image = CanvasSnapshots.capture(canvas);
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

        // Group preserves pixel-perfect alignment between the drawing canvas and its selection overlay, including after a canvas resize.
        Group canvasLayers = new Group(canvas, overlay);

        // Center canvases smaller than the visible area. Keep the holder at least as large as the canvas so larger canvases still get scrollbars.
        StackPane centeredCanvas = new StackPane(canvasLayers);
        centeredCanvas.setAlignment(Pos.CENTER);
        centeredCanvas.minWidthProperty().bind(canvas.widthProperty());
        centeredCanvas.minHeightProperty().bind(canvas.heightProperty());

        ScrollPane canvasScrollPane = new ScrollPane(centeredCanvas);
        canvasScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        canvasScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        canvasScrollPane.setFitToWidth(true);
        canvasScrollPane.setFitToHeight(true);
        canvasScrollPane.setPannable(false);

        // Toolbar remains visible while the canvas itself scrolls.
        drawingPane = new BorderPane();
        drawingPane.setTop(toolbar);
        drawingPane.setCenter(canvasScrollPane);
    }

    /**
     * Previews the shape while dragging.
     * @param curX the current horizontal mouse position
     * @param curY the current vertical mouse position
     */
    private void previewShape(double curX, double curY) {
        shapePreview.preview(startX, startY, curX, curY, dashed, colorPicker.getValue(), currentTool, numberOfSides, numberOfStarPoints);
    }

    /**
     * Asks the user for the number of polygon sides.
     */
    public void promptPolygonSides() {
        numberOfSides = ShapePrompts.askPolygonSides(numberOfSides);
    }

    /**
     * Asks the user for the number of star points.
     */
    public void promptStarPoints() {
        numberOfStarPoints = ShapePrompts.askStarPoints(numberOfStarPoints);
    }

    /**
     * Asks to clear the canvas and saves its undo state.
     */
    public void refresh() { canvasOperations.clearWithConfirmation(); }

    /**
     * Stores the current image in the undo history.
     */
    private void saveUndoState() { canvasOperations.saveUndoState(); }

    /**
     * Restores the previous canvas state.
     */
    public void undo() { canvasOperations.undo(); }

    /**
     * Restores the next canvas state.
     */
    public void redo() { canvasOperations.redo(); }

    /**
     * Copies the currently selected image region.
     */
    public void copySelection() {
        selection.copySelection();
        logAction("Copy selection");
    }

    /**
     * Pastes the copied image region.
     */
    public void pasteSelection() {
        selection.pasteSelection();
        logAction("Paste selection");
        setTool("select");
    }

    /**
     * Rotates the whole canvas or the selected region.
     * @param degrees clockwise rotation angle
     * @param selectedOnly true to rotate only the selection
     */
    public void rotate(int degrees, boolean selectedOnly) {
        Rotation.rotateDrawing(canvas, selection, history, this::markChanged, this::logAction, degrees, selectedOnly);
    }

    /**
     * Flips the canvas or a selected region.
     * @param horizontal true to flip left to right
     * @param selectedOnly true to flip only the selection
     */
    public void flip(boolean horizontal, boolean selectedOnly) {
        Rotation.flipDrawing(canvas, selection, history, this::markChanged, this::logAction, horizontal, selectedOnly);
    }

    /**
     * Checks whether an image region is selected.
     * @return true if a selection exists
     */
    public boolean hasSelection() {
        return selection.hasSelection();
    }

    /**
     * Sets the name used in drawing event logs.
     * @param source supplier of the current image or tab name
     */
    public void setLoggingSource(Supplier<String> source) {
        loggingSource = source == null ? () -> "new tab" : source;
    }

    /**
     * Records an action in the event log.
     * @param action the action description
     */
    private void logAction(String action) {
        Logging.log(loggingSource.get(), action);
    }

    /**
     * Sets the callback for drawing changes.
     * @param onChange callback run after changes
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    /**
     * Notifies listeners that the drawing changed.
     */
    private void markChanged() {
        onChange.run();
    }

    /**
     * Returns the drawing toolbar.
     * @return toolbar containing drawing tools
     */
    public HBox getToolbar() {
        return toolbar;
    }

    /**
     * Returns the full drawing layout.
     * @return the pane containing toolbar and canvas
     */
    public BorderPane getDrawingPane() {
        return drawingPane;
    }

    /**
     * Returns the active drawing canvas.
     * @return canvas used for editing
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Loads an image onto the canvas.
     * @param image the image to load
     */
    public void loadImage(Image image) { canvasOperations.loadImage(image); }
}
