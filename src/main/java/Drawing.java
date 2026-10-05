import java.util.Optional;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform; 
import javafx.stage.Screen;              
import java.util.Stack;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;

/**
 * Public Class for the Drawing functionality in GRPaint
 * Defines and calculates what each Tool and Shape does 
 * when the user clicks on it
 */
public class Drawing { //start of the class for tools
    private Canvas canvas; 
    private GraphicsContext gc; 
    private double startX; 
    private double startY; 
    private BorderPane drawingPane; 
    private String currentTool = ""; 
    private String lastTool = "pencil"; 
    private boolean lineStarted = false; 
    private boolean dashed = false; 
    private ColorPicker colorPicker;
    private Label widthLabel;
    private WritableImage shapeSnapshot; 
    private Stack<WritableImage> undoStack = new Stack<>();
    private Stack<WritableImage> redoStack = new Stack<>();
    private static final int MAX_UNDO = 50; // Max number of undo steps kept so snapshots don't eat all the memory
    private int numberOfSides = 5; // Number of sides used by the regular polygon tool
    private Canvas overlay; //Overlay canvas that only draws the dashed selection outline
    private GraphicsContext overlayGc;
    private WritableImage selectedImage;   // pixels currently selected
    private double selX, selY, selW, selH; // position and size of the selection (in canvas units)
    private double selStartX, selStartY;   // where the user started dragging the selection
    private boolean selecting = false;     // true while dragging out a selection box
    private boolean moving = false;        // true while dragging a selection around
    private double moveGrabX, moveGrabY;   // where inside the selection the user grabbed it
    private boolean floating = false;      // true while the selection is "lifted" off the canvas (moving or just pasted)
    private WritableImage floatBase;       // canvas WITHOUT the lifted selection, redrawn under it every frame
    private static WritableImage clipboardImage;
    private static double clipW, clipH;

    /**
     * Method that sets the Tool to the current selected tool
     * @param tool the current tool selected by user
     */
    public void setTool(String tool) { //method for constructing the tool
        if (!tool.equals("colorGrabber")) {
            lastTool = tool;
        }
        currentTool = tool; //sets the current tool to tool
        lineStarted = false; //sets linestarted to false

        if (!tool.equals("eraser")) {
            gc.setStroke(colorPicker.getValue());
        }

        // The lifted selection is put down when the user leaves the select/move tools
        if (!tool.equals("select") && !tool.equals("move")) {
            floating = false;
        }

        // Polygon asks how many sides it should have
        if (tool.equals("polygon")) {
            promptPolygonSides();
        }

        // Only show the selection outline while select/move is active
        if (tool.equals("select") || tool.equals("move")) {
            redrawOutline();
        } else {
            clearOverlay();
        }
    }

    /**
     * Method that checks if the user selected a shape
     * @param tool the current tool selected by the user
     */
    private boolean isShapeTool(String tool) {
        // CHANGED: added righttriangle, diamond, and polygon
        return tool.equals("square") || tool.equals("circle") || tool.equals("rectangle")
            || tool.equals("ellipse") || tool.equals("triangle") || tool.equals("star")
            || tool.equals("righttriangle") || tool.equals("diamond") || tool.equals("polygon");
    }

    /**
     * Gets how many real screen pixels there are per canvas unit (1 on a normal screen, 1.25/1.5/2 on scaled screens)
     * @return the render scale
     */
    private double getRenderScale() {
        if (canvas.getScene() != null && canvas.getScene().getWindow() != null) {
            return canvas.getScene().getWindow().getOutputScaleX();
        }
        return Screen.getPrimary().getOutputScaleX();
    }

