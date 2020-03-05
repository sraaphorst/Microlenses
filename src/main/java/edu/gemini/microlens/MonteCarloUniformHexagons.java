package edu.gemini.microlens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MonteCarloUniformHexagons extends JPanel {
    private enum Microlenses {
        /**
         * The side of the hexagons in arcsecs and the filling factor as a percentage of hits.
         * The circles are the number of concentric circles forming the lens.
         */
        STANDARD_RESOLUTION(0.2272, 0.85, 2),
        HIGH_RESOLUTION(0.1365, 0.93, 3);

        final double side;
        final double filling_factor;
        final int circles;

        Microlenses(final double side, final double filling_factor, final int circles) {
            this.side = side;
            this.filling_factor = filling_factor;
            this.circles = circles;
        }
    }

    // Create the boundary around the hexagons.
    // We need to surround them because this is our sample set.
    // This is a bit hacky but I'm not sure how else to do it.
    // We will store the offsets into the PathIterators for each hexagon and then get them to make sure the
    // points coincide exactly with the hexagons. This makes a 24-sided shape that surrounds the hexagons.
    // Note that in the path offset, elements at 0, 1, 7, and 8 are the same points: they represent the move to and
    // line to operations.
    private final static int[] hexagonalOffsets = {0, 1, 4, 6, 5, 2};
    private final static int[][] hexagonalOffsetIndices = new int[6][];
    static {
        hexagonalOffsetIndices[0] = new int[]{1, 2, 3, 4}; // In hex 0
        hexagonalOffsetIndices[1] = new int[]{2, 3, 4, 5}; // In hex 1
        hexagonalOffsetIndices[2] = new int[]{3, 4, 5, 0}; // In hex 4
        hexagonalOffsetIndices[3] = new int[]{4, 5, 0, 1}; // In hex 6
        hexagonalOffsetIndices[4] = new int[]{5, 0, 1, 2}; // In hex 6
        hexagonalOffsetIndices[5] = new int[]{0, 1, 2, 3}; // In hex 2
    }
    private Path2D sampleSpace;

    /**
     * We want 14 hexagons, with 7 hexagons inside of 7 hexagons with a
     * fixed padding with the goal of achieving a certain "filling factor",
     * i.e. percentage of hits.
     *
     * We then perform a Monte Carlo simulation by selecting points at
     * random and determining if they are in the inner hexagons. This gives
     * us the filling factor for this fixed padding. If we achieve the filling
     * factor, then the padding is the average distance between the
     * microlenses.
     */
    // The size of the component and the padding around it.
    // This PADDING is different from padding, which is the padding between microlenses.
    private final static int SIZE = 900;
    private final static int PADDING = 100;
    private final static Dimension d = new Dimension(SIZE + 2 * PADDING, SIZE + 2 * PADDING);

    private final List<RegularHexagon> hexagons;
    private final List<Point2D.Double> centres;


    double conversion;
    double hexagonRadius;

    // The number of hits and misses.
    public int hits = 0;
    public int misses = 0;

    /**
     * Initialize the simulation.
     * @param microlenses the type of simulation
     * @param padding the padding IN arcsec
     */
    public MonteCarloUniformHexagons(final Microlenses microlenses, final double padding) {
        centres = new ArrayList<>();
        hexagons = new ArrayList<>();

        // The image is square. Calculate the arcseconds across so that we can determine the number of pixels
        // per arsecond. We can do this by drawing a line horizontally through the middle, which is four
        // paddedBisectors + two bisectors.
        hexagonRadius = microlenses.side;

        setSize(d);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);

        init(padding);
    }

    public void init(final double padding) {
        // A padded hexagon is the hexagon which includes the gap around it.
        final double paddedHexagonRadius = hexagonRadius + padding;

        // We get the bisectors using the Pythagorean formula and the fact that a regular hexagon is simply
        // six equilateral triangles, so each side of the hexagon has the same length as the radius.
        final double bisector = Math.sqrt(3) / 2.0 * hexagonRadius;
        final double paddedBisector = Math.sqrt(3) / 2.0 * (hexagonRadius + padding);

        // Width of the world in arcseconds.
        final double width = 2 * bisector + 4 * paddedBisector;

        // Calculate the conversion factor from arcseconds to pixels.
        conversion = SIZE / width; // pixels / arcseconds

        centres.clear();
        centres.add(new Point2D.Double(bisector + paddedBisector, paddedHexagonRadius));
        centres.add(new Point2D.Double(bisector + 3 * paddedBisector, paddedHexagonRadius));
        centres.add(new Point2D.Double(bisector, 2 * paddedHexagonRadius + paddedHexagonRadius / 2.0)); //The y coordinate may not be right.
        centres.add(new Point2D.Double(bisector + 2 * paddedBisector, 2 * paddedHexagonRadius + paddedHexagonRadius / 2.0)); //The y coordinate may not be right.
        centres.add(new Point2D.Double(bisector + 4 * paddedBisector, 2 * paddedHexagonRadius + paddedHexagonRadius / 2.0)); //The y coordinate may not be right.
        centres.add(new Point2D.Double(bisector + paddedBisector, 4 * paddedHexagonRadius));
        centres.add(new Point2D.Double(bisector + 3 * paddedBisector, 4 * paddedHexagonRadius));

        hexagons.clear();
        centres.forEach(c -> {
            RegularHexagon hexagon = new RegularHexagon();
            final double hcx = c.getX();
            final double hcy = c.getY();

            final AffineTransform trans = new AffineTransform();
            trans.translate(PADDING + hcx * conversion, PADDING + hcy * conversion);
            trans.rotate(Math.PI / 2.0);
            trans.scale(hexagonRadius * conversion, hexagonRadius * conversion);
            hexagon.transform(trans);
            hexagons.add(hexagon);
        });

//        addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                System.out.println(e.getPoint());
//            }
//        });

        // Determine if a point is in a hexagon.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Point p = e.getPoint();
                for (RegularHexagon h : hexagons) {
                    if (h.contains(p)) {
                        ++hits;
                        System.out.println("Inside hexagon " + h.getId() + ", hits: " + hits + " point: " + p);
                        return;
                    }
                }
                //websockets
                ++misses;
                System.out.println("Misses: " + misses);
            }
        });
    }

    private Path2D createSampleSpaceBorder() {
        // Create the border as described above around the exterior hexagons.
        sampleSpace = new Path2D.Double();
        boolean firstMove = true;

        // We grab all the information from the path iterators to make it easier to work with them later.
        double[] info = new double[6];
        double[] xpoints = new double[6];
        double[] ypoints = new double[6];

        for (int i = 0; i < 6; ++i) {
            System.out.println("* HEX " + hexagonalOffsets[i]);
            final RegularHexagon hex = hexagons.get(hexagonalOffsets[i]);

            // Grab the six points from the path iterator for hexagonakOffset i.
            final PathIterator pi = hex.getPathIterator(new AffineTransform());
            pi.next();
            for (int j = 0; j < 6; ++j) {
                pi.currentSegment(info);
                xpoints[j] = info[0];
                ypoints[j] = info[1];
                pi.next();
            }

            for (int j = 0; j < 6; ++j)
                System.out.println("\t" + j + ": " + xpoints[j] + ", " + ypoints[j]);

            // Now piece together the four points from this hexagon.
            // If this is the first move, just move: no line.
            for (int j = 0; j < 4; ++j) {
                if (firstMove) {
                    firstMove = false;
                    System.out.println("Moving to " + xpoints[hexagonalOffsetIndices[i][j]] + ", " +  ypoints[hexagonalOffsetIndices[i][j]]);
                    sampleSpace.moveTo(xpoints[hexagonalOffsetIndices[i][j]], ypoints[hexagonalOffsetIndices[i][j]]);
                } else {
                    System.out.println("hex=" + hexagonalOffsets[i] + " pointIdx=" + j);
                    System.out.println("Line to " + xpoints[hexagonalOffsetIndices[i][j]] + ", " +  ypoints[hexagonalOffsetIndices[i][j]]);
                    sampleSpace.lineTo(xpoints[hexagonalOffsetIndices[i][j]], ypoints[hexagonalOffsetIndices[i][j]]);
                }
            }
        }
        sampleSpace.closePath();

        return sampleSpace;
    }


    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;

        final Dimension size = getSize();
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, size.width, size.height);

        g2d.setColor(Color.cyan);
        g2d.drawRect(PADDING, PADDING, SIZE, SIZE);

        final int cx = (size.width - SIZE) / 2;
        final int cy = (size.height - SIZE) / 2;

        g2d.setColor(Color.yellow);
        g2d.setStroke(new BasicStroke(1));

        hexagons.forEach(h -> {
            g2d.setColor(h.getColor());
            g2d.draw(h);
        });

        Path2D sampleSpace = createSampleSpaceBorder();
        g2d.setColor(Color.cyan);
        g2d.draw(sampleSpace);
//        double[] pts = new double[6];
//        final RegularHexagon h = hexagons.get(1);
//        final PathIterator pi = h.getPathIterator(new AffineTransform());
//        while (!pi.isDone()) {
//            pi.currentSegment(pts);
//            for (int i=0; i < 6; ++i)
//                System.out.print(pts[i] + " ");
//            System.out.println();
//            pi.next();
//        }
//        System.out.println();

    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Hexagons");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final MonteCarloUniformHexagons hexagonPanel = new MonteCarloUniformHexagons(Microlenses.STANDARD_RESOLUTION, 0.04);
        frame.add(hexagonPanel);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int xPos = (screenSize.width - hexagonPanel.getWidth()) / 2;
        final int yPos = (screenSize.height - hexagonPanel.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        frame.setSize(d);
        frame.setMinimumSize(d);
        frame.setPreferredSize(d);
        frame.setMaximumSize(d);
        frame.setVisible(true);
    }
}
