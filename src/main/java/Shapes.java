import javafx.scene.canvas.GraphicsContext;

/**
 * Public class that contains the algorithms for drawing the shapes in GRPaint.
 */
public class Shapes {
    /**
     * Public method that draws the selected shape.
     * Uses a switch case to call the algorithm for the current shape.
     * @param gc the graphics context used to draw the shape
     * @param tool the current shape tool
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param width the width of the shape
     * @param height the height of the shape
     * @param sides the number of sides for a polygon
     * @param starPoints the number of points for a star
     */
    public static void drawShape(GraphicsContext gc, String tool, double x, double y,
                                 double width, double height, int sides, int starPoints) {
        switch (tool) {
            case "rectangle":
                gc.strokeRect(x, y, width, height);
                break;
            case "square": {
                double side = Math.max(width, height);
                gc.strokeRect(x, y, side, side);
                break;
            }
            case "ellipse":
                gc.strokeOval(x, y, width, height);
                break;
            case "circle": {
                double diameter = Math.max(width, height);
                gc.strokeOval(x, y, diameter, diameter);
                break;
            }
            case "triangle":
                gc.strokePolygon(new double[]{x, x + width, x + width / 2},
                                 new double[]{y + height, y + height, y}, 3);
                break;
            case "righttriangle":
                gc.strokePolygon(new double[]{x, x, x + width},
                                 new double[]{y, y + height, y + height}, 3);
                break;
            case "star":
                drawStar(gc, x, y, width, height, starPoints);
                break;
            case "diamond":
                gc.strokePolygon(new double[]{x + width / 2, x + width, x + width / 2, x},
                                 new double[]{y, y + height / 2, y + height, y + height / 2}, 4);
                break;
            case "polygon":
                drawPolygon(gc, x, y, width, height, sides);
                break;
            default:
                break;
        }
    }

    /**
     * Public method for drawing a star with a variable number of points.
     * Four points create a diamond. Larger stars alternate outside and inside points.
     * @param gc the graphics context used to draw the star
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param width the width of the star
     * @param height the height of the star
     * @param points the number of points the star should have
     */
    public static void drawStar(GraphicsContext gc, double x, double y,
                                double width, double height, int points) {
        if (points < 4) return;
        double[][] coordinates = createStarPoints(x, y, width, height, points);
        gc.strokePolygon(coordinates[0], coordinates[1], coordinates[0].length);
    }

    /**
     * Public method that calculates the x and y coordinates for a star.
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param width the width of the star
     * @param height the height of the star
     * @param points the number of points the star should have
     * @return an array containing the x coordinates and y coordinates
     */
    public static double[][] createStarPoints(double x, double y, double width, double height, int points) {
        if (points < 4) throw new IllegalArgumentException("A star needs at least 4 points.");
        if (points == 4) {
            return new double[][]{
                {x + width / 2, x + width, x + width / 2, x},
                {y, y + height / 2, y + height, y + height / 2}
            };
        }
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double outerRadius = Math.min(width, height) / 2;
        double innerRadius = outerRadius / 2.5;
        double[] xs = new double[points * 2];
        double[] ys = new double[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = -Math.PI / 2 + Math.PI * i / points;
            xs[i] = centerX + radius * Math.cos(angle);
            ys[i] = centerY + radius * Math.sin(angle);
        }
        return new double[][]{xs, ys};
    }

    /**
     * Public method for drawing a regular polygon with any number of sides.
     * Places the points evenly around a circle inside the bounding box.
     * @param gc the graphics context used to draw the polygon
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param width the width of the polygon
     * @param height the height of the polygon
     * @param sides the number of sides (3 or more)
     */
    public static void drawPolygon(GraphicsContext gc, double x, double y,
                                   double width, double height, int sides) {
        if (sides < 3) return;
        double[][] coordinates = createPolygonPoints(x, y, width, height, sides);
        gc.strokePolygon(coordinates[0], coordinates[1], sides);
    }

    /**
     * Public method that calculates the x and y coordinates for a regular polygon.
     * @param x the leftmost corner of x
     * @param y the topmost corner of y
     * @param width the width of the polygon
     * @param height the height of the polygon
     * @param sides the number of sides (3 or more)
     * @return an array containing the x coordinates and y coordinates
     */
    public static double[][] createPolygonPoints(double x, double y, double width, double height, int sides) {
        if (sides < 3) throw new IllegalArgumentException("A polygon needs at least 3 sides.");
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = Math.min(width, height) / 2;
        double[] xs = new double[sides];
        double[] ys = new double[sides];
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides - Math.PI / 2;
            xs[i] = centerX + radius * Math.cos(angle);
            ys[i] = centerY + radius * Math.sin(angle);
        }
        return new double[][]{xs, ys};
    }
}
