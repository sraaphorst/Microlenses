package edu.gemini.microlens;

import javax.swing.*;
import java.awt.*;
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
    // The size of the component.
    private final static int SIZE = 1200;
    private final static int PADDING = 300;
    private final static Dimension d = new Dimension(SIZE + PADDING, SIZE + PADDING);

    // These are the microlenses.
    private final double innerHexagonRadius;
    private final List<RegularHexagon> innerHexagons;

    // These are the hexagons that surround the microlenses: i.e. the microlenses and the padding.
    private final double outerHexagonRadius;
    private final List<RegularHexagon> outerHexagons;

    // The set of microlenses with which we are working.
    private final Microlenses microlenses;

    // The padding in arcsecs.
    public MonteCarloUniformHexagons(final Microlenses microlenses, double padding) {
        this.microlenses = microlenses;

        // Create the hexagons.
        // The number of pixels per inner and outer hexagon.
        final double hexagonPoint = (SIZE + PADDING) / 2.0;
        innerHexagonRadius = (SIZE - PADDING) * microlenses.side;
        outerHexagonRadius = (SIZE - PADDING) * (microlenses.side + padding);

        // Create the inner hexagon, and then create the concentric rings.
        innerHexagons = new ArrayList<>();
        outerHexagons = new ArrayList<>();

        // We start with one hexagon as a test.
        final RegularHexagon inner = new RegularHexagon();
        final AffineTransform innerTrans = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        innerTrans.scale(innerHexagonRadius, innerHexagonRadius);
        inner.transform(innerTrans);
        innerHexagons.add(inner);

        final RegularHexagon outer = new RegularHexagon();
        final AffineTransform outerTrans = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        outerTrans.scale(outerHexagonRadius, outerHexagonRadius);
        outer.transform(outerTrans);
        outerHexagons.add(outer);

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

        g2d.setColor(Color.yellow);
        g2d.setStroke(new BasicStroke(3));
        innerHexagons.forEach(g2d::draw);

        g2d.setColor(Color.green);
        outerHexagons.forEach(g2d::draw);
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Hexagon");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final MonteCarloUniformHexagons hexagonPanel = new MonteCarloUniformHexagons(Microlenses.HIGH_RESOLUTION, 0.01);
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
