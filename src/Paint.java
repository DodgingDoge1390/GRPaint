import javafx.scene.canvas.Canvas; // JavaFX library for Canvas is the area we draw on
import javafx.scene.canvas.GraphicsContext; // JavaFX library forGraphicsContext gives us drawing tools
import javafx.scene.control.Button; // JavaFX library for buttons
import javafx.scene.control.ColorPicker; // JavaFX library ColorPicker lets the user choose a color
import javafx.scene.control.Label; // JavaFX library label displays text
import javafx.scene.control.Slider; // JavaFX library slider lets us change the line width
import javafx.scene.input.MouseEvent; // JavaFX library for mouseEvent detects mouse actions
import javafx.scene.layout.BorderPane; // JavaFX library for borderPane organizes our controls
import javafx.scene.layout.HBox; // JavaFX library for HBox puts controls next to each other
import javafx.scene.paint.Color; // JavaFX libray for color
import javafx.scene.image.Image; // JavaFX library for the Image
import javax.xml.namespace.QName;


public class Drawing { //start of the class for tools
    private Canvas canvas; // Canvas that the user draws on
    private GraphicsContext gc; // GraphicsContext used to draw
    private double startX; // X position where the mouse started
    private double startY; // Y position where the mouse started
    private BorderPane drawingPane; // Layout that holds everything
    private String currentTool = ""; // Holder for the current tool which is represented as nothing right now
    private boolean lineStarted = false; // Boolean for the linestarted 
    private Runnable onChange;
    
    public void setTool(String tool) { //method for constructing the tool
        currentTool = tool; //sets the current tool to toll
        lineStarted = false; //sets let started to false
    }

    public Drawing(double width, double height, Runnable onChange) { //takes the width and the height of the drawing
        this.onChange = onChange; //sets the onChange 
        canvas = new Canvas(width, height); //creates a canvas for the drawing of the width and height

        gc = canvas.getGraphicsContext2D(); //gets the graphicsContext for the canvas and sets it to gc

        gc.setStroke(Color.BLACK); //sets the standard lineto black
        gc.setLineWidth(2); //sets the standard width to 2
        ColorPicker colorPicker = new ColorPicker(Color.BLACK); //creates a colorpicker and sets it to the color black
        colorPicker.setOnAction(e -> {gc.setStroke(colorPicker.getValue());}); //On the action, gets the stroke and gets the user's input of their color of choice and sets it to that

        Label widthLabel = new Label("Line Width: 2"); //sets the label for the line width
        Slider widthSlider = new Slider(0, 30, 2); //sets a slider for the line width
        
        widthSlider.setShowTickMarks(true); //shows the tick marks
        widthSlider.setShowTickLabels(true); //shows the tick labels 
        widthSlider.setMajorTickUnit(5); //sets the tick labels by every five
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> { //sets the value to whatever the user inputs, and whenever this number is changed...
            gc.setLineWidth(newValue.doubleValue()); //it sets the new linewidth to the new value of the slider
            widthLabel.setText("Line Width: " + newValue.intValue()); //sets the new text for the width label to the new value of line width
        }); //end of listener

        Button refreshButton = new Button("Refresh"); //refresh button
        refreshButton.setOnAction(e -> refresh()); //On the action, it goes to refresh method

        HBox toolbar = new HBox(10); //toolbar for holding all the tools functions
        toolbar.setStyle("-fx-background-color: #d3d3d3;"); //sets the background color of the toolbar to a light gray

        toolbar.getChildren().addAll(refreshButton, widthLabel, widthSlider, colorPicker); //adds all the variables to the toolbar

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { //On the mouse pressed..
            if (currentTool.equals("pencil")) { //if the current tool is pencil
                startX = e.getX(); //get the x and set it as the start
                startY = e.getY(); //get the y and set it as the start
            } //end of if
        }); //end of mouse pressed

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> { //On the mouse dragged
            if (currentTool.equals("pencil")) { //if the current tool is pencil
                gc.strokeLine( //creates a line for the canvas draws in between the (start of X, start of Y) and (new X, new Y)
                    startX, //takes start of x
                    startY, //start of y
                    e.getX(), //gets new x
                    e.getY() //and new Y
                ); //of line stroke
                onChange.run(); //tells autosave the drawing changed
                startX = e.getX(); //creates a new startX 
                startY = e.getY(); //creates a new startY
            } //end of if
        }); //end of mouse dragged

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> { //On the mouse clicked..(difference is it waits until you let go of mouse)
            if (currentTool.equals("line")) { //if the tool is line
                if (!lineStarted) { //if the line hasn't started
                    startX = e.getX(); //get start x
                    startY = e.getY(); //get start y
                    lineStarted = true; //sets the linestarted to true
                } else { //else
                    gc.strokeLine( //stroke line to canvas
                        startX, //start x
                        startY, //start y
                        e.getX(), //new x
                        e.getY() //new y
                    ); //end of stroke
                    onChange.run(); //tells autosave the drawing changed
                    lineStarted = false; //sets line started to false
                } //end of else
            } //end of if
        }); //end of on action


        drawingPane = new BorderPane(); //new borderpane for the drawing organization
        drawingPane.setTop(toolbar); //sets toolbar on top
        drawingPane.setCenter(canvas); //sets the canvas in the center
    } //end of Drawing Class

    private void refresh() { //start of refresh method
        gc.clearRect( //clears the Rectangle(canvas)
            0, //origin x
            0, //origin y
            canvas.getWidth(), //gets width
            canvas.getHeight() //gets height
        ); //end of clear
        onChange.run(); //tells autosave the drawing changed
    } //end of refresh

    public BorderPane getDrawingPane() { //get drawingpane method
        return drawingPane; //returns drawing pane
    } //end of method

    public Canvas getCanvas() { //get canvas method
        return canvas; //return canvas
    } //end of method

    public void loadImage(Image image) { //load image method
        gc.clearRect( //clears the rect same thing as before
            0, 
            0, 
            canvas.getWidth(), 
            canvas.getHeight()
        ); 
        gc.drawImage(image, 0, 0); //draws the image from origin 0,0
    } //end of method
}//end public Drawing class
