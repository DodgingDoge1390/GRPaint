import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Public class that tests the star and polygon coordinate calculations.
 */
public class ShapesTest {
    /**
     * Checks that a four point star is a diamond with four corners.
     */
    @Test
    public void testFourPointDiamond() {
        double[][] points = Shapes.createStarPoints(0, 0, 100, 100, 4);
        assertEquals(4, points[0].length);
        assertEquals(4, points[1].length);
        assertEquals(50, points[0][0], 0.0001);
        assertEquals(0, points[1][0], 0.0001);
    }

    /**
     * Checks that an eight point star has alternating outer and inner corners.
     */
    @Test
    public void testEightPointStar() {
        double[][] points = Shapes.createStarPoints(0, 0, 100, 100, 8);
        assertEquals(16, points[0].length);
        assertEquals(16, points[1].length);
        double outerY = points[1][0];
        double innerY = points[1][1];
        assertTrue(outerY < innerY);
    }

    /**
     * Checks that a polygon has the requested number of sides.
     */
    @Test
    public void testPolygonSides() {
        double[][] points = Shapes.createPolygonPoints(0, 0, 100, 100, 7);
        assertEquals(7, points[0].length);
        assertEquals(7, points[1].length);
    }

    /**
     * Checks that invalid star point counts are not allowed.
     */
    @Test
    public void testInvalidStarPoints() {
        assertThrows(IllegalArgumentException.class,
            () -> Shapes.createStarPoints(0, 0, 100, 100, 3));
    }

    /**
     * Checks that invalid polygon side counts are not allowed.
     */
    @Test
    public void testInvalidPolygonSides() {
        assertThrows(IllegalArgumentException.class,
            () -> Shapes.createPolygonPoints(0, 0, 100, 100, 2));
    }
}
