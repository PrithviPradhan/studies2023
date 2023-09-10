package org.team100.lib.rrt.example.full_state_arena;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.team100.lib.example.Arena;
import org.team100.lib.geom.Obstacle;
import org.team100.lib.graph.LinkInterface;
import org.team100.lib.graph.Node;
import org.team100.lib.index.KDNode;
import org.team100.lib.index.KDTree;
import org.team100.lib.planner.Runner;
import org.team100.lib.planner.Solver;
import org.team100.lib.rrt.RRTStar7;
import org.team100.lib.space.Path;
import org.team100.lib.space.Sample;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N4;

/**
 * Four-dimensional model, plot spatial dimensions which are indices 0 and 2.
 * 
 * TODO: make both x/y and xdot/ydot pics
 */
public class FullStateArenaView extends JComponent {
    private static final boolean DEBUG = false;

    private final Runner<N4> _rrtStar;
    private final Arena<N4> _robotModel;
    private int framecounter;

    private Image _backgroundImage;

    private final NumberFormat _integerFormat = NumberFormat.getIntegerInstance();

    private KDNode<Node<N4>> _T_a;
    private KDNode<Node<N4>> _T_b;

    public FullStateArenaView(Arena<N4> arena, Runner<N4> rrtStar, KDNode<Node<N4>> T_a, KDNode<Node<N4>> T_b) {
        _rrtStar = rrtStar;
        _robotModel = arena;
        _T_a = T_a;
        _T_b = T_b;
    }



    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        final FullStateHolonomicArena arena = new FullStateHolonomicArena();
        KDNode<Node<N4>> T_a = new KDNode<>(new Node<>(arena.initial()));
        KDNode<Node<N4>> T_b = new KDNode<>(new Node<>(arena.goal()));
        //final Solver<N4> solver = new RRTStar6<>(arena, new Sample<>(arena), 3, T_a, T_b);
        final RRTStar7<FullStateHolonomicArena> solver = new RRTStar7<>(arena, new Sample<>(arena), 3, T_a, T_b);
        solver.setRadius(3); // hoo boy
        final Runner<N4> runner = new Runner<>(solver);
        final FullStateArenaView view = new FullStateArenaView(arena, runner, T_a, T_b);


