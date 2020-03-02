package edu.gemini.microlens;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public final class TestRegularHexagon extends JPanel {
    private final static int SIZE = 1000;
    private final RegularHexagon hexagon;
    private final AffineTransform trans = AffineTransform.getTranslateInstance(SIZE / 2.0, SIZE / 2.0);

    private TestRegularHexagon() {
        hexagon = new RegularHexagon();
        final AffineTransform trans = AffineTransform.getTranslateInstance(SIZE / 2.0, SIZE / 2.0);
        trans.scale(250, 250);
        hexagon.transform(trans);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Point p = e.getPoint();
                if (hexagon.contains(p))
                    System.out.println("Inside hexagon");
                else
                    System.out.println("Outside hexagon");
            }
        });
        setSize(new Dimension(SIZE, SIZE));
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setMaximumSize(new Dimension(SIZE, SIZE));
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
        g2d.setStroke(new BasicStroke(5));
        g2d.draw(hexagon);
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Hexagon");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final TestRegularHexagon hexagonPanel = new TestRegularHexagon();
        frame.add(hexagonPanel);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int xPos = (screenSize.width - hexagonPanel.getWidth()) / 2;
        final int yPos = (screenSize.height - hexagonPanel.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        frame.setSize(new Dimension(SIZE, SIZE));
        frame.setMinimumSize(new Dimension(SIZE, SIZE));
        frame.setPreferredSize(new Dimension(SIZE, SIZE));
        frame.setMaximumSize(new Dimension(SIZE, SIZE));
        frame.setVisible(true);
    }
}