    /**
     * Takes a snapshot of the canvas at the real screen resolution.
     * canvas.snapshot(null, null) only gives an image at 1x size. On a scaled screen
     * that small image was stretched back over the canvas every time a shape was previewed/undone,
     * which is what made the old drawing blurry. Scaling the snapshot keeps every pixel.
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
     * The destination size is given so the full resolution snapshot lines up 1 to 1 with the canvas.
     * @param image the snapshot to draw
     */
    private void drawSnapshot(WritableImage image) {
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Public method for setting the drawing for the canvas and the tools
     * Sets toolbar, colorpicker, and refresh button
     * On a mouse dragged and pencil or eraser it will take the point of the start to the position of the mouse dragged
     * On a mouse clicked and it is a line it will take the position of the click if the user clicks again then a line will be drawn
     * On a mouse pressed and it is a pencil or an eraser it will take check if the line has been started and take the point the user pressed and create a line also creates a snapshot of it too
     * On a mouse released and it is a shape it will call the preview function for shape
     * @param width takes the width of the drawing
     * @param height takes the height of the drawing
     */
    public Drawing(double width, double height) { 
        canvas = new Canvas(width, height);

        gc = canvas.getGraphicsContext2D(); 

        // Overlay canvas for the selection outline, mouseTransparent so clicks reach the real canvas
        overlay = new Canvas(width, height);
        overlay.setMouseTransparent(true);
        overlayGc = overlay.getGraphicsContext2D();

        //sets the tools color and width
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); 

        // Blank white starting image (before this the canvas was transparent,
        // which saved as black in jpg files)
        fillWhite();
        
        //Sets the colorpicker
        colorPicker = new ColorPicker(Color.BLACK); 
        colorPicker.setOnAction(e -> {gc.setStroke(colorPicker.getValue());}); 

        //Sets the slider and adds a listener
        widthLabel = new Label("Outline Width: 2px"); 
        Slider widthSlider = new Slider(0, 50, 2); 
        widthSlider.setShowTickMarks(true); 
        widthSlider.setShowTickLabels(true); 
        widthSlider.setMajorTickUnit(5); 
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> { 
            gc.setLineWidth(newValue.doubleValue()); 
            widthLabel.setText("Outline Width: " + newValue.intValue() + "px"); 
        });

        //Adds a checkbox for the dashed outline
        CheckBox dashedCheckBox = new CheckBox("Dashed Outline"); 
        dashedCheckBox.setOnAction(e -> dashed = dashedCheckBox.isSelected()); 
        
        //adds a refresh button
        Button refreshButton = new Button("Refresh"); 
        refreshButton.setOnAction(e -> refresh()); 

