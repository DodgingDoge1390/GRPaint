import javafx.scene.canvas.Canvas; // Canvas is the area we draw on
import javafx.scene.canvas.GraphicsContext; // GraphicsContext gives us drawing tools
import javafx.scene.control.Button; // Button creates buttons
import javafx.scene.control.ColorPicker; // ColorPicker lets the user choose a color
import javafx.scene.control.Label; // Label displays text
import javafx.scene.control.Slider; // Slider lets us change the line width
import javafx.scene.input.MouseEvent; // MouseEvent detects mouse actions
import javafx.scene.layout.BorderPane; // BorderPane organizes our controls
import javafx.scene.layout.HBox; // HBox puts controls next to each other
import javafx.scene.paint.Color; // Color lets us choose colors
import javafx.scene.image.Image;


public class Drawing {
    private Canvas canvas; // Canvas that the user draws on
    private GraphicsContext gc; // GraphicsContext used to draw
    private double startX; // X position where the mouse started
    private double startY; // Y position where the mouse started
    private BorderPane drawingPane; // Layout that holds everything
    private String currentTool = "";
    private boolean lineStarted = false;
    
    public void setTool(String tool) {
        currentTool = tool;
        lineStarted = false;
    }

    public Drawing(double width, double height) {
        canvas = new Canvas(width, height);

        gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(e -> {

            gc.setStroke(colorPicker.getValue());
        });

        Label widthLabel = new Label("Line Width: 2");
        Slider widthSlider = new Slider(0, 30, 2);
        

        widthSlider.setShowTickMarks(true);
        widthSlider.setShowTickLabels(true);
        widthSlider.setMajorTickUnit(5);
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            gc.setLineWidth(newValue.doubleValue());
            widthLabel.setText("Line Width: " + newValue.intValue());
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());

        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-background-color: #d3d3d3;"); //sets the background color of the toolbar to a light gray

        toolbar.getChildren().addAll(refreshButton, widthLabel, widthSlider, colorPicker);

        // Mouse pressed
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (currentTool.equals("pencil")) {
                startX = e.getX();
                startY = e.getY();
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (currentTool.equals("pencil")) {
                gc.strokeLine(
                    startX,
                    startY,
                    e.getX(),
                    e.getY()
                );

                startX = e.getX();
                startY = e.getY();
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentTool.equals("line")) {
                if (!lineStarted) {
                    startX = e.getX();
                    startY = e.getY();
                    lineStarted = true;
                } else {
                    gc.strokeLine(
                        startX,
                        startY,
                        e.getX(),
                        e.getY()
                    );

                    lineStarted = false;
                }
            }
        });


        drawingPane = new BorderPane();
        drawingPane.setTop(toolbar);
        drawingPane.setCenter(canvas);
    }

    private void refresh() {
        gc.clearRect(
            0,
            0,
            canvas.getWidth(),
            canvas.getHeight()
        );
    }
    public BorderPane getDrawingPane() {
        return drawingPane;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void loadImage(Image image) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(image, 0, 0);
    }
}