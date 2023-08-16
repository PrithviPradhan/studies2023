package edu.unc.robotics.prrts.tree;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a single configuration in the RRT* tree. The path to the
 * node can be computed by following the parents until null, and then
 * reversing the order.
 *
 * The public API may safely be accessed while the PRRTStar is running.
 * There is a possibility that the path to a node will change while it
 * is being accessed, but the config member will not change. For
 * efficiency, the config member is exposed as a direct reference an array.
 * It should NOT be modified by the caller.
 */
public class Node {
    private final double[] _config;
    private final boolean _inGoal;
    /** the parent.  maybe call it the parent? */
    private final AtomicReference<Link> _link;

    public Node(double[] config, boolean inGoal) {
        _config = config;
        _inGoal = inGoal;
        _link = new AtomicReference<>(new Link(this));
    }

    /**
     * Create a new node and 
     * @param config state of this node
     * @param inGoal true if the state is within the goal
     * @param linkDist distance to the parent?
     * @param parent link pointing to this node (node is head of the link)
     */
    public Node(double[] config, boolean inGoal, double linkDist, Link parent) {
        _config = config;
        _inGoal = inGoal;
        // link from parent to this node
        Link link = new Link(this, linkDist, parent);
        _link = new AtomicReference<>(link);
        parent.addChild(link);
    }

    public Link setLink(Link oldLink, double linkDist, Link parent) {
        Link newLink = new Link(this, linkDist, parent);
        if (!_link.compareAndSet(oldLink, newLink)) {
            return null;
        }
        assert newLink.get_pathDist() <= oldLink.get_pathDist();
        parent.addChild(newLink);
        return newLink;
    }

    public double[] get_config() {
        return _config;
    }

    public boolean is_inGoal() {
        return _inGoal;
    }

    public AtomicReference<Link> get_link() {
        return _link;
    }
}