        // Undo and redo buttons in the toolbar
        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undo());
        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> redo());

        // Shapes dropdown in the toolbar
        MenuButton shapesButton = new MenuButton("Shapes");
        String[][] shapes = {
            {"Square", "square"}, {"Circle", "circle"}, {"Rectangle", "rectangle"},
            {"Ellipse", "ellipse"}, {"Triangle", "triangle"}, {"Right Triangle", "righttriangle"},
            {"Star", "star"}, {"Diamond", "diamond"}, {"Polygon", "polygon"}
        };
        for (String[] shape : shapes) {
            MenuItem shapeItem = new MenuItem(shape[0]);
            shapeItem.setOnAction(e -> {
                setTool(shape[1]);
                shapesButton.setText("Shape: " + shape[0]); //shows which shape is picked
            });
            shapesButton.getItems().add(shapeItem);
        }

        //Adds the syling for the toolbar
        HBox toolbar = new HBox(10); 
        toolbar.setStyle("-fx-background-color: #d3d3d3;"); 

        //adds all the items into the main toolbar
        toolbar.getChildren().addAll(refreshButton, undoButton, redoButton, shapesButton, widthLabel, widthSlider, colorPicker, dashedCheckBox); 

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { 
            if (currentTool.equals("eraser")) {
                gc.setStroke(Color.WHITE);
            }
            if (currentTool.equals("pencil") || currentTool.equals("eraser")) {
                saveUndoState(); //one undo step per stroke
                startX = e.getX(); 
                startY = e.getY(); 
            } 

            //if the current tool is a Shape it will get the X,Y and send a snapshot to the user
            if (isShapeTool(currentTool)) { 
                saveUndoState();
                startX = e.getX();
                startY = e.getY();
                shapeSnapshot = takeSnapshot(); // sharp snapshot
            } 

            if (currentTool.equals("select") || currentTool.equals("move")) {
                if (selectedImage != null && insideSelection(e.getX(), e.getY())) {
                    startMove(e.getX(), e.getY());
                } else if (currentTool.equals("select")) {
                    floating = false; //whatever was lifted is already sitting on the canvas
                    selStartX = e.getX();
                    selStartY = e.getY();
                    selecting = true;
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> { 
            if (currentTool.equals("eraser")) {
                gc.setStroke(Color.WHITE);
            }
            if (currentTool.equals("pencil") || currentTool.equals("eraser")) {
                gc.setLineDashes((double[]) null);
                gc.strokeLine( 
                    startX, 
                    startY, 
                    e.getX(),
                    e.getY()
                ); 

                startX = e.getX(); 
                startY = e.getY();
            } 

            //If the currentTool is a shape it will call the preview function of the current X,Y
            if (isShapeTool(currentTool)) { 
                previewShape(e.getX(), e.getY()); 
            } 

            // Live outline while dragging out a selection
            if (selecting) {
                drawOutline(Math.min(selStartX, e.getX()), Math.min(selStartY, e.getY()),
                            Math.abs(e.getX() - selStartX), Math.abs(e.getY() - selStartY));
            }

            //Live draw while moving a selection
            if (moving) {
                selX = e.getX() - moveGrabX;
                selY = e.getY() - moveGrabY;
                drawSnapshot(floatBase); //canvas without the selection
                gc.drawImage(selectedImage, selX, selY, selW, selH); //selection at its new spot
                drawOutline(selX, selY, selW, selH);
            }
        }); 

        //On the mouse released if the tool is a shape it will call the preview function
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { 
            if (isShapeTool(currentTool)) { 
                previewShape(e.getX(), e.getY());
            }

            // Finish the selection by copying the pixels inside the box
            if (selecting) {
                selecting = false;
                finishSelection(e.getX(), e.getY());
            }

            // Done moving (the selection stays lifted so it can be moved again without damaging what is under it)
            if (moving) {
                moving = false;
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
                    gc.setLineDashes(dashed ? new double[]{10, 10} : null); 
                    gc.strokeLine( 
                        startX, 
                        startY,
                        e.getX(), 
                        e.getY() 
                    ); 
                    lineStarted = false;
                }
            }

            // The text tool code used to be nested inside the line tool's if
            // (so it could never run) and there was a second copy in previewShape.
            // It now lives here on its own.
            if (currentTool.equals("text")) {
                startX = e.getX();
                startY = e.getY();

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add Text");
                dialog.setHeaderText("Enter text:");

                dialog.showAndWait().ifPresent(text -> {
                    saveUndoState();

                    gc.setFill(colorPicker.getValue());
                    gc.setFont(Font.font(24));
                    gc.fillText(text, startX, startY);
                });
            }

            //ColorGrabber
            if (currentTool.equals("colorGrabber")) {
                //creates a snapshot of the canvas
                WritableImage image = takeSnapshot(); // CHANGED: full resolution snapshot
                //creates a pixelreader to get the color of the pixel
                PixelReader pixelReader = image.getPixelReader();

                // CHANGED: the snapshot can be bigger than the canvas, so the click is scaled to match
                double scaleX = image.getWidth() / canvas.getWidth();
                double scaleY = image.getHeight() / canvas.getHeight();
                int px = (int) Math.min(image.getWidth() - 1, Math.max(0, e.getX() * scaleX));
                int py = (int) Math.min(image.getHeight() - 1, Math.max(0, e.getY() * scaleY));

                //gets the coordinates of the click to get the color of the pixel
                Color pickedColor = pixelReader.getColor(px, py);
                //sets the colorpicker and the gc stroke to the pixel color
                colorPicker.setValue(pickedColor);
                gc.setStroke(pickedColor);
                currentTool = lastTool; 
            }
        }); 
        //creates a drawingpane to organize the canvas and the toolbar
        drawingPane = new BorderPane();
        drawingPane.setTop(toolbar); 
        drawingPane.setCenter(new StackPane(canvas, overlay)); 
    }

    /**
     * Private method that draws an image of the current x,y to the desired shape
     * If it is dashed it will check for it and also the colorpicker
     * Takes the four points of the user's input, which is top right, top left, bottom right, bottom left
     * if the shape goes below the bottom right or left, then the bottom right or left changes
     * Switch case used to get each of the six shapes
     * each shape has its own algorithm to draw.
     * @param curX the current x coordinate
     * @param curY the current y coordinate
     */
    private void previewShape(double curX, double curY) {
        if (shapeSnapshot != null) {
            drawSnapshot(shapeSnapshot); // CHANGED: draws the sharp snapshot 1 to 1 (this was the blur bug)
        }

        gc.setLineDashes(dashed ? new double[]{10, 10} : null); 
        gc.setStroke(colorPicker.getValue()); 

        //sets all four points
        double x = Math.min(startX, curX); //takes the minimum of the start of the preview x vs the current x (leftmost x point)
        double y = Math.min(startY, curY); //takes the minimum of the start of the preview y vs the current y (topmost y point)
        double w = Math.abs(curX - startX); //takes the absolute value of the current x - the start of x  (width)
        double h = Math.abs(curY - startY); //takes the absolute value of the current y - the start of y (height)

        //use switch case to draw each shape if the case of the currenttool is one of the shapes then it will draw the shape 
        switch (currentTool) {
            case "rectangle":
                gc.strokeRect(x, y, w, h); //easiest to draw since there is a function for it
                break;
            case "square": {
                double side = Math.max(w, h); //takes the greater of the two values
                gc.strokeRect(x, y, side, side); //since square is a rectangle with two of the same sides, we can just set those sides to the same which represent the width and the height
                break;
            }
            case "ellipse":
                gc.strokeOval(x, y, w, h); //easy to draw since there is a function for it
                break;
            case "circle": {
                double diameter = Math.max(w, h); //takes the diameter using whicheveer is wider the width or the height
                gc.strokeOval(x, y, diameter, diameter); //since the an circle is a ellipse but with the same diameter, we can just set the width and the height to the same
                break;
            }
            case "triangle": {
                double[] xs = {x, x + w, x + w / 2}; //finds the three coordinates of the x and puts it into an array
                double[] ys = {y + h, y + h, y}; //finds the three y coordinates and puts them into an array
                gc.strokePolygon(xs, ys, 3); //(x,y+h) = bottom left (x+w,y+h) bottom right, ((x+w)/2, y) = middle top draws the triangle at these points
                break;
            }
            case "righttriangle": { // CHANGED: added braces so xs/ys stay scoped to this case
                double[] xs = {x, x, x + w}; //right angle sits in the bottom left corner
                double[] ys = {y, y + h, y + h};
                gc.strokePolygon(xs, ys, 3);
                break;
            }
            case "star": 
                drawStar(x, y, w, h); //takes the variables to the starr function
                break;
            case "diamond": { //The extra shape of my choice
                double[] xs = {x + w / 2, x + w, x + w / 2, x}; //top, right, bottom, left points
                double[] ys = {y, y + h / 2, y + h, y + h / 2};
                gc.strokePolygon(xs, ys, 4);
                break;
            }
            case "polygon":
                drawPolygon(x, y, w, h, numberOfSides);
                break;
            // Removed the "text" case from here, text is handled in the click handler now
            default:
                break;
            
        } 
    } 

    /**
     * Private method for drawing a star
     * Using the x,y,w, and h, we can calculate the center of the star and 
     * work out to get all the points in the star
     * We create 10 differnet x,y coordinates for the star and draw the polygon
     * after it has been solved
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param w the width
     * @param h the height
     */
    private void drawStar(double x, double y, double w, double h) { //draws a 5 point star inside the bounding box, this is the extra bonus shape
        double cx = x + w / 2; //center x
        double cy = y + h / 2; //center y
        double outerRadius = Math.max(w, h) / 2; //outer point radius
        double innerRadius = outerRadius / 2.5; //inner point radius

        double[] xs = new double[10]; //new array for the x coordinates
        double[] ys = new double[10]; //new array for the y coordinates
        for (int i = 0; i < 10; i++) { //alternates between outer and inner points to make the star shape
            double radius = (i % 2 == 0) ? outerRadius : innerRadius; //if i is even then use the outer radius ifelse use the innerradius
            double angle = Math.PI / 2 * 3 + (Math.PI * i / 5); //calculates the angle around the center
            xs[i] = cx + radius * Math.cos(angle); //start at the center of and move outward according to the radius and angle
            ys[i] = cy + radius * Math.sin(angle); //same thing and they give us the x and y coordinate of the star points
        } 
        gc.strokePolygon(xs, ys, 10);
    }

    /**
     * Private method for drawing a regular polygon with any number of sides
     * Places the points evenly around a circle inside the bounding box
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param w the width
     * @param h the height
     * @param sides the number of sides (3 or more)
     */
    private void drawPolygon(double x, double y, double w, double h, int sides) {
        double centerX = x + w / 2;
        double centerY = y + h / 2;
        double radius = Math.min(w, h) / 2;

        double[] xs = new double[sides];
        double[] ys = new double[sides];

        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides - Math.PI / 2;

            xs[i] = centerX + radius * Math.cos(angle);
            ys[i] = centerY + radius * Math.sin(angle);
        }
        gc.strokePolygon(xs, ys, sides);
    }

    /**
     * Asks the user how many sides the polygon tool should draw
     * Anything under 3 or not a number is rejected and the old value is kept
     */
    public void promptPolygonSides() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(numberOfSides));
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
     * Error box for a bad polygon side count
     */
    private void showSidesError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Sides");
        alert.setHeaderText("Invalid number of sides");
        alert.setContentText("Please enter a whole number that is 3 or more.");
        alert.showAndWait();
    }

    /**
     * Fills the whole canvas with white so it is a blank image
     */
    private void fillWhite() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Refresh button to get rid of everything on the canvas
     * Also used for the Clear Canvas menu item, asks "are you sure" first
     */
    public void refresh() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will clear the entire canvas.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveUndoState();
            clearSelection(); // NEW
            // CHANGED: fills white instead of clearRect so the canvas stays a blank white image
            fillWhite();
        }
    }

    /**
     * Saves the current canvas onto the undo stack (and empties the redo stack)
     */
    private void saveUndoState() {
        WritableImage snapshot = takeSnapshot(); // CHANGED: sharp snapshot
        undoStack.push(snapshot);
        if (undoStack.size() > MAX_UNDO) {
            undoStack.remove(0);
        }
        redoStack.clear();
    }

    /**
     * Public method that undoes the last action through taking a snapshot
     * and drawing it back on the canvas
     */
    public void undo() {
        if (!undoStack.empty()) {
            WritableImage current = takeSnapshot();
            redoStack.push(current);

            WritableImage previous = undoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            drawSnapshot(previous); 
            clearSelection(); 
        }
    }
    
    /**
     * Public method that redoes the last undone action through taking a snapshot
     * and drawing it back on the canvas
     */
    public void redo() {
        if (!redoStack.empty()) {
            WritableImage current = takeSnapshot(); // CHANGED: sharp snapshot
            undoStack.push(current);

            WritableImage next = redoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            drawSnapshot(next); // CHANGED: drawn 1 to 1
            clearSelection(); // NEW
        }
    }

    /**
     * Checks if a point is inside the current selection
     * @param x the x of the point
     * @param y the y of the point
     * @return true if the point is inside the selection box
     */
    private boolean insideSelection(double x, double y) {
        return x >= selX && x <= selX + selW && y >= selY && y <= selY + selH;
    }

    /**
     * Finishes a selection when the mouse is released
     * Copies the pixels inside the dragged box out of a full resolution snapshot
     * A click with no drag just clears the selection
     * @param endX the x where the mouse was released
     * @param endY the y where the mouse was released
     */
    private void finishSelection(double endX, double endY) {
        //keeps the box inside the canvas
        double x0 = Math.max(0, Math.min(selStartX, endX));
        double y0 = Math.max(0, Math.min(selStartY, endY));
        double x1 = Math.min(canvas.getWidth(), Math.max(selStartX, endX));
        double y1 = Math.min(canvas.getHeight(), Math.max(selStartY, endY));

        if (x1 - x0 < 1 || y1 - y0 < 1) {
            clearSelection();
            return;
        }

        //Gets the snapshot at full resolution and calculates the selection box in that image
        WritableImage snap = takeSnapshot();
        double scaleX = snap.getWidth() / canvas.getWidth();
        double scaleY = snap.getHeight() / canvas.getHeight();
        int px = (int) Math.round(x0 * scaleX);
        int py = (int) Math.round(y0 * scaleY);
        int pw = Math.max(1, Math.min((int) Math.round((x1 - x0) * scaleX), (int) snap.getWidth() - px));
        int ph = Math.max(1, Math.min((int) Math.round((y1 - y0) * scaleY), (int) snap.getHeight() - py));

        // Copies the pixels inside the selection box into selectedImage and sets its position and size
        selectedImage = new WritableImage(snap.getPixelReader(), px, py, pw, ph);
        selX = x0;
        selY = y0;
        selW = pw / scaleX;
        selH = ph / scaleY;
        floating = false;
        redrawOutline();
    }

    /**
     * Public method that starts dragging the selection
     * The first time, the selection is lifted: its old spot is filled white and the canvas without
     * it is saved as floatBase. While dragging, floatBase is redrawn and the selection is drawn on top (live draw).
     * After that the selection stays lifted so moving it again doesn't erase what it was dropped on.
     * @param mouseX the x of the mouse press
     * @param mouseY the y of the mouse press
     */
    private void startMove(double mouseX, double mouseY) {
        saveUndoState();
        if (!floating) {
            gc.setFill(Color.WHITE);
            gc.fillRect(selX, selY, selW, selH); //erase the old spot
            floatBase = takeSnapshot();
            gc.drawImage(selectedImage, selX, selY, selW, selH); //put it back so nothing jumps
            floating = true;
        }
        moveGrabX = mouseX - selX;
        moveGrabY = mouseY - selY;
        moving = true;
    }

    /**
     * Public method that copies the current selection so it can be pasted
     */
    public void copySelection() {
        if (selectedImage != null) {
            clipboardImage = selectedImage;
            clipW = selW;
            clipH = selH;
        }
    }

    /**
     * Public method that pastes the copied piece into the top left corner and switches to the select tool
     * so the user can drag it (live) to where they want it
     */
    public void pasteSelection() {
        if (clipboardImage == null) {
            return;
        }
        saveUndoState();
        floatBase = takeSnapshot(); //canvas before the paste, so dragging the paste doesn't erase anything
        selectedImage = clipboardImage;
        selX = 0;
        selY = 0;
        selW = clipW;
        selH = clipH;
        gc.drawImage(selectedImage, selX, selY, selW, selH);
        floating = true;
        currentTool = "select";
        lineStarted = false;
        redrawOutline();
    }

    /**
     * Public method that forgets the current selection and hides its outline
     */
    private void clearSelection() {
        selectedImage = null;
        floating = false;
        moving = false;
        selecting = false;
        clearOverlay();
    }

    /**
     * Public method that clears everything on the overlay canvas
     */
    private void clearOverlay() {
        overlayGc.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
    }

    /**
     * Public method that draws the dashed selection outline on the overlay
     */
    private void drawOutline(double x, double y, double w, double h) {
        clearOverlay();
        overlayGc.setStroke(Color.BLUE);
        overlayGc.setLineWidth(1);
        overlayGc.setLineDashes(5, 5);
        overlayGc.strokeRect(x, y, w, h);
    }

    /**
     * Public method that redrawing the outline for the current selection (if there is one)
     */
    private void redrawOutline() {
        if (selectedImage != null) {
            drawOutline(selX, selY, selW, selH);
        } else {
            clearOverlay();
        }
    }

    /**
     * Public method for getting the DrawingPane
     * @return the drawingpane
     */
    public BorderPane getDrawingPane() { 
        return drawingPane; 
    } 

    /**
     * Public method for getting the canvas
     * @return the canvas
     */
    public Canvas getCanvas() { 
        return canvas;
    } 

    /**
     * Public method for loading the image after refreshing the canvas
     * @param image the image that is being loaded
     */
    public void loadImage(Image image) { 
        saveUndoState();
        clearSelection();
        fillWhite();
        gc.drawImage(image, 0, 0);
    } 
}