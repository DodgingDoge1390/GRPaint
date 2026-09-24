import javafx.scene.control.Tab;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import java.io.File;

/**
 * PaintTab class that extends the JavaFX library Tab
 * Sets the Tabs for the GRPaint application
 */
public class PaintTab extends Tab {
    private Drawing drawing;
    private final StackPane contentWrapper;
    private final ScrollPane scrollPane;
    private File currentFile;

    /**
     * Public method creating a new PaintTab with the drawing canvas
     * Sets up the scrollPane and the contentWrapper for the drawing canvas
     * 
     * @param title of the tab
     * @param width of the current drawing canvas
     * @param height of the current drawing canvas
     */
    public PaintTab(String title, double width, double height) {
        super(title);

        drawing = new Drawing(width, height);

        contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: white;");
        contentWrapper.getChildren().setAll(drawing.getDrawingPane());

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(contentWrapper);

        setContent(scrollPane); // this is what makes it show inside the TabPane
        setClosable(true);
    }

    /**
     * Method for getting the drawing canvas
     * @return drawing canvas
     */
    public Drawing getDrawing() { return drawing; }

    /**
     * Method for setting the drawing canvas
     * Sets the current drawing as the new drawing and puts it into the contentWrapper
     * @param newDrawing takes the new drawing
     */
    public void setDrawing(Drawing newDrawing) {
        drawing = newDrawing;
        contentWrapper.getChildren().setAll(drawing.getDrawingPane());
    }

    /**
     * Public method for getting the currentFile
     * @return the current File
     */
    public File getCurrentFile() { return currentFile; }

    /**
     * Public method for setting the currentFile
     * @param file takes the current file 
     */
    public void setCurrentFile(File file) { currentFile = file; }
}