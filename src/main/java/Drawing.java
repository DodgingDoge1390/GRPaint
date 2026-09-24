import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Public Class for the Drawing functionality in GRPaint
 * Defines and calculates what each Tool and Shape does 
 * when the user clicks on it
 */
public class Drawing { //start of the class for tools
    private Canvas canvas; // Canvas that the user draws on
    private GraphicsContext gc; // GraphicsContext used to draw
    private double startX; // X position where the mouse/shape started
    private double startY; // Y position where the mouse/shape started
    private BorderPane drawingPane; // Layout that holds everything
    private String currentTool = ""; // Holder for the current tool
    private String lastTool = "pencil"; // last non-colorGrabber tool, so grabbing a color hands control back to whatever tool (including a shape) was active
    private boolean lineStarted = false; // Boolean for the linestarted
    private boolean dashed = false; // whether outlines should be dashed
    private ColorPicker colorPicker;
    private Label widthLabel;
    private WritableImage shapeSnapshot; // canvas snapshot taken right before a shape starts, so we can redraw the live preview each drag

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

        if (tool.equals("pencil")){
            gc.setStroke(colorPicker.getValue());
        }
    }

    /**
     * Method that checks if the user selected a shape
     * @param tool the current tool selected by the user
     */
    private boolean isShapeTool(String tool) {
        return tool.equals("square") || tool.equals("circle") || tool.equals("rectangle")
            || tool.equals("ellipse") || tool.equals("triangle") || tool.equals("star");
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

        //sets the tools color and width
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); 
        
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

        //Adds the syling for the toolbar
        HBox toolbar = new HBox(10); 
        toolbar.setStyle("-fx-background-color: #d3d3d3;"); 

        //adds all the items into the main toolbar
        toolbar.getChildren().addAll(refreshButton, widthLabel, widthSlider, colorPicker, dashedCheckBox); 

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { 
            if (currentTool.equals("eraser")) {
                gc.setStroke(Color.WHITE);
            }
            if (currentTool.equals("pencil") || currentTool.equals("eraser")) {
                startX = e.getX(); 
                startY = e.getY(); 
            } 

            //if the current tool is a Shape it will get the X,Y and send a snapshot to the user
            if (isShapeTool(currentTool)) { 
                startX = e.getX();
                startY = e.getY();
                shapeSnapshot = canvas.snapshot(null, null);
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
        }); 

        //On the mouse released if the tool is a shape it will call the preview function
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { 
            if (isShapeTool(currentTool)) { 
                previewShape(e.getX(), e.getY());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> { 
            if (currentTool.equals("line")) { 
                if (!lineStarted) {
                    startX = e.getX(); 
                    startY = e.getY();
                    lineStarted = true;
                } else { 
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

            //ColorGrabber
            if (currentTool.equals("colorGrabber")) {
                //creates a snapshot of the canvas
                WritableImage image = canvas.snapshot(null, null);
                //creates a pixelreader to get the color of the pixel
                PixelReader pixelReader = image.getPixelReader();

                //gets the coordinates of the click to get the color of the pixel
                Color pickedColor = pixelReader.getColor(
                    (int) e.getX(),
                    (int) e.getY()
                );
                //sets the colorpicker and the gc stroke to the pixel color
                colorPicker.setValue(pickedColor);
                gc.setStroke(pickedColor);
                currentTool = lastTool; 
            }
        }); 
        //creates a drawingpane to organize the canvas and the toolbar
        drawingPane = new BorderPane();
        drawingPane.setTop(toolbar); 
        drawingPane.setCenter(canvas); 
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
            gc.drawImage(shapeSnapshot, 0, 0); 
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
            case "star": 
                drawStar(x, y, w, h); //takes the variables to the starr function
                break;
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
     * Refresh button to get rid of everything on the canvas
     */
    private void refresh() { 
        gc.clearRect( 
            0, 
            0,
            canvas.getWidth(), 
            canvas.getHeight() 
        ); 
    } 

    /**
     * Method for getting the DrawingPane
     * @return the drawingpane
     */
    public BorderPane getDrawingPane() { 
        return drawingPane; 
    } 

    /**
     * Method for getting the canvas
     * @return the canvas
     */
    public Canvas getCanvas() { 
        return canvas;
    } 

    /**
     * Method for loading the image after refreshing the canvas
     * @param image the image that is being loaded
     */
    public void loadImage(Image image) { 
        refresh(); 
        gc.drawImage(image, 0, 0);
    } 
} 