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

    // The number of hits and misses.
    public int hits = 0;
    public int misses = 0;

    // The padding in arcsecs.
    public MonteCarloUniformHexagons(final Microlenses microlenses, double padding) {
        this.microlenses = microlenses;

        // Create the hexagons.
        // The number of pixels per inner and outer hexagon.
        final double hexagonPoint = (SIZE + PADDING) / 2.0;

        // microlenses.side and microlenses.side + padding / 2 are in arcsec, so we convert to pixels.
        innerHexagonRadius = (SIZE - PADDING) * microlenses.side;
        outerHexagonRadius = (SIZE - PADDING) * (microlenses.side + padding / 2);

        // Create the inner hexagon, and then create the concentric rings.
        innerHexagons = new ArrayList<>();
        outerHexagons = new ArrayList<>();

        // We start with one hexagon as a test.
        final RegularHexagon inner = new RegularHexagon();
        final AffineTransform innerTrans = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        innerTrans.scale(innerHexagonRadius, innerHexagonRadius);
        inner.transform(innerTrans);
        innerHexagons.add(inner);

        // This is right, but requires the outer hexagon radius to be added.
        final RegularHexagon inner3 = new RegularHexagon();
        double a = innerHexagonRadius * Math.sqrt(3);
        final AffineTransform innerTrans3 = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint - a);
        innerTrans3.scale(innerHexagonRadius, innerHexagonRadius);
        inner3.transform(innerTrans3);
        innerHexagons.add(inner3);

        final RegularHexagon inner2 = new RegularHexagon();
        final AffineTransform innerTrans2 = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint - 2 * innerHexagonRadius);
        innerTrans2.scale(innerHexagonRadius, innerHexagonRadius);
        inner2.transform(innerTrans2);
        innerHexagons.add(inner2);

        // Determine if a point is in a hexagon.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Point p = e.getPoint();
                for (RegularHexagon h: innerHexagons)
                    if (h.contains(p)) {
                        ++hits;
                        System.out.println("Inside hexagon " + h.getId() + ", hits: " + hits);
                        return;
                    }
                ++misses;
                System.out.println("Misses: " + misses);
            }
        });


        final RegularHexagon outer = new RegularHexagon();
        final AffineTransform outerTrans = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        outerTrans.scale(outerHexagonRadius, outerHexagonRadius);
        outer.transform(outerTrans);
        outerHexagons.add(outer);

        final RegularHexagon outer2 = new RegularHexagon();
        final AffineTransform outerTrans2 = AffineTransform.getTranslateInstance(hexagonPoint, hexagonPoint);
        outerTrans2.scale(outerHexagonRadius, outerHexagonRadius);
        outerTrans2.translate(500, 10);
        System.out.println(-innerHexagonRadius - outerHexagonRadius);
        //outerTrans2.translate(200, -innerHexagonRadius - outerHexagonRadius);
        outer2.transform(outerTrans);
        outerHexagons.add(outer2);

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

        System.out.println("Drawing " + innerHexagons.size() + " inner hexagons.");
        g2d.setColor(Color.yellow);
        g2d.setStroke(new BasicStroke(1));
        innerHexagons.forEach(g2d::draw);

        System.out.println("Drawing " + outerHexagons.size() + " inner hexagons.");
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