        // final JFrame frame = new FullStateArenaFrame(view);
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view);


        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.setSize(1600, 800);
                frame.setVisible(true);
                frame.repaint();
            }
        });

        //runner.runForDurationMS(100000);
        runner.runSamples(1000);

        Path<N4> bestPath = runner.getBestPath();
        if (bestPath == null) {
            System.out.println("failed to find path");
        } else {
            System.out.println("found path");
            System.out.println(bestPath);
        }
        System.out.println("done");
        frame.repaint();
        view.repaint();

    }






    @Override
    protected void paintComponent(Graphics graphics) {
        doPaint((Graphics2D) graphics, getSize());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    public void doPaint(Graphics2D g, Dimension size) {
        if (DEBUG)
            System.out.println("doPaint");
        Arena<N4> robotModel = _robotModel;
        Matrix<N4,N1> min = robotModel.getMin();
        Matrix<N4,N1> max = robotModel.getMax();

        Path<N4> bestPath = _rrtStar.getBestPath();

        framecounter += 1;
        if (framecounter > 100) {
            framecounter = 0;
            createBGImage(min, max, size, bestPath);
        }

        g.drawImage(_backgroundImage, 0, 0, null);

        AffineTransform savedTransform = g.getTransform();

        g.setTransform(savedTransform);

        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String count = _integerFormat.format(_rrtStar.getStepNo());
        g.drawString(count, 4, 4 + fm.getAscent());
        g.setColor(Color.BLACK);
        g.drawString(count, 3, 3 + fm.getAscent());
    }

    /** min and max are (x xdot y ydot) */
    private void createBGImage(Matrix<N4,N1> min, Matrix<N4,N1> max, Dimension size, Path<N4> link) {
        _backgroundImage = createImage(size.width, size.height);

        Graphics2D g = (Graphics2D) _backgroundImage.getGraphics();
        AffineTransform savedTransform = g.getTransform();

        double scale = setupGraphics(min, max, size, g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size.width, size.height);

        // obstacles
        g.setStroke(new BasicStroke(0f));
        for (Obstacle obstacle : _robotModel.obstacles()) {
            g.setColor(obstacle.color());
            g.fill(obstacle.shape());
        }

        renderRRTTree(g, scale);

        renderPaths(link, g, scale);

        g.setTransform(savedTransform);
        g.dispose();
    }

    private static final boolean renderTree = true;

    public void renderRRTTree(Graphics2D g, double scale) {
        if (!renderTree)
            return;
        if (DEBUG)
            System.out.println("renderRRTTree");
        Line2D.Double line = new Line2D.Double();
        g.setStroke(new BasicStroke((float)(1.0/scale)));
        for (Node<N4> node : KDTree.values(_T_a)) {
            LinkInterface<N4> incoming = node.getIncoming();
            if (incoming != null) {
                Node<N4> parent = incoming.get_source();
                Matrix<N4,N1> n = node.getState();
                Matrix<N4,N1> p = parent.getState();
                if (DEBUG)
                    System.out.printf("A node %s parent %s\n"
                            + n.toString(), p.toString());
                g.setColor(Color.GREEN);
                line.setLine(n.get(0,0), n.get(2,0), p.get(0,0), p.get(2,0));
                g.draw(line);
            }
        }
        for (Node<N4> node : KDTree.values(_T_b)) {
            LinkInterface<N4> incoming = node.getIncoming();
            if (incoming != null) {
                Node<N4> parent = incoming.get_source();
                Matrix<N4,N1> n = node.getState();
                Matrix<N4,N1> p = parent.getState();
                if (DEBUG)
                    System.out.printf("B node %s parent %s\n"
                            + n.toString(), p.toString());
                g.setColor(Color.RED);
                line.setLine(n.get(0,0), n.get(2,0), p.get(0,0), p.get(2,0));
                g.draw(line);
            }
        }
    }

    private void renderPaths(Path<N4> path, Graphics2D g, double scale) {
        if (path == null) {
            return;
        }

        Line2D.Double line = new Line2D.Double();
        g.setStroke(new BasicStroke((float)(5.0/scale)));

        List<Matrix<N4,N1>> statesA = path.getStatesA();
        if (statesA.size() > 1) {
            Iterator<Matrix<N4,N1>> pathIter = statesA.iterator();
            Matrix<N4,N1> prev = pathIter.next();
            while (pathIter.hasNext()) {
                Matrix<N4,N1>  curr = pathIter.next();
                g.setColor(Color.GREEN);
                line.setLine(prev.get(0,0), prev.get(2,0), curr.get(0,0), curr.get(2,0));
                g.draw(line);
                prev = curr;
            }
        }
        List<Matrix<N4,N1>> statesB = path.getStatesB();
        if (statesB.size() > 1) {
            Iterator<Matrix<N4,N1>> pathIter = statesB.iterator();
            Matrix<N4,N1> prev = pathIter.next();
            while (pathIter.hasNext()) {
                Matrix<N4,N1> curr = pathIter.next();
                g.setColor(Color.RED);
                line.setLine(prev.get(0,0), prev.get(2,0), curr.get(0,0), curr.get(2,0));
                g.draw(line);
                prev = curr;
            }
        }

        Matrix<N4,N1> nA = statesA.get(statesA.size() - 1);
        Matrix<N4,N1> nB = statesB.get(0);
        if (nA != null && nB != null) {
            g.setColor(Color.BLACK);
            line.setLine(nA.get(0,0), nA.get(2,0), nB.get(0,0), nB.get(2,0));
            g.draw(line);
        } else {
            System.out.println("NULLS");
        }

    }

    /** min and max are (x xdot y ydot) */
    private double setupGraphics(Matrix<N4,N1> min, Matrix<N4,N1> max, Dimension size, Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.translate(min.get(0,0), min.get(2,0));
        double scale = Math.min(
                size.width / (max.get(0,0) - min.get(0,0)),
                size.height / (max.get(2,0) - min.get(2,0)));
        g.scale(scale, scale);
       // g.setStroke(new BasicStroke((float) (2 / scale)));
        return scale;
    }
}
