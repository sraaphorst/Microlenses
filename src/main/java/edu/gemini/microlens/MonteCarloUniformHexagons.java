package edu.gemini.microlens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
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
    private final static int SIZE = 1200;
    private final static int PADDING = 300;
    private final static Dimension d = new Dimension(SIZE + PADDING, SIZE + PADDING);

    // These are the microlenses.
    private final double hexagonRadius;
    private final List<RegularHexagon> hexagons;

    // These are the hexagons that surround the microlenses: i.e. the microlenses and the padding.
    private final double paddedHexagonRadius;

    // The set of microlenses with which we are working.
    private final Microlenses microlenses;

    // The number of hits and misses.
    public int hits = 0;
    public int misses = 0;

    // The padding in arcsecs.
    public MonteCarloUniformHexagons(final Microlenses microlenses, double padding) {
        this.microlenses = microlenses;

        // Create the hexagons.
        // The number of pixels per inner and outer hexagon.
        final double hexagonPoint = (SIZE + PADDING) / 2.0;

        // These are in arcsec:
        // 1. hexagonRadius is the radius of the microlens.
        // 2. paddedHexagonRadius is the radius of the microlens with the padding beween them.
        // microlenses.side and (microlenses.side + padding) / 2 are in arcsec, so we convert to pixels.
        hexagonRadius = (SIZE - PADDING) * microlenses.side;
        paddedHexagonRadius = (SIZE - PADDING) * (microlenses.side + padding / 2);

        // This is also in arcsec, and is the length of the bisection of the equilateral triangles making up the
        // hexagons. It does not include the padding.
        final double hexagonBisector = hexagonRadius * Math.sqrt(3);
        final double paddedHexagonBisector = paddedHexagonRadius * Math.sqrt(3);

        // Create the inner hexagon, and then create the concentric rings.
        hexagons = new ArrayList<>();

        final RegularHexagon h = new RegularHexagon();
        final AffineTransform transform = new AffineTransform();
        transform.translate(200, 200);
        transform.rotate(Math.PI / 1.3);
        transform.scale(200, 200);
//        transform.translate(-300, -500);
        h.transform(transform);
        hexagons.add(h);

        // This is a hexagon in ring 0, i.e. a solitary hexagon in the centre.
        final RegularHexagon hexagonRing0 = new RegularHexagon();
        final AffineTransform trans0 = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        trans0.scale(hexagonRadius, hexagonRadius);
        hexagonRing0.transform(trans0);
        hexagons.add(hexagonRing0);

        // This is the top ring 1 in hexagon ring 1.
        final RegularHexagon hexagonRing1_0 = new RegularHexagon();
        final AffineTransform trans1_0 = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint - hexagonBisector - (paddedHexagonRadius - hexagonRadius));
        trans1_0.scale(hexagonRadius, hexagonRadius);
        hexagonRing1_0.transform(trans1_0);
        hexagons.add(hexagonRing1_0);

        final RegularHexagon hexagonRing1_1 = new RegularHexagon();
        final AffineTransform trans1_1 = new AffineTransform();
        // Order of scale and rotate matters! rotate = 3 has no effect.
        trans1_1.translate(hexagonPoint, hexagonPoint - hexagonBisector - (paddedHexagonRadius - hexagonRadius));
        trans1_1.rotate(Math.PI / 1000.0, hexagonPoint, hexagonPoint);
        trans1_1.scale(hexagonRadius, hexagonRadius);
        hexagonRing1_1.transform(trans1_1);
        hexagons.add(hexagonRing1_1);

//        // This is the bottom ring 1 in hexagon ring 1.
//        final RegularHexagon hexagonRing1_1 = new RegularHexagon();
//        final AffineTransform trans1_1 = new AffineTransform();
//        trans1_1.translate(hexagonPoint, hexagonPoint + a + (paddedHexagonRadius - hexagonRadius));
//        trans1_1.scale(hexagonRadius, hexagonRadius);
//        hexagonRing1_1.transform(trans1_1);
//        hexagons.add(hexagonRing1_1);
//
//        // This is the bottom ring 1 in hexagon ring 1.
//        final RegularHexagon hexagonRing1_3 = new RegularHexagon();
//        final AffineTransform trans1_3 = new AffineTransform();
//        trans1_3.translate(hexagonPoint + 1.5 * paddedHexagonRadius, hexagonPoint - paddedHexagonRadius);
//        trans1_3.scale(hexagonRadius, hexagonRadius);
//        hexagonRing1_3.transform(trans1_3);
//        hexagons.add(hexagonRing1_3);
//
//        // This is the bottom ring 1 in hexagon ring 1.
//        final RegularHexagon hexagonRing1_2 = new RegularHexagon();
//        final AffineTransform trans1_2 = new AffineTransform();
//        trans1_2.translate(hexagonPoint + 1.5 * hexagonRadius, hexagonPoint + paddedHexagonRadius);
//        trans1_2.scale(hexagonRadius, hexagonRadius);
//        hexagonRing1_2.transform(trans1_2);
//        hexagons.add(hexagonRing1_2);


//        final RegularHexagon hexagonRing1_1 = new RegularHexagon();
//        final AffineTransform trans1_1 = new AffineTransform();
//        trans1_1.translate(hexagonPoint + 1.5 * hexagonRadius + padding, hexagonPoint - hexagonRadius + 5 * padding);
//        trans1_1.scale(hexagonRadius, hexagonRadius);
//        //trans1_1.rotate(Math.PI / 3.0);
//        hexagonRing1_1.transform(trans1_1);
//        hexagons.add(hexagonRing1_1);

        // Determine if a point is in a hexagon.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Point p = e.getPoint();
                for (RegularHexagon h: hexagons)
                    if (h.contains(p)) {
                        ++hits;
                        System.out.println("Inside hexagon " + h.getId() + ", hits: " + hits);
                        return;
                    }
                ++misses;
                System.out.println("Misses: " + misses);
            }
        });

        setSize(d);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;

        final Dimension size = getSize();
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, size.width, size.height);

        final int cx = (size.width - SIZE) / 2;
        final int cy = (size.height - SIZE) / 2;
        System.out.println(cx + " " + cy);

        System.out.println("Drawing " + hexagons.size() + " inner hexagons.");
        g2d.setColor(Color.yellow);
        g2d.setStroke(new BasicStroke(1));
        hexagons.forEach(g2d::draw);
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Hexagon");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final MonteCarloUniformHexagons hexagonPanel = new MonteCarloUniformHexagons(Microlenses.HIGH_RESOLUTION, 0.05);
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
